package com.game.slg.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.ui.SlgColors.Brass
import com.game.slg.ui.SlgColors.Danger
import com.game.slg.ui.SlgColors.Steel
import com.game.slg.ui.SlgColors.Success
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * SLG 화면용 Compose Canvas 그래픽 모음.
 *
 * 텍스트로 늘어놓던 지표·진행도·전장을 그림으로 바꾼다.
 * 모든 컴포저블은 안드로이드 뷰 없이 Canvas 만 쓰므로 APK 용량이 늘지 않는다.
 */

// ============================================================================
// 1. 레이더 차트 — 내정·문화·군사 지표 5각형
// ============================================================================

data class RadarStat(val label: String, val value: Int, val max: Int = 100, val color: Color = Brass)

@Composable
fun SlgRadarChart(
    stats: List<RadarStat>,
    modifier: Modifier = Modifier,
    accent: Color = Brass
) {
    Canvas(modifier = modifier) {
        if (stats.size < 3) return@Canvas
        val n = stats.size
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = min(cx, cy) - 22f

        fun point(index: Int, r: Float): Offset {
            val angle = (-90f + 360f / n * index) * PI.toFloat() / 180f
            return Offset(cx + r * cos(angle), cy + r * sin(angle))
        }

        // 배경 링 4겹
        for (ring in 1..4) {
            val path = Path()
            for (i in 0 until n) {
                val p = point(i, radius * ring / 4f)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, color = SlgColors.Line.copy(alpha = 0.75f), style = Stroke(width = 1.2f))
        }
        // 축
        for (i in 0 until n) {
            val p = point(i, radius)
            drawLine(SlgColors.Line.copy(alpha = 0.7f), Offset(cx, cy), p, strokeWidth = 1f)
        }
        // 값 다각형
        val valuePath = Path()
        for (i in 0 until n) {
            val ratio = (stats[i].value.toFloat() / stats[i].max.coerceAtLeast(1)).coerceIn(0.04f, 1f)
            val p = point(i, radius * ratio)
            if (i == 0) valuePath.moveTo(p.x, p.y) else valuePath.lineTo(p.x, p.y)
        }
        valuePath.close()
        drawPath(valuePath, color = accent.copy(alpha = 0.24f))
        drawPath(valuePath, color = accent, style = Stroke(width = 2.4f))
        for (i in 0 until n) {
            val ratio = (stats[i].value.toFloat() / stats[i].max.coerceAtLeast(1)).coerceIn(0.04f, 1f)
            val p = point(i, radius * ratio)
            drawCircle(stats[i].color, radius = 3.4f, center = p)
        }

        // 라벨 (2글자 이내) + 수치
        val labelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textSize = 10.sp.toPx()
            color = android.graphics.Color.rgb(237, 234, 227)
        }
        stats.forEachIndexed { index, stat ->
            val p = point(index, radius + 14f)
            drawContext.canvas.nativeCanvas.drawText(stat.label, p.x, p.y + 3f, labelPaint)
        }
    }
}

// ============================================================================
// 2. 도넛 게이지 — 의심도·사상자율·투표율
// ============================================================================

@Composable
fun SlgDonutGauge(
    ratio: Float,
    valueText: String,
    caption: String,
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Float = 11f
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = thickness
            val inset = stroke / 2f + 1f
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(
                color = Color(0xFF262D3A),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * ratio.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valueText, color = color, fontSize = SlgType.TITLE, fontWeight = FontWeight.ExtraBold)
            Text(caption, color = SlgColors.Muted, fontSize = SlgType.MICRO, maxLines = 1)
        }
    }
}

// ============================================================================
// 3. 병력 대비 막대 — 아군(좌) vs 적(우)
// ============================================================================

