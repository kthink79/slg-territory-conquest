package com.game.slg.core.region

import com.game.slg.core.model.CombatArm
import com.game.slg.core.model.Season
import java.util.Random

/**
 * 다른 세력에 묶인 형태 — 세부맵의 연결선·배지, 리더 명부의 상태 표시에 쓴다.
 *
 * · [MERGE] 병합 — 한 깃발 아래로 합쳐져 우두머리 리더의 편제에 들어간다.
 * · [SUBORDINATION] 종속 — 독자 병력은 유지하되 절기마다 조공을 바치고 병력을 차출한다.
 */
enum class LeaderBond(val label: String, val icon: String, val description: String) {
    MERGE("병합", "⚔", "한 세력으로 합쳐져 우두머리 리더의 편제로 들어갑니다."),
    SUBORDINATION("종속", "🏳", "독자 병력을 유지하되 절기마다 조공을 바치고 병력을 차출합니다.");
}

/** AI 턴 1회의 조건 */
data class AiSeasonOrder(
    val season: Season,
    val turn: Int,
    /** 지역 연합(스폰 보호) 중인지 — 연합 중에는 우리 영지를 공격하지 않는다 */
    val unionActive: Boolean,
    val regionName: String = ""
)

/** AI 리더가 우리 영지에 가한 피해 1건 */
data class AiPlayerRaid(
    val lordId: String,
    val lordName: String,
    val troopLoss: Int,
    val goldLoss: Int,
    val foodLoss: Int,
    val message: String
)

/**
 * 이번 절기에 일어난 세력 확장 1건 — 우호 리더가 공격받았을 때 지원군을 보낼 근거가 된다.
 * (사용자 요청 2026-11-25: "다른 리더가 공격받을 때 내가 지원군/도움을 보낼 수도 있어야 한다")
 */
data class AiExpansion(
    val leaderId: String,
    val leaderName: String,
    val targetId: String,
    val targetName: String,
    val kind: LeaderBond,
    val absorbedTroops: Int = 0
)

/** 한 절기의 AI 동향 */
data class AiSeasonOutcome(
    val lords: List<AiLordProfile>,
    val lines: List<String>,
    val raids: List<AiPlayerRaid> = emptyList(),
    val mergedCount: Int = 0,
    val subordinatedCount: Int = 0,
    val independentCount: Int = 0,
    /** 이번 절기 확장 1건 — 없으면 null */
    val expansion: AiExpansion? = null
) {
    val troopLoss: Int get() = raids.sumOf { it.troopLoss }
    val goldLoss: Int get() = raids.sumOf { it.goldLoss }
    val foodLoss: Int get() = raids.sumOf { it.foodLoss }
    val hasNews: Boolean get() = lines.isNotEmpty()

    /** 머리말 없이 한 줄 요약 */
    fun summaryLine(): String =
        "독립 ${independentCount}명 · 병합 ${mergedCount}명 · 종속 ${subordinatedCount}명"
}

/**
 * AI 리더 능동 활동 엔진 — 절기마다 AI 리더가 가만히 있지 않게 한다.
 *
 * 사용자 요청(2026-11-24): *"주변 AI 리더들의 변동이 거의 없어 성장을 하거나, 병력을 키우거나
 * 주변 세력과 전투를 하는 등 나름대로 세력을 넓히려는 활동을 하게 해줘"*
 *
 * 절기 한 번에 AI 리더가 하는 일
 *  1. 내정 — 성향에 맞춰 자금·식량을 확보한다(물류 성향은 자금, 기갑·균형은 병력).
 *  2. 병력 양성 — 모집비를 쓰고, 명성으로 정해지는 상한까지 병력을 늘린다. 유지비를 못 대면 이탈한다.
 *  3. 세력 확장 — 가장 강한 독립 리더가 약한 이웃을 ① 흡수 병합 ② 조공 종속으로 묶는다(절기당 1건).
 *  4. 견제 — 연합이 끝난 뒤에는 우리와 비등하거나 더 큰 리더가 외곽을 습격한다(절기당 최대 1건).
 *
 * · 시드가 같으면 결과가 같다(재현 가능) — 테스트와 저장 복원에 쓴다.
 * · 병합된 세력도 거점 병력은 남겨 두어, 세부맵에서 "병합/종속" 관계와 남은 병력을 함께 볼 수 있다.
 * · 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */
object AiLordTurnEngine {

    /** 병합 조건 — 이 배수 이상 강하면 편제를 통째로 흡수한다 */
    const val MERGE_RATIO = 1.6

