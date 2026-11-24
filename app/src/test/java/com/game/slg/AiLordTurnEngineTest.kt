package com.game.slg

import com.game.slg.core.model.CombatArm
import com.game.slg.core.model.Season
import com.game.slg.core.region.AiLordProfile
import com.game.slg.core.region.AiLordTurnEngine
import com.game.slg.core.region.AiSeasonOrder
import com.game.slg.core.region.LeaderBond
import com.game.slg.core.region.LordTrait
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AI 리더 능동 활동 엔진 테스트.
 *
 * 사용자 요청(2026-11-24): *"AI 리더들이 성장을 하거나, 병력을 키우거나 주변 세력과 전투를 하는 등
 * 나름대로 세력을 넓히려는 활동을 하게 해줘"*
 */
class AiLordTurnEngineTest {

    private fun lord(
        id: String,
        name: String,
        troops: Int,
        gold: Int = 600,
        grain: Int = 900,
        prestige: Int = 45,
        trait: LordTrait = LordTrait.BALANCED
    ) = AiLordProfile(
        id = id,
        name = name,
        regionCode = "4420033000",
        regionName = "탕정면",
        stronghold = "탕정면 중앙 물류단지 1구역",
        trait = trait,
        troops = troops,
        grain = grain,
        gold = gold,
        prestige = prestige,
        arm = CombatArm.INFANTRY
    )

    private fun order(turn: Int, unionActive: Boolean = true) =
        AiSeasonOrder(season = Season.SPRING, turn = turn, unionActive = unionActive, regionName = "탕정면")

    // ── 성장 ────────────────────────────────────────────────────

    @Test
    fun `절기가 지나면 AI 리더의 병력과 재화가 늘어난다`() {
        val lords = listOf(
            lord("ai_1", "김도현", troops = 120, gold = 300, grain = 400),
            lord("ai_2", "이서준", troops = 200, gold = 500, grain = 700, trait = LordTrait.LOGISTICS)
        )
        val outcome = AiLordTurnEngine.runSeason(lords, player = null, order = order(1), seed = 42L)

        assertEquals(2, outcome.lords.size)
        outcome.lords.forEach { grown ->
            val before = lords.first { it.id == grown.id }
            assertTrue("${grown.name} 병력이 줄었다", grown.troops > before.troops)
            assertTrue("${grown.name} 자금이 음수", grown.gold >= 0)
            assertTrue("${grown.name} 식량이 음수", grown.grain >= 0)
            assertTrue("${grown.name} 활동 기록이 비었다", grown.activity.isNotBlank())
        }
        assertTrue("요약 줄이 있어야 한다", outcome.lines.first().contains("[AI 동향]"))
        assertTrue(outcome.lines.first().contains("리더"))
    }

    @Test
    fun `연속 절기로 성장하면 병력이 두 배 이상이 된다`() {
        var lords = listOf(lord("ai_1", "박민재", troops = 100, gold = 400, grain = 800))
        repeat(4) { turn ->
            lords = AiLordTurnEngine.runSeason(lords, player = null, order = order(turn + 1), seed = 7L).lords
        }
        assertTrue("4절기 뒤 병력 ${lords.first().troops}", lords.first().troops >= 200)
    }

    @Test
    fun `명성으로 정해진 병력 상한을 넘지 않는다`() {
        val rich = lord("ai_1", "최하준", troops = 100, gold = 100_000, grain = 100_000, prestige = 10)
        val ceiling = AiLordTurnEngine.troopCeiling(10)
        var grown = rich
        repeat(8) { turn ->
            grown = AiLordTurnEngine.runSeason(listOf(grown), player = null, order = order(turn + 1), seed = 3L)
                .lords.first()
        }
        assertTrue("상한 ${ceiling} 초과 — ${grown.troops}", grown.troops <= ceiling)
    }

    @Test
    fun `자금과 식량이 없어도 재화가 음수로 내려가지 않는다`() {
        val starving = lord("ai_1", "정소영", troops = 400, gold = 0, grain = 0)
        val outcome = AiLordTurnEngine.runSeason(listOf(starving), player = null, order = order(2), seed = 11L)
        val after = outcome.lords.first()
        assertTrue(after.troops >= 0)
        assertTrue(after.gold >= 0)
        assertTrue(after.grain >= 0)
    }

    // ── 세력 확장: 병합·종속 ─────────────────────────────────────

    @Test
    fun `강한 리더가 약한 이웃을 병합하거나 종속시킨다`() {
        val strong = lord("ai_1", "강도현", troops = 900, gold = 2_000, grain = 3_000, prestige = 70)
        val weak = lord("ai_2", "약서준", troops = 120, gold = 300, grain = 400, prestige = 30)
        val other = lord("ai_3", "중민재", troops = 200, gold = 400, grain = 600, prestige = 40)

        val result = (1..40).map { seed ->
            AiLordTurnEngine.runSeason(
                listOf(strong, weak, other),
                player = null,
                order = order(1),
                seed = seed.toLong()
            )
        }.first { it.lords.any { lord -> lord.masterId != null } }

        val bound = result.lords.filter { it.masterId != null }
        assertEquals("절기당 확장은 1건", 1, bound.size)
        val servant = bound.first()
        val master = result.lords.first { it.id == servant.masterId }
        assertEquals("ai_1", master.id)
        assertTrue(servant.bond == LeaderBond.MERGE || servant.bond == LeaderBond.SUBORDINATION)
        assertTrue("예속 리더는 거점 병력을 유지한다", servant.troops >= 0)
        assertTrue("우두머리 병력은 상한 이하", master.troops <= AiLordTurnEngine.troopCeiling(master.prestige))
        assertTrue(result.lines.any { it.contains("병합") || it.contains("종속") })
    }

