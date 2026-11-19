package com.game.slg.core.hero

import com.game.slg.core.empire.EmploymentRequest

/**
 * 영웅 탐색 · 방문 합류 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-19): *"처음부터 240명 전체 목록이 다 보이지 않게 가리고,
 * 보유/만난 영웅 위주 표시(미발견 영웅은 비공개·실루엣 처리). '영웅 탐색' 액션을 통해
 * 지역/절기별 랜덤으로 재야 영웅을 발견·등용. 명성·민심·절기 조건 또는 랜덤 이벤트로
 * 특정 영웅이 스스로 찾아와 합류를 청하는 방문 이벤트 추가."*
 *
 * · 탐색은 이름을 지정하지 않는다. 자금을 쓰고 지역·절기에 맞는 재야 영웅을 만난다.
 * · 아직 만나지 못한 영웅은 목록에서 실루엣(비공개)으로만 보인다.
 * · 방문 이벤트는 절기가 바뀔 때 조건(명성·민심·절기)에 맞는 영웅이 찾아온다.
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */
data class HeroSearchResult(
    val roster: HeroRosterState,
    val hero: Hero?,
    val success: Boolean,
    val message: String,
    val goldDelta: Int = 0
)

/** 찾아온 영웅 — 수락하면 무상 합류, 거절하면 떠난다 */
data class HeroVisit(
    val heroId: String,
    val heroName: String,
    val title: String,
    val season: String,
    val reason: String,
    val requestText: String,
    val acceptLine: String,
    val declineLine: String
) {
    val summary: String get() = "$heroName · $title — $reason"
}

object HeroDiscoveryEngine {

    /** 탐색 1회 비용 — 이름을 지정하지 않고 재야를 훑는다 */
    const val SEARCH_COST = 250

    /** 탐색으로 얻는 호감도 */
    const val SEARCH_AFFINITY = 45

    /**
     * 영웅 탐색 — 아직 만나지 못한 영웅 중 지역·절기에 맞는 한 명을 발견한다.
     *
     * 같은 지역·절기·시드면 같은 결과가 나오도록 시드 기반으로 뽑는다(재현 가능).
     */
    fun search(
        roster: HeroRosterState,
        gold: Int,
        regionCode: String,
        season: String,
        seed: Long
    ): HeroSearchResult {
        if (gold < SEARCH_COST) {
            return HeroSearchResult(
                roster = roster,
                hero = null,
                success = false,
                message = "[탐색 불가] 자금이 부족합니다. (필요 ${SEARCH_COST}G / 보유 ${gold}G)"
            )
        }
        val pool = HeroRoster.ALL.filter { roster.recordOf(it.id).stage == HeroStage.UNDISCOVERED }
        if (pool.isEmpty()) {
            return HeroSearchResult(
                roster = roster,
                hero = null,
                success = false,
                message = "[탐색] ${HeroRoster.ALL.size}명의 영웅을 모두 만났습니다. 더 찾을 재야가 없습니다."
            )
        }
        // 지역·절기로 후보군을 좁힌 뒤 시드로 고른다
        val seasonal = pool.filter { seasonalRole(it.role, season) }
        val candidates = if (seasonal.isNotEmpty()) seasonal else pool
        val index = ((seed xor regionCode.hashCode().toLong() xor season.hashCode().toLong()) and Long.MAX_VALUE)
            .mod(candidates.size.toLong())
            .toInt()
        val hero = candidates[index]
        val record = roster.recordOf(hero.id)
        val next = roster.copy(
            records = roster.records + (
                hero.id to record.copy(stage = HeroStage.SCOUTED, affinity = SEARCH_AFFINITY)
                )
        )
        return HeroSearchResult(
            roster = next,
            hero = hero,
            success = true,
            message = "[탐색] ${season} ${regionCode.takeLast(3)} 일대에서 ${hero.name} · ${hero.title}을(를) 만났습니다. " +
                "등급 ${hero.grade.label} · ${hero.role.label} · 호감도 $SEARCH_AFFINITY",
            goldDelta = -SEARCH_COST
        )
    }

