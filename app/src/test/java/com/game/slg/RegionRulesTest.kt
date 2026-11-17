package com.game.slg

import com.game.slg.core.region.AiLordGenerator
import com.game.slg.core.region.CommandProgress
import com.game.slg.core.region.ExpansionTier
import com.game.slg.core.region.LordCapacity
import com.game.slg.core.region.RegionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 지역 데이터 · 호족 정원(인구 1,000명당 1석) · AI 호족 명부 · 지휘 성장 규칙 테스트.
 */
class RegionRulesTest {

    private val tangjeong = RegionCatalog.defaultRegion()

    // ── 호족 정원 ──────────────────────────────────────────────

    @Test
    fun `호족 정원은 인구 1000명당 1석이다`() {
        assertEquals(1, LordCapacity.of(1000))
        assertEquals(48, LordCapacity.of(48604))
        assertEquals(99, LordCapacity.of(99090))
        assertEquals(1, LordCapacity.of(999)) // 999명 → 1석(최소)
        assertEquals(1, LordCapacity.of(0))
        assertEquals(LordCapacity.CITIZENS_PER_LORD, 1000)
    }

    @Test
    fun `탕정면 인구 48604명이면 호족 48석이다`() {
        assertEquals("탕정면", tangjeong.name)
        assertEquals(48604, tangjeong.population)
        assertEquals(48, tangjeong.lordCapacity)

        // 실제 사용자 예시(약 4만 5천 명)와 같은 자릿수
        assertTrue(tangjeong.lordCapacity in 45..49)
    }

    @Test
    fun `모든 지역의 정원은 최소 1석 이상이고 인구를 넘지 않는다`() {
        RegionCatalog.AREAS.forEach { area ->
            assertTrue("${area.name} 정원", area.lordCapacity >= 1)
            assertTrue("${area.name} 정원이 인구보다 많다", area.lordCapacity <= area.population)
        }
    }

    // ── 지역 데이터 ────────────────────────────────────────────

    @Test
    fun `지역 데이터는 법정동 코드와 인접 그래프가 일관된다`() {
        val codes = RegionCatalog.AREAS.map { it.code }
        assertEquals("법정동 코드 중복 없음", codes.size, codes.distinct().size)
        assertTrue("법정동 코드 10자리", codes.all { it.length == 10 })
        assertTrue("인구 양수", RegionCatalog.AREAS.all { it.population > 0 })

        RegionCatalog.AREAS.forEach { area ->
            area.neighbors.forEach { neighbor ->
                val other = RegionCatalog.area(neighbor)
                assertTrue("${area.name} 인접 ${neighbor} 누락", other != null)
                assertTrue(
                    "인접 관계는 양방향이어야 한다 (${area.name} → ${other!!.name})",
                    other.neighbors.contains(area.code)
                )
            }
            assertTrue("${area.name} 인접 거점이 없다", area.neighbors.isNotEmpty())
        }
    }

    @Test
    fun `데이터에는 충남 아산과 수도권 주요 읍면동이 포함된다`() {
        val names = RegionCatalog.AREAS.map { it.name }
        listOf("탕정면", "배방읍", "음봉면", "불당1동", "매탄3동", "광교1동", "역삼1동", "판교동").forEach {
            assertTrue("$it 누락", names.contains(it))
        }
        assertTrue("시·군·구 묶음", RegionCatalog.GROUPS.size >= 9)
        assertTrue("시·도", RegionCatalog.PROVINCES.contains("충청남도"))
        assertTrue("시·도", RegionCatalog.PROVINCES.contains("서울특별시"))
        assertTrue("전체 인구", RegionCatalog.totalPopulation() > 1_200_000)
    }

    @Test
    fun `탕정면에서 인접 읍면동은 hop1, 그 다음은 hop2다`() {
        val hops = RegionCatalog.hopDistances(tangjeong.code)

        assertEquals(0, hops[tangjeong.code])
        assertEquals(1, hops["4420034000"]) // 배방읍
        assertEquals(1, hops["4420035000"]) // 음봉면
        assertEquals(1, hops["4420036000"]) // 염치읍

        // 두 다리: 온양1동(염치 경유), 온양2동(음봉 경유), 천안 불당1동(배방 경유)
        assertEquals(2, hops["4420051000"])
        assertEquals(2, hops["4420052000"])
        assertEquals(2, hops["4413151000"])
        // 세 다리: 천안 백석동(불당1동 경유)
        assertEquals(3, hops["4413153000"])
    }

    // ── AI 호족 명부 ───────────────────────────────────────────

    @Test
    fun `AI 호족은 호족 정원만큼 생성된다`() {
        val lords = AiLordGenerator.generate(tangjeong)

        assertEquals(tangjeong.lordCapacity, lords.size)
        assertEquals(lords.size, lords.map { it.id }.distinct().size)
        assertEquals("이름은 겹치지 않는다", lords.size, lords.map { it.name }.distinct().size)
        assertTrue(lords.all { it.regionCode == tangjeong.code })
        assertTrue(lords.all { it.troops > 0 && it.gold > 0 && it.grain > 0 && it.prestige > 0 })
        assertTrue(lords.all { it.stronghold.startsWith(tangjeong.name) })
    }

