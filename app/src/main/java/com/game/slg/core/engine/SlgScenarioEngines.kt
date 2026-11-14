package com.game.slg.core.engine

import com.game.slg.core.model.*

// =============================================================================
// 1. 「연방 붕괴의 그림자: 광교·용인 유민 유입 딜레마」 엔진
// =============================================================================

/**
 * 유민 유입 딜레마 엔진 인터페이스
 * - 연방 통신·물류망 붕괴로 유민 800명이 매탄동 성문 앞에 몰려든 상황.
 * - 성문 개방(식량 -500 · 인구 +800 · 민심 +10 · 문화 명성 +8)
 * - 성문 통제(자금 +200 관세 · 민심 -12 · 외교 평판 -8)
 */
interface IRefugeeCrisisEngine {

    /** 성문 앞에 몰려든 유민 수 */
    val refugeeCount: Int

    /** 선택지 목록 */
    fun choices(): List<RefugeeCrisisChoice>

    /** 선택지 처리 (식량·자금 보유량 검증 포함) */
    fun resolve(choiceId: String, food: Int, gold: Int): RefugeeCrisisResult
}

/**
 * 유민 유입 딜레마 엔진 기본 구현체
 */
class RefugeeCrisisEngine : IRefugeeCrisisEngine {

    override val refugeeCount: Int = 800

    /** 개방 선택지: 소요 식량 */
    private val openFoodCost: Int = 500

    /** 개방 선택지: 수용 인구 */
    private val acceptedPopulation: Int = 800

    /** 통제 선택지: 관세 수입 */
    private val tariffGold: Int = 200

    override fun choices(): List<RefugeeCrisisChoice> = listOf(
        RefugeeCrisisChoice(
            id = "REFUGEE_A_OPEN",
            response = RefugeeResponse.OPEN_GATE,
            label = "임시 거주지 개방 및 식량 배급",
            hint = "식량 -$openFoodCost · 인구 +$acceptedPopulation · 민심 +10 · 문화 명성 +8"
        ),
        RefugeeCrisisChoice(
            id = "REFUGEE_B_CONTROL",
            response = RefugeeResponse.CONTROL_GATE,
            label = "성문 통제 및 선별 수용",
            hint = "자금 +$tariffGold (관세) · 민심 -12 · 외교 평판 -8"
        )
    )

    override fun resolve(choiceId: String, food: Int, gold: Int): RefugeeCrisisResult {
        val choice = choices().firstOrNull { it.id == choiceId }
            ?: return RefugeeCrisisResult(
                isSuccess = false,
                choiceId = choiceId,
                response = RefugeeResponse.CONTROL_GATE,
                failureReason = "알 수 없는 선택지입니다. (id=$choiceId)",
                message = "[유민 이벤트 실패] 알 수 없는 선택지입니다. (id=$choiceId)"
            )

        return when (choice.response) {
            RefugeeResponse.OPEN_GATE -> {
                if (food < openFoodCost) {
                    RefugeeCrisisResult(
                        isSuccess = false,
                        choiceId = choiceId,
                        response = choice.response,
                        failureReason = "식량이 부족합니다. (필요 ${openFoodCost}석 / 보유 ${food}석)",
                        message = "[성문 개방 불가] 배급할 식량이 부족합니다. (필요 ${openFoodCost}석 / 보유 ${food}석)\n" +
                            "성문 통제로 선별 수용하거나 추수를 기다려야 합니다."
                    )
                } else {
                    RefugeeCrisisResult(
                        isSuccess = true,
                        choiceId = choiceId,
                        response = choice.response,
                        foodChange = -openFoodCost,
                        populationChange = acceptedPopulation,
                        moraleChange = 10,
                        culturePrestigeChange = 8,
                        message = "[성문 개방] 매탄동 성문이 열리고 유민 ${acceptedPopulation}명이 임시 거주지로 들어왔다. " +
                            "아이들의 울음과 밥 짓는 연기가 골목을 채운다.\n" +
                            "\"호족님, 이 은혜는 잊지 않겠습니다.\" — 식량 -${openFoodCost}석 · 인구 +${acceptedPopulation} · " +
                            "민심 +10 · 문화 명성 +8"
                    )
                }
            }

            RefugeeResponse.CONTROL_GATE -> {
                RefugeeCrisisResult(
                    isSuccess = true,
                    choiceId = choiceId,
                    response = choice.response,
                    goldChange = tariffGold,
                    moraleChange = -12,
                    diplomacyChange = -8,
                    message = "[성문 통제] 성문 앞에 검을 세우고 신분이 확인된 이들만 선별 수용했다. " +
                        "통행 관세 ${tariffGold}G가 금고에 들어왔지만, 성벽 밖에서 밤을 새운 이들의 원망이 남았다.\n" +
                        "자금 +$tariffGold · 민심 -12 · 외교 평판 -8"
                )
            }
        }
    }
}

