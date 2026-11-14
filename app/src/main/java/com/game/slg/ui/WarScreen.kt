package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/**
 * 탭 3 — ⚔️ 전장·전략
 */
@Composable
fun WarScreen(
    state: SlgGameState,
    onRunOperation: (String) -> Unit
) {
    SlgScreenFrame {
        SlgSectionCard(title = "현 전장 상황", badge = "광교산 방면", accent = SlgColors.Danger) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SlgResourceChip("⚔", "아군 병력", "${state.troops}", SlgColors.Steel, Modifier.weight(1f))
                SlgResourceChip("🌾", "적 군량", "${state.enemyFood}", SlgColors.Brass, Modifier.weight(1f))
                SlgResourceChip("🎯", "정보", if (state.intelGained) "확보" else "미확보", SlgColors.Success, Modifier.weight(1f))
            }
            Text(
                "병력 손실이 곧 세력 상실입니다. 정면 대회전보다 지형·매복·보급선을 이용한 저손실 현대전을 지향합니다.",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
        }

        SlgSectionCard(title = "작전 선택", accent = SlgColors.Brass) {
            SlgActionTile(
                label = "🏔 산악 매복 — 광교산 협곡",
                hint = "지형 방어 +250% · 첫 턴 적 사기 삭감 · 적 700 대 아군 ${state.troops}",
                accent = Color(0xFF90A4AE),
                onClick = { onRunOperation("OP_AMBUSH") }
            )
            SlgActionTile(
                label = "🌊 하천 도하 요격 — 매탄천",
                hint = "도하 지점 요격 · 기동 페널티 · 적 650 대 아군 ${state.troops}",
                accent = SlgColors.Steel,
                onClick = { onRunOperation("OP_RIVER") }
            )
            SlgActionTile(
                label = "🏯 요새 수성 — 매탄동 산성",
                hint = "산성 요새 사상자 80% 격감 · 적 3,000 대 아군 ${state.troops} · 보급선 차단 시 무혈 항복",
                accent = SlgColors.Brass,
                onClick = { onRunOperation("OP_FORTRESS") }
            )
            Text(
                "요새 수성은 방어 시설의 절대 우위를 검증하는 작전입니다. 이벤트 B에서 첩자로 적 군량고를 확보했다면 " +
                        "보급선 차단 판정이 함께 적용됩니다.",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
        }

        val report = state.lastReport
        if (report != null) {
            SlgSectionCard(
                title = "작전 결과",
                badge = report.operation,
                accent = if (report.defenderWon) SlgColors.Success else SlgColors.Danger
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SlgPill(
                        if (report.defenderWon) "방어 성공" else "거점 상실",
                        if (report.defenderWon) SlgColors.Success else SlgColors.Danger,
                        filled = true
                    )
                    Spacer(Modifier.width(6.dp))
                    SlgPill(report.verdict, if (report.defenderWon) SlgColors.Brass else SlgColors.Danger)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SlgResourceChip("💀", "적 사상자", "${report.attackerLosses}", SlgColors.Success, Modifier.weight(1f))
                    SlgResourceChip("🩸", "아군 사상자", "${report.defenderLosses}", SlgColors.Danger, Modifier.weight(1f))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(10.dp)
                ) {
                    Text(report.message, color = SlgColors.Text.copy(alpha = 0.9f), fontSize = 10.sp, lineHeight = 15.sp)
                }
                Text(
                    "사상자율 ${if (state.troops + report.defenderLosses == 0) 0 else (report.defenderLosses * 100 / (state.troops + report.defenderLosses))}% — " +
                            "10% 이하 유지가 성주 등극의 조건입니다.",
                    color = SlgColors.Muted,
                    fontSize = 9.sp
                )
            }
        }

        SlgSectionCard(title = "전투 로그", accent = SlgColors.Muted) {
            SlgLogBox(state.log, maxLines = 6)
        }

        SlgSectionCard(title = "저손실 전쟁 원칙", accent = SlgColors.Steel) {
            Text(
                "① 요새·산성 등 방어 시설을 먼저 확보한다\n" +
                        "② 매복·도하 요격으로 지형 이점을 취한다\n" +
                        "③ 첩자로 군량고·성문·호족 이간을 준비한다\n" +
                        "④ 외교로 다자 구도를 만들어 단독 결전을 피한다\n" +
                        "⑤ 과반 호족의 편입·연합으로 성주 지위를 확보한다",
                color = SlgColors.Muted,
                fontSize = 10.sp,
                lineHeight = 16.sp
            )
        }
    }
}
