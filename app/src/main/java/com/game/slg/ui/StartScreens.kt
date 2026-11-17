package com.game.slg.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.region.LordCapacity
import com.game.slg.core.region.RegionArea
import com.game.slg.core.region.RegionCatalog
import com.game.slg.core.region.RegionGroup

private fun comma(value: Int): String = "%,d".format(value)

/**
 * 새 게임 1단계 — 시작 지역 선택.
 *
 * 시·도 → 시·군·구 → 읍·면·동 순서로 좁히고, 카드마다 인구와
 * 「인구 1,000명당 1호족」 규칙으로 계산한 호족 정원을 보여 준다.
 */
@Composable
fun RegionSelectScreen(
    onChoose: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 사용자가 예로 든 탕정면(충남 아산시)을 기본으로 보여 준다
    var province by rememberSaveable { mutableStateOf(RegionCatalog.defaultRegion().province) }
    var groupKey by rememberSaveable { mutableStateOf(RegionCatalog.defaultRegion().groupKey) }
    val groups: List<RegionGroup> = RegionCatalog.groupsOfProvince(province)
    val currentGroup = groups.firstOrNull { it.key == groupKey } ?: groups.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlgColors.Background)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E1218))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlgBackButton(onBack = onBack)
            Spacer(Modifier.weight(1f))
            Text("🗺 시작 지역", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "어디에서 시작할까",
                color = SlgColors.Text,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "인구 1,000명당 호족 1석 — 지역을 고르면 그 인구만큼 AI 호족이 이미 운영 중입니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = 15.sp
            )
        }

        // 시·도
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(RegionCatalog.PROVINCES) { item ->
                SelectChip(
                    label = item,
                    selected = item == province,
                    accent = SlgColors.Brass,
                    onClick = {
                        province = item
                        groupKey = ""
                    }
                )
            }
        }

        // 시·군·구
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(groups) { group ->
                SelectChip(
                    label = group.label,
                    selected = currentGroup?.key == group.key,
                    accent = SlgColors.Steel,
                    onClick = { groupKey = group.key }
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        if (currentGroup != null) {
            RegionGroupHeader(currentGroup)
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(currentGroup.areas, key = { it.code }) { area ->
                    AreaCard(area = area, onClick = { onChoose(area.code) })
                }
                item {
                    Text(
                        "· 인구는 행정안전부 주민등록인구(2026년 9월) 기준입니다. 일부 지역은 최근 공개 수치의 근사치입니다.\n" +
                            "· 호족 정원 = 인구 ÷ 1,000 (내림, 최소 1석). 지역을 고른 뒤 AI 호족 한 명을 인계받습니다.",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.9f) else SlgColors.SurfaceAlt)
            .border(1.dp, if (selected) accent else SlgColors.Line, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF10131A) else SlgColors.Muted,
            fontSize = SlgType.LABEL,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun RegionGroupHeader(group: RegionGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SlgPill("${group.areas.size}개 읍·면·동", SlgColors.Stone)
        Spacer(Modifier.width(5.dp))
        SlgPill("👥 ${comma(group.totalPopulation)}", SlgColors.Text)
        Spacer(Modifier.width(5.dp))
        SlgPill("🏙 ${group.totalLordSlots}석", SlgColors.Brass)
    }
}

@Composable
private fun AreaCard(area: RegionArea, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlgColors.Surface)
            .border(1.dp, SlgColors.Line.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SlgColors.Brass.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(area.terrain.icon(), fontSize = 16.sp)
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    area.name,
                    color = SlgColors.Text,
                    fontSize = SlgType.BODY,
                    fontWeight = FontWeight.Bold
                )
                if (area.code == RegionCatalog.DEFAULT_AREA_CODE) {
                    Spacer(Modifier.width(5.dp))
                    SlgPill("예시", SlgColors.Brass, filled = true)
                }
            }
            Text(
                "👥 ${comma(area.population)}명  ·  🏙 호족 ${area.lordCapacity}석",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO
            )
            Text(
                "${LordCapacity.CITIZENS_PER_LORD}명당 1석 · 1인당 주민 ${comma(LordCapacity.residentsPerLord(area.population))}명",
                color = SlgColors.Muted.copy(alpha = 0.7f),
                fontSize = SlgType.MICRO
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("선택", color = SlgColors.Brass, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
            Text("›", color = SlgColors.Brass, fontSize = SlgType.BODY)
        }
    }
}

