package com.game.slg.core.region

import com.game.slg.core.model.TreatyType

/**
 * 지도 인접 지역 패널 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-25 07:45):
 *  *"지도에서 인접 지역(온양2동, 배방읍, 음봉면 등)을 선택하면 하단 패널에 해당 지역과의
 *   전투·외교·전략 메뉴를 바로 띄운다. 인접 지역에 통합 리더(단일 연방 지배자)가 아직 없을
 *   경우 지역 전체가 아니라 그 지역 내 개별 리더 단위로 전투 선택이 가능해야 한다."*
 *
 * 이 파일은 화면과 무관한 순수 계산만 담는다 — 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 인접 지역 패널에서 바로 여는 세 가지 메뉴 */
enum class AdjacentActionKind(val label: String, val icon: String, val detail: String) {
    WAR("전투", "⚔", "통합 리더 또는 개별 리더와 교전"),
    DIPLOMACY("외교", "🤝", "불가침·방위 동맹·조공 협상"),
    STRATEGY("전략", "🧠", "보급로 차단·여론전·정찰 공작")
}

/** 그 지역이 하나의 지배자로 묶였는지, 아직 개별 리더로 흩어져 있는지 */
enum class AdjacentCommand(val label: String, val detail: String) {
    UNIFIED("통합 리더", "단일 연방 지배자가 지역 전체를 통솔합니다 — 지역 단위로 전투·외교합니다."),
    DIVIDED("개별 리더", "통합 지배자가 없습니다 — 지역 전체가 아니라 리더 한 명씩 상대합니다.")
}

/** 패널에 올리는 리더 1명 — 세부맵 카드에서 필요한 값만 옮긴다 */
data class AdjacentLeaderTarget(
    val id: String,
    val name: String,
    val stronghold: String,
    val troops: Int,
    val gold: Int,
    val food: Int,
    val prestige: Int,
    val statusLabel: String,
    /** 다른 리더에게 병합·종속된 상태인지 — 개별 전투 대상에서 뺀다 */
    val bound: Boolean = false,
    /** 우리 연방(직할·속국·자치령)으로 이미 편입된 리더인지 — 전투 대상이 아니다 */
    val ours: Boolean = false,
    val masterName: String? = null
) {
    val troopLabel: String get() = "⚔ %,d".format(troops)
    val relationLabel: String get() =
        when {
            ours -> "우리 연방 · ${statusLabel}"
            bound -> "${masterName ?: "우두머리"} ${statusLabel}"
            else -> statusLabel
        }
}

/** 하단 패널 데이터 — 지역 1곳 */
data class AdjacentRegionPanel(
    val code: String,
    val name: String,
    val relationLabel: String,
    val command: AdjacentCommand,
    val ruler: AdjacentLeaderTarget?,
    val leaders: List<AdjacentLeaderTarget>,
    val unionNote: String = ""
) {
    val unified: Boolean get() = command == AdjacentCommand.UNIFIED

    val totalTroops: Int get() = leaders.sumOf { it.troops }

    /** 전투 대상 — 통합 리더면 그 한 명, 아니면 병합·종속되지 않은 개별 리더 */
    val warTargets: List<AdjacentLeaderTarget>
        get() = if (unified) listOfNotNull(ruler).filterNot { it.ours }
        else leaders.filterNot { it.bound || it.ours }

    val headcountLabel: String
        get() = if (unified) "통합 리더 1명"
        else "개별 리더 ${warTargets.size}명" +
            if (absorbedLeaders.isNotEmpty()) " · 편입 ${absorbedLeaders.size}" else ""

    /** 이미 우리 연합으로 편입된 리더 */
    val absorbedLeaders: List<AdjacentLeaderTarget> get() = leaders.filter { it.ours }

    val headline: String
        get() = if (unified) {
            "${ruler?.name ?: "통합 리더"} 리더가 ${name} 전체를 통솔"
        } else {
            "${name} 리더 ${leaders.size}명이 각자 거점 운영 — 개별 교전"
        }

    /** 전투 버튼 아래 안내 — 왜 지역 단위/개별 단위인지 */
    val warHint: String
        get() = if (unified) {
            "통합 리더를 무너뜨리면 지역 전체가 흔들립니다."
        } else {
            "통합 지배자가 없어 ${warTargets.size}명 중 한 명을 골라 칩니다."
        }

    /** 외교 상대 이름 */
    val diplomacyPartner: String
        get() = if (unified) ruler?.name ?: name else "${name} 리더 연합"

    fun target(id: String): AdjacentLeaderTarget? = leaders.firstOrNull { it.id == id }
}

object AdjacentRegionEngine {

    /**
     * 통합 리더(단일 연방 지배자) 판정.
     *
     * 우두머리 없이 서 있는 리더가 정확히 1명뿐이고, 나머지가 모두 그에게 병합·종속돼 있으면
     * 그를 지역 지배자로 본다. 독립 리더가 둘 이상이면 아직 통합되지 않은 것으로 본다.
     */
    fun unifiedRuler(board: RegionBoard): RegionLeaderInfo? {
        if (board.leaders.size < 2) return board.leaders.firstOrNull()
        val free = board.leaders.filter { it.masterId == null && !it.status.isBound }
        if (free.size != 1) return null
        val ruler = free.first()
        val followers = board.leaders.count { it.masterId == ruler.id }
        return if (followers > 0) ruler else null
    }

