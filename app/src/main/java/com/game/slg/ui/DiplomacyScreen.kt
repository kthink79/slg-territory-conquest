package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.game.slg.core.model.EspionageOpType
import com.game.slg.core.model.NightMeetingChoice
import com.game.slg.core.model.TreatyType

/**
 * 서브 메뉴 「외교·첩보」 — 탭 3개: 조약 / 첩보 / 인재.
 * 조약·공작은 「대상 + 효과 배지」만 보고 고르고, 규칙 설명은 「?」로 접었다.
 */
@Composable
fun DiplomacyScreen(
    state: SlgGameState,
    onProposeTreaty: (TreatyType) -> Unit,
    onSecureCasusBelli: () -> Unit,
    onEspionage: (EspionageOpType) -> Unit,
    onOpenNightMeeting: (String) -> Unit,
    onResolveNightMeeting: (NightMeetingChoice) -> Unit,
    onExploitDoubleAgent: () -> Unit,
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (DiplomacyTab.entries[tabIndex.coerceIn(0, DiplomacyTab.entries.lastIndex)]) {
            DiplomacyTab.TREATY -> TreatyPanel(state, onProposeTreaty, onSecureCasusBelli)
            DiplomacyTab.ESPIONAGE -> EspionagePanel(state, onEspionage)
            DiplomacyTab.TALENT -> TalentPanel(state, onOpenNightMeeting, onResolveNightMeeting, onExploitDoubleAgent)
        }
    }
}

@Composable
private fun TreatyPanel(
    state: SlgGameState,
    onProposeTreaty: (TreatyType) -> Unit,
    onSecureCasusBelli: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SlgPill("🤝 외교 ${state.diplomacy}", SlgColors.Violet)
        Spacer(Modifier.width(5.dp))
        SlgPill("조약 ${state.treaties.size}", SlgColors.Stone)
        Spacer(Modifier.weight(1f))
        Text("파기는 명성 −", color = SlgColors.Danger, fontSize = SlgType.MICRO)
    }

    SlgChoiceCard(
        title = "불가침 — 원천동 상인회",
        icon = "📜",
        effects = listOf(
            Effect("⏳", "4턴", SlgColors.Steel),
            Effect("🛡", "상호 불가침", SlgColors.Success),
            Effect("⭐", "-50 파기", SlgColors.Danger)
        ),
        accent = SlgColors.Violet,
        onClick = { onProposeTreaty(TreatyType.NON_AGGRESSION_PACT) }
    )
    SlgChoiceCard(
        title = "상호방위 — 용인 수지",
        icon = "🛡",
        effects = listOf(
            Effect("⏳", "3턴", SlgColors.Steel),
            Effect("⚔", "상호 참전", SlgColors.Steel),
            Effect("⭐", "-80 파기", SlgColors.Danger)
        ),
        accent = SlgColors.Steel,
        onClick = { onProposeTreaty(TreatyType.DEFENSIVE_ALLIANCE) }
    )
    SlgChoiceCard(
        title = "속국 조공 — 광교 호족연합",
        icon = "👑",
        effects = listOf(
            Effect("⏳", "6턴", SlgColors.Steel),
            Effect("💰", "+100/절기", SlgColors.Brass)
        ),
        accent = SlgColors.Brass,
        onClick = { onProposeTreaty(TreatyType.VASSAL_TRIBUTARY) }
    )

    if (state.treaties.isEmpty()) {
        Text("체결된 조약 없음 — 전 세력 잠재 적대", color = SlgColors.Muted, fontSize = SlgType.MICRO)
    } else {
        state.treaties.forEach { treaty ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(SlgColors.Surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📜", fontSize = SlgType.BODY_SMALL)
                Spacer(Modifier.width(7.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        treaty.typeLabel,
                        color = SlgColors.Text,
                        fontSize = SlgType.LABEL,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(treaty.partner, color = SlgColors.Muted, fontSize = SlgType.MICRO, maxLines = 1)
                }
                SlgPill("${treaty.remainingTurns}턴", SlgColors.Steel)
            }
        }
    }

    SlgSectionCard(title = "선전포고 명분", accent = SlgColors.Danger) {
        if (state.casusBelli == null) {
            SlgChoiceCard(
                title = "명분 확보 — 국경 침범 조사",
                icon = "⚑",
                effects = listOf(
                    Effect("⭐", "페널티 0", SlgColors.Success),
                    Effect("🤝", "+4", SlgColors.Steel)
                ),
                accent = SlgColors.Danger,
                onClick = onSecureCasusBelli
            )
            SlgExpand(
                summary = "명분 없이 선전포고하면?",
                detail = "명성 -40과 외교 -20의 페널티가 적용됩니다. 국경 침범·조약 파기·군량고 무단 축조 " +
                    "같은 사건을 확보해 명분을 만들어야 합니다.",
                accent = SlgColors.Danger
            )
        } else {
            SlgPill("명분 확보 완료", SlgColors.Danger, filled = true)
            ResultBox(state.casusBelli, SlgColors.Danger)
        }
    }
}

