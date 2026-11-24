package com.game.slg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.game.slg.ui.GameStage
import com.game.slg.ui.LordSelectScreen
import com.game.slg.ui.MainNavigationState
import com.game.slg.ui.MainTab
import com.game.slg.ui.RegionSelectScreen
import com.game.slg.ui.SlgBottomNav
import com.game.slg.ui.SlgColors
import com.game.slg.ui.SlgGameState
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.RegionDetailMapOverlay
import com.game.slg.ui.SlgInfoOverlay
import com.game.slg.ui.SlgIntroScreen
import com.game.slg.ui.SlgMainScreen
import com.game.slg.ui.SlgTabSurface
import com.game.slg.ui.StatusAlertBuilder

/**
 * 「단일 연방의 황혼」 — 2020년대 대한민국 자치구 영지전.
 *
 * 화면 흐름
 *  · 인트로(4장면) → 시작 지역 선택 → AI 리더 인계 → 메인
 *  · 하단 탭 5개: 지도 / 영지 / 영웅 / 작전 / 외교
 *  · 그 안의 상단 탭이 세부 화면을 나누고, 글씨를 키운 만큼 한 화면 항목을 줄인다.
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
    // 지역 세부맵이 열려 있으면 뒤로가기는 세부맵만 닫는다
    BackHandler(enabled = !infoOpen && state.regionDetailCode != null) {
        viewModel.closeRegionDetail()
    }
    // 상세 탭에서 뒤로가기 → 지도
    BackHandler(enabled = !infoOpen && state.stage == GameStage.PLAY && !navigation.isHome) {
        viewModel.backToMain()
    }
    // 리더 선택에서 뒤로가기 → 지역 선택
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
                    onBack = { viewModel.backToRegionSelect() },
                    onChooseStartMode = { viewModel.chooseStartMode(it) },
                    onPickHero = { viewModel.pickHeroForStart(it) },
                    onRequestEmployment = { lordId ->
                        viewModel.requestEmployment(lordId)
                        if (viewModel.uiState.value.stage == GameStage.PLAY) {
                            mapViewport = MapViewport()
                        }
                    }
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
    state: SlgGameState,
    navigation: MainNavigationState,
    viewModel: SlgGameViewModel,
    mapViewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    infoOpen: Boolean,
    onInfoOpenChange: (Boolean) -> Unit
) {
    val alerts = StatusAlertBuilder.build(state)
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (navigation.isHome) {
                SlgMainScreen(
                    state = state,
                    viewport = mapViewport,
                    onViewportChange = onViewportChange,
                    onSelectTerritory = { viewModel.selectTerritory(it) },
                    onOpenTab = { tab, sub -> viewModel.openTab(tab, sub) },
                    onAdvanceSeason = { viewModel.advanceSeason() },
                    onOpenInfo = { onInfoOpenChange(true) },
                    onDismissLevelUp = { viewModel.dismissLevelUp() },
                    onBoost = { viewModel.boostResource(it) },
                    onOpenRegionDetail = { viewModel.openRegionDetail() }
                )
            } else {
                SlgTabSurface(
                    state = state,
                    navigation = navigation,
                    viewModel = viewModel,
                    onAdvanceSeason = { viewModel.advanceSeason() }
                )
            }
        }

        // 하단 탭 — 어느 화면에서든 한 번에 다른 탭으로 이동
        SlgBottomNav(
            selected = navigation.tab,
            badgeOf = { tab -> alerts.count { it.target == tab } },
            onSelect = { tab ->
                if (tab == MainTab.MAP) viewModel.backToMain() else viewModel.openTab(tab, 0)
            }
        )
    }

    // 지역 세부맵 — 지도·탭 어느 화면에서든 위에 뜬다
    state.regionDetailBoard?.let { board ->
        RegionDetailMapOverlay(
            board = board,
            unionActive = state.union.active,
            onClose = { viewModel.closeRegionDetail() },
            onChallenge = { id ->
                viewModel.closeRegionDetail()
                viewModel.challengeRegionLord(id)
            }
        )
    }

    if (infoOpen) {
        SlgInfoOverlay(
            state = state,
            onClose = { onInfoOpenChange(false) },
            onOpenTab = { tab, sub -> viewModel.openTab(tab, sub) },
            onReplayIntro = { viewModel.replayIntro() },
            onRestart = { viewModel.restartCampaign() }
        )
    }
}
