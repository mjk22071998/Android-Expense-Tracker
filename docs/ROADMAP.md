# Expense Tracker — Project Roadmap & Progress Report

> **Current Version:** `v0.4` (Build 4)  
> **Status:** Milestone Complete — Phase: Supabase Cloud Sync & Backup  
> **Architecture:** Offline-First, Bidirectional Cloud Sync with Supabase (Room Local SOT + WorkManager)  
> **Quality Gate:** 102 Unit & Stress Tests Passing (100% success rate) | CI/CD Automated Build & Test Passing  
> **Hardware Target:** Android 7.0 (API 24) through Android 15 (API 35+); physically tested & verified on Android 9 (API 28).

---

## 1. Product Identity & Foundational Guardrails

1. **Offline-First Single Source of Truth**:
   - The local Room SQLite database is the sole, authoritative source of truth for all on-device application operations.
   - 100% offline guest mode operates with zero required network connectivity or account creation.
   - Cloud backup and multi-device synchronization via Supabase is completely optional and non-intrusive.
2. **Single-Ledger Simplicity**:
   - Single personal ledger (`Personal`) seeded at first launch.
   - Zero multi-ledger switching confusion or account synchronization overhead.
   - Running balances are maintained atomically at $O(1)$ read complexity.
3. **Organic Forest Luxury Aesthetic**:
   - Bespoke fintech UI inspired by modern wealth interfaces (canopy gradients, stitched leathercraft overview card, floating pill navigation).
   - High-contrast typography and WCAG 2.2 AA compliant touch targets.
   - Symmetrically centered theme mode selector (`System` | `Light` | `Dark`).
4. **Low-Memory & Legacy Device Optimization**:
   - Guaranteed smooth execution on legacy and entry-level hardware (e.g. Android 9 / 3GB RAM devices).
   - Strict pagination and memory-conscious sync buffers (< 12 MB heap overhead).
   - Zero background network drain when offline or idle.
5. **Security & Privacy Guardrails**:
   - Supabase Row-Level Security (RLS) enforcing `(select auth.uid()) = user_id` with subquery caching.
   - Hardware-backed token storage via Android `EncryptedSharedPreferences`.
   - Safe sign-out that retains user records locally without data loss.

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

### Milestone v0.3 — Visual Analytics & Cashflow Charts ✅
- **Native Hardware-Accelerated Charts**:
  - Built using Jetpack Compose `Canvas` with zero third-party dependencies (0 KB library bloat).
  - 3-Way Mode Toggle: Expenses (Crimson Rose gradient), Income (Emerald Green gradient), Balance (Electric Lime spline with radiant area glow).
  - Granular date grouping: Daily data points for Week and Month views, Monthly data points for Year view.
  - Smooth Bézier curve interpolation with intelligent zero-fill for inactive days and carryforward for running balance.
  - Interactive touch-scrubbing with haptic feedback tick, vertical tracking crosshair, and floating detail tooltip pill.

### Milestone v0.4 — Supabase Cloud Sync & Backup, Offline-First Architecture & Auth Workflows ✅
- **Offline-First Bidirectional Synchronization**:
  - Full local-first Room database single source of truth paired with Supabase remote backend.
  - Multi-table sync (`ledgers`, `categories`, `transactions`) with Last-Write-Wins (LWW) conflict resolution by UTC `updated_at`.
  - Non-destructive soft-delete cascading propagation (`isDeleted` flag).
  - Background synchronization via AndroidX `WorkManager` (15m connected periodic job + debounced one-time sync).
- **Runtime Authentication & Password Security**:
  - Live 5-point password requirements checklist (8+ length, lowercase, uppercase, number, symbol) matching server security policies.
  - Forgot password flow with 8-digit email OTP verification (`supabase.auth.resetPasswordForEmail` + `verifyEmailOtp`).
  - Account password change workflow in Settings requiring current password validation.
  - Guest mode with zero sign-in barrier and safe sign-out preserving local device transactions.
- **UI & UX Refinement**:
  - Cloud Backup card in Settings with ambient sync pill badge and circular status indicator (`ic_cloud_done`, `ic_cloud_upload`, `ic_sync`).
  - Symmetrically centered theme mode selector (`System` | `Light` | `Dark`).
  - Optimized database RLS policies with subquery caching (`(select auth.uid())`), duplicate permissive policy removal, and performance foreign key indexing.
- **Automated Verification**:
  - 102 unit & stress tests passing (100% success rate).

