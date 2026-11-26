package com.game.slg.core.region

import com.game.slg.core.empire.GovernancePolicy
import com.game.slg.core.empire.TaxLevel

/**
 * 행정 단위 4단계 승격과 단계별 관리 메뉴 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-26):
 *  *"행정 단위 4단계 승격 & 메뉴 확장: (1) 읍·면·동 → (2) 시·군·구 → (3) 도·광역시 →
 *    (4) 국가/연방. 단위가 승격될수록 해당 단계에 맞는 관리 메뉴가 실제로 늘어나고 사용 가능해야 한다."*
 *
 * 읍·면·동에서 시작해 편입 영지·우호 세력·명성·의회 권한이 쌓이면 상위 행정 단위로 승격한다.
 * 승격하면 그 단계의 관리 메뉴가 실제로 열리고, 각 메뉴는 자금을 써서 효과를 낸다.
 * (메뉴 목록·요구 조건·효과 계산만 담는다 — 화면은 ui 계층이 그린다.)
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 행정 단위 4단계 */
enum class AdminTier(
    val order: Int,
    val label: String,
    val short: String,
    val icon: String,
    val detail: String,
    val requireTerritories: Int,
    val requireAllied: Int,
    val requirePrestige: Int,
    val requireAuthority: Int,
    val promoteCost: Int
) {
    DONG(
        order = 0,
        label = "읍·면·동",
        short = "읍면동",
        icon = "🏘",
        detail = "기본 거점 · 인접 전투/외교/공작 · 태수 일임 · 기본 세율/치안",
        requireTerritories = 0,
        requireAllied = 0,
        requirePrestige = 0,
        requireAuthority = 0,
        promoteCost = 0
    ),
    CITY(
        order = 1,
        label = "시·군·구",
        short = "시군구",
        icon = "🏙",
        detail = "시군 청사 · 통합 세제/치안 일괄 조정 · 광역 군단 · 시군구 단위 전쟁/동맹",
        requireTerritories = 2,
        requireAllied = 3,
        requirePrestige = 60,
        requireAuthority = 10,
        promoteCost = 1_200
    ),
    PROVINCE(
        order = 2,
        label = "도·광역시",
        short = "도광역시",
        icon = "🏛",
        detail = "도청 · 지방 의회 · 광역 보급망 · 고위 영웅 관직 임명 · 도 단위 원정",
        requireTerritories = 4,
        requireAllied = 5,
        requirePrestige = 80,
        requireAuthority = 25,
        promoteCost = 2_500
    ),
    STATE(
        order = 3,
        label = "국가·연방",
        short = "국가연방",
        icon = "👑",
        detail = "중앙 정부 부처(국방/외교/재무/정보) · 전국 총동원령 · 연방 헌법 조례 · 국가 외교전/종속국 관리",
        requireTerritories = 6,
        requireAllied = 7,
        requirePrestige = 95,
        requireAuthority = 40,
        promoteCost = 5_000
    );

    val next: AdminTier? get() = entries.firstOrNull { it.order == this.order + 1 }
    val isFinal: Boolean get() = next == null

    companion object {
        fun byName(name: String?): AdminTier = entries.firstOrNull { it.name == name } ?: DONG
    }
}

/** 관리 메뉴가 하는 일 — 화면 이동이거나 실제 효과 */
enum class AdminActionKind { NAVIGATE, EFFECT }

/**
 * 행정 단계별 관리 메뉴 1개.
 * @param tier 이 메뉴가 열리는 최소 단위
 * @param targetTabId 화면 이동 메뉴일 때 열 탭 (ui.MainTab.name)
 */
