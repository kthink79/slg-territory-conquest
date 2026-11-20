package com.game.slg.core.economy

import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.model.FacilityType

/**
 * 재원 4종 — 홈 상단 HUD 에서 각각 눌러 늘리는 방법을 연다.
 *
 * 사용자 요청(2026-11-18): *"화면에서 식량, 골드, 인구수, 병력을 선택 했을때
 * 증가 시킬수 있는 방법들이 나오는 서브 메뉴가 나오도록 해줘"*
 */
enum class ResourceKind(
    val label: String,
    val icon: String,
    val accentHex: Long
) {
    GOLD("골드", "💰", 0xFFE2B755),
    FOOD("식량", "🌾", 0xFF81C784),
    POPULATION("인구수", "👥", 0xFFEDEAE3),
    TROOPS("병력", "⚔", 0xFF7FA4D6);

    companion object {
        /** 홈 HUD 에 놓는 순서 */
        val hudOrder: List<ResourceKind> = listOf(GOLD, FOOD, POPULATION, TROOPS)
    }
}

/**
 * 초반 재원 밸런스 — 2026-11-18 사용자 지적 *"SLG 게임 초기 시작시 재원이 너무 부족해"* 반영.
 *
 * 이전에는 AI 호족을 인계받으면 자금 180~500G · 식량 260~500석 · 병력 55~160명으로 시작해
 * 첫 연구(400G)·시설(700G)·영웅 탐색(200G)조차 버거웠다. 지금은 인계와 함께 창업 지원금을
 * 얹고, 절기마다 인구에 비례한 기본 수급을 더해 초반 4계절을 쾌적하게 만든다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */
object ResourceBalance {

    /** 게임 상태의 기본값 (인계 전 미리보기) */
    const val START_GOLD = 12_000
    const val START_FOOD = 24_000
    const val START_TROOPS = 1_800

    /** 호족 인계 때 함께 들어오는 창업 지원 */
    const val FOUNDING_GOLD = 8_000
    const val FOUNDING_FOOD = 16_000
    const val FOUNDING_TROOPS = 1_200

    /** 매 절기 기본 자금 수급 — 인구 30명당 1G + 기본 500G */
    fun seasonGold(population: Int): Int = 500 + (population.coerceAtLeast(0) / 30)

    /** 매 절기 기본 식량 수급 — 인구 20명당 1석 + 기본 800석, 물류 허브 +12% */
    fun seasonFood(population: Int, facilities: Set<FacilityType> = emptySet()): Int {
        val base = 800 + (population.coerceAtLeast(0) / 20)
        val hub = if (facilities.contains(FacilityType.LOGISTICS_HUB)) 1.12 else 1.0
        return (base * hub).toInt()
    }

    /** HUD·시트에 보이는 한 줄 — "절기마다 자금 +2,110G · 식량 +3,230석" */
    fun incomeLabel(population: Int, facilities: Set<FacilityType> = emptySet()): String =
        "절기 기본 수급 · 자금 +%,dG · 식량 +%,d석".format(seasonGold(population), seasonFood(population, facilities))
}

/**
 * 재원을 늘리는 방법의 종류.
 *  · INSTANT  — 그 자리에서 바로 실행 (교역·모집·정착 지원·긴급 세수)
 *  · NAVIGATE — 해당 화면으로 이동해 처리
 *  · SEASON   — 절기를 진행해 수급을 받는다
 */
enum class BoostKind { INSTANT, NAVIGATE, SEASON }

/** 이동 대상 화면 — UI 탭과 1:1로 이어진다 */
enum class BoostTarget {
    TECH, HERO_PLACE, HERO_RECRUIT, FACILITIES, EVENTS, COUNCIL, PROMOTION, SEASON_VIEW, WAR_OPS
}

/** 즉시 실행 항목의 조건 */
data class BoostRequirement(
    val gold: Int = 0,
    val food: Int = 0,
    /** 이번 절기에 쓸 수 있는 횟수 (0 = 제한 없음) */
    val perSeasonLimit: Int = 0
)

/** 재원 1종의 증가 방법 1건 */
data class ResourceBoostAction(
    val id: String,
    val resource: ResourceKind,
    val icon: String,
    val title: String,
    /** 목록에 보이는 효과 한 줄 */
    val effect: String,
    /** 눌렀을 때 무슨 일이 일어나는지 */
    val detail: String,
    val kind: BoostKind,
    val requirement: BoostRequirement = BoostRequirement(),
    val target: BoostTarget? = null
) {
    val isInstant: Boolean get() = kind == BoostKind.INSTANT
}

/** 즉시 실행 결과 — 화면은 이 값으로 상태를 갱신한다 */
data class BoostOutcome(
    val ok: Boolean,
    val message: String,
    val goldDelta: Int = 0,
    val foodDelta: Int = 0,
    val populationDelta: Int = 0,
    val troopsDelta: Int = 0,
    val moraleDelta: Int = 0
)

