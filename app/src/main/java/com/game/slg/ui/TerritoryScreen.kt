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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.game.slg.core.engine.BuildValidation
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.model.FacilityCategory
import com.game.slg.core.model.FacilityType
import com.game.slg.core.model.TerrainType
import com.game.slg.core.region.SeasonClock
import kotlinx.coroutines.delay

/**
 * 서브 메뉴 「영지·내정」 — 탭 4개: 영지 / 지표 / 절기 / 시설.
 */
@Composable
fun TerritoryScreen(
    state: SlgGameState,
    onAdvanceSeason: () -> Unit,
    onDismissCeremony: () -> Unit,
    tabIndex: Int = 0,
    onSelectTerritory: (String) -> Unit = {},
    onOpenRegionDetail: (String) -> Unit = {},
    onBuildFacility: (FacilityType) -> BuildValidation = { BuildValidation(false, 0) },
    onSetSeasonSpeed: (Int) -> Unit = {},
    onToggleAutoAdvance: (Boolean) -> Unit = {},
    onToggleAutopilot: (Boolean) -> Unit = {}
) {
    SlgScreenFrame {
        PromotionCeremonyBanner(state = state, onDismissCeremony = onDismissCeremony)
        when (TerritoryTab.entries[tabIndex.coerceIn(0, TerritoryTab.entries.lastIndex)]) {
            TerritoryTab.HOME -> TerritoryOverview(state)
            TerritoryTab.STATS -> TerritoryStats(state, onSelectTerritory, onOpenRegionDetail)
            TerritoryTab.SEASON -> SeasonPanel(
                state = state,
                onAdvanceSeason = onAdvanceSeason,
                onSetSeasonSpeed = onSetSeasonSpeed,
                onToggleAutoAdvance = onToggleAutoAdvance,
                onToggleAutopilot = onToggleAutopilot
            )
            TerritoryTab.FACILITIES -> FacilityPanel(state, onBuildFacility)
        }
    }
}

@Composable
private fun TerritoryOverview(state: SlgGameState) {
    // 관할 구역 일러스트 + 지휘관
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.slg_city_maetan),
            contentDescription = "관할 구역",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x22000000), Color(0xE60C0E13))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SlgPill("내 거점", SlgColors.Brass, filled = true)
                SlgPill(state.seasonName.substringBefore(" ("), SlgColors.Steel)
                SlgPill("Lv.${state.command.level}", SlgColors.Success)
            }
            Text(
                state.regionFullName,
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
                Text(
                    "${state.playerLord?.name ?: "이광수"} 리더",
                    color = SlgColors.Brass,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.Bold
                )
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

    // 지역·리더 정원
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("관할 구역", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            SlgPill("🏙 ${state.lordCapacity}석", SlgColors.Brass)
        }
        SlgStatBar(
            ratio = state.registeredLords.toFloat() / state.lordCapacity.coerceAtLeast(1),
            color = SlgColors.Brass,
            height = 5
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SlgSummaryChip("👥", "${state.regionPopulation}", SlgColors.Text, Modifier.weight(1f))
            SlgSummaryChip("🏙", "리더 ${state.aiLordCount}", SlgColors.Steel, Modifier.weight(1f))
            SlgSummaryChip("📐", "1,000명/석", SlgColors.Brass, Modifier.weight(1f))
        }
        Text(
            "인구 ${state.regionPopulation}명 ÷ 1,000 = 리더 ${state.lordCapacity}석 · 인계 ${state.registeredLords}석 · " +
                "나머지 ${state.aiLordCount}석은 다른 리더 운영",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        SlgPill(state.expansionTier.label, SlgColors.Steel, filled = true)
        Text(
            "${state.expansionTier.detail} — 지휘 Lv.${state.command.level} " +
                "(다음 Lv.까지 ${state.command.xpRemaining} XP)",
            color = SlgColors.Text.copy(alpha = 0.85f),
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
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
private fun TerritoryStats(
    state: SlgGameState,
    onSelectTerritory: (String) -> Unit,
    onOpenRegionDetail: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SlgPill("해금 ${state.unlockedNodeCount}", SlgColors.Success)
            Spacer(Modifier.width(5.dp))
            SlgPill("잠김 ${state.lockedNodeCount}", SlgColors.Muted)
            Spacer(Modifier.weight(1f))
            Text("Lv.${state.command.level} ${state.expansionTier.label}", color = SlgColors.Brass, fontSize = SlgType.MICRO)
        }
        state.mapNodes
            .sortedWith(compareBy({ it.hopDistance }, { -it.population }))
            .forEach { node ->
                val accent = if (node.isLocked) SlgColors.Line else relationColor(node.relationLabel)
                val selected = node.code == state.selectedTerritoryCode
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
                        .clickable { onSelectTerritory(node.code) }
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
                        Text(if (node.isLocked) "🔒" else node.terrain.emoji(), fontSize = SlgType.LABEL)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                node.name,
                                color = if (node.isLocked) SlgColors.Muted else SlgColors.Text,
                                fontSize = SlgType.BODY_SMALL,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(5.dp))
                            SlgPill(
                                if (node.isLocked) "Lv.${tierLevelForHopPublic(node.hopDistance)} 해금" else node.relationLabel,
                                accent
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👥 ${node.population}", color = SlgColors.Muted, fontSize = SlgType.MICRO)
                            Spacer(Modifier.width(7.dp))
                            Text("🏙 ${node.lordCapacity}석", color = SlgColors.Muted, fontSize = SlgType.MICRO)
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "⚔ ${if (node.isHome) state.troops else node.troops}",
                                color = accent,
                                fontSize = SlgType.MICRO
                            )
                        }
                    }
                    if (selected) SlgPill("선택", SlgColors.Brass, filled = true)
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SlgColors.Steel.copy(alpha = 0.16f))
                            .border(1.dp, SlgColors.Steel.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clickable { onOpenRegionDetail(node.code) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "🔍 확대",
                            color = SlgColors.Steel,
                            fontSize = SlgType.MICRO,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        SlgExpand(
            summary = "이 지역의 사정 — 원문",
            detail = state.selectedTerritory?.note ?: "지도에서 영지를 탭하면 상세가 바뀝니다.",
            accent = SlgColors.Brass
        )
        SlgExpand(
            summary = "해금 규칙",
            detail = "Lv.1 관할 구역(내 읍·면·동) → Lv.2 인접 읍·면·동 → Lv.3 연접 구역 → Lv.4 시·군 전역 → " +
                "Lv.6 광역 생활권 → Lv.8 전국 연결망. 작전·절기·의회·시설로 경험치를 얻습니다.",
            accent = SlgColors.Steel
        )
    }
}

