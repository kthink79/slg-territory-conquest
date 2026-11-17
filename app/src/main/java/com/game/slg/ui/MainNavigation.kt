package com.game.slg.ui

import com.game.slg.core.model.FacilityType

/**
 * 메인 화면 하단 「서브 메뉴」와 상세 화면의 탭 구조.
 *
 * 사용자 요청: *"텍스트를 많이 줄이고 비쥬얼 요소 기반으로 하고 메뉴가 많으면 탭으로 구성하거나
 * 서브 메뉴로 만들어서 화면에 한번에 보여지는 텍스를 줄여줘"*
 *
 * 그래서 화면마다 (1) 큰 이모지 아이콘의 서브 메뉴 타일, (2) 2~4칸 탭 바, (3) 접히는 「?」 설명
 * 세 층으로만 정보를 낸다. 각 탭 이름은 2~3글자로 고정한다.
 */
enum class SubMenu(
    val id: String,
    /** 하단 타일 라벨 — 3글자 이내 */
    val label: String,
    /** 상세 화면 제목 */
    val title: String,
    val emoji: String,
    val accentHex: Long,
    val isAction: Boolean = false
) {
    TERRITORY(
        id = "submenu_territory",
        label = "영지",
        title = "영지·내정",
        emoji = "🏯",
        accentHex = 0xFFE2B755
    ),
    TECH(
        id = "submenu_tech",
        label = "기술",
        title = "4대 테크트리",
        emoji = "🛠",
        accentHex = 0xFF64B5F6
    ),
    COUNCIL(
        id = "submenu_council",
        label = "의회",
        title = "호족 의회",
        emoji = "🏛",
        accentHex = 0xFF7FA4D6
    ),
    WAR(
        id = "submenu_war",
        label = "전장",
        title = "전장·전략",
        emoji = "⚔",
        accentHex = 0xFFE57373
    ),
    DIPLOMACY(
        id = "submenu_diplomacy",
        label = "외교",
        title = "외교·첩보",
        emoji = "🤝",
        accentHex = 0xFFB39DDB
    ),
    EVENTS(
        id = "submenu_events",
        label = "사건",
        title = "시나리오 사건",
        emoji = "🔔",
        accentHex = 0xFFF5D98A
    ),
    PROMOTION(
        id = "submenu_promotion",
        label = "로드맵",
        title = "승격 로드맵",
        emoji = "👑",
        accentHex = 0xFF81C784
    ),
    SEASON(
        id = "submenu_season",
        label = "절기",
        title = "절기 진행",
        emoji = "🌾",
        accentHex = 0xFF90A4AE,
        isAction = true
    );

    /** 상세 화면으로 열리는 항목인지 */
    val opensDetail: Boolean get() = !isAction

    companion object {
        /** 상세 화면이 있는 항목 (테스트·화면 매핑 검증용) */
        val detailMenus: List<SubMenu> = entries.filter { it.opensDetail }
    }
}

/** 상세 화면 탭 — 모든 탭 라벨은 2~3글자 */
interface ScreenTab {
    val label: String
}

enum class TerritoryTab(override val label: String) : ScreenTab {
    HOME("영지"),
    STATS("지표"),
    SEASON("절기"),
    FACILITIES("시설")
}

/** 테크트리 탭 — 계통 4개가 곧 탭이다 */
enum class TechTab(val branch: String, override val label: String) : ScreenTab {
    ADMIN("내정", "내정"),
    SCIENCE("과학기술", "과학"),
    CULTURE("문화", "문화"),
    MILITARY("군사", "군사")
}

enum class CouncilTab(override val label: String) : ScreenTab {
    BILLS("안건"),
    LORDS("호족")
}

enum class WarTab(override val label: String) : ScreenTab {
    OPS("작전"),
    REPORT("전과"),
    DOCTRINE("원칙")
}

enum class DiplomacyTab(override val label: String) : ScreenTab {
    TREATY("조약"),
    ESPIONAGE("첩보"),
    TALENT("인재")
}

enum class EventTab(override val label: String) : ScreenTab {
    REFUGEE("유민"),
    MERCHANT("상인"),
    SCOUT("정찰대")
}

enum class PromotionTab(override val label: String) : ScreenTab {
    ROADMAP("로드맵"),
    CONDITIONS("조건"),
    PATH("경로")
}

