package com.game.slg.core.region

import com.game.slg.core.model.TerrainType

/**
 * 현실 행정구역 1개(읍·면·동) — 게임의 최소 영지 단위.
 *
 * 인구는 행정안전부 주민등록인구(2026년 9월 기준)를 그대로 옮겼고,
 * 일부 지역은 공개 통계의 최근값을 반올림한 근사치다.
 */
data class RegionArea(
    val code: String,                 // 법정동 코드 (예: 4420033000 아산시 탕정면)
    val name: String,                 // "탕정면", "역삼1동"
    val province: String,             // "충청남도"
    val city: String,                 // "아산시", "강남구"
    val district: String = "",        // "영통구" (구가 있는 시)
    val population: Int,              // 주민등록인구
    val terrain: TerrainType = TerrainType.PLAIN,
    val neighbors: List<String> = emptyList() // 인접 읍·면·동 코드
) {
    /** 시·군·구 단위 묶음 키 (예: "경기도 수원시 영통구") */
    val groupKey: String
        get() = listOf(province, city, district).filter { it.isNotBlank() }.joinToString(" ")

    val fullName: String
        get() = listOf(province, city, district, name).filter { it.isNotBlank() }.joinToString(" ")

    /** 이 지역에 등록 가능한 리더 정원 — 인구 1,000명당 1개(최소 1개) */
    val lordCapacity: Int
        get() = LordCapacity.of(population)

    /** 리더 정원이 가득 찼는지 (신규 가입 불가) */
    val isLordSlotFull: Boolean
        get() = lordCapacity <= 0
}

/**
 * 리더 정원 규칙 — 사용자 확정 사항.
 *
 * "지역 별 등록 가입한 리더(가칭) 수는 인구 1000명당 1개씩 가능하게 설정해줘"
 */
object LordCapacity {
    /** 리더 1명이 담당하는 주민 수 */
    const val CITIZENS_PER_LORD: Int = 1000

    /** 인구 → 리더 정원 (내림, 최소 1개) */
    fun of(population: Int): Int =
        (population / CITIZENS_PER_LORD).coerceAtLeast(1)

    /** 정원이 몇 명분의 주민을 대표하는지 */
    fun residentsPerLord(population: Int): Int =
        population / of(population).coerceAtLeast(1)
}

/** 시·군·구 단위 시작 지역 선택 묶음 */
data class RegionGroup(
    val key: String,
    val province: String,
    val label: String,
    val areas: List<RegionArea>
) {
    val totalPopulation: Int get() = areas.sumOf { it.population }
    val totalLordSlots: Int get() = areas.sumOf { it.lordCapacity }
}

/**
 * 시작 지역 데이터베이스 — 충남 아산·천안, 수도권(수원·성남·용인·화성·서울), 부산·대구.
 *
 * 모든 노드는 인접 그래프로 연결되며, 시작 지역에서 BFS hop 거리로
 * "내 구역 → 인접 읍·면·동 → 시·군 전역 → 광역 생활권" 단계가 정해진다.
 */
object RegionCatalog {

    /** 기본 시작 지역 — 사용자가 예로 든 충남 아산시 탕정면 */
    const val DEFAULT_AREA_CODE: String = "4420033000"

    private fun area(
        code: String,
        name: String,
        province: String,
        city: String,
        population: Int,
        district: String = "",
        terrain: TerrainType = TerrainType.PLAIN,
        neighbors: List<String> = emptyList()
    ) = RegionArea(code, name, province, city, district, population, terrain, neighbors)

