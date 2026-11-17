package com.game.slg.core.model

// =============================================================================
// 추가 시나리오 3종 모델
// 1) 「연방 붕괴의 그림자: 인접 구역 이재민 유입 딜레마」 — 자치구 내정 위기 이벤트
// 2) 「호족 의회의 이면: 히든 인재 야간 밀회」 — 포섭 / 숙청 / 이중첩자
// 3) 「대표 호족 승격 마일스톤 로드맵」 — 읍·면·동 호족 → 과반 연합 대표 → 광역 통합군주
// =============================================================================

// ── 1. 유민 유입 딜레마 ──────────────────────────────────────────────────────

/**
 * 유민 대응 선택지 종류
 */
enum class RefugeeResponse(val isOpenGate: Boolean) {
    OPEN_GATE(true),     // 임시 거주지 개방 및 식량 배급
    CONTROL_GATE(false)  // 통제선 유지 및 선별 수용
}

/**
 * 유민 이벤트 선택지 (UI 표시용)
 */
data class RefugeeCrisisChoice(
    val id: String,
    val response: RefugeeResponse,
    val label: String,
    val hint: String
)

/**
 * 유민 이벤트 처리 결과
 */
data class RefugeeCrisisResult(
    val isSuccess: Boolean,
    val choiceId: String,
    val response: RefugeeResponse,
    val foodChange: Int = 0,
    val goldChange: Int = 0,
    val populationChange: Int = 0,
    val moraleChange: Int = 0,
    val culturePrestigeChange: Int = 0,
    val diplomacyChange: Int = 0,
    val failureReason: String? = null,
    val message: String = ""
)

// ── 2. 히든 인재 야간 밀회 ───────────────────────────────────────────────────

/**
 * 야간 밀회 3지선다
 */
enum class NightMeetingChoice(val displayName: String) {
    RECRUIT("신뢰를 주고 핵심 참모로 포섭"),
    PURGE("즉시 숙청 및 재산 몰수"),
    DOUBLE_AGENT("역이용해 이중첩자로 상대 거점에 재파견")
}

/**
 * 야간 밀회 처리 결과
 */
data class NightMeetingResult(
    val isSuccess: Boolean,
    val choice: NightMeetingChoice,
    val talentName: String,
    val suspicionChange: Int = 0,
    val goldChange: Int = 0,
    val moraleChange: Int = 0,
    val prestigeChange: Int = 0,
    val espionageChange: Int = 0,
    val talentRemoved: Boolean = false,
    val doubleAgentBuff: String? = null,
    val failureReason: String? = null,
    val message: String = ""
)

// ── 3. 대표 호족 승격 마일스톤 로드맵 ─────────────────────────────────────────

/**
 * 승격 단계: 거점 호족 → 과반 연합 대표 → 광역 통합군주
 * - requiredAlliedTerritories: 우호/동맹으로 묶어야 하는 인접 영지 수
 * - requiredPrestige: 요구 명성
 * - councilAuthorityBonus: 승격 시 획득하는 의회 권한 보너스
 */
enum class PromotionStage(
    val displayName: String,
    val order: Int,
    val requiredAlliedTerritories: Int,
    val requiredPrestige: Int,
    val councilAuthorityBonus: Int
) {
    DISTRICT_LORD("거점 호족", 0, 0, 0, 0),
    UNITED_CASTELLAN("연합 대표", 1, 2, 80, 10),
    CITY_SOVEREIGN("광역 통합군주", 2, 3, 95, 25);

    /** 다음 승격 단계 (최종 단계면 null) */
    val nextStage: PromotionStage?
        get() = values().firstOrNull { it.order == this.order + 1 }

    /** 최종 단계 여부 */
    val isFinal: Boolean
        get() = nextStage == null
}

/**
 * 승격 조건 평가 결과 (로드맵 바 표시용)
 */
data class PromotionEvaluation(
    val current: PromotionStage,
    val next: PromotionStage?,
    val alliedTerritories: Int,
    val requiredAlliedTerritories: Int,
    val prestige: Int,
    val requiredPrestige: Int,
    val isEligible: Boolean,
    val progressRatio: Float,
    val requirementSummary: String
)

/**
 * 승격 처리 결과 (👑 승격 의식 배너 + 의회 권한 확장 보너스)
 */
data class PromotionResult(
    val isSuccess: Boolean,
    val stage: PromotionStage,
    val promoted: Boolean = false,
    val ceremonyTitle: String? = null,
    val ceremonyBody: String? = null,
    val councilAuthorityBonus: Int = 0,
    val failureReason: String? = null,
    val message: String = ""
)
