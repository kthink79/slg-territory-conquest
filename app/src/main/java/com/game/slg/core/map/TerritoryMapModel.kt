package com.game.slg.core.map

import com.game.slg.core.model.TerrainType
import com.game.slg.core.region.RegionArea
import com.game.slg.core.region.RegionCatalog
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 메인 화면 지도 노드 — 시작 지역에서 hop 거리로 배치되는 읍·면·동 거점.
 */
data class MapNode(
    val code: String,
    val name: String,
    val region: String,
    val worldX: Float,
    val worldY: Float,
    /** 시작 지역에서 떨어진 다리 수 (0 = 내 구역) */
    val hopDistance: Int,
    val terrain: TerrainType,
    val relationLabel: String,
    val owner: String,
    val population: Int,
    val troops: Int,
    val note: String,
    val isHome: Boolean = false,
    /** 지휘 레벨로 작전 범위가 열렸는지 */
    val unlocked: Boolean = true,
    /** 이 지역의 리더 정원 (인구 / 1,000) */
    val lordCapacity: Int = 0
) {
    /** 지도에서 잠긴 거점은 흐리게 그린다 */
    val isLocked: Boolean get() = !unlocked
}

/** 화면 좌표(px) */
data class ScreenPoint(val x: Float, val y: Float)

/** 세계 좌표 경계 */
data class WorldBounds(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    fun contains(x: Float, y: Float): Boolean = x in minX..maxX && y in minY..maxY
}

/**
 * 지도 배치 계산 — 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 *
 * 시작 지역(0,0)을 중심으로 hop 1은 바로 옆 고리, hop 2는 그 바깥 고리에 놓는다.
 * 잠긴 거점도 한 단계 미리 보여 주어(안개) 다음에 무엇이 열리는지 알 수 있게 한다.
 *
 * 2026-11-21 사용자 실기 피드백 반영:
 *  · 고리 반경(= 영지 사이 거리)을 이전보다 30% 줄여 가까운 영지들이 한 화면에 더 밀집한다.
 *  · 확대/축소 범위를 0.4× ~ 4.0× 로 넓히고 기본 배율을 1.15× 로 높여 처음부터 지도가 크게 보인다.
 */
object TerritoryMapLayout {

    /** 세계 좌표 최대 반경 (이 밖으로는 화면이 나가지 않는다) */
    const val WORLD_HALF_EXTENT = 1.12f

    /** 확대/축소 한계 — 0.4배 ~ 4.0배 */
    const val MIN_SCALE = 0.4f
    const val MAX_SCALE = 4.0f
    const val DEFAULT_SCALE = 1.15f

    /** +/- 버튼 한 번에 바뀌는 배율 — 크게 움직여 몇 번에 끝난다 */
    const val ZOOM_STEP = 1.45f

    /** 배율 1.0에서 화면 절반이 덮는 세계 거리 */
    const val BASE_VIEW_RADIUS = 0.95f

    /** 영향권 경계 점선 반경 (hop 1 고리) */
    const val INFLUENCE_RADIUS = 0.36f

    /** 영지 노드 반지름(dp) — 기본 배율 1.15×에서 13dp(직경 26dp) */
    const val NODE_RADIUS_DP = 13f

    /** 영지 이름 라벨 글씨(dp) */
    const val NODE_LABEL_DP = 13f

    /** 병력·잠김 라벨 글씨(dp) */
    const val NODE_SUB_LABEL_DP = 11.5f

    /**
     * 배율에 따른 노드·라벨 확대 계수 — 기본 배율에서 1.0, 작아져도 0.72 아래로는 내려가지 않는다.
     * 실기기에서 노드가 점처럼 보이던 문제를 막는 하한이다.
     */
    fun nodeZoomFactor(scale: Float): Float =
        sqrt(scale / DEFAULT_SCALE).coerceIn(0.72f, 1.95f)

    /** 배율에 따른 노드 반지름(dp) */
    fun nodeRadiusDp(scale: Float): Float = NODE_RADIUS_DP * nodeZoomFactor(scale)

    /** 장식용 하천 폴리라인 — 실제 지형과 무관한 지도 질감 */
    val RIVER: List<Pair<Float, Float>> = listOf(
        -1.25f to -0.10f,
        -0.62f to 0.06f,
        -0.18f to 0.12f,
        0.22f to 0.30f,
        0.70f to 0.44f,
        1.25f to 0.58f
    )

    /**
     * hop → 고리 반경 (2026-11-21 이전 값의 70% — 영지 간 간격 30% 압축).
     *
     * 2026-11-27(v0.11.0): 통치 지역이 넓어지면 지도가 hop 6~8까지 그려지므로
     * 5고리부터는 0.14씩 넓혀 겹치지 않게 한다. 기존 1~4고리 값은 그대로 둔다.
     */
    fun ringRadius(hop: Int): Float = when (hop) {
        0 -> 0f
        1 -> 0.36f
        2 -> 0.56f
        3 -> 0.73f
        4 -> 0.88f
        5 -> 1.00f
        6 -> 1.09f
        else -> 1.02f + (hop - 5) * 0.14f
    }