enum class AdminMenu(
    val id: String,
    val label: String,
    val icon: String,
    val tier: AdminTier,
    val detail: String,
    val kind: AdminActionKind,
    val targetTabId: String? = null,
    val targetSub: Int = 0,
    val cost: Int = 0
) {
    // ── 1단계 · 읍·면·동 ──────────────────────────────────────────
    BASE("base", "기본 거점", "🏯", AdminTier.DONG, "내 거점 운영", AdminActionKind.NAVIGATE, "DOMAIN", 0),
    ADJACENT_WAR("adjacent_war", "인접 전투", "⚔", AdminTier.DONG, "인접 지역 교전", AdminActionKind.NAVIGATE, "WAR", 0),
    DIPLOMACY("diplomacy", "외교", "🤝", AdminTier.DONG, "조약·협상", AdminActionKind.NAVIGATE, "DIPLOMACY", 0),
    CONSTRUCTION("construction", "공작", "🧠", AdminTier.DONG, "보급 차단·여론전·정찰", AdminActionKind.NAVIGATE, "WAR", 1),
    VICEROY("viceroy", "태수 일임", "🎖", AdminTier.DONG, "영웅에게 통치 위임", AdminActionKind.NAVIGATE, "EMPIRE", 1),
    TAX_SECURITY("tax_security", "세율·치안", "⚖", AdminTier.DONG, "영지별 세율·치안·징병", AdminActionKind.NAVIGATE, "EMPIRE", 1),

    // ── 2단계 · 시·군·구 ─────────────────────────────────────────
    CITY_HALL("city_hall", "시청사", "🏛", AdminTier.CITY, "의회 권한 +5 · 명성 +2", AdminActionKind.EFFECT, cost = 1_200),
    UNIFIED_TAX("unified_tax", "통합 세제", "💰", AdminTier.CITY, "전 영지 세율 한 단계 ↑", AdminActionKind.EFFECT, cost = 900),
    UNIFIED_SECURITY("unified_security", "통합 치안", "🛡", AdminTier.CITY, "전 영지 치안 +10", AdminActionKind.EFFECT, cost = 700),
    LEGION("legion", "광역 군단", "⚔", AdminTier.CITY, "정규 병력 상한의 8% 즉시 모집", AdminActionKind.EFFECT, cost = 1_500),
    CITY_WAR("city_war", "시군구 전쟁", "🎯", AdminTier.CITY, "의회 전쟁 안건 상정", AdminActionKind.NAVIGATE, "DOMAIN", 5),
    CITY_ALLIANCE("city_alliance", "시군구 동맹", "🤝", AdminTier.CITY, "외교 +6 · 우호 영지 +1", AdminActionKind.EFFECT, cost = 500),

    // ── 3단계 · 도·광역시 ────────────────────────────────────────
    PROVINCIAL_HALL("provincial_hall", "도청", "🏛", AdminTier.PROVINCE, "의회 권한 +8 · 명성 +3", AdminActionKind.EFFECT, cost = 2_000),
    LOCAL_COUNCIL("local_council", "지방 의회", "🗳", AdminTier.PROVINCE, "민심 +5 · 의회 권한 +3", AdminActionKind.EFFECT, cost = 800),
    SUPPLY_NET("supply_net", "광역 보급망", "🚚", AdminTier.PROVINCE, "식량 +2,000석", AdminActionKind.EFFECT, cost = 1_000),
    HIGH_OFFICE("high_office", "고위 관직", "🎖", AdminTier.PROVINCE, "명성 +4 · 민심 +3", AdminActionKind.EFFECT, cost = 1_200),
    PROVINCE_EXPEDITION("province_expedition", "도 원정", "🗡", AdminTier.PROVINCE, "2절기 원정 전투 +10%", AdminActionKind.EFFECT, cost = 1_400),

    // ── 4단계 · 국가·연방 ────────────────────────────────────────
    DEFENSE_MINISTRY("defense_ministry", "국방부", "🛡", AdminTier.STATE, "전 영지 방어 +10", AdminActionKind.EFFECT, cost = 1_500),
    FOREIGN_MINISTRY("foreign_ministry", "외교부", "🌐", AdminTier.STATE, "외교 +8 · 명성 +2", AdminActionKind.EFFECT, cost = 1_000),
    FINANCE_MINISTRY("finance_ministry", "재무부", "💰", AdminTier.STATE, "전국 세수 징수 +2,500G", AdminActionKind.EFFECT, cost = 600),
    INTEL_MINISTRY("intel_ministry", "정보부", "🕵", AdminTier.STATE, "첩보력 +10", AdminActionKind.EFFECT, cost = 900),
    MOBILIZATION("mobilization", "총동원령", "📣", AdminTier.STATE, "상한의 15% 즉시 소집", AdminActionKind.EFFECT, cost = 2_000),
    CONSTITUTION("constitution", "헌법 조례", "📜", AdminTier.STATE, "민심 +8 · 명성 +5 · 권한 +5", AdminActionKind.EFFECT, cost = 1_800),
    NATIONAL_DIPLOMACY("national_diplomacy", "국가 외교전", "🗺", AdminTier.STATE, "외교 +10", AdminActionKind.EFFECT, cost = 1_400),
    DEPENDENCY("dependency", "종속국 관리", "🏳", AdminTier.STATE, "전 속국 조공률 +10%", AdminActionKind.EFFECT, cost = 700);

    val unlockedBy: String get() = tier.label

    companion object {
        fun byId(id: String): AdminMenu? = entries.firstOrNull { it.id == id }
    }
}

