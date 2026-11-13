package com.game.slg.ui

import androidx.lifecycle.ViewModel
import com.game.slg.core.engine.IDiplomacyEngine
import com.game.slg.core.engine.ILogisticsEngine
import com.game.slg.core.engine.ISeasonEngine
import com.game.slg.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * SLG 시뮬레이션 UI 상태
 */
data class SlgUiState(
    val currentSeason: Season = Season.SPRING,
    val treasuryGold: Int = 1000,
    val treasuryGrain: Int = 2000,
    val isSupplyConnected: Boolean = true,
    val statusLog: String = "팔도 영지전 SLG 시뮬레이터 준비 완료",
    val treaties: List<DiplomaticTreaty> = emptyList(),
    val territories: Map<String, Territory> = emptyMap(),
    val lords: List<Lord> = emptyList()
)

/**
 * 4계절 경제, GIS 보급 수송, 외교 엔진을 총괄하는 SlgViewModel
 */
class SlgViewModel(
    val seasonEngine: ISeasonEngine,
    val logisticsEngine: ILogisticsEngine,
    val diplomacyEngine: IDiplomacyEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<SlgUiState> = _uiState.asStateFlow()

    private fun createInitialState(): SlgUiState {
        // 기본 3개 거점 설정 (후방 본거지 t1 -> 중간 관문 t2 -> 최전방 전초 t3)
        val t1 = Territory(
            code = "t_rear_01",
            name = "아산시 배방읍 (후방 본거지)",
            province = "충남",
            cityCountyDistrict = "아산시",
            population = 50000,
            maxLords = 5,
            terrain = TerrainType.PLAIN,
            fertileScore = 1.2,
            adjacentCodes = listOf("t_mid_02")
        )
        val t2 = Territory(
            code = "t_mid_02",
            name = "천안시 풍세면 (중간 관문 요충)",
            province = "충남",
            cityCountyDistrict = "천안시",
            population = 25000,
            maxLords = 3,
            terrain = TerrainType.MOUNTAIN,
            fertileScore = 1.0,
            adjacentCodes = listOf("t_rear_01", "t_front_03")
        )
        val t3 = Territory(
            code = "t_front_03",
            name = "안성시 공도읍 (최전방 전초)",
            province = "경기",
            cityCountyDistrict = "안성시",
            population = 30000,
            maxLords = 3,
            terrain = TerrainType.PLAIN,
            fertileScore = 0.9,
            adjacentCodes = listOf("t_mid_02")
        )

        val playerLord = Lord(
            id = "lord_player",
            name = "충청 대호족 (플레이어)",
            isHumanPlayer = true,
            territoryCode = t1.code,
            troops = 1200,
            grain = 2000,
            gold = 1000,
            prestige = 80,
            isCastellan = true
        )
        val frontLord = Lord(
            id = "lord_front",
            name = "전방 파견장군",
            isHumanPlayer = false,
            territoryCode = t3.code,
            troops = 800,
            grain = 500,
            gold = 300,
            prestige = 60
        )
        val neighborLord = Lord(
            id = "lord_neighbor",
            name = "경기 호족 이경원",
            isHumanPlayer = false,
            territoryCode = "t_neighbor_04",
            troops = 1500,
            grain = 3000,
            gold = 2000,
            prestige = 75
        )

        return SlgUiState(
            currentSeason = Season.SPRING,
            treasuryGold = playerLord.gold,
            treasuryGrain = playerLord.grain,
            isSupplyConnected = true,
            territories = mapOf(t1.code to t1, t2.code to t2, t3.code to t3),
            lords = listOf(playerLord, frontLord, neighborLord)
        )
    }

    /**
     * 1. 4계절 사이클 진행 (봄: 조세, 여름: 치수/풍수해, 가을: 추수, 겨울: 유지비/한파 감모)
     */
    fun advanceSeason(dikeMaintenanceDone: Boolean = true) {
        _uiState.update { state ->
            val playerLord = state.lords.first { it.id == "lord_player" }
            val territory = state.territories[playerLord.territoryCode] ?: state.territories.values.first()

            val seasonResult = seasonEngine.processSeasonalCycle(
                currentSeason = state.currentSeason,
                territory = territory,
                lords = listOf(playerLord),
                dikeMaintenanceDone = dikeMaintenanceDone
            )

            // 외교 조약 턴 차감 및 조공 처리
            val lordsMap = state.lords.associateBy { it.id }
            val updatedTreaties = diplomacyEngine.processTurnTreaties(state.treaties, lordsMap)

            val nextSeason = state.currentSeason.next()
            state.copy(
                currentSeason = nextSeason,
                treasuryGold = playerLord.gold,
                treasuryGrain = playerLord.grain,
                treaties = updatedTreaties,
                statusLog = "[계절 순환: ${state.currentSeason.displayName}]\n${seasonResult.message}"
            )
        }
    }

    /**
     * 2. GIS 보급선 검증 및 수송대 파견
     */
    fun verifyAndDispatchConvoy(hostileCodes: Set<String>, sendGold: Int = 300, sendGrain: Int = 500) {
        _uiState.update { state ->
            val rearLord = state.lords.first { it.id == "lord_player" }
            val frontLord = state.lords.first { it.id == "lord_front" }

            val routeStatus = logisticsEngine.verifySupplyConnectivity(
                originCode = rearLord.territoryCode,
                destCode = frontLord.territoryCode,
                territoryMap = state.territories,
                hostileTerritoryCodes = hostileCodes
            )

            val convoy = TransportConvoy(
                id = "convoy_${System.currentTimeMillis()}",
                fromTerritoryCode = rearLord.territoryCode,
                toTerritoryCode = frontLord.territoryCode,
                gold = sendGold,
                grain = sendGrain
            )

            val convoyResult = logisticsEngine.dispatchTransportConvoy(convoy, rearLord, frontLord, routeStatus)
            if (!routeStatus.isConnected) {
                logisticsEngine.applySupplyCutPenalties(frontLord, routeStatus)
            }

            state.copy(
                isSupplyConnected = routeStatus.isConnected,
                treasuryGold = rearLord.gold,
                treasuryGrain = rearLord.grain,
                statusLog = "[GIS 보급선 & 수송대 결과]\n경로 연결 상태: ${if (routeStatus.isConnected) "정상 연결" else "적 점령 단절!"}\n${convoyResult.message}"
            )
        }
    }

    /**
     * 3. 외교 조약 체결
     */
    fun signTreaty(type: TreatyType, durationTurns: Int = 4, tributaryGold: Int = 100) {
        _uiState.update { state ->
            val playerLord = state.lords.first { it.id == "lord_player" }
            val neighborLord = state.lords.first { it.id == "lord_neighbor" }

            val newTreaty = diplomacyEngine.proposeTreaty(
                lord1 = playerLord,
                lord2 = neighborLord,
                type = type,
                durationTurns = durationTurns,
                tributaryGold = tributaryGold
            )

            state.copy(
                treaties = state.treaties + newTreaty,
                statusLog = "[외교 조약 체결 완료]\n조약 유형: ${type.name}, 유효 기간: ${durationTurns}턴 (상대: ${neighborLord.name})"
            )
        }
    }

    /**
     * 4. 첩보 공작 수행
     */
    fun conductEspionage(opType: EspionageOpType) {
        _uiState.update { state ->
            val playerLord = state.lords.first { it.id == "lord_player" }
            val neighborLord = state.lords.first { it.id == "lord_neighbor" }
            val targetTerritory = Territory(
                code = neighborLord.territoryCode,
                name = "적성 거점",
                province = "경기",
                cityCountyDistrict = "수원시",
                population = 45000,
                maxLords = 4,
                terrain = TerrainType.PLAIN,
                fertileScore = 1.1
            )

            val spyTalent = Talent(
                id = "talent_spy_01",
                name = "밀정 강호인",
                isPlayerHiddenTalent = true,
                warPower = 70,
                intelligence = 92,
                leadership = 80,
                politics = 85,
                charm = 88,
                employerLordId = playerLord.id
            )

            val espionageResult = diplomacyEngine.conductEspionage(
                agent = spyTalent,
                targetLord = neighborLord,
                targetTerritory = targetTerritory,
                opType = opType
            )

            state.copy(
                statusLog = "[첩보 공작 결과]\n${espionageResult.message}${espionageResult.intelGathered?.let { "\n정보: $it" } ?: ""}"
            )
        }
    }
}