@Composable
private fun SeasonPanel(
    state: SlgGameState,
    onAdvanceSeason: () -> Unit,
    onSetSeasonSpeed: (Int) -> Unit = {},
    onToggleAutoAdvance: (Boolean) -> Unit = {},
    onToggleAutopilot: (Boolean) -> Unit = {}
) {
    // 1초마다 현재 시각을 다시 읽어 남은 시간을 보여 준다
    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val clock = state.clock

    // ── 지속 시간 흐름 — 절기가 저절로 흐른다 (2026-11-25 요청) ──
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .border(1.dp, SlgColors.Steel.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⏱ 절기 시계", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            SlgPill(clock.speedLabel, if (clock.autoAdvance) SlgColors.Success else SlgColors.Muted)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                if (clock.autoAdvance) "다음 절기까지" else "자동 진행 멈춤",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                modifier = Modifier.weight(1f)
            )
            Text(
                clock.countdownLabel(now),
                color = if (clock.autoAdvance) SlgColors.Brass else SlgColors.Muted,
                fontSize = SlgType.TITLE,
                fontWeight = FontWeight.ExtraBold
            )
        }
        SlgStatBar(
            ratio = clock.progress(now),
            color = SlgColors.Brass,
            height = 7
        )
        Text(
            "시간이 흐르면 절기가 자동으로 넘어가고, AI가 내정·모병·방어를 대신 운영합니다. " +
                "미접속 동안 흐른 시간도 돌아오면 그만큼 진행됩니다.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            SeasonClock.SPEED_MINUTES.forEach { minutes ->
                val selected = clock.seasonMinutes == minutes
                val label = if (minutes >= 60) "${minutes / 60}시간" else "${minutes}분"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) SlgColors.Brass.copy(alpha = 0.22f) else SlgColors.SurfaceAlt)
                        .border(
                            1.dp,
                            if (selected) SlgColors.Brass.copy(alpha = 0.8f) else SlgColors.Line,
                            RoundedCornerShape(50)
                        )
                        .clickable { onSetSeasonSpeed(minutes) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (selected) SlgColors.Brass else SlgColors.Muted,
                        fontSize = SlgType.LABEL,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ToggleChip(
                label = "자동 진행",
                on = clock.autoAdvance,
                accent = SlgColors.Success,
                modifier = Modifier.weight(1f)
            ) { onToggleAutoAdvance(!clock.autoAdvance) }
            ToggleChip(
                label = "AI 대행",
                on = clock.aiAutopilot,
                accent = SlgColors.Steel,
                modifier = Modifier.weight(1f)
            ) { onToggleAutopilot(!clock.aiAutopilot) }
        }
        if (state.offlineReport != null) {
            ResultBox(state.offlineReport, SlgColors.Steel)
        }
    }

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
                    "다음 절기에 지방세·결산·유지비가 정산됩니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    maxLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SlgPill("🌱 예산", SlgColors.Success)
                    SlgPill("☀ 재난", SlgColors.Water)
                }
            }
        }
        SlgChoiceCard(
            title = "다음 절기 진행",
            icon = "🌾",
            effects = listOf(
                Effect("💰", "세수", SlgColors.Brass),
                Effect("🌾", "결산", SlgColors.Success),
                Effect("🛠", "유지비", SlgColors.Muted)
            ),
            accent = SlgColors.Success,
            onClick = onAdvanceSeason
        )
        SlgExpand(
            summary = "절기 규칙",
            detail = "봄 지방세 → 여름 재난 대응·집중호우 → 가을 결산·보급 비축 → 겨울 동절기 유지비 순으로 4계절이 돕니다. " +
                "여름에는 배수·제방 정비를 마쳐야 침수 피해를 막을 수 있고, 물류 허브를 지으면 절기 보급이 12% 늘어납니다.",
            accent = SlgColors.Success
        )
        SlgLogBox(state.log, maxLines = 3)
    }
}

