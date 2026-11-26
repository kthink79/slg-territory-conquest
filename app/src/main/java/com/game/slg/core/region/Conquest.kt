package com.game.slg.core.region

import com.game.slg.core.empire.ManagedTerritory
import com.game.slg.core.empire.TerritoryStatus

/**
 * 인접 지역 무력 병합 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-26):
 *  *"인접 지역 전투에서 적 병력을 전멸(잔여 병력 0 또는 항복 조건)시키면 즉시 직할 병합(DIRECT) /
 *    속국 편입(VASSAL) / 자치령(AUTONOMOUS) 중 선택해 ManagedTerritory로 편입한다.
 *    해당 지역 리더·영웅도 흡수·처분 대기로 이관한다."*
 *
 * 전투 계산은 ui 계층이 하고, 이 엔진은 (1) 편입 제안 판정, (2) 선택한 형태의 편입 결과만 만든다.
 * 리더·영웅 이관 목록은 화면이 로스터에서 채워 넣는다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 전멸·항복 뒤에 뜨는 편입 선택지 */
data class ConquestOffer(
    val leaderId: String,
    val leaderName: String,
    val stronghold: String,
    val regionCode: String,
    val regionName: String,
    /** 교전 전 병력 */
    val troopsBefore: Int,
    /** 이번 교전으로 잃은 적 병력 */
    val losses: Int,
    val remainingTroops: Int,
    val ourTroops: Int,
    val population: Int,
    val gold: Int,
    val food: Int,
    /** 항복 조건으로 열린 제안인경우 true (전멸이면 false) */
    val surrender: Boolean
) {
    val reasonLabel: String get() = if (surrender) "항복" else "전멸"

    val headline: String
        get() = "$leaderName 리더 $reasonLabel — ${regionName} 편입 방식을 고르세요"

    val populationLabel: String get() = "👥 ${"%,d".format(population)}"

    fun optionLabel(mode: TerritoryStatus): String = when (mode) {
        TerritoryStatus.DIRECT -> "직할 병합"
        TerritoryStatus.VASSAL -> "속국 편입"
        TerritoryStatus.AUTONOMOUS -> "자치령"
    }

    fun optionEffect(mode: TerritoryStatus): String = when (mode) {
        TerritoryStatus.DIRECT -> "세수 100% · 징병 100% · 민심 낮음"
        TerritoryStatus.VASSAL -> "조공 + 징병 70% · 자치 유지"
        TerritoryStatus.AUTONOMOUS -> "조공만 · 징병 30% · 우호 유지"
    }
}

/** 무력 병합·외교 편입으로 우리 연합에 들어온 인접 리더 1명 — 저장·지도 표시용 */
data class ConquestRecord(
    val leaderId: String,
    val leaderName: String,
    val regionCode: String,
    val regionName: String,
    val status: TerritoryStatus,
    val population: Int
) {
    /** 저장용 — "leaderId|regionCode|status|leaderName|population|regionName" */
    fun encode(): String = listOf(
        leaderId, regionCode, status.name, leaderName, population.toString(), regionName
    ).joinToString("|")

    companion object {
        fun decode(raw: String): ConquestRecord? {
            val p = raw.split('|')
            val id = p.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
            val status = TerritoryStatus.entries.firstOrNull { it.name == p.getOrNull(2) } ?: return null
            return ConquestRecord(
                leaderId = id,
                leaderName = p.getOrNull(3)?.ifBlank { "리더" } ?: "리더",
                regionCode = p.getOrNull(1) ?: "",
                regionName = p.getOrNull(5)?.ifBlank { p.getOrNull(1) ?: "" } ?: "",
                status = status,
                population = p.getOrNull(4)?.toIntOrNull() ?: 0
            )
        }
    }
}

/** 편입 결과 */
data class ConquestResult(
    val success: Boolean,
    val message: String,
    val status: TerritoryStatus,
    val territory: ManagedTerritory? = null,
    val goldDelta: Int = 0,
    val prestigeDelta: Int = 0,
    val moraleDelta: Int = 0
)

object ConquestEngine {

    /** 속국 조공 기준값 — 인구 3,000명당 1G, 최소 20G */
    fun baseTribute(population: Int): Int = (population / 3_000).coerceAtLeast(20)

    /** 자치령 조공 기준값 */
    fun autonomyTribute(population: Int): Int = (population / 5_000).coerceAtLeast(10)

    /** 편입 영지의 고유 코드 — 같은 지역의 여러 리더를 각각 관리한다 */
    fun territoryCode(regionCode: String, leaderId: String): String = "$regionCode:$leaderId"

    /**
     * 교전 결과로 편입 제안을 만들지 판정한다.
     *
     * · 잔여 병력 0 → 전멸 편입
     * · 이겼고 우리 병력이 상대의 2배 이상이며 잔여가 15% 이하 → 항복 편입
     */
    fun offerAfterBattle(
        leaderId: String,
        leaderName: String,
        stronghold: String,
        regionCode: String,
        regionName: String,
        troopsBefore: Int,
        losses: Int,
        ourTroops: Int,
        won: Boolean,
        population: Int,
        gold: Int,
        food: Int
    ): ConquestOffer? {
        if (troopsBefore <= 0) return null
        val remaining = (troopsBefore - losses.coerceAtLeast(0)).coerceAtLeast(0)
        val surrender = won && remaining > 0 &&
            ourTroops >= troopsBefore * 2 && remaining <= troopsBefore * 15 / 100
        if (remaining > 0 && !surrender) return null
        return ConquestOffer(
            leaderId = leaderId,
            leaderName = leaderName,
            stronghold = stronghold,
            regionCode = regionCode,
            regionName = regionName,
            troopsBefore = troopsBefore,
            losses = losses,
            remainingTroops = remaining,
            ourTroops = ourTroops,
            population = population,
            gold = gold,
            food = food,
            surrender = remaining > 0
        )
    }

