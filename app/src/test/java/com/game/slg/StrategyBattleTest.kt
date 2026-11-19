package com.game.slg

import com.game.slg.core.engine.BattleStrategy
import com.game.slg.core.engine.StrategyBattleEngine
import com.game.slg.core.engine.WeaponTech
import com.game.slg.core.engine.WeaponTechState
import com.game.slg.core.model.ForceComposition
import com.game.slg.core.model.TerrainType
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 전략·무기 기술 전투 — 같은 병력이라도 진형 상성과 기술 격차로 손실률이 크게 벌어지는지 본다.
 */
class StrategyBattleTest {

    private fun force(troops: Int) = ForceComposition.standard(troops)

    @Test
    fun `전략 상성이 이기면 같은 병력이라도 손실률이 크게 벌어진다`() {
        val winning = StrategyBattleEngine.resolve(
            own = force(1_000),
            enemy = force(1_000),
            terrain = TerrainType.PLAIN,
            ownStrategy = BattleStrategy.WEDGE,
            enemyStrategy = BattleStrategy.FRONTAL,
            ownTech = WeaponTechState(),
            enemyTechLevel = 0
        )
        val losing = StrategyBattleEngine.resolve(
            own = force(1_000),
            enemy = force(1_000),
            terrain = TerrainType.PLAIN,
            ownStrategy = BattleStrategy.FRONTAL,
            enemyStrategy = BattleStrategy.WEDGE,
            ownTech = WeaponTechState(),
            enemyTechLevel = 0
        )

        assertTrue("상성 우세는 이긴다", winning.ownWon)
        assertTrue("상성 열세는 진다", !losing.ownWon)
        assertTrue(
            "우세 교환비 ${winning.exchangeRatio} > 열세 ${losing.exchangeRatio}",
            winning.exchangeRatio > losing.exchangeRatio * 2
        )
        assertTrue(winning.ownLossRatio < losing.ownLossRatio / 2)
    }

    @Test
    fun `무기 기술이 앞서면 아군 손실이 줄고 적 손실이 늘어난다`() {
        val base = StrategyBattleEngine.resolve(
            own = force(1_000), enemy = force(1_000), terrain = TerrainType.PLAIN,
            ownStrategy = BattleStrategy.FRONTAL, enemyStrategy = BattleStrategy.FRONTAL,
            ownTech = WeaponTechState(), enemyTechLevel = 0
        )
        val advanced = StrategyBattleEngine.resolve(
            own = force(1_000), enemy = force(1_000), terrain = TerrainType.PLAIN,
            ownStrategy = BattleStrategy.FRONTAL, enemyStrategy = BattleStrategy.FRONTAL,
            ownTech = WeaponTechState(
                mapOf(
                    WeaponTech.SMALL_ARMS to 4,
                    WeaponTech.ARMOR to 4,
                    WeaponTech.FIREPOWER to 4,
                    WeaponTech.DRONE to 4,
                    WeaponTech.AIR_DEFENSE to 4
                )
            ),
            enemyTechLevel = 0
        )

        assertTrue(advanced.ownPower > base.ownPower)
        assertTrue(advanced.ownLosses < base.ownLosses)
        assertTrue(advanced.enemyLosses > base.enemyLosses)
        assertTrue(advanced.ownTechLevel > 0)
    }

    @Test
    fun `지형에 따라 같은 진형도 성능이 달라진다`() {
        assertTrue(
            BattleStrategy.AMBUSH.terrainFactor(TerrainType.MOUNTAIN) >
                BattleStrategy.AMBUSH.terrainFactor(TerrainType.PLAIN)
        )
        assertTrue(
            BattleStrategy.WEDGE.terrainFactor(TerrainType.PLAIN) >
                BattleStrategy.WEDGE.terrainFactor(TerrainType.MOUNTAIN)
        )
        assertTrue(
            BattleStrategy.DEFENSE.terrainFactor(TerrainType.RIVER) >
                BattleStrategy.DEFENSE.terrainFactor(TerrainType.PLAIN)
        )
    }

    @Test
    fun `방어전은 진지 보정으로 사상자가 크게 줄어든다`() {
        val defense = StrategyBattleEngine.resolve(
            own = force(1_400), enemy = force(2_170), terrain = TerrainType.PLAIN,
            ownStrategy = BattleStrategy.DEFENSE, enemyStrategy = BattleStrategy.WEDGE,
            ownTech = WeaponTechState(), enemyTechLevel = 1,
            defenseBonusRatio = 0.1, casualtyReduction = 0.08,
            fortressReduction = 0.65, durabilityFactor = 1.0, defending = true
        )

        assertTrue(defense.ownWon)
        assertTrue("방어 손실률 ${defense.ownLossRatio}", defense.ownLossRatio <= 0.25)
        assertTrue(defense.lines.any { it.contains("방어 진지") })
    }

    @Test
    fun `무기 기술 비용은 레벨이 오를수록 비싸진다`() {
        assertTrue(WeaponTech.DRONE.costOf(1) < WeaponTech.DRONE.costOf(2))
        assertTrue(WeaponTech.DRONE.costOf(2) < WeaponTech.DRONE.costOf(3))
        val state = WeaponTechState(mapOf(WeaponTech.DRONE to 1))
        assertTrue(state.multiplier > 1.0)
        assertTrue(state.summary().contains("드론"))
    }

    @Test
    fun `작전 결과 리포트에 전략·교환비·계산 근거가 담긴다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        repeat(7) { vm.advanceSeason() } // 스폰 보호 종료
        vm.selectBattleStrategy(BattleStrategy.WEDGE)
        vm.runOperation("OP_BASE")

        val report = vm.uiState.value.lastReport
        assertNotNull(report)
        assertTrue(report!!.strategyLabel.contains("vs"))
        assertTrue(report.exchangeLabel.isNotBlank())
        assertTrue(report.breakdown.any { it.contains("전략 상성") })
        assertTrue(report.breakdown.any { it.contains("무기 기술") })
    }

    @Test
    fun `전략을 바꾸면 다음 작전에 적용되고 무기 연구는 자금을 쓴다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val before = vm.uiState.value

        vm.selectBattleStrategy(BattleStrategy.AMBUSH)
        assertEquals(BattleStrategy.AMBUSH, vm.uiState.value.battleStrategy)

        vm.researchWeaponTech(WeaponTech.DRONE)
        val after = vm.uiState.value
        assertEquals(1, after.weaponTech.levelOf(WeaponTech.DRONE))
        assertTrue(after.gold < before.gold)
    }

    @Test
    fun `적 전략과 기술 레벨은 같은 규칙으로 계산된다`() {
        assertNotNull(StrategyBattleEngine.enemyStrategyFor(TerrainType.PLAIN, 1))
        assertTrue(StrategyBattleEngine.enemyTechLevel(0, 0) in 0..4)
        assertTrue(StrategyBattleEngine.enemyTechLevel(6, 2) in 0..4)
    }
}
