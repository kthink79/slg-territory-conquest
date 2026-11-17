package com.game.slg.core.region

import com.game.slg.core.model.CombatArm
import java.util.Random

/**
 * AI 리더 성향 — 초기 거점 성격과 병과 특화.
 */
enum class LordTrait(
    val displayName: String,
    val icon: String,
    val description: String,
    val preferredArm: CombatArm,
    val troopBonus: Int,
    val goldBonus: Int,
    val prestigeBonus: Int
) {
    BALANCED("균형형", "⚖", "보병·기갑을 고르게 굴리는 표준 편제", CombatArm.INFANTRY, 10, 60, 4),
    ARMOR("기갑 중시", "🛡", "K2 전차 중심의 정면 돌파 세력", CombatArm.ARMOR, 20, 20, 6),
    DRONE("드론 특화", "🛰", "정찰·자폭 드론으로 먼저 보고 먼저 때린다", CombatArm.DRONE, 0, 80, 5),
    AIR_DEFENSE("방공 중시", "🎯", "무인기와 항공 전력을 차단하는 방어 세력", CombatArm.AIR_DEFENSE, 5, 40, 8),
    SPECIAL("특수전", "🥷", "소수 정예 침투조로 거점을 급습한다", CombatArm.SPECIAL_OPS, -5, 30, 10),
    LOGISTICS("물류·보급", "🚚", "보급선을 쥐고 절기마다 자원을 쌓는다", CombatArm.LOGISTICS, -10, 120, 3)
}

/**
 * 게임 시작 시 이미 그 지역을 운영하고 있는 AI 리더 1명.
 * 플레이어는 이 명부에서 하나를 골라 그 세력을 인계받는다.
 */
data class AiLordProfile(
    val id: String,
    val name: String,
    val regionCode: String,
    val regionName: String,
    /** 거점 이름 (예: "탕정 물류단지 3구역") */
    val stronghold: String,
    val trait: LordTrait,
    val troops: Int,
    val grain: Int,
    val gold: Int,
    val prestige: Int,
    val arm: CombatArm
) {
    /** 명부 표시용 이름 */
    val title: String get() = "$name 리더"

    /** 인계 시 플레이어 세력이 되는지 */
    fun asPlayerLord(): AiLordProfile = copy(name = name)
}

/**
 * 지역 인구 → 리더 정원만큼 AI 리더을 만든다.
 *
 * - 같은 지역·같은 시드는 항상 같은 명부를 만든다(재현 가능).
 * - 이름·거점·성향이 겹치지 않게 뽑는다.
 */
object AiLordGenerator {

    private val SURNAMES = listOf(
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
        "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍"
    )

    private val GIVEN_NAMES = listOf(
        "도현", "서준", "민재", "하준", "지훈", "현우", "건우", "우진", "선우", "준서",
        "예린", "수아", "지윤", "하은", "서연", "민서", "지아", "유진", "다은", "채원",
        "태호", "성민", "정훈", "광수", "영철", "상현", "미경", "정은", "소영", "혜진"
    )

    private val STRONGHOLDS = listOf(
        "중앙 물류단지", "스마트 관제센터", "산업로 기지", "신도시 A블록",
        "역세권 오피스", "농수산 유통센터", "발전소 보안구역", "체육공원 야전기지",
        "시외버스 터미널", "테크노파크", "재난안전센터", "구도심 재개발구역"
    )

    /** 지역의 리더 정원만큼 AI 리더 명부 생성 */
    fun generate(region: RegionArea, seed: Long = seedOf(region)): List<AiLordProfile> =
        generate(region, region.lordCapacity, seed)

    /** 정원을 직접 지정해 생성 (테스트·데모용) */
    fun generate(region: RegionArea, count: Int, seed: Long): List<AiLordProfile> {
        val safeCount = count.coerceAtLeast(0)
        if (safeCount == 0) return emptyList()
        val random = Random(seed)
        val usedNames = mutableSetOf<String>()
        val usedStrongholds = mutableMapOf<String, Int>()
        val traitPool = LordTrait.entries

        return (0 until safeCount).map { index ->
            val name = pickName(random, index, usedNames)
            val trait = traitPool[(index + random.nextInt(traitPool.size)) % traitPool.size]
            val baseStronghold = STRONGHOLDS[(index * 5 + random.nextInt(STRONGHOLDS.size)) % STRONGHOLDS.size]
            val strongholdIndex = (usedStrongholds.getOrDefault(baseStronghold, 0) + 1)
            usedStrongholds[baseStronghold] = strongholdIndex
            val stronghold = "${region.name} $baseStronghold ${strongholdIndex}구역"

            val troops = (55 + random.nextInt(46) + trait.troopBonus).coerceAtLeast(20)
            val grain = 260 + random.nextInt(240) + trait.goldBonus / 2
            val gold = 180 + random.nextInt(320) + trait.goldBonus
            val prestige = (32 + random.nextInt(26) + trait.prestigeBonus).coerceIn(10, 95)

            AiLordProfile(
                id = "ai_${region.code}_${index + 1}",
                name = name,
                regionCode = region.code,
                regionName = region.name,
                stronghold = stronghold,
                trait = trait,
                troops = troops,
                grain = grain,
                gold = gold,
                prestige = prestige,
                arm = trait.preferredArm
            )
        }
    }

    fun seedOf(region: RegionArea): Long = region.code.hashCode().toLong()

    /** 첫 번째 AI 리더 — 인계 없이 시작 흐름을 확인할 때 쓰는 기본값 */
    fun defaultProfile(region: RegionArea): AiLordProfile =
        generate(region).firstOrNull() ?: AiLordProfile(
            id = "ai_${region.code}_0",
            name = "김도현",
            regionCode = region.code,
            regionName = region.name,
            stronghold = "${region.name} 중앙 물류단지 1구역",
            trait = LordTrait.BALANCED,
            troops = 60,
            grain = 300,
            gold = 250,
            prestige = 40,
            arm = CombatArm.INFANTRY
        )

    private fun pickName(random: Random, index: Int, used: MutableSet<String>): String {
        var attempt = 0
        while (attempt < 400) {
            val surname = SURNAMES[(index + attempt * 3 + random.nextInt(SURNAMES.size)) % SURNAMES.size]
            val given = GIVEN_NAMES[(index * 7 + attempt * 5 + random.nextInt(GIVEN_NAMES.size)) % GIVEN_NAMES.size]
            val candidate = surname + given
            if (used.add(candidate)) return candidate
            attempt += 1
        }
        // 이론상 도달하지 않지만, 이름이 모두 소진되면 번호를 붙여 고유성을 지킨다
        val fallback = SURNAMES[index % SURNAMES.size] + GIVEN_NAMES[index % GIVEN_NAMES.size] + (index + 1)
        used.add(fallback)
        return fallback
    }
}
