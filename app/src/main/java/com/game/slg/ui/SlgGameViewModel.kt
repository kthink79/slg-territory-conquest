package com.game.slg.ui

import androidx.lifecycle.ViewModel
import com.game.slg.core.engine.CombatEngine
import com.game.slg.core.engine.CombatEngineContract
import com.game.slg.core.engine.CouncilEngine
import com.game.slg.core.engine.CouncilEngineContract
import com.game.slg.core.engine.DiplomacyEngine
import com.game.slg.core.engine.IDiplomacyEngine
import com.game.slg.core.engine.IHiddenTalentNightEngine
import com.game.slg.core.engine.IPromotionEngine
import com.game.slg.core.engine.IRefugeeCrisisEngine
import com.game.slg.core.engine.ISeasonEngine
import com.game.slg.core.engine.HiddenTalentNightEngine
import com.game.slg.core.engine.PromotionEngine
import com.game.slg.core.engine.RefugeeCrisisEngine
import com.game.slg.core.engine.SeasonEngine
import com.game.slg.core.engine.SiegeResult
import com.game.slg.core.model.BillType
import com.game.slg.core.model.CouncilBill
import com.game.slg.core.model.DiplomaticTreaty
import com.game.slg.core.model.EspionageOpType
import com.game.slg.core.model.EspionageResult
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ============================================================================
// 세계관 데이터 — 「단일 연방의 황혼」 (경기도 수원시 영통구 매탄동 본진)
// ============================================================================

enum class Relation(val label: String) {
    ALLIED("아군 영지"),
    NEUTRAL_MERCHANT("중립 상인회"),
    WARY("경계 세력"),
    MOUNTAIN_GATE("산악 협곡")
}

data class TerritoryInfo(
    val code: String,
    val name: String,
    val region: String,
    val population: Int,
    val relation: Relation,
    val owner: String,
    val troops: Int,
    val terrain: TerrainType,
    val note: String
)

/**
 * 4대 테크트리 한 단계
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

/**
 * 호족 의회 안건 상태
 */
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

/**
 * 작전 실행 결과
 */
data class OperationReport(
    val operation: String,
    val defenderWon: Boolean,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val message: String,
    val verdict: String
)

/**
 * 외교 조약 표시용
 */
data class TreatyView(
    val id: String,
    val typeLabel: String,
    val partner: String,
    val remainingTurns: Int,
    val benefit: String
)

/**
 * 히든 인재(위장 유저) 동향
 */
data class HiddenTalentView(
    val name: String,
    val cover: String,
    val hint: String,
    val suspicion: Int
)

object SlgWorld {
    const val PLAYER_TERRITORY = "4111154000"
    const val PLAYER_TERRITORY_NAME = "경기도 수원시 영통구 매탄동"

    val TERRITORIES: List<TerritoryInfo> = listOf(
        TerritoryInfo(
            code = PLAYER_TERRITORY,
            name = "매탄동",
            region = "경기 수원시 영통구",
            population = 128400,
            relation = Relation.ALLIED,
            owner = "이광수 호족 (플레이어)",
            troops = 1200,
            terrain = TerrainType.PLAIN,
            note = "본진 · 정보통신 허브 · 산성 요새"
        ),
        TerritoryInfo(
            code = "4111155000",
            name = "원천동",
            region = "경기 수원시 영통구",
            population = 48200,
            relation = Relation.NEUTRAL_MERCHANT,
            owner = "원천동 상인연합",
            troops = 620,
            terrain = TerrainType.PLAIN,
            note = "야간 물자 협약 제안 · 관세 분쟁"
        ),
        TerritoryInfo(
            code = "4111156000",
            name = "광교동",
            region = "경기 수원시 영통구",
            population = 63400,
            relation = Relation.WARY,
            owner = "광교 호족연합",
            troops = 1850,
            terrain = TerrainType.MOUNTAIN,
            note = "경계 세력 · 광교산 협곡 정찰대 출현"
        ),
        TerritoryInfo(
            code = "4146152000",
            name = "용인 수지",
            region = "경기 용인시 수지구",
            population = 21500,
            relation = Relation.MOUNTAIN_GATE,
            owner = "수지 산악 협곡 세력",
            troops = 940,
            terrain = TerrainType.MOUNTAIN,
            note = "산악 협곡 요충 · 보급로 관문"
        )
    )