    /** 절기별로 잘 어울리는 역할 — 후보를 좁히는 데만 쓴다 */
    fun seasonalRole(role: HeroRole, season: String): Boolean = when {
        season.startsWith("봄") -> role == HeroRole.ADMIN || role == HeroRole.COMMERCE
        season.startsWith("여름") -> role == HeroRole.TECH || role == HeroRole.OPS
        season.startsWith("가을") -> role == HeroRole.COMMERCE || role == HeroRole.DIPLOMACY
        else -> role == HeroRole.OPS || role == HeroRole.INTEL
    }

    /**
     * 찾아온 영웅 — 명성·민심·절기 조건에 맞는 영웅이 스스로 합류를 청한다.
     * 조건이 맞지 않으면 null.
     */
    fun visitCandidate(
        roster: HeroRosterState,
        prestige: Int,
        morale: Int,
        season: String,
        seed: Long,
        regionName: String
    ): HeroVisit? {
        // 방문은 절기마다 항상 오지는 않는다 — 시드의 3분의 1에서만 열린다
        val gate = ((seed xor season.hashCode().toLong()) and Long.MAX_VALUE).mod(3L)
        if (gate != 0L) return null

        val pool = HeroRoster.ALL.filter { roster.recordOf(it.id).stage == HeroStage.UNDISCOVERED }
        if (pool.isEmpty()) return null
        val eligible = pool.filter { hero ->
            prestige >= (hero.requiredPrestige - 18) &&
                morale >= 45 &&
                hero.stats.total >= 220
        }
        if (eligible.isEmpty()) return null
        val hero = eligible[(seed and Long.MAX_VALUE).mod(eligible.size.toLong()).toInt()]
        return HeroVisit(
            heroId = hero.id,
            heroName = hero.name,
            title = hero.title,
            season = season,
            reason = "${season.substringBefore(" (")} ${regionName} 소문 — ${hero.role.label} 능력 ${hero.stats.primaryOf(hero.role)}",
            requestText = "${hero.name} · ${hero.title}이(가) 우리 거점을 찾아와 합류를 청합니다. " +
                "\"${hero.quote}\" — 명성 ${prestige}을(를) 보고 왔다고 합니다.",
            acceptLine = "[합류] ${hero.name}이(가) ${hero.role.label} 슬롯으로 들어왔습니다. (무상 합류)",
            declineLine = "[거절] ${hero.name}이(가) 다른 곳을 찾아 떠났습니다. 다시 오지 않을 수 있습니다."
        )
    }

    /** 방문 수락 — 무상으로 보유 영웅이 된다 */
    fun acceptVisit(roster: HeroRosterState, visit: HeroVisit): HeroActionResult {
        val hero = HeroRoster.byId(visit.heroId)
            ?: return HeroActionResult(roster, false, "[합류] 알 수 없는 영웅입니다. (${visit.heroId})")
        val record = roster.recordOf(hero.id)
        if (record.owned) {
            return HeroActionResult(roster, false, "[합류] ${hero.name}은(는) 이미 우리 영웅입니다.")
        }
        val slotFree = roster.heroIn(hero.role) == null
        val next = roster.copy(
            records = roster.records + (
                hero.id to record.copy(
                    stage = HeroStage.HIRED,
                    affinity = 85,
                    assignedRole = if (slotFree) hero.role else null
                )
                )
        )
        return HeroActionResult(
            roster = next,
            success = true,
            message = visit.acceptLine,
            prestigeDelta = 2
        )
    }

    /** 방문 거절 — 그 영웅은 미탐색으로 돌아가고 다시 만나기 어려워진다 */
    fun declineVisit(roster: HeroRosterState, visit: HeroVisit): HeroActionResult {
        val record = roster.recordOf(visit.heroId)
        val next = roster.copy(
            records = roster.records + (
                visit.heroId to record.copy(stage = HeroStage.UNDISCOVERED, affinity = 0)
                )
        )
        return HeroActionResult(next, true, visit.declineLine, prestigeDelta = -1)
    }

    /** 아직 만나지 못한 영웅 수 — 목록에는 실루엣으로만 보인다 */
    fun undiscoveredCount(roster: HeroRosterState): Int =
        HeroRoster.ALL.count { roster.recordOf(it.id).stage == HeroStage.UNDISCOVERED }

    /** 화면에 보이는(보유 + 만난) 영웅 */
    fun knownHeroes(roster: HeroRosterState): List<Hero> =
        HeroRoster.ALL.filter { roster.recordOf(it.id).stage != HeroStage.UNDISCOVERED }
}

/**
 * 리더·영웅 쌍방향 블라인드 원칙.
 *
 * 사용자 요청: *"영지 리더는 상대가 실제 유저인지 AI인지 절대 알 수 없도록
 * (UI 표식·AI 행동 차별·외교/전투/등용 분기 제거) 모든 리더·영웅 세력을 동일 규칙으로 대우."*
 *
 * 이 객체는 화면·엔진이 공통으로 쓰는 문구와 판정만 담는다.
 * "유저/AI"를 드러내는 표현은 여기서도, 화면에서도 쓰지 않는다.
 */
object BlindPrinciple {