### Milestone v0.5 — Multi-Ledger Management & Cloud Sync (Online Multi-Ledgers) ✅
- **Multi-Ledger Data Architecture & Room Migration (v3 $\to$ v4)**:
  - Added `isDeleted` flag to local Room `Ledger` entity with backwards-compatible schema migration `MIGRATION_3_4`.
  - Stored `activeLedgerId` in Jetpack DataStore `UserPreferences`, defaulting to `ledger_default` if unspecified or invalid.
  - Implemented reactive `LedgerRepository` flows delivering active ledger state alongside all active ledgers list.
  - Enforced cascading soft deletion (`softDeleteLedger`) for non-default ledgers while guarding the default primary ledger against accidental removal.
- **TopBar Interactive Ledger Switcher & Selector Sheet**:
  - Transformed static title in `DashboardTopBar` into an interactive, luxury glass pill: `[Wallet Icon] <Active Ledger Name> [Chevron Down]`.
  - Luxury **Ledger Switcher Bottom Sheet** (`LedgerSwitcherBottomSheet`) displaying all user ledgers with:
    - Ledger title, currency code, and live running balance.
    - Active radio checkmark indicator and "Default" chip badge.
    - One-tap switching with smooth dashboard and chart state transitions.
    - Overflow options menu on custom ledgers: "Edit Ledger" and "Delete Ledger".
    - High-contrast `+ Create New Ledger` CTA button.
- **Ledger Management & Creation Dialog**:
  - `CreateEditLedgerDialog` supporting custom ledger name (with 30-char validation) and currency selector (popular currency chips + full ISO picker modal).
  - Edit ledger details (rename, change currency).
  - `DeleteLedgerConfirmDialog` with safety safeguards (warns that transactions remain archived, reverts active ledger to default, soft-deletes and flags for sync).
- **Bidirectional Multi-Ledger Cloud Synchronization**:
  - Full synchronization of all user ledgers via Supabase PostgREST `ledgers` table.
  - Handled remote ledger creation, updates, and soft deletions across devices.
  - Automatically re-derived balance, income, and expense totals upon transaction cloud delta pull (`recalculateDerivedState`).
- **Scoped Dashboard, Forms & Analytics Integration**:
  - Automatically scoped Dashboard balance, quick actions, search queries, and transaction history to `activeLedgerId`.
  - `AddEditTransactionViewModel` defaults newly logged transactions and currency symbols to the active ledger.
  - Transaction history, PDF statement exports, and dynamic subjects aggregate data exclusively for the active ledger.
- **Automated Verification**:
  - 108 unit & stress tests passing (100% success rate).

---

## 3. Current Version & Living Status

| Metric | Current State |
|---|---|
| **App Version** | `v0.5` |
| **Version Code** | `5` |
| **Active Ledgers** | Multi-Ledger Support (Default `Personal` + Custom Ledgers) |
| **Network Footprint** | Offline-First (Optional Supabase Cloud Sync & Backup) |
| **Automated Tests** | 108 unit & stress tests passing (`100%`) |
| **CI/CD Status** | Automated Build & Test on push/PR (`ubuntu-latest`) |
| **Minimum SDK** | API 24 (Android 7.0 Nougat) |
| **Target SDK** | API 37 (Android 15+) |

---

## 4. Forward Roadmap & Next Phases

### 📦 Milestone v0.6 — Local Data Portability & Data Management (PLANNED)
**Goal:** Provide offline-first backup and restore capabilities independent of cloud services, and allow safe data resets.
- [ ] **JSON Data Export**:
  - Export all transactions, categories, and ledger state into an encrypted or plain human-readable `.json` backup file.
  - Export metadata (schema version, export timestamp, total transaction count, checksum).
  - Dedicated save action to Phone Storage / SD Card or share via system share sheet.
- [ ] **JSON Data Restore / Import**:
  - File picker to select a previously exported `.json` backup.
  - Strict validation phase: schema verification, foreign key integrity, and checksum validation before touching the database.
  - Option to merge or overwrite existing records with atomic balance recalculation.
- [ ] **Database Reset / Clear Data Safeguard**:
  - Functional wire-up for the "Clear All Records" dialog in `SettingsScreen`.
  - Resets transactions while preserving default categories and re-initializing the default `Personal` ledger with 0 balance.
- [ ] **Category Icon Catalog Expansion**:
  - Expand available Material Symbols for custom categories (health, travel, utilities, pets, education, investments).

---

### 📊 Milestone v0.7 — Advanced Visual Analytics & Trends (PLANNED)
**Goal:** Deepen spending insight and financial distribution using native Compose Canvas without third-party chart libraries.
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

