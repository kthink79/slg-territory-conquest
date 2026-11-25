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
import com.game.slg.core.economy.BoostKind
import com.game.slg.core.economy.BoostTarget
import com.game.slg.core.economy.ResourceBalance
import com.game.slg.core.economy.ResourceBoosts
import com.game.slg.core.empire.AbsorptionEngine
import com.game.slg.core.empire.AbsorptionMethod
import com.game.slg.core.empire.AbsorptionOutcome
import com.game.slg.core.empire.EmpireContext
import com.game.slg.core.empire.EmpireEngine
import com.game.slg.core.empire.EmpireSeasonResult
import com.game.slg.core.empire.Faction
import com.game.slg.core.empire.FactionRelation
import com.game.slg.core.empire.HeroCareer
import com.game.slg.core.hero.LordNeed
import com.game.slg.core.empire.ManagedTerritory
import com.game.slg.core.empire.ManpowerEngine
import com.game.slg.core.empire.MercenaryBand
import com.game.slg.core.empire.MercenaryEngine
import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.engine.BattleStrategy
import com.game.slg.core.engine.StrategyBattleEngine
import com.game.slg.core.engine.StrategyBattleResult
import com.game.slg.core.engine.WeaponTech
import com.game.slg.core.engine.WeaponTechState
import com.game.slg.core.hero.HeroDiscoveryEngine
import com.game.slg.core.hero.HeroDispositionEngine
import com.game.slg.core.hero.PendingDisposition
import com.game.slg.core.hero.HeroEmploymentEngine
import com.game.slg.core.hero.HeroVisit
import com.game.slg.core.empire.EmploymentRequest
import com.game.slg.core.hero.Hero
import com.game.slg.core.hero.HeroBonus
import com.game.slg.core.hero.HeroEngine
import com.game.slg.core.hero.HeroRole
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.hero.HeroStage
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
import com.game.slg.core.region.AiLordTurnEngine
import com.game.slg.core.region.AiSeasonOrder
import com.game.slg.core.region.AdjacentActionKind
import com.game.slg.core.region.AdjacentDiplomacyEngine
import com.game.slg.core.region.AdjacentActionResult
import com.game.slg.core.region.AdjacentRegionEngine
import com.game.slg.core.region.AdjacentRegionPanel
import com.game.slg.core.region.AdjacentStrategyEngine
import com.game.slg.core.region.AdjacentStrategyOp
import com.game.slg.core.region.AidCall
import com.game.slg.core.region.AidDirection
import com.game.slg.core.region.AidSource
import com.game.slg.core.region.CoalitionCouncilEngine
import com.game.slg.core.region.CoalitionState
import com.game.slg.core.region.CouncilVotingPower
import com.game.slg.core.region.DefenseAid
import com.game.slg.core.region.EmergencyCouncil
import com.game.slg.core.region.EmergencyCouncilEngine
import com.game.slg.core.region.SeasonClock
import com.game.slg.core.region.SeasonClockEngine
import com.game.slg.core.region.LeaderBond
import com.game.slg.core.region.LeaderStatus
import com.game.slg.core.region.RegionBoard
import com.game.slg.core.region.RegionBoardBuilder
import com.game.slg.core.region.RegionLeaderSeed
import com.game.slg.core.region.LordTrait
import com.game.slg.core.region.CommandProgress
import com.game.slg.core.region.ExpansionTier
import com.game.slg.core.region.AttackScope
import com.game.slg.core.region.AttackSide
import com.game.slg.core.region.AttackVerdict
import com.game.slg.core.region.LordCapacity
import com.game.slg.core.region.RegionArea
import com.game.slg.core.region.RegionUnion
import com.game.slg.core.region.RegionUnionEngine
import com.game.slg.core.region.RegionCatalog
import com.game.slg.core.save.CampaignSave
import com.game.slg.core.save.CampaignStore
import com.game.slg.core.save.InMemoryCampaignStore
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

/** 리더 의회 안건 상태 */
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
    val requiredLevel: Int = 1,
    /** 전략 상성 한 줄 — "기갑 예형 > 정면 종심" */
    val strategyLabel: String = "",
    /** 교환비 — "6.2 : 1" */
    val exchangeLabel: String = "",
    /** 전투 계산 근거 3~6줄 */
    val breakdown: List<String> = emptyList(),
    /** 아군·적 손실률 (%) */
    val ownLossPercent: Int = 0,
    val enemyLossPercent: Int = 0
)

/** 외교 조약 표시용 */
data class TreatyView(
    val id: String,
    val typeLabel: String,
    val partner: String,
    val remainingTurns: Int,
    val benefit: String
)

/** 인접 지역 행동 시트 — 전투·외교·전략 중 무엇을 열었는지 */
data class AdjacentSheet(
    val kind: AdjacentActionKind,
    val code: String,
    val name: String,
    val result: AdjacentActionResult? = null
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
    LORD_SELECT("리더 선택"),
    PLAY("플레이")
}

/** 시작 방식 — 리더로 시작 / 영웅으로 시작 */
enum class StartMode(val label: String, val detail: String) {
    LORD("리더로 시작", "지역 리더 하나를 인계받아 영지를 운영합니다."),
    HERO("영웅으로 시작", "재야 영웅으로 시작해 리더에게 등용을 청합니다. 상대는 유저일 수도, 아닐 수도 있습니다.")
}