@Composable
fun SlgForceBar(own: Int, enemy: Int, modifier: Modifier = Modifier) {
    val max = maxOf(own, enemy, 1)
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좌: 아군 — 오른쪽 끝이 중앙
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            SlgStatBar(ratio = own.toFloat() / max, color = Steel, height = 9)
        }
        Box(
            modifier = Modifier
                .background(SlgColors.Line)
                .size(width = 2.dp, height = 13.dp)
        )
        // 우: 적군 — 왼쪽 끝이 중앙
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            SlgStatBar(ratio = enemy.toFloat() / max, color = Danger, height = 9)
        }
    }
}

// ============================================================================
// 4. 테크트리 세로 계통도
// ============================================================================

@Composable
fun SlgTechBranchCanvas(
    stageNames: List<String>,
    researched: List<Boolean>,
    availableStage: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (stageNames.isEmpty()) return@Canvas
        val count = stageNames.size
        val left = 26f
        val top = 18f
        val step = (size.height - top * 2) / (count - 1).coerceAtLeast(1)
        val centerX = left
        val namePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.LEFT
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textSize = 11.sp.toPx()
        }
        for (index in 0 until count) {
            val y = top + step * index
            if (index > 0) {
                val prevY = top + step * (index - 1)
                val done = researched.getOrElse(index - 1) { false }
                drawLine(
                    color = if (done) accent else SlgColors.Line,
                    start = Offset(centerX, prevY),
                    end = Offset(centerX, y),
                    strokeWidth = if (done) 4f else 2f,
                    cap = StrokeCap.Round
                )
            }
            val done = researched.getOrElse(index) { false }
            val isAvailable = !done && index <= availableStage
            drawCircle(
                color = when {
                    done -> accent
                    isAvailable -> accent.copy(alpha = 0.24f)
                    else -> SlgColors.SurfaceAlt
                },
                radius = 11f,
                center = Offset(centerX, y)
            )
            drawCircle(
                color = when {
                    done -> accent
                    isAvailable -> accent
                    else -> SlgColors.Line
                },
                radius = 11f,
                center = Offset(centerX, y),
                style = Stroke(width = 2f)
            )
            if (done) {
                drawLine(
                    color = SlgColors.Background,
                    start = Offset(centerX - 4f, y),
                    end = Offset(centerX - 1f, y + 3.5f),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = SlgColors.Background,
                    start = Offset(centerX - 1f, y + 3.5f),
                    end = Offset(centerX + 4.5f, y - 3.5f),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }
            namePaint.color = when {
                done -> android.graphics.Color.rgb(226, 183, 85)
                isAvailable -> android.graphics.Color.rgb(237, 234, 227)
                else -> android.graphics.Color.rgb(152, 161, 176)
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${index + 1}. ${stageNames[index]}",
                centerX + 20f,
                y + 4f,
                namePaint
            )
        }
    }
}

// ============================================================================
// 5. 승격 로드맵 트랙
// ============================================================================

@Composable
fun SlgRoadmapTrack(
    stages: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (stages.isEmpty()) return@Canvas
        val count = stages.size
        val y = size.height * 0.42f
        val step = size.width / count
        val labelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textSize = 10.sp.toPx()
        }
        // 진행선
        val progressX = step * (currentIndex + 0.5f)
        drawLine(
            color = SlgColors.Line,
            start = Offset(step * 0.5f, y),
            end = Offset(size.width - step * 0.5f, y),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Brass,
            start = Offset(step * 0.5f, y),
            end = Offset(progressX.coerceAtLeast(step * 0.5f), y),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        stages.forEachIndexed { index, name ->
            val cx = step * (index + 0.5f)
            val achieved = index <= currentIndex
            val isCurrent = index == currentIndex
            if (isCurrent) {
                drawCircle(Brass.copy(alpha = 0.22f), radius = 16f, center = Offset(cx, y))
            }
            drawCircle(
                color = if (achieved) Brass else SlgColors.Surface,
                radius = 9f,
                center = Offset(cx, y)
            )
            drawCircle(
                color = if (achieved) Brass else SlgColors.Line,
                radius = 9f,
                center = Offset(cx, y),
                style = Stroke(width = 2f)
            )
            labelPaint.color = if (achieved) {
                android.graphics.Color.rgb(226, 183, 85)
            } else {
                android.graphics.Color.rgb(152, 161, 176)
            }
            drawContext.canvas.nativeCanvas.drawText(name, cx, y + 27f, labelPaint)
        }
    }
}

