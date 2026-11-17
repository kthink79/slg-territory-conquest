package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.game.slg.core.map.MapViewport
import com.game.slg.core.model.PromotionStage

/** 메인 화면 고정 높이 — 지도가 남는 공간을 모두 차지한다 */
object HomeLayout {
    const val TOP_BAR = 56
    const val STATUS_STRIP = 120

    /** 하단 메인 탭 바 높이 (지도 밖에 놓인다) */
    const val BOTTOM_NAV = 76

    /** 주어진 화면 높이에서 지도가 차지하는 높이 */
    fun mapHeight(screenHeightDp: Int): Int =
        (screenHeightDp - TOP_BAR - STATUS_STRIP - BOTTOM_NAV).coerceAtLeast(0)
}

/**
 * 지도 탭 — 지도가 주인공인 1면 구성.
 *
 * ```text
 * ┌──────────────────────────────────┐
 * │ 💰4200 🌾8600 ⚔1200  🛡 보호 7턴  📊 │
 * ├──────────────────────────────────┤
 * │        제한 범위 지도              │
 * ├──────────────────────────────────┤
 * │ ● 탕정면 ⚔1200  Lv.1 관할 ▲        │
 * │ 🛡 지역 연합 · 스폰 보호 7턴 남음     │
 * │ 🚪이재민 🛰정찰조 🏛표결2 →          │
 * └──────────────────────────────────┘
 *        🗺 지도 🏙 영지 🦸 영웅 ⚔ 작전 🤝 외교
 * ```
 */
@Composable
fun SlgMainScreen(
    state: SlgGameState,
    viewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    onSelectTerritory: (String) -> Unit,
    onOpenTab: (MainTab, Int) -> Unit,
    onAdvanceSeason: () -> Unit,
    onOpenInfo: () -> Unit,
    onDismissLevelUp: () -> Unit = {}
) {
    val alerts = StatusAlertBuilder.build(state)
    Column(modifier = Modifier.fillMaxSize().background(SlgColors.Background)) {
        HomeTopBar(state = state, alertCount = alerts.size, onOpenInfo = onOpenInfo)

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            TerritoryMapPane(
                modifier = Modifier.fillMaxSize(),
                state = state,
                viewport = viewport,
                onViewportChange = onViewportChange,
                onSelectTerritory = onSelectTerritory
            )
            if (state.levelUpMessage != null) {
                LevelUpBanner(
                    message = state.levelUpMessage,
                    onDismiss = onDismissLevelUp
                )
            }
        }

        HomeStatusStrip(
            state = state,
            alerts = alerts,
            onOpenTab = onOpenTab,
            onAdvanceSeason = onAdvanceSeason
        )
    }
}

/** 상단 1줄 — 자원 요약 칩 + 지역 연합 배지 + 📊 정보 버튼 */
@Composable
private fun HomeTopBar(state: SlgGameState, alertCount: Int, onOpenInfo: () -> Unit) {
    val urgent = StatusAlertBuilder.build(state).any { it.severity == AlertSeverity.CRITICAL }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HomeLayout.TOP_BAR.dp)
            .background(Color(0xFF0E1218))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        MainInfoPanel.topSummary(state).take(3).forEach { entry ->
            SlgSummaryChip(
                icon = entry.icon,
                value = entry.value,
                color = Color(entry.toneHex)
            )
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (urgent) SlgColors.Danger.copy(alpha = 0.22f) else SlgColors.Brass.copy(alpha = 0.18f)
                )
                .border(
                    1.dp,
                    if (urgent) SlgColors.Danger.copy(alpha = 0.85f) else SlgColors.Brass.copy(alpha = 0.7f),
                    RoundedCornerShape(50)
                )
                .clickable { onOpenInfo() }
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    MainInfoPanel.BUTTON_LABEL,
                    color = if (urgent) SlgColors.Danger else SlgColors.Text,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.Bold
                )
                if (alertCount > 0) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "$alertCount",
                        color = if (urgent) SlgColors.Danger else SlgColors.Brass,
                        fontSize = SlgType.LABEL,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/** 지휘 레벨업 배너 — 지도 위에 한 번 뜨고 닫힌다 */
@Composable
private fun LevelUpBanner(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xF2E2B755))
                .clickable { onDismiss() }
                .padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message,
                color = Color(0xFF1A1305),
                fontSize = SlgType.LABEL,
                lineHeight = SlgLine.MICRO,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            Text("✕", color = Color(0xFF1A1305), fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
        }
    }
}

