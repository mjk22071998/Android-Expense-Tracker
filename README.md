# Expense Tracker

A production-grade, offline-first personal finance Android application built with modern Android development practices.

---

## Features

### Current (v0.1) — Complete
- **Offline First** — fully functional without internet, always
- **Transaction Management** — add, edit, and soft-delete income and expense transactions, with atomic balance recalculation on every operation
- **Smart Balance Tracking** — real-time running balance with O(1) reads, maintained on the ledger itself rather than recalculated by scanning transactions
- **Category System** — transactions organized by categories with Material Symbol icons, seeded automatically on first launch with fixed, idempotent IDs
- **Default Ledger** — created automatically on first launch with a SIM-detected default currency
- **Date Range Filtering** — filter by Last 30 Days, This Week (Monday start), This Month, This Year, or Custom range
- **Currency Support** — default currency detected from the device's SIM country (falling back to locale), full searchable currency picker, correct native symbol rendering rather than raw currency codes
- **Theme Support** — System, Light, or Dark, persisted and applied live without restarting the app
- **Branded App Startup** — native Android SplashScreen API covers process/window creation, handing off seamlessly to a Compose loading state gated on first-launch database seeding, so the Dashboard never renders with empty or partial data
- **Transaction Detail View** — tap any transaction for a full detail dialog
- **Consistent Brand Design** — gradient hero cards for balance and income/expense summaries, custom Material Symbol icon set
- **Structured Error Handling** — a sealed `UiError` type and an `AppResult<T>` wrapper around every repository write, so failures surface as a readable message instead of a silent crash or a stuck loading state

### In Progress (v0.2)
- **Custom Categories** — users can create their own categories inline from the transaction form, choosing a name and an icon from a curated Material Symbol grid. Seeded default categories remain protected from editing or deletion. Code complete; a full manual end-to-end pass since the error-handling refactor is still outstanding (see Testing Strategy)
- **Expanded Filters & Search** — further date-range filtering and in-list search — not yet started

---

## Design Language

The app uses a consistent purple/blue brand identity with gradient hero cards (Balance Card, Income/Expense summaries) rather than flat Material 3 defaults, aiming for a distinctive feel without abandoning platform conventions.

A deliberate decision was made **not** to shift the whole screen's color theme based on transaction type (e.g., turning the Add/Edit Transaction screen red for expenses, green for income). Red and green carry strong, universal meaning — danger/error and success/safety — and applying them as full-screen themes for routine actions like logging an expense creates a mismatch between the emotion a color evokes and the user's actual intent. Instead, red/green are used only as small, contextual accents: transaction list item borders, amount text, and category icon tinting. The rest of the app, including transaction entry, stays on-brand.

---

## Architecture

This project follows **Clean Architecture** with **MVVM** pattern, ensuring separation of concerns, testability, and scalability.

```
app/
├── data/
│   ├── local/                  # Room database, DAOs, entities, seeders
│   │   ├── dao/                # Data Access Objects
│   │   ├── database/           # AppDatabase, migrations
│   │   └── entity/             # Room entities + projections
│   └── repository/             # Repository interfaces + implementations
│
├── domain/
│   └── model/                  # Domain models, sealed classes, UiError/AppResult, helpers
│
├── presentation/
│   ├── common/                  # BaseViewModel and other shared ViewModel infrastructure
│   ├── dashboard/                # Dashboard screen, ViewModel, UiState
│   ├── transaction/              # Add/Edit transaction screens
│   ├── settings/                 # Settings screen, ViewModel, UiState
│   └── navigation/               # NavGraph, Screen sealed class
│
└── di/                         # Hilt dependency injection modules
```

### Architecture Principles

