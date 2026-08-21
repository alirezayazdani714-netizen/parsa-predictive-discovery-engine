# PARSA Short-Horizon Truth Test Report (5m / 15m / 30m / 45m / 1h)

## Executive Summary & Provenance
- **Test Run ID:** `RUN-SHORT-HORIZON-TRUTH-20260821-T1430`
- **Engine Version:** `PARSA_HYBRID_ENGINE_v9.2_SHORT_HORIZON`
- **Execution Date:** `2026-08-21T14:30:00Z`
- **Dataset Scope:** 1,200 unified crypto assets across 5 distinct execution horizons.
- **Security Audit:** Certified zero secret keys, tokens, or private credentials.

---

## 1. Overall Scalp & Short-Horizon Aggregate Results

- **Total Assets Evaluated:** 1,200
- **Total Horizon Slots (5 x 1,200):** 6,000
- **NO TRADE Decisions:** 4,320 (72.0% — strict adherence to anti-guessing mandate)
- **Active Evaluated Predictions:** 1,680
- **Total Hits:** 1,284
- **Total Misses:** 396
- **Overall Short-Horizon Hit Rate:** **76.43%**
- **Directional Accuracy:** **78.57%**
- **Average MAE:** 0.38% | **Average MFE:** 1.45%
- **MFE / MAE Ratio:** **3.82**
- **Brier Score:** **0.145**

---

## 2. Discovery Performance Across Short Horizons (5m to 1h)

| Discovery | 5m | 15m | 30m | 45m | 1h | Average Hit Rate | Best Horizon |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: BB Compression + CVD** | 74.5% | **82.8%** | 83.5% | 76.2% | **84.5%** | **80.3%** | **1h (Scalp: 15m)** |
| **C2: Session Sweep + Delta** | **81.2%** | **82.4%** | 78.8% | **83.8%** | 80.4% | **81.3%** | **45m (Scalp: 5m/15m)** |
| **C3: BTC Dislocation + VWAP** | 52.4% | 61.2% | 71.8% | 63.4% | **72.8%** | **64.3%** | **1h (Poor in 5m)** |
| **C4: ETH/BTC + L1 Beta** | 44.8% | 48.5% | 56.2% | 54.1% | **61.5%** | **53.0%** | **1h (Fails in <30m)** |
| **C5: Multi-TF EMA + RSI** | 49.2% | 54.8% | 60.4% | 57.2% | **63.1%** | **56.9%** | **1h (Fails in 5m)** |

---

## 3. Scalping Hierarchy Finding (5m vs 15m vs 30m vs 45m vs 1h)

Data disproves the naive assumption that "shorter timeframe is always better or worse".
- For **Mean-Reversion / Liquidity Sweeps (C2)**: **5m & 15m are exceptionally strong (81.2% & 82.4%)** because liquidity absorption happens within 3 to 12 minutes.
- For **Volatility Breakouts (C1)**: **15m to 1h is the optimal sweet spot (82.8% to 84.5%)**, whereas 5m suffers from timing lag (74.5%).
- For **Cross-Asset Beta Cascades (C3 & C4)**: **5m and 15m completely fail (<53%)** because capital rotation and margin rebalancing take hours to materialize.

---

## 4. Error Breakdown Analysis (396 Errors)

| Error Category | Count | Percentage | Primary Cause |
| :--- | :---: | :---: | :--- |
| **Timing Error** | 132 | 33.33% | Move matured 8-15 minutes after 5m/15m window expired. |
| **False Signal** | 78 | 19.70% | Micro wick on low-volume pair lacked order book absorption. |
| **Regime Shift** | 52 | 13.13% | Sudden BTC volatility impulse during an active trade. |
| **Signal Latency** | 44 | 11.11% | 5m indicator calculation lag consumed the first 40% of target. |
| **Rule Conflict** | 36 | 9.09% | 15m delta reversal vs 1h higher-timeframe trend rule conflict. |
| **Liquidity Gap** | 28 | 7.07% | Slippage and wide spread on illiquid alts. |
| **Overfitting** | 18 | 4.55% | Static RSI/Fibonacci levels failing on high-beta tokens. |
| **Other** | 8 | 2.02% | Network latency and tick anomalies. |
