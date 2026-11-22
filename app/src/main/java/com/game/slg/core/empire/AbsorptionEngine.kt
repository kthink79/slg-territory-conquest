package com.game.slg.core.empire

/**
 * 흡수·편입 엔진 — 같은 지역의 다른 리더 세력을 우리 세력으로 묶는 6가지 길.
 *
 * 사용자 요청: *"같은 지역 내 타 리더 세력 흡수·편입 다각화 — ① 전투 전멸 흡수,
 * ② 첩자 민심 전환 속국, ③ 무력 압박 조공, ④ 연방 결성, ⑤ 경제 종속 편입,
 * ⑥ 친선 자치령 합류"*
 *
 * 여섯 방법은 서로 다른 자원(병력·첩보·자금·식량·외교 영웅)을 요구하고,
 * 성공했을 때 편입 형태(직할·속국·자치령)와 효과가 다르다.
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */
data class EmpireContext(
    val troops: Int,
    val prestige: Int,
    val gold: Int,
    val food: Int,
    val morale: Int,
    val diplomacy: Int,
    val espionagePower: Int,
    /** 배치된 외교 영웅의 설득력 보너스 */
    val diplomacyHeroBonus: Int = 0,
    /** 배치된 작전 영웅의 전투 보너스 */
    val opsHeroBonus: Int = 0,
    val seasonName: String = "봄",
    val regionName: String = ""
)

object AbsorptionEngine {

    /** 원조 1회 비용 */
    const val AID_GOLD = 800
    const val AID_FOOD = 1_200

    /** 첩자 공작 비용 */
    const val SUBVERSION_GOLD = 400

    /** 친선 파견 비용 */
    const val GOODWILL_GOLD = 300

    /** 지금 이 방법을 쓸 수 있는지 — null 이면 가능, 아니면 사유 */
    fun availability(
        method: AbsorptionMethod,
        faction: Faction,
        ctx: EmpireContext
    ): String? {
        if (faction.incorporated) return "${faction.name}은(는) 이미 우리 연방에 편입됐습니다."
        return when (method) {
            AbsorptionMethod.CONQUEST -> when {
                faction.troops <= 0 -> null
                ctx.troops < (faction.troops * 1.05).toInt() ->
                    "병력 부족 — 상대 ${"%,d".format(faction.troops)}명의 105%인 " +
                        "${"%,d".format((faction.troops * 1.05).toInt())}명이 필요합니다. (보유 ${"%,d".format(ctx.troops)})"
                else -> null
            }

            AbsorptionMethod.SUBVERSION -> when {
                ctx.espionagePower < 50 -> "첩보력 부족 — 50 이상 필요 (현재 ${ctx.espionagePower})"
                faction.morale > 40 -> "상대 민심 ${faction.morale} — 40 이하로 떨어져야 첩자가 먹힙니다."
                ctx.gold < SUBVERSION_GOLD -> "자금 부족 — ${SUBVERSION_GOLD}G 필요"
                else -> null
            }

            AbsorptionMethod.TRIBUTE_PRESSURE -> when {
                faction.relation == FactionRelation.VASSAL -> "이미 조공을 바치는 속국입니다."
                ctx.troops < (faction.troops * 1.6).toInt() ->
                    "압박 부족 — 상대의 160%인 ${"%,d".format((faction.troops * 1.6).toInt())}명이 필요합니다."
                else -> null
            }

            AbsorptionMethod.FEDERATION -> when {
                faction.affinity < 60 -> "우호도 부족 — 60 이상 필요 (현재 ${faction.affinity})"
                !faction.commonEnemy -> "공동 적이 없습니다 — 먼저 같은 적을 만들어야 연방이 결성됩니다."
                else -> null
            }

            AbsorptionMethod.ECONOMIC_DEPENDENCE -> when {
                ctx.gold < AID_GOLD -> "자금 부족 — 원조 1회에 ${"%,d".format(AID_GOLD)}G 필요"
                ctx.food < AID_FOOD -> "식량 부족 — 원조 1회에 ${"%,d".format(AID_FOOD)}석 필요"
                else -> null
            }

            AbsorptionMethod.GOODWILL_AUTONOMY -> when {
                (ctx.diplomacyHeroBonus < 8 && ctx.diplomacy < 60) ->
                    "외교 역량 부족 — 외교 영웅(설득력 +8) 배치 또는 외교 60 이상 필요"
                faction.affinity < 45 -> "우호도 부족 — 45 이상 필요 (현재 ${faction.affinity})"
                ctx.gold < GOODWILL_GOLD -> "자금 부족 — ${GOODWILL_GOLD}G 필요"
                else -> null
            }
        }
    }

