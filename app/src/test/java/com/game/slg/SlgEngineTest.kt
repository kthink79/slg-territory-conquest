package com.game.slg

import com.game.slg.core.engine.*
import com.game.slg.core.model.*
import org.junit.Assert.*
import org.junit.Test

class SlgEngineTest {

    private val combatEngine: CombatEngineContract = CombatEngine()
    private val councilEngine: CouncilEngineContract = CouncilEngine()
    private val infiltrationEngine: InfiltrationEngineContract = InfiltrationEngine()

    // ----------------------------------------------------
    // Clean Architecture Domain Contracts Verification
    // ----------------------------------------------------

    @Test
    fun testCleanArchitectureContracts() {
        val combat: Any = CombatEngine()
        val council: Any = CouncilEngine()
        val infiltration: Any = InfiltrationEngine()

        assertTrue("CombatEngine must implement CombatEngineContract", combat is CombatEngineContract)
        assertTrue("CouncilEngine must implement CouncilEngineContract", council is CouncilEngineContract)
        assertTrue("InfiltrationEngine must implement InfiltrationEngineContract", infiltration is InfiltrationEngineContract)
    }

    // ----------------------------------------------------
    // CombatEngine: Fortress, Terrain & Multi-turn Sieges
    // ----------------------------------------------------

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
        // 4000 * 0.25 = 1000 raw losses. MOUNTAIN_FORTRESS gives 80% reduction: 1000 * 0.2 = 200
        assertEquals(200, result.defenderLosses)
        assertTrue("수성군 사상자가 80% 격감되어야 함", result.defenderLosses <= 250)
    }

    @Test
    fun testAllFortressLevelsCasualtyReductionRatios() {
        val levels = listOf(
            FortressLevel.WOODEN_PALISADE to 0.40,
            FortressLevel.STONE_WALL to 0.65,
            FortressLevel.MOUNTAIN_FORTRESS to 0.80,
            FortressLevel.IMPREGNABLE_GATE to 0.85
        )

        for ((level, expectedRatio) in levels) {
            val fortress = Fortress(level = level, durability = 500, maxDurability = 500)
            assertEquals(expectedRatio, fortress.casualtyReductionRatio, 0.001)

            val territory = Territory(
                code = "test_code",
                name = "Test",
                province = "P",
                cityCountyDistrict = "C",
                population = 10000,
                maxLords = 2,
                terrain = TerrainType.PLAIN,
                fortress = fortress
            )

            val result = combatEngine.resolveSiege(
                attackers = 1000,
                defenders = 1000,
                territory = territory,
                isSupplyLineCut = false
            )
            // raw loss = 1000 * 0.25 = 250. Expected loss = Math.round(250 * (1.0 - expectedRatio)).toInt()
            val expectedLoss = Math.round(250 * (1.0 - expectedRatio)).toInt()
            assertEquals("Losses for $level should match reduction ratio", expectedLoss, result.defenderLosses)
        }
    }

    @Test
    fun testCombatWithoutFortress() {
        val territory = Territory(
            code = "open_field",
            name = "평야 개활지",
            province = "경기도",
            cityCountyDistrict = "평택시",
            population = 50000,
            maxLords = 5,
            terrain = TerrainType.PLAIN,
            fortress = null
        )

        val result = combatEngine.resolveSiege(
            attackers = 1000,
            defenders = 500,
            territory = territory,
            isSupplyLineCut = false
        )

        // Raw defender losses = 1000 * 0.25 = 250 without any reduction ratio
        assertEquals(250, result.defenderLosses)
    }

    @Test
    fun testMultiTurnSiegeAttritionAndFortressDegradation() {
        val fortress = Fortress(
            level = FortressLevel.STONE_WALL,
            durability = 600,
            maxDurability = 1000,
            garrisonTroops = 2000
        )
        val territory = Territory(
            code = "fort_01",
            name = "남한산성",
            province = "경기도",
            cityCountyDistrict = "광주시",
            population = 70000,
            maxLords = 7,
            terrain = TerrainType.MOUNTAIN,
            fortress = fortress
        )

        var attackers = 5000
        var defenders = 2000

        // Multi-turn siege simulation (2 turns)
        for (turn in 1..2) {
            val initialDurability = territory.fortress?.durability ?: 0
            if (initialDurability > 0 && attackers > 0) {
                val result = combatEngine.resolveSiege(
                    attackers = attackers,
                    defenders = defenders,
                    territory = territory,
                    isSupplyLineCut = false
                )

                assertTrue("Siege turn $turn finished", result.defenderLosses >= 0)
                assertTrue("Fortress durability should degrade each turn under heavy assault",
                    (territory.fortress?.durability ?: 0) < initialDurability
                )

                attackers = (attackers - result.attackerLosses).coerceAtLeast(0)
                defenders = (defenders - result.defenderLosses).coerceAtLeast(0)
            }
        }

        assertTrue("Attackers took damage over multi-turn assault", attackers < 5000)
        assertTrue("Fortress took sustained damage across turns", fortress.durability < 600)
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
        assertEquals("Defenders must take zero casualties on bloodless victory", 0, result.defenderLosses)
        assertEquals("Attackers surrender/rout with 40% losses", 2000, result.attackerLosses)
    }

    @Test
    fun testSiegeZeroTroopBoundaryEdgeCases() {
        val territory = Territory(
            code = "t_zero",
            name = "공허한 거점",
            province = "강원도",
            cityCountyDistrict = "원주시",
            population = 10000,
            maxLords = 1,
            terrain = TerrainType.PLAIN
        )

        // 0 attackers vs defenders
        val zeroAttackerResult = combatEngine.resolveSiege(0, 500, territory, false)
        assertTrue(zeroAttackerResult.defenderWon)
        assertEquals(0, zeroAttackerResult.defenderLosses)
        assertEquals(0, zeroAttackerResult.attackerLosses)

        // Attacker assault against empty garrison
        val zeroDefenderResult = combatEngine.resolveSiege(500, 0, territory, false)
        assertFalse(zeroDefenderResult.defenderWon)
        assertEquals(0, zeroDefenderResult.defenderLosses)
        assertEquals(0, zeroDefenderResult.attackerLosses)
    }

    // ----------------------------------------------------
    // InfiltrationEngine: Multi-turn Stealth & Coup Edge Cases
    // ----------------------------------------------------

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
        assertEquals(160, coup.newLordPrestige)
    }

    @Test
    fun testMultiTurnInfiltrationStealthAccumulation() {
        val hiddenTalent = Talent(
            id = "t_multi",
            name = "정약용",
            isPlayerHiddenTalent = true,
            warPower = 70,
            intelligence = 99,
            leadership = 85,
            politics = 95,
            charm = 90
        )

        assertEquals(100, hiddenTalent.loyalty)
        assertEquals(0, hiddenTalent.privateTroops)
        assertEquals(0, hiddenTalent.rebellionPrestige)

        // Simulate 6 turns of stealth recruiting and subverting
        for (turn in 1..6) {
            infiltrationEngine.accumulatePower(
                talent = hiddenTalent,
                recruitedTroops = 200,
                subvertProgress = 15
            )
        }

        assertEquals(1200, hiddenTalent.privateTroops)
        assertEquals(90, hiddenTalent.rebellionPrestige)
        // Loyalty decremented: 100 - (15/2)*6 = 100 - 42 = 58
        assertEquals(58, hiddenTalent.loyalty)
    }

    @Test
    fun testInfiltrationLoyaltyCannotDropBelowZero() {
        val hiddenTalent = Talent(
            id = "t_loyal_drop",
            name = "신숙주",
            isPlayerHiddenTalent = true,
            warPower = 75,
            intelligence = 90,
            leadership = 80,
            politics = 90,
            charm = 80,
            loyalty = 20
        )

        // Subvert progress of 100 would drop loyalty by 50 -> must floor at 0
        infiltrationEngine.accumulatePower(hiddenTalent, recruitedTroops = 100, subvertProgress = 100)
        assertEquals(0, hiddenTalent.loyalty)
    }

    @Test
    fun testCoupThresholdBoundaries() {
        val hiddenTalent = Talent(
            id = "t_bound",
            name = "조조",
            isPlayerHiddenTalent = true,
            warPower = 90,
            intelligence = 95,
            leadership = 95,
            politics = 90,
            charm = 90
        )
        val employer = Lord(
            id = "lord_dong",
            name = "동탁",
            isHumanPlayer = false,
            territoryCode = "code_1",
            troops = 1000,
            grain = 5000,
            gold = 5000,
            prestige = 100
        )

        // Case 1: Troops ratio 79% (< 80%) with prestige 60 (>= 50) -> Failure
        hiddenTalent.privateTroops = 799
        hiddenTalent.rebellionPrestige = 60
        val coupFailTroops = infiltrationEngine.attemptCoup(hiddenTalent, employer)
        assertFalse("Power ratio below 0.8 should fail coup", coupFailTroops.success)

        // Case 2: Troops ratio 80% (>= 80%) with prestige 49 (< 50) -> Failure
        hiddenTalent.privateTroops = 800
        hiddenTalent.rebellionPrestige = 49
        val coupFailPrestige = infiltrationEngine.attemptCoup(hiddenTalent, employer)
        assertFalse("Rebellion prestige below 50 should fail coup", coupFailPrestige.success)

        // Case 3: Exactly 80% power ratio and exactly 50 prestige -> Success
        hiddenTalent.privateTroops = 800
        hiddenTalent.rebellionPrestige = 50
        val coupExactSuccess = infiltrationEngine.attemptCoup(hiddenTalent, employer)
        assertTrue("Exact threshold (80% troops and 50 prestige) must succeed", coupExactSuccess.success)
        assertEquals(800, employer.troops)
        assertEquals(150, coupExactSuccess.newLordPrestige)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNonHiddenTalentCannotAccumulatePower() {
        val normalTalent = Talent(
            id = "t_npc",
            name = "일반 관리",
            isPlayerHiddenTalent = false,
            warPower = 60,
            intelligence = 60,
            leadership = 60,
            politics = 60,
            charm = 60
        )

        infiltrationEngine.accumulatePower(normalTalent, 100, 20)
    }

    @Test
    fun testNonHiddenTalentCoupRejected() {
        val normalTalent = Talent(
            id = "t_npc",
            name = "일반 관리",
            isPlayerHiddenTalent = false,
            warPower = 60,
            intelligence = 60,
            leadership = 60,
            politics = 60,
            charm = 60
        )
        val lord = Lord("l1", "주군", false, "t1", 500, 1000, 1000, 50)
        val coup = infiltrationEngine.attemptCoup(normalTalent, lord)
        assertFalse(coup.success)
        assertEquals("위장 잠입 유저가 아닙니다.", coup.message)
    }

    // ----------------------------------------------------
    // CouncilEngine: Multi-turn Bills, Levy & Rebellion Triggers
    // ----------------------------------------------------

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

    @Test
    fun testMultiTurnCouncilBills() {
        val lords = listOf(
            Lord("l1", "호족 1", false, "t1", troops = 3000, grain = 1500, gold = 1000, prestige = 50),
            Lord("l2", "호족 2", false, "t1", troops = 1500, grain = 800,  gold = 600,  prestige = 40),
            Lord("l3", "호족 3", false, "t1", troops = 2500, grain = 2000, gold = 800,  prestige = 45)
        )

        // Turn 1: FORTRESS_EXPAND bill -> Unanimously passed, no rebellion
        val fortressBill = CouncilBill("b1", "산성 보수 결의안", BillType.FORTRESS_EXPAND, "castellan")
        val res1 = councilEngine.voteOnBill(fortressBill, lords)
        assertTrue(res1.isPassed)
        assertEquals(3, res1.votesInFavor)
        assertEquals(0, res1.votesAgainst)
        assertFalse(res1.isRebellionTriggered)

        // Turn 2: DECLARE_WAR bill -> Requires troops > 2000 (l1 and l3 vote favor, l2 votes against)
        val warBill = CouncilBill("b2", "인접 영지 정벌안", BillType.DECLARE_WAR, "castellan")
        val res2 = councilEngine.voteOnBill(warBill, lords)
        assertTrue("2 out of 3 lords favor war", res2.isPassed)
        assertEquals(2, res2.votesInFavor)
        assertEquals(1, res2.votesAgainst)
        assertFalse("Against ratio is only 33% (< 65%), no rebellion", res2.isRebellionTriggered)

        // Turn 3: SPECIAL_TAX bill -> Requires grain > 1000 (l1, l3 vote favor, l2 votes against)
        val taxBill = CouncilBill("b3", "전시 군량 징발령", BillType.SPECIAL_TAX, "castellan")
        val res3 = councilEngine.voteOnBill(taxBill, lords)
        assertTrue(res3.isPassed)
        assertFalse(res3.isRebellionTriggered)

        // Turn 4: Reasonable TROOP_LEVY -> 200 troops (<35% for all lords)
        val reasonableLevyBill = CouncilBill("b4", "통상 경비대 차출령", BillType.TROOP_LEVY, "castellan", troopLevyAmount = 200)
        val res4 = councilEngine.voteOnBill(reasonableLevyBill, lords)
        assertTrue("Reasonable levy should pass", res4.isPassed)
        assertEquals(3, res4.votesInFavor)
        assertFalse(res4.isRebellionTriggered)
    }

    @Test
    fun testCouncilEmptyLordsEdgeCase() {
        val emptyLords = emptyList<Lord>()
        val bill = CouncilBill("b_empty", "유령 의회 안건", BillType.FORTRESS_EXPAND, "c1")
        val result = councilEngine.voteOnBill(bill, emptyLords)

        assertFalse(result.isPassed)
        assertEquals(0, result.votesInFavor)
        assertEquals(0, result.votesAgainst)
        assertFalse("No lords should not trigger rebellion division by zero", result.isRebellionTriggered)
    }

    @Test
    fun testCouncilTieVoteDoesNotPass() {
        val lords = listOf(
            Lord("l1", "호족 1", false, "t1", troops = 3000, grain = 2000, gold = 500, prestige = 30),
            Lord("l2", "호족 2", false, "t1", troops = 1000, grain = 500, gold = 500, prestige = 30)
        )
        // DECLARE_WAR: l1 (troops=3000) votes favor, l2 (troops=1000) votes against
        val bill = CouncilBill("b_tie", "선전포고 동의안", BillType.DECLARE_WAR, "c1")
        val result = councilEngine.voteOnBill(bill, lords)

        assertEquals(1, result.votesInFavor)
        assertEquals(1, result.votesAgainst)
        assertFalse("Tie vote (1 vs 1) should not pass (favor > against is false)", result.isPassed)
        assertFalse("Against ratio is 50%, below 65% rebellion threshold", result.isRebellionTriggered)
    }
}
