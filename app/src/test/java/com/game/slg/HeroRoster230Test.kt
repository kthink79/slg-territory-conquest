package com.game.slg

import com.game.slg.core.hero.HeroEngine
import com.game.slg.core.hero.HeroGrade
import com.game.slg.core.hero.HeroRole
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.hero.HeroStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 현대 영웅 230명 로스터 정합성 — 2026-11-18 일괄 탑재분.
 *
 * 구글 드라이브 `SLG_현대영웅_케릭터` 의 통합 로스터(roster_001_230.json)를
 * 게임 카탈로그로 옮겼을 때 번호·이름·역할·등급·능력치·특기·초상화가 어긋나지 않는지 본다.
 */
class HeroRoster230Test {

    @Test
    fun `로스터는 1번부터 230번까지 빈 번호 없이 이어진다`() {
        assertEquals(230, HeroRoster.ALL.size)
        assertEquals(
            (1..230).toList(),
            HeroRoster.ALL.map { it.number }
        )
        assertEquals(
            (1..230).map { "hero_" + it.toString().padStart(3, '0') },
            HeroRoster.ALL.map { it.id }
        )
    }

    @Test
    fun `이름은 230명 모두 다르다`() {
        val names = HeroRoster.ALL.map { it.name }
        assertEquals(230, names.distinct().size)
        assertTrue(names.all { it.isNotBlank() })
    }

    @Test
    fun `모든 영웅이 등급과 5개 능력치와 특기를 갖는다`() {
        HeroRoster.ALL.forEach { hero ->
            assertTrue("${hero.name} 등급", hero.grade in HeroGrade.entries)
            assertTrue("${hero.name} 특기 없음", hero.specialty.isNotBlank())
            assertTrue("${hero.name} 직업 없음", hero.title.isNotBlank())
            assertTrue("${hero.name} 대사 없음", hero.quote.isNotBlank())
            listOf(
                hero.stats.admin, hero.stats.tech, hero.stats.commerce,
                hero.stats.ops, hero.stats.diplomacy
            ).forEach { stat ->
                assertTrue("${hero.name} 능력치 $stat", stat in 1..100)
            }
            assertTrue("${hero.name} 능력치 합계", hero.stats.total in 60..500)
        }
    }

    @Test
    fun `등급 분포와 역할 분포가 로스터와 같다`() {
        val byGrade = HeroRoster.ALL.groupingBy { it.grade }.eachCount()
        assertEquals(28, byGrade[HeroGrade.SSR])
        assertEquals(65, byGrade[HeroGrade.SR])
        assertEquals(97, byGrade[HeroGrade.R])
        assertEquals(40, byGrade[HeroGrade.N])

        val byRole = HeroRoster.ALL.groupingBy { it.role }.eachCount()
        assertEquals(45, byRole[HeroRole.ADMIN])
        assertEquals(53, byRole[HeroRole.TECH])
        assertEquals(46, byRole[HeroRole.COMMERCE])
        assertEquals(46, byRole[HeroRole.OPS])
        assertEquals(36, byRole[HeroRole.DIPLOMACY])
        assertEquals(4, byRole[HeroRole.INTEL])
    }

    @Test
    fun `초상화는 230장이 모두 다른 리소스 이름으로 이어진다`() {
        val portraits = HeroRoster.PORTRAIT_NAMES
        assertEquals(230, portraits.size)
        assertEquals(230, portraits.distinct().size)
        assertEquals("slg_hero_001", portraits.first())
        assertEquals("slg_hero_230", portraits.last())
    }

    @Test
    fun `등급이 높을수록 고용비과 필요 명성이 높다`() {
        // 기본 3명은 고용비 0G 로 시작하므로 로스터 11번 이후로 비교한다
        val rated = HeroRoster.ALL.drop(10)
        val ssr = rated.filter { it.grade == HeroGrade.SSR }
        val r = rated.filter { it.grade == HeroGrade.R }

        assertTrue(ssr.minOf { it.hireCost } > r.maxOf { it.hireCost })
        assertTrue(ssr.map { it.requiredPrestige }.average() > r.map { it.requiredPrestige }.average())
    }

    @Test
    fun `11번 이후 영웅도 배치 효과가 비어 있지 않다`() {
        HeroRoster.ALL.drop(10).forEach { hero ->
            assertTrue("${hero.name} 효과 없음", !hero.bonus.isEmpty)
            assertTrue("${hero.name} 효과 한 줄", hero.effectLabel.isNotBlank())
            assertTrue("${hero.name} 필요 명성", hero.requiredPrestige in 1..100)
            assertTrue("${hero.name} 고용비", hero.hireCost >= 300)
        }
    }

    @Test
    fun `정보 역할은 기술 능력치로 효과가 계산된다`() {
        val intelHeroes = HeroRoster.ALL.filter { it.role == HeroRole.INTEL }

        assertEquals(4, intelHeroes.size)
        intelHeroes.forEach { hero ->
            assertTrue("${hero.name} 첩보 효과", hero.bonus.intelBonus > 0)
            assertEquals(0, hero.bonus.goldPercent)
            assertEquals(0, hero.bonus.combatPercent)
        }
    }

    @Test
    fun `새 영웅도 탐색 설득 고용 3단계로 영입된다`() {
        val roster = HeroRosterState.initial()
        val target = HeroRoster.byId("hero_230")!!
        assertNotNull(target)

        val scouted = HeroEngine.scout(roster, target.id, gold = 5_000)
        assertTrue(scouted.success)

        val persuaded = HeroEngine.persuade(scouted.roster, target.id, prestige = 100, diplomacyBonus = 30)
        assertTrue(persuaded.success)

        val hired = HeroEngine.hire(persuaded.roster, target.id, gold = 9_000)
        assertTrue(hired.success)
        assertTrue(hired.roster.recordOf(target.id).owned)
    }

    @Test
    fun `능력치 요약은 다섯 항목을 모두 보여 준다`() {
        val line = HeroStats(admin = 82, tech = 48, commerce = 55, ops = 21, diplomacy = 74).summary()

        assertTrue(line.contains("내정 82"))
        assertTrue(line.contains("기술 48"))
        assertTrue(line.contains("상업 55"))
        assertTrue(line.contains("작전 21"))
        assertTrue(line.contains("외교 74"))
    }
}