    /** 원본 데이터 — 인접 관계는 한쪽만 적어도 양방향으로 보정된다. */
    private val RAW_AREAS: List<RegionArea> = listOf(
        // ── 충청남도 아산시 (2026-09 주민등록) ──────────────────────────────
        area("4420033000", "탕정면", "충청남도", "아산시", 48604,
            neighbors = listOf("4420034000", "4420035000", "4420036000")),
        area("4420034000", "배방읍", "충청남도", "아산시", 99090,
            neighbors = listOf("4420033000", "4420035000", "4413151000")),
        area("4420035000", "음봉면", "충청남도", "아산시", 22862,
            neighbors = listOf("4420033000", "4420034000", "4420052000")),
        area("4420036000", "염치읍", "충청남도", "아산시", 5527,
            neighbors = listOf("4420033000", "4420051000", "4420032000")),
        area("4420051000", "온양1동", "충청남도", "아산시", 9227,
            neighbors = listOf("4420036000", "4420052000")),
        area("4420052000", "온양2동", "충청남도", "아산시", 9551,
            neighbors = listOf("4420051000", "4420035000", "4420037000")),
        area("4420037000", "신창면", "충청남도", "아산시", 22599,
            neighbors = listOf("4420052000", "4420032000")),
        area("4420032000", "송악면", "충청남도", "아산시", 3872,
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("4420036000", "4420037000")),

        // ── 충청남도 천안시 ────────────────────────────────────────────────
        area("4413151000", "불당1동", "충청남도", "천안시", 31539, district = "서북구",
            neighbors = listOf("4420034000", "4413152000", "4413153000")),
        area("4413152000", "불당2동", "충청남도", "천안시", 37327, district = "서북구",
            neighbors = listOf("4413151000", "4413153000")),
        area("4413153000", "백석동", "충청남도", "천안시", 40205, district = "서북구",
            neighbors = listOf("4413151000", "4413152000", "4413154000", "4413155000")),
        area("4413154000", "신방동", "충청남도", "천안시", 42757, district = "동남구",
            neighbors = listOf("4413153000", "4413155000", "4413131000")),
        area("4413155000", "성정1동", "충청남도", "천안시", 16772, district = "서북구",
            neighbors = listOf("4413153000", "4413154000")),
        area("4413131000", "목천읍", "충청남도", "천안시", 21652, district = "동남구",
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("4413154000")),

        // ── 경기도 수원시 ──────────────────────────────────────────────────
        area("4111154000", "매탄3동", "경기도", "수원시", 33599, district = "영통구",
            neighbors = listOf("4111154100", "4111155000", "4111351000")),
        area("4111154100", "매탄4동", "경기도", "수원시", 19241, district = "영통구",
            neighbors = listOf("4111154000", "4111157000")),
        area("4111155000", "원천동", "경기도", "수원시", 45639, district = "영통구",
            neighbors = listOf("4111154000", "4111156000", "4111157000")),
        area("4111156000", "광교1동", "경기도", "수원시", 53856, district = "영통구",
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("4111155000", "4111157000", "4146151000")),
        area("4111157000", "영통1동", "경기도", "수원시", 32240, district = "영통구",
            neighbors = listOf("4111155000", "4111156000", "4111154100", "4146351000", "4159051000")),
        area("4111351000", "권선1동", "경기도", "수원시", 23880, district = "권선구",
            neighbors = listOf("4111154000")),

        // ── 경기도 성남시 분당구 ───────────────────────────────────────────
        area("4113551000", "정자1동", "경기도", "성남시", 30421, district = "분당구",
            neighbors = listOf("4113552000", "4113553000", "1171052000")),
        area("4113552000", "서현1동", "경기도", "성남시", 30506, district = "분당구",
            neighbors = listOf("4113551000", "4113554000")),
        area("4113553000", "판교동", "경기도", "성남시", 26227, district = "분당구",
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("4113551000", "4113554000", "4146351000")),
        area("4113554000", "삼평동", "경기도", "성남시", 23382, district = "분당구",
            neighbors = listOf("4113553000", "4113552000")),

        // ── 경기도 용인시 ──────────────────────────────────────────────────
        area("4146151000", "풍덕천1동", "경기도", "용인시", 33400, district = "수지구",
            neighbors = listOf("4146351000", "4111156000")),
        area("4146351000", "구갈동", "경기도", "용인시", 27100, district = "기흥구",
            neighbors = listOf("4146151000", "4146551000", "4111157000")),
        area("4146551000", "포곡읍", "경기도", "용인시", 21400, district = "처인구",
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("4146351000")),

        // ── 경기도 화성시 ──────────────────────────────────────────────────
        area("4159051000", "동탄1동", "경기도", "화성시", 44200,
            neighbors = listOf("4159052000", "4159031000", "4111157000")),
        area("4159052000", "동탄2동", "경기도", "화성시", 40100,
            neighbors = listOf("4159051000", "4159031000")),
        area("4159031000", "봉담읍", "경기도", "화성시", 25300,
            neighbors = listOf("4159051000", "4159052000")),

        // ── 서울특별시 강남구 ──────────────────────────────────────────────
        area("1168051000", "역삼1동", "서울특별시", "강남구", 34307,
            neighbors = listOf("1168052000", "1168053000", "1168054000")),
        area("1168052000", "삼성1동", "서울특별시", "강남구", 12700,
            neighbors = listOf("1168051000", "1168053000", "1168054000", "1171051000")),
        area("1168053000", "청담동", "서울특별시", "강남구", 26488,
            neighbors = listOf("1168051000", "1168052000", "1168054000")),
        area("1168054000", "대치1동", "서울특별시", "강남구", 23535,
            neighbors = listOf("1168051000", "1168052000", "1168053000", "1168056000")),
        area("1168056000", "대치2동", "서울특별시", "강남구", 38306,
            neighbors = listOf("1168054000")),

        // ── 서울특별시 송파구 ──────────────────────────────────────────────
        area("1171051000", "잠실3동", "서울특별시", "송파구", 25300,
            neighbors = listOf("1168052000", "1171053000")),
        area("1171053000", "방이1동", "서울특별시", "송파구", 22100,
            neighbors = listOf("1171051000", "1171052000")),
        area("1171052000", "문정1동", "서울특별시", "송파구", 28400,
            neighbors = listOf("1171053000")),

        // ── 부산광역시 해운대구 ────────────────────────────────────────────
        area("2635051000", "우1동", "부산광역시", "해운대구", 26900,
            terrain = TerrainType.COASTAL,
            neighbors = listOf("2635052000")),
        area("2635052000", "좌1동", "부산광역시", "해운대구", 19400,
            terrain = TerrainType.COASTAL,
            neighbors = listOf("2635051000", "2635053000")),
        area("2635053000", "반송2동", "부산광역시", "해운대구", 17300,
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("2635052000", "2635054000")),
        area("2635054000", "송정동", "부산광역시", "해운대구", 11200,
            terrain = TerrainType.COASTAL,
            neighbors = listOf("2635053000", "2635051000")),
        area("2635055000", "우2동", "부산광역시", "해운대구", 18900,
            terrain = TerrainType.COASTAL,
            neighbors = listOf("2635051000", "2635052000")),
        area("2635056000", "좌4동", "부산광역시", "해운대구", 15600,
            neighbors = listOf("2635052000")),
        area("2650051000", "광안1동", "부산광역시", "수영구", 19800,
            terrain = TerrainType.COASTAL,
            neighbors = listOf("2635051000", "2650052000")),
        area("2650052000", "남천1동", "부산광역시", "수영구", 12400,
            terrain = TerrainType.COASTAL,
            neighbors = listOf("2650051000")),

        // ── 대구광역시 수성구 ──────────────────────────────────────────────
        area("2726051000", "범어1동", "대구광역시", "수성구", 22600,
            neighbors = listOf("2726052000")),
        area("2726052000", "만촌1동", "대구광역시", "수성구", 28100,
            neighbors = listOf("2726051000", "2726053000", "2726055000")),
        area("2726053000", "범어4동", "대구광역시", "수성구", 21300,
            neighbors = listOf("2726051000", "2726054000")),
        area("2726054000", "지산1동", "대구광역시", "수성구", 24100,
            terrain = TerrainType.MOUNTAIN,
            neighbors = listOf("2726053000", "2726056000")),
        area("2726055000", "만촌3동", "대구광역시", "수성구", 22700,
            neighbors = listOf("2726052000")),
        area("2726056000", "황금1동", "대구광역시", "수성구", 20500,
            neighbors = listOf("2726054000", "2726051000"))
    )

