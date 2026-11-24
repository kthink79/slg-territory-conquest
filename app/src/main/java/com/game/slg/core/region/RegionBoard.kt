package com.game.slg.core.region

import com.game.slg.core.empire.TerritoryStatus

/**
 * 세부맵에 표시하는 리더의 지위.
 *
 * 사용자 요청(2026-11-24): *"지역내 각 리더의 배치와 현황을 볼 수 있는 세부맵… 내가 3개의 리더를
 * 병합시키거나 종속시켰다면 그런 것도 표현이 되어야 하고"*
 *  → 내 세력(★), 독립(🏴), 그리고 우리 연방·다른 리더에게 묶인 형태를 한눈에 구분한다.
 */
enum class LeaderStatus(val label: String, val icon: String, val accentHex: Long) {
    PLAYER("내 세력", "★", 0xFFE2B755),
    INDEPENDENT("독립", "🏴", 0xFF7FA4D6),

    /** 다른 AI 리더에게 병합됨 */
    MERGED("병합", "⚔", 0xFFE57373),

    /** 다른 AI 리더에게 종속됨 */
    SUBORDINATED("종속", "🏳", 0xFFB39DDB),

    /** 우리 연방 직할 — 전투 흡수·교전 강제 편입 */
    DIRECT("직할", "🏙", 0xFF81C784),

    /** 우리 연방 속국 — 첩자·압박·경제 종속 */
    VASSAL("속국", "🤝", 0xFF81C784),

    /** 우리 연방 자치령 — 연방 결성·친선 합류 */
    AUTONOMOUS("자치령", "🏘", 0xFF90A4AE);

    /** 우리 세력으로 묶였는지 */
    val isOurs: Boolean get() = this == PLAYER || this == DIRECT || this == VASSAL || this == AUTONOMOUS

    /** 다른 세력 아래로 들어갔는지 */
    val isBound: Boolean get() = this == MERGED || this == SUBORDINATED
}

/** 세부맵에 넣을 리더 1명 — ui.LordView · AiLordProfile 에서 옮겨 담는 순수 값 */
data class RegionLeaderSeed(
    val id: String,
    val name: String,
    val stronghold: String,
    val traitLabel: String,
    val traitIcon: String,
    val troops: Int,
    val gold: Int,
    val food: Int,
    val prestige: Int,
    val isPlayer: Boolean = false,
    /** 우리 연방 편입 형태 — null 이면 우리 세력이 아니다 */
    val federation: TerritoryStatus? = null,
    /** AI 리더끼리의 병합·종속 */
    val aiBond: LeaderBond? = null,
    val masterId: String? = null,
    /** 편입 경위·활동 한 줄 */
    val note: String = ""
)

/** 세부맵 카드 1장 */
data class RegionLeaderInfo(
    val id: String,
    val name: String,
    val stronghold: String,
    val traitLabel: String,
    val traitIcon: String,
    val troops: Int,
    val gold: Int,
    val food: Int,
    val prestige: Int,
    val status: LeaderStatus,
    /** 병합·종속 대상(우두머리) 리더 id */
    val masterId: String? = null,
    val masterName: String? = null,
    /** 카드 아래 한 줄 — 활동·편입 경위·관계 */
    val note: String = ""
)

/** 세부맵 격자 위 자리 — column(가로) · row(세로) */
data class LeaderSeat(val id: String, val column: Int, val row: Int)

/** 우두머리 → 예속 리더 연결선 */
data class RegionBondLine(
    val masterId: String,
    val servantId: String,
    val label: String,
    val kind: LeaderBond
)

/**
 * 지역 1곳의 세부맵 — 리더 배치(격자) · 병합/종속 연결선 · 재화/병력.
 *
 * [live] 가 true 면 지금 운영 중인 내 지역(실시간), false 면 아직 정찰하지 않은 다른 지역(추정 명부)이다.
 */
data class RegionBoard(
    val regionCode: String,
    val regionName: String,
    val leaders: List<RegionLeaderInfo>,
    val seats: List<LeaderSeat>,
    val bonds: List<RegionBondLine>,
    val columns: Int,
    val rows: Int,
    val live: Boolean
) {
    val totalCount: Int get() = leaders.size

    val playerCount: Int get() = leaders.count { it.status == LeaderStatus.PLAYER }

    /** 우리 연방으로 묶인 리더 수 (플레이어 제외) */
    val ourFederationCount: Int
        get() = leaders.count { it.status.isOurs && it.status != LeaderStatus.PLAYER }

    val mergedCount: Int get() = leaders.count { it.status == LeaderStatus.MERGED }

    val subordinatedCount: Int get() = leaders.count { it.status == LeaderStatus.SUBORDINATED }

    val independentCount: Int get() = leaders.count { it.status == LeaderStatus.INDEPENDENT }

    /** 병합·종속으로 묶인 전체 수 — 화면 배지 */
    val boundCount: Int get() = mergedCount + subordinatedCount

    val totalTroops: Int get() = leaders.sumOf { it.troops }

    fun seatOf(id: String): LeaderSeat? = seats.firstOrNull { it.id == id }

    fun leaderOf(id: String): RegionLeaderInfo? = leaders.firstOrNull { it.id == id }

    /** 관계 요약 — "직할 2 · 속국 1 · 병합 3 · 종속 2" */
    fun relationSummary(): String = buildList {
        val direct = leaders.count { it.status == LeaderStatus.DIRECT }
        val vassal = leaders.count { it.status == LeaderStatus.VASSAL }
        val autonomous = leaders.count { it.status == LeaderStatus.AUTONOMOUS }
        if (direct > 0) add("직할 $direct")
        if (vassal > 0) add("속국 $vassal")
        if (autonomous > 0) add("자치령 $autonomous")
        if (mergedCount > 0) add("병합 $mergedCount")
        if (subordinatedCount > 0) add("종속 $subordinatedCount")
        if (independentCount > 0) add("독립 $independentCount")
    }.joinToString(" · ")
}

