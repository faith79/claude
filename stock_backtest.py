import subprocess, sys
from datetime import datetime, timedelta
from concurrent.futures import ThreadPoolExecutor, as_completed

for pkg, imp in [("pykrx", "pykrx"), ("finance-datareader", "FinanceDataReader")]:
    try:
        __import__(imp)
    except ImportError:
        subprocess.check_call([sys.executable, "-m", "pip", "install", pkg, "-q"])

from pykrx import stock
import FinanceDataReader as fdr
import pandas as pd

today = datetime.today()

d = today
while d.weekday() >= 5:
    d -= timedelta(days=1)
data_end_dt = d
data_end   = data_end_dt.strftime("%Y%m%d")

data_start_dt = data_end_dt - timedelta(days=90)
data_start    = data_start_dt.strftime("%Y%m%d")

report_start_dt = data_end_dt - timedelta(days=35)
report_start    = report_start_dt.strftime("%Y%m%d")

print(f"데이터 범위: {data_start} ~ {data_end}")
print(f"리포트 범위: {report_start} ~ {data_end}")

print("\n거래일 캘린더 조회 중...")
cal_df = stock.get_market_ohlcv(fromdate=data_start, todate=data_end, ticker="005930")
trading_days = [idx.strftime("%Y%m%d") for idx in cal_df.index]
report_days  = [d for d in trading_days if d >= report_start]
print(f"전체 거래일: {len(trading_days)}일  |  리포트 대상: {len(report_days)}일 ({report_days[0]}~{report_days[-1]})")

print("\n전 종목 OHLCV 수집 중 (약 3~5분)...")
kospi_list  = fdr.StockListing("KOSPI")["Code"].tolist()
kosdaq_list = fdr.StockListing("KOSDAQ")["Code"].tolist()
ticker_mkt  = [(t, "KOSPI") for t in kospi_list] + [(t, "KOSDAQ") for t in kosdaq_list]

all_data: dict[tuple, pd.DataFrame] = {}

def fetch_one(tm):
    ticker, market = tm
    try:
        df = fdr.DataReader(ticker, start=data_start, end=data_end)
        if not df.empty:
            return (ticker, market, df)
    except Exception:
        pass
    return (ticker, market, None)

with ThreadPoolExecutor(max_workers=15) as ex:
    futures = {ex.submit(fetch_one, tm): tm for tm in ticker_mkt}
    done = 0
    for f in as_completed(futures):
        ticker, market, df = f.result()
        if df is not None:
            all_data[(ticker, market)] = df
        done += 1
        if done % 500 == 0:
            print(f"  [{done}/{len(ticker_mkt)}] 수집 중...")

print(f"수집 완료: {len(all_data)}개 종목")

def get_day(ticker, market, date_str):
    df = all_data.get((ticker, market))
    if df is None:
        return None
    ts = pd.Timestamp(date_str)
    if ts not in df.index:
        return None
    r = df.loc[ts]
    return {
        "open":       float(r.get("Open",   0)),
        "high":       float(r.get("High",   0)),
        "low":        float(r.get("Low",    0)),
        "close":      float(r.get("Close",  0)),
        "volume":     float(r.get("Volume", 0)),
        "change_pct": round(float(r.get("Change", 0)) * 100, 4),
    }

def vol_avg20(ticker, market, up_to_date_str):
    df = all_data.get((ticker, market))
    if df is None:
        return None
    ts = pd.Timestamp(up_to_date_str)
    sub = df[df.index <= ts]["Volume"]
    if len(sub) < 5:
        return None
    return float(sub.tail(20).mean())

def get_cap(ticker, date_str):
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
    b = c / 1e8
    return f"{b/1e4:.1f}조" if b >= 10000 else f"{round(b)}억"

def compute_opinion(body_ratio, vol_ratio, cap):
    score = 0
    if body_ratio <= 0.05:
        score += 1
    if vol_ratio >= 3.0:
        score += 1
    if cap is not None:
        b = cap / 1e8
        if b >= 3000:
            score += 1
        elif b < 500:
            score -= 1
    return score

