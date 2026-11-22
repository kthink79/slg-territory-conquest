package com.game.slg

import com.game.slg.core.empire.AbsorptionEngine
import com.game.slg.core.empire.AbsorptionMethod
import com.game.slg.core.empire.EmpireContext
import com.game.slg.core.empire.Faction
import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.hero.Hero
import com.game.slg.core.hero.HeroDispositionEngine
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.hero.HeroStage
import com.game.slg.core.save.InMemoryCampaignStore
import com.game.slg.ui.SlgGameState
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 동일 지역 교전 0병력 강제 편입(A안) · 소속 영웅 등용·방출 처분 루틴 — 2026-11-22 신규 시스템.
 *
 * · 교전으로 상대 병력을 0 으로 만들면 추가 12% 손실 없이 직할로 강제 편입된다.
 * · 편입한 세력의 소속 영웅(2~4명)은 처분 대기로 넘어가 등용하거나 방출한다.
 * · 적 리더 병력·편입 상태·처분 대기는 저장되어 재시작해도 되돌아가지 않는다.
 */
class ForcedAnnexationAndDispositionTest {

    private fun ctx(troops: Int = 2_000) = EmpireContext(
        troops = troops,
        prestige = 60,
        gold = 5_000,
        food = 5_000,
        morale = 60,
        diplomacy = 55,
        espionagePower = 60
    )

    private fun faction(troops: Int = 500, heroIds: List<String> = emptyList()) = Faction(
        id = "faction_ai_test_1",
        name = "김도현",
        territoryCode = "test_1",
        territoryName = "탕정 1구역",
        population = 12_000,
        troops = troops,
        morale = 55,
        affinity = 40,
        prestige = 50,
        gold = 900,
        food = 1_500,
        heroIds = heroIds
    )

    /** 보호 기간(7턴)이 끝난 플레이 상태 */
    private fun playingVm(store: InMemoryCampaignStore = InMemoryCampaignStore()): SlgGameViewModel {
        val vm = SlgGameViewModel(store = store)
        vm.chooseLord(vm.uiState.value.lords.first().id)
        repeat(7) { vm.advanceSeason() }
        return vm
    }

    /** 병력이 가장 적은 리더를 0병력까지 몰아붙인다 — 마지막 교전의 전후 상태를 돌려준다 */
    private fun annexWeakestLord(vm: SlgGameViewModel): Triple<String, SlgGameState, SlgGameState> {
        val target = vm.uiState.value.lords
            .filterNot { it.isPlayer }
            .minByOrNull { it.troops }!!
        var before = vm.uiState.value
        var after = before
        var guard = 0
        while (guard < 25 && !after.lords.first { it.id == target.id }.incorporated) {
            before = vm.uiState.value
            vm.challengeRegionLord(target.id)
            after = vm.uiState.value
            guard += 1
        }
        assertTrue("25회 교전 안에 0병력 편입이 일어나야 한다", after.lords.first { it.id == target.id }.incorporated)
        return Triple(target.id, before, after)
    }

    // ── 강제 편입 ─────────────────────────────────────────────

    @Test
    fun `강제 편입은 추가 12퍼센트 손실 없이 직할 영지를 만든다`() {
        val heroes = HeroDispositionEngine.heroesForLord("ai_test_1")
        val outcome = AbsorptionEngine.forceConquest(faction(troops = 0, heroIds = heroes), ctx(troops = 100))

        assertTrue(outcome.success)
        assertTrue(outcome.absorbed)
        assertEquals(0, outcome.troopLoss)
        assertEquals(TerritoryStatus.DIRECT, outcome.territory?.status)
        assertEquals(0, outcome.territory?.troops)
        assertEquals(0, outcome.faction.troops)
        assertTrue(outcome.faction.incorporated)
        assertEquals(6, outcome.prestigeDelta)
        assertEquals(heroes, outcome.capturedHeroes)
        assertTrue(outcome.message.contains("강제 편입"))
    }

    @Test
    fun `이미 편입된 세력은 다시 강제 편입되지 않는다`() {
        val outcome = AbsorptionEngine.forceConquest(faction(troops = 0).copy(incorporated = true), ctx())
        assertFalse(outcome.success)
        assertTrue(outcome.message.contains("이미"))
    }

