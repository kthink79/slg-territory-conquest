package com.game.slg.core.map

import com.game.slg.core.model.TerrainType

/**
 * 메인 3단 화면 상단 1/3 — 「내 영지 중심 제한 범위 지도」의 순수 좌표 모델.
 *
 * 전체 팔도 지도는 너무 커서 한 화면에 담을 수 없으므로, 플레이어 본진(매탄동)을 중심으로
 * 호족 영향권(hop 거리 [LOCAL_HOP_LIMIT] 이내)만 보여 주고, 그 밖의 원거리 세력은
 * 지도에 그리지 않고 '범위 밖' 개수로만 알린다.
 *
 * 좌표계는 본진을 (0,0)으로 두는 정규화된 세계 좌표(-[WORLD_HALF_EXTENT] ~ +[WORLD_HALF_EXTENT])다.
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 확대·축소·이동 경계를 그대로 검증할 수 있다.
 */
data class MapNode(
    val code: String,
    val name: String,
    val region: String,
    val worldX: Float,
    val worldY: Float,
    val hopDistance: Int,
    val terrain: TerrainType,
    val relationLabel: String,
    val owner: String,
    val population: Int,
    val troops: Int,
    val note: String,
    val isHome: Boolean = false
)

/** 화면 좌표(px) */
data class ScreenPoint(val x: Float, val y: Float)

/** 세계 좌표 경계 */
data class WorldBounds(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    fun contains(x: Float, y: Float): Boolean = x in minX..maxX && y in minY..maxY
}

/**
 * 본진 중심 지역 지도 데이터 — 현재 4개 영지 + 영향권 밖 원거리 세력 4곳.
 * [LOCAL_HOP_LIMIT] 이내 노드만 지도에 그린다.
 */
object TerritoryMapLayout {

    /** 세계 좌표 최대 반경 (이 밖으로는 화면이 나가지 않는다) */
    const val WORLD_HALF_EXTENT = 1.30f

    /** 확대/축소 한계 — 사용자 요청 범위 0.7배 ~ 2.5배 */
    const val MIN_SCALE = 0.7f
    const val MAX_SCALE = 2.5f
    const val DEFAULT_SCALE = 1.0f

    /** 배율 1.0에서 화면 절반이 덮는 세계 거리 (작을수록 더 확대된 상태) */
    const val BASE_VIEW_RADIUS = 0.95f

    /** 표시 범위 — 본진에서 몇 다리(hop)까지 그릴지 */
    const val LOCAL_HOP_LIMIT = 2

    const val HOME_CODE = "4111154000"

    /** 영향권 안쪽 — 지도에 그려지는 영지 */
    val LOCAL_NODES: List<MapNode> = listOf(
        MapNode(
            code = HOME_CODE,
            name = "매탄동",
            region = "경기 수원시 영통구",
            worldX = 0f,
            worldY = 0f,
            hopDistance = 0,
            terrain = TerrainType.PLAIN,
            relationLabel = "아군 영지",
            owner = "이광수 호족 (본진)",
            population = 128400,
            troops = 1200,
            note = "본진 · 정보통신 허브 · 산성 요새",
            isHome = true
        ),
        MapNode(
            code = "4111155000",
            name = "원천동",
            region = "경기 수원시 영통구",
            worldX = 0.62f,
            worldY = -0.34f,
            hopDistance = 1,
            terrain = TerrainType.PLAIN,
            relationLabel = "중립 상인회",
            owner = "원천동 상인연합",
            population = 48200,
            troops = 620,
            note = "야간 물자 협약 제안 · 관세 분쟁"
        ),
        MapNode(
            code = "4111156000",
            name = "광교동",
            region = "경기 수원시 영통구",
            worldX = -0.60f,
            worldY = -0.44f,
            hopDistance = 1,
            terrain = TerrainType.MOUNTAIN,
            relationLabel = "경계 세력",
            owner = "광교 호족연합",
            population = 63400,
            troops = 1850,
            note = "광교산 협곡 · 정찰대 출현"
        ),
        MapNode(
            code = "4146152000",
            name = "용인 수지",
            region = "경기 용인시 수지구",
            worldX = 0.36f,
            worldY = 0.62f,
            hopDistance = 2,
            terrain = TerrainType.MOUNTAIN,
            relationLabel = "산악 협곡",
            owner = "수지 산악 협곡 세력",
            population = 21500,
            troops = 940,
            note = "산악 협곡 요충 · 보급로 관문"
        )
    )

