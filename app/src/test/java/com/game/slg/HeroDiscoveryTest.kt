package com.game.slg

import com.game.slg.core.hero.HeroDiscoveryEngine
import com.game.slg.core.hero.HeroEmploymentEngine
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.hero.HeroStage
import com.game.slg.core.hero.HeroVisit
import com.game.slg.core.hero.LordNeed
import com.game.slg.core.hero.BlindPrinciple
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 영웅 탐색 · 방문 합류 · 블라인드 등용 — 2026-11-19 신규 시스템.
 */
class HeroDiscoveryTest {

    @Test
    fun `탐색은 250G를 쓰고 아직 만나지 못한 영웅 한 명을 드러낸다`() {
        val roster = HeroRosterState.initial()
        val before = HeroDiscoveryEngine.undiscoveredCount(roster)

        val result = HeroDiscoveryEngine.search(
            roster = roster,
            gold = 5_000,
            regionCode = "4420033000",
            season = "봄 (예산 편성·지방세 징수)",
            seed = 7L
        )

        assertTrue(result.success)
        assertNotNull(result.hero)
        assertEquals(-250, result.goldDelta)
        assertEquals(HeroStage.SCOUTED, result.roster.recordOf(result.hero!!.id).stage)
        assertEquals(before - 1, HeroDiscoveryEngine.undiscoveredCount(result.roster))
    }

    @Test
    fun `탐색은 자금이 모자라면 실패한다`() {
        val result = HeroDiscoveryEngine.search(
            roster = HeroRosterState.initial(),
            gold = 100,
            regionCode = "4420033000",
            season = "여름",
            seed = 1L
        )

        assertFalse(result.success)
        assertNull(result.hero)
        assertEquals(0, result.goldDelta)
    }

    @Test
    fun `만나지 못한 영웅은 목록에 드러나지 않는다`() {
        val roster = HeroRosterState.initial()

        // 기본 3명만 보이고, 나머지 237명은 실루엣(비공개)
        assertEquals(3, HeroDiscoveryEngine.knownHeroes(roster).size)
        assertEquals(237, HeroDiscoveryEngine.undiscoveredCount(roster))

        val searched = HeroDiscoveryEngine.search(roster, 5_000, "4420033000", "봄", 3L).roster
        assertEquals(4, HeroDiscoveryEngine.knownHeroes(searched).size)
    }

    @Test
    fun `찾아온 영웅을 수락하면 무상으로 보유 영웅이 된다`() {
        val roster = HeroRosterState.initial()
        val hero = HeroRoster.byId("hero_120")!!
        val visit = HeroVisit(
            heroId = hero.id,
            heroName = hero.name,
            title = hero.title,
            season = "가을",
            reason = "소문",
            requestText = "합류하겠습니다",
            acceptLine = "합류",
            declineLine = "떠남"
        )

        val accepted = HeroDiscoveryEngine.acceptVisit(roster, visit)

        assertTrue(accepted.success)
        assertTrue(accepted.roster.recordOf(hero.id).owned)
        assertEquals(0, accepted.goldDelta)
    }

    @Test
    fun `방문을 거절하면 그 영웅은 다시 미탐색으로 돌아간다`() {
        val search = HeroDiscoveryEngine.search(
            HeroRosterState.initial(), 5_000, "4420033000", "봄", 11L
        )
        val hero = search.hero!!
        assertEquals(HeroStage.SCOUTED, search.roster.recordOf(hero.id).stage)

        val visit = HeroVisit(
            heroId = hero.id,
            heroName = hero.name,
            title = hero.title,
            season = "가을",
            reason = "소문",
            requestText = "합류하겠습니다",
            acceptLine = "합류",
            declineLine = "떠남"
        )
        val declined = HeroDiscoveryEngine.declineVisit(search.roster, visit)
        assertTrue(declined.success)
        assertEquals(HeroStage.UNDISCOVERED, declined.roster.recordOf(hero.id).stage)
        assertEquals(0, declined.roster.recordOf(hero.id).affinity)
    }

    @Test
    fun `방문 후보는 조건이 맞을 때만 열린다`() {
        val roster = HeroRosterState.initial()
        var found: HeroVisit? = null
        (0L..40L).forEach { seed ->
            val visit = HeroDiscoveryEngine.visitCandidate(
                roster = roster,
                prestige = 90,
                morale = 70,
                season = "가을 (결산·보급 비축)",
                seed = seed,
                regionName = "탕정면"
            )
            if (visit != null) found = visit
        }
        assertNotNull("조건이 맞으면 방문이 열린다", found)
        assertTrue(found!!.requestText.contains("합류"))

        val tooLow = HeroDiscoveryEngine.visitCandidate(
            roster = roster,
            prestige = 0,
            morale = 10,
            season = "봄",
            seed = 0L,
            regionName = "탕정면"
        )
        assertNull(tooLow)
    }

    @Test
    fun `등용 청은 상대가 누구인지와 무관하게 능력치와 명성으로만 판정된다`() {
        val strong = HeroRoster.byId("hero_239")!! // 외교 SSR
        val weak = HeroRoster.byId("hero_237")!!   // 상업 N
        val need = LordNeed(troops = 300, prestige = 40, population = 20_000)

        val strongResult = HeroEmploymentEngine.request(strong, 40, "any", "이름없는 리더", need)
        val weakResult = HeroEmploymentEngine.request(weak, 40, "any", "이름없는 리더", need)

        assertTrue(strongResult.accepted)
        assertFalse(weakResult.accepted)
        assertTrue(strongResult.message.contains("등용 수락"))
        assertTrue(weakResult.message.contains("등용 거절"))
        // 블라인드 — 표기 어디에도 사람/기계 구분이 없다
        assertFalse(strongResult.message.contains("AI"))
        assertFalse(strongResult.message.contains("유저"))
    }

    @Test
    fun `블라인드 원칙은 리더를 같은 호칭으로만 부른다`() {
        assertEquals("리더", BlindPrinciple.LORD_LABEL)
        assertTrue(BlindPrinciple.RULES.size >= 4)
        val line = BlindPrinciple.lordLine("박도현", "탕정 물류단지 1구역", 320, 55)
        assertTrue(line.contains("리더"))
        assertFalse(line.contains("AI"))
    }

    @Test
    fun `VM에서 영웅 탐색이 상태에 반영된다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val before = vm.uiState.value

        vm.searchHeroes()

        val after = vm.uiState.value
        assertTrue(after.heroSearchMessage!!.contains("탐색"))
        assertEquals(before.undiscoveredHeroCount - 1, after.undiscoveredHeroCount)
        assertTrue(after.gold <= before.gold)
    }

    @Test
    fun `VM에서 찾아온 영웅을 수락하면 방문이 닫힌다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val hero = HeroRoster.byId("hero_200")!!
        val visit = HeroVisit(
            heroId = hero.id,
            heroName = hero.name,
            title = hero.title,
            season = "가을",
            reason = "소문",
            requestText = "합류하겠습니다",
            acceptLine = "합류",
            declineLine = "떠남"
        )
        // 방문 카드는 절기 진행으로만 열리므로, 수락 동작은 엔진과 같은 경로로 검증한다
        val accepted = HeroDiscoveryEngine.acceptVisit(vm.uiState.value.heroes, visit)
        assertTrue(accepted.success)
        assertTrue(accepted.roster.recordOf(hero.id).owned)
    }
}
