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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.game.slg.R
import com.game.slg.core.model.BillType
import com.game.slg.core.region.AdjacentActionKind
import com.game.slg.core.region.AidDirection
import com.game.slg.core.region.CoalitionBill

/**
 * 서브 메뉴 「리더 의회」 — 탭 4개: 안건 / 연합 / 긴급 / 리더.
 *
 * 사용자 요청(2026-11-25 07:45)
 *  · 의회에 안건을 올려 지역 리더 연합으로 공동 전투·외교·전략을 진행한다.
 *  · 외부 침공 때 긴급 의회가 열려 방어 안건을 상정·가결한다.
 *  · 편입한 리더 수만큼 의결권(표)을 행사한다.
 */
@Composable
fun CouncilScreen(
    state: SlgGameState,
    onVoteBill: (String) -> Unit,
    onSponsorCoalition: (AdjacentActionKind) -> Unit = {},
    onVoteCoalition: (String) -> Unit = {},
    onVoteEmergency: () -> Unit = {},
    onRequestAid: () -> Unit = {},
    onAnswerAidCall: () -> Unit = {},
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (CouncilTab.entries[tabIndex.coerceIn(0, CouncilTab.entries.lastIndex)]) {
            CouncilTab.BILLS -> CouncilBills(state, onVoteBill)
            CouncilTab.COALITION -> CouncilCoalition(state, onSponsorCoalition, onVoteCoalition)
            CouncilTab.EMERGENCY -> CouncilEmergency(state, onVoteEmergency, onRequestAid, onAnswerAidCall)
            CouncilTab.LORDS -> CouncilLords(state)
        }
    }
}

@Composable
private fun CouncilBills(state: SlgGameState, onVoteBill: (String) -> Unit) {
    SlgArtHeader(
        image = R.drawable.slg_council_chamber,
        title = "${state.regionName} 리더 의회",
        badges = listOf("🏛 대표 권한" to SlgColors.Steel, "권한 +${state.councilAuthorityBonus}%" to SlgColors.Brass),
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

    // 의결권 — 편입한 리더 수만큼 표를 행사한다
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlgColors.Brass.copy(alpha = 0.12f))
            .border(1.dp, SlgColors.Brass.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🗳", fontSize = SlgType.BODY)
        Spacer(Modifier.width(7.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                state.councilVotingLabel,
                color = SlgColors.Brass,
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "직할·편입 리더가 늘수록 표가 늘어납니다 — 표결은 모두 찬성으로 들어갑니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO
            )
        }
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
                        BillType.TROOP_LEVY -> "인력 소집"
                        BillType.SPECIAL_TAX -> "특별 조례"
                        BillType.FORTRESS_EXPAND -> "시설 증설"
                        BillType.JOINT_WAR -> "공동 전투"
                        BillType.JOINT_DIPLOMACY -> "공동 외교"
                        BillType.JOINT_STRATEGY -> "공동 전략"
                        BillType.EMERGENCY_DEFENSE -> "긴급 방어"
                        else -> "무력 결의"
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
                SlgPill("불복 위험 — 반대 리더 연합", SlgColors.Danger, filled = true)
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
        detail = "대표 리더은 경비 인력 소집·특별 조례를 상정할 수 있으나, 리더 65% 이상이 반대하면 " +
            "연합 불복이 일어납니다. 승격할 때마다 의회 권한 보너스가 올라 반대표를 낮춥니다. " +
            "직할·속국·자치령으로 편입한 리더 1명당 의결권 1표가 더해집니다.",
        accent = SlgColors.Steel
    )
}

