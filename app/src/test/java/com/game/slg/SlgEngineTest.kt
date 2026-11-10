package com.game.slg

import com.game.slg.core.engine.CombatEngine
import com.game.slg.core.engine.CouncilEngine
import com.game.slg.core.engine.InfiltrationEngine
import com.game.slg.core.model.*
import org.junit.Assert.*
import org.junit.Test

class SlgEngineTest {

    private val combatEngine = CombatEngine()
    private val councilEngine = CouncilEngine()
    private val infiltrationEngine = InfiltrationEngine()

    @Test
    fun testFortressCasualtyReduction() {
        val territory = Territory(
            code = "4413125000",
            name = "아산시 배방읍",
            province = "충남",
            cityCountyDistrict = "아산시",
            population = 85000,
            maxLords = 8,
            terrain = TerrainType.MOUNTAIN,
            fortress = Fortress(
                level = FortressLevel.MOUNTAIN_FORTRESS,
                durability = 1000,
                maxDurability = 1000,
                garrisonTroops = 1500
            )
        )

        val result = combatEngine.resolveSiege(
            attackers = 4000,
            defenders = 1500,
            territory = territory,
            isSupplyLineCut = false
        )

        assertTrue("요새와 지형 이점으로 수성군이 승리해야 함", result.defenderWon)
        assertTrue("수성군 사상자가 80% 격감되어야 함", result.defenderLosses <= 250)
    }

    @Test
    fun testSupplyLineCutBloodlessVictory() {
        val territory = Territory(
            code = "1168010100",
            name = "역삼동",
            province = "서울",
            cityCountyDistrict = "강남구",
            population = 36000,
            maxLords = 4,
            terrain = TerrainType.PLAIN
        )

        val result = combatEngine.resolveSiege(
            attackers = 5000,
            defenders = 1000,
            territory = territory,
            isSupplyLineCut = true
        )

        assertTrue(result.defenderWon)
        assertTrue(result.bloodlessVictory)
        assertEquals(0, result.defenderLosses)
    }

    @Test
    fun testHiddenTalentInfiltrationAndCoup() {
        val hiddenTalent = Talent(
            id = "t_user",
            name = "이광수",
            isPlayerHiddenTalent = true,
            warPower = 90,
            intelligence = 95,
            leadership = 92,
            politics = 88,
            charm = 91
        )
        val employer = Lord(
            id = "lord_npc",
            name = "김호족",
            isHumanPlayer = false,
            territoryCode = "4413125000",
            troops = 1000,
            grain = 3000,
            gold = 2000,
            prestige = 70
        )

        infiltrationEngine.accumulatePower(hiddenTalent, recruitedTroops = 1200, subvertProgress = 60)
        val coup = infiltrationEngine.attemptCoup(hiddenTalent, employer)

        assertTrue("하극상 찬탈이 성공해야 함", coup.success)
        assertEquals(1200, employer.troops)
    }

    @Test
    fun testCouncilRebellionOnExcessiveLevy() {
        val lords = listOf(
            Lord("l1", "호족 A", false, "t1", 500, 1000, 500, 30),
            Lord("l2", "호족 B", false, "t1", 400, 1000, 500, 30),
            Lord("l3", "호족 C", false, "t1", 300, 1000, 500, 30)
        )
        val oppressiveBill = CouncilBill(
            id = "b1",
            title = "과도한 50% 병력 징발령",
            type = BillType.TROOP_LEVY,
            proposedByCastellanId = "castellan",
            troopLevyAmount = 250 // 50% 차출 요구
        )

        val result = councilEngine.voteOnBill(oppressiveBill, lords)

        assertFalse("과도한 차출령은 부결되어야 함", result.isPassed)
        assertTrue("호족 연합 반란이 트리거되어야 함", result.isRebellionTriggered)
    }
}