    const val RULE_LINE = "모든 리더와 영웅은 같은 규칙으로만 대우합니다. 상대가 사람인지 기계인지는 표시하지 않습니다."

    val RULES: List<String> = listOf(
        "① 리더 명부·지도·외교 화면에 사람/기계 표식을 두지 않는다.",
        "② 외교·전투·등용 판정은 능력치·명성·세력 필요도만 본다.",
        "③ 같은 조건이면 누구에게나 같은 답이 나온다.",
        "④ 영웅으로 시작한 유저도 리더에게 등용을 청할 수 있고, 리더의 답은 같은 기준으로 정해진다.",
        "⑤ 상대 리더가 우리 편입 제안에 답하는 방식도 동일하다."
    )

    /** 리더를 가리키는 유일한 호칭 */
    const val LORD_LABEL = "리더"

    /** 어떤 리더든 같은 형식으로 보여 준다 */
    fun lordLine(name: String, stronghold: String, troops: Int, prestige: Int): String =
        "$name 리더 · $stronghold · ⚔ ${"%,d".format(troops)} · 명성 $prestige"
}

/** 등용 청을 받는 리더의 처지 — 유저/AI 구분 없이 이 값만 쓴다 */
data class LordNeed(
    val troops: Int,
    val prestige: Int,
    val population: Int,
    val atWar: Boolean = false
) {
    /** 이 리더가 지금 가장 필요한 역할 */
    val neededRole: HeroRole
        get() = when {
            troops < 500 -> HeroRole.OPS
            prestige < 50 -> HeroRole.DIPLOMACY
            population > 40_000 -> HeroRole.ADMIN
            else -> HeroRole.COMMERCE
        }

    /** 문턱 — 세력이 급하면 조금 낮아진다 */
    val threshold: Int
        get() = (if (atWar || troops < 400) 42 else 50)
}

/**
 * 영웅 → 리더 등용 청.
 *
 * 판정: (5개 능력치 합 ÷ 8) + (명성 ÷ 5) + (리더가 필요로 하는 역할이면 +8) ≥ 문턱.
 * 상대가 누구인지와 무관하게 이 식만 쓴다.
 */
object HeroEmploymentEngine {

    fun score(hero: Hero, prestige: Int, need: LordNeed): Int {
        val base = hero.stats.total / 8
        val prestigePart = prestige / 5
        val needBonus = if (hero.role == need.neededRole) 8 else 0
        return base + prestigePart + needBonus
    }

    fun request(
        hero: Hero,
        prestige: Int,
        lordId: String,
        lordName: String,
        need: LordNeed
    ): EmploymentRequest {
        val total = score(hero, prestige, need)
        val accepted = total >= need.threshold
        val loyalty = if (accepted) (60 + (total - need.threshold)).coerceIn(50, 95) else 0
        val reason = "능력 합 ${hero.stats.total}÷8=${hero.stats.total / 8} + 명성 ${prestige}÷5=${prestige / 5} + " +
            "${if (hero.role == need.neededRole) "필요 역할(${need.neededRole.label}) +8" else "역할 보정 0"} " +
            "= $total / 문턱 ${need.threshold}"
        val message = if (accepted) {
            "[등용 수락] ${lordName} 리더가 ${hero.name}을(를) 받아들였습니다. " +
                "충성 $loyalty · $reason"
        } else {
            "[등용 거절] ${lordName} 리더가 고개를 저었습니다. — $reason. " +
                "공적을 쌓아 명성을 올린 뒤 다시 청하십시오."
        }
        return EmploymentRequest(
            heroId = hero.id,
            heroName = hero.name,
            lordId = lordId,
            lordName = lordName,
            accepted = accepted,
            loyalty = loyalty,
            message = message,
            reasonLine = reason
        )
    }
}
