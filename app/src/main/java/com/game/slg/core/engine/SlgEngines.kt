package com.game.slg.core.engine

import com.game.slg.core.model.*

/**
 * Clean Architecture Domain Contracts
 */
interface CouncilEngineContract {
    fun voteOnBill(bill: CouncilBill, lords: List<Lord>): CouncilBill
}

interface InfiltrationEngineContract {
    fun accumulatePower(talent: Talent, recruitedTroops: Int, subvertProgress: Int)
    fun attemptCoup(talent: Talent, employerLord: Lord): CoupResult
}

interface CombatEngineContract {
    fun resolveSiege(
        attackers: Int,
        defenders: Int,
        territory: Territory,
        isSupplyLineCut: Boolean
    ): SiegeResult
}

interface ISeasonEngine {
    fun processSeasonalCycle(
        currentSeason: Season,
        territory: Territory,
        lords: List<Lord>,
        dikeMaintenanceDone: Boolean,
        randomSeed: Long? = null
    ): SeasonActionResult
}

interface ILogisticsEngine {
    fun verifySupplyConnectivity(
        originCode: String,
        destCode: String,
        territoryMap: Map<String, Territory>,
        hostileTerritoryCodes: Set<String>
    ): SupplyRouteStatus

    fun dispatchTransportConvoy(
        convoy: TransportConvoy,
        fromLord: Lord,
        toLord: Lord,
        routeStatus: SupplyRouteStatus
    ): ConvoyResult

    fun applySupplyCutPenalties(lord: Lord, routeStatus: SupplyRouteStatus): Boolean
}

interface IDiplomacyEngine {
    fun proposeTreaty(
        lord1: Lord,
        lord2: Lord,
        type: TreatyType,
        durationTurns: Int,
        tributaryGold: Int = 0
    ): DiplomaticTreaty

    fun processTurnTreaties(
        treaties: List<DiplomaticTreaty>,
        lordsMap: Map<String, Lord>
    ): List<DiplomaticTreaty>

    fun checkTreatyViolation(
        attackerId: String,
        targetLordId: String,
        treaties: List<DiplomaticTreaty>
    ): TreatyViolationResult

    fun conductEspionage(
        agent: Talent,
        targetLord: Lord,
        targetTerritory: Territory,
        opType: EspionageOpType
    ): EspionageResult
}

/**
 * 성주-호족 의회 및 전시 병력 차출·반란 엔진
 */
class CouncilEngine : CouncilEngineContract {

    /**
     * 성주가 지역 정책(병력 차출 등)을 호족 의회에 상정
     */
    override fun voteOnBill(bill: CouncilBill, lords: List<Lord>): CouncilBill {
        var favor = 0
        var against = 0

        for (lord in lords) {
            // 호족들의 병력 규모와 성향에 따른 투표 시뮬레이션
            val acceptsLevy = when (bill.type) {
                BillType.TROOP_LEVY -> {
                    // 병력의 30% 초과 차출 요구 시 반발
                    val levyRatio = bill.troopLevyAmount.toDouble() / (lord.troops.coerceAtLeast(1))
                    levyRatio < 0.35
                }
                BillType.SPECIAL_TAX -> lord.grain > 1000
                BillType.FORTRESS_EXPAND -> true
                BillType.DECLARE_WAR -> lord.troops > 2000
            }

            if (acceptsLevy) favor++ else against++
        }

        bill.votesInFavor = favor
        bill.votesAgainst = against
        bill.isPassed = favor > against

        // 반대표가 65% 이상일 경우 다른 호족들과 연합해 성주에 반란을 일으킬 수 있음 (호족이 없을 경우 방지)
        if (lords.isNotEmpty() && (against.toDouble() / lords.size >= 0.65)) {
            bill.isRebellionTriggered = true
        } else {
            bill.isRebellionTriggered = false
        }

        return bill
    }
}

/**
 * 삼국지식 인재 탐색·등용 및 '재야 인재 위장 잠입 스타팅' (하극상) 엔진
 */
class InfiltrationEngine : InfiltrationEngineContract {

