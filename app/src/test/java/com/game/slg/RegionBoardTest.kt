package com.game.slg

import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.region.LeaderBond
import com.game.slg.core.region.LeaderStatus
import com.game.slg.core.region.RegionBoardBuilder
import com.game.slg.core.region.RegionLeaderSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 지역 세부맵 배치·관계 테스트.
 *
 * 사용자 요청(2026-11-24): *"지역내 각 리더의 배치와 현황을 볼 수 있는 세부맵… 3개의 리더를
 * 병합시키거나 종속시켰다면 그런 것도 표현… 좌우 상하 스크롤"*
 */
class RegionBoardTest {

    private fun seed(
        id: String,
        name: String,
        troops: Int = 300,
        isPlayer: Boolean = false,
        federation: TerritoryStatus? = null,
        aiBond: LeaderBond? = null,
        masterId: String? = null,
        note: String = ""
    ) = RegionLeaderSeed(
        id = id,
        name = name,
        stronghold = "탕정면 $name 구역",
        traitLabel = "균형형",
        traitIcon = "⚖",
        troops = troops,
        gold = 500,
        food = 800,
        prestige = 45,
        isPlayer = isPlayer,
        federation = federation,
        aiBond = aiBond,
        masterId = masterId,
        note = note
    )

    @Test
    fun `내 세력이 첫 자리에 앉고 우리 편입 세력이 바로 뒤에 붙는다`() {
        val board = RegionBoardBuilder.build(
            "4420033000", "탕정면",
            listOf(
                seed("lord_player", "이광수", isPlayer = true),
                seed("ai_1", "김도현", federation = TerritoryStatus.DIRECT),
                seed("ai_2", "이서준", federation = TerritoryStatus.VASSAL),
                seed("ai_3", "박민재", federation = TerritoryStatus.AUTONOMOUS),
                seed("ai_4", "최하준"),
                seed("ai_5", "정소영")
            )
        )

        assertEquals(6, board.totalCount)
        assertEquals(LeaderStatus.PLAYER, board.leaders.first().status)
        assertEquals(3, board.ourFederationCount)
        assertEquals(3, board.leaders.count { it.status.isOurs && it.status != LeaderStatus.PLAYER })
        assertEquals(LeaderStatus.DIRECT, board.leaderOf("ai_1")!!.status)
        assertEquals(LeaderStatus.VASSAL, board.leaderOf("ai_2")!!.status)
        assertEquals(LeaderStatus.AUTONOMOUS, board.leaderOf("ai_3")!!.status)
        assertEquals(LeaderStatus.INDEPENDENT, board.leaderOf("ai_4")!!.status)

        // 편입 세력은 모두 내 세력을 우두머리로 가리키고 연결선이 그어진다
        listOf("ai_1", "ai_2", "ai_3").forEach { id ->
            val info = board.leaderOf(id)!!
            assertEquals("lord_player", info.masterId)
            assertNotNull(board.bonds.firstOrNull { it.servantId == id && it.masterId == "lord_player" })
        }
        assertEquals(3, board.bonds.size)
        assertEquals(RegionBoardBuilder.COLUMNS, board.columns)
    }

    @Test
    fun `AI 병합과 종속이 연결선으로 표시된다`() {
        val board = RegionBoardBuilder.build(
            "4420033000", "탕정면",
            listOf(
                seed("lord_player", "이광수", isPlayer = true),
                seed("ai_1", "강도현", troops = 900),
                seed("ai_2", "약서준", troops = 200, aiBond = LeaderBond.MERGE, masterId = "ai_1"),
                seed("ai_3", "을민재", troops = 250, aiBond = LeaderBond.SUBORDINATION, masterId = "ai_1")
            )
        )

        assertEquals(LeaderStatus.MERGED, board.leaderOf("ai_2")!!.status)
        assertEquals(LeaderStatus.SUBORDINATED, board.leaderOf("ai_3")!!.status)
        assertEquals("강도현", board.leaderOf("ai_2")!!.masterName)
        assertEquals(1, board.mergedCount)
        assertEquals(1, board.subordinatedCount)
        assertEquals(2, board.boundCount)

        val mergeLine = board.bonds.first { it.servantId == "ai_2" }
        assertEquals(LeaderBond.MERGE, mergeLine.kind)
        val subLine = board.bonds.first { it.servantId == "ai_3" }
        assertEquals(LeaderBond.SUBORDINATION, subLine.kind)

        assertEquals("병합 1 · 종속 1 · 독립 1", board.relationSummary())
    }

    @Test
    fun `예속 리더는 우두머리 바로 옆자리에 앉는다`() {
        val board = RegionBoardBuilder.build(
            "4420033000", "탕정면",
            listOf(
                seed("lord_player", "이광수", isPlayer = true),
                seed("ai_1", "강도현", troops = 900),
                seed("ai_2", "약서준", troops = 200, aiBond = LeaderBond.MERGE, masterId = "ai_1"),
                seed("ai_3", "최하준", troops = 300)
            )
        )
        val ids = board.leaders.map { it.id }
        assertEquals("ai_1", ids[1])
        assertEquals("ai_2", ids[2])
        val masterSeat = board.seatOf("ai_1")!!
        val servantSeat = board.seatOf("ai_2")!!
        assertEquals(masterSeat.row, servantSeat.row)
        assertEquals(masterSeat.column + 1, servantSeat.column)
    }

    @Test
    fun `리더 48명이면 격자가 가로 4열 세로 13행이 되어 좌우상하 스크롤이 필요하다`() {
        val seeds = buildList {
            add(seed("lord_player", "이광수", isPlayer = true))
            (1..48).forEach { index ->
                add(seed("ai_$index", "리더$index", troops = 100 + index))
            }
        }
        val board = RegionBoardBuilder.build("4420033000", "탕정면", seeds)

        assertEquals(49, board.totalCount)
        assertEquals(4, board.columns)
        assertEquals(13, board.rows)
        assertTrue("가로 스크롤", board.columns > 2)
        assertTrue("세로 스크롤", board.rows > 4)
        assertEquals(49, board.seats.size)
        assertEquals("자리 중복 없음", 49, board.seats.map { it.column to it.row }.distinct().size)
        assertTrue("자리는 격자 안", board.seats.all { it.column in 0 until board.columns && it.row in 0 until board.rows })
    }

    @Test
    fun `지역이 다르면 추정 명부로 표시된다`() {
        val board = RegionBoardBuilder.build(
            "4420034000", "배방읍",
            listOf(seed("ai_1", "김도현", note = "정찰 전 추정 명부")),
            live = false
        )
        assertEquals(false, board.live)
        assertEquals("정찰 전 추정 명부", board.leaders.first().note)
    }
}
