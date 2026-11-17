package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R

/**
 * 서브 메뉴 「전장·전략」 — 탭 3개: 작전 / 전과 / 원칙.
 * 현대 병과(기동보병·기갑·화력·드론·방공·특수전) 작전만 남겼다.
 */
@Composable
fun WarScreen(
    state: SlgGameState,
    onRunOperation: (String) -> Unit,
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (WarTab.entries[tabIndex.coerceIn(0, WarTab.entries.lastIndex)]) {
            WarTab.OPS -> WarOperations(state, onRunOperation)
            WarTab.REPORT -> WarReport(state)
            WarTab.DOCTRINE -> WarDoctrine()
        }
    }
}

@Composable
private fun WarOperations(state: SlgGameState, onRunOperation: (String) -> Unit) {
    val enemyNode = state.mapNodes.firstOrNull { it.hopDistance == 1 && it.unlocked }
    val enemyName = enemyNode?.name ?: "인접 구역"
    val canAdjacent = state.command.unlockedHop >= 1

    SlgArtHeader(
        image = R.drawable.slg_war_field,
        title = "$enemyName 방면",
        badges = listOf(
            "⚔ 아군 ${state.troops}" to SlgColors.Steel,
            "🎯 적 보급 ${state.enemyFood}" to SlgColors.Brass
        ),
        height = 132
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⚔ ${state.troops}", color = SlgColors.Steel, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Box(modifier = Modifier.weight(1f)) {
            SlgForceBar(own = state.troops, enemy = (state.troops * 1.55).toInt())
        }
        Spacer(Modifier.width(6.dp))
        Text("적 ${(state.troops * 1.55).toInt()}", color = SlgColors.Danger, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
    }

    OperationCard(
        kind = OperationKind.MOUNTAIN,
        icon = "🛰",
        title = "무인기 정찰·타격",
        subtitle = enemyName,
        effects = listOf(
            Effect("🛰", "드론 +", SlgColors.Steel),
            Effect("⚔", "적 0.95×", SlgColors.Danger),
            Effect("🎯", "표적 획득", SlgColors.Brass)
        ),
        accent = SlgColors.Steel,
        enabled = canAdjacent,
        footnote = if (canAdjacent) null else "Lv.2 인접 구역 해금 필요",
        onClick = { onRunOperation("OP_DRONE") }
    )
    OperationCard(
        kind = OperationKind.RIVER,
        icon = "🌉",
        title = "교량·도하 봉쇄",
        subtitle = "하천·교량",
        effects = listOf(
            Effect("🎯", "요격", SlgColors.Steel),
            Effect("🚚", "보급 차단", SlgColors.Brass),
            Effect("⚔", "적 1.05×", SlgColors.Danger)
        ),
        accent = SlgColors.Water,
        enabled = canAdjacent,
        footnote = if (canAdjacent) null else "Lv.2 인접 구역 해금 필요",
        onClick = { onRunOperation("OP_BRIDGE") }
    )
    OperationCard(
        kind = OperationKind.FORTRESS,
        icon = "🛡",
        title = "통합 방호기지 방어",
        subtitle = state.regionName,
        effects = listOf(
            Effect("🛡", "사상자 -${80 + state.facilities.size * 2}%", SlgColors.Success),
            Effect("🎯", "무혈 항복", SlgColors.Brass),
            Effect("⚔", "적 1.55×", SlgColors.Danger)
        ),
        accent = SlgColors.Brass,
        enabled = true,
        onClick = { onRunOperation("OP_BASE") }
    )
    SlgExpand(
        summary = "작전 규칙",
        detail = "병력 손실이 곧 세력 상실입니다. 정면 대회전보다 지형·무인기·보급선을 이용한 저손실 현대전을 지향합니다. " +
            "Lv.1에서는 관할 구역 방어전만 가능하고, Lv.2부터 인접 읍·면·동에 작전을 걸 수 있습니다. " +
            "첩보로 적 보급 거점을 확보했다면 방어전에서 보급선 차단(무혈 항복) 판정이 적용됩니다.",
        accent = SlgColors.Steel
    )
}

