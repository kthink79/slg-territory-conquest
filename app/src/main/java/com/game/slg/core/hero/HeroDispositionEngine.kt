package com.game.slg.core.hero

import com.game.slg.core.empire.Faction

/**
 * 소속 영웅 처분 엔진 — 편입한 세력 밑에 있던 영웅을 등용하거나 재야로 돌려보낸다.
 *
 * 사용자 요청(2026-11-22): *"동일 지역 교전 0병력 즉시 강제 편입(A안) 및 소속 영웅 등용·방출
 * 처분 루틴(기본안)"*
 *
 * · 리더마다 2~4명의 소속 영웅이 있다. 배정은 `lord.id` 시드로 결정되므로 저장하지 않아도
 *   같은 게임에서 항상 같은 결과가 나온다.
 * · 편입되면 그 리더의 소속 영웅은 처분 대기로 넘어간다.
 * · [등용] 등급 기준가에서 20% 할인한 자금을 내고 우리 로스터로 들인다. 빈 슬롯이 있으면 바로 배치.
 * · [방출] 재야로 돌려보낸다. 단계 SCOUTED · 호감도 −15 · 명성 −1 (다시 영입할 수 있다).
 * · 미처분 영웅은 다음 절기로 넘어갈 때 자동으로 재야가 된다(방치 페널티 없음).
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 처분을 기다리는 영웅 1명 — 어느 리더 밑에 있었는지 함께 남긴다 */
data class PendingDisposition(
    val heroId: String,
    val lordId: String,
    val lordName: String
) {
    /** 저장용 한 줄 — "heroId|lordId|lordName" */
    fun encode(): String = listOf(heroId, lordId, lordName).joinToString("|")

    companion object {
        fun decode(raw: String): PendingDisposition? {
            val parts = raw.split('|')
            val heroId = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
            return PendingDisposition(
                heroId = heroId,
                lordId = parts.getOrNull(1) ?: "",
                lordName = parts.getOrNull(2) ?: "리더"
            )
        }
    }
}

object HeroDispositionEngine {

    /** 리더 1명이 데리고 있는 소속 영웅 수의 범위 */
    const val MIN_HEROES_PER_LORD = 2
    const val MAX_HEROES_PER_LORD = 4

    /** 등용 비용 할인 (%) — 등급 기준가에서 깎는다 */
    const val RECRUIT_DISCOUNT_PERCENT = 20

    /** 방출 시 호감도 하락 */
    const val RELEASE_AFFINITY_PENALTY = 15

    /** 방출 시 명성 하락 */
    const val RELEASE_PRESTIGE_PENALTY = 1

    /** 붙잡힌 영웅의 첫 호감도 — 방출되면 40 이 되어 다시 설득할 수 있다 */
    const val CAPTURED_AFFINITY = 55

    /** 시드 기반 2~4명 배정 후보 — 시작 영웅 001~003 은 제외한다 */
    private val POOL: List<Hero> by lazy {
        HeroRoster.ALL
            .filterNot { it.id in HeroRosterState.STARTING_IDS }
            .sortedBy { it.id }
    }

    /**
     * 리더 한 명의 소속 영웅 — `lordId` 시드로 결정적이다.
     * 이미 다른 리더에게 배정된 영웅은 [exclude] 로 빼고 고른다.
     */
    fun heroesForLord(lordId: String, exclude: Set<String> = emptySet()): List<String> {
        if (POOL.isEmpty()) return emptyList()
        val candidates = POOL.filterNot { it.id in exclude }
        if (candidates.isEmpty()) return emptyList()
        var state = seedOf(lordId).toLong()
        val wanted = MIN_HEROES_PER_LORD +
            (Math.floorMod(seedOf(lordId), MAX_HEROES_PER_LORD - MIN_HEROES_PER_LORD + 1))
                .coerceIn(0, MAX_HEROES_PER_LORD - MIN_HEROES_PER_LORD)
        val picked = LinkedHashSet<String>()
        var guard = 0
        while (picked.size < wanted && guard < wanted * 40 + 40) {
            state = state * 6364136223846793005L + 1442695040888963407L
            val idx = ((state ushr 33).toInt() and Int.MAX_VALUE) % candidates.size
            picked += candidates[idx].id
            guard += 1
        }
        return picked.toList()
    }

    private fun seedOf(text: String): Int {
        var hash = 1125899907
        text.forEach { ch -> hash = hash * 31 + ch.code }
        return if (hash == 0) 7 else hash
    }

    /** 등용 비용 — 등급 기준가의 80% */
    fun recruitCost(hero: Hero): Int =
        (hero.grade.hireBase * (100 - RECRUIT_DISCOUNT_PERCENT) / 100).coerceAtLeast(100)

    /** 편입한 세력의 소속 영웅을 처분 대기 목록으로 — 화면·저장에 쓰는 형태 */
    fun capturesOf(faction: Faction): List<PendingDisposition> {
        val lordId = faction.id.removePrefix("faction_")
        return faction.heroIds
            .filter { HeroRoster.byId(it) != null }
            .map { PendingDisposition(heroId = it, lordId = lordId, lordName = faction.name) }
    }

