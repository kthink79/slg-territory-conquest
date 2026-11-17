package com.game.slg

import com.game.slg.core.engine.FacilityEngine
import com.game.slg.core.engine.ModernBattleEngine
import com.game.slg.core.model.CombatArm
import com.game.slg.core.model.FacilityType
import com.game.slg.core.model.ForceComposition
import com.game.slg.core.model.Fortress
import com.game.slg.core.model.FortressLevel
import com.game.slg.core.model.TerrainType
import com.game.slg.core.model.Territory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * 현대 병과 편제 · 전투 엔진 · 시설 엔진 테스트.
 */
class ModernForceTest {

    private val battle = ModernBattleEngine()
    private val facilities = FacilityEngine()
    private val engine = FacilityEngine()

    private fun territory(
        terrain: TerrainType = TerrainType.PLAIN,
        fortress: Fortress? = null
    ) = Territory(
        code = "4420033000",
        name = "탕정면",
        province = "충청남도",
        cityCountyDistrict = "아산시",
        population = 48604,
        maxLords = 48,
        terrain = terrain,
        fortress = fortress
    )

    // ── 병과 ──────────────────────────────────────────────────

    @Test
    fun `현대 병과는 실제 편제와 장비를 쓴다`() {
        val names = CombatArm.entries.map { it.displayName }
        listOf("기동보병", "기계화 기갑", "화력지원", "드론 정찰전대", "방공 포대", "특수작전팀", "물류·수송대")
            .forEach { assertTrue("$it 누락", names.contains(it)) }

        assertTrue(CombatArm.INFANTRY.weapon.contains("K2 소총"))
        assertTrue(CombatArm.ARMOR.weapon.contains("K2 전차"))
        assertTrue(CombatArm.ARTILLERY.weapon.contains("K9"))
        assertTrue(CombatArm.DRONE.weapon.contains("드론"))
        assertTrue(CombatArm.AIR_DEFENSE.weapon.contains("지대공"))

        // 봉건 병과 잔재가 없어야 한다
        val text = CombatArm.entries.joinToString { it.displayName + it.weapon + it.role + it.icon }
        listOf("창병", "궁병", "기사", "투석기", "성벽").forEach {
            assertFalse("봉건 병과 잔재: $it", text.contains(it))
        }
    }

    @Test
    fun `기본 편제는 병력 수를 보병 기갑 드론 화력으로 나눈다`() {
        val force = ForceComposition.standard(1000)

        assertEquals(600, force.troopsOf(CombatArm.INFANTRY))
        assertEquals(200, force.troopsOf(CombatArm.ARMOR))
        assertEquals(100, force.troopsOf(CombatArm.DRONE))
        assertEquals(100, force.troopsOf(CombatArm.ARTILLERY))
        assertTrue(abs(force.totalTroops - 1000) <= 3)
        assertTrue(force.describe().contains("기동보병"))
    }

    @Test
    fun `지형에 따라 병과 전투력이 달라진다`() {
        val armor = ForceComposition.of(CombatArm.ARMOR, 300)
        val drone = ForceComposition.of(CombatArm.DRONE, 300)

        assertTrue("기갑은 평야에서 강하다", armor.attackPower(TerrainType.PLAIN) > drone.attackPower(TerrainType.PLAIN))
        assertTrue("드론은 산악에서 강하다", drone.attackPower(TerrainType.MOUNTAIN) > armor.attackPower(TerrainType.MOUNTAIN))
        assertTrue("기갑은 산악에서 약해진다", armor.attackPower(TerrainType.MOUNTAIN) < armor.attackPower(TerrainType.PLAIN))
        assertTrue("방공은 해안에서 강하다", CombatArm.AIR_DEFENSE.terrainFactor(TerrainType.COASTAL) > 1.0)
    }

    // ── 전투 ──────────────────────────────────────────────────

    @Test
    fun `방호 시설이 있으면 사상자가 크게 줄어든다`() {
        val attackers = ForceComposition.standard(400)
        val defenders = ForceComposition.standard(300)

        val without = battle.resolve(attackers, defenders, territory(fortress = null))
        val withBunker = battle.resolve(
            attackers,
            defenders,
            territory(
                fortress = Fortress(FortressLevel.UNDERGROUND_BUNKER, 1000, 1000, 300)
            )
        )

        assertTrue("벙커가 있으면 방어에 유리하다", withBunker.defenderPower > without.defenderPower)
        assertTrue("벙커가 있으면 사상자가 적다", withBunker.defenderLosses < without.defenderLosses)
        assertTrue("방어 성공", withBunker.defenderWon)
    }

    @Test
    fun `모든 방호 시설 등급의 사상자 감소율이 유지된다`() {
        assertEquals(0.40, FortressLevel.CIVIL_FENCE.casualtyReduction, 0.001)
        assertEquals(0.65, FortressLevel.CONCRETE_BARRIER.casualtyReduction, 0.001)
        assertEquals(0.80, FortressLevel.UNDERGROUND_BUNKER.casualtyReduction, 0.001)
        assertEquals(0.85, FortressLevel.INTEGRATED_NET.casualtyReduction, 0.001)
        assertEquals("민방위 방호 펜스", FortressLevel.CIVIL_FENCE.displayName)
        assertTrue(FortressLevel.INTEGRATED_NET.displayName.contains("통합 방호망"))
    }

