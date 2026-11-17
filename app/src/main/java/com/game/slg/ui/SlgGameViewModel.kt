package com.game.slg.ui

import androidx.lifecycle.ViewModel
import com.game.slg.core.engine.BuildValidation
import com.game.slg.core.engine.CombatEngine
import com.game.slg.core.engine.CombatEngineContract
import com.game.slg.core.engine.CouncilEngine
import com.game.slg.core.engine.CouncilEngineContract
import com.game.slg.core.engine.DiplomacyEngine
import com.game.slg.core.engine.FacilityEngine
import com.game.slg.core.engine.IDiplomacyEngine
import com.game.slg.core.engine.IHiddenTalentNightEngine
import com.game.slg.core.engine.IModernBattleEngine
import com.game.slg.core.engine.IPromotionEngine
import com.game.slg.core.engine.IRefugeeCrisisEngine
import com.game.slg.core.engine.ISeasonEngine
import com.game.slg.core.engine.HiddenTalentNightEngine
import com.game.slg.core.engine.ModernBattleEngine
import com.game.slg.core.engine.ModernBattleResult
import com.game.slg.core.engine.PromotionEngine
import com.game.slg.core.engine.RefugeeCrisisEngine
import com.game.slg.core.engine.SeasonEngine
import com.game.slg.core.map.MapNode
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.model.BillType
import com.game.slg.core.model.CombatArm
import com.game.slg.core.model.CouncilBill
import com.game.slg.core.model.DiplomaticTreaty
import com.game.slg.core.model.EspionageOpType
import com.game.slg.core.model.EspionageResult
import com.game.slg.core.model.FacilityType
import com.game.slg.core.model.ForceComposition
import com.game.slg.core.model.Fortress
import com.game.slg.core.model.FortressLevel
import com.game.slg.core.model.Lord
import com.game.slg.core.model.NightMeetingChoice
import com.game.slg.core.model.NightMeetingResult
import com.game.slg.core.model.PromotionEvaluation
import com.game.slg.core.model.PromotionResult
import com.game.slg.core.model.PromotionStage
import com.game.slg.core.model.RefugeeCrisisResult
import com.game.slg.core.model.Season
import com.game.slg.core.model.Talent
import com.game.slg.core.model.TerrainType
import com.game.slg.core.model.Territory
import com.game.slg.core.model.TreatyType
import com.game.slg.core.region.AiLordGenerator
import com.game.slg.core.region.AiLordProfile
import com.game.slg.core.region.CommandProgress
import com.game.slg.core.region.ExpansionTier
import com.game.slg.core.region.LordCapacity
import com.game.slg.core.region.RegionArea
import com.game.slg.core.region.RegionCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ============================================================================
// 세계관 — 「단일 연방의 황혼」 2020년대 대한민국, 기초 자치구 군웅할거
// ============================================================================

/** 지도 위 거점의 관계 표시 */
enum class Relation(val label: String) {
    ALLIED("아군 거점"),
    NEUTRAL_MERCHANT("중립 물류"),
    WARY("경계 세력"),
    CIVIC("시민 자치회"),
    OBSERVED("관찰 구역"),
    LOCKED("미개방"),
    MOUNTAIN_GATE("산악 관문");

    companion object {
        fun fromLabel(label: String): Relation =
            entries.firstOrNull { it.label == label } ?: OBSERVED
    }
}

/**
 * 4대 테크트리 한 단계 — 봉건 기술(향약·호패·화약)을 모두 현대 인프라로 교체.
 */
data class TechItem(
    val id: String,
    val branch: String,
    val stage: Int,
    val name: String,
    val description: String,
    val cost: Int,
    val effect: String
)

/** 호족 의회 안건 상태 */
data class BillState(
    val id: String,
    val title: String,
    val type: BillType,
    val levy: Int,
    val favor: Int = 0,
    val against: Int = 0,
    val passed: Boolean? = null,
    val rebellion: Boolean = false,
    val message: String? = null
)

/** 작전 실행 결과 */
data class OperationReport(
    val operation: String,
    val defenderWon: Boolean,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val message: String,
    val verdict: String,
    val targetName: String = "",
    val composition: String = "",
    val requiredLevel: Int = 1
)

/** 외교 조약 표시용 */
data class TreatyView(
    val id: String,
    val typeLabel: String,
    val partner: String,
    val remainingTurns: Int,
    val benefit: String
)

/** 히든 인재(위장 신분) 동향 */
data class HiddenTalentView(
    val name: String,
    val cover: String,
    val hint: String,
    val suspicion: Int
)

/** 게임 시작 흐름 */
enum class GameStage(val title: String) {
    INTRO("인트로"),
    REGION_SELECT("지역 선택"),
    LORD_SELECT("호족 선택"),
    PLAY("플레이")
}

/** 호족 명부 1줄 — AI 호족과 플레이어가 같은 형식으로 보인다 */
data class LordView(
    val id: String,
    val name: String,
    val stronghold: String,
    val traitLabel: String,
    val traitIcon: String,
    val troops: Int,
    val gold: Int,
    val grain: Int,
    val prestige: Int,
    val arms: String,
    val isPlayer: Boolean = false
)

object SlgWorld {

    val TECH_ITEMS: List<TechItem> = listOf(
        // ── 내정 (행정·재정) ──
        TechItem("admin_1", "내정", 1, "통합 관제센터", "분산된 상황실을 하나로 묶어 거점을 실시간 관리한다.", 400, "절기 자금 +8%"),
        TechItem("admin_2", "내정", 2, "스마트 민원행정", "주민 신청·허가를 온라인으로 처리해 행정 부담을 던다.", 700, "자금 +150/절기"),
        TechItem("admin_3", "내정", 3, "지방세 재정 최적화", "세목을 정리해 자치구 재정을 안정화한다.", 1200, "자금 +300/절기 · 민심 +3"),
        TechItem("admin_4", "내정", 4, "주민 데이터 플랫폼", "인구·이동 데이터를 통합해 정책과 첩보 정확도를 높인다.", 1800, "민심 +6 · 첩보 방어 +15%"),
        // ── 과학기술 ──
        TechItem("sci_1", "과학기술", 1, "통신망 복구", "끊긴 기지국과 광케이블을 복구해 지휘 통신을 살린다.", 500, "지휘 경험 +5%"),
        TechItem("sci_2", "과학기술", 2, "드론 정비 공장", "정찰·자폭 드론을 자체 정비·생산한다.", 900, "드론 전투력 +10%"),
        TechItem("sci_3", "과학기술", 3, "위성·GIS 정찰", "위성 영상과 GIS로 보급로와 지형을 정밀 분석한다.", 1300, "수송 손실 -20%"),
        TechItem("sci_4", "과학기술", 4, "AI 지휘 지원체계", "전장 데이터를 학습해 작전 판단을 보조한다.", 2000, "작전 피해 -10%"),
        // ── 문화 (시민·여론) ──
        TechItem("cul_1", "문화", 1, "지역 커뮤니티 센터", "주민 공간을 열어 자치 참여를 끌어낸다.", 350, "민심 +4"),
        TechItem("cul_2", "문화", 2, "시민 안전 교육", "대피·구호 훈련으로 재난 대응력을 높인다.", 800, "재난 피해 -15%"),
        TechItem("cul_3", "문화", 3, "언론·홍보 네트워크", "지역 매체와 채널을 확보해 여론을 주도한다.", 1300, "외교 +8"),
        TechItem("cul_4", "문화", 4, "통합 브랜드", "연결된 구역을 하나의 이름으로 묶는다.", 1900, "점령지 민심 +12"),
        // ── 군사 (현대 편제) ──
        TechItem("mil_1", "군사", 1, "기동보병 편제", "K2 소총·K808 방호차량으로 기동 타격조를 편성한다.", 450, "방어 시 사상자 -8%"),
        TechItem("mil_2", "군사", 2, "기계화 기갑", "K2 전차·K21 전투차를 투입해 정면 돌파력을 얻는다.", 850, "적 사상자 +12%"),
        TechItem("mil_3", "군사", 3, "화력지원 사격통제", "K9·천무의 사격 절차를 자동화한다.", 1400, "화력 피해 +15%"),
        TechItem("mil_4", "군사", 4, "드론·방공 통합", "드론 타격과 방공망을 한 체계로 묶는다.", 2100, "방공·드론 +30%")
    )

