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
import com.game.slg.core.model.EspionageOpType
import com.game.slg.core.model.TreatyType

/**
 * 탭 4 — 🤝 외교·첩보
 */
@Composable
fun DiplomacyScreen(
    state: SlgGameState,
    onProposeTreaty: (TreatyType) -> Unit,
    onSecureCasusBelli: () -> Unit,
    onEspionage: (EspionageOpType) -> Unit
) {
    SlgScreenFrame {
        SlgSectionCard(title = "다자 외교", badge = "외교 ${state.diplomacy}", accent = Color(0xFFB39DDB)) {
            SlgActionTile(
                label = "불가침 조약 — 원천동 상인회",
                hint = "4턴 유효 · 상호 불가침 · 배신 시 명성 -50",
                accent = Color(0xFFB39DDB),
                onClick = { onProposeTreaty(TreatyType.NON_AGGRESSION_PACT) }
            )
            SlgActionTile(
                label = "상호방위 조약 — 용인 수지 산악 협곡",
                hint = "3턴 유효 · 피격 시 상호 참전 · 배신 시 명성 -80",
                accent = SlgColors.Steel,
                onClick = { onProposeTreaty(TreatyType.DEFENSIVE_ALLIANCE) }
            )
            SlgActionTile(
                label = "속국 조공 조약 — 광교 호족연합",
                hint = "6턴 유효 · 매 절기 조공 100G",
                accent = SlgColors.Brass,
                onClick = { onProposeTreaty(TreatyType.VASSAL_TRIBUTARY) }
            )
            if (state.treaties.isEmpty()) {
                Text("체결된 조약 없음 — 모든 인접 세력이 잠재 적대 상태입니다.", color = SlgColors.Muted, fontSize = 9.sp)
            } else {
                state.treaties.forEach { treaty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlgColors.SurfaceAlt)
                            .padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(treaty.typeLabel, color = SlgColors.Text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(5.dp))
                                SlgPill("${treaty.remainingTurns}턴 남음", SlgColors.Steel)
                            }
                            Text("${treaty.partner} · ${treaty.benefit}", color = SlgColors.Muted, fontSize = 8.sp)
                        }
                    }
                }
            }
        }

        SlgSectionCard(title = "선전포고 명분 (Casus Belli)", accent = SlgColors.Danger) {
            if (state.casusBelli == null) {
                Text(
                    "명분 없이 선전포고하면 명성 -40과 외교 -20의 페널티가 적용됩니다. " +
                            "국경 침범·조약 파기·군량고 무단 축조 등의 사건을 확보해 명분을 만들어야 합니다.",
                    color = SlgColors.Muted,
                    fontSize = 9.sp,
                    lineHeight = 14.sp
                )
                SlgActionTile(
                    label = "명분 확보 (광교 호족연합 국경 침범 조사)",
                    hint = "명성 페널티 없이 선전포고 가능 · 외교 +4",
                    accent = SlgColors.Danger,
                    onClick = onSecureCasusBelli
                )
            } else {
                SlgPill("명분 확보 완료", SlgColors.Danger, filled = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlgColors.Danger.copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Text(state.casusBelli, color = SlgColors.Text.copy(alpha = 0.9f), fontSize = 10.sp, lineHeight = 15.sp)
                }
            }
        }

        SlgSectionCard(title = "첩자 공작", badge = "실행 ${state.espionageCount}회", accent = SlgColors.Steel) {
            SlgActionTile(
                label = "🔥 군량고 방화",
                hint = "적 군량 -300석 · 명성 +2 (이간계 공작 병행)",
                accent = SlgColors.Danger,
                onClick = { onEspionage(EspionageOpType.INSTIGATE_DISSENT) }
            )
            SlgActionTile(
                label = "🕸 이간계 — 호족 내부 분열 조장",
                hint = "적 명성/사기 교란 · 반란 유도",
                accent = Color(0xFFB39DDB),
                onClick = { onEspionage(EspionageOpType.INSTIGATE_DISSENT) }
            )
            SlgActionTile(
                label = "📜 정찰 — 적 군세·군량 파악",
                hint = "적 병력·군량·비옥도 정보 입수",
                accent = SlgColors.Steel,
                onClick = { onEspionage(EspionageOpType.GATHER_INTEL) }
            )
            if (state.espionageMessage != null) {
                ResultBox(state.espionageMessage, SlgColors.Steel)
            }
        }

        SlgSectionCard(
            title = "히든 인재 동향",
            badge = "위장 유저 의심",
            accent = Color(0xFF90A4AE)
        ) {
            Text(
                "호족이 아닌 인재로 시작한 유저는 위장 신분으로 사병을 모으고 하극상을 준비합니다. " +
                        "의심도가 높은 인물을 추적해 고용주 세력을 지키십시오.",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
            state.hiddenTalents.forEach { talent ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(talent.name, color = SlgColors.Text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(6.dp))
                        SlgPill(talent.cover, Color(0xFF90A4AE))
                    }
                    Text(talent.hint, color = SlgColors.Muted, fontSize = 8.sp, lineHeight = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("의심도", color = SlgColors.Muted, fontSize = 8.sp)
                        Spacer(Modifier.width(5.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            SlgStatBar(
                                ratio = talent.suspicion / 100f,
                                color = when {
                                    talent.suspicion >= 60 -> SlgColors.Danger
                                    talent.suspicion >= 40 -> SlgColors.Brass
                                    else -> SlgColors.Muted
                                },
                                height = 5
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                        Text("${talent.suspicion}", color = SlgColors.Text, fontSize = 8.sp)
                    }
                }
            }
        }

        SlgSectionCard(title = "외교 기록", accent = SlgColors.Muted) {
            SlgLogBox(state.log, maxLines = 6)
        }
    }
}