/** 지역 리더 연합 — 공동 전투·외교·전략 안건 */
@Composable
private fun CouncilCoalition(
    state: SlgGameState,
    onSponsor: (AdjacentActionKind) -> Unit,
    onVote: (String) -> Unit
) {
    SlgArtHeader(
        image = R.drawable.slg_council_chamber,
        title = "지역 리더 연합 의회",
        badges = listOf(
            "🤝 참여 ${state.coalition.supporters}명" to SlgColors.Steel,
            "🗳 ${state.councilVotingPower}표" to SlgColors.Brass
        ),
        height = 128
    )

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        SlgPill("대기 ${state.coalition.pendingCount}건", if (state.coalition.pendingCount > 0) SlgColors.Brass else SlgColors.Muted)
        if (state.coalition.battleBonusPercent > 0) SlgPill("⚔ +${state.coalition.battleBonusPercent}%", SlgColors.Danger)
        if (state.coalition.diplomacyBonus > 0) SlgPill("🤝 +${state.coalition.diplomacyBonus}", SlgColors.Success)
        if (state.coalition.strategyDiscountPercent > 0) SlgPill("🧠 -${state.coalition.strategyDiscountPercent}%", SlgColors.Violet)
    }

    val adjacent = state.selectedAdjacentPanel
    if (adjacent != null) {
        SlgSectionCard(
            title = "안건 상정 · ${adjacent.name}",
            badge = adjacent.headcountLabel,
            accent = SlgColors.Brass
        ) {
            Text(
                "${adjacent.relationLabel} · ${adjacent.headline}",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                AdjacentActionKind.entries.forEach { kind ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(SlgColors.Brass.copy(alpha = 0.16f))
                            .border(1.dp, SlgColors.Brass.copy(alpha = 0.55f), RoundedCornerShape(11.dp))
                            .padding(vertical = 10.dp)
                            .clickable { onSponsor(kind) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${kind.icon} ${kind.label}",
                            color = SlgColors.Brass,
                            fontSize = SlgType.LABEL,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
            Text(
                "안건을 올리면 참여 리더의 찬성표가 쌓이고, 표결에서 내 의결권 ${state.councilVotingPower}표가 더해집니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
        }
    } else {
        SlgSectionCard(title = "안건 상정", badge = "지역 미선택", accent = SlgColors.Line) {
            Text(
                "지도에서 인접 지역(온양2동·배방읍·음봉면 등)을 누르고 하단 패널의 전투·외교·전략을 열면 " +
                    "「의회에 공동 작전 안건 상정」이 있습니다. 여기에 안건이 쌓입니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
        }
    }

    if (state.coalition.bills.isEmpty()) {
        SlgSectionCard(title = "안건 기록", badge = "없음", accent = SlgColors.Stone) {
            Text(
                "아직 올린 연합 안건이 없습니다. 공동 전투로 이기면 다음 교전에 보너스가 붙고, " +
                    "공동 외교는 협상 성공률을, 공동 전략은 공작 비용을 낮춥니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
        }
    }

    state.coalition.bills.reversed().forEach { bill ->
        CoalitionBillCard(bill = bill, onVote = onVote)
    }

    SlgExpand(
        summary = "연합 의회 규칙",
        detail = "① 지도에서 인접 지역을 고르고 전투·외교·전략 시트에서 안건을 상정합니다.\n" +
            "② 찬성표는 참여 리더(지역 연합 중 리더·조약 상대) 수로 정해집니다.\n" +
            "③ 표결에서 내 의결권(기본 1 + 직할·편입 리더 수)이 찬성으로 더해집니다.\n" +
            "④ 가결되면 공동 전투 +4~18%·공동 외교 +8·공동 전략 비용 -30%가 다음 행동에 붙습니다.",
        accent = SlgColors.Brass
    )
}

@Composable
private fun CoalitionBillCard(bill: CoalitionBill, onVote: (String) -> Unit) {
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
                maxLines = 2
            )
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
        }
        SlgVoteSplit(
            favor = bill.favor,
            against = bill.against,
            modifier = Modifier.fillMaxWidth().height(24.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "찬성 ${bill.favor} · 반대 ${bill.against} · 내 표 ${bill.ourVotes}",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO
            )
            Spacer(Modifier.weight(1f))
            if (bill.pending) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(SlgColors.Brass)
                        .clickable { onVote(bill.id) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        "🗳 표결",
                        color = androidx.compose.ui.graphics.Color(0xFF10131A),
                        fontSize = SlgType.LABEL,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        if (bill.message != null) {
            ResultBox(bill.message, if (bill.passed == true) SlgColors.Success else SlgColors.Danger)
        }
    }
}

/** 긴급 의회 — 외부 침공 방어 · 상호 지원 */
@Composable
private fun CouncilEmergency(
    state: SlgGameState,
    onVote: () -> Unit,
    onRequestAid: () -> Unit,
    onAnswerAidCall: () -> Unit
) {
    SlgArtHeader(
        image = R.drawable.slg_council_chamber,
        title = "긴급 의회 · 상호 방어",
        badges = listOf(
            "🚨 ${if (state.emergencyOpen) "소집 중" else "평시"}" to
                if (state.emergencyOpen) SlgColors.Danger else SlgColors.Muted,
            "🗳 ${state.councilVotingPower}표" to SlgColors.Brass
        ),
        height = 128
    )

    val council = state.emergency
    if (council == null) {
        SlgSectionCard(title = "긴급 의회", badge = "열린 안건 없음", accent = SlgColors.Line) {
            Text(
                "타 지역 세력이 우리 지역을 침공하면 자동으로 긴급 의회가 열립니다. " +
                    "방어 결의를 가결하면 2절기 동안 방어 +12%가 붙습니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
        }
    } else {
        SlgSectionCard(
            title = council.headline,
            badge = if (council.pending) "표결 대기" else if (council.passed == true) "가결" else "부결",
            accent = if (council.pending) SlgColors.Danger else SlgColors.Steel
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SlgPill("침공 ${council.invaderName}", SlgColors.Danger)
                SlgPill("피해 ${"%,d".format(council.troopsLost)}명", SlgColors.Muted)
                SlgPill("남은 ${council.turnsLeft}절기", SlgColors.Stone)
            }
            SlgVoteSplit(
                favor = council.favor,
                against = council.against,
                modifier = Modifier.fillMaxWidth().height(26.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "찬성 ${council.favor} · 반대 ${council.against} · 내 표 ${council.ourVotes}",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (council.pending) "긴급 방어 결의 — 방어 +12%" else if (council.passed == true) "방어 태세 유지" else "각자 대응",
                    color = if (council.passed == true) SlgColors.Success else SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.Bold
                )
            }
            if (council.pending) {
                SlgChoiceCard(
                    title = "긴급 방어 결의 표결",
                    icon = "🚨",
                    effects = listOf(
                        Effect("🛡", "방어 +12%", SlgColors.Steel),
                        Effect("❤", "민심 +4", SlgColors.Success)
                    ),
                    accent = SlgColors.Danger,
                    onClick = onVote
                )
            }
            if (council.message != null) {
                ResultBox(council.message, if (council.passed == true) SlgColors.Success else SlgColors.Danger)
            }

            // 방어 도움 요청
            val requested = council.aids.filter { it.direction == AidDirection.REQUESTED }
            if (requested.isEmpty()) {
                SlgChoiceCard(
                    title = "인근 우호 리더에게 방어 도움 요청",
                    icon = "🤝",
                    effects = listOf(
                        Effect("⚔", "지원군", SlgColors.Steel),
                        Effect("💰", "외교·명성", SlgColors.Success)
                    ),
                    accent = SlgColors.Success,
                    onClick = onRequestAid
                )
            } else {
                SlgPill("지원군 +${"%,d".format(council.requestedTroops)}명", SlgColors.Success, filled = true)
                requested.forEach { aid ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤝 ${aid.leaderName}", color = SlgColors.Text, fontSize = SlgType.MICRO)
                        Spacer(Modifier.weight(1f))
                        Text("+${"%,d".format(aid.troops)}", color = SlgColors.Steel, fontSize = SlgType.MICRO)
                    }
                }
            }
        }
    }

    // 다른 리더가 공격받을 때 — 내가 지원군을 보낼 수 있다
    val call = state.aidCall
    if (call != null) {
        SlgSectionCard(
            title = "지원 요청",
            badge = if (call.answered) "응답 완료" else "대기",
            accent = if (call.answered) SlgColors.Success else SlgColors.Brass
        ) {
            Text(
                call.headline,
                color = SlgColors.Text,
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.Bold
            )
            Text(
                "우리 병력의 10%(최대 800명)를 보내면 ${call.victimName} 리더의 ${call.kind.label}을 막아냅니다 — " +
                    "명성 +5 · 외교 +6.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            if (!call.answered) {
                SlgChoiceCard(
                    title = "지원군 파병",
                    icon = "🚚",
                    effects = listOf(
                        Effect("⚔", "-${"%,d".format((state.totalTroops * 0.10).toInt().coerceAtMost(800))}", SlgColors.Steel),
                        Effect("⭐", "+5", SlgColors.Brass)
                    ),
                    accent = SlgColors.Brass,
                    onClick = onAnswerAidCall
                )
            }
            if (call.result != null) {
                ResultBox(call.result, SlgColors.Success)
            }
        }
    }

    if (state.aidLog.isNotEmpty()) {
        SlgSectionCard(title = "방어 지원 기록", badge = "${state.aidLog.size}건", accent = SlgColors.Steel) {
            state.aidLog.takeLast(5).reversed().forEach { aid ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${if (aid.direction == AidDirection.REQUESTED) "받음" else "보냄"} · ${aid.leaderName}",
                        color = SlgColors.Text,
                        fontSize = SlgType.MICRO,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${if (aid.direction == AidDirection.REQUESTED) "+" else "-"}${"%,d".format(aid.troops)}",
                        color = if (aid.direction == AidDirection.REQUESTED) SlgColors.Success else SlgColors.Danger,
                        fontSize = SlgType.MICRO
                    )
                }
            }
        }
    }

    SlgExpand(
        summary = "긴급 의회 · 상호 방어 규칙",
        detail = "① 외부 세력의 습격을 받으면 절기가 넘어갈 때 긴급 의회가 자동으로 열립니다.\n" +
            "② 방어 결의는 표결로 정해지고, 가결되면 2절기 동안 방어 +12%·민심 +4가 붙습니다.\n" +
            "③ 내 영지가 침공당하면 인근 우호 리더에게 지원을 요청해 병력을 받을 수 있습니다.\n" +
            "④ 반대로 다른 리더가 병합·종속당할 때 지원군을 보내 막아낼 수 있습니다(명성·외교 상승).\n" +
            "⑤ 모든 표결에는 내 의결권(기본 1 + 직할·편입 리더 수)이 들어갑니다.",
        accent = SlgColors.Danger
    )
}

@Composable
private fun CouncilLords(state: SlgGameState) {
    val lords = buildList {
        state.lords.filter { it.isPlayer }.forEach { lord ->
            add(Triple(lord.name, "대표 · 플레이어", state.troops) to state.prestige)
        }
        state.lords.filter { !it.isPlayer }.take(11).forEach { lord ->
            add(Triple(lord.name, lord.traitLabel, lord.troops) to lord.prestige)
        }
    }
    val maxTroops = lords.maxOf { it.first.third }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(SlgColors.Brass.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🗳", fontSize = SlgType.BODY_SMALL)
            Spacer(Modifier.width(6.dp))
            Text(
                state.councilVotingLabel,
                color = SlgColors.Brass,
                fontSize = SlgType.MICRO,
                fontWeight = FontWeight.ExtraBold
            )
        }
        lords.forEach { (info, prestige) ->
            val (name, role, troops) = info
            val isPlayer = role.contains("플레이어")
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
                            if (isPlayer) SlgColors.Brass.copy(alpha = 0.22f)
                            else SlgColors.SurfaceAlt
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.substring(0, 1),
                        color = if (isPlayer) SlgColors.Brass else SlgColors.Muted,
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${name} 리더", color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(5.dp))
                        SlgPill(role, if (isPlayer) SlgColors.Brass else SlgColors.Muted)
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
            summary = "리더 ${lords.size}석 구성",
            detail = "${state.regionFullName}에는 리더 정원 ${state.lordCapacity}석이 있습니다. " +
                "그중 1석을 인계받았고 나머지 ${state.aiLordCount}석은 다른 리더가 운영합니다. " +
                "표결에서 반대 65%를 넘기면 연합 불복으로 이어집니다. " +
                "직할·편입한 리더 ${state.incorporatedLordCount}명이 내 의결권에 더해집니다.",
            accent = SlgColors.Brass
        )
    }
}
