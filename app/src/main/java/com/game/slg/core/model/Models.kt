package com.game.slg.core.model

/**
 * 현실 지명 기반 영지 (최하 단위: 읍·면·동)
 *
 * 2020년대 대한민국 자치구 — 리더이 운영하는 하나의 생활권 거점이다.
 */
data class Territory(
    val code: String,              // 행정동 법정동 코드 (예: "4420033000" 아산시 탕정면)
    val name: String,              // "탕정면", "온양1동", "역삼1동" 등
    val province: String,          // "충청남도", "서울특별시"
    val cityCountyDistrict: String,// "아산시", "강남구"
    val population: Int,           // 실제 주민등록인구
    val maxLords: Int,             // 리더 정원 — 인구 1,000명당 1명
    val terrain: TerrainType,      // 지형 특성
    var currentLordIds: List<String> = emptyList(),
    var castellanId: String? = null, // 대표 리더 ID (지역 중심 거점)
    var fortress: Fortress? = null,
    val fertileScore: Double = 1.0,  // 물류·생산 지수 (절기 보급 생산 계수)
    val adjacentCodes: List<String> = emptyList() // 인접 영지 GIS 연결 그래프
)

enum class TerrainType(val defenseBonus: Double, val mobilityPenalty: Double) {
    PLAIN(1.0, 1.0),      // 평야: 기본
    MOUNTAIN(2.5, 0.4),   // 산악: 방어 극대화, 기동 페널티
    RIVER(1.8, 0.6),      // 하천/수계: 도하 시 취약, 방어 유리
    COASTAL(1.3, 0.9)     // 해안: 무역 보너스, 상륙전 대응
}

/**
 * 방호 시설 — 펜스·콘크리트 방벽·지하 벙커·통합 방호망.
 * 방어 시설을 먼저 확보하면 방어 시 사상자를 최대 85%까지 줄인다.
 */
data class Fortress(
    val level: FortressLevel,
    var durability: Int,
    val maxDurability: Int = 1000,
    val garrisonTroops: Int = 0
) {
    init {
        require(durability >= 0) { "Fortress durability cannot be negative" }
        require(maxDurability > 0) { "Fortress maxDurability must be positive" }
        require(garrisonTroops >= 0) { "Fortress garrisonTroops cannot be negative" }
    }

    val casualtyReductionRatio: Double
        get() = level.casualtyReduction

    fun takeDamage(damage: Int) {
        durability = (durability - damage).coerceIn(0, maxDurability)
    }

    fun repair(amount: Int) {
        durability = (durability + amount).coerceIn(0, maxDurability)
    }
}

enum class FortressLevel(val displayName: String, val casualtyReduction: Double) {
    CIVIL_FENCE("민방위 방호 펜스", 0.40),
    CONCRETE_BARRIER("콘크리트 방벽 초소", 0.65),
    UNDERGROUND_BUNKER("지하 지휘 벙커", 0.80),
    INTEGRATED_NET("통합 방호망", 0.85)
}

/**
 * 유저 및 AI 리더 (Lord)
 */
data class Lord(
    val id: String,
    val name: String,
    val isHumanPlayer: Boolean,
    var territoryCode: String,
    var troops: Int,
    var grain: Int,
    var gold: Int,
    var prestige: Int,
    var isCastellan: Boolean = false,
    var allegianceLordId: String? = null // 종속된 상위 리더/대표 리더 ID
) {
    init {
        require(troops >= 0) { "Lord troops cannot be negative" }
        require(grain >= 0) { "Lord grain cannot be negative" }
        require(gold >= 0) { "Lord gold cannot be negative" }
    }
}

/**
 * 인재 (삼국지식 인재 탐색 및 위장 잠입 스타팅)
 */
data class Talent(
    val id: String,
    val name: String,
    val isPlayerHiddenTalent: Boolean, // 다른 유저 모르게 일반 인재로 시작한 히든 유저인지 여부
    val warPower: Int,                 // 무력 (1~100)
    val intelligence: Int,             // 지력 (1~100)
    val leadership: Int,               // 통솔 (1~100)
    val politics: Int,                 // 정치 (1~100)
    val charm: Int,                    // 매력 (1~100)
    var employerLordId: String? = null,// 고용주 리더 ID
    var loyalty: Int = 100,            // 충성도 (0~100)
    var privateTroops: Int = 0,        // 암약하며 조용히 키운 사병 수
    var rebellionPrestige: Int = 0     // 쿠데타/하극상 지지도
) {
    init {
        loyalty = loyalty.coerceIn(0, 100)
        rebellionPrestige = rebellionPrestige.coerceAtLeast(0)
        privateTroops = privateTroops.coerceAtLeast(0)
    }
}