@Composable
private fun EspionagePanel(state: SlgGameState, onEspionage: (EspionageOpType) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SlgPill("🕵 첩보 ${state.espionagePower}", SlgColors.Violet)
        Spacer(Modifier.width(5.dp))
        SlgPill("실행 ${state.espionageCount}회", SlgColors.Stone)
        Spacer(Modifier.weight(1f))
        SlgPill(
            if (state.intelGained) "정보 확보" else "정보 미확보",
            if (state.intelGained) SlgColors.Success else SlgColors.Muted
        )
    }
    SlgChoiceCard(
        title = "군량고 방화",
        icon = "🔥",
        effects = listOf(
            Effect("🌾", "적 -300", SlgColors.Success),
            Effect("⭐", "+2", SlgColors.Brass)
        ),
        accent = SlgColors.Danger,
        onClick = { onEspionage(EspionageOpType.INSTIGATE_DISSENT) }
    )
    SlgChoiceCard(
        title = "이간계 — 호족 분열",
        icon = "🕸",
        effects = listOf(
            Effect("🎭", "사기 교란", SlgColors.Violet),
            Effect("🏛", "반란 유도", SlgColors.Danger)
        ),
        accent = SlgColors.Violet,
        onClick = { onEspionage(EspionageOpType.INSTIGATE_DISSENT) }
    )
    SlgChoiceCard(
        title = "정찰 — 군세 파악",
        icon = "📜",
        effects = listOf(
            Effect("🎯", "병력·군량", SlgColors.Steel),
            Effect("🎯", "비옥도", SlgColors.Steel)
        ),
        accent = SlgColors.Steel,
        onClick = { onEspionage(EspionageOpType.GATHER_INTEL) }
    )
    if (state.espionageMessage != null) {
        ResultBox(state.espionageMessage, SlgColors.Steel)
    }
    SlgExpand(
        summary = "첩자 공작 규칙",
        detail = "공작은 매 절기 1회씩 성공 판정이 있습니다. 군량고 방화는 적 군량 -300, " +
            "이간계는 호족 반란을 유도하고, 정찰은 적 병력·군량·비옥도를 드러냅니다.",
        accent = SlgColors.Violet
    )
}