// ============================================================================
// 6. 작전 지형도 — 산악 매복 · 하천 도하 · 요새 수성
// ============================================================================

enum class OperationKind { MOUNTAIN, RIVER, FORTRESS }

@Composable
fun SlgOperationScene(kind: OperationKind, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // 밤 하늘
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF16202C), Color(0xFF0B0F16))),
            size = size
        )
        drawCircle(Color(0xFFD9D3C0).copy(alpha = 0.55f), radius = h * 0.09f, center = Offset(w * 0.82f, h * 0.22f))

        // 지형 실루엣
        val ridge = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.72f)
            lineTo(w * 0.16f, h * 0.50f)
            lineTo(w * 0.30f, h * 0.66f)
            lineTo(w * 0.46f, h * 0.40f)
            lineTo(w * 0.62f, h * 0.66f)
            lineTo(w * 0.80f, h * 0.52f)
            lineTo(w, h * 0.70f)
            lineTo(w, h)
            close()
        }
        drawPath(ridge, color = Color(0xFF1B2734))

        when (kind) {
            OperationKind.MOUNTAIN -> {
                // 협곡(통로) 밝은 길
                drawPath(
                    Path().apply {
                        moveTo(w * 0.30f, h)
                        lineTo(w * 0.44f, h * 0.42f)
                        lineTo(w * 0.54f, h * 0.42f)
                        lineTo(w * 0.66f, h)
                        close()
                    },
                    color = Color(0xFF2A3746)
                )
                // 매복 병력 (산 위 삼각형 무리)
                repeat(5) { i ->
                    val bx = w * (0.32f + i * 0.045f)
                    val by = h * 0.44f - i * 3f
                    drawPath(
                        Path().apply {
                            moveTo(bx, by)
                            lineTo(bx + 9f, by + 13f)
                            lineTo(bx - 9f, by + 13f)
                            close()
                        },
                        color = Steel
                    )
                }
                // 적 진입 화살표
                drawArrow(Offset(w * 0.72f, h * 0.86f), Offset(w * 0.50f, h * 0.56f), Danger)
            }

            OperationKind.RIVER -> {
                // 하천
                drawRect(
                    color = SlgColors.Water.copy(alpha = 0.55f),
                    topLeft = Offset(0f, h * 0.60f),
                    size = Size(w, h * 0.16f)
                )
                repeat(6) { i ->
                    val ry = h * 0.63f + (i % 3) * h * 0.04f
                    drawLine(
                        color = Color(0xFF7FB0D0).copy(alpha = 0.5f),
                        start = Offset(w * 0.08f + i * w * 0.16f, ry),
                        end = Offset(w * 0.18f + i * w * 0.16f, ry),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
                // 도하 지점 표시
                drawLine(Steel, Offset(w * 0.34f, h * 0.58f), Offset(w * 0.52f, h * 0.78f), strokeWidth = 4f, cap = StrokeCap.Round)
                drawArrow(Offset(w * 0.30f, h * 0.42f), Offset(w * 0.48f, h * 0.70f), Danger)
                // 요격 병력
                repeat(4) { i ->
                    drawCircle(Steel, radius = 5f, center = Offset(w * (0.60f + i * 0.05f), h * 0.66f))
                }
            }

            OperationKind.FORTRESS -> {
                // 성벽 + 망루
                val wallTop = h * 0.52f
                drawRect(
                    color = Color(0xFF2E3542),
                    topLeft = Offset(w * 0.10f, wallTop),
                    size = Size(w * 0.80f, h * 0.30f)
                )
                repeat(14) { i ->
                    drawRect(
                        color = Color(0xFF3A4350),
                        topLeft = Offset(w * 0.10f + i * w * 0.058f, wallTop - 7f),
                        size = Size(w * 0.030f, 7f)
                    )
                }
                listOf(0.12f, 0.46f, 0.78f).forEach { fx ->
                    drawRect(
                        color = Color(0xFF414B5A),
                        topLeft = Offset(w * fx, wallTop - h * 0.16f),
                        size = Size(w * 0.10f, h * 0.16f)
                    )
                    drawPath(
                        Path().apply {
                            moveTo(w * fx - 4f, wallTop - h * 0.16f)
                            lineTo(w * (fx + 0.05f), wallTop - h * 0.24f)
                            lineTo(w * (fx + 0.10f) + 4f, wallTop - h * 0.16f)
                            close()
                        },
                        color = Brass.copy(alpha = 0.75f)
                    )
                }
                // 성문
                drawRect(
                    color = Color(0xFF151A22),
                    topLeft = Offset(w * 0.435f, wallTop + h * 0.10f),
                    size = Size(w * 0.13f, h * 0.20f)
                )
                drawArrow(Offset(w * 0.62f, h * 0.92f), Offset(w * 0.50f, h * 0.72f), Danger)
            }
        }
    }
}

