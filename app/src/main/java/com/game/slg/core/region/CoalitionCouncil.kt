package com.game.slg.core.region

/**
 * 지역 리더 연합 의회 — 공동 전투·외교·전략과 긴급 방어.
 *
 * 사용자 요청(2026-11-25 07:45)
 *  ① *"의회에 안건을 올려 나 혼자가 아닌 지역 리더 연합으로 공동 전투·외교·전략 진행"*
 *  ② *"타 지역 세력이 우리 지역으로 쳐들어왔을 때 긴급 의회가 열려 다 함께 방어 안건을 상정·가결"*
 *  ③ *"내 영지로 침공이 들어온 경우 인근 우호 리더들에게 방어 도움을 요청… 다른 리더가 공격받을 때
 *     내가 지원군/도움을 보낼 수도"*
 *
 * 안드로이드 의존성 없이 JVM 단위 테스트로 검증한다.
 */

/** 연합 안건 1건 — 인접 지역에 대한 공동 작전 */
data class CoalitionBill(
    val id: String,
    val kind: AdjacentActionKind,
    val regionCode: String,
    val regionName: String,
    val title: String,
    val requestedSeason: Int,
    /** 찬성표 · 반대표 · 우리(플레이어) 몫 표 */
    val favor: Int = 0,
    val against: Int = 0,
    val ourVotes: Int = 1,
    val passed: Boolean? = null,
    val message: String? = null
) {
    val pending: Boolean get() = passed == null
    val totalVotes: Int get() = favor + against

    /** 반대 비율 (%) — 65% 이상이면 연합 불복 */
    val againstPercent: Int get() = if (totalVotes == 0) 0 else against * 100 / totalVotes
}

/** 연합 의회 상태 — 안건 목록 + 통과된 공동 작전 보너스 */
data class CoalitionState(
    val bills: List<CoalitionBill> = emptyList(),
    /** 공동 전투로 얻은 다음 교전 보너스 (%) */
    val battleBonusPercent: Int = 0,
    /** 공동 외교로 얻은 외교력 */
    val diplomacyBonus: Int = 0,
    /** 공동 전략으로 얻은 전략 비용 할인 (%) */
    val strategyDiscountPercent: Int = 0,
    /** 이번 안건에 참여한 우호 리더 수 */
    val supporters: Int = 0
) {
    val pendingCount: Int get() = bills.count { it.pending }
    val passedCount: Int get() = bills.count { it.passed == true }

    fun pendingOf(kind: AdjacentActionKind): List<CoalitionBill> =
        bills.filter { it.pending && it.kind == kind }

    fun latestOf(kind: AdjacentActionKind): CoalitionBill? =
        bills.lastOrNull { it.kind == kind }

    /** 공동 작전 보너스를 한 번 쓰고 소모한다 */
    fun consumeBattleBonus(): CoalitionState = copy(battleBonusPercent = 0)

    fun consumeStrategyDiscount(): CoalitionState = copy(strategyDiscountPercent = 0)
}

object CoalitionCouncilEngine {

    /** 안건 상정 — 우호 리더 수로 찬성표가 정해진다 */
    fun sponsor(
        kind: AdjacentActionKind,
        regionCode: String,
        regionName: String,
        seasonTurn: Int,
        supporters: Int,
        sequence: Int = 0
    ): CoalitionBill {
        val title = when (kind) {
            AdjacentActionKind.WAR -> "[공동 전투] $regionName 연합 원정"
            AdjacentActionKind.DIPLOMACY -> "[공동 외교] $regionName 연합 교섭"
            AdjacentActionKind.STRATEGY -> "[공동 전략] $regionName 연합 공작"
        }
        val id = "coalition_${kind.name.lowercase()}_${regionCode}_${seasonTurn}" +
            if (sequence > 0) "_$sequence" else ""
        return CoalitionBill(
            id = id,
            kind = kind,
            regionCode = regionCode,
            regionName = regionName,
            title = title,
            requestedSeason = seasonTurn,
            favor = supporters.coerceAtLeast(0) * 3,
            against = 0
        )
    }

