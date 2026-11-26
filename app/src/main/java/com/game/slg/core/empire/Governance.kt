package com.game.slg.core.empire

/**
 * 편입 영지 세부 통치 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-26):
 *  *"편입된 ManagedTerritory별 세부 통치 메뉴 추가: 세율(낮음/보통/높음), 치안·반란 억제,
 *    징병 할당량 조절, 지역 시설(관아·병영·성벽·상단) 투자, 속국 조공률·지원군 차출 관리."*
 *
 * · 세율·징병 할당량은 매 절기 세수·징병량과 민심에 바로 작용한다.
 * · 치안이 낮고 세율·징병이 높으면 반란 위험이 올라가 민심·병력이 빠진다.
 * · 시설 투자는 1회성이며 영지마다 한 번씩만 지을 수 있다.
 * · 속국은 조공률과 지원군 차출로 운영한다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 세율 3단계 — 높이면 세수가 늘고 민심이 흘러내린다 */
enum class TaxLevel(
    val label: String,
    val icon: String,
    val ratePercent: Int,
    val moraleDrift: Int,
    val riskWeight: Int,
    val order: Int
) {
    LOW("낮음", "🌱", 70, 2, 0, 0),
    NORMAL("보통", "⚖", 100, 0, 8, 1),
    HIGH("높음", "🔥", 130, -3, 22, 2);

    val multiplier: Double get() = ratePercent / 100.0

    companion object {
        fun ofOrder(order: Int): TaxLevel = entries.firstOrNull { it.order == order } ?: NORMAL
        fun byName(name: String?): TaxLevel = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

/** 징병 할당량 3단계 — 높이면 병력이 늘고 민심·반란 위험이 커진다 */
enum class LevyQuota(
    val label: String,
    val icon: String,
    val percent: Int,
    val moraleDrift: Int,
    val riskWeight: Int
) {
    LOW("낮음", "🪶", 40, 1, 0),
    NORMAL("보통", "⚔", 70, 0, 6),
    HIGH("높음", "🔺", 100, -2, 16);

    val multiplier: Double get() = percent / 100.0

    companion object {
        fun byName(name: String?): LevyQuota = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

/** 지역 시설 4종 — 영지마다 1회 투자 */
enum class TerritoryFacility(
    val id: String,
    val label: String,
    val icon: String,
    val cost: Int,
    val detail: String
) {
    OFFICE("office", "관아", "🏛", 600, "세수 +8% · 치안 +6"),
    BARRACKS("barracks", "병영", "🏰", 700, "징병 +15% · 방어 +5"),
    WALL("wall", "성벽", "🧱", 800, "방어 +12"),
    GUILD("guild", "상단", "🏪", 500, "식량 +6% · 세수 +6%");

    companion object {
        fun byId(id: String): TerritoryFacility? = entries.firstOrNull { it.id == id }
    }
}

/** 편입 영지 1곳의 통치 정책 — 저장·화면에 그대로 쓴다 */
data class GovernancePolicy(
    val taxLevel: TaxLevel = TaxLevel.NORMAL,
    /** 치안 0~100 */
    val security: Int = 50,
    val levyQuota: LevyQuota = LevyQuota.NORMAL,
    val facilities: Set<TerritoryFacility> = emptySet(),
    /** 속국·자치령 조공률 (%) 10~60 */
    val tributeRate: Int = 30,
    /** 차출해 본군에 붙인 지원군 수 — 영지 주둔 병력에서 빠져나간다 */
    val supportTroops: Int = 0
) {
    private fun hasFacility(f: TerritoryFacility): Boolean = facilities.contains(f)

    /** 세수 계수 — 세율 × 시설 보정 */
    val taxMultiplier: Double
        get() = taxLevel.multiplier *
            (1.0 + (if (hasFacility(TerritoryFacility.OFFICE)) 0.08 else 0.0) +
                (if (hasFacility(TerritoryFacility.GUILD)) 0.06 else 0.0))

    /** 식량 계수 */
    val foodMultiplier: Double
        get() = 1.0 + (if (hasFacility(TerritoryFacility.GUILD)) 0.06 else 0.0)

    /** 징병 계수 */
    val levyMultiplier: Double
        get() = levyQuota.multiplier * (if (hasFacility(TerritoryFacility.BARRACKS)) 1.15 else 1.0)

    /** 방어 보정 */
    val defenseBonus: Int
        get() = (if (hasFacility(TerritoryFacility.WALL)) 12 else 0) +
            (if (hasFacility(TerritoryFacility.BARRACKS)) 5 else 0) -
            (if (taxLevel == TaxLevel.HIGH) 4 else 0)

    /** 반란 위험 0~100 — 세율·징병·치안의 합성 */
    val rebellionRisk: Int
        get() = (
            taxLevel.riskWeight + levyQuota.riskWeight +
                ((100 - security) * 0.30).toInt() -
                (if (hasFacility(TerritoryFacility.OFFICE)) 4 else 0)
            ).coerceIn(0, 100)

    /** 반란 위험 등급 */
    val riskLabel: String
        get() = when {
            rebellionRisk >= 60 -> "반란 위험"
            rebellionRisk >= 35 -> "불안"
            else -> "안정"
        }

    /** 화면 한 줄 — "보통 · 치안 50 · 징병 보통 · 안정" */
    fun summaryLine(): String =
        "${taxLevel.label} · 치안 $security · 징병 ${levyQuota.label} · $riskLabel"

    /** 저장용 — "NORMAL/50/NORMAL/office,wall/30/0" (영지 저장은 | 로 열을 나누므로 / 를 쓴다) */
    fun encode(): String = listOf(
        taxLevel.name,
        security.toString(),
        levyQuota.name,
        facilities.joinToString(",") { it.id }.ifBlank { "-" },
        tributeRate.toString(),
        supportTroops.toString()
    ).joinToString("/")

    companion object {
        fun decode(raw: String?): GovernancePolicy {
            val p = raw?.split('/') ?: return GovernancePolicy()
            return GovernancePolicy(
                taxLevel = TaxLevel.byName(p.getOrNull(0)),
                security = p.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 100) ?: 50,
                levyQuota = LevyQuota.byName(p.getOrNull(2)),
                facilities = p.getOrNull(3).orEmpty()
                    .split(',')
                    .mapNotNull { TerritoryFacility.byId(it.trim()) }
                    .toSet(),
                tributeRate = p.getOrNull(4)?.toIntOrNull()?.coerceIn(10, 60) ?: 30,
                supportTroops = p.getOrNull(5)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
            )
        }
    }
}

/** 통치 조작 1건의 결과 */
data class GovernanceActionResult(
    val success: Boolean,
    val message: String,
    val policy: GovernancePolicy,
    val goldDelta: Int = 0,
    /** 영지 주둔 병력 변화 (음수면 본군으로 차출) */
    val territoryTroopDelta: Int = 0,
    /** 본군 병력 변화 (차출 시 +) */
    val armyTroopDelta: Int = 0,
    val prestigeDelta: Int = 0,
    val moraleDelta: Int = 0
)

/** 절기 통치 정산 — 편입 영지가 정책대로 돌아간 결과 */
data class GovernanceSeason(
    val taxRatio: Double,
    val foodRatio: Double,
    val levyRatio: Double,
    val securityDelta: Int,
    val moraleDelta: Int,
    val rebellion: Boolean,
    val troopLoss: Int,
    val line: String
)

object GovernanceEngine {

    /** 치안 순찰 비용 */
    const val PATROL_COST = 180

    /** 치안 순찰 1회 상승 */
    const val PATROL_GAIN = 8

    /** 조공률 1단계 (10%) */
    const val TRIBUTE_STEP = 10

    /** 지원군 차출 비율 — 주둔 병력의 25%까지 */
    const val SUPPORT_RATIO = 0.25

    fun setTax(policy: GovernancePolicy, level: TaxLevel): GovernanceActionResult =
        GovernanceActionResult(
            success = true,
            message = "[세율] ${level.label}(${level.ratePercent}%) — " +
                "절기 세수 ×${"%.2f".format(level.multiplier)} · 민심 ${signed(level.moraleDrift)}",
            policy = policy.copy(taxLevel = level)
        )

    fun setLevy(policy: GovernancePolicy, quota: LevyQuota): GovernanceActionResult =
        GovernanceActionResult(
            success = true,
            message = "[징병] ${quota.label}(${quota.percent}%) — 절기 징병 ×${"%.2f".format(quota.multiplier)} · " +
                "민심 ${signed(quota.moraleDrift)}",
            policy = policy.copy(levyQuota = quota)
        )

    /** 치안 순찰 — 자금을 써서 반란 위험을 낮춘다 */
    fun patrol(policy: GovernancePolicy, gold: Int): GovernanceActionResult {
        if (policy.security >= 100) {
            return GovernanceActionResult(false, "[치안] 이미 최고 수준입니다.", policy)
        }
        if (gold < PATROL_COST) {
            return GovernanceActionResult(
                false,
                "[치안] 자금이 부족합니다. (필요 ${PATROL_COST}G / 보유 ${gold}G)",
                policy
            )
        }
        val next = policy.copy(security = (policy.security + PATROL_GAIN).coerceAtMost(100))
        return GovernanceActionResult(
            success = true,
            message = "[치안] 순찰·단속 — 치안 ${policy.security} → ${next.security} · " +
                "반란 위험 ${policy.rebellionRisk} → ${next.rebellionRisk}",
            policy = next,
            goldDelta = -PATROL_COST
        )
    }

    /** 지역 시설 투자 — 영지마다 1회 */
    fun invest(policy: GovernancePolicy, facilityId: String, gold: Int): GovernanceActionResult {
        val facility = TerritoryFacility.byId(facilityId)
            ?: return GovernanceActionResult(false, "[시설] 알 수 없는 시설입니다.", policy)
        if (policy.facilities.contains(facility)) {
            return GovernanceActionResult(false, "[시설] ${facility.label}은(는) 이미 지었습니다.", policy)
        }
        if (gold < facility.cost) {
            return GovernanceActionResult(
                false,
                "[시설] 자금이 부족합니다. (필요 ${facility.cost}G / 보유 ${gold}G)",
                policy
            )
        }
        val next = policy.copy(facilities = policy.facilities + facility)
        return GovernanceActionResult(
            success = true,
            message = "[시설] ${facility.icon} ${facility.label} 준공 — ${facility.detail} (${facility.cost}G)",
            policy = next,
            goldDelta = -facility.cost
        )
    }

    /** 속국 조공률 조절 — 높이면 조공이 늘고 민심·우호가 흔들린다 */
    fun setTributeRate(policy: GovernancePolicy, rate: Int): GovernanceActionResult {
        val next = rate.coerceIn(10, 60)
        if (next == policy.tributeRate) {
            return GovernanceActionResult(false, "[조공] 이미 ${next}%입니다.", policy)
        }
        val delta = next - policy.tributeRate
        return GovernanceActionResult(
            success = true,
            message = "[조공] ${policy.tributeRate}% → ${next}% — " +
                (if (delta > 0) "조공이 늘지만 민심 -${delta / 10} · 명성 +1"
                else "조공이 줄지만 민심 +${-delta / 10}"),
            policy = policy.copy(tributeRate = next),
            moraleDelta = if (delta > 0) -(delta / 10) else (-delta / 10),
            prestigeDelta = if (delta > 0) 1 else 0
        )
    }

    /** 지원군 차출 — 주둔 병력의 25%까지 본군으로 끌어온다 */
    fun callSupport(policy: GovernancePolicy, garrison: Int, troops: Int = -1): GovernanceActionResult {
        val available = (garrison * SUPPORT_RATIO).toInt()
        if (available < 10) {
            return GovernanceActionResult(
                false,
                "[차출] ${governanceName(garrison)} 주둔 병력이 적어 지원군을 뽑을 수 없습니다.",
                policy
            )
        }
        val requested = if (troops > 0) troops.coerceAtMost(available) else available
        val next = policy.copy(supportTroops = policy.supportTroops + requested)
        return GovernanceActionResult(
            success = true,
            message = "[차출] 지원군 ${"%,d".format(requested)}명을 본군에 붙였습니다 — " +
                "주둔 병력에서 빠져나갑니다.",
            policy = next,
            territoryTroopDelta = -requested,
            armyTroopDelta = requested
        )
    }

    /**
     * 절기 정산 — 정책이 세수·식량·징병·민심·치안에 어떻게 작용했는지.
     * 반란 위험이 60 이상이면 민심 -4, 병력 4% 이탈이 함께 일어난다.
     */
    fun season(territory: ManagedTerritory): GovernanceSeason {
        val policy = territory.policy
        val rebellion = policy.rebellionRisk >= 60
        val moraleDelta = policy.taxLevel.moraleDrift + policy.levyQuota.moraleDrift +
            ((policy.security - 50) / 25) + (if (rebellion) -4 else 0)
        val securityDelta = (if (policy.security < 50) 2 else if (policy.security > 80) -1 else 0) +
            (if (policy.facilities.contains(TerritoryFacility.OFFICE)) 1 else 0)
        val troopLoss = if (rebellion) (territory.troops * 0.04).toInt() else 0
        val line = buildString {
            append("[통치] ${territory.name} — ${policy.summaryLine()}")
            append(" · 세수 ×${"%.2f".format(policy.taxMultiplier)}")
            if (rebellion) append(" · ⚠ 반란 진압 (병력 -${"%,d".format(troopLoss)}명)")
        }
        return GovernanceSeason(
            taxRatio = policy.taxMultiplier,
            foodRatio = policy.foodMultiplier,
            levyRatio = policy.levyMultiplier,
            securityDelta = securityDelta,
            moraleDelta = moraleDelta,
            rebellion = rebellion,
            troopLoss = troopLoss,
            line = line
        )
    }

    private fun governanceName(garrison: Int): String = "이 영지"

    private fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"
}