    /**
     * 유저가 호족이 아닌 재야 인재로 시작했을 때 조용히 사병을 모으고 암약
     */
    override fun accumulatePower(talent: Talent, recruitedTroops: Int, subvertProgress: Int) {
        require(talent.isPlayerHiddenTalent) { "일반 인재는 암약 시스템을 독립 실행할 수 없습니다." }
        require(recruitedTroops >= 0) { "모병 수는 음수일 수 없습니다." }
        require(subvertProgress >= 0) { "암약 진척도는 음수일 수 없습니다." }
        talent.privateTroops += recruitedTroops
        talent.rebellionPrestige += subvertProgress
        // 고용주에 대한 충성도 위장 감소 (0~100 범위 유지)
        talent.loyalty = (talent.loyalty - (subvertProgress / 2)).coerceIn(0, 100)
    }

    /**
     * 고용주 호족 세력 취득 (하극상/찬탈) 결행
     * 사병 수와 지지도가 고용주의 병력을 압도할 때 성공
     */
    override fun attemptCoup(talent: Talent, employerLord: Lord): CoupResult {
        if (!talent.isPlayerHiddenTalent) {
            return CoupResult(success = false, message = "위장 잠입 유저가 아닙니다.")
        }

        val powerRatio = talent.privateTroops.toDouble() / employerLord.troops.coerceAtLeast(1)
        val success = powerRatio >= 0.8 && talent.rebellionPrestige >= 50

        return if (success) {
            // 하극상 성공: 고용주의 영지와 지위를 찬탈하고 새 호족으로 등극
            employerLord.troops = talent.privateTroops
            CoupResult(
                success = true,
                message = "${talent.name} 님이 주군 ${employerLord.name}을 축출하고 세력을 완벽히 찬탈했습니다!",
                newLordPrestige = talent.rebellionPrestige + 100
            )
        } else {
            CoupResult(
                success = false,
                message = "하극상 모의가 발각되어 실패했습니다. 토벌 위험에 직면합니다."
            )
        }
    }
}

data class CoupResult(
    val success: Boolean,
    val message: String,
    val newLordPrestige: Int = 0
)

/**
 * 방어 요새 수성 우위 & GIS 지형 단판 대회전 엔진
 */
class CombatEngine : CombatEngineContract {

    /**
     * 공성전 및 요새 수성 전투 시뮬레이션
     * - 방어 시설(목책·석성·산성) 구축 시 수성군 사상자 최대 80% 격감
     * - 보급선 단절 시 공격군 사기 저하 및 무혈 항복 유도
     */
    override fun resolveSiege(
        attackers: Int,
        defenders: Int,
        territory: Territory,
        isSupplyLineCut: Boolean
    ): SiegeResult {
        // 보급선이 끊긴 공격군은 병력 손실 없이 와해 또는 무혈 항복
        if (isSupplyLineCut) {
            return SiegeResult(
                defenderWon = true,
                bloodlessVictory = true,
                attackerLosses = (attackers * 0.4).toInt(),
                defenderLosses = 0,
                message = "적군의 보급로를 완벽히 차단하여 무혈 항복을 받아냈습니다!"
            )
        }

        val fortress = territory.fortress
        val reductionRatio = fortress?.casualtyReductionRatio ?: 0.0
        val terrainBonus = territory.terrain.defenseBonus

        // 수성군 실효 전투력 (요새 + 지형 보너스 적용)
        val effectiveDefenderPower = defenders * terrainBonus * (1.0 + (fortress?.durability?.toDouble() ?: 0.0) / 1000.0)
        val effectiveAttackerPower = attackers.toDouble()

        val defenderWon = effectiveDefenderPower >= effectiveAttackerPower

        val rawDefenderLosses = Math.round(attackers * 0.25).toInt()
        val actualDefenderLosses = Math.round(rawDefenderLosses * (1.0 - reductionRatio)).toInt().coerceIn(0, defenders)
        val attackerLosses = Math.round(defenders * terrainBonus * 0.7).toInt().coerceIn(0, attackers)

        // 다중 턴 공성전 시 요새 내구도 감소 반영 (요새가 있고 공격자가 있을 경우)
        if (fortress != null && fortress.durability > 0 && attackers > 0) {
            val siegeDamage = (attackers * 0.1).toInt().coerceAtLeast(10)
            fortress.takeDamage(siegeDamage)
        }

        return SiegeResult(
            defenderWon = defenderWon,
            bloodlessVictory = false,
            attackerLosses = attackerLosses,
            defenderLosses = actualDefenderLosses,
            message = if (defenderWon) {
                "요새 방어망과 지형지물을 활용해 적의 대공세를 격퇴했습니다! (사상자 ${(reductionRatio * 100).toInt()}% 방어 격감)"
            } else {
                "성벽이 돌파되어 거점을 상실했습니다."
            }
        )
    }
}