    val TECH_ITEMS: List<TechItem> = listOf(
        TechItem("admin_1", "내정", 1, "수로 개간", "매탄천 물길을 정비해 농경지와 상수도를 확보한다.", 400, "가을 군량 +8%"),
        TechItem("admin_2", "내정", 2, "장시 활성화", "영통·매탄 상권을 결합한 정기 장시를 연다.", 700, "자금 +150/절기"),
        TechItem("admin_3", "내정", 3, "대동세", "인구 비례 세율을 일원화해 호족의 세 부담을 던다.", 1200, "자금 +300/절기 · 민심 +3"),
        TechItem("admin_4", "내정", 4, "호패법", "주민 등록과 호패로 유동 인구를 파악한다.", 1800, "민심 +6 · 첩보 방어 +15%"),
        TechItem("sci_1", "과학기술", 1, "석조 축조", "요새와 가옥의 석조 시공술을 도입한다.", 500, "요새 내구도 +10%"),
        TechItem("sci_2", "과학기술", 2, "야금·제련", "합금 단련로를 세워 무기와 방호구를 개량한다.", 900, "아군 피해 -5%"),
        TechItem("sci_3", "과학기술", 3, "도로망 측량", "GIS 기반 도로망을 측량해 보급 속도를 높인다.", 1300, "수송대 손실 -20%"),
        TechItem("sci_4", "과학기술", 4, "화약 공성무기", "화약 추진 공성포를 제작한다.", 2000, "공성 피해 +25%"),
        TechItem("cul_1", "문화", 1, "향약·두레", "마을 자치 규약과 공동 노동 조직을 세운다.", 350, "민심 +4"),
        TechItem("cul_2", "문화", 2, "서원 설립", "학당을 세워 인재를 길러낸다.", 800, "인재 등용 +1"),
        TechItem("cul_3", "문화", 3, "가문 명망", "가문의 이름을 세워 외교 협상력을 높인다.", 1300, "외교 +8"),
        TechItem("cul_4", "문화", 4, "점령지 동화", "점령 지역의 문화를 통합한다.", 1900, "점령지 민심 +12"),
        TechItem("mil_1", "군사", 1, "창병 방진", "창병 방진으로 기병 돌격을 억제한다.", 450, "방어 시 사상자 -8%"),
        TechItem("mil_2", "군사", 2, "복합노궁", "사거리와 관통력이 높은 복합궁을 지급한다.", 850, "적 사상자 +12%"),
        TechItem("mil_3", "군사", 3, "중갑기병", "중갑기병을 편성해 협곡 돌파를 막는다.", 1400, "산악 매복 성공률 +15%"),
        TechItem("mil_4", "군사", 4, "공성 충차", "요새를 부수는 충차를 제작한다.", 2100, "요새 파괴 +30%")
    )

    val HIDDEN_TALENTS: List<HiddenTalentView> = listOf(
        HiddenTalentView("한도윤", "원천동 상인회 서기", "야간에 사병 40명을 모집한 정황 · 소문 미확산", 62),
        HiddenTalentView("서이레", "광교동 회계 담당", "호족연합의 병력 장부를 이중으로 관리 중", 48),
        HiddenTalentView("문태오", "매탄동 시장 상인", "최근 원천동 상인과 접촉 빈도 급증", 33)
    )
}

// ============================================================================
// UI 상태
// ============================================================================

