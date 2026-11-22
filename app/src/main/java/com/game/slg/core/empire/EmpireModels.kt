package com.game.slg.core.empire

import com.game.slg.core.hero.HeroRole

/**
 * 영토 운영 모델 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-19): *"영지 선택·전환 및 편입 영지 직접 통치 / 강력한 영웅 일임 +
 * 속국 통합 운영", "인구수 5% 병력 상한 & 고비용 용병", "같은 지역 내 타 리더 세력
 * 흡수·편입 다각화"*
 *
 * · 편입 영지는 직할 / 속국 / 자치령 세 가지로 운영된다.
 * · 영웅을 태수로 일임하면 영웅 능력치에 따라 자동 개발·민심·세수·징병·방어가 돌아간다.
 * · 정규 병력은 전체 인구의 5%까지만 모을 수 있고, 그 위는 고비용 용병으로 채운다.
 * · 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 편입 영지의 운영 형태 */
enum class TerritoryStatus(val label: String, val icon: String, val description: String) {
    DIRECT("직할", "🏙", "리더가 직접 통치한다. 세수·징병 100%"),
    VASSAL("속국", "🤝", "조공을 바치고 병력을 차출한다. 세수 60% · 징병 70%"),
    AUTONOMOUS("자치령", "🏳", "자치를 인정받는 대신 우호·조공만 유지한다. 세수 40% · 징병 30%");

    /** 세수 계수 */
    val taxRate: Double get() = when (this) {
        DIRECT -> 1.0
        VASSAL -> 0.60
        AUTONOMOUS -> 0.40
    }

    /** 징병 계수 */
    val levyRate: Double get() = when (this) {
        DIRECT -> 1.0
        VASSAL -> 0.70
        AUTONOMOUS -> 0.30
    }
}

/**
 * 편입 영지 1곳 — 직접 통치하거나 영웅에게 일임한다.
 *
 * @param viceroyHeroId 일임한 영웅 id (null 이면 미배치)
 * @param autoOrder 일임 시 자동 운영 지침
 */
data class ManagedTerritory(
    val code: String,
    val name: String,
    val population: Int,
    val status: TerritoryStatus = TerritoryStatus.DIRECT,
    /** 민심 0~100 */
    val morale: Int = 55,
    /** 개발도 0~100 — 세수·징병·방어의 바탕 */
    val development: Int = 20,
    /** 이 영지의 주둔 병력 */
    val troops: Int = 0,
    /** 이 영지에 쌓인 식량 */
    val food: Int = 0,
    /** 이 영지에 쌓인 자금 */
    val gold: Int = 0,
    /** 방어 태세 0~100 */
    val defense: Int = 30,
    val viceroyHeroId: String? = null,
    val viceroyName: String? = null,
    val viceroyRole: String? = null,
    /** 절기당 조공 (속국·자치령) */
    val tributePerSeason: Int = 0,
    /** 편입 경위 — 화면에 한 줄로 보인다 */
    val absorbedBy: String = "인계"
) {
    /** 이 영지가 낼 수 있는 정규 병력 상한 — 인구의 5% */
    val troopCap: Int get() = ManpowerEngine.capOf(population)

    /** 영지 1곳의 세력 지수 — 개발·민심·병력·방어의 합성 */
    val powerIndex: Int
        get() = (development * 4 + morale * 2 + troops / 10 + defense * 3)

    /** 절기 세수 — 개발도와 민심, 운영 형태가 함께 작용한다 */
    fun taxIncome(): Int =
        ((population / 400.0) * (0.5 + development / 100.0) * status.taxRate).toInt() +
            tributePerSeason

    /** 절기 징병 가능 수 — 인구 5% 상한 안에서 개발도만큼 채운다 */
    fun leviesPerSeason(): Int =
        (population * 0.05 * (development / 120.0) * status.levyRate).toInt()

    /** 절기 식량 생산 */
    fun foodIncome(): Int =
        ((population / 90.0) * (0.6 + development / 150.0) * status.taxRate).toInt()

    val needsViceroy: Boolean get() = viceroyHeroId == null

    /** 화면용 한 줄 — "직할 · 민심 62 · 개발 48 · ⚔ 1,200" */
    fun summaryLine(): String =
        "$status · 민심 $morale · 개발 $development · ⚔ ${"%,d".format(troops)}"
}

/** 세력과의 관계 — 유저/AI 구분 없이 같은 이름만 쓴다 */
enum class FactionRelation(val label: String, val icon: String) {
    HOSTILE("적대", "⚔"),
    WARY("경계", "👁"),
    NEUTRAL("중립", "⚖"),
    FRIENDLY("우호", "🤝"),
    ALLIED("연합", "🛡"),
    VASSAL("속국", "🏳"),
    AUTONOMOUS("자치령", "🏘")
}