    /** 병력이 이미 0인 리더를 다시 칠 때 — 바로 편입 선택으로 */
    fun offerForEmptyGarrison(
        leaderId: String,
        leaderName: String,
        stronghold: String,
        regionCode: String,
        regionName: String,
        population: Int,
        gold: Int,
        food: Int,
        ourTroops: Int
    ): ConquestOffer = ConquestOffer(
        leaderId = leaderId,
        leaderName = leaderName,
        stronghold = stronghold,
        regionCode = regionCode,
        regionName = regionName,
        troopsBefore = 0,
        losses = 0,
        remainingTroops = 0,
        ourTroops = ourTroops,
        population = population,
        gold = gold,
        food = food,
        surrender = false
    )

    /**
     * 편입 실행 — 고른 형태로 ManagedTerritory를 만든다.
     *
     * @param casusBelli 가결된 의회 전쟁 안건의 명분 (전리품 보너스)
     */
    fun resolve(
        offer: ConquestOffer,
        mode: TerritoryStatus,
        casusBelli: CasusBelli = CasusBelli.NONE
    ): ConquestResult {
        val loot = (offer.gold / 2 * (100 + casusBelli.lootBonusPercent) / 100)
        val territory = when (mode) {
            TerritoryStatus.DIRECT -> ManagedTerritory(
                code = territoryCode(offer.regionCode, offer.leaderId),
                name = "${offer.regionName} · ${offer.leaderName} 거점",
                population = offer.population,
                status = TerritoryStatus.DIRECT,
                morale = 35,
                development = 25,
                troops = 0,
                food = offer.food / 3,
                gold = offer.gold / 3,
                defense = 45,
                absorbedBy = "무력 병합"
            )
            TerritoryStatus.VASSAL -> ManagedTerritory(
                code = territoryCode(offer.regionCode, offer.leaderId),
                name = "${offer.regionName} · ${offer.leaderName} 속국",
                population = offer.population,
                status = TerritoryStatus.VASSAL,
                morale = 52,
                development = 25,
                troops = 0,
                food = offer.food / 4,
                gold = offer.gold / 4,
                defense = 30,
                tributePerSeason = baseTribute(offer.population),
                absorbedBy = "무력 병합 · 속국"
            )
            TerritoryStatus.AUTONOMOUS -> ManagedTerritory(
                code = territoryCode(offer.regionCode, offer.leaderId),
                name = "${offer.regionName} · ${offer.leaderName} 자치령",
                population = offer.population,
                status = TerritoryStatus.AUTONOMOUS,
                morale = 66,
                development = 28,
                troops = 0,
                food = offer.food / 4,
                gold = offer.gold / 4,
                defense = 22,
                tributePerSeason = autonomyTribute(offer.population),
                absorbedBy = "무력 병합 · 자치령"
            )
        }
        val modeLabel = when (mode) {
            TerritoryStatus.DIRECT -> "직할 병합"
            TerritoryStatus.VASSAL -> "속국 편입"
            TerritoryStatus.AUTONOMOUS -> "자치령 편입"
        }
        val prestige = when (mode) {
            TerritoryStatus.DIRECT -> 7
            TerritoryStatus.VASSAL -> 5
            TerritoryStatus.AUTONOMOUS -> 3
        }
        val morale = when (mode) {
            TerritoryStatus.DIRECT -> -2
            TerritoryStatus.VASSAL -> 0
            TerritoryStatus.AUTONOMOUS -> 2
        }
        return ConquestResult(
            success = true,
            message = "[무력 병합] ${offer.leaderName} 리더 ${offer.reasonLabel} → " +
                "${offer.regionName} $modeLabel · 인구 ${"%,d".format(offer.population)}명 · " +
                "노획 ${"%,d".format(loot)}G · 명성 +$prestige" +
                if (casusBelli.isEmpty) "" else " · 의회 명분 ${casusBelli.label}",
            status = mode,
            territory = territory,
            goldDelta = loot,
            prestigeDelta = prestige,
            moraleDelta = morale
        )
    }

    /** 외교 속국 조공 조약이 성사됐을 때 실제 속국 영지로 편입 */
    fun vassalFromTreaty(
        regionCode: String,
        regionName: String,
        leaderId: String,
        leaderName: String,
        population: Int
    ): ManagedTerritory = ManagedTerritory(
        code = territoryCode(regionCode, leaderId),
        name = "$regionName · $leaderName 속국",
        population = population,
        status = TerritoryStatus.VASSAL,
        morale = 58,
        development = 26,
        troops = 0,
        defense = 26,
        tributePerSeason = baseTribute(population),
        absorbedBy = "속국 조공 조약"
    )
}
