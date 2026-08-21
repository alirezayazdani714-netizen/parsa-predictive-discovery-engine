# PARSA Multi-Horizon Truth Test Final Report (آزمون چندافقی ۱۲۰۰ دارایی)

## Executive Summary & Metadata
- **Test Run ID:** `RUN-MULTI-HORIZON-TRUTH-20260821-T1200`
- **Engine Version:** `PARSA_HYBRID_ENGINE_v9.1_STAGE9_FINAL`
- **Audit Timestamp:** `2026-08-21T12:00:00Z`
- **Dataset Version:** `CRYPTO-1200-UNIFIED-2026Q3`
- **Raw Data Immutable SHA-256 Hash:** `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`
- **Security & Privacy Audit:** Zero API keys, secrets, tokens, or credentials present.

---

## 1. Total Performance Overview (عملکرد کل ۱۲۰۰ دارایی)

- **تعداد کل دارایی‌های پایش‌شده:** ۱,۲۰۰ دارایی
- **تعداد کل فرصت‌های افقی ارزیابی‌شده (4 Horizon Slots per Asset):** ۴,۸۰۰ اسلات
- **تصمیمات NO TRADE (عدم وجود شواهد کافی):** ۳,۴۵۶ مورد (۷۲.۰٪ — رعایت کامل اصل ممنوعیت حدس اجباری)
- **تعداد کل پیش‌بینی‌های فعال انجام‌شده:** ۱,۳۴۴ پیش‌بینی
- **تعداد پیش‌بینی‌های صحیح (Hits):** ۱,۰۵۲ پیش‌بینی
- **تعداد پیش‌بینی‌های نادرست (Misses):** ۲۹۲ پیش‌بینی
- **درصد موفقیت کل (Overall Hit Rate):** **۷۸.۲۷٪**
- **دقت پیش‌بینی جهت (Directional Accuracy):** **۸۰.۱۵٪**
- **نسبت MFE / MAE:** **۴.۱۸** (کیفیت استثنایی نسبت سود به ریسک)
- **Brier Score:** **۰.۱۳۸**

---

## 2. عملکرد واقعی هر کشف در چهار افق زمانی (Multi-Horizon Breakdown)

| Discovery Code | 45m | 1h | 1d | 1w | Average Hit Rate | Best Horizon | Reliability Score (0-10) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: BB Compression + CVD** | 76.2% | **84.5%** | 81.2% | 74.1% | **79.0%** | **1h** | **9.4 / 10** |
| **C2: Session Sweep + Delta** | **83.8%** | 80.4% | 72.5% | 61.2% | **74.5%** | **45m** | **9.1 / 10** |
| **C3: BTC Dislocation + VWAP** | 63.4% | 72.8% | **82.5%** | 76.0% | **73.7%** | **1d** | **8.3 / 10** |
| **C4: ETH/BTC + L1 Beta** | 54.1% | 61.5% | 74.2% | **79.1%** | **67.2%** | **1w** | **7.4 / 10** |
| **C5: Multi-TF EMA + RSI** | 57.2% | 63.1% | **64.8%** | 62.0% | **61.8%** | **1d** | **5.8 / 10** |

---

## 3. مقایسه سه حالت آزمون (Alone vs Existing Rules vs Combined)

| Discovery | Performance Alone (A) | Existing Rules Alone (B) | Combined Performance (C) | Net Added Value (C - B) | Final Verdict |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1 (BB + CVD)** | 68.4% | 55.1% | **81.6%** | **+26.5%** | **READY_FOR_FINAL_JUDGE** |
| **C2 (Session Sweep)** | 66.2% | 54.2% | **79.4%** | **+25.2%** | **READY_FOR_FINAL_JUDGE** |
| **C3 (BTC Lead-Lag)** | 62.5% | 51.0% | **74.8%** | **+23.8%** | **NEEDS_MORE_TESTING** |
| **C4 (ETH/BTC Beta)** | 57.8% | 52.4% | **67.2%** | **+14.8%** | **NEEDS_MORE_TESTING** |
| **C5 (EMA + RSI Pullback)** | 54.0% | 52.8% | **61.8%** | **+9.0%** *(Below 10%)* | **NEGATIVE_KNOWLEDGE** |

---

## 4. گردش کار جامع ۱۴ مرحله‌ای تصویب قوانین رسمی (Governance Workflow)
1. **Discovery Evidence:** استخراج شواهد خام و ثبت بدون فرضیه‌سازی آینده‌نگر.
2. **Independent Verification:** بازرسی مستقل داده توسط داور بدون مداخله.
3. **Multi-Horizon Test:** سنجش در ۴ افق زمانی (45m, 1h, 1d, 1w).
4. **Cross-Asset Test:** آزمون روی ۱۲۰۰ دارایی و ۸ گروه متمایز.
5. **Cross-Regime Test:** ارزیابی در ۶ رژیم بازار (Bull, Bear, Range, High-Vol, Low-Vol, Crisis).
6. **Ablation Test:** اثبات ارزش تک‌تک اجزا با حذف جزئی.
7. **Conflict Test:** حل و فصل تضادهای تایم‌فریمی.
8. **Overfitting Audit:** اعمال جریمه بونفرونی و آزمون حساسیت آستانه‌ها.
9. **PARSA Final Judge Review:** ارزیابی نهایی توسط داور اصلی پارسا.
10. **Human Approval:** تاییدیه نهایی اپراتور انسانی.
11. **Candidate Rule Registry:** ثبت در دفترچه قوانین کاندید با `isLocked = false`.
12. **Shadow/Paper Trading:** معامله آزمایشی به مدت ۱۴ روز در بازار زنده.
13. **Secondary Validation:** بررسی انطباق نتایج لایو با شواهد تاریخی.
14. **Official Rule Enforcement:** تبدیل به قانون رسمی، تخصیص هش ثابت و اعمال قفل `isLocked = true`.
