# 한국 주식 조건 스캐너 → 카카오톡 발송

## Purpose
코스피/코스닥 전 종목 대상으로 **D-1 상한가 → D-0 장다리 도지 캔들 + 거래량 급증** 조건을 만족하는 종목을 스캐닝하여, 결과를 카카오톡 "나에게 보내기"로 발송합니다.

숫자 인자로 과거 거래일 기준 조회 가능:
- `/stock-kakao-alert` → 최근 2거래일 (목→금)
- `/stock-kakao-alert 1` → 1거래일 이전 (화→목)
- `/stock-kakao-alert 2` → 2거래일 이전 (월→화), 선거일 등 휴장일 자동 제외

## Trigger
`/stock-kakao-alert [offset]`  (offset 생략 시 0)

## Prerequisites
- Python 3.8+ (pykrx, finance-datareader 자동 설치 포함)
- KakaotalkChat-MemoChat MCP 연결됨 (`mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat`)
- 인터넷 연결 (KRX 데이터, Naver Finance, PyPI 접근)

## Execution Flow

### Step 1: Run Python via PowerShell

Parse `[offset]` from the skill invocation argument (default 0 if omitted).

Execute via PowerShell:
```
$env:PYTHONIOENCODING="utf-8"; & "C:\Users\JOEY\AppData\Local\Programs\Python\Python312\python.exe" "D:\GIT\claude\_stock_scan.py" {offset} 2>$null
```

The script at `D:\GIT\claude\_stock_scan.py` must contain the code below. Write it before running if it doesn't exist.