    @Test
    fun `교전으로 상대 병력을 0으로 만들면 그 자리에서 강제 편입된다`() {
        val vm = playingVm()
        val (lordId, before, after) = annexWeakestLord(vm)

        val lord = after.lords.first { it.id == lordId }
        val factionAfter = after.factions.first { it.id == "faction_$lordId" }

        assertTrue(lord.incorporated)
        assertEquals(0, lord.troops)
        assertTrue(factionAfter.incorporated)
        assertEquals(0, factionAfter.troops)
        assertTrue(after.statusMessage.contains("강제 편입"))
        assertTrue(
            "편입 영지가 직할로 추가된다",
            after.territories.any { it.code == factionAfter.territoryCode && it.status == TerritoryStatus.DIRECT }
        )
        // 교전 손실만 반영되고 편입 12% 가 이중으로 깎이지 않는다
        assertEquals(before.troops - after.lastReport!!.defenderLosses, after.troops)
        val factionBefore = before.factions.first { it.id == "faction_$lordId" }
        assertEquals(before.gold + factionBefore.gold / 2, after.gold)
    }

    @Test
    fun `편입된 리더는 교전 목록에서 잠긴다`() {
        val vm = playingVm()
        val (lordId, _, after) = annexWeakestLord(vm)
        val troopsBefore = after.troops

        vm.challengeRegionLord(lordId)

        val state = vm.uiState.value
        assertTrue(state.statusMessage.contains("이미 우리 연방에 편입"))
        assertEquals(troopsBefore, state.troops)
    }

    @Test
    fun `교전으로 깎은 적 병력은 흡수 화면 숫자에도 반영된다`() {
        val vm = playingVm()
        val target = vm.uiState.value.lords.filterNot { it.isPlayer }.maxByOrNull { it.troops }!!

        vm.challengeRegionLord(target.id)

        val state = vm.uiState.value
        val lordTroops = state.lords.first { it.id == target.id }.troops
        val factionTroops = state.factions.first { it.id == "faction_${target.id}" }.troops
        assertEquals(lordTroops, factionTroops)
        assertTrue("교전으로 상대 병력이 줄어야 한다", lordTroops < target.troops)
    }

    // ── 저장 · 복원 ───────────────────────────────────────────

    @Test
    fun `적 리더 병력과 편입 상태가 저장되고 복원된다`() {
        val store = InMemoryCampaignStore()
        val vm = playingVm(store)
        val (lordId, _, after) = annexWeakestLord(vm)
        val annexedFaction = after.factions.first { it.id == "faction_$lordId" }

        val restored = SlgGameViewModel(store = store).uiState.value
        val restoredLord = restored.lords.first { it.id == lordId }
        val restoredFaction = restored.factions.first { it.id == "faction_$lordId" }

        assertTrue(restoredLord.incorporated)
        assertEquals(0, restoredLord.troops)
        assertTrue(restoredFaction.incorporated)
        assertEquals(annexedFaction.troops, restoredFaction.troops)
        assertTrue(restored.territories.any { it.code == annexedFaction.territoryCode })
    }

    @Test
    fun `교전으로 줄인 적 병력도 재시작 후 그대로다`() {
        val store = InMemoryCampaignStore()
        val vm = playingVm(store)
        val target = vm.uiState.value.lords
            .filterNot { it.isPlayer }
            .maxByOrNull { it.troops }!!

        vm.challengeRegionLord(target.id)
        val damaged = vm.uiState.value.lords.first { it.id == target.id }.troops
        assertTrue("교전으로 병력이 줄어야 한다", damaged < target.troops)

        val restored = SlgGameViewModel(store = store).uiState.value
        assertEquals(damaged, restored.lords.first { it.id == target.id }.troops)
        assertEquals(damaged, restored.factions.first { it.id == "faction_${target.id}" }.troops)
    }

    // ── 소속 영웅 배정 ────────────────────────────────────────

