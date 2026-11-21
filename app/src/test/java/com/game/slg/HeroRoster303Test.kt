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
 * 현대 영웅 303명 로스터 정합성 (2026-11-21).
 *
 * 구글 드라이브 `SLG_현대영웅_케릭터` 의 통합 로스터(roster_001_303.json)를
 * 게임 카탈로그로 옮겼을 때 번호·이름·역할·등급·능력치·특기·초상화가 어긋나지 않는지 본다.
 * 241~260번은 다국적 배치, 261~303번은 사용자가 올린 합성 시트를 2:3 세로로 분할한 배치다.
 */
class HeroRoster303Test {

    @Test
    fun `로스터는 1번부터 303번까지 빈 번호 없이 이어진다`() {
        assertEquals(303, HeroRoster.ALL.size)
        assertEquals(
            (1..303).toList(),
            HeroRoster.ALL.map { it.number }
        )
        assertEquals(
            (1..303).map { "hero_" + it.toString().padStart(3, '0') },
            HeroRoster.ALL.map { it.id }
        )
    }

    @Test
    fun `이름은 303명 모두 다르다`() {
        val names = HeroRoster.ALL.map { it.name }
        assertEquals(303, names.distinct().size)
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
    fun `등급 분포와 역할 분포가 303명 로스터와 같다`() {
        val byGrade = HeroRoster.ALL.groupingBy { it.grade }.eachCount()
        assertEquals(42, byGrade[HeroGrade.SSR])
        assertEquals(94, byGrade[HeroGrade.SR])
        assertEquals(115, byGrade[HeroGrade.R])
        assertEquals(52, byGrade[HeroGrade.N])

        val byRole = HeroRoster.ALL.groupingBy { it.role }.eachCount()
        assertEquals(56, byRole[HeroRole.ADMIN])
        assertEquals(74, byRole[HeroRole.TECH])
        assertEquals(60, byRole[HeroRole.COMMERCE])
        assertEquals(63, byRole[HeroRole.OPS])
        assertEquals(45, byRole[HeroRole.DIPLOMACY])
        assertEquals(5, byRole[HeroRole.INTEL])
    }

    @Test
    fun `초상화는 303장이 모두 다른 리소스 이름으로 이어진다`() {
        val portraits = HeroRoster.PORTRAIT_NAMES
        assertEquals(303, portraits.size)
        assertEquals(303, portraits.distinct().size)
        assertEquals("slg_hero_001", portraits.first())
        assertEquals("slg_hero_303", portraits.last())
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
    fun `251~303번 53명은 드라이브 로스터의 이름 역할 등급 능력치를 그대로 갖는다`() {
        // 251~260: 외국인 배치 9 · 261~280: 시트 분할 10 · 281~303: 시트 분할 11
        val expected = mapOf(
            251 to listOf("라미 알사예드", HeroRole.INTEL, HeroGrade.SSR, 72),
            252 to listOf("루아나 파이샹", HeroRole.COMMERCE, HeroGrade.SR, 86),
            253 to listOf("알바로 니에토", HeroRole.TECH, HeroGrade.SSR, 96),
            254 to listOf("마리엘 산토스", HeroRole.DIPLOMACY, HeroGrade.SR, 90),
            255 to listOf("콰메 멘사", HeroRole.ADMIN, HeroGrade.R, 82),
            256 to listOf("에바 노바크", HeroRole.OPS, HeroGrade.R, 84),
            257 to listOf("에밀리아노 바르가스", HeroRole.COMMERCE, HeroGrade.N, 72),
            258 to listOf("하야트 알만수리", HeroRole.TECH, HeroGrade.SR, 88),
            259 to listOf("시포 응코시", HeroRole.OPS, HeroGrade.R, 80),
            260 to listOf("안네커 데브리스", HeroRole.ADMIN, HeroGrade.SR, 86),
            261 to listOf("한태호", HeroRole.OPS, HeroGrade.SR, 84),
            262 to listOf("서지민", HeroRole.TECH, HeroGrade.R, 78),
            263 to listOf("권정훈", HeroRole.COMMERCE, HeroGrade.SSR, 94),
            264 to listOf("배수진", HeroRole.TECH, HeroGrade.N, 71),
            265 to listOf("최진혁", HeroRole.ADMIN, HeroGrade.SR, 83),
            266 to listOf("이선영", HeroRole.TECH, HeroGrade.R, 83),
            267 to listOf("강민혁", HeroRole.OPS, HeroGrade.SR, 83),
            268 to listOf("박지우", HeroRole.TECH, HeroGrade.N, 71),
            269 to listOf("윤혜경", HeroRole.DIPLOMACY, HeroGrade.SR, 88),
            270 to listOf("신동화", HeroRole.TECH, HeroGrade.R, 80),
            271 to listOf("송은채", HeroRole.DIPLOMACY, HeroGrade.SSR, 90),
            272 to listOf("정태준", HeroRole.COMMERCE, HeroGrade.SR, 85),
            273 to listOf("백시현", HeroRole.OPS, HeroGrade.R, 83),
            274 to listOf("고상욱", HeroRole.ADMIN, HeroGrade.N, 71),
            275 to listOf("임유나", HeroRole.TECH, HeroGrade.SR, 88),
            276 to listOf("하진우", HeroRole.OPS, HeroGrade.R, 78),
            277 to listOf("조은주", HeroRole.ADMIN, HeroGrade.SR, 85),
            278 to listOf("김도윤", HeroRole.TECH, HeroGrade.SSR, 88),
            279 to listOf("민서연", HeroRole.COMMERCE, HeroGrade.N, 76),
            280 to listOf("홍성태", HeroRole.OPS, HeroGrade.SR, 89),
            281 to listOf("박상호", HeroRole.ADMIN, HeroGrade.SR, 85),
            282 to listOf("최지아", HeroRole.TECH, HeroGrade.R, 78),
            283 to listOf("오상진", HeroRole.DIPLOMACY, HeroGrade.SSR, 88),
            284 to listOf("강유정", HeroRole.OPS, HeroGrade.N, 73),
            285 to listOf("윤준혁", HeroRole.OPS, HeroGrade.SR, 88),
            286 to listOf("신예은", HeroRole.TECH, HeroGrade.R, 79),
            287 to listOf("권혜진", HeroRole.TECH, HeroGrade.SSR, 92),
            288 to listOf("한승우", HeroRole.COMMERCE, HeroGrade.N, 75),
            289 to listOf("배진호", HeroRole.ADMIN, HeroGrade.SR, 88),
            290 to listOf("지승우", HeroRole.OPS, HeroGrade.SSR, 92),
            291 to listOf("조현아", HeroRole.COMMERCE, HeroGrade.N, 76),
            292 to listOf("민경태", HeroRole.TECH, HeroGrade.SR, 85),
            293 to listOf("백하린", HeroRole.TECH, HeroGrade.R, 80),
            294 to listOf("고태훈", HeroRole.OPS, HeroGrade.SR, 89),
            295 to listOf("황동주", HeroRole.COMMERCE, HeroGrade.SR, 85),
            296 to listOf("선우현", HeroRole.OPS, HeroGrade.SSR, 91),
            297 to listOf("강태석", HeroRole.OPS, HeroGrade.N, 74),
            298 to listOf("신서윤", HeroRole.TECH, HeroGrade.SR, 87),
            299 to listOf("정유진", HeroRole.COMMERCE, HeroGrade.R, 78),
            300 to listOf("안재현", HeroRole.TECH, HeroGrade.SR, 86),
            301 to listOf("차영주", HeroRole.DIPLOMACY, HeroGrade.SSR, 94),
            302 to listOf("유동혁", HeroRole.COMMERCE, HeroGrade.N, 76),
            303 to listOf("오창식", HeroRole.ADMIN, HeroGrade.SR, 84)
        )
        assertEquals(53, expected.size)
        val newBatch = (251..303).map { HeroRoster.byId("hero_" + it.toString().padStart(3, '0')) }
        assertTrue(newBatch.all { it != null })
        assertEquals(53, newBatch.map { it!!.id }.distinct().size)

        expected.forEach { (no, values) ->
            val hero = HeroRoster.byId("hero_" + no.toString().padStart(3, '0'))
            assertNotNull("$no 번 영웅 없음", hero)
            assertEquals(values[0], hero!!.name)
            assertEquals(values[1], hero.role)
            assertEquals(values[2], hero.grade)
            assertEquals("$no 번 주 능력치", values[3], hero.stats.primaryOf(hero.role))
            assertTrue("$no 번 효과", !hero.bonus.isEmpty)
            assertEquals("slg_hero_" + no.toString().padStart(3, '0'), hero.portraitName)
        }
    }

    @Test
    fun `251~303번은 출신과 민족을 갖고 프로필 줄에 출신이 보인다`() {
        (251..303).forEach { no ->
            val hero = HeroRoster.byId("hero_$no")!!
            assertTrue("$no 번 ${hero.name} 출신 없음", hero.origin.isNotBlank())
            assertTrue("$no 번 ${hero.name} 민족 없음", hero.ethnicity.isNotBlank())
            assertTrue("$no 번 ${hero.name} 프로필 줄", hero.profileLine.contains(hero.origin))
        }

        // 251~260은 외국인 배치, 261~303은 한국인 시트 분할 배치
        val foreign = (251..260).map { HeroRoster.byId("hero_$it")!! }
        assertTrue("251~260번은 한국인이 아니다", foreign.none { it.ethnicity.startsWith("한국") })
        val sheet = (261..303).map { HeroRoster.byId("hero_$it")!! }
        assertTrue("261~303번은 한국인 배치", sheet.all { it.ethnicity == "한국인" })
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

        assertEquals(5, intelHeroes.size)
        intelHeroes.forEach { hero ->
            assertTrue("${hero.name} 첩보 효과", hero.bonus.intelBonus > 0)
            assertEquals(0, hero.bonus.goldPercent)
            assertEquals(0, hero.bonus.combatPercent)
        }
    }

    @Test
    fun `303번 영웅도 탐색 설득 고용 3단계로 영입된다`() {
        val roster = HeroRosterState.initial()
        val target = HeroRoster.byId("hero_303")!!
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