    val HIDDEN_TALENTS: List<HiddenTalentView> = listOf(
        HiddenTalentView("한도윤", "물류단지 회계 서기", "야간에 경비 인력 40명을 모집한 정황 · 소문 미확산", 62),
        HiddenTalentView("서이레", "구역 자치회 회계 담당", "호족연합의 병력 장부를 이중으로 관리 중", 48),
        HiddenTalentView("문태오", "중앙로 상인", "최근 인접 구역 인사와 접촉 빈도 급증", 33)
    )

    /** 인구 1,000명당 호족 1명 — 화면 안내용 */
    const val LORD_RULE_LABEL = "인구 1,000명당 1호족"
}

// ============================================================================
// UI 상태
// ============================================================================

data class SlgGameState(
    // ── 시작 흐름 ──
    val stage: GameStage = GameStage.INTRO,
    val introSeen: Boolean = false,
    val introReplay: Boolean = false,
    // ── 시작 지역 ──
    val regionCode: String = RegionCatalog.DEFAULT_AREA_CODE,
    val regionName: String = "탕정면",
    val regionFullName: String = "충청남도 아산시 탕정면",
    val regionGroupKey: String = "충청남도 아산시",
    val regionPopulation: Int = 48604,
    val lordCapacity: Int = LordCapacity.of(48604),
    // ── 호족 명부 ──
    val lords: List<LordView> = emptyList(),
    val playerLordId: String? = null,
    val selectedLordId: String? = null,
    val registeredLords: Int = 0,
    // ── 지휘 성장·해금 ──
    val command: CommandProgress = CommandProgress(),
    val unlockedHop: Int = 0,
    val levelUpMessage: String? = null,
    // ── 시설 ──
    val facilities: Set<FacilityType> = emptySet(),
    // ── 지도 ──
    val mapNodes: List<MapNode> = emptyList(),
    val selectedTerritoryCode: String = RegionCatalog.DEFAULT_AREA_CODE,
    // ── 기본 지표 ──
    val seasonName: String = "봄 (예산 편성·지방세 징수)",
    val population: Int = 48604,
    val gold: Int = 4200,
    val food: Int = 8600,
    val troops: Int = 1200,
    val morale: Int = 62,
    val prestige: Int = 74,
    val diplomacy: Int = 55,
    val enemyFood: Int = 2400,
    val statusMessage: String = "「단일 연방의 황혼」 D+14 — 관할 구역에서 하루가 시작됩니다.",
    val log: List<String> = listOf("[D+14] 연방 해체 14일차. 기초 자치구마다 자체 경비 조직을 세웠다."),
    val eventAChoice: String? = null,
    val eventAResult: String? = null,
    val eventBChoice: String? = null,
    val eventBResult: String? = null,
    val intelGained: Boolean = false,
    val researchedTechs: Set<String> = emptySet(),
    val bills: List<BillState> = listOf(
        BillState("bill_levy", "경비 인력 소집안", BillType.TROOP_LEVY, levy = 40),
        BillState("bill_sec", "치안 유지 특별 조례", BillType.SPECIAL_TAX, levy = 0)
    ),
    val lastReport: OperationReport? = null,
    val treaties: List<TreatyView> = emptyList(),
    val casusBelli: String? = null,
    val espionageMessage: String? = null,
    val espionageCount: Int = 0,
    val hiddenTalents: List<HiddenTalentView> = SlgWorld.HIDDEN_TALENTS,
    val culturePrestige: Int = 48,
    val refugeeChoice: String? = null,
    val refugeeResult: String? = null,
    val nightMeetingTalentName: String? = null,
    val nightMeetingChoice: String? = null,
    val nightMeetingResult: String? = null,
    val espionagePower: Int = 40,
    val doubleAgentBuff: String? = null,
    val alliedTerritories: Int = 0,
    val promotionStage: PromotionStage = PromotionStage.DISTRICT_LORD,
    val councilAuthorityBonus: Int = 0,
    val promotionCeremony: String? = null,
    val promotionMessage: String? = null
) {
    /** 선택된 지도 거점 */
    val selectedTerritory: MapNode?
        get() = TerritoryMapLayout.node(mapNodes, selectedTerritoryCode)

    val selectedMapNode: MapNode?
        get() = selectedTerritory

    val homeNode: MapNode?
        get() = TerritoryMapLayout.home(mapNodes)

    val regionArea: RegionArea?
        get() = RegionCatalog.area(regionCode)

    val playerLord: LordView?
        get() = lords.firstOrNull { it.id == playerLordId }

    val isCampaignStarted: Boolean
        get() = playerLordId != null

    /** 현재 지휘 단계 */
    val expansionTier: ExpansionTier
        get() = command.tier

    /** 화면에 보이는 잠긴 거점 수 */
    val lockedNodeCount: Int
        get() = mapNodes.count { it.isLocked }

    val unlockedNodeCount: Int
        get() = mapNodes.count { it.unlocked }

    /** 같은 구역에서 이미 인계된 호족(플레이어)을 뺀 AI 운영 수 */
    val aiLordCount: Int
        get() = (lordCapacity - if (isCampaignStarted) 1 else 0).coerceAtLeast(0)

    /** 다음 해금 단계 */
    val nextTier: ExpansionTier?
        get() = expansionTier.next

    val isRefugeeAccepted: Boolean get() = refugeeChoice == "REFUGEE_A_OPEN"

    val nextPromotionStage: PromotionStage? get() = promotionStage.nextStage

    val isPromotionEligible: Boolean
        get() = nextPromotionStage?.let {
            alliedTerritories >= it.requiredAlliedTerritories && prestige >= it.requiredPrestige
        } ?: false

    val nightMeetingCandidates: List<HiddenTalentView>
        get() = hiddenTalents.filter { it.suspicion >= NIGHT_MEETING_SUSPICION }

    /** 조약으로 우호 관계인 거점 이름 목록 */
    val alliedNames: List<String>
        get() = treaties.map { it.partner }.distinct()

    companion object {
        const val NIGHT_MEETING_SUSPICION: Int = 50
    }
}