/** 선택 영지 1줄 + 지역 연합 상태 + 경보 칩 가로 스크롤 */
@Composable
private fun HomeStatusStrip(
    state: SlgGameState,
    alerts: List<StatusAlert>,
    onOpenTab: (MainTab, Int) -> Unit,
    onAdvanceSeason: () -> Unit
) {
    val selected = state.selectedTerritory
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(HomeLayout.STATUS_STRIP.dp)
            .background(Color(0xFF0E1218))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(relationColor(selected?.relationLabel ?: "관찰 구역"))
            )
            Spacer(Modifier.width(5.dp))
            Text(
                selected?.name ?: "영지 선택",
                color = SlgColors.Text,
                fontSize = SlgType.BODY,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(5.dp))
            Text(
                "⚔ ${if (selected?.isHome == true) state.troops else selected?.troops ?: 0}",
                color = SlgColors.Steel,
                fontSize = SlgType.LABEL
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SlgColors.Brass.copy(alpha = 0.16f))
                    .clickable { onOpenTab(MainTab.DOMAIN, DomainTab.STATS.ordinal) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Lv.${state.command.level} ${state.expansionTier.label} ›",
                    color = SlgColors.Brass,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }

        // 지역 연합 · 스폰 보호 — 남은 턴을 항상 보여 준다
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (state.union.active) SlgColors.Success.copy(alpha = 0.14f)
                        else SlgColors.SurfaceAlt
                    )
                    .border(
                        1.dp,
                        if (state.union.active) SlgColors.Success.copy(alpha = 0.5f) else SlgColors.Line,
                        RoundedCornerShape(9.dp)
                    )
                    .clickable { onOpenTab(MainTab.DIPLOMACY, DiplomacyTab.TREATY.ordinal) }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Text(
                    state.union.statusLabel,
                    color = if (state.union.active) SlgColors.Success else SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(SlgColors.Success.copy(alpha = 0.16f))
                    .border(1.dp, SlgColors.Success.copy(alpha = 0.5f), RoundedCornerShape(9.dp))
                    .clickable { onAdvanceSeason() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    "🌾 절기",
                    color = SlgColors.Success,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (alerts.isEmpty()) {
            Text("✅ 미결 사건 없음", color = SlgColors.Success, fontSize = SlgType.MICRO)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(alerts, key = { it.id }) { alert ->
                    AlertChip(alert = alert, onClick = { onOpenTab(alert.target, alert.targetSub) })
                }
            }
        }
    }
}

@Composable
private fun AlertChip(alert: StatusAlert, onClick: () -> Unit) {
    val accent = Color(alert.severity.accentHex)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(alert.icon, fontSize = SlgType.LABEL)
        Spacer(Modifier.width(4.dp))
        Text(alert.short, color = SlgColors.Text, fontSize = SlgType.MICRO, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(3.dp))
        Text("›", color = accent, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
    }
}

/**
 * 📊 정보 패널 — 숫자·규칙·진행이 모두 여기로 모인다.
 * 글자를 키운 만큼 2열 칩을 1열 행으로 바꿨다.
 */
