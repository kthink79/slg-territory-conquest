package com.game.slg.ui

import com.game.slg.core.model.FacilityType

/**
 * 메인 하단 탭과 화면별 상단 탭 구조.
 *
 * 사용자 요청: *"SLG 게임이 아직도 글씨가 너무 작아 메뉴를 다 넣을 공간이 없다면
 * 탭을 추가해서라도 조금 더 키워줘."*
 *
 * 그래서 메뉴를 한 화면에 우겨넣지 않고 두 층으로 나눈다.
 *  · 하단 탭 5개 — 지도 / 영지 / 영웅 / 작전 / 외교
 *  · 상단 탭 2~7개 — 그 탭 안의 세부 화면 (라벨은 2~4글자)
 *
 * 탭 라벨은 전부 짧게 두고, 글자 크기는 [SlgType]에서 키운다.
 */
enum class MainTab(
    val id: String,
    /** 하단 탭 라벨 — 2글자 */
    val label: String,
    /** 화면 제목 */
    val title: String,
    val emoji: String,
    val accentHex: Long
) {
    MAP(
        id = "tab_map",
        label = "지도",
        title = "관할 지도",
        emoji = "🗺",
        accentHex = 0xFF7FA4D6
    ),
    DOMAIN(
        id = "tab_domain",
        label = "영지",
        title = "영지·내정",
        emoji = "🏙",
        accentHex = 0xFFE2B755
    ),
    HERO(
        id = "tab_hero",
        label = "영웅",
        title = "영웅·인재",
        emoji = "🦸",
        accentHex = 0xFFB39DDB
    ),
    WAR(
        id = "tab_war",
        label = "작전",
        title = "작전·전장",
        emoji = "⚔",
        accentHex = 0xFFE57373
    ),
    DIPLOMACY(
        id = "tab_diplomacy",
        label = "외교",
        title = "외교·첩보",
        emoji = "🤝",
        accentHex = 0xFF81C784
    );

    companion object {
        /** 하단 탭 순서 — 지도가 기본 화면 */
        val bottomOrder: List<MainTab> = listOf(MAP, DOMAIN, HERO, WAR, DIPLOMACY)
    }
}

/** 영지 탭 상단 탭 — 내정·시설·기술·의회·승격·사건 */
enum class DomainTab(val label: String) {
    HOME("내정"),
    STATS("지표"),
    FACILITIES("시설"),
    SEASON("절기"),
    TECH("기술"),
    COUNCIL("의회"),
    PROMOTION("승격"),
    EVENTS("사건")
}

/** 영웅 탭 상단 탭 — 배치 / 영입 */
enum class HeroTab(val label: String) {
    PLACE("배치"),
    RECRUIT("영입")
}

/** 작전 탭 상단 탭 */
enum class WarTab(val label: String) {
    OPS("작전"),
    REGION("지역"),
    REPORT("전과"),
    DOCTRINE("원칙")
}

/** 외교 탭 상단 탭 */
enum class DiplomacyTab(val label: String) {
    TREATY("조약"),
    ESPIONAGE("첩보"),
    TALENT("인재")
}

/** 기술 계통 탭 — 영지 탭 안 기술 화면의 계통 선택 */
enum class TechTab(val branch: String, val label: String) {
    ADMIN("내정", "내정"),
    SCIENCE("과학기술", "과학"),
    CULTURE("문화", "문화"),
    MILITARY("군사", "군사")
}

/** 영지 화면 안쪽 탭 — TerritoryScreen 이 쓰는 4개 */
enum class TerritoryTab(val label: String) {
    HOME("영지"),
    STATS("지표"),
    SEASON("절기"),
    FACILITIES("시설")
}

/** 의회 화면 안쪽 탭 — 안건 / 리더 명부 */
enum class CouncilTab(val label: String) {
    BILLS("안건"),
    LORDS("리더")
}

/** 사건 화면 안쪽 탭 — 유민 / 상인 / 정찰대 */
enum class EventTab(val label: String) {
    REFUGEE("유민"),
    MERCHANT("상인"),
    SCOUT("정찰대")
}

