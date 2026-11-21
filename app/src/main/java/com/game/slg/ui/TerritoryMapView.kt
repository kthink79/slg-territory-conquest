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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.game.slg.core.map.MapNode
import com.game.slg.core.map.MapViewport
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.model.TerrainType
import com.game.slg.core.region.RegionCatalog

/**
 * 메인 화면 지도 — 선택 지역을 중심으로 한 제한 범위 + 단계별 안개 해제.
 *
 * 지도는 화면의 주인공이다. 해금된 거점은 세력 색과 병력 바로,
 * 잠긴 거점은 안개(어두운 원 + 자물쇠)로 그려 다음 확장 목표를 보여 준다.
 */
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
    val nodes = state.mapNodes
    val selectedCode = state.selectedTerritoryCode

    Box(modifier = modifier.background(SlgColors.Surface)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(nodes) {
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
                .pointerInput(nodes) {
                    detectTapGestures { offset ->
                        val node = currentViewport.hitTest(
                            pointX = offset.x,
                            pointY = offset.y,
                            widthPx = size.width.toFloat(),
                            heightPx = size.height.toFloat(),
                            nodes = nodes
                        )
                        if (node != null) currentOnSelect(node.code)
                    }
                }
        ) {
            drawTerritoryMap(
                viewport = viewport,
                nodes = nodes,
                selectedCode = selectedCode,
                homeTroops = state.troops,
                unlockedHop = state.unlockedHop,
                enemyPressure = state.eventBChoice == null
            )
        }

        // ── 좌상단: 지역 + 지휘 단계 ──
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SlgPill("🗺 ${state.regionName} 영향권", SlgColors.Brass)
                SlgPill("Lv.${state.command.level} ${state.expansionTier.label}", SlgColors.Steel)
            }
            if (state.nextTier != null) {
                Text(
                    "다음 Lv.${state.nextTier!!.level} · ${state.nextTier!!.label}",
                    color = SlgColors.Text.copy(alpha = 0.70f),
                    fontSize = SlgType.MICRO
                )
            }
        }

        // ── 우상단: ＋ － 내 구역 ──
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            MapControlButton("＋") { currentOnViewportChange(currentViewport.zoomIn()) }
            MapControlButton("－") { currentOnViewportChange(currentViewport.zoomOut()) }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SlgColors.SurfaceAlt.copy(alpha = 0.94f))
                    .border(1.dp, SlgColors.Brass.copy(alpha = 0.55f), RoundedCornerShape(50))
                    .clickable { currentOnViewportChange(currentViewport.reset()) }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text("⤾ 내 구역", color = SlgColors.Brass, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
            }
        }

        // ── 좌하단: 범례 ──
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC0C0E13))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "아군" to SlgColors.Steel,
                "중립" to SlgColors.Violet,
                "경계" to SlgColors.Danger,
                "잠김" to SlgColors.Line
            ).forEach { (label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(3.dp))
                    Text(label, color = SlgColors.Muted, fontSize = SlgType.MICRO)
                }
            }
        }

        // ── 우하단: 배율 · 범위 밖 ──
        val area = state.regionArea
        val outside = area?.let { TerritoryMapLayout.outOfRangeCount(it, state.unlockedHop) } ?: 0
        Text(
            "${"%.1f".format(viewport.scale)}× · 잠김 ${state.lockedNodeCount} · 밖 ${outside}곳",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC0C0E13))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun MapControlButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SlgColors.SurfaceAlt.copy(alpha = 0.94f))
            .border(1.dp, SlgColors.Line, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = SlgColors.Text, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
    }
}

/**
 * 지도 라벨 한 줄 — 마커를 다 그린 뒤, 겹치지 않는 자리에만 올린다.
 * priority 0 = 본진, 1 = 선택한 거점(둘은 무조건 보인다), 2 이상 = hop 순서, 100 이상 = 잠긴 거점.
 */
private class MapNodeLabel(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val name: String,
    val sub: String?,
    val priority: Int,
    val isLocked: Boolean,
    val subBaseline: Float
)

/**
 * 지도 렌더링 — 고리(해금 반경), 도로, 거점, 안개.
 */