data class SlgGameState(
    val seasonName: String = "봄 (춘경 및 조세 징수)",
    val population: Int = 128400,
    val gold: Int = 4200,
    val food: Int = 8600,
    val troops: Int = 1200,
    val morale: Int = 62,
    val prestige: Int = 74,
    val diplomacy: Int = 55,
    val enemyFood: Int = 2400,
    val statusMessage: String = "「단일 연방의 황혼」 D+14 — 매탄동 호족 본진에서 하루가 시작됩니다.",
    val log: List<String> = listOf("[D+14] 연방 해체 14일차. 영통구 일대 자치구 호족들이 각자의 깃발을 세웠다."),
    // ── 샘플 이벤트 ──
    val eventAChoice: String? = null,
    val eventAResult: String? = null,
    val eventBChoice: String? = null,
    val eventBResult: String? = null,
    val intelGained: Boolean = false,
    // ── 내정·의회 ──
    val researchedTechs: Set<String> = emptySet(),
    val bills: List<BillState> = listOf(
        BillState("bill_levy", "전시 병력 차출안", BillType.TROOP_LEVY, levy = 300),
        BillState("bill_sec", "치안 유지 조례 (특별세)", BillType.SPECIAL_TAX, levy = 0)
    ),
    // ── 전장 ──
    val lastReport: OperationReport? = null,
    // ── 외교·첩보 ──
    val treaties: List<TreatyView> = emptyList(),
    val casusBelli: String? = null,
    val espionageMessage: String? = null,
    val espionageCount: Int = 0,
    val hiddenTalents: List<HiddenTalentView> = SlgWorld.HIDDEN_TALENTS,
    // ── 「연방 붕괴의 그림자」: 유민 유입 딜레마 ──
    val culturePrestige: Int = 48,
    val refugeeChoice: String? = null,
    val refugeeResult: String? = null,
    // ── 「호족 의회의 이면」: 야간 밀회 ──
    val nightMeetingTalentName: String? = null,
    val nightMeetingChoice: String? = null,
    val nightMeetingResult: String? = null,
    val espionagePower: Int = 40,
    val doubleAgentBuff: String? = null,
    // ── 성주 승격 로드맵 ──
    val alliedTerritories: Int = 0,
    val promotionStage: PromotionStage = PromotionStage.DISTRICT_LORD,
    val councilAuthorityBonus: Int = 0,
    val promotionCeremony: String? = null,
    val promotionMessage: String? = null,
    // ── 메인 3단 화면: 지도에서 선택한 영지 ──
    val selectedTerritoryCode: String = SlgWorld.PLAYER_TERRITORY
) {
    /** 지도에서 선택된 영지 정보 */
    val selectedTerritory: TerritoryInfo?
        get() = SlgWorld.TERRITORIES.firstOrNull { it.code == selectedTerritoryCode }

    /** 선택된 영지의 지도 노드 (범위 밖이면 null) */
    val selectedMapNode: com.game.slg.core.map.MapNode?
        get() = com.game.slg.core.map.TerritoryMapLayout.node(selectedTerritoryCode)
    /** 유민 수용 여부 (연방 붕괴 이벤트 결과 표시용) */
    val isRefugeeAccepted: Boolean get() = refugeeChoice == "REFUGEE_A_OPEN"

    /** 다음 승격 단계 */
    val nextPromotionStage: PromotionStage? get() = promotionStage.nextStage

    /** 승격 의식 개최 가능 여부 */
    val isPromotionEligible: Boolean
        get() = nextPromotionStage?.let {
            alliedTerritories >= it.requiredAlliedTerritories && prestige >= it.requiredPrestige
        } ?: false

    /** 야간 밀회 대상 (의심도 50 이상) */
    val nightMeetingCandidates: List<HiddenTalentView>
        get() = hiddenTalents.filter { it.suspicion >= NIGHT_MEETING_SUSPICION }

    companion object {
        /** 야간 밀회 대상 의심도 기준 */
        const val NIGHT_MEETING_SUSPICION: Int = 50
    }
}

// ============================================================================
// ViewModel — 4탭(영지·지도 / 내정·의회 / 전장·전략 / 외교·첩보)
// ============================================================================

