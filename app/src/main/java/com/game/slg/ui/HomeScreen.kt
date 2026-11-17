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
import androidx.compose.ui.unit.sp
import com.game.slg.core.map.MapViewport
import com.game.slg.core.model.PromotionStage

/** 메인 화면 고정 높이 — 지도가 남는 공간을 모두 차지한다 */
object HomeLayout {
    const val TOP_BAR = 40
    const val STATUS_STRIP = 88
    const val MENU_GRID = 158

    /** 주어진 화면 높이에서 지도가 차지하는 높이 */
    fun mapHeight(screenHeightDp: Int): Int =
        (screenHeightDp - TOP_BAR - STATUS_STRIP - MENU_GRID).coerceAtLeast(0)
}

/**
 * 메인 화면 — 지도가 주인공인 1면 구성.
 *
 * ```
 * ┌──────────────────────────────┐
 * │ 💰4200 🌾8600 ⚔1200   📊 정보 8 │
 * ├──────────────────────────────┤
 * │      Lv.1 관할 구역 · 탕정면      │  ← 지휘 단계 배지
 * │        제한 범위 지도           │
 * ├──────────────────────────────┤
 * │ ● 탕정면 ⚔1200  Lv.1 관할 ▲    │
 * │ 🚪이재민 🛰정찰조 🏛표결2 →      │
 * ├──────────────────────────────┤
 * │ 🏯   🛠   🏛   ⚔              │
 * │ 영지 기술 의회 전장             │
 * │ 🤝   🔔   👑   🌾              │
 * └──────────────────────────────┘
 * ```
 */
@Composable
fun SlgMainScreen(
    state: SlgGameState,
    viewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    onSelectTerritory: (String) -> Unit,
    onOpenMenu: (SubMenu) -> Unit,
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
            onOpenMenu = onOpenMenu
        )

        SubMenuGrid(
            state = state,
            alerts = alerts,
            onOpenMenu = onOpenMenu,
            onAdvanceSeason = onAdvanceSeason
        )
    }
}

/** 상단 1줄 — 자원 요약 칩 + 📊 정보 버튼 */
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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🏯", fontSize = SlgType.BODY)
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
                .padding(horizontal = 9.dp, vertical = 4.dp)
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
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xF2E2B755))
                .clickable { onDismiss() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = Color(0xFF1A1305), fontSize = SlgType.LABEL, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f), maxLines = 2)
            Text("✕", color = Color(0xFF1A1305), fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
        }
    }
}

/** 선택 영지 1줄 + 승격 점 + 경보 칩 가로 스크롤 */
@Composable
private fun HomeStatusStrip(
    state: SlgGameState,
    alerts: List<StatusAlert>,
    onOpenMenu: (SubMenu) -> Unit
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
                    .size(7.dp)
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
            Spacer(Modifier.width(6.dp))
            Text("❤ ${state.morale}", color = SlgColors.Text, fontSize = SlgType.MICRO)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SlgColors.Brass.copy(alpha = 0.16f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    "Lv.${state.command.level} ${state.expansionTier.label}",
                    color = SlgColors.Brass,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(5.dp))
            PromotionDots(state = state)
            Spacer(Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SlgColors.Brass.copy(alpha = 0.14f))
                    .clickable { onOpenMenu(SubMenu.TERRITORY) }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("영지 ›", color = SlgColors.Brass, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
            }
        }

        if (alerts.isEmpty()) {
            Text("✅ 미결 사건 없음", color = SlgColors.Success, fontSize = SlgType.MICRO)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(alerts, key = { it.id }) { alert ->
                    AlertChip(alert = alert, onClick = { onOpenMenu(alert.target) })
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(alert.icon, fontSize = SlgType.LABEL)
        Spacer(Modifier.width(4.dp))
        Text(alert.short, color = SlgColors.Text, fontSize = SlgType.MICRO, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(3.dp))
        Text("›", color = accent, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
    }
}

/** 승격 3단계 미니 점 — 거점 호족 → 연합 대표 → 광역 통합군주 */
@Composable
private fun PromotionDots(state: SlgGameState) {
    val stages = PromotionStage.entries
    Row(verticalAlignment = Alignment.CenterVertically) {
        stages.forEachIndexed { index, stage ->
            val achieved = stage.order <= state.promotionStage.order
            Box(
                modifier = Modifier
                    .size(if (stage == state.promotionStage) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (achieved) SlgColors.Brass else SlgColors.Line)
            )
            if (index < stages.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(7.dp)
                        .height(1.5.dp)
                        .background(if (stages[index + 1].order <= state.promotionStage.order) SlgColors.Brass else SlgColors.Line)
                )
            }
        }
    }
}

