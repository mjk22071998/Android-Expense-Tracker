# Expense Tracker — Project Roadmap & Progress Report

> **Current Version:** `v0.3` (Build 3)  
> **Status:** Active Development — Phase: Local Data Portability & Backup  
> **Architecture:** 100% Standalone, Single-Ledger Offline Expense Tracker for Android (API 24+)  
> **Quality Gate:** 59 Unit & Stress Tests Passing (100% success rate) | CI/CD Automated Build & Test Passing  
> **Hardware Target:** Android 7.0 (API 24) through Android 15 (API 35+); physically tested & verified on Android 9 (API 28).

---

## 1. Product Identity & Foundational Guardrails

1. **100% Standalone & Offline-First**:
   - Zero internet permissions in `AndroidManifest.xml` for core functionality.
   - Zero external cloud services, remote sync engines, or analytics tracking required for day-to-day use.
   - All financial data, transactions, and categories reside exclusively on the device in a local Room SQLite database.
2. **Single-Ledger Simplicity**:
   - Single personal ledger (`Personal`) seeded at first launch.
   - Zero multi-ledger switching confusion or account synchronization overhead.
   - Running balances are maintained atomically at $O(1)$ read complexity.
3. **Organic Forest Luxury Aesthetic**:
   - Bespoke fintech UI inspired by modern wealth interfaces (canopy gradients, stitched leathercraft overview card, floating pill navigation).
   - High-contrast typography and WCAG 2.2 AA compliant touch targets.
   - Seamless dynamic theming (Dark, Light, and System follow modes).
4. **Low-Memory & Legacy Device Optimization**:
   - Guaranteed smooth execution on legacy and entry-level hardware (e.g. Android 9 / 3GB RAM devices).
   - Zero background network drain and minimal battery footprint.

---

## 2. Progress Report: What Has Already Been Done

### Milestone v0.1 — Core Persistence & Architecture Foundation ✅
- **Room SQLite Local Database**:
  - Full relational schema: `transactions`, `ledgers`, `categories`, and `monthly_snapshots`.
  - ACID-compliant database operations with foreign key cascading and indices.
  - Automatic database seeding on first launch (default categories and default `Personal` ledger).
- **Domain & Clean Architecture**:
  - MVVM pattern with Hilt `@Singleton` dependency injection.
  - Reactive repository layer delivering live Kotlin Coroutine `Flow` streams.
  - $O(1)$ running balance calculation updated atomically upon transaction creation, modification, and soft deletion.
- **Base UI & Navigation**:
  - Declarative Jetpack Compose navigation graph.
  - Basic transaction logging (Income / Expense) with category association and notes.

### Milestone v0.2 — Feature Complete Offline MVP & Luxury UI ✅
- **Organic Forest Luxury Design System**:
  - Radial gradient canopy hero section (`#2E633B` $\to$ `#122E1B` $\to$ `#06110A`).
  - Hero Balance display with formatted currency symbols.
  - Floating pill bottom navigation bar with dual-state glass icons.
  - Total Financial Overview sheet featuring stitched leathercraft card aesthetics.
  - Directional cashflow visual indicators (Emerald Green `+` for Inflow, Crimson Rose `−` for Outflow).
  - Receipt-style transaction detail inspection dialog.
- **Strategy-Pattern Filtering & Live Search**:
  - Extensible `DateRangeFilter` strategy: `AllTime`, `Today`, `ThisWeek`, `ThisMonth`, `ThisYear`, and `Custom Date Range`.
  - Debounced real-time search across notes and category titles.
  - Multi-category filter bottom sheet with active filter badge indicators.
- **Custom Category Management**:
  - Full user category CRUD (add custom categories, custom icon selection, color palette choices).
  - Safeguards preventing accidental deletion of categories assigned to active transactions.
- **ISO A4 PDF Statement Export**:
  - Native on-device PDF generation using Android's hardware `PdfDocument` API.
  - Executive financial summary (Inflow, Outflow, Net Balance) plus itemized ledger table.
  - Dual export pathways: direct save to Phone Storage / SD Card (`Download/ExpenseTracker/`) and system Share sheet via secure `FileProvider`.
  - Dual-tier storage permission handling (API 24–28 `WRITE_EXTERNAL_STORAGE` vs. API 29+ Scoped Storage).
- **User Preferences & Theming**:
  - Jetpack DataStore Preferences for persistent user settings.
  - Searchable currency picker supporting 150+ currencies with natural colloquial symbols (`Rs`, `$`, `€`, `₹`, `£`).
  - Complete Light, Dark, and System theme support with dynamic edge-to-edge system bar tinting.
- **Accessibility & UX Polish**:
  - Accessible Material 3 tooltips (`AppTooltip`) on all dashboard, form, and navigation icon buttons.
  - AndroidX `SplashScreen` API with seamless handoff to first-run database initialization.

### Hardening, Stress Testing & Standalone Verification ✅
- **Stability & Concurrency Fixes**:
  - Guaranteed `MonthlySnapshot` row preservation and generation on historical transaction date edits.
  - Fixed system theme synchronization using `isSystemInDarkTheme()`.
  - DataStore `IOException` graceful error handling and recovery.
  - High-contrast date picker field styling in light mode.
