# Multi-Ledger Management & Cloud Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable users (especially authenticated accounts syncing to Supabase) to create, switch, edit, and synchronize multiple isolated ledgers (e.g., Personal, Business, Family, Side Hustle) with independent running balances, isolated transaction histories, scoped analytics, and multi-device cloud sync.

**Architecture:** 
- The local Room SQLite database remains the primary immediate source of truth.
- `Ledger` Room entity gains `isDeleted: Boolean` via `MIGRATION_3_4`.
- `UserPreferences` in Jetpack DataStore stores and emits `activeLedgerId`.
- `DashboardViewModel` reacts to active ledger changes, updating balance, transaction flow, and spline charts in real time.
- `DashboardTopBar` gains an interactive, luxury glass pill switcher opening `LedgerSwitcherBottomSheet`.
- Full CRUD workflows: create ledger, edit ledger, safe deletion (with transaction warnings, soft deletion, and fallback to default ledger).
- `SyncManager` pushes and pulls multiple ledgers to/from Supabase PostgREST `public.ledgers` with RLS `auth.uid() = user_id`.

**Tech Stack:** Kotlin 2.4+, Jetpack Compose, Room 2.8.5, Hilt 2.60.1, Supabase Kotlin SDK 3.x, Jetpack DataStore Preferences.