/** 승격 조건 평가 */
data class AdminEvaluation(
    val current: AdminTier,
    val next: AdminTier?,
    val territories: Int,
    val allied: Int,
    val prestige: Int,
    val authority: Int,
    val gold: Int,
    val isEligible: Boolean,
    val progressRatio: Float,
    val summary: String
)

/** 승격 결과 */
data class AdminPromotionResult(
    val success: Boolean,
    val tier: AdminTier,
    val promoted: Boolean = false,
    val message: String,
    val authorityDelta: Int = 0,
    val prestigeDelta: Int = 0,
    val newlyOpened: List<AdminMenu> = emptyList()
)

/** 관리 메뉴 실행에 필요한 현재 값 */
data class AdminContext(
    val gold: Int,
    val food: Int = 0,
    val troops: Int = 0,
    val troopCap: Int = 0,
    val morale: Int = 50,
    val diplomacy: Int = 50,
    val prestige: Int = 50,
    val espionage: Int = 20,
    val authority: Int = 0,
    val territories: List<com.game.slg.core.empire.ManagedTerritory> = emptyList()
)

/** 관리 메뉴 실행 결과 — 화면이 그대로 적용한다 */
data class AdminActionResult(
    val success: Boolean,
    val message: String,
    val goldDelta: Int = 0,
    val foodDelta: Int = 0,
    val troopDelta: Int = 0,
    val moraleDelta: Int = 0,
    val diplomacyDelta: Int = 0,
    val prestigeDelta: Int = 0,
    val espionageDelta: Int = 0,
    val authorityDelta: Int = 0,
    val alliedDelta: Int = 0,
    /** 전 영지 세율 한 단계 상향 */
    val taxStep: Int = 0,
    /** 전 영지 치안 변화 */
    val securityDelta: Int = 0,
    /** 전 영지 방어 변화 */
    val defenseDelta: Int = 0,
    /** 전 속국 조공률 변화 */
    val tributeStep: Int = 0,
    /** 원정 전투 보너스 (%) */
    val combatBonusPercent: Int = 0
)

object AdminTierEngine {

    /** 현재 단위에서 쓸 수 있는 관리 메뉴 — 승격할수록 늘어난다 */
    fun menusOf(tier: AdminTier): List<AdminMenu> =
        AdminMenu.entries.filter { it.tier.order <= tier.order }

    /** 이번 승격으로 새로 열리는 메뉴 */
    fun newlyOpenedAt(tier: AdminTier): List<AdminMenu> =
        AdminMenu.entries.filter { it.tier == tier }

    /** 메뉴가 열려 있는지 */
    fun isUnlocked(menu: AdminMenu, tier: AdminTier): Boolean = menu.tier.order <= tier.order

    fun evaluate(
        current: AdminTier,
        territories: Int,
        allied: Int,
        prestige: Int,
        authority: Int,
        gold: Int
    ): AdminEvaluation {
        val next = current.next
            ?: return AdminEvaluation(
                current = current,
                next = null,
                territories = territories,
                allied = allied,
                prestige = prestige,
                authority = authority,
                gold = gold,
                isEligible = false,
                progressRatio = 1f,
                summary = "최종 단계 — ${current.label} 정부를 완성했습니다."
            )
        val ratios = listOf(
            if (next.requireTerritories == 0) 1f else territories.toFloat() / next.requireTerritories,
            if (next.requireAllied == 0) 1f else allied.toFloat() / next.requireAllied,
            if (next.requirePrestige == 0) 1f else prestige.toFloat() / next.requirePrestige,
            if (next.requireAuthority == 0) 1f else authority.toFloat() / next.requireAuthority,
            if (next.promoteCost == 0) 1f else gold.toFloat() / next.promoteCost
        ).map { it.coerceIn(0f, 1f) }
        val eligible = territories >= next.requireTerritories &&
            allied >= next.requireAllied &&
            prestige >= next.requirePrestige &&
            authority >= next.requireAuthority &&
            gold >= next.promoteCost
        return AdminEvaluation(
            current = current,
            next = next,
            territories = territories,
            allied = allied,
            prestige = prestige,
            authority = authority,
            gold = gold,
            isEligible = eligible,
            progressRatio = ratios.min(),
            summary = "다음 ${next.label} — 영지 $territories/${next.requireTerritories} · " +
                "우호 $allied/${next.requireAllied} · 명성 $prestige/${next.requirePrestige} · " +
                "권한 $authority/${next.requireAuthority} · 자금 ${"%,d".format(gold)}/${"%,d".format(next.promoteCost)}"
        )
    }

