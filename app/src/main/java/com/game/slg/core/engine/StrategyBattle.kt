package com.game.slg.core.engine

import com.game.slg.core.model.ForceComposition
import com.game.slg.core.model.TerrainType
import kotlin.math.pow

/**
 * 전략 · 무기 기술 — 《단일 연방의 황혼》 전투 손실 차별화.
 *
 * 사용자 요청(2026-11-19): *"단순 병력 수 비교가 아니라 선택한 전략(진형·계략·지형 상성)과
 * 무기 기술(무기 연구 레벨·영웅 기술 스탯) 차이에 따라 아군과 적군의 병력 손실률(교환비)이
 * 크게 벌어지도록 전투 계산식 및 전투 결과 UI 개편"*
 *
 * · 진형 6종은 서로 상성(이기는 조합/지는 조합)을 가진다.
 * · 지형에 따라 같은 진형도 성능이 달라진다.
 * · 무기 기술 레벨과 작전 영웅 능력치가 전투력에 곱해진다.
 * · 손실률은 전력비의 1.5제곱에 비례해, 우세하면 2~5% · 열세하면 40% 이상으로 크게 벌어진다.
 */
enum class BattleStrategy(
    val label: String,
    val icon: String,
    val detail: String
) {
    FRONTAL("정면 종심", "➡", "보병을 깊게 세워 정면에서 밀어붙인다. 매복에 강하다."),
    WEDGE("기갑 예형", "🔺", "기갑을 쐐기로 세워 정면을 뚫는다. 정면 종심에 강하다."),
    ENCIRCLE("측면 포위", "↔", "양 날개로 돌아 들어가 포위한다. 예형 돌파에 강하다."),
    DEFENSE("방어 진지", "🛡", "진지를 굳혀 적을 들이받게 한다. 포위에 강하다."),
    AMBUSH("매복 기습", "🌫", "숨었다가 측후방을 급습한다. 방어 진지에 강하다."),
    FEINT("기만 후퇴", "🎭", "일부러 물러나 적을 끌어낸 뒤 되친다. 측면 포위·하천에서 강하다.");

    /** 이 진형이 이기는 상대 */
    val beats: Set<BattleStrategy>
        get() = when (this) {
            FRONTAL -> setOf(AMBUSH)
            WEDGE -> setOf(FRONTAL)
            ENCIRCLE -> setOf(WEDGE)
            DEFENSE -> setOf(ENCIRCLE)
            AMBUSH -> setOf(DEFENSE)
            FEINT -> setOf(ENCIRCLE)
        }

    /** 지형 계수 */
    fun terrainFactor(terrain: TerrainType): Double = when (terrain) {
        TerrainType.PLAIN -> when (this) {
            FRONTAL -> 1.05
            WEDGE -> 1.15
            ENCIRCLE -> 1.05
            DEFENSE -> 0.95
            AMBUSH -> 0.95
            FEINT -> 1.00
        }
        TerrainType.MOUNTAIN -> when (this) {
            FRONTAL -> 0.85
            WEDGE -> 0.75
            ENCIRCLE -> 1.00
            DEFENSE -> 1.15
            AMBUSH -> 1.20
            FEINT -> 0.90
        }
        TerrainType.RIVER -> when (this) {
            FRONTAL -> 0.90
            WEDGE -> 0.90
            ENCIRCLE -> 1.00
            DEFENSE -> 1.10
            AMBUSH -> 1.10
            FEINT -> 1.15
        }
        TerrainType.COASTAL -> when (this) {
            FRONTAL -> 1.00
            WEDGE -> 1.00
            ENCIRCLE -> 1.10
            DEFENSE -> 1.05
            AMBUSH -> 1.00
            FEINT -> 1.05
        }
    }

    companion object {
        /** 상성 승리 계수 / 패배 계수 */
        const val EDGE_WIN = 1.35
        const val EDGE_LOSE = 0.72

        /** a 기준 상성 계수 */
        fun matchup(a: BattleStrategy, b: BattleStrategy): Double = when {
            a == b -> 1.0
            a.beats.contains(b) -> EDGE_WIN
            b.beats.contains(a) -> EDGE_LOSE
            else -> 1.0
        }

        /** 상성 한 줄 — "기갑 예형 > 정면 종심" */
        fun edgeLabel(a: BattleStrategy, b: BattleStrategy): String = when {
            a == b -> "같은 전략 — 상성 없음"
            a.beats.contains(b) -> "${a.label} 우세 (×${EDGE_WIN})"
            b.beats.contains(a) -> "${a.label} 열세 (×${EDGE_LOSE})"
            else -> "상성 없음 (×1.0)"
        }
    }
}

