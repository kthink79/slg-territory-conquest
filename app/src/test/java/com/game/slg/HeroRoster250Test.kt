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
 * 현대 영웅 250명 로스터 정합성 — 2026-11-20 241~250번 추가분.
 *
 * 구글 드라이브 `SLG_현대영웅_케릭터` 의 통합 로스터(roster_001_250.json)를
 * 게임 카탈로그로 옮겼을 때 번호·이름·역할·등급·능력치·특기·초상화가 어긋나지 않는지 본다.
 * 241~250번은 다국적 배치로 `origin`·`ethnicity` 필드가 더해진 첫 배치다.
 */
class HeroRoster250Test {

    @Test
    fun `로스터는 1번부터 250번까지 빈 번호 없이 이어진다`() {
        assertEquals(250, HeroRoster.ALL.size)
        assertEquals(
            (1..250).toList(),
            HeroRoster.ALL.map { it.number }
        )
        assertEquals(
            (1..250).map { "hero_" + it.toString().padStart(3, '0') },
            HeroRoster.ALL.map { it.id }
        )
    }

    @Test
    fun `이름은 250명 모두 다르다`() {
        val names = HeroRoster.ALL.map { it.name }
        assertEquals(250, names.distinct().size)
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
    fun `등급 분포와 역할 분포가 250명 로스터와 같다`() {
        val byGrade = HeroRoster.ALL.groupingBy { it.grade }.eachCount()
        assertEquals(32, byGrade[HeroGrade.SSR])
        assertEquals(73, byGrade[HeroGrade.SR])
        assertEquals(103, byGrade[HeroGrade.R])
        assertEquals(42, byGrade[HeroGrade.N])

        val byRole = HeroRoster.ALL.groupingBy { it.role }.eachCount()
        assertEquals(48, byRole[HeroRole.ADMIN])
        assertEquals(58, byRole[HeroRole.TECH])
        assertEquals(50, byRole[HeroRole.COMMERCE])
        assertEquals(50, byRole[HeroRole.OPS])
        assertEquals(40, byRole[HeroRole.DIPLOMACY])
        assertEquals(4, byRole[HeroRole.INTEL])
    }

    @Test
    fun `초상화는 250장이 모두 다른 리소스 이름으로 이어진다`() {
        val portraits = HeroRoster.PORTRAIT_NAMES
        assertEquals(250, portraits.size)
        assertEquals(250, portraits.distinct().size)
        assertEquals("slg_hero_001", portraits.first())
        assertEquals("slg_hero_250", portraits.last())
    }

    @Test
    fun `231~250번은 로스터의 이름 역할 등급 능력치를 그대로 갖는다`() {
        val expected = mapOf(
            231 to listOf("도재훈", HeroRole.OPS, HeroGrade.SSR, 93),
            232 to listOf("백하영", HeroRole.TECH, HeroGrade.SR, 88),
            233 to listOf("권이현", HeroRole.COMMERCE, HeroGrade.R, 78),
            234 to listOf("노유정", HeroRole.ADMIN, HeroGrade.SR, 86),
            235 to listOf("서재우", HeroRole.TECH, HeroGrade.SR, 90),
            236 to listOf("하지수", HeroRole.DIPLOMACY, HeroGrade.R, 84),
            237 to listOf("명재훈", HeroRole.COMMERCE, HeroGrade.N, 70),
            238 to listOf("유하윤", HeroRole.OPS, HeroGrade.R, 78),
            239 to listOf("신도경", HeroRole.DIPLOMACY, HeroGrade.SSR, 92),
            240 to listOf("채민서", HeroRole.TECH, HeroGrade.SR, 86),
            241 to listOf("쿠로사와 렌", HeroRole.OPS, HeroGrade.SSR, 94),
            242 to listOf("응우옌 티 란", HeroRole.COMMERCE, HeroGrade.R, 84),
            243 to listOf("아르준 라구나단", HeroRole.TECH, HeroGrade.SR, 90),
            244 to listOf("엘리자베스 코널리", HeroRole.DIPLOMACY, HeroGrade.SR, 92),
            245 to listOf("마티아스 크뢰거", HeroRole.TECH, HeroGrade.SSR, 96),
            246 to listOf("카미유 르루아", HeroRole.COMMERCE, HeroGrade.N, 74),
            247 to listOf("치네두 오카포", HeroRole.ADMIN, HeroGrade.R, 80),
            248 to listOf("타니카 브룩스", HeroRole.OPS, HeroGrade.R, 82),
            249 to listOf("와니쿠 응구기", HeroRole.ADMIN, HeroGrade.SR, 88),
            250 to listOf("패트릭 도노번", HeroRole.DIPLOMACY, HeroGrade.SR, 88)
        )
        expected.forEach { (no, values) ->
            val hero = HeroRoster.byId("hero_" + no.toString().padStart(3, '0'))
            assertNotNull("$no 번 영웅 없음", hero)
            assertEquals(values[0], hero!!.name)
            assertEquals(values[1], hero.role)
            assertEquals(values[2], hero.grade)
            assertEquals(values[3], hero.stats.primaryOf(hero.role))
            assertTrue(hero.effectLabel.isNotBlank())
            assertEquals("slg_hero_" + no.toString().padStart(3, '0'), hero.portraitName)
        }
    }

    @Test
    fun `241~250번은 다국적 배치로 출신과 민족 필드를 갖는다`() {
        val newBatch = (241..250).map { HeroRoster.byId("hero_$it")!! }

        assertEquals(10, newBatch.size)
        newBatch.forEach { hero ->
            assertTrue("${hero.name} 출신 없음", hero.origin.isNotBlank())
            assertTrue("${hero.name} 민족 없음", hero.ethnicity.isNotBlank())
            // 출신은 카드의 프로필 줄에 함께 보인다
            assertTrue("${hero.name} 프로필 줄", hero.profileLine.contains(hero.origin))
        }
        // 한국인은 넣지 않았다 — 아시아계 3 · 백인 4 · 흑인 3
        assertEquals(3, newBatch.count { it.ethnicity.startsWith("아시아계") })
        assertEquals(4, newBatch.count { it.ethnicity.startsWith("백인") })
        assertEquals(3, newBatch.count { it.ethnicity.startsWith("흑인") })

        // 기존 240명은 필드가 비어 있어도 프로필 줄이 그대로다
        HeroRoster.ALL.take(240).forEach { hero ->
            assertTrue("${hero.name} 기존 배치 출신", hero.origin.isEmpty())
            assertTrue("${hero.name} 기존 배치 민족", hero.ethnicity.isEmpty())
        }
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
    fun `250번 영웅도 탐색 설득 고용 3단계로 영입된다`() {
        val roster = HeroRosterState.initial()
        val target = HeroRoster.byId("hero_250")!!
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
