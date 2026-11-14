package com.game.slg.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R
import com.game.slg.core.model.BillType

/**
 * 탭 2 — 🏛️ 내정·의회
 */
@Composable
fun CouncilScreen(
    state: SlgGameState,
    onResearch: (String) -> Unit,
    onVoteBill: (String) -> Unit
) {
    SlgScreenFrame {
        // ── 의회·작전실 배경 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.slg_council_chamber),
                contentDescription = "호족 의회",
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
                    .padding(12.dp)
            ) {
                Text(
                    "매탄동 호족 의회 · 작전실",
                    color = SlgColors.Text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "4대 계통 기술 발전 트리 · 성주-호족 안건 표결",
                    color = SlgColors.Muted,
                    fontSize = 9.sp
                )
            }
        }

        // ── 4대 테크트리 ──
        val branches = listOf(
            "내정" to Color(0xFF81C784),
            "과학기술" to Color(0xFF64B5F6),
            "문화" to Color(0xFFB39DDB),
            "군사" to Color(0xFFE57373)
        )
        SlgSectionCard(title = "4대 테크트리", badge = "완료 ${state.researchedTechs.size}/${SlgWorld.TECH_ITEMS.size}", accent = SlgColors.Brass) {
            branches.forEach { (branch, color) ->
                val items = SlgWorld.TECH_ITEMS.filter { it.branch == branch }.sortedBy { it.stage }
                val done = items.count { state.researchedTechs.contains(it.id) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SlgPill(branch, color, filled = done == items.size)
                    Spacer(Modifier.width(6.dp))
                    Text("$done/${items.size}단계", color = SlgColors.Muted, fontSize = 9.sp)
                }
                SlgStatBar(ratio = done.toFloat() / items.size, color = color, height = 5)
                items.forEach { tech ->
                    val researched = state.researchedTechs.contains(tech.id)
                    val prereq = items.firstOrNull { it.stage == tech.stage - 1 }
                    val prereqDone = prereq == null || state.researchedTechs.contains(prereq.id)
                    val affordable = state.gold >= tech.cost
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (researched) color.copy(alpha = 0.12f) else SlgColors.SurfaceAlt)
                            .padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${tech.stage}단계 ${tech.name}",
                                    color = if (researched) color else SlgColors.Text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(5.dp))
                                SlgPill(
                                    when {
                                        researched -> "완료"
                                        !prereqDone -> "선행 필요"
                                        !affordable -> "자금 부족"
                                        else -> "연구 가능"
                                    },
                                    when {
                                        researched -> SlgColors.Success
                                        !prereqDone -> SlgColors.Muted
                                        !affordable -> SlgColors.Danger
                                        else -> color
                                    }
                                )
                            }
                            Text(tech.effect, color = SlgColors.Muted, fontSize = 8.sp)
                            Text("${tech.cost}G · ${tech.description}", color = SlgColors.Muted, fontSize = 8.sp)
                        }
                        SlgChip(
                            if (researched) "완료" else "연구 ${tech.cost}G",
                            selected = researched,
                            accent = color,
                            onClick = { if (!researched) onResearch(tech.id) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // ── 호족 의회 표결 ──
        SlgSectionCard(title = "호족 의회 안건 표결", badge = "성주 권한", accent = SlgColors.Steel) {
            Text(
                "성주는 전시 병력 차출·특별세를 상정할 수 있으나, 호족 65% 이상이 반대하면 연합 반란이 일어납니다.",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
            state.bills.forEach { bill ->
                val totalVotes = (bill.favor + bill.against).coerceAtLeast(1)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            bill.title,
                            color = SlgColors.Text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(6.dp))
                        SlgPill(
                            when (bill.type) {
                                BillType.TROOP_LEVY -> "병력 차출"
                                BillType.SPECIAL_TAX -> "특별세"
                                BillType.FORTRESS_EXPAND -> "요새 확장"
                                else -> "선전포고"
                            },
                            SlgColors.Steel
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(bill.favor.toFloat().coerceAtLeast(0.1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SlgColors.Success)
                        )
                        Box(
                            modifier = Modifier
                                .weight(bill.against.toFloat().coerceAtLeast(0.1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SlgColors.Danger)
                        )
                    }
                    Text(
                        "찬성 ${bill.favor} : 반대 ${bill.against}" +
                                if (bill.passed == null) " (표결 전)" else if (bill.passed) " → 가결" else " → 부결",
                        color = SlgColors.Muted,
                        fontSize = 9.sp
                    )
                    if (bill.passed == null) {
                        SlgActionTile(
                            label = "표결 실행",
                            hint = if (bill.type == BillType.TROOP_LEVY) "차출 규모 ${bill.levy}명 · 가결 시 병력 +${bill.levy}"
                            else "가결 시 자금 +700G",
                            accent = SlgColors.Steel,
                            onClick = { onVoteBill(bill.id) }
                        )
                    }
                    if (bill.rebellion) {
                        SlgPill("반란 위험 — 반대 호족 연합 결성", SlgColors.Danger, filled = true)
                    }
                    if (bill.message != null) {
                        ResultBox(bill.message, if (bill.passed == true) SlgColors.Success else SlgColors.Danger)
                    }
                }
            }
        }
    }
}
