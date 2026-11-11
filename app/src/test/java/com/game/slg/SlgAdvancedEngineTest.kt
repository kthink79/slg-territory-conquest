package com.game.slg

import com.game.slg.core.engine.*
import com.game.slg.core.model.*
import com.game.slg.ui.SlgViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SlgAdvancedEngineTest {

    private lateinit var seasonEngine: ISeasonEngine
    private lateinit var logisticsEngine: ILogisticsEngine
    private lateinit var diplomacyEngine: IDiplomacyEngine

    @Before
    fun setUp() {
        seasonEngine = SeasonEngine()
        logisticsEngine = LogisticsEngine()
        diplomacyEngine = DiplomacyEngine()
    }

    // ----------------------------------------------------
    // Clean Architecture Domain Contracts Verification
    // ----------------------------------------------------

    @Test
    fun testDomainContractsVerification() {
        assertTrue("SeasonEngine must implement ISeasonEngine", seasonEngine is ISeasonEngine)
        assertTrue("LogisticsEngine must implement ILogisticsEngine", logisticsEngine is ILogisticsEngine)
        assertTrue("DiplomacyEngine must implement IDiplomacyEngine", diplomacyEngine is IDiplomacyEngine)
    }

    // ----------------------------------------------------
    // 1. Four Seasons Cycle & Regional Economy Engine Tests
    // ----------------------------------------------------

    @Test
    fun testSpringTaxCollection() {
        val territory = Territory(
            code = "t_spring",
            name = "아산시 배방읍",
            province = "충남",
            cityCountyDistrict = "아산시",
            population = 40000,
            maxLords = 4,
            terrain = TerrainType.PLAIN,
            fertileScore = 1.0
        )
        val lord1 = Lord("l1", "호족 1", true, territory.code, troops = 1000, grain = 1000, gold = 500, prestige = 50)
        val lord2 = Lord("l2", "호족 2", false, territory.code, troops = 1000, grain = 1000, gold = 500, prestige = 50)
        val lords = listOf(lord1, lord2)

        // Population tax = 40,000 * 0.05 = 2000. Lord tax = 2 * 100 = 200. Total = 2200.
        val result = seasonEngine.processSeasonalCycle(Season.SPRING, territory, lords, dikeMaintenanceDone = true)

        assertEquals(Season.SPRING, result.season)
        assertEquals(2200, result.goldChange)
        assertEquals(0, result.grainChange)
        // Split equally between 2 lords: each gets +1100 -> gold becomes 1600
        assertEquals(1600, lord1.gold)
        assertEquals(1600, lord2.gold)
    }

    @Test
    fun testSummerDikeMaintenanceAndBumperEvent() {
        val territory = Territory(
            code = "t_summer",
            name = "평택시 안중읍",
            province = "경기",
            cityCountyDistrict = "평택시",
            population = 30000,
            maxLords = 3,
            terrain = TerrainType.RIVER,
            fertileScore = 1.0
        )
        val lord = Lord("l1", "호족", true, territory.code, troops = 1000, grain = 2000, gold = 1000, prestige = 60)

        // Seed 10L produces dice = 13 (< 50) -> Bumper Weather
        val bumperResult = seasonEngine.processSeasonalCycle(
            currentSeason = Season.SUMMER,
            territory = territory,
            lords = listOf(lord),
            dikeMaintenanceDone = true,
            randomSeed = 10L
        )

        assertEquals(SummerDisasterType.BUMPER_WEATHER, bumperResult.disasterType)
        assertEquals(-200, bumperResult.goldChange)
        assertEquals(500, bumperResult.grainChange)
        assertEquals(2500, lord.grain)

        // Test summer without dike maintenance triggering flood disaster
        // Seed 2L produces dice = 8 (< 60) -> flood event
        val lordFlooded = Lord("l_flood", "수해 호족", false, territory.code, troops = 1000, grain = 3000, gold = 1000, prestige = 50)
        val floodResult = seasonEngine.processSeasonalCycle(
            currentSeason = Season.SUMMER,
            territory = territory,
            lords = listOf(lordFlooded),
            dikeMaintenanceDone = false,
            randomSeed = 2L
        )

        assertEquals(SummerDisasterType.FLOOD_DISASTER, floodResult.disasterType)
        assertTrue(floodResult.grainChange < 0)
        assertTrue(lordFlooded.grain < 3000)
    }

    @Test
    fun testAutumnHarvestScalingWithFertileScore() {
        // High fertile territory (fertileScore = 1.5)
        val fertileTerritory = Territory(
            code = "t_autumn_fertile",
            name = "김제시 만경읍",
            province = "전북",
            cityCountyDistrict = "김제시",
            population = 20000,
            maxLords = 2,
            terrain = TerrainType.PLAIN,
            fertileScore = 1.5
        )
        // Base harvest = 20000 * 0.15 = 3000. Scaling with 1.5 = 4500 grain.
        val fertileLord = Lord("l_fertile", "호족", true, fertileTerritory.code, troops = 1000, grain = 1000, gold = 1000, prestige = 50)
        val fertileResult = seasonEngine.processSeasonalCycle(Season.AUTUMN, fertileTerritory, listOf(fertileLord), true)

        assertEquals(4500, fertileResult.grainChange)
        assertEquals(5500, fertileLord.grain)

        // Low fertile territory (fertileScore = 0.8)
        val barrenTerritory = fertileTerritory.copy(fertileScore = 0.8)
        // Base harvest = 20000 * 0.15 = 3000. Scaling with 0.8 = 2400 grain.
        val barrenLord = Lord("l_barren", "호족", true, barrenTerritory.code, troops = 1000, grain = 1000, gold = 1000, prestige = 50)
        val barrenResult = seasonEngine.processSeasonalCycle(Season.AUTUMN, barrenTerritory, listOf(barrenLord), true)

        assertEquals(2400, barrenResult.grainChange)
        assertEquals(3400, barrenLord.grain)
    }

    @Test
    fun testWinterHarshUpkeepAndAttritionShortage() {
        val territory = Territory(
            code = "t_winter",
            name = "철원군 갈말읍",
            province = "강원",
            cityCountyDistrict = "철원군",
            population = 15000,
            maxLords = 2,
            terrain = TerrainType.MOUNTAIN,
            fertileScore = 0.7
        )

        // Case A: Sufficient grain (Troops = 1000, Grain = 3000) -> consumes 1000 grain, 0 attrition
        val lordSafe = Lord("l_safe", "보급 충분 호족", true, territory.code, troops = 1000, grain = 3000, gold = 500, prestige = 50)
        val safeResult = seasonEngine.processSeasonalCycle(Season.WINTER, territory, listOf(lordSafe), true)

        assertEquals(0, safeResult.winterAttritionLosses)
        assertEquals(-1000, safeResult.grainChange)
        assertEquals(2000, lordSafe.grain)
        assertEquals(1000, lordSafe.troops)

        // Case B: Grain shortage (Troops = 2000, Grain = 400) -> Shortage = 1600 -> Attrition = 800
        val lordStarving = Lord("l_starve", "식량 부족 호족", true, territory.code, troops = 2000, grain = 400, gold = 500, prestige = 50)
        val starveResult = seasonEngine.processSeasonalCycle(Season.WINTER, territory, listOf(lordStarving), true)

        assertEquals(800, starveResult.winterAttritionLosses)
        assertEquals(0, lordStarving.grain)
        assertEquals(1200, lordStarving.troops) // 2000 - 800 = 1200
    }

    @Test
    fun testFullFourSeasonCycleIntegration() {
        val territory = Territory(
            code = "t_cycle",
            name = "나주시 금천면",
            province = "전남",
            cityCountyDistrict = "나주시",
            population = 20000,
            maxLords = 2,
            terrain = TerrainType.PLAIN,
            fertileScore = 1.0
        )
        val lord = Lord("l_cycle", "나주목사", true, territory.code, troops = 1000, grain = 1500, gold = 1000, prestige = 60)
        val lords = listOf(lord)

        var season = Season.SPRING

        // Spring
        val r1 = seasonEngine.processSeasonalCycle(season, territory, lords, true)
        assertEquals(Season.SPRING, r1.season)
        assertTrue(lord.gold > 1000)

        // Summer
        season = season.next()
        val r2 = seasonEngine.processSeasonalCycle(season, territory, lords, true, randomSeed = 10L)
        assertEquals(Season.SUMMER, r2.season)

        // Autumn
        season = season.next()
        val r3 = seasonEngine.processSeasonalCycle(season, territory, lords, true)
        assertEquals(Season.AUTUMN, r3.season)
        assertEquals(3000, r3.grainChange) // 20000 * 0.15 * 1.0 = 3000

        // Winter
        season = season.next()
        val r4 = seasonEngine.processSeasonalCycle(season, territory, lords, true)
        assertEquals(Season.WINTER, r4.season)
        assertEquals(0, r4.winterAttritionLosses)

        // Full cycle wraps back to SPRING
        assertEquals(Season.SPRING, season.next())
    }

    // ----------------------------------------------------
    // 2. GIS Supply Route & Logistics Transportation Engine Tests
    // ----------------------------------------------------

    @Test
    fun testLogisticsRouteConnectivityVerification() {
        // Rear (t1) <-> Mid (t2) <-> Front (t3)
        val t1 = Territory("t1", "배방읍", "충남", "아산", 50000, 5, TerrainType.PLAIN, adjacentCodes = listOf("t2"))
        val t2 = Territory("t2", "풍세면", "충남", "천안", 25000, 3, TerrainType.MOUNTAIN, adjacentCodes = listOf("t1", "t3"))
        val t3 = Territory("t3", "공도읍", "경기", "안성", 30000, 3, TerrainType.PLAIN, adjacentCodes = listOf("t2"))

        val map = mapOf(t1.code to t1, t2.code to t2, t3.code to t3)

        // 1. Without hostile occupation -> Connected
        val openRoute = logisticsEngine.verifySupplyConnectivity("t1", "t3", map, hostileTerritoryCodes = emptySet())
        assertTrue("보급로가 정상 연결되어야 함", openRoute.isConnected)
        assertEquals(listOf("t1", "t2", "t3"), openRoute.path)
        assertEquals(0, openRoute.moralePenalty)
        assertFalse(openRoute.triggersBloodlessSurrender)

        // 2. With intermediate hostile occupation (t2 captured) -> Route cut
        val cutRoute = logisticsEngine.verifySupplyConnectivity("t1", "t3", map, hostileTerritoryCodes = setOf("t2"))
        assertFalse("중간 거점 적 점령 시 보급로가 차단되어야 함", cutRoute.isConnected)
        assertTrue("사기 저하 페널티 발생", cutRoute.moralePenalty > 0)
        assertTrue("무혈 항복 트리거 활성화", cutRoute.triggersBloodlessSurrender)
    }

    @Test
    fun testTransportConvoyDeliverySuccessAndCutOffLoss() {
        val t1 = Territory("t1", "본거지", "충남", "아산", 50000, 5, TerrainType.PLAIN, adjacentCodes = listOf("t2"))
        val t2 = Territory("t2", "관문", "충남", "천안", 25000, 3, TerrainType.MOUNTAIN, adjacentCodes = listOf("t1", "t3"))
        val t3 = Territory("t3", "전초", "경기", "안성", 30000, 3, TerrainType.PLAIN, adjacentCodes = listOf("t2"))
        val map = mapOf(t1.code to t1, t2.code to t2, t3.code to t3)

        val rearLord = Lord("lord_rear", "후방 영주", true, "t1", troops = 1500, grain = 3000, gold = 2000, prestige = 70)
        val frontLord = Lord("lord_front", "전방 수성장", false, "t3", troops = 800, grain = 200, gold = 100, prestige = 50)

        // Case 1: Supply line open -> successful convoy delivery
        val openRoute = logisticsEngine.verifySupplyConnectivity("t1", "t3", map, emptySet())
        val convoy1 = TransportConvoy("c1", "t1", "t3", gold = 500, grain = 1000)
        val result1 = logisticsEngine.dispatchTransportConvoy(convoy1, rearLord, frontLord, openRoute)

        assertTrue(result1.success)
        assertEquals(500, result1.deliveredGold)
        assertEquals(1000, result1.deliveredGrain)
        assertEquals(1500, rearLord.gold)
        assertEquals(2000, rearLord.grain)
        assertEquals(600, frontLord.gold)
        assertEquals(1200, frontLord.grain)

        // Case 2: Intermediate territory cut off -> Convoy intercepted, resources lost
        val cutRoute = logisticsEngine.verifySupplyConnectivity("t1", "t3", map, setOf("t2"))
        val convoy2 = TransportConvoy("c2", "t1", "t3", gold = 300, grain = 500)
        val result2 = logisticsEngine.dispatchTransportConvoy(convoy2, rearLord, frontLord, cutRoute)

        assertFalse("차단된 보급로로 파견된 수송대는 나포/실패해야 함", result2.success)
        assertEquals(0, result2.deliveredGold)
        assertEquals(0, result2.deliveredGrain)
        // Rear lord lost sent resources
        assertEquals(1200, rearLord.gold)
        assertEquals(1500, rearLord.grain)
        // Front lord received nothing
        assertEquals(600, frontLord.gold)
        assertEquals(1200, frontLord.grain)

        // Check front lord morale cut penalty and bloodless surrender trigger
        val triggersSurrender = logisticsEngine.applySupplyCutPenalties(frontLord, cutRoute)
        assertTrue(triggersSurrender)
        assertEquals(10, frontLord.prestige) // 50 - 40 = 10 (penalty applied)
    }

    // ----------------------------------------------------
    // 3. Diplomacy & Espionage Engine Tests
    // ----------------------------------------------------

    @Test
    fun testTreatyDurationAndVassalTributaryTurnProcessing() {
        val suzerain = Lord("l_suzerain", "종주국 성주", true, "t1", troops = 3000, grain = 5000, gold = 1000, prestige = 90)
        val vassal = Lord("l_vassal", "복속된 속국", false, "t2", troops = 1000, grain = 2000, gold = 500, prestige = 40)
        val lordsMap = mapOf(suzerain.id to suzerain, vassal.id to vassal)

        // Propose Vassal Tributary Treaty (3 turns, 150 gold per turn)
        val treaty = diplomacyEngine.proposeTreaty(
            lord1 = suzerain,
            lord2 = vassal,
            type = TreatyType.VASSAL_TRIBUTARY,
            durationTurns = 3,
            tributaryGold = 150
        )

        assertEquals(3, treaty.remainingTurns)
        assertEquals(150, treaty.tributaryGoldPerTurn)

        // Process Turn 1: Vassal pays 150 gold
        var activeTreaties = diplomacyEngine.processTurnTreaties(listOf(treaty), lordsMap)
        assertEquals(1, activeTreaties.size)
        assertEquals(2, treaty.remainingTurns)
        assertEquals(1150, suzerain.gold)
        assertEquals(350, vassal.gold)

        // Process Turn 2: Vassal pays 150 gold
        activeTreaties = diplomacyEngine.processTurnTreaties(activeTreaties, lordsMap)
        assertEquals(1, activeTreaties.size)
        assertEquals(1, treaty.remainingTurns)
        assertEquals(1300, suzerain.gold)
        assertEquals(200, vassal.gold)

        // Process Turn 3: Last turn, treaty expires
        activeTreaties = diplomacyEngine.processTurnTreaties(activeTreaties, lordsMap)
        assertEquals(0, activeTreaties.size)
        assertEquals(1450, suzerain.gold)
        assertEquals(50, vassal.gold)
    }

    @Test
    fun testTreatyViolationPrestigePenalties() {
        val lordA = Lord("l_a", "호족 A", true, "t1", troops = 2000, grain = 2000, gold = 1000, prestige = 80)
        val lordB = Lord("l_b", "호족 B", false, "t2", troops = 1500, grain = 1500, gold = 800, prestige = 70)

        val nonAggressionTreaty = diplomacyEngine.proposeTreaty(
            lord1 = lordA,
            lord2 = lordB,
            type = TreatyType.NON_AGGRESSION_PACT,
            durationTurns = 5
        )

        // Lord A attacks Lord B while Non-Aggression Pact is active
        val violation = diplomacyEngine.checkTreatyViolation(
            attackerId = lordA.id,
            targetLordId = lordB.id,
            treaties = listOf(nonAggressionTreaty)
        )

        assertTrue(violation.isViolated)
        assertEquals(lordA.id, violation.violatorLordId)
        assertEquals(50, violation.prestigePenalty)

        // Defensive alliance violation has higher penalty (80)
        val alliance = diplomacyEngine.proposeTreaty(lordA, lordB, TreatyType.DEFENSIVE_ALLIANCE, 5)
        val allianceViolation = diplomacyEngine.checkTreatyViolation(lordA.id, lordB.id, listOf(alliance))
        assertEquals(80, allianceViolation.prestigePenalty)

        // Attack when no treaty exists
        val noTreatyViolation = diplomacyEngine.checkTreatyViolation("random_lord", lordB.id, listOf(nonAggressionTreaty))
        assertFalse(noTreatyViolation.isViolated)
        assertEquals(0, noTreatyViolation.prestigePenalty)
    }

    @Test
    fun testEspionageOperationsWithTalents() {
        val talentSpy = Talent(
            id = "t_spy",
            name = "밀정 강민",
            isPlayerHiddenTalent = true,
            warPower = 70,
            intelligence = 95,
            leadership = 75,
            politics = 80,
            charm = 85
        )

        val enemyLord = Lord("l_enemy", "적장", false, "t_enemy", troops = 3000, grain = 4000, gold = 1500, prestige = 80)
        val enemyTerritory = Territory("t_enemy", "적성 거점", "경기", "화성", 40000, 4, TerrainType.PLAIN, fertileScore = 1.2)

        // 1. Gather Intel Operation
        val intelResult = diplomacyEngine.conductEspionage(talentSpy, enemyLord, enemyTerritory, EspionageOpType.GATHER_INTEL)
        assertTrue(intelResult.success)
        assertNotNull(intelResult.intelGathered)
        assertTrue(intelResult.intelGathered!!.contains("병력 3000"))

        // 2. Instigate Dissent Operation
        val dissentResult = diplomacyEngine.conductEspionage(talentSpy, enemyLord, enemyTerritory, EspionageOpType.INSTIGATE_DISSENT)
        assertTrue(dissentResult.success)
        assertTrue(dissentResult.moraleOrLoyaltyDeduction > 0)
        assertEquals(80 - dissentResult.moraleOrLoyaltyDeduction, enemyLord.prestige)
    }

    // ----------------------------------------------------
    // 4. SlgViewModel State & Integration Tests
    // ----------------------------------------------------

    @Test
    fun testSlgViewModelIntegration() {
        val vm = SlgViewModel(seasonEngine, logisticsEngine, diplomacyEngine)
        val initialState = vm.uiState.value

        assertEquals(Season.SPRING, initialState.currentSeason)
        assertTrue(initialState.isSupplyConnected)

        // Advance season: SPRING -> SUMMER
        vm.advanceSeason(dikeMaintenanceDone = true)
        val summerState = vm.uiState.value
        assertEquals(Season.SUMMER, summerState.currentSeason)

        // Sign treaty
        vm.signTreaty(TreatyType.NON_AGGRESSION_PACT, durationTurns = 4)
        assertEquals(1, vm.uiState.value.treaties.size)

        // Dispatch convoy through cut route
        vm.verifyAndDispatchConvoy(hostileCodes = setOf("t_mid_02"))
        assertFalse(vm.uiState.value.isSupplyConnected)
    }
}
