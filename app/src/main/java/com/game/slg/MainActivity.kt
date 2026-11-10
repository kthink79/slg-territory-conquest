package com.game.slg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.engine.CombatEngine
import com.game.slg.core.engine.CouncilEngine
import com.game.slg.core.engine.InfiltrationEngine
import com.game.slg.core.model.*

class MainActivity : ComponentActivity() {
    private val combatEngine = CombatEngine()
    private val councilEngine = CouncilEngine()
    private val infiltrationEngine = InfiltrationEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SlgMainScreen(
                    combatEngine = combatEngine,
                    councilEngine = councilEngine,
                    infiltrationEngine = infiltrationEngine
                )
            }
        }
    }
}

@Composable
fun SlgMainScreen(
    combatEngine: CombatEngine,
    councilEngine: CouncilEngine,
    infiltrationEngine: InfiltrationEngine
) {
    var statusMessage by remember { mutableStateOf("팔도 영지전 SLG 시뮬레이터 준비 완료") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121418))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏰 천하통일: 팔도 영지전",
            color = Color(0xFFE2B755),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "현실 지명 GIS 행정구역 기반 영지 전략 SLG",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "시스템 핵심 메커니즘 검증",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val territory = Territory(
                            code = "4413125000",
                            name = "아산시 배방읍",
                            province = "충청남도",
                            cityCountyDistrict = "아산시",
                            population = 85000,
                            maxLords = 8,
                            terrain = TerrainType.MOUNTAIN,
                            fortress = Fortress(
                                level = FortressLevel.MOUNTAIN_FORTRESS,
                                durability = 1000,
                                maxDurability = 1000,
                                garrisonTroops = 1500
                            )
                        )
                        val result = combatEngine.resolveSiege(
                            attackers = 5000,
                            defenders = 1500,
                            territory = territory,
                            isSupplyLineCut = false
                        )
                        statusMessage = "[수성전 시뮬레이션 결과]\n${result.message}\n방어군 사상자: ${result.defenderLosses}명 (80% 격감)\n공격군 사상자: ${result.attackerLosses}명"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("1. 거점 방어 시설 수성 우위 검증")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val hiddenUser = Talent(
                            id = "user_01",
                            name = "이광수",
                            isPlayerHiddenTalent = true,
                            warPower = 88,
                            intelligence = 94,
                            leadership = 90,
                            politics = 85,
                            charm = 92
                        )
                        val lord = Lord(
                            id = "npc_lord_01",
                            name = "아산 호족 최장로",
                            isHumanPlayer = false,
                            territoryCode = "4413125000",
                            troops = 1200,
                            grain = 5000,
                            gold = 3000,
                            prestige = 80
                        )
                        infiltrationEngine.accumulatePower(hiddenUser, recruitedTroops = 1500, subvertProgress = 70)
                        val coup = infiltrationEngine.attemptCoup(hiddenUser, lord)
                        statusMessage = "[히든 인재 하극상 결과]\n${coup.message}\n신규 명성: ${coup.newLordPrestige}"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("2. 히든 인재 위장 잠입 & 하극상 찬탈 검증")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val lords = listOf(
                            Lord("l1", "호족 A", false, "t1", 500, 2000, 1000, 50),
                            Lord("l2", "호족 B", false, "t1", 400, 1500, 800, 40),
                            Lord("l3", "호족 C", false, "t1", 300, 1000, 500, 30)
                        )
                        val bill = CouncilBill(
                            id = "bill_01",
                            title = "전시 병력 60% 강제 차출령",
                            type = BillType.TROOP_LEVY,
                            proposedByCastellanId = "castellan_01",
                            troopLevyAmount = 300
                        )
                        val result = councilEngine.voteOnBill(bill, lords)
                        statusMessage = "[호족 의회 투표 결과]\n가결 여부: ${result.isPassed} (찬성 ${result.votesInFavor} : 반대 ${result.votesAgainst})\n반란 트리거: ${result.isRebellionTriggered} (반대 호족 연합 봉기)"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("3. 성주-호족 의회 & 반란 시스템 검증")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B22))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "시뮬레이션 출력 로그", color = Color(0xFF88A0C0), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = statusMessage, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}