/** 하단 서브 메뉴 — 4×2 타일, 미결 건수 배지 */
@Composable
private fun SubMenuGrid(
    state: SlgGameState,
    alerts: List<StatusAlert>,
    onOpenMenu: (SubMenu) -> Unit,
    onAdvanceSeason: () -> Unit
) {
    val badgeByMenu = alerts.groupingBy { it.target }.eachCount()
    val menus = SubMenu.entries
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(HomeLayout.MENU_GRID.dp)
            .background(Color(0xFF0B0E13))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        menus.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { menu ->
                    SlgMenuTile(
                        emoji = menu.emoji,
                        label = menu.label,
                        badge = badgeByMenu[menu] ?: 0,
                        accent = Color(menu.accentHex),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = {
                            if (menu == SubMenu.SEASON) onAdvanceSeason() else onOpenMenu(menu)
                        }
                    )
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * 📊 정보 패널 — 숫자·규칙·진행이 모두 여기로 모인다.
 */
@Composable
fun SlgInfoOverlay(
    state: SlgGameState,
    onClose: () -> Unit,
    onOpenMenu: (SubMenu) -> Unit,
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
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
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
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("✕", color = SlgColors.Text, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
                }
            }

            // 세력 지표 — 2열 칩 그리드
            MainInfoPanel.detailRows(state).chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pair.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(SlgColors.SurfaceAlt)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(entry.icon, fontSize = SlgType.LABEL)
                            Spacer(Modifier.width(5.dp))
                            Column {
                                Text(entry.label, color = SlgColors.Muted, fontSize = SlgType.MICRO)
                                Text(
                                    entry.value,
                                    color = Color(entry.toneHex),
                                    fontSize = SlgType.LABEL,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            // 지휘 성장 · 해금
            SlgSectionCard(title = "지휘 성장", badge = "Lv.${state.command.level}", accent = SlgColors.Brass) {
                SlgStatBar(ratio = state.command.progressRatio, color = SlgColors.Brass, height = 6)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "다음 레벨까지 ${state.command.xpRemaining} XP",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO
                    )
                    Spacer(Modifier.weight(1f))
                    SlgPill(state.expansionTier.label, SlgColors.Brass, filled = true)
                }
                state.command.nextUnlockLabel()?.let { next ->
                    Text("다음 해금 · $next", color = SlgColors.Steel, fontSize = SlgType.MICRO, lineHeight = 15.sp)
                }
                Text(
                    "작전·절기·의회·시설로 경험치를 얻습니다. 레벨이 오르면 지도의 잠긴 거점이 열립니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = 15.sp
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
                            onOpenMenu(alert.target)
                        }
                        .padding(horizontal = 9.dp, vertical = 7.dp),
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
                        maxLines = 1
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
                    "③ 정보국으로 보급 거점·호족 이간을 준비한다\n" +
                    "④ 호족 65% 이상 반대 시 연합 불복(반란)이 일어난다\n" +
                    "⑤ 우호 거점과 명성으로 연합 대표 → 광역 통합군주로 승격한다",
                accent = SlgColors.Brass
            )

            SlgExpand(
                summary = "지역·호족 규칙",
                detail = "${state.regionFullName} · 인구 ${state.regionPopulation}명\n" +
                    "호족 정원 = 인구 ÷ 1,000 = ${state.lordCapacity}석 (현재 ${state.registeredLords}석 인계)\n" +
                    "AI 호족 ${state.aiLordCount}명이 같은 구역에서 계속 운영됩니다.\n" +
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