/**
 * 같은 지역의 다른 리더 세력 1곳.
 *
 * 편입 대상이며, [relation]은 흡수 방법에 따라 달라진다.
 * 이 리더가 실제 유저인지 AI인지는 어떤 필드로도 드러나지 않는다(블라인드 원칙).
 */
data class Faction(
    val id: String,
    val name: String,
    val territoryCode: String,
    val territoryName: String,
    val population: Int,
    val troops: Int,
    val morale: Int,
    val affinity: Int,
    val prestige: Int,
    val gold: Int = 600,
    val food: Int = 1200,
    val relation: FactionRelation = FactionRelation.NEUTRAL,
    /** 이 세력이 우리 연방에 편입됐는지 */
    val incorporated: Boolean = false,
    val absorbedBy: String? = null,
    /** 경제 원조 누적 횟수 (경제 종속 편입 조건) */
    val aidCount: Int = 0,
    /** 공동 적 여부 (연방 결성 조건) */
    val commonEnemy: Boolean = false,
    /** 절기 조공 */
    val tributePerSeason: Int = 0,
    /**
     * 이 세력 밑에 있는 영웅 id 목록 — lord.id 시드로 결정적으로 2~4명.
     * 편입되면 이 영웅들이 처분 대기로 넘어간다.
     */
    val heroIds: List<String> = emptyList()
) {
    val troopRatioAgainstUs: Double get() = troops.toDouble()

    val statusLine: String get() = "${relation.icon} ${relation.label} · ⚔ ${"%,d".format(troops)} · 민심 $morale"

    val isAbsorbable: Boolean get() = !incorporated
}

/** 흡수·편입 방법 6종 */
enum class AbsorptionMethod(
    val label: String,
    val icon: String,
    val short: String,
    val condition: String
) {
    CONQUEST(
        "전투 흡수",
        "⚔",
        "병력 전멸 → 직접 흡수",
        "우리 병력이 상대의 105% 이상일 때 전투로 전멸시켜 직할로 편입"
    ),
    SUBVERSION(
        "첩자 민심 전환",
        "🕵",
        "민심 40 이하 → 속국",
        "첩보력 50 이상 + 상대 민심 40 이하일 때 민심을 돌려 속국으로 편입"
    ),
    TRIBUTE_PRESSURE(
        "무력 압박 조공",
        "💪",
        "병력 1.6배 → 조공",
        "우리 병력이 상대의 160% 이상일 때 압박해 절기 조공을 받는다"
    ),
    FEDERATION(
        "연방 결성",
        "🛡",
        "우호 60 + 공동 적",
        "우호도 60 이상이고 공동 적이 있을 때 연합을 결성해 맹주가 된다"
    ),
    ECONOMIC_DEPENDENCE(
        "경제 종속 편입",
        "💰",
        "원조 2회 → 편입",
        "식량·골드 원조를 2회 하고 교역 의존을 만들면 경제적으로 편입된다"
    ),
    GOODWILL_AUTONOMY(
        "친선 자치령 합류",
        "🏳",
        "외교 영웅 파견",
        "외교 영웅(외교 70 이상)을 파견해 자치를 보장하면 자치령으로 합류한다"
    )
}

/** 흡수 시도 결과 */
data class AbsorptionOutcome(
    val success: Boolean,
    val method: AbsorptionMethod,
    val message: String,
    val faction: Faction,
    val goldDelta: Int = 0,
    val foodDelta: Int = 0,
    val prestigeDelta: Int = 0,
    val moraleDelta: Int = 0,
    /** 새로 편입된 영지 (직할·속국·자치령) */
    val territory: ManagedTerritory? = null,
    /** 이번에 받은 조공·노획 */
    val lootLabel: String = "",
    /** 전투 흡수에서 잃은 아군 병력 */
    val troopLoss: Int = 0,
    /** 편입으로 처분 대기에 넘어간 상대 소속 영웅 id */
    val capturedHeroes: List<String> = emptyList()
) {
    val absorbed: Boolean get() = success && territory != null
}

/** 한 절기의 영토 운영 결과 */
data class EmpireTurnReport(
    val goldChange: Int,
    val foodChange: Int,
    val troopChange: Int,
    val populationChange: Int,
    val moraleChange: Int,
    val lines: List<String>,
    val territories: List<ManagedTerritory>
) {
    val summary: String get() = lines.joinToString("\n")
}

