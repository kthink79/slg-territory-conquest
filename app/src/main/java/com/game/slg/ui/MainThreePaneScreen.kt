package com.game.slg.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.map.MapNode
import com.game.slg.core.map.MapViewport
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.model.PromotionStage
import com.game.slg.core.model.TerrainType
import kotlin.math.sqrt

/**
 * 메인 3단 화면 — 상단 1/3 지도 · 중간 1/3 현재 상태 · 하단 1/3 서브 메뉴.
 *
 * 사용자 요청: "메인 UI 의 1/3 크기는 맵을 보여줄수 있게 해줘 전체 맵은 너무 크니 내 영지 기준으로
 * 특정 범위 까지만 보이게 하고 확대 축소 기능을 넣어줘 그리고 나머지 1/3은 현재 상태를 보여주는
 * 용도로 쓰고 나머지 영역에는 서브 메뉴 아이콘을 추가해줘"
 */
@Composable
fun MainThreePaneScreen(
    state: SlgGameState,
    viewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    onSelectTerritory: (String) -> Unit,
    onOpenMenu: (SubMenu) -> Unit,
    onAdvanceSeason: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(SlgColors.Background)) {
        // ── 상단 1/3 : 내 영지 중심 제한 범위 지도 ──
        TerritoryMapPane(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = state,
            viewport = viewport,
            onViewportChange = onViewportChange,
            onSelectTerritory = onSelectTerritory
        )
        PaneDivider()
        // ── 중간 1/3 : 현재 상태 ──
        CurrentStatusPane(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = state,
            onOpenMenu = onOpenMenu
        )
        PaneDivider()
        // ── 하단 1/3 : 서브 메뉴 아이콘 ──
        SubMenuPane(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = state,
            onOpenMenu = onOpenMenu,
            onAdvanceSeason = onAdvanceSeason
        )
    }
}

@Composable
private fun PaneDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SlgColors.Line)
    )
}

// ============================================================================
// 상단 1/3 — 제한 범위 + 확대/축소 지도
// ============================================================================

