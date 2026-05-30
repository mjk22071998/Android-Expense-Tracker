# Expense Tracker

A production-grade, offline-first personal finance Android application built with modern Android development practices. Designed for individuals and groups to track income, expenses, and manage multiple ledgers with seamless cloud synchronization.

---

## Features

### Current (v0.1)
- **Offline First** — fully functional without internet, always
- **Transaction Management** — add, edit, and soft-delete income and expense transactions
- **Smart Balance Tracking** — real-time running balance with O(1) reads
- **Monthly Snapshots** — opening and closing balance tracked per month for accurate accounting
- **Category System** — transactions organized by categories with Material icons
- **Date Range Filtering** — filter by Last 30 Days, This Week, This Month, This Year, or Custom range
- **Currency Support** — auto-detected from device locale, user selectable with full currency list
- **Theme Support** — light, dark, and system default

### Planned
- **Auth & Cloud Sync** (v0.3) — Firebase Auth with background Firestore sync
- **Group Ledgers** (v0.4) — shared expense management with real-time collaboration
- **Multiple Personal Ledgers** (v0.5) — manage separate budgets in one app
- **Monetization** (v0.6) — free tier with Pro upgrade via Google Play Billing

---

## Architecture

This project follows **Clean Architecture** with **MVVM** pattern, ensuring separation of concerns, testability, and scalability across all planned versions.

```
app/
├── data/
│   ├── local/                  # Room database, DAOs, entities
│   │   ├── dao/                # Data Access Objects
│   │   ├── database/           # AppDatabase, migrations
│   │   └── entity/             # Room entities + projections
│   ├── remote/                 # Firestore, Firebase (v0.3+)
│   └── repository/             # Repository implementations
│
├── domain/
│   └── model/                  # Domain models, sealed classes, helpers
│
├── presentation/
│   ├── dashboard/              # Dashboard screen, ViewModel, UiState
│   ├── transaction/            # Add/Edit transaction screens
│   ├── settings/               # Settings screen, ViewModel, UiState
│   └── navigation/             # NavGraph, Screen sealed class
│
└── di/                         # Hilt dependency injection modules
```

### Architecture Principles

- **Offline First** — Room is always the single source of truth. UI never reads from remote directly
- **Unidirectional Data Flow** — Repository → ViewModel → UiState → UI
- **Reactive UI** — Room `Flow` + `StateFlow` means UI updates automatically on data changes
- **Plug & Play** — Repository interfaces allow swapping local-only implementation for synced implementation in v0.3 without touching UI or ViewModel code
- **Atomic Operations** — transaction inserts, updates, and deletes are wrapped in Room `@Transaction` ensuring balance and monthly snapshots are never out of sync

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| Navigation | Navigation Compose |
| Database | Room (SQLite) |
| Dependency Injection | Hilt |
| Async | Kotlin Coroutines + Flow |
| Local Preferences | DataStore |
| Background Sync | WorkManager *(v0.3)* |
| Auth | Firebase Auth *(v0.3)* |
| Cloud Sync | Firestore *(v0.3)* |
| Push Notifications | Firebase Cloud Messaging *(v0.4)* |
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
totalExpense    REAL                    running total expense
createdAt       INTEGER
updatedAt       INTEGER
isDefault       INTEGER
```

**`categories`**
```
id              TEXT    PRIMARY KEY     (UUID)
name            TEXT
icon            TEXT                    Material icon identifier
transactionType TEXT                    INCOME | EXPENSE | BOTH
isDefault       INTEGER
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

### Key Design Decisions

- **UUID primary keys** — device-generated, collision-free across devices for sync
- **Soft delete** — `isDeleted` flag instead of physical delete, enabling sync propagation
- **Running balance** — maintained on `Ledger` for O(1) reads, never calculated by scanning all rows
- **Monthly snapshots** — opening/closing balance per month updated atomically with every transaction
- **Composite indexes** — `(ledgerId, type, isDeleted, date)` for fast period queries
- **Atomic transactions** — Room `@Transaction` guarantees balance and snapshot never drift from actual data

---

## Sync Architecture (v0.3)

```
┌─────────────────────────────────────────┐
│              Android Device              │
│                                          │
│  UI ──→ ViewModel ──→ Repository         │
│                           │              │
│                      Room (SQLite)       │
│                      Source of Truth     │
│                           │              │
│                      Sync Queue          │
│                    (PENDING_UPLOAD)      │
│                           │              │
│                      WorkManager         │
│                    (on connectivity)     │
└───────────────────────────┼─────────────┘
                            │
                    Firebase Firestore
                    (cloud mirror)
```

- **Write local first, sync later** — UI is never blocked by network
- **Last Write Wins** — `updatedAt` timestamps resolve conflicts
- **Sync queue** — `sync_status` column tracks `SYNCED / PENDING_UPLOAD / PENDING_DELETE`
- **Offline group edits** — group transactions queue locally and sync on reconnect

---

## Versioned Roadmap

| Version | Focus | Status |
|---|---|---|
| v0.1 | Core offline functionality | In Progress |
| v0.2 | Filters, date ranges, search | Planned |
| v0.3 | Auth + personal cloud sync | Planned |
| v0.4 | Group ledgers | Planned |
| v0.5 | Multiple personal ledgers | Planned |
| v0.6 | Monetization | Planned |
| v1.0 | Production release | Planned |

---

## Project Setup

### Prerequisites
- Android Studio Narwhal or later
- JDK 11+
- Android SDK 24+

### Clone & Run
```bash
git clone https://github.com/yourusername/expense-tracker.git
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
kotlin = "2.x.x"
agp = "9.x.x"
composeBom = "2026.x.x"

# Database
room = "2.x.x"

# DI
hilt = "2.59.x"

# Async
kotlinx-coroutines = "1.x.x"

# Navigation
navigation-compose = "2.x.x"

# Preferences
datastore = "1.x.x"
```

---

## Testing Strategy *(Planned for v0.2+)*

- **Unit Tests** — ViewModels tested with `FakeRepository` implementations, no DB required
- **Integration Tests** — Room DAO tests with in-memory database
- **UI Tests** — Compose UI tests with `createComposeRule()`

Repository interfaces enable full ViewModel testing without a real database — a core architectural benefit.

---

## Contributing

This is currently a personal portfolio project. Contribution are warmly welcome

---