    /** 영향권 밖 — 지도에 그리지 않고 '범위 밖 N곳'으로만 표시 */
    val DISTANT_NODES: List<MapNode> = listOf(
        MapNode(
            code = "4111157000", name = "영통동", region = "경기 수원시 영통구",
            worldX = 1.02f, worldY = 0.92f, hopDistance = 3, terrain = TerrainType.PLAIN,
            relationLabel = "탐지 밖", owner = "미탐지 세력", population = 87400, troops = 0,
            note = "표시 범위 밖 · 첩보 필요"
        ),
        MapNode(
            code = "4111158000", name = "망포동", region = "경기 수원시 영통구",
            worldX = -1.04f, worldY = 0.86f, hopDistance = 3, terrain = TerrainType.PLAIN,
            relationLabel = "탐지 밖", owner = "미탐지 세력", population = 76300, troops = 0,
            note = "표시 범위 밖 · 첩보 필요"
        ),
        MapNode(
            code = "4159011000", name = "화성 동탄", region = "경기 화성시",
            worldX = 1.08f, worldY = -0.88f, hopDistance = 3, terrain = TerrainType.PLAIN,
            relationLabel = "탐지 밖", owner = "미탐지 세력", population = 142000, troops = 0,
            note = "표시 범위 밖 · 첩보 필요"
        ),
        MapNode(
            code = "4146153000", name = "용인 기흥", region = "경기 용인시 기흥구",
            worldX = -1.02f, worldY = -0.94f, hopDistance = 3, terrain = TerrainType.MOUNTAIN,
            relationLabel = "탐지 밖", owner = "미탐지 세력", population = 98600, troops = 0,
            note = "표시 범위 밖 · 첩보 필요"
        )
    )

    /** 매탄천(하천) 형상 — 세계 좌표 폴리라인 */
    val RIVER: List<Pair<Float, Float>> = listOf(
        -1.25f to -0.10f,
        -0.62f to 0.06f,
        -0.18f to 0.12f,
        0.22f to 0.30f,
        0.70f to 0.44f,
        1.25f to 0.58f
    )

    /** 도로망 — 본진에서 각 영지로 뻗는 간선 */
    val ROADS: List<Pair<Float, Float>> = LOCAL_NODES
        .filterNot { it.isHome }
        .map { it.worldX to it.worldY }

    /** 본진 영향권 경계 반경 (지도에 점선으로 표시) */
    const val INFLUENCE_RADIUS = 0.98f

    fun node(code: String): MapNode? = LOCAL_NODES.firstOrNull { it.code == code }

    /** 범위 안쪽 노드 (hop 거리 기준) */
    fun localNodes(maxHop: Int = LOCAL_HOP_LIMIT): List<MapNode> =
        LOCAL_NODES.filter { it.hopDistance <= maxHop }

    /** 표시 범위 밖 노드 */
    fun outOfRangeNodes(maxHop: Int = LOCAL_HOP_LIMIT): List<MapNode> =
        (LOCAL_NODES + DISTANT_NODES).filter { it.hopDistance > maxHop }

    /** 범위 밖 개수 — 하단 지도 배지 표시용 */
    fun outOfRangeCount(maxHop: Int = LOCAL_HOP_LIMIT): Int = outOfRangeNodes(maxHop).size

    /** hop 거리 기준 포함 여부 */
    fun isInRange(node: MapNode, maxHop: Int = LOCAL_HOP_LIMIT): Boolean = node.hopDistance <= maxHop
}

/**
 * 상단 1/3 지도의 확대/축소·이동 상태.
 * 모든 조작은 [MIN_SCALE]~[MAX_SCALE] 배율과 세계 경계 안쪽으로 스스로 제한된다.
 */