    /** 종속 조건 — 이 배수 이상 강하면 조공을 바치게 한다 */
    const val SUBORDINATE_RATIO = 1.25

    /** 확장에 나설 수 있는 최소 병력 */
    const val MIN_PROJECTION = 220

    /** 병합할 때 상대가 남기는 거점 병력 비율 */
    const val SERVANT_KEEP_RATIO = 0.6

    /** 종속 세력이 절기마다 바치는 조공 비율(자금) */
    const val TRIBUTE_RATIO = 0.18

    /** 우리 영지를 습격할 수 있는 병력 배수 — 우리와 비등해도 건드린다 */
    const val RAID_RATIO = 1.05

    /** 절기당 확장(병합·종속) 건수 */
    const val MAX_EXPANSION_PER_SEASON = 1

    /** 절기당 우리 영지 습격 건수 */
    const val MAX_RAID_PER_SEASON = 1

    /** 로그에 남기는 최대 줄 수 */
    const val NEWS_LIMIT = 4

    /** 명성으로 정해지는 병력 상한 — 명성 40이면 1,380명 */
    fun troopCeiling(prestige: Int): Int = 180 + prestige.coerceIn(0, 100) * 30

    /**
     * AI 리더 한 명을 한 절기 운영한다.
     * @return 갱신된 리더 · 활동 한 줄 · 이번 절기 모집 병력
     */
    fun growSeason(lord: AiLordProfile, rng: Random): AiLordProfile {
        // 병합으로 편제가 넘어간 세력은 자체 성장을 멈춘다
        if (lord.bond == LeaderBond.MERGE) return lord

        val trait = lord.trait
        // 1) 내정 — 자금·식량
        val goldIncome = 90 + trait.goldBonus + lord.prestige + rng.nextInt(40)
        val foodIncome = 120 + lord.troops / 10 + rng.nextInt(80)
        var gold = lord.gold + goldIncome
        var grain = lord.grain + foodIncome

        // 2) 병력 양성 — 모집비(1명당 4G)를 쓰고 상한까지만 채운다. 금고에는 40%를 남긴다(조공·비상금)
        val ceiling = troopCeiling(lord.prestige)
        val room = (ceiling - lord.troops).coerceAtLeast(0)
        val budget = (gold * 0.6).toInt()
        val recruits = minOf(room, budget / 4, 120 + lord.prestige + trait.troopBonus.coerceAtLeast(0))
        val spent = recruits * 4
        gold -= spent
        var troops = lord.troops + recruits

        // 3) 유지비 — 식량이 모자라면 병력이 이탈한다
        val upkeep = troops / 6
        if (grain >= upkeep) {
            grain -= upkeep
        } else {
            val shortage = upkeep - grain
            grain = 0
            val desertion = (shortage / 2).coerceAtMost(troops / 3)
            troops -= desertion
        }

        val activity = buildString {
            append("병력 +${format(recruits)}")
            append(" · 모집비 ${format(spent)}G")
            if (recruits == 0) append(" · 상한 ${format(ceiling)}명")
            if (trait == LordTrait.LOGISTICS) append(" · 보급선 확보")
        }

        return lord.copy(
            troops = troops.coerceAtLeast(0),
            gold = gold.coerceAtLeast(0),
            grain = grain.coerceAtLeast(0),
            activity = activity
        )
    }

