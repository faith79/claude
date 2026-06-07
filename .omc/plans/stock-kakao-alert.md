# Plan: 한국 주식 조건 스캐너 → 카카오톡 발송 스킬

**Status**: pending approval
**Source Spec**: `.omc/specs/deep-interview-stock-kakao-alert.md`
**Interview ID**: stock-kakao-alert-20260607
**Plan Created**: 2026-06-07
**Mode**: consensus (RALPLAN-DR short, --direct)

---

## RALPLAN-DR Summary

### Principles

1. **Zero external infra** — 외부 서버·DB 없이 Claude Code MCP 도구만 사용
2. **Spec fidelity** — Deep Interview 명세(14.6% 모호성)를 100% 반영, 임의 확장 금지
3. **Fail safe** — pykrx 오류·장 비영업일·빈 결과 모두 graceful 처리, 항상 카카오에 메시지 발송
4. **Testable by inspection** — Python REPL 내 조건 로직은 날짜·데이터를 바꿔 즉시 검증 가능
5. **Single-file skill** — `.claude/skills/stock-kakao-alert/SKILL.md` 한 파일로 완결

### Decision Drivers

1. **MCP 도구 가용성** — Python REPL(`mcp__plugin_oh-my-claudecode_t__python_repl`)과 KakaotalkChat-MemoChat이 이미 연결돼 있음
2. **KOSPI/KOSDAQ 전 종목 커버리지** — pykrx만이 무료·공식·전 종목 OHLCV를 제공
3. **스킬 재사용성** — on-demand + 선택적 `/schedule` 통합, 단일 엔트리포인트

### Viable Options

#### Option A (선택): pykrx 배치 풀 + Python REPL 인라인 필터링
- **Approach**: `stock.get_market_ohlcv(date, market)` 한 번 호출로 전 종목 OHLCV DataFrame 수신, Python 내에서 3개 조건 벡터 필터링
- **Pros**: API 호출 수 최소화(시장당 1회), 판다스 연산으로 빠른 필터링, 코드 구조 단순
- **Cons**: 첫 호출 시 pykrx pip install 필요, 대용량 DataFrame 메모리 사용

#### Option B (기각): 종목 코드 목록 조회 후 ticker별 개별 API 호출
- **Approach**: `get_market_ticker_list()`로 코드 목록 획득 후 ticker마다 OHLCV 개별 조회
- **Pros**: 메모리 절약
- **Cons**: KOSPI 800+ / KOSDAQ 1600+ 종목 × 3일 × 2시장 = 7000+ 개별 API 호출 → 속도·레이트리밋 문제
- **Invalidation**: 실행 시간 수 분 초과로 실용성 없음

---

## Requirements Summary

### 기능 요구사항

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| F1 | KOSPI + KOSDAQ 전 종목 D-0/D-1/D-2 OHLCV 수집 (pykrx) | Must |
| F2 | D-2 상한가 필터: 전일 대비 등락률 +29.9% ~ +30.0% | Must |
| F3 | D-1 도지 캔들 필터: &#124;종가-시가&#124;/(고가-저가) ≤ 0.10, 위아래 수염 존재 | Must |
| F4 | D-0 거래량 급증 필터: 당일 거래량 ≥ 20일 평균 × 1.5 | Must |
| F5 | 조건 만족 종목 → KakaotalkChat-MemoChat으로 포맷 메시지 발송 | Must |
| F6 | 조건 만족 종목 없음 → "오늘은 조건 만족 종목이 없습니다" 발송 | Must |
| F7 | 장 비영업일(휴장) 실행 시 graceful 처리 + 메시지 발송 | Must |
| F8 | `/stock-kakao-alert` slash command on-demand 호출 | Must |
| F9 | SKILL.md 내 daily schedule 설정 안내 포함 | Should |
| F10 | 결과 수 제한 없음 (전체 발송) | Must |

### 비기능 요구사항

| ID | 요구사항 |
|----|---------|
| NF1 | 외부 서버·DB 없이 MCP 도구만 사용 |
| NF2 | pykrx 미설치 시 자동 pip install 후 재실행 |
| NF3 | 실행 로그(수집 종목 수, 필터 단계별 통과 수) 텍스트 출력 |

---

## Acceptance Criteria

