package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 하단 탭 하나의 화면 틀 — «지도로» + 제목 + 상단 탭 + 본문.
 *
 * 상단 탭은 [ScreenTabs]가 정하고(최대 7개, 두 줄까지), 안쪽에 한 번 더
 * 작은 선택 줄(기술 계통 · 의회 안건/리더 · 사건 3종)을 둔다.
 * 탭을 늘린 만큼 한 화면에 담는 항목이 줄어 글씨를 키울 공간이 생긴다.
 */
@Composable
fun SlgTabSurface(
    state: SlgGameState,
    navigation: MainNavigationState,
    viewModel: SlgGameViewModel,
    onAdvanceSeason: () -> Unit
) {
    val tab = navigation.tab
    val accent = Color(tab.accentHex)

    Column(modifier = Modifier.fillMaxSize().background(SlgColors.Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E1218))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlgBackButton(onBack = { viewModel.backToMain() })
            Spacer(Modifier.width(8.dp))
            Text(
                "${tab.emoji} ${tab.title}",
                color = SlgColors.Text,
                fontSize = SlgType.BODY,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (state.union.active) SlgColors.Success.copy(alpha = 0.16f)
                        else SlgColors.SurfaceAlt
                    )
                    .border(
                        1.dp,
                        if (state.union.active) SlgColors.Success.copy(alpha = 0.55f) else SlgColors.Line,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    state.union.chipLabel,
                    color = if (state.union.active) SlgColors.Success else SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        val labels = ScreenTabs.labelsOf(tab)
        if (labels.isNotEmpty()) {
            SlgTabBar(
                tabs = labels,
                selectedIndex = navigation.sub,
                accent = accent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                onSelect = { index -> viewModel.selectSubTab(index) }
            )
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = SlgLayout.MAX_CONTENT_WIDTH.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
            when (tab) {
                MainTab.MAP -> Unit
                MainTab.DOMAIN -> DomainSurface(state, navigation.sub, viewModel, onAdvanceSeason)
                MainTab.HERO -> HeroScreen(
                    state = state,
                    onScout = { viewModel.scoutHero(it) },
                    onPersuade = { viewModel.persuadeHero(it) },
                    onHire = { viewModel.hireHero(it) },
                    onAssign = { id, role -> viewModel.assignHero(id, role) },
                    onUnassign = { viewModel.unassignHero(it) },
                    tabIndex = navigation.sub,
                    onSearch = { viewModel.searchHeroes() },
                    onAcceptVisit = { viewModel.acceptHeroVisit() },
                    onDeclineVisit = { viewModel.declineHeroVisit() },
                    onDismiss = { viewModel.dismissHero(it) }
                )

                MainTab.WAR -> WarSurface(state, navigation.sub, viewModel)
                MainTab.DIPLOMACY -> DiplomacySurface(state, navigation.sub, viewModel)
                MainTab.EMPIRE -> EmpireScreen(
                    state = state,
                    onSelectMethod = { method, factionId -> viewModel.attemptAbsorption(method, factionId) },
                    onCreateCommonEnemy = { viewModel.createCommonEnemy(it) },
                    onInstigate = { viewModel.instigateFaction(it) },
                    onSwitchTerritory = { viewModel.switchManagedTerritory(it) },
                    onAssignViceroy = { heroId, code -> viewModel.assignViceroy(heroId, code) },
                    onUnassignViceroy = { viewModel.unassignViceroy(it) },
                    onDevelopTerritory = { viewModel.developTerritory(it) },
                    onRecruitRegular = { viewModel.recruitRegular() },
                    onHireMercenary = { viewModel.hireMercenary() },
                    onDismissMercenary = { viewModel.dismissMercenary(it) },
                    onGatherPrivateTroops = { viewModel.gatherPrivateTroops() },
                    onReportMerit = { viewModel.reportMerit() },
                    onDeclareIndependence = { viewModel.declareIndependence() },
                    onRecruitPending = { viewModel.recruitPendingHero(it) },
                    onReleasePending = { viewModel.releasePendingHero(it) },
                    tabIndex = navigation.sub
                )
            }
            }
        }
    }
}

/** 안쪽 선택 줄 — 기술 계통 · 의회 안건/리더 · 사건 3종 */
@Composable
fun SlgInlineTabs(
    labels: List<String>,
    selectedIndex: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) accent.copy(alpha = 0.22f) else SlgColors.SurfaceAlt)
                    .border(
                        1.dp,
                        if (selected) accent.copy(alpha = 0.8f) else SlgColors.Line.copy(alpha = 0.6f),
                        RoundedCornerShape(50)
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) accent else SlgColors.Muted,
                    fontSize = SlgType.LABEL,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/** 영지 탭 — 내정 / 지표 / 시설 / 기술 / 의회 / 승격 / 사건 */
