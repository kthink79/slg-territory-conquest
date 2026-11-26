package com.game.slg.core.empire

import com.game.slg.core.hero.HeroStats

/**
 * 영토 운영 엔진 — 편입 영지의 절기 정산.
 *
 * · 태수(영웅)를 앉힌 영지는 영웅 능력치대로 자동 개발·민심·세수·징병·방어가 돌아간다.
 * · 태수가 없는 영지는 세수가 60%로 줄고 민심이 조금씩 흘러내린다(직접 통치로 보정).
 * · 속국·자치령의 조공이 절기마다 들어온다.
 * · 정규 병력은 편입 영지 전체 인구의 5%를 넘지 못한다.
 */
data class EmpireSeasonResult(
    val territories: List<ManagedTerritory>,
    val factions: List<Faction>,
    val goldGain: Int,
    val foodGain: Int,
    val troopGain: Int,
    val population: Int,
    val lines: List<String>
)

object EmpireEngine {

    /** 태수 없이 방치된 영지의 세수 계수 */
    const val NEGLECTED_TAX_RATE = 0.60

    /**
     * 절기 1회 정산.
     *
     * @param heroStats 영웅 id → 능력치 (일임한 태수의 능력치)
     */
    fun processSeason(
        territories: List<ManagedTerritory>,
        factions: List<Faction>,
        regularTroops: Int,
        seasonName: String,
        heroStats: Map<String, HeroStats>
    ): EmpireSeasonResult {
        var gold = 0
        var food = 0
        var levies = 0
        var population = 0
        val lines = mutableListOf<String>()

        val cap = ManpowerEngine.capOf(territories.sumOf { it.population })
        var room = (cap - regularTroops).coerceAtLeast(0)

        val updated = territories.map { territory ->
            population += territory.population
            // 통치 정책(세율·치안·징병·시설)이 세수·식량·징병·민심에 먼저 작용한다
            val policySeason = GovernanceEngine.season(territory)
            val stats = territory.viceroyHeroId?.let { heroStats[it] }
            if (stats != null) {
                val gov = ViceroyEngine.govern(
                    territory = territory,
                    admin = stats.admin,
                    tech = stats.tech,
                    commerce = stats.commerce,
                    ops = stats.ops,
                    diplomacy = stats.diplomacy
                )
                val gained = (gov.troopGain * policySeason.levyRatio).toInt().coerceAtMost(room)
                room -= gained
                gold += gov.goldGain + territory.taxIncome()
                food += gov.foodGain + territory.foodIncome()
                levies += gained
                lines += ViceroyEngine.reportLine(territory, gov)
                lines += policySeason.line
                territory.copy(
                    development = (territory.development + gov.developmentGain).coerceIn(0, 100),
                    morale = (territory.morale + gov.moraleGain + policySeason.moraleDelta)
                        .coerceIn(0, 100),
                    defense = (territory.defense + gov.defenseGain).coerceIn(0, 100),
                    troops = (territory.troops + gained - policySeason.troopLoss)
                        .coerceAtLeast(0).coerceAtMost(territory.troopCap),
                    food = territory.food + gov.foodGain,
                    gold = territory.gold + gov.goldGain,
                    policy = territory.policy.copy(
                        security = (territory.policy.security + policySeason.securityDelta)
                            .coerceIn(0, 100)
                    )
                )
            } else {
                val tax = (territory.taxIncome() * NEGLECTED_TAX_RATE).toInt()
                gold += tax
                food += (territory.foodIncome() * NEGLECTED_TAX_RATE).toInt()
                lines += "[미일임] ${territory.name} — 태수 없이 방치되어 세수 ${"%,d".format(tax)}G, 민심 -1"
                lines += policySeason.line
                territory.copy(
                    morale = (territory.morale - 1 + policySeason.moraleDelta).coerceIn(0, 100),
                    defense = (territory.defense - 1).coerceIn(0, 100),
                    troops = (territory.troops - policySeason.troopLoss).coerceAtLeast(0),
                    policy = territory.policy.copy(
                        security = (territory.policy.security + policySeason.securityDelta)
                            .coerceIn(0, 100)
                    )
                )
            }
        }

        // 속국·자치령 조공
        val tribute = factions.filter { it.incorporated && it.tributePerSeason > 0 }
            .sumOf { it.tributePerSeason }
        if (tribute > 0) {
            gold += tribute
            lines += "[조공] 속국·자치령 ${factions.count { it.incorporated }}곳 — +${"%,d".format(tribute)}G"
        }

        lines.add(0, "[영토] $seasonName 정산 — 영지 ${territories.size}곳 · 인구 ${"%,d".format(population)}명 · " +
            "정규 상한 ${"%,d".format(cap)}명")

        return EmpireSeasonResult(
            territories = updated,
            factions = factions,
            goldGain = gold,
            foodGain = food,
            troopGain = levies,
            population = population,
            lines = lines
        )
    }

    /**
     * 편입 영지의 총력 — 속국·자치령이 많을수록 강해진다.
     * 작전 전투력에 더해지는 보너스(%)로 쓴다.
     */
    fun coalitionBonusPercent(territories: List<ManagedTerritory>): Int {
        val vassals = territories.count { it.status != TerritoryStatus.DIRECT }
        val land = territories.sumOf { it.development } / 50
        return (vassals * 4 + land).coerceIn(0, 40)
    }

    /** 전체 영지의 절기 세수 예상 */
    fun projectedTax(territories: List<ManagedTerritory>, factions: List<Faction>): Int =
        territories.sumOf { it.taxIncome() } +
            factions.filter { it.incorporated }.sumOf { it.tributePerSeason }
}
