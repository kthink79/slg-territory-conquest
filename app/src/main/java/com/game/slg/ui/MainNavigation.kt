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
    ),
    EMPIRE(
        id = "tab_empire",
        label = "영토",
        title = "영토·흡수",
        emoji = "🗺",
        accentHex = 0xFFE2B755
    );

    companion object {
        /** 하단 탭 순서 — 지도가 기본 화면 */
        val bottomOrder: List<MainTab> = listOf(MAP, DOMAIN, HERO, WAR, DIPLOMACY, EMPIRE)
    }
}

/** 영지 탭 상단 탭 — 내정·지표·시설·절기·기술·의회·승격·사건·행정 */
enum class DomainTab(val label: String, val icon: String) {
    HOME("내정", "🏠"),
    STATS("지표", "📊"),
    FACILITIES("시설", "🏗"),
    SEASON("절기", "🌾"),
    TECH("기술", "🔬"),
    COUNCIL("의회", "🏛"),
    PROMOTION("승격", "👑"),
    EVENTS("사건", "🎭"),
    ADMIN("행정", "🏢")
}

/** 영웅 탭 상단 탭 — 배치 / 영입 */
enum class HeroTab(val label: String, val icon: String) {
    PLACE("배치", "🎖"),
    RECRUIT("영입", "🔎")
}

/** 작전 탭 상단 탭 */
enum class WarTab(val label: String, val icon: String) {
    OPS("작전", "⚔"),
    STRATEGY("전략", "🧠"),
    REGION("지역", "🗺"),
    REPORT("전과", "📜"),
    DOCTRINE("원칙", "📕")
}

/** 영토 탭 상단 탭 — 흡수 / 통치 / 병력 */
enum class EmpireTab(val label: String, val icon: String) {
    ABSORB("흡수", "🤝"),
    GOVERN("통치", "🏛"),
    MANPOWER("병력", "⚔")
}

/** 외교 탭 상단 탭 */
enum class DiplomacyTab(val label: String, val icon: String) {
    TREATY("조약", "📜"),
    ESPIONAGE("첩보", "🕵"),
    TALENT("인재", "👤")
}

/** 기술 계통 탭 — 영지 탭 안 기술 화면의 계통 선택 */
enum class TechTab(val branch: String, val label: String, val icon: String) {
    ADMIN("내정", "내정", "🏠"),
    SCIENCE("과학기술", "과학", "🔬"),
    CULTURE("문화", "문화", "🎭"),
    MILITARY("군사", "군사", "⚔")
}

/** 영지 화면 안쪽 탭 — TerritoryScreen 이 쓰는 4개 */
enum class TerritoryTab(val label: String, val icon: String) {
    HOME("영지", "🏠"),
    STATS("지표", "📊"),
    SEASON("절기", "🌾"),
    FACILITIES("시설", "🏗")
}

/** 의회 화면 안쪽 탭 — 안건 / 연합 / 전쟁 / 긴급 / 리더 */
enum class CouncilTab(val label: String, val icon: String) {
    BILLS("안건", "📜"),
    COALITION("연합", "🤝"),
    WAR("전쟁", "⚔"),
    EMERGENCY("긴급", "🚨"),
    LORDS("리더", "👥")
}

/** 사건 화면 안쪽 탭 — 유민 / 상인 / 정찰대 */
enum class EventTab(val label: String, val icon: String) {
    REFUGEE("유민", "🚪"),
    MERCHANT("상인", "🌙"),
    SCOUT("정찰대", "🛰")
}