```python
import subprocess, sys

offset = int(sys.argv[1]) if len(sys.argv) > 1 else 0

try:
    from pykrx import stock
except ImportError:
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pykrx", "-q"])
    from pykrx import stock

import pandas as pd
from datetime import datetime, timedelta
from concurrent.futures import ThreadPoolExecutor, as_completed

_RENAME = {
    '시가': 'open', '고가': 'high', '저가': 'low', '종가': 'close',
    '거래량': 'volume', '거래대금': 'trading_value', '등락률': 'change_pct'
}

def safe_fetch(date: str, market: str) -> pd.DataFrame:
    try:
        df = stock.get_market_ohlcv(date, market=market)
        return df.rename(columns=_RENAME) if not df.empty else pd.DataFrame()
    except Exception:
        return pd.DataFrame()

def fetch_market_weekend(d0: str, d1: str, market: str):
    """FinanceDataReader(Naver Finance) 기반 주말 대체 수집. 2거래일(d0, d1) 반환."""
    try:
        import FinanceDataReader as fdr
    except ImportError:
        subprocess.check_call([sys.executable, "-m", "pip", "install", "finance-datareader", "-q"])
        import FinanceDataReader as fdr

    listing = fdr.StockListing(market)
    tickers = listing['Code'].tolist()
    all_data = {}

    def fetch_one(ticker):
        try:
            df = fdr.DataReader(ticker, start=d1, end=d0)
            return (ticker, df) if not df.empty else (ticker, None)
        except Exception:
            return ticker, None

    with ThreadPoolExecutor(max_workers=10) as ex:
        futures = {ex.submit(fetch_one, t): t for t in tickers}
        for f in as_completed(futures):
            ticker, df = f.result()
            if df is not None:
                all_data[ticker] = df

    def to_day_df(date_str: str) -> pd.DataFrame:
        ts = pd.Timestamp(date_str)
        rows = {}
        for ticker, df in all_data.items():
            if ts in df.index:
                r = df.loc[ts]
                rows[ticker] = {
                    'open':       float(r.get('Open',   0)),
                    'high':       float(r.get('High',   0)),
                    'low':        float(r.get('Low',    0)),
                    'close':      float(r.get('Close',  0)),
                    'volume':     float(r.get('Volume', 0)),
                    'change_pct': round(float(r.get('Change', 0)) * 100, 4),
                }
        return pd.DataFrame(rows).T if rows else pd.DataFrame()

    return to_day_df(d0), to_day_df(d1)

def find_trading_days(max_collect: int = 40) -> list:
    """실제 거래일 목록 반환 (최신순). 삼성전자 캘린더 기반으로 공휴일 자동 제외."""
    d = datetime.today()
    candidates = []
    for _ in range(max_collect * 3):
        if d.weekday() < 5:
            candidates.append(d.strftime("%Y%m%d"))
            if len(candidates) == max_collect:
                break
        d -= timedelta(days=1)
    if not candidates:
        return []
    try:
        df = stock.get_market_ohlcv(
            fromdate=candidates[-1], todate=candidates[0], ticker="005930"
        )
        if df.empty:
            return []
        return [idx.strftime("%Y%m%d") for idx in reversed(df.index)]
    except Exception:
        return []

def fmt_date(d: str) -> str:
    return f"{int(d[4:6])}월 {int(d[6:8])}일"

all_dates = find_trading_days()
if len(all_dates) < offset + 2:
    print("INSUFFICIENT_DATA")
else:
    d0 = all_dates[offset]      # 기준 D-0 (장다리도지+거래량 확인일)
    d1 = all_dates[offset + 1]  # 기준 D-1 (상한가 확인일)
    print(f"스캔 날짜: D-1={d1}({fmt_date(d1)}), D-0={d0}({fmt_date(d0)}) [offset={offset}]")

    kospi_d0  = safe_fetch(d0, "KOSPI")
    kospi_d1  = safe_fetch(d1, "KOSPI")
    kosdaq_d0 = safe_fetch(d0, "KOSDAQ")
    kosdaq_d1 = safe_fetch(d1, "KOSDAQ")

    if kospi_d0.empty and kosdaq_d0.empty:
        print("배치 수집 불가, FinanceDataReader fallback 시작 (약 2분 소요)...")
        with ThreadPoolExecutor(max_workers=2) as ex:
            fk = ex.submit(fetch_market_weekend, d0, d1, "KOSPI")
            fq = ex.submit(fetch_market_weekend, d0, d1, "KOSDAQ")
            kospi_d0, kospi_d1   = fk.result()
            kosdaq_d0, kosdaq_d1 = fq.result()
        print(f"[fallback] KOSPI={len(kospi_d0)}, KOSDAQ={len(kosdaq_d0)}")
    else:
        print(f"수집: KOSPI={len(kospi_d0)}, KOSDAQ={len(kosdaq_d0)}")

    results = []
    for market_name, df_d0, df_d1 in [
        ("KOSPI",  kospi_d0,  kospi_d1),
        ("KOSDAQ", kosdaq_d0, kosdaq_d1),
    ]:
        common = df_d0.index.intersection(df_d1.index)
        for ticker in common:
            # 조건 1: D-1 상한가 (+29.9% ~ +30.0%)
            d1_pct = df_d1.loc[ticker, 'change_pct']
            if not (29.9 <= d1_pct <= 30.0):
                continue

            # 조건 2: D-0 장다리 도지 캔들
            # 몸통 ≤ 10%, 위 수염 ≥ 30%, 아래 수염 ≥ 30%
            o  = df_d0.loc[ticker, 'open']
            h  = df_d0.loc[ticker, 'high']
            lo = df_d0.loc[ticker, 'low']
            c  = df_d0.loc[ticker, 'close']
            body_range   = h - lo
            if body_range == 0:
                continue
            body_ratio   = abs(c - o) / body_range
            upper_shadow = h - max(o, c)
            lower_shadow = min(o, c) - lo
            upper_ratio  = upper_shadow / body_range
            lower_ratio  = lower_shadow / body_range
            if body_ratio > 0.10 or upper_ratio < 0.30 or lower_ratio < 0.30:
                continue

            # 조건 3: D-0 거래량 >= 20일 평균 x 1.5
            vol_d0 = df_d0.loc[ticker, 'volume']
            from_date = (datetime.strptime(d1, "%Y%m%d") - timedelta(days=45)).strftime("%Y%m%d")
            hist = stock.get_market_ohlcv(fromdate=from_date, todate=d1, ticker=ticker)
            if len(hist) < 5:
                continue
            vol_avg_20 = hist['거래량'].tail(20).mean()
            if vol_avg_20 == 0:
                continue
            vol_ratio = vol_d0 / vol_avg_20
            if vol_ratio < 1.5:
                continue

            name = stock.get_market_ticker_name(ticker)
            results.append({
                "ticker":   ticker, "name": name, "market": market_name,
                "d1_pct":   round(float(d1_pct), 2),
                "d0_body":  round(float(body_ratio), 3),
                "d0_upper": round(float(upper_ratio), 3),
                "d0_lower": round(float(lower_ratio), 3),
                "d0_vol":   round(float(vol_ratio), 2),
            })

    print(f"RESULTS_COUNT={len(results)}")
    print(f"DATE_D1={d1}|DATE_D0={d0}")
    for r in results:
        print(f"STOCK|{r['ticker']}|{r['name']}|{r['market']}|{r['d1_pct']}|{r['d0_body']}|{r['d0_upper']}|{r['d0_lower']}|{r['d0_vol']}")
```

### Step 2: Parse output and send KakaoTalk

Parse the `DATE_D1` and `DATE_D0` lines from stdout for message formatting.

```
fmt_date(d) = M월 D일  (e.g. "20260601" → "6월 1일")
```