    @Test
    fun `보급선이 끊긴 공격군은 무혈 항복한다`() {
        val result = battle.resolve(
            attackers = ForceComposition.standard(500),
            defenders = ForceComposition.standard(200),
            territory = territory(),
            isSupplyLineCut = true
        )

        assertTrue(result.defenderWon)
        assertTrue(result.bloodlessVictory)
        assertEquals(0, result.defenderLosses)
        assertEquals(200, result.attackerLosses)
        assertTrue(result.message.contains("무혈 항복"))
    }

    @Test
    fun `시설 방어 보너스가 전투에 반영된다`() {
        val attackers = ForceComposition.standard(400)
        val defenders = ForceComposition.standard(300)
        val plain = battle.resolve(attackers, defenders, territory())
        val fortified = battle.resolve(
            attackers, defenders, territory(),
            defenseBonusRatio = engine.defenseBonus(setOf(FacilityType.RADAR_NETWORK, FacilityType.SMART_DEFENSE_NET)),
            casualtyReductionBonus = engine.casualtyReduction(setOf(FacilityType.SMART_DEFENSE_NET))
        )

        assertTrue(fortified.defenderPower > plain.defenderPower)
        assertTrue(fortified.defenderLosses <= plain.defenderLosses)
    }

    @Test
    fun `병력이 없는 쪽은 전투 없이 정리된다`() {
        val noAttack = battle.resolve(ForceComposition(emptyList()), ForceComposition.standard(100), territory())
        assertTrue(noAttack.defenderWon)
        assertEquals(0, noAttack.attackerLosses)

        val noDefense = battle.resolve(ForceComposition.standard(100), ForceComposition(emptyList()), territory())
        assertFalse(noDefense.defenderWon)
        assertEquals(0, noDefense.defenderLosses)
    }

    @Test
    fun `현대전은 저손실 규칙 안에서 움직인다`() {
        val result = battle.resolve(
            attackers = ForceComposition.standard(300),
            defenders = ForceComposition.standard(300),
            territory = territory(
                terrain = TerrainType.MOUNTAIN,
                fortress = Fortress(FortressLevel.CONCRETE_BARRIER, 800, 1000, 300)
            )
        )

        assertTrue("사상자는 병력을 넘지 않는다", result.defenderLosses <= 300)
        assertTrue("공격군 사상자도 병력을 넘지 않는다", result.attackerLosses <= 300)
        assertTrue(result.attackerPower > 0)
        assertTrue(result.defenderPower > 0)
    }

    // ── 시설 ──────────────────────────────────────────────────

    @Test
    fun `시설은 자금이 있어야 짓고 두 번 지을 수 없다`() {
        assertTrue(facilities.validate(FacilityType.COMMAND_CENTER, 5000, emptySet()).canBuild)
        assertFalse(facilities.validate(FacilityType.COMMAND_CENTER, 100, emptySet()).canBuild)
        assertFalse(
            facilities.validate(FacilityType.COMMAND_CENTER, 5000, setOf(FacilityType.COMMAND_CENTER)).canBuild
        )
        assertEquals(0, facilities.cost(FacilityType.COMMAND_CENTER, setOf(FacilityType.COMMAND_CENTER)))
    }

    @Test
    fun `시설 효과가 종류별로 누적된다`() {
        val owned = setOf(
            FacilityType.RADAR_NETWORK,
            FacilityType.SMART_DEFENSE_NET,
            FacilityType.CYBER_INTEL_OFFICE,
            FacilityType.LOGISTICS_HUB,
            FacilityType.COMMAND_CENTER,
            FacilityType.CIVIL_DEFENSE_CENTER
        )

        assertEquals(0.18, facilities.defenseBonus(owned), 0.001)
        assertEquals(0.08, facilities.casualtyReduction(owned), 0.001)
        assertEquals(15, facilities.intelBonus(owned))
        assertEquals(0.12, facilities.economyBonus(owned), 0.001)
        assertEquals(6, facilities.moraleBonus(owned))
        assertEquals(0.10, facilities.xpBonus(owned), 0.001)
        assertEquals(0.0, facilities.healRatio(owned), 0.001)
    }

    @Test
    fun `드론 격납고는 드론 병력과 전투력을 보강한다`() {
        val base = ForceComposition.standard(1000)
        val reinforced = engine.reinforced(base, setOf(FacilityType.DRONE_HANGAR))

        assertTrue(reinforced.troopsOf(CombatArm.DRONE) > base.troopsOf(CombatArm.DRONE))
        assertEquals(115, reinforced.troopsOf(CombatArm.DRONE))
        assertTrue(reinforced.attackPower() > base.attackPower())
        assertEquals(base, engine.reinforced(base, emptySet()))
    }

    @Test
    fun `시설 이름과 효과 문구는 현대 인프라 용어다`() {
        val names = FacilityType.entries.map { it.displayName }
        listOf("지휘통제실", "드론 격납고", "방공 레이더망", "물류 허브", "스마트 방호망", "통신·사이버 정보국")
            .forEach { assertTrue("$it 누락", names.contains(it)) }

        val text = FacilityType.entries.joinToString { it.displayName + it.effectLabel + it.description }
        listOf("성", "요새", "목책", "장원").forEach {
            assertFalse("봉건 시설 잔재: $it", text.contains(it))
        }
    }
}