data class SiegeResult(
    val defenderWon: Boolean,
    val bloodlessVictory: Boolean,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val message: String
)

/**
 * 4계절 순환 및 지역 경제 엔진 (SeasonEngine)
 * - 4-turn seasonal cycle: Spring, Summer, Autumn, Winter.
 * - Spring: Tax collection from territory population and lords (Gold revenue).
 * - Summer: Flood control / dike maintenance public works, random disaster/bumper events.
 * - Autumn: Great agricultural harvest (Grain accumulation scaling with territory fertile score).
 * - Winter: Harsh winter troop upkeep (increased grain consumption and winter attrition risk).
 */
class SeasonEngine : ISeasonEngine {

    override fun processSeasonalCycle(
        currentSeason: Season,
        territory: Territory,
        lords: List<Lord>,
        dikeMaintenanceDone: Boolean,
        randomSeed: Long?
    ): SeasonActionResult {
        val totalTroops = lords.sumOf { it.troops }

        return when (currentSeason) {
            Season.SPRING -> {
                // 봄: 춘경 및 조세 징수. 인구당 0.05골드 + 호족 인두세 각 100골드
                val populationTax = (territory.population * 0.05).toInt()
                val lordTax = lords.size * 100
                val totalGoldRevenue = populationTax + lordTax
                // 영지 내 호족들에게 골드 균등 또는 총액 분배 (대표 호족/성주 증액)
                if (lords.isNotEmpty()) {
                    val share = totalGoldRevenue / lords.size
                    lords.forEach { it.gold += share }
                }

                SeasonActionResult(
                    season = Season.SPRING,
                    goldChange = totalGoldRevenue,
                    grainChange = 0,
                    message = "봄 춘경 조세 징수 완료: 영지 인구(${territory.population}명) 및 호족(${lords.size}명)으로부터 ${totalGoldRevenue} 금화 징수."
                )
            }

            Season.SUMMER -> {
                // 여름: 치수 사업 / 제방 보수 및 풍수해 이벤트
                val rng = if (randomSeed != null) java.util.Random(randomSeed) else java.util.Random()
                val dice = rng.nextInt(100)

                if (dikeMaintenanceDone) {
                    // 치수 공공사업 완료: 수해 방지 및 50% 확률로 기후 최적 풍년 징조
                    val isBumper = dice < 50
                    val disaster = if (isBumper) SummerDisasterType.BUMPER_WEATHER else SummerDisasterType.FLOOD_PREVENTED
                    val grainBonus = if (isBumper) 500 else 0
                    if (lords.isNotEmpty() && grainBonus > 0) {
                        val share = grainBonus / lords.size
                        lords.forEach { it.grain += share }
                    }

                    SeasonActionResult(
                        season = Season.SUMMER,
                        goldChange = -200, // 치수 사업 공공비용 지출
                        grainChange = grainBonus,
                        disasterType = disaster,
                        message = if (isBumper) {
                            "여름 치수 제방 정비 완료 및 최적의 기후: 홍수를 완벽 예방하고 호우를 관개용수로 활용 (+${grainBonus} 군량)."
                        } else {
                            "여름 치수 정비 완료: 격류를 막아내어 농경지 침수 피해를 전면 방지했습니다."
                        }
                    )
                } else {
                    // 치수 미실시: 60% 확률로 홍수 재해 발생
                    val isFlood = dice < 60
                    if (isFlood) {
                        val loss = (territory.population * 0.02).toInt() * 50
                        lords.forEach { it.grain = (it.grain - 300).coerceAtLeast(0) }
                        SeasonActionResult(
                            season = Season.SUMMER,
                            goldChange = -400,
                            grainChange = -loss,
                            disasterType = SummerDisasterType.FLOOD_DISASTER,
                            message = "여름 치수 미흡으로 제방 결괴: 대홍수로 농경지와 가옥 침수 (-${loss} 군량 피해)."
                        )
                    } else {
                        SeasonActionResult(
                            season = Season.SUMMER,
                            goldChange = 0,
                            grainChange = 0,
                            disasterType = SummerDisasterType.NONE,
                            message = "여름 무사 통과: 제방 정비가 미흡했으나 천행으로 큰 비 없이 지나갔습니다."
                        )
                    }
                }
            }

            Season.AUTUMN -> {
                // 가을: 대추수. 기본 인구 생산량 * 비옥도(fertileScore)
                val baseHarvest = (territory.population * 0.15).toInt()
                val totalHarvestGrain = (baseHarvest * territory.fertileScore).toInt()

                if (lords.isNotEmpty()) {
                    val share = totalHarvestGrain / lords.size
                    lords.forEach { it.grain += share }
                }

                SeasonActionResult(
                    season = Season.AUTUMN,
                    goldChange = 0,
                    grainChange = totalHarvestGrain,
                    message = "가을 대추수 풍년: 비옥도(${territory.fertileScore}) 반영 총 ${totalHarvestGrain} 군량 수확 비축 완료."
                )
            }

            Season.WINTER -> {
                // 겨울: 혹한기 병력 유지비 (1인당 1 군량 소모) 및 군량 부족 시 동사/탈영 감모
                val requiredGrain = (totalTroops * 1.0).toInt()
                val currentTotalGrain = lords.sumOf { it.grain }
                var attritionLosses = 0

                if (currentTotalGrain >= requiredGrain) {
                    // 군량 충분: 유지비 균등 차감
                    var remainingDeduct = requiredGrain
                    for (lord in lords) {
                        val deduct = (lord.grain.toDouble() / currentTotalGrain * requiredGrain).toInt().coerceAtMost(lord.grain)
                        lord.grain -= deduct
                        remainingDeduct -= deduct
                    }
                    if (remainingDeduct > 0 && lords.isNotEmpty()) {
                        lords[0].grain = (lords[0].grain - remainingDeduct).coerceAtLeast(0)
                    }

                    SeasonActionResult(
                        season = Season.WINTER,
                        goldChange = 0,
                        grainChange = -requiredGrain,
                        winterAttritionLosses = 0,
                        message = "혹한기 군량 보급 완료: 병력 유지비 ${requiredGrain} 군량을 성공적으로 소모했습니다."
                    )
                } else {
                    // 군량 부족: 전 군량 소진 및 부족분에 따른 한파 감모(겨울 탈영 및 아사)
                    val shortage = requiredGrain - currentTotalGrain
                    lords.forEach { it.grain = 0 }
                    attritionLosses = (shortage * 0.5).toInt().coerceAtLeast(1)

                    var remainingLoss = attritionLosses
                    for (lord in lords) {
                        val lordLoss = (lord.troops.toDouble() / totalTroops.coerceAtLeast(1) * attritionLosses).toInt().coerceAtMost(lord.troops)
                        lord.troops -= lordLoss
                        remainingLoss -= lordLoss
                    }
                    if (remainingLoss > 0 && lords.isNotEmpty()) {
                        lords[0].troops = (lords[0].troops - remainingLoss).coerceAtLeast(0)
                    }

                    SeasonActionResult(
                        season = Season.WINTER,
                        goldChange = 0,
                        grainChange = -currentTotalGrain,
                        winterAttritionLosses = attritionLosses,
                        message = "혹한기 군량 고갈 발생! 혹한 속 동사 및 탈영으로 병력 ${attritionLosses}명이 감모되었습니다."
                    )
                }
            }
        }
    }
}