    /** 지도에 미리 보여 줄 수 있는 가장 바깥 고리 — 통치 범위가 넓어지면 여기까지 그린다 */
    const val MAX_PREVIEW_HOP = 8

    /**
     * 이번 해금 범위에서 안개로 미리 보여 줄 바깥 고리.
     * 해금 hop + 1(다음 단계 미리보기)을 상한 [MAX_PREVIEW_HOP] 까지 연다.
     */
    fun previewHopOf(unlockedHop: Int): Int =
        (unlockedHop.coerceAtLeast(0) + 1).coerceAtMost(MAX_PREVIEW_HOP)

    /**
     * 세계 좌표 경계 — 바깥 미리보기 고리까지 화면을 옮길 수 있게 넓힌다.
     * 기본값([WORLD_HALF_EXTENT])보다 좁아지지는 않는다.
     */
    fun worldHalfExtentFor(previewHop: Int): Float =
        (ringRadius(previewHop) + 0.10f).coerceAtLeast(WORLD_HALF_EXTENT)

    /**
     * 시작 지역 + 지휘 레벨(해금 hop)으로 지도 노드 생성.
     *
     * @param start 시작 지역
     * @param unlockedHop 지휘 레벨로 열린 최대 hop
     * @param playerName 플레이어 리더 이름 (본진 표기)
     * @param homeTroops 본진 병력
     */
    fun build(
        start: RegionArea,
        unlockedHop: Int,
        playerName: String = "플레이어",
        homeTroops: Int = 0
    ): List<MapNode> {
        val distances = RegionCatalog.hopDistances(start.code, limit = 30)
        val previewHop = previewHopOf(unlockedHop)
        val included = distances.filterValues { it <= previewHop }

        // hop 별로 모아 고리 위에 균등 배치
        val byHop = included.entries.groupBy({ it.value }, { RegionCatalog.area(it.key) })
        val nodes = mutableListOf<MapNode>()

        for (hop in 0..previewHop) {
            val areas = byHop[hop].orEmpty().filterNotNull()
                .sortedBy { it.code }
            if (areas.isEmpty()) continue
            val radius = ringRadius(hop)
            areas.forEachIndexed { index, area ->
                val angle = if (hop == 0) {
                    0.0
                } else {
                    (-90.0 + 360.0 / areas.size * index + hop * 17.0) * Math.PI / 180.0
                }
                val x = (radius * cos(angle)).toFloat()
                val y = (radius * sin(angle)).toFloat()
                val isHome = hop == 0
                val unlocked = hop <= unlockedHop
                nodes += MapNode(
                    code = area.code,
                    name = area.name,
                    region = area.groupKey,
                    worldX = if (isHome) 0f else x,
                    worldY = if (isHome) 0f else y,
                    hopDistance = hop,
                    terrain = area.terrain,
                    relationLabel = relationOf(hop, index, unlocked),
                    owner = when {
                        isHome -> "$playerName (인계)"
                        !unlocked -> "미탐지 세력"
                        hop == 1 -> listOf("물류 협의회", "경계 세력", "시민 자치회")[index % 3]
                        else -> "관찰 구역"
                    },
                    population = area.population,
                    troops = when {
                        isHome -> homeTroops
                        !unlocked -> 0
                        else -> (area.lordCapacity * 6).coerceAtMost(1200)
                    },
                    note = "리더 정원 ${area.lordCapacity}석 · 인구 ${area.population}명",
                    isHome = isHome,
                    unlocked = unlocked,
                    lordCapacity = area.lordCapacity
                )
            }
        }
        return nodes
    }

    /** 시작 지역 묶음 안에서 지도에 아직 그리지 않은 거점 수 */
    fun outOfRangeCount(start: RegionArea, unlockedHop: Int): Int {
        val distances = RegionCatalog.hopDistances(start.code, limit = 30)
        val previewHop = previewHopOf(unlockedHop)
        return distances.count { it.value > previewHop }
    }

    /** 잠긴 거점 수 (다음 해금 대상) */
    fun lockedCount(nodes: List<MapNode>): Int = nodes.count { it.isLocked }

    /** 해금된 거점 수 */
    fun unlockedCount(nodes: List<MapNode>): Int = nodes.count { it.unlocked }

    private fun relationOf(hop: Int, index: Int, unlocked: Boolean): String = when {
        hop == 0 -> "아군 거점"
        !unlocked -> "미개방"
        hop == 1 && index % 3 == 0 -> "중립 물류"
        hop == 1 && index % 3 == 1 -> "경계 세력"
        hop == 1 -> "시민 자치회"
        else -> "관찰 구역"
    }