/** 승격 화면 안쪽 탭 — 로드맵 / 조건 / 경로 */
enum class PromotionTab(val label: String, val icon: String) {
    ROADMAP("로드맵", "🗺"),
    CONDITIONS("조건", "✅"),
    PATH("경로", "➡")
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
        MainTab.EMPIRE -> EmpireTab.entries.map { it.label }
    }

    /** 탭 라벨 앞에 붙는 아이콘 — "아이콘 + 1~2단어 라벨" 구성 */
    fun iconsOf(tab: MainTab): List<String> = when (tab) {
        MainTab.MAP -> emptyList()
        MainTab.DOMAIN -> DomainTab.entries.map { it.icon }
        MainTab.HERO -> HeroTab.entries.map { it.icon }
        MainTab.WAR -> WarTab.entries.map { it.icon }
        MainTab.DIPLOMACY -> DiplomacyTab.entries.map { it.icon }
        MainTab.EMPIRE -> EmpireTab.entries.map { it.icon }
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
        // 찾아온 영웅 — 합류를 청하는 방문 이벤트
        if (state.heroVisit != null) {
            alerts += StatusAlert(
                id = "alert_hero_visit",
                icon = "🚪",
                short = "합류 청함",
                title = "${state.heroVisit.heroName} 합류 청함",
                severity = AlertSeverity.WARNING,
                target = MainTab.HERO,
                targetSub = HeroTab.RECRUIT.ordinal
            )
        }
        // ── 인접 지역 · 연합 의회 · 긴급 방어 (2026-11-25) ──
        val pendingCoalition = state.coalition.pendingCount
        if (pendingCoalition > 0) {
            alerts += StatusAlert(
                id = "alert_coalition",
                icon = "🏛",
                short = "연합 안건 ${pendingCoalition}건",
                title = "지역 리더 연합 공동 작전 안건 ${pendingCoalition}건 표결 대기",
                severity = AlertSeverity.INFO,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.COUNCIL.ordinal
            )
        }
        val emergency = state.emergency
        if (emergency != null && emergency.pending) {
            alerts += StatusAlert(
                id = "alert_emergency",
                icon = "🚨",
                short = "긴급 의회",
                title = "${emergency.invaderName}의 침공 — 긴급 방어 의회 소집",
                severity = AlertSeverity.CRITICAL,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.COUNCIL.ordinal
            )
        }
        val aidCall = state.openAidCall
        if (aidCall != null) {
            alerts += StatusAlert(
                id = "alert_aid_call",
                icon = "🤝",
                short = "지원 요청",
                title = aidCall.headline,
                severity = AlertSeverity.WARNING,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.COUNCIL.ordinal
            )
        }
        // 태수 없는 편입 영지
        val noViceroy = state.territories.count { it.needsViceroy && it.absorbedBy != "인계" }
        if (noViceroy > 0) {
            alerts += StatusAlert(
                id = "alert_viceroy",
                icon = "🏛",
                short = "태수 ${noViceroy}곳",
                title = "태수 없는 영지 ${noViceroy}곳",
                severity = AlertSeverity.INFO,
                target = MainTab.EMPIRE,
                targetSub = EmpireTab.GOVERN.ordinal
            )
        }
        // 정규 병력 상한 도달 — 용병 안내
        if (state.troopCap > 0 && state.troops >= state.troopCap) {
            alerts += StatusAlert(
                id = "alert_troop_cap",
                icon = "⚔",
                short = "상한 도달",
                title = "정규 병력 상한 ${state.troopCap}명",
                severity = AlertSeverity.WARNING,
                target = MainTab.EMPIRE,
                targetSub = EmpireTab.MANPOWER.ordinal
            )
        }
        // ── 편입 대기 · 의회 전쟁 안건 (v0.10.0) ──
        if (state.pendingConquest != null) {
            alerts += StatusAlert(
                id = "alert_conquest",
                icon = "🏳",
                short = "편입 선택",
                title = state.pendingConquest.headline,
                severity = AlertSeverity.CRITICAL,
                target = MainTab.WAR,
                targetSub = WarTab.REGION.ordinal
            )
        }
        val pendingWar = state.warAgendas.count { it.pending }
        if (pendingWar > 0) {
            alerts += StatusAlert(
                id = "alert_war_agenda",
                icon = "⚔",
                short = "전쟁 안건 ${pendingWar}건",
                title = "의회 전쟁 안건 ${pendingWar}건 표결 대기",
                severity = AlertSeverity.WARNING,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.COUNCIL.ordinal
            )
        }
        if (state.adminEvaluation.isEligible) {
            alerts += StatusAlert(
                id = "alert_admin_promote",
                icon = "🏢",
                short = "행정 승격",
                title = "${state.adminTier.label} → ${state.adminTier.next?.label ?: "최종"} 승격 가능",
                severity = AlertSeverity.INFO,
                target = MainTab.DOMAIN,
                targetSub = DomainTab.ADMIN.ordinal
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

    /** 홈 상단 1줄 — 골드·식량·인구수·병력 (각각 눌러 늘리는 서브 메뉴가 열린다) */
    fun topSummary(state: SlgGameState): List<InfoEntry> = listOf(
        InfoEntry("💰", "골드", "${state.gold}", 0xFFE2B755),
        InfoEntry("🌾", "식량", "${state.food}", 0xFF81C784),
        InfoEntry("👥", "인구수", "${state.population}", 0xFFEDEAE3),
        InfoEntry("⚔", "병력", "${state.troops}", 0xFF7FA4D6)
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
            add(InfoEntry("❤", "민심", "${state.morale}", 0xFFEDEAE3))
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
            add(
                InfoEntry(
                    "🗺",
                    "영토",
                    "${state.territories.size}곳 · 편입 ${state.absorbedCount} · 연방 +${state.coalitionBonusPercent}%",
                    0xFFE2B755
                )
            )
            add(
                InfoEntry(
                    "⚔",
                    "상한",
                    "정규 ${state.troops}/${state.troopCap} · 용병 ${state.mercenaryTroops}",
                    0xFF7FA4D6
                )
            )
            add(InfoEntry("🛠", "무기", "Lv.${state.weaponTech.totalLevels} ×${"%.2f".format(state.weaponTech.multiplier)}", 0xFF64B5F6))
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

/**
 * 하단 탭 배지 → 상단 서브 메뉴 배지 분배.
 *
 * 사용자 요청(2026-11-25 07:45): *"하단 탭(영지 3, 작전 1, 외교 1, 영토 1)에 표시되는 숫자 배지가
 * 해당 탭 내부의 상단 서브 메뉴 버튼(내정/지표/시설/절기/기술/의회/승격/사건 등)에도 동일하게
 * 연동·분배되어 어디를 확인해야 하는지 바로 알 수 있게 한다."*
 *
 * 경보 1건은 이미 (탭, 서브) 위치를 알고 있으므로, 그 위치로 숫자를 나눠 주기만 하면 된다.
 * 하단 탭 숫자 = 그 탭 서브 배지의 합 — 두 숫자가 어긋나지 않는다.
 */
object SubMenuBadges {

    /** 탭의 서브 메뉴별 배지 수 (서브 탭이 없으면 빈 목록) */
    fun countsOf(state: SlgGameState, tab: MainTab): List<Int> {
        val count = ScreenTabs.countOf(tab)
        if (count <= 0) return emptyList()
        val alerts = StatusAlertBuilder.build(state).filter { it.target == tab }
        return List(count) { sub -> alerts.count { it.targetSub == sub } }
    }

    /** 하단 탭 배지 — 그 탭의 모든 상단 서브 배지 합 */
    fun tabBadge(state: SlgGameState, tab: MainTab): Int {
        val counts = countsOf(state, tab)
        return if (counts.isEmpty()) {
            StatusAlertBuilder.build(state).count { it.target == tab }
        } else {
            counts.sum()
        }
    }

    /** 상단 서브 메뉴 배지 하나 */
    fun subBadge(state: SlgGameState, tab: MainTab, sub: Int): Int =
        countsOf(state, tab).getOrElse(sub) { 0 }

    /** 의회 안쪽 탭(안건·연합·전쟁·긴급·리더) 배지 — 3단 분배 */
    fun councilInnerCounts(state: SlgGameState): List<Int> {
        val alerts = StatusAlertBuilder.build(state).filter {
            it.target == MainTab.DOMAIN && it.targetSub == DomainTab.COUNCIL.ordinal
        }
        return listOf(
            alerts.count { it.id == "alert_bills" },
            alerts.count { it.id == "alert_coalition" },
            alerts.count { it.id == "alert_war_agenda" },
            alerts.count { it.id == "alert_emergency" || it.id == "alert_aid_call" },
            0
        )
    }
}