/**
 * 새 게임 2단계 — AI 호족 인계.
 *
 * 지역 인구가 정한 정원만큼 AI 호족이 이미 운영 중이고,
 * 그중 하나를 골라 세력·거점·병력·자금을 그대로 물려받는다.
 */
@Composable
fun LordSelectScreen(
    state: SlgGameState,
    onPick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlgColors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E1218))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlgBackButton(onBack = onBack)
            Spacer(Modifier.weight(1f))
            Text("🏙 호족 인계", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
        }

        // 지역 요약 — 인구 ÷ 1,000
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SlgColors.Surface)
                .border(1.dp, SlgColors.Brass.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.regionName,
                    color = SlgColors.Text,
                    fontSize = SlgType.TITLE,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(6.dp))
                SlgPill(state.regionGroupKey.substringAfter(" ").ifBlank { "자치구" }, SlgColors.Stone)
            }
            Text(
                state.regionFullName,
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SlgSummaryChip("👥", comma(state.regionPopulation), SlgColors.Text, Modifier.weight(1f))
                SlgSummaryChip("🏙", "${state.lordCapacity}석", SlgColors.Brass, Modifier.weight(1f))
                SlgSummaryChip("🤖", "AI ${state.lordCapacity}", SlgColors.Steel, Modifier.weight(1f))
            }
            Text(
                "인구 ${comma(state.regionPopulation)} ÷ ${LordCapacity.CITIZENS_PER_LORD} = " +
                    "호족 ${state.lordCapacity}석 · 지금은 AI ${state.lordCapacity}명이 각자 거점을 운영 중",
                color = SlgColors.Brass,
                fontSize = SlgType.MICRO,
                lineHeight = 15.sp
            )
            Text(
                "한 명을 인계받으면 나머지는 AI로 계속 운영됩니다. 초반 작전 범위는 선택한 읍·면·동 내부입니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = 15.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 12.dp, end = 12.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.lords, key = { it.id }) { lord ->
                LordCard(lord = lord, onClick = { onPick(lord.id) })
            }
        }
    }
}

@Composable
private fun LordCard(lord: LordView, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlgColors.Surface)
            .border(1.dp, SlgColors.Line.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SlgColors.Brass.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(lord.traitIcon, fontSize = 17.sp)
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${lord.name} 호족",
                    color = SlgColors.Text,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(5.dp))
                SlgPill(lord.traitLabel, SlgColors.Steel)
            }
            Text(lord.stronghold, color = SlgColors.Muted, fontSize = SlgType.MICRO, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚔ ${lord.troops}", color = SlgColors.Steel, fontSize = SlgType.MICRO)
                Text("💰 ${lord.gold}", color = SlgColors.Brass, fontSize = SlgType.MICRO)
                Text("🌾 ${lord.grain}", color = SlgColors.Success, fontSize = SlgType.MICRO)
                Text("⭐ ${lord.prestige}", color = SlgColors.Text, fontSize = SlgType.MICRO)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SlgColors.Brass.copy(alpha = 0.16f))
                .border(1.dp, SlgColors.Brass.copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("인계", color = SlgColors.Brass, fontSize = SlgType.LABEL, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** 지형 아이콘 */
fun TerrainIconOf(area: RegionArea): String = area.terrain.icon()

private fun com.game.slg.core.model.TerrainType.icon(): String = when (this) {
    com.game.slg.core.model.TerrainType.MOUNTAIN -> "▲"
    com.game.slg.core.model.TerrainType.RIVER -> "≈"
    com.game.slg.core.model.TerrainType.COASTAL -> "〜"
    else -> "▦"
}
