# 한국 주식 조건 스캐너 → 카카오톡 발송

## Purpose
코스피/코스닥 전 종목 대상으로 **D-1 상한가 → D-0 도지 캔들 + 거래량 급증** 2가지 조건을 모두 만족하는 종목을 스캐닝하여, 결과를 카카오톡 "나에게 보내기"로 발송합니다.

주말·공휴일에 실행해도 **실제 데이터가 있는 최근 2 거래일**을 자동으로 탐색합니다.
(예: 일요일 실행 → 목(D-1)·금(D-0) 데이터 활용 / 목요일이 휴장이면 수(D-1)·금(D-0) 활용)

## Trigger
`/stock-kakao-alert`

## Prerequisites
- Python 3.8+ (pykrx, finance-datareader 자동 설치 포함)
- KakaotalkChat-MemoChat MCP 연결됨 (`mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat`)
- 인터넷 연결 (KRX 데이터, Naver Finance, PyPI 접근)

## Execution Flow

### Step 1: Run Python via PowerShell

Execute via PowerShell: `$env:PYTHONIOENCODING="utf-8"; & "C:\Users\JOEY\AppData\Local\Programs\Python\Python312\python.exe" "D:\GIT\claude\_stock_scan.py" 2>$null`

The script at `D:\GIT\claude\_stock_scan.py` must contain the code below. Write it before running if it doesn't exist.

```python
import subprocess, sys
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

def find_trading_days(n: int = 2, max_back: int = 20) -> list:
    """최근 n 거래일 날짜 반환 (최신순). 주말·공휴일에도 동작."""
    d = datetime.today()
    candidates = []
    for _ in range(max_back * 2):
        if d.weekday() < 5:
            candidates.append(d.strftime("%Y%m%d"))
            if len(candidates) == max_back:
                break
        d -= timedelta(days=1)
    if len(candidates) < n:
        return []
    try:
        df = stock.get_market_ohlcv(
            fromdate=candidates[-1], todate=candidates[0], ticker="005930"
        )
        if df.empty:
            return []
        dates = [idx.strftime("%Y%m%d") for idx in reversed(df.index)]
        return dates[:n]
    except Exception:
        return []

dates = find_trading_days(2)
if len(dates) < 2:
    print("INSUFFICIENT_DATA")
else:
    d0, d1 = dates  # d0=최근(금), d1=전일(목)
    print(f"스캔 날짜: D-1={d1}, D-0={d0}")

    kospi_d0  = safe_fetch(d0, "KOSPI")
    kospi_d1  = safe_fetch(d1, "KOSPI")
    kosdaq_d0 = safe_fetch(d0, "KOSDAQ")
    kosdaq_d1 = safe_fetch(d1, "KOSDAQ")

    if kospi_d0.empty and kosdaq_d0.empty:
        print("배치 수집 불가, FinanceDataReader fallback 시작 (약 2분 소요)...")
        with ThreadPoolExecutor(max_workers=2) as ex:
            fk = ex.submit(fetch_market_weekend, d0, d1, "KOSPI")
            fq = ex.submit(fetch_market_weekend, d0, d1, "KOSDAQ")
            kospi_d0,  kospi_d1  = fk.result()
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

            # 조건 2: D-0 도지 캔들 (|종가-시가|/(고가-저가) ≤ 0.10 + 위아래 수염 필수)
            o  = df_d0.loc[ticker, 'open']
            h  = df_d0.loc[ticker, 'high']
            lo = df_d0.loc[ticker, 'low']
            c  = df_d0.loc[ticker, 'close']
            body_range = h - lo
            if body_range == 0:
                continue
            doji_ratio = abs(c - o) / body_range
            if doji_ratio > 0.10 or o == lo or c == h:
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
                "ticker": ticker, "name": name, "market": market_name,
                "d1_pct":  round(float(d1_pct), 2),
                "d0_doji": round(float(doji_ratio), 3),
                "d0_vol":  round(float(vol_ratio), 2),
            })

    print(f"RESULTS_COUNT={len(results)}")
    for r in results:
        print(f"STOCK|{r['ticker']}|{r['name']}|{r['market']}|{r['d1_pct']}|{r['d0_doji']}|{r['d0_vol']}")
```