/**
 * 재원별 증가 방법 목록 + 즉시 실행 규칙.
 * 실제로 지금 게임에 있는 시스템(교역·징집·정착 지원·세수·기술·영웅·시설·사건·승격)과만 연결한다.
 */
object ResourceBoosts {

    // ── 교역·모집 단가 ──
    const val MARKET_BUY_GOLD = 600
    const val MARKET_BUY_FOOD = 2_000
    const val MARKET_SELL_FOOD = 1_500
    const val MARKET_SELL_GOLD = 450
    const val RECRUIT_GOLD = 500
    const val RECRUIT_FOOD = 400
    const val RECRUIT_TROOPS = 300
    const val SETTLE_GOLD = 700
    const val SETTLE_FOOD = 300
    const val SETTLE_POPULATION = 250
    const val SETTLE_MORALE = 1
    const val LEVY_GOLD = 1_200
    const val LEVY_MORALE = -6

    val ALL: List<ResourceBoostAction> = listOf(
        // ── 골드 ──
        ResourceBoostAction(
            id = "gold_sell_food",
            resource = ResourceKind.GOLD,
            icon = "🌾",
            title = "비축 식량 판매",
            effect = "식량 ${"%,d".format(MARKET_SELL_FOOD)}석 → 자금 +${MARKET_SELL_GOLD}G",
            detail = "비축고의 식량을 인접 물류 상인회에 넘겨 현금을 만든다. 보급이 넉넉할 때 쓴다.",
            kind = BoostKind.INSTANT,
            requirement = BoostRequirement(food = MARKET_SELL_FOOD)
        ),
        ResourceBoostAction(
            id = "gold_levy",
            resource = ResourceKind.GOLD,
            icon = "🏛",
            title = "긴급 세수 징수",
            effect = "자금 +${"%,d".format(LEVY_GOLD)}G · 민심 ${LEVY_MORALE}",
            detail = "관할 구역에 임시 부과금을 매긴다. 절기마다 2회까지, 민심이 깎인다.",
            kind = BoostKind.INSTANT,
            requirement = BoostRequirement(perSeasonLimit = 2)
        ),
        ResourceBoostAction(
            id = "gold_tech",
            resource = ResourceKind.GOLD,
            icon = "🛠",
            title = "지방세 재정 최적화 연구",
            effect = "절기 자금 +300G (내정 3단계)",
            detail = "영지 › 기술에서 내정 계통을 연구하면 절기마다 들어오는 자금이 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.TECH
        ),
        ResourceBoostAction(
            id = "gold_hero",
            resource = ResourceKind.GOLD,
            icon = "🏛",
            title = "내정 영웅 배치",
            effect = "절기 자금 +% (능력치·등급 비례)",
            detail = "영웅 › 배치에서 내정 영웅을 앉히면 절기 자금이 비율로 늘어난다. ${HeroRoster.ALL.size}명 중에서 고른다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.HERO_PLACE
        ),
        ResourceBoostAction(
            id = "gold_merchant",
            resource = ResourceKind.GOLD,
            icon = "🌙",
            title = "야간 물류 협약 / 통행료",
            effect = "자금 +900G 또는 보급 +1,200석",
            detail = "영지 › 사건에서 물류 상인회 안건을 처리한다. 통행료를 매기면 자금, 면제하면 보급이 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.EVENTS
        ),

        // ── 식량 ──
        ResourceBoostAction(
            id = "food_buy",
            resource = ResourceKind.FOOD,
            icon = "🛒",
            title = "시장에서 식량 매입",
            effect = "자금 ${MARKET_BUY_GOLD}G → 식량 +${"%,d".format(MARKET_BUY_FOOD)}석",
            detail = "지역 시장에서 비축 식량을 산다. 자금이 넉넉하고 보급이 급할 때 쓴다.",
            kind = BoostKind.INSTANT,
            requirement = BoostRequirement(gold = MARKET_BUY_GOLD)
        ),
        ResourceBoostAction(
            id = "food_season",
            resource = ResourceKind.FOOD,
            icon = "🍂",
            title = "절기 진행으로 수급",
            effect = "절기 기본 수급 + 가을 수확·여름 비축",
            detail = "절기를 넘기면 인구에 비례한 기본 보급이 들어오고, 가을에는 큰 수확이 더해진다.",
            kind = BoostKind.SEASON,
            target = BoostTarget.SEASON_VIEW
        ),
        ResourceBoostAction(
            id = "food_hub",
            resource = ResourceKind.FOOD,
            icon = "🚚",
            title = "물류 허브 건설",
            effect = "절기 보급 +12%",
            detail = "영지 › 시설에서 물류 허브를 세우면 절기마다 들어오는 보급이 12% 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.FACILITIES
        ),
        ResourceBoostAction(
            id = "food_hero",
            resource = ResourceKind.FOOD,
            icon = "💰",
            title = "상업 영웅 배치",
            effect = "절기 보급 +% (능력치·등급 비례)",
            detail = "영웅 › 배치에서 상업 영웅을 앉히면 절기 보급이 비율로 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.HERO_PLACE
        ),
        ResourceBoostAction(
            id = "food_merchant",
            resource = ResourceKind.FOOD,
            icon = "🌙",
            title = "야간 물류 협약",
            effect = "보급 +1,200석 (자금 -600G)",
            detail = "영지 › 사건에서 통행료를 면제하고 상호 보급 협정을 맺으면 보급이 크게 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.EVENTS
        ),

        // ── 인구수 ──
        ResourceBoostAction(
            id = "pop_settle",
            resource = ResourceKind.POPULATION,
            icon = "🏘",
            title = "정착 지원",
            effect = "자금 ${SETTLE_GOLD}G·식량 ${SETTLE_FOOD}석 → 인구 +${SETTLE_POPULATION}명 · 민심 +$SETTLE_MORALE",
            detail = "빈 주거와 일자리를 제공해 인구를 받아들인다. 절기마다 3회까지.",
            kind = BoostKind.INSTANT,
            requirement = BoostRequirement(gold = SETTLE_GOLD, food = SETTLE_FOOD, perSeasonLimit = 3)
        ),
        ResourceBoostAction(
            id = "pop_refugee",
            resource = ResourceKind.POPULATION,
            icon = "🚪",
            title = "이재민 수용",
            effect = "인구 증가 · 민심 상승",
            detail = "영지 › 사건에서 이재민 800명 통제선을 처리하면 인구와 민심이 함께 오른다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.EVENTS
        ),
        ResourceBoostAction(
            id = "pop_facility",
            resource = ResourceKind.POPULATION,
            icon = "🚨",
            title = "민방위 지원센터 건설",
            effect = "민심 +6 · 주민 정착 기반",
            detail = "영지 › 시설에서 민방위 지원센터를 세우면 민심이 올라 인구 이탈이 줄어든다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.FACILITIES
        ),
        ResourceBoostAction(
            id = "pop_tech",
            resource = ResourceKind.POPULATION,
            icon = "🛠",
            title = "주민 데이터 플랫폼 연구",
            effect = "민심 +6 · 첩보 방어 +15%",
            detail = "영지 › 기술에서 내정 4단계를 연구하면 인구·이동 데이터로 정착이 안정된다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.TECH
        ),
        ResourceBoostAction(
            id = "pop_promote",
            resource = ResourceKind.POPULATION,
            icon = "👑",
            title = "승격으로 영지 확대",
            effect = "관할 구역·인구 기반 확대",
            detail = "영지 › 승격에서 다음 단계 조건을 채우면 더 넓은 구역의 인구가 세력에 들어온다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.PROMOTION
        ),

        // ── 병력 ──
        ResourceBoostAction(
            id = "troops_recruit",
            resource = ResourceKind.TROOPS,
            icon = "🪖",
            title = "경비 인력 모집",
            effect = "자금 ${RECRUIT_GOLD}G·식량 ${RECRUIT_FOOD}석 → 병력 +${RECRUIT_TROOPS}명",
            detail = "예비 인력과 장비를 사서 바로 배치한다. 절기마다 3회까지.",
            kind = BoostKind.INSTANT,
            requirement = BoostRequirement(gold = RECRUIT_GOLD, food = RECRUIT_FOOD, perSeasonLimit = 3)
        ),
        ResourceBoostAction(
            id = "troops_levy",
            resource = ResourceKind.TROOPS,
            icon = "🏛",
            title = "경비 인력 소집안 표결",
            effect = "병력 +40% (의회 통과 시)",
            detail = "영지 › 의회에서 소집안을 표결한다. 통과하면 한 번에 많은 병력을 확보한다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.COUNCIL
        ),
        ResourceBoostAction(
            id = "troops_hero",
            resource = ResourceKind.TROOPS,
            icon = "⚔",
            title = "작전 영웅 배치",
            effect = "작전 전투력 +% (능력치·등급 비례)",
            detail = "영웅 › 배치에서 작전 영웅을 앉히면 작전 전투력이 비율로 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.HERO_PLACE
        ),
        ResourceBoostAction(
            id = "troops_facility",
            resource = ResourceKind.TROOPS,
            icon = "🛰",
            title = "드론 격납고 건설",
            effect = "드론 전투력 +15%",
            detail = "영지 › 시설에서 드론 격납고를 세우면 드론 병과 전력이 늘어난다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.FACILITIES
        ),
        ResourceBoostAction(
            id = "troops_ops",
            resource = ResourceKind.TROOPS,
            icon = "🎯",
            title = "작전으로 노획",
            effect = "작전 성공 시 병력·보급 확보",
            detail = "작전 탭에서 인접 거점에 작전을 걸어 이기면 노획과 명성을 얻는다.",
            kind = BoostKind.NAVIGATE,
            target = BoostTarget.WAR_OPS
        )
    )

