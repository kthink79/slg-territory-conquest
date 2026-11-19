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
 * 「인구 1,000명당 1리더」 규칙으로 계산한 리더 정원을 보여 준다.
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
                "인구 1,000명당 리더 1석 — 지역을 고르면 그 인구만큼 리더가 이미 운영 중입니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
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
                            "· 리더 정원 = 인구 ÷ 1,000 (내림, 최소 1석). 지역을 고른 뒤 리더 한 명을 인계받습니다.",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO,
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
                "👥 ${comma(area.population)}명  ·  🏙 리더 ${area.lordCapacity}석",
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
 * 새 게임 2단계 — 리더 인계 또는 영웅으로 시작.
 *
 * 지역 인구가 정한 정원만큼 리더가 이미 운영 중이고,
 * 그중 하나를 골라 세력·거점·병력·자금을 그대로 물려받는다.
 */
@Composable
fun LordSelectScreen(
    state: SlgGameState,
    onPick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onChooseStartMode: (StartMode) -> Unit = {},
    onPickHero: (String) -> Unit = {},
    onRequestEmployment: (String) -> Unit = {}
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
            Text(
                if (state.startMode == StartMode.HERO) "🦸 영웅으로 시작" else "🏙 리더 인계",
                color = SlgColors.Text,
                fontSize = SlgType.BODY,
                fontWeight = FontWeight.Bold
            )
        }

        // 시작 방식 — 리더 / 영웅
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StartMode.entries.forEach { mode ->
                val selected = state.startMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) SlgColors.Brass.copy(alpha = 0.22f) else SlgColors.SurfaceAlt)
                        .border(
                            1.dp,
                            if (selected) SlgColors.Brass else SlgColors.Line,
                            RoundedCornerShape(50)
                        )
                        .clickable { onChooseStartMode(mode) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        mode.label,
                        color = if (selected) SlgColors.Brass else SlgColors.Muted,
                        fontSize = SlgType.LABEL,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        if (state.startMode == StartMode.HERO) {
            SlgSectionCard(
                title = "등용할 영웅",
                badge = state.pendingHeroPick?.let { com.game.slg.core.hero.HeroRoster.byId(it)?.name } ?: "선택",
                accent = SlgColors.Violet
            ) {
                Text(
                    "재야 영웅으로 시작합니다. 리더는 유저일 수도, 아닐 수도 있으며 " +
                        "수락·거절은 능력치·명성·세력 필요도로만 정해집니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(
                        com.game.slg.core.hero.HeroRoster.ALL.filterIndexed { index, _ -> index % 20 == 0 }
                    ) { hero ->
                        val selected = state.pendingHeroPick == hero.id
                        Column(
                            modifier = Modifier
                                .width(104.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) SlgColors.Violet.copy(alpha = 0.18f) else SlgColors.SurfaceAlt)
                                .border(
                                    1.dp,
                                    if (selected) SlgColors.Violet else SlgColors.Line,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onPickHero(hero.id) }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HeroPortrait(hero = hero, widthDp = 78)
                            Spacer(Modifier.height(5.dp))
                            Text(hero.name, color = SlgColors.Text, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                "${hero.role.label} · ${hero.grade.label}",
                                color = SlgColors.Muted,
                                fontSize = SlgType.MICRO,
                                maxLines = 1
                            )
                        }
                    }
                }
                state.pendingHeroPick?.let { id ->
                    com.game.slg.core.hero.HeroRoster.byId(id)?.let { hero ->
                        Text(
                            "${hero.name} · ${hero.title} — ${hero.statLine} / 특기 ${hero.specialty}",
                            color = SlgColors.Text,
                            fontSize = SlgType.MICRO,
                            lineHeight = SlgLine.MICRO
                        )
                    }
                }
                state.pendingEmployment?.let { request ->
                    ResultBox(
                        request.message,
                        if (request.accepted) SlgColors.Success else SlgColors.Danger
                    )
                }
            }
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
                SlgSummaryChip("🏙", "리더 ${state.lordCapacity}", SlgColors.Steel, Modifier.weight(1f))
            }
            Text(
                "인구 ${comma(state.regionPopulation)} ÷ ${LordCapacity.CITIZENS_PER_LORD} = " +
                    "리더 ${state.lordCapacity}석 · 지금은 ${state.lordCapacity}명의 리더가 각자 거점을 운영 중",
                color = SlgColors.Brass,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            Text(
                "한 명을 인계받으면 나머지는 그대로 운영됩니다. 초반 작전 범위는 선택한 읍·면·동 내부입니다. " +
                    "누가 사람인지 기계인지는 표시되지 않습니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
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
                LordCard(
                    lord = lord,
                    actionLabel = if (state.startMode == StartMode.HERO) "등용 청" else "인계",
                    enabled = state.startMode != StartMode.HERO || state.pendingHeroPick != null,
                    onClick = {
                        if (state.startMode == StartMode.HERO) onRequestEmployment(lord.id)
                        else onPick(lord.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun LordCard(
    lord: LordView,
    actionLabel: String = "인계",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlgColors.Surface)
            .border(1.dp, SlgColors.Line.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
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
                    "${lord.name} 리더",
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
            Text(
                actionLabel,
                color = if (enabled) SlgColors.Brass else SlgColors.Muted,
                fontSize = SlgType.LABEL,
                fontWeight = FontWeight.ExtraBold
            )
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