    /**
     * 표결 — 플레이어 의결권([ourVotes])은 찬성으로 들어간다.
     * 반대표는 상정 직후 AI 리더 성향에 따라 정해지고, 우리 의결권이 많을수록 뒤집힌다.
     */
    fun vote(
        bill: CoalitionBill,
        opposingVotes: Int,
        ourVotes: Int
    ): CoalitionBill {
        val favor = bill.favor + ourVotes.coerceAtLeast(1)
        val against = opposingVotes.coerceAtLeast(0)
        val passed = favor > against
        val message = buildString {
            append("[${bill.title}] 찬성 $favor : 반대 $against → ")
            append(if (passed) "가결" else "부결")
            append(" (내 의결권 ${ourVotes.coerceAtLeast(1)}표 포함)")
        }
        return bill.copy(
            favor = favor,
            against = against,
            ourVotes = ourVotes.coerceAtLeast(1),
            passed = passed,
            message = message
        )
    }

    /** 가결된 안건이 주는 보너스 */
    fun applyPassed(state: CoalitionState, bill: CoalitionBill): CoalitionState {
        if (bill.passed != true) return state
        return when (bill.kind) {
            AdjacentActionKind.WAR -> state.copy(
                battleBonusPercent = (state.battleBonusPercent +
                    CoalitionClamp.battleBonus(bill.favor - bill.against)).coerceAtMost(30)
            )
            AdjacentActionKind.DIPLOMACY -> state.copy(
                diplomacyBonus = (state.diplomacyBonus + 8).coerceAtMost(30)
            )
            AdjacentActionKind.STRATEGY -> state.copy(
                strategyDiscountPercent = (state.strategyDiscountPercent + 30).coerceAtMost(60)
            )
        }
    }

    /** 한 절기가 지나면 안건 기록은 최근 6건만 남긴다 */
    fun prune(state: CoalitionState): CoalitionState =
        state.copy(bills = state.bills.takeLast(6))
}

/** 작은 계산 도우미 — 찬성-반대 차이를 전투 보너스로 */
private object CoalitionClamp {
    fun battleBonus(margin: Int): Int = (margin.coerceAtLeast(0) * 2).coerceIn(4, 18)
}

/** 의결권 — 내가 직할·편입한 리더 수만큼 표를 가진다 */
object CouncilVotingPower {

    /** 기본 1표 + 편입 리더 1명당 1표 */
    fun of(incorporatedLeaders: Int): Int = 1 + incorporatedLeaders.coerceAtLeast(0)

    fun label(power: Int, incorporatedLeaders: Int): String =
        "내 의결권 ${power}표 (기본 1 + 편입 ${incorporatedLeaders.coerceAtLeast(0)}명)"
}

/** 방어 지원이 어느 쪽으로 오갔는지 */
enum class AidDirection(val label: String) {
    REQUESTED("요청 받음"),
    SENT("내가 파병")
}

/** 방어 지원 1건 */
data class DefenseAid(
    val leaderName: String,
    val troops: Int,
    val direction: AidDirection,
    val note: String = ""
)

/** 다른 리더가 공격받고 있다 — 지원군을 보낼 기회 */
data class AidCall(
    val attackerName: String,
    val victimName: String,
    val victimId: String,
    val kind: LeaderBond,
    val absorbedTroops: Int = 0,
    val answered: Boolean = false,
    val result: String? = null
) {
    val headline: String
        get() = "$attackerName 리더가 $victimName 리더를 ${kind.label} 중입니다"
}

/** 긴급 의회 — 외부 침공 때 열려 방어 안건을 상정·가결한다 */
data class EmergencyCouncil(
    val id: String,
    val invaderName: String,
    val regionName: String,
    val troopsLost: Int,
    val calledTurn: Int,
    val turnsLeft: Int = 2,
    val favor: Int = 0,
    val against: Int = 0,
    val ourVotes: Int = 1,
    val resolved: Boolean = false,
    val passed: Boolean? = null,
    val message: String? = null,
    val aids: List<DefenseAid> = emptyList()
) {
    val pending: Boolean get() = !resolved
    val totalVotes: Int get() = favor + against
    val againstPercent: Int get() = if (totalVotes == 0) 0 else against * 100 / totalVotes

    /** 요청해서 받은 지원 병력 합계 */
    val requestedTroops: Int
        get() = aids.filter { it.direction == AidDirection.REQUESTED }.sumOf { it.troops }

    val headline: String
        get() = "${invaderName}의 침공 — $regionName 긴급 의회"
}