// =============================================================================
// 2. 「호족 의회의 이면: 히든 인재 야간 밀회」 엔진
// =============================================================================

/**
 * 히든 인재 야간 밀회 엔진 인터페이스
 * - 의심도 50 이상인 인재에게 「🌙 야간 밀회: 비밀 서신」을 보내 3지선다로 처리한다.
 */
interface IHiddenTalentNightEngine {

    /** 야간 밀회 대상이 되는 의심도 기준 */
    val suspicionThreshold: Int

    /** 대상 자격 판정 */
    fun isEligible(suspicion: Int): Boolean

    /** 3지선다 목록 */
    fun choices(): List<NightMeetingChoice>

    /** 선택 처리 */
    fun resolve(
        choice: NightMeetingChoice,
        talentName: String,
        suspicion: Int,
        gold: Int
    ): NightMeetingResult
}

/**
 * 히든 인재 야간 밀회 엔진 기본 구현체
 */
class HiddenTalentNightEngine : IHiddenTalentNightEngine {

    override val suspicionThreshold: Int = 50

    override fun isEligible(suspicion: Int): Boolean = suspicion >= suspicionThreshold

    override fun choices(): List<NightMeetingChoice> = NightMeetingChoice.values().toList()

    override fun resolve(
        choice: NightMeetingChoice,
        talentName: String,
        suspicion: Int,
        gold: Int
    ): NightMeetingResult {
        if (!isEligible(suspicion)) {
            return NightMeetingResult(
                isSuccess = false,
                choice = choice,
                talentName = talentName,
                failureReason = "의심도 $suspicion — 밀회 대상 기준(${suspicionThreshold})에 미달합니다.",
                message = "[야간 밀회 불가] '${talentName}'의 의심도가 $suspicion 으로 기준(${suspicionThreshold}) 미달입니다. " +
                    "아직 비밀 서신을 보낼 근거가 부족합니다."
            )
        }

        return when (choice) {
            NightMeetingChoice.RECRUIT -> NightMeetingResult(
                isSuccess = true,
                choice = choice,
                talentName = talentName,
                suspicionChange = -30,
                prestigeChange = 5,
                espionageChange = 10,
                message = "[포섭 성공] 비밀 서신에 답장이 왔다. \"호족님의 뜻을 따르겠습니다.\"\n" +
                    "'$talentName'을(를) 핵심 참모로 포섭했습니다. 의심도 -30 · 명성 +5 · 첩보력 +10"
            )

            NightMeetingChoice.PURGE -> NightMeetingResult(
                isSuccess = true,
                choice = choice,
                talentName = talentName,
                goldChange = 400,
                moraleChange = -8,
                talentRemoved = true,
                message = "[숙청 집행] 자정 무렵 매탄동 저잣거리에서 '$talentName'이(가) 끌려갔다. " +
                    "압수한 재산은 금고로 들어왔다.\n" +
                    "자금 +400 · 민심 -8 · 인재 명부에서 제거 — 다만 골목의 눈빛이 차가워졌다."
            )

            NightMeetingChoice.DOUBLE_AGENT -> {
                if (gold < 150) {
                    NightMeetingResult(
                        isSuccess = false,
                        choice = choice,
                        talentName = talentName,
                        failureReason = "역파견 비용이 부족합니다. (필요 150G / 보유 ${gold}G)",
                        message = "[역파견 불가] 이중첩자로 되돌려 보낼 자금이 부족합니다. (필요 150G / 보유 ${gold}G)"
                    )
                } else {
                    NightMeetingResult(
                        isSuccess = true,
                        choice = choice,
                        talentName = talentName,
                        goldChange = -150,
                        suspicionChange = -10,
                        espionageChange = 5,
                        doubleAgentBuff = "광교동 정보·보급 교란 — 적 군량 -200석 · 다음 첩보 성공률 +20%",
                        message = "[이중첩자 역파견] '$talentName'에게 위조된 신분과 150G를 쥐여 주고 광교동으로 돌려보냈다. " +
                            "녀석이 넘기는 서신에는 우리가 원하는 숫자만 적힐 것이다.\n" +
                            "자금 -150 · 첩보력 +5 · 광교동 정보·보급 교란 버프 획득 (적 군량 -200석)"
                    )
                }
            }
        }
    }
}

// =============================================================================
// 3. 「성주(Castellan) 승격 마일스톤 로드맵」 엔진
// =============================================================================