/** 승격 화면 안쪽 탭 — 로드맵 / 조건 / 경로 */
enum class PromotionTab(val label: String) {
    ROADMAP("로드맵"),
    CONDITIONS("조건"),
    PATH("경로")
}

/** 화면 탭 목록 */
object ScreenTabs {

    /** 한 줄에 놓는 탭 수 — 넘치면 다음 줄로 넘긴다 */
    const val PER_ROW = 4

    fun labelsOf(tab: MainTab): List<String> = when (tab) {
        MainTab.MAP -> emptyList()
        MainTab.DOMAIN -> DomainTab.entries.map { it.label }
        MainTab.HERO -> HeroTab.entries.map { it.label }
        MainTab.WAR -> WarTab.entries.map { it.label }
        MainTab.DIPLOMACY -> DiplomacyTab.entries.map { it.label }
    }

    fun countOf(tab: MainTab): Int = labelsOf(tab).size
}

/**
 * 하단 탭 + 상단 탭 선택 상태.
 * 화면 없이 JVM에서 검증할 수 있도록 순수 데이터로 둔다.
 */
data class MainNavigationState(
    val tab: MainTab = MainTab.MAP,
    val subIndex: Int = 0
) {
    /** 지도 탭이 곧 메인 화면이다 */
    val isHome: Boolean get() = tab == MainTab.MAP

    /** 열려 있는 탭 (지도 탭이면 null) */
    val openedTab: MainTab? get() = if (isHome) null else tab

    /** 현재 상단 탭 인덱스 (탭이 없는 화면이면 0) */
    val sub: Int get() = subIndex.coerceIn(0, (ScreenTabs.countOf(tab) - 1).coerceAtLeast(0))

    fun open(next: MainTab, sub: Int = 0): MainNavigationState =
        if (next == MainTab.MAP) MainNavigationState()
        else MainNavigationState(
            tab = next,
            subIndex = sub.coerceIn(0, (ScreenTabs.countOf(next) - 1).coerceAtLeast(0))
        )

    fun selectSub(index: Int): MainNavigationState = copy(
        subIndex = index.coerceIn(0, (ScreenTabs.countOf(tab) - 1).coerceAtLeast(0))
    )

    /** '‹ 메인' 또는 하단 지도 탭 */
    fun back(): MainNavigationState = MainNavigationState()
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
    val target: MainTab,
    val targetSub: Int = 0
)