- [ ] **AC1**: KOSPI 전 종목 OHLCV 수집 성공 — `len(df_kospi) > 500` 검증
- [ ] **AC2**: KOSDAQ 전 종목 OHLCV 수집 성공 — `len(df_kosdaq) > 1000` 검증
- [ ] **AC3**: D-2 등락률 계산 정확 — pykrx `등락률` 값 ≥ 29.9 AND ≤ 30.0 (percent 단위, ratio 아님)
- [ ] **AC4**: 도지 비율 계산 정확 — `abs(close - open) / (high - low) ≤ 0.10` (분모 0 방어 포함)
- [ ] **AC5**: 거래량 비율 계산 정확 — `vol_d0 / vol_20d_avg ≥ 1.5`
- [ ] **AC6**: 조건 만족 종목 있을 때 — KakaotalkChat-MemoChat MCP 호출 후 에러 없이 반환; 카카오톡 "나에게 쓰기" 채팅방에 메시지 수신 육안 확인
- [ ] **AC7**: 조건 만족 종목 없을 때 — "오늘은 조건 만족 종목이 없습니다" 메시지 발송
- [ ] **AC8**: 장 비영업일·데이터 미집계 실행 — `df_d0.empty` 또는 `len(df_d0) == 0` 시 "장 비영업일이거나 데이터가 아직 집계 중입니다" 메시지 카카오 발송; `get_nearest_business_day_in_a_week(prev=True)` 반환값으로 당일 = 거래일 판단
- [ ] **AC9**: `/stock-kakao-alert` 커맨드로 SKILL.md 실행 가능
- [ ] **AC10**: SKILL.md 내 cron schedule 안내 섹션 존재

---

## Implementation Steps

### Step 1: 디렉터리 및 파일 구조 생성

```
.claude/skills/stock-kakao-alert/
└── SKILL.md
```

**파일**: `.claude/skills/stock-kakao-alert/SKILL.md`

스킬 파일 구조:
```
# Purpose
# Trigger
# Prerequisites
# Execution Flow (with Python REPL code blocks)
# Output Format
# Error Handling
# Schedule Setup Guide
```

### Step 2: Python REPL 실행 코드 설계

SKILL.md 내 Python 코드 블록 (Claude가 Python REPL MCP로 실행):

```python
# ① 환경 준비
import subprocess, sys
try:
    from pykrx import stock
except ImportError:
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pykrx", "-q"])
    from pykrx import stock

import pandas as pd
from datetime import datetime, timedelta

# ② 거래일 계산
def get_business_day(offset_days: int) -> str:
    """D+offset 거래일 반환 (YYYYMMDD)"""
    today = datetime.today()
    date = today - timedelta(days=abs(offset_days))
    # 주말·공휴일 처리: pykrx nearest business day
    return stock.get_nearest_business_day_in_a_week(
        date=date.strftime("%Y%m%d"), prev=True
    )

d0 = get_business_day(0)   # 당일
d1 = get_business_day(1)   # D-1
d2 = get_business_day(2)   # D-2

# ③ OHLCV 수집 (배치 풀)
def fetch_market(market: str, date: str) -> pd.DataFrame:
    df = stock.get_market_ohlcv(date, market=market)
    # 명시적 dict rename — 컬럼 순서 변화에 안전
    df = df.rename(columns={
        '시가': 'open', '고가': 'high', '저가': 'low', '종가': 'close',
        '거래량': 'volume', '거래대금': 'trading_value', '등락률': 'change_pct'
    })
    return df

# KOSPI + KOSDAQ 3일치 수집
kospi_d0 = fetch_market("KOSPI", d0)
kospi_d1 = fetch_market("KOSPI", d1)
kospi_d2 = fetch_market("KOSPI", d2)
kosdaq_d0 = fetch_market("KOSDAQ", d0)
kosdaq_d1 = fetch_market("KOSDAQ", d1)
kosdaq_d2 = fetch_market("KOSDAQ", d2)

# ④ 3-Step 필터링
results = []
for market_name, df_d0, df_d1, df_d2 in [
    ("KOSPI", kospi_d0, kospi_d1, kospi_d2),
    ("KOSDAQ", kosdaq_d0, kosdaq_d1, kosdaq_d2),
]:
    common = df_d0.index.intersection(df_d1.index).intersection(df_d2.index)
    
    for ticker in common:
        # 조건 1: D-2 상한가 (+29.9% ~ +30%)
        d2_pct = df_d2.loc[ticker, 'change_pct']
        if not (29.9 <= d2_pct <= 30.0):
            continue
        
        # 조건 2: D-1 도지 캔들
        o, h, l, c = (df_d1.loc[ticker, x] for x in ['open','high','low','close'])
        body_range = h - l
        if body_range == 0:
            continue
        doji_ratio = abs(c - o) / body_range
        if doji_ratio > 0.10:
            continue
        # 위아래 수염 확인
        if o == l or c == h:
            continue
        
        # 조건 3: D-0 거래량 급증 (20일 평균의 1.5배)
        vol_d0 = df_d0.loc[ticker, 'volume']
        hist = stock.get_market_ohlcv(
            fromdate=(datetime.strptime(d1,"%Y%m%d")-timedelta(days=30)).strftime("%Y%m%d"),
            todate=d1, ticker=ticker  # d1까지만 — D-0 거래량을 평균에서 제외
        )
        if len(hist) < 5:
            continue
        vol_avg_20 = hist['거래량'].tail(20).mean()
        vol_ratio = vol_d0 / vol_avg_20 if vol_avg_20 > 0 else 0
        if vol_ratio < 1.5:
            continue
        
        # 종목명 조회
        name = stock.get_market_ticker_name(ticker)
        results.append({
            "ticker": ticker, "name": name, "market": market_name,
            "d2_pct": round(d2_pct, 2),
            "d1_doji": round(doji_ratio, 3),
            "d0_vol_ratio": round(vol_ratio, 2),
        })

print(f"필터 결과: {len(results)}개 종목")
```

