package com.game.slg.core.map

import com.game.slg.core.model.TerrainType
import com.game.slg.core.region.RegionArea
import com.game.slg.core.region.RegionCatalog
import kotlin.math.cos
import kotlin.math.sin

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
    /** 이 지역의 호족 정원 (인구 / 1,000) */
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
 */
object TerritoryMapLayout {

    /** 세계 좌표 최대 반경 (이 밖으로는 화면이 나가지 않는다) */
    const val WORLD_HALF_EXTENT = 1.55f

    /** 확대/축소 한계 — 0.7배 ~ 2.5배 */
    const val MIN_SCALE = 0.7f
    const val MAX_SCALE = 2.5f
    const val DEFAULT_SCALE = 1.0f

    /** 배율 1.0에서 화면 절반이 덮는 세계 거리 */
    const val BASE_VIEW_RADIUS = 0.95f

    /** 영향권 경계 점선 반경 (hop 1 고리) */
    const val INFLUENCE_RADIUS = 0.52f

    /** 장식용 하천 폴리라인 — 실제 지형과 무관한 지도 질감 */
    val RIVER: List<Pair<Float, Float>> = listOf(
        -1.25f to -0.10f,
        -0.62f to 0.06f,
        -0.18f to 0.12f,
        0.22f to 0.30f,
        0.70f to 0.44f,
        1.25f to 0.58f
    )

    /** hop → 고리 반경 */
    fun ringRadius(hop: Int): Float = when (hop) {
        0 -> 0f
        1 -> 0.52f
        2 -> 0.80f
        3 -> 1.04f
        4 -> 1.26f
        else -> 1.46f
    }

    /**
     * 시작 지역 + 지휘 레벨(해금 hop)으로 지도 노드 생성.
     *
     * @param start 시작 지역
     * @param unlockedHop 지휘 레벨로 열린 최대 hop
     * @param playerName 플레이어 호족 이름 (본진 표기)
     * @param homeTroops 본진 병력
     */
    fun build(
        start: RegionArea,
        unlockedHop: Int,
        playerName: String = "플레이어",
        homeTroops: Int = 0
    ): List<MapNode> {
        val distances = RegionCatalog.hopDistances(start.code, limit = 30)
        val previewHop = (unlockedHop + 1).coerceAtMost(6)
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
                    note = "호족 정원 ${area.lordCapacity}석 · 인구 ${area.population}명",
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
        val previewHop = (unlockedHop + 1).coerceAtMost(6)
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
 * 상단 지도의 확대/축소·이동 상태.
 */
data class MapViewport(
    val scale: Float = TerritoryMapLayout.DEFAULT_SCALE,
    val centerX: Float = 0f,
    val centerY: Float = 0f
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

    fun zoomIn(step: Float = 1.3f): MapViewport = zoomedBy(step)

    fun zoomOut(step: Float = 1.3f): MapViewport = zoomedBy(1f / step)

    fun pannedBy(dxWorld: Float, dyWorld: Float): MapViewport =
        copy(centerX = centerX + dxWorld, centerY = centerY + dyWorld).clampedToWorld()

    /** 내 구역 중심(초기 배율·본진 중앙)으로 복귀 */
    fun reset(): MapViewport = MapViewport()

    fun centeredOnHome(): MapViewport = MapViewport(scale = scale)

    val panLimit: Float
        get() = (TerritoryMapLayout.WORLD_HALF_EXTENT - viewHalfExtent).coerceAtLeast(0f)

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
        tapRadiusPx: Float = 48f
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