/**
 * 리더 의회 정책 안건
 */
data class CouncilBill(
    val id: String,
    val title: String,
    val type: BillType,
    val proposedByCastellanId: String,
    val troopLevyAmount: Int = 0,
    var votesInFavor: Int = 0,
    var votesAgainst: Int = 0,
    var isPassed: Boolean = false,
    var isRebellionTriggered: Boolean = false
)

enum class BillType {
    TROOP_LEVY,       // 예비군·경비 인력 소집
    SPECIAL_TAX,      // 긴급 재정 조례
    FORTRESS_EXPAND,  // 방호 시설 증설
    DECLARE_WAR,      // 인접 세력에 대한 무력 대응 결의
    JOINT_WAR,        // 지역 리더 연합 공동 전투
    JOINT_DIPLOMACY,  // 지역 리더 연합 공동 외교
    JOINT_STRATEGY,   // 지역 리더 연합 공동 전략
    EMERGENCY_DEFENSE // 외부 침공 시 긴급 방어 결의
}

/**
 * 4계절 순환 사이클 (Four Seasons Cycle)
 */
enum class Season(val displayName: String) {
    SPRING("봄 (예산 편성·지방세 징수)"),
    SUMMER("여름 (재난 대응·치수 사업)"),
    AUTUMN("가을 (결산·보급 비축)"),
    WINTER("겨울 (동절기 유지비·한파 대비)");

    fun next(): Season = when (this) {
        SPRING -> SUMMER
        SUMMER -> AUTUMN
        AUTUMN -> WINTER
        WINTER -> SPRING
    }
}

/**
 * 여름철 집중호우·재난 대응 결과
 */
enum class SummerDisasterType {
    NONE,
    FLOOD_PREVENTED,  // 배수·제방 정비 성공으로 침수 예방
    FLOOD_DISASTER,   // 대응 미흡으로 인한 침수 피해
    BUMPER_WEATHER    // 가뭄 없는 최적의 기후 (풍작 조짐)
}

/**
 * 계절 처리 결과
 */
data class SeasonActionResult(
    val season: Season,
    val goldChange: Int,
    val grainChange: Int,
    val disasterType: SummerDisasterType = SummerDisasterType.NONE,
    val winterAttritionLosses: Int = 0,
    val message: String
)

/**
 * GIS 보급선 및 수송대 모델
 */
data class TransportConvoy(
    val id: String,
    val fromTerritoryCode: String,
    val toTerritoryCode: String,
    val gold: Int,
    val grain: Int
)

data class SupplyRouteStatus(
    val isConnected: Boolean,
    val path: List<String> = emptyList(),
    val blockedByTerritoryCode: String? = null,
    val moralePenalty: Int = 0,
    val triggersBloodlessSurrender: Boolean = false,
    val message: String = ""
)

data class ConvoyResult(
    val success: Boolean,
    val deliveredGold: Int,
    val deliveredGrain: Int,
    val message: String
)

/**
 * 외교 조약 & 첩보 모델
 */
enum class TreatyType {
    NON_AGGRESSION_PACT, // 불가침 조약
    DEFENSIVE_ALLIANCE,  // 방위 동맹
    VASSAL_TRIBUTARY     // 속국 조공 조약
}

data class DiplomaticTreaty(
    val id: String,
    val lordId1: String,
    val lordId2: String,
    val treatyType: TreatyType,
    var remainingTurns: Int,
    val tributaryGoldPerTurn: Int = 0 // 속국이 종주국에 바치는 턴당 조공
)

data class TreatyViolationResult(
    val isViolated: Boolean,
    val violatorLordId: String?,
    val prestigePenalty: Int,
    val message: String
)

enum class EspionageOpType {
    GATHER_INTEL,       // 적 영지 및 병력 정보 정찰
    INSTIGATE_DISSENT   // 리더/가신 이간계 및 민심·사기 교란
}

data class EspionageResult(
    val success: Boolean,
    val opType: EspionageOpType,
    val intelGathered: String? = null,
    val moraleOrLoyaltyDeduction: Int = 0,
    val message: String
)
