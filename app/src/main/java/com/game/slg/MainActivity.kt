package com.game.slg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.game.slg.core.map.MapViewport
import com.game.slg.ui.AndroidCampaignStore
import com.game.slg.ui.CouncilScreen
import com.game.slg.ui.DiplomacyScreen
import com.game.slg.ui.EventScenarioScreen
import com.game.slg.ui.GameStage
import com.game.slg.ui.LordSelectScreen
import com.game.slg.ui.MainRoute
import com.game.slg.ui.PromotionScreen
import com.game.slg.ui.RegionSelectScreen
import com.game.slg.ui.SlgColors
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.SlgInfoOverlay
import com.game.slg.ui.SlgIntroScreen
import com.game.slg.ui.SlgMainScreen
import com.game.slg.ui.SubMenu
import com.game.slg.ui.SubScreenHost
import com.game.slg.ui.TechTreeScreen
import com.game.slg.ui.TerritoryScreen
import com.game.slg.ui.WarScreen

/**
 * 「단일 연방의 황혼」 — 2020년대 대한민국 자치구 영지전.
 *
 * 화면 흐름
 *  · 인트로(4장면) → 시작 지역 선택 → AI 호족 인계 → 메인
 *  · 메인 : 지도(주인공) + 경보 칩 1줄 + 서브 메뉴 8칸, 상단은 자원 1줄
 *  · 상세 : «메인» + 제목 + 2~4칸 탭 바 + 그림·배지 중심 본문
 */
class MainActivity : ComponentActivity() {
    private lateinit var gameViewModel: SlgGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameViewModel = SlgGameViewModel(store = AndroidCampaignStore(applicationContext))
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = SlgColors.Brass,
                    background = SlgColors.Background,
                    surface = SlgColors.Surface,
                    onSurface = SlgColors.Text,
                    onBackground = SlgColors.Text
                )
            ) {
                SlgApp(gameViewModel)
            }
        }
    }
}

@Composable
fun SlgApp(viewModel: SlgGameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val navigation by viewModel.navigation.collectAsState()
    var mapViewport by remember { mutableStateOf(MapViewport()) }
    var infoOpen by rememberSaveable { mutableStateOf(false) }
    var introIndex by rememberSaveable { mutableIntStateOf(0) }

    // 인트로로 돌아올 때(다시보기)는 항상 첫 장면부터
    LaunchedEffect(state.stage) {
        if (state.stage == GameStage.INTRO) introIndex = 0
    }

    // 📊 정보 모달이 열려 있으면 뒤로가기는 모달만 닫는다
    BackHandler(enabled = infoOpen) { infoOpen = false }
    // 상세 화면에서 뒤로가기 → 메인
    BackHandler(enabled = !infoOpen && state.stage == GameStage.PLAY && !navigation.isHome) {
        viewModel.backToMain()
    }
    // 호족 선택에서 뒤로가기 → 지역 선택
    BackHandler(enabled = !infoOpen && state.stage == GameStage.LORD_SELECT) {
        viewModel.backToRegionSelect()
    }
    // 지역 선택에서 뒤로가기 → 인트로
    BackHandler(enabled = !infoOpen && state.stage == GameStage.REGION_SELECT) {
        viewModel.replayIntro()
    }

    Scaffold(containerColor = SlgColors.Background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.stage) {
                GameStage.INTRO -> SlgIntroScreen(
                    sceneIndex = introIndex,
                    onNext = { introIndex = (introIndex + 1).coerceAtMost(3) },
                    onSkip = {
                        introIndex = 0
                        viewModel.finishIntro()
                    }
                )

                GameStage.REGION_SELECT -> RegionSelectScreen(
                    onChoose = { code ->
                        viewModel.chooseStartRegion(code)
                        mapViewport = MapViewport()
                    },
                    onBack = { viewModel.replayIntro() }
                )

                GameStage.LORD_SELECT -> LordSelectScreen(
                    state = state,
                    onPick = { id ->
                        viewModel.chooseLord(id)
                        mapViewport = MapViewport()
                    },
                    onBack = { viewModel.backToRegionSelect() }
                )

                GameStage.PLAY -> PlaySurface(
                    state = state,
                    navigation = navigation,
                    viewModel = viewModel,
                    mapViewport = mapViewport,
                    onViewportChange = { mapViewport = it },
                    infoOpen = infoOpen,
                    onInfoOpenChange = { infoOpen = it }
                )
            }
        }
    }
}

