package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R
import com.game.slg.core.model.BillType

/**
 * 서브 메뉴 「호족 의회」 — 탭 2개: 안건 / 호족.
 *
 * 안건은 찬반 스플릿 바(반대 65% 반란선 포함)만 보고 표결한다.
 */
@Composable
fun CouncilScreen(
    state: SlgGameState,
    onVoteBill: (String) -> Unit,
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (CouncilTab.entries[tabIndex.coerceIn(0, CouncilTab.entries.lastIndex)]) {
            CouncilTab.BILLS -> CouncilBills(state, onVoteBill)
            CouncilTab.LORDS -> CouncilLords(state)
        }
    }
}

@Composable
private fun CouncilBills(state: SlgGameState, onVoteBill: (String) -> Unit) {
    SlgArtHeader(
        image = R.drawable.slg_council_chamber,
        title = "매탄동 호족 의회",
        badges = listOf("🏛 성주 권한" to SlgColors.Steel, "권한 +${state.councilAuthorityBonus}%" to SlgColors.Brass),
        height = 136
    )

    val pending = state.bills.count { it.passed == null }
    Row(verticalAlignment = Alignment.CenterVertically) {
        SlgPill("안건 ${state.bills.size}", SlgColors.Stone)
        Spacer(Modifier.width(5.dp))
        SlgPill(if (pending == 0) "표결 완료" else "대기 ${pending}건", if (pending == 0) SlgColors.Success else SlgColors.Brass)
        Spacer(Modifier.weight(1f))
        Text("반대 65% = 반란", color = SlgColors.Danger, fontSize = SlgType.MICRO)
    }

    state.bills.forEach { bill ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(SlgColors.Surface)
                .border(1.dp, SlgColors.Line.copy(alpha = 0.7f), RoundedCornerShape(13.dp))
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    bill.title,
                    color = SlgColors.Text,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
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
            SlgVoteSplit(
                favor = bill.favor,
                against = bill.against,
                modifier = Modifier.fillMaxWidth().height(26.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                SlgPill(
                    when (bill.passed) {
                        null -> "표결 전"
                        true -> "가결"
                        false -> "부결"
                    },
                    when (bill.passed) {
                        null -> SlgColors.Muted
                        true -> SlgColors.Success
                        false -> SlgColors.Danger
                    },
                    filled = bill.passed == true
                )
                Spacer(Modifier.weight(1f))
            }
            if (bill.rebellion) {
                SlgPill("반란 위험 — 반대 호족 연합", SlgColors.Danger, filled = true)
            }
            if (bill.passed == null) {
                SlgChoiceCard(
                    title = "표결 실행",
                    icon = "🗳",
                    effects = if (bill.type == BillType.TROOP_LEVY) {
                        listOf(
                            Effect("⚔", "+${bill.levy}", SlgColors.Steel),
                            Effect("❤", "민심 변동", SlgColors.Muted)
                        )
                    } else {
                        listOf(
                            Effect("💰", "+700G", SlgColors.Brass),
                            Effect("❤", "민심 변동", SlgColors.Muted)
                        )
                    },
                    accent = SlgColors.Steel,
                    onClick = { onVoteBill(bill.id) }
                )
            }
            if (bill.message != null) {
                ResultBox(bill.message, if (bill.passed == true) SlgColors.Success else SlgColors.Danger)
            }
        }
    }

    SlgExpand(
        summary = "의회 규칙",
        detail = "성주는 전시 병력 차출·특별세를 상정할 수 있으나, 호족 65% 이상이 반대하면 " +
            "연합 반란이 일어납니다. 승격할 때마다 의회 권한 보너스가 올라 반대표를 낮춥니다.",
        accent = SlgColors.Steel
    )
}

@Composable
private fun CouncilLords(state: SlgGameState) {
    val lords = listOf(
        Triple("이광수", "성주 · 플레이어", state.troops) to state.prestige,
        Triple("한서윤", "호족", 320) to 55,
        Triple("오진혁", "호족", 410) to 48,
        Triple("배수아", "호족", 260) to 61
    )
    val maxTroops = lords.maxOf { it.first.third }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lords.forEach { (info, prestige) ->
            val (name, role, troops) = info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(SlgColors.Surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (role.contains("성주")) SlgColors.Brass.copy(alpha = 0.22f)
                            else SlgColors.SurfaceAlt
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.substring(0, 1),
                        color = if (role.contains("성주")) SlgColors.Brass else SlgColors.Muted,
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(5.dp))
                        SlgPill(role, if (role.contains("성주")) SlgColors.Brass else SlgColors.Muted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚔", color = SlgColors.Steel, fontSize = SlgType.MICRO)
                        Spacer(Modifier.width(3.dp))
                        Box(modifier = Modifier.width(64.dp)) {
                            SlgStatBar(ratio = troops / maxTroops.toFloat(), color = SlgColors.Steel, height = 4)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("$troops", color = SlgColors.Steel, fontSize = SlgType.MICRO)
                        Spacer(Modifier.width(8.dp))
                        Text("⭐ $prestige", color = SlgColors.Brass, fontSize = SlgType.MICRO)
                    }
                }
            }
        }
        SlgExpand(
            summary = "호족 5석 구성",
            detail = "매탄동 본진에는 4명의 호족이 있습니다. 성주(플레이어)를 포함해 5석이며, " +
                "표결에서 반대 65%를 넘기면 연합 반란으로 이어집니다.",
            accent = SlgColors.Brass
        )
    }
}
