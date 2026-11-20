package com.game.slg.core.hero

/**
 * 현대 영웅 로스터 011~250번 — 구글 드라이브 `SLG_현대영웅_케릭터`
 * (`1hEK1MPQjWhuwoPaOhGluWyTDbliHg_uj`)의 통합 로스터 `roster_001_250.json`에서 옮긴 240명.
 *
 * · 직업군(job)·등급(SSR/SR/R/N)·5개 능력치·특기(specialty)를 그대로 옮겼다.
 * · 배치 효과·필요 명성·고용비는 [buildRosterHero]가 능력치와 등급에서 계산한다.
 * · 초상화는 `res/drawable-nodpi/slg_hero_011.jpg` ~ `slg_hero_250.jpg`(540x810 JPEG).
 * · 1~10번은 [HeroRoster] 본문에 손으로 쓴 기본 10명이므로 여기에 없다.
 * · 241~250번(2026-11-20, 다국적 배치 8)부터 로스터의 `origin`·`ethnicity` 필드를 같이 옮긴다.
 */
internal object HeroExpansion {

    val HEROES: List<Hero> = listOf(
        buildRosterHero(
            no = 11, name = "서준호", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 32, gender = "남", job = "시청 정책기획관", specialty = "정책 기획 · 부서 조율",
            admin = 84, tech = 44, commerce = 52, ops = 26, diplomacy = 72,
            quote = "\"정책 기획 · 부서 조율\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 12, name = "오세라", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 28, gender = "여", job = "반도체 공정 엔지니어", specialty = "수율 개선 · 공정 이상 진단",
            admin = 38, tech = 88, commerce = 33, ops = 24, diplomacy = 39,
            quote = "\"수율 개선 · 공정 이상 진단\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 13, name = "문태호", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 54, gender = "남", job = "항만 물류회사 대표", specialty = "항만 물류 · 물류 거점 확보",
            admin = 62, tech = 35, commerce = 87, ops = 41, diplomacy = 66,
            quote = "\"항만 물류 · 물류 거점 확보\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 14, name = "신지안", role = HeroRole.TECH, grade = HeroGrade.SSR,
            age = 42, gender = "여", job = "AI 연구소장", specialty = "AI 모델 설계 · 데이터 전략",
            admin = 55, tech = 92, commerce = 47, ops = 22, diplomacy = 58,
            quote = "\"AI 모델 설계 · 데이터 전략\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 15, name = "김도훈", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 28, gender = "남", job = "경호 요원 (전직 대통령실)", specialty = "요인 경호 · 위협 사전 제거",
            admin = 24, tech = 52, commerce = 20, ops = 85, diplomacy = 43,
            quote = "\"요인 경호 · 위협 사전 제거\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 16, name = "홍미경", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 54, gender = "여", job = "시의원", specialty = "의회 협상 · 예산 확보",
            admin = 82, tech = 28, commerce = 58, ops = 24, diplomacy = 81,
            quote = "\"의회 협상 · 예산 확보\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 17, name = "노재식", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 62, gender = "남", job = "지역 원로 (전직 향토사학자)", specialty = "향토 네트워크 · 민심 통합",
            admin = 85, tech = 22, commerce = 46, ops = 30, diplomacy = 84,
            quote = "\"향토 네트워크 · 민심 통합\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 18, name = "하예린", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 32, gender = "여", job = "드론·UAV 스타트업 CTO", specialty = "무인기 운용 · 정찰 자동화",
            admin = 33, tech = 87, commerce = 51, ops = 55, diplomacy = 36,
            quote = "\"무인기 운용 · 정찰 자동화\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 19, name = "장길수", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 44, gender = "남", job = "건설 현장소장", specialty = "공정 관리 · 현장 통제",
            admin = 49, tech = 61, commerce = 78, ops = 54, diplomacy = 42,
            quote = "\"공정 관리 · 현장 통제\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 20, name = "권다인", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 22, gender = "여", job = "사이버 보안 분석가", specialty = "침해 사고 추적 · 취약점 분석",
            admin = 21, tech = 83, commerce = 25, ops = 47, diplomacy = 28,
            quote = "\"침해 사고 추적 · 취약점 분석\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 21, name = "이재성", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 34, gender = "남", job = "증권사 애널리스트", specialty = "시장 분석 · 자금 조달",
            admin = 42, tech = 45, commerce = 85, ops = 27, diplomacy = 62,
            quote = "\"시장 분석 · 자금 조달\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 22, name = "정소희", role = HeroRole.DIPLOMACY, grade = HeroGrade.SSR,
            age = 44, gender = "여", job = "외교부 협상관", specialty = "다자 협상 · 의제 설계",
            admin = 68, tech = 30, commerce = 44, ops = 25, diplomacy = 90,
            quote = "\"다자 협상 · 의제 설계\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 23, name = "안병철", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 54, gender = "남", job = "바이오 제약 수석연구원", specialty = "신약 임상 설계 · 연구 심사",
            admin = 46, tech = 85, commerce = 39, ops = 21, diplomacy = 47,
            quote = "\"신약 임상 설계 · 연구 심사\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 24, name = "최하늘", role = HeroRole.INTEL, grade = HeroGrade.R,
            age = 28, gender = "여", job = "탐사보도 기자", specialty = "취재 접근 · 내부 고발 확보",
            admin = 30, tech = 48, commerce = 33, ops = 58, diplomacy = 64,
            quote = "\"취재 접근 · 내부 고발 확보\" — 조용한 쪽이 정확합니다."
        ),
        buildRosterHero(
            no = 25, name = "박정우", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 34, gender = "남", job = "인권 변호사", specialty = "법리 설득 · 여론 공론화",
            admin = 54, tech = 32, commerce = 40, ops = 23, diplomacy = 86,
            quote = "\"법리 설득 · 여론 공론화\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 26, name = "엄정숙", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 62, gender = "여", job = "전직 교육감·원로", specialty = "교육 행정 · 세대 통합",
            admin = 85, tech = 24, commerce = 38, ops = 20, diplomacy = 77,
            quote = "\"교육 행정 · 세대 통합\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 27, name = "배성진", role = HeroRole.OPS, grade = HeroGrade.N,
            age = 28, gender = "남", job = "소방 구조대원", specialty = "인명 구조 · 재난 초기 대응",
            admin = 27, tech = 58, commerce = 22, ops = 82, diplomacy = 40,
            quote = "\"인명 구조 · 재난 초기 대응\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 28, name = "강해인", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 34, gender = "여", job = "경찰 특수기동대 지휘관", specialty = "진압 지휘 · 돌발 상황 통제",
            admin = 35, tech = 49, commerce = 24, ops = 89, diplomacy = 45,
            quote = "\"진압 지휘 · 돌발 상황 통제\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 29, name = "조성민", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 44, gender = "남", job = "통신·네트워크 보안팀장", specialty = "통신망 방어 · 감청 대응",
            admin = 44, tech = 82, commerce = 37, ops = 50, diplomacy = 35,
            quote = "\"통신망 방어 · 감청 대응\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 30, name = "김소율", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 28, gender = "여", job = "유통 MD·바이어", specialty = "상품 기획 · 유통 협상",
            admin = 36, tech = 30, commerce = 76, ops = 20, diplomacy = 68,
            quote = "\"상품 기획 · 유통 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 31, name = "한상철", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 54, gender = "남", job = "예비역 육군 대령", specialty = "부대 지휘 · 예비전력 동원",
            admin = 52, tech = 41, commerce = 30, ops = 88, diplomacy = 60,
            quote = "\"부대 지휘 · 예비전력 동원\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 32, name = "윤하영", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 34, gender = "여", job = "국제구호 NGO 활동가", specialty = "구호 물자 협상 · 현장 조정",
            admin = 48, tech = 36, commerce = 31, ops = 52, diplomacy = 83,
            quote = "\"구호 물자 협상 · 현장 조정\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 33, name = "고재만", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 62, gender = "남", job = "예비역 해군 제독", specialty = "해상 작전 · 연합 지휘",
            admin = 58, tech = 47, commerce = 42, ops = 86, diplomacy = 74,
            quote = "\"해상 작전 · 연합 지휘\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 34, name = "임세영", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 44, gender = "여", job = "부동산 개발사 임원", specialty = "부지 확보 · 개발 인허가",
            admin = 60, tech = 33, commerce = 84, ops = 26, diplomacy = 71,
            quote = "\"부지 확보 · 개발 인허가\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 35, name = "김선우", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 22, gender = "남", job = "AI 대학원 연구원", specialty = "모델 경량화 · 실험 구현",
            admin = 18, tech = 78, commerce = 22, ops = 30, diplomacy = 26,
            quote = "\"모델 경량화 · 실험 구현\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 36, name = "이정임", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 54, gender = "여", job = "은행 지점장", specialty = "여신 심사 · 고객 신뢰 관리",
            admin = 56, tech = 26, commerce = 80, ops = 22, diplomacy = 75,
            quote = "\"여신 심사 · 고객 신뢰 관리\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 37, name = "황인수", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 44, gender = "남", job = "출입국·세관 관리관", specialty = "신원 검증 · 국경 통제",
            admin = 74, tech = 39, commerce = 35, ops = 48, diplomacy = 66,
            quote = "\"신원 검증 · 국경 통제\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 38, name = "채수빈", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 28, gender = "여", job = "배터리·에너지 연구원", specialty = "배터리 소재 · 에너지 저장",
            admin = 31, tech = 84, commerce = 34, ops = 20, diplomacy = 37,
            quote = "\"배터리 소재 · 에너지 저장\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 39, name = "송민준", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 28, gender = "남", job = "국제회의 통역사", specialty = "동시 통역 · 뉘앙스 조율",
            admin = 34, tech = 44, commerce = 40, ops = 22, diplomacy = 80,
            quote = "\"동시 통역 · 뉘앙스 조율\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 40, name = "김태웅", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 44, gender = "남", job = "재난안전 대응 지휘관", specialty = "재난 지휘 · 자원 배분",
            admin = 61, tech = 46, commerce = 28, ops = 83, diplomacy = 57,
            quote = "\"재난 지휘 · 자원 배분\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 41, name = "이현주", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 44, gender = "여", job = "구청 복지국장", specialty = "복지 행정 · 취약계층 지원",
            admin = 80, tech = 30, commerce = 36, ops = 23, diplomacy = 73,
            quote = "\"복지 행정 · 취약계층 지원\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 42, name = "오진혁", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 44, gender = "남", job = "수출입 무역상", specialty = "수출입 교섭 · 물류 계약",
            admin = 43, tech = 35, commerce = 79, ops = 31, diplomacy = 70,
            quote = "\"수출입 교섭 · 물류 계약\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 43, name = "정찬우", role = HeroRole.INTEL, grade = HeroGrade.SR,
            age = 44, gender = "남", job = "정보기관 분석관", specialty = "정보 융합 · 위협 평가",
            admin = 39, tech = 66, commerce = 30, ops = 64, diplomacy = 72,
            quote = "\"정보 융합 · 위협 평가\" — 조용한 쪽이 정확합니다."
        ),
        buildRosterHero(
            no = 44, name = "최영달", role = HeroRole.ADMIN, grade = HeroGrade.SSR,
            age = 54, gender = "남", job = "행정중심도시 기획단장", specialty = "도시 기획 · 이해관계 조정",
            admin = 86, tech = 42, commerce = 55, ops = 33, diplomacy = 78,
            quote = "\"도시 기획 · 이해관계 조정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 45, name = "박소민", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 34, gender = "여", job = "프랜차이즈 사업가", specialty = "가맹 확장 · 상권 개척",
            admin = 45, tech = 29, commerce = 77, ops = 24, diplomacy = 74,
            quote = "\"가맹 확장 · 상권 개척\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 46, name = "이하람", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 28, gender = "여", job = "전직 해병 수색대원", specialty = "수색 정찰 · 근접 전투",
            admin = 26, tech = 55, commerce = 21, ops = 87, diplomacy = 38,
            quote = "\"수색 정찰 · 근접 전투\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 47, name = "김예지", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 28, gender = "여", job = "지자체 대변인", specialty = "브리핑 · 위기 커뮤니케이션",
            admin = 66, tech = 31, commerce = 34, ops = 20, diplomacy = 79,
            quote = "\"브리핑 · 위기 커뮤니케이션\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 48, name = "서지영", role = HeroRole.INTEL, grade = HeroGrade.R,
            age = 34, gender = "여", job = "산업보안 수사관", specialty = "기술 유출 수사 · 내부 감사",
            admin = 37, tech = 68, commerce = 32, ops = 61, diplomacy = 63,
            quote = "\"기술 유출 수사 · 내부 감사\" — 조용한 쪽이 정확합니다."
        ),
        buildRosterHero(
            no = 49, name = "강민석", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 36, gender = "남", job = "지자체 재난안전 담당 공무원", specialty = "재난 상황 통제·비상 대응 지휘",
            admin = 88, tech = 61, commerce = 45, ops = 72, diplomacy = 66,
            quote = "\"재난 상황 통제·비상 대응 지휘\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 50, name = "하준호", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 29, gender = "남", job = "토목기사", specialty = "교량·터널 구조 보강 설계",
            admin = 62, tech = 90, commerce = 54, ops = 68, diplomacy = 43,
            quote = "\"교량·터널 구조 보강 설계\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 51, name = "윤소미", role = HeroRole.DIPLOMACY, grade = HeroGrade.SSR,
            age = 42, gender = "여", job = "외교부 조약국 서기관", specialty = "조약 협상·의전 실무",
            admin = 66, tech = 41, commerce = 58, ops = 39, diplomacy = 90,
            quote = "\"조약 협상·의전 실무\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 52, name = "정태욱", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 38, gender = "남", job = "육군 통신 장교", specialty = "야전 통신망 구축·전자전 회피",
            admin = 55, tech = 78, commerce = 40, ops = 86, diplomacy = 48,
            quote = "\"야전 통신망 구축·전자전 회피\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 53, name = "문세라", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 39, gender = "여", job = "무역회사 이사", specialty = "항만 물류 계약·수출 협상",
            admin = 58, tech = 50, commerce = 89, ops = 42, diplomacy = 74,
            quote = "\"항만 물류 계약·수출 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 54, name = "김다인", role = HeroRole.ADMIN, grade = HeroGrade.N,
            age = 24, gender = "여", job = "지자체 주민센터 신입 직원", specialty = "민원 응대·현장 안내",
            admin = 74, tech = 55, commerce = 48, ops = 36, diplomacy = 70,
            quote = "\"민원 응대·현장 안내\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 55, name = "최병철", role = HeroRole.TECH, grade = HeroGrade.SSR,
            age = 51, gender = "남", job = "방산 엔지니어", specialty = "유도무기 체계 통합·시험평가",
            admin = 52, tech = 92, commerce = 46, ops = 70, diplomacy = 44,
            quote = "\"유도무기 체계 통합·시험평가\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 56, name = "배수현", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 29, gender = "여", job = "경찰 기동대 대원", specialty = "군중 통제·진압 대형",
            admin = 48, tech = 52, commerce = 32, ops = 88, diplomacy = 46,
            quote = "\"군중 통제·진압 대형\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 57, name = "권혁준", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 45, gender = "남", job = "세관원", specialty = "밀수 적발·화물 검색",
            admin = 72, tech = 62, commerce = 66, ops = 70, diplomacy = 58,
            quote = "\"밀수 적발·화물 검색\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 58, name = "남궁진", role = HeroRole.DIPLOMACY, grade = HeroGrade.SSR,
            age = 61, gender = "남", job = "전직 대사", specialty = "정상 회담 중재·인적 네트워크",
            admin = 70, tech = 38, commerce = 60, ops = 36, diplomacy = 93,
            quote = "\"정상 회담 중재·인적 네트워크\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 59, name = "전성우", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 58, gender = "남", job = "시의회 의장", specialty = "의회 표 대결·예산 조율",
            admin = 86, tech = 40, commerce = 68, ops = 44, diplomacy = 76,
            quote = "\"의회 표 대결·예산 조율\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 60, name = "유가온", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 32, gender = "여", job = "스타트업 CTO", specialty = "플랫폼 아키텍처·기술 조직 리드",
            admin = 58, tech = 89, commerce = 78, ops = 44, diplomacy = 52,
            quote = "\"플랫폼 아키텍처·기술 조직 리드\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 61, name = "명순자", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 53, gender = "여", job = "전통시장 상인회장", specialty = "상권 결집·시장 여론",
            admin = 78, tech = 34, commerce = 84, ops = 40, diplomacy = 72,
            quote = "\"상권 결집·시장 여론\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 62, name = "탁재웅", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 43, gender = "남", job = "소방 구조대장", specialty = "화재 진압·인명 구조 지휘",
            admin = 62, tech = 68, commerce = 36, ops = 91, diplomacy = 50,
            quote = "\"화재 진압·인명 구조 지휘\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 63, name = "하은별", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 28, gender = "여", job = "국제회의 통역사", specialty = "동시 통역·뉘앙스 포착",
            admin = 54, tech = 48, commerce = 46, ops = 40, diplomacy = 86,
            quote = "\"동시 통역·뉘앙스 포착\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 64, name = "신유라", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 46, gender = "여", job = "구청 도시재생 팀장", specialty = "재개발 갈등 조정·구역 계획",
            admin = 85, tech = 60, commerce = 52, ops = 47, diplomacy = 68,
            quote = "\"재개발 갈등 조정·구역 계획\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 65, name = "도경민", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 23, gender = "남", job = "로봇공학 대학원생", specialty = "로봇 제어·프로토타이핑",
            admin = 44, tech = 84, commerce = 38, ops = 52, diplomacy = 35,
            quote = "\"로봇 제어·프로토타이핑\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 66, name = "한지오", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 29, gender = "남", job = "핀테크 창업자", specialty = "금융 서비스 설계·투자 유치",
            admin = 56, tech = 75, commerce = 87, ops = 46, diplomacy = 64,
            quote = "\"금융 서비스 설계·투자 유치\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 67, name = "방대현", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 55, gender = "남", job = "예비역 해병 대령", specialty = "상륙 작전·수비 진지 구축",
            admin = 58, tech = 52, commerce = 40, ops = 90, diplomacy = 54,
            quote = "\"상륙 작전·수비 진지 구축\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 68, name = "임철호", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 49, gender = "남", job = "국제법 변호사", specialty = "국제 분쟁 소송·협상",
            admin = 68, tech = 50, commerce = 62, ops = 42, diplomacy = 88,
            quote = "\"국제 분쟁 소송·협상\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 69, name = "오금자", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 62, gender = "여", job = "마을 통장·노인회장", specialty = "마을 여론·이장 네트워크",
            admin = 83, tech = 30, commerce = 62, ops = 38, diplomacy = 75,
            quote = "\"마을 여론·이장 네트워크\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 70, name = "진세영", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 42, gender = "여", job = "의료기기 개발자", specialty = "의료기기 인증·임상 설계",
            admin = 60, tech = 87, commerce = 58, ops = 44, diplomacy = 52,
            quote = "\"의료기기 인증·임상 설계\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 71, name = "노아라", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 28, gender = "여", job = "유통 MD", specialty = "상품 기획·판매 흐름 분석",
            admin = 50, tech = 46, commerce = 86, ops = 38, diplomacy = 66,
            quote = "\"상품 기획·판매 흐름 분석\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 72, name = "홍미진", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 41, gender = "여", job = "해양경찰", specialty = "해상 수색·연안 통제",
            admin = 54, tech = 60, commerce = 34, ops = 85, diplomacy = 48,
            quote = "\"해상 수색·연안 통제\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 73, name = "백서경", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 37, gender = "여", job = "구호 NGO 조정관", specialty = "구호 협의·현장 중재",
            admin = 66, tech = 42, commerce = 44, ops = 50, diplomacy = 84,
            quote = "\"구호 협의·현장 중재\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 74, name = "유건우", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 28, gender = "남", job = "행정고시 출신 사무관", specialty = "정책 기획·부처 조율",
            admin = 82, tech = 58, commerce = 50, ops = 40, diplomacy = 70,
            quote = "\"정책 기획·부처 조율\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 75, name = "구자성", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 61, gender = "남", job = "통신망 기술 원로", specialty = "통신망 복구·주파수 설계",
            admin = 60, tech = 88, commerce = 44, ops = 62, diplomacy = 50,
            quote = "\"통신망 복구·주파수 설계\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 76, name = "엄태산", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 52, gender = "남", job = "주류·생필품 도매업자", specialty = "도매 유통망·현금 흐름",
            admin = 64, tech = 38, commerce = 88, ops = 44, diplomacy = 70,
            quote = "\"도매 유통망·현금 흐름\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 77, name = "박시우", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 23, gender = "남", job = "전직 특수부대원 출신 경호원", specialty = "근접 경호·위협 제압",
            admin = 34, tech = 58, commerce = 30, ops = 92, diplomacy = 40,
            quote = "\"근접 경호·위협 제압\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 78, name = "최연숙", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 56, gender = "여", job = "문화 외교관", specialty = "문화 교류·전시 기획",
            admin = 70, tech = 44, commerce = 58, ops = 32, diplomacy = 89,
            quote = "\"문화 교류·전시 기획\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 79, name = "차민정", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 35, gender = "여", job = "통계청 분석관", specialty = "통계 조사·수치 검증",
            admin = 86, tech = 74, commerce = 44, ops = 36, diplomacy = 56,
            quote = "\"통계 조사·수치 검증\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 80, name = "손예린", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 22, gender = "여", job = "드론 기술자", specialty = "드론 정찰·자율 비행",
            admin = 40, tech = 85, commerce = 38, ops = 72, diplomacy = 46,
            quote = "\"드론 정찰·자율 비행\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 81, name = "남기훈", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 31, gender = "남", job = "자산관리사", specialty = "자산 배분·고객 신뢰",
            admin = 52, tech = 46, commerce = 90, ops = 34, diplomacy = 74,
            quote = "\"자산 배분·고객 신뢰\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 82, name = "진소연", role = HeroRole.OPS, grade = HeroGrade.N,
            age = 32, gender = "여", job = "군의관", specialty = "야전 외상 처치·의무 후송",
            admin = 50, tech = 76, commerce = 32, ops = 80, diplomacy = 54,
            quote = "\"야전 외상 처치·의무 후송\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 83, name = "고윤성", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 28, gender = "남", job = "외교부 신참 사무관", specialty = "공문 작성·의전 실무",
            admin = 60, tech = 44, commerce = 42, ops = 38, diplomacy = 78,
            quote = "\"공문 작성·의전 실무\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 84, name = "류재만", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 47, gender = "남", job = "지방세무서장", specialty = "세원 발굴·체납 정리",
            admin = 84, tech = 48, commerce = 64, ops = 42, diplomacy = 56,
            quote = "\"세원 발굴·체납 정리\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 85, name = "표지훈", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 35, gender = "남", job = "반도체 장비 엔지니어", specialty = "장비 셋업·수율 개선",
            admin = 48, tech = 91, commerce = 42, ops = 58, diplomacy = 40,
            quote = "\"장비 셋업·수율 개선\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 86, name = "옥선영", role = HeroRole.COMMERCE, grade = HeroGrade.SSR,
            age = 62, gender = "여", job = "호텔그룹 회장", specialty = "호텔·유통 제국 경영",
            admin = 80, tech = 42, commerce = 92, ops = 46, diplomacy = 82,
            quote = "\"호텔·유통 제국 경영\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 87, name = "김도현", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 33, gender = "남", job = "도시계획가", specialty = "구획 정리",
            admin = 84, tech = 66, commerce = 58, ops = 47, diplomacy = 72,
            quote = "지도 위에 선을 긋고, 도시의 다음 10년을 설계한다."
        ),
        buildRosterHero(
            no = 88, name = "서지안", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 27, gender = "여", job = "전력망 기술자", specialty = "계통 복구",
            admin = 55, tech = 74, commerce = 42, ops = 58, diplomacy = 46,
            quote = "정전 3초 전에 손을 대는 사람."
        ),
        buildRosterHero(
            no = 89, name = "문한결", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 48, gender = "남", job = "항만 관제사", specialty = "선석 배정",
            admin = 70, tech = 61, commerce = 83, ops = 66, diplomacy = 58,
            quote = "부두의 모든 선박과 화물이 그의 목소리를 기다린다."
        ),
        buildRosterHero(
            no = 90, name = "정하윤", role = HeroRole.COMMERCE, grade = HeroGrade.SSR,
            age = 35, gender = "여", job = "금융 애널리스트", specialty = "시장 조기경보",
            admin = 74, tech = 55, commerce = 92, ops = 48, diplomacy = 78,
            quote = "숫자가 거짓말하기 전에 먼저 읽어낸다."
        ),
        buildRosterHero(
            no = 91, name = "오현석", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 52, gender = "남", job = "군수보급관", specialty = "보급선 사전 확보",
            admin = 68, tech = 54, commerce = 44, ops = 73, diplomacy = 52,
            quote = "전투는 시작되기 전에 그의 창고에서 갈린다."
        ),
        buildRosterHero(
            no = 92, name = "배수민", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 22, gender = "여", job = "드론 파일럿", specialty = "정밀 정찰",
            admin = 38, tech = 71, commerce = 45, ops = 68, diplomacy = 40,
            quote = "가장 가까이 보려고 가장 멀리서 배회한다."
        ),
        buildRosterHero(
            no = 93, name = "한지훈", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 34, gender = "남", job = "방송 PD", specialty = "여론 연출",
            admin = 52, tech = 50, commerce = 58, ops = 44, diplomacy = 69,
            quote = "카메라 앞에서 사람은 가장 솔직해진다."
        ),
        buildRosterHero(
            no = 94, name = "최은서", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 44, gender = "여", job = "노무사", specialty = "단체 협상",
            admin = 72, tech = 45, commerce = 60, ops = 42, diplomacy = 85,
            quote = "갈등의 값을 정확히 계산해 내는 중재자."
        ),
        buildRosterHero(
            no = 95, name = "강민재", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 46, gender = "남", job = "경찰 정보관", specialty = "내부 공작 적발",
            admin = 55, tech = 58, commerce = 41, ops = 82, diplomacy = 63,
            quote = "무언가를 아는 표정만으로 심판이 되는 사람."
        ),
        buildRosterHero(
            no = 96, name = "윤소라", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 31, gender = "여", job = "환경공학자", specialty = "수질·토양 정화",
            admin = 62, tech = 72, commerce = 44, ops = 46, diplomacy = 57,
            quote = "남은 땅을 다시 쓸 수 있게 만드는 재건의 기술자."
        ),
        buildRosterHero(
            no = 97, name = "권용태", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 43, gender = "남", job = "소방 구조대장", specialty = "재난 수습",
            admin = 60, tech = 55, commerce = 38, ops = 84, diplomacy = 62,
            quote = "불이 붙은 뒤에 가장 미움받지 않는 선택을 한다."
        ),
        buildRosterHero(
            no = 98, name = "이하늘", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 29, gender = "여", job = "응급의학과 의사", specialty = "응급 처치",
            admin = 64, tech = 63, commerce = 36, ops = 68, diplomacy = 58,
            quote = "한 명이라도 더 살아 있는 쪽으로 계산한다."
        ),
        buildRosterHero(
            no = 99, name = "노경아", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 53, gender = "여", job = "세무사", specialty = "자금 흐름 추적",
            admin = 66, tech = 42, commerce = 74, ops = 35, diplomacy = 55,
            quote = "장부 한 장에서 영지의 체력을 읽어낸다."
        ),
        buildRosterHero(
            no = 100, name = "김상철", role = HeroRole.ADMIN, grade = HeroGrade.SSR,
            age = 63, gender = "남", job = "원로 행정관", specialty = "연방 행정 재건",
            admin = 93, tech = 46, commerce = 70, ops = 58, diplomacy = 86,
            quote = "무너진 문서 체계를 다시 세우는 마지막 관료."
        ),
        buildRosterHero(
            no = 101, name = "신유진", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 26, gender = "여", job = "데이터 사이언티스트", specialty = "수급 예측 모델",
            admin = 64, tech = 86, commerce = 55, ops = 52, diplomacy = 44,
            quote = "감이 아니라 데이터로 다음 계절을 산다."
        ),
        buildRosterHero(
            no = 102, name = "천지훈", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 36, gender = "남", job = "변호사", specialty = "협약서 검토",
            admin = 60, tech = 43, commerce = 56, ops = 45, diplomacy = 72,
            quote = "조항 하나로 전쟁을 피하고 영토를 지킨다."
        ),
        buildRosterHero(
            no = 103, name = "이서현", role = HeroRole.COMMERCE, grade = HeroGrade.SSR,
            age = 37, gender = "여", job = "스타트업 대표", specialty = "상권 재편",
            admin = 72, tech = 74, commerce = 90, ops = 55, diplomacy = 80,
            quote = "없던 시장을 만들어 판을 바꾸는 사람."
        ),
        buildRosterHero(
            no = 104, name = "마성호", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 54, gender = "남", job = "조선 설계 엔지니어", specialty = "선박·중공업 설계",
            admin = 62, tech = 75, commerce = 50, ops = 48, diplomacy = 42,
            quote = "용골 하나를 고쳐 배의 수명을 20년 늘린다."
        ),
        buildRosterHero(
            no = 105, name = "백인숙", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 57, gender = "여", job = "스마트팜 농업인", specialty = "식량 자급",
            admin = 73, tech = 55, commerce = 62, ops = 34, diplomacy = 54,
            quote = "흉년을 겪어 본 사람만 아는 비축의 감각."
        ),
        buildRosterHero(
            no = 106, name = "문가온", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 28, gender = "여", job = "미술관 큐레이터", specialty = "문화 교류",
            admin = 44, tech = 35, commerce = 46, ops = 28, diplomacy = 58,
            quote = "취향을 통해 사람의 약점을 읽는다."
        ),
        buildRosterHero(
            no = 107, name = "김태윤", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 38, gender = "남", job = "철도 운영 관제사", specialty = "병력·물자 수송",
            admin = 55, tech = 64, commerce = 40, ops = 70, diplomacy = 42,
            quote = "단 한 번의 지연도 허용하지 않는 시간표의 주인."
        ),
        buildRosterHero(
            no = 108, name = "고은별", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 29, gender = "여", job = "물류센터 관리자", specialty = "재고 회전",
            admin = 55, tech = 42, commerce = 60, ops = 44, diplomacy = 46,
            quote = "창고의 빈 공간을 돈으로 바꾸는 사람."
        ),
        buildRosterHero(
            no = 109, name = "유재혁", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 41, gender = "남", job = "통신망 엔지니어", specialty = "통신 복구",
            admin = 50, tech = 73, commerce = 42, ops = 57, diplomacy = 38,
            quote = "끊긴 목소리를 다시 잇는 사람."
        ),
        buildRosterHero(
            no = 110, name = "서정후", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 61, gender = "남", job = "외교통상관", specialty = "조약 중재",
            admin = 76, tech = 40, commerce = 68, ops = 47, diplomacy = 88,
            quote = "적의 언어로 적의 이익을 설득하는 노련함."
        ),
        buildRosterHero(
            no = 111, name = "남지우", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 23, gender = "남", job = "자율주행 SW 엔지니어", specialty = "무인 차량 제어",
            admin = 34, tech = 62, commerce = 38, ops = 40, diplomacy = 32,
            quote = "사람보다 빠른 판단을 코드로 옮기는 중."
        ),
        buildRosterHero(
            no = 112, name = "심혜린", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 47, gender = "여", job = "재난대응 코디네이터", specialty = "비상 대응 체계",
            admin = 80, tech = 58, commerce = 44, ops = 78, diplomacy = 70,
            quote = "혼란한 현장에서 순서를 만들어 내는 목소리."
        ),
        buildRosterHero(
            no = 113, name = "최강일", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 51, gender = "남", job = "항공 운항관리사", specialty = "공중 수송 지휘",
            admin = 58, tech = 62, commerce = 40, ops = 72, diplomacy = 50,
            quote = "날씨와 연료와 시간을 동시에 계산하는 사람."
        ),
        buildRosterHero(
            no = 114, name = "장미래", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 32, gender = "여", job = "배터리 소재 연구원", specialty = "축전·충전 기술",
            admin = 50, tech = 71, commerce = 46, ops = 38, diplomacy = 40,
            quote = "에너지의 밀도를 조금씩 밀어 올리는 연구자."
        ),
        buildRosterHero(
            no = 115, name = "이준호", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 29, gender = "남", job = "반도체 공정 엔지니어", specialty = "수율 개선",
            admin = 60, tech = 84, commerce = 52, ops = 44, diplomacy = 42,
            quote = "1%의 수율을 위해 천 번의 공정을 다시 짠다."
        ),
        buildRosterHero(
            no = 116, name = "박선영", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 42, gender = "여", job = "국회 보좌관", specialty = "입법 로비",
            admin = 60, tech = 38, commerce = 54, ops = 48, diplomacy = 71,
            quote = "회의실 복도에서 결정을 미리 만드는 사람."
        ),
        buildRosterHero(
            no = 117, name = "정재환", role = HeroRole.ADMIN, grade = HeroGrade.N,
            age = 66, gender = "남", job = "향토사학자", specialty = "지명·구획 고증",
            admin = 55, tech = 40, commerce = 32, ops = 26, diplomacy = 52,
            quote = "옛 지도에서 지금의 경계선을 찾아낸다."
        ),
        buildRosterHero(
            no = 118, name = "한소민", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 27, gender = "여", job = "셰프 겸 외식업 대표", specialty = "식당 운영",
            admin = 44, tech = 36, commerce = 60, ops = 30, diplomacy = 52,
            quote = "한 끼의 만족으로 사람을 붙잡아 두는 기술."
        ),
        buildRosterHero(
            no = 119, name = "김우진", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 33, gender = "남", job = "기상청 예보관", specialty = "기상 예측",
            admin = 46, tech = 60, commerce = 34, ops = 44, diplomacy = 40,
            quote = "내일의 하늘을 오늘의 숫자로 바꾼다."
        ),
        buildRosterHero(
            no = 120, name = "오정연", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 36, gender = "여", job = "호텔 총지배인", specialty = "접객·의전",
            admin = 68, tech = 44, commerce = 81, ops = 40, diplomacy = 79,
            quote = "낯선 사람을 하룻밤 만에 편으로 만든다."
        ),
        buildRosterHero(
            no = 121, name = "배성훈", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 55, gender = "남", job = "건설 현장 소장", specialty = "거점 축조",
            admin = 70, tech = 52, commerce = 48, ops = 66, diplomacy = 45,
            quote = "도면과 날씨와 사람을 모두 감당하는 현장의 어른."
        ),
        buildRosterHero(
            no = 122, name = "문채원", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 29, gender = "여", job = "심리상담가 겸 협상가", specialty = "인질·갈등 협상",
            admin = 46, tech = 30, commerce = 34, ops = 25, diplomacy = 62,
            quote = "상대가 말하지 않은 것을 먼저 듣는 사람."
        ),
        buildRosterHero(
            no = 123, name = "주상혁", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 45, gender = "남", job = "민방위 구조 훈련관", specialty = "민병 훈련",
            admin = 48, tech = 52, commerce = 28, ops = 74, diplomacy = 44,
            quote = "평범한 시민을 하루 만에 부대처럼 세운다."
        ),
        buildRosterHero(
            no = 124, name = "이가영", role = HeroRole.ADMIN, grade = HeroGrade.N,
            age = 34, gender = "여", job = "스마트시티 서비스 기획자", specialty = "행정 서비스 설계",
            admin = 58, tech = 54, commerce = 42, ops = 34, diplomacy = 48,
            quote = "시민의 불편을 제도로 고치는 기획자."
        ),
        buildRosterHero(
            no = 125, name = "서해인", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 32, gender = "여", job = "도시철도 종합관제사", specialty = "열차 운행 조정·비상 시 복구 지휘",
            admin = 72, tech = 90, commerce = 48, ops = 74, diplomacy = 58,
            quote = "\"열차 운행 조정·비상 시 복구 지휘\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 126, name = "라승우", role = HeroRole.COMMERCE, grade = HeroGrade.SSR,
            age = 42, gender = "남", job = "스마트팜 기업 대표", specialty = "수직농장 수익 모델·식량 자급 계약",
            admin = 75, tech = 78, commerce = 86, ops = 44, diplomacy = 70,
            quote = "\"수직농장 수익 모델·식량 자급 계약\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 127, name = "진하람", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 28, gender = "여", job = "금융권 보안 아키텍트", specialty = "제로트러스트 설계·침해 차단",
            admin = 55, tech = 92, commerce = 52, ops = 66, diplomacy = 49,
            quote = "\"제로트러스트 설계·침해 차단\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 128, name = "오택규", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 51, gender = "남", job = "소방본부 지휘관", specialty = "대형 화재 진압 지휘·현장 통제",
            admin = 70, tech = 58, commerce = 38, ops = 93, diplomacy = 67,
            quote = "\"대형 화재 진압 지휘·현장 통제\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 129, name = "표유진", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 41, gender = "여", job = "상권 활성화 컨설턴트", specialty = "상권 진단·점포 유치 협상",
            admin = 64, tech = 42, commerce = 85, ops = 39, diplomacy = 74,
            quote = "\"상권 진단·점포 유치 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 130, name = "석동철", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 55, gender = "남", job = "노사 교섭 전문가", specialty = "단체교섭 중재·갈등 봉합",
            admin = 62, tech = 38, commerce = 55, ops = 34, diplomacy = 91,
            quote = "\"단체교섭 중재·갈등 봉합\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 131, name = "맹기훈", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 45, gender = "남", job = "중장비·조선 현장 감독", specialty = "중장비 운용 통제·공정 안전 관리",
            admin = 58, tech = 74, commerce = 36, ops = 83, diplomacy = 41,
            quote = "\"중장비 운용 통제·공정 안전 관리\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 132, name = "연서희", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 31, gender = "여", job = "화학공정 연구원", specialty = "정밀 화학공정 설계·위험물 분석",
            admin = 51, tech = 91, commerce = 44, ops = 52, diplomacy = 47,
            quote = "\"정밀 화학공정 설계·위험물 분석\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 133, name = "하문경", role = HeroRole.ADMIN, grade = HeroGrade.SSR,
            age = 43, gender = "여", job = "기업 법무팀장", specialty = "계약 분쟁 대응·법적 리스크 관리",
            admin = 86, tech = 56, commerce = 64, ops = 41, diplomacy = 79,
            quote = "\"계약 분쟁 대응·법적 리스크 관리\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 134, name = "편도현", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 42, gender = "남", job = "해양경비안전 본부 간부", specialty = "해상 봉쇄·구조 작전 지휘",
            admin = 63, tech = 66, commerce = 35, ops = 90, diplomacy = 62,
            quote = "\"해상 봉쇄·구조 작전 지휘\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 135, name = "은정아", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 48, gender = "여", job = "구청 비서실장", specialty = "기관 조율·민원 갈등 조정",
            admin = 84, tech = 40, commerce = 58, ops = 37, diplomacy = 76,
            quote = "\"기관 조율·민원 갈등 조정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 136, name = "추민재", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 22, gender = "남", job = "물류 자동화 로봇 엔지니어", specialty = "창고 로봇 경로 최적화·설비 증설",
            admin = 39, tech = 82, commerce = 47, ops = 45, diplomacy = 38,
            quote = "\"창고 로봇 경로 최적화·설비 증설\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 137, name = "탁수연", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 34, gender = "여", job = "재난심리지원 전문의", specialty = "재난 주민 심리 안정·회복 지원",
            admin = 78, tech = 49, commerce = 33, ops = 42, diplomacy = 71,
            quote = "\"재난 주민 심리 안정·회복 지원\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 138, name = "남상우", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 48, gender = "남", job = "손해사정사", specialty = "재난 손해 산정·보상 협상",
            admin = 60, tech = 47, commerce = 81, ops = 44, diplomacy = 63,
            quote = "\"재난 손해 산정·보상 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 139, name = "우가영", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 31, gender = "여", job = "항공교통관제사", specialty = "공역 통제·이착륙 우선순위 판단",
            admin = 57, tech = 80, commerce = 39, ops = 84, diplomacy = 52,
            quote = "\"공역 통제·이착륙 우선순위 판단\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 140, name = "채윤아", role = HeroRole.ADMIN, grade = HeroGrade.N,
            age = 41, gender = "여", job = "도시녹지 팀장", specialty = "공원·가로수 조성·녹지 확충",
            admin = 79, tech = 63, commerce = 38, ops = 46, diplomacy = 57,
            quote = "\"공원·가로수 조성·녹지 확충\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 141, name = "계동훈", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 32, gender = "남", job = "사이버수사 경위", specialty = "추적 수사·해킹 조직 검거",
            admin = 48, tech = 79, commerce = 34, ops = 82, diplomacy = 45,
            quote = "\"추적 수사·해킹 조직 검거\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 142, name = "조성만", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 52, gender = "남", job = "수산유통 기업 대표", specialty = "수산물 유통망·경매 시장 장악",
            admin = 57, tech = 35, commerce = 88, ops = 41, diplomacy = 66,
            quote = "\"수산물 유통망·경매 시장 장악\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 143, name = "위은채", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 28, gender = "여", job = "통상교섭 실무관", specialty = "통상 협정 문안·시장 개방 협상",
            admin = 58, tech = 51, commerce = 62, ops = 33, diplomacy = 84,
            quote = "\"통상 협정 문안·시장 개방 협상\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 144, name = "방기선", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 54, gender = "남", job = "발전소 운전원", specialty = "터빈 운전·전력 공급 안정화",
            admin = 46, tech = 74, commerce = 28, ops = 55, diplomacy = 36,
            quote = "\"터빈 운전·전력 공급 안정화\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 145, name = "노재홍", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 57, gender = "남", job = "지역신문 편집국장", specialty = "여론 형성·지역 의제 설정",
            admin = 66, tech = 40, commerce = 49, ops = 32, diplomacy = 83,
            quote = "\"여론 형성·지역 의제 설정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 146, name = "도하윤", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 35, gender = "여", job = "스타트업 CFO", specialty = "자금 조달·투자 유치 협상",
            admin = 70, tech = 54, commerce = 89, ops = 37, diplomacy = 68,
            quote = "\"자금 조달·투자 유치 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 147, name = "설강민", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 31, gender = "남", job = "해양구조 잠수사", specialty = "수중 구조·난파선 잠수 작업",
            admin = 37, tech = 69, commerce = 25, ops = 86, diplomacy = 40,
            quote = "\"수중 구조·난파선 잠수 작업\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 148, name = "명혜란", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 51, gender = "여", job = "지역 라디오 진행자", specialty = "지역 여론 완화·주민 소통 방송",
            admin = 62, tech = 31, commerce = 44, ops = 28, diplomacy = 77,
            quote = "\"지역 여론 완화·주민 소통 방송\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 149, name = "옥진우", role = HeroRole.TECH, grade = HeroGrade.SSR,
            age = 35, gender = "남", job = "자율주행 소프트웨어 리드", specialty = "자율주행 인지·판단 알고리즘",
            admin = 43, tech = 94, commerce = 52, ops = 58, diplomacy = 46,
            quote = "\"자율주행 인지·판단 알고리즘\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 150, name = "천복만", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 61, gender = "남", job = "요식업 협회장", specialty = "전통시장 상권 결속·업계 대표 교섭",
            admin = 64, tech = 30, commerce = 79, ops = 35, diplomacy = 72,
            quote = "\"전통시장 상권 결속·업계 대표 교섭\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 151, name = "고다혜", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 30, gender = "여", job = "국립공원 레인저", specialty = "산악 구조·야생 환경 단속",
            admin = 58, tech = 61, commerce = 30, ops = 77, diplomacy = 48,
            quote = "\"산악 구조·야생 환경 단속\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 152, name = "엄재일", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 53, gender = "남", job = "세무사", specialty = "세무 조정·재정 건전성 자문",
            admin = 69, tech = 36, commerce = 82, ops = 31, diplomacy = 59,
            quote = "\"세무 조정·재정 건전성 자문\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 153, name = "권소윤", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 23, gender = "여", job = "전투기 정비사", specialty = "기체 정비·출격 가동률 유지",
            admin = 33, tech = 79, commerce = 27, ops = 62, diplomacy = 30,
            quote = "\"기체 정비·출격 가동률 유지\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 154, name = "안성민", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 42, gender = "남", job = "교통정책 과장", specialty = "교통망 계획·노선 배분",
            admin = 81, tech = 58, commerce = 41, ops = 52, diplomacy = 69,
            quote = "\"교통망 계획·노선 배분\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 155, name = "지우경", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 43, gender = "여", job = "의료기기 기업 대표", specialty = "의료기기 수출·병원 공급 계약",
            admin = 67, tech = 76, commerce = 88, ops = 32, diplomacy = 61,
            quote = "\"의료기기 수출·병원 공급 계약\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 156, name = "함석구", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 62, gender = "남", job = "재난통신망 운영자", specialty = "비상 무선망 구축·교신 중계",
            admin = 52, tech = 72, commerce = 29, ops = 41, diplomacy = 55,
            quote = "\"비상 무선망 구축·교신 중계\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 157, name = "길은주", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 49, gender = "여", job = "통관·무역 컴플라이언스 자문", specialty = "통관 분쟁 해결·수출입 규제 협의",
            admin = 61, tech = 44, commerce = 67, ops = 30, diplomacy = 82,
            quote = "\"통관 분쟁 해결·수출입 규제 협의\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 158, name = "공인택", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 52, gender = "남", job = "민간경비업체 대표", specialty = "시설 경비 계약·경비 인력 배치",
            admin = 56, tech = 41, commerce = 63, ops = 78, diplomacy = 54,
            quote = "\"시설 경비 계약·경비 인력 배치\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 159, name = "형진국", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 62, gender = "남", job = "대학 산학협력단장", specialty = "산학 협력 협약·기술 이전 교섭",
            admin = 72, tech = 62, commerce = 58, ops = 28, diplomacy = 85,
            quote = "\"산학 협력 협약·기술 이전 교섭\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 160, name = "두준영", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 28, gender = "남", job = "배달 플랫폼 창업자", specialty = "플랫폼 확장·라이더 네트워크 장악",
            admin = 46, tech = 67, commerce = 78, ops = 42, diplomacy = 60,
            quote = "\"플랫폼 확장·라이더 네트워크 장악\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 161, name = "모영주", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 48, gender = "여", job = "환경영향평가 전문가", specialty = "환경 영향 평가·개발 갈등 조정",
            admin = 83, tech = 69, commerce = 45, ops = 38, diplomacy = 72,
            quote = "\"환경 영향 평가·개발 갈등 조정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 162, name = "기보라", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 32, gender = "여", job = "원자력 안전 검사관", specialty = "원전 안전 검사·방사선 관리",
            admin = 49, tech = 77, commerce = 26, ops = 44, diplomacy = 40,
            quote = "\"원전 안전 검사·방사선 관리\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 163, name = "채하윤", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 27, gender = "여", job = "웹툰 작가 겸 스튜디오 대표", specialty = "장르 기획 · 팬덤 운영",
            admin = 40, tech = 66, commerce = 78, ops = 35, diplomacy = 55,
            quote = "\"장르 기획 · 팬덤 운영\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 164, name = "곽민석", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 34, gender = "남", job = "항공기 기장", specialty = "장거리 운항 판단 · 비상 착륙 지휘",
            admin = 45, tech = 72, commerce = 38, ops = 86, diplomacy = 62,
            quote = "\"장거리 운항 판단 · 비상 착륙 지휘\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 165, name = "엄혜정", role = HeroRole.ADMIN, grade = HeroGrade.SSR,
            age = 52, gender = "여", job = "종합병원 간호부원장", specialty = "병동 운영 · 대규모 재난 간호 총괄",
            admin = 92, tech = 58, commerce = 40, ops = 66, diplomacy = 80,
            quote = "\"병동 운영 · 대규모 재난 간호 총괄\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 166, name = "백승호", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 26, gender = "남", job = "K-pop 안무가", specialty = "무대 연출 · 팀 사기 진작",
            admin = 30, tech = 52, commerce = 55, ops = 48, diplomacy = 62,
            quote = "\"무대 연출 · 팀 사기 진작\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 167, name = "유지안", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 45, gender = "여", job = "국립박물관 보존과학자", specialty = "유물 손상 진단 · 복원 처방",
            admin = 60, tech = 80, commerce = 38, ops = 30, diplomacy = 52,
            quote = "\"유물 손상 진단 · 복원 처방\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 168, name = "노성만", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 58, gender = "남", job = "조선소 노동조합 위원장", specialty = "대규모 교섭 · 현장 통제",
            admin = 66, tech = 55, commerce = 48, ops = 70, diplomacy = 88,
            quote = "\"대규모 교섭 · 현장 통제\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 169, name = "김채린", role = HeroRole.OPS, grade = HeroGrade.N,
            age = 23, gender = "여", job = "이스포츠 프로게이머", specialty = "순간 판단 · 교전 설계",
            admin = 22, tech = 78, commerce = 45, ops = 70, diplomacy = 38,
            quote = "\"순간 판단 · 교전 설계\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 170, name = "손태준", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 39, gender = "남", job = "스포츠 에이전트", specialty = "선수 계약 협상 · 이적 설계",
            admin = 45, tech = 30, commerce = 82, ops = 50, diplomacy = 76,
            quote = "\"선수 계약 협상 · 이적 설계\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 171, name = "장복순", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 61, gender = "여", job = "전직 초등학교 교장", specialty = "지역 교육 자원 결집",
            admin = 78, tech = 30, commerce = 36, ops = 25, diplomacy = 70,
            quote = "\"지역 교육 자원 결집\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 172, name = "구본혁", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 47, gender = "남", job = "해외 플랜트 기계설비 엔지니어", specialty = "해외 공사 기계 계통 총괄",
            admin = 50, tech = 84, commerce = 42, ops = 60, diplomacy = 46,
            quote = "\"해외 공사 기계 계통 총괄\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 173, name = "민아영", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 36, gender = "여", job = "항만 도선사", specialty = "대형 선박 접안 지휘 · 기상 판단",
            admin = 48, tech = 74, commerce = 40, ops = 92, diplomacy = 60,
            quote = "\"대형 선박 접안 지휘 · 기상 판단\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 174, name = "서강준", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 29, gender = "남", job = "게임 서버 개발자", specialty = "대규모 접속 안정화",
            admin = 28, tech = 76, commerce = 40, ops = 38, diplomacy = 32,
            quote = "\"대규모 접속 안정화\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 175, name = "홍윤정", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 44, gender = "여", job = "광역의회 의원", specialty = "예산 심사 · 조례 설계",
            admin = 86, tech = 40, commerce = 58, ops = 35, diplomacy = 82,
            quote = "\"예산 심사 · 조례 설계\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 176, name = "배정식", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 62, gender = "남", job = "농협 조합장", specialty = "지역 농산물 유통망 결집",
            admin = 74, tech = 38, commerce = 66, ops = 30, diplomacy = 68,
            quote = "\"지역 농산물 유통망 결집\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 177, name = "주미래", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 31, gender = "여", job = "뷰티 브랜드 마케팅 실장", specialty = "브랜드 확장 · 해외 팬덤 마케팅",
            admin = 48, tech = 38, commerce = 88, ops = 42, diplomacy = 70,
            quote = "\"브랜드 확장 · 해외 팬덤 마케팅\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 178, name = "김형섭", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 49, gender = "남", job = "공인회계사", specialty = "상장사 감사 · 분식 회계 적발",
            admin = 62, tech = 50, commerce = 80, ops = 28, diplomacy = 54,
            quote = "\"상장사 감사 · 분식 회계 적발\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 179, name = "이다온", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 27, gender = "여", job = "야생동물 수의사", specialty = "야생동물 구조 · 감염병 차단",
            admin = 40, tech = 78, commerce = 30, ops = 52, diplomacy = 58,
            quote = "\"야생동물 구조 · 감염병 차단\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 180, name = "정기훈", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 55, gender = "남", job = "전직 경찰청 국제협력관", specialty = "국제 공조 · 치안 협력망",
            admin = 64, tech = 52, commerce = 40, ops = 74, diplomacy = 86,
            quote = "\"국제 공조 · 치안 협력망\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 181, name = "최서윤", role = HeroRole.TECH, grade = HeroGrade.SSR,
            age = 38, gender = "여", job = "인공위성 지상국 운영 엔지니어", specialty = "위성 교신 복구 · 궤도 운용",
            admin = 46, tech = 94, commerce = 35, ops = 68, diplomacy = 56,
            quote = "\"위성 교신 복구 · 궤도 운용\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 182, name = "신동혁", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 33, gender = "남", job = "라이브커머스 사업가", specialty = "실시간 판매 흥행 설계",
            admin = 30, tech = 48, commerce = 72, ops = 40, diplomacy = 58,
            quote = "\"실시간 판매 흥행 설계\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 183, name = "허진경", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 50, gender = "여", job = "국가유산청 학예연구관", specialty = "국가유산 지정 · 보존 행정",
            admin = 80, tech = 52, commerce = 32, ops = 22, diplomacy = 66,
            quote = "\"국가유산 지정 · 보존 행정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 184, name = "문상엽", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 41, gender = "남", job = "소방항공대 조종사", specialty = "산불 진화 공중 지휘",
            admin = 40, tech = 70, commerce = 30, ops = 84, diplomacy = 48,
            quote = "\"산불 진화 공중 지휘\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 185, name = "이슬비", role = HeroRole.DIPLOMACY, grade = HeroGrade.N,
            age = 25, gender = "여", job = "음악 프로듀서", specialty = "사운드 설계 · 협업 조율",
            admin = 25, tech = 62, commerce = 52, ops = 30, diplomacy = 64,
            quote = "\"사운드 설계 · 협업 조율\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 186, name = "최만복", role = HeroRole.ADMIN, grade = HeroGrade.N,
            age = 67, gender = "남", job = "어민회장", specialty = "어촌 공동체 결속 · 어장 조정",
            admin = 70, tech = 30, commerce = 48, ops = 25, diplomacy = 60,
            quote = "\"어촌 공동체 결속 · 어장 조정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 187, name = "오유나", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 40, gender = "여", job = "바이오 임상개발 책임자", specialty = "임상 설계 · 규제 대응",
            admin = 58, tech = 84, commerce = 50, ops = 34, diplomacy = 68,
            quote = "\"임상 설계 · 규제 대응\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 188, name = "김태산", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 30, gender = "남", job = "잠수함 기관사", specialty = "추진 계통 정비 · 침묵 운항",
            admin = 35, tech = 76, commerce = 25, ops = 82, diplomacy = 36,
            quote = "\"추진 계통 정비 · 침묵 운항\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 189, name = "명지혜", role = HeroRole.COMMERCE, grade = HeroGrade.SSR,
            age = 47, gender = "여", job = "신용평가사 애널리스트", specialty = "신용 리스크 해부 · 등급 판정",
            admin = 66, tech = 48, commerce = 90, ops = 32, diplomacy = 66,
            quote = "\"신용 리스크 해부 · 등급 판정\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 190, name = "차상우", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 44, gender = "남", job = "문화재단 사무국장", specialty = "문화 사업 기획 · 후원 협상",
            admin = 62, tech = 35, commerce = 58, ops = 30, diplomacy = 78,
            quote = "\"문화 사업 기획 · 후원 협상\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 191, name = "박가은", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 29, gender = "여", job = "UX 디자이너", specialty = "사용자 흐름 설계",
            admin = 30, tech = 70, commerce = 52, ops = 28, diplomacy = 50,
            quote = "\"사용자 흐름 설계\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 192, name = "조영식", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 56, gender = "남", job = "국제물류 포워딩 대표", specialty = "글로벌 운송 노선 개척",
            admin = 58, tech = 45, commerce = 82, ops = 52, diplomacy = 64,
            quote = "\"글로벌 운송 노선 개척\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 193, name = "함미숙", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 33, gender = "여", job = "근로감독관", specialty = "사업장 감독 · 위반 시정",
            admin = 76, tech = 46, commerce = 30, ops = 44, diplomacy = 62,
            quote = "\"사업장 감독 · 위반 시정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 194, name = "김도경", role = HeroRole.OPS, grade = HeroGrade.N,
            age = 24, gender = "남", job = "산악구조대원", specialty = "암벽 구조 · 악천후 수색",
            admin = 25, tech = 58, commerce = 22, ops = 74, diplomacy = 42,
            quote = "\"암벽 구조 · 악천후 수색\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 195, name = "윤보영", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 58, gender = "여", job = "국제학교 교감", specialty = "국제 교육 과정 운영",
            admin = 72, tech = 40, commerce = 46, ops = 28, diplomacy = 80,
            quote = "\"국제 교육 과정 운영\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 196, name = "이준서", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 37, gender = "남", job = "이스포츠 구단 단장", specialty = "선수단 운영 · 리그 협상",
            admin = 52, tech = 52, commerce = 80, ops = 58, diplomacy = 62,
            quote = "\"선수단 운영 · 리그 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 197, name = "송하영", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 42, gender = "여", job = "공항 보안검색 팀장", specialty = "검색 절차 통제 · 위협 식별",
            admin = 44, tech = 60, commerce = 28, ops = 80, diplomacy = 50,
            quote = "\"검색 절차 통제 · 위협 식별\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 198, name = "권태윤", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 51, gender = "남", job = "특허 변리사", specialty = "특허 분쟁 · 권리 설계",
            admin = 50, tech = 86, commerce = 52, ops = 26, diplomacy = 62,
            quote = "\"특허 분쟁 · 권리 설계\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 199, name = "심유경", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 35, gender = "여", job = "시민단체 사무총장", specialty = "연대 조직 · 공론화 전략",
            admin = 70, tech = 42, commerce = 48, ops = 40, diplomacy = 86,
            quote = "\"연대 조직 · 공론화 전략\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 200, name = "서정우", role = HeroRole.DIPLOMACY, grade = HeroGrade.SSR,
            age = 60, gender = "남", job = "전직 외교특임대사", specialty = "비공개 중재 · 특임 교섭",
            admin = 70, tech = 40, commerce = 52, ops = 44, diplomacy = 94,
            quote = "\"비공개 중재 · 특임 교섭\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 201, name = "봉재현", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 45, gender = "남", job = "육군 특전사 부사관 교관", specialty = "산악 침투 훈련 설계 · 신병 사격 교정",
            admin = 66, tech = 74, commerce = 30, ops = 95, diplomacy = 62,
            quote = "\"산악 침투 훈련 설계 · 신병 사격 교정\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 202, name = "어준혁", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 47, gender = "남", job = "해군 상륙함 함장", specialty = "상륙 작전 지휘 · 함정 손상 통제",
            admin = 60, tech = 76, commerce = 38, ops = 92, diplomacy = 74,
            quote = "\"상륙 작전 지휘 · 함정 손상 통제\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 203, name = "선우경", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 38, gender = "여", job = "육군 정보대대장", specialty = "적 편성 분석 · 정보 수집망 운용",
            admin = 68, tech = 76, commerce = 32, ops = 84, diplomacy = 60,
            quote = "\"적 편성 분석 · 정보 수집망 운용\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 204, name = "예도현", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 33, gender = "남", job = "육군 폭발물처리(EOD) 팀장", specialty = "폭발물 해체 · 위험 지역 통제",
            admin = 42, tech = 74, commerce = 25, ops = 88, diplomacy = 40,
            quote = "\"폭발물 해체 · 위험 지역 통제\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 205, name = "현지수", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 34, gender = "여", job = "국방과학연구소 무기체계 시험평가관", specialty = "시험평가 설계 · 결함 판정",
            admin = 48, tech = 90, commerce = 28, ops = 60, diplomacy = 46,
            quote = "\"시험평가 설계 · 결함 판정\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 206, name = "감태호", role = HeroRole.ADMIN, grade = HeroGrade.SSR,
            age = 58, gender = "남", job = "예비역 기계화여단장", specialty = "대규모 부대 운영 · 예비전력 조직화",
            admin = 90, tech = 62, commerce = 44, ops = 84, diplomacy = 80,
            quote = "\"대규모 부대 운영 · 예비전력 조직화\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 207, name = "금도윤", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 41, gender = "남", job = "해병대 수색대 교관", specialty = "수중 침투 · 상륙 정찰",
            admin = 50, tech = 62, commerce = 24, ops = 94, diplomacy = 46,
            quote = "\"수중 침투 · 상륙 정찰\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 208, name = "반서영", role = HeroRole.TECH, grade = HeroGrade.SSR,
            age = 39, gender = "여", job = "스마트팜 수경재배 대표", specialty = "수경재배 자동화 · 작물 생육 데이터 분석",
            admin = 66, tech = 95, commerce = 76, ops = 28, diplomacy = 58,
            quote = "\"수경재배 자동화 · 작물 생육 데이터 분석\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 209, name = "용현우", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 55, gender = "남", job = "원양 참치선망 선장", specialty = "원양 조업 지휘 · 어획 판매 협상",
            admin = 54, tech = 66, commerce = 84, ops = 72, diplomacy = 50,
            quote = "\"원양 조업 지휘 · 어획 판매 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 210, name = "고순덕", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 63, gender = "여", job = "제주 해녀(물질)", specialty = "해녀 공동체 운영 · 조업 구역 조정",
            admin = 74, tech = 58, commerce = 46, ops = 72, diplomacy = 66,
            quote = "\"해녀 공동체 운영 · 조업 구역 조정\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 211, name = "우성호", role = HeroRole.COMMERCE, grade = HeroGrade.SR,
            age = 49, gender = "남", job = "김 양식장 대표", specialty = "김 양식 품질 관리 · 도매 거래선 개척",
            admin = 58, tech = 72, commerce = 80, ops = 46, diplomacy = 52,
            quote = "\"김 양식 품질 관리 · 도매 거래선 개척\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 212, name = "양기복", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 57, gender = "남", job = "한우 목장 경영인", specialty = "축산 경영 · 사료 자급 체계",
            admin = 82, tech = 60, commerce = 74, ops = 38, diplomacy = 56,
            quote = "\"축산 경영 · 사료 자급 체계\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 213, name = "원미란", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 45, gender = "여", job = "사과 과수원 6차산업 대표", specialty = "과수 재배 · 가공 체험 상품 기획",
            admin = 60, tech = 66, commerce = 78, ops = 32, diplomacy = 54,
            quote = "\"과수 재배 · 가공 체험 상품 기획\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 214, name = "승재호", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 52, gender = "남", job = "친환경 쌀 농업법인 대표", specialty = "친환경 벼 재배 단지 운영 · 농지 통합",
            admin = 76, tech = 58, commerce = 66, ops = 30, diplomacy = 52,
            quote = "\"친환경 벼 재배 단지 운영 · 농지 통합\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 215, name = "선다인", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 36, gender = "여", job = "전복 양식장 대표", specialty = "전복 종묘 관리 · 수질 모니터링",
            admin = 46, tech = 82, commerce = 58, ops = 34, diplomacy = 40,
            quote = "\"전복 종묘 관리 · 수질 모니터링\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 216, name = "국승민", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 54, gender = "남", job = "국가대표 양궁 감독", specialty = "경기 운영 전략 · 선수단 정신력 훈련",
            admin = 70, tech = 66, commerce = 45, ops = 93, diplomacy = 82,
            quote = "\"경기 운영 전략 · 선수단 정신력 훈련\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 217, name = "현유리", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 33, gender = "여", job = "유도 국가대표 출신 관장", specialty = "사범 지도 · 체육계 협력 교섭",
            admin = 72, tech = 58, commerce = 40, ops = 80, diplomacy = 84,
            quote = "\"사범 지도 · 체육계 협력 교섭\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 218, name = "염다솔", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 24, gender = "여", job = "수영 국가대표", specialty = "자유형 스퍼트 · 레이스 페이스 설계",
            admin = 30, tech = 54, commerce = 22, ops = 88, diplomacy = 50,
            quote = "\"자유형 스퍼트 · 레이스 페이스 설계\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 219, name = "예강민", role = HeroRole.OPS, grade = HeroGrade.N,
            age = 29, gender = "남", job = "마라톤 국가대표", specialty = "후반 페이스 배분 · 지구력 관리",
            admin = 34, tech = 50, commerce = 26, ops = 82, diplomacy = 42,
            quote = "\"후반 페이스 배분 · 지구력 관리\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 220, name = "봉하린", role = HeroRole.OPS, grade = HeroGrade.SR,
            age = 27, gender = "여", job = "배구 국가대표 세터", specialty = "경기 조율 · 동료 사기 관리",
            admin = 36, tech = 48, commerce = 30, ops = 84, diplomacy = 76,
            quote = "\"경기 조율 · 동료 사기 관리\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 221, name = "성기범", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 61, gender = "남", job = "전직 프로배구 감독", specialty = "선수단 장악 · 구단·언론 협상",
            admin = 70, tech = 52, commerce = 48, ops = 78, diplomacy = 88,
            quote = "\"선수단 장악 · 구단·언론 협상\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 222, name = "나윤슬", role = HeroRole.OPS, grade = HeroGrade.N,
            age = 30, gender = "여", job = "철인3종 국가대표", specialty = "복합 전환 훈련 · 한계 돌파",
            admin = 38, tech = 60, commerce = 32, ops = 84, diplomacy = 46,
            quote = "\"복합 전환 훈련 · 한계 돌파\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 223, name = "국선호", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 58, gender = "남", job = "선박 용접 명장", specialty = "특수 용접 · 선체 결함 보수",
            admin = 50, tech = 92, commerce = 36, ops = 58, diplomacy = 44,
            quote = "\"특수 용접 · 선체 결함 보수\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 224, name = "용기태", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 44, gender = "남", job = "타워크레인 기사", specialty = "초고층 양중 계획 · 정밀 인양",
            admin = 36, tech = 78, commerce = 28, ops = 74, diplomacy = 32,
            quote = "\"초고층 양중 계획 · 정밀 인양\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 225, name = "금영실", role = HeroRole.TECH, grade = HeroGrade.R,
            age = 46, gender = "여", job = "전기공사 기능장", specialty = "고압 배전 시공 · 정전 복구",
            admin = 44, tech = 80, commerce = 52, ops = 40, diplomacy = 38,
            quote = "\"고압 배전 시공 · 정전 복구\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 226, name = "김영백", role = HeroRole.TECH, grade = HeroGrade.N,
            age = 55, gender = "남", job = "제철소 제련 기능장", specialty = "고로 운전 · 용탕 품질 관리",
            admin = 52, tech = 86, commerce = 38, ops = 50, diplomacy = 40,
            quote = "\"고로 운전 · 용탕 품질 관리\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 227, name = "박해운", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 50, gender = "여", job = "장거리 화물차 기사", specialty = "장거리 운송 노선 개척 · 화물 배차",
            admin = 46, tech = 62, commerce = 76, ops = 44, diplomacy = 50,
            quote = "\"장거리 운송 노선 개척 · 화물 배차\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 228, name = "정해든", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 41, gender = "여", job = "산림경영 법인 대표", specialty = "산림 자원 관리 · 벌채 계획 수립",
            admin = 78, tech = 64, commerce = 58, ops = 34, diplomacy = 56,
            quote = "\"산림 자원 관리 · 벌채 계획 수립\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 229, name = "황보경", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 44, gender = "여", job = "해양수산부 어촌정책관", specialty = "어촌 정책 협의 · 지자체·어업인 중재",
            admin = 76, tech = 48, commerce = 42, ops = 30, diplomacy = 86,
            quote = "\"어촌 정책 협의 · 지자체·어업인 중재\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 230, name = "이강훈", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 48, gender = "남", job = "농기계·종자 유통 대표", specialty = "농기계 판매망 구축 · 종자 계약 유통",
            admin = 58, tech = 70, commerce = 82, ops = 38, diplomacy = 66,
            quote = "\"농기계 판매망 구축 · 종자 계약 유통\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 231, name = "도재훈", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 36, gender = "남", job = "소방 항공대 구조 헬기 조종사", specialty = "산악·해상 구조 출동 지휘 · 저시정 조종",
            admin = 55, tech = 68, commerce = 40, ops = 93, diplomacy = 62,
            quote = "\"산악·해상 구조 출동 지휘 · 저시정 조종\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 232, name = "백하영", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 41, gender = "여", job = "국립과학수사연구원 법의학자", specialty = "사인 감정 · 미세 증거 판독",
            admin = 62, tech = 88, commerce = 46, ops = 34, diplomacy = 54,
            quote = "\"사인 감정 · 미세 증거 판독\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 233, name = "권이현", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 33, gender = "남", job = "수제 맥주 양조장 브루마스터", specialty = "레시피 설계 · 로컬 유통 협상",
            admin = 48, tech = 62, commerce = 78, ops = 30, diplomacy = 58,
            quote = "\"레시피 설계 · 로컬 유통 협상\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 234, name = "노유정", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 29, gender = "여", job = "지역아동센터 사회복지사", specialty = "돌봄 사례 관리 · 지역 자원 연계",
            admin = 86, tech = 42, commerce = 38, ops = 28, diplomacy = 76,
            quote = "\"돌봄 사례 관리 · 지역 자원 연계\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다."
        ),
        buildRosterHero(
            no = 235, name = "서재우", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 44, gender = "남", job = "데이터센터 냉각 설비 엔지니어", specialty = "열관리 설계 · 무중단 냉각 운전",
            admin = 54, tech = 90, commerce = 44, ops = 46, diplomacy = 40,
            quote = "\"열관리 설계 · 무중단 냉각 운전\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 236, name = "하지수", role = HeroRole.DIPLOMACY, grade = HeroGrade.R,
            age = 35, gender = "여", job = "항공사 서비스 교육 컨설턴트", specialty = "응대 매뉴얼 설계 · 감정 노동 관리",
            admin = 56, tech = 30, commerce = 52, ops = 26, diplomacy = 84,
            quote = "\"응대 매뉴얼 설계 · 감정 노동 관리\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 237, name = "명재훈", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 27, gender = "남", job = "전통시장 청년 상인회 기획자", specialty = "시장 축제 기획 · 온라인 판로 개척",
            admin = 44, tech = 38, commerce = 70, ops = 32, diplomacy = 58,
            quote = "\"시장 축제 기획 · 온라인 판로 개척\" — 사람이 모이는 길에 돈이 있습니다."
        ),
        buildRosterHero(
            no = 238, name = "유하윤", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 31, gender = "여", job = "프로 스포츠 구단 재활 트레이너", specialty = "부상 복귀 프로그램 · 컨디셔닝 관리",
            admin = 52, tech = 62, commerce = 30, ops = 78, diplomacy = 48,
            quote = "\"부상 복귀 프로그램 · 컨디셔닝 관리\" — 작전은 준비에서 끝납니다."
        ),
        buildRosterHero(
            no = 239, name = "신도경", role = HeroRole.DIPLOMACY, grade = HeroGrade.SSR,
            age = 49, gender = "남", job = "외교부 통상교섭 실무관", specialty = "통상 협상 · 쟁점 조율",
            admin = 74, tech = 48, commerce = 66, ops = 38, diplomacy = 92,
            quote = "\"통상 협상 · 쟁점 조율\" — 상대의 말을 먼저 들어야 제 말이 들립니다."
        ),
        buildRosterHero(
            no = 240, name = "채민서", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 38, gender = "여", job = "도시재생 목조건축가", specialty = "목구조 설계 · 노후 지역 재생 계획",
            admin = 72, tech = 86, commerce = 50, ops = 34, diplomacy = 64,
            quote = "\"목구조 설계 · 노후 지역 재생 계획\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠."
        ),
        buildRosterHero(
            no = 241, name = "쿠로사와 렌", role = HeroRole.OPS, grade = HeroGrade.SSR,
            age = 41, gender = "남", job = "스마트시티 통합안전관제센터장", specialty = "도시 센서망 이상 징후 판독 · 다중 재난 동시 대응",
            admin = 58, tech = 76, commerce = 42, ops = 94, diplomacy = 48,
            quote = "\"도시 센서망 이상 징후 판독 · 다중 재난 동시 대응\" — 작전은 준비에서 끝납니다.",
            origin = "일본 도쿄", ethnicity = "아시아계(일본인)"
        ),
        buildRosterHero(
            no = 242, name = "응우옌 티 란", role = HeroRole.COMMERCE, grade = HeroGrade.R,
            age = 29, gender = "여", job = "로부스타 커피 수출 상사 대표", specialty = "산지 직거래 계약 · 선적 항로 협상",
            admin = 46, tech = 38, commerce = 84, ops = 30, diplomacy = 60,
            quote = "\"산지 직거래 계약 · 선적 항로 협상\" — 사람이 모이는 길에 돈이 있습니다.",
            origin = "베트남 다낭", ethnicity = "아시아계(베트남인)"
        ),
        buildRosterHero(
            no = 243, name = "아르준 라구나단", role = HeroRole.TECH, grade = HeroGrade.SR,
            age = 36, gender = "남", job = "항공교통관제 자동화 소프트웨어 아키텍트", specialty = "다중 레이더 트랙 융합 · 관제 절차 자동화",
            admin = 46, tech = 90, commerce = 40, ops = 50, diplomacy = 50,
            quote = "\"다중 레이더 트랙 융합 · 관제 절차 자동화\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠.",
            origin = "인도 벵갈루루", ethnicity = "아시아계(인도인)"
        ),
        buildRosterHero(
            no = 244, name = "엘리자베스 코널리", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 45, gender = "여", job = "인도적 지원 조정관", specialty = "구호 물자 통로 협상 · 다국적 기구 운영",
            admin = 68, tech = 44, commerce = 52, ops = 38, diplomacy = 92,
            quote = "\"구호 물자 통로 협상 · 다국적 기구 운영\" — 상대의 말을 먼저 들어야 제 말이 들립니다.",
            origin = "미국 보스턴", ethnicity = "백인(유럽계 미국인)"
        ),
        buildRosterHero(
            no = 245, name = "마티아스 크뢰거", role = HeroRole.TECH, grade = HeroGrade.SSR,
            age = 54, gender = "남", job = "초정밀 공작기계 수석 엔지니어", specialty = "마이크론 공차 설계 · 생산 라인 전환",
            admin = 64, tech = 96, commerce = 54, ops = 34, diplomacy = 62,
            quote = "\"마이크론 공차 설계 · 생산 라인 전환\" — 설비는 거짓말을 안 합니다. 데이터로 증명하죠.",
            origin = "독일 슈투트가르트", ethnicity = "백인(독일인)"
        ),
        buildRosterHero(
            no = 246, name = "카미유 르루아", role = HeroRole.COMMERCE, grade = HeroGrade.N,
            age = 34, gender = "여", job = "향료 원료 소싱 디렉터", specialty = "원료 등급 감별 · 농가 소싱 네트워크",
            admin = 42, tech = 44, commerce = 74, ops = 28, diplomacy = 56,
            quote = "\"원료 등급 감별 · 농가 소싱 네트워크\" — 사람이 모이는 길에 돈이 있습니다.",
            origin = "프랑스 그라스", ethnicity = "백인(프랑스인)"
        ),
        buildRosterHero(
            no = 247, name = "치네두 오카포", role = HeroRole.ADMIN, grade = HeroGrade.R,
            age = 39, gender = "남", job = "대도시 상수도 공기업 운영혁신 책임자", specialty = "누수 구역 우선순위 책정 · 요금 체계 개편",
            admin = 80, tech = 52, commerce = 44, ops = 32, diplomacy = 66,
            quote = "\"누수 구역 우선순위 책정 · 요금 체계 개편\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다.",
            origin = "나이지리아 라고스", ethnicity = "흑인(아프리카계)"
        ),
        buildRosterHero(
            no = 248, name = "타니카 브룩스", role = HeroRole.OPS, grade = HeroGrade.R,
            age = 32, gender = "여", job = "국제 스포츠 이벤트 안전 운영 총괄", specialty = "인파 통제 동선 설계 · 비상 대피 시나리오",
            admin = 48, tech = 56, commerce = 32, ops = 82, diplomacy = 50,
            quote = "\"인파 통제 동선 설계 · 비상 대피 시나리오\" — 작전은 준비에서 끝납니다.",
            origin = "미국 애틀랜타", ethnicity = "흑인(아프리카계 미국인)"
        ),
        buildRosterHero(
            no = 249, name = "와니쿠 응구기", role = HeroRole.ADMIN, grade = HeroGrade.SR,
            age = 27, gender = "여", job = "기후 적응·식수 접근 정책 분석가", specialty = "가뭄 예측 데이터 해석 · 물 배분 협의",
            admin = 88, tech = 54, commerce = 36, ops = 30, diplomacy = 82,
            quote = "\"가뭄 예측 데이터 해석 · 물 배분 협의\" — 제 일입니다. 예산은 감이 아니라 숫자로 짭니다.",
            origin = "케냐 나이로비", ethnicity = "흑인(아프리카계)"
        ),
        buildRosterHero(
            no = 250, name = "패트릭 도노번", role = HeroRole.DIPLOMACY, grade = HeroGrade.SR,
            age = 47, gender = "남", job = "북극·기후 다자협상 대표", specialty = "다자 협약 문안 조율 · 이해관계국 중재",
            admin = 70, tech = 40, commerce = 62, ops = 36, diplomacy = 88,
            quote = "\"다자 협약 문안 조율 · 이해관계국 중재\" — 상대의 말을 먼저 들어야 제 말이 들립니다.",
            origin = "캐나다 오타와", ethnicity = "백인(유럽계 캐나다인)"
        ),
    )
}
