package com.game.slg.core.region

/**
 * 리더 연합 출진 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-27, v0.11.0):
 *  *"의회 전쟁 안건이 가결되면 안건 카드에서 바로 리더 연합 출진 버튼을 눌러,
 *    내 지역·편입 리더들의 병력과 가결된 명분을 한눈에 확인하고 곧장 전투로 이어지게 한다."*
 *
 * · 가결(`WarAgenda.active`)된 안건만 출진할 수 있다.
 * · 출진 병력 = 내 병력 + 편입 리더들이 내놓는 병력(각 병력의 35%, 최대 6명).
 * · 사상자는 출진 병력 비율대로 나눠 진다 — 연합이 함께 싸우므로 내 병력 손실이 줄어든다.
 * · 명분([CasusBelli])은 출진 1회에 적용되고 소모된다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 편입 리더 1명이 연합 출진에 내놓는 병력 */
data class CoalitionMuster(
    val leaderName: String,
    val troops: Int,
    val note: String = ""
)

/** 가결된 전쟁 안건으로 편성한 연합 출진 계획 */
data class CoalitionWarPlan(
    val agendaId: String,
    val kind: WarTargetKind,
    val regionCode: String,
    val regionName: String,
    val targetName: String,
    /** 내 정규·용병 병력 */
    val ownTroops: Int,
    val musters: List<CoalitionMuster>,
    val enemyName: String,
    val enemyTroops: Int,
    val casusBelli: CasusBelli
) {
    val coalitionTroops: Int get() = musters.sumOf { it.troops }

    val totalTroops: Int get() = ownTroops + coalitionTroops

    val leaderCount: Int get() = musters.size

    val targetLabel: String get() = if (regionName.isBlank()) targetName else regionName

    /** 적 병력 대비 배율 — 1.0 이상이면 우세 */
    val ratio: Float get() = if (enemyTroops <= 0) 9f else totalTroops.toFloat() / enemyTroops

    val oddsLabel: String
        get() = when {
            enemyTroops <= 0 -> "저항 없음"
            ratio >= 1.6f -> "압도적 우세"
            ratio >= 1.1f -> "우세"
            ratio >= 0.9f -> "호각"
            else -> "열세 — 명분·연합에 기대야 합니다"
        }

    val headline: String
        get() = "[리더 연합 출진] $targetLabel · ${kind.label} — " +
            "연합 병력 ${"%,d".format(totalTroops)}명(내 ${"%,d".format(ownTroops)} + " +
            "리더 ${leaderCount}명 ${"%,d".format(coalitionTroops)})"

    /** 확인 카드에 한 줄씩 올리는 근거 */
    fun breakdown(): List<String> = buildList {
        add("내 병력 ${"%,d".format(ownTroops)}명")
        if (musters.isEmpty()) {
            add("합류한 편입 리더 없음 — 영지를 편입하면 연합 병력이 붙습니다")
        } else {
            add("편입 리더 ${musters.size}명 합류: " +
                musters.joinToString(", ") { "${it.leaderName} ${"%,d".format(it.troops)}" })
        }
        add("적 ${enemyName} ${"%,d".format(enemyTroops)}명 · ${oddsLabel}")
        add(
            if (casusBelli.isEmpty) "가결된 명분 없음 — 보너스 없이 출진합니다"
            else "의회 명분 ${casusBelli.label}"
        )
    }
}

object CoalitionWarEngine {

    /** 편입 리더가 내놓는 병력 비율 (%) */
    const val MUSTER_RATE_PERCENT = 35

    /** 한 번의 출진에 합류하는 편입 리더 수 상한 */
    const val MAX_MUSTER_LEADERS = 6

    /** 합류 병력의 하한 — 너무 적은 병력은 표시만 된다 */
    const val MIN_MUSTER_TROOPS = 30

    /** 편입 리더 병력 → 합류 병력. 병력이 많은 리더부터 최대 6명. */
    fun mustersOf(leaders: List<Pair<String, Int>>): List<CoalitionMuster> =
        leaders.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(MAX_MUSTER_LEADERS)
            .map { (name, troops) ->
                CoalitionMuster(
                    leaderName = name,
                    troops = (troops * MUSTER_RATE_PERCENT / 100)
                        .coerceAtLeast(MIN_MUSTER_TROOPS)
                        .coerceAtMost(troops),
                    note = "편입 리더 병력 $MUSTER_RATE_PERCENT%"
                )
            }

    /** 출진 계획 편성 — 가결·미소모 안건이 아니면 명분은 비어 있다 */
    fun plan(
        agenda: WarAgenda,
        ownTroops: Int,
        leaders: List<Pair<String, Int>>,
        enemyName: String,
        enemyTroops: Int
    ): CoalitionWarPlan = CoalitionWarPlan(
        agendaId = agenda.id,
        kind = agenda.kind,
        regionCode = agenda.regionCode,
        regionName = agenda.regionName,
        targetName = agenda.targetName,
        ownTroops = ownTroops.coerceAtLeast(0),
        musters = mustersOf(leaders),
        enemyName = enemyName.ifBlank { "상대" },
        enemyTroops = enemyTroops.coerceAtLeast(0),
        casusBelli = agenda.casusBelli
    )

    /**
     * 사상자를 출진 병력 비율대로 나눈다.
     * @return (내가 부담하는 사상자, 편입 리더별 사상자 — [musters] 순서)
     */
    fun splitLosses(
        losses: Int,
        ownTroops: Int,
        musters: List<CoalitionMuster>
    ): Pair<Int, List<Int>> {
        val safeLosses = losses.coerceAtLeast(0)
        val coalition = musters.sumOf { it.troops }
        val total = ownTroops + coalition
        if (safeLosses == 0 || total <= 0 || coalition <= 0) return safeLosses to musters.map { 0 }
        val perLeader = musters.map { (safeLosses.toLong() * it.troops / total).toInt() }
        val shared = perLeader.sum()
        return (safeLosses - shared).coerceAtLeast(0) to perLeader
    }

    /** 출진 결과 한 줄 */
    fun resultMessage(
        plan: CoalitionWarPlan,
        won: Boolean,
        enemyLosses: Int,
        ownLosses: Int,
        loot: Int
    ): String = buildString {
        append("[연합 출진] ${plan.targetLabel} · ${plan.enemyName} — ")
        append(if (won) "승리" else "패퇴")
        append(" · 연합 ${"%,d".format(plan.totalTroops)}명(리더 ${plan.leaderCount}명 합류)")
        append(" · 적 사상자 ${"%,d".format(enemyLosses)} · 아군 사상자 ${"%,d".format(ownLosses)}")
        if (loot > 0) append(" · 노획 ${"%,d".format(loot)}G")
        if (!plan.casusBelli.isEmpty) append(" · 명분 ${plan.casusBelli.label} 소모")
    }
}
