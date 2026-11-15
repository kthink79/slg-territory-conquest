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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.game.slg.core.map.MapViewport
import com.game.slg.ui.CouncilScreen
import com.game.slg.ui.DiplomacyScreen
import com.game.slg.ui.EventScenarioScreen
import com.game.slg.ui.MainRoute
import com.game.slg.ui.MainThreePaneScreen
import com.game.slg.ui.PromotionScreen
import com.game.slg.ui.SlgColors
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.SubMenu
import com.game.slg.ui.SubScreenHost
import com.game.slg.ui.TechTreeScreen
import com.game.slg.ui.TerritoryScreen
import com.game.slg.ui.WarScreen

/**
 * 팔도 영지전 — 「단일 연방의 황혼」
 *
 * 메인 화면은 3단 분할이다.
 *  · 상단 1/3 : 내 영지(매탄동) 기준 제한 범위 지도 + 확대/축소·이동·영지 선택
 *  · 중간 1/3 : 현재 상태(자금·군량·병력·보급, 선택 영지, 승격 진행, 경보)
 *  · 하단 1/3 : 서브 메뉴 아이콘 (탭하면 기존 4탭 기능 전체가 상세 화면으로 열린다)
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

    // 상세 화면에서 뒤로가기 → 메인 3단 화면
    BackHandler(enabled = !navigation.isHome) { viewModel.backToMain() }

    Scaffold(containerColor = SlgColors.Background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val route = navigation.route) {
                is MainRoute.Home -> MainThreePaneScreen(
                    state = state,
                    viewport = mapViewport,
                    onViewportChange = { mapViewport = it },
                    onSelectTerritory = { viewModel.selectTerritory(it) },
                    onOpenMenu = { viewModel.openSubMenu(it) },
                    onAdvanceSeason = { viewModel.advanceSeason() }
                )

                is MainRoute.Detail -> SubScreenHost(
                    menu = route.menu,
                    onBack = { viewModel.backToMain() }
                ) {
                    when (route.menu) {
                        SubMenu.TERRITORY -> TerritoryScreen(
                            state = state,
                            onAdvanceSeason = { viewModel.advanceSeason() },
                            onDismissCeremony = { viewModel.dismissCeremony() }
                        )

                        SubMenu.TECH -> TechTreeScreen(
                            state = state,
                            onResearch = { id -> viewModel.research(id) }
                        )

                        SubMenu.COUNCIL -> CouncilScreen(
                            state = state,
                            onVoteBill = { id -> viewModel.voteBill(id) }
                        )

                        SubMenu.WAR -> WarScreen(
                            state = state,
                            onRunOperation = { id -> viewModel.runOperation(id) }
                        )

                        SubMenu.DIPLOMACY -> DiplomacyScreen(
                            state = state,
                            onProposeTreaty = { type -> viewModel.proposeTreaty(type) },
                            onSecureCasusBelli = { viewModel.secureCasusBelli() },
                            onEspionage = { op -> viewModel.conductEspionage(op) },
                            onOpenNightMeeting = { name -> viewModel.openNightMeeting(name) },
                            onResolveNightMeeting = { choice -> viewModel.resolveNightMeeting(choice) },
                            onExploitDoubleAgent = { viewModel.exploitDoubleAgent() }
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
        }
    }
}