    /** 승격 — 조건을 채웠으면 다음 단위로 올라가고 새 메뉴가 열린다 */
    fun promote(
        current: AdminTier,
        territories: Int,
        allied: Int,
        prestige: Int,
        authority: Int,
        gold: Int
    ): AdminPromotionResult {
        val next = current.next
            ?: return AdminPromotionResult(
                success = false,
                tier = current,
                message = "[행정] 이미 최종 단계 '${current.label}'입니다."
            )
        val evaluation = evaluate(current, territories, allied, prestige, authority, gold)
        if (!evaluation.isEligible) {
            return AdminPromotionResult(
                success = false,
                tier = current,
                message = "[행정 승격 불가] ${evaluation.summary}"
            )
        }
        val opened = newlyOpenedAt(next)
        return AdminPromotionResult(
            success = true,
            tier = next,
            promoted = true,
            message = "[행정 승격] '${current.label}' → '${next.label}' — " +
                "관리 메뉴 ${opened.size}개가 새로 열렸습니다 (${opened.joinToString(", ") { "${it.icon} ${it.label}" }}). " +
                "승격 비용 ${"%,d".format(next.promoteCost)}G",
            authorityDelta = 5 + next.order * 5,
            prestigeDelta = 2,
            newlyOpened = opened
        )
    }

