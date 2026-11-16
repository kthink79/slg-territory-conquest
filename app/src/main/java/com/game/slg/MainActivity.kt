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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.game.slg.core.map.MapViewport
import com.game.slg.ui.CouncilScreen
import com.game.slg.ui.DiplomacyScreen
import com.game.slg.ui.EventScenarioScreen
import com.game.slg.ui.MainRoute
import com.game.slg.ui.PromotionScreen
import com.game.slg.ui.SlgColors
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.SlgInfoOverlay
import com.game.slg.ui.SlgMainScreen
import com.game.slg.ui.SubMenu
import com.game.slg.ui.SubScreenHost
import com.game.slg.ui.TechTreeScreen
import com.game.slg.ui.TerritoryScreen
import com.game.slg.ui.WarScreen

/**
 * 팔도 영지전 — 「단일 연방의 황혼」
 *
 * 화면은 두 층뿐이다.
 *  · 메인 : 지도(주인공) + 경보 칩 1줄 + 서브 메뉴 8칸, 상단은 자원 1줄
 *  · 상세 : «메인» + 제목 + 2~4칸 탭 바 + 그림·배지 중심 본문
 * 긴 문장은 「📊 정보」 모달과 「?」 접기로만 나온다.
 */
class MainActivity : ComponentActivity() {
    private val gameViewModel = SlgGameViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // 📊 정보 모달이 열려 있으면 뒤로가기는 모달만 닫는다
    BackHandler(enabled = infoOpen) { infoOpen = false }
    // 상세 화면에서 뒤로가기 → 메인
    BackHandler(enabled = !infoOpen && !navigation.isHome) { viewModel.backToMain() }

    Scaffold(containerColor = SlgColors.Background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val route = navigation.route) {
                is MainRoute.Home -> SlgMainScreen(
                    state = state,
                    viewport = mapViewport,
                    onViewportChange = { mapViewport = it },
                    onSelectTerritory = { viewModel.selectTerritory(it) },
                    onOpenMenu = { viewModel.openSubMenu(it) },
                    onAdvanceSeason = { viewModel.advanceSeason() },
                    onOpenInfo = { infoOpen = true }
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
                            onSelectTerritory = { viewModel.selectTerritory(it) }
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

                        // 실행형 메뉴(절기 진행)는 화면이 없다
                        SubMenu.SEASON -> Unit
                    }
                }
            }

            if (infoOpen) {
                SlgInfoOverlay(
                    state = state,
                    onClose = { infoOpen = false },
                    onOpenMenu = { viewModel.openSubMenu(it) }
                )
            }
        }
    }
}