    /** 모든 방법의 가능 여부 — 화면에 6줄로 보인다 */
    fun board(faction: Faction, ctx: EmpireContext): List<Pair<AbsorptionMethod, String?>> =
        AbsorptionMethod.entries.map { it to availability(it, faction, ctx) }

    /** 흡수 실행 — 성공하면 편입 영지를 만들어 돌려준다 */
    fun resolve(
        method: AbsorptionMethod,
        faction: Faction,
        ctx: EmpireContext
    ): AbsorptionOutcome {
        availability(method, faction, ctx)?.let { reason ->
            return AbsorptionOutcome(
                success = false,
                method = method,
                message = "[${method.label}] 불가 — $reason",
                faction = faction
            )
        }
        val outcome = when (method) {
            AbsorptionMethod.CONQUEST -> conquest(faction, ctx)
            AbsorptionMethod.SUBVERSION -> subversion(faction, ctx)
            AbsorptionMethod.TRIBUTE_PRESSURE -> tributePressure(faction, ctx)
            AbsorptionMethod.FEDERATION -> federation(faction, ctx)
            AbsorptionMethod.ECONOMIC_DEPENDENCE -> economicAid(faction, ctx)
            AbsorptionMethod.GOODWILL_AUTONOMY -> goodwill(faction, ctx)
        }
        // 실제로 편입된 경우에만 소속 영웅이 처분 대기로 넘어간다 (경제 원조 1회차 같은 중간 단계 제외)
        return if (outcome.absorbed && outcome.capturedHeroes.isEmpty()) {
            outcome.copy(capturedHeroes = faction.heroIds)
        } else outcome
    }