- **Offline First** — Room is always the single source of truth. UI never reads from remote directly
- **Unidirectional Data Flow** — Repository → ViewModel → UiState → UI
- **Reactive UI** — Room `Flow` + `StateFlow` means UI updates automatically on data changes
- **Plug & Play** — Repository interfaces allow swapping the local-only implementation for a synced one later, without touching UI or ViewModel code
- **Atomic Operations** — transaction inserts, updates, and deletes are wrapped in Room `@Transaction` ensuring balance and monthly snapshots are never out of sync
- **Structured Failure Handling** — repository functions return `AppResult<T>` rather than throwing; every ViewModel extends `BaseViewModel` and uses `launchSafely` as a last-line-of-defense `CoroutineExceptionHandler`, so an unexpected failure degrades to a visible error state instead of crashing the app

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Icons | Material Symbols, sourced as individual vector drawables |
| Architecture | Clean Architecture + MVVM |
| Navigation | Navigation Compose |
| Database | Room (SQLite) |
| Dependency Injection | Hilt |
| Async | Kotlin Coroutines + Flow |
| Local Preferences | DataStore |
| App Startup | AndroidX SplashScreen API + Compose loading overlay |
| Testing | JUnit4, MockK, Turbine, Room in-memory database testing |
| Build System | Gradle with Version Catalogs (TOML) |
| Code Gen | KSP (Kotlin Symbol Processing) |

---

## Database Schema

### Entities

**`transactions`**
```
id              TEXT    PRIMARY KEY     (UUID, device-generated)
ledgerId        TEXT    FOREIGN KEY     → ledgers(id) CASCADE
categoryId      TEXT    FOREIGN KEY     → categories(id) SET NULL
amount          REAL
type            TEXT                    INCOME | EXPENSE
note            TEXT
date            INTEGER                 epoch milliseconds
createdAt       INTEGER
updatedAt       INTEGER
isDeleted       INTEGER                 soft delete flag
```

**`ledgers`**
```
id              TEXT    PRIMARY KEY     (UUID)
name            TEXT
currencyCode    TEXT
balance         REAL                    running balance — O(1) read
totalIncome     REAL                    running total income
totalExpenses   REAL                    running total expense
createdAt       INTEGER
updatedAt       INTEGER
isDefault       INTEGER
```

**`categories`**
```
id              TEXT    PRIMARY KEY     (fixed on seed, e.g. "cat_food"; UUID for user-created)
name            TEXT
icon            TEXT                    Material Symbol identifier
transactionType TEXT                    INCOME | EXPENSE | BOTH
isDefault       INTEGER                 true for seeded categories, false for user-created (v0.2)
```

**`monthly_snapshots`**
```
id              TEXT    PRIMARY KEY     (UUID)
ledgerId        TEXT    FOREIGN KEY     → ledgers(id) CASCADE
month           INTEGER
year            INTEGER
openingBalance  REAL
closingBalance  REAL
totalIncome     REAL
totalExpense    REAL
createdAt       INTEGER
updatedAt       INTEGER

UNIQUE INDEX    (ledgerId, month, year)
```
Maintained atomically alongside every transaction write, but not currently surfaced in the UI — reserved for a future reporting/analytics view.

### Key Design Decisions

- **UUID primary keys** — device-generated, collision-free across devices. Seeded default categories and the default ledger use fixed, human-readable IDs instead, so repeated seeding on every app launch is naturally idempotent via `OnConflictStrategy.IGNORE`
- **Soft delete** — `isDeleted` flag instead of physical delete
- **Running balance** — maintained on `Ledger` for O(1) reads, never calculated by scanning all rows
- **Composite indexes** — `(ledgerId, type, isDeleted, date)` for fast period queries
- **Atomic transactions** — Room `@Transaction` guarantees balance and snapshot never drift from actual data, on insert, edit, and soft delete alike
- **`isDefault` on categories** — designed in from v0.1 specifically to support user-created categories later without a schema change: the deletion query already scopes to `WHERE isDefault = 0`, and the category foreign key already uses `SET_NULL` rather than `RESTRICT`, so v0.2's custom categories slotted in as pure UI/ViewModel work with zero migration

---

## Engineering Notes — v0.1 Scope Decisions

A few decisions changed during v0.1 based on real-world testing rather than the original plan:

