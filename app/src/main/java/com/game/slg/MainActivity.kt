package com.game.slg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.ui.CouncilScreen
import com.game.slg.ui.DiplomacyScreen
import com.game.slg.ui.SlgColors
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.SlgPill
import com.game.slg.ui.TerritoryScreen
import com.game.slg.ui.WarScreen

/**
 * 팔도 영지전 — 「단일 연방의 황혼」
 * 4탭 하단 내비게이션: 🗺 영지·지도 · 🏛 내정·의회 · ⚔ 전장·전략 · 🤝 외교·첩보
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
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = SlgColors.Background,
        bottomBar = { SlgBottomBar(selected = tab, onSelect = { tab = it }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlgColors.Surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "🏯 팔도 영지전 : 단일 연방의 황혼",
                        color = SlgColors.Brass,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "현실 지명 GIS 행정구역 기반 영지 전략 · D+14",
                        color = SlgColors.Muted,
                        fontSize = 9.sp
                    )
                }
                SlgPill(state.seasonName.substringBefore(" (") , SlgColors.Steel)
                Spacer(Modifier.width(6.dp))
                SlgPill("${state.gold}G", SlgColors.Brass)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (tab) {
                    0 -> TerritoryScreen(
                        state = state,
                        onAdvanceSeason = { viewModel.advanceSeason() },
                        onChooseEventA = { accept -> viewModel.chooseEventA(accept) },
                        onChooseEventB = { ambush -> viewModel.chooseEventB(ambush) }
                    )
                    1 -> CouncilScreen(
                        state = state,
                        onResearch = { id -> viewModel.research(id) },
                        onVoteBill = { id -> viewModel.voteBill(id) }
                    )
                    2 -> WarScreen(
                        state = state,
                        onRunOperation = { id -> viewModel.runOperation(id) }
                    )
                    else -> DiplomacyScreen(
                        state = state,
                        onProposeTreaty = { type -> viewModel.proposeTreaty(type) },
                        onSecureCasusBelli = { viewModel.secureCasusBelli() },
                        onEspionage = { op -> viewModel.conductEspionage(op) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SlgBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "🗺" to "영지·지도",
        "🏛" to "내정·의회",
        "⚔" to "전장·전략",
        "🤝" to "외교·첩보"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlgColors.Surface)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(item.first, fontSize = 17.sp)
                Text(
                    item.second,
                    color = if (index == selected) SlgColors.Brass else SlgColors.Muted,
                    fontSize = 9.sp,
                    fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