    @Test
    fun `병합된 세력은 자체 성장을 멈추고 조공도 바치지 않는다`() {
        val servant = lord("ai_2", "이서준", troops = 150).copy(
            masterId = "ai_1",
            bond = LeaderBond.MERGE,
            activity = "강도현 리더에게 병합"
        )
        val master = lord("ai_1", "강도현", troops = 600)
        val outcome = AiLordTurnEngine.runSeason(listOf(master, servant), player = null, order = order(3), seed = 5L)
        val after = outcome.lords.first { it.id == "ai_2" }
        assertEquals(servant.troops, after.troops)
        assertEquals(servant.gold, after.gold)
    }

    @Test
    fun `종속 세력은 절기마다 우두머리에게 조공을 바친다`() {
        val master = lord("ai_1", "강도현", troops = 600, gold = 1_000, prestige = 70)
        val vassal = lord("ai_2", "을서준", troops = 200, gold = 1_000).copy(
            masterId = "ai_1",
            bond = LeaderBond.SUBORDINATION
        )
        val outcome = AiLordTurnEngine.runSeason(listOf(master, vassal), player = null, order = order(4), seed = 9L)
        val masterAfter = outcome.lords.first { it.id == "ai_1" }
        val vassalAfter = outcome.lords.first { it.id == "ai_2" }
        // 같은 조건에서 종속 세력이 없을 때와 비교 — 차액이 조공이다
        val control = AiLordTurnEngine.runSeason(listOf(master), player = null, order = order(4), seed = 9L)
            .lords.first()
        assertTrue("조공만큼 우두머리 자금이 더 들어와야 한다", masterAfter.gold > control.gold)
        assertTrue("자금은 음수가 되지 않는다", vassalAfter.gold >= 0)
        assertTrue("조공 기록이 남아야 한다", outcome.lines.any { it.contains("조공") })
    }

    // ── 견제: 우리 영지 습격 ─────────────────────────────────────

    @Test
    fun `연합 기간에는 AI가 우리 영지를 습격하지 않는다`() {
        val raider = lord("ai_1", "강도현", troops = 5_000, prestige = 90)
        val player = lord("lord_player", "이광수", troops = 800, gold = 5_000, grain = 9_000)
        (1..30).forEach { seed ->
            val outcome = AiLordTurnEngine.runSeason(
                listOf(raider),
                player = player,
                order = order(seed, unionActive = true),
                seed = seed.toLong()
            )
            assertTrue("연합 중 습격 발생(seed=$seed)", outcome.raids.isEmpty())
            assertEquals(0, outcome.troopLoss)
        }
    }

    @Test
    fun `연합이 끝나면 강한 AI 리더가 외곽을 습격한다`() {
        val raider = lord("ai_1", "강도현", troops = 5_000, prestige = 90)
        val player = lord("lord_player", "이광수", troops = 1_000, gold = 5_000, grain = 9_000)
        val outcome = (1..60).map { seed ->
            AiLordTurnEngine.runSeason(
                listOf(raider),
                player = player,
                order = order(seed, unionActive = false),
                seed = seed.toLong()
            )
        }.first { it.raids.isNotEmpty() }

        val raid = outcome.raids.first()
        assertEquals(1, outcome.raids.size)
        assertTrue("습격 피해가 커야 한다", raid.troopLoss > 0 && raid.troopLoss <= player.troops / 8)
        assertTrue(raid.message.contains("습격"))
        assertTrue(outcome.lines.any { it.contains("습격") })
        assertEquals(raid.troopLoss, outcome.troopLoss)
    }

    @Test
    fun `우리 병력이 더 크면 AI는 습격하지 않는다`() {
        val weak = lord("ai_1", "약도현", troops = 100, prestige = 20)
        val player = lord("lord_player", "이광수", troops = 5_000, gold = 9_000, grain = 9_000)
        (1..20).forEach { seed ->
            val outcome = AiLordTurnEngine.runSeason(
                listOf(weak),
                player = player,
                order = order(seed, unionActive = false),
                seed = seed.toLong()
            )
            assertFalse("병력이 약한데 습격(seed=$seed)", outcome.raids.isNotEmpty())
        }
    }

    // ── 결정성 ──────────────────────────────────────────────────

    @Test
    fun `같은 시드면 같은 결과가 나온다`() {
        val lords = listOf(
            lord("ai_1", "김도현", troops = 300, prestige = 60),
            lord("ai_2", "이서준", troops = 150, prestige = 35),
            lord("ai_3", "박민재", troops = 90, prestige = 30)
        )
        val player = lord("lord_player", "이광수", troops = 1_200, gold = 6_000, grain = 9_000)
        val first = AiLordTurnEngine.runSeason(lords, player, order(5, unionActive = false), seed = 2_024L)
        val second = AiLordTurnEngine.runSeason(lords, player, order(5, unionActive = false), seed = 2_024L)

        assertEquals(first.lords.map { it.troops }, second.lords.map { it.troops })
        assertEquals(first.lords.map { it.masterId }, second.lords.map { it.masterId })
        assertEquals(first.lines, second.lines)
        assertEquals(first.troopLoss, second.troopLoss)
    }

    @Test
    fun `리더가 없으면 아무 일도 없다`() {
        val outcome = AiLordTurnEngine.runSeason(emptyList(), player = null, order = order(1))
        assertTrue(outcome.lords.isEmpty())
        assertTrue(outcome.lines.isEmpty())
        assertFalse(outcome.hasNews)
    }
}