    /** 붙잡힌 영웅의 기록을 만들거나 갱신한다 — 소속 리더와 첫 호감도 */
    fun markCaptured(roster: HeroRosterState, pending: List<PendingDisposition>): HeroRosterState {
        var next = roster
        pending.forEach { entry ->
            val record = next.recordOf(entry.heroId)
            if (record.owned) return@forEach
            next = next.withRecord(
                record.copy(
                    stage = if (record.stage == HeroStage.UNDISCOVERED) HeroStage.SCOUTED else record.stage,
                    affinity = if (record.affinity > 0) record.affinity else CAPTURED_AFFINITY,
                    allegianceLordId = entry.lordId
                )
            )
        }
        return next
    }

    /** 등용 — 자금을 내고 우리 로스터로. 빈 슬롯이 있으면 그 자리에 앉힌다. */
    fun recruit(
        roster: HeroRosterState,
        heroId: String,
        gold: Int
    ): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[등용] 알 수 없는 인물입니다. ($heroId)")
        val record = roster.recordOf(heroId)
        if (record.owned) {
            return HeroActionResult(roster, false, "[등용] ${hero.name}은(는) 이미 우리 리더입니다.")
        }
        val cost = recruitCost(hero)
        if (gold < cost) {
            return HeroActionResult(
                roster, false,
                "[등용 불가] 자금이 부족합니다. (필요 ${"%,d".format(cost)}G / 보유 ${"%,d".format(gold)}G)"
            )
        }
        val slotFree = roster.heroIn(hero.role) == null
        val next = roster.withRecord(
            record.copy(
                stage = HeroStage.HIRED,
                affinity = record.affinity.coerceAtLeast(CAPTURED_AFFINITY),
                assignedRole = if (slotFree) hero.role else null,
                allegianceLordId = null
            )
        )
        val tail = if (slotFree) " ${hero.role.label} 슬롯에 배치했습니다." else " 슬롯이 가득 차 대기 중입니다."
        return HeroActionResult(
            roster = next,
            success = true,
            message = "[등용] ${hero.name} · ${hero.grade.label} ${hero.role.label} — " +
                "기준가 ${"%,d".format(hero.grade.hireBase)}G 에서 ${RECRUIT_DISCOUNT_PERCENT}% 할인한 " +
                "${"%,d".format(cost)}G.${tail}",
            goldDelta = -cost
        )
    }

    /** 방출 — 재야로 돌려보낸다. 호감도 −15 · 명성 −1 */
    fun release(roster: HeroRosterState, heroId: String): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[방출] 알 수 없는 인물입니다. ($heroId)")
        val record = roster.recordOf(heroId)
        val next = roster.withRecord(
            record.copy(
                stage = HeroStage.SCOUTED,
                affinity = (record.affinity - RELEASE_AFFINITY_PENALTY).coerceIn(0, 100),
                assignedRole = null,
                allegianceLordId = null
            )
        )
        return HeroActionResult(
            roster = next,
            success = true,
            message = "[방출] ${hero.name}을(를) 재야로 돌려보냈습니다. " +
                "호감도 ${record.affinity} → ${(record.affinity - RELEASE_AFFINITY_PENALTY).coerceAtLeast(0)} · " +
                "명성 −$RELEASE_PRESTIGE_PENALTY",
            prestigeDelta = -RELEASE_PRESTIGE_PENALTY
        )
    }

    /** 내 영웅 내보내기 — 방출과 같은 규칙, 태수 자리는 화면에서 따로 해제한다 */
    fun dismiss(roster: HeroRosterState, heroId: String): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[내보내기] 알 수 없는 인물입니다. ($heroId)")
        if (!roster.recordOf(heroId).owned) {
            return HeroActionResult(roster, false, "[내보내기] ${hero.name}은(는) 우리 로스터에 없습니다.")
        }
        val record = roster.recordOf(heroId)
        val next = roster.withRecord(
            record.copy(
                stage = HeroStage.SCOUTED,
                affinity = (record.affinity - RELEASE_AFFINITY_PENALTY).coerceIn(0, 100),
                assignedRole = null,
                allegianceLordId = null
            )
        )
        return HeroActionResult(
            roster = next,
            success = true,
            message = "[내보내기] ${hero.name}과(와)의 계약을 끝냈습니다. " +
                "재야로 돌아갔고 호감도 ${record.affinity} → " +
                "${(record.affinity - RELEASE_AFFINITY_PENALTY).coerceAtLeast(0)} · 명성 −$RELEASE_PRESTIGE_PENALTY",
            prestigeDelta = -RELEASE_PRESTIGE_PENALTY
        )
    }

    /**
     * 절기가 바뀔 때 남은 처분 대기 영웅을 자동 방출한다.
     * 방치 페널티(호감도·명성)는 주지 않는다 — 화면을 닫아 둔 것만으로 손해를 보지 않게 한다.
     */
    fun autoRelease(
        roster: HeroRosterState,
        pending: List<PendingDisposition>
    ): Pair<HeroRosterState, Int> {
        if (pending.isEmpty()) return roster to 0
        var next = roster
        var released = 0
        pending.forEach { entry ->
            val record = next.recordOf(entry.heroId)
            if (record.owned) return@forEach
            next = next.withRecord(
                record.copy(
                    stage = HeroStage.SCOUTED,
                    assignedRole = null,
                    allegianceLordId = null
                )
            )
            released += 1
        }
        return next to released
    }
}
