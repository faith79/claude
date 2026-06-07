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

def get_cap(ticker, date_str):
    """시가총액(원) 반환. 조회 실패 시 None."""
    try:
        df = stock.get_market_cap_by_date(date_str, date_str, ticker)
        if not df.empty and '시가총액' in df.columns:
            return float(df['시가총액'].iloc[0])
    except Exception:
        pass
    return None

def fmt_cap(c):
    if c is None:
        return "?"
    b = c / 1e8  # 억원 단위
    return f"{b/1e4:.1f}조" if b >= 10000 else f"{round(b)}억"

def compute_opinion(body_ratio, vol_ratio, cap):
    """시총·도지강도·거래량 배수로 투자의견 산출."""
    score, notes = 0, []
    if body_ratio <= 0.05:
        score += 1
        notes.append("강도지")
    if vol_ratio >= 3.0:
        score += 1
        notes.append("거래↑↑")
    if cap is not None:
        b = cap / 1e8
        if b >= 3000:
            score += 1
            notes.append("대형주")
        elif b >= 500:
            notes.append("중형주")
        else:
            score -= 1
            notes.append("소형주⚠️")
    icon  = "✅" if score >= 2 else ("⚠️" if score >= 1 else "❌")
    label = "매수추천" if score >= 2 else ("관망" if score >= 1 else "비추천")
    suffix = f"({','.join(notes)})" if notes else ""
    return icon, f"{label}{suffix}"

def fmt_date(d: str) -> str:
    return f"{int(d[4:6])}월 {int(d[6:8])}일"

all_dates = find_trading_days()
if len(all_dates) < offset + 2:
    print("INSUFFICIENT_DATA")
else:
    d0 = all_dates[offset]
    d1 = all_dates[offset + 1]
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
            d1_pct = df_d1.loc[ticker, 'change_pct']
            if not (29.9 <= d1_pct <= 30.0):
                continue

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

            cap = get_cap(ticker, d0)
            op_icon, op_label = compute_opinion(body_ratio, vol_ratio, cap)
            cap_str = fmt_cap(cap)

            name = stock.get_market_ticker_name(ticker)
            results.append({
                "ticker":   ticker, "name": name, "market": market_name,
                "d1_pct":   round(float(d1_pct), 2),
                "d0_body":  round(float(body_ratio), 3),
                "d0_upper": round(float(upper_ratio), 3),
                "d0_lower": round(float(lower_ratio), 3),
                "d0_vol":   round(float(vol_ratio), 2),
                "op_icon":  op_icon,
                "op_label": op_label,
                "cap_str":  cap_str,
            })

    print(f"RESULTS_COUNT={len(results)}")
    print(f"DATE_D1={d1}|DATE_D0={d0}")
    for r in results:
        print(f"STOCK|{r['ticker']}|{r['name']}|{r['market']}|{r['d1_pct']}|{r['d0_body']}|{r['d0_upper']}|{r['d0_lower']}|{r['d0_vol']}|{r['op_icon']}|{r['op_label']}|{r['cap_str']}")