private fun DrawScope.drawArrow(from: Offset, to: Offset, color: Color) {
    drawLine(color, from, to, strokeWidth = 3f, cap = StrokeCap.Round)
    val dx = to.x - from.x
    val dy = to.y - from.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
    val ux = dx / len
    val uy = dy / len
    val head = 10f
    drawPath(
        Path().apply {
            moveTo(to.x, to.y)
            lineTo(to.x - ux * head + uy * head * 0.55f, to.y - uy * head - ux * head * 0.55f)
            lineTo(to.x - ux * head - uy * head * 0.55f, to.y - uy * head + ux * head * 0.55f)
            close()
        },
        color = color
    )
}

// ============================================================================
// 7. 절기 바퀴 — 봄·여름·가을·겨울
// ============================================================================

@Composable
fun SlgSeasonWheel(currentIndex: Int, modifier: Modifier = Modifier) {
    val labels = listOf("봄", "여름", "가을", "겨울")
    val icons = listOf("🌱", "☀", "🌾", "❄")
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2f - 12f
            val thick = 16f
            for (i in 0 until 4) {
                val selected = i == currentIndex
                drawArc(
                    color = if (selected) Brass else SlgColors.SurfaceAlt,
                    startAngle = -90f + 90f * i + 3f,
                    sweepAngle = 84f,
                    useCenter = false,
                    topLeft = Offset(
                        size.width / 2f - radius + thick / 2f,
                        size.height / 2f - radius + thick / 2f
                    ),
                    size = Size(radius * 2 - thick, radius * 2 - thick),
                    style = Stroke(width = thick, cap = StrokeCap.Butt)
                )
            }
        }
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icons[currentIndex], fontSize = 22.sp)
            Text(labels[currentIndex], color = Brass, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
            Text("절기", color = SlgColors.Muted, fontSize = SlgType.MICRO)
        }
    }
}

// ============================================================================
// 8. 투표 스플릿 — 찬성(우) vs 반대(좌)
// ============================================================================

@Composable
fun SlgVoteSplit(favor: Int, against: Int, modifier: Modifier = Modifier) {
    val total = (favor + against).coerceAtLeast(1)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SlgColors.SurfaceAlt)
    ) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(against.toFloat().coerceAtLeast(0.6f))
                    .fillMaxSize()
                    .background(Danger.copy(alpha = 0.75f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("반대 $against", color = SlgColors.Text, fontSize = SlgType.MICRO, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .weight(favor.toFloat().coerceAtLeast(0.6f))
                    .fillMaxSize()
                    .background(Success.copy(alpha = 0.75f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("찬성 $favor", color = SlgColors.Text, fontSize = SlgType.MICRO, maxLines = 1)
            }
        }
        // 반대 65% = 연합 반란선
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * 0.65f
            drawLine(
                color = Brass,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2.5f
            )
        }
    }
}