/** 병력 운영 상태 — 정규 + 용병 */
data class ManpowerState(
    val regular: Int = 0,
    val mercenaries: List<MercenaryBand> = emptyList(),
    /** 직전 절기에 용병 유지비를 못 줘서 이탈한 수 */
    val lastDeserted: Int = 0
) {
    val mercenaryTroops: Int get() = mercenaries.sumOf { it.troops }
    val total: Int get() = regular + mercenaryTroops
    val upkeepGold: Int get() = mercenaries.sumOf { it.upkeepPerSeason }
}

/**
 * 고비용 용병 부대 — 정규 병력 상한과 별도로 운용한다.
 * 고용비·유지비가 정규 병력의 3~4배이고, 유지비를 못 주면 이탈한다.
 */
data class MercenaryBand(
    val id: String,
    val name: String,
    val troops: Int,
    val hireCost: Int,
    val upkeepPerSeason: Int,
    val contractSeasons: Int = 4,
    val origin: String = "용병단"
) {
    val summary: String get() = "$name ${"%,d".format(troops)}명 · 유지비 ${"%,d".format(upkeepPerSeason)}G/절기"
}

/** 정규 병력 상한 — 전체 영지 인구의 5% */
object ManpowerEngine {

    /** 정규 병력 상한 비율 (%) */
    const val CAP_PERCENT = 5

    fun capOf(population: Int): Int = (population.coerceAtLeast(0).toLong() * CAP_PERCENT / 100).toInt()

    /** 정규 병력 여유 */
    fun roomFor(population: Int, regular: Int): Int = (capOf(population) - regular).coerceAtLeast(0)

    /** 편입 영지 전체 인구 */
    fun totalPopulation(territories: List<ManagedTerritory>): Int =
        territories.sumOf { it.population }

    /** 편입 영지 전체의 정규 병력 상한 */
    fun empireCap(territories: List<ManagedTerritory>): Int = capOf(totalPopulation(territories))

    /** 속국·자치령을 포함한 총력 지수 — 편입이 늘수록 커진다 */
    fun totalPower(territories: List<ManagedTerritory>, regularTroops: Int, mercenaries: Int): Int {
        val land = territories.sumOf { it.powerIndex }
        return (regularTroops + mercenaries) + land / 10
    }

    /** "정규 1,200 / 상한 2,430 (인구 48,604의 5%) · 용병 600" */
    fun label(population: Int, regular: Int, mercenary: Int): String =
        "정규 ${"%,d".format(regular)} / 상한 ${"%,d".format(capOf(population))}" +
            " (인구 ${"%,d".format(population)}의 $CAP_PERCENT%) · 용병 ${"%,d".format(mercenary)}"
}

/** 용병 고용 규칙 */
object MercenaryEngine {

    /** 100명당 고용비 — 정규 모집(자금 500G/300명)의 약 14배 */
    const val HIRE_PER_100 = 2_400

    /** 100명당 절기 유지비 */
    const val UPKEEP_PER_100 = 900

    /** 유지비 미납 시 이탈 비율 */
    const val DESERT_RATIO = 0.35

    val NAMES: List<String> = listOf(
        "백호 용병단", "철마 기갑단", "야간 독수리대", "청룡 방공대",
        "유랑 드론대", "금강 경비단", "한빛 특수전조", "동해 수송단"
    )

    fun hireCostOf(troops: Int): Int =
        (troops.coerceAtLeast(0) * HIRE_PER_100 / 100).coerceAtLeast(HIRE_PER_100)

    fun upkeepOf(troops: Int): Int =
        (troops.coerceAtLeast(0) * UPKEEP_PER_100 / 100).coerceAtLeast(UPKEEP_PER_100 / 2)

    /** 용병 고용 — 자금이 모자라면 실패 사유를 돌려준다 */
    fun hire(
        bands: List<MercenaryBand>,
        troops: Int,
        gold: Int,
        seedName: Int = bands.size
    ): Pair<MercenaryBand?, String> {
        val size = troops.coerceAtLeast(100)
        val cost = hireCostOf(size)
        if (gold < cost) {
            return null to "[용병] 자금이 부족합니다. (필요 ${"%,d".format(cost)}G / 보유 ${"%,d".format(gold)}G)"
        }
        val name = NAMES[seedName % NAMES.size]
        val band = MercenaryBand(
            id = "merc_${bands.size + 1}",
            name = name,
            troops = size,
            hireCost = cost,
            upkeepPerSeason = upkeepOf(size)
        )
        return band to "[용병] $name ${"%,d".format(size)}명 고용 — ${"%,d".format(cost)}G, " +
            "절기 유지비 ${"%,d".format(band.upkeepPerSeason)}G (정규 상한과 별도)"
    }