// ============================================================================
// ViewModel — 인트로 → 지역 선택 → 호족 인계 → 4탭 영지 운영
// ============================================================================

class SlgGameViewModel(
    private val seasonEngine: ISeasonEngine = SeasonEngine(),
    private val combatEngine: CombatEngineContract = CombatEngine(),
    private val modernBattleEngine: IModernBattleEngine = ModernBattleEngine(),
    private val facilityEngine: FacilityEngine = FacilityEngine(),
    private val councilEngine: CouncilEngineContract = CouncilEngine(),
    private val diplomacyEngine: IDiplomacyEngine = DiplomacyEngine(),
    private val refugeeCrisisEngine: IRefugeeCrisisEngine = RefugeeCrisisEngine(),
    private val nightMeetingEngine: IHiddenTalentNightEngine = HiddenTalentNightEngine(),
    private val promotionEngine: IPromotionEngine = PromotionEngine()
) : ViewModel() {

    private val startRegion = RegionCatalog.defaultRegion()
    private val startLords = AiLordGenerator.generate(startRegion)

    private val _uiState = MutableStateFlow(
        SlgGameState(
            regionCode = startRegion.code,
            regionName = startRegion.name,
            regionFullName = startRegion.fullName,
            regionGroupKey = startRegion.groupKey,
            regionPopulation = startRegion.population,
            lordCapacity = startRegion.lordCapacity,
            population = startRegion.population,
            lords = startLords.map { it.toView() },
            registeredLords = 0,
            mapNodes = TerritoryMapLayout.build(
                start = startRegion,
                unlockedHop = 0,
                playerName = "이광수",
                homeTroops = 1200
            ),
            selectedTerritoryCode = startRegion.code
        )
    )
    val uiState: StateFlow<SlgGameState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow(MainNavigationState())
    val navigation: StateFlow<MainNavigationState> = _navigation.asStateFlow()

    private var season: Season = Season.SPRING
    private val treaties = mutableListOf<DiplomaticTreaty>()

    // ──────────────────────────────────────────────────────────────
    // 시작 흐름: 인트로 → 지역 선택 → 호족 인계
    // ──────────────────────────────────────────────────────────────

    /** 인트로 마지막 장면 또는 SKIP */
    fun finishIntro() {
        _uiState.update { s ->
            s.copy(
                stage = if (s.isCampaignStarted || s.introReplay) GameStage.PLAY else GameStage.REGION_SELECT,
                introSeen = true,
                introReplay = false
            )
        }
    }

    /** 📊 정보 → 인트로 다시보기 */
    fun replayIntro() {
        _uiState.update { it.copy(stage = GameStage.INTRO, introReplay = it.isCampaignStarted) }
    }

    /** 시·도/시·군·구 목록 */
    fun provinces(): List<String> = RegionCatalog.PROVINCES

    fun groupsOf(province: String) = RegionCatalog.groupsOfProvince(province)

    fun areasOf(groupKey: String) = RegionCatalog.areasOfGroup(groupKey)

    /**
     * 시작 지역 선택 — 인구에 맞는 호족 정원을 계산하고 AI 호족 명부를 새로 만든다.
     */
    fun chooseStartRegion(regionCode: String) {
        val region = RegionCatalog.area(regionCode) ?: return
        val lords = AiLordGenerator.generate(region)
        _uiState.update { s ->
            s.copy(
                stage = GameStage.LORD_SELECT,
                regionCode = region.code,
                regionName = region.name,
                regionFullName = region.fullName,
                regionGroupKey = region.groupKey,
                regionPopulation = region.population,
                population = region.population,
                lordCapacity = region.lordCapacity,
                lords = lords.map { it.toView() },
                playerLordId = null,
                selectedLordId = null,
                registeredLords = 0,
                command = CommandProgress(),
                unlockedHop = 0,
                facilities = emptySet(),
                mapNodes = TerritoryMapLayout.build(region, 0, "이광수", 0),
                selectedTerritoryCode = region.code,
                statusMessage = "[지역 선택] ${region.fullName} — 인구 ${region.population}명 · 호족 ${region.lordCapacity}석",
                log = listOf("[D+14] ${region.fullName}에 ${region.lordCapacity}명의 호족이 각자 거점을 세웠다.")
            )
        }
    }

    /** 명부에서 미리 보기 */
    fun selectLordPreview(lordId: String) {
        _uiState.update { s ->
            if (s.lords.none { it.id == lordId }) s
            else s.copy(selectedLordId = lordId)
        }
    }

    /** 지역 선택으로 되돌아가기 */
    fun backToRegionSelect() {
        _uiState.update { it.copy(stage = GameStage.REGION_SELECT) }
    }

    /**
     * AI 호족 하나를 인계받아 플레이어 세력으로 시작.
     * 인계 시 그 호족의 병력·자금·보급·명성을 그대로 물려받는다.
     */
    fun chooseLord(lordId: String) {
        _uiState.update { s ->
            val chosen = s.lords.firstOrNull { it.id == lordId } ?: return@update s
            val playerName = "이광수"
            val updatedLords = s.lords.map { lord ->
                if (lord.id == lordId) lord.copy(name = playerName, isPlayer = true)
                else lord.copy(isPlayer = false)
            }
            val region = s.regionArea ?: RegionCatalog.defaultRegion()
            val command = CommandProgress()
            s.copy(
                stage = GameStage.PLAY,
                playerLordId = lordId,
                selectedLordId = lordId,
                lords = updatedLords,
                registeredLords = 1,
                gold = chosen.gold,
                food = chosen.grain,
                troops = chosen.troops,
                prestige = chosen.prestige,
                morale = (55 + chosen.prestige / 5).coerceIn(0, 100),
                command = command,
                unlockedHop = command.unlockedHop,
                mapNodes = TerritoryMapLayout.build(region, command.unlockedHop, playerName, chosen.troops),
                selectedTerritoryCode = s.regionCode,
                statusMessage = "[인계 완료] ${chosen.stronghold} — ${chosen.name} 호족의 거점을 이어받았습니다.",
                log = (s.log + "[인계] ${s.regionFullName} ${chosen.stronghold}의 ${chosen.name} 호족 세력을 인계받았다. " +
                    "Lv.1 관할 구역부터 시작합니다.").takeLast(8)
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 메인 3단 화면
    // ──────────────────────────────────────────────────────────────

    fun selectTerritory(code: String) {
        _uiState.update { s ->
            val node = TerritoryMapLayout.node(s.mapNodes, code) ?: return@update s
            if (!node.unlocked) {
                return@update s.copy(
                    statusMessage = "[미개방] '${node.name}'은(는) Lv.${tierForHop(node.hopDistance)} " +
                        "${tierForHopLabel(node.hopDistance)} 단계에서 열립니다."
                )
            }
            if (s.selectedTerritoryCode == node.code) s
            else s.copy(
                selectedTerritoryCode = node.code,
                statusMessage = "[지도] '${node.name}' 선택 — ${node.relationLabel}"
            )
        }
    }

    fun openSubMenu(menu: SubMenu) {
        _navigation.update { it.open(menu) }
    }

    fun backToMain() {
        _navigation.update { it.back() }
    }

    // ──────────────────────────────────────────────────────────────
    // 영지·지도
    // ──────────────────────────────────────────────────────────────

    /** 절기 진행 — 4계절 순환 (봄 세수 · 여름 재난 · 가을 결산 · 겨울 유지비) */
    fun advanceSeason(dikeMaintenanceDone: Boolean = true) {
        _uiState.update { s ->
            val playerName = s.playerLord?.name ?: "이광수"
            val playerLord = Lord(
                id = "lord_player",
                name = "$playerName 호족",
                isHumanPlayer = true,
                territoryCode = s.regionCode,
                troops = s.troops,
                grain = s.food,
                gold = s.gold,
                prestige = s.prestige,
                isCastellan = true
            )
            val economy = facilityEngine.economyBonus(s.facilities)
            val territory = buildTerritory(
                area = s.regionArea ?: RegionCatalog.defaultRegion(),
                fortress = homeFortress(s),
                fertileScore = 1.0 + economy
            )
            val result = seasonEngine.processSeasonalCycle(
                currentSeason = season,
                territory = territory,
                lords = listOf(playerLord),
                dikeMaintenanceDone = dikeMaintenanceDone,
                randomSeed = 20261114L
            )
            val nextSeason = season.next()
            val grainGain = (result.grainChange * (1.0 + economy)).toInt()
            val before = s.command
            val gained = before.gainXp(CommandProgress.XP_SEASON)
            val message = "[절기 순환: ${season.displayName}] ${result.message}"
            season = nextSeason
            s.copy(
                seasonName = nextSeason.displayName,
                gold = playerLord.gold,
                food = (playerLord.grain + (grainGain - result.grainChange)).coerceAtLeast(0),
                command = gained,
                unlockedHop = gained.unlockedHop,
                mapNodes = if (gained.level != before.level) {
                    TerritoryMapLayout.build(
                        start = s.regionArea ?: RegionCatalog.defaultRegion(),
                        unlockedHop = gained.unlockedHop,
                        playerName = playerName,
                        homeTroops = playerLord.troops
                    )
                } else s.mapNodes,
                levelUpMessage = if (gained.level != before.level) levelUpText(gained) else s.levelUpMessage,
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
    }

    /** 이벤트 A — 인접 물류 상인회의 야간 보급 협약 */
    fun chooseEventA(acceptPact: Boolean) {
        _uiState.update { s ->
            if (s.eventAChoice != null) {
                return@update s.copy(statusMessage = "[이벤트 A] 이미 처분한 안건입니다.")
            }
            val partner = firstNeighborName(s)
            if (acceptPact) {
                s.copy(
                    eventAChoice = "A_PACT",
                    eventAResult = "[상호 보급 협정 체결] 통행료를 면제하고 야간 물류로를 열었다. $partner 물류 상인회가 " +
                        "관할 창고에 식량과 연료를 채워 넣는다. 자금 -600, 보급 +1,200, 민심 +8, 외교 +12. " +
                        "$partner 은(는) '협력 물류'로 전환.",
                    gold = (s.gold - 600).coerceAtLeast(0),
                    food = s.food + 1200,
                    morale = (s.morale + 8).coerceAtMost(100),
                    diplomacy = (s.diplomacy + 12).coerceAtMost(100),
                    statusMessage = "[이벤트 A] 통행료 면제·상호 보급 협정 체결 — 보급 +1,200"
                ).let { next -> next.copy(alliedTerritories = alliedTerritoryCount(next)) }
            } else {
                s.copy(
                    eventAChoice = "A_TARIFF",
                    eventAResult = "[통행료 징수] 야간 통행 차량에 통행료를 매기고 비축고를 채웠다. 단기 자금은 " +
                        "늘었지만 상인들의 시선이 차가워졌다. 자금 +900, 민심 -10, 외교 -15. " +
                        "$partner 은(는) '경계 세력'으로 전환.",
                    gold = s.gold + 900,
                    morale = (s.morale - 10).coerceAtLeast(0),
                    diplomacy = (s.diplomacy - 15).coerceAtLeast(0),
                    statusMessage = "[이벤트 A] 통행료 징수 및 자체 비축 — 자금 +900"
                )
            }
        }
    }

    /** 이벤트 B — 인접 구역의 무인기 정찰조 출현 */
    fun chooseEventB(ambush: Boolean) {
        _uiState.update { s ->
            if (s.eventBChoice != null) {
                return@update s.copy(statusMessage = "[이벤트 B] 이미 처분한 안건입니다.")
            }
            val scoutName = secondNeighborName(s)
            if (ambush) {
                s.copy(
                    eventBChoice = "B_AMBUSH",
                    eventBResult = "[무인기 요격] 대드론 재밍으로 정찰 드론 3기를 불시착시켰다. " +
                        "아군 피해 8명, 노획 장비 250G 회수, 명성 +6. $scoutName 은(는) 보복을 준비한다.",
                    troops = (s.troops - 8).coerceAtLeast(0),
                    gold = s.gold + 250,
                    prestige = (s.prestige + 6).coerceAtMost(100),
                    morale = (s.morale + 2).coerceAtMost(100),
                    statusMessage = "[이벤트 B] 무인기 요격 — 병력 -8, 노획 250G"
                )
            } else {
                s.copy(
                    eventBChoice = "B_SCOUT",
                    eventBResult = "[첩보 파견] 물류 기사로 위장한 인력이 $scoutName 인근에 잠입했다. " +
                        "정찰조는 사전 답사조였고, 외곽에 연료·탄약 집적지가 세워지고 있다. " +
                        "자금 -200, 민심 +3, 적 보급 거점 위치 확보.",
                    gold = (s.gold - 200).coerceAtLeast(0),
                    morale = (s.morale + 3).coerceAtMost(100),
                    intelGained = true,
                    statusMessage = "[이벤트 B] 첩보 파견 — 적 보급 거점 위치 확보 (자금 -200)"
                )
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 내정·의회·시설
    // ──────────────────────────────────────────────────────────────

    fun research(techId: String) {
        _uiState.update { s ->
            val tech = SlgWorld.TECH_ITEMS.firstOrNull { it.id == techId } ?: return@update s
            if (s.researchedTechs.contains(techId)) {
                return@update s.copy(statusMessage = "[연구] 이미 완료된 기술입니다.")
            }
            val previous = SlgWorld.TECH_ITEMS.firstOrNull {
                it.branch == tech.branch && it.stage == tech.stage - 1
            }
            if (previous != null && !s.researchedTechs.contains(previous.id)) {
                return@update s.copy(statusMessage = "[연구 불가] 선행 기술 '${previous.name}'을(를) 먼저 연구해야 합니다.")
            }
            if (s.gold < tech.cost) {
                return@update s.copy(statusMessage = "[연구 불가] 자금이 부족합니다. (필요 ${tech.cost}G / 보유 ${s.gold}G)")
            }
            val message = "[연구 완료] ${tech.branch} ${tech.stage}단계 '${tech.name}' — ${tech.effect}"
            s.copy(
                gold = s.gold - tech.cost,
                researchedTechs = s.researchedTechs + techId,
                morale = if (tech.branch == "문화") (s.morale + 2).coerceAtMost(100) else s.morale,
                diplomacy = if (tech.branch == "문화") (s.diplomacy + 2).coerceAtMost(100) else s.diplomacy,
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
    }

    /** 시설 건설 — 지휘통제실·드론 격납고·방공 레이더망 등 */
    fun buildFacility(type: FacilityType): BuildValidation {
        var validation = BuildValidation(false, type.cost, "건설할 수 없습니다.")
        _uiState.update { s ->
            validation = facilityEngine.validate(type, s.gold, s.facilities)
            if (!validation.canBuild) {
                return@update s.copy(statusMessage = "[시설] ${validation.reason}")
            }
            val before = s.command
            val gained = before.gainXp(CommandProgress.XP_FACILITY)
            s.copy(
                facilities = s.facilities + type,
                gold = s.gold - validation.cost,
                morale = (s.morale + facilityEngine.moraleBonus(setOf(type))).coerceIn(0, 100),
                espionagePower = (s.espionagePower + facilityEngine.intelBonus(setOf(type))).coerceIn(0, 100),
                command = gained,
                unlockedHop = gained.unlockedHop,
                levelUpMessage = if (gained.level != before.level) levelUpText(gained) else s.levelUpMessage,
                log = (s.log + "[시설] ${type.displayName} 건설 — ${type.effectLabel}").takeLast(8),
                statusMessage = "[시설] ${type.displayName} 건설 완료 — ${type.effectLabel}"
            )
        }
        return validation
    }

    /** 호족 의회 안건 표결 */
    fun voteBill(billId: String) {
        _uiState.update { s ->
            val state = s.bills.firstOrNull { it.id == billId } ?: return@update s
            if (state.passed != null) {
                return@update s.copy(statusMessage = "[의회] 이미 표결이 끝난 안건입니다. (${if (state.passed) "가결" else "부결"})")
            }
            val bill = CouncilBill(
                id = state.id,
                title = state.title,
                type = state.type,
                proposedByCastellanId = "castellan_player",
                troopLevyAmount = state.levy
            )
            val lords = buildCouncilLords(s)
            val result = councilEngine.voteOnBill(bill, lords)

            val passed = result.isPassed
            val rebellion = result.isRebellionTriggered
            val message = buildString {
                append("[호족 의회 표결] '${state.title}' — 찬성 ${result.votesInFavor} : 반대 ${result.votesAgainst} → ")
                append(if (passed) "가결" else "부결")
                if (rebellion) append("\n반대 65% 이상! 반대 호족들이 연합해 대표 호족에 불복합니다.")
            }

            var gold = s.gold
            var troops = s.troops
            var morale = s.morale
            var prestige = s.prestige
            if (passed) {
                when (state.type) {
                    BillType.TROOP_LEVY -> troops += state.levy
                    BillType.SPECIAL_TAX -> gold += 700
                    else -> Unit
                }
                morale = (morale - 3).coerceAtLeast(0)
            }
            if (rebellion) {
                morale = (morale - 8).coerceAtLeast(0)
                prestige = (prestige - 5).coerceAtLeast(0)
            }

            val before = s.command
            val gained = before.gainXp(CommandProgress.XP_COUNCIL)

            s.copy(
                bills = s.bills.map {
                    if (it.id == billId) it.copy(
                        favor = result.votesInFavor,
                        against = result.votesAgainst,
                        passed = passed,
                        rebellion = rebellion,
                        message = message
                    ) else it
                },
                gold = gold,
                troops = troops,
                morale = morale,
                prestige = prestige,
                command = gained,
                unlockedHop = gained.unlockedHop,
                levelUpMessage = if (gained.level != before.level) levelUpText(gained) else s.levelUpMessage,
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 전장·전략 — 현대 병과 작전
    // ──────────────────────────────────────────────────────────────

    /**
     * 작전 실행. 지휘 레벨이 낮으면 인접 구역 작전은 잠긴다.
     * 작전 성공은 경험치를 주고, 경험치는 다음 해금 범위를 연다.
     */
    fun runOperation(operationId: String) {
        _uiState.update { s ->
            val requiredHop = when (operationId) {
                "OP_DRONE", "OP_BRIDGE" -> 1
                else -> 0
            }
            if (s.command.unlockedHop < requiredHop) {
                val tier = ExpansionTier.entries.firstOrNull { it.maxHop >= requiredHop }
                return@update s.copy(
                    statusMessage = "[작전 불가] ${tier?.label ?: "인접 구역"}은(는) Lv.${tier?.level ?: 2}부터 가능합니다. " +
                        "관할 구역 방어전으로 경험을 쌓으세요."
                )
            }

            val target = when (operationId) {
                "OP_DRONE", "OP_BRIDGE" -> s.mapNodes.firstOrNull { it.hopDistance == 1 && it.unlocked }
                else -> s.homeNode
            } ?: s.homeNode
            val targetTerrain = target?.terrain ?: TerrainType.PLAIN
            val homeTerrain = s.homeNode?.terrain ?: TerrainType.PLAIN

            val own = facilityEngine.reinforced(ForceComposition.standard(s.troops), s.facilities)
            val enemyMultiplier = when (operationId) {
                "OP_DRONE" -> 0.95
                "OP_BRIDGE" -> 1.05
                else -> 1.55
            }
            val enemyTroops = (s.troops * enemyMultiplier).toInt().coerceAtLeast(60)
            val enemy = when (operationId) {
                "OP_DRONE" -> {
                    // 무인기 정찰전대 + 화력지원 (적 대드론 전력)
                    val drones = (enemyTroops * 0.55).toInt()
                    ForceComposition.of(CombatArm.DRONE, drones)
                        .plus(ForceComposition.of(CombatArm.ARTILLERY, enemyTroops - drones))
                }
                else -> ForceComposition.standard(enemyTroops)
            }

            // OP_BASE는 아군이 방어, 나머지 두 작전은 아군이 거점을 타격한다
            val weDefend = operationId == "OP_BASE"
            val battleTerrain = if (operationId == "OP_BRIDGE") TerrainType.RIVER else {
                if (weDefend) homeTerrain else targetTerrain
            }
            val fortress = if (weDefend) homeFortress(s) else null
            val battleTerritory = buildTerritory(
                area = (s.regionArea ?: RegionCatalog.defaultRegion()),
                fortress = fortress
            ).copy(terrain = battleTerrain)

            val operationName = when (operationId) {
                "OP_DRONE" -> "무인기 정찰·타격"
                "OP_BRIDGE" -> "교량·도하 봉쇄"
                else -> "통합 방호기지 방어"
            }

            val result: ModernBattleResult = modernBattleEngine.resolve(
                attackers = if (weDefend) enemy else own,
                defenders = if (weDefend) own else enemy,
                territory = battleTerritory,
                isSupplyLineCut = s.intelGained && weDefend,
                defenseBonusRatio = if (weDefend) facilityEngine.defenseBonus(s.facilities) else 0.0,
                casualtyReductionBonus = if (weDefend) facilityEngine.casualtyReduction(s.facilities) else 0.0
            )

            val ourLosses = if (weDefend) result.defenderLosses else result.attackerLosses
            val enemyLosses = if (weDefend) result.attackerLosses else result.defenderLosses
            val ourWin = if (weDefend) result.defenderWon else !result.defenderWon

            val lossRatio = if (s.troops == 0) 0.0 else ourLosses.toDouble() / s.troops
            val verdict = when {
                lossRatio <= 0.10 -> "저손실 현대전 성공 — 사상자 ${(lossRatio * 100).toInt()}%"
                lossRatio <= 0.20 -> "감내 가능 손실 — 사상자 ${(lossRatio * 100).toInt()}%"
                else -> "치명적 손실 — 사상자 ${(lossRatio * 100).toInt()}%. 재편성 필요"
            }
            val report = OperationReport(
                operation = operationName,
                defenderWon = ourWin,
                attackerLosses = enemyLosses,
                defenderLosses = ourLosses,
                message = result.message,
                verdict = verdict,
                targetName = if (weDefend) s.regionName else (target?.name ?: s.regionName),
                composition = "아군 ${own.describe()} / 적 ${enemy.describe()}",
                requiredLevel = requiredHop + 1
            )
            val outcome = if (ourWin) "작전 성공" else "작전 실패"
            val line = "[작전] $operationName — $outcome, " +
                "적 사상자 $enemyLosses, 아군 사상자 $ourLosses · $verdict"
            val heal = (ourLosses * facilityEngine.healRatio(s.facilities)).toInt()
            val before = s.command
            val baseXp = if (ourWin) {
                CommandProgress.XP_OPERATION_WIN
            } else {
                CommandProgress.XP_OPERATION_LOSS
            }
            val techBonus = if (s.researchedTechs.contains("sci_1")) 5 else 0
            val gained = before.gainXp(baseXp + techBonus)
            s.copy(
                troops = (s.troops - ourLosses + heal).coerceAtLeast(0),
                prestige = (s.prestige + if (ourWin) 3 else -4).coerceIn(0, 100),
                command = gained,
                unlockedHop = gained.unlockedHop,
                mapNodes = if (gained.level != before.level) {
                    TerritoryMapLayout.build(
                        start = s.regionArea ?: RegionCatalog.defaultRegion(),
                        unlockedHop = gained.unlockedHop,
                        playerName = s.playerLord?.name ?: "이광수",
                        homeTroops = s.troops
                    )
                } else s.mapNodes,
                levelUpMessage = if (gained.level != before.level) levelUpText(gained) else s.levelUpMessage,
                lastReport = report,
                log = (s.log + line).takeLast(8),
                statusMessage = line
            )
        }
    }

    /** 작전 후 레벨업 안내를 닫는다 */
    fun dismissLevelUp() {
        _uiState.update { it.copy(levelUpMessage = null) }
    }

    // ──────────────────────────────────────────────────────────────
    // 외교·첩보
    // ──────────────────────────────────────────────────────────────

    fun proposeTreaty(type: TreatyType) {
        _uiState.update { s ->
            val partnerName = when (type) {
                TreatyType.NON_AGGRESSION_PACT -> firstNeighborName(s)
                TreatyType.DEFENSIVE_ALLIANCE -> secondNeighborName(s)
                TreatyType.VASSAL_TRIBUTARY -> thirdNeighborName(s)
            }
            val playerLord = buildPlayerLord(s)
            val partner = Lord(
                id = "lord_${type.name.lowercase()}",
                name = partnerName,
                isHumanPlayer = false,
                territoryCode = neighborCode(s),
                troops = 260,
                grain = 1500,
                gold = 900,
                prestige = 55
            )
            val benefit = when (type) {
                TreatyType.NON_AGGRESSION_PACT -> "4턴간 상호 불가침 · 보복전 방지"
                TreatyType.DEFENSIVE_ALLIANCE -> "3턴간 피격 시 상호 참전 · 배신 명성 페널티 -80"
                TreatyType.VASSAL_TRIBUTARY -> "매 절기 조공 +100G 수입"
            }
            val duration = when (type) {
                TreatyType.NON_AGGRESSION_PACT -> 4
                TreatyType.DEFENSIVE_ALLIANCE -> 3
                TreatyType.VASSAL_TRIBUTARY -> 6
            }
            val treaty = diplomacyEngine.proposeTreaty(
                lord1 = playerLord,
                lord2 = partner,
                type = type,
                durationTurns = duration,
                tributaryGold = if (type == TreatyType.VASSAL_TRIBUTARY) 100 else 0
            )
            treaties.add(treaty)
            val message = "[외교] $partnerName 과(와) ${typeLabel(type)} 체결 — ${duration}턴 유효. $benefit"
            s.copy(
                treaties = s.treaties + TreatyView(
                    id = treaty.id,
                    typeLabel = typeLabel(type),
                    partner = partnerName,
                    remainingTurns = duration,
                    benefit = benefit
                ),
                diplomacy = (s.diplomacy + 6).coerceAtMost(100),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            ).let { next -> next.copy(alliedTerritories = alliedTerritoryCount(next)) }
        }
    }

    /** 무력 대응 명분(Casus Belli) 확보 */
    fun secureCasusBelli() {
        _uiState.update { s ->
            if (s.casusBelli != null) {
                return@update s.copy(statusMessage = "[명분] 이미 확보한 명분이 있습니다: ${s.casusBelli}")
            }
            val cause = "${secondNeighborName(s)}의 통제선 무단 통과(2회) · 외곽 탄약 집적지 무단 축조 — 명성 페널티 없이 무력 대응 가능"
            s.copy(
                casusBelli = cause,
                diplomacy = (s.diplomacy + 4).coerceAtMost(100),
                log = (s.log + "[명분 확보] $cause").takeLast(8),
                statusMessage = "[명분 확보] $cause"
            )
        }
    }

    /** 첩보 공작 — 보급 거점 교란 · 이간계 · 정찰 */
    fun conductEspionage(opType: EspionageOpType) {
        _uiState.update { s ->
            val agent = Talent(
                id = "talent_spy_01",
                name = "정보관 정하람",
                isPlayerHiddenTalent = false,
                warPower = 68,
                intelligence = 92,
                leadership = 80,
                politics = 85,
                charm = 88
            )
            val targetName = secondNeighborName(s)
            val targetLord = Lord(
                id = "lord_target",
                name = "$targetName 대표",
                isHumanPlayer = false,
                territoryCode = neighborCode(s),
                troops = 260,
                grain = s.enemyFood,
                gold = 2400,
                prestige = 70
            )
            val targetTerritory = buildTerritory(
                area = RegionCatalog.neighbors(s.regionCode).firstOrNull() ?: (s.regionArea ?: RegionCatalog.defaultRegion()),
                fortress = null
            )
            val result: EspionageResult = diplomacyEngine.conductEspionage(
                agent = agent,
                targetLord = targetLord,
                targetTerritory = targetTerritory,
                opType = opType
            )
            val intelBonus = facilityEngine.intelBonus(s.facilities)
            val extra = when {
                !result.success -> ""
                opType == EspionageOpType.GATHER_INTEL -> "\n적 잔여 보급 ${s.enemyFood}석 · 병력 ${targetLord.troops}명"
                else -> "\n$targetName 외곽 집적지 교란, 적 보급 -300석 (현재 ${(s.enemyFood - 300).coerceAtLeast(0)}석)"
            }
            s.copy(
                enemyFood = if (result.success && opType == EspionageOpType.INSTIGATE_DISSENT) {
                    (s.enemyFood - 300).coerceAtLeast(0)
                } else s.enemyFood,
                prestige = if (result.success) (s.prestige + 2).coerceAtMost(100) else s.prestige,
                espionagePower = (s.espionagePower + intelBonus).coerceIn(0, 100),
                espionageMessage = result.message + extra,
                espionageCount = s.espionageCount + 1,
                log = (s.log + "[첩보] ${result.message}").takeLast(8),
                statusMessage = "[첩보] ${result.message}"
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 시나리오 사건
    // ──────────────────────────────────────────────────────────────

    fun chooseRefugeeCrisis(choiceId: String): RefugeeCrisisResult? {
        var resultOut: RefugeeCrisisResult? = null
        _uiState.update { s ->
            if (s.refugeeChoice != null) {
                return@update s.copy(statusMessage = "[이재민 이벤트] 이미 처분한 안건입니다.")
            }
            val result = refugeeCrisisEngine.resolve(choiceId, s.food, s.gold)
            resultOut = result
            if (!result.isSuccess) {
                return@update s.copy(
                    refugeeResult = result.message,
                    statusMessage = result.message.lineSequence().first()
                )
            }
            s.copy(
                refugeeChoice = result.choiceId,
                refugeeResult = result.message,
                food = (s.food + result.foodChange).coerceAtLeast(0),
                gold = (s.gold + result.goldChange).coerceAtLeast(0),
                population = (s.population + result.populationChange).coerceAtLeast(0),
                morale = (s.morale + result.moraleChange).coerceIn(0, 100),
                culturePrestige = (s.culturePrestige + result.culturePrestigeChange).coerceIn(0, 100),
                diplomacy = (s.diplomacy + result.diplomacyChange).coerceIn(0, 100),
                log = (s.log + "[이재민] ${result.message.lineSequence().first()}").takeLast(8),
                statusMessage = result.message.lineSequence().first()
            )
        }
        return resultOut
    }

    fun openNightMeeting(talentName: String) {
        _uiState.update { s ->
            val talent = s.hiddenTalents.firstOrNull { it.name == talentName }
                ?: return@update s.copy(statusMessage = "[밀회 불가] 명부에 없는 인물입니다. ($talentName)")
            if (!nightMeetingEngine.isEligible(talent.suspicion)) {
                return@update s.copy(
                    statusMessage = "[밀회 불가] '${talent.name}'의 의심도 ${talent.suspicion} — " +
                        "기준(${nightMeetingEngine.suspicionThreshold}) 미달입니다."
                )
            }
            s.copy(
                nightMeetingTalentName = talent.name,
                nightMeetingChoice = null,
                nightMeetingResult = null,
                statusMessage = "[야간 밀회] '${talent.name}'에게 비밀 서신을 보냈습니다. 자정, 하천 산책로 아래."
            )
        }
    }

    fun resolveNightMeeting(choice: NightMeetingChoice): NightMeetingResult? {
        var resultOut: NightMeetingResult? = null
        _uiState.update { s ->
            val talentName = s.nightMeetingTalentName
                ?: return@update s.copy(statusMessage = "[밀회] 먼저 야간 밀회 대상을 선택하세요.")
            val talent = s.hiddenTalents.firstOrNull { it.name == talentName }
                ?: return@update s.copy(statusMessage = "[밀회] 대상을 찾을 수 없습니다. ($talentName)")
            if (s.nightMeetingChoice != null) {
                return@update s.copy(statusMessage = "[밀회] 이미 선택을 마친 사건입니다.")
            }

            val result = nightMeetingEngine.resolve(choice, talent.name, talent.suspicion, s.gold)
            resultOut = result
            if (!result.isSuccess) {
                return@update s.copy(
                    nightMeetingResult = result.message,
                    statusMessage = result.message.lineSequence().first()
                )
            }

            val updatedTalents = s.hiddenTalents.map { hidden ->
                when {
                    hidden.name != talent.name -> hidden
                    result.talentRemoved -> hidden.copy(suspicion = 0, hint = "경질 완료 — 명단에서 제외됨")
                    else -> hidden.copy(suspicion = (hidden.suspicion + result.suspicionChange).coerceIn(0, 100))
                }
            }.filterNot { result.talentRemoved && it.name == talent.name }

            s.copy(
                nightMeetingChoice = choice.name,
                nightMeetingResult = result.message,
                hiddenTalents = updatedTalents,
                gold = (s.gold + result.goldChange).coerceAtLeast(0),
                morale = (s.morale + result.moraleChange).coerceIn(0, 100),
                prestige = (s.prestige + result.prestigeChange).coerceIn(0, 100),
                espionagePower = (s.espionagePower + result.espionageChange).coerceIn(0, 100),
                doubleAgentBuff = result.doubleAgentBuff ?: s.doubleAgentBuff,
                enemyFood = if (result.doubleAgentBuff != null) (s.enemyFood - 200).coerceAtLeast(0) else s.enemyFood,
                log = (s.log + "[야간 밀회] ${result.message.lineSequence().first()}").takeLast(8),
                statusMessage = result.message.lineSequence().first()
            )
        }
        return resultOut
    }

    fun exploitDoubleAgent() {
        _uiState.update { s ->
            val buff = s.doubleAgentBuff
                ?: return@update s.copy(statusMessage = "[교란 불가] 보유한 이중첩자 교란 효과가 없습니다.")
            s.copy(
                enemyFood = (s.enemyFood - 300).coerceAtLeast(0),
                espionageMessage = "[이중첩자 교란] 적 보급 거점 좌표를 넘겨받아 보급로를 끊었다. 적 보급 -300석 " +
                    "(현재 ${(s.enemyFood - 300).coerceAtLeast(0)}석).",
                log = (s.log + "[이중첩자 교란] $buff").takeLast(8),
                statusMessage = "[이중첩자 교란] 적 보급 -300석"
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 승격 로드맵
    // ──────────────────────────────────────────────────────────────

    fun promotionEvaluation(): PromotionEvaluation {
        val s = _uiState.value
        return promotionEngine.evaluate(s.promotionStage, alliedTerritoryCount(s), s.prestige)
    }

    fun promote(): PromotionResult {
        var resultOut: PromotionResult? = null
        _uiState.update { s ->
            val allies = alliedTerritoryCount(s)
            val result = promotionEngine.promote(s.promotionStage, allies, s.prestige)
            resultOut = result
            if (!result.isSuccess) {
                return@update s.copy(
                    alliedTerritories = allies,
                    promotionMessage = result.message,
                    statusMessage = result.message
                )
            }
            val before = s.command
            val gained = before.gainXp(CommandProgress.XP_PROMOTION)
            s.copy(
                alliedTerritories = allies,
                promotionStage = result.stage,
                councilAuthorityBonus = s.councilAuthorityBonus + result.councilAuthorityBonus,
                promotionCeremony = result.ceremonyBody,
                promotionMessage = result.message,
                prestige = (s.prestige + 3).coerceIn(0, 100),
                command = gained,
                unlockedHop = gained.unlockedHop,
                levelUpMessage = if (gained.level != before.level) levelUpText(gained) else s.levelUpMessage,
                log = (s.log + "[승격] ${result.message}").takeLast(8),
                statusMessage = result.message
            )
        }
        return resultOut ?: PromotionResult(
            isSuccess = false,
            stage = _uiState.value.promotionStage,
            failureReason = "승격 처리 결과를 얻지 못했습니다.",
            message = "[승격 실패] 승격 처리 결과를 얻지 못했습니다."
        )
    }

    fun dismissCeremony() {
        _uiState.update { s -> s.copy(promotionCeremony = null) }
    }

    // ──────────────────────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────────────────────

    private fun AiLordProfile.toView(): LordView = LordView(
        id = id,
        name = name,
        stronghold = stronghold,
        traitLabel = trait.displayName,
        traitIcon = trait.icon,
        troops = troops,
        gold = gold,
        grain = grain,
        prestige = prestige,
        arms = arm.displayName
    )

    private fun levelUpText(command: CommandProgress): String =
        "🎖 지휘 Lv.${command.level} — ${command.tier.label} 해금. ${command.tier.detail}"

    private fun tierForHop(hop: Int): Int =
        ExpansionTier.entries.firstOrNull { it.maxHop >= hop }?.level ?: 1

    private fun tierForHopLabel(hop: Int): String =
        ExpansionTier.entries.firstOrNull { it.maxHop >= hop }?.label ?: "전국 연결망"

    private fun firstNeighborName(s: SlgGameState): String =
        s.mapNodes.firstOrNull { it.hopDistance == 1 }?.name ?: "인접 구역"

    private fun secondNeighborName(s: SlgGameState): String =
        s.mapNodes.filter { it.hopDistance == 1 }.getOrNull(1)?.name
            ?: s.mapNodes.firstOrNull { it.hopDistance == 1 }?.name
            ?: "인접 구역"

    private fun thirdNeighborName(s: SlgGameState): String =
        s.mapNodes.filter { it.hopDistance == 1 }.getOrNull(2)?.name
            ?: secondNeighborName(s)

    private fun neighborCode(s: SlgGameState): String =
        s.mapNodes.firstOrNull { it.hopDistance == 1 }?.code ?: s.regionCode

    private fun typeLabel(type: TreatyType): String = when (type) {
        TreatyType.NON_AGGRESSION_PACT -> "불가침 조약"
        TreatyType.DEFENSIVE_ALLIANCE -> "상호방위 조약"
        TreatyType.VASSAL_TRIBUTARY -> "속국 조공 조약"
    }

    /** 인접 영지 중 우호·동맹으로 묶인 수 (승격 조건) */
    private fun alliedTerritoryCount(s: SlgGameState): Int {
        var count = 0
        if (s.eventAChoice == "A_PACT") count += 1
        if (s.treaties.any { it.typeLabel == "속국 조공 조약" }) count += 1
        if (s.treaties.any { it.typeLabel == "상호방위 조약" }) count += 1
        return count
    }

    private fun buildPlayerLord(s: SlgGameState): Lord = Lord(
        id = "lord_player",
        name = "${s.playerLord?.name ?: "이광수"} 호족 (${s.regionName})",
        isHumanPlayer = true,
        territoryCode = s.regionCode,
        troops = s.troops,
        grain = s.food,
        gold = s.gold,
        prestige = s.prestige,
        isCastellan = true
    )

    /** 의회 구성 — 플레이어를 제외한 AI 호족 (최대 12석) */
    private fun buildCouncilLords(s: SlgGameState): List<Lord> {
        val aiLords = s.lords.filter { !it.isPlayer }.take(12)
        if (aiLords.isEmpty()) {
            return listOf(
                Lord("lord_1", "인접 호족 A", false, s.regionCode, troops = 300, grain = 1200, gold = 800, prestige = 55)
            )
        }
        return aiLords.map { lord ->
            Lord(
                id = lord.id,
                name = "${lord.name} 호족",
                isHumanPlayer = false,
                territoryCode = s.regionCode,
                troops = lord.troops,
                grain = lord.grain,
                gold = lord.gold,
                prestige = lord.prestige
            )
        }
    }

    private fun homeFortress(s: SlgGameState): Fortress {
        val level = when {
            s.facilities.contains(FacilityType.SMART_DEFENSE_NET) -> FortressLevel.INTEGRATED_NET
            s.facilities.contains(FacilityType.RADAR_NETWORK) -> FortressLevel.UNDERGROUND_BUNKER
            else -> FortressLevel.CONCRETE_BARRIER
        }
        return Fortress(
            level = level,
            durability = 1000,
            maxDurability = 1000,
            garrisonTroops = s.troops
        )
    }

    private fun buildTerritory(
        area: RegionArea,
        fortress: Fortress?,
        fertileScore: Double = 1.0
    ): Territory = Territory(
        code = area.code,
        name = area.name,
        province = area.province,
        cityCountyDistrict = listOf(area.city, area.district).filter { it.isNotBlank() }.joinToString(" "),
        population = area.population,
        maxLords = area.lordCapacity,
        terrain = area.terrain,
        fortress = fortress,
        fertileScore = fertileScore
    )

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