    /** [RegionBoard] → 하단 패널 */
    fun panel(
        code: String,
        name: String,
        relationLabel: String,
        board: RegionBoard,
        unionActive: Boolean
    ): AdjacentRegionPanel {
        val rulerInfo = unifiedRuler(board)
        val leaders = board.leaders.map { info ->
            AdjacentLeaderTarget(
                id = info.id,
                name = info.name,
                stronghold = info.stronghold,
                troops = info.troops,
                gold = info.gold,
                food = info.food,
                prestige = info.prestige,
                statusLabel = info.status.label,
                bound = info.status.isBound,
                ours = info.status.isOurs && info.status != LeaderStatus.PLAYER,
                masterName = info.masterName
            )
        }
        val command = if (rulerInfo != null) AdjacentCommand.UNIFIED else AdjacentCommand.DIVIDED
        val note = if (unionActive) "지역 연합 기간 — 같은 지역 안에서만 교전이 막힙니다." else ""
        return AdjacentRegionPanel(
            code = code,
            name = name,
            relationLabel = relationLabel,
            command = command,
            ruler = rulerInfo?.let { info -> leaders.firstOrNull { it.id == info.id } },
            leaders = leaders,
            unionNote = note
        )
    }

    /** 대외 원정 허가 — 인접 지역 원정은 스폰 보호막에 막히지 않는다 */
    fun expeditionVerdict(union: RegionUnion): AttackVerdict =
        RegionUnionEngine.decide(union, AttackScope.ADJACENT, AttackSide.EXPEDITION)

    /**
     * 공동 전투 참여 리더가 주는 전투 보너스(%).
     * 지역 리더 연합으로 함께 나서면 1명당 6%, 최대 30%까지 붙는다.
     */
    fun coalitionBonusPercent(supporters: Int): Int =
        (supporters.coerceAtLeast(0) * 6).coerceAtMost(30)
}

/** 전략 공작 — 인접 지역에 값으로 하는 세 가지 */
enum class AdjacentStrategyOp(
    val id: String,
    val label: String,
    val icon: String,
    val cost: Int,
    val detail: String
) {
    BLOCKADE("blockade", "보급로 차단", "🚧", 320, "적 보급선을 끊어 비축 식량을 태웁니다."),
    RHETORIC("rhetoric", "여론전", "📣", 240, "주민 여론을 돌려 외교·명성을 올립니다."),
    RECON("recon", "정찰 공작", "🛰", 180, "거점 배치와 병력을 확인하고 첩보력을 키웁니다.");

    companion object {
        fun byId(id: String): AdjacentStrategyOp? = entries.firstOrNull { it.id == id }
    }
}

/** 인접 지역에 대한 행동 1건의 결과 — 화면에 그대로 보여 준다 */
data class AdjacentActionResult(
    val success: Boolean,
    val title: String,
    val detail: String,
    val goldDelta: Int = 0,
    val foodDelta: Int = 0,
    val troopDelta: Int = 0,
    val prestigeDelta: Int = 0,
    val moraleDelta: Int = 0,
    val diplomacyDelta: Int = 0,
    val enemyFoodDelta: Int = 0,
    val espionagePowerDelta: Int = 0,
    val intelGained: Boolean = false
) {
    /** 결과 한 줄 — 로그·상태 문구에 쓴다 */
    fun oneLine(): String = "[${if (success) "성공" else "실패"}] $title — $detail"
}

object AdjacentStrategyEngine {