    /**
     * 절기 유지비 정산.
     * @return 남은 부대 · 지급액 · 이탈 병력 · 안내
     */
    fun maintain(bands: List<MercenaryBand>, gold: Int): MercenaryMaintenance {
        if (bands.isEmpty()) return MercenaryMaintenance(bands, 0, 0, null)
        val cost = bands.sumOf { it.upkeepPerSeason }
        if (gold >= cost) {
            return MercenaryMaintenance(bands, cost, 0, "[용병] 유지비 ${"%,d".format(cost)}G 지급 — 전 부대 유지")
        }
        // 자금이 모자라면 부대별로 이탈이 발생한다
        var remainingGold = gold
        var deserted = 0
        val kept = mutableListOf<MercenaryBand>()
        bands.forEach { band ->
            if (remainingGold >= band.upkeepPerSeason) {
                remainingGold -= band.upkeepPerSeason
                kept += band
            } else {
                val lost = (band.troops * DESERT_RATIO).toInt().coerceAtLeast(1)
                deserted += lost
                val left = band.troops - lost
                if (left >= 50) kept += band.copy(troops = left, upkeepPerSeason = upkeepOf(left))
            }
        }
        return MercenaryMaintenance(
            bands = kept,
            paid = gold - remainingGold,
            deserted = deserted,
            message = "[용병] 유지비 부족 — ${"%,d".format(deserted)}명이 계약을 파기하고 떠났습니다."
        )
    }
}

data class MercenaryMaintenance(
    val bands: List<MercenaryBand>,
    val paid: Int,
    val deserted: Int,
    val message: String?
)

/**
 * 영웅 일임 — 태수로 앉힌 영웅의 능력치가 영지 운영을 대신한다.
 *
 * · 내정 → 세수·개발
 * · 상업 → 식량·교역
 * · 작전 → 징병·방어
 * · 외교 → 민심
 * · 기술 → 개발 속도
 */
object ViceroyEngine {

    /** 영웅 능력치로 계산한 절기 운영 결과 */
    fun govern(
        territory: ManagedTerritory,
        admin: Int,
        tech: Int,
        commerce: Int,
        ops: Int,
        diplomacy: Int
    ): TerritoryGovernance {
        val devGain = ((admin + tech) / 60.0).toInt().coerceIn(1, 4)
        val moraleGain = ((diplomacy - 50) / 25.0).toInt()
        val troopsGain = (territory.population * 0.004 * (ops / 100.0) * territory.status.levyRate)
            .toInt()
            .coerceAtLeast(0)
        val goldGain = ((territory.population / 500.0) * (0.4 + (admin / 200.0))).toInt()
        val foodGain = ((territory.population / 120.0) * (0.5 + (commerce / 200.0))).toInt()
        val defenseGain = ((ops - 50) / 20).coerceAtLeast(0)
        return TerritoryGovernance(
            developmentGain = devGain,
            moraleGain = moraleGain,
            troopGain = troopsGain,
            goldGain = goldGain,
            foodGain = foodGain,
            defenseGain = defenseGain
        )
    }

    /** 영웅에게 일임했을 때 절기마다 붙는 자동 운영 한 줄 */
    fun reportLine(territory: ManagedTerritory, gov: TerritoryGovernance): String =
        "[일임] ${territory.name} · ${territory.viceroyName ?: "태수"} — " +
            "개발 +${gov.developmentGain} · 민심 ${signed(gov.moraleGain)} · " +
            "징병 +${gov.troopGain} · 세수 +${gov.goldGain}G · 방어 ${signed(gov.defenseGain)}"

    private fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"
}

data class TerritoryGovernance(
    val developmentGain: Int,
    val moraleGain: Int,
    val troopGain: Int,
    val goldGain: Int,
    val foodGain: Int,
    val defenseGain: Int
)

/**
 * 영웅으로 시작한 유저가 리더에게 등용을 청한 결과.
 *
 * 수락/거절은 상대가 유저인지 AI인지와 무관하게 같은 기준
 * (영웅 능력치 · 명성 · 세력의 필요)으로만 결정된다.
 */
data class EmploymentRequest(
    val heroId: String,
    val heroName: String,
    val lordId: String,
    val lordName: String,
    val accepted: Boolean,
    val loyalty: Int,
    val message: String,
    val reasonLine: String
)

/** 영웅으로 시작한 유저의 경력 상태 */
data class HeroCareer(
    val heroId: String,
    val heroName: String,
    val employerLordId: String,
    val employerLordName: String,
    val employerTerritory: String,
    val loyalty: Int,
    val merit: Int,
    val privateTroops: Int,
    val independent: Boolean = false
) {
    val summary: String get() = buildString {
        append("$heroName · ${employerLordName} 리더 휘하")
        append(" · 충성 $loyalty")
        append(" · 공적 $merit")
        append(" · 사병 ${"%,d".format(privateTroops)}")
        if (independent) append(" · 독립")
    }
}