### Step 3: 메시지 포맷 및 카카오 발송 코드 설계

```python
# ⑤ 메시지 포맷
from datetime import date
scan_date = date.today().strftime("%Y년 %m월 %d일")

if not results:
    message = f"📊 [주식 조건 스캐너] {scan_date}\n⚠️ 오늘은 조건 만족 종목이 없습니다."
else:
    lines = [f"📊 [주식 조건 스캐너] {scan_date}\n", f"✅ 조건 만족 종목 {len(results)}개\n"]
    for i, r in enumerate(results, 1):
        lines.append(
            f"{i}. {r['name']} ({r['ticker']}) [{r['market']}]\n"
            f"   - D-2 등락률: +{r['d2_pct']}%\n"
            f"   - D-1 캔들 비율: {r['d1_doji']}\n"
            f"   - D-0 거래량 비율: {r['d0_vol_ratio']}x (20일 평균 대비)\n"
        )
    lines.append("\n---\n🔍 조건: D-2 상한가 → D-1 도지 → D-0 거래량 1.5배↑")
    message = "\n".join(lines)

print("=== 발송 메시지 ===")
print(message)
```

### Step 4: KakaotalkChat-MemoChat MCP 호출

SKILL.md에 Claude가 실행할 액션으로 명시:

```
After Python REPL completes, Claude calls:
mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat
  action: "send_to_me"
  message: <formatted message from Python>
```

### Step 5: SKILL.md 전체 작성

파일 위치: `.claude/skills/stock-kakao-alert/SKILL.md`

필수 섹션:
1. `# Purpose` — 스킬 목적
2. `# Trigger` — `/stock-kakao-alert`
3. `# Prerequisites` — Python 3.8+, pykrx (자동 설치), KakaotalkChat-MemoChat MCP 연결
4. `# Execution Flow` — Python REPL 코드 + KakaoTalk 발송 순서
5. `# Output Format` — 메시지 템플릿
6. `# Error Handling` — 비영업일, pykrx 오류, 빈 결과
7. `# Schedule Setup` — `/schedule daily 09:00 /stock-kakao-alert` 예시

---

## Risks and Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| pykrx API 변경 (컬럼명 변경) | Medium | High | `df.columns` 출력으로 컬럼 확인 후 동적 매핑; 컬럼 수 검증 |
| 장 비영업일(토·일·공휴일) 데이터 없음 | High | Medium | `get_nearest_business_day_in_a_week`로 최근 거래일 사용; 공휴일은 빈 DataFrame 처리 |
| 종목 코드 3자리/6자리 혼합 | Low | Low | pykrx ticker는 6자리 문자열로 일관됨 (ticker=`000660` 형식) |
| KakaotalkChat MCP 발송 실패 | Low | High | Python 실행 후 메시지 먼저 stdout 출력, MCP 실패 시 사용자에게 수동 복사 안내 |
| 20일 거래 데이터 부족 (상장 초기 종목) | Medium | Low | `if len(hist) < 5: continue` — 5거래일 미만 데이터 종목 건너뜀 |
| Python REPL 실행 시간 초과 (전 종목 개별 API 필요 시) | Low | Medium | Option A(배치 풀) 채택으로 시장당 OHLCV 1회 호출; 개별 종목 20일 hist는 필터 통과 후만 실행 |
| 장 마감 후 데이터 미집계 (15:30~16:30) | Medium | Medium | SKILL.md에 "16:30 이후 실행 권장" 명시; 배치 DataFrame이 비어있으면 "데이터 집계 중" 메시지 발송 |
| KakaotalkChat-MemoChat MCP 파라미터 스키마 변경 | Low | High | SKILL.md 작성 전 `mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat` 도구 스키마 실제 확인 필수 |