- Monthly opening/closing balance is computed and stored atomically with every transaction, but its UI surface was removed from the Dashboard to keep the hero balance card compact and legible on smaller screens. The underlying data is untouched and available for a future reporting view.
- Currency detection switched from locale-only to SIM-country-based detection (with locale as fallback). Locale-only detection reliably misidentified the user's currency and symbol on devices with an English display language but a different actual region.
- `material-icons-extended` was dropped in favor of individually sourced Material Symbols vector drawables, avoiding a large, since-deprecated dependency and noticeably reducing APK size.
- App startup uses two layered mechanisms rather than one: the native AndroidX SplashScreen API covers the OS-level window creation gap before Compose exists, and a Compose overlay covers the first-launch database seeding window, so the user never sees an empty or partially-loaded Dashboard on cold start.
- Default categories and the default ledger are seeded on every app launch using fixed IDs and an ignore-on-conflict strategy, rather than a one-time migration, keeping first-run setup simple and safe to re-run.

---

## Current Status

| Version | Focus | Status |
|---|---|---|
| v0.1 | Core offline functionality | Complete |
| v0.2 | Custom categories, error handling, tests, filters & search | In Progress |

This repository intentionally does not publish its longer-term roadmap during Hacktoberfest — see **Contributing** below for why, and what kinds of PRs are actually in scope right now.

---

## Project Setup

### Prerequisites
- Android Studio Narwhal or later
- JDK 11+
- Android SDK 24+

### Clone & Run
```bash
git clone https://github.com/mjk22071998/expense-tracker.git
cd expense-tracker
```

Open in Android Studio, let Gradle sync, then run on emulator or device.

### Requirements
- **Min SDK** — API 24 (Android 7.0)
- **Target SDK** — API 36
- **Compile SDK** — API 36

---

## Key Dependencies

```toml
# Core
kotlin = "2.4.10"
agp = "9.4.0"
composeBom = "2026.08.00"

# Database
room = "2.8.5"

# DI
hilt = "2.60.1"

# Async
kotlinx-coroutines = "1.11.0"

# Navigation
navigation-compose = "2.10.1"

# Preferences
datastore = "1.2.1"

# Testing
kotlinx-coroutines-test = "1.11.0"
turbine = "1.2.1"
mockk = "1.14.11"
```

---

## Testing Strategy

**Confirmed passing, on-device (`src/androidTest`, real Room + SQLite, in-memory database):**
- `LedgerDaoTest` — insert/read consistency, running-balance arithmetic, and the `isDefault` deletion guard
- `TransactionDaoTest` — atomic balance updates on insert, edit (including a type change from expense to income), and soft delete; soft-deleted transactions correctly excluded from queries

**Written, using Fakes/MockK, not yet confirmed run (`src/test`, plain JVM):**
- `SettingsViewModelTest`
- `DashboardViewModelTest`

**Known gaps — good first contributions:**
- Run and confirm the two ViewModel test suites above; fix anything that surfaces
- `AddEditTransactionViewModelTest` does not exist yet
- `CategoryDao` / `CategoryRepository` have zero test coverage at any layer
- No test currently proves a real `SQLiteConstraintException` correctly converts to `UiError.Database` via `safeCall`/`toUiError()` — everything there has only been reasoned about, not verified against a genuine Room exception
- A full manual pass of the custom-category add/edit/delete flow hasn't been re-confirmed since the error-handling refactor landed
- No Compose UI tests exist yet (`createComposeRule()`)

Repository interfaces enable full ViewModel testing without a real database — the actual payoff of that design choice.

---

## Contributing

This is a personal learning project, opened up for **Hacktoberfest**. Contributions are welcome — with one deliberate constraint: **PRs should improve what already exists, not propose new major features.** That means bug fixes, filling the test coverage gaps listed above, refactors, accessibility improvements, performance work, documentation, and finishing the in-progress v0.2 items (custom categories, filters & search) are all in scope. Proposals for things like cloud sync, group ledgers, or monetization are not being accepted right now — not because they're bad ideas, but because this project has a longer-term roadmap that's intentionally kept out of this public repository for the duration of Hacktoberfest, precisely to keep contributor effort focused on the current codebase rather than speculative future work.

If you're unsure whether something is in scope, open an issue first before starting a PR.