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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.model.BillType

/**
 * 서브 메뉴 「호족 의회」 — 성주 권한 안건 상정과 표결.
 * 4대 테크트리는 「테크트리」 서브 메뉴로 분리했다.
 */
@Composable
fun CouncilScreen(
    state: SlgGameState,
    onVoteBill: (String) -> Unit
) {
    SlgScreenFrame {
        CouncilChamberHeader(
            title = "매탄동 호족 의회 · 작전실",
            subtitle = "전시 병력 차출 · 특별세 · 요새 확장 안건 표결"
        )

        SlgSectionCard(title = "호족 의회 안건 표결", badge = "성주 권한", accent = SlgColors.Steel) {
            Text(
                "성주는 전시 병력 차출·특별세를 상정할 수 있으나, 호족 65% 이상이 반대하면 연합 반란이 일어납니다. " +
                    "의회 권한 보너스는 현재 +${state.councilAuthorityBonus}% 입니다.",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
            state.bills.forEach { bill ->
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

        SlgSectionCard(title = "호족 구성", badge = "매탄동 5석", accent = SlgColors.Brass) {
            listOf(
                "이광수 호족 (플레이어 · 성주)" to "병력 ${state.troops} · 명성 ${state.prestige}",
                "한서윤 호족" to "병력 320 · 명성 55",
                "오진혁 호족" to "병력 410 · 명성 48",
                "배수아 호족" to "병력 260 · 명성 61"
            ).forEach { (name, detail) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, color = SlgColors.Text, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text(detail, color = SlgColors.Muted, fontSize = 8.5.sp)
                }
            }
        }
    }
}