/**
 * GIS 보급선 & 병참 수송 엔진 (LogisticsEngine)
 * - 隣接 영지 그래프 연결성 검증 (Adjacent territory graph connectivity verification).
 * - 수송대 호송 (후방에서 전방 거점으로 금화 및 군량 보급).
 * - 중간 거점 적 점령 시 보급선 단절, 사기 급감 및 무혈 항복(bloodless surrender) 유도.
 */
class LogisticsEngine : ILogisticsEngine {

    override fun verifySupplyConnectivity(
        originCode: String,
        destCode: String,
        territoryMap: Map<String, Territory>,
        hostileTerritoryCodes: Set<String>
    ): SupplyRouteStatus {
        if (originCode == destCode) {
            return SupplyRouteStatus(isConnected = true, path = listOf(originCode), message = "동일 거점 내 보급")
        }

        if (hostileTerritoryCodes.contains(originCode) || hostileTerritoryCodes.contains(destCode)) {
            val blocked = if (hostileTerritoryCodes.contains(originCode)) originCode else destCode
            return SupplyRouteStatus(
                isConnected = false,
                blockedByTerritoryCode = blocked,
                moralePenalty = 30,
                triggersBloodlessSurrender = true,
                message = "출발지 또는 목적지가 적군에 의해 차단되었습니다."
            )
        }

        // BFS search along adjacentCodes
        val queue: java.util.ArrayDeque<List<String>> = java.util.ArrayDeque()
        val visited = mutableSetOf<String>()
        queue.add(listOf(originCode))
        visited.add(originCode)

        var foundPath: List<String>? = null

        while (queue.isNotEmpty()) {
            val currentPath = queue.poll() ?: break
            val currentCode = currentPath.last()

            if (currentCode == destCode) {
                foundPath = currentPath
                break
            }

            val territory = territoryMap[currentCode] ?: continue
            for (neighborCode in territory.adjacentCodes) {
                // 적 점령 지역이면 진입 불가 (보급로 단절)
                if (hostileTerritoryCodes.contains(neighborCode)) {
                    continue
                }
                if (!visited.contains(neighborCode)) {
                    visited.add(neighborCode)
                    queue.add(currentPath + neighborCode)
                }
            }
        }

        return if (foundPath != null) {
            SupplyRouteStatus(
                isConnected = true,
                path = foundPath,
                message = "GIS 보급선 연결 정상 (${foundPath.size - 1}개 거점 경로 통과)"
            )
        } else {
            SupplyRouteStatus(
                isConnected = false,
                blockedByTerritoryCode = hostileTerritoryCodes.firstOrNull(),
                moralePenalty = 40,
                triggersBloodlessSurrender = true,
                message = "중간 거점이 적군에 점령되어 보급선이 완전히 차단되었습니다!"
            )
        }
    }