/** 서브 메뉴별 탭 목록 (실행형 메뉴는 탭 없음) */
object ScreenTabs {
    fun labelsOf(menu: SubMenu): List<String> = when (menu) {
        SubMenu.TERRITORY -> TerritoryTab.entries.map { it.label }
        SubMenu.TECH -> TechTab.entries.map { it.label }
        SubMenu.COUNCIL -> CouncilTab.entries.map { it.label }
        SubMenu.WAR -> WarTab.entries.map { it.label }
        SubMenu.DIPLOMACY -> DiplomacyTab.entries.map { it.label }
        SubMenu.EVENTS -> EventTab.entries.map { it.label }
        SubMenu.PROMOTION -> PromotionTab.entries.map { it.label }
        SubMenu.SEASON -> emptyList()
    }

    /** 화면 한 곳에 4개를 넘는 탭을 두지 않는다 */
    const val MAX_TABS = 4
}

/** 메인 화면 경로 */
sealed interface MainRoute {
    data object Home : MainRoute
    data class Detail(val menu: SubMenu) : MainRoute
}

/**
 * 메인 화면 ↔ 서브 메뉴 상세 화면 전환 상태.
 * 화면 없이 JVM에서 검증할 수 있도록 순수 데이터로 둔다.
 */
data class MainNavigationState(val route: MainRoute = MainRoute.Home) {

    val isHome: Boolean get() = route is MainRoute.Home

    /** 현재 열린 서브 메뉴 (메인 화면이면 null) */
    val openedMenu: SubMenu? get() = (route as? MainRoute.Detail)?.menu

    /** 서브 메뉴 열기 — 실행형 항목은 화면을 열지 않는다 */
    fun open(menu: SubMenu): MainNavigationState =
        if (menu.opensDetail) copy(route = MainRoute.Detail(menu)) else this

    /** '‹ 메인' 으로 복귀 */
    fun back(): MainNavigationState = copy(route = MainRoute.Home)
}

/** 현재 상태 패널의 경보 등급 */
enum class AlertSeverity(val label: String, val accentHex: Long) {
    CRITICAL("긴급", 0xFFE57373),
    WARNING("주의", 0xFFE2B755),
    INFO("정보", 0xFF7FA4D6)
}

/**
 * 메인 화면 경보 1건 — 칩에는 [icon] + [short] 만 보이고,
 * 긴 문장은 📊 정보 패널에서만 보인다.
 */
data class StatusAlert(
    val id: String,
    val icon: String,
    val short: String,
    val title: String,
    val severity: AlertSeverity,
    val target: SubMenu
)

/**
 * 활성 경보 생성기. 미결 사건만 모아 아이콘·짧은 라벨로 줄이고,
 * 각 사건이 처리되는 서브 메뉴로 바로 연결한다.
 */
object StatusAlertBuilder {

    fun build(state: SlgGameState): List<StatusAlert> {
        val alerts = mutableListOf<StatusAlert>()

        if (state.refugeeChoice == null) {
            alerts += StatusAlert(
                id = "alert_refugee",
                icon = "🚪",
                short = "이재민 800",
                title = "이재민 800명 통제선",
                severity = AlertSeverity.CRITICAL,
                target = SubMenu.EVENTS
            )
        }
        if (state.eventBChoice == null) {
            alerts += StatusAlert(
                id = "alert_scout",
                icon = "🛰",
                short = "무인기 3",
                title = "무인기 정찰조 출현",
                severity = AlertSeverity.WARNING,
                target = SubMenu.WAR
            )
        }
        if (state.eventAChoice == null) {
            alerts += StatusAlert(
                id = "alert_merchant",
                icon = "🌙",
                short = "야간 물류",
                title = "물류 상인회 협약",
                severity = AlertSeverity.WARNING,
                target = SubMenu.EVENTS
            )
        }
        val pendingBills = state.bills.count { it.passed == null }
        if (pendingBills > 0) {
            alerts += StatusAlert(
                id = "alert_bills",
                icon = "🏛",
                short = "표결 ${pendingBills}건",
                title = "표결 대기 ${pendingBills}건",
                severity = AlertSeverity.WARNING,
                target = SubMenu.COUNCIL
            )
        }
        if (state.nightMeetingChoice == null && state.nightMeetingCandidates.isNotEmpty()) {
            val prime = state.nightMeetingCandidates.maxByOrNull { it.suspicion }
            alerts += StatusAlert(
                id = "alert_night_meeting",
                icon = "👤",
                short = "밀회 제안",
                title = "${prime?.name ?: "의심 인물"} 밀회",
                severity = AlertSeverity.WARNING,
                target = SubMenu.DIPLOMACY
            )
        }
        if (state.isPromotionEligible) {
            alerts += StatusAlert(
                id = "alert_promotion",
                icon = "👑",
                short = "승격 가능",
                title = "승격 조건 충족",
                severity = AlertSeverity.INFO,
                target = SubMenu.PROMOTION
            )
        }
        if (state.food < 1500) {
            alerts += StatusAlert(
                id = "alert_food",
                icon = "🌾",
                short = "보급 부족",
                title = "보급 ${state.food}석",
                severity = AlertSeverity.CRITICAL,
                target = SubMenu.TERRITORY
            )
        }
        if (state.enemyFood in 1..1500) {
            alerts += StatusAlert(
                id = "alert_enemy_food",
                icon = "🎯",
                short = "적 보급",
                title = "적 보급 ${state.enemyFood}석",
                severity = AlertSeverity.INFO,
                target = SubMenu.WAR
            )
        }
        if (state.facilities.size < FacilityType.entries.size && state.gold >= 700) {
            alerts += StatusAlert(
                id = "alert_facility",
                icon = "🏗",
                short = "시설 가능",
                title = "시설 건설 가능",
                severity = AlertSeverity.INFO,
                target = SubMenu.TERRITORY
            )
        }
        return alerts
    }
}