    @Test
    fun `리더마다 소속 영웅이 2에서 4명씩 결정적으로 배정된다`() {
        val lords = SlgGameViewModel().uiState.value.lords.map { it.id }
        val assigned = mutableSetOf<String>()
        val seen = mutableSetOf<String>()
        lords.forEach { lordId ->
            val excluded = assigned.toSet()
            val heroes = HeroDispositionEngine.heroesForLord(lordId, excluded)
            assigned += heroes
            assertTrue("$lordId → ${heroes.size}명", heroes.size in 2..4)
            assertEquals("같은 리더는 항상 같은 소속", heroes, HeroDispositionEngine.heroesForLord(lordId, excluded))
            assertEquals("같은 리더는 매번 같은 이름", heroes, HeroDispositionEngine.heroesForLord(lordId, excluded))
            assertTrue(heroes.none { it in HeroRosterState.STARTING_IDS })
            heroes.forEach { id ->
                assertNotNull(HeroRoster.byId(id))
                assertTrue("소속이 겹치면 안 된다: $id", seen.add(id))
            }
        }
    }

    @Test
    fun `편입한 세력의 소속 영웅은 처분 대기 목록으로 넘어온다`() {
        val vm = playingVm()
        val (lordId, _, after) = annexWeakestLord(vm)
        val faction = after.factions.first { it.id == "faction_$lordId" }

        assertEquals(faction.heroIds.size, after.pendingDisposition.size)
        after.pendingDisposition.forEach { entry ->
            assertEquals(lordId, entry.lordId)
            assertTrue(faction.heroIds.contains(entry.heroId))
            assertEquals(lordId, after.heroes.recordOf(entry.heroId).allegianceLordId)
        }
    }

    // ── 등용 · 방출 ───────────────────────────────────────────

    @Test
    fun `등용 비용은 등급 기준가에서 20퍼센트 할인이다`() {
        val hero = HeroRoster.byId("hero_010")!!
        assertEquals(
            hero.grade.hireBase * 80 / 100,
            HeroDispositionEngine.recruitCost(hero)
        )
    }

    @Test
    fun `등용하면 자금을 내고 빈 슬롯에 자동 배치된다`() {
        val heroId = "hero_010"
        val hero = HeroRoster.byId(heroId)!!
        val roster = HeroRosterState.initial() // 빈 슬롯이 있는 기본 상태
        val cost = HeroDispositionEngine.recruitCost(hero)

        val broke = HeroDispositionEngine.recruit(roster, heroId, cost - 1)
        assertFalse(broke.success)
        assertTrue(broke.message.contains("자금이 부족"))

        val result = HeroDispositionEngine.recruit(roster, heroId, cost)
        assertTrue(result.success)
        assertEquals(-cost, result.goldDelta)
        assertEquals(HeroStage.HIRED, result.roster.recordOf(heroId).stage)
        assertEquals(hero.role, result.roster.recordOf(heroId).assignedRole)
        assertNull(result.roster.recordOf(heroId).allegianceLordId)
    }

    @Test
    fun `방출하면 재야가 되고 호감도 15 명성 1이 깎인다`() {
        val heroId = "hero_012"
        val roster = HeroDispositionEngine.markCaptured(
            HeroRosterState(),
            listOf(com.game.slg.core.hero.PendingDisposition(heroId, "ai_x_1", "김도현"))
        )
        val captured = roster.recordOf(heroId)
        assertEquals(HeroDispositionEngine.CAPTURED_AFFINITY, captured.affinity)

        val result = HeroDispositionEngine.release(roster, heroId)
        assertTrue(result.success)
        assertEquals(HeroStage.SCOUTED, result.roster.recordOf(heroId).stage)
        assertEquals(captured.affinity - 15, result.roster.recordOf(heroId).affinity)
        assertEquals(-1, result.prestigeDelta)
        assertNull(result.roster.recordOf(heroId).allegianceLordId)
        assertFalse(result.roster.recordOf(heroId).owned)
    }

