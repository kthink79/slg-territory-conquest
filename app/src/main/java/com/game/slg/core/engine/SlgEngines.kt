package com.game.slg.core.engine

import com.game.slg.core.model.*

/**
 * Clean Architecture Domain Contracts
 */
interface CouncilEngineContract {
    fun voteOnBill(bill: CouncilBill, lords: List<Lord>): CouncilBill
}

interface InfiltrationEngineContract {
    fun accumulatePower(talent: Talent, recruitedTroops: Int, subvertProgress: Int)
    fun attemptCoup(talent: Talent, employerLord: Lord): CoupResult
}

interface CombatEngineContract {
    fun resolveSiege(
        attackers: Int,
        defenders: Int,
        territory: Territory,
        isSupplyLineCut: Boolean
    ): SiegeResult
}

/**
 * 성주-호족 의회 및 전시 병력 차출·반란 엔진
 */
class CouncilEngine : CouncilEngineContract {

    /**
     * 성주가 지역 정책(병력 차출 등)을 호족 의회에 상정
     */
    override fun voteOnBill(bill: CouncilBill, lords: List<Lord>): CouncilBill {
        var favor = 0
        var against = 0

        for (lord in lords) {
            // 호족들의 병력 규모와 성향에 따른 투표 시뮬레이션
            val acceptsLevy = when (bill.type) {
                BillType.TROOP_LEVY -> {
                    // 병력의 30% 초과 차출 요구 시 반발
                    val levyRatio = bill.troopLevyAmount.toDouble() / (lord.troops.coerceAtLeast(1))
                    levyRatio < 0.35
                }
                BillType.SPECIAL_TAX -> lord.grain > 1000
                BillType.FORTRESS_EXPAND -> true
                BillType.DECLARE_WAR -> lord.troops > 2000
            }

            if (acceptsLevy) favor++ else against++
        }

        bill.votesInFavor = favor
        bill.votesAgainst = against
        bill.isPassed = favor > against

        // 반대표가 65% 이상일 경우 다른 호족들과 연합해 성주에 반란을 일으킬 수 있음 (호족이 없을 경우 방지)
        if (lords.isNotEmpty() && (against.toDouble() / lords.size >= 0.65)) {
            bill.isRebellionTriggered = true
        } else {
            bill.isRebellionTriggered = false
        }

        return bill
    }
}

/**
 * 삼국지식 인재 탐색·등용 및 '재야 인재 위장 잠입 스타팅' (하극상) 엔진
 */
class InfiltrationEngine : InfiltrationEngineContract {

    /**
     * 유저가 호족이 아닌 재야 인재로 시작했을 때 조용히 사병을 모으고 암약
     */
    override fun accumulatePower(talent: Talent, recruitedTroops: Int, subvertProgress: Int) {
        require(talent.isPlayerHiddenTalent) { "일반 인재는 암약 시스템을 독립 실행할 수 없습니다." }
        require(recruitedTroops >= 0) { "모병 수는 음수일 수 없습니다." }
        require(subvertProgress >= 0) { "암약 진척도는 음수일 수 없습니다." }
        talent.privateTroops += recruitedTroops
        talent.rebellionPrestige += subvertProgress
        // 고용주에 대한 충성도 위장 감소 (0~100 범위 유지)
        talent.loyalty = (talent.loyalty - (subvertProgress / 2)).coerceIn(0, 100)
    }

    /**
     * 고용주 호족 세력 취득 (하극상/찬탈) 결행
     * 사병 수와 지지도가 고용주의 병력을 압도할 때 성공
     */
    override fun attemptCoup(talent: Talent, employerLord: Lord): CoupResult {
        if (!talent.isPlayerHiddenTalent) {
            return CoupResult(success = false, message = "위장 잠입 유저가 아닙니다.")
        }

        val powerRatio = talent.privateTroops.toDouble() / employerLord.troops.coerceAtLeast(1)
        val success = powerRatio >= 0.8 && talent.rebellionPrestige >= 50

        return if (success) {
            // 하극상 성공: 고용주의 영지와 지위를 찬탈하고 새 호족으로 등극
            employerLord.troops = talent.privateTroops
            CoupResult(
                success = true,
                message = "${talent.name} 님이 주군 ${employerLord.name}을 축출하고 세력을 완벽히 찬탈했습니다!",
                newLordPrestige = talent.rebellionPrestige + 100
            )
        } else {
            CoupResult(
                success = false,
                message = "하극상 모의가 발각되어 실패했습니다. 토벌 위험에 직면합니다."
            )
        }
    }
}

data class CoupResult(
    val success: Boolean,
    val message: String,
    val newLordPrestige: Int = 0
)

/**
 * 방어 요새 수성 우위 & GIS 지형 단판 대회전 엔진
 */
class CombatEngine : CombatEngineContract {

    /**
     * 공성전 및 요새 수성 전투 시뮬레이션
     * - 방어 시설(목책·석성·산성) 구축 시 수성군 사상자 최대 80% 격감
     * - 보급선 단절 시 공격군 사기 저하 및 무혈 항복 유도
     */
    override fun resolveSiege(
        attackers: Int,
        defenders: Int,
        territory: Territory,
        isSupplyLineCut: Boolean
    ): SiegeResult {
        // 보급선이 끊긴 공격군은 병력 손실 없이 와해 또는 무혈 항복
        if (isSupplyLineCut) {
            return SiegeResult(
                defenderWon = true,
                bloodlessVictory = true,
                attackerLosses = (attackers * 0.4).toInt(),
                defenderLosses = 0,
                message = "적군의 보급로를 완벽히 차단하여 무혈 항복을 받아냈습니다!"
            )
        }

        val fortress = territory.fortress
        val reductionRatio = fortress?.casualtyReductionRatio ?: 0.0
        val terrainBonus = territory.terrain.defenseBonus

        // 수성군 실효 전투력 (요새 + 지형 보너스 적용)
        val effectiveDefenderPower = defenders * terrainBonus * (1.0 + (fortress?.durability?.toDouble() ?: 0.0) / 1000.0)
        val effectiveAttackerPower = attackers.toDouble()

        val defenderWon = effectiveDefenderPower >= effectiveAttackerPower

        val rawDefenderLosses = Math.round(attackers * 0.25).toInt()
        val actualDefenderLosses = Math.round(rawDefenderLosses * (1.0 - reductionRatio)).toInt().coerceIn(0, defenders)
        val attackerLosses = Math.round(defenders * terrainBonus * 0.7).toInt().coerceIn(0, attackers)

        // 다중 턴 공성전 시 요새 내구도 감소 반영 (요새가 있고 공격자가 있을 경우)
        if (fortress != null && fortress.durability > 0 && attackers > 0) {
            val siegeDamage = (attackers * 0.1).toInt().coerceAtLeast(10)
            fortress.takeDamage(siegeDamage)
        }

        return SiegeResult(
            defenderWon = defenderWon,
            bloodlessVictory = false,
            attackerLosses = attackerLosses,
            defenderLosses = actualDefenderLosses,
            message = if (defenderWon) {
                "요새 방어망과 지형지물을 활용해 적의 대공세를 격퇴했습니다! (사상자 ${(reductionRatio * 100).toInt()}% 방어 격감)"
            } else {
                "성벽이 돌파되어 거점을 상실했습니다."
            }
        )
    }
}

data class SiegeResult(
    val defenderWon: Boolean,
    val bloodlessVictory: Boolean,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val message: String
)
