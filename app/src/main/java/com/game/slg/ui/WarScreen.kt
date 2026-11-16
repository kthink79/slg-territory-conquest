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
 * 작전마다 지형도(Canvas)를 붙여 어떤 싸움인지 그림으로 먼저 읽게 했다.
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
    SlgArtHeader(
        image = R.drawable.slg_war_field,
        title = "광교산 방면",
        badges = listOf("⚔ 아군 ${state.troops}" to SlgColors.Steel, "🎯 적 군량 ${state.enemyFood}" to SlgColors.Brass),
        height = 132
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⚔ ${state.troops}", color = SlgColors.Steel, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Box(modifier = Modifier.weight(1f)) {
            SlgForceBar(own = state.troops, enemy = 3000)
        }
        Spacer(Modifier.width(6.dp))
        Text("적 3,000", color = SlgColors.Danger, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
    }

    OperationCard(
        kind = OperationKind.MOUNTAIN,
        icon = "🏔",
        title = "산악 매복",
        subtitle = "광교산 협곡",
        effects = listOf(
            Effect("🛡", "방어 +250%", SlgColors.Steel),
            Effect("⚔", "적 700", SlgColors.Danger),
            Effect("💥", "사기 -", SlgColors.Muted)
        ),
        accent = SlgColors.Stone,
        onClick = { onRunOperation("OP_AMBUSH") }
    )
    OperationCard(
        kind = OperationKind.RIVER,
        icon = "🌊",
        title = "하천 도하 요격",
        subtitle = "매탄천",
        effects = listOf(
            Effect("🎯", "요격", SlgColors.Steel),
            Effect("🏃", "기동 -", SlgColors.Muted),
            Effect("⚔", "적 650", SlgColors.Danger)
        ),
        accent = SlgColors.Steel,
        onClick = { onRunOperation("OP_RIVER") }
    )
    OperationCard(
        kind = OperationKind.FORTRESS,
        icon = "🏯",
        title = "요새 수성",
        subtitle = "매탄동 산성",
        effects = listOf(
            Effect("🛡", "사상자 -80%", SlgColors.Success),
            Effect("🎯", "무혈 항복", SlgColors.Brass),
            Effect("⚔", "적 3,000", SlgColors.Danger)
        ),
        accent = SlgColors.Brass,
        onClick = { onRunOperation("OP_FORTRESS") }
    )
    SlgExpand(
        summary = "작전 규칙",
        detail = "병력 손실이 곧 세력 상실입니다. 정면 대회전보다 지형·매복·보급선을 이용한 저손실 현대전을 지향합니다. " +
            "요새 수성은 방어 시설의 절대 우위를 검증하는 작전이며, 첩자로 적 군량고를 확보했다면 보급선 차단 판정이 함께 적용됩니다.",
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(SlgColors.Surface)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(13.dp))
            .clickable { onClick() }
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
                Text(title, color = SlgColors.Text, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text(subtitle, color = SlgColors.Muted, fontSize = SlgType.MICRO, maxLines = 1)
            }
            SlgEffectRow(effects)
        }
        Spacer(Modifier.width(4.dp))
        Text("›", color = accent, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
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
                    ratio = report.attackerLosses / 3000f,
                    color = SlgColors.Success
                )
                SlgGaugeRow(
                    icon = "🩸",
                    value = "${report.defenderLosses}",
                    ratio = report.defenderLosses / 1200f,
                    color = SlgColors.Danger
                )
                SlgPill(
                    if (lossRate <= 0.10f) "10% 이하 — 성주 조건 유지" else "10% 초과 — 조건 미달",
                    if (lossRate <= 0.10f) SlgColors.Success else SlgColors.Danger
                )
            }
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
        "🛡" to "방어 시설 먼저",
        "🏔" to "매복·도하로 지형 이점",
        "🕵" to "첩자로 군량고·성문",
        "🤝" to "다자 외교로 결전 회피",
        "🏛" to "과반 호족 연합 → 성주"
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
            detail = "① 요새·산성 등 방어 시설을 먼저 확보한다\n" +
                "② 매복·도하 요격으로 지형 이점을 취한다\n" +
                "③ 첩자로 군량고·성문·호족 이간을 준비한다\n" +
                "④ 외교로 다자 구도를 만들어 단독 결전을 피한다\n" +
                "⑤ 과반 호족의 편입·연합으로 성주 지위를 확보한다",
            accent = SlgColors.Steel
        )
    }
}