    /**
     * 전략 공작 판정 — 첩보력·영웅 정보 보너스로 성공률이 오른다. 시드가 같으면 결과가 같다.
     */
    fun resolve(
        op: AdjacentStrategyOp,
        regionName: String,
        espionagePower: Int,
        heroIntelBonus: Int,
        prestige: Int,
        seed: Long
    ): AdjacentActionResult {
        val roll = ((seed % 100L) + 100L).toInt() % 100
        val chance = when (op) {
            AdjacentStrategyOp.BLOCKADE -> 35 + espionagePower / 2 + heroIntelBonus
            AdjacentStrategyOp.RHETORIC -> 45 + prestige / 3 + heroIntelBonus
            AdjacentStrategyOp.RECON -> 70 + espionagePower / 4 + heroIntelBonus
        }.coerceIn(20, 95)
        val success = roll < chance
        return when (op) {
            AdjacentStrategyOp.BLOCKADE -> AdjacentActionResult(
                success = success,
                title = if (success) "보급로 차단 성공" else "보급로 차단 실패",
                detail = if (success) {
                    "$regionName 외곽 보급선을 끊었습니다 — 적 비축 식량 -500석, 명성 +3."
                } else {
                    "$regionName 경비대가 순찰을 늘렸습니다 — 자금만 소모되고 보급선은 살아 있습니다."
                },
                goldDelta = -op.cost,
                enemyFoodDelta = if (success) -500 else 0,
                prestigeDelta = if (success) 3 else 0,
                diplomacyDelta = if (success) 0 else -2,
                espionagePowerDelta = if (success) 3 else 0
            )

            AdjacentStrategyOp.RHETORIC -> AdjacentActionResult(
                success = success,
                title = if (success) "여론전 성공" else "여론전 역풍",
                detail = if (success) {
                    "$regionName 주민 여론이 우리 쪽으로 돌아섰습니다 — 외교 +7, 명성 +2, 민심 +2."
                } else {
                    "$regionName 방송이 우리 공작을 폭로했습니다 — 외교 -4, 민심 -2."
                },
                goldDelta = -op.cost,
                diplomacyDelta = if (success) 7 else -4,
                prestigeDelta = if (success) 2 else 0,
                moraleDelta = if (success) 2 else -2
            )

            AdjacentStrategyOp.RECON -> AdjacentActionResult(
                success = success,
                title = if (success) "정찰 완료" else "정찰 발각",
                detail = if (success) {
                    "$regionName 리더 배치·병력·재화를 확인했습니다 — 첩보력 +6."
                } else {
                    "$regionName 순찰대에 정찰조가 발각됐습니다 — 첩보력 -2."
                },
                goldDelta = -op.cost,
                espionagePowerDelta = if (success) 6 else -2,
                intelGained = success
            )
        }
    }
}

object AdjacentDiplomacyEngine {

    fun costOf(type: TreatyType): Int = when (type) {
        TreatyType.NON_AGGRESSION_PACT -> 300
        TreatyType.DEFENSIVE_ALLIANCE -> 600
        TreatyType.VASSAL_TRIBUTARY -> 900
    }

    fun durationOf(type: TreatyType): Int = when (type) {
        TreatyType.NON_AGGRESSION_PACT -> 4
        TreatyType.DEFENSIVE_ALLIANCE -> 3
        TreatyType.VASSAL_TRIBUTARY -> 6
    }

    fun benefitOf(type: TreatyType): String = when (type) {
        TreatyType.NON_AGGRESSION_PACT -> "4턴간 상호 불가침 · 보복전 방지"
        TreatyType.DEFENSIVE_ALLIANCE -> "3턴간 피격 시 상호 참전 · 공동 방어"
        TreatyType.VASSAL_TRIBUTARY -> "매 절기 조공 +100G · 병력 차출"
    }

    /**
     * 인접 지역과의 조약 제안 — 외교력·명성·상대 규모로 성공률이 정해진다.
     * 통합 리더가 있으면 그와, 없으면 지역 리더 연합 전체와 맺는다(그래서 성공률이 낮다).
     */
    fun resolve(
        type: TreatyType,
        partnerName: String,
        unified: Boolean,
        diplomacy: Int,
        prestige: Int,
        leaderCount: Int,
        seed: Long
    ): AdjacentActionResult {
        val roll = ((seed % 100L) + 100L).toInt() % 100
        val base = when (type) {
            TreatyType.NON_AGGRESSION_PACT -> 45
            TreatyType.DEFENSIVE_ALLIANCE -> 30
            TreatyType.VASSAL_TRIBUTARY -> 18
        }
        val spreadPenalty = if (unified) 0 else (leaderCount - 1).coerceAtLeast(0) * 4
        val chance = (base + diplomacy / 2 + prestige / 4 - spreadPenalty).coerceIn(10, 92)
        val success = roll < chance
        val cost = costOf(type)
        return if (success) {
            AdjacentActionResult(
                success = true,
                title = "${typeLabel(type)} 체결",
                detail = "$partnerName 과(와) ${typeLabel(type)}을(를) 맺었습니다 — " +
                    "${benefitOf(type)} · 유효 ${durationOf(type)}턴.",
                goldDelta = -cost,
                diplomacyDelta = if (type == TreatyType.VASSAL_TRIBUTARY) 4 else 8,
                prestigeDelta = if (type == TreatyType.VASSAL_TRIBUTARY) 3 else 2
            )
        } else {
            AdjacentActionResult(
                success = false,
                title = "${typeLabel(type)} 결렬",
                detail = "$partnerName 이(가) 제안을 거절했습니다 — " +
                    (if (unified) "상대 리더의 요구가 큽니다." else "리더마다 이해가 달라 연합 협상이 어렵습니다.") +
                    " 외교 +2.",
                goldDelta = -cost / 4,
                diplomacyDelta = 2
            )
        }
    }

    fun typeLabel(type: TreatyType): String = when (type) {
        TreatyType.NON_AGGRESSION_PACT -> "상호 불가침"
        TreatyType.DEFENSIVE_ALLIANCE -> "상호방위 조약"
        TreatyType.VASSAL_TRIBUTARY -> "속국 조공 조약"
    }
}