/** 시설 — 지휘통제실·드론 격납고·방공 레이더망·물류 허브·스마트 방호망 등 */
@Composable
private fun FacilityPanel(
    state: SlgGameState,
    onBuildFacility: (FacilityType) -> BuildValidation
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("현대 시설", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            SlgPill("${state.facilities.size}/${FacilityType.entries.size} 건설", SlgColors.Brass)
        }
        SlgStatBar(
            ratio = state.facilities.size.toFloat() / FacilityType.entries.size.coerceAtLeast(1),
            color = SlgColors.Brass,
            height = 5
        )
        Text(
            "자금 ${state.gold}G · 시설은 1회 건설로 효과가 계속 적용됩니다.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
    }

    FacilityCategory.entries.forEach { category ->
        SlgSectionCard(title = category.label, badge = "${FacilityType.byCategory(category).count { state.facilities.contains(it) }}건", accent = SlgColors.Steel) {
            FacilityType.byCategory(category).forEach { facility ->
                val owned = state.facilities.contains(facility)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (owned) SlgColors.Brass.copy(alpha = 0.12f) else SlgColors.SurfaceAlt)
                        .clickable(enabled = !owned && state.gold >= facility.cost) { onBuildFacility(facility) }
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(facility.icon, fontSize = SlgType.TITLE)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            facility.displayName,
                            color = if (owned) SlgColors.Brass else SlgColors.Text,
                            fontSize = SlgType.LABEL,
                            fontWeight = FontWeight.Bold
                        )
                        Text(facility.effectLabel, color = SlgColors.Muted, fontSize = SlgType.MICRO)
                    }
                    SlgPill(
                        if (owned) "건설됨" else "${facility.cost}G",
                        when {
                            owned -> SlgColors.Success
                            state.gold >= facility.cost -> SlgColors.Brass
                            else -> SlgColors.Muted
                        },
                        filled = owned
                    )
                }
            }
        }
    }

    SlgExpand(
        summary = "시설 효과 정리",
        detail = "지휘통제실 작전 경험 +10% · 드론 격납고 드론 전투력 +15% · 방공 레이더망 방어 +10%·첩보 +5 · " +
            "물류 허브 절기 보급 +12% · 스마트 방호망 사상자 -8% · 통신·사이버 정보국 첩보 +10 · " +
            "민방위 지원센터 민심 +6 · 야전 의무지원대 작전 후 병력 +5%.",
        accent = SlgColors.Brass
    )
    SlgLogBox(state.log, maxLines = 3)
}

/** 지형 이모지 */
fun TerrainType.emoji(): String = when (this) {
    TerrainType.MOUNTAIN -> "▲"
    TerrainType.RIVER -> "≈"
    TerrainType.COASTAL -> "〜"
    else -> "▦"
}

/** hop 거리가 열리는 최소 지휘 레벨 */
fun tierLevelForHopPublic(hop: Int): Int =
    com.game.slg.core.region.ExpansionTier.entries.firstOrNull { it.maxHop >= hop }?.level ?: 8

/** 현재 절기 → 바퀴 인덱스 (봄 0 · 여름 1 · 가을 2 · 겨울 3) */
fun SlgGameState.seasonWheelIndex(): Int = when {
    seasonName.startsWith("봄") -> 0
    seasonName.startsWith("여름") -> 1
    seasonName.startsWith("가을") -> 2
    else -> 3
}

/** 켜기/끄기 칩 — 자동 진행·AI 대행 */
@Composable
private fun ToggleChip(
    label: String,
    on: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (on) accent.copy(alpha = 0.18f) else SlgColors.SurfaceAlt)
            .border(
                1.dp,
                if (on) accent.copy(alpha = 0.7f) else SlgColors.Line,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (on) "●" else "○",
            color = if (on) accent else SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "$label ${if (on) "켜짐" else "꺼짐"}",
            color = if (on) accent else SlgColors.Muted,
            fontSize = SlgType.LABEL,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