    @Test
    fun `같은 지역과 시드는 항상 같은 명부를 만든다`() {
        val first = AiLordGenerator.generate(tangjeong)
        val second = AiLordGenerator.generate(tangjeong)

        assertEquals(first.map { it.name }, second.map { it.name })
        assertEquals(first.map { it.troops }, second.map { it.troops })
        assertEquals(first.map { it.trait }, second.map { it.trait })
    }

    @Test
    fun `다른 지역은 다른 호족 명부를 만든다`() {
        val gangnam = RegionCatalog.area("1168051000")!! // 역삼1동
        val a = AiLordGenerator.generate(tangjeong)
        val b = AiLordGenerator.generate(gangnam)

        assertEquals(gangnam.lordCapacity, b.size)
        assertEquals(34, gangnam.lordCapacity)
        assertNotEquals(a.first().id, b.first().id)
        assertTrue(b.all { it.stronghold.startsWith(gangnam.name) })
    }

    @Test
    fun `AI 호족은 성향별 병과 특화를 가진다`() {
        val lords = AiLordGenerator.generate(tangjeong)

        assertTrue(lords.all { it.trait.preferredArm == it.arm })
        assertTrue("성향 다양성", lords.map { it.trait }.distinct().size >= 3)
    }

    // ── 지휘 성장 · 해금 단계 ──────────────────────────────────

    @Test
    fun `지휘 레벨이 해금 hop을 정한다`() {
        assertEquals(0, ExpansionTier.unlockedHop(1))
        assertEquals(1, ExpansionTier.unlockedHop(2))
        assertEquals(2, ExpansionTier.unlockedHop(3))
        assertEquals(3, ExpansionTier.unlockedHop(4))
        assertEquals(3, ExpansionTier.unlockedHop(5))
        assertEquals(4, ExpansionTier.unlockedHop(6))
        assertEquals(5, ExpansionTier.unlockedHop(8))
        assertEquals(5, ExpansionTier.unlockedHop(10))

        assertEquals("관할 구역", ExpansionTier.forLevel(1).label)
        assertEquals("인접 구역", ExpansionTier.forLevel(2).label)
        assertEquals("시·군 전역", ExpansionTier.forLevel(4).label)
        assertEquals("광역 생활권", ExpansionTier.forLevel(6).label)
    }

    @Test
    fun `경험치는 레벨을 넘기고 남은 값만 이월된다`() {
        val start = CommandProgress()
        assertEquals(1, start.level)
        assertEquals(0, start.xp)
        assertEquals(60, start.xpToNextLevel)

        val once = start.gainXp(CommandProgress.XP_OPERATION_WIN) // 70 XP
        assertEquals(2, once.level)
        assertEquals(10, once.xp)
        assertEquals(1, once.unlockedHop)

        val twice = once.gainXp(CommandProgress.XP_OPERATION_WIN)
        assertEquals(2, twice.level)
        assertEquals(80, twice.xp)

        // 여러 레벨을 한 번에 넘는다
        val boosted = CommandProgress().gainXp(600)
        assertTrue(boosted.level >= 5)
        assertTrue(boosted.unlockedHop >= 3)
    }

    @Test
    fun `최대 레벨에서는 경험치가 더 쌓이지 않는다`() {
        val maxed = CommandProgress().gainXp(100_000)

        assertEquals(CommandProgress.MAX_LEVEL, maxed.level)
        assertTrue(maxed.isMaxLevel)
        assertTrue(maxed.xp < maxed.xpToNextLevel)
        assertEquals(maxed, maxed.gainXp(500))
    }

    @Test
    fun `다음 해금 안내는 남은 단계를 알려 준다`() {
        val start = CommandProgress()
        assertTrue(start.nextUnlockLabel()!!.contains("Lv.2"))
        assertTrue(start.nextUnlockLabel()!!.contains("인접 구역"))
        assertEquals(null, CommandProgress(level = CommandProgress.MAX_LEVEL).nextUnlockLabel())
    }

    @Test
    fun `탕정면에서 단계별로 열리는 거점 고리가 정해져 있다`() {
        val hops = RegionCatalog.hopDistances(tangjeong.code)
        val byHop = hops.entries
            .groupBy({ it.value }, { RegionCatalog.area(it.key)?.name ?: it.key })
            .mapValues { it.value.sorted() }

        assertEquals(listOf("배방읍", "염치읍", "음봉면"), byHop[1])
        assertTrue(byHop[2]!!.containsAll(listOf("불당1동", "송악면", "온양1동", "온양2동")))
        assertTrue(byHop[3]!!.contains("백석동"))
        // 같은 충남 권역(아산·천안)은 모두 이어진다
        val chungnam = RegionCatalog.AREAS.filter { it.province == "충청남도" }
        assertTrue("충남 권역 연결", chungnam.all { hops.containsKey(it.code) })
        println("탕정면 확장 고리: " + byHop.toSortedMap())

        // 수도권 권역도 수원에서 모두 이어진다 (부산·대구는 별도 생활권)
        val metroHops = RegionCatalog.hopDistances("4111154000", limit = 30)
        val metro = RegionCatalog.AREAS.filter { it.province in setOf("경기도", "서울특별시") }
        assertTrue("수도권 권역 연결: " + metro.filter { !metroHops.containsKey(it.code) }.joinToString { it.name }, metro.all { metroHops.containsKey(it.code) })
    }
}
