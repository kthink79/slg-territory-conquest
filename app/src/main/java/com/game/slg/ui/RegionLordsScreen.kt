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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 작전 탭 「지역」 — 같은 지역 리더 명부와 지역 연합 상태.
 *
 * 지역 연합(스폰 보호) 기간에는 교전 버튼이 잠기고, 남은 턴이 0이 되면 열린다.
 */
@Composable
fun RegionLordsScreen(
    state: SlgGameState,
    onChallenge: (String) -> Unit
) {
    SlgScreenFrame {
        SlgSectionCard(
            title = "지역 연합",
            badge = if (state.union.active) "${state.union.remainingTurns}턴" else "종료",
            accent = if (state.union.active) SlgColors.Success else SlgColors.Danger
        ) {
            Text(
                state.union.statusLabel,
                color = if (state.union.active) SlgColors.Success else SlgColors.Muted,
                fontSize = SlgType.BODY,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                if (state.union.active) "리더 ${state.unionPartnerCount}명과 자동 연합 · 절기마다 -1턴"
                else "보호 종료 — 같은 지역 리더와 교전 가능",
                color = SlgColors.Muted,
                fontSize = SlgType.LABEL,
                maxLines = 1
            )
        }

        SlgSectionCard(
            title = "AI 리더 동향",
            badge = if (state.aiNews.isEmpty()) "아직 기록 없음" else "최근 절기",
            accent = SlgColors.Violet
        ) {
            if (state.aiNews.isEmpty()) {
                Text(
                    "절기를 넘기면 동향이 여기에 쌓입니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.LABEL
                )
            } else {
                state.aiNews.forEach { line ->
                    Text(
                        line,
                        color = SlgColors.Text.copy(alpha = 0.9f),
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SlgPill("병합 ${state.lords.count { it.bond == com.game.slg.core.region.LeaderBond.MERGE }}명", SlgColors.Danger)
                SlgPill(
                    "종속 ${state.lords.count { it.bond == com.game.slg.core.region.LeaderBond.SUBORDINATION }}명",
                    SlgColors.Violet
                )
                SlgPill("편입 ${state.incorporatedLordCount}명", SlgColors.Success)
            }
        }

        SlgSectionCard(
            title = "같은 지역 리더",
            badge = if (state.incorporatedLordCount > 0) "${state.aiLordCount}명 · 편입 ${state.incorporatedLordCount}"
            else "${state.aiLordCount}명",
            accent = SlgColors.Steel
        ) {
            val partners = state.lords.filterNot { it.isPlayer }
            partners.take(12).forEach { lord ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.union.active) {
                        Text("🛡", fontSize = SlgType.BODY_SMALL)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(SlgColors.Danger)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${lord.name} 리더",
                            color = SlgColors.Text,
                            fontSize = SlgType.BODY_SMALL,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            when {
                                lord.incorporated -> "🏳 ${lord.stronghold} · 편입 완료"
                                lord.isBound -> "${lord.bond?.icon ?: "⛓"} ${lord.bond?.label ?: "예속"} · ${lord.stronghold}"
                                else -> "${lord.stronghold} · ⚔ ${lord.troops} · ${lord.traitIcon} ${lord.traitLabel}"
                            },
                            color = when {
                                lord.incorporated -> SlgColors.Success
                                lord.isBound -> SlgColors.Violet
                                else -> SlgColors.Muted
                            },
                            fontSize = SlgType.MICRO,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                when {
                                    lord.incorporated -> SlgColors.Surface
                                    state.union.active -> SlgColors.Success.copy(alpha = 0.16f)
                                    else -> SlgColors.Danger.copy(alpha = 0.9f)
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    lord.incorporated -> SlgColors.Success.copy(alpha = 0.5f)
                                    state.union.active -> SlgColors.Success.copy(alpha = 0.6f)
                                    else -> SlgColors.Danger
                                },
                                RoundedCornerShape(9.dp)
                            )
                            .clickable(enabled = !state.union.active && !lord.incorporated) { onChallenge(lord.id) }
                            .padding(horizontal = 11.dp, vertical = 8.dp)
                    ) {
                        Text(
                            when {
                                lord.incorporated -> "편입"
                                state.union.active -> "연합"
                                else -> "⚔ 교전"
                            },
                            color = when {
                                lord.incorporated -> SlgColors.Success
                                state.union.active -> SlgColors.Success
                                else -> Color(0xFF10131A)
                            },
                            fontSize = SlgType.LABEL,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
            if (partners.size > 12) {
                Text(
                    "외 ${partners.size - 12}명은 의회 명부에서 확인할 수 있습니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO
                )
            }
        }

        if (state.lastReport != null && state.lastReport!!.operation == "지역 리더 압박") {
            SlgSectionCard(title = "최근 교전", badge = state.lastReport!!.verdict, accent = SlgColors.Danger) {
                Text(
                    state.lastReport!!.message,
                    color = SlgColors.Text,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
                Text(
                    state.lastReport!!.composition,
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }
        }

        SlgExpand(
            summary = "지역 연합 · 스폰 보호 규칙",
            detail = "① 게임을 시작해 지역을 인계받으면 7턴 동안 같은 지역 모든 리더와 자동 연합합니다.\n" +
                "② 연합 기간에는 지역 안에서 서로 공격할 수 없습니다.\n" +
                "③ 같은 기간 외부 인접 구역도 신규 거점을 공격·점령할 수 없습니다(보호막).\n" +
                "④ 절기를 진행할 때마다 1턴씩 줄고, 0이 되면 교전이 열립니다.\n" +
                "⑤ 대외 원정(무인기·교량 작전)은 보호 기간에도 나갈 수 있습니다.\n" +
                "⑥ 교전에서 상대 리더 병력을 0으로 만들면 추가 손실 없이 즉시 강제 편입되고, " +
                "그 리더의 소속 영웅은 영토 탭 「처분 대기」에서 등용하거나 방출합니다.",
            accent = SlgColors.Success
        )

        SlgLogBox(state.log, maxLines = 4)
    }
}
