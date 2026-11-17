package com.game.slg.core.engine

import com.game.slg.core.model.CombatArm
import com.game.slg.core.model.FacilityType
import com.game.slg.core.model.ForceComposition
import com.game.slg.core.model.TerrainType
import com.game.slg.core.model.Territory

/**
 * 현대 병과 전투 결과.
 */
data class ModernBattleResult(
    val defenderWon: Boolean,
    val bloodlessVictory: Boolean,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val attackerPower: Int,
    val defenderPower: Int,
    val attackerComposition: String,
    val defenderComposition: String,
    val message: String
)

interface IModernBattleEngine {
    fun resolve(
        attackers: ForceComposition,
        defenders: ForceComposition,
        territory: Territory,
        isSupplyLineCut: Boolean = false,
        /** 시설(스마트 방호망 등)로 얻는 추가 방어 계수 */
        defenseBonusRatio: Double = 0.0,
        /** 시설로 얻는 사상자 감소율 */
        casualtyReductionBonus: Double = 0.0
    ): ModernBattleResult
}

/**
 * 현대 병과 전투 엔진.
 *
 * - 기갑·화력은 평야에서, 드론·특수전은 산악·하천에서 강하다.
 * - 방호 시설(벙커·통합 방호망)은 사상자를 최대 85%까지 줄인다.
 * - 보급선이 끊긴 공격군은 무혈 항복한다.
 * - 정면 대회전보다 저손실 작전을 지향한다(피해율 10% 이하).
 */
class ModernBattleEngine : IModernBattleEngine {

    override fun resolve(
        attackers: ForceComposition,
        defenders: ForceComposition,
        territory: Territory,
        isSupplyLineCut: Boolean,
        defenseBonusRatio: Double,
        casualtyReductionBonus: Double
    ): ModernBattleResult {
        val attackTroops = attackers.totalTroops
        val defenseTroops = defenders.totalTroops
        val terrain = territory.terrain

        if (isSupplyLineCut && attackTroops > 0) {
            val routed = (attackTroops * 0.4).toInt()
            return ModernBattleResult(
                defenderWon = true,
                bloodlessVictory = true,
                attackerLosses = routed,
                defenderLosses = 0,
                attackerPower = attackers.attackPower(terrain).toInt(),
                defenderPower = defenders.defensePower(terrain).toInt(),
                attackerComposition = attackers.describe(),
                defenderComposition = defenders.describe(),
                message = "물류·보급선을 끊어 적 공격군 ${routed}명이 무혈 항복했습니다. 아군 사상자 0."
            )
        }

        if (attackTroops <= 0) {
            return ModernBattleResult(
                defenderWon = true,
                bloodlessVictory = false,
                attackerLosses = 0,
                defenderLosses = 0,
                attackerPower = 0,
                defenderPower = defenders.defensePower(terrain).toInt(),
                attackerComposition = attackers.describe(),
                defenderComposition = defenders.describe(),
                message = "적 공격 부대가 편성되지 않아 접촉 없이 상황이 종료되었습니다."
            )
        }

        if (defenseTroops <= 0) {
            return ModernBattleResult(
                defenderWon = false,
                bloodlessVictory = false,
                attackerLosses = 0,
                defenderLosses = 0,
                attackerPower = attackers.attackPower(terrain).toInt(),
                defenderPower = 0,
                attackerComposition = attackers.describe(),
                defenderComposition = defenders.describe(),
                message = "방어 병력이 없어 거점이 그대로 개방되었습니다."
            )
        }

        val fortressReduction = territory.fortress?.casualtyReductionRatio ?: 0.0
        val durabilityFactor = (territory.fortress?.durability ?: 0) / 1000.0
        val facilityDefense = defenseBonusRatio.coerceIn(0.0, 0.6)

        val attackPower = attackers.attackPower(terrain)
        val defensePower = defenders.defensePower(terrain) * (1.0 + facilityDefense + durabilityFactor * 1.2)

        val totalPower = (attackPower + defensePower).coerceAtLeast(1.0)
        val defenderWon = defensePower >= attackPower

        val rawAttackerLossRatio = 0.25 * (defensePower / totalPower)
        val rawDefenderLossRatio = 0.30 * (attackPower / totalPower)
        val reduction = (fortressReduction + casualtyReductionBonus).coerceIn(0.0, 0.95)

        val attackerLosses = (attackTroops * rawAttackerLossRatio).toInt().coerceIn(0, attackTroops)
        val defenderLosses = (defenseTroops * rawDefenderLossRatio * (1.0 - reduction))
            .toInt()
            .coerceIn(0, defenseTroops)

        // 방호 시설이 있으면 공격 피해를 조금씩 입는다
        territory.fortress?.let { fortress ->
            if (fortress.durability > 0 && attackTroops > 0) {
                fortress.takeDamage((attackTroops * 0.05).toInt().coerceAtLeast(5))
            }
        }

        val message = if (defenderWon) {
            "현대 방호선과 지형 이점으로 적 공세를 막아냈습니다. " +
                "적 사상자 ${attackerLosses}명 · 아군 사상자 ${defenderLosses}명 " +
                "(방호 시설 사상자 감소 ${(reduction * 100).toInt()}%)."
        } else {
            "방호선이 돌파되어 거점을 내주었습니다. " +
                "적 사상자 ${attackerLosses}명 · 아군 사상자 ${defenderLosses}명."
        }

        return ModernBattleResult(
            defenderWon = defenderWon,
            bloodlessVictory = false,
            attackerLosses = attackerLosses,
            defenderLosses = defenderLosses,
            attackerPower = attackPower.toInt(),
            defenderPower = defensePower.toInt(),
            attackerComposition = attackers.describe(),
            defenderComposition = defenders.describe(),
            message = message
        )
    }
}

