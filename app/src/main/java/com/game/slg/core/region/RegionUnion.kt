package com.game.slg.core.region

/**
 * 지역 연합 · 스폰 보호 — 《단일 연방의 황혼》
 *
 * 사용자 요청: *"게임을 시작해 지역을 선택하면 초기 7턴 스폰 타임 동안 같은 지역 내
 * 모든 AI 리더와 자동 '지역 연합' 상태를 유지해 서로 공격하지 못하게 하고,
 * 외부 인접 지역에서도 신규 유저의 거점을 공격·점령할 수 없도록 보호막을 적용해줘."*
 *
 * 두 장치를 함께 둔다.
 *  ① 지역 연합 — 같은 지역(읍·면·동) 안에서는 우리끼리 교전하지 않는다.
 *  ② 스폰 보호막 — 보호 기간에는 외부 구역의 침공이 방어선에서 막힌다.
 *
 * 턴은 절기 진행(1회 = 1턴)으로 줄어들고, 0이 되면 연합과 보호막이 함께 풀린다.
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */
data class RegionUnion(
    val regionCode: String = "",
    val regionName: String = "",
    val remainingTurns: Int = DEFAULT_TURNS
) {
    val active: Boolean get() = remainingTurns > 0

    /** 상단·외교 화면에 그대로 보이는 1줄 표시 */
    val statusLabel: String
        get() = if (active) {
            "🛡️ 지역 연합 · 스폰 보호 ${remainingTurns}턴 남음"
        } else {
            "⚔ 지역 연합 종료 — 교전 가능"
        }

    /** 짧은 칩 문구 */
    val chipLabel: String get() = if (active) "🛡️ 보호 ${remainingTurns}턴" else "⚔ 교전 가능"

    companion object {
        /** 스폰 보호 기간 (턴) */
        const val DEFAULT_TURNS = 7
    }
}

/** 공격이 어디를 향하는지 */
enum class AttackScope(val label: String) {
    SAME_REGION("지역 내"),
    ADJACENT("인접 구역"),
    EXTERNAL("외부 구역")
}

/** 누가 누구를 치는지 */
enum class AttackSide(val label: String) {
    EXPEDITION("대외 원정"),
    INVASION("외부 침공")
}

/** 공격 허가 판정 */
enum class AttackVerdict(val allowed: Boolean, val label: String) {
    ALLOWED(true, "공격 가능"),
    BLOCKED_UNION(false, "지역 연합 — 지역 내 교전 금지"),
    BLOCKED_SHIELD(false, "스폰 보호막 — 외부 침공 차단")
}

object RegionUnionEngine {

    /** 지역 선택·인계 직후 연합을 연다 */
    fun open(regionCode: String, regionName: String, turns: Int = RegionUnion.DEFAULT_TURNS): RegionUnion =
        RegionUnion(regionCode = regionCode, regionName = regionName, remainingTurns = turns.coerceAtLeast(0))

    /**
     * 공격 허가 판정.
     *
     * · 지역 연합 기간에는 같은 지역 안의 교전(내보내기·받기 모두)이 금지된다.
     * · 스폰 보호막 기간에는 외부 구역의 침공이 차단된다. 내보내는 대외 원정은 허용한다.
     * · 연합이 끝나면 모두 허용된다.
     */
    fun decide(union: RegionUnion, scope: AttackScope, side: AttackSide): AttackVerdict {
        if (!union.active) return AttackVerdict.ALLOWED
        return when {
            scope == AttackScope.SAME_REGION -> AttackVerdict.BLOCKED_UNION
            side == AttackSide.INVASION -> AttackVerdict.BLOCKED_SHIELD
            else -> AttackVerdict.ALLOWED
        }
    }

    /** 지도·명부에 붙는 같은 지역 리더의 관계 이름 */
    fun relationLabel(union: RegionUnion, previous: String): String =
        if (union.active) "지역 연합" else previous

    /** 절기 1회가 지날 때 턴을 하나 줄인다. 0이 된 순간의 안내 문구를 함께 돌려준다. */
    fun onTurnPassed(union: RegionUnion): Pair<RegionUnion, String?> {
        if (!union.active) return union to null
        val next = union.copy(remainingTurns = union.remainingTurns - 1)
        val message = if (!next.active) {
            "[지역 연합 종료] 스폰 보호가 끝났습니다. 같은 지역 리더들과의 교전과 외부 침공 대응이 열립니다."
        } else {
            "[지역 연합] 스폰 보호 ${next.remainingTurns}턴 남음 — 지역 내 교전이 계속 금지됩니다."
        }
        return next to message
    }

    /** 보호막이 실제로 침공을 막았을 때의 안내 */
    fun shieldRepelMessage(union: RegionUnion): String =
        "🛡️ 스폰 보호막 — 외부 침공이 지역 연합 방어선에서 막혔습니다. 사상자 0. (남은 보호 ${union.remainingTurns}턴)"

    /** 지역 연합 기간에 지역 내 공격을 시도했을 때의 안내 */
    fun unionBlockMessage(union: RegionUnion): String =
        "🛡️ 지역 연합 기간입니다 — 같은 지역 리더와는 ${union.remainingTurns}턴 뒤에 교전할 수 있습니다."
}