### 🎯 Milestone v0.8 — Category Budgets & Spending Limits (PLANNED)
**Goal:** Help users control spending through customizable monthly thresholds per category.
- [ ] **Monthly Budget Targets**:
  - Set optional monthly budget amounts per expense category (e.g., Dining: Rs 15,000/month).
- [ ] **Visual Budget Progress Indicators**:
  - Linear progress indicators on Category items displaying current spend vs. budget.
  - Three-tier status colors: Emerald (<80%), Amber (80–99%), Crimson (≥100% / Over budget).
- [ ] **Dashboard Budget Warning Banner**:
  - Subtle, non-intrusive alert pill on the dashboard when one or more categories reach or exceed their budget.

---

### ⏰ Milestone v0.9 — Scheduled Reminders & Recurring Transactions (PLANNED)
**Goal:** Automate logging of fixed recurring expenses and bills completely on-device.
- [ ] **Recurring Expense Templates**:
  - Create recurring templates (Daily, Weekly, Monthly, Yearly) for subscriptions, rent, utility bills.
- [ ] **Local Android Bill Reminders**:
  - Purely local Android `AlarmManager` / local notifications scheduled when a bill is due.
  - Completely offline; zero push notification services (no FCM, no background network).
- [ ] **One-Tap Recurring Logging**:
  - Tap "Log Now" directly from the reminder or in-app recurring queue to insert into the ledger with a single confirmation.

---

### 🏆 Milestone v1.0 — Production Release & Distribution Polish (PLANNED)
**Goal:** Prepare the application for release distribution on F-Droid, GitHub Releases, and Google Play Store.
- [ ] **ProGuard & R8 Optimization**:
  - Comprehensive shrinking, obfuscation, and optimization rules verified for Room, Hilt, Supabase, and Kotlinx Serialization.
- [ ] **Release Signing & Build Automation**:
  - Configure release keystore signing via environment variables.
  - Output optimized Android App Bundle (`.aab`) and standalone release APK (`.apk`).
- [ ] **Zero-Warning Lint & Documentation**:
  - Final static analysis check with zero errors/warnings.
  - Complete user manual, release notes, and changelog.

---

### 💬 Milestone v1.1 — Optional WhatsApp AI Companion (PLANNED)
**Goal:** Allow users to optionally link their phone number to an official WhatsApp Business bot, enabling friction-free expense logging and balance queries directly from WhatsApp without adding any in-app chat clutter or SDK bloat to the Android app.
- [ ] **WhatsApp Account Binding (In-App Settings)**:
  - Optional setting in `SettingsScreen`: *"Link WhatsApp Assistant"*.
  - User binds their phone number with a quick one-time verification.
  - Securely maps verified `phone_number` ↔ `user_id` in Supabase.
- [ ] **Server-Side AI Assistant (Zero App Bloat)**:
  - **No In-App Chat UI**: The Android app remains 100% clean, offline-first, and lightweight—zero LLM libraries, zero chat screens, and zero RAM overhead on 3GB devices.
  - **Multi-Item & Batch Expense Logging**:
    - Handles single or multi-item messages in one go (e.g. *"I bought milk for Rs 250, I bought eggs for Rs 2,000"*).
    - LLM extracts structured parameter arrays:
      - Item 1: `amount: 250.0`, `note: "Milk"`, `category: "Groceries"`
      - Item 2: `amount: 2000.0`, `note: "Eggs"`, `category: "Groceries"`
    - Inserts all items into the user's cloud ledger and replies on WhatsApp:
      > *"✅ Logged 2 expenses:
      > • Milk: Rs 250 (Groceries)
      > • Eggs: Rs 2,000 (Groceries)
      > Total: Rs 2,250 | Current Balance: Rs 42,980"*
  - **Conversational Queries with Slot-Filling (Missing Parameters Clarification)**:
    - **Live Balance Checks**: User asks *"What's my current balance?"* → Agent queries ledger and replies with current running balance, total income, and total expenses.
    - **Adaptive Clarification for Underspecified Queries**: If a user asks *"How much did I spend on travelling?"* without specifying a timeframe, the agent detects the missing parameter and clarifies:
      > *"Are you looking for this month, last month, or all time?"*
      Upon user's reply (*"Last month"*), it filters `category = 'travel'` across the exact date boundary and replies with the aggregated total.
- [ ] **Automatic Background Sync to Device**:
  - Transactions recorded via WhatsApp automatically flow down to the user's local Room database during the next background sync or when the app is opened.
