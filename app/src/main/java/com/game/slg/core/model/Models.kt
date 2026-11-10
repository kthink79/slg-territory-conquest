package com.game.slg.core.model

/**
 * 현실 지명 기반 영지 (최하 단위: 읍·면·동)
 */
data class Territory(
    val code: String,              // 행정동 법정동 코드 (예: "4413125000" 아산시 배방읍)
    val name: String,              // "배방읍", "온양1동", "역삼동" 등
    val province: String,          // "충청남도", "서울특별시"
    val cityCountyDistrict: String,// "아산시", "강남구"
    val population: Int,           // 실제 현실 반영 인구수
    val maxLords: Int,             // 인구 만 명당 1명 등 호족 슬롯
    val terrain: TerrainType,      // 지형 특성
    var currentLordIds: List<String> = emptyList(),
    var castellanId: String? = null, // 성주 ID (지역 중심 거점)
    var fortress: Fortress? = null
)

enum class TerrainType(val defenseBonus: Double, val mobilityPenalty: Double) {
    PLAIN(1.0, 1.0),      // 평야: 기본
    MOUNTAIN(2.5, 0.4),   // 산악: 수성 방어 극대화, 기동 페널티
    RIVER(1.8, 0.6),      // 하천/수계: 도하 시 취약, 방어 유리
    COASTAL(1.3, 0.9)     // 해안: 무역 보너스, 상륙전 대응
}

/**
 * 방어 요새 시설
 * 영지 방어 시설 구축 시 수성에서 절대적 우위(사상자 80% 격감 등)를 제공
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
        get() = when (level) {
            FortressLevel.WOODEN_PALISADE -> 0.40 // 40% 피해 감소
            FortressLevel.STONE_WALL -> 0.65       // 65% 피해 감소
            FortressLevel.MOUNTAIN_FORTRESS -> 0.80 // 80% 극감 (수성 절대 우위)
            FortressLevel.IMPREGNABLE_GATE -> 0.85  // 85% 극감
        }

    fun takeDamage(damage: Int) {
        durability = (durability - damage).coerceIn(0, maxDurability)
    }

    fun repair(amount: Int) {
        durability = (durability + amount).coerceIn(0, maxDurability)
    }
}

enum class FortressLevel(val displayName: String) {
    WOODEN_PALISADE("호족 장원 목책"),
    STONE_WALL("석조 읍성"),
    MOUNTAIN_FORTRESS("산성 요새"),
    IMPREGNABLE_GATE("천혜의 관문")
}

/**
 * 유저 및 AI 호족 (Lord)
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
    var allegianceLordId: String? = null // 종속된 상위 호족/성주 ID
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
    var employerLordId: String? = null,// 고용주 호족 ID
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
 * 호족 의회 정책 안건
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
    TROOP_LEVY,       // 전시 병력 차출
    SPECIAL_TAX,      // 전시 군량 징발
    FORTRESS_EXPAND,  // 지역 관문 요새화
    DECLARE_WAR       // 인접 성주에 대한 선전포고
}