/** 무기 기술 5계통 — 레벨 0~4 */
enum class WeaponTech(
    val label: String,
    val icon: String,
    val effect: String,
    val levels: List<String>
) {
    SMALL_ARMS("소화기·야전장비", "🪖", "보병 화력·생존성", listOf("기본 지급", "개량 소총", "방탄 장구", "야전 통합", "정밀 조준경")),
    ARMOR("기갑·장갑", "🛡", "기갑 돌파력·방호", listOf("기본 장갑", "반응장갑", "능동 방호", "통합 생존", "차세대 장갑")),
    FIREPOWER("화력·사격통제", "💥", "화력 정밀도·제압", listOf("수동 사격", "디지털 통제", "자동 사격", "다연장 연동", "AI 사격통제")),
    DRONE("드론·전자전", "🛰", "정찰·교란·정밀타격", listOf("상용 드론", "군용 정찰", "자폭 편대", "전자전 탑재", "자율 편대")),
    AIR_DEFENSE("방공·요격", "🎯", "적 드론·항공 차단", listOf("기본 요격", "레이더 연동", "복합 요격", "재밍 통합", "광역 방공"));

    /** 레벨 1당 상승폭 */
    val perLevel: Double get() = 0.06

    fun levelName(level: Int): String = levels[level.coerceIn(0, levels.lastIndex)]

    /** 다음 레벨 비용 — 900G 부터 1.6배씩 */
    fun costOf(nextLevel: Int): Int = (900 * 1.6.pow((nextLevel - 1).coerceAtLeast(0))).toInt()
}

/** 무기 기술 상태 */
data class WeaponTechState(val levels: Map<WeaponTech, Int> = emptyMap()) {

    fun levelOf(tech: WeaponTech): Int = levels[tech] ?: 0

    val totalLevels: Int get() = levels.values.sum()

    /** 전체 상승폭 — 5계통 × 4레벨 = 최대 +120% */
    val multiplier: Double get() = 1.0 + totalLevels * WeaponTech.SMALL_ARMS.perLevel

    /** 화면용 — "드론·전자전 Lv.2 · 방공·요격 Lv.1" */
    fun summary(): String {
        val parts = levels.filterValues { it > 0 }.map { (tech, lv) -> "${tech.label} Lv.$lv" }
        return if (parts.isEmpty()) "무기 기술 미연구 (전투력 ×1.00)" else parts.joinToString(" · ")
    }

    fun upgrade(tech: WeaponTech, gold: Int): Pair<WeaponTechState, Int>? {
        val current = levelOf(tech)
        if (current >= 4) return null
        val cost = tech.costOf(current + 1)
        if (gold < cost) return null
        return WeaponTechState(levels + (tech to current + 1)) to cost
    }
}

/** 전투 전략 해석 결과 */
data class StrategyBattleResult(
    val ownWon: Boolean,
    val ownLosses: Int,
    val enemyLosses: Int,
    val ownLossRatio: Double,
    val enemyLossRatio: Double,
    val exchangeRatio: Double,
    val ownPower: Int,
    val enemyPower: Int,
    val ownStrategy: BattleStrategy,
    val enemyStrategy: BattleStrategy,
    val ownTechLevel: Int,
    val enemyTechLevel: Int,
    val lines: List<String>,
    val message: String
) {
    /** "교환비 6.2 : 1" */
    val exchangeLabel: String
        get() = if (ownLosses == 0) "무손실" else "%.1f : 1".format(exchangeRatio)

    val verdict: String
        get() = when {
            ownLossRatio <= 0.05 -> "압도적 우세 — 아군 손실 ${(ownLossRatio * 100).toInt()}%"
            ownLossRatio <= 0.12 -> "우세 — 아군 손실 ${(ownLossRatio * 100).toInt()}%"
            ownLossRatio <= 0.22 -> "접전 — 아군 손실 ${(ownLossRatio * 100).toInt()}%"
            else -> "열세 — 아군 손실 ${(ownLossRatio * 100).toInt()}% · 재편성 필요"
        }
}

object StrategyBattleEngine {