/** 방어 지원 후보 — 우호 리더 1명 */
data class AidSource(
    val leaderId: String,
    val leaderName: String,
    val troops: Int,
    val friendly: Boolean
)

object EmergencyCouncilEngine {

    fun call(invaderName: String, regionName: String, troopsLost: Int, turn: Int): EmergencyCouncil =
        EmergencyCouncil(
            id = "emergency_${turn}_${invaderName.hashCode()}",
            invaderName = invaderName,
            regionName = regionName,
            troopsLost = troopsLost.coerceAtLeast(0),
            calledTurn = turn,
            favor = 0,
            against = 0
        )

    /** 긴급 방어 안건 표결 — 플레이어 의결권은 찬성 */
    fun vote(council: EmergencyCouncil, opposingVotes: Int, ourVotes: Int): EmergencyCouncil {
        if (council.resolved) return council
        val favor = council.favor + ourVotes.coerceAtLeast(1) + 2
        val against = opposingVotes.coerceAtLeast(0)
        val passed = favor > against
        val message = buildString {
            append("[긴급 의회 · 방어 결의] 찬성 $favor : 반대 $against → ")
            append(if (passed) "가결" else "부결")
            append(
                if (passed) " — 지역 리더 연합이 방어 태세에 들어갑니다(방어 +12%, 2절기)."
                else " — 리더들이 각자 대응하기로 했습니다(방어 보너스 없음)."
            )
        }
        return council.copy(
            favor = favor,
            against = against,
            ourVotes = ourVotes.coerceAtLeast(1),
            resolved = true,
            passed = passed,
            message = message
        )
    }

    /**
     * 방어 도움 요청 — 우호 리더만 응한다. 외교력·명성이 높을수록 많이 보낸다.
     * 상대 병력의 8~15%를 보내며, 최대 3명까지 응답한다.
     */
    fun requestAid(
        council: EmergencyCouncil,
        sources: List<AidSource>,
        diplomacy: Int,
        prestige: Int
    ): EmergencyCouncil {
        val friendly = sources.filter { it.friendly && it.troops > 60 }
            .sortedByDescending { it.troops }
            .take(3)
        if (friendly.isEmpty()) {
            return council.copy(
                message = "[방어 지원] 응답한 우호 리더가 없습니다 — 외교력·명성을 올리거나 조약을 맺어 두세요."
            )
        }
        val ratio = (0.08 + (diplomacy + prestige) / 1200.0).coerceIn(0.08, 0.15)
        val aids = friendly.map { source ->
            val troops = (source.troops * ratio).toInt().coerceAtLeast(30)
            DefenseAid(
                leaderName = source.leaderName,
                troops = troops,
                direction = AidDirection.REQUESTED,
                note = "${council.invaderName} 방어 지원"
            )
        }
        val total = aids.sumOf { it.troops }
        return council.copy(
            aids = council.aids.filter { it.direction == AidDirection.REQUESTED } + aids,
            message = "[방어 지원] 우호 리더 ${aids.size}명이 지원군 ${"%,d".format(total)}명을 보냈습니다 — " +
                aids.joinToString(", ") { "${it.leaderName} ${"%,d".format(it.troops)}" }
        )
    }

    /** 다른 리더를 도우러 보내는 병력 — 내 병력의 10%(최대 800) */
    fun outgoingTroops(myTroops: Int): Int = (myTroops * 0.10).toInt().coerceIn(30, 800)

    /** 지원 병력을 보낸 결과 — 침공을 막아 병합을 되돌린다 */
    fun answerCall(call: AidCall, myTroops: Int): Pair<AidCall, Int> {
        if (call.answered) return call to 0
        val sent = outgoingTroops(myTroops)
        val message = "[방어 지원] ${call.victimName} 리더에게 ${"%,d".format(sent)}명을 보냈습니다 — " +
            "${call.attackerName}의 ${call.kind.label}을(를) 막고 명성 +5 · 외교 +6."
        return call.copy(answered = true, result = message) to sent
    }

    /** 절기가 지나면 남은 턴을 줄이고, 끝나면 null */
    fun decay(council: EmergencyCouncil): EmergencyCouncil? {
        if (!council.resolved) return council
        val next = council.copy(turnsLeft = council.turnsLeft - 1)
        return if (next.turnsLeft <= 0) null else next
    }
}