### Step 2: Parse output and send KakaoTalk

After the script completes, parse stdout:

**If `INSUFFICIENT_DATA`:**
Send via `mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat`:
`{ "message": "📊 [주식 조건 스캐너] 최근 거래일 데이터를 충분히 수집하지 못했습니다. 잠시 후 다시 시도해주세요." }`
Then stop.

**If `RESULTS_COUNT=0`:**
Send: `{ "message": "📊 [주식 조건 스캐너] D-0: {d0}\n⚠️ 조건 만족 종목이 없습니다.\n🔍 D-1 상한가→D-0 도지+거래량 1.5배↑" }`
Then stop.

**If `RESULTS_COUNT=N` (N ≥ 1):**

Message 1 — 헤더: `{ "message": "📊 [주식 조건 스캐너] D-0: {d0}\n✅ 조건 만족 종목 {N}개" }`

For each STOCK line (i=1,2,...): `{ "message": "{i}. {name}({ticker})[{market}]\nD-1 상한가:+{d1_pct}% / D-0 도지:{d0_doji} 거래량:{d0_vol}x" }`

Message last — 푸터: `{ "message": "🔍 조건: D-1 상한가→D-0 도지 캔들+거래량 1.5배↑" }`

All messages sent via `mcp__claude_ai_PlayMCP__KakaotalkChat-MemoChat`.

## 조건 설명

| 조건 | 기준 | 설명 |
|------|------|------|
| D-1 상한가 | +29.9% ~ +30.0% | 전날 상한가 |
| D-0 도지 캔들 | \|종가-시가\|/(고가-저가) ≤ 10%, 위아래 수염 존재 | 당일 십자 캔들 |
| D-0 거래량 | D-0 거래량 ≥ 20일 평균 × 1.5 | 평균 대비 1.5배 이상 |

## Output Format

### 결과 있을 때
```
[메시지 1] 📊 [주식 조건 스캐너] D-0: 20260605
✅ 조건 만족 종목 1개

[메시지 2] 1. 로보스타(090360)[KOSDAQ]
D-1 상한가:+29.95% / D-0 도지:0.08 거래량:3.2x

[메시지 3] 🔍 조건: D-1 상한가→D-0 도지 캔들+거래량 1.5배↑
```

### 결과 없을 때
```
📊 [주식 조건 스캐너] D-0: 20260605
⚠️ 조건 만족 종목이 없습니다.
🔍 D-1 상한가→D-0 도지+거래량 1.5배↑
```

## Error Handling

| 상황 | 처리 |
|------|------|
| pykrx 미설치 | 자동 `pip install pykrx` 후 재실행 |
| 최근 20 평일 내 거래일 2일 미발견 | `INSUFFICIENT_DATA` → 재시도 안내 발송 |
| 주말·장외 (배치 empty) | FinanceDataReader(Naver Finance) fallback |
| body_range == 0 | 해당 종목 건너뜀 |
| len(hist) < 5 (상장 초기) | 해당 종목 건너뜀 |
| 결과 0개 | "조건 만족 종목이 없습니다" 발송 |

## 실행 시점별 동작 예시

| 실행 시점 | D-1 | D-0 |
|-----------|-----|-----|
| 금요일 17시 이후 | 목 | 금 |
| 토·일 | 목 | 금 |
| 월요일 (월=휴장) | 목 | 금 |
| 연휴 직후 | 연휴 전 2번째 거래일 | 마지막 거래일 |

## Schedule Setup

```
/schedule "0 17 * * 1-5" /stock-kakao-alert
```
(평일 오후 5시 실행 — 장 마감 후 데이터 완전 집계 보장)

## Notes

- **데이터 소스**: pykrx (KRX 공식) + FinanceDataReader(Naver Finance, 주말 fallback)
- **대상 시장**: KOSPI + KOSDAQ 전 종목 (~2,700개)
- **실행 시간**: 평일 약 30-60초 / 주말 약 2분 (fallback)
- **v1 범위 외**: 미국 주식, 자동 매수/매도, 차트 이미지, 그룹 채팅방 발송