/**
 * 승격 로드맵 엔진 인터페이스
 * - 읍·면·동 호족 → 과반 연합 성주 → 수원시 통합군주
 * - 인접 영지 우호/동맹 수와 명성 조건을 채우면 승격 의식을 올릴 수 있다.
 */
interface IPromotionEngine {

    /** 현재 단계의 승격 조건 평가 */
    fun evaluate(current: PromotionStage, alliedTerritories: Int, prestige: Int): PromotionEvaluation

    /** 승격 의식 실행 */
    fun promote(current: PromotionStage, alliedTerritories: Int, prestige: Int): PromotionResult
}

/**
 * 승격 로드맵 엔진 기본 구현체
 */
class PromotionEngine : IPromotionEngine {

    override fun evaluate(
        current: PromotionStage,
        alliedTerritories: Int,
        prestige: Int
    ): PromotionEvaluation {
        val next = current.nextStage
        if (next == null) {
            return PromotionEvaluation(
                current = current,
                next = null,
                alliedTerritories = alliedTerritories,
                requiredAlliedTerritories = 0,
                prestige = prestige,
                requiredPrestige = 0,
                isEligible = false,
                progressRatio = 1f,
                requirementSummary = "최종 단계 — 수원시 통합군주로서 로드맵을 완주했습니다."
            )
        }

        val alliedRatio =
            if (next.requiredAlliedTerritories == 0) 1f
            else (alliedTerritories.toFloat() / next.requiredAlliedTerritories).coerceIn(0f, 1f)
        val prestigeRatio =
            if (next.requiredPrestige == 0) 1f
            else (prestige.toFloat() / next.requiredPrestige).coerceIn(0f, 1f)
        val ratio = minOf(alliedRatio, prestigeRatio)
        val eligible = alliedTerritories >= next.requiredAlliedTerritories && prestige >= next.requiredPrestige

        val summary = buildString {
            append("다음 단계 '${next.displayName}' 조건 — ")
            append("우호·동맹 영지 ${alliedTerritories}/${next.requiredAlliedTerritories} · ")
            append("명성 ${prestige}/${next.requiredPrestige}")
            if (eligible) append(" · 승격 의식 개최 가능!")
        }

        return PromotionEvaluation(
            current = current,
            next = next,
            alliedTerritories = alliedTerritories,
            requiredAlliedTerritories = next.requiredAlliedTerritories,
            prestige = prestige,
            requiredPrestige = next.requiredPrestige,
            isEligible = eligible,
            progressRatio = ratio,
            requirementSummary = summary
        )
    }

    override fun promote(
        current: PromotionStage,
        alliedTerritories: Int,
        prestige: Int
    ): PromotionResult {
        val evaluation = evaluate(current, alliedTerritories, prestige)
        val next = evaluation.next
            ?: return PromotionResult(
                isSuccess = false,
                stage = current,
                failureReason = "이미 최종 단계입니다.",
                message = "[승격 불가] 이미 '${current.displayName}' — 로드맵의 마지막 단계입니다."
            )

        if (!evaluation.isEligible) {
            return PromotionResult(
                isSuccess = false,
                stage = current,
                failureReason = evaluation.requirementSummary,
                message = "[승격 조건 미달] ${evaluation.requirementSummary}"
            )
        }

        return PromotionResult(
            isSuccess = true,
            stage = next,
            promoted = true,
            ceremonyTitle = if (next == PromotionStage.UNITED_CASTELLAN) "👑 성주 승격 의식" else "👑 수원시 통합군주 즉위식",
            ceremonyBody = if (next == PromotionStage.UNITED_CASTELLAN) {
                "매탄동 광장에 호족들이 모였다. 과반의 연합 서명이 놓이고, " +
                    "'${current.displayName}' 이광수 호족이 성주 자리에 올랐다.\n" +
                    "의회 권한이 확장되어 병력 차출 요구 상한이 +${next.councilAuthorityBonus}% 완화되고, " +
                    "특별세 안건 가결 요건이 낮아졌다."
            } else {
                "수원시 전역의 자치구 깃발이 하나로 내려앉았다. 성주에서 통합군주로 — " +
                    "영통·매탄·광교·수지가 하나의 행정 질서로 묶였다.\n" +
                    "의회 권한 +${next.councilAuthorityBonus}% — 이제 전시 병력 차출을 의회 반대로 막을 수 없다."
            },
            councilAuthorityBonus = next.councilAuthorityBonus,
            message = "[승격 완료] '${current.displayName}' → '${next.displayName}' (의회 권한 +${next.councilAuthorityBonus}%)"
        )
    }
}