**If `INSUFFICIENT_DATA`:**
Send: `{ "message": "📊 [주식 조건 스캐너] 데이터 부족 (offset={offset}). 잠시 후 다시 시도해주세요." }`
Then stop.

**If `RESULTS_COUNT=0`:**
Send: `{ "message": "📊 [주식 조건 스캐너]\n기준일: {fmt_date(d1)} ~ {fmt_date(d0)}\n⚠️ 조건 만족 종목이 없습니다.\n🔍 D-1 상한가→D-0 장다리도지+거래량 1.5배↑" }`
Then stop.

**If `RESULTS_COUNT=N` (N ≥ 1):**

Message 1 — 헤더:
`{ "message": "📊 [주식 조건 스캐너]\n기준일: {fmt_date(d1)} ~ {fmt_date(d0)}\n✅ 조건 만족 종목 {N}개" }`

For each STOCK line (i=1,2,...):
`{ "message": "{i}. {name}({ticker})[{market}]\nD-1 상한가:+{d1_pct}% / D-0 몸통:{d0_body} 위:{d0_upper} 아래:{d0_lower} 거래:{d0_vol}x" }`

Message last — 푸터:
`{ "message": "🔍 조건: D-1 상한가→D-0 장다리도지+거래량 1.5배↑" }`

All messages sent via `mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat`.

## 조건 설명

| 조건 | 기준 | 설명 |
|------|------|------|
| D-1 상한가 | +29.9% ~ +30.0% | 전날 상한가 |
| D-0 장다리 도지 — 몸통 | \|종가-시가\| / (고가-저가) ≤ 10% | 작은 몸통 |
| D-0 장다리 도지 — 위 수염 | (고가 - max(시가,종가)) / (고가-저가) ≥ 30% | 긴 위 꼬리 |
| D-0 장다리 도지 — 아래 수염 | (min(시가,종가) - 저가) / (고가-저가) ≥ 30% | 긴 아래 꼬리 |
| D-0 거래량 | D-0 거래량 ≥ 20일 평균 × 1.5 | 평균 대비 1.5배 이상 |

## offset 동작 예시 (일요일 2026-06-07 기준, 6/3 선거일 자동 제외)

| 호출 | D-1 | D-0 | 기준일 |
|------|-----|-----|--------|
| `/stock-kakao-alert` | 6/4(목) | 6/5(금) | 6월 4일 ~ 6월 5일 |
| `/stock-kakao-alert 1` | 6/2(화) | 6/4(목) | 6월 2일 ~ 6월 4일 |
| `/stock-kakao-alert 2` | 6/1(월) | 6/2(화) | 6월 1일 ~ 6월 2일 |
| `/stock-kakao-alert 3` | 5/29(목) | 6/1(월) | 5월 29일 ~ 6월 1일 |

## Output Format

### 결과 있을 때
```
[메시지 1] 📊 [주식 조건 스캐너]
기준일: 6월 1일 ~ 6월 2일
✅ 조건 만족 종목 1개

[메시지 2] 1. 로보스타(090360)[KOSDAQ]
D-1 상한가:+29.95% / D-0 몸통:0.05 위:0.42 아래:0.38 거래:3.2x

[메시지 3] 🔍 조건: D-1 상한가→D-0 장다리도지+거래량 1.5배↑
```

### 결과 없을 때
```
📊 [주식 조건 스캐너]
기준일: 6월 4일 ~ 6월 5일
⚠️ 조건 만족 종목이 없습니다.
🔍 D-1 상한가→D-0 장다리도지+거래량 1.5배↑
```

## Error Handling

| 상황 | 처리 |
|------|------|
| pykrx 미설치 | 자동 pip install 후 재실행 |
| offset이 너무 커서 데이터 부족 | INSUFFICIENT_DATA 발송 |
| 주말·장외 (배치 empty) | FinanceDataReader(Naver Finance) fallback |
| body_range == 0 | 해당 종목 건너뜀 |
| len(hist) < 5 (상장 초기) | 해당 종목 건너뜀 |
| 결과 0개 | "조건 만족 종목이 없습니다" 발송 |

## Notes

- **데이터 소스**: pykrx (KRX 공식) + FinanceDataReader(Naver Finance, 주말/장외 fallback)
- **대상 시장**: KOSPI + KOSDAQ 전 종목 (~2,700개)
- **실행 시간**: 평일 약 30-60초 / 주말 약 2분 (fallback)
- **거래일 캘린더**: 삼성전자(005930) 단일 ticker 조회 기반 — 공휴일·선거일 자동 제외
- **v1 범위 외**: 미국 주식, 자동 매수/매도, 차트 이미지, 그룹 채팅방 발송