def fmt(d):
    return f"{int(d[4:6])}/{int(d[6:8])}"

print("\n조건 스캔 중...")
qualifying = []

for i in range(len(trading_days) - 2):
    d1_date  = trading_days[i]
    d0_date  = trading_days[i + 1]
    dp1_date = trading_days[i + 2]

    if d0_date not in report_days:
        continue

    for (ticker, market) in list(all_data.keys()):
        r_d1 = get_day(ticker, market, d1_date)
        if r_d1 is None:
            continue
        if not (29.9 <= r_d1["change_pct"] <= 30.0):
            continue

        r_d0 = get_day(ticker, market, d0_date)
        if r_d0 is None:
            continue
        o, h, lo, c = r_d0["open"], r_d0["high"], r_d0["low"], r_d0["close"]
        body_range = h - lo
        if body_range == 0:
            continue
        body_ratio   = abs(c - o) / body_range
        upper_ratio  = (h - max(o, c)) / body_range
        lower_ratio  = (min(o, c) - lo) / body_range
        if body_ratio > 0.10 or upper_ratio < 0.30 or lower_ratio < 0.30:
            continue

        avg_vol = vol_avg20(ticker, market, d1_date)
        if avg_vol is None or avg_vol == 0:
            continue
        vol_ratio = r_d0["volume"] / avg_vol
        if vol_ratio < 1.5:
            continue

        r_dp1 = get_day(ticker, market, dp1_date)
        if r_dp1 is None or r_dp1["open"] == 0:
            continue

        dp1_open  = r_dp1["open"]
        dp1_high  = r_dp1["high"]
        dp1_close = r_dp1["close"]
        max_gain  = (dp1_high / dp1_open - 1) * 100

        try:
            name = stock.get_market_ticker_name(ticker)
        except Exception:
            name = ticker

        qualifying.append({
            "ticker":    ticker,
            "name":      name,
            "market":    market,
            "d1_date":   d1_date,
            "d0_date":   d0_date,
            "dp1_date":  dp1_date,
            "d1_pct":    round(r_d1["change_pct"], 2),
            "d0_body":   round(body_ratio, 3),
            "d0_upper":  round(upper_ratio, 3),
            "d0_lower":  round(lower_ratio, 3),
            "d0_vol":    round(vol_ratio, 2),
            "bullish":   c >= o,           # 양봉 도지
            "dp1_open":  dp1_open,
            "dp1_high":  dp1_high,
            "dp1_close": dp1_close,
            "dp1_gap":   round((dp1_open / r_d0["close"] - 1) * 100, 2) if r_d0["close"] else 0,
            "max_gain":  round(max_gain, 2),
            "ok_3pct":   dp1_high >= dp1_open * 1.03,
            "ok_5pct":   dp1_high >= dp1_open * 1.05,
        })

print(f"1차 필터 통과: {len(qualifying)}건 → 시총 조회 중...")

for q in qualifying:
    cap = get_cap(q["ticker"], q["d0_date"])
    q["cap"]       = cap
    q["cap_str"]   = fmt_cap(cap)
    q["cap_ok"]    = cap is not None and cap >= 500e8   # 500억 이상
    q["cap_ok_1k"] = cap is not None and cap >= 1000e8  # 1000억 이상
    q["op_score"]  = compute_opinion(q["d0_body"], q["d0_vol"], cap)

print(f"시총 조회 완료")

# ── 필터 조합 정의 ─────────────────────────────────────────────
filters = {
    "기본 (현재)":             lambda q: True,
    "거래량 2배+":             lambda q: q["d0_vol"] >= 2.0,
    "시총 500억+":             lambda q: q["cap_ok"],
    "시총 1000억+":            lambda q: q["cap_ok_1k"],
    "투자의견 관망+ (≥1점)":   lambda q: q["op_score"] >= 1,
    "투자의견 매수추천 (≥2점)": lambda q: q["op_score"] >= 2,
    "양봉 도지":               lambda q: q["bullish"],
    "시총500억+ & 거래량2배+": lambda q: q["cap_ok"] and q["d0_vol"] >= 2.0,
    "시총500억+ & 양봉도지":   lambda q: q["cap_ok"] and q["bullish"],
    "갭업 5%미만":             lambda q: q["dp1_gap"] < 5.0,
}