    fun node(nodes: List<MapNode>, code: String): MapNode? = nodes.firstOrNull { it.code == code }

    fun home(nodes: List<MapNode>): MapNode? = nodes.firstOrNull { it.isHome }
}

/**
 * 통치 범위 자동 확장 — 지휘 레벨만이 아니라 실제로 운영하는 영지 수와
 * 편입 영지까지의 거리에 따라 작전 해금 범위가 저절로 넓어진다.
 *
 * 사용자 요청(2026-11-27, v0.11.0): *"통치 지역 확장 비례 맵 범위 자동 확대"*
 *
 * · 지휘 레벨이 여는 hop 은 기본값이다.
 * · 영지를 3곳 · 6곳 · 12곳 이상 편입하면 각각 +1 · +2 · +3 hop 이 더 열린다.
 * · 편입한 영지가 더 먼 고리에 있어도 그 거리까지는 열어 둔다(그 지역을 계속 운영해야 하므로).
 */
object MapExpansionEngine {

    /** 자동 확장을 포함한 최대 작전 반경 */
    const val MAX_EFFECTIVE_HOP = 7

    /** 통치 영지 수가 주는 hop 보너스 */
    fun territoryBonusHop(governedCount: Int): Int = when {
        governedCount >= 12 -> 3
        governedCount >= 6 -> 2
        governedCount >= 3 -> 1
        else -> 0
    }

    /** 지휘 레벨 · 영지 수 · 편입 거리 중 가장 넓은 값이 실제 작전 범위다 */
    fun effectiveUnlockedHop(
        commandHop: Int,
        governedCount: Int,
        farthestGovernedHop: Int
    ): Int = maxOf(
        commandHop.coerceAtLeast(0),
        commandHop.coerceAtLeast(0) + territoryBonusHop(governedCount),
        farthestGovernedHop.coerceAtLeast(0)
    ).coerceIn(0, MAX_EFFECTIVE_HOP)

    /** 편입 영지 코드들 중 시작 지역에서 가장 먼 hop */
    fun farthestHop(startCode: String, governedCodes: List<String>): Int {
        if (governedCodes.isEmpty()) return 0
        val distances = RegionCatalog.hopDistances(startCode, limit = MAX_EFFECTIVE_HOP)
        return governedCodes.mapNotNull { code ->
            distances[code.substringBefore(':')]
        }.maxOrNull() ?: 0
    }

    /** 화면 배지 한 줄 — 자동 확장이 붙었을 때만 보여 준다 */
    fun expansionLabel(commandHop: Int, effectiveHop: Int, governedCount: Int): String? {
        if (effectiveHop <= commandHop) return null
        val bonus = territoryBonusHop(governedCount)
        return if (bonus > 0) "통치 ${governedCount}곳 · 자동 +${effectiveHop - commandHop}hop"
        else "편입 거리 반영 · ${effectiveHop}hop"
    }
}

/**
 * 상단 지도의 확대/축소·이동 상태.
 */