data class MapViewport(
    val scale: Float = TerritoryMapLayout.DEFAULT_SCALE,
    val centerX: Float = 0f,
    val centerY: Float = 0f
) {

    /** 화면 절반이 덮는 세계 거리 — 배율이 클수록 작아진다 */
    val viewHalfExtent: Float get() = TerritoryMapLayout.BASE_VIEW_RADIUS / scale

    /** 현재 보이는 세계 범위 */
    fun visibleBounds(): WorldBounds {
        val half = viewHalfExtent
        return WorldBounds(
            minX = centerX - half,
            maxX = centerX + half,
            minY = centerY - half,
            maxY = centerY + half
        )
    }

    /** 배율 곱 — 한계에서 멈춘다 */
    fun zoomedBy(factor: Float): MapViewport {
        if (factor <= 0f || factor.isNaN() || factor.isInfinite()) return this
        val next = (scale * factor).coerceIn(TerritoryMapLayout.MIN_SCALE, TerritoryMapLayout.MAX_SCALE)
        if (next == scale) return this
        return copy(scale = next).clampedToWorld()
    }

    fun zoomIn(step: Float = 1.3f): MapViewport = zoomedBy(step)

    fun zoomOut(step: Float = 1.3f): MapViewport = zoomedBy(1f / step)

    /** 세계 좌표 기준 이동 — 경계 밖으로는 나가지 않는다 */
    fun pannedBy(dxWorld: Float, dyWorld: Float): MapViewport =
        copy(centerX = centerX + dxWorld, centerY = centerY + dyWorld).clampedToWorld()

    /** 내 영지 중심(초기 배율·본진 중앙)으로 복귀 */
    fun reset(): MapViewport = MapViewport()

    /** 본진을 화면 중앙에 두되 배율은 유지 */
    fun centeredOnHome(): MapViewport = MapViewport(scale = scale)

    /** 이동 가능한 최대 오프셋 (세계 경계 - 화면 절반) */
    val panLimit: Float
        get() = (TerritoryMapLayout.WORLD_HALF_EXTENT - viewHalfExtent).coerceAtLeast(0f)

    fun clampedToWorld(): MapViewport {
        val limit = panLimit
        return copy(
            centerX = centerX.coerceIn(-limit, limit),
            centerY = centerY.coerceIn(-limit, limit)
        )
    }

    /** 세계 좌표가 지금 화면 안에 들어오는지 */
    fun isVisible(x: Float, y: Float): Boolean = visibleBounds().contains(x, y)

    fun isVisible(node: MapNode): Boolean = isVisible(node.worldX, node.worldY)

    /** 지금 화면에 보이는 영지 목록 (범위 안쪽 + 화면 안) */
    fun visibleNodes(
        nodes: List<MapNode> = TerritoryMapLayout.localNodes()
    ): List<MapNode> = nodes.filter { TerritoryMapLayout.isInRange(it) && isVisible(it) }

    /**
     * 화면 px → 세계 좌표.
     * 정사각 기준으로 배율을 유지하기 위해 짧은 변을 기준 길이로 쓴다.
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

    /** 세계 좌표 → 화면 px */
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

    /** 1 세계 단위당 px */
    fun unitPerWorld(widthPx: Float, heightPx: Float): Float? {
        val side = minOf(widthPx, heightPx)
        if (side <= 0f) return null
        return side / (2f * viewHalfExtent)
    }

    /**
     * 탭 지점에서 가장 가까운 영지 선택 — 반경([tapRadiusPx]) 밖이면 null.
     */
    fun hitTest(
        pointX: Float,
        pointY: Float,
        widthPx: Float,
        heightPx: Float,
        nodes: List<MapNode> = TerritoryMapLayout.localNodes(),
        tapRadiusPx: Float = 48f
    ): MapNode? {
        val (wx, wy) = worldAt(pointX, pointY, widthPx, heightPx)
        val unit = unitPerWorld(widthPx, heightPx) ?: return null
        val radiusWorld = tapRadiusPx / unit
        return visibleNodes(nodes)
            .map { node -> node to hypot(node.worldX - wx, node.worldY - wy) }
            .filter { (_, distance) -> distance <= radiusWorld }
            .minByOrNull { (_, distance) -> distance }
            ?.first
    }

    private fun hypot(dx: Float, dy: Float): Float = kotlin.math.sqrt(dx * dx + dy * dy)
}
