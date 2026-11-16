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
import androidx.compose.ui.graphics.Brush
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
 * │ 💰4200 🌾8600 ⚔1200   📊 정보 8 │  ← 1줄 요약
 * ├──────────────────────────────┤
 * │                              │
 * │        제한 범위 지도          │  ← 화면의 절반 이상
 * │                              │
 * ├──────────────────────────────┤
 * │ ● 매탄동 ⚔1200   ○─●─○ 승격   │  ← 선택 영지 1줄
 * │ 🚪유민800 🏔정찰대 🏛표결2 →    │  ← 경보 칩 가로 스크롤
 * ├──────────────────────────────┤
 * │ 🏯   🛠   🏛   ⚔              │
 * │ 영지 기술 의회 전장             │  ← 서브 메뉴 8칸
 * │ 🤝   🔔   👑   🌾              │
 * └──────────────────────────────┘
 * ```
 *
 * 기존 「현재 상태」 1/3 패널의 긴 문장은 전부 [SlgInfoOverlay] 로 옮겼다.
 */
@Composable
fun SlgMainScreen(
    state: SlgGameState,
    viewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    onSelectTerritory: (String) -> Unit,
    onOpenMenu: (SubMenu) -> Unit,
    onAdvanceSeason: () -> Unit,
    onOpenInfo: () -> Unit
) {
    val alerts = StatusAlertBuilder.build(state)
    Column(modifier = Modifier.fillMaxSize().background(SlgColors.Background)) {
        HomeTopBar(state = state, alertCount = alerts.size, onOpenInfo = onOpenInfo)

        // ── 지도 — 화면의 주인공 (남는 높이 전부) ──
        TerritoryMapPane(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = state,
            viewport = viewport,
            onViewportChange = onViewportChange,
            onSelectTerritory = onSelectTerritory
        )

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
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (selected != null) relationColor(selected.relation.label) else SlgColors.Muted)
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
                "⚔ ${if (selected?.code == SlgWorld.PLAYER_TERRITORY) state.troops else selected?.troops ?: 0}",
                color = SlgColors.Steel,
                fontSize = SlgType.LABEL
            )
            Spacer(Modifier.width(6.dp))
            Text("❤ ${state.morale}", color = SlgColors.Text, fontSize = SlgType.MICRO)
            Spacer(Modifier.weight(1f))
            PromotionDots(state = state)
            Spacer(Modifier.width(6.dp))
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

/** 승격 3단계 미니 점 — 호족 → 성주 → 통합군주 */
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
                        .width(9.dp)
                        .height(1.5.dp)
                        .background(if (stages[index + 1].order <= state.promotionStage.order) SlgColors.Brass else SlgColors.Line)
                )
            }
        }
        Spacer(Modifier.width(5.dp))
        Text(
            state.promotionStage.displayName.substringBefore(" "),
            color = SlgColors.Brass,
            fontSize = SlgType.MICRO,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
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
 * 📊 정보 패널 — 기존 「현재 상태」의 긴 문장이 모두 여기로 모였다.
 * 홈 화면 자체는 숫자와 아이콘만 보여 주고, 읽을 것이 필요한 순간에만 연다.
 */
@Composable
fun SlgInfoOverlay(
    state: SlgGameState,
    onClose: () -> Unit,
    onOpenMenu: (SubMenu) -> Unit
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
                .fillMaxHeight(0.86f)
                .clip(RoundedCornerShape(18.dp))
                .background(SlgColors.Surface)
                .border(1.dp, SlgColors.Brass.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                // 카드 안쪽 탭이 바깥 스크림으로 새지 않게 막는다
                .clickable { }
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                detail = "① 방어 시설(산성·요새)을 먼저 확보하면 사상자가 80% 줄어든다\n" +
                    "② 산악 매복·하천 도하 요격으로 지형 이점을 취한다\n" +
                    "③ 첩자로 군량고·성문·호족 이간을 준비한다\n" +
                    "④ 호족 65% 이상 반대 시 연합 반란이 일어난다\n" +
                    "⑤ 우호·동맹 영지와 명성으로 성주 → 통합군주로 승격한다",
                accent = SlgColors.Brass
            )

            SlgLogBox(state.log, maxLines = 3)
        }
    }
}