/**
 * 활성 경보 생성기. 미결 사건만 모아 아이콘·짧은 라벨로 줄이고,
 * 각 사건이 처리되는 화면으로 바로 연결한다.
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
                target = MainTab.DOMAIN,
                targetSub = DomainTab.EVENTS.ordinal
            )
        }
        if (state.eventBChoice == null) {
            alerts += StatusAlert(
                id = "alert_scout",
                icon = "🛰",
                short = "무인기 3",
                title = "무인기 정찰조 출현",
                severity = AlertSeverity.WARNING,
                target = MainTab.WAR,
                targetSub = WarTab.OPS.ordinal
            )
        }
        if (state.eventAChoice == null) {
            alerts += StatusAlert(
                id = "alert_merchant",
                icon = "🌙",
                short = "야간 물류",
                title = "물류 상인회 협약",
                severity = AlertSeverity.WARNING,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.EVENTS.ordinal
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
                target = MainTab.DOMAIN,
                targetSub = DomainTab.COUNCIL.ordinal
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
                target = MainTab.DIPLOMACY,
                targetSub = DiplomacyTab.TALENT.ordinal
            )
        }
        if (state.isPromotionEligible) {
            alerts += StatusAlert(
                id = "alert_promotion",
                icon = "👑",
                short = "승격 가능",
                title = "승격 조건 충족",
                severity = AlertSeverity.INFO,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.PROMOTION.ordinal
            )
        }
        if (state.food < 1500) {
            alerts += StatusAlert(
                id = "alert_food",
                icon = "🌾",
                short = "보급 부족",
                title = "보급 ${state.food}석",
                severity = AlertSeverity.CRITICAL,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.HOME.ordinal
            )
        }
        if (state.enemyFood in 1..1500) {
            alerts += StatusAlert(
                id = "alert_enemy_food",
                icon = "🎯",
                short = "적 보급",
                title = "적 보급 ${state.enemyFood}석",
                severity = AlertSeverity.INFO,
                target = MainTab.WAR,
                targetSub = WarTab.OPS.ordinal
            )
        }
        if (state.facilities.size < FacilityType.entries.size && state.gold >= 700) {
            alerts += StatusAlert(
                id = "alert_facility",
                icon = "🏗",
                short = "시설 가능",
                title = "시설 건설 가능",
                severity = AlertSeverity.INFO,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.FACILITIES.ordinal
            )
        }
        // 지역 연합이 곧 끝난다 — 외교 화면에서 대비
        if (state.union.active && state.union.remainingTurns <= 2) {
            alerts += StatusAlert(
                id = "alert_union",
                icon = "🛡",
                short = "연합 ${state.union.remainingTurns}턴",
                title = "지역 연합이 ${state.union.remainingTurns}턴 뒤 끝납니다",
                severity = AlertSeverity.WARNING,
                target = MainTab.DIPLOMACY,
                targetSub = DiplomacyTab.TREATY.ordinal
            )
        }
        // 영입 대기 영웅 — 설득이 끝났으면 고용만 남았다
        val hireReady = state.heroes.recruitables.count {
            state.heroes.recordOf(it.id).stage == com.game.slg.core.hero.HeroStage.PERSUADED
        }
        if (hireReady > 0) {
            alerts += StatusAlert(
                id = "alert_hero_hire",
                icon = "🦸",
                short = "고용 ${hireReady}명",
                title = "설득을 마친 영웅 ${hireReady}명 — 고용만 남았습니다",
                severity = AlertSeverity.INFO,
                target = MainTab.HERO,
                targetSub = HeroTab.RECRUIT.ordinal
            )
        }
        return alerts
    }
}

/**
 * 상단 1줄 요약 + 📊 정보 패널 데이터.
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

    /** 📊 정보 패널 — 지역·리더 정원·지휘 단계·세력 지표 */
    fun detailRows(state: SlgGameState): List<InfoEntry> {
        val next = state.nextPromotionStage
        return buildList {
            add(InfoEntry("🗺", "지역", state.regionName, 0xFF7FA4D6))
            add(InfoEntry("👥", "인구", "%,d".format(state.regionPopulation), 0xFFEDEAE3))
            add(InfoEntry("🏙", "리더", "${state.registeredLords}/${state.lordCapacity}석", 0xFFE2B755))
            add(InfoEntry("🎖", "지휘", "Lv.${state.command.level}", 0xFFE2B755))
            add(InfoEntry("🌫", "해금", state.expansionTier.label, 0xFF7FA4D6))
            add(InfoEntry("🗓", "절기", state.seasonName.substringBefore(" ("), 0xFF90A4AE))
            add(InfoEntry("🏗", "시설", "${state.facilities.size}곳", 0xFF90A4AE))
            add(InfoEntry("🤝", "우호", "${state.alliedTerritories}곳", 0xFF7FA4D6))
            add(InfoEntry("⭐", "명성", "${state.prestige}", 0xFFE2B755))
            add(InfoEntry("🎭", "첩보", "${state.espionagePower}", 0xFFB39DDB))
            add(InfoEntry("🎯", "적량", "${state.enemyFood}", 0xFFE57373))
            add(
                InfoEntry(
                    "🛡",
                    "연합",
                    if (state.union.active) "보호 ${state.union.remainingTurns}턴" else "종료",
                    0xFF81C784
                )
            )
            add(InfoEntry("🦸", "영웅", "${state.heroes.ownedCount}명 · 배치 ${state.heroes.placedCount}", 0xFFB39DDB))
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