@Composable
fun SlgInfoOverlay(
    state: SlgGameState,
    onClose: () -> Unit,
    onOpenTab: (MainTab, Int) -> Unit,
    onReplayIntro: () -> Unit = {},
    onRestart: () -> Unit = {}
) {
    val alerts = StatusAlertBuilder.build(state)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD906080C))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(18.dp))
                .background(SlgColors.Surface)
                .border(1.dp, SlgColors.Brass.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .clickable { }
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊 정보", color = SlgColors.Text, fontSize = SlgType.TITLE, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(6.dp))
                SlgPill(state.seasonName.substringBefore(" ("), SlgColors.Stone)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SlgColors.SurfaceAlt)
                        .clickable { onClose() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("✕", color = SlgColors.Text, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
                }
            }

            // 지역 연합 · 스폰 보호 상태
            SlgSectionCard(
                title = "지역 연합",
                badge = if (state.union.active) "${state.union.remainingTurns}턴" else "종료",
                accent = SlgColors.Success
            ) {
                Text(
                    state.union.statusLabel,
                    color = if (state.union.active) SlgColors.Success else SlgColors.Muted,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (state.union.active) {
                        "같은 지역 AI 리더 ${state.unionPartnerCount}명과 자동 연합 상태입니다. " +
                            "지역 안에서는 교전이 없고, 외부 구역의 침공도 보호막이 막습니다. " +
                            "절기를 진행할 때마다 1턴씩 줄어듭니다."
                    } else {
                        "스폰 보호가 끝났습니다. 같은 지역 리더와 교전하고 외부 침공에 대응할 수 있습니다."
                    },
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }

            // 세력 지표 — 큰 글씨 1열 행
            SlgSectionCard(title = "세력 지표", badge = "Lv.${state.command.level}", accent = SlgColors.Brass) {
                MainInfoPanel.detailRows(state).forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .background(SlgColors.SurfaceAlt)
                            .padding(horizontal = 9.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(entry.icon, fontSize = SlgType.BODY_SMALL)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            entry.label,
                            color = SlgColors.Muted,
                            fontSize = SlgType.LABEL,
                            modifier = Modifier.width(48.dp)
                        )
                        Text(
                            entry.value,
                            color = Color(entry.toneHex),
                            fontSize = SlgType.LABEL,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                    }
                }
            }

            // 지휘 성장 · 해금
            SlgSectionCard(title = "지휘 성장", badge = "Lv.${state.command.level}", accent = SlgColors.Brass) {
                SlgStatBar(ratio = state.command.progressRatio, color = SlgColors.Brass, height = 7)
                Text(
                    "다음 레벨까지 ${state.command.xpRemaining} XP · ${state.expansionTier.label}",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO
                )
                state.command.nextUnlockLabel()?.let { next ->
                    Text("다음 해금 · $next", color = SlgColors.Steel, fontSize = SlgType.MICRO, lineHeight = SlgLine.MICRO)
                }
            }

            // 영웅 요약
            SlgSectionCard(
                title = "영웅",
                badge = "${state.heroes.ownedCount}명",
                accent = SlgColors.Violet
            ) {
                Text(
                    "배치 보너스 · ${state.heroBonus.summary()}",
                    color = SlgColors.Text,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
                SlgChoiceCard(
                    title = "영웅 탭 열기",
                    icon = "🦸",
                    effects = listOf(
                        Effect("🪑", "배치 ${state.heroes.placedCount}", SlgColors.Violet),
                        Effect("🔎", "영입 ${state.heroes.ownedCount}/${com.game.slg.core.hero.HeroRoster.ALL.size}", SlgColors.Brass)
                    ),
                    accent = SlgColors.Violet,
                    onClick = {
                        onClose()
                        onOpenTab(MainTab.HERO, HeroTab.PLACE.ordinal)
                    }
                )
            }

            // 경보 — 누르면 해당 화면으로 이동
            Text("미결 사건 ${alerts.size}건", color = SlgColors.Muted, fontSize = SlgType.MICRO)
            alerts.forEach { alert ->
                val accent = Color(alert.severity.accentHex)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(accent.copy(alpha = 0.10f))
                        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                        .clickable {
                            onClose()
                            onOpenTab(alert.target, alert.targetSub)
                        }
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(alert.icon, fontSize = SlgType.BODY_SMALL)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        alert.title,
                        color = SlgColors.Text,
                        fontSize = SlgType.LABEL,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    SlgPill(alert.severity.label, accent)
                    Spacer(Modifier.width(4.dp))
                    Text("›", color = accent, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
                }
            }

            // 규칙 요약 — 화면에서는 접혀 있다
            SlgExpand(
                summary = "전쟁·의회 원칙 요약",
                detail = "① 방호 시설(벙커·통합 방호망)을 먼저 확보하면 사상자가 80% 줄어든다\n" +
                    "② 드론·특수전은 산악·하천, 기갑·화력은 평야에서 강하다\n" +
                    "③ 정보국으로 보급 거점·리더 이간을 준비한다\n" +
                    "④ 리더 65% 이상 반대 시 연합 불복(반란)이 일어난다\n" +
                    "⑤ 우호 거점과 명성으로 연합 대표 → 광역 통합군주로 승격한다",
                accent = SlgColors.Brass
            )

            SlgExpand(
                summary = "지역·리더 규칙",
                detail = "${state.regionFullName} · 인구 ${state.regionPopulation}명\n" +
                    "리더 정원 = 인구 ÷ 1,000 = ${state.lordCapacity}석 (현재 ${state.registeredLords}석 인계)\n" +
                    "AI 리더 ${state.aiLordCount}명이 같은 구역에서 계속 운영됩니다.\n" +
                    "지휘 Lv.1 관할 구역 → Lv.2 인접 읍·면·동 → Lv.3 연접 구역 → Lv.4 시·군 전역 → Lv.6 광역 생활권",
                accent = SlgColors.Steel
            )

            SlgChoiceCard(
                title = "인트로 다시보기",
                icon = "🎬",
                effects = listOf(Effect("📱", "4장면", SlgColors.Violet)),
                accent = SlgColors.Violet,
                onClick = {
                    onClose()
                    onReplayIntro()
                }
            )

            SlgChoiceCard(
                title = "새 게임 — 지역 다시 선택",
                icon = "🔄",
                effects = listOf(Effect("🗺", "저장 삭제", SlgColors.Danger)),
                accent = SlgColors.Danger,
                onClick = {
                    onClose()
                    onRestart()
                }
            )

            SlgLogBox(state.log, maxLines = 3)
        }
    }
}
