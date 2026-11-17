package com.game.slg.core.model

/**
 * 현대 군 편제 — 「단일 연방의 황혼」 2020년대 대한민국 자치구 군웅.
 *
 * 기존 봉건 병과(창병·궁병·기사·투석기)를 모두 지우고,
 * 실제 편제와 장비(K2 소총·K808·K2 전차·K9·드론·지대공)로 바꿨다.
 */
enum class CombatArm(
    val displayName: String,
    /** 주 장비 */
    val weapon: String,
    /** 역할 */
    val role: String,
    val icon: String,
    /** 공격 계수 */
    val attack: Double,
    /** 방어 계수 */
    val defense: Double,
    /** 기동 계수 */
    val mobility: Double,
    /** 절기당 100명 기준 보급 소요 */
    val upkeep: Int
) {
    INFANTRY(
        displayName = "기동보병",
        weapon = "K2 소총 · K808 방호차량",
        role = "거점 점령 · 시가지 통제",
        icon = "🪖",
        attack = 1.00,
        defense = 1.10,
        mobility = 1.20,
        upkeep = 1
    ),
    ARMOR(
        displayName = "기계화 기갑",
        weapon = "K2 전차 · K21 보병전투차",
        role = "정면 돌파 · 기갑 돌격",
        icon = "🛡",
        attack = 1.65,
        defense = 1.50,
        mobility = 1.00,
        upkeep = 3
    ),
    ARTILLERY(
        displayName = "화력지원",
        weapon = "K9 자주포 · 천무 다연장로켓",
        role = "원거리 제압 · 진지 파괴",
        icon = "💥",
        attack = 1.80,
        defense = 0.70,
        mobility = 0.60,
        upkeep = 3
    ),
    DRONE(
        displayName = "드론 정찰전대",
        weapon = "정찰 드론 · 자폭 드론 · 대드론 레이더",
        role = "표적 획득 · 정밀 타격",
        icon = "🛰",
        attack = 1.30,
        defense = 0.80,
        mobility = 1.80,
        upkeep = 1
    ),
    AIR_DEFENSE(
        displayName = "방공 포대",
        weapon = "단거리 지대공 · 재밍 장비",
        role = "드론·항공 차단 · 요충 방호",
        icon = "🎯",
        attack = 0.90,
        defense = 1.70,
        mobility = 0.70,
        upkeep = 2
    ),
    SPECIAL_OPS(
        displayName = "특수작전팀",
        weapon = "침투·저격·폭파 장비",
        role = "거점 확보 · 지휘부 교란",
        icon = "🥷",
        attack = 1.40,
        defense = 0.90,
        mobility = 1.60,
        upkeep = 2
    ),
    LOGISTICS(
        displayName = "물류·수송대",
        weapon = "수송 차량 · 유류·탄약 보급",
        role = "보급 유지 · 수리 지원",
        icon = "🚚",
        attack = 0.40,
        defense = 0.60,
        mobility = 1.00,
        upkeep = 1
    );

    /** 지형별 전투 계수 — 산악은 기갑이 막히고 드론·특수전이 강하다 */
    fun terrainFactor(terrain: TerrainType): Double = when (terrain) {
        TerrainType.PLAIN -> when (this) {
            ARMOR -> 1.15
            ARTILLERY -> 1.05
            DRONE -> 1.00
            else -> 1.00
        }
        TerrainType.MOUNTAIN -> when (this) {
            DRONE -> 1.25
            SPECIAL_OPS -> 1.20
            INFANTRY -> 1.05
            ARMOR -> 0.75
            ARTILLERY -> 0.80
            AIR_DEFENSE -> 1.10
            LOGISTICS -> 0.85
        }
        TerrainType.RIVER -> when (this) {
            DRONE -> 1.15
            SPECIAL_OPS -> 1.10
            ARMOR -> 0.85
            LOGISTICS -> 0.90
            else -> 1.00
        }
        TerrainType.COASTAL -> when (this) {
            AIR_DEFENSE -> 1.20
            DRONE -> 1.10
            ARMOR -> 0.90
            else -> 1.00
        }
    }

    companion object {
        /** 화면 표시 순서 */
        val displayOrder: List<CombatArm> = listOf(
            INFANTRY, ARMOR, ARTILLERY, DRONE, AIR_DEFENSE, SPECIAL_OPS, LOGISTICS
        )
    }
}

/** 병과별 병력 */
data class ArmUnit(val arm: CombatArm, val troops: Int) {
    init {
        require(troops >= 0) { "병력은 음수일 수 없습니다." }
    }
}

/**
 * 부대 편성 — 병과별 병력 묶음.
 * 전투력은 병과 계수 × 지형 계수 × 병력으로 계산한다.
 */
data class ForceComposition(val units: List<ArmUnit>) {

    val totalTroops: Int get() = units.sumOf { it.troops }

    fun troopsOf(arm: CombatArm): Int = units.filter { it.arm == arm }.sumOf { it.troops }

    /** 공격 전투력 */
    fun attackPower(terrain: TerrainType = TerrainType.PLAIN): Double =
        units.sumOf { it.troops * it.arm.attack * it.arm.terrainFactor(terrain) }