class SlgGameViewModel(
    private val seasonEngine: ISeasonEngine = SeasonEngine(),
    private val combatEngine: CombatEngineContract = CombatEngine(),
    private val councilEngine: CouncilEngineContract = CouncilEngine(),
    private val diplomacyEngine: IDiplomacyEngine = DiplomacyEngine(),
    private val refugeeCrisisEngine: IRefugeeCrisisEngine = RefugeeCrisisEngine(),
    private val nightMeetingEngine: IHiddenTalentNightEngine = HiddenTalentNightEngine(),
    private val promotionEngine: IPromotionEngine = PromotionEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SlgGameState())
    val uiState: StateFlow<SlgGameState> = _uiState.asStateFlow()

    /** 메인 3단 화면 ↔ 서브 메뉴 상세 화면 경로 */
    private val _navigation = MutableStateFlow(MainNavigationState())
    val navigation: StateFlow<MainNavigationState> = _navigation.asStateFlow()

    private var season: Season = Season.SPRING

    // ──────────────────────────────────────────────────────────────
    // 메인 3단 화면 (지도 선택 · 서브 메뉴 이동)
    // ──────────────────────────────────────────────────────────────

    /** 상단 1/3 지도에서 영지 선택 */
    fun selectTerritory(code: String) {
        val node = com.game.slg.core.map.TerritoryMapLayout.node(code) ?: return
        _uiState.update { s ->
            if (s.selectedTerritoryCode == node.code) s
            else s.copy(
                selectedTerritoryCode = node.code,
                statusMessage = "[지도] '${node.name}' 선택 — ${node.relationLabel}"
            )
        }
    }

    /** 하단 1/3 서브 메뉴 열기 */
    fun openSubMenu(menu: SubMenu) {
        _navigation.update { it.open(menu) }
    }

    /** '← 메인 3단 화면' 복귀 */
    fun backToMain() {
        _navigation.update { it.back() }
    }
    private val treaties = mutableListOf<DiplomaticTreaty>()

    // ──────────────────────────────────────────────────────────────
    // 영지·지도
    // ──────────────────────────────────────────────────────────────

    /** 절기 진행 — 4계절 경제 순환 (봄 조세 · 여름 치수 · 가을 추수 · 겨울 유지비) */
    fun advanceSeason(dikeMaintenanceDone: Boolean = true) {
        _uiState.update { s ->
            val playerLord = buildPlayerLord(s)
            val territory = buildTerritory(
                info = SlgWorld.TERRITORIES.first(),
                fortress = null
            )
            val result = seasonEngine.processSeasonalCycle(
                currentSeason = season,
                territory = territory,
                lords = listOf(playerLord),
                dikeMaintenanceDone = dikeMaintenanceDone,
                randomSeed = 20261114L
            )
            val nextSeason = season.next()
            val message = "[절기 순환: ${season.displayName}] ${result.message}"
            season = nextSeason
            s.copy(
                seasonName = nextSeason.displayName,
                gold = playerLord.gold,
                food = playerLord.grain,
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
    }

    /** 이벤트 A — 원천동 상인연합의 야간 물자 협약 제안 */
    fun chooseEventA(acceptPact: Boolean) {
        _uiState.update { s ->
            if (s.eventAChoice != null) {
                return@update s.copy(statusMessage = "[이벤트 A] 이미 처분한 안건입니다.")
            }
            if (acceptPact) {
                s.copy(
                    eventAChoice = "A_PACT",
                    eventAResult = "[상호 보급 협정 체결] 관세를 면제하고 야간 보급로를 열었다. 원천동 상인들이 " +
                            "매탄동 창고에 곡물과 연료를 채워 넣는다. 자금 -600, 식량 +1,200, 민심 +8, 외교 +12. " +
                            "원천동은 '협력 상인회'로 전환.",
                    gold = (s.gold - 600).coerceAtLeast(0),
                    food = s.food + 1200,
                    morale = (s.morale + 8).coerceAtMost(100),
                    diplomacy = (s.diplomacy + 12).coerceAtMost(100),
                    statusMessage = "[이벤트 A] 관세 면제·상호 보급 협정 체결 — 식량 +1,200"
                ).let { next -> next.copy(alliedTerritories = alliedTerritoryCount(next)) }
            } else {
                s.copy(
                    eventAChoice = "A_TARIFF",
                    eventAResult = "[통행세 징수] 야간 통행 차량에 통행세를 매기고 비축고를 채웠다. 단기 자금은 " +
                            "늘었지만 상인들의 시선이 차가워졌다. 자금 +900, 민심 -10, 외교 -15. " +
                            "원천동은 '경계 상인회'로 전환.",
                    gold = s.gold + 900,
                    morale = (s.morale - 10).coerceAtLeast(0),
                    diplomacy = (s.diplomacy - 15).coerceAtLeast(0),
                    statusMessage = "[이벤트 A] 통행세 징수 및 자체 비축 — 자금 +900"
                )
            }
        }
    }

    /** 이벤트 B — 광교산 협곡에 정체불명 정찰대 출현 */
    fun chooseEventB(ambush: Boolean) {
        _uiState.update { s ->
            if (s.eventBChoice != null) {
                return@update s.copy(statusMessage = "[이벤트 B] 이미 처분한 안건입니다.")
            }
            if (ambush) {
                s.copy(
                    eventBChoice = "B_AMBUSH",
                    eventBResult = "[산악 매복 요격] 협곡 능선에 매복조를 배치해 정찰대 3개 분대를 무력화했다. " +
                            "아군 피해 80명, 노획 물자 250G 회수, 명성 +6. 광교 호족연합은 보복을 준비한다.",
                    troops = (s.troops - 80).coerceAtLeast(0),
                    gold = s.gold + 250,
                    prestige = (s.prestige + 6).coerceAtMost(100),
                    morale = (s.morale + 2).coerceAtMost(100),
                    statusMessage = "[이벤트 B] 산악 매복 요격 — 병력 -80, 노획 250G"
                )
            } else {
                s.copy(
                    eventBChoice = "B_SCOUT",
                    eventBResult = "[첩자 파견] 매탄동 상인으로 위장한 첩자가 광교산 협곡에 잠입했다. " +
                            "정찰대는 광교 호족연합의 사전 답사조였고, 협곡에 군량고가 세워지고 있다. " +
                            "자금 -200, 민심 +3, 적 군량고 위치 확보.",
                    gold = (s.gold - 200).coerceAtLeast(0),
                    morale = (s.morale + 3).coerceAtMost(100),
                    intelGained = true,
                    statusMessage = "[이벤트 B] 첩자 파견 — 적 군량고 위치 확보 (자금 -200)"
                )
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 내정·의회
    // ──────────────────────────────────────────────────────────────

    fun research(techId: String) {
        _uiState.update { s ->
            val tech = SlgWorld.TECH_ITEMS.firstOrNull { it.id == techId } ?: return@update s
            if (s.researchedTechs.contains(techId)) {
                return@update s.copy(statusMessage = "[연구] 이미 완료된 기술입니다.")
            }
            // 같은 계통의 이전 단계를 먼저 완료해야 한다
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
            val lords = buildCouncilLords()
            val result = councilEngine.voteOnBill(bill, lords)

            val passed = result.isPassed
            val rebellion = result.isRebellionTriggered
            val message = buildString {
                append("[호족 의회 표결] '${state.title}' — 찬성 ${result.votesInFavor} : 반대 ${result.votesAgainst} → ")
                append(if (passed) "가결" else "부결")
                if (rebellion) append("\n반대 65% 이상! 반대 호족들이 연합해 반란을 준비합니다.")
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
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 전장·전략
    // ──────────────────────────────────────────────────────────────

    /** 산악 매복 / 하천 도하 요격 / 요새 수성 작전 시뮬레이션 */
    fun runOperation(operationId: String) {
        _uiState.update { s ->
            val (operationName, territory, attackers, defenders) = when (operationId) {
                "OP_AMBUSH" -> Operation(
                    "광교산 협곡 매복",
                    buildTerritory(
                        SlgWorld.TERRITORIES.first { it.name == "광교동" },
                        fortress = null
                    ),
                    attackers = 700,
                    defenders = s.troops
                )
                "OP_RIVER" -> Operation(
                    "매탄천 도하 요격",
                    buildTerritory(
                        SlgWorld.TERRITORIES.first(),
                        fortress = null
                    ).copy(terrain = TerrainType.RIVER),
                    attackers = 650,
                    defenders = s.troops
                )
                else -> Operation(
                    "매탄동 산성 요새 수성",
                    buildTerritory(
                        SlgWorld.TERRITORIES.first(),
                        fortress = Fortress(
                            level = FortressLevel.MOUNTAIN_FORTRESS,
                            durability = 1000,
                            maxDurability = 1000,
                            garrisonTroops = 800
                        )
                    ).copy(terrain = TerrainType.MOUNTAIN),
                    attackers = 3000,
                    defenders = s.troops
                )
            }

            val result: SiegeResult = combatEngine.resolveSiege(
                attackers = attackers,
                defenders = defenders,
                territory = territory,
                isSupplyLineCut = s.intelGained && operationId == "OP_FORTRESS"
            )
            val lossRatio = if (defenders == 0) 0.0 else result.defenderLosses.toDouble() / defenders
            val verdict = when {
                lossRatio <= 0.10 -> "저손실 현대전 성공 — 사상자 ${(lossRatio * 100).toInt()}%"
                lossRatio <= 0.20 -> "감내 가능 손실 — 사상자 ${(lossRatio * 100).toInt()}%"
                else -> "치명적 손실 — 사상자 ${(lossRatio * 100).toInt()}%. 재편성 필요"
            }
            val report = OperationReport(
                operation = operationName,
                defenderWon = result.defenderWon,
                attackerLosses = result.attackerLosses,
                defenderLosses = result.defenderLosses,
                message = result.message,
                verdict = verdict
            )
            val line = "[작전] $operationName — ${if (result.defenderWon) "방어 성공" else "거점 상실"}, " +
                    "적 사상자 ${result.attackerLosses}, 아군 사상자 ${result.defenderLosses} · $verdict"
            s.copy(
                troops = (s.troops - result.defenderLosses).coerceAtLeast(0),
                prestige = (s.prestige + if (result.defenderWon) 3 else -4).coerceIn(0, 100),
                lastReport = report,
                log = (s.log + line).takeLast(8),
                statusMessage = line
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 외교·첩보
    // ──────────────────────────────────────────────────────────────

    fun proposeTreaty(type: TreatyType) {
        _uiState.update { s ->
            val partnerName = when (type) {
                TreatyType.NON_AGGRESSION_PACT -> "원천동 상인회"
                TreatyType.DEFENSIVE_ALLIANCE -> "용인 수지 산악 협곡 세력"
                TreatyType.VASSAL_TRIBUTARY -> "광교 호족연합"
            }
            val playerLord = buildPlayerLord(s)
            val partner = Lord(
                id = "lord_${type.name.lowercase()}",
                name = partnerName,
                isHumanPlayer = false,
                territoryCode = "4111155000",
                troops = 620,
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
            val message = "[외교] $partnerName 과(와) ${type.name} 체결 — ${duration}턴 유효. $benefit"
            s.copy(
                treaties = s.treaties + TreatyView(
                    id = treaty.id,
                    typeLabel = when (type) {
                        TreatyType.NON_AGGRESSION_PACT -> "불가침 조약"
                        TreatyType.DEFENSIVE_ALLIANCE -> "상호방위 조약"
                        TreatyType.VASSAL_TRIBUTARY -> "속국 조공 조약"
                    },
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

    /** 선전포고 명분(Casus Belli) 확보 */
    fun secureCasusBelli() {
        _uiState.update { s ->
            if (s.casusBelli != null) {
                return@update s.copy(statusMessage = "[명분] 이미 확보한 명분이 있습니다: ${s.casusBelli}")
            }
            val cause = "광교 호족연합의 국경 무단 정찰(2회) · 협곡 군량고 무단 축조 — 명성 페널티 없이 선전포고 가능"
            s.copy(
                casusBelli = cause,
                diplomacy = (s.diplomacy + 4).coerceAtMost(100),
                log = (s.log + "[명분 확보] $cause").takeLast(8),
                statusMessage = "[명분 확보] $cause"
            )
        }
    }

    /** 첩자 공작 — 군량고 방화 · 이간계 · 정찰 */
    fun conductEspionage(opType: EspionageOpType) {
        _uiState.update { s ->
            val agent = Talent(
                id = "talent_spy_01",
                name = "첩자 정하람",
                isPlayerHiddenTalent = false,
                warPower = 68,
                intelligence = 92,
                leadership = 80,
                politics = 85,
                charm = 88
            )
            val targetLord = Lord(
                id = "lord_gwanggyo",
                name = "광교 호족연합 대표 진무열",
                isHumanPlayer = false,
                territoryCode = "4111156000",
                troops = 1850,
                grain = s.enemyFood,
                gold = 2400,
                prestige = 70
            )
            val targetTerritory = buildTerritory(
                SlgWorld.TERRITORIES.first { it.name == "광교동" },
                fortress = null
            )
            val result: EspionageResult = diplomacyEngine.conductEspionage(
                agent = agent,
                targetLord = targetLord,
                targetTerritory = targetTerritory,
                opType = opType
            )
            val extra = when {
                !result.success -> ""
                opType == EspionageOpType.GATHER_INTEL -> "\n적 잔여 군량 ${s.enemyFood}석 · 병력 ${targetLord.troops}명"
                else -> {
                    "\n광교동 군량고에 방화, 적 군량 -300석 (현재 ${(s.enemyFood - 300).coerceAtLeast(0)}석)"
                }
            }
            s.copy(
                enemyFood = if (result.success && opType == EspionageOpType.INSTIGATE_DISSENT) {
                    (s.enemyFood - 300).coerceAtLeast(0)
                } else s.enemyFood,
                prestige = if (result.success) (s.prestige + 2).coerceAtMost(100) else s.prestige,
                espionageMessage = result.message + extra,
                espionageCount = s.espionageCount + 1,
                log = (s.log + "[첩보] ${result.message}").takeLast(8),
                statusMessage = "[첩보] ${result.message}"
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 「연방 붕괴의 그림자」 — 유민 유입 딜레마
    // ──────────────────────────────────────────────────────────────

    /** 유민 대응 선택 (성문 개방 / 성문 통제) */
    fun chooseRefugeeCrisis(choiceId: String): RefugeeCrisisResult? {
        var resultOut: RefugeeCrisisResult? = null
        _uiState.update { s ->
            if (s.refugeeChoice != null) {
                return@update s.copy(statusMessage = "[유민 이벤트] 이미 처분한 안건입니다.")
            }
            val result = refugeeCrisisEngine.resolve(choiceId, s.food, s.gold)
            resultOut = result
            if (!result.isSuccess) {
                return@update s.copy(
                    refugeeResult = result.message,
                    statusMessage = result.message.lineSequence().first()
                )
            }
            val next = s.copy(
                refugeeChoice = result.choiceId,
                refugeeResult = result.message,
                food = (s.food + result.foodChange).coerceAtLeast(0),
                gold = (s.gold + result.goldChange).coerceAtLeast(0),
                population = (s.population + result.populationChange).coerceAtLeast(0),
                morale = (s.morale + result.moraleChange).coerceIn(0, 100),
                culturePrestige = (s.culturePrestige + result.culturePrestigeChange).coerceIn(0, 100),
                diplomacy = (s.diplomacy + result.diplomacyChange).coerceIn(0, 100),
                log = (s.log + "[유민] ${result.message.lineSequence().first()}").takeLast(8),
                statusMessage = result.message.lineSequence().first()
            )
            next
        }
        return resultOut
    }

    // ──────────────────────────────────────────────────────────────
    // 「호족 의회의 이면」 — 히든 인재 야간 밀회
    // ──────────────────────────────────────────────────────────────

    /** 🌙 야간 밀회: 비밀 서신 개설 (의심도 50 이상 대상) */
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
                statusMessage = "[야간 밀회] '${talent.name}'에게 비밀 서신을 보냈습니다. 자정, 매탄천 다리 아래."
            )
        }
    }

    /** 야간 밀회 3지선다 처리 (포섭 / 숙청 / 이중첩자) */
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
                    result.talentRemoved -> hidden.copy(suspicion = 0, hint = "숙청 완료 — 명단에서 제외됨")
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

    /** 이중첩자 교란 효과로 광교동 군량고를 한 번 더 무너뜨림 */
    fun exploitDoubleAgent() {
        _uiState.update { s ->
            val buff = s.doubleAgentBuff
                ?: return@update s.copy(statusMessage = "[교란 불가] 보유한 이중첩자 교란 효과가 없습니다.")
            s.copy(
                enemyFood = (s.enemyFood - 300).coerceAtLeast(0),
                espionageMessage = "[이중첩자 교란] 광교동 군량고 좌표를 넘겨받아 보급로를 끊었다. 적 군량 -300석 (현재 ${(s.enemyFood - 300).coerceAtLeast(0)}석).",
                log = (s.log + "[이중첩자 교란] $buff").takeLast(8),
                statusMessage = "[이중첩자 교란] 적 군량 -300석"
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 「성주 승격 마일스톤 로드맵」
    // ──────────────────────────────────────────────────────────────

    /** 현재 승격 조건 평가 */
    fun promotionEvaluation(): PromotionEvaluation {
        val s = _uiState.value
        return promotionEngine.evaluate(s.promotionStage, alliedTerritoryCount(s), s.prestige)
    }

    /** 👑 승격 의식 개최 */
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
            s.copy(
                alliedTerritories = allies,
                promotionStage = result.stage,
                councilAuthorityBonus = s.councilAuthorityBonus + result.councilAuthorityBonus,
                promotionCeremony = result.ceremonyBody,
                promotionMessage = result.message,
                prestige = (s.prestige + 3).coerceIn(0, 100),
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

    /** 👑 승격 의식 배너 닫기 */
    fun dismissCeremony() {
        _uiState.update { s -> s.copy(promotionCeremony = null) }
    }

    // ──────────────────────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────────────────────

    /** 인접 영지 중 우호·동맹으로 묶인 수 (성주 승격 조건) */
    private fun alliedTerritoryCount(s: SlgGameState): Int {
        var count = 0
        // 원천동 상인회 — 야간 물자 협약 체결 시 협력 상인회로 전환
        if (s.eventAChoice == "A_PACT") count += 1
        // 광교동 호족연합 — 속국 조공 조약
        if (s.treaties.any { it.typeLabel == "속국 조공 조약" }) count += 1
        // 용인 수지 산악 협곡 — 상호방위 조약
        if (s.treaties.any { it.typeLabel == "상호방위 조약" }) count += 1
        return count
    }

    private fun buildPlayerLord(s: SlgGameState): Lord = Lord(
        id = "lord_player",
        name = "이광수 호족 (매탄동)",
        isHumanPlayer = true,
        territoryCode = SlgWorld.PLAYER_TERRITORY,
        troops = s.troops,
        grain = s.food,
        gold = s.gold,
        prestige = s.prestige,
        isCastellan = true
    )

    private fun buildCouncilLords(): List<Lord> = listOf(
        Lord("lord_1", "매탄동 호족 한서윤", false, SlgWorld.PLAYER_TERRITORY, troops = 320, grain = 1200, gold = 800, prestige = 55),
        Lord("lord_2", "매탄동 호족 오진혁", false, SlgWorld.PLAYER_TERRITORY, troops = 410, grain = 1500, gold = 900, prestige = 48),
        Lord("lord_3", "매탄동 호족 배수아", false, SlgWorld.PLAYER_TERRITORY, troops = 260, grain = 900, gold = 600, prestige = 61)
    )

    private fun buildTerritory(info: TerritoryInfo, fortress: Fortress?): Territory = Territory(
        code = info.code,
        name = info.name,
        province = "경기",
        cityCountyDistrict = info.region,
        population = info.population,
        maxLords = (info.population / 10000).coerceAtLeast(1),
        terrain = info.terrain,
        fortress = fortress
    )

    private data class Operation(
        val name: String,
        val territory: Territory,
        val attackers: Int,
        val defenders: Int
    )
}