    @Test
    fun `처분 대기 영웅을 화면에서 등용하고 방출할 수 있다`() {
        val vm = playingVm()
        annexWeakestLord(vm)
        val pending = vm.uiState.value.pendingDisposition
        assertTrue(pending.isNotEmpty())

        // 방출 — 자금 없이 가능
        val toRelease = pending.first().heroId
        val prestigeBefore = vm.uiState.value.prestige
        vm.releasePendingHero(toRelease)

        val afterRelease = vm.uiState.value
        assertEquals(pending.size - 1, afterRelease.pendingDisposition.size)
        assertEquals(HeroStage.SCOUTED, afterRelease.heroes.recordOf(toRelease).stage)
        assertEquals(prestigeBefore - 1, afterRelease.prestige)

        // 등용 — 자금이 모자라면 그대로 남는다
        val toRecruit = vm.uiState.value.pendingDisposition.firstOrNull() ?: return
        val hero = HeroRoster.byId(toRecruit.heroId)!!
        val cost = HeroDispositionEngine.recruitCost(hero)
        while (vm.uiState.value.gold < cost) {
            val before = vm.uiState.value.gold
            vm.boostResource("gold_levy")
            if (vm.uiState.value.gold <= before) break
        }
        vm.recruitPendingHero(toRecruit.heroId)

        val afterRecruit = vm.uiState.value
        assertEquals(HeroStage.HIRED, afterRecruit.heroes.recordOf(toRecruit.heroId).stage)
        assertTrue(afterRecruit.pendingDisposition.none { it.heroId == toRecruit.heroId })
        assertNull(afterRecruit.heroes.recordOf(toRecruit.heroId).allegianceLordId)
    }

    @Test
    fun `처분하지 않은 영웅은 다음 절기에 자동 방출된다`() {
        val vm = playingVm()
        annexWeakestLord(vm)
        val pending = vm.uiState.value.pendingDisposition
        assertTrue(pending.isNotEmpty())

        vm.advanceSeason()

        val state = vm.uiState.value
        assertTrue(state.pendingDisposition.isEmpty())
        pending.forEach { entry ->
            assertEquals(HeroStage.SCOUTED, state.heroes.recordOf(entry.heroId).stage)
            assertNull(state.heroes.recordOf(entry.heroId).allegianceLordId)
            assertFalse(state.heroes.recordOf(entry.heroId).owned)
        }
        assertTrue(state.statusMessage.contains("자동") || state.statusMessage.contains("재야"))
    }

    @Test
    fun `처분 대기는 재시작 후에도 남는다`() {
        val store = InMemoryCampaignStore()
        val vm = playingVm(store)
        annexWeakestLord(vm)
        val pending = vm.uiState.value.pendingDisposition

        val restored = SlgGameViewModel(store = store).uiState.value
        assertEquals(pending.map { it.heroId }, restored.pendingDisposition.map { it.heroId })
        assertEquals(pending.map { it.lordId }, restored.pendingDisposition.map { it.lordId })
    }

    // ── 내 영웅 방출 ──────────────────────────────────────────

    @Test
    fun `내 영웅 방출은 로스터에서 빼고 태수 연결을 해제한다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val hero = vm.uiState.value.heroes.ownedHeroes.first()
        vm.assignViceroy(hero.id)
        assertTrue(vm.uiState.value.territories.any { it.viceroyHeroId == hero.id })

        val prestigeBefore = vm.uiState.value.prestige
        vm.dismissHero(hero.id)

        val state = vm.uiState.value
        assertFalse(state.heroes.recordOf(hero.id).owned)
        assertEquals(HeroStage.SCOUTED, state.heroes.recordOf(hero.id).stage)
        assertNull(state.heroes.recordOf(hero.id).assignedRole)
        assertTrue(state.territories.none { it.viceroyHeroId == hero.id })
        assertEquals(prestigeBefore - 1, state.prestige)
    }

    @Test
    fun `방출한 영웅은 저장되고 복원된다`() {
        val store = InMemoryCampaignStore()
        val vm = SlgGameViewModel(store = store)
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val hero = vm.uiState.value.heroes.ownedHeroes.first()
        vm.dismissHero(hero.id)

        val restored = SlgGameViewModel(store = store).uiState.value
        assertFalse(restored.heroes.recordOf(hero.id).owned)
        assertEquals(HeroStage.SCOUTED, restored.heroes.recordOf(hero.id).stage)
    }
}