- **Input Sanitization**:
  - Mathematical hardening: blocks `NaN`, `Infinity`, and invalid negative sign stripping in amount fields.
  - Currency helper resilience with automatic fallback on blank codes.
- **Comprehensive 59-Test Suite**:
  - Added full stress test suites: `ConcurrencyAndLoadStressTest`, `DateBoundaryStressTest`, `PaginationAndReportStressTest`, `RepositoryAndDomainStressTest`, and `ViewModelStressTest`.
  - 59 out of 59 tests passing in ~6 seconds with 100% success rate.
- **CI/CD Streamlining**:
  - Hardened GitHub Actions workflow (`android-ci.yml`) to run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`.
  - Fixed Gradle wrapper download timeouts (`networkTimeout=120000`, `retries=5`).
- **Physical Device Validation**:
  - Deployed and tested on real physical device: OPPO A12 (`CPH2083`, Android 9 / API 28).
  - Executed 500-event automated Android Monkey stress test with 0 dropped events and 0 crashes (`VM exit code 0`).
- **Architecture Lockdown**:
  - Scrubbed legacy cloud sync scaffolding and interfaces (`SyncableEntity`, `SyncContracts`).
  - Confirmed 0 network permissions in manifest.

---

## 3. Current Version & Living Status

| Metric | Current State |
|---|---|
| **App Version** | `v0.3` |
| **Version Code** | `3` |
| **Default Ledger** | Single Ledger (`Personal`, ID: `ledger_default`) |
| **Network Footprint** | 0 bytes (No network permissions, 100% offline) |
| **Automated Tests** | 59 unit & stress tests passing (`100%`) |
| **CI/CD Status** | Automated Build & Test on push/PR (`ubuntu-latest`) |
| **Minimum SDK** | API 24 (Android 7.0 Nougat) |
| **Target SDK** | API 37 (Android 15+) |

---

## 4. Forward Roadmap & Execution Steps

### 🚀 Milestone v0.3 — Local Data Portability & Backup (IN PROGRESS)
**Goal:** Empower users with full ownership and portability of their local financial data without relying on any external cloud server.
- [ ] **JSON Data Export**:
  - Export all transactions, categories, and ledger state into an encrypted or plain human-readable `.json` backup file.
  - Export metadata (schema version, export timestamp, total transaction count, checksum).
  - Dedicated save action to Phone Storage / SD Card or share via system share sheet.
- [ ] **JSON Data Restore / Import**:
  - File picker to select a previously exported `.json` backup.
  - Strict validation phase: schema verification, foreign key integrity, and checksum validation before touching the database.
  - Option to merge or overwrite existing records.
  - Post-restore atomic balance and monthly snapshot recalculation.
- [ ] **Database Reset / Clear Data**:
  - Functional wire-up for the "Clear All Records" dialog in `SettingsScreen`.
  - Resets transactions while preserving default categories and re-initializing the default `Personal` ledger with 0 balance.
- [ ] **Category Icon Catalog Expansion**:
  - Expand available Material Symbols for custom categories (health, travel, utilities, pets, education, investments).

---

### ☁️ Milestone v0.4 — Cloud Sync & Authentication (Supabase) (PLANNED)
**Goal:** Seamless, opt-in cloud synchronization and backup powered by Supabase while preserving 100% offline-first operation.
- [ ] **Supabase GoTrue Authentication**:
  - Secure Email/Password and Google OAuth sign-in.
  - Hardware-backed token storage in Android `EncryptedSharedPreferences`.
  - Zero-data-loss merge: automatically associate existing offline records to the user's new account on first login.
- [ ] **Lightweight WorkManager Sync Engine**:
  - Background REST sync queue tracking dirty rows (`SYNCED`, `PENDING_UPLOAD`, `PENDING_DELETE`).
  - Strict sync ordering to prevent foreign key errors: Ledgers → Categories → Transactions.
  - 100-item chunked pagination to protect memory (< 12 MB heap overhead on 3GB RAM devices).
  - Server timestamp authority (`server_updated_at`) with UUID idempotency (`ON CONFLICT (id) DO UPDATE`).
- [ ] **WhatsApp-Style Sync UX**:
  - Non-blocking in-app sync indicator; transactions hydrate into Room reactively.
  - Silent day-to-day syncs (≤ 25 items); native progress notification strictly for large initial restores (> 100 items).

---

### 📊 Milestone v0.5 — Visual Analytics & Trends (PLANNED)
**Goal:** Provide visual financial intelligence and spending distributions using native Compose Canvas without bulky third-party chart libraries.
- [ ] **Category Donut / Pie Breakdown**:
  - Interactive, hardware-accelerated Compose Canvas category donut chart.
  - Center label displaying total period outflow.
  - Color-coded legend with percentage breakdown for top categories.
- [ ] **Income vs. Expense Monthly Bar Chart**:
  - Side-by-side or stacked monthly comparison bars for historical cashflow analysis.
  - Responsive layout adapting to portrait and landscape orientations.
- [ ] **Spending Insights & Highlights**:
  - Highlight top spending category of the month.
  - Average daily spend calculation for the active period filter.

---

### 🎯 Milestone v0.6 — Category Budgets & Spending Limits (PLANNED)
**Goal:** Help users control spending through customizable monthly thresholds per category.
- [ ] **Monthly Budget Targets**:
  - Set optional monthly budget amounts per expense category (e.g., Dining: $250/month).
- [ ] **Visual Budget Progress Indicators**:
  - Linear progress indicators on Category items displaying current spend vs. budget.
  - Three-tier status colors: Emerald (<80%), Amber (80–99%), Crimson (≥100% / Over budget).
- [ ] **Dashboard Budget Warning Banner**:
  - Subtle, non-intrusive alert pill on the dashboard when one or more categories reach or exceed their budget.

---

### ⏰ Milestone v0.7 — Scheduled Reminders & Recurring Transactions (PLANNED)
**Goal:** Automate logging of fixed recurring expenses and bills completely on-device.
- [ ] **Recurring Expense Templates**:
  - Create recurring templates (Daily, Weekly, Monthly, Yearly) for subscriptions, rent, loan payments.
- [ ] **Local Android Bill Reminders**:
  - Purely local Android `AlarmManager` / local notifications scheduled when a bill is due.
  - Completely offline; zero push notification services (no FCM, no background network).
- [ ] **One-Tap Recurring Logging**:
  - Tap "Log Now" directly from the reminder or in-app recurring queue to insert into the ledger with a single confirmation.

---

### 🏆 Milestone v1.0 — Production Release & Distribution Polish (PLANNED)
**Goal:** Prepare the application for release distribution on F-Droid, GitHub Releases, and Google Play Store.
- [ ] **ProGuard & R8 Optimization**:
  - Comprehensive shrinking, obfuscation, and optimization rules verified for Room, Hilt, and Kotlinx Serialization.
- [ ] **Release Signing & Build Automation**:
  - Configure release keystore signing via environment variables.
  - Output optimized Android App Bundle (`.aab`) and standalone release APK (`.apk`).
- [ ] **Zero-Warning Lint & Documentation**:
  - Final static analysis check with zero errors/warnings.
  - Complete user manual, release notes, and changelog.

---

### 🧠 Milestone v1.1 — AI Intelligence & External APIs ("Talk to Your Data") (PLANNED)
**Goal:** Enable users to converse directly with their financial records using natural language and leverage external APIs like WhatsApp for automated entry.
- [ ] **"Talk to Your Financial Data" (Conversational AI Assistant)**:
  - In-app chat interface allowing users to query their spending in plain English:
    - *"How much did I spend on dining out this month?"*
    - *"What was my single biggest expense in October?"*
    - *"Compare my fuel spending between September and November."*
    - *"Show me all expenses over $100 without notes."*
  - **Function Calling & Safe Read-Only Queries**: The AI model uses structured tool calling to execute safe, parameterized read-only queries against local SQLite data.
  - **Financial Health Summaries**: Natural language narrative summaries of monthly trends, recurring leaks, and anomaly spikes.
- [ ] **External APIs & Messaging Integrations**:
  - **Conversational Messaging Logging (WhatsApp / Telegram Integration)**:
    - User sends a natural language message on WhatsApp (e.g. *"I bought milk for Rs 250"*, *"Paid Rs 1,500 electricity bill"*, or *"Received Rs 80,000 salary"*).
    - **LLM Entity & Action Extraction**: The model parses unstructured text via function calling into structured parameters (`amount: 250.0`, `type: EXPENSE`, `note: "Milk"`, `category: "Groceries"`).
    - **Cloud Webhook Pathway**: WhatsApp Cloud API webhook triggers a Supabase Edge Function running Gemini/LLM, inserts the transaction directly into the user's cloud ledger, syncs to the Android device, and sends a WhatsApp confirmation reply: *"✅ Logged Rs 250 for Milk under Groceries. Current balance: Rs 14,200"*.
    - **On-Device Android Notification Alternative**: Optional Android `NotificationListenerService` to detect financial SMS or messaging notifications and offer one-tap expense logging without third-party webhooks.
  - **Live Currency Exchange Rates API**: Fetch and cache daily global FX rates for foreign transactions and multi-currency conversion.
  - **Smart Receipt Scanner & OCR API**: Snap a photo of a receipt or invoice; extract merchant, total amount, date, and suggested category with one-tap confirmation.
  - **Bank Notification / Statement Parser API**: Smart parsing of bank transaction SMS or CSV statements.
- [ ] **Privacy & Low-Spec Performance Architecture**:
  - **Zero Raw Database Exposure**: Database records stay strictly local; only schema tool definitions and specific aggregated metrics are exchanged with the model.
  - **Streaming Responses**: Token-by-token streaming in Compose with graceful typing animations for low perceived latency on 3GB RAM devices.
  - **Offline Resilience**: Clear, graceful fallback explaining that AI conversational queries require an active connection, while core offline ledger features remain 100% accessible.