    /**
     * 관리 메뉴 실행.
     * 화면 이동 메뉴(NAVIGATE)는 효과 없이 안내만 돌려주고, 효과 메뉴는 자금을 써서 값을 바꾼다.
     */
    fun act(menu: AdminMenu, tier: AdminTier, ctx: AdminContext): AdminActionResult {
        if (!isUnlocked(menu, tier)) {
            return AdminActionResult(
                success = false,
                message = "[관리] '${menu.label}'은(는) ${menu.tier.label} 단계에서 열립니다."
            )
        }
        if (menu.kind == AdminActionKind.NAVIGATE) {
            return AdminActionResult(
                success = true,
                message = "[관리] ${menu.icon} ${menu.label} — ${menu.detail}"
            )
        }
        if (ctx.gold < menu.cost) {
            return AdminActionResult(
                success = false,
                message = "[관리] 자금이 부족합니다. (필요 ${"%,d".format(menu.cost)}G / 보유 ${"%,d".format(ctx.gold)}G)"
            )
        }
        val gold = -menu.cost
        return when (menu) {
            AdminMenu.CITY_HALL -> AdminActionResult(
                true, "[시청사] 광역 행정을 세웠습니다 — 의회 권한 +5 · 명성 +2", goldDelta = gold,
                authorityDelta = 5, prestigeDelta = 2
            )
            AdminMenu.UNIFIED_TAX -> AdminActionResult(
                true, "[통합 세제] 전 영지 세율을 한 단계 올렸습니다 — 민심이 흔들립니다.", goldDelta = gold,
                taxStep = 1, moraleDelta = -2
            )
            AdminMenu.UNIFIED_SECURITY -> AdminActionResult(
                true, "[통합 치안] 전 영지 치안 +10 — 반란 위험이 낮아졌습니다.", goldDelta = gold,
                securityDelta = 10
            )
            AdminMenu.LEGION -> {
                val room = (ctx.troopCap - ctx.troops).coerceAtLeast(0)
                val raised = minOf(room, (ctx.troopCap * 0.08).toInt().coerceAtLeast(60))
                AdminActionResult(
                    true, "[광역 군단] ${"%,d".format(raised)}명을 즉시 소집했습니다.", goldDelta = gold,
                    troopDelta = raised
                )
            }
            AdminMenu.CITY_ALLIANCE -> AdminActionResult(
                true, "[시군구 동맹] 이웃 시·군·구와 우호 협약 — 외교 +6 · 우호 영지 +1", goldDelta = gold,
                diplomacyDelta = 6, alliedDelta = 1
            )
            AdminMenu.PROVINCIAL_HALL -> AdminActionResult(
                true, "[도청] 광역 행정청을 열었습니다 — 의회 권한 +8 · 명성 +3", goldDelta = gold,
                authorityDelta = 8, prestigeDelta = 3
            )
            AdminMenu.LOCAL_COUNCIL -> AdminActionResult(
                true, "[지방 의회] 주민 대표를 세웠습니다 — 민심 +5 · 의회 권한 +3", goldDelta = gold,
                moraleDelta = 5, authorityDelta = 3
            )
            AdminMenu.SUPPLY_NET -> AdminActionResult(
                true, "[광역 보급망] 창고를 연결했습니다 — 식량 +2,000석", goldDelta = gold, foodDelta = 2_000
            )
            AdminMenu.HIGH_OFFICE -> AdminActionResult(
                true, "[고위 관직] 영웅에게 관직을 내렸습니다 — 명성 +4 · 민심 +3", goldDelta = gold,
                prestigeDelta = 4, moraleDelta = 3
            )
            AdminMenu.PROVINCE_EXPEDITION -> AdminActionResult(
                true, "[도 원정] 2절기 동안 원정 전투 +10%", goldDelta = gold, combatBonusPercent = 10
            )
            AdminMenu.DEFENSE_MINISTRY -> AdminActionResult(
                true, "[국방부] 전 영지 방어 +10 — 성벽·병영을 점검했습니다.", goldDelta = gold, defenseDelta = 10
            )
            AdminMenu.FOREIGN_MINISTRY -> AdminActionResult(
                true, "[외교부] 대외 협상단을 파견했습니다 — 외교 +8 · 명성 +2", goldDelta = gold,
                diplomacyDelta = 8, prestigeDelta = 2
            )
            AdminMenu.FINANCE_MINISTRY -> AdminActionResult(
                true, "[재무부] 전국 세수를 한 번에 징수했습니다 — 자금 +2,500G", goldDelta = gold + 2_500
            )
            AdminMenu.INTEL_MINISTRY -> AdminActionResult(
                true, "[정보부] 첩보망을 넓혔습니다 — 첩보력 +10", goldDelta = gold, espionageDelta = 10
            )
            AdminMenu.MOBILIZATION -> {
                val room = (ctx.troopCap - ctx.troops).coerceAtLeast(0)
                val raised = minOf(room, (ctx.troopCap * 0.15).toInt().coerceAtLeast(100))
                AdminActionResult(
                    true, "[총동원령] 예비대 ${"%,d".format(raised)}명이 집결했습니다.", goldDelta = gold,
                    troopDelta = raised, moraleDelta = -2
                )
            }
            AdminMenu.CONSTITUTION -> AdminActionResult(
                true, "[헌법 조례] 연방 헌법을 공포했습니다 — 민심 +8 · 명성 +5 · 의회 권한 +5", goldDelta = gold,
                moraleDelta = 8, prestigeDelta = 5, authorityDelta = 5
            )
            AdminMenu.NATIONAL_DIPLOMACY -> AdminActionResult(
                true, "[국가 외교전] 정상 외교를 열었습니다 — 외교 +10", goldDelta = gold, diplomacyDelta = 10
            )
            AdminMenu.DEPENDENCY -> AdminActionResult(
                true, "[종속국 관리] 전 속국 조공률 +10% — 민심이 조금 흔들립니다.", goldDelta = gold,
                tributeStep = 1, moraleDelta = -2
            )
            else -> AdminActionResult(false, "[관리] 처리할 수 없는 메뉴입니다.")
        }
    }

    /** 세율 한 단계 상향 도우미 (통합 세제) */
    fun raiseTax(policy: GovernancePolicy): GovernancePolicy =
        policy.copy(taxLevel = TaxLevel.ofOrder((policy.taxLevel.order + 1).coerceAtMost(TaxLevel.HIGH.order)))
}