/**
 * 시설 건설 판정.
 */
data class BuildValidation(
    val canBuild: Boolean,
    val cost: Int,
    val reason: String? = null
)

interface IFacilityEngine {
    fun cost(type: FacilityType, owned: Set<FacilityType>): Int
    fun validate(type: FacilityType, gold: Int, owned: Set<FacilityType>): BuildValidation
    fun defenseBonus(owned: Set<FacilityType>): Double
    fun casualtyReduction(owned: Set<FacilityType>): Double
    fun intelBonus(owned: Set<FacilityType>): Int
    fun economyBonus(owned: Set<FacilityType>): Double
    fun moraleBonus(owned: Set<FacilityType>): Int
    fun xpBonus(owned: Set<FacilityType>): Double
    fun healRatio(owned: Set<FacilityType>): Double
    fun droneBonus(owned: Set<FacilityType>): Double
}

/**
 * 시설 엔진 — 지휘통제실·드론 격납고·방공 레이더망·물류 허브·스마트 방호망 등.
 * 시설은 1회만 건설하며, 효과는 서로 더해진다.
 */
class FacilityEngine : IFacilityEngine {

    override fun cost(type: FacilityType, owned: Set<FacilityType>): Int =
        if (owned.contains(type)) 0 else type.cost

    override fun validate(type: FacilityType, gold: Int, owned: Set<FacilityType>): BuildValidation {
        if (owned.contains(type)) {
            return BuildValidation(false, 0, "이미 건설된 시설입니다.")
        }
        if (gold < type.cost) {
            return BuildValidation(false, type.cost, "자금이 부족합니다. (필요 ${type.cost}G / 보유 ${gold}G)")
        }
        return BuildValidation(true, type.cost)
    }

    override fun defenseBonus(owned: Set<FacilityType>): Double {
        var bonus = 0.0
        if (owned.contains(FacilityType.RADAR_NETWORK)) bonus += 0.10
        if (owned.contains(FacilityType.SMART_DEFENSE_NET)) bonus += 0.08
        if (owned.contains(FacilityType.DRONE_HANGAR)) bonus += 0.05
        return bonus
    }

    override fun casualtyReduction(owned: Set<FacilityType>): Double =
        if (owned.contains(FacilityType.SMART_DEFENSE_NET)) 0.08 else 0.0

    override fun intelBonus(owned: Set<FacilityType>): Int {
        var bonus = 0
        if (owned.contains(FacilityType.RADAR_NETWORK)) bonus += 5
        if (owned.contains(FacilityType.CYBER_INTEL_OFFICE)) bonus += 10
        return bonus
    }

    override fun economyBonus(owned: Set<FacilityType>): Double =
        if (owned.contains(FacilityType.LOGISTICS_HUB)) 0.12 else 0.0

    override fun moraleBonus(owned: Set<FacilityType>): Int =
        if (owned.contains(FacilityType.CIVIL_DEFENSE_CENTER)) 6 else 0

    override fun xpBonus(owned: Set<FacilityType>): Double =
        if (owned.contains(FacilityType.COMMAND_CENTER)) 0.10 else 0.0

    override fun healRatio(owned: Set<FacilityType>): Double =
        if (owned.contains(FacilityType.FIELD_HOSPITAL)) 0.05 else 0.0

    override fun droneBonus(owned: Set<FacilityType>): Double =
        if (owned.contains(FacilityType.DRONE_HANGAR)) 0.15 else 0.0

    /** 시설로 늘어난 드론 병과 병력 (격납고: 드론 편제 +15%) */
    fun reinforced(composition: ForceComposition, owned: Set<FacilityType>): ForceComposition {
        val bonus = droneBonus(owned)
        if (bonus <= 0.0) return composition
        val drones = composition.troopsOf(CombatArm.DRONE)
        if (drones <= 0) return composition
        val extra = (drones * bonus).toInt()
        return composition.plus(ForceComposition.of(CombatArm.DRONE, extra))
    }

    /** 지형에 맞는 권장 편성 — 화면 안내용 */
    fun recommendedArm(terrain: TerrainType): CombatArm = when (terrain) {
        TerrainType.MOUNTAIN -> CombatArm.DRONE
        TerrainType.RIVER -> CombatArm.DRONE
        TerrainType.COASTAL -> CombatArm.AIR_DEFENSE
        TerrainType.PLAIN -> CombatArm.ARMOR
    }
}