@Composable
private fun TalentPanel(
    state: SlgGameState,
    onOpenNightMeeting: (String) -> Unit,
    onResolveNightMeeting: (NightMeetingChoice) -> Unit,
    onExploitDoubleAgent: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SlgPill("👤 히든 인재 ${state.hiddenTalents.size}", SlgColors.Stone)
        Spacer(Modifier.width(5.dp))
        SlgPill("밀회 기준 ${SlgGameState.NIGHT_MEETING_SUSPICION}", SlgColors.Muted)
        Spacer(Modifier.weight(1f))
        if (state.nightMeetingChoice != null) SlgPill("밀회 완료", SlgColors.Success)
    }

    state.hiddenTalents.forEach { talent ->
        val accent = when {
            talent.suspicion >= 60 -> SlgColors.Danger
            talent.suspicion >= 40 -> SlgColors.Brass
            else -> SlgColors.Muted
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlgColors.Surface)
                .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlgDonutGauge(
                ratio = talent.suspicion / 100f,
                valueText = "${talent.suspicion}",
                caption = "의심도",
                color = accent,
                modifier = Modifier.size(64.dp),
                thickness = 8f
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        talent.name,
                        color = SlgColors.Text,
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(5.dp))
                    SlgPill(talent.cover, SlgColors.Stone)
                }
                if (talent.suspicion >= SlgGameState.NIGHT_MEETING_SUSPICION) {
                    if (state.nightMeetingTalentName == talent.name && state.nightMeetingChoice == null) {
                        Text(
                            "🌙 밀회 진행 중 — 아래에서 처리",
                            color = SlgColors.Violet,
                            fontSize = SlgType.MICRO
                        )
                    } else {
                        SlgChoiceCard(
                            title = "🌙 야간 밀회",
                            effects = listOf(Effect("👤", "${talent.suspicion}", accent)),
                            accent = SlgColors.Violet,
                            onClick = { onOpenNightMeeting(talent.name) }
                        )
                    }
                } else {
                    Text(
                        "밀회 기준 미달 — ${talent.suspicion}/${SlgGameState.NIGHT_MEETING_SUSPICION}",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO
                    )
                }
            }
        }
        SlgExpand(
            summary = "${talent.name} 동향 — 원문",
            detail = talent.hint,
            accent = accent
        )
    }

    if (state.nightMeetingTalentName != null && state.nightMeetingChoice == null) {
        SlgSectionCard(title = "자정 · 매탄천 다리", badge = "3지선다", accent = SlgColors.Violet) {
            Text(
                "'${state.nightMeetingTalentName}'이(가) 비밀 서신을 손에 쥐고 나타났다.",
                color = SlgColors.Text,
                fontSize = SlgType.BODY_SMALL,
                maxLines = 2
            )
            SlgChoiceCard(
                title = "포섭 — 핵심 참모로",
                icon = "🤝",
                effects = listOf(
                    Effect("👤", "의심 -30", SlgColors.Success),
                    Effect("⭐", "+5", SlgColors.Brass),
                    Effect("🕵", "+10", SlgColors.Violet)
                ),
                accent = SlgColors.Success,
                onClick = { onResolveNightMeeting(NightMeetingChoice.RECRUIT) }
            )
            SlgChoiceCard(
                title = "숙청 — 재산 몰수",
                icon = "⚔",
                effects = listOf(
                    Effect("💰", "+400", SlgColors.Brass),
                    Effect("❤", "-8", SlgColors.Danger)
                ),
                accent = SlgColors.Danger,
                onClick = { onResolveNightMeeting(NightMeetingChoice.PURGE) }
            )
            SlgChoiceCard(
                title = "역이용 — 이중첩자",
                icon = "🕸",
                effects = listOf(
                    Effect("💰", "-150", SlgColors.Danger),
                    Effect("🎭", "광교 교란", SlgColors.Violet)
                ),
                accent = SlgColors.Violet,
                enabled = state.gold >= 150,
                onClick = { onResolveNightMeeting(NightMeetingChoice.DOUBLE_AGENT) }
            )
        }
    }

    if (state.nightMeetingChoice != null) {
        ResultBox(state.nightMeetingResult, SlgColors.Violet)
    }
    if (state.doubleAgentBuff != null) {
        SlgChoiceCard(
            title = "이중첩자 교란 효과 발동",
            icon = "🕸",
            effects = listOf(Effect("🎭", state.doubleAgentBuff, SlgColors.Violet)),
            accent = SlgColors.Violet,
            onClick = onExploitDoubleAgent
        )
    }
}