# ── 리포트 생성 ─────────────────────────────────────────────────
lines = []
lines.append("=" * 65)
lines.append("📊  주식 조건 스캐너 백테스트 리포트 (필터 비교)")
lines.append(f"    기간: {fmt(report_days[0])} ~ {fmt(report_days[-1])}  (최근 1개월)")
lines.append("=" * 65)
lines.append("전략: D-1 상한가(+29.9~30%) → D-0 장다리도지+거래량1.5배 → D+1 매수")
lines.append("검증: D+1 시가 대비 장중 최고가 상승률")
lines.append("")

lines.append("┌─ 필터별 성공률 비교 " + "─" * 44)
lines.append(f"│ {'필터':<26} {'건수':>4}  {'3%율':>6}  {'5%율':>6}  {'평균상승':>8}")
lines.append("├" + "─" * 64)

best_filter = None
best_rate   = -1

for fname, ffunc in filters.items():
    sub = [q for q in qualifying if ffunc(q)]
    n = len(sub)
    if n == 0:
        lines.append(f"│ {fname:<26} {'0':>4}  {'—':>6}  {'—':>6}  {'—':>8}")
        continue
    n3   = sum(1 for q in sub if q["ok_3pct"])
    n5   = sum(1 for q in sub if q["ok_5pct"])
    avg  = sum(q["max_gain"] for q in sub) / n
    r3   = n3 / n * 100
    r5   = n5 / n * 100
    mark = " ★" if r3 > best_rate else ""
    if r3 > best_rate:
        best_rate   = r3
        best_filter = fname
    lines.append(f"│ {fname:<26} {n:>4}  {r3:>5.1f}%  {r5:>5.1f}%  {avg:>+7.1f}%{mark}")

lines.append("└" + "─" * 64)
lines.append(f"  ★ 최적 필터: {best_filter} ({best_rate:.1f}%)")
lines.append("")

# ── 전체 케이스 상세 ───────────────────────────────────────────
lines.append("=" * 65)
lines.append("📋  전체 케이스 상세")
lines.append("-" * 65)
lines.append(f"  {'종목':<16} {'날짜':^16} {'시총':>6}  {'거래':>5}  {'몸통':>5}  {'최고':>6}  {'3%':>3}  {'5%':>3}  {'의견':>4}")
lines.append("-" * 65)

for c in sorted(qualifying, key=lambda x: x["d0_date"]):
    name_str  = f"{c['name'][:6]}({c['ticker']})"
    dates_str = f"{fmt(c['d1_date'])}→{fmt(c['d0_date'])}→{fmt(c['dp1_date'])}"
    m3 = "✅" if c["ok_3pct"] else "❌"
    m5 = "✅" if c["ok_5pct"] else "❌"
    b  = "양" if c["bullish"] else "음"
    op = f"{c['op_score']:+d}"
    lines.append(
        f"  {name_str:<16} {dates_str:<16} {c['cap_str']:>6}  {c['d0_vol']:>4.1f}x  "
        f"{c['d0_body']:>5.3f}{b}  {c['max_gain']:>+6.1f}%  {m3}  {m5}  {op}"
    )

lines.append("-" * 65)
lines.append("")
lines.append(f"생성: {datetime.now().strftime('%Y-%m-%d %H:%M')}")
lines.append("=" * 65)

report = "\n".join(lines)
print("\n" + report)

out_path = r"D:\GIT\claude\stock_backtest_report.txt"
with open(out_path, "w", encoding="utf-8") as f:
    f.write(report)
print(f"\n📁 리포트 저장: {out_path}")