data class MapViewport(
    val scale: Float = TerritoryMapLayout.DEFAULT_SCALE,
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    /**
     * 화면을 옮길 수 있는 세계 반경 — 통치 지역이 넓어져 바깥 고리가 생기면 넓힌다.
     * 기본값은 지도 기본 경계라 예전 동작과 같다.
     */
    val worldHalfExtent: Float = TerritoryMapLayout.WORLD_HALF_EXTENT
) {

    /** 화면 절반이 덮는 세계 거리 — 배율이 클수록 작아진다 */
    val viewHalfExtent: Float get() = TerritoryMapLayout.BASE_VIEW_RADIUS / scale

    fun visibleBounds(): WorldBounds {
        val half = viewHalfExtent
        return WorldBounds(
            minX = centerX - half,
            maxX = centerX + half,
            minY = centerY - half,
            maxY = centerY + half
        )
    }

    fun zoomedBy(factor: Float): MapViewport {
        if (factor <= 0f || factor.isNaN() || factor.isInfinite()) return this
        val next = (scale * factor).coerceIn(TerritoryMapLayout.MIN_SCALE, TerritoryMapLayout.MAX_SCALE)
        if (next == scale) return this
        return copy(scale = next).clampedToWorld()
    }

    fun zoomIn(step: Float = TerritoryMapLayout.ZOOM_STEP): MapViewport = zoomedBy(step)

    fun zoomOut(step: Float = TerritoryMapLayout.ZOOM_STEP): MapViewport = zoomedBy(1f / step)

    fun pannedBy(dxWorld: Float, dyWorld: Float): MapViewport =
        copy(centerX = centerX + dxWorld, centerY = centerY + dyWorld).clampedToWorld()

    /** 내 구역 중심(초기 배율·본진 중앙)으로 복귀 — 넓어진 경계는 유지한다 */
    fun reset(): MapViewport = MapViewport(worldHalfExtent = worldHalfExtent)

    fun centeredOnHome(): MapViewport = MapViewport(scale = scale, worldHalfExtent = worldHalfExtent)

    /**
     * 세계 경계를 새 반경으로 맞춘다. 좁아지면 중심을 경계 안으로 당긴다.
     * 통치 지역이 늘어 미리보기 고리가 바깥에 생겼을 때 이동 여지를 확보한다.
     */
    fun withWorldExtent(extent: Float): MapViewport {
        if (extent.isNaN() || extent.isInfinite()) return this
        val safe = extent.coerceAtLeast(TerritoryMapLayout.WORLD_HALF_EXTENT)
        if (safe == worldHalfExtent) return this
        return copy(worldHalfExtent = safe).clampedToWorld()
    }

    /**
     * 해금 범위가 넓어졌을 때 바깥 고리까지 한 화면에 담도록 배율을 낮춘다.
     * 이미 더 넓게 보고 있으면 그대로 둔다 — 사용자가 정한 배율을 되돌리지 않는다.
     */
    fun fitPreview(previewHop: Int): MapViewport {
        val ring = TerritoryMapLayout.ringRadius(previewHop)
        if (ring <= 0f) return this
        val required = TerritoryMapLayout.BASE_VIEW_RADIUS / (ring * 1.06f)
        if (scale <= required) return this
        val next = required.coerceIn(TerritoryMapLayout.MIN_SCALE, TerritoryMapLayout.MAX_SCALE)
        return copy(scale = next).clampedToWorld()
    }

    val panLimit: Float
        get() = (worldHalfExtent - viewHalfExtent).coerceAtLeast(0f)

    fun clampedToWorld(): MapViewport {
        val limit = panLimit
        return copy(
            centerX = centerX.coerceIn(-limit, limit),
            centerY = centerY.coerceIn(-limit, limit)
        )
    }

    fun isVisible(x: Float, y: Float): Boolean = visibleBounds().contains(x, y)

    fun isVisible(node: MapNode): Boolean = isVisible(node.worldX, node.worldY)

    /** 화면 안에 들어오는 거점 */
    fun visibleNodes(nodes: List<MapNode>): List<MapNode> = nodes.filter { isVisible(it) }

    /** 해금 + 화면 안에 있는 거점 */
    fun visibleUnlockedNodes(nodes: List<MapNode>): List<MapNode> =
        visibleNodes(nodes).filter { it.unlocked }

    /**
     * 화면 px → 세계 좌표.
     */
    fun worldAt(
        pointX: Float,
        pointY: Float,
        widthPx: Float,
        heightPx: Float
    ): Pair<Float, Float> {
        val unit = unitPerWorld(widthPx, heightPx) ?: return centerX to centerY
        return (centerX + (pointX - widthPx / 2f) / unit) to (centerY + (pointY - heightPx / 2f) / unit)
    }

    fun screenAt(
        worldX: Float,
        worldY: Float,
        widthPx: Float,
        heightPx: Float
    ): ScreenPoint {
        val unit = unitPerWorld(widthPx, heightPx) ?: 0f
        return ScreenPoint(
            x = widthPx / 2f + (worldX - centerX) * unit,
            y = heightPx / 2f + (worldY - centerY) * unit
        )
    }

    fun unitPerWorld(widthPx: Float, heightPx: Float): Float? {
        val side = minOf(widthPx, heightPx)
        if (side <= 0f) return null
        return side / (2f * viewHalfExtent)
    }

    /**
     * 탭 지점에서 가장 가까운 거점 — 반경([tapRadiusPx]) 밖이면 null.
     * 잠긴 거점은 선택하지 않는다(먼저 레벨을 올려야 한다).
     */
    fun hitTest(
        pointX: Float,
        pointY: Float,
        widthPx: Float,
        heightPx: Float,
        nodes: List<MapNode>,
        tapRadiusPx: Float = 56f
    ): MapNode? {
        val (wx, wy) = worldAt(pointX, pointY, widthPx, heightPx)
        val unit = unitPerWorld(widthPx, heightPx) ?: return null
        val radiusWorld = tapRadiusPx / unit
        return visibleUnlockedNodes(nodes)
            .map { node -> node to hypot(node.worldX - wx, node.worldY - wy) }
            .filter { (_, distance) -> distance <= radiusWorld }
            .minByOrNull { (_, distance) -> distance }
            ?.first
    }

    private fun hypot(dx: Float, dy: Float): Float = kotlin.math.sqrt(dx * dx + dy * dy)
}
