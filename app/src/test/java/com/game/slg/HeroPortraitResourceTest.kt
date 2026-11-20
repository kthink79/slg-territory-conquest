package com.game.slg

import com.game.slg.core.hero.HeroRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 초상화 리소스 정합성 — 250명 로스터와 `drawable-nodpi/slg_hero_*.jpg` 가 1대1인지 본다.
 *
 * 단위 테스트는 카탈로그의 이름만 검사하므로, 실제 탑재 파일이 빠지거나 규격(2:3 · 540x810)이
 * 어긋난 배치가 들어오는 것을 여기서 막는다. 241~250번 추가분(2026-11-20)까지 포함한다.
 */
class HeroPortraitResourceTest {

    private val portraitDir: File by lazy {
        listOf(
            File("src/main/res/drawable-nodpi"),
            File("app/src/main/res/drawable-nodpi")
        ).firstOrNull { it.isDirectory } ?: File("src/main/res/drawable-nodpi")
    }

    @Test
    fun `초상화 폴더에 250장이 있다`() {
        assertTrue("초상화 폴더 없음: ${portraitDir.absolutePath}", portraitDir.isDirectory)
        val files = portraitDir.listFiles { f -> f.name.startsWith("slg_hero_") } ?: emptyArray()

        assertEquals(250, files.size)
        assertEquals(
            (1..250).map { "slg_hero_" + it.toString().padStart(3, '0') + ".jpg" }.sorted(),
            files.map { it.name }.sorted()
        )
    }

    @Test
    fun `모든 초상화가 2대3 규격 540x810 JPEG 이다`() {
        HeroRoster.PORTRAIT_NAMES.forEach { name ->
            val file = File(portraitDir, "$name.jpg")
            assertTrue("$name 파일 없음", file.isFile)
            assertTrue("$name 용량 ${file.length()}B — 빈 파일", file.length() > 10_000)

            val bytes = file.readBytes()
            assertEquals("$name JPEG 시그니처", 0xFF, bytes[0].toInt() and 0xFF)
            assertEquals("$name JPEG 시그니처", 0xD8, bytes[1].toInt() and 0xFF)

            val size = jpegSize(bytes)
            assertNotNull("$name JPEG 헤더 판독 실패", size)
            assertEquals("$name 가로", 540, size!!.first)
            assertEquals("$name 세로", 810, size.second)
        }
    }

    @Test
    fun `새로 넣은 241~250번 초상화도 같은 규격이다`() {
        (241..250).forEach { no ->
            val file = File(portraitDir, "slg_hero_$no.jpg")
            assertTrue("$no 번 초상화 없음", file.isFile)
            val size = jpegSize(file.readBytes())
            assertNotNull("$no 번 JPEG 헤더 판독 실패", size)
            assertEquals(540, size!!.first)
            assertEquals(810, size.second)
        }
    }

    /** JPEG SOF 세그먼트에서 (가로, 세로) 를 읽는다 — 외부 라이브러리 없이 헤더만 본다 */
    private fun jpegSize(bytes: ByteArray): Pair<Int, Int>? {
        var i = 2
        while (i + 8 < bytes.size) {
            if ((bytes[i].toInt() and 0xFF) != 0xFF) {
                i++
                continue
            }
            val marker = bytes[i + 1].toInt() and 0xFF
            if (marker == 0xD8 || marker == 0xD9 || marker == 0x01 || marker in 0xD0..0xD7) {
                i += 2
                continue
            }
            val length = ((bytes[i + 2].toInt() and 0xFF) shl 8) or (bytes[i + 3].toInt() and 0xFF)
            val isSof = marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC
            if (isSof) {
                val height = ((bytes[i + 5].toInt() and 0xFF) shl 8) or (bytes[i + 6].toInt() and 0xFF)
                val width = ((bytes[i + 7].toInt() and 0xFF) shl 8) or (bytes[i + 8].toInt() and 0xFF)
                return width to height
            }
            if (length < 2) return null
            i += 2 + length
        }
        return null
    }
}