private fun DrawScope.drawTerritoryMap(
    viewport: MapViewport,
    nodes: List<MapNode>,
    selectedCode: String,
    homeTroops: Int,
    unlockedHop: Int,
    enemyPressure: Boolean
) {
    val width = size.width
    val height = size.height
    val unit = viewport.unitPerWorld(width, height) ?: return

    fun sx(worldX: Float) = width / 2f + (worldX - viewport.centerX) * unit
    fun sy(worldY: Float) = height / 2f + (worldY - viewport.centerY) * unit

    // 배경 — 밤 지도 톤
    drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF12202B), Color(0xFF0A0E14))),
        size = size
    )

    // 좌표 격자
    val gridStep = 0.25f
    var gx = -TerritoryMapLayout.WORLD_HALF_EXTENT
    while (gx <= TerritoryMapLayout.WORLD_HALF_EXTENT) {
        val x = sx(gx)
        if (x in 0f..width) {
            drawLine(Color(0xFF1B2A38), Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
        }
        gx += gridStep
    }
    var gy = -TerritoryMapLayout.WORLD_HALF_EXTENT
    while (gy <= TerritoryMapLayout.WORLD_HALF_EXTENT) {
        val y = sy(gy)
        if (y in 0f..height) {
            drawLine(Color(0xFF1B2A38), Offset(0f, y), Offset(width, y), strokeWidth = 1f)
        }
        gy += gridStep
    }

    // 장식용 하천
    val river = TerritoryMapLayout.RIVER
    for (index in 0 until river.size - 1) {
        drawLine(
            color = Color(0xFF24435E),
            start = Offset(sx(river[index].first), sy(river[index].second)),
            end = Offset(sx(river[index + 1].first), sy(river[index + 1].second)),
            strokeWidth = 9f,
            cap = StrokeCap.Round
        )
    }
    for (index in 0 until river.size - 1) {
        drawLine(
            color = Color(0xFF5D93B8).copy(alpha = 0.65f),
            start = Offset(sx(river[index].first), sy(river[index].second)),
            end = Offset(sx(river[index + 1].first), sy(river[index + 1].second)),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }

    val homeX = sx(0f)
    val homeY = sy(0f)

    // 해금 반경 고리 — 지휘 단계가 지도에 그대로 보인다
    for (hop in 0..unlockedHop) {
        val radius = TerritoryMapLayout.ringRadius(hop)
        if (radius <= 0f) continue
        drawCircle(
            color = SlgColors.Brass.copy(alpha = if (hop == unlockedHop) 0.35f else 0.16f),
            radius = radius * unit,
            center = Offset(homeX, homeY),
            style = Stroke(
                width = if (hop == unlockedHop) 1.8f else 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f)
            )
        )
    }

    // 본진 → 해금 거점 간선 (도로·보급로)
    nodes.filter { !it.isHome && it.unlocked }.forEach { node ->
        drawLine(
            color = SlgColors.Brass.copy(alpha = 0.24f),
            start = Offset(homeX, homeY),
            end = Offset(sx(node.worldX), sy(node.worldY)),
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
    }
    // 잠긴 거점은 끊긴 점선
    nodes.filter { !it.isHome && it.isLocked }.forEach { node ->
        drawLine(
            color = SlgColors.Line.copy(alpha = 0.7f),
            start = Offset(homeX, homeY),
            end = Offset(sx(node.worldX), sy(node.worldY)),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
        )
    }

    // 위협 표시 — 미해결 정찰조 사건
    if (enemyPressure) {
        val enemy = nodes.firstOrNull { it.hopDistance == 1 && it.unlocked }
        if (enemy != null) {
            drawLine(
                color = SlgColors.Danger.copy(alpha = 0.75f),
                start = Offset(sx(enemy.worldX), sy(enemy.worldY)),
                end = Offset(homeX, homeY),
                strokeWidth = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f), 0f)
            )
        }
    }

    val namePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val subPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT
    }

    // ── 노드·라벨 크기 ──
    // 2026-11-21 시인성 개선: px 고정값이라 실기기에서 점처럼 보이던 것을 dp 기준으로 키웠다.
    // 배율이 커지면 조금씩 더 키우되(√배율), 최소·최대는 묶어 둔다.
    val zoomFactor = TerritoryMapLayout.nodeZoomFactor(viewport.scale)
    val nodeRadius = TerritoryMapLayout.NODE_RADIUS_DP.dp.toPx() * zoomFactor
    val nameTextPx = TerritoryMapLayout.NODE_LABEL_DP.dp.toPx() * zoomFactor
    val subTextPx = TerritoryMapLayout.NODE_SUB_LABEL_DP.dp.toPx() * zoomFactor
    val labelGap = 5.dp.toPx() * zoomFactor
    val barHeight = 3.6.dp.toPx() * zoomFactor

    // 라벨이 겹지지 않게 — 먼저 마커를 다 그리고, 그 다음 빈 자리에만 글자를 올린다.
    val labelSlots = mutableListOf<android.graphics.RectF>()
    val labelItems = mutableListOf<MapNodeLabel>()
    fun placeLabel(cx: Float, baseline: Float, text: String, paint: android.graphics.Paint, force: Boolean): Boolean {
        val half = paint.measureText(text) / 2f
        val top = baseline - paint.textSize * 0.92f
        val bottom = baseline + paint.textSize * 0.22f
        val rect = android.graphics.RectF(cx - half - 3f, top, cx + half + 3f, bottom)
        val clash = labelSlots.any { android.graphics.RectF.intersects(it, rect) }
        if (clash && !force) return false
        labelSlots += rect
        drawContext.canvas.nativeCanvas.drawText(text, cx, baseline, paint)
        return true
    }

    // 축소해서 라벨이 들어갈 자리가 없을 때는 병력 줄을 먼저 생략한다
    val showSubLabel = viewport.scale >= 0.8f

    // ── 1단계: 거점 마커 ──
    val visible = viewport.visibleNodes(nodes)
    visible.forEach { node ->
        val cx = sx(node.worldX)
        val cy = sy(node.worldY)
        val selected = node.code == selectedCode
        val relation = relationColor(node.relationLabel)
        val radius = nodeRadius

        // 마커 자리는 글자를 못 올리는 장애물로 등록한다
        labelSlots += android.graphics.RectF(
            cx - radius - 1f, cy - radius - 1f, cx + radius + 1f, cy + radius + 1f
        )

        val terrainMark = when (node.terrain) {
            TerrainType.MOUNTAIN -> "▲"
            TerrainType.RIVER -> "≈"
            TerrainType.COASTAL -> "〜"
            else -> "·"
        }
        val troops = if (node.isHome) homeTroops else node.troops

        if (node.isLocked) {
            // 안개 — 어두운 원 + 점선 테두리 + 자물쇠
            drawCircle(Color(0xFF10151C).copy(alpha = 0.92f), radius = radius, center = Offset(cx, cy))
            drawCircle(
                color = SlgColors.Line.copy(alpha = 0.75f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f))
            )
            labelItems += MapNodeLabel(
                cx = cx, cy = cy, radius = radius, name = "🔒 ${node.name} $terrainMark",
                sub = if (showSubLabel) "Lv.${tierLevelForHop(node.hopDistance)}" else null,
                priority = 100 + node.hopDistance, isLocked = true,
                subBaseline = cy + radius + labelGap + subTextPx
            )
            return@forEach
        }

        if (selected) {
            drawCircle(SlgColors.Brass.copy(alpha = 0.26f), radius = radius + 9.dp.toPx(), center = Offset(cx, cy))
        }
        if (node.isHome) {
            drawCircle(SlgColors.Brass.copy(alpha = 0.20f), radius = radius + 6.dp.toPx(), center = Offset(cx, cy))
        }
        drawCircle(relation.copy(alpha = 0.92f), radius = radius, center = Offset(cx, cy))
        drawCircle(Color(0xFF10151C), radius = radius * 0.80f, center = Offset(cx, cy))
        drawCircle(
            color = if (node.isHome) SlgColors.Brass else relation,
            radius = radius * 0.55f,
            center = Offset(cx, cy)
        )

        // 본진은 지휘통제소 아이콘(건물 + 안테나)
        if (node.isHome) {
            val s = radius * 0.55f
            drawRect(
                color = Color(0xFF10151C),
                topLeft = Offset(cx - s * 0.95f, cy - s * 0.15f),
                size = Size(s * 1.9f, s * 0.9f)
            )
            drawLine(
                color = Color(0xFF10151C),
                start = Offset(cx + s * 0.7f, cy - s * 0.1f),
                end = Offset(cx + s * 0.7f, cy - s * 1.15f),
                strokeWidth = 1.8.dp.toPx()
            )
            drawCircle(Color(0xFF10151C), radius = s * 0.22f, center = Offset(cx + s * 0.7f, cy - s * 1.2f))
        }
        if (selected) {
            drawCircle(
                color = SlgColors.Brass,
                radius = radius + 3.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 2.4.dp.toPx())
            )
        }

        // 병력 두께 — 노드 아래 미니 바
        val troopsRatio = (troops / 400f).coerceIn(0.08f, 1f)
        val barWidth = radius * 2.1f
        val barTop = cy + radius + labelGap * 0.6f
        drawRect(
            color = Color(0xCC0C0E13),
            topLeft = Offset(cx - barWidth / 2f, barTop),
            size = Size(barWidth, barHeight)
        )
        drawRect(
            color = relation,
            topLeft = Offset(cx - barWidth / 2f, barTop),
            size = Size(barWidth * troopsRatio, barHeight)
        )
        labelSlots += android.graphics.RectF(
            cx - barWidth / 2f, barTop, cx + barWidth / 2f, barTop + barHeight
        )
        labelItems += MapNodeLabel(
            cx = cx, cy = cy, radius = radius, name = "${node.name} $terrainMark",
            sub = if (showSubLabel) "⚔ $troops" else null,
            priority = if (node.isHome) 0 else if (selected) 1 else 2 + node.hopDistance,
            isLocked = false,
            subBaseline = barTop + barHeight + subTextPx
        )
    }

    // ── 2단계: 라벨 — 본진·선택·가까운 거점부터 빈 자리에 올린다 ──
    namePaint.textSize = nameTextPx
    subPaint.textSize = subTextPx
    labelItems.sortedBy { it.priority }.forEach { item ->
        val force = item.priority <= 1
        if (item.isLocked && viewport.scale < 0.75f) return@forEach
        namePaint.color = when {
            item.priority == 1 -> android.graphics.Color.WHITE
            item.isLocked -> android.graphics.Color.rgb(132, 141, 156)
            else -> android.graphics.Color.rgb(240, 237, 231)
        }
        val nameBaseline = item.cy - item.radius - labelGap
        // 이름이 다른 마커와 겹치면 병력 줄은 끼워 넣지 않는다
        val namePlaced = placeLabel(item.cx, nameBaseline, item.name, namePaint, force)
        val sub = item.sub
        if (sub != null && (namePlaced || force)) {
            subPaint.color = if (item.isLocked) {
                android.graphics.Color.rgb(112, 121, 136)
            } else {
                android.graphics.Color.rgb(168, 177, 192)
            }
            placeLabel(item.cx, item.subBaseline, sub, subPaint, force)
        }
    }

    // 가장자리 안개
    val fog = SlgColors.Background
    val fogWidth = height * 0.14f
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

/** hop 거리가 열리는 최소 지휘 레벨 */
private fun tierLevelForHop(hop: Int): Int =
    com.game.slg.core.region.ExpansionTier.entries.firstOrNull { it.maxHop >= hop }?.level ?: 8

/** 지도 거점 이름 목록 (테스트·디버그) */
fun territoryNames(state: SlgGameState): List<String> = state.mapNodes.map { it.name }

/** 현재 선택 지역의 인접 거점 이름 */
fun neighborNames(state: SlgGameState): List<String> =
    state.mapNodes.filter { it.hopDistance == 1 }.map { it.name }

/** 데이터에 포함된 전체 시작 지역 수 */
fun regionCount(): Int = RegionCatalog.regionCount()
