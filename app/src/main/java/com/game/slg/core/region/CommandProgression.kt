package com.game.slg.core.region

/**
 * 지휘 단계별 작전 반경 — 사용자 확정 사항.
 *
 * "초반에는 선택한 지역 기준 일정 범위까지만 영향을 줄 수 있도록 해야겠고
 *  성장, 레벨업 여부에 따라 주변으로 확대 하는 구조로 수정해줘"
 *
 * 탕정면을 예로 들면
 *  Lv.1 탕정면 내부 → Lv.2 인접 읍·면·동(배방읍·음봉면·염치읍)
 *  → Lv.3 연접 구역(온양1·2동, 불당1동…) → Lv.4 시·군 전역 → Lv.6 광역 생활권.
 */
enum class ExpansionTier(
    /** 이 단계가 열리는 지휘 레벨 */
    val level: Int,
    /** 시작 지역에서 허용되는 최대 hop 거리 */
    val maxHop: Int,
    val label: String,
    val detail: String
) {
    HOME_ZONE(
        level = 1,
        maxHop = 0,
        label = "관할 구역",
        detail = "선택한 읍·면·동 내부의 거점만 작전 가능"
    ),
    ADJACENT(
        level = 2,
        maxHop = 1,
        label = "인접 구역",
        detail = "인접한 읍·면·동까지 영향권 확대"
    ),
    CONNECTED(
        level = 3,
        maxHop = 2,
        label = "연접 구역",
        detail = "두 단계 이웃 읍·면·동까지 확장"
    ),
    CITY_WIDE(
        level = 4,
        maxHop = 3,
        label = "시·군 전역",
        detail = "같은 시·군의 주요 읍·면·동 전역"
    ),
    METRO(
        level = 6,
        maxHop = 4,
        label = "광역 생활권",
        detail = "인접 시·군까지 연결되는 광역권"
    ),
    NATIONAL(
        level = 8,
        maxHop = 5,
        label = "전국 연결망",
        detail = "연결된 모든 거점이 작전 범위에 들어옴"
    );

    /** 다음 단계 (마지막이면 null) */
    val next: ExpansionTier? get() = entries.firstOrNull { it.level == this.level + 1 }

    /** 다음 단계까지 필요한 레벨 (마지막이면 null) */
    val nextLevel: Int? get() = next?.level

    companion object {
        /** 레벨에 해당하는 가장 높은 단계 */
        fun forLevel(level: Int): ExpansionTier =
            entries.lastOrNull { it.level <= level } ?: HOME_ZONE

        fun unlockedHop(level: Int): Int = forLevel(level).maxHop
    }
}

/**
 * 지휘관 성장 — 작전·절기·의회 처리로 경험치를 얻어 레벨이 오른다.
 * 레벨이 곧 영향권(맵 해금 범위)이다.
 */
data class CommandProgress(
    val level: Int = 1,
    val xp: Int = 0
) {
    init {
        require(level >= 1) { "지휘 레벨은 1 이상입니다." }
        require(xp >= 0) { "경험치는 음수일 수 없습니다." }
    }

    val tier: ExpansionTier get() = ExpansionTier.forLevel(level)

    /** 지금 열린 최대 hop 거리 */
    val unlockedHop: Int get() = tier.maxHop

    /** 이번 레벨에서 다음 레벨로 가는 데 필요한 경험치 */
    val xpToNextLevel: Int get() = xpForLevel(level)

    /** 다음 레벨까지 남은 경험치 */
    val xpRemaining: Int get() = (xpToNextLevel - xp).coerceAtLeast(0)

    val progressRatio: Float
        get() = (xp.toFloat() / xpToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)

    val isMaxLevel: Boolean get() = level >= MAX_LEVEL

    /** 경험치 획득 — 여러 레벨을 한 번에 넘을 수 있다 */
    fun gainXp(amount: Int): CommandProgress {
        if (amount <= 0) return this
        var newLevel = level
        var newXp = xp + amount
        while (newLevel < MAX_LEVEL && newXp >= xpForLevel(newLevel)) {
            newXp -= xpForLevel(newLevel)
            newLevel += 1
        }
        if (newLevel >= MAX_LEVEL) {
            newLevel = MAX_LEVEL
            newXp = newXp.coerceAtMost(xpForLevel(MAX_LEVEL) - 1)
        }
        return CommandProgress(newLevel, newXp)
    }

    /** 레벨이 올랐는지 (연출용) */
    fun levelUpFrom(before: CommandProgress): Boolean = level > before.level

    /** 다음 해금 안내 한 줄 */
    fun nextUnlockLabel(): String? {
        val next = tier.next ?: return null
        return "Lv.${next.level} ${next.label} — ${next.detail}"
    }

    companion object {
        const val MAX_LEVEL: Int = 10

        /** 레벨 L→L+1 에 필요한 경험치 */
        fun xpForLevel(level: Int): Int = 60 + (level - 1) * 40

        /** 작전 성공 */
        const val XP_OPERATION_WIN: Int = 70

        /** 작전 손실 — 경험은 남는다 */
        const val XP_OPERATION_LOSS: Int = 35

        /** 절기 정산 */
        const val XP_SEASON: Int = 40

        /** 의회 안건 처리 */
        const val XP_COUNCIL: Int = 15

        /** 시설 건설 */
        const val XP_FACILITY: Int = 10

        /** 승격 */
        const val XP_PROMOTION: Int = 60
    }
}