    /**
     * 한 절기의 AI 리더 전체 턴.
     *
     * @param lords 같은 지역 AI 리더 명부 (플레이어 제외)
     * @param player 플레이어 세력 — 없으면 견제(습격)는 생략한다
     */
    fun runSeason(
        lords: List<AiLordProfile>,
        player: AiLordProfile?,
        order: AiSeasonOrder,
        seed: Long? = null
    ): AiSeasonOutcome {
        if (lords.isEmpty()) {
            return AiSeasonOutcome(
                lords = lords,
                lines = emptyList(),
                independentCount = 0
            )
        }
        val baseSeed = seed ?: (order.turn.toLong() * 7_919L + order.season.ordinal * 131L + lords.size)
        var working = lords.toMutableList()
        val news = mutableListOf<String>()
        val raids = mutableListOf<AiPlayerRaid>()

        // ── 1단계: 성장 (모든 리더, 병합된 세력 제외) ──────────────────
        working = working.mapIndexed { index, lord ->
            val rng = Random(baseSeed + index * 31L + lord.id.hashCode().toLong())
            growSeason(lord, rng)
        }.toMutableList()

        // ── 2단계: 종속 조공 ──────────────────────────────────────────
        val tributeTotals = mutableMapOf<String, Int>()
        working.forEachIndexed { index, lord ->
            val masterId = lord.masterId ?: return@forEachIndexed
            if (lord.bond != LeaderBond.SUBORDINATION) return@forEachIndexed
            val masterIndex = working.indexOfFirst { it.id == masterId }
            if (masterIndex < 0) return@forEachIndexed
            val tribute = (lord.gold * TRIBUTE_RATIO).toInt().coerceAtLeast(0)
            if (tribute <= 0) return@forEachIndexed
            val master = working[masterIndex]
            working[masterIndex] = master.copy(gold = master.gold + tribute)
            working[index] = lord.copy(gold = (lord.gold - tribute).coerceAtLeast(0))
            tributeTotals[master.name] = (tributeTotals[master.name] ?: 0) + tribute
        }
        val tributeNotes = tributeTotals.entries
            .sortedByDescending { it.value }
            .take(2)
            .map { (name, amount) ->
                "[AI 동향] 종속 세력이 $name 리더에게 조공 ${format(amount)}G를 바쳤습니다."
            }

        // ── 3단계: 세력 확장 — 힘 있는 독립 리더가 약한 이웃을 병합하거나 종속시킨다 ──
        var mergedCount = working.count { it.bond == LeaderBond.MERGE }
        var subordinatedCount = working.count { it.bond == LeaderBond.SUBORDINATION }
        val expansionRng = Random(baseSeed + 977L)
        val independent = working.filter { it.masterId == null }
        var expansion: AiExpansion? = null
        // 세력권 상위 3명 중 한 명이 이번 절기에 움직인다 — 매번 같은 패권자만 자라지 않게
        val actors = independent
            .filter { it.troops >= MIN_PROJECTION }
            .sortedByDescending { it.troops + it.prestige * 8L }
            .take(3)
        val leader = if (actors.isEmpty()) null else actors[expansionRng.nextInt(actors.size)]
        if (leader != null) {
            val candidates = working.filter { target ->
                target.id != leader.id &&
                    target.masterId == null &&
                    target.troops >= 40 &&
                    target.troops * SUBORDINATE_RATIO <= leader.troops
            }
            val chance = (18 + leader.prestige / 2).coerceIn(18, 70)
            if (candidates.isNotEmpty() && expansionRng.nextInt(100) < chance) {
                // 약한 쪽이면 병합(편제 흡수), 힘 있는 쪽이면 종속(조공) — 둘 다 나오게 번갈아 노린다
                val target = if (expansionRng.nextBoolean()) {
                    candidates.minByOrNull { it.troops }!!
                } else {
                    candidates.maxByOrNull { it.troops }!!
                }
                val leaderIndex = working.indexOfFirst { it.id == leader.id }
                val targetIndex = working.indexOfFirst { it.id == target.id }
                val ceiling = troopCeiling(leader.prestige)
                val room = (ceiling - leader.troops).coerceAtLeast(0)
                if (leader.troops >= target.troops * MERGE_RATIO && room >= 30) {
                    val absorbed = (target.troops * (1.0 - SERVANT_KEEP_RATIO)).toInt().coerceAtMost(room)
                    val keep = (target.troops * SERVANT_KEEP_RATIO).toInt()
                    val leaderTroops = leader.troops + absorbed
                    working[leaderIndex] = working[leaderIndex].copy(
                        troops = leaderTroops,
                        gold = working[leaderIndex].gold + target.gold / 3,
                        grain = working[leaderIndex].grain + target.grain / 3,
                        activity = "${target.name} 리더를 병합 — 편제 +${format(absorbed)}명"
                    )
                    working[targetIndex] = working[targetIndex].copy(
                        masterId = leader.id,
                        bond = LeaderBond.MERGE,
                        troops = keep,
                        gold = working[targetIndex].gold / 2,
                        grain = working[targetIndex].grain / 2,
                        activity = "${leader.name} 리더에게 병합 — 거점 병력 ${format(keep)}명 유지"
                    )
                    mergedCount += 1
                    expansion = AiExpansion(
                        leaderId = leader.id,
                        leaderName = leader.name,
                        targetId = target.id,
                        targetName = target.name,
                        kind = LeaderBond.MERGE,
                        absorbedTroops = absorbed
                    )
                    news += "[AI 동향] ${leader.name} 리더가 ${target.name} 리더 세력을 병합했습니다 — " +
                        "편제 +${format(absorbed)}명 · 거점 병력 ${format(keep)}명 유지."
                } else {
                    working[leaderIndex] = working[leaderIndex].copy(
                        prestige = (leader.prestige + 2).coerceAtMost(100),
                        activity = "${target.name} 리더를 종속 — 조공 확보"
                    )
                    working[targetIndex] = working[targetIndex].copy(
                        masterId = leader.id,
                        bond = LeaderBond.SUBORDINATION,
                        troops = (target.troops * 0.9).toInt().coerceAtLeast(1),
                        activity = "${leader.name} 리더에게 종속 — 조공 ${format((target.gold * TRIBUTE_RATIO).toInt())}G/절기"
                    )
                    subordinatedCount += 1
                    expansion = AiExpansion(
                        leaderId = leader.id,
                        leaderName = leader.name,
                        targetId = target.id,
                        targetName = target.name,
                        kind = LeaderBond.SUBORDINATION,
                        absorbedTroops = (target.troops - (target.troops * 0.9).toInt()).coerceAtLeast(0)
                    )
                    news += "[AI 동향] ${leader.name} 리더가 ${target.name} 리더를 종속시켰습니다 — " +
                        "절기 조공 ${format((target.gold * TRIBUTE_RATIO).toInt())}G · 병력 차출."
                }
            }
        }

        // ── 4단계: 견제 — 연합이 끝났으면 가장 강한 리더가 우리 외곽을 습격한다 ──
        if (!order.unionActive && player != null && player.troops > 0) {
            val raider = working
                .filter { it.masterId == null && it.troops >= (player.troops * RAID_RATIO).toInt() }
                .maxByOrNull { it.troops }
            if (raider != null) {
                val raidRng = Random(baseSeed + 4_099L)
                val chance = (30 + raider.prestige / 3).coerceIn(30, 75)
                if (raidRng.nextInt(100) < chance) {
                    val troopLoss = (player.troops * (0.02 + raidRng.nextInt(21) / 1000.0)).toInt().coerceAtLeast(1)
                    val goldLoss = (player.gold * 0.02).toInt()
                    val foodLoss = (player.grain * 0.02).toInt()
                    val raiderIndex = working.indexOfFirst { it.id == raider.id }
                    val counterLoss = (troopLoss / 2).coerceAtLeast(1)
                    working[raiderIndex] = working[raiderIndex].copy(
                        troops = (raider.troops - counterLoss).coerceAtLeast(1),
                        prestige = (raider.prestige + 1).coerceAtMost(100),
                        activity = "우리 외곽 습격 — 병력 -${format(troopLoss)} · 자금 -${format(goldLoss)}G"
                    )
                    val message = "[AI 동향] ${raider.name} 리더가 우리 외곽 초소를 습격했습니다 — " +
                        "병력 -${format(troopLoss)} · 자금 -${format(goldLoss)}G · 식량 -${format(foodLoss)}석."
                    news += message
                    raids += AiPlayerRaid(
                        lordId = raider.id,
                        lordName = raider.name,
                        troopLoss = troopLoss,
                        goldLoss = goldLoss,
                        foodLoss = foodLoss,
                        message = message
                    )
                }
            }
        }

        // ── 5단계: 요약 ──────────────────────────────────────────────
        val stillIndependent = working.count { it.masterId == null }
        val summary = "[AI 동향] ${order.regionName.ifBlank { "지역" }} 리더 ${working.size}명 — " +
            "독립 ${stillIndependent}명 · 병합 ${working.count { it.bond == LeaderBond.MERGE }}명 · " +
            "종속 ${working.count { it.bond == LeaderBond.SUBORDINATION }}명"
        val lines = (listOf(summary) + news + tributeNotes).take(NEWS_LIMIT)

        return AiSeasonOutcome(
            lords = working,
            lines = lines,
            raids = raids,
            mergedCount = mergedCount,
            subordinatedCount = subordinatedCount,
            independentCount = stillIndependent,
            expansion = expansion
        )
    }

}

/** 숫자 표기 — 1,234 */
private fun format(value: Int): String = "%,d".format(value)

/** 기본 AI 리더 하나 — 테스트·기본값 */
fun sampleLord(
    id: String = "ai_test_1",
    name: String = "김도현",
    troops: Int = 300,
    gold: Int = 600,
    grain: Int = 800,
    prestige: Int = 45,
    trait: LordTrait = LordTrait.BALANCED,
    arm: CombatArm = CombatArm.INFANTRY
): AiLordProfile = AiLordProfile(
    id = id,
    name = name,
    regionCode = RegionCatalog.DEFAULT_AREA_CODE,
    regionName = "탕정면",
    stronghold = "탕정면 중앙 물류단지 1구역",
    trait = trait,
    troops = troops,
    grain = grain,
    gold = gold,
    prestige = prestige,
    arm = arm
)
