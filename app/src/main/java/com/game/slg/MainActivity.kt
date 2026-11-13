package com.game.slg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.engine.*
import com.game.slg.core.model.*
import com.game.slg.ui.SlgViewModel

class MainActivity : ComponentActivity() {
    private val combatEngine = CombatEngine()
    private val councilEngine = CouncilEngine()
    private val infiltrationEngine = InfiltrationEngine()
    private val seasonEngine = SeasonEngine()
    private val logisticsEngine = LogisticsEngine()
    private val diplomacyEngine = DiplomacyEngine()

    private val viewModel by lazy {
        SlgViewModel(
            seasonEngine = seasonEngine,
            logisticsEngine = logisticsEngine,
            diplomacyEngine = diplomacyEngine
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SlgMainScreen(
                    viewModel = viewModel,
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
    viewModel: SlgViewModel,
    combatEngine: CombatEngine,
    councilEngine: CouncilEngine,
    infiltrationEngine: InfiltrationEngine
) {
    val uiState by viewModel.uiState.collectAsState()
    var combatStatusMessage by remember { mutableStateOf<String?>(null) }

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

        // Status overview banner (Current season, treasury gold/grain, supply connectivity)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A212D))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "현재 절기: ${uiState.currentSeason.displayName}",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("국고 금화: ${uiState.treasuryGold} 냥", color = Color.White, fontSize = 14.sp)
                    Text("군량 비축: ${uiState.treasuryGrain} 석", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GIS 보급망 연결: ${if (uiState.isSupplyConnected) "원활 (정상)" else "차단 (적 점령)"}",
                    color = if (uiState.isSupplyConnected) Color(0xFF81C784) else Color(0xFFE57373),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable Simulation Controls
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. 4계절 경제 순환 & GIS 보급망",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.advanceSeason(dikeMaintenanceDone = true) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("다음 계절 진행 (치수 완비)")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.verifyAndDispatchConvoy(hostileCodes = emptySet()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("후방->전방 수송대 파견 (보급선 정상)")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.verifyAndDispatchConvoy(hostileCodes = setOf("t_mid_02")) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B2500)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("중간 관문 적 점령 시 보급선 차단 시뮬레이션")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. 외교 조약 & 인재 연계 첩보",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.signTreaty(TreatyType.NON_AGGRESSION_PACT, durationTurns = 4) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("인접 호족과 불가침 조약(4턴) 체결")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.conductEspionage(EspionageOpType.GATHER_INTEL) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("인재 첩보: 적진 군사 기밀 정찰")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.conductEspionage(EspionageOpType.INSTIGATE_DISSENT) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("인재 첩보: 적 진영 이간계 공작")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. 전투 / 잠입 / 의회 핵심 엔진",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                            combatStatusMessage = "[수성전 시뮬레이션 결과]\n${result.message}\n방어군 사상자: ${result.defenderLosses}명 (80% 격감)\n공격군 사상자: ${result.attackerLosses}명"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("요새 수성 우위 검증")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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
                            combatStatusMessage = "[히든 인재 하극상 결과]\n${coup.message}\n신규 명성: ${coup.newLordPrestige}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("히든 인재 하극상 찬탈 검증")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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
                            combatStatusMessage = "[호족 의회 투표 결과]\n가결 여부: ${result.isPassed} (찬성 ${result.votesInFavor} : 반대 ${result.votesAgainst})\n반란 트리거: ${result.isRebellionTriggered} (반대 호족 연합 봉기)"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("성주-호족 의회 & 반란 검증")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B22))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "시뮬레이션 실시간 출력 로그", color = Color(0xFF88A0C0), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = combatStatusMessage ?: uiState.statusLog,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}