@Composable
fun TerritoryMapPane(
    modifier: Modifier,
    state: SlgGameState,
    viewport: MapViewport,
    onViewportChange: (MapViewport) -> Unit,
    onSelectTerritory: (String) -> Unit
) {
    val currentViewport by rememberUpdatedState(viewport)
    val currentOnViewportChange by rememberUpdatedState(onViewportChange)
    val currentOnSelect by rememberUpdatedState(onSelectTerritory)
    val selectedCode = state.selectedTerritoryCode

    Box(modifier = modifier.background(SlgColors.Surface)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val vp = currentViewport
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        var next = vp
                        if (zoom != 1f) next = next.zoomedBy(zoom)
                        if (pan.x != 0f || pan.y != 0f) {
                            val unit = next.unitPerWorld(width, height) ?: 1f
                            next = next.pannedBy(-pan.x / unit, -pan.y / unit)
                        }
                        if (next != vp) currentOnViewportChange(next)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val node = currentViewport.hitTest(
                            pointX = offset.x,
                            pointY = offset.y,
                            widthPx = size.width.toFloat(),
                            heightPx = size.height.toFloat()
                        )
                        if (node != null) currentOnSelect(node.code)
                    }
                }
        ) {
            drawTerritoryMap(
                viewport = viewport,
                selectedCode = selectedCode,
                homesOnlyTroops = state.troops
            )
        }

        // ── 좌상단: 화면 제목 + 표시 범위 안내 ──
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 10.dp)
        ) {
            Text(
                "🗺 매탄동 영향권",
                color = SlgColors.Brass,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "hop ≤ ${TerritoryMapLayout.LOCAL_HOP_LIMIT} · 핀치/＋－ · 탭 선택",
                color = SlgColors.Muted,
                fontSize = 7.5.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SlgPill(state.promotionStage.displayName, SlgColors.Brass)
                SlgPill(state.seasonName.substringBefore(" ("), SlgColors.Steel)
            }
        }

        // ── 우상단: 확대 · 축소 · 내 영지 중심 ──
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 10.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            MapControlButton(label = "＋", onClick = {
                currentOnViewportChange(currentViewport.zoomIn())
            })
            MapControlButton(label = "－", onClick = {
                currentOnViewportChange(currentViewport.zoomOut())
            })
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SlgColors.SurfaceAlt.copy(alpha = 0.92f))
                    .border(1.dp, SlgColors.Brass.copy(alpha = 0.55f), RoundedCornerShape(50))
                    .clickable { currentOnViewportChange(currentViewport.reset()) }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "내 영지 중심",
                    tint = SlgColors.Brass,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text("내 영지", color = SlgColors.Brass, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── 좌하단: 선택 영지 요약 ──
        val selectedInfo = state.selectedTerritory
        val selectedNode = state.selectedMapNode
        if (selectedInfo != null && selectedNode != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SlgColors.SurfaceAlt.copy(alpha = 0.92f))
                    .border(1.dp, relationColor(selectedInfo.relation.label).copy(alpha = 0.6f), RoundedCornerShape(9.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(relationColor(selectedInfo.relation.label))
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "${selectedInfo.name} · ${selectedInfo.relation.label}",
                    color = SlgColors.Text,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        // ── 우하단: 배율 + 범위 밖 개수 ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "배율 ${"%.1f".format(viewport.scale)}×",
                color = SlgColors.Muted,
                fontSize = 8.sp
            )
            Text(
                "범위 밖 ${TerritoryMapLayout.outOfRangeCount()}곳",
                color = SlgColors.Steel,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun MapControlButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(SlgColors.SurfaceAlt.copy(alpha = 0.92f))
            .border(1.dp, SlgColors.Line, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = SlgColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * 지도 렌더링 — 지형지물(하천·도로), 영지 노드, 영향권 경계를 1/3 높이에서도 읽히게 그린다.
 */
private fun DrawScope.drawTerritoryMap(
    viewport: MapViewport,
    selectedCode: String,
    homesOnlyTroops: Int
) {
    val width = size.width
    val height = size.height
    val unit = viewport.unitPerWorld(width, height) ?: return

    fun sx(worldX: Float) = width / 2f + (worldX - viewport.centerX) * unit
    fun sy(worldY: Float) = height / 2f + (worldY - viewport.centerY) * unit

    // 배경 — 밤 지도 톤
    drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF111A24), Color(0xFF0B0F16))),
        size = size
    )

    // 좌표 격자 (0.25 세계 단위) — 지도감 부여
    val gridStep = 0.25f
    var gx = -TerritoryMapLayout.WORLD_HALF_EXTENT
    while (gx <= TerritoryMapLayout.WORLD_HALF_EXTENT) {
        val x = sx(gx)
        if (x in 0f..width) {
            drawLine(
                color = Color(0xFF1B2634),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
        }
        gx += gridStep
    }
    var gy = -TerritoryMapLayout.WORLD_HALF_EXTENT
    while (gy <= TerritoryMapLayout.WORLD_HALF_EXTENT) {
        val y = sy(gy)
        if (y in 0f..height) {
            drawLine(
                color = Color(0xFF1B2634),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        gy += gridStep
    }

    // 매탄천(하천)
    val river = TerritoryMapLayout.RIVER
    for (index in 0 until river.size - 1) {
        drawLine(
            color = Color(0xFF2C5170),
            start = Offset(sx(river[index].first), sy(river[index].second)),
            end = Offset(sx(river[index + 1].first), sy(river[index + 1].second)),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
    }
    for (index in 0 until river.size - 1) {
        drawLine(
            color = Color(0xFF4E7C9E).copy(alpha = 0.75f),
            start = Offset(sx(river[index].first), sy(river[index].second)),
            end = Offset(sx(river[index + 1].first), sy(river[index + 1].second)),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }

    // 본진 영향권 경계 — 점선 원 + 안내 라벨
    val homeX = sx(0f)
    val homeY = sy(0f)
    drawCircle(
        color = SlgColors.Brass.copy(alpha = 0.45f),
        radius = TerritoryMapLayout.INFLUENCE_RADIUS * unit,
        center = Offset(homeX, homeY),
        style = Stroke(
            width = 1.6f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f)
        )
    )

    // 본진 → 각 영지 간선 도로
    TerritoryMapLayout.localNodes().forEach { node ->
        if (node.isHome) return@forEach
        drawLine(
            color = SlgColors.Brass.copy(alpha = 0.28f),
            start = Offset(homeX, homeY),
            end = Offset(sx(node.worldX), sy(node.worldY)),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )
    }

    val labelPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val subPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT
    }

    // 영지 노드 — 범위 안쪽(hop ≤ 2)이고 화면 안에 있는 것만
    val visible = viewport.visibleNodes(TerritoryMapLayout.localNodes())
    visible.forEach { node ->
        val cx = sx(node.worldX)
        val cy = sy(node.worldY)
        val selected = node.code == selectedCode
        val relation = relationColor(node.relationLabel)
        val radius = (13f * sqrt(viewport.scale)).coerceIn(9f, 22f)

        if (selected) {
            drawCircle(
                color = SlgColors.Brass.copy(alpha = 0.28f),
                radius = radius + 7f,
                center = Offset(cx, cy)
            )
        }
        if (node.isHome) {
            drawCircle(
                color = SlgColors.Brass.copy(alpha = 0.22f),
                radius = radius + 5f,
                center = Offset(cx, cy)
            )
        }
        drawCircle(color = relation.copy(alpha = 0.9f), radius = radius, center = Offset(cx, cy))
        drawCircle(color = SlgColors.Surface, radius = radius - 3f, center = Offset(cx, cy))
        drawCircle(
            color = if (node.isHome) SlgColors.Brass else relation,
            radius = radius - 6f,
            center = Offset(cx, cy)
        )
        if (selected) {
            drawCircle(
                color = SlgColors.Brass,
                radius = radius + 3f,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )
        }

        // 지형 표식 (산악 ▲, 하천 ≈, 평야 ·)
        val terrainMark = when (node.terrain) {
            TerrainType.MOUNTAIN -> "▲"
            TerrainType.RIVER -> "≈"
            TerrainType.COASTAL -> "〜"
            else -> "·"
        }
        labelPaint.textSize = (10f * sqrt(viewport.scale)).coerceIn(8f, 14f)
        labelPaint.color = (if (selected) android.graphics.Color.WHITE else android.graphics.Color.rgb(237, 234, 227))
        labelPaint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        drawContext.canvas.nativeCanvas.drawText(
            "${node.name} $terrainMark",
            cx,
            cy - radius - 4f,
            labelPaint
        )
        subPaint.textSize = (8.5f * sqrt(viewport.scale)).coerceIn(7f, 12f)
        subPaint.color = android.graphics.Color.rgb(152, 161, 176)
        val sub = if (node.isHome) "본진 · 병력 $homesOnlyTroops" else "${node.relationLabel} · 병력 ${node.troops}"
        drawContext.canvas.nativeCanvas.drawText(sub, cx, cy + radius + 9f, subPaint)
    }

    // 지도 가장자리 안개 — 표시 범위가 여기서 끝난다는 시각 신호
    val fog = SlgColors.Background
    val fogWidth = height * 0.16f
    drawRect(
        brush = Brush.horizontalGradient(listOf(fog, fog.copy(alpha = 0f)), startX = 0f, endX = fogWidth),
        topLeft = Offset(0f, 0f),
        size = Size(fogWidth, height)
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(fog.copy(alpha = 0f), fog), startX = width - fogWidth, endX = width),
        topLeft = Offset(width - fogWidth, 0f),
        size = Size(fogWidth, height)
    )
}

// ============================================================================
// 중간 1/3 — 현재 상태
// ============================================================================

@Composable
fun CurrentStatusPane(
    modifier: Modifier,
    state: SlgGameState,
    onOpenMenu: (SubMenu) -> Unit
) {
    val alerts = StatusAlertBuilder.build(state)
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 13.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SlgColors.Brass)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "현재 상태",
                color = SlgColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "D+14 · ${state.seasonName.substringBefore(" (")}",
                color = SlgColors.Muted,
                fontSize = 8.sp,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            SlgPill("${state.promotionStage.displayName}", SlgColors.Brass)
        }

        Spacer(Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 승격 의식 알림
            if (state.promotionCeremony != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(SlgColors.Brass.copy(alpha = 0.18f))
                        .border(1.dp, SlgColors.Brass.copy(alpha = 0.6f), RoundedCornerShape(9.dp))
                        .clickable { onOpenMenu(SubMenu.PROMOTION) }
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "👑 ${state.promotionStage.displayName} 승격 — 의회 권한 +${state.councilAuthorityBonus}%",
                        color = SlgColors.Brass,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text("로드맵 ›", color = SlgColors.Brass, fontSize = 8.sp)
                }
            }

            // 자원
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SlgResourceChip("💰", "자금", "${state.gold}", SlgColors.Brass, Modifier.weight(1f))
                SlgResourceChip("🌾", "군량", "${state.food}", SlgColors.Success, Modifier.weight(1f))
                SlgResourceChip("⚔", "병력", "${state.troops}", SlgColors.Steel, Modifier.weight(1f))
                SlgResourceChip("🛡", "보급", "${state.morale}", SlgColors.Text, Modifier.weight(1f))
            }

            // 선택 영지 상세
            val territory = state.selectedTerritory
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(SlgColors.Surface)
                    .clickable { onOpenMenu(SubMenu.TERRITORY) }
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            territory?.name ?: "영지 미선택",
                            color = SlgColors.Text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(5.dp))
                        if (territory != null) {
                            SlgPill(territory.relation.label, relationColor(territory.relation.label))
                        }
                    }
                    Text(
                        territory?.let {
                            "${it.terrain.name} · 인구 ${it.population} · 병력 ${it.troops} · ${it.owner}"
                        } ?: "지도에서 영지를 탭하세요",
                        color = SlgColors.Muted,
                        fontSize = 8.sp
                    )
                    if (territory != null) {
                        Text(territory.note, color = SlgColors.Muted, fontSize = 8.sp)
                    }
                }
                Text("내정 ›", color = SlgColors.Brass, fontSize = 8.5.sp)
            }

            // 승격 진행
            PromotionProgressRow(state = state, onOpenPromotion = { onOpenMenu(SubMenu.PROMOTION) })

            // 활성 경보/이벤트
            if (alerts.isEmpty()) {
                Text("활성 경보 없음 — 절기를 진행해 새 사건을 받으세요.", color = SlgColors.Muted, fontSize = 8.5.sp)
            } else {
                alerts.forEach { alert ->
                    AlertRow(alert = alert, onClick = { onOpenMenu(alert.target) })
                }
            }

            // 최근 기록
            Text(
                "· ${state.log.lastOrNull() ?: "기록 없음"}",
                color = SlgColors.Muted,
                fontSize = 8.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun PromotionProgressRow(state: SlgGameState, onOpenPromotion: () -> Unit) {
    val stages = PromotionStage.entries.toList()
    val next = state.nextPromotionStage
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(SlgColors.Surface)
            .clickable { onOpenPromotion() }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "승격 진행",
                color = SlgColors.Text,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
            stages.forEachIndexed { index, stage ->
                val achieved = stage.order <= state.promotionStage.order
                Text(
                    if (achieved) "●" else "○",
                    color = if (achieved) SlgColors.Brass else SlgColors.Line,
                    fontSize = 8.sp
                )
                if (index < stages.lastIndex) {
                    Text("─", color = SlgColors.Line, fontSize = 6.sp)
                }
            }
            Spacer(Modifier.width(5.dp))
            Text(
                stages.joinToString(" → ") { it.displayName.substringBefore(" ") },
                color = SlgColors.Muted,
                fontSize = 7.5.sp
            )
        }
        if (next != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("영지", color = SlgColors.Muted, fontSize = 8.sp, modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.weight(1f)) {
                    SlgStatBar(
                        ratio = if (next.requiredAlliedTerritories == 0) 1f
                        else state.alliedTerritories.toFloat() / next.requiredAlliedTerritories,
                        color = SlgColors.Steel,
                        height = 5
                    )
                }
                Text(
                    " ${state.alliedTerritories}/${next.requiredAlliedTerritories}",
                    color = SlgColors.Steel,
                    fontSize = 8.sp
                )
                Spacer(Modifier.width(6.dp))
                Text("명성", color = SlgColors.Muted, fontSize = 8.sp)
                Box(modifier = Modifier.weight(1f)) {
                    SlgStatBar(
                        ratio = if (next.requiredPrestige == 0) 1f
                        else state.prestige.toFloat() / next.requiredPrestige,
                        color = SlgColors.Brass,
                        height = 5
                    )
                }
                Text(
                    " ${state.prestige}/${next.requiredPrestige}",
                    color = SlgColors.Brass,
                    fontSize = 8.sp
                )
            }
        } else {
            Text("로드맵 완주 — 수원시 통합군주", color = SlgColors.Brass, fontSize = 8.5.sp)
        }
    }
}

