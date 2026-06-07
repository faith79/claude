# Deep Interview Spec: 한국 주식 조건 스캐너 → 카카오톡 발송 스킬

## Metadata
- Interview ID: stock-kakao-alert-20260607
- Rounds: 6
- Final Ambiguity Score: 14.6%
- Type: greenfield
- Generated: 2026-06-07
- Threshold: 0.20
- Threshold Source: default
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.92 | 40% | 0.368 |
| Constraint Clarity | 0.80 | 30% | 0.240 |
| Success Criteria | 0.82 | 30% | 0.246 |
| **Total Clarity** | | | **0.854** |
| **Ambiguity** | | | **14.6%** |

## Topology
| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| Stock Recommendation | active | KOSPI/KOSDAQ 종목 중 3개 조건 동시 만족 종목 선정 | ✓ 조건 알고리즘 완전 정의 |
| KakaoTalk Delivery | active | 결과를 "나에게 보내기"로 포맷·발송 | ✓ 대상·포맷·예외처리 정의 |
| Skill Trigger | active | On-demand 호출 + 선택적 스케줄 | ✓ 두 가지 실행 경로 정의 |

## Goal
코스피/코스닥 전 종목 대상으로 **D-2 상한가 → D-1 도지 캔들 → D-0 거래량 급증** 3가지 조건을 순차적으로 모두 만족하는 종목을 매일 스캐닝하여, 결과를 포맷된 메시지로 카카오톡 "나에게 보내기"로 발송하는 Claude Code 스킬을 만든다.

## Stock Selection Algorithm (3-Step Filter)

### 조건 1 — D-2 (2거래일 전): 상한가
- 전일 종가 대비 등락률 +29.9% ~ +30.0%
- 또는 당일 상한가(Upper Limit Price)에 도달하여 마감

### 조건 2 — D-1 (1거래일 전): 십자형 캔들(Doji)
- |종가 - 시가| / (고가 - 저가) ≤ 0.10
- 위아래 수염 모두 존재 (시가 ≠ 저가, 종가 ≠ 고가)

### 조건 3 — D-0 (당일/최근 종가): 거래량 급증
- 당일 거래량 ≥ 최근 20거래일 평균 거래량 × 1.5

## Output Format (KakaoTalk Message)
```
📊 [주식 조건 스캐너] {날짜}

✅ 조건 만족 종목 {N}개

1. {종목명} ({종목코드}) [{코스피/코스닥}]
   - D-2 등락률: +29.95%
   - D-1 캔들 비율: 0.07
   - D-0 거래량 비율: 2.3x (20일 평균 대비)

2. ...

---
🔍 조건: D-2 상한가 → D-1 도지 → D-0 거래량 1.5배↑
```

결과 없을 때:
```
📊 [주식 조건 스캐너] {날짜}
⚠️ 오늘은 조건 만족 종목이 없습니다.
```

## Constraints
- 대상: 코스피 + 코스닥 전 종목
- 데이터 소스: pykrx (Python 라이브러리, 무료, KRX 공식 데이터)
- 발송 대상: 나에게 보내기 (KakaotalkChat-MemoChat MCP)
- 결과 수 제한: 없음 (조건 만족 전체 발송)
- 빈 결과 처리: "해당 종목 없음" 메시지 발송
- 트리거: on-demand (수동) + 선택적 스케줄 (`/schedule` 활용)

## Non-Goals
- 미국 주식 (NASDAQ/NYSE) 스캐닝
- 매수/매도 자동 주문 실행
- 종목 차트 이미지 생성
- 특정 친구·그룹 채팅방 발송 (v1 범위 외)
- 알림 기록 저장 및 히스토리 관리

## Acceptance Criteria
- [ ] pykrx로 당일 기준 KOSPI + KOSDAQ 전 종목의 D-0, D-1, D-2 OHLCV 데이터를 수집
- [ ] 3개 조건 필터링 로직이 명세대로 계산 (등락률, 캔들 비율, 거래량 비율)
- [ ] 조건 만족 종목이 있으면 포맷된 메시지를 KakaotalkChat-MemoChat으로 발송
- [ ] 조건 만족 종목이 없으면 "해당 종목 없음" 메시지를 발송
- [ ] `/stock-kakao-alert` 또는 유사한 슬래시 커맨드로 on-demand 호출 가능
- [ ] 스킬 내 스케줄 안내 포함 (daily cron 설정 방법)
- [ ] 장 비영업일(토·일·공휴일)에 실행 시 graceful 처리 (데이터 없음 메시지)

## Technical Context
- **Python REPL**: `mcp__plugin_oh-my-claudecode_t__python_repl` 사용 가능 — pykrx 실행
- **KakaoTalk MCP**: `mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat` 연결됨
- **pykrx**: `pip install pykrx` 후 `from pykrx import stock` 사용
  - `stock.get_market_ohlcv(date, market='KOSPI')` — OHLCV 전 종목
  - `stock.get_market_ticker_list(date, market='KOSPI')` — 종목 코드 목록
- **거래일 계산**: pykrx `get_nearest_business_day_in_a_week` 활용

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| StockCandle | core domain | ticker, date, open, high, low, close, volume | StockCandle belongs to Market |
| Condition | core domain | d2_upper_limit, d1_doji_ratio, d0_volume_ratio | Condition applies to StockCandle |
| MatchedStock | core domain | ticker, name, market, d2_change, d1_body_ratio, d0_vol_ratio | MatchedStock is output of Condition |
| KakaoMessage | supporting | date, matched_count, stocks[], empty_flag | KakaoMessage wraps MatchedStock list |
| Market | external system | KOSPI, KOSDAQ | Market contains StockCandle |

## Implementation Notes
```
Skill execution flow:
1. Python REPL → pykrx → get OHLCV for today, D-1, D-2 (KOSPI + KOSDAQ)
2. For each ticker: apply 3 conditions in sequence (short-circuit)
3. Collect MatchedStock list
4. Format KakaoMessage string
5. Call KakaotalkChat-MemoChat MCP → send to 나에게
```

## Interview Transcript
<details>
<summary>Full Q&A (6 rounds)</summary>

### Round 0 (Topology)
**Q:** Topology 3개 컴포넌트가 맞나요?
**A:** 맞아요 (3개 모두 포함)

### Round 1 (Goal — Stock Recommendation)
**Q:** 주식을 어떤 방식으로 선정하려고 하나요?
**A:** (직접 입력) D-2 상한가 + D-1 도지 캔들 + D-0 거래량 150% — 3개 조건 동시 만족 KOSPI/KOSDAQ 종목

### Round 2 (Goal — KakaoTalk Delivery)
**Q:** 카카오톡 도착지는 어디인가요?
**A:** 나에게로 우선, 나중에 더 고려

### Round 3 (Goal — Skill Trigger)
**Q:** 이 스킬은 언제 실행되어야 하나요?
**A:** on-demand + 선택적 스케줄

### Round 4 (Constraint — Data Source)
**Q:** 데이터를 어디서 가져오련가요?
**A:** 데이터 수집은 Claude가 결정하면 돼

### Round 5 (Success Criteria — Empty Result)
**Q:** 조건을 모두 만족하는 종목이 없으면 어떻게 보내나요?
**A:** "해당 종목 없음" 메시지 전송

### Round 6 (Success Criteria — Result Limit)
**Q:** 조건 만족 종목이 10개면 몇 개까지 보내야 하나요?
**A:** 제한 없이 전체 전송
</details>
