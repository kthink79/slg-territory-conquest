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
import kotlin.math.sqrt

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
            .size(26.dp)
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

    // 거점 노드
    val visible = viewport.visibleNodes(nodes)
    visible.forEach { node ->
        val cx = sx(node.worldX)
        val cy = sy(node.worldY)
        val selected = node.code == selectedCode
        val relation = relationColor(node.relationLabel)
        val radius = (15f * sqrt(viewport.scale)).coerceIn(11f, 24f)

        if (node.isLocked) {
            // 안개 — 어두운 원 + 점선 테두리 + 자물쇠
            drawCircle(Color(0xFF10151C).copy(alpha = 0.92f), radius = radius, center = Offset(cx, cy))
            drawCircle(
                color = SlgColors.Line.copy(alpha = 0.75f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f))
            )
            namePaint.textSize = (11.5f * sqrt(viewport.scale)).coerceIn(10f, 15f)
            namePaint.color = android.graphics.Color.rgb(120, 129, 143)
            drawContext.canvas.nativeCanvas.drawText("🔒 ${node.name}", cx, cy - radius - 6f, namePaint)
            subPaint.textSize = (9.5f * sqrt(viewport.scale)).coerceIn(8.5f, 12f)
            subPaint.color = android.graphics.Color.rgb(105, 114, 128)
            drawContext.canvas.nativeCanvas.drawText("Lv.${tierLevelForHop(node.hopDistance)}", cx, cy + radius + 16f, subPaint)
            return@forEach
        }

        if (selected) {
            drawCircle(SlgColors.Brass.copy(alpha = 0.26f), radius = radius + 9f, center = Offset(cx, cy))
        }
        if (node.isHome) {
            drawCircle(SlgColors.Brass.copy(alpha = 0.20f), radius = radius + 6f, center = Offset(cx, cy))
        }
        drawCircle(relation.copy(alpha = 0.92f), radius = radius, center = Offset(cx, cy))
        drawCircle(Color(0xFF10151C), radius = radius - 3f, center = Offset(cx, cy))
        drawCircle(
            color = if (node.isHome) SlgColors.Brass else relation,
            radius = radius - 6.5f,
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
                strokeWidth = 1.8f
            )
            drawCircle(Color(0xFF10151C), radius = s * 0.22f, center = Offset(cx + s * 0.7f, cy - s * 1.2f))
        }
        if (selected) {
            drawCircle(
                color = SlgColors.Brass,
                radius = radius + 3f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.2f)
            )
        }

        // 병력 두께 — 노드 아래 미니 바
        val troops = if (node.isHome) homeTroops else node.troops
        val troopsRatio = (troops / 400f).coerceIn(0.08f, 1f)
        val barWidth = radius * 2.2f
        drawRect(
            color = Color(0xCC0C0E13),
            topLeft = Offset(cx - barWidth / 2f, cy + radius + 3f),
            size = Size(barWidth, 4f)
        )
        drawRect(
            color = relation,
            topLeft = Offset(cx - barWidth / 2f, cy + radius + 3f),
            size = Size(barWidth * troopsRatio, 4f)
        )

        // 이름 + 지형 표식
        val terrainMark = when (node.terrain) {
            TerrainType.MOUNTAIN -> "▲"
            TerrainType.RIVER -> "≈"
            TerrainType.COASTAL -> "〜"
            else -> "·"
        }
        namePaint.textSize = (11.5f * sqrt(viewport.scale)).coerceIn(10f, 15f)
        namePaint.color = if (selected) android.graphics.Color.WHITE else android.graphics.Color.rgb(237, 234, 227)
        drawContext.canvas.nativeCanvas.drawText(
            "${node.name} $terrainMark",
            cx,
            cy - radius - 6f,
            namePaint
        )
        subPaint.textSize = (9.5f * sqrt(viewport.scale)).coerceIn(8.5f, 12f)
        subPaint.color = android.graphics.Color.rgb(152, 161, 176)
        drawContext.canvas.nativeCanvas.drawText("⚔ $troops", cx, cy + radius + 18f, subPaint)
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