    override fun dispatchTransportConvoy(
        convoy: TransportConvoy,
        fromLord: Lord,
        toLord: Lord,
        routeStatus: SupplyRouteStatus
    ): ConvoyResult {
        require(convoy.gold >= 0 && convoy.grain >= 0) { "수송 물자는 음수일 수 없습니다." }
        if (fromLord.gold < convoy.gold || fromLord.grain < convoy.grain) {
            return ConvoyResult(
                success = false,
                deliveredGold = 0,
                deliveredGrain = 0,
                message = "출발지 자원 부족으로 수송대를 편성할 수 없습니다."
            )
        }

        // 출발 호족 자원 차감
        fromLord.gold -= convoy.gold
        fromLord.grain -= convoy.grain

        if (routeStatus.isConnected) {
            // 목적 호족 자원 적재
            toLord.gold += convoy.gold
            toLord.grain += convoy.grain
            return ConvoyResult(
                success = true,
                deliveredGold = convoy.gold,
                deliveredGrain = convoy.grain,
                message = "보급로를 통해 금화 ${convoy.gold}, 군량 ${convoy.grain}이 전방에 안전하게 도착했습니다."
            )
        } else {
            // 보급로 차단: 수송대 적에게 나포/소실
            return ConvoyResult(
                success = false,
                deliveredGold = 0,
                deliveredGrain = 0,
                message = "적군이 차단한 거점(${routeStatus.blockedByTerritoryCode ?: "적 점령지"})에서 수송대가 나포되었습니다! (자원 전량 손실)"
            )
        }
    }

    override fun applySupplyCutPenalties(lord: Lord, routeStatus: SupplyRouteStatus): Boolean {
        if (!routeStatus.isConnected) {
            lord.prestige = (lord.prestige - routeStatus.moralePenalty).coerceAtLeast(0)
            return routeStatus.triggersBloodlessSurrender
        }
        return false
    }
}

/**
 * 외교 & 첩보 엔진 (DiplomacyEngine)
 * - 불가침 조약, 방위 동맹, 속국 조공 조약
 * - 조약 기간 차감 및 조약 파기/위반 시 명성 페널티
 * - 인재 연계 첩보 (정보 정찰 및 이간계/민심 교란)
 */
class DiplomacyEngine : IDiplomacyEngine {