/** 리더 명부 1줄 — AI 리더과 플레이어가 같은 형식으로 보인다 */
data class LordView(
    val id: String,
    val name: String,
    val stronghold: String,
    val trait: LordTrait,
    val troops: Int,
    val gold: Int,
    val grain: Int,
    val prestige: Int,
    val arm: CombatArm,
    val isPlayer: Boolean = false,
    /** 이 리더의 세력이 우리 연방에 편입됐는지 — 교전 버튼이 잠긴다 */
    val incorporated: Boolean = false,
    /** 다른 AI 리더에게 병합·종속된 형태 — null 이면 독립 세력 */
    val bond: LeaderBond? = null,
    /** 병합·종속 대상(우두머리) 리더 id */
    val masterId: String? = null,
    /** 최근 절기의 AI 활동 한 줄 — 세부맵 카드에 보인다 */
    val activity: String = ""
) {
    val traitLabel: String get() = trait.displayName
    val traitIcon: String get() = trait.icon
    val arms: String get() = arm.displayName

    /** 다른 세력에 묶였는지 */
    val isBound: Boolean get() = masterId != null && bond != null
}

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
        HiddenTalentView("서이레", "구역 자치회 회계 담당", "리더 연합의 병력 장부를 이중으로 관리 중", 48),
        HiddenTalentView("문태오", "중앙로 상인", "최근 인접 구역 인사와 접촉 빈도 급증", 33)
    )

    /** 인구 1,000명당 리더 1명 — 화면 안내용 */
    const val LORD_RULE_LABEL = "인구 1,000명당 1리더"
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
    // ── 리더 명부 ──
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
    /** 세부맵을 열어 둔 지역 코드 — null 이면 닫혀 있다 */
    val regionDetailCode: String? = null,
    // ── 기본 지표 ──
    val seasonName: String = "봄 (예산 편성·지방세 징수)",
    val population: Int = 48604,
    val gold: Int = ResourceBalance.START_GOLD,
    val food: Int = ResourceBalance.START_FOOD,
    val troops: Int = ResourceBalance.START_TROOPS,
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
    val promotionMessage: String? = null,
    // ── 영웅(인재) ──
    val heroes: HeroRosterState = HeroRosterState.initial(),
    val heroMessage: String? = null,
    // ── 재원 탭 서브 메뉴 (식량·골드·인구수·병력) ──
    val resourceMessage: String? = null,
    /** 이번 절기에 재원 증가 방법을 쓴 횟수 — 절기를 넘기면 초기화된다 */
    val boostUses: Map<String, Int> = emptyMap(),
    // ── 지역 연합 · 스폰 보호 ──
    val union: RegionUnion = RegionUnion(),
    // ── 영토 운영: 편입 영지 · 일임 · 용병 ──
    val territories: List<ManagedTerritory> = emptyList(),
    val selectedManagedCode: String = "",
    val factions: List<Faction> = emptyList(),
    val mercenaries: List<MercenaryBand> = emptyList(),
    val empireMessage: String? = null,
    val lastAbsorption: AbsorptionOutcome? = null,
    // ── AI 리더 동향 ──
    /** 지난 절기에 AI 리더들이 벌인 일 — 최대 4줄 */
    val aiNews: List<String> = emptyList(),
    /** 진행한 절기 횟수 — AI 턴 시드·세부맵 연출에 쓴다 */
    val seasonTurn: Int = 0,
    // ── 전략 · 무기 기술 ──
    val weaponTech: WeaponTechState = WeaponTechState(),
    val battleStrategy: BattleStrategy = BattleStrategy.FRONTAL,
    val strategyMessage: String? = null,
    val lastStrategy: StrategyBattleResult? = null,
    // ── 영웅 탐색 · 방문 합류 ──
    val heroVisit: HeroVisit? = null,
    val heroVisitMessage: String? = null,
    val heroSearchMessage: String? = null,
    // ── 영웅으로 시작 ──
    val startMode: StartMode = StartMode.LORD,
    val pendingHeroPick: String? = null,
    val heroCareer: HeroCareer? = null,
    val pendingEmployment: EmploymentRequest? = null,
    // ── 편입 세력 소속 영웅 처분 (등용·방출) ──
    val pendingDisposition: List<PendingDisposition> = emptyList(),
    // ── 인접 지역 전투·외교·전략 패널 ──
    /** 지도에서 연 인접 지역 행동 시트 — 닫혀 있으면 null */
    val adjacentSheet: AdjacentSheet? = null,
    /** 인접 지역 행동 결과 한 번 */
    val adjacentMessage: String? = null,
    /** 인접 지역 교전으로 깎은 리더 병력 — leaderId → 누적 손실 */
    val adjacentDamage: Map<String, Int> = emptyMap(),
    // ── 지역 리더 연합 의회 (공동 전투·외교·전략) ──
    val coalition: CoalitionState = CoalitionState(),
    // ── 긴급 의회 · 상호 방어 지원 ──
    val emergency: EmergencyCouncil? = null,
    val aidCall: AidCall? = null,
    val aidLog: List<DefenseAid> = emptyList(),
    /** 긴급 방어 결의로 얻은 방어 보너스가 남은 절기 */
    val defensePostureTurns: Int = 0,
    // ── 지속 시간 흐름 · 미접속 AI 대행 ──
    val clock: SeasonClock = SeasonClock(),
    /** 미접속 동안 AI가 대신 운영한 결과 — 한 번 뜨고 닫친다 */
    val offlineReport: String? = null
) {
    /** 배치된 영웅이 주는 실제 보너스 */
    val heroBonus: HeroBonus
        get() = heroes.activeBonus()

    /** 스폰 보호 중인지 */
    val spawnProtected: Boolean
        get() = union.active

    /** 이 공격이 지금 허용되는지 */
    fun attackVerdict(scope: AttackScope, side: AttackSide = AttackSide.EXPEDITION): AttackVerdict =
        RegionUnionEngine.decide(union, scope, side)

    /** 지역 명부의 같은 지역 AI 리더 수 (연합 대상) */
    val unionPartnerCount: Int
        get() = if (union.active) aiLordCount else 0
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

    /** 같은 구역에서 이미 인계된 리더(플레이어)을 뺀 AI 운영 수 */
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

    // ── 영토 운영 파생값 ────────────────────────────────────────

    /** 편입 영지 전체 인구 — 정규 병력 상한의 기준 */
    val totalTerritoryPopulation: Int
        get() = territories.sumOf { it.population }.coerceAtLeast(population)

    /** 정규 병력 상한 — 전체 인구의 5% */
    val troopCap: Int
        get() = ManpowerEngine.capOf(totalTerritoryPopulation)

    val mercenaryTroops: Int
        get() = mercenaries.sumOf { it.troops }

    /** 정규 + 용병 */
    val totalTroops: Int
        get() = troops + mercenaryTroops

    /** 속국·자치령 포함 총력 (전투 보너스의 근거) */
    val empirePower: Int
        get() = ManpowerEngine.totalPower(territories, troops, mercenaryTroops)

    /** 연방·속국이 많을수록 커지는 전투 보너스 (%) */
    val coalitionBonusPercent: Int
        get() = EmpireEngine.coalitionBonusPercent(territories)

    val selectedManaged: ManagedTerritory?
        get() = territories.firstOrNull { it.code == selectedManagedCode }
            ?: territories.firstOrNull { it.status == TerritoryStatus.DIRECT }
            ?: territories.firstOrNull()

    // ── 지역 세부맵 ─────────────────────────────────────────────

    /** 세부맵을 열어 둔 지역 노드 */
    val regionDetailNode: MapNode?
        get() = regionDetailCode?.let { TerritoryMapLayout.node(mapNodes, it) }

    /** 세부맵 데이터 — 열려 있을 때만 계산한다 */
    val regionDetailBoard: RegionBoard?
        get() = regionDetailCode?.let { regionBoardFor(it) }

    /**
     * 지역 1곳의 리더 배치·현황.
     *
     * · 내 지역([regionCode])은 지금 운영 중인 리더 명부를 그대로 쓴다(실시간).
     * · 다른 지역은 결정적으로 생성한 명부를 "정찰 전 추정"으로 보여 준다.
     */
    fun regionBoardFor(code: String): RegionBoard {
        val node = TerritoryMapLayout.node(mapNodes, code)
        val regionName = node?.name ?: regionCode
        if (code == regionCode) {
            val playerName = playerLord?.name ?: "이광수"
            val seeds = lords.map { lord ->
                val faction = factions.firstOrNull { it.id == "faction_${lord.id}" }
                val territory = faction?.let { f -> territories.firstOrNull { it.code == f.territoryCode } }
                val federation = if (lord.incorporated) territory?.status ?: TerritoryStatus.VASSAL else null
                val note = when {
                    lord.isPlayer -> "${regionName} 인계 세력"
                    lord.isBound -> lord.activity
                    federation != null -> listOfNotNull(
                        territory?.absorbedBy?.let { "편입 경위 · $it" },
                        lord.activity.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    else -> lord.activity
                }
                RegionLeaderSeed(
                    id = lord.id,
                    name = if (lord.isPlayer) playerName else lord.name,
                    stronghold = lord.stronghold.ifBlank { regionName },
                    traitLabel = lord.traitLabel,
                    traitIcon = lord.traitIcon,
                    troops = if (lord.isPlayer) troops else lord.troops,
                    gold = if (lord.isPlayer) gold else lord.gold,
                    food = if (lord.isPlayer) food else lord.grain,
                    prestige = if (lord.isPlayer) prestige else lord.prestige,
                    isPlayer = lord.isPlayer,
                    federation = federation,
                    aiBond = lord.bond,
                    masterId = lord.masterId,
                    note = note
                )
            }
            return RegionBoardBuilder.build(code, regionName, seeds, live = true)
        }
        val area = RegionCatalog.area(code)
        val generated = area?.let { AiLordGenerator.generate(it) }.orEmpty()
        val seeds = generated.map { profile ->
            RegionLeaderSeed(
                id = profile.id,
                name = profile.name,
                stronghold = profile.stronghold,
                traitLabel = profile.trait.displayName,
                traitIcon = profile.trait.icon,
                troops = (profile.troops - (adjacentDamage[profile.id] ?: 0)).coerceAtLeast(0),
                gold = profile.gold,
                food = profile.grain,
                prestige = profile.prestige,
                note = if (node?.isLocked == true) "미개방 구역" else "정찰 전 추정 명부"
            )
        }
        return RegionBoardBuilder.build(code, regionName, seeds, live = false)
    }

    /** 아직 편입하지 않은 세력 */
    val openFactions: List<Faction>
        get() = factions.filter { !it.incorporated }

    /** 편입한 세력 수 */
    val incorporatedCount: Int
        get() = factions.count { it.incorporated }

    val absorbedCount: Int
        get() = territories.count { it.absorbedBy != "인계" }

    /** 우리 세력의 처지 — 흡수 판정에 쓰는 값 */
    fun empireContext(): EmpireContext = EmpireContext(
        troops = totalTroops,
        prestige = prestige,
        gold = gold,
        food = food,
        morale = morale,
        diplomacy = diplomacy,
        espionagePower = espionagePower,
        diplomacyHeroBonus = heroBonus.diplomacyBonus,
        opsHeroBonus = heroBonus.combatPercent,
        seasonName = seasonName,
        regionName = regionName
    )

    /** 절기 예상 세수 (영지 + 조공) */
    val projectedTax: Int
        get() = EmpireEngine.projectedTax(territories, factions)

    /** 아직 만나지 못한 영웅 수 — 목록에는 실루엣으로 나온다 */
    val undiscoveredHeroCount: Int
        get() = HeroDiscoveryEngine.undiscoveredCount(heroes)

    /** 보유·만난 영웅 */
    val knownHeroes: List<Hero>
        get() = HeroDiscoveryEngine.knownHeroes(heroes)

    /** 영웅 스타트 모드인지 */
    val isHeroStart: Boolean
        get() = startMode == StartMode.HERO && !isCampaignStarted && pendingHeroPick != null

    /** 처분 대기 영웅 수 — 영토 탭 배지에 쓴다 */
    val pendingDispositionCount: Int
        get() = pendingDisposition.size

    /** 처분 대기 영웅의 실제 영웅 정보 — 없어진 id 는 걸러낸다 */
    val pendingHeroes: List<Pair<PendingDisposition, Hero>>
        get() = pendingDisposition.mapNotNull { entry ->
            HeroRoster.byId(entry.heroId)?.let { entry to it }
        }

    /** 편입(직할·속국·자치령)된 리더 수 */
    val incorporatedLordCount: Int
        get() = lords.count { !it.isPlayer && it.incorporated }

    // ── 인접 지역 패널 · 연합 의회 · 방어 (2026-11-25) ───────────────

    /**
     * 선택한 인접 지역의 하단 패널 — 내 지역·잠긴 지역이면 null.
     * 통합 리더가 있으면 지역 단위, 없으면 개별 리더 단위로 전투 대상을 돌려준다.
     */
    val selectedAdjacentPanel: AdjacentRegionPanel?
        get() {
            val node = selectedTerritory ?: return null
            if (node.code == regionCode || node.isLocked) return null
            return AdjacentRegionEngine.panel(
                code = node.code,
                name = node.name,
                relationLabel = node.relationLabel,
                board = regionBoardFor(node.code),
                unionActive = union.active
            )
        }

    /** 의회 의결권 — 기본 1표 + 직할·편입 리더 수 */
    val councilVotingPower: Int
        get() = CouncilVotingPower.of(incorporatedLordCount)

    val councilVotingLabel: String
        get() = CouncilVotingPower.label(councilVotingPower, incorporatedLordCount)

    /** 긴급 방어 결의가 살아 있는 동안의 방어 보너스 (%) */
    val defensePostureBonusPercent: Int
        get() = if (defensePostureTurns > 0) 12 else 0

    /** 연합 의회 대기 안건 수 */
    val coalitionPendingCount: Int get() = coalition.pendingCount

    /** 긴급 의회가 열려 있는지 */
    val emergencyOpen: Boolean get() = emergency != null

    /** 지금 바로 보낼 수 있는 지원 요청 하나 — 이미 처리했으면 null */
    val openAidCall: AidCall? get() = aidCall?.takeIf { !it.answered }

    companion object {
        const val NIGHT_MEETING_SUSPICION: Int = 50
    }
}

// ============================================================================
// ViewModel — 인트로 → 지역 선택 → 리더 인계 → 4탭 영지 운영
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
    private val promotionEngine: IPromotionEngine = PromotionEngine(),
    private val store: CampaignStore = InMemoryCampaignStore()
) : ViewModel() {

    private val saved: CampaignSave? = store.load()
    private val startRegion: RegionArea =
        saved?.let { RegionCatalog.area(it.regionCode) } ?: RegionCatalog.defaultRegion()
    private val startLords: List<AiLordProfile> = AiLordGenerator.generate(startRegion)

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<SlgGameState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow(MainNavigationState())
    val navigation: StateFlow<MainNavigationState> = _navigation.asStateFlow()

    private var season: Season = Season.SPRING
    private val treaties = mutableListOf<DiplomaticTreaty>()

    /** 저장값이 있으면 그 지역·리더으로 이어서 시작한다 */
    private fun buildInitialState(): SlgGameState {
        val base = SlgGameState(
            stage = if (store.isIntroSeen()) GameStage.REGION_SELECT else GameStage.INTRO,
            introSeen = store.isIntroSeen(),
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
            selectedTerritoryCode = startRegion.code,
            heroes = HeroRosterState(),
            territories = listOf(homeTerritory(startRegion, 1_200)),
            selectedManagedCode = startRegion.code,
            factions = factionsOf(startRegion, startLords),
            union = RegionUnion(
                regionCode = startRegion.code,
                regionName = startRegion.name,
                remainingTurns = 0
            ),
            // 시간은 계속 흐른다 — 새 게임도 지금부터 시계를 센다
            clock = SeasonClock.start(System.currentTimeMillis())
        )
        val restore = saved ?: return base
        val lord = startLords.firstOrNull { it.id == restore.lordId } ?: return base
        val facilities = restore.facilities.mapNotNull { name ->
            FacilityType.entries.firstOrNull { it.name == name }
        }.toSet()
        val stage = PromotionStage.entries.firstOrNull { it.name == restore.promotionStage }
            ?: PromotionStage.DISTRICT_LORD
        val command = CommandProgress(restore.level.coerceIn(1, CommandProgress.MAX_LEVEL), restore.xp)
        val restoredTerritories = restore.territories.mapNotNull { decodeTerritory(it) }
            .ifEmpty { listOf(homeTerritory(startRegion, restore.troops)) }
        val restoredCodes = restoredTerritories.map { it.code }.toSet()
        // 교전으로 깎은 적 리더 병력과 편입 상태 — 저장값이 있으면 그쪽이 우선
        val savedFactionStates = restore.factionStates.mapNotNull { raw ->
            val p = raw.split('|')
            val id = p.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            id to ((p.getOrNull(1)?.toIntOrNull() ?: 0) to (p.getOrNull(2) == "true"))
        }.toMap()
        val restoredFactions = factionsOf(startRegion, startLords, lord.id).map { faction ->
            val owned = restoredTerritories.firstOrNull { it.code == faction.territoryCode }
            val fromTerritory = if (owned == null) faction
            else faction.copy(
                incorporated = true,
                absorbedBy = owned.absorbedBy,
                relation = when (owned.status) {
                    TerritoryStatus.DIRECT -> FactionRelation.VASSAL
                    TerritoryStatus.VASSAL -> FactionRelation.VASSAL
                    TerritoryStatus.AUTONOMOUS -> FactionRelation.AUTONOMOUS
                },
                tributePerSeason = owned.tributePerSeason,
                troops = owned.troops
            )
            val saved = savedFactionStates[faction.id] ?: return@map fromTerritory
            fromTerritory.copy(
                troops = saved.first.coerceAtLeast(0),
                incorporated = fromTerritory.incorporated || saved.second
            )
        }
        val restoredPending = restore.pendingDisposition.mapNotNull { PendingDisposition.decode(it) }
        // AI 리더 성장·병합 기록 — 저장값이 있으면 생성값 대신 그쪽을 쓴다
        val savedLordStates = restore.lordStates.mapNotNull { raw ->
            val p = raw.split('|')
            val id = p.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            id to p
        }.toMap()
        val restoredRoster = if (restore.ownedHeroes.isEmpty()) {
            HeroRosterState.initial()
        } else {
            HeroRosterState.fromSaves(
                owned = restore.ownedHeroes,
                placed = restore.heroPlacements,
                affinity = restore.heroAffinity,
                discovered = restore.heroDiscovery
            )
        }
        return base.copy(
            stage = GameStage.PLAY,
            introSeen = true,
            playerLordId = lord.id,
            selectedLordId = lord.id,
            lords = base.lords.map { view ->
                if (view.id == lord.id) view.copy(name = "이광수", isPlayer = true)
                else {
                    val saved = savedLordStates[view.id]
                    val grown = if (saved == null) view else view.copy(
                        troops = saved.getOrNull(1)?.toIntOrNull() ?: view.troops,
                        gold = saved.getOrNull(2)?.toIntOrNull() ?: view.gold,
                        grain = saved.getOrNull(3)?.toIntOrNull() ?: view.grain,
                        prestige = saved.getOrNull(4)?.toIntOrNull() ?: view.prestige,
                        masterId = saved.getOrNull(5)?.takeIf { it.isNotBlank() },
                        bond = saved.getOrNull(6)?.takeIf { it.isNotBlank() }?.let { name ->
                            LeaderBond.entries.firstOrNull { it.name == name }
                        },
                        activity = saved.getOrNull(7) ?: ""
                    )
                    val faction = restoredFactions.firstOrNull { it.id == "faction_${grown.id}" }
                    if (faction == null) grown
                    else grown.copy(troops = faction.troops, incorporated = faction.incorporated)
                }
            },
            registeredLords = 1,
            gold = restore.gold,
            food = restore.food,
            troops = restore.troops,
            morale = restore.morale,
            prestige = restore.prestige,
            facilities = facilities,
            researchedTechs = restore.researchedTechs,
            alliedTerritories = restore.alliedTerritories,
            promotionStage = stage,
            councilAuthorityBonus = restore.councilAuthorityBonus,
            command = command,
            unlockedHop = command.unlockedHop,
            mapNodes = TerritoryMapLayout.build(
                start = startRegion,
                unlockedHop = command.unlockedHop,
                playerName = "이광수",
                homeTroops = restore.troops
            ),
            selectedTerritoryCode = startRegion.code,
            heroes = HeroDispositionEngine.markCaptured(restoredRoster, restoredPending),
            pendingDisposition = restoredPending,
            union = RegionUnion(
                regionCode = startRegion.code,
                regionName = startRegion.name,
                remainingTurns = restore.spawnTurns.coerceIn(0, RegionUnion.DEFAULT_TURNS)
            ),
            territories = restoredTerritories,
            selectedManagedCode = restoredTerritories.firstOrNull()?.code ?: startRegion.code,
            factions = restoredFactions,
            mercenaries = restore.mercenaries.mapNotNull { decodeMercenary(it) },
            weaponTech = WeaponTechState(
                restore.weaponTech.mapNotNull { (name, level) ->
                    WeaponTech.entries.firstOrNull { it.name == name }?.let { it to level }
                }.toMap()
            ),
            startMode = if (restore.startMode == StartMode.HERO.name) StartMode.HERO else StartMode.LORD,
            heroCareer = restore.heroCareer?.let { career ->
                HeroCareer(
                    heroId = career.substringBefore('|'),
                    heroName = career.substringAfter('|').substringBefore('|'),
                    employerLordId = career.split('|').getOrNull(6)?.takeIf { it.isNotBlank() }
                        ?: "ai_${startRegion.code}_1",
                    employerLordName = career.split('|').getOrNull(2) ?: "리더",
                    employerTerritory = startRegion.name,
                    loyalty = career.split('|').getOrNull(3)?.toIntOrNull() ?: 70,
                    merit = career.split('|').getOrNull(4)?.toIntOrNull() ?: 0,
                    privateTroops = career.split('|').getOrNull(5)?.toIntOrNull() ?: 0
                )
            },
            seasonTurn = restore.seasonTurn,
            adjacentDamage = restore.adjacentDamage.mapNotNull { raw ->
                val id = raw.substringBefore('|').takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val damage = raw.substringAfter('|', "0").toIntOrNull() ?: 0
                if (damage <= 0) null else id to damage
            }.toMap(),
            clock = SeasonClock(
                seasonMinutes = restore.seasonMinutes.coerceIn(1, 1440),
                lastTickMillis = if (restore.lastTickMillis > 0L) restore.lastTickMillis
                else System.currentTimeMillis(),
                autoAdvance = restore.autoAdvance,
                aiAutopilot = restore.aiAutopilot
            ),
            statusMessage = "[이어서] ${startRegion.fullName} · Lv.${command.level} ${command.tier.label} — 저장된 진행을 불러왔습니다.",
            log = listOf("[이어서] ${startRegion.fullName}에서 플레이를 재개했습니다.")
        )
    }

    /** 재개에 필요한 최소값만 저장한다 */
    private fun persist() {
        val state = _uiState.value
        val lordId = state.playerLordId ?: return
        if (state.stage != GameStage.PLAY) return
        store.save(
            CampaignSave(
                regionCode = state.regionCode,
                lordId = lordId,
                level = state.command.level,
                xp = state.command.xp,
                gold = state.gold,
                food = state.food,
                troops = state.troops,
                morale = state.morale,
                prestige = state.prestige,
                facilities = state.facilities.map { it.name }.toSet(),
                researchedTechs = state.researchedTechs,
                alliedTerritories = state.alliedTerritories,
                promotionStage = state.promotionStage.name,
                councilAuthorityBonus = state.councilAuthorityBonus,
                spawnTurns = state.union.remainingTurns,
                ownedHeroes = state.heroes.ownedHeroes.map { it.id }.toSet(),
                heroPlacements = state.heroes.ownedHeroes
                    .mapNotNull { hero ->
                        state.heroes.recordOf(hero.id).assignedRole?.let { hero.id to it.name }
                    }
                    .toMap(),
                heroAffinity = state.heroes.ownedHeroes.associate { it.id to state.heroes.recordOf(it.id).affinity },
                territories = state.territories.map { encodeTerritory(it) },
                mercenaries = state.mercenaries.map { encodeMercenary(it) },
                weaponTech = state.weaponTech.levels.mapKeys { it.key.name },
                startMode = state.startMode.name,
                heroCareer = state.heroCareer?.let {
                    "${it.heroId}|${it.heroName}|${it.employerLordName}|${it.loyalty}|${it.merit}|${it.privateTroops}|${it.employerLordId}"
                },
                factionStates = state.factions.map { "${it.id}|${it.troops}|${it.incorporated}" },
                pendingDisposition = state.pendingDisposition.map { it.encode() },
                heroDiscovery = state.heroes.records.values
                    .filter { !it.owned && it.stage != HeroStage.UNDISCOVERED }
                    .associate { it.id to "${it.stage.name}|${it.affinity}" },
                lordStates = state.lords.filterNot { it.isPlayer }.map { encodeLordState(it) },
                seasonTurn = state.seasonTurn,
                lastTickMillis = state.clock.lastTickMillis,
                seasonMinutes = state.clock.seasonMinutes,
                autoAdvance = state.clock.autoAdvance,
                aiAutopilot = state.clock.aiAutopilot,
                adjacentDamage = state.adjacentDamage.map { (id, damage) -> "$id|$damage" }
            )
        )
    }

    /** AI 리더 한 명 상태 — "id|troops|gold|grain|prestige|masterId|bond|activity" */
    private fun encodeLordState(lord: LordView): String = listOf(
        lord.id,
        lord.troops.toString(),
        lord.gold.toString(),
        lord.grain.toString(),
        lord.prestige.toString(),
        lord.masterId ?: "",
        lord.bond?.name ?: "",
        lord.activity.replace('|', '/')
    ).joinToString("|")

    private fun encodeTerritory(t: ManagedTerritory): String = listOf(
        t.code, t.name, t.population.toString(), t.status.name, t.morale.toString(),
        t.development.toString(), t.troops.toString(), t.food.toString(), t.gold.toString(),
        t.defense.toString(), t.viceroyHeroId ?: "", t.viceroyName ?: "", t.viceroyRole ?: "",
        t.tributePerSeason.toString(), t.absorbedBy
    ).joinToString("|")

    private fun decodeTerritory(raw: String): ManagedTerritory? {
        val p = raw.split('|')
        if (p.size < 10) return null
        val status = TerritoryStatus.entries.firstOrNull { it.name == p[3] } ?: TerritoryStatus.DIRECT
        return ManagedTerritory(
            code = p[0],
            name = p[1],
            population = p[2].toIntOrNull() ?: 0,
            status = status,
            morale = p[4].toIntOrNull() ?: 55,
            development = p[5].toIntOrNull() ?: 20,
            troops = p[6].toIntOrNull() ?: 0,
            food = p[7].toIntOrNull() ?: 0,
            gold = p[8].toIntOrNull() ?: 0,
            defense = p[9].toIntOrNull() ?: 30,
            viceroyHeroId = p.getOrNull(10)?.takeIf { it.isNotBlank() },
            viceroyName = p.getOrNull(11)?.takeIf { it.isNotBlank() },
            viceroyRole = p.getOrNull(12)?.takeIf { it.isNotBlank() },
            tributePerSeason = p.getOrNull(13)?.toIntOrNull() ?: 0,
            absorbedBy = p.getOrNull(14) ?: "인계"
        )
    }

    private fun encodeMercenary(band: MercenaryBand): String = listOf(
        band.id, band.name, band.troops.toString(), band.hireCost.toString(),
        band.upkeepPerSeason.toString(), band.contractSeasons.toString(), band.origin
    ).joinToString("|")

    private fun decodeMercenary(raw: String): MercenaryBand? {
        val p = raw.split('|')
        if (p.size < 6) return null
        return MercenaryBand(
            id = p[0],
            name = p[1],
            troops = p[2].toIntOrNull() ?: 0,
            hireCost = p[3].toIntOrNull() ?: 0,
            upkeepPerSeason = p[4].toIntOrNull() ?: 0,
            contractSeasons = p[5].toIntOrNull() ?: 4,
            origin = p.getOrNull(6) ?: "용병단"
        )
    }

    /** 📊 정보 → 새 게임 (저장 삭제 후 지역 선택으로) */
    fun restartCampaign() {
        store.save(null)
        val region = RegionCatalog.defaultRegion()
        val lords = AiLordGenerator.generate(region)
        season = Season.SPRING
        treaties.clear()
        _navigation.value = MainNavigationState()
        _uiState.value = SlgGameState(
            stage = GameStage.REGION_SELECT,
            introSeen = true,
            regionCode = region.code,
            regionName = region.name,
            regionFullName = region.fullName,
            regionGroupKey = region.groupKey,
            regionPopulation = region.population,
            lordCapacity = region.lordCapacity,
            population = region.population,
            lords = lords.map { it.toView() },
            registeredLords = 0,
            mapNodes = TerritoryMapLayout.build(region, 0, "이광수", 0),
            selectedTerritoryCode = region.code,
            heroes = HeroRosterState(),
            territories = listOf(homeTerritory(region, 0)),
            selectedManagedCode = region.code,
            factions = factionsOf(region, lords),
            union = RegionUnion(regionCode = region.code, regionName = region.name, remainingTurns = 0),
            statusMessage = "[새 게임] 시작 지역을 다시 선택하세요.",
            log = listOf("[새 게임] 새 지역에서 다시 시작합니다.")
        )
    }

    // ──────────────────────────────────────────────────────────────
    // 시작 흐름: 인트로 → 지역 선택 → 리더 인계
    // ──────────────────────────────────────────────────────────────

    /** 인트로 마지막 장면 또는 SKIP */
    fun finishIntro() {
        store.setIntroSeen(true)
        _uiState.update { s ->
            s.copy(
                stage = if (s.isCampaignStarted || s.introReplay) GameStage.PLAY else GameStage.REGION_SELECT,
                introSeen = true,
                introReplay = false
            )
        }
        persist()
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
     * 시작 지역 선택 — 인구에 맞는 리더 정원을 계산하고 AI 리더 명부를 새로 만든다.
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
                heroes = HeroRosterState(),
                territories = listOf(homeTerritory(region, 0)),
                selectedManagedCode = region.code,
                factions = factionsOf(region, lords),
                union = RegionUnion(regionCode = region.code, regionName = region.name, remainingTurns = 0),
                statusMessage = "[지역 선택] ${region.fullName} — 인구 ${region.population}명 · 리더 ${region.lordCapacity}석",
                log = listOf("[D+14] ${region.fullName}에 ${region.lordCapacity}명의 리더이 각자 거점을 세웠다.")
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
     * AI 리더 하나를 인계받아 플레이어 세력으로 시작.
     * 인계 시 그 리더의 병력·자금·보급·명성을 그대로 물려받는다.
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
                prestige = chosen.prestige,
                // 초반 재원 부족 완화 — 인계분에 창업 지원(자금 8,000G · 식량 16,000석 · 병력 1,200명)을 더한다
                gold = chosen.gold + ResourceBalance.FOUNDING_GOLD,
                food = chosen.grain + ResourceBalance.FOUNDING_FOOD,
                troops = chosen.troops + ResourceBalance.FOUNDING_TROOPS,
                morale = (55 + chosen.prestige / 5).coerceIn(0, 100),
                command = command,
                unlockedHop = command.unlockedHop,
                mapNodes = TerritoryMapLayout.build(
                    region,
                    command.unlockedHop,
                    playerName,
                    chosen.troops + ResourceBalance.FOUNDING_TROOPS
                ),
                selectedTerritoryCode = s.regionCode,
                heroes = HeroRosterState.initial(),
                territories = listOf(
                    homeTerritory(region, chosen.troops + ResourceBalance.FOUNDING_TROOPS)
                ),
                selectedManagedCode = region.code,
                factions = factionsOf(region, AiLordGenerator.generate(region), lordId),
                heroMessage = "기본 영웅 3명과 함께 시작합니다 — 한서연[내정] · 윤도현[기술] · 차명숙[상업]",
                union = RegionUnionEngine.open(s.regionCode, s.regionName),
                statusMessage = "[인계 완료] ${chosen.stronghold} — ${chosen.name} 리더의 거점을 이어받았습니다. " +
                    "창업 지원 자금 ${comma(ResourceBalance.FOUNDING_GOLD)}G · 식량 ${comma(ResourceBalance.FOUNDING_FOOD)}석 · " +
                    "병력 ${comma(ResourceBalance.FOUNDING_TROOPS)}명.",
                log = (s.log + "[인계] ${s.regionFullName} ${chosen.stronghold}의 ${chosen.name} 리더 세력을 인계받았다. " +
                    "Lv.1 관할 구역부터 시작합니다." +
                    "[지역 연합] 같은 지역 리더 ${s.lordCapacity - 1}명과 자동 연합 · 스폰 보호 " +
                    "${RegionUnion.DEFAULT_TURNS}턴 시작.").takeLast(8)
            )
        }
        persist()
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
            if (s.selectedTerritoryCode == node.code) {
                // 이미 선택한 지역을 한 번 더 누르면 — 리더 배치·현황 세부맵을 연다
                s.copy(
                    regionDetailCode = node.code,
                    statusMessage = "[세부맵] '${node.name}' 지역의 리더 배치와 현황을 엽니다."
                )
            } else s.copy(
                selectedTerritoryCode = node.code,
                statusMessage = "[지도] '${node.name}' 선택 — ${node.relationLabel} · 한 번 더 누르면 세부맵"
            )
        }
    }

    /** 선택 지역(또는 지정 지역)의 리더 배치·현황 세부맵 열기 */
    fun openRegionDetail(code: String = _uiState.value.selectedTerritoryCode) {
        _uiState.update { s ->
            val node = TerritoryMapLayout.node(s.mapNodes, code) ?: return@update s
            if (!node.unlocked) {
                return@update s.copy(
                    statusMessage = "[미개방] '${node.name}'은(는) Lv.${tierForHop(node.hopDistance)} " +
                        "${tierForHopLabel(node.hopDistance)} 단계에서 열립니다."
                )
            }
            s.copy(
                selectedTerritoryCode = code,
                regionDetailCode = code,
                statusMessage = "[세부맵] '${node.name}' — 리더 ${node.lordCapacity}석 배치·현황"
            )
        }
    }

    /** 세부맵 닫기 */
    fun closeRegionDetail() {
        _uiState.update { s -> s.copy(regionDetailCode = null) }
    }

    fun openTab(tab: MainTab, sub: Int = 0) {
        _navigation.update { it.open(tab, sub) }
    }

    fun selectSubTab(index: Int) {
        _navigation.update { it.selectSub(index) }
    }

    fun backToMain() {
        _navigation.update { it.back() }
    }

    // ──────────────────────────────────────────────────────────────
    // 영지·지도
    // ──────────────────────────────────────────────────────────────

    /** 절기 진행 — 4계절 순환 (봄 세수 · 여름 재난 · 가을 결산 · 겨울 유지비) */
    fun advanceSeason(dikeMaintenanceDone: Boolean = true) {
        val now = System.currentTimeMillis()
        _uiState.update { s ->
            nextSeasonState(s, dikeMaintenanceDone)
                // 수동 진행도 시계를 현재에 맞춘다 — 다음 자동 절기는 여기서부터 센다
                .copy(clock = s.clock.copy(lastTickMillis = now))
        }
        persist()
    }

    /**
     * 절기 1회 진행의 실제 계산 — 수동 버튼과 자동 시계(미접속 대행)가 같은 엔진을 쓴다.
     */
    private fun nextSeasonState(s: SlgGameState, dikeMaintenanceDone: Boolean): SlgGameState {
            val playerName = s.playerLord?.name ?: "이광수"
            val playerLord = Lord(
                id = "lord_player",
                name = "$playerName 리더",
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
                fertileScore = 1.0 + economy,
                population = s.population
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
            val heroBonus = s.heroBonus
            val heroGold = (result.goldChange.coerceAtLeast(0) * heroBonus.goldPercent) / 100
            val heroGrain = (grainGain.coerceAtLeast(0) * heroBonus.foodPercent) / 100
            // 초반 재원 부족 완화 — 절기마다 인구에 비례한 기본 수급을 더한다
            val baseGold = ResourceBalance.seasonGold(s.population)
            val baseFood = ResourceBalance.seasonFood(s.population, s.facilities)
            val (unionNext, unionMessage) = RegionUnionEngine.onTurnPassed(s.union)
            val before = s.command
            val gained = before.gainXp(CommandProgress.XP_SEASON)
            val heroNote = if (heroBonus.isEmpty) "" else "\n[영웅] 배치 보너스 — ${heroBonus.summary()}"
            val baseNote = "\n[수급] 기본 수급 — 자금 +${comma(baseGold)}G · 식량 +${comma(baseFood)}석"

            // ── 영토 운영 정산 — 일임 태수 · 속국 조공 · 용병 유지비 ──
            val heroStats = s.heroes.ownedHeroes.associate { it.id to it.stats }
            val empire = EmpireEngine.processSeason(
                territories = s.territories,
                factions = s.factions,
                regularTroops = s.troops,
                seasonName = season.displayName,
                heroStats = heroStats
            )
            val goldBeforeMerc = playerLord.gold + heroGold + baseGold + empire.goldGain
            val mercMaintenance = MercenaryEngine.maintain(s.mercenaries, goldBeforeMerc)
            val mercNote = mercMaintenance.message?.let { "\n$it" } ?: ""
            val empireNote = if (empire.lines.isEmpty()) "" else "\n" + empire.lines.joinToString("\n")
            val capPopulation = empire.population.coerceAtLeast(s.population)
            val cap = ManpowerEngine.capOf(capPopulation)
            val levies = empire.troopGain.coerceAtMost((cap - s.troops).coerceAtLeast(0))
            val newTroops = (playerLord.troops + levies).coerceAtMost(cap)
            val foodBeforeAi = (playerLord.grain + (grainGain - result.grainChange) + heroGrain + baseFood + empire.foodGain)
                .coerceAtLeast(0)
            val goldBeforeAi = (goldBeforeMerc - mercMaintenance.paid).coerceAtLeast(0)

            // ── AI 리더 능동 활동 — 성장 · 병합/종속 · 우리 영지 견제 ──
            val aiArea = s.regionArea ?: RegionCatalog.defaultRegion()
            val aiPlayer = AiLordProfile(
                id = "lord_player",
                name = playerName,
                regionCode = aiArea.code,
                regionName = aiArea.name,
                stronghold = s.playerLord?.stronghold?.ifBlank { aiArea.name } ?: aiArea.name,
                trait = s.playerLord?.trait ?: LordTrait.BALANCED,
                troops = newTroops,
                grain = foodBeforeAi,
                gold = goldBeforeAi,
                prestige = s.prestige,
                arm = s.playerLord?.arm ?: CombatArm.INFANTRY
            )
            val aiOrder = AiSeasonOrder(
                season = season,
                turn = s.seasonTurn + 1,
                // 이번 절기에 들어설 때의 연합 상태로 판단한다(보호가 막 끝난 절기에는 습격하지 않는다)
                unionActive = s.union.active,
                regionName = s.regionName
            )
            val aiOutcome = AiLordTurnEngine.runSeason(
                // 우리 연방에 편입된 세력은 영토 탭에서 운영하므로 AI 턴에서 뺀다
                lords = s.lords.filterNot { it.isPlayer || it.incorporated }.map { it.toProfile(aiArea) },
                player = aiPlayer,
                order = aiOrder
            )
            val aiLords = aiOutcome.lords.map { it.toView() }
            val nextLords = s.lords.map { lord ->
                if (lord.isPlayer || lord.incorporated) lord
                else aiLords.firstOrNull { it.id == lord.id } ?: lord
            }
            // AI 성장·병합을 세력 목록에도 맞춰 둔다 — 영토 탭·세부맵의 숫자가 어긋나지 않게
            val nextFactions = s.factions.map { faction ->
                val lord = aiLords.firstOrNull { "faction_${it.id}" == faction.id } ?: return@map faction
                if (faction.incorporated) faction
                else faction.copy(
                    troops = lord.troops,
                    gold = lord.gold,
                    food = lord.grain,
                    prestige = lord.prestige
                )
            }
            val aiNote = if (aiOutcome.hasNews) "\n" + aiOutcome.lines.joinToString("\n") else ""
            val newTroopsAfterAi = (newTroops - aiOutcome.troopLoss).coerceAtLeast(0)
            val goldAfterAi = (goldBeforeAi - aiOutcome.goldLoss).coerceAtLeast(0)
            val foodAfterAi = (foodBeforeAi - aiOutcome.foodLoss).coerceAtLeast(0)
            val message = "[절기 순환: ${season.displayName}] ${result.message}" + heroNote + baseNote +
                empireNote + mercNote
            // ── 긴급 의회 · 상호 방어 · 방어 태세 ──────────────────────
            // 타 지역 세력이 우리 외곽을 습격하면 긴급 의회가 열린다(이미 열려 있으면 갱신).
            val raid = aiOutcome.raids.firstOrNull()
            val emergencyNext: EmergencyCouncil? = when {
                raid != null -> EmergencyCouncilEngine.call(
                    invaderName = raid.lordName,
                    regionName = s.regionName,
                    troopsLost = raid.troopLoss,
                    turn = aiOrder.turn
                )
                else -> s.emergency?.let { EmergencyCouncilEngine.decay(it) }
            }
            // 다른 리더가 공격받았다 — 지원군을 보낼 기회를 남긴다
            val expansion = aiOutcome.expansion
            val aidNext: AidCall? = when {
                expansion == null -> s.aidCall
                else -> AidCall(
                    attackerName = expansion.leaderName,
                    victimName = expansion.targetName,
                    victimId = expansion.targetId,
                    kind = expansion.kind,
                    absorbedTroops = expansion.absorbedTroops
                )
            }
            val defenseTurns = (s.defensePostureTurns - 1).coerceAtLeast(0)
            // 처분하지 않은 편입 세력 소속 영웅은 다음 절기로 넘어가며 자동으로 재야가 된다(방치 페널티 없음)
            val (releasedRoster, releasedCount) = HeroDispositionEngine.autoRelease(s.heroes, s.pendingDisposition)
            val pendingNote = if (releasedCount > 0) {
                "\n[처분] 절기가 바뀌어 처분 대기 영웅 ${releasedCount}명을 재야로 돌려보냈습니다."
            } else ""
            season = nextSeason

            // 다음 절기에 찾아올 영웅 — 명성·민심·절기 조건을 만족하면 방문 이벤트가 열린다
            val visitSeed = (s.union.remainingTurns * 31L + nextSeason.ordinal * 7L + s.territories.size * 13L)
            val visit = if (s.heroVisit == null) {
                HeroDiscoveryEngine.visitCandidate(
                    roster = s.heroes,
                    prestige = s.prestige,
                    morale = s.morale,
                    season = nextSeason.displayName,
                    seed = visitSeed,
                    regionName = s.regionName
                )
            } else s.heroVisit

            return s.copy(
                seasonName = nextSeason.displayName,
                gold = goldAfterAi,
                food = foodAfterAi,
                troops = newTroopsAfterAi,
                territories = empire.territories,
                mercenaries = mercMaintenance.bands,
                lords = nextLords,
                factions = nextFactions,
                aiNews = aiOutcome.lines,
                seasonTurn = aiOrder.turn,
                heroVisit = visit,
                heroes = releasedRoster,
                pendingDisposition = if (releasedCount > 0) emptyList() else s.pendingDisposition,
                emergency = emergencyNext,
                aidCall = aidNext,
                defensePostureTurns = defenseTurns,
                coalition = CoalitionCouncilEngine.prune(s.coalition),
                boostUses = emptyMap(),
                union = unionNext,
                command = gained,
                unlockedHop = gained.unlockedHop,
                mapNodes = if (gained.level != before.level) {
                    TerritoryMapLayout.build(
                        start = s.regionArea ?: RegionCatalog.defaultRegion(),
                        unlockedHop = gained.unlockedHop,
                        playerName = playerName,
                        homeTroops = newTroopsAfterAi
                    )
                } else s.mapNodes,
                levelUpMessage = if (gained.level != before.level) levelUpText(gained) else s.levelUpMessage,
                log = (s.log + message + pendingNote + listOfNotNull(unionMessage) + aiOutcome.lines).takeLast(8),
                statusMessage = message + pendingNote + aiNote
            )
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
        persist()
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
        persist()
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
            val discount = s.heroBonus.researchDiscountPercent.coerceIn(0, 60)
            val cost = (tech.cost * (100 - discount) / 100).coerceAtLeast(1)
            if (s.gold < cost) {
                return@update s.copy(statusMessage = "[연구 불가] 자금이 부족합니다. (필요 ${cost}G / 보유 ${s.gold}G)")
            }
            val message = "[연구 완료] ${tech.branch} ${tech.stage}단계 '${tech.name}' — ${tech.effect}" +
                if (discount > 0) " (기술 영웅 할인 -$discount% · ${cost}G)" else ""
            s.copy(
                gold = s.gold - cost,
                researchedTechs = s.researchedTechs + techId,
                morale = if (tech.branch == "문화") (s.morale + 2).coerceAtMost(100) else s.morale,
                diplomacy = if (tech.branch == "문화") (s.diplomacy + 2).coerceAtMost(100) else s.diplomacy,
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
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
        if (validation.canBuild) persist()
        return validation
    }

    /** 리더 의회 안건 표결 */
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
            // 내가 직할·편입한 리더 수만큼 의결권을 행사한다 (2026-11-25 요청)
            val result = councilEngine.voteOnBill(bill, lords, s.councilVotingPower)

            val passed = result.isPassed
            val rebellion = result.isRebellionTriggered
            val message = buildString {
                append("[리더 의회 표결] '${state.title}' — 찬성 ${result.votesInFavor} : 반대 ${result.votesAgainst} → ")
                append(if (passed) "가결" else "부결")
                append("\n${s.councilVotingLabel}으로 표를 행사했습니다.")
                if (rebellion) append("\n반대 65% 이상! 반대 리더들이 연합해 대표 리더에 불복합니다.")
            }

            var gold = s.gold
            var troops = s.troops
            var morale = s.morale
            var prestige = s.prestige
            if (passed) {
                when (state.type) {
                    BillType.TROOP_LEVY -> troops += state.levy
                    BillType.SPECIAL_TAX -> gold += 700
                    BillType.JOINT_WAR -> troops += state.levy / 2
                    BillType.JOINT_DIPLOMACY -> Unit
                    BillType.JOINT_STRATEGY -> Unit
                    BillType.EMERGENCY_DEFENSE -> Unit
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
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 인접 지역 전투·외교·전략 — 지도 하단 패널 (2026-11-25)
    // ──────────────────────────────────────────────────────────────

    /** 지도에서 고른 인접 지역의 전투·외교·전략 시트를 연다 */
    fun openAdjacentAction(kind: AdjacentActionKind) {
        _uiState.update { s ->
            val node = s.selectedTerritory
                ?: return@update s.copy(statusMessage = "[지도] 먼저 지역을 선택하세요.")
            if (node.code == s.regionCode) {
                return@update s.copy(statusMessage = "[지도] 내 지역입니다 — 영지·작전 탭에서 운영하세요.")
            }
            if (node.isLocked) {
                return@update s.copy(statusMessage = "[미개방] '${node.name}'은(는) 아직 열리지 않은 구역입니다.")
            }
            s.copy(adjacentSheet = AdjacentSheet(kind, node.code, node.name), adjacentMessage = null)
        }
    }

    fun closeAdjacentAction() {
        _uiState.update { it.copy(adjacentSheet = null) }
    }

    fun dismissAdjacentMessage() {
        _uiState.update { it.copy(adjacentMessage = null) }
    }

    /**
     * 인접 지역 리더와 교전한다.
     *
     * 통합 리더가 없는 지역은 개별 리더 한 명씩,
     * 통합 리더가 있는 지역은 그 지배자를 지역 대표로 상대한다.
     */
    fun challengeAdjacentLeader(leaderId: String) {
        _uiState.update { s ->
            val sheet = s.adjacentSheet
                ?: return@update s.copy(statusMessage = "[전투] 먼저 지도에서 인접 지역을 고르세요.")
            val board = s.regionBoardFor(sheet.code)
            val info = board.leaderOf(leaderId)
                ?: return@update s.copy(statusMessage = "[전투] 그 지역 명부에 없는 리더입니다.")
            if (info.status.isBound) {
                return@update s.copy(
                    statusMessage = "[전투] ${info.name} 리더은(는) ${info.masterName ?: "우두머리"} 리더에게 " +
                        "${info.status.label}된 세력입니다 — 우두머리를 상대하세요."
                )
            }
            val verdict = AdjacentRegionEngine.expeditionVerdict(s.union)
            if (!verdict.allowed) {
                return@update s.copy(statusMessage = verdict.label)
            }
            val area = RegionCatalog.area(sheet.code) ?: (s.regionArea ?: RegionCatalog.defaultRegion())
            val own = ForceComposition.standard(
                (s.totalTroops * (1.0 + s.heroBonus.combatPercent / 100.0)).toInt()
            )
            val enemy = ForceComposition.standard(info.troops.coerceAtLeast(60))
            val enemyStrategy = StrategyBattleEngine.enemyStrategyFor(area.terrain, info.name.hashCode())
            val opsBonus = (
                s.heroBonus.combatPercent + s.coalition.battleBonusPercent + s.defensePostureBonusPercent
                ).coerceAtMost(70)
            val strat = StrategyBattleEngine.resolve(
                own = own,
                enemy = enemy,
                terrain = area.terrain,
                ownStrategy = s.battleStrategy,
                enemyStrategy = enemyStrategy,
                ownTech = s.weaponTech,
                enemyTechLevel = StrategyBattleEngine.enemyTechLevel(s.unlockedHop, info.name.hashCode()),
                ownOpsBonusPercent = opsBonus,
                defending = false
            )
            val win = strat.ownWon
            val ourLosses = strat.ownLosses
            val theirLosses = strat.enemyLosses
            val loot = if (win) 60 + info.gold / 10 else 0
            val message = "[인접 원정] ${sheet.name} · ${info.name} 리더 — ${if (win) "압박 성공" else "공격 실패"} · " +
                "적 사상자 ${theirLosses}, 아군 사상자 $ourLosses (교환비 ${strat.exchangeLabel})" +
                if (s.coalition.battleBonusPercent > 0) " · 연합 공동 전투 +${s.coalition.battleBonusPercent}%" else ""
            val report = OperationReport(
                operation = "인접 지역 원정",
                defenderWon = !win,
                attackerLosses = theirLosses,
                defenderLosses = ourLosses,
                message = message,
                verdict = if (win) "${info.name} 거점 압박" else "원정 실패 — 재편성 필요",
                targetName = info.name,
                composition = "아군 ${own.describe()} / 적 ${enemy.describe()}",
                strategyLabel = "${s.battleStrategy.icon} ${s.battleStrategy.label} vs ${enemyStrategy.icon} ${enemyStrategy.label}",
                exchangeLabel = strat.exchangeLabel,
                breakdown = strat.lines,
                ownLossPercent = (strat.ownLossRatio * 100).toInt(),
                enemyLossPercent = (strat.enemyLossRatio * 100).toInt()
            )
            val (regularLeft, mercsLeft) = applyLosses(s.troops, s.mercenaries, ourLosses)
            s.copy(
                troops = regularLeft,
                mercenaries = mercsLeft,
                adjacentDamage = s.adjacentDamage + (
                    info.id to ((s.adjacentDamage[info.id] ?: 0) + theirLosses)
                    ),
                gold = (s.gold + loot).coerceAtLeast(0),
                prestige = (s.prestige + (if (win) 4 else -3)).coerceIn(0, 100),
                morale = (s.morale + (if (win) 2 else -3)).coerceIn(0, 100),
                coalition = s.coalition.consumeBattleBonus(),
                lastReport = report,
                adjacentMessage = message,
                adjacentSheet = sheet.copy(result = null),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    /** 인접 지역과 조약을 맺는다 — 통합 리더가 없으면 리더 연합 전체와 협상한다 */
    fun proposeAdjacentDiplomacy(type: TreatyType) {
        _uiState.update { s ->
            val sheet = s.adjacentSheet
                ?: return@update s.copy(statusMessage = "[외교] 먼저 지도에서 인접 지역을 고르세요.")
            val node = TerritoryMapLayout.node(s.mapNodes, sheet.code)
            val panel = AdjacentRegionEngine.panel(
                code = sheet.code,
                name = sheet.name,
                relationLabel = node?.relationLabel ?: "경계 세력",
                board = s.regionBoardFor(sheet.code),
                unionActive = s.union.active
            )
            val cost = AdjacentDiplomacyEngine.costOf(type)
            if (s.gold < cost) {
                return@update s.copy(adjacentMessage = "[외교] 자금이 부족합니다. (필요 ${cost}G / 보유 ${s.gold}G)")
            }
            val base = AdjacentDiplomacyEngine.resolve(
                type = type,
                partnerName = panel.diplomacyPartner,
                unified = panel.unified,
                diplomacy = (s.diplomacy + s.coalition.diplomacyBonus).coerceAtMost(100),
                prestige = s.prestige,
                leaderCount = panel.leaders.size,
                seed = s.seasonTurn * 131L + sheet.code.hashCode()
            )
            val res = base
            val view = if (res.success) TreatyView(
                id = "treaty_${type.name}_${sheet.code}",
                typeLabel = AdjacentDiplomacyEngine.typeLabel(type),
                partner = panel.diplomacyPartner,
                remainingTurns = AdjacentDiplomacyEngine.durationOf(type),
                benefit = AdjacentDiplomacyEngine.benefitOf(type)
            ) else null
            val message = res.oneLine()
            s.copy(
                gold = (s.gold + res.goldDelta).coerceAtLeast(0),
                diplomacy = (s.diplomacy + res.diplomacyDelta).coerceIn(0, 100),
                prestige = (s.prestige + res.prestigeDelta).coerceIn(0, 100),
                treaties = if (view != null) s.treaties + view else s.treaties,
                adjacentMessage = message,
                adjacentSheet = sheet.copy(result = res),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            ).let { next -> next.copy(alliedTerritories = alliedTerritoryCount(next)) }
        }
        persist()
    }

    /** 인접 지역에 전략 공작을 편다 — 보급로 차단·여론전·정찰 */
    fun runAdjacentStrategy(opId: String) {
        val op = AdjacentStrategyOp.byId(opId) ?: return
        _uiState.update { s ->
            val sheet = s.adjacentSheet
                ?: return@update s.copy(statusMessage = "[전략] 먼저 지도에서 인접 지역을 고르세요.")
            val discount = s.coalition.strategyDiscountPercent
            val cost = (op.cost * (100 - discount) / 100).coerceAtLeast(1)
            if (s.gold < cost) {
                return@update s.copy(adjacentMessage = "[전략] 자금이 부족합니다. (필요 ${cost}G / 보유 ${s.gold}G)")
            }
            val base = AdjacentStrategyEngine.resolve(
                op = op,
                regionName = sheet.name,
                espionagePower = s.espionagePower,
                heroIntelBonus = s.heroBonus.intelBonus,
                prestige = s.prestige,
                seed = s.seasonTurn * 97L + op.id.hashCode()
            )
            val res = base.copy(goldDelta = -cost)
            val message = res.oneLine() +
                if (discount > 0) " (연합 공동 전략 -${discount}% · ${cost}G)" else ""
            s.copy(
                gold = (s.gold + res.goldDelta).coerceAtLeast(0),
                food = (s.food + res.foodDelta).coerceAtLeast(0),
                enemyFood = (s.enemyFood + res.enemyFoodDelta).coerceAtLeast(0),
                prestige = (s.prestige + res.prestigeDelta).coerceIn(0, 100),
                morale = (s.morale + res.moraleDelta).coerceIn(0, 100),
                diplomacy = (s.diplomacy + res.diplomacyDelta).coerceIn(0, 100),
                espionagePower = (s.espionagePower + res.espionagePowerDelta).coerceIn(0, 100),
                intelGained = s.intelGained || res.intelGained,
                coalition = s.coalition.consumeStrategyDiscount(),
                adjacentMessage = message,
                adjacentSheet = sheet.copy(result = res),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 지역 리더 연합 의회 · 긴급 의회 · 상호 방어
    // ──────────────────────────────────────────────────────────────

    /** 선택한 인접 지역에 대한 공동 전투·외교·전략 안건을 의회에 올린다 */
    fun sponsorCoalitionBill(kind: AdjacentActionKind) {
        _uiState.update { s ->
            val sheet = s.adjacentSheet
            val node = sheet?.let { TerritoryMapLayout.node(s.mapNodes, it.code) } ?: s.selectedTerritory
            if (node == null || node.code == s.regionCode) {
                return@update s.copy(statusMessage = "[의회] 인접 지역을 먼저 고르세요.")
            }
            if (s.coalition.pendingOf(kind).any { it.regionCode == node.code }) {
                return@update s.copy(
                    statusMessage = "[의회] 같은 지역·같은 종류의 안건이 이미 표결 대기 중입니다."
                )
            }
            val supporters = (if (s.union.active) s.aiLordCount else s.treaties.size).coerceAtMost(12)
            val bill = CoalitionCouncilEngine.sponsor(
                kind = kind,
                regionCode = node.code,
                regionName = node.name,
                seasonTurn = s.seasonTurn,
                supporters = supporters,
                sequence = s.coalition.bills.size
            )
            val message = "[의회 상정] ${bill.title} — 참여 리더 ${supporters}명 · " +
                "${s.councilVotingLabel}. 영지 › 의회 › 연합에서 표결하세요."
            s.copy(
                coalition = s.coalition.copy(
                    bills = s.coalition.bills + bill,
                    supporters = supporters
                ),
                adjacentMessage = message,
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    /** 연합 안건 표결 — 내 의결권(편입 리더 수)만큼 표를 행사한다 */
    fun voteCoalitionBill(billId: String) {
        _uiState.update { s ->
            val bill = s.coalition.bills.firstOrNull { it.id == billId }
                ?: return@update s.copy(statusMessage = "[의회] 안건을 찾을 수 없습니다.")
            if (!bill.pending) {
                return@update s.copy(statusMessage = "[의회] 이미 표결이 끝난 안건입니다.")
            }
            val opponents = (bill.favor / 3 + 1)
            val voted = CoalitionCouncilEngine.vote(bill, opponents, s.councilVotingPower)
            val next = CoalitionCouncilEngine.applyPassed(
                s.coalition.copy(
                    bills = s.coalition.bills.map { if (it.id == billId) voted else it }
                ),
                voted
            )
            val bonusNote = if (voted.passed == true) when (voted.kind) {
                AdjacentActionKind.WAR -> "\n공동 전투 보너스 +${next.battleBonusPercent}% — 다음 인접 교전에 적용"
                AdjacentActionKind.DIPLOMACY -> "\n공동 외교 +${next.diplomacyBonus} — 다음 혈상에 적용"
                AdjacentActionKind.STRATEGY -> "\n공동 전략 비용 -${next.strategyDiscountPercent}% — 다음 공작에 적용"
            } else ""
            val message = (voted.message ?: "") + bonusNote
            s.copy(
                coalition = next,
                morale = (s.morale + if (voted.passed == true) 2 else 0).coerceAtMost(100),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    /** 긴급 방어 결의 표결 */
    fun voteEmergencyBill() {
        _uiState.update { s ->
            val council = s.emergency
                ?: return@update s.copy(statusMessage = "[긴급 의회] 지금은 열린 긴급 의회가 없습니다.")
            if (council.resolved) {
                return@update s.copy(statusMessage = "[긴급 의회] 이미 표결이 끝났습니다.")
            }
            val opponents = (s.aiLordCount / 4).coerceAtLeast(0)
            val voted = EmergencyCouncilEngine.vote(council, opponents, s.councilVotingPower)
            val message = voted.message ?: ""
            s.copy(
                emergency = voted,
                defensePostureTurns = if (voted.passed == true) 2 else 0,
                morale = (s.morale + if (voted.passed == true) 4 else -2).coerceIn(0, 100),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    /** 인근 우호 리더들에게 방어 도움을 요청한다 */
    fun requestDefenseAid() {
        _uiState.update { s ->
            val council = s.emergency
                ?: return@update s.copy(statusMessage = "[방어 지원] 지금은 지원을 요청할 침공이 없습니다.")
            if (council.aids.any { it.direction == AidDirection.REQUESTED }) {
                return@update s.copy(statusMessage = "[방어 지원] 이미 지원을 요청했습니다.")
            }
            val sources = s.lords.filterNot { it.isPlayer }.map { lord ->
                AidSource(
                    leaderId = lord.id,
                    leaderName = lord.name,
                    troops = lord.troops,
                    friendly = s.union.active || s.treaties.isNotEmpty()
                )
            }
            val next = EmergencyCouncilEngine.requestAid(council, sources, s.diplomacy, s.prestige)
            val gained = (next.requestedTroops - council.requestedTroops).coerceAtLeast(0)
            val message = next.message ?: ""
            s.copy(
                emergency = next,
                troops = s.troops + gained,
                aidLog = s.aidLog + next.aids.filter { it.direction == AidDirection.REQUESTED },
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    /** 다른 리더가 공격받을 때 지원군을 보낸다 — 병합·종속을 막아낸다 */
    fun answerAidCall() {
        _uiState.update { s ->
            val call = s.aidCall
                ?: return@update s.copy(statusMessage = "[방어 지원] 지금은 지원을 요청받은 리더가 없습니다.")
            if (call.answered) {
                return@update s.copy(statusMessage = "[방어 지원] 이미 응답한 요청입니다.")
            }
            val (answered, sent) = EmergencyCouncilEngine.answerCall(call, s.totalTroops)
            val (regularLeft, mercsLeft) = applyLosses(s.troops, s.mercenaries, sent)
            val message = answered.result ?: ""
            s.copy(
                troops = regularLeft,
                mercenaries = mercsLeft,
                prestige = (s.prestige + 5).coerceIn(0, 100),
                diplomacy = (s.diplomacy + 6).coerceIn(0, 100),
                lords = s.lords.map { lord ->
                    if (lord.id != call.victimId) lord
                    else lord.copy(
                        masterId = null,
                        bond = null,
                        troops = (lord.troops + call.absorbedTroops / 2).coerceAtLeast(1),
                        activity = "우리 지원군이 방어 — 독립 유지"
                    )
                },
                aidCall = answered,
                aidLog = s.aidLog + DefenseAid(
                    leaderName = call.victimName,
                    troops = sent,
                    direction = AidDirection.SENT,
                    note = "${call.attackerName}의 ${call.kind.label} 차단"
                ),
                log = (s.log + message).takeLast(8),
                statusMessage = message
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 지속 시간 흐름 · 미접속 AI 대행 (2026-11-25)
    // ──────────────────────────────────────────────────────────────

    /** 절기 한 칸에 걸리는 시간을 바꾼다 (10·30·60·180분) */
    fun setSeasonSpeed(minutes: Int) {
        _uiState.update {
            it.copy(
                clock = it.clock.copy(
                    seasonMinutes = minutes.coerceIn(1, 1440),
                    lastTickMillis = System.currentTimeMillis()
                )
            )
        }
        persist()
    }

    /** 자동 절기 진행 켜기/끄기 — 기존 수동 '절기' 버튼과 함께 쓴다 */
    fun setAutoAdvance(on: Boolean) {
        _uiState.update {
            it.copy(
                clock = it.clock.copy(
                    autoAdvance = on,
                    lastTickMillis = System.currentTimeMillis()
                ),
                statusMessage = if (on) "[시간] 자동 절기 진행을 켰습니다." else "[시간] 자동 진행을 멈췄습니다 — '절기' 버튼으로 진행하세요."
            )
        }
        persist()
    }

    /** 미접속 AI 대행 켜기/끄기 */
    fun setAiAutopilot(on: Boolean) {
        _uiState.update {
            it.copy(
                clock = it.clock.copy(aiAutopilot = on, lastTickMillis = System.currentTimeMillis()),
                statusMessage = if (on) "[AI 대행] 미접속 동안 AI가 내정·방어를 이어갑니다."
                else "[AI 대행] 미접속 동안에는 절기를 진행하지 않습니다."
            )
        }
        persist()
    }

    fun dismissOfflineReport() {
        _uiState.update { it.copy(offlineReport = null) }
    }

    /**
     * 화면 시계가 15초마다 부른다 — 흐른 시간만큼 절기를 자동으로 진행한다.
     *
     * · 지난 시간이 절기 한 칸을 넘으면 그만큼 [nextSeasonState] 를 돌린다(수동 버튼과 같은 엔진).
     * · 5분 이상 비었으면 '미접속'으로 보고 AI 대행 리포트를 남긴다.
     */
    fun tickRealTime(nowMillis: Long = System.currentTimeMillis()) {
        val before = _uiState.value
        if (before.stage != GameStage.PLAY || before.playerLordId == null) return
        if (before.clock.lastTickMillis <= 0L) {
            _uiState.update { it.copy(clock = it.clock.copy(lastTickMillis = nowMillis)) }
            return
        }
        val tick = SeasonClockEngine.tick(before.clock, nowMillis, before.clock.autoAdvance)
        // 미접속 AI 대행이 꺼져 있으면 비어 있는 시간은 건너뛴다(권리를 침해하지 않는 선)
        val applyDue = if (tick.offline && !before.clock.aiAutopilot) 0 else tick.due
        if (applyDue <= 0) {
            if (tick.clock != before.clock) {
                val note = if (tick.offline && !before.clock.aiAutopilot) {
                    "[AI 대행] 미접속 ${SeasonClockEngine.awayLabel(tick.awayMillis)} 동안 자동 진행이 꺼져 있어 절기가 흐르지 않았습니다."
                } else null
                _uiState.update {
                    it.copy(
                        clock = tick.clock,
                        statusMessage = note ?: it.statusMessage
                    )
                }
            }
            return
        }
        val goldBefore = before.gold
        val foodBefore = before.food
        val troopsBefore = before.troops
        var state = before
        var lastNews: List<String> = emptyList()
        var emergency: EmergencyCouncil? = before.emergency
        repeat(applyDue) {
            state = nextSeasonState(state, dikeMaintenanceDone = true)
            if (state.aiNews.isNotEmpty()) lastNews = state.aiNews
            if (state.emergency != null) emergency = state.emergency
        }
        val report = if (tick.offline && before.clock.aiAutopilot) {
            SeasonClockEngine.offlineSummary(
                seasons = applyDue,
                awayMillis = tick.awayMillis,
                goldBefore = goldBefore,
                goldAfter = state.gold,
                foodBefore = foodBefore,
                foodAfter = state.food,
                troopsBefore = troopsBefore,
                troopsAfter = state.troops,
                aiNews = lastNews,
                emergency = emergency
            )
        } else state.offlineReport
        _uiState.value = state.copy(
            clock = tick.clock,
            offlineReport = report,
            statusMessage = "[시간] 절기 " + ("%d".format(applyDue)) + "회가 자동으로 흘렀습니다 — " +
                state.seasonName.substringBefore(" (")
        )
        persist()
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

            // 스폰 보호막 — 보호 기간에는 외부 침공이 지역 연합 방어선에서 자동으로 막힌다.
            if (operationId == "OP_BASE" && s.spawnProtected) {
                val shieldMessage = RegionUnionEngine.shieldRepelMessage(s.union)
                val before = s.command
                val gained = before.gainXp(CommandProgress.XP_OPERATION_WIN)
                val report = OperationReport(
                    operation = "통합 방호기지 방어",
                    defenderWon = true,
                    attackerLosses = 0,
                    defenderLosses = 0,
                    message = shieldMessage,
                    verdict = "🛡️ 스폰 보호막 — 사상자 0",
                    targetName = s.regionName,
                    composition = "지역 연합 방어선 / 외부 침공 저지",
                    requiredLevel = 1
                )
                return@update s.copy(
                    prestige = (s.prestige + 1).coerceIn(0, 100),
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
                    log = (s.log + shieldMessage).takeLast(8),
                    statusMessage = shieldMessage
                )
            }

            val target = when (operationId) {
                "OP_DRONE", "OP_BRIDGE" -> s.mapNodes.firstOrNull { it.hopDistance == 1 && it.unlocked }
                else -> s.homeNode
            } ?: s.homeNode
            val targetTerrain = target?.terrain ?: TerrainType.PLAIN
            val homeTerrain = s.homeNode?.terrain ?: TerrainType.PLAIN

            // 정규 + 용병을 함께 편성한다 (용병은 정규 상한과 별도)
            val own = facilityEngine.reinforced(
                ForceComposition.standard(
                    (s.totalTroops * (1.0 + s.heroBonus.combatPercent / 100.0)).toInt()
                ),
                s.facilities
            )
            val enemyMultiplier = when (operationId) {
                "OP_DRONE" -> 0.95
                "OP_BRIDGE" -> 1.05
                else -> 1.55
            }
            val enemyTroops = (s.totalTroops * enemyMultiplier).toInt().coerceAtLeast(60)
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

            // 전략 · 무기 기술이 손실률을 크게 가른다.
            // 방어전은 진지 사수이므로 방어 진형을 쓰고, 공격전은 고른 전략을 쓴다.
            val ownStrategy = if (weDefend) BattleStrategy.DEFENSE else s.battleStrategy
            val enemySeed = (s.unlockedHop * 7 + operationId.length + s.territories.size)
            val enemyStrategy = StrategyBattleEngine.enemyStrategyFor(battleTerrain, enemySeed)
            val strat = StrategyBattleEngine.resolve(
                own = own,
                enemy = enemy,
                terrain = battleTerrain,
                ownStrategy = ownStrategy,
                enemyStrategy = enemyStrategy,
                ownTech = s.weaponTech,
                enemyTechLevel = StrategyBattleEngine.enemyTechLevel(s.unlockedHop, enemySeed),
                ownOpsBonusPercent = (s.heroBonus.combatPercent + s.coalitionBonusPercent).coerceAtMost(60),
                defenseBonusRatio = if (weDefend) facilityEngine.defenseBonus(s.facilities) else 0.0,
                casualtyReduction = if (weDefend) facilityEngine.casualtyReduction(s.facilities) else 0.0,
                fortressReduction = if (weDefend) (fortress?.casualtyReductionRatio ?: 0.0) else 0.0,
                durabilityFactor = if (weDefend) 1.0 else 0.0,
                defending = weDefend
            )
            // 보급선을 끊었다면 방어전에서 적이 무혈 항복한다
            val supplyRout = s.intelGained && weDefend
            val enemyLosses = if (supplyRout) (enemy.totalTroops * 0.4).toInt() else strat.enemyLosses
            val ourLosses = if (supplyRout) 0 else strat.ownLosses
            val ourWin = if (supplyRout) true else strat.ownWon

            val lossRatio = if (s.totalTroops == 0) 0.0 else ourLosses.toDouble() / s.totalTroops
            val verdict = when {
                supplyRout -> "보급선 차단 — 무혈 항복, 사상자 0"
                lossRatio <= 0.05 -> "압도적 우세 — 아군 손실 ${(lossRatio * 100).toInt()}%"
                lossRatio <= 0.12 -> "저손실 현대전 성공 — 사상자 ${(lossRatio * 100).toInt()}%"
                lossRatio <= 0.22 -> "감내 가능 손실 — 사상자 ${(lossRatio * 100).toInt()}%"
                else -> "치명적 손실 — 사상자 ${(lossRatio * 100).toInt()}%. 재편성 필요"
            }
            val report = OperationReport(
                operation = operationName,
                defenderWon = ourWin,
                attackerLosses = enemyLosses,
                defenderLosses = ourLosses,
                message = if (supplyRout) {
                    "물류·보급선을 끊어 적 공격군 ${comma(enemyLosses)}명이 무혈 항복했습니다. 아군 사상자 0."
                } else strat.message,
                verdict = verdict,
                targetName = if (weDefend) s.regionName else (target?.name ?: s.regionName),
                composition = "아군 ${own.describe()} / 적 ${enemy.describe()}",
                requiredLevel = requiredHop + 1,
                strategyLabel = "${ownStrategy.icon} ${ownStrategy.label} vs ${enemyStrategy.icon} ${enemyStrategy.label}",
                exchangeLabel = strat.exchangeLabel,
                breakdown = strat.lines,
                ownLossPercent = (lossRatio * 100).toInt(),
                enemyLossPercent = if (enemy.totalTroops == 0) 0
                    else (enemyLosses * 100 / enemy.totalTroops)
            )
            val strategyMemo = strat.lines
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
            val (regularLeft, mercsLeft) = applyLosses(s.troops, s.mercenaries, ourLosses)
            s.copy(
                troops = (regularLeft + heal).coerceAtLeast(0),
                mercenaries = mercsLeft,
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
                lastStrategy = strat,
                log = (s.log + line + strategyMemo).takeLast(8),
                statusMessage = line
            )
        }
        persist()
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
        persist()
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
                espionagePower = (s.espionagePower + intelBonus + s.heroBonus.intelBonus).coerceIn(0, 100),
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
    // 영웅(인재) — 탐색 · 설득 · 고용 · 슬롯 배치
    // ──────────────────────────────────────────────────────────────

    /** 탐색 — 미보유 영웅의 접점을 확보한다 (200G) */
    fun scoutHero(heroId: String) {
        val s = _uiState.value
        applyHeroResult(HeroEngine.scout(s.heroes, heroId, s.gold))
    }

    /** 설득 — 명성 + 외교 영웅의 설득력으로 마음을 연다 */
    fun persuadeHero(heroId: String) {
        val s = _uiState.value
        applyHeroResult(HeroEngine.persuade(s.heroes, heroId, s.prestige, s.heroBonus.diplomacyBonus))
    }

    /** 고용 — 자금을 지급하고 보유 영웅으로 만든다 */
    fun hireHero(heroId: String) {
        val s = _uiState.value
        applyHeroResult(HeroEngine.hire(s.heroes, heroId, s.gold))
    }

    /** 슬롯 배치 */
    fun assignHero(heroId: String, role: HeroRole) {
        applyHeroResult(HeroEngine.assign(_uiState.value.heroes, heroId, role))
    }

    /** 슬롯에서 내리기 */
    fun unassignHero(heroId: String) {
        applyHeroResult(HeroEngine.unassign(_uiState.value.heroes, heroId))
    }

    private fun applyHeroResult(result: com.game.slg.core.hero.HeroActionResult) {
        _uiState.update { s ->
            s.copy(
                heroes = result.roster,
                gold = (s.gold + result.goldDelta).coerceAtLeast(0),
                prestige = (s.prestige + result.prestigeDelta).coerceIn(0, 100),
                heroMessage = result.message,
                statusMessage = result.message,
                log = if (result.success) (s.log + result.message).takeLast(8) else s.log
            )
        }
        persist()
    }

    /**
     * 처분 대기 영웅 등용 — 등급 기준가 20% 할인. 빈 슬롯이 있으면 그 자리에 바로 앉는다.
     */
    fun recruitPendingHero(heroId: String) {
        val s = _uiState.value
        if (s.pendingDisposition.none { it.heroId == heroId }) {
            _uiState.update { it.copy(empireMessage = "[등용] 처분 대기 목록에 없는 영웅입니다.") }
            return
        }
        val result = HeroDispositionEngine.recruit(s.heroes, heroId, s.gold)
        if (!result.success) {
            _uiState.update { it.copy(empireMessage = result.message, statusMessage = result.message) }
            return
        }
        _uiState.update { state ->
            state.copy(
                heroes = result.roster,
                gold = (state.gold + result.goldDelta).coerceAtLeast(0),
                prestige = (state.prestige + result.prestigeDelta).coerceIn(0, 100),
                pendingDisposition = state.pendingDisposition.filterNot { it.heroId == heroId },
                heroMessage = result.message,
                empireMessage = result.message,
                statusMessage = result.message,
                log = (state.log + result.message).takeLast(8)
            )
        }
        persist()
    }

    /** 처분 대기 영웅 방출 — 재야로 돌려보낸다 (호감도 -15 · 명성 -1) */
    fun releasePendingHero(heroId: String) {
        val s = _uiState.value
        if (s.pendingDisposition.none { it.heroId == heroId }) {
            _uiState.update { it.copy(empireMessage = "[방출] 처분 대기 목록에 없는 영웅입니다.") }
            return
        }
        val result = HeroDispositionEngine.release(s.heroes, heroId)
        _uiState.update { state ->
            state.copy(
                heroes = result.roster,
                prestige = (state.prestige + result.prestigeDelta).coerceIn(0, 100),
                pendingDisposition = state.pendingDisposition.filterNot { it.heroId == heroId },
                heroMessage = result.message,
                empireMessage = result.message,
                statusMessage = result.message,
                log = (state.log + result.message).takeLast(8)
            )
        }
        persist()
    }

    /** 내 영웅 내보내기 — 태수로 앉아 있으면 그 영지 연결부터 해제한다 */
    fun dismissHero(heroId: String) {
        val s = _uiState.value
        val result = HeroDispositionEngine.dismiss(s.heroes, heroId)
        if (!result.success) {
            _uiState.update { it.copy(heroMessage = result.message, statusMessage = result.message) }
            return
        }
        val clearedTerritories = s.territories.map {
            if (it.viceroyHeroId == heroId) it.copy(viceroyHeroId = null, viceroyName = null, viceroyRole = null)
            else it
        }
        val wasViceroy = clearedTerritories != s.territories
        val message = result.message + if (wasViceroy) " (태수 직도 함께 해제됐습니다.)" else ""
        _uiState.update { state ->
            state.copy(
                heroes = result.roster,
                territories = clearedTerritories,
                prestige = (state.prestige + result.prestigeDelta).coerceIn(0, 100),
                heroMessage = message,
                empireMessage = message,
                statusMessage = message,
                log = (state.log + message).takeLast(8)
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 영토 흡수 · 일임 통치 · 병력/용병
    // ──────────────────────────────────────────────────────────────

    /** 영지 선택·전환 — 통치할 편입 영지를 바꾼다 */
    fun switchManagedTerritory(code: String) {
        _uiState.update { s ->
            val target = s.territories.firstOrNull { it.code == code } ?: return@update s
            s.copy(
                selectedManagedCode = target.code,
                empireMessage = "[영지 전환] ${target.name} · ${target.status} — " +
                    "민심 ${target.morale} · 개발 ${target.development} · ⚔ ${target.troops}",
                statusMessage = "[영지 전환] ${target.name} 선택"
            )
        }
        persist()
    }

    /** 영웅을 태수로 일임 — 영웅 능력치대로 자동 운영된다 */
    fun assignViceroy(heroId: String, territoryCode: String? = null) {
        _uiState.update { s ->
            val hero = s.heroes.ownedHeroes.firstOrNull { it.id == heroId }
                ?: return@update s.copy(empireMessage = "[일임 불가] 보유하지 않은 영웅입니다.")
            val target = territoryCode?.let { code -> s.territories.firstOrNull { it.code == code } }
                ?: s.selectedManaged
                ?: return@update s.copy(empireMessage = "[일임 불가] 편입 영지가 없습니다.")
            val cleared = s.territories.map {
                if (it.viceroyHeroId == heroId) it.copy(viceroyHeroId = null, viceroyName = null, viceroyRole = null)
                else it
            }
            val updated = cleared.map {
                if (it.code == target.code) it.copy(
                    viceroyHeroId = hero.id,
                    viceroyName = hero.name,
                    viceroyRole = hero.role.label
                ) else it
            }
            val message = "[일임] ${hero.name}(${hero.role.label}) → ${target.name} 태수 — " +
                "매 절기 개발·민심·세수·징병·방어를 자동으로 운영합니다. (${hero.statLine})"
            s.copy(
                territories = updated,
                selectedManagedCode = target.code,
                empireMessage = message,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 태수 해임 */
    fun unassignViceroy(territoryCode: String? = null) {
        _uiState.update { s ->
            val target = territoryCode?.let { code -> s.territories.firstOrNull { it.code == code } }
                ?: s.selectedManaged ?: return@update s
            s.copy(
                territories = s.territories.map {
                    if (it.code == target.code) it.copy(
                        viceroyHeroId = null, viceroyName = null, viceroyRole = null
                    ) else it
                },
                empireMessage = "[일임 해제] ${target.name} — 직접 통치로 전환",
                statusMessage = "[일임 해제] ${target.name}"
            )
        }
        persist()
    }

    /** 직접 개발 — 자금을 들여 개발도를 올린다 */
    fun developTerritory(territoryCode: String? = null) {
        val cost = 350
        _uiState.update { s ->
            val target = territoryCode?.let { code -> s.territories.firstOrNull { it.code == code } }
                ?: s.selectedManaged ?: return@update s
            if (s.gold < cost) {
                return@update s.copy(empireMessage = "[개발 불가] 자금이 부족합니다. (필요 ${cost}G / 보유 ${s.gold}G)")
            }
            val message = "[개발] ${target.name} 개발도 ${target.development} → ${(target.development + 5).coerceAtMost(100)} " +
                "(자금 -${cost}G)"
            s.copy(
                gold = s.gold - cost,
                territories = s.territories.map {
                    if (it.code == target.code) it.copy(
                        development = (it.development + 5).coerceAtMost(100),
                        morale = (it.morale + 1).coerceAtMost(100)
                    ) else it
                },
                empireMessage = message,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 흡수·편입 시도 — 6가지 방법 중 하나 */
    fun attemptAbsorption(method: AbsorptionMethod, factionId: String) {
        _uiState.update { s ->
            val faction = s.factions.firstOrNull { it.id == factionId }
                ?: return@update s.copy(empireMessage = "[흡수] 알 수 없는 세력입니다.")
            val outcome = AbsorptionEngine.resolve(method, faction, s.empireContext())
            if (!outcome.success) {
                return@update s.copy(
                    empireMessage = outcome.message,
                    lastAbsorption = outcome,
                    statusMessage = outcome.message
                )
            }
            val territories = outcome.territory?.let { newTerritory ->
                if (s.territories.any { it.code == newTerritory.code }) s.territories
                else s.territories + newTerritory
            } ?: s.territories
            val (regularLeft, mercsLeft) = applyLosses(s.troops, s.mercenaries, outcome.troopLoss)
            val captured = if (outcome.capturedHeroes.isEmpty()) emptyList()
            else HeroDispositionEngine.capturesOf(faction)
            s.copy(
                gold = (s.gold + outcome.goldDelta).coerceAtLeast(0),
                food = (s.food + outcome.foodDelta).coerceAtLeast(0),
                troops = regularLeft,
                mercenaries = mercsLeft,
                prestige = (s.prestige + outcome.prestigeDelta).coerceIn(0, 100),
                morale = (s.morale + outcome.moraleDelta).coerceIn(0, 100),
                factions = s.factions.map { if (it.id == faction.id) outcome.faction else it },
                territories = territories,
                selectedManagedCode = outcome.territory?.code ?: s.selectedManagedCode,
                lastAbsorption = outcome,
                empireMessage = outcome.message,
                alliedTerritories = s.alliedTerritories + 1,
                lords = s.lords.map { lord ->
                    if (faction.id == "faction_${lord.id}") lord.copy(
                        troops = outcome.faction.troops,
                        incorporated = outcome.faction.incorporated
                    )
                    else lord
                },
                heroes = HeroDispositionEngine.markCaptured(s.heroes, captured),
                pendingDisposition = s.pendingDisposition + captured,
                log = (s.log + outcome.message).takeLast(8),
                statusMessage = outcome.message
            )
        }
        persist()
    }

    /**
     * 첩자 민심 공작 — 상대 세력의 민심을 떨어뜨려 속국 편입 조건(민심 40 이하)을 만든다.
     * 첩보력이 높을수록 잘 먹힌다.
     */
    fun instigateFaction(factionId: String) {
        val cost = 300
        _uiState.update { s ->
            val faction = s.factions.firstOrNull { it.id == factionId } ?: return@update s
            if (faction.incorporated) {
                return@update s.copy(empireMessage = "[공작] ${faction.name}은(는) 이미 편입된 세력입니다.")
            }
            if (s.gold < cost) {
                return@update s.copy(
                    empireMessage = "[공작 불가] 자금이 부족합니다. (필요 ${cost}G / 보유 ${s.gold}G)"
                )
            }
            // 성공 판정 — 첩보력 + 명성으로 상대 명성을 넘어야 한다
            val attack = s.espionagePower + s.prestige / 5 + s.heroBonus.intelBonus
            val defense = 45 + faction.prestige / 3
            val success = attack >= defense
            val drop = if (success) 14 else 4
            val nextMorale = (faction.morale - drop).coerceAtLeast(0)
            val message = if (success) {
                "[민심 공작] ${faction.name} 거점에 유언비어와 이간계가 퍼졌습니다. " +
                    "민심 ${faction.morale} → $nextMorale (첩보 $attack / 방어 $defense) — " +
                    (if (nextMorale <= 40) "이제 첩자 민심 전환으로 속국 편입이 가능합니다." else "한 번 더 공작이 필요합니다.")
            } else {
                "[민심 공작 실패] ${faction.name}이(가) 첩자를 색출했습니다. 민심 ${faction.morale} → $nextMorale " +
                    "(첩보 $attack / 방어 $defense)"
            }
            s.copy(
                gold = s.gold - cost,
                espionagePower = (s.espionagePower - 2).coerceAtLeast(0),
                factions = s.factions.map {
                    if (it.id == faction.id) it.copy(
                        morale = nextMorale,
                        affinity = (it.affinity - 4).coerceAtLeast(0),
                        relation = if (nextMorale <= 40) FactionRelation.WARY else it.relation
                    ) else it
                },
                empireMessage = message,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 공동 적 조성 — 연방 결성 조건을 만든다 (자금 250G) */
    fun createCommonEnemy(factionId: String) {
        val cost = 250
        _uiState.update { s ->
            val faction = s.factions.firstOrNull { it.id == factionId } ?: return@update s
            if (faction.commonEnemy) {
                return@update s.copy(empireMessage = "[공동 적] ${faction.name}과는 이미 같은 적을 두고 있습니다.")
            }
            if (s.gold < cost) {
                return@update s.copy(empireMessage = "[공동 적] 자금이 부족합니다. (필요 ${cost}G / 보유 ${s.gold}G)")
            }
            val message = "[공동 적] ${faction.name}과(와) 인접 구역의 공동 적을 지목했습니다. " +
                "우호도 +8 · 연방 결성 조건 충족 (자금 -${cost}G)"
            s.copy(
                gold = s.gold - cost,
                factions = s.factions.map {
                    if (it.id == faction.id) it.copy(
                        commonEnemy = true,
                        affinity = (it.affinity + 8).coerceAtMost(100)
                    ) else it
                },
                empireMessage = message,
                statusMessage = message
            )
        }
        persist()
    }

    /** 정규 병력 모집 — 전체 인구 5% 상한을 넘지 못한다 */
    fun recruitRegular(troops: Int = 300) {
        val goldCost = (troops * 5 / 3).coerceAtLeast(200)
        val foodCost = (troops * 4 / 3).coerceAtLeast(150)
        _uiState.update { s ->
            val room = (s.troopCap - s.troops).coerceAtLeast(0)
            if (room <= 0) {
                return@update s.copy(
                    empireMessage = "[모집 불가] 정규 병력 상한 ${comma(s.troopCap)}명에 이미 도달했습니다. " +
                        "용병을 쓰거나 영지를 편입해 인구를 늘리십시오.",
                    statusMessage = "[모집 불가] 정규 상한 도달"
                )
            }
            val actual = troops.coerceAtMost(room)
            if (s.gold < goldCost || s.food < foodCost) {
                return@update s.copy(empireMessage = "[모집 불가] 자금 ${comma(goldCost)}G · 식량 ${comma(foodCost)}석이 필요합니다.")
            }
            val message = "[모집] 정규병 ${comma(actual)}명 — 자금 -${comma(goldCost)}G · 식량 -${comma(foodCost)}석. " +
                ManpowerEngine.label(s.totalTerritoryPopulation, s.troops + actual, s.mercenaryTroops)
            s.copy(
                troops = s.troops + actual,
                gold = (s.gold - goldCost).coerceAtLeast(0),
                food = (s.food - foodCost).coerceAtLeast(0),
                territories = s.territories.map {
                    if (it.status == TerritoryStatus.DIRECT) it.copy(troops = it.troops + actual) else it
                },
                mapNodes = TerritoryMapLayout.build(
                    start = s.regionArea ?: RegionCatalog.defaultRegion(),
                    unlockedHop = s.unlockedHop,
                    playerName = s.playerLord?.name ?: "이광수",
                    homeTroops = s.troops + actual
                ),
                empireMessage = message,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 고비용 용병 고용 — 정규 상한과 별도 */
    fun hireMercenary(troops: Int = 300) {
        _uiState.update { s ->
            val cost = MercenaryEngine.hireCostOf(troops)
            val (band, message) = MercenaryEngine.hire(s.mercenaries, troops, s.gold, s.mercenaries.size)
            if (band == null) {
                return@update s.copy(empireMessage = message, statusMessage = message)
            }
            s.copy(
                gold = s.gold - cost,
                mercenaries = s.mercenaries + band,
                empireMessage = message,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 용병 계약 해지 */
    fun dismissMercenary(bandId: String) {
        _uiState.update { s ->
            val band = s.mercenaries.firstOrNull { it.id == bandId } ?: return@update s
            s.copy(
                mercenaries = s.mercenaries.filterNot { it.id == bandId },
                empireMessage = "[용병] ${band.name} ${comma(band.troops)}명과의 계약을 해지했습니다.",
                statusMessage = "[용병 해지] ${band.name}"
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 전략 · 무기 기술
    // ──────────────────────────────────────────────────────────────

    /** 진형·계략 선택 — 다음 작전부터 적용된다 */
    fun selectBattleStrategy(strategy: BattleStrategy) {
        _uiState.update { s ->
            s.copy(
                battleStrategy = strategy,
                strategyMessage = "[전략] ${strategy.icon} ${strategy.label} — ${strategy.detail}",
                statusMessage = "[전략] ${strategy.label} 선택"
            )
        }
    }

    /** 무기 기술 연구 — 계통별 0~4레벨 */
    fun researchWeaponTech(tech: WeaponTech) {
        _uiState.update { s ->
            val current = s.weaponTech.levelOf(tech)
            if (current >= 4) {
                return@update s.copy(strategyMessage = "[무기 연구] ${tech.label}은(는) 이미 최고 레벨입니다.")
            }
            val cost = tech.costOf(current + 1)
            if (s.gold < cost) {
                return@update s.copy(
                    strategyMessage = "[무기 연구 불가] 자금이 부족합니다. (필요 ${comma(cost)}G / 보유 ${comma(s.gold)}G)"
                )
            }
            val next = WeaponTechState(s.weaponTech.levels + (tech to current + 1))
            val message = "[무기 연구] ${tech.label} Lv.${current + 1} '${tech.levelName(current + 1)}' — " +
                "${tech.effect} · 전투력 ×${"%.2f".format(next.multiplier)} (자금 -${comma(cost)}G)"
            s.copy(
                gold = s.gold - cost,
                weaponTech = next,
                strategyMessage = message,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 영웅 탐색 · 방문 합류
    // ──────────────────────────────────────────────────────────────

    /** 영웅 탐색 — 이름을 지정하지 않고 재야 영웅을 만난다 */
    fun searchHeroes() {
        _uiState.update { s ->
            val seed = (s.command.xp + s.heroes.ownedCount * 17L + s.prestige * 7L + s.log.size)
            val result = HeroDiscoveryEngine.search(
                roster = s.heroes,
                gold = s.gold,
                regionCode = s.regionCode,
                season = s.seasonName,
                seed = seed
            )
            s.copy(
                heroes = result.roster,
                gold = (s.gold + result.goldDelta).coerceAtLeast(0),
                heroSearchMessage = result.message,
                heroMessage = result.message,
                statusMessage = result.message,
                log = if (result.success) (s.log + result.message).takeLast(8) else s.log
            )
        }
        persist()
    }

    /** 찾아온 영웅 수락 — 무상 합류 */
    fun acceptHeroVisit() {
        _uiState.update { s ->
            val visit = s.heroVisit ?: return@update s
            val result = HeroDiscoveryEngine.acceptVisit(s.heroes, visit)
            s.copy(
                heroes = result.roster,
                prestige = (s.prestige + result.prestigeDelta).coerceIn(0, 100),
                heroVisit = null,
                heroVisitMessage = result.message,
                heroMessage = result.message,
                statusMessage = result.message,
                log = (s.log + result.message).takeLast(8)
            )
        }
        persist()
    }

    /** 찾아온 영웅 거절 */
    fun declineHeroVisit() {
        _uiState.update { s ->
            val visit = s.heroVisit ?: return@update s
            val result = HeroDiscoveryEngine.declineVisit(s.heroes, visit)
            s.copy(
                heroes = result.roster,
                heroVisit = null,
                heroVisitMessage = result.message,
                statusMessage = result.message
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 영웅으로 시작 — 리더에게 등용 청
    // ──────────────────────────────────────────────────────────────

    /** 시작 방식 전환 */
    fun chooseStartMode(mode: StartMode) {
        _uiState.update { s ->
            s.copy(
                startMode = mode,
                pendingHeroPick = null,
                pendingEmployment = null,
                statusMessage = if (mode == StartMode.HERO) {
                    "[영웅 시작] 등용을 청할 영웅을 고르세요. 리더는 유저일 수도, 아닐 수도 있습니다."
                } else {
                    "[리더 시작] 인계받을 리더를 명부에서 고르세요."
                }
            )
        }
    }

    /** 영웅 스타트에서 플레이할 영웅 선택 */
    fun pickHeroForStart(heroId: String) {
        _uiState.update { s ->
            val hero = HeroRoster.byId(heroId) ?: return@update s
            s.copy(
                pendingHeroPick = hero.id,
                pendingEmployment = null,
                statusMessage = "[영웅 시작] ${hero.name} · ${hero.title} — 등용을 청할 리더를 고르세요. " +
                    "(${hero.statLine})"
            )
        }
    }

    /**
     * 리더에게 등용 청 — 수락/거절은 상대가 누구인지와 무관하게
     * 능력치·명성·세력 필요도로만 결정된다(블라인드 원칙).
     */
    fun requestEmployment(lordId: String) {
        _uiState.update { s ->
            val hero = s.pendingHeroPick?.let { HeroRoster.byId(it) }
                ?: return@update s.copy(statusMessage = "[등용] 먼저 영웅을 고르세요.")
            val lord = s.lords.firstOrNull { it.id == lordId }
                ?: return@update s.copy(statusMessage = "[등용] 명부에 없는 리더입니다.")
            val need = LordNeed(
                troops = lord.troops,
                prestige = lord.prestige,
                population = s.regionPopulation
            )
            val startPrestige = (12 + hero.stats.total / 12).coerceIn(15, 55)
            val request = HeroEmploymentEngine.request(
                hero = hero,
                prestige = startPrestige,
                lordId = lord.id,
                lordName = lord.name,
                need = need
            )
            if (!request.accepted) {
                return@update s.copy(
                    pendingEmployment = request,
                    statusMessage = request.message,
                    log = (s.log + request.message).takeLast(8)
                )
            }
            val region = s.regionArea ?: RegionCatalog.defaultRegion()
            // 영웅 스타트는 동료 없이 시작한다 — 동료는 탐색·방문 합류로만 늘어난다
            val roster = HeroRosterState().let { base ->
                base.copy(
                    records = base.records + (
                        hero.id to base.recordOf(hero.id).copy(
                            stage = HeroStage.HIRED,
                            affinity = 85,
                            assignedRole = hero.role
                        )
                        )
                )
            }
            val troops = 120
            val message = request.message + " — ${lord.name} 리더 휘하의 ${hero.role.label} 영웅으로 시작합니다."
            s.copy(
                stage = GameStage.PLAY,
                startMode = StartMode.HERO,
                pendingEmployment = request,
                playerLordId = lord.id,
                selectedLordId = lord.id,
                registeredLords = 1,
                prestige = startPrestige,
                gold = 1_500,
                food = 2_400,
                troops = troops,
                morale = 58,
                heroes = roster,
                heroMessage = "${hero.name}으로 시작합니다. 동료 영웅은 탐색과 방문 합류로만 늘릴 수 있습니다.",
                heroCareer = HeroCareer(
                    heroId = hero.id,
                    heroName = hero.name,
                    employerLordId = lord.id,
                    employerLordName = "${lord.name} 리더",
                    employerTerritory = lord.stronghold,
                    loyalty = request.loyalty,
                    merit = 0,
                    privateTroops = 40
                ),
                command = CommandProgress(),
                unlockedHop = 0,
                mapNodes = TerritoryMapLayout.build(region, 0, hero.name, troops),
                selectedTerritoryCode = region.code,
                territories = listOf(homeTerritory(region, troops, 58)),
                selectedManagedCode = region.code,
                factions = factionsOf(region, AiLordGenerator.generate(region), lord.id),
                union = RegionUnionEngine.open(s.regionCode, s.regionName),
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 영웅 경력 — 사병 모집 */
    fun gatherPrivateTroops() {
        val cost = 420
        _uiState.update { s ->
            val career = s.heroCareer ?: return@update s
            if (s.gold < cost) return@update s.copy(statusMessage = "[사병] 자금이 부족합니다. (필요 ${cost}G)")
            val gain = 60
            val message = "[사병] 개인 병력 +${gain}명 (자금 -${cost}G) — 사병 ${comma(career.privateTroops + gain)}명. " +
                "리더 몰래 키우는 힘입니다."
            s.copy(
                gold = s.gold - cost,
                heroCareer = career.copy(privateTroops = career.privateTroops + gain),
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 영웅 경력 — 공적 보고 (명성·충성 상승) */
    fun reportMerit() {
        _uiState.update { s ->
            val career = s.heroCareer ?: return@update s
            val meritGain = 8
            val message = "[공적] 임무 보고 — 공적 ${career.merit + meritGain}, 명성 +3, 충성 +2"
            s.copy(
                prestige = (s.prestige + 3).coerceIn(0, 100),
                heroCareer = career.copy(
                    merit = career.merit + meritGain,
                    loyalty = (career.loyalty + 2).coerceAtMost(100)
                ),
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    /** 영웅 경력 — 독립 (명성 55 이상, 공적 40 이상) */
    fun declareIndependence() {
        _uiState.update { s ->
            val career = s.heroCareer ?: return@update s
            if (career.merit < 40 || s.prestige < 55) {
                return@update s.copy(
                    statusMessage = "[독립 불가] 공적 ${career.merit}/40 · 명성 ${s.prestige}/55 — 더 쌓아야 합니다."
                )
            }
            val message = "[독립] ${career.heroName}이(가) ${career.employerLordName}의 휘하를 떠나 " +
                "독자 세력을 세웠습니다. 사병 ${comma(career.privateTroops)}명이 따릅니다."
            s.copy(
                heroCareer = career.copy(independent = true),
                startMode = StartMode.LORD,
                troops = s.troops + career.privateTroops,
                statusMessage = message,
                log = (s.log + message).takeLast(8)
            )
        }
        persist()
    }

    // ──────────────────────────────────────────────────────────────
    // 재원 탭 서브 메뉴 — 식량 · 골드 · 인구수 · 병력
    // ──────────────────────────────────────────────────────────────

    /** 이번 절기에 이 방법을 쓴 횟수 */
    fun boostUsesOf(actionId: String): Int = _uiState.value.boostUses[actionId] ?: 0

    /**
     * 재원 증가 방법 실행 — 즉시 실행은 상태를 바꾸고,
     * 화면이 필요한 항목은 해당 탭으로, 절기 항목은 절기를 넘긴다.
     */
    fun boostResource(actionId: String) {
        val action = ResourceBoosts.byId(actionId)
            ?: return
        when (action.kind) {
            BoostKind.NAVIGATE -> {
                val (tab, sub) = boostTargetOf(action.target)
                _uiState.update { it.copy(resourceMessage = null) }
                openTab(tab, sub)
            }

            BoostKind.SEASON -> {
                _uiState.update { it.copy(resourceMessage = null) }
                advanceSeason()
            }

            BoostKind.INSTANT -> {
                val s = _uiState.value
                val used = s.boostUses[action.id] ?: 0
                val outcome = ResourceBoosts.execute(
                    actionId = action.id,
                    gold = s.gold,
                    food = s.food,
                    population = s.population,
                    troops = s.troops,
                    morale = s.morale,
                    usedThisSeason = used
                )
                _uiState.update { st ->
                    if (!outcome.ok) {
                        st.copy(resourceMessage = outcome.message, statusMessage = outcome.message)
                    } else {
                        // 정규 병력은 전체 인구 5% 상한을 넘지 못한다 (용병은 별도)
                        val troopDelta = if (outcome.troopsDelta > 0) {
                            outcome.troopsDelta.coerceAtMost((st.troopCap - st.troops).coerceAtLeast(0))
                        } else outcome.troopsDelta
                        val capNote = if (outcome.troopsDelta > 0 && troopDelta < outcome.troopsDelta) {
                            "\n[상한] 정규 병력은 인구의 5%(${comma(st.troopCap)}명)까지입니다 — ${comma(troopDelta)}명만 충원. 용병을 쓰십시오."
                        } else ""
                        st.copy(
                            gold = (st.gold + outcome.goldDelta).coerceAtLeast(0),
                            food = (st.food + outcome.foodDelta).coerceAtLeast(0),
                            population = (st.population + outcome.populationDelta).coerceAtLeast(0),
                            troops = (st.troops + troopDelta).coerceAtLeast(0),
                            morale = (st.morale + outcome.moraleDelta).coerceIn(0, 100),
                            boostUses = st.boostUses + (action.id to used + 1),
                            resourceMessage = outcome.message + capNote,
                            statusMessage = outcome.message + capNote,
                            log = (st.log + outcome.message).takeLast(8),
                            mapNodes = if (troopDelta == 0) st.mapNodes else {
                                TerritoryMapLayout.build(
                                    start = st.regionArea ?: RegionCatalog.defaultRegion(),
                                    unlockedHop = st.unlockedHop,
                                    playerName = st.playerLord?.name ?: "이광수",
                                    homeTroops = (st.troops + troopDelta).coerceAtLeast(0)
                                )
                            }
                        )
                    }
                }
                persist()
            }
        }
    }

    /** 재원 증가 방법 → 화면 이동 대상 */
    private fun boostTargetOf(target: BoostTarget?): Pair<MainTab, Int> = when (target) {
        BoostTarget.TECH -> MainTab.DOMAIN to DomainTab.TECH.ordinal
        BoostTarget.HERO_PLACE -> MainTab.HERO to HeroTab.PLACE.ordinal
        BoostTarget.HERO_RECRUIT -> MainTab.HERO to HeroTab.RECRUIT.ordinal
        BoostTarget.FACILITIES -> MainTab.DOMAIN to DomainTab.FACILITIES.ordinal
        BoostTarget.EVENTS -> MainTab.DOMAIN to DomainTab.EVENTS.ordinal
        BoostTarget.COUNCIL -> MainTab.DOMAIN to DomainTab.COUNCIL.ordinal
        BoostTarget.PROMOTION -> MainTab.DOMAIN to DomainTab.PROMOTION.ordinal
        BoostTarget.SEASON_VIEW -> MainTab.DOMAIN to DomainTab.SEASON.ordinal
        BoostTarget.WAR_OPS -> MainTab.WAR to WarTab.OPS.ordinal
        null -> MainTab.MAP to 0
    }

    /** 재원 시트 숫자 표기 */
    private fun comma(value: Int): String = "%,d".format(value)

    /** 영웅 탭에서 빈 슬롯을 대기 영웅으로 채운다 */
    fun autoFillHeroSlots() {
        val filled = HeroEngine.fillEmptySlots(_uiState.value.heroes)
        applyHeroResult(
            com.game.slg.core.hero.HeroActionResult(
                roster = filled,
                success = filled != _uiState.value.heroes,
                message = "[배치] 빈 슬롯을 대기 영웅으로 채웠습니다."
            )
        )
    }

    // ──────────────────────────────────────────────────────────────
    // 지역 연합 — 같은 지역 리더 교전
    // ──────────────────────────────────────────────────────────────

    /**
     * 지역 내 AI 리더 압박. 지역 연합 기간에는 교전이 막히고,
     * 연합이 끝나면 실제 교전이 열린다.
     */
    fun challengeRegionLord(lordId: String) {
        _uiState.update { s ->
            val target = s.lords.firstOrNull { it.id == lordId && !it.isPlayer }
                ?: return@update s.copy(statusMessage = "[교전] 명부에 없는 리더입니다.")
            if (target.incorporated) {
                val message = "[교전] ${target.name} 리더는 이미 우리 연방에 편입된 세력입니다. " +
                    "교전 대신 영토 탭에서 운영하세요."
                return@update s.copy(
                    statusMessage = message,
                    log = (s.log + message).takeLast(8)
                )
            }
            val verdict = s.attackVerdict(AttackScope.SAME_REGION, AttackSide.EXPEDITION)
            if (!verdict.allowed) {
                val message = RegionUnionEngine.unionBlockMessage(s.union)
                return@update s.copy(
                    statusMessage = message,
                    log = (s.log + message).takeLast(8)
                )
            }

            val area = s.regionArea ?: RegionCatalog.defaultRegion()
            val own = ForceComposition.standard(
                (s.totalTroops * (1.0 + s.heroBonus.combatPercent / 100.0)).toInt()
            )
            val enemy = ForceComposition.standard(target.troops.coerceAtLeast(60))
            val enemyStrategy = StrategyBattleEngine.enemyStrategyFor(area.terrain, target.name.hashCode())
            val strat = StrategyBattleEngine.resolve(
                own = own,
                enemy = enemy,
                terrain = area.terrain,
                ownStrategy = s.battleStrategy,
                enemyStrategy = enemyStrategy,
                ownTech = s.weaponTech,
                enemyTechLevel = StrategyBattleEngine.enemyTechLevel(s.unlockedHop, target.name.hashCode()),
                ownOpsBonusPercent = (s.heroBonus.combatPercent + s.coalitionBonusPercent).coerceAtMost(60),
                defending = false
            )
            val win = strat.ownWon
            val ourLosses = strat.ownLosses
            val theirLosses = strat.enemyLosses
            val message = "[교전] ${target.name} 리더 — ${if (win) "압박 성공" else "공격 실패"} · " +
                "적 사상자 $theirLosses, 아군 사상자 $ourLosses (교환비 ${strat.exchangeLabel})"
            val report = OperationReport(
                operation = "지역 리더 압박",
                defenderWon = !win,
                attackerLosses = theirLosses,
                defenderLosses = ourLosses,
                message = message,
                verdict = if (win) "지역 내 주도권 확보" else "지역 내 반발 — 재편성 필요",
                targetName = target.name,
                composition = "아군 ${own.describe()} / 적 ${enemy.describe()}",
                requiredLevel = 1,
                strategyLabel = "${s.battleStrategy.icon} ${s.battleStrategy.label} vs ${enemyStrategy.icon} ${enemyStrategy.label}",
                exchangeLabel = strat.exchangeLabel,
                breakdown = strat.lines,
                ownLossPercent = (strat.ownLossRatio * 100).toInt(),
                enemyLossPercent = (strat.enemyLossRatio * 100).toInt()
            )
            val (regularLeft, mercsLeft) = applyLosses(s.troops, s.mercenaries, ourLosses)
            val nextTroops = (target.troops - theirLosses).coerceAtLeast(0)
            val factionId = "faction_${target.id}"
            val faction = s.factions.firstOrNull { it.id == factionId }
            // 상대 병력이 0 이 되면 추가 손실 없이 즉시 강제 편입 (A안 — 같은 지역 리더 전체 대상)
            val forced = if (nextTroops <= 0 && faction != null && !faction.incorporated) {
                val context = s.empireContext().copy(troops = regularLeft + mercsLeft.sumOf { it.troops })
                AbsorptionEngine.forceConquest(faction, context)
            } else null
            val annexed = forced?.success == true
            val captured = if (annexed) {
                HeroDispositionEngine.capturesOf(faction!!)
            } else emptyList()
            val finalMessage = if (annexed) {
                message + "\n" + forced!!.message
            } else message
            val territories = if (annexed) {
                val newTerritory = forced!!.territory
                if (newTerritory == null || s.territories.any { it.code == newTerritory.code }) s.territories
                else s.territories + newTerritory
            } else s.territories
            s.copy(
                troops = regularLeft,
                mercenaries = mercsLeft,
                gold = (s.gold + (forced?.goldDelta ?: 0)).coerceAtLeast(0),
                prestige = (s.prestige + (if (win) 4 else -3) + (forced?.prestigeDelta ?: 0)).coerceIn(0, 100),
                morale = (s.morale + (if (win) 2 else -3) + (forced?.moraleDelta ?: 0)).coerceIn(0, 100),
                lords = s.lords.map { lord ->
                    if (lord.id != target.id) lord
                    else lord.copy(
                        troops = if (annexed) 0 else nextTroops,
                        incorporated = annexed,
                        // 병합·종속된 세력을 우리가 빼앗으면 예속 관계는 끊기고 우리 연방으로 들어온다
                        bond = if (annexed) null else lord.bond,
                        masterId = if (annexed) null else lord.masterId,
                        activity = if (annexed) "우리 연방으로 강제 편입" else lord.activity
                    )
                },
                factions = s.factions.map { current ->
                    when {
                        current.id != factionId -> current
                        annexed -> forced!!.faction
                        else -> current.copy(troops = nextTroops)
                    }
                },
                territories = territories,
                selectedManagedCode = if (annexed) forced!!.territory?.code ?: s.selectedManagedCode
                else s.selectedManagedCode,
                heroes = HeroDispositionEngine.markCaptured(s.heroes, captured),
                pendingDisposition = s.pendingDisposition + captured,
                lastAbsorption = if (annexed) forced else s.lastAbsorption,
                lastReport = report,
                log = (s.log + finalMessage).takeLast(8),
                statusMessage = finalMessage
            )
        }
        persist()
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
        persist()
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
        trait = trait,
        troops = troops,
        gold = gold,
        grain = grain,
        prestige = prestige,
        arm = arm,
        bond = bond,
        masterId = masterId,
        activity = activity
    )

    /** 화면용 리더 → AI 턴 엔진 입력값 */
    private fun LordView.toProfile(region: RegionArea): AiLordProfile = AiLordProfile(
        id = id,
        name = name,
        regionCode = region.code,
        regionName = region.name,
        stronghold = stronghold,
        trait = trait,
        troops = troops,
        grain = grain,
        gold = gold,
        prestige = prestige,
        arm = arm,
        masterId = masterId,
        bond = bond,
        activity = activity
    )

    // ──────────────────────────────────────────────────────────────
    // 영토 · 세력 초기화
    // ──────────────────────────────────────────────────────────────

    /** 본진 영지 — 시작 지역은 직할로 직접 통치한다 */
    private fun homeTerritory(area: RegionArea, troops: Int, morale: Int = 62): ManagedTerritory =
        ManagedTerritory(
            code = area.code,
            name = area.name,
            population = area.population,
            status = TerritoryStatus.DIRECT,
            morale = morale,
            development = 35,
            troops = troops,
            food = 0,
            gold = 0,
            defense = 45,
            absorbedBy = "인계"
        )

    /**
     * 같은 지역 리더 세력 — 편입 대상.
     * 리더 명부와 같은 사람들이며, 유저인지 AI인지 드러나는 값은 두지 않는다.
     */
    private fun factionsOf(area: RegionArea, lords: List<AiLordProfile>, playerLordId: String? = null): List<Faction> {
        val perLord = (area.population / area.lordCapacity.coerceAtLeast(1)).coerceAtLeast(500)
        // 소속 영웅 — 인계받은 리더까지 포함한 전원 순서로 배정해, 내가 누구를 골라도
        // 다른 리더의 소속이 바뀌지 않게 한다(lord.id 시드라 재시작해도 같다).
        val assigned = mutableSetOf<String>()
        val heroesByLord = lords.associate { lord ->
            val ids = HeroDispositionEngine.heroesForLord(lord.id, assigned)
            assigned += ids
            lord.id to ids
        }
        return lords.filter { it.id != playerLordId }.mapIndexed { index, lord ->
            Faction(
                id = "faction_${lord.id}",
                name = lord.name,
                territoryCode = "${area.code}_${index + 1}",
                territoryName = "${area.name} ${index + 1}구역",
                population = (perLord * (0.85 + (index % 4) * 0.1)).toInt(),
                troops = lord.troops.coerceAtLeast(60),
                morale = (38 + (lord.prestige % 30)).coerceIn(20, 92),
                affinity = (28 + (lord.prestige % 35)).coerceIn(10, 85),
                prestige = lord.prestige,
                gold = lord.gold,
                food = lord.grain,
                relation = when {
                    lord.prestige >= 60 -> FactionRelation.FRIENDLY
                    lord.prestige >= 45 -> FactionRelation.NEUTRAL
                    else -> FactionRelation.WARY
                },
                commonEnemy = index % 3 == 0,
                heroIds = heroesByLord[lord.id].orEmpty()
            )
        }
    }

    /** 영지 목록에서 방어 태세·민심이 높은 순으로 정렬한 총력 */
    private fun empireSummary(state: SlgGameState): String =
        "영지 ${state.territories.size}곳(편입 ${state.absorbedCount}) · " +
            ManpowerEngine.label(state.totalTerritoryPopulation, state.troops, state.mercenaryTroops)

    /** 손실 배분 — 정규병을 먼저 잃고, 모자라면 용병이 쓰러진다 */
    private fun applyLosses(
        regular: Int,
        mercenaries: List<MercenaryBand>,
        losses: Int
    ): Pair<Int, List<MercenaryBand>> {
        val total = losses.coerceAtLeast(0)
        val fromRegular = total.coerceAtMost(regular)
        var rest = total - fromRegular
        val kept = mercenaries.mapNotNull { band ->
            if (rest <= 0) band
            else {
                val take = rest.coerceAtMost(band.troops)
                rest -= take
                val left = band.troops - take
                if (left <= 0) null else band.copy(troops = left, upkeepPerSeason = MercenaryEngine.upkeepOf(left))
            }
        }
        return (regular - fromRegular) to kept
    }

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
        name = "${s.playerLord?.name ?: "이광수"} 리더 (${s.regionName})",
        isHumanPlayer = true,
        territoryCode = s.regionCode,
        troops = s.troops,
        grain = s.food,
        gold = s.gold,
        prestige = s.prestige,
        isCastellan = true
    )

    /** 의회 구성 — 플레이어를 제외한 AI 리더 (최대 12석) */
    private fun buildCouncilLords(s: SlgGameState): List<Lord> {
        // 우리 연방으로 편입된 리더는 의결권(내 표)으로 이미 반영되므로 AI 의석에서 뺀다
        val aiLords = s.lords.filter { !it.isPlayer && !it.incorporated }.take(12)
        if (aiLords.isEmpty()) {
            return listOf(
                Lord("lord_1", "인접 리더 A", false, s.regionCode, troops = 300, grain = 1200, gold = 800, prestige = 55)
            )
        }
        return aiLords.map { lord ->
            Lord(
                id = lord.id,
                name = "${lord.name} 리더",
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
        fertileScore: Double = 1.0,
        population: Int = area.population
    ): Territory = Territory(
        code = area.code,
        name = area.name,
        province = area.province,
        cityCountyDistrict = listOf(area.city, area.district).filter { it.isNotBlank() }.joinToString(" "),
        population = population.coerceAtLeast(1),
        maxLords = area.lordCapacity,
        terrain = area.terrain,
        fortress = fortress,
        fertileScore = fertileScore
    )

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
