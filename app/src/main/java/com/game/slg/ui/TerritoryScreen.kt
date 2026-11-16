package com.game.slg.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R
import com.game.slg.core.model.TerrainType

/**
 * 서브 메뉴 「영지·내정」 — 탭 3개: 영지 / 지표 / 절기.
 *
 * 첫 탭은 본진 일러스트 + 레이더 차트, 둘째 탭은 인접 행정구역 카드,
 * 셋째 탭은 절기 바퀴와 정산만 보여 준다. 긴 설명은 「?」로 접었다.
 */
@Composable
fun TerritoryScreen(
    state: SlgGameState,
    onAdvanceSeason: () -> Unit,
    onDismissCeremony: () -> Unit,
    tabIndex: Int = 0,
    onSelectTerritory: (String) -> Unit = {}
) {
    SlgScreenFrame {
        PromotionCeremonyBanner(state = state, onDismissCeremony = onDismissCeremony)
        when (TerritoryTab.entries[tabIndex.coerceIn(0, TerritoryTab.entries.lastIndex)]) {
            TerritoryTab.HOME -> TerritoryOverview(state)
            TerritoryTab.STATS -> TerritoryStats(state, onSelectTerritory)
            TerritoryTab.SEASON -> SeasonPanel(state, onAdvanceSeason)
        }
    }
}

@Composable
private fun TerritoryOverview(state: SlgGameState) {
    // 본진 일러스트 + 지휘관
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.slg_city_maetan),
            contentDescription = "매탄동 본진",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0x22000000), Color(0xE60C0E13)))
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SlgPill("본진", SlgColors.Brass, filled = true)
                SlgPill(state.seasonName.substringBefore(" ("), SlgColors.Steel)
                SlgPill("호족 5석", SlgColors.Stone)
            }
            Text(
                "경기도 수원시 영통구 매탄동",
                color = SlgColors.Text,
                fontSize = SlgType.TITLE,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text("지휘관", color = SlgColors.Muted, fontSize = SlgType.MICRO)
                Text("이광수 호족", color = SlgColors.Brass, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            Image(
                painter = painterResource(R.drawable.slg_commander_hero),
                contentDescription = "지휘관",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, SlgColors.Brass, CircleShape)
            )
        }
    }

    // 통치 지표 레이더
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("통치 지표", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            SlgPill("민심 ${state.morale}", SlgColors.Success)
        }
        SlgRadarChart(
            stats = listOf(
                RadarStat("민심", state.morale, color = SlgColors.Success),
                RadarStat("명성", state.prestige, color = SlgColors.Brass),
                RadarStat("외교", state.diplomacy, color = SlgColors.Steel),
                RadarStat("문화", state.culturePrestige, color = SlgColors.Violet),
                RadarStat("첩보", state.espionagePower, color = SlgColors.Stone)
            ),
            modifier = Modifier.fillMaxWidth().height(190.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SlgSummaryChip("👥", "${state.population}", SlgColors.Text, Modifier.weight(1f))
            SlgSummaryChip("💰", "${state.gold}", SlgColors.Brass, Modifier.weight(1f))
            SlgSummaryChip("🌾", "${state.food}", SlgColors.Success, Modifier.weight(1f))
            SlgSummaryChip("⚔", "${state.troops}", SlgColors.Steel, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TerritoryStats(state: SlgGameState, onSelectTerritory: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SlgWorld.TERRITORIES.forEach { territory ->
            val accent = relationColor(territory.relation.label)
            val selected = territory.code == state.selectedTerritoryCode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selected) accent.copy(alpha = 0.12f) else SlgColors.Surface)
                    .border(
                        1.dp,
                        if (selected) accent.copy(alpha = 0.7f) else SlgColors.Line.copy(alpha = 0.6f),
                        RoundedCornerShape(11.dp)
                    )
                    .clickable { onSelectTerritory(territory.code) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(territory.terrain.emoji(), fontSize = 15.sp)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            territory.name,
                            color = SlgColors.Text,
                            fontSize = SlgType.BODY_SMALL,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(5.dp))
                        SlgPill(territory.relation.label, accent)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👥 ${territory.population}", color = SlgColors.Muted, fontSize = SlgType.MICRO)
                        Spacer(Modifier.width(7.dp))
                        Box(modifier = Modifier.width(56.dp)) {
                            SlgStatBar(
                                ratio = territory.troops / 2000f,
                                color = accent,
                                height = 4
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("⚔ ${territory.troops}", color = accent, fontSize = SlgType.MICRO)
                    }
                }
                if (selected) SlgPill("선택", SlgColors.Brass, filled = true)
            }
        }
        SlgExpand(
            summary = "이 지역의 사정 — 원문",
            detail = state.selectedTerritory?.note ?: "지도에서 영지를 탭하면 상세가 바뀝니다.",
            accent = SlgColors.Brass
        )
    }
}

@Composable
private fun SeasonPanel(state: SlgGameState, onAdvanceSeason: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SlgSeasonWheel(currentIndex = state.seasonWheelIndex(), modifier = Modifier.size(104.dp))
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                Text(
                    state.seasonName.substringBefore(" ("),
                    color = SlgColors.Brass,
                    fontSize = SlgType.TITLE,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "다음 절기에 조세·추수·유지비가 정산됩니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    maxLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SlgPill("🌱 봄 조세", SlgColors.Success)
                    SlgPill("☀ 여름 치수", SlgColors.Water)
                }
            }
        }
        SlgChoiceCard(
            title = "다음 절기 진행",
            icon = "🌾",
            effects = listOf(
                Effect("💰", "조세", SlgColors.Brass),
                Effect("🌾", "추수", SlgColors.Success),
                Effect("🛠", "유지비", SlgColors.Muted)
            ),
            accent = SlgColors.Success,
            onClick = onAdvanceSeason
        )
        SlgExpand(
            summary = "절기 규칙",
            detail = "봄 조세 → 여름 치수·풍수해 → 가을 대추수 → 겨울 혹한기 유지비 순으로 4계절이 돕니다. " +
                "여름에는 치수 사업을 반드시 마쳐야 홍수 피해를 막을 수 있습니다.",
            accent = SlgColors.Success
        )
        SlgLogBox(state.log, maxLines = 3)
    }
}

/** 지형 이모지 */
fun TerrainType.emoji(): String = when (this) {
    TerrainType.MOUNTAIN -> "▲"
    TerrainType.RIVER -> "≈"
    TerrainType.COASTAL -> "〜"
    else -> "▦"
}

/** 현재 절기 → 바퀴 인덱스 (봄 0 · 여름 1 · 가을 2 · 겨울 3) */
fun SlgGameState.seasonWheelIndex(): Int = when {
    seasonName.startsWith("봄") -> 0
    seasonName.startsWith("여름") -> 1
    seasonName.startsWith("가을") -> 2
    else -> 3
}