/**
 * 상단 1줄 요약 + 📊 정보 패널 데이터.
 *
 * 기존 「현재 상태」 1/3 패널의 긴 문장들을 모달로 옮기고,
 * 홈에는 숫자 칩 4개만 남긴다.
 */
data class InfoEntry(
    val icon: String,
    val label: String,
    val value: String,
    val toneHex: Long
)

object MainInfoPanel {

    const val BUTTON_LABEL = "📊 정보"

    /** 홈 상단 1줄 — 자금·보급·병력·민심 */
    fun topSummary(state: SlgGameState): List<InfoEntry> = listOf(
        InfoEntry("💰", "자금", "${state.gold}", 0xFFE2B755),
        InfoEntry("🌾", "보급", "${state.food}", 0xFF81C784),
        InfoEntry("⚔", "병력", "${state.troops}", 0xFF7FA4D6),
        InfoEntry("❤", "민심", "${state.morale}", 0xFFEDEAE3)
    )

    /** 📊 정보 패널 — 지역·호족 정원·지휘 단계·세력 지표 */
    fun detailRows(state: SlgGameState): List<InfoEntry> {
        val next = state.nextPromotionStage
        return buildList {
            add(InfoEntry("🗺", "지역", state.regionName, 0xFF7FA4D6))
            add(InfoEntry("👥", "인구", "%,d".format(state.regionPopulation), 0xFFEDEAE3))
            add(InfoEntry("🏯", "호족", "${state.registeredLords}/${state.lordCapacity}석", 0xFFE2B755))
            add(InfoEntry("🎖", "지휘", "Lv.${state.command.level}", 0xFFE2B755))
            add(InfoEntry("🌫", "해금", state.expansionTier.label, 0xFF7FA4D6))
            add(InfoEntry("🗓", "절기", state.seasonName.substringBefore(" ("), 0xFF90A4AE))
            add(InfoEntry("🏗", "시설", "${state.facilities.size}곳", 0xFF90A4AE))
            add(InfoEntry("🤝", "우호", "${state.alliedTerritories}곳", 0xFF7FA4D6))
            add(InfoEntry("⭐", "명성", "${state.prestige}", 0xFFE2B755))
            add(InfoEntry("🎭", "첩보", "${state.espionagePower}", 0xFFB39DDB))
            add(InfoEntry("🎯", "적량", "${state.enemyFood}", 0xFFE57373))
            if (next != null) {
                add(
                    InfoEntry(
                        "➡",
                        "다음",
                        "${next.displayName} · 거점 ${state.alliedTerritories}/${next.requiredAlliedTerritories} · 명성 ${state.prestige}/${next.requiredPrestige}",
                        0xFF81C784
                    )
                )
            }
        }
    }

    fun alertCount(state: SlgGameState): Int = StatusAlertBuilder.build(state).size

    fun hasUrgentAlert(state: SlgGameState): Boolean =
        StatusAlertBuilder.build(state).any { it.severity == AlertSeverity.CRITICAL }
}