    /** 방어 전투력 */
    fun defensePower(terrain: TerrainType = TerrainType.PLAIN): Double =
        units.sumOf { it.troops * it.arm.defense * it.arm.terrainFactor(terrain) }

    /** 100명당 보급 소요 */
    fun upkeep(): Int = units.sumOf { it.troops * it.arm.upkeep } / 100

    fun plus(other: ForceComposition): ForceComposition {
        val merged = (units + other.units).groupBy { it.arm }
            .map { (arm, list) -> ArmUnit(arm, list.sumOf { it.troops }) }
            .filter { it.troops > 0 }
            .sortedBy { CombatArm.displayOrder.indexOf(it.arm) }
        return ForceComposition(merged)
    }

    /** 병과 요약 — "기동보병 800 · 기갑 200" */
    fun describe(): String = units
        .filter { it.troops > 0 }
        .joinToString(" · ") { "${it.arm.displayName} ${it.troops}" }

    companion object {
        /** 병력 수를 기본 편제(보병 60% · 기갑 20% · 드론 10% · 화력 10%)로 나눈다 */
        fun standard(troops: Int): ForceComposition {
            val safe = troops.coerceAtLeast(0)
            return ForceComposition(
                listOf(
                    ArmUnit(CombatArm.INFANTRY, (safe * 0.60).toInt()),
                    ArmUnit(CombatArm.ARMOR, (safe * 0.20).toInt()),
                    ArmUnit(CombatArm.DRONE, (safe * 0.10).toInt()),
                    ArmUnit(CombatArm.ARTILLERY, (safe * 0.10).toInt())
                ).filter { it.troops > 0 }
            )
        }

        /** 병과 하나로만 구성 (작전별 특수 편성) */
        fun of(arm: CombatArm, troops: Int): ForceComposition =
            if (troops <= 0) ForceComposition(emptyList()) else ForceComposition(listOf(ArmUnit(arm, troops)))
    }
}

/** 시설 분류 */
enum class FacilityCategory(val label: String) {
    COMMAND("지휘"),
    DEFENSE("방호"),
    INTEL("정보"),
    ECONOMY("내정"),
    SUPPORT("지원")
}

/**
 * 현대 시설 — 성곽·목책을 대신하는 자치구 인프라.
 */
enum class FacilityType(
    val displayName: String,
    val icon: String,
    val cost: Int,
    val category: FacilityCategory,
    val effectLabel: String,
    val description: String
) {
    COMMAND_CENTER(
        displayName = "지휘통제실",
        icon = "🖥",
        cost = 900,
        category = FacilityCategory.COMMAND,
        effectLabel = "작전 경험 +10%",
        description = "전 거점 상황을 한 화면에 모아 지휘 경험치 획득을 늘린다."
    ),
    DRONE_HANGAR(
        displayName = "드론 격납고",
        icon = "🛰",
        cost = 750,
        category = FacilityCategory.DEFENSE,
        effectLabel = "드론 전투력 +15%",
        description = "정찰·자폭 드론을 상시 대기시켜 드론 병과 전투력을 높인다."
    ),
    RADAR_NETWORK(
        displayName = "방공 레이더망",
        icon = "📡",
        cost = 1100,
        category = FacilityCategory.DEFENSE,
        effectLabel = "방어 +10% · 첩보 +5",
        description = "저고도 레이더로 무인기 침투를 조기에 잡아낸다."
    ),
    LOGISTICS_HUB(
        displayName = "물류 허브",
        icon = "🚚",
        cost = 850,
        category = FacilityCategory.ECONOMY,
        effectLabel = "절기 보급 +12%",
        description = "유통 거점을 묶어 절기마다 들어오는 보급품을 늘린다."
    ),
    SMART_DEFENSE_NET(
        displayName = "스마트 방호망",
        icon = "🛡",
        cost = 1300,
        category = FacilityCategory.DEFENSE,
        effectLabel = "사상자 -8%",
        description = "감시 센서와 자동 통제선으로 방어 시 아군 손실을 줄인다."
    ),
    CYBER_INTEL_OFFICE(
        displayName = "통신·사이버 정보국",
        icon = "🕵",
        cost = 1000,
        category = FacilityCategory.INTEL,
        effectLabel = "첩보 +10",
        description = "통신 감청과 사이버 정찰로 상대 거점 정보를 먼저 확보한다."
    ),
    CIVIL_DEFENSE_CENTER(
        displayName = "민방위 지원센터",
        icon = "🚨",
        cost = 700,
        category = FacilityCategory.SUPPORT,
        effectLabel = "민심 +6",
        description = "주민 대피·구호 체계를 세워 민심을 안정시킨다."
    ),
    FIELD_HOSPITAL(
        displayName = "야전 의무지원대",
        icon = "🏥",
        cost = 800,
        category = FacilityCategory.SUPPORT,
        effectLabel = "작전 후 병력 +5%",
        description = "부상자를 신속히 복귀시켜 작전 뒤 병력 손실을 되돌린다."
    );

    companion object {
        fun byCategory(category: FacilityCategory): List<FacilityType> =
            entries.filter { it.category == category }
    }
}