/**
 * 세부맵 배치 계산 — 안드로이드·Compose 의존성 없이 JVM에서 검증한다.
 *
 * 배치 규칙
 *  1. 내 세력(★)이 0번 자리(맨 위 왼쪽)에 앉는다.
 *  2. 우리 연방으로 묶인 리더(직할·속국·자치령)가 그 옆에 붙는다 — "내가 3개를 병합·종속시켰다"가 한눈에 보이게.
 *  3. AI 우두머리와 그에 병합·종속된 리더는 붙여 놓는다(연결선이 짧아진다).
 *  4. 나머지 독립 리더가 뒤를 잇는다.
 *
 * 리더가 48명이면 4열 × 13행이 되어 좌우·상하 스크롤이 모두 필요하다.
 */
object RegionBoardBuilder {

    /** 한 줄에 놓는 카드 수 — 폰에서 2장이 보이고 좌우로 넘긴다 */
    const val COLUMNS = 4

    fun build(
        regionCode: String,
        regionName: String,
        seeds: List<RegionLeaderSeed>,
        live: Boolean = true
    ): RegionBoard {
        // 1) 지위 계산
        val infos = seeds.map { seed -> toInfo(seed, seeds) }

        // 2) 배치 순서
        val byId = infos.associateBy { it.id }
        val ordered = mutableListOf<RegionLeaderInfo>()
        val added = mutableSetOf<String>()

        fun addGroup(leader: RegionLeaderInfo) {
            if (!added.add(leader.id)) return
            ordered += leader
            // 이 리더에 묶인 세력을 바로 뒤에 붙인다
            infos.filter { it.masterId == leader.id }.forEach { addGroup(it) }
        }

        infos.firstOrNull { it.status == LeaderStatus.PLAYER }?.let { addGroup(it) }
        infos.filter { it.status.isOurs && it.status != LeaderStatus.PLAYER }.forEach { addGroup(it) }
        infos.filter { info ->
            info.masterId == null && infos.any { it.masterId == info.id }
        }.forEach { addGroup(it) }
        infos.forEach { addGroup(it) }

        // 3) 격자 자리
        val seats = ordered.mapIndexed { index, leader ->
            LeaderSeat(
                id = leader.id,
                column = index % COLUMNS,
                row = index / COLUMNS
            )
        }
        val rows = ((ordered.size + COLUMNS - 1) / COLUMNS).coerceAtLeast(1)

        // 4) 연결선 — 우두머리가 실제로 존재할 때만
        val bonds = ordered.mapNotNull { leader ->
            val masterId = leader.masterId ?: return@mapNotNull null
            val master = byId[masterId] ?: return@mapNotNull null
            val kind = when (leader.status) {
                LeaderStatus.MERGED, LeaderStatus.DIRECT -> LeaderBond.MERGE
                LeaderStatus.SUBORDINATED, LeaderStatus.VASSAL -> LeaderBond.SUBORDINATION
                else -> null
            }
            RegionBondLine(
                masterId = masterId,
                servantId = leader.id,
                label = "${leader.status.label} · ${master.name}",
                kind = kind ?: LeaderBond.SUBORDINATION
            )
        }

        return RegionBoard(
            regionCode = regionCode,
            regionName = regionName,
            leaders = ordered,
            seats = seats,
            bonds = bonds,
            columns = COLUMNS,
            rows = rows,
            live = live
        )
    }

    private fun toInfo(seed: RegionLeaderSeed, all: List<RegionLeaderSeed>): RegionLeaderInfo {
        val status = when {
            seed.isPlayer -> LeaderStatus.PLAYER
            seed.federation == TerritoryStatus.DIRECT -> LeaderStatus.DIRECT
            seed.federation == TerritoryStatus.VASSAL -> LeaderStatus.VASSAL
            seed.federation == TerritoryStatus.AUTONOMOUS -> LeaderStatus.AUTONOMOUS
            seed.aiBond == LeaderBond.MERGE -> LeaderStatus.MERGED
            seed.aiBond == LeaderBond.SUBORDINATION -> LeaderStatus.SUBORDINATED
            else -> LeaderStatus.INDEPENDENT
        }
        val masterId = when {
            seed.isPlayer -> null
            status.isOurs -> seed.masterId ?: all.firstOrNull { it.isPlayer }?.id
            status.isBound -> seed.masterId
            else -> seed.masterId
        }
        val masterName = masterId?.let { id -> all.firstOrNull { it.id == id }?.name }
        val note = buildString {
            if (seed.note.isNotBlank()) append(seed.note)
            if (status.isBound && masterName != null) {
                if (isNotEmpty()) append(" · ")
                append("${masterName} 리더 ${status.label}")
            }
        }
        return RegionLeaderInfo(
            id = seed.id,
            name = seed.name,
            stronghold = seed.stronghold,
            traitLabel = seed.traitLabel,
            traitIcon = seed.traitIcon,
            troops = seed.troops.coerceAtLeast(0),
            gold = seed.gold.coerceAtLeast(0),
            food = seed.food.coerceAtLeast(0),
            prestige = seed.prestige.coerceIn(0, 100),
            status = status,
            masterId = if (status == LeaderStatus.PLAYER) null else masterId,
            masterName = masterName,
            note = note
        )
    }
}