    /**
     * 전투 해석.
     *
     * @param defending 우리가 방어 측이면 true (방어 전투력·진지 보정 적용)
     */
    fun resolve(
        own: ForceComposition,
        enemy: ForceComposition,
        terrain: TerrainType,
        ownStrategy: BattleStrategy,
        enemyStrategy: BattleStrategy,
        ownTech: WeaponTechState,
        enemyTechLevel: Int,
        ownOpsBonusPercent: Int = 0,
        defenseBonusRatio: Double = 0.0,
        casualtyReduction: Double = 0.0,
        fortressReduction: Double = 0.0,
        durabilityFactor: Double = 0.0,
        defending: Boolean = false
    ): StrategyBattleResult {
        val ownBase = if (defending) own.defensePower(terrain) else own.attackPower(terrain)
        val enemyBase = if (defending) enemy.attackPower(terrain) else enemy.defensePower(terrain)

        val ownStrategyMult = ownStrategy.terrainFactor(terrain) *
            BattleStrategy.matchup(ownStrategy, enemyStrategy)
        val enemyStrategyMult = enemyStrategy.terrainFactor(terrain) *
            BattleStrategy.matchup(enemyStrategy, ownStrategy)

        val ownTechMult = ownTech.multiplier
        val enemyTechMult = 1.0 + enemyTechLevel * 0.06
        val opsMult = 1.0 + ownOpsBonusPercent.coerceIn(0, 60) / 200.0
        val fortMult = if (defending) (1.0 + defenseBonusRatio.coerceIn(0.0, 0.6) + durabilityFactor * 1.2) else 1.0

        val ownEff = ownBase * ownStrategyMult * ownTechMult * opsMult * fortMult
        val enemyEff = enemyBase * enemyStrategyMult * enemyTechMult

        val ratio = if (ownEff <= 0.0) 100.0 else (enemyEff / ownEff)
        val ownLossRatioRaw = (0.26 * ratio.pow(1.5)).coerceIn(0.01, 0.65)
        val reduction = (casualtyReduction + fortressReduction).coerceIn(0.0, 0.95)
        val ownLossRatio = (ownLossRatioRaw * (1.0 - reduction)).coerceIn(0.005, 0.65)
        val enemyLossRatio = (0.26 * (1.0 / ratio).pow(1.5)).coerceIn(0.01, 0.65)

        val ownTroops = own.totalTroops
        val enemyTroops = enemy.totalTroops
        val ownLosses = (ownTroops * ownLossRatio).toInt().coerceIn(0, ownTroops)
        val enemyLosses = (enemyTroops * enemyLossRatio).toInt().coerceIn(0, enemyTroops)
        val exchange = if (ownLosses == 0) enemyLosses.toDouble() else enemyLosses.toDouble() / ownLosses

        val lines = buildList {
            add("전략 상성 · ${BattleStrategy.edgeLabel(ownStrategy, enemyStrategy)}")
            add("지형 · ${terrain.name} — ${ownStrategy.label} ×${"%.2f".format(ownStrategy.terrainFactor(terrain))}")
            add("무기 기술 · 아군 Lv.${
                ownTech.totalLevels
            } ×${"%.2f".format(ownTechMult)} / 적 Lv.$enemyTechLevel ×${"%.2f".format(enemyTechMult)}")
            if (ownOpsBonusPercent > 0) add("작전 영웅 · +$ownOpsBonusPercent%")
            if (defending) add("방어 진지 · ×${"%.2f".format(fortMult)} · 사상자 감소 ${(reduction * 100).toInt()}%")
            add("손실률 · 아군 ${(ownLossRatio * 100).toInt()}% / 적 ${(enemyLossRatio * 100).toInt()}%")
        }

        val message = if (ownEff >= enemyEff) {
            "우리 전략이 적을 눌렀습니다. 적 사상자 ${"%,d".format(enemyLosses)}명 · " +
                "아군 사상자 ${"%,d".format(ownLosses)}명 (교환비 ${"%.1f".format(exchange)} : 1)."
        } else {
            "적 전략에 밀렸습니다. 적 사상자 ${"%,d".format(enemyLosses)}명 · " +
                "아군 사상자 ${"%,d".format(ownLosses)}명 (교환비 ${"%.1f".format(exchange)} : 1)."
        }

        return StrategyBattleResult(
            ownWon = ownEff >= enemyEff,
            ownLosses = ownLosses,
            enemyLosses = enemyLosses,
            ownLossRatio = ownLossRatio,
            enemyLossRatio = enemyLossRatio,
            exchangeRatio = exchange,
            ownPower = ownEff.toInt(),
            enemyPower = enemyEff.toInt(),
            ownStrategy = ownStrategy,
            enemyStrategy = enemyStrategy,
            ownTechLevel = ownTech.totalLevels,
            enemyTechLevel = enemyTechLevel,
            lines = lines,
            message = message
        )
    }

    /** 적이 고를 전략 — 지형과 병력 구성으로 결정 (유저/AI 구분 없이 같은 규칙) */
    fun enemyStrategyFor(terrain: TerrainType, seed: Int): BattleStrategy = when (terrain) {
        TerrainType.MOUNTAIN -> if (seed % 2 == 0) BattleStrategy.AMBUSH else BattleStrategy.DEFENSE
        TerrainType.RIVER -> if (seed % 2 == 0) BattleStrategy.FEINT else BattleStrategy.DEFENSE
        TerrainType.COASTAL -> if (seed % 2 == 0) BattleStrategy.ENCIRCLE else BattleStrategy.FRONTAL
        TerrainType.PLAIN -> when (seed % 3) {
            0 -> BattleStrategy.WEDGE
            1 -> BattleStrategy.FRONTAL
            else -> BattleStrategy.ENCIRCLE
        }
    }

    /** 적 무기 기술 레벨 — 지휘 단계·거리로만 결정된다 */
    fun enemyTechLevel(unlockedHop: Int, seed: Int): Int =
        (unlockedHop + seed % 3).coerceIn(0, 4)
}