    fun of(resource: ResourceKind): List<ResourceBoostAction> = ALL.filter { it.resource == resource }

    fun byId(id: String): ResourceBoostAction? = ALL.firstOrNull { it.id == id }

    /** 즉시 실행 항목의 조건 검사 — null 이면 실행 가능, 아니면 화면에 보일 사유 */
    fun availability(
        action: ResourceBoostAction,
        gold: Int,
        food: Int,
        usedThisSeason: Int
    ): String? {
        val limit = action.requirement.perSeasonLimit
        if (limit > 0 && usedThisSeason >= limit) {
            return "${action.title} — 이번 절기 ${limit}회를 모두 썼습니다."
        }
        if (gold < action.requirement.gold) {
            return "자금이 부족합니다. (필요 ${action.requirement.gold}G / 보유 ${gold}G)"
        }
        if (food < action.requirement.food) {
            return "식량이 부족합니다. (필요 ${action.requirement.food}석 / 보유 ${food}석)"
        }
        return null
    }

    /** 즉시 실행 — INSTANT 항목만 상태 변화를 돌려준다 */
    fun execute(
        actionId: String,
        gold: Int,
        food: Int,
        population: Int,
        troops: Int,
        morale: Int,
        usedThisSeason: Int
    ): BoostOutcome {
        val action = byId(actionId)
            ?: return BoostOutcome(false, "[재원] 알 수 없는 방법입니다. ($actionId)")
        if (!action.isInstant) {
            return BoostOutcome(false, "[재원] ${action.title} — 화면에서 직접 처리해야 합니다.")
        }
        availability(action, gold, food, usedThisSeason)?.let { return BoostOutcome(false, "[재원] $it") }

        return when (action.id) {
            "gold_sell_food" -> BoostOutcome(
                ok = true,
                message = "[교역] 비축 식량 ${"%,d".format(MARKET_SELL_FOOD)}석을 팔아 자금 +${MARKET_SELL_GOLD}G를 확보했습니다.",
                goldDelta = MARKET_SELL_GOLD,
                foodDelta = -MARKET_SELL_FOOD
            )

            "gold_levy" -> BoostOutcome(
                ok = true,
                message = "[세수] 긴급 부과금으로 자금 +${"%,d".format(LEVY_GOLD)}G — 민심 $LEVY_MORALE.",
                goldDelta = LEVY_GOLD,
                moraleDelta = LEVY_MORALE
            )

            "food_buy" -> BoostOutcome(
                ok = true,
                message = "[교역] 자금 -${MARKET_BUY_GOLD}G로 식량 +${"%,d".format(MARKET_BUY_FOOD)}석을 사들였습니다.",
                goldDelta = -MARKET_BUY_GOLD,
                foodDelta = MARKET_BUY_FOOD
            )

            "pop_settle" -> BoostOutcome(
                ok = true,
                message = "[정착] 주거·일자리를 내주고 인구 +${SETTLE_POPULATION}명을 받아들였습니다. 민심 +$SETTLE_MORALE.",
                goldDelta = -SETTLE_GOLD,
                foodDelta = -SETTLE_FOOD,
                populationDelta = SETTLE_POPULATION,
                moraleDelta = SETTLE_MORALE
            )

            "troops_recruit" -> BoostOutcome(
                ok = true,
                message = "[모집] 경비 인력 ${RECRUIT_TROOPS}명을 모았습니다. 자금 -${RECRUIT_GOLD}G · 식량 -${RECRUIT_FOOD}석.",
                goldDelta = -RECRUIT_GOLD,
                foodDelta = -RECRUIT_FOOD,
                troopsDelta = RECRUIT_TROOPS
            )

            else -> BoostOutcome(false, "[재원] ${action.title} — 아직 실행 규칙이 없습니다.")
        }
    }

    /** 화면에 보일 이번 절기 사용 횟수 문구 */
    fun usageLabel(action: ResourceBoostAction, usedThisSeason: Int): String? {
        val limit = action.requirement.perSeasonLimit
        if (limit <= 0) return null
        return "이번 절기 $usedThisSeason/${limit}회"
    }

    /** 현재 재원 숫자 (HUD·시트 표시용) */
    fun valueOf(kind: ResourceKind, gold: Int, food: Int, population: Int, troops: Int): Int =
        when (kind) {
            ResourceKind.GOLD -> gold
            ResourceKind.FOOD -> food
            ResourceKind.POPULATION -> population
            ResourceKind.TROOPS -> troops
        }
}