    override fun proposeTreaty(
        lord1: Lord,
        lord2: Lord,
        type: TreatyType,
        durationTurns: Int,
        tributaryGold: Int
    ): DiplomaticTreaty {
        require(durationTurns > 0) { "조약 기간은 최소 1턴 이상이어야 합니다." }
        require(tributaryGold >= 0) { "조공액은 음수일 수 없습니다." }

        return DiplomaticTreaty(
            id = "treaty_${System.currentTimeMillis()}_${lord1.id}_${lord2.id}",
            lordId1 = lord1.id,
            lordId2 = lord2.id,
            treatyType = type,
            remainingTurns = durationTurns,
            tributaryGoldPerTurn = tributaryGold
        )
    }

    override fun processTurnTreaties(
        treaties: List<DiplomaticTreaty>,
        lordsMap: Map<String, Lord>
    ): List<DiplomaticTreaty> {
        val activeTreaties = mutableListOf<DiplomaticTreaty>()

        for (treaty in treaties) {
            // 조공 조약 처리 (lordId2가 속국 -> lordId1 종주국으로 금화 상납)
            if (treaty.treatyType == TreatyType.VASSAL_TRIBUTARY && treaty.tributaryGoldPerTurn > 0) {
                val vassal = lordsMap[treaty.lordId2]
                val suzerain = lordsMap[treaty.lordId1]

                if (vassal != null && suzerain != null) {
                    val pay = treaty.tributaryGoldPerTurn.coerceAtMost(vassal.gold)
                    vassal.gold -= pay
                    suzerain.gold += pay
                }
            }

            treaty.remainingTurns -= 1
            if (treaty.remainingTurns > 0) {
                activeTreaties.add(treaty)
            }
        }

        return activeTreaties
    }

    override fun checkTreatyViolation(
        attackerId: String,
        targetLordId: String,
        treaties: List<DiplomaticTreaty>
    ): TreatyViolationResult {
        val violatedTreaty = treaties.firstOrNull { treaty ->
            (treaty.lordId1 == attackerId && treaty.lordId2 == targetLordId) ||
            (treaty.lordId2 == attackerId && treaty.lordId1 == targetLordId)
        }

        return if (violatedTreaty != null) {
            val penalty = when (violatedTreaty.treatyType) {
                TreatyType.NON_AGGRESSION_PACT -> 50
                TreatyType.DEFENSIVE_ALLIANCE -> 80
                TreatyType.VASSAL_TRIBUTARY -> 40
            }
            TreatyViolationResult(
                isViolated = true,
                violatorLordId = attackerId,
                prestigePenalty = penalty,
                message = "신의를 저버리고 ${violatedTreaty.treatyType.name}을(를) 일방 파기했습니다! (명성 -${penalty} 페널티)"
            )
        } else {
            TreatyViolationResult(
                isViolated = false,
                violatorLordId = null,
                prestigePenalty = 0,
                message = "유효한 외교 조약이 없는 상태에서의 교전입니다."
            )
        }
    }

    override fun conductEspionage(
        agent: Talent,
        targetLord: Lord,
        targetTerritory: Territory,
        opType: EspionageOpType
    ): EspionageResult {
        // 성공률 계산: 인재 지력과 매력 반영
        val baseSuccess = (agent.intelligence * 0.7 + agent.charm * 0.3).toInt()
        val success = baseSuccess >= 60

        return if (success) {
            when (opType) {
                EspionageOpType.GATHER_INTEL -> {
                    EspionageResult(
                        success = true,
                        opType = opType,
                        intelGathered = "적장 ${targetLord.name} 군세: 병력 ${targetLord.troops}명, 군량 ${targetLord.grain}, 금화 ${targetLord.gold}, 영지 비옥도 ${targetTerritory.fertileScore}",
                        message = "${agent.name} 첩보 공작 성공: 적군의 핵심 군사 기밀을 입수했습니다."
                    )
                }
                EspionageOpType.INSTIGATE_DISSENT -> {
                    val deduction = (agent.intelligence * 0.25).toInt().coerceAtLeast(10)
                    targetLord.prestige = (targetLord.prestige - deduction).coerceAtLeast(0)
                    EspionageResult(
                        success = true,
                        opType = opType,
                        moraleOrLoyaltyDeduction = deduction,
                        message = "${agent.name} 이간계 성공: ${targetLord.name} 진영 내부에 불신과 분열을 조장했습니다 (명성/사기 -${deduction})."
                    )
                }
            }
        } else {
            EspionageResult(
                success = false,
                opType = opType,
                message = "첩보 활동이 적 경계망에 포착되어 미수에 그쳤습니다."
            )
        }
    }
}