    /**
     * 강제 편입 — 같은 지역 리더와 교전해 병력을 전부 소진시켰을 때, 추가 손실 없이 바로 편입한다.
     *
     * 교전에서 이미 아군 손실을 치렀으므로 전투 흡수의 12% 를 다시 물리지 않는다.
     * 대상 세력은 0병력·편입 완료로 정리되고, 직할 영지가 새로 생긴다.
     */
    fun forceConquest(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        if (faction.incorporated) {
            return AbsorptionOutcome(
                success = false,
                method = AbsorptionMethod.CONQUEST,
                message = "[강제 편입] ${faction.name}은(는) 이미 우리 연방에 편입됐습니다.",
                faction = faction
            )
        }
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.DIRECT,
            morale = 30,
            development = 20,
            troops = 0,
            absorbedBy = "교전 강제 편입"
        )
        val loot = faction.gold / 2
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.CONQUEST,
            message = "[강제 편입] ${faction.name} 리더의 병력을 전멸시켰습니다. " +
                "${faction.territoryName}이(가) 직할로 넘어왔습니다 — 추가 손실 없음 · " +
                "노획 ${"%,d".format(loot)}G · 명성 +6 (소속 영웅 ${faction.heroIds.size}명)",
            faction = faction.copy(
                troops = 0,
                incorporated = true,
                absorbedBy = "교전 강제 편입",
                relation = FactionRelation.VASSAL
            ),
            goldDelta = loot,
            prestigeDelta = 6,
            moraleDelta = -2,
            territory = territory,
            troopLoss = 0,
            lootLabel = "노획 ${"%,d".format(loot)}G",
            capturedHeroes = faction.heroIds
        )
    }

    private fun conquest(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        val losses = (ctx.troops * 0.12).toInt().coerceAtLeast(1)
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.DIRECT,
            morale = 35,
            development = 25,
            troops = faction.troops / 5,
            absorbedBy = AbsorptionMethod.CONQUEST.label
        )
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.CONQUEST,
            message = "[전투 흡수] ${faction.name} 세력을 전멸시켜 ${faction.territoryName}을(를) 직할로 편입했습니다. " +
                "아군 사상자 ${"%,d".format(losses)}명 · 노획 ${"%,d".format(faction.gold / 2)}G · 명성 +6",
            faction = faction.copy(
                troops = 0,
                incorporated = true,
                absorbedBy = AbsorptionMethod.CONQUEST.label,
                relation = FactionRelation.VASSAL
            ),
            goldDelta = faction.gold / 2,
            prestigeDelta = 6,
            moraleDelta = -2,
            territory = territory,
            troopLoss = losses,
            lootLabel = "노획 ${"%,d".format(faction.gold / 2)}G"
        )
    }

    private fun subversion(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        val tribute = (faction.population / 3_000).coerceAtLeast(20)
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.VASSAL,
            morale = 48,
            development = 20,
            troops = faction.troops / 4,
            absorbedBy = AbsorptionMethod.SUBVERSION.label,
            tribute = tribute
        )
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.SUBVERSION,
            message = "[첩자 민심 전환] ${faction.name} 거점의 민심이 돌아서 ${faction.territoryName}이(가) " +
                "속국으로 편입됐습니다. 절기 조공 +${"%,d".format(tribute)}G",
            faction = faction.copy(
                incorporated = true,
                relation = FactionRelation.VASSAL,
                absorbedBy = AbsorptionMethod.SUBVERSION.label,
                tributePerSeason = tribute
            ),
            goldDelta = -SUBVERSION_GOLD,
            prestigeDelta = 4,
            territory = territory,
            lootLabel = "조공 +${"%,d".format(tribute)}G/절기"
        )
    }

    private fun tributePressure(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        val tribute = (faction.gold / 3).coerceAtLeast(60)
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.VASSAL,
            morale = 45,
            development = 30,
            troops = faction.troops / 3,
            absorbedBy = AbsorptionMethod.TRIBUTE_PRESSURE.label,
            tribute = tribute
        )
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.TRIBUTE_PRESSURE,
            message = "[무력 압박] ${"%,d".format(ctx.troops)}명의 병력으로 ${faction.name}을(를) 압박해 " +
                "속국으로 묶었습니다. 절기 조공 +${"%,d".format(tribute)}G · 즉시 조공 300G",
            faction = faction.copy(
                incorporated = true,
                relation = FactionRelation.VASSAL,
                absorbedBy = AbsorptionMethod.TRIBUTE_PRESSURE.label,
                tributePerSeason = tribute
            ),
            goldDelta = 300,
            prestigeDelta = 3,
            territory = territory,
            lootLabel = "조공 +${"%,d".format(tribute)}G/절기"
        )
    }

    private fun federation(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.AUTONOMOUS,
            morale = 62,
            development = 35,
            troops = faction.troops / 2,
            absorbedBy = AbsorptionMethod.FEDERATION.label,
            tribute = 0
        )
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.FEDERATION,
            message = "[연방 결성] 공동 적을 앞세워 ${faction.name}과(와) 연방을 결성하고 맹주 지위를 얻었습니다. " +
                "연방 병력 ${"%,d".format(faction.troops / 2)}명이 우리 작전에 함께 섭니다.",
            faction = faction.copy(
                incorporated = true,
                relation = FactionRelation.ALLIED,
                absorbedBy = AbsorptionMethod.FEDERATION.label
            ),
            prestigeDelta = 8,
            moraleDelta = 3,
            territory = territory,
            lootLabel = "연방 병력 +${"%,d".format(faction.troops / 2)}명"
        )
    }

    private fun economicAid(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        val nextAid = faction.aidCount + 1
        if (nextAid < 2) {
            return AbsorptionOutcome(
                success = true,
                method = AbsorptionMethod.ECONOMIC_DEPENDENCE,
                message = "[경제 원조] ${faction.name}에 자금 ${"%,d".format(AID_GOLD)}G · 식량 " +
                    "${"%,d".format(AID_FOOD)}석을 보냈습니다. 우호도가 올랐습니다 " +
                    "(${nextAid}/2 — 한 번 더 원조하면 경제 종속으로 편입됩니다).",
                faction = faction.copy(
                    aidCount = nextAid,
                    affinity = (faction.affinity + 12).coerceAtMost(100),
                    relation = if (faction.affinity + 12 >= 60) FactionRelation.FRIENDLY else faction.relation
                ),
                goldDelta = -AID_GOLD,
                foodDelta = -AID_FOOD,
                lootLabel = "우호 +12"
            )
        }
        val tribute = (faction.population / 2_500).coerceAtLeast(30)
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.VASSAL,
            morale = 58,
            development = 28,
            troops = faction.troops / 3,
            absorbedBy = AbsorptionMethod.ECONOMIC_DEPENDENCE.label,
            tribute = tribute
        )
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.ECONOMIC_DEPENDENCE,
            message = "[경제 종속] 두 번의 원조로 ${faction.name}이(가) 우리 교역망에 묶였습니다. " +
                "${faction.territoryName}이(가) 속국으로 편입 — 절기 조공 +${"%,d".format(tribute)}G",
            faction = faction.copy(
                aidCount = nextAid,
                incorporated = true,
                relation = FactionRelation.VASSAL,
                absorbedBy = AbsorptionMethod.ECONOMIC_DEPENDENCE.label,
                tributePerSeason = tribute
            ),
            goldDelta = -AID_GOLD,
            foodDelta = -AID_FOOD,
            prestigeDelta = 5,
            territory = territory,
            lootLabel = "조공 +${"%,d".format(tribute)}G/절기"
        )
    }

    private fun goodwill(faction: Faction, ctx: EmpireContext): AbsorptionOutcome {
        val territory = territoryOf(
            faction = faction,
            status = TerritoryStatus.AUTONOMOUS,
            morale = 66,
            development = 32,
            troops = faction.troops / 4,
            absorbedBy = AbsorptionMethod.GOODWILL_AUTONOMY.label,
            tribute = (faction.population / 5_000).coerceAtLeast(10)
        )
        return AbsorptionOutcome(
            success = true,
            method = AbsorptionMethod.GOODWILL_AUTONOMY,
            message = "[친선 자치령] 외교 영웅을 파견해 자치를 보장했습니다. ${faction.name}이(가) " +
                "${faction.territoryName} 자치령으로 합류했습니다.",
            faction = faction.copy(
                incorporated = true,
                relation = FactionRelation.AUTONOMOUS,
                absorbedBy = AbsorptionMethod.GOODWILL_AUTONOMY.label,
                tributePerSeason = territory.tributePerSeason
            ),
            goldDelta = -GOODWILL_GOLD,
            prestigeDelta = 4,
            moraleDelta = 2,
            territory = territory,
            lootLabel = "자치령 합류"
        )
    }

    private fun territoryOf(
        faction: Faction,
        status: TerritoryStatus,
        morale: Int,
        development: Int,
        troops: Int,
        absorbedBy: String,
        tribute: Int = 0
    ): ManagedTerritory = ManagedTerritory(
        code = faction.territoryCode,
        name = faction.territoryName,
        population = faction.population,
        status = status,
        morale = morale,
        development = development,
        troops = troops,
        food = faction.food / 3,
        gold = faction.gold / 3,
        defense = if (status == TerritoryStatus.DIRECT) 40 else 25,
        tributePerSeason = tribute,
        absorbedBy = absorbedBy
    )
}