@Composable
private fun OperationCard(
    kind: OperationKind,
    icon: String,
    title: String,
    subtitle: String,
    effects: List<Effect>,
    accent: Color,
    enabled: Boolean = true,
    footnote: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(SlgColors.Surface)
            .border(1.dp, accent.copy(alpha = if (enabled) 0.4f else 0.15f), RoundedCornerShape(13.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 96.dp, height = 62.dp)
                .clip(RoundedCornerShape(9.dp))
        ) {
            SlgOperationScene(kind = kind, modifier = Modifier.fillMaxWidth().height(62.dp))
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = SlgType.BODY_SMALL)
                Spacer(Modifier.width(4.dp))
                Text(
                    title,
                    color = if (enabled) SlgColors.Text else SlgColors.Muted,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Text(subtitle, color = SlgColors.Muted, fontSize = SlgType.MICRO, maxLines = 1)
            }
            SlgEffectRow(effects)
            if (footnote != null) {
                Text(footnote, color = SlgColors.Brass, fontSize = SlgType.MICRO, maxLines = 1)
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(
            if (enabled) "›" else "🔒",
            color = if (enabled) accent else SlgColors.Muted,
            fontSize = SlgType.BODY,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WarReport(state: SlgGameState) {
    val report = state.lastReport
    if (report == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(SlgColors.Surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("아직 작전 기록이 없습니다", color = SlgColors.Muted, fontSize = SlgType.LABEL)
        }
        return
    }
    val total = state.troops + report.defenderLosses
    val lossRate = if (total == 0) 0f else report.defenderLosses.toFloat() / total
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SlgPill(
                if (report.defenderWon) "방어 성공" else "거점 상실",
                if (report.defenderWon) SlgColors.Success else SlgColors.Danger,
                filled = true
            )
            Spacer(Modifier.width(6.dp))
            SlgPill(report.operation, SlgColors.Stone)
            Spacer(Modifier.weight(1f))
            SlgPill(report.verdict, SlgColors.Brass)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            SlgDonutGauge(
                ratio = lossRate,
                valueText = "${(lossRate * 100).toInt()}%",
                caption = "아군 사상자율",
                color = if (lossRate <= 0.10f) SlgColors.Success else SlgColors.Danger,
                modifier = Modifier.size(104.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                SlgGaugeRow(
                    icon = "💀",
                    value = "${report.attackerLosses}",
                    ratio = report.attackerLosses / 400f,
                    color = SlgColors.Success
                )
                SlgGaugeRow(
                    icon = "🩸",
                    value = "${report.defenderLosses}",
                    ratio = report.defenderLosses / 400f,
                    color = SlgColors.Danger
                )
                SlgPill(
                    if (lossRate <= 0.10f) "10% 이하 — 저손실 유지" else "10% 초과 — 재편성 필요",
                    if (lossRate <= 0.10f) SlgColors.Success else SlgColors.Danger
                )
            }
        }
        if (report.composition.isNotBlank()) {
            Text("🪖 ${report.composition}", color = SlgColors.Muted, fontSize = SlgType.MICRO, lineHeight = 15.sp)
        }
        SlgExpand(
            summary = "작전 보고 — 원문",
            detail = report.message,
            accent = SlgColors.Steel
        )
        SlgLogBox(state.log, maxLines = 3)
    }
}

@Composable
private fun WarDoctrine() {
    val rules = listOf(
        "🛡" to "방호 시설 먼저",
        "🛰" to "무인기로 먼저 본다",
        "🌉" to "교량·보급로 차단",
        "🕵" to "정보국으로 보급 거점",
        "🤝" to "다자 외교로 결전 회피",
        "🏛" to "과반 호족 연합 → 대표"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rules.forEachIndexed { index, (icon, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(SlgColors.Surface)
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = SlgType.BODY)
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    color = SlgColors.Text,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${index + 1}",
                    color = SlgColors.Brass,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        SlgExpand(
            summary = "저손실 전쟁 원칙 전문",
            detail = "① 방공 레이더망·스마트 방호망 등 방호 시설을 먼저 확보한다\n" +
                "② 드론 정찰로 표적을 먼저 확보하고 기갑·화력으로 마무리한다\n" +
                "③ 교량·물류 허브를 끊어 적 보급을 차단한다\n" +
                "④ 정보국·첩보로 적 보급 거점과 호족 이간을 준비한다\n" +
                "⑤ 과반 호족의 편입·연합으로 연합 대표 지위를 확보한다",
            accent = SlgColors.Steel
        )
    }
}
