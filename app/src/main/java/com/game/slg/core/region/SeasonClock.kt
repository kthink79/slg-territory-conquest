package com.game.slg.core.region

/**
 * 지속 시간 흐름 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-25 07:45):
 *  *"수동 '절기' 버튼으로만 멈춰 있는 턴제에서 벗어나 시간이 계속 흘러가는 구조(실시간 경과 기반
 *   자동 절기 진행)로 개선한다. (기존 수동 절기/시간 배속 등 UI 호환 고려)
 *   사용자가 접속하지 않은 동안에도(오프라인 경과 시간 계산 등) AI가 대신 내정·방어·기본 운영을
 *   이어서 진행하도록 구현한다."*
 *
 * 규칙
 *  · 절기 1회는 [seasonMinutes] 분마다 자동으로 흐른다(기본 30분, 10/30/60/180분 선택).
 *  · 앱을 닫아 둔 동안의 경과도 같은 시계로 계산해, 다시 열면 그만큼 절기를 한꺼번에 진행한다.
 *  · [autoAdvance] 가 꺼져 있으면 시계는 멈춘 것으로 보고 수동 '절기' 버튼만 쓴다(기존 방식 호환).
 *  · 한 번에 따라잡는 절기는 [MAX_CATCH_UP] 회로 제한한다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */
data class SeasonClock(
    /** 절기 1회에 걸리는 시간(분) */
    val seasonMinutes: Int = DEFAULT_SEASON_MINUTES,
    /** 마지막으로 시계를 맞춘 시각 (epoch millis) — 0 이면 아직 시작 전 */
    val lastTickMillis: Long = 0L,
    /** 자동 진행 켜기/끄기 */
    val autoAdvance: Boolean = true,
    /** 미접속 동안 AI가 내정·방어를 대신 운영하는지 */
    val aiAutopilot: Boolean = true
) {
    val periodMillis: Long get() = seasonMinutes.coerceAtLeast(1) * 60_000L

    /** 다음 절기까지 남은 비율 0.0~1.0 */
    fun progress(nowMillis: Long): Float {
        if (!autoAdvance) return 0f
        if (lastTickMillis <= 0L) return 0f
        val elapsed = (nowMillis - lastTickMillis).coerceAtLeast(0L)
        return (elapsed % periodMillis).toFloat() / periodMillis.toFloat()
    }

    /** 다음 절기까지 남은 시간 "12:34" */
    fun countdownLabel(nowMillis: Long): String {
        if (!autoAdvance) return "일시정지"
        if (lastTickMillis <= 0L) return timeLabel(seasonMinutes * 60_000L)
        val elapsed = (nowMillis - lastTickMillis).coerceAtLeast(0L)
        val remain = periodMillis - (elapsed % periodMillis)
        return timeLabel(remain)
    }

    val speedLabel: String get() = if (autoAdvance) "자동 · ${perLabel()}" else "수동"

    private fun perLabel(): String = when {
        seasonMinutes >= 60 -> "${seasonMinutes / 60}시간/절기"
        else -> "${seasonMinutes}분/절기"
    }

    private fun timeLabel(millis: Long): String {
        val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    companion object {
        const val DEFAULT_SEASON_MINUTES = 30

        /** 고를 수 있는 시간 배속 — 기존 '절기' 버튼과 함께 쓴다 */
        val SPEED_MINUTES: List<Int> = listOf(10, 30, 60, 180)

        fun start(nowMillis: Long, minutes: Int = DEFAULT_SEASON_MINUTES): SeasonClock =
            SeasonClock(seasonMinutes = minutes, lastTickMillis = nowMillis)
    }
}

/** 한 번의 시계 점검 결과 */
data class SeasonTick(
    /** 지금 진행해야 할 절기 수 */
    val due: Int,
    /** 마지막 점검 이후 지난 시간(밀리초) */
    val awayMillis: Long,
    /** 새 시계 (lastTickMillis 갱신) */
    val clock: SeasonClock,
    /** 오프라인(장시간 미접속)으로 판정됐는지 */
    val offline: Boolean
) {
    val hasDue: Boolean get() = due > 0
}

object SeasonClockEngine {

    /** 한 번에 따라잡을 수 있는 최대 절기 수 */
    const val MAX_CATCH_UP = 48

    /** 이 시간 이상 비어 있으면 '미접속'으로 보고 AI 대행 리포트를 남긴다 */
    const val OFFLINE_MILLIS = 5 * 60_000L

    /**
     * 시계를 점검한다.
     *
     * · [SeasonClock.autoAdvance] 가 꺼져 있으면 절기를 진행하지 않고 시계만 현재로 맞춘다.
     * · 지난 시간이 [SeasonClock.periodMillis] 를 넘으면 그만큼 절기를 진행하고,
     *   남은 나머지는 다음 절기 진행도로 넘긴다(절기 중간에서 시작하지 않는다).
     */
    fun tick(clock: SeasonClock, nowMillis: Long, autoAdvance: Boolean = clock.autoAdvance): SeasonTick {
        if (clock.lastTickMillis <= 0L) {
            val fresh = clock.copy(lastTickMillis = nowMillis)
            return SeasonTick(due = 0, awayMillis = 0L, clock = fresh, offline = false)
        }
        val elapsed = (nowMillis - clock.lastTickMillis).coerceAtLeast(0L)
        if (!autoAdvance) {
            // 수동 모드 — 시간은 흐르지 않는다. 시계만 현재로 맞춰 둔다.
            return SeasonTick(due = 0, awayMillis = elapsed, clock = clock.copy(lastTickMillis = nowMillis), offline = false)
        }
        val period = clock.periodMillis
        val rawDue = (elapsed / period).toInt()
        val due = rawDue.coerceAtMost(MAX_CATCH_UP)
        val consumed = due.toLong() * period
        val nextLast = nowMillis - (elapsed - consumed)
        val offline = due > 0 && elapsed >= OFFLINE_MILLIS
        return SeasonTick(
            due = due,
            awayMillis = elapsed,
            clock = clock.copy(lastTickMillis = nextLast),
            offline = offline
        )
    }

    /** "2시간 10분" 처럼 사람이 읽는 경과 시간 */
    fun awayLabel(millis: Long): String {
        val totalMinutes = (millis / 60_000L).coerceAtLeast(0L)
        if (totalMinutes < 1) return "${(millis / 1000L).coerceAtLeast(0L)}초"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
    }

    /**
     * 미접속 AI 대행 리포트 — AI가 대신 운영한 절기 수와 재원·병력 변화를 한 문단으로.
     */
    fun offlineSummary(
        seasons: Int,
        awayMillis: Long,
        goldBefore: Int,
        goldAfter: Int,
        foodBefore: Int,
        foodAfter: Int,
        troopsBefore: Int,
        troopsAfter: Int,
        aiNews: List<String>,
        emergency: EmergencyCouncil?
    ): String = buildString {
        append("🤖 미접속 ${awayLabel(awayMillis)} — AI가 절기 ${seasons}회를 대신 운영했습니다.")
        append("\n재원 변화 · 자금 ${signed(goldAfter - goldBefore)}G · 식량 ${signed(foodAfter - foodBefore)}석 · 병력 ${signed(troopsAfter - troopsBefore)}명")
        val news = aiNews.firstOrNull { it.contains("AI 동향") }
        if (news != null) append("\n$news")
        if (emergency != null && emergency.troopsLost > 0) {
            append("\n⚔ ${emergency.invaderName}의 침공으로 병력 ${"%,d".format(emergency.troopsLost)}명을 잃었습니다 — 긴급 의회가 열렸습니다.")
        }
        append("\nAI는 내정·모병·방어 기본 운영을 이어 갔습니다. 확인 후 다음 절기를 조정하세요.")
    }

    private fun signed(value: Int): String = if (value >= 0) "+%,d".format(value) else "-%,d".format(-value)
}