@Composable
private fun AlertRow(alert: StatusAlert, onClick: () -> Unit) {
    val accent = Color(alert.severity.accentHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.5.dp, height = 22.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(Modifier.width(7.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                alert.title,
                color = SlgColors.Text,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(alert.detail, color = SlgColors.Muted, fontSize = 7.5.sp, lineHeight = 11.sp)
        }
        Spacer(Modifier.width(4.dp))
        Text("${alert.severity.label} ›", color = accent, fontSize = 8.sp)
    }
}

// ============================================================================
// 하단 1/3 — 서브 메뉴 아이콘
// ============================================================================

@Composable
fun SubMenuPane(
    modifier: Modifier,
    state: SlgGameState,
    onOpenMenu: (SubMenu) -> Unit,
    onAdvanceSeason: () -> Unit
) {
    val alerts = StatusAlertBuilder.build(state)
    val alertCountByMenu = alerts.groupingBy { it.target }.eachCount()

    Column(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 13.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SlgColors.Steel)
            )
            Spacer(Modifier.width(6.dp))
            Text("서브 메뉴", color = SlgColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text("탭하여 상세 화면 열기", color = SlgColors.Muted, fontSize = 8.sp)
        }

        Spacer(Modifier.height(6.dp))

        val menus = SubMenu.entries.toList()
        val rows = menus.chunked(4)
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    row.forEach { menu ->
                        SubMenuCard(
                            menu = menu,
                            badge = alertCountByMenu[menu] ?: 0,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = {
                                when {
                                    menu == SubMenu.SEASON -> onAdvanceSeason()
                                    menu.opensDetail -> onOpenMenu(menu)
                                    else -> Unit
                                }
                            }
                        )
                    }
                    // 마지막 줄 빈 칸 채우기
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun SubMenuCard(
    menu: SubMenu,
    badge: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val accent = Color(menu.accentHex)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(SlgColors.Surface)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = menu.iconVector(),
                    contentDescription = menu.label,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                menu.label,
                color = SlgColors.Text,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                lineHeight = 11.sp
            )
            Text(
                menu.caption,
                color = SlgColors.Muted,
                fontSize = 7.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                lineHeight = 9.sp
            )
        }
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(SlgColors.Danger),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", color = Color(0xFF10131A), fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** 서브 메뉴 → Material 아이콘 */
fun SubMenu.iconVector(): ImageVector = when (iconKey) {
    SubMenuIcon.HOME -> Icons.Filled.Home
    SubMenuIcon.TECH -> Icons.Filled.Build
    SubMenuIcon.COUNCIL -> Icons.Filled.AccountBox
    SubMenuIcon.WAR -> Icons.Filled.Warning
    SubMenuIcon.DIPLOMACY -> Icons.Filled.Email
    SubMenuIcon.EVENT -> Icons.Filled.Notifications
    SubMenuIcon.PROMOTION -> Icons.Filled.Star
    SubMenuIcon.SEASON -> Icons.Filled.Refresh
}