**Spec:** [`docs/superpowers/specs/2026-11-21-multi-ledger-management-design.md`](file:///d:/AndroidStudioProjects/ExpenseTracker/docs/superpowers/specs/2026-11-21-multi-ledger-management-design.md)

---

## Global Constraints

- Android API level floor is 24 (Android 7.0 Nougat); target SDK is 37.
- Room database schema migration from v3 to v4 must be backwards compatible with zero data loss (`ALTER TABLE ledgers ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0`).
- The primary default ledger (`isDefault = true`) can never be deleted.
- Deleting a non-default ledger must warn the user if transactions exist, cascade soft-deletion (`isDeleted = 1`), and immediately switch the active ledger back to `ledger_default`.
- Commits must follow Conventional Commits format (`feat(ledger):`, `fix(ledger):`, `test(ledger):`) with `--no-gpg-sign`.

---

## Task Decomposition (Tracer-First)

### Task 1: Database Migration (v3 $\to$ v4) & Data Access Layer
**Files:**
- `app/src/main/java/com/example/expensetracker/data/local/entity/Ledger.kt`
- `app/src/main/java/com/example/expensetracker/data/local/dao/LedgerDao.kt`
- `app/src/main/java/com/example/expensetracker/data/local/database/AppDatabase.kt`
- `app/src/main/java/com/example/expensetracker/data/local/UserPreferences.kt`
- `app/src/main/java/com/example/expensetracker/data/remote/mapper/SyncMappers.kt`

- [ ] **Step 1: Update `Ledger` Entity**
  Add `val isDeleted: Boolean = false` to `Ledger` entity with default false and update factory method.
- [ ] **Step 2: Add Room `MIGRATION_3_4`**
  Add `MIGRATION_3_4` to `AppDatabase` executing `ALTER TABLE ledgers ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0;` and bump database version to 4.
- [ ] **Step 3: Update `LedgerDao` Queries**
  - Update `getAllLedgers()` to `SELECT * FROM ledgers WHERE isDeleted = 0 ORDER BY isDefault DESC, createdAt ASC`.
  - Add `softDelete(id: String, updatedAt: Long)`: `UPDATE ledgers SET isDeleted = 1, syncStatus = 1, updatedAt = :updatedAt WHERE id = :id AND isDefault = 0`.
  - Update `getPendingSyncLedgers()` to include dirty or deleted ledgers.
- [ ] **Step 4: Update `UserPreferences`**
  Add `activeLedgerId: Flow<String?>` and `suspend fun setActiveLedgerId(id: String)`.
- [ ] **Step 5: Update `SyncMappers`**
  Map `Ledger.isDeleted` to `LedgerDto.isDeleted` in `toDto` and `toEntity`.

---

### Task 2: Repository & ViewModel Scoping (Tracer Slice)
**Files:**
- `app/src/main/java/com/example/expensetracker/domain/repository/LedgerRepository.kt`
- `app/src/main/java/com/example/expensetracker/data/repository/LedgerRepositoryImpl.kt`
- `app/src/main/java/com/example/expensetracker/presentation/dashboard/DashboardViewModel.kt`
- `app/src/main/java/com/example/expensetracker/presentation/dashboard/DashboardUiState.kt`
- `app/src/main/java/com/example/expensetracker/presentation/transaction/AddEditTransactionViewModel.kt`

- [ ] **Step 1: Expand `LedgerRepository`**
  Add `getActiveLedger(activeId: String?): Flow<Ledger?>` and `softDeleteLedger(id: String): AppResult<Unit>`.
- [ ] **Step 2: Update `DashboardUiState`**
  Add `val activeLedger: Ledger? = null`, `val allLedgers: List<Ledger> = emptyList()`, and `val isLedgerSwitcherVisible: Boolean = false`.
- [ ] **Step 3: Refactor `DashboardViewModel` to Observe Active Ledger**
  Combine `userPreferences.activeLedgerId` with `ledgerRepository.getAllLedgers()`. When active ledger switches:
  - Update `balance`, `totalIncome`, and `totalExpense` from active ledger.
  - Re-trigger `loadTransactions()`, `loadMonthlyComparison()`, and `loadCategoryUsages()` with new active ledger ID.
  - Re-compute spline chart dataset.
- [ ] **Step 4: Wire `AddEditTransactionViewModel`**
  Read active ledger ID so new transactions automatically default to the selected ledger.

---

### Task 3: Luxury UI — TopBar Switcher & Switcher Bottom Sheet
**Files:**
- `app/src/main/java/com/example/expensetracker/presentation/dashboard/DashboardTopBar.kt`
- `app/src/main/java/com/example/expensetracker/presentation/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/example/expensetracker/presentation/ledger/LedgerSwitcherBottomSheet.kt` [NEW]
- `app/src/main/java/com/example/expensetracker/presentation/ledger/LedgerItemCard.kt` [NEW]

- [ ] **Step 1: Create `LedgerSwitcherBottomSheet`**
  - Modal bottom sheet displaying all active ledgers in elegant forest luxury cards.
  - Displays ledger name, currency, balance, active radio check, and default badge.
  - One-tap selection switches active ledger and dismisses sheet.
  - Overflow menu on custom ledgers: Edit and Delete.
  - Sticky bottom CTA: "+ New Ledger" button (`ElectricLime`).
- [ ] **Step 2: Upgrade `DashboardTopBar`**
  - Replace static title with interactive clickable pill: `[Book Icon] <Active Ledger Name> [Chevron Down]`.
  - Clicking pill opens `LedgerSwitcherBottomSheet`.

---

### Task 4: Ledger Management (CRUD) & Deletion Safeguards
**Files:**
- `app/src/main/java/com/example/expensetracker/presentation/ledger/CreateEditLedgerDialog.kt` [NEW]
- `app/src/main/java/com/example/expensetracker/presentation/ledger/DeleteLedgerConfirmDialog.kt` [NEW]
- `app/src/main/java/com/example/expensetracker/presentation/dashboard/DashboardViewModel.kt`

- [ ] **Step 1: Create `CreateEditLedgerDialog`**
  - Dialog / Sheet to create or rename a ledger.
  - Custom name input with validation (non-blank, distinct).
  - Currency picker (defaulting to app currency).
  - On submit: calls `dashboardViewModel.createLedger(name, currency)` or `renameLedger(id, name)`.
- [ ] **Step 2: Create `DeleteLedgerConfirmDialog`**
  - Warns if transactions exist in the ledger.
  - Action: deletes ledger (`softDelete`), switches active ledger to default `ledger_default`, enqueues sync.

---

### Task 5: Cloud Synchronization & Multi-Device Testing
**Files:**
- `app/src/main/java/com/example/expensetracker/data/sync/SyncManagerImpl.kt`
- `app/src/test/java/com/example/expensetracker/data/sync/SyncManagerTest.kt`
- `app/src/test/java/com/example/expensetracker/presentation/dashboard/DashboardViewModelTest.kt`

- [ ] **Step 1: Verify `SyncManagerImpl` Multi-Ledger Support**
  - Confirm `pushLedgers` upserts new and updated ledgers to Supabase.
  - Confirm `pullLedgers` downloads cloud ledgers and updates Room.
- [ ] **Step 2: Automated Unit Tests**
  - Write tests for `DashboardViewModel` multi-ledger switching.
  - Write tests for `LedgerDao` migration 3->4 and soft-delete.
  - Run `./gradlew testDebugUnitTest` and ensure 100% pass rate.