---

## Verification Steps

1. **단위 검증**: Python REPL에서 `d0="20241205"` 등 과거 거래일을 하드코딩해 AC1~AC5 수치 검증 (또는 `pykrx stock.get_market_ohlcv("20241205","KOSPI")['등락률'].max()` 로 상한가 종목 존재 여부 먼저 탐색)
2. **포맷 검증**: `message` 변수 `print()` 후 출력 형식이 스펙 Output Format과 일치하는지 육안 확인
3. **카카오 발송 검증**: MCP 호출 후 카카오톡 "나에게 쓰기" 채팅방에 메시지 수신 확인
4. **빈 결과 검증**: `results = []` 강제 설정 후 "오늘은 조건 만족 종목이 없습니다" 메시지 발송 확인
5. **비영업일 검증**: `d0`을 토요일 날짜로 설정 후 `get_nearest_business_day_in_a_week` 반환값 확인
6. **slash command 검증**: Claude Code에서 `/stock-kakao-alert` 입력 후 SKILL.md 실행 트리거 확인

---

## ADR (Architecture Decision Record)

### Decision
pykrx 배치 OHLCV 풀 + Python REPL 인라인 필터링 (Option A) 채택

### Drivers
1. KOSPI/KOSDAQ 전 종목 동시 커버가 필요함 (2500+ 종목)
2. API 호출 수 최소화로 실행 시간 수 초 내 완료 요구
3. 추가 인프라(서버, DB, 스케줄러) 없이 MCP 도구만 사용

### Alternatives Considered
- **Option B (ticker별 개별 API)**: 7000+ 호출로 실행 시간·레이트리밋 문제 → 기각
- **UsStockInfo MCP**: 미국 주식 전용, 한국 주식 지원 없음 → 기각
- **외부 증권사 API (KIS Developers 등)**: API 키 발급·비용·서버 필요 → 원칙 1 위반, 기각

### Why Chosen
배치 OHLCV 풀은 시장당 1 API 호출로 전 종목 데이터를 확보하며, pandas 벡터 연산으로 조건 필터링이 O(n) 수행됨. pykrx는 KRX 공식 데이터 무료 제공, pip 설치 즉시 사용 가능.

### Consequences
- **Positive**: 실행 시간 < 60초 목표 달성 가능, 유지보수 단순
- **Negative**: 종목별 20일 거래량 hist 조회는 필터 통과 후 개별 수행 (보통 0~10개 종목, 무시 가능)
- **Neutral**: pykrx 버전 업데이트 시 컬럼명 변경 가능성 → 컬럼 검증 코드 추가

### Follow-ups
- [ ] pykrx 2.x → 3.x 마이그레이션 시 컬럼명 매핑 업데이트
- [ ] 결과 10개 초과 시 메시지 분할 발송 기능 (v2)
- [ ] 특정 친구·그룹 채팅방 발송 옵션 (v2, 명세 외)

---

## Changelog (Improvements Applied)

**Architect review (iteration 1):**
- [BUG-1 FIXED] `todate=d0` → `todate=d1` — D-0 거래량을 20일 평균 계산에서 제외
- [BUG-2 FIXED] AC3 수치 단위 수정 — `≥ 0.299` → `≥ 29.9` (pykrx percent 단위 반영)
- [BUG-3 FIXED] 컬럼 rename을 positional → explicit dict 방식으로 변경
- [RISK ADDED] 장 마감 후 데이터 미집계 시나리오 리스크 항목 추가
- [RISK ADDED] KakaotalkChat-MemoChat MCP 파라미터 스키마 검증 필요 항목 추가

**Critic review (iteration 1) — APPROVE with improvements:**
- [AC6 강화] "발송 성공" → MCP 에러 없음 + 카카오 수신 육안 확인으로 구체화
- [AC8 강화] "graceful 처리" → `df_d0.empty` 조건 코드 + 발송 메시지 명시
- [Verification 강화] Step 1 날짜 예시 추가 (`20241205` 과거 거래일 하드코딩 방법)