    /** 양방향 인접 관계로 보정한 최종 데이터 */
    val AREAS: List<RegionArea> = symmetrize(RAW_AREAS)

    val BY_CODE: Map<String, RegionArea> = AREAS.associateBy { it.code }

    /** 시·군·구 묶음 — 선택 화면의 2단계 목록 */
    val GROUPS: List<RegionGroup> = AREAS
        .groupBy { it.groupKey }
        .map { (key, areas) ->
            val first = areas.first()
            RegionGroup(
                key = key,
                province = first.province,
                label = if (first.district.isBlank()) first.city else "${first.city} ${first.district}",
                areas = areas.sortedByDescending { it.population }
            )
        }
        .sortedWith(compareBy({ provinceOrder(it.province) }, { it.label }))

    /** 시·도 목록 (선택 화면 상단 칩) */
    val PROVINCES: List<String> = AREAS.map { it.province }.distinct()
        .sortedBy { provinceOrder(it) }

    init {
        require(AREAS.isNotEmpty()) { "지역 데이터가 비어 있습니다." }
        require(AREAS.all { it.population > 0 }) { "인구는 0보다 커야 합니다." }
        require(AREAS.all { it.lordCapacity >= 1 }) { "리더 정원은 최소 1개입니다." }
        require(AREAS.all { it.code.length == 10 }) { "법정동 코드는 10자리입니다." }
        require(BY_CODE.size == AREAS.size) { "법정동 코드가 중복됩니다." }
    }