@Composable
private fun DomainSurface(
    state: SlgGameState,
    sub: Int,
    viewModel: SlgGameViewModel,
    onAdvanceSeason: () -> Unit
) {
    val domainTab = DomainTab.entries[sub.coerceIn(0, DomainTab.entries.lastIndex)]
    when (domainTab) {
        DomainTab.HOME -> TerritoryScreen(
            state = state,
            onAdvanceSeason = onAdvanceSeason,
            onDismissCeremony = { viewModel.dismissCeremony() },
            tabIndex = 0,
            onSelectTerritory = { viewModel.selectTerritory(it) },
            onOpenRegionDetail = { viewModel.openRegionDetail(it) },
            onBuildFacility = { viewModel.buildFacility(it) }
        )

        DomainTab.STATS -> TerritoryScreen(
            state = state,
            onAdvanceSeason = onAdvanceSeason,
            onDismissCeremony = { viewModel.dismissCeremony() },
            tabIndex = 1,
            onSelectTerritory = { viewModel.selectTerritory(it) },
            onOpenRegionDetail = { viewModel.openRegionDetail(it) },
            onBuildFacility = { viewModel.buildFacility(it) }
        )

        DomainTab.FACILITIES -> TerritoryScreen(
            state = state,
            onAdvanceSeason = onAdvanceSeason,
            onDismissCeremony = { viewModel.dismissCeremony() },
            tabIndex = 3,
            onSelectTerritory = { viewModel.selectTerritory(it) },
            onOpenRegionDetail = { viewModel.openRegionDetail(it) },
            onBuildFacility = { viewModel.buildFacility(it) }
        )

        DomainTab.SEASON -> TerritoryScreen(
            state = state,
            onAdvanceSeason = onAdvanceSeason,
            onDismissCeremony = { viewModel.dismissCeremony() },
            tabIndex = 2,
            onSelectTerritory = { viewModel.selectTerritory(it) },
            onOpenRegionDetail = { viewModel.openRegionDetail(it) },
            onBuildFacility = { viewModel.buildFacility(it) }
        )

        DomainTab.TECH -> {
            var branch by rememberSaveable { mutableIntStateOf(0) }
            Column(Modifier.fillMaxSize()) {
                SlgInlineTabs(
                    labels = TechTab.entries.map { it.label },
                    selectedIndex = branch,
                    accent = Color(0xFF64B5F6),
                    onSelect = { branch = it }
                )
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    TechTreeScreen(
                        state = state,
                        onResearch = { viewModel.research(it) },
                        tabIndex = branch
                    )
                }
            }
        }

        DomainTab.COUNCIL -> {
            var inner by rememberSaveable { mutableIntStateOf(0) }
            Column(Modifier.fillMaxSize()) {
                SlgInlineTabs(
                    labels = CouncilTab.entries.map { it.label },
                    selectedIndex = inner,
                    accent = SlgColors.Steel,
                    onSelect = { inner = it }
                )
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    CouncilScreen(
                        state = state,
                        onVoteBill = { viewModel.voteBill(it) },
                        tabIndex = inner
                    )
                }
            }
        }

        DomainTab.PROMOTION -> {
            var inner by rememberSaveable { mutableIntStateOf(0) }
            Column(Modifier.fillMaxSize()) {
                SlgInlineTabs(
                    labels = PromotionTab.entries.map { it.label },
                    selectedIndex = inner,
                    accent = SlgColors.Brass,
                    onSelect = { inner = it }
                )
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    PromotionScreen(
                        state = state,
                        onPromote = { viewModel.promote() },
                        onDismissCeremony = { viewModel.dismissCeremony() },
                        tabIndex = inner
                    )
                }
            }
        }

        DomainTab.EVENTS -> {
            var inner by rememberSaveable { mutableIntStateOf(0) }
            Column(Modifier.fillMaxSize()) {
                SlgInlineTabs(
                    labels = EventTab.entries.map { it.label },
                    selectedIndex = inner,
                    accent = SlgColors.Violet,
                    onSelect = { inner = it }
                )
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    EventScenarioScreen(
                        state = state,
                        onChooseEventA = { viewModel.chooseEventA(it) },
                        onChooseEventB = { viewModel.chooseEventB(it) },
                        onChooseRefugee = { viewModel.chooseRefugeeCrisis(it) },
                        tabIndex = inner
                    )
                }
            }
        }
    }
}

/** 작전 탭 — 작전 / 지역 / 전과 / 원칙 */
@Composable
private fun WarSurface(state: SlgGameState, sub: Int, viewModel: SlgGameViewModel) {
    when (WarTab.entries[sub.coerceIn(0, WarTab.entries.lastIndex)]) {
        WarTab.OPS -> WarScreen(
            state = state,
            onRunOperation = { viewModel.runOperation(it) },
            tabIndex = WarTab.OPS.ordinal,
            onSelectStrategy = { viewModel.selectBattleStrategy(it) },
            onResearchTech = { viewModel.researchWeaponTech(it) }
        )

        WarTab.STRATEGY -> WarScreen(
            state = state,
            onRunOperation = { viewModel.runOperation(it) },
            tabIndex = WarTab.STRATEGY.ordinal,
            onSelectStrategy = { viewModel.selectBattleStrategy(it) },
            onResearchTech = { viewModel.researchWeaponTech(it) }
        )

        WarTab.REGION -> RegionLordsScreen(
            state = state,
            onChallenge = { viewModel.challengeRegionLord(it) }
        )

        WarTab.REPORT -> WarScreen(
            state = state,
            onRunOperation = { viewModel.runOperation(it) },
            tabIndex = WarTab.REPORT.ordinal
        )

        WarTab.DOCTRINE -> WarScreen(
            state = state,
            onRunOperation = { viewModel.runOperation(it) },
            tabIndex = WarTab.DOCTRINE.ordinal
        )
    }
}

/** 외교 탭 — 조약 / 첩보 / 인재 */
@Composable
private fun DiplomacySurface(state: SlgGameState, sub: Int, viewModel: SlgGameViewModel) {
    DiplomacyScreen(
        state = state,
        onProposeTreaty = { viewModel.proposeTreaty(it) },
        onSecureCasusBelli = { viewModel.secureCasusBelli() },
        onEspionage = { viewModel.conductEspionage(it) },
        onOpenNightMeeting = { viewModel.openNightMeeting(it) },
        onResolveNightMeeting = { viewModel.resolveNightMeeting(it) },
        onExploitDoubleAgent = { viewModel.exploitDoubleAgent() },
        tabIndex = sub
    )
}