@Composable
private fun PlaySurface(
    state: com.game.slg.ui.SlgGameState,
    navigation: com.game.slg.ui.MainNavigationState,
    viewModel: SlgGameViewModel,
    mapViewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    infoOpen: Boolean,
    onInfoOpenChange: (Boolean) -> Unit
) {
    when (val route = navigation.route) {
        is MainRoute.Home -> SlgMainScreen(
            state = state,
            viewport = mapViewport,
            onViewportChange = onViewportChange,
            onSelectTerritory = { viewModel.selectTerritory(it) },
            onOpenMenu = { viewModel.openSubMenu(it) },
            onAdvanceSeason = { viewModel.advanceSeason() },
            onOpenInfo = { onInfoOpenChange(true) },
            onDismissLevelUp = { viewModel.dismissLevelUp() }
        )

        is MainRoute.Detail -> SubScreenHost(
            menu = route.menu,
            onBack = { viewModel.backToMain() }
        ) { tab ->
            when (route.menu) {
                SubMenu.TERRITORY -> TerritoryScreen(
                    state = state,
                    onAdvanceSeason = { viewModel.advanceSeason() },
                    onDismissCeremony = { viewModel.dismissCeremony() },
                    tabIndex = tab,
                    onSelectTerritory = { viewModel.selectTerritory(it) },
                    onBuildFacility = { viewModel.buildFacility(it) }
                )

                SubMenu.TECH -> TechTreeScreen(
                    state = state,
                    onResearch = { id -> viewModel.research(id) },
                    tabIndex = tab
                )

                SubMenu.COUNCIL -> CouncilScreen(
                    state = state,
                    onVoteBill = { id -> viewModel.voteBill(id) },
                    tabIndex = tab
                )

                SubMenu.WAR -> WarScreen(
                    state = state,
                    onRunOperation = { id -> viewModel.runOperation(id) },
                    tabIndex = tab
                )

                SubMenu.DIPLOMACY -> DiplomacyScreen(
                    state = state,
                    onProposeTreaty = { type -> viewModel.proposeTreaty(type) },
                    onSecureCasusBelli = { viewModel.secureCasusBelli() },
                    onEspionage = { op -> viewModel.conductEspionage(op) },
                    onOpenNightMeeting = { name -> viewModel.openNightMeeting(name) },
                    onResolveNightMeeting = { choice -> viewModel.resolveNightMeeting(choice) },
                    onExploitDoubleAgent = { viewModel.exploitDoubleAgent() },
                    tabIndex = tab
                )

                SubMenu.EVENTS -> EventScenarioScreen(
                    state = state,
                    onChooseEventA = { accept -> viewModel.chooseEventA(accept) },
                    onChooseEventB = { ambush -> viewModel.chooseEventB(ambush) },
                    onChooseRefugee = { choiceId -> viewModel.chooseRefugeeCrisis(choiceId) }
                )

                SubMenu.PROMOTION -> PromotionScreen(
                    state = state,
                    onPromote = { viewModel.promote() },
                    onDismissCeremony = { viewModel.dismissCeremony() }
                )

                SubMenu.SEASON -> Unit
            }
        }
    }

    if (infoOpen) {
        SlgInfoOverlay(
            state = state,
            onClose = { onInfoOpenChange(false) },
            onOpenMenu = { viewModel.openSubMenu(it) },
            onReplayIntro = { viewModel.replayIntro() },
            onRestart = { viewModel.restartCampaign() }
        )
    }
}
