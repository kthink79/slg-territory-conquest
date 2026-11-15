package com.game.slg.ui

/**
 * 메인 3단 화면 하단 1/3 「서브 메뉴」 아이콘 정의.
 *
 * 각 항목은 기존 4탭(영지·지도 / 내정·의회 / 전장·전략 / 외교·첩보)의 모든 기능을
 * 하나도 빠뜨리지 않고 상세 화면으로 열어 준다. 마지막 '절기 진행'만 화면 이동이 아니라
 * 즉시 실행 동작([isAction])이다.
 */
enum class SubMenu(
    val id: String,
    val label: String,
    val caption: String,
    val iconKey: SubMenuIcon,
    val accentHex: Long,
    val isAction: Boolean = false
) {
    TERRITORY(
        id = "submenu_territory",
        label = "영지·내정",
        caption = "본진 · 자원",
        iconKey = SubMenuIcon.HOME,
        accentHex = 0xFFE2B755
    ),
    TECH(
        id = "submenu_tech",
        label = "테크트리",
        caption = "4대 계통 연구",
        iconKey = SubMenuIcon.TECH,
        accentHex = 0xFF64B5F6
    ),
    COUNCIL(
        id = "submenu_council",
        label = "호족 의회",
        caption = "안건 표결",
        iconKey = SubMenuIcon.COUNCIL,
        accentHex = 0xFF7FA4D6
    ),
    WAR(
        id = "submenu_war",
        label = "전장·전략",
        caption = "매복·도하·수성",
        iconKey = SubMenuIcon.WAR,
        accentHex = 0xFFE57373
    ),
    DIPLOMACY(
        id = "submenu_diplomacy",
        label = "외교·첩보",
        caption = "조약·공작·밀회",
        iconKey = SubMenuIcon.DIPLOMACY,
        accentHex = 0xFFB39DDB
    ),
    EVENTS(
        id = "submenu_events",
        label = "이벤트",
        caption = "시나리오 선택",
        iconKey = SubMenuIcon.EVENT,
        accentHex = 0xFFF5D98A
    ),
    PROMOTION(
        id = "submenu_promotion",
        label = "성주 로드맵",
        caption = "호족→통합군주",
        iconKey = SubMenuIcon.PROMOTION,
        accentHex = 0xFF81C784
    ),
    SEASON(
        id = "submenu_season",
        label = "절기 진행",
        caption = "조세·추수 정산",
        iconKey = SubMenuIcon.SEASON,
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

/** 서브 메뉴 아이콘 종류 — Compose 계층에서 Material 아이콘으로 변환한다 */
enum class SubMenuIcon {
    HOME, TECH, COUNCIL, WAR, DIPLOMACY, EVENT, PROMOTION, SEASON
}

/** 메인 화면 경로 */
sealed interface MainRoute {
    data object Home : MainRoute
    data class Detail(val menu: SubMenu) : MainRoute
}

/**
 * 메인 3단 화면 ↔ 서브 메뉴 상세 화면 전환 상태.
 * 화면 없이 JVM에서 검증할 수 있도록 순수 데이터로 둔다.
 */
data class MainNavigationState(val route: MainRoute = MainRoute.Home) {

    val isHome: Boolean get() = route is MainRoute.Home

    /** 현재 열린 서브 메뉴 (메인 화면이면 null) */
    val openedMenu: SubMenu? get() = (route as? MainRoute.Detail)?.menu

    /** 서브 메뉴 열기 — 실행형 항목은 화면을 열지 않는다 */
    fun open(menu: SubMenu): MainNavigationState =
        if (menu.opensDetail) copy(route = MainRoute.Detail(menu)) else this

    /** '← 메인 3단 화면' 으로 복귀 */
    fun back(): MainNavigationState = copy(route = MainRoute.Home)
}

/** 현재 상태 패널의 경보 등급 */
enum class AlertSeverity(val label: String, val accentHex: Long) {
    CRITICAL("긴급", 0xFFE57373),
    WARNING("주의", 0xFFE2B755),
    INFO("정보", 0xFF7FA4D6)
}

/** 중간 1/3 「현재 상태」 패널에 뜨는 경보 1건 */
data class StatusAlert(
    val id: String,
    val title: String,
    val detail: String,
    val severity: AlertSeverity,
    val target: SubMenu
)

/**
 * 활성 경보·이벤트 목록 생성기.
 * 미결 사건(유민 800명·야간 협약·협곡 정찰대·의회 표결·야간 밀회·승격 조건)을 모아
 * 각 사건이 처리되는 서브 메뉴로 바로 연결한다.
 */
object StatusAlertBuilder {

    fun build(state: SlgGameState): List<StatusAlert> {
        val alerts = mutableListOf<StatusAlert>()

        if (state.refugeeChoice == null) {
            alerts += StatusAlert(
                id = "alert_refugee",
                title = "광교·용인 유민 800명 성문 앞",
                detail = "개방 배급(식량 -500) 과 선별 통제 중 결정 대기",
                severity = AlertSeverity.CRITICAL,
                target = SubMenu.EVENTS
            )
        }
        if (state.eventBChoice == null) {
            alerts += StatusAlert(
                id = "alert_scout",
                title = "광교산 협곡 정찰대 30여 명",
                detail = "산악 매복 요격 또는 첩자 파견 선택 대기",
                severity = AlertSeverity.WARNING,
                target = SubMenu.WAR
            )
        }
        if (state.eventAChoice == null) {
            alerts += StatusAlert(
                id = "alert_merchant",
                title = "원천동 상인연합 야간 협약",
                detail = "관세 면제 · 상호 보급 협정 미체결",
                severity = AlertSeverity.WARNING,
                target = SubMenu.EVENTS
            )
        }
        val pendingBills = state.bills.count { it.passed == null }
        if (pendingBills > 0) {
            alerts += StatusAlert(
                id = "alert_bills",
                title = "호족 의회 표결 대기 ${pendingBills}건",
                detail = "반대 65% 이상이면 연합 반란 위험",
                severity = AlertSeverity.WARNING,
                target = SubMenu.COUNCIL
            )
        }
        val nightCandidates = state.nightMeetingCandidates
        if (state.nightMeetingChoice == null && nightCandidates.isNotEmpty()) {
            val prime = nightCandidates.maxByOrNull { it.suspicion }
            alerts += StatusAlert(
                id = "alert_night_meeting",
                title = "${prime?.name ?: "의심 인물"} 밀회 제안 (의심도 ${prime?.suspicion ?: 0})",
                detail = "포섭 · 숙청 · 이중첩자 3지선다 미결",
                severity = AlertSeverity.WARNING,
                target = SubMenu.DIPLOMACY
            )
        }
        if (state.isPromotionEligible) {
            alerts += StatusAlert(
                id = "alert_promotion",
                title = "${state.nextPromotionStage?.displayName ?: "성주"} 승격 조건 충족",
                detail = "승격 의식 개최 시 의회 권한 +${state.nextPromotionStage?.councilAuthorityBonus ?: 0}%",
                severity = AlertSeverity.INFO,
                target = SubMenu.PROMOTION
            )
        }
        if (state.food < 1500) {
            alerts += StatusAlert(
                id = "alert_food",
                title = "군량 부족 (${state.food}석)",
                detail = "유민 개방 배급에 500석이 필요합니다",
                severity = AlertSeverity.CRITICAL,
                target = SubMenu.TERRITORY
            )
        }
        if (state.enemyFood in 1..1500) {
            alerts += StatusAlert(
                id = "alert_enemy_food",
                title = "광교동 적 군량 ${state.enemyFood}석",
                detail = "보급선 차단 · 무혈 항복 사정권",
                severity = AlertSeverity.INFO,
                target = SubMenu.WAR
            )
        }
        return alerts
    }
}