    fun area(code: String): RegionArea? = BY_CODE[code]

    /** 기본 시작 지역 (탕정면) */
    fun defaultRegion(): RegionArea = BY_CODE.getValue(DEFAULT_AREA_CODE)

    fun groupsOfProvince(province: String): List<RegionGroup> =
        GROUPS.filter { it.province == province }

    fun areasOfGroup(groupKey: String): List<RegionArea> =
        AREAS.filter { it.groupKey == groupKey }.sortedByDescending { it.population }

    fun neighbors(code: String): List<RegionArea> =
        BY_CODE[code]?.neighbors.orEmpty().mapNotNull { BY_CODE[it] }

    /**
     * 시작 지역에서의 BFS hop 거리.
     * 같은 시·군을 넘어 인접 시·군까지 이어지므로 "광역 생활권" 단계 계산에 쓰인다.
     */
    fun hopDistances(centerCode: String, limit: Int = 8): Map<String, Int> {
        val center = BY_CODE[centerCode] ?: return emptyMap()
        val distance = mutableMapOf(center.code to 0)
        val queue = ArrayDeque<String>()
        queue.add(center.code)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val hop = distance.getValue(current)
            if (hop >= limit) continue
            for (next in BY_CODE[current]?.neighbors.orEmpty()) {
                if (distance.containsKey(next)) continue
                distance[next] = hop + 1
                queue.add(next)
            }
        }
        return distance
    }

    /** 시작 지역 묶음(시·군·구) 안의 읍·면·동 */
    fun sameGroup(region: RegionArea): List<RegionArea> =
        areasOfGroup(region.groupKey)

    fun regionCount(): Int = AREAS.size
    fun groupCount(): Int = GROUPS.size
    fun totalPopulation(): Int = AREAS.sumOf { it.population }
    fun totalLordSlots(): Int = AREAS.sumOf { it.lordCapacity }

    private fun symmetrize(raw: List<RegionArea>): List<RegionArea> {
        val links = mutableMapOf<String, MutableSet<String>>()
        raw.forEach { links.getOrPut(it.code) { mutableSetOf() } }
        raw.forEach { area ->
            area.neighbors.forEach { neighbor ->
                if (links.containsKey(neighbor)) {
                    links.getOrPut(area.code) { mutableSetOf() }.add(neighbor)
                    links.getOrPut(neighbor) { mutableSetOf() }.add(area.code)
                }
            }
        }
        return raw.map { area ->
            area.copy(neighbors = links[area.code].orEmpty().sorted())
        }
    }

    private fun provinceOrder(province: String): Int = when (province) {
        "서울특별시" -> 0
        "경기도" -> 1
        "충청남도" -> 2
        "부산광역시" -> 3
        "대구광역시" -> 4
        else -> 9
    }
}
