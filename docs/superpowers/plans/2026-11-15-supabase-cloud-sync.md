# Supabase Cloud Sync & Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add optional, 100% offline-first Supabase cloud backup and synchronization to Expense Tracker using the official Supabase Kotlin SDK, AndroidX WorkManager, and Jetpack Compose.

**Architecture:** The local Room SQLite database remains the primary immediate source of truth with sub-10ms latency. Supabase provides remote PostgreSQL storage and GoTrue authentication. A deterministic `SyncManager` orchestrates push/pull snapshot passes over AndroidX `WorkManager`, guaranteeing concurrency safety, zero data loss, and mathematical ledger consistency through a post-sync balance recalculation pass.

**Tech Stack:** Kotlin 2.4+, Jetpack Compose, Room 2.8.5, Hilt 2.60.1, Supabase Kotlin SDK 3.x (`auth-kt`, `postgrest-kt`), Ktor 3.x Android client, AndroidX WorkManager 2.10.x, Kotlinx Serialization.

**Spec:** [`docs/superpowers/specs/2026-11-15-supabase-cloud-sync-design.md`](file:///d:/AndroidStudioProjects/ExpenseTracker/docs/superpowers/specs/2026-11-15-supabase-cloud-sync-design.md)

---

## Global Constraints

- Android API level floor is 24 (Android 7.0 Nougat); target SDK is 37.
- Local Room database is always written to first; network requests never block UI interactions.
- All database IDs use `String` primary keys (defaulting to UUID string) across both Room and Supabase `TEXT` primary keys to eliminate translation layers.
- Supabase tables enforce Row-Level Security (RLS) restricting row access strictly to `auth.uid() = user_id`.
- Foreign key on `transactions.category_id` must be `ON DELETE SET NULL` and nullable across both Room and Supabase.
- Conventional commits with explicit scopes: `feat(sync):`, `feat(auth):`, `fix(db):`, `test(sync):` using `--no-gpg-sign`.
- Sign-out retains local Room data for offline guest continuity; token is cleared from secure storage.

---

## Review Focus

1. **Concurrent local edit during active network upload:** Local `updatedAt` is bumped and `syncStatus` is reset to `PENDING_UPLOAD`. The active worker must not mark the record as `SYNCED` if its `updatedAt` changed since the snapshot was taken.
2. **Category deletion while transactions exist:** Deleting a category sets transaction foreign key to null without deleting any transaction rows.
3. **Empty cloud account hydration on reinstall:** Fresh install with empty default ledger purges the local seeded placeholder and hydrates all user ledgers, categories, and transactions from Supabase.
4. **Post-downstream balance drift:** Downstream transaction upserts must trigger an atomic ledger balance and monthly snapshot recalculation pass.
5. **Network failure mid-sync:** WorkManager catches transient IO/Ktor exceptions, leaves dirty records as `PENDING_UPLOAD`, and schedules exponential backoff retry.

---

## Task Decomposition

### Task 1: Supabase Backend Schema & Row-Level Security Setup

**Files:**
- Supabase Remote Database: `migrations/20261115_initial_sync_schema.sql`

**Interfaces:**
- Consumes: Supabase Project `ExpenseTracker` (`fwycftgspnnthkneomlw`)
- Produces: Remote PostgreSQL tables `public.ledgers`, `public.categories`, `public.transactions` with RLS policies

- [ ] **Step 1: Execute SQL Migration on Supabase via MCP**
  Apply schema defining `ledgers`, `categories`, and `transactions` tables with `TEXT` primary keys, RLS enabled, and `auth.uid() = user_id` policies:
  ```sql
  create table if not exists public.ledgers (
      id text primary key default gen_random_uuid()::text,
      user_id uuid references auth.users(id) on delete cascade not null,
      name text not null,
      currency_code text not null default 'USD',
      is_default boolean not null default false,
      created_at timestamptz not null default now(),
      updated_at timestamptz not null default now(),
      is_deleted boolean not null default false
  );
  alter table public.ledgers enable row level security;
  create policy "Users manage own ledgers" on public.ledgers for all using (auth.uid() = user_id);

  create table if not exists public.categories (
      id text primary key default gen_random_uuid()::text,
      user_id uuid references auth.users(id) on delete cascade not null,
      name text not null,
      type text not null check (type in ('EXPENSE', 'INCOME')),
      icon text not null default 'other',
      is_default boolean not null default false,
      created_at timestamptz not null default now(),
      updated_at timestamptz not null default now(),
      is_deleted boolean not null default false
  );
  alter table public.categories enable row level security;
  create policy "Users manage own categories" on public.categories for all using (auth.uid() = user_id);

  create table if not exists public.transactions (
      id text primary key default gen_random_uuid()::text,
      user_id uuid references auth.users(id) on delete cascade not null,
      ledger_id text references public.ledgers(id) on delete cascade not null,
      category_id text references public.categories(id) on delete set null,
      amount numeric(15, 2) not null,
      type text not null check (type in ('EXPENSE', 'INCOME')),
      date bigint not null,
      note text not null default '',
      created_at timestamptz not null default now(),
      updated_at timestamptz not null default now(),
      is_deleted boolean not null default false
  );
  alter table public.transactions enable row level security;
  create policy "Users manage own transactions" on public.transactions for all using (auth.uid() = user_id);
  ```

- [ ] **Step 2: Verify Supabase tables and RLS policies**
  Run `list_tables` on `supabase_mcp` for schemas `["public"]`. Verify `ledgers`, `categories`, and `transactions` exist with RLS enabled.

- [ ] **Step 3: Commit schema migration file**
  Save SQL definition to `docs/supabase/schema.sql` and commit.
  ```bash
  git add docs/supabase/schema.sql
  git commit -m "feat(supabase): create ledgers, categories, and transactions tables with rls" --no-gpg-sign
  ```

---

### Task 2: Build Scaffolding, Dependencies & Supabase Client Injection

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/example/expensetracker/di/SupabaseModule.kt`
- Test: `app/src/test/java/com/example/expensetracker/di/SupabaseModuleTest.kt`

**Interfaces:**
- Consumes: Supabase URL and Anon Key in `local.properties` / `BuildConfig`
- Produces: `@Singleton` Hilt-provided `SupabaseClient` with `Auth` and `Postgrest` plugins installed

- [ ] **Step 1: Update `gradle/libs.versions.toml`**
  Add versions and library definitions for Supabase BOM (`3.1.4`), `auth-kt`, `postgrest-kt`, `ktor-client-android` (`3.1.1`), `androidx-work-runtime-ktx` (`2.10.0`), `kotlinx-serialization-json` (`1.8.0`), and Kotlin serialization plugin (`2.4.21`).

- [ ] **Step 2: Update `app/build.gradle.kts`**
  Apply `kotlin("plugin.serialization")` plugin.
  Add buildConfig fields `SUPABASE_URL` and `SUPABASE_ANON_KEY` reading from `local.properties` (with fallback to default project URL and anon key).
  Add implementation dependencies for Supabase, Ktor Android, WorkManager, and Kotlinx Serialization.

- [ ] **Step 3: Write failing unit test for `SupabaseModule`**
  Create `app/src/test/java/com/example/expensetracker/di/SupabaseModuleTest.kt` asserting that `SupabaseModule.provideSupabaseClient()` returns a non-null client with `Auth` and `Postgrest` configured.

- [ ] **Step 4: Implement `SupabaseModule.kt`**
  ```kotlin
  @Module
  @InstallIn(SingletonComponent::class)
  object SupabaseModule {
      @Provides
      @Singleton
      fun provideSupabaseClient(): SupabaseClient {
          return createSupabaseClient(
              supabaseUrl = BuildConfig.SUPABASE_URL,
              supabaseKey = BuildConfig.SUPABASE_ANON_KEY
          ) {
              install(Auth)
              install(Postgrest)
          }
      }
  }
  ```

- [ ] **Step 5: Run tests and assemble**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.di.SupabaseModuleTest"`
  Expected: PASS

- [ ] **Step 6: Commit**
  ```bash
  git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/example/expensetracker/di/SupabaseModule.kt app/src/test/java/com/example/expensetracker/di/SupabaseModuleTest.kt
  git commit -m "feat(build): add supabase-kt, ktor, workmanager dependencies and hilt module" --no-gpg-sign
  ```

---

### Task 3: Local Room Database Migration (Version 2 $\rightarrow$ 3)

**Files:**
- Modify: `app/src/main/java/com/example/expensetracker/data/local/entity/Ledger.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/entity/Category.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/entity/Transaction.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/ExpenseDatabase.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/dao/LedgerDao.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/dao/CategoryDao.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/dao/TransactionDao.kt`
- Create: `app/src/test/java/com/example/expensetracker/data/local/Migration2To3Test.kt`

**Interfaces:**
- Consumes: Room database version 2
- Produces: Room database version 3 with columns `syncStatus: Int` (default 0), `updatedAt: Long`, `serverId: String?` across `ledgers`, `categories`, and `transactions` tables. DAO methods to query dirty pending rows.

- [ ] **Step 1: Write failing migration test**
  In `app/src/test/java/com/example/expensetracker/data/local/Migration2To3Test.kt`, assert migration runs SQL:
  - `ALTER TABLE ledgers ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0;`
  - `ALTER TABLE ledgers ADD COLUMN serverId TEXT;`
  - `ALTER TABLE categories ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0;`
  - `ALTER TABLE categories ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0;`
  - `ALTER TABLE categories ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0;`
  - `ALTER TABLE categories ADD COLUMN serverId TEXT;`
  - `ALTER TABLE transactions ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0;`
  - `ALTER TABLE transactions ADD COLUMN serverId TEXT;`

- [ ] **Step 2: Update Room Entities**
  Add `syncStatus: Int = SyncStatus.SYNCED`, `serverId: String? = null` to `Ledger`, `Category`, `Transaction`. Add `updatedAt: Long = System.currentTimeMillis()` and `isDeleted: Boolean = false` to `Category`.
  Define `SyncStatus` constants object: `const val SYNCED = 0; const val PENDING_UPLOAD = 1; const val PENDING_DELETE = 2`.

- [ ] **Step 3: Define `MIGRATION_2_3` in `ExpenseDatabase.kt`**
  Increment database version to `3`. Add `MIGRATION_2_3` to `addMigrations(...)`.

- [ ] **Step 4: Add DAO Queries for Pending Sync**
  In `LedgerDao`: `getPendingSyncLedgers(): List<Ledger>`, `markAsSynced(id: String, serverId: String, verifiedUpdatedAt: Long): Int`
  In `CategoryDao`: `getPendingSyncCategories(): List<Category>`, `markAsSynced(id: String, serverId: String, verifiedUpdatedAt: Long): Int`
  In `TransactionDao`: `getPendingSyncTransactions(): List<Transaction>`, `markAsSynced(id: String, serverId: String, verifiedUpdatedAt: Long): Int`

- [ ] **Step 5: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.data.local.Migration2To3Test"`
  Expected: PASS

- [ ] **Step 6: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/data/local/
  git commit -m "feat(db): implement room migration 2 to 3 adding sync status and timestamps" --no-gpg-sign
  ```

---

### Task 4: Supabase Data Transfer Objects (DTOs) & Bidirectional Mappers

**Files:**
- Create: `app/src/main/java/com/example/expensetracker/data/remote/dto/LedgerDto.kt`
- Create: `app/src/main/java/com/example/expensetracker/data/remote/dto/CategoryDto.kt`
- Create: `app/src/main/java/com/example/expensetracker/data/remote/dto/TransactionDto.kt`
- Create: `app/src/main/java/com/example/expensetracker/data/remote/mapper/SyncMappers.kt`
- Create: `app/src/test/java/com/example/expensetracker/data/remote/SyncMappersTest.kt`

**Interfaces:**
- Consumes: Room entities `Ledger`, `Category`, `Transaction`
- Produces: `@Serializable` DTOs and bidirectional converter functions (`toDto(userId)`, `toEntity()`)

- [ ] **Step 1: Write failing tests in `SyncMappersTest.kt`**
  Test conversion from `Transaction` to `TransactionDto` and back, verifying nullability of `categoryId`, correct timestamp conversion, amount formatting, and preservation of `isDeleted`.

- [ ] **Step 2: Implement DTO classes with `@SerialName`**
  Create `LedgerDto`, `CategoryDto`, and `TransactionDto` matching the Supabase column names (`user_id`, `created_at`, `updated_at`, `is_deleted`, `ledger_id`, `category_id`).

- [ ] **Step 3: Implement Mapper Extensions in `SyncMappers.kt`**
  Implement `Ledger.toDto(userId: String): LedgerDto`, `LedgerDto.toEntity(): Ledger`, and corresponding functions for Category and Transaction.

- [ ] **Step 4: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.data.remote.SyncMappersTest"`
  Expected: PASS

- [ ] **Step 5: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/data/remote/ app/src/test/java/com/example/expensetracker/data/remote/
  git commit -m "feat(sync): add remote dtos and bidirectional room entity mappers" --no-gpg-sign
  ```

---

### Task 5: Supabase Auth Repository (Email & Password Authentication)

**Files:**
- Create: `app/src/main/java/com/example/expensetracker/domain/repository/AuthRepository.kt`
- Create: `app/src/main/java/com/example/expensetracker/data/repository/SupabaseAuthRepositoryImpl.kt`
- Modify: `app/src/main/java/com/example/expensetracker/di/RepositoryModule.kt`
- Create: `app/src/test/java/com/example/expensetracker/data/repository/SupabaseAuthRepositoryTest.kt`

**Interfaces:**
- Consumes: `SupabaseClient` with `Auth` plugin
- Produces: `AuthRepository` with `signInWithEmail(email, password)`, `signUpWithEmail(email, password)`, `signOut()`, `currentUserFlow: StateFlow<AuthUser?>`, `currentUserId: String?`

- [ ] **Step 1: Write failing unit test in `SupabaseAuthRepositoryTest.kt`**
  Test sign-in success, invalid credentials mapping, sign-up with email, and sign-out clearing state.

- [ ] **Step 2: Define `AuthRepository` domain interface and `AuthUser` data class**
  `data class AuthUser(val id: String, val email: String?)`

- [ ] **Step 3: Implement `SupabaseAuthRepositoryImpl`**
  Inject `SupabaseClient`. Implement `signInWithEmail` via `supabase.auth.signInWith(Email) { ... }`, `signUpWithEmail` via `supabase.auth.signUpWith(Email) { ... }`, and `signOut` via `supabase.auth.signOut()`.
  Expose `currentUserFlow` mapped from `supabase.auth.sessionStatus`.

- [ ] **Step 4: Bind in `RepositoryModule.kt`**
  Bind `AuthRepository` to `SupabaseAuthRepositoryImpl`.

- [ ] **Step 5: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.data.repository.SupabaseAuthRepositoryTest"`
  Expected: PASS

- [ ] **Step 6: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/domain/repository/AuthRepository.kt app/src/main/java/com/example/expensetracker/data/repository/SupabaseAuthRepositoryImpl.kt app/src/main/java/com/example/expensetracker/di/RepositoryModule.kt app/src/test/java/com/example/expensetracker/data/repository/SupabaseAuthRepositoryTest.kt
  git commit -m "feat(auth): implement supabase email and password authentication repository" --no-gpg-sign
  ```

---

### Task 6: Downstream Balance & Monthly Snapshot Recalculation Engine

**Files:**
- Modify: `app/src/main/java/com/example/expensetracker/data/local/dao/TransactionDao.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/dao/LedgerDao.kt`
- Modify: `app/src/main/java/com/example/expensetracker/data/local/dao/MonthlySnapshotDao.kt`
- Create: `app/src/test/java/com/example/expensetracker/data/local/BalanceRecalculationTest.kt`

**Interfaces:**
- Consumes: Raw transaction rows in Room
- Produces: `recalculateLedgerBalances(ledgerId: String)` and `recalculateMonthlySnapshots(ledgerId: String, months: Set<Pair<Int, Int>>)`

- [ ] **Step 1: Write failing test in `BalanceRecalculationTest.kt`**
  Insert 5 transactions directly into Room (simulating downstream cloud delta pull), call recalculation function, and assert `Ledger.balance`, `totalIncome`, and `totalExpenses` match exact transaction sums.

- [ ] **Step 2: Implement Recalculation Queries & Transaction Block**
  In `LedgerDao`: `updateTotals(ledgerId: String, balance: Double, income: Double, expenses: Double, updatedAt: Long)`
  In `TransactionDao`: Implement `@androidx.room.Transaction suspend fun recalculateDerivedState(ledgerId: String, ledgerDao: LedgerDao, snapshotDao: MonthlySnapshotDao)`:
  - Calculates `SELECT SUM(amount) WHERE type='INCOME' AND isDeleted=0` and `SUM(amount) WHERE type='EXPENSE' AND isDeleted=0`.
  - Updates `ledger` row with exact math.
  - Recomputes closing balances of affected `monthly_snapshots`.

- [ ] **Step 3: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.data.local.BalanceRecalculationTest"`
  Expected: PASS

- [ ] **Step 4: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/data/local/
  git commit -m "feat(db): add atomic ledger balance and monthly snapshot recalculation pass" --no-gpg-sign
  ```

---

### Task 7: Core Sync Engine (`SyncManager`) & Snapshot Isolation

**Files:**
- Create: `app/src/main/java/com/example/expensetracker/domain/sync/SyncManager.kt`
- Create: `app/src/main/java/com/example/expensetracker/data/sync/SyncManagerImpl.kt`
- Modify: `app/src/main/java/com/example/expensetracker/di/SyncModule.kt`
- Create: `app/src/test/java/com/example/expensetracker/data/sync/SyncManagerTest.kt`

**Interfaces:**
- Consumes: `SupabaseClient.postgrest`, `AuthRepository`, `LedgerDao`, `CategoryDao`, `TransactionDao`, `MonthlySnapshotDao`
- Produces: `SyncManager` with `suspend fun sync(): SyncResult`, `val isSyncing: StateFlow<Boolean>`, `val lastSyncTime: StateFlow<Long?>`

- [ ] **Step 1: Write failing unit test in `SyncManagerTest.kt`**
  - Test A: Sequential upload uploads Ledgers, then Categories, then Transactions.
  - Test B: Snapshot isolation — if a transaction's `updatedAt` changes while upload is in flight, it is NOT marked as `SYNCED` and remains `PENDING_UPLOAD`.
  - Test C: Downstream pull upserts newer cloud records and triggers `recalculateDerivedState`.

- [ ] **Step 2: Define `SyncManager` interface and `SyncResult` sealed class**
  `sealed interface SyncResult { object Success : SyncResult; data class Error(val throwable: Throwable) : SyncResult; object NotAuthenticated : SyncResult }`

- [ ] **Step 3: Implement `SyncManagerImpl`**
  - Check authentication; return `NotAuthenticated` if signed out.
  - **Push Phase:** Claim immutable snapshot of dirty rows (`PENDING_UPLOAD`, `PENDING_DELETE`).
  - Upsert to Supabase PostgREST tables.
  - For each row uploaded, verify local `updatedAt == snapshot.updatedAt` before marking `SYNCED`.
  - **Pull Phase:** Select rows from Supabase where `updated_at > lastSyncTimestamp`.
  - Upsert cloud rows into Room using last-write-wins (LWW) respecting local pending changes.
  - Invoke `recalculateDerivedState`.
  - Update `lastSyncTime` in preferences.

- [ ] **Step 4: Bind in `SyncModule.kt`**
  Provide `SyncManager` as `@Singleton`.

- [ ] **Step 5: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.data.sync.SyncManagerTest"`
  Expected: PASS

- [ ] **Step 6: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/domain/sync/ app/src/main/java/com/example/expensetracker/data/sync/ app/src/test/java/com/example/expensetracker/data/sync/
  git commit -m "feat(sync): implement core sync manager with snapshot isolation and delta merge" --no-gpg-sign
  ```

---

### Task 8: WorkManager Integration (`SyncWorker`)

**Files:**
- Create: `app/src/main/java/com/example/expensetracker/worker/SyncWorker.kt`
- Create: `app/src/main/java/com/example/expensetracker/worker/SyncScheduler.kt`
- Modify: `app/src/main/java/com/example/expensetracker/ExpenseTrackerApplication.kt`
- Create: `app/src/test/java/com/example/expensetracker/worker/SyncWorkerTest.kt`

**Interfaces:**
- Consumes: `SyncManager`
- Produces: `SyncScheduler.enqueueOneTimeSync()`, `SyncScheduler.schedulePeriodicSync()`, `SyncWorker`

- [ ] **Step 1: Write unit test in `SyncWorkerTest.kt`**
  Verify worker delegates to `SyncManager.sync()` and maps `SyncResult.Success` to `Result.success()`, transient errors to `Result.retry()`.

- [ ] **Step 2: Implement `SyncWorker` with `@HiltWorker`**
  Inject `SyncManager`. In `doWork()`, execute `syncManager.sync()`. Return `Result.success()`, `Result.retry()`, or `Result.failure()`.

- [ ] **Step 3: Implement `SyncScheduler`**
  Provide helper functions to enqueue unique one-time expedited sync (`ExistingWorkPolicy.REPLACE`) with `NetworkType.CONNECTED` constraint, and periodic sync (every 6 hours).

- [ ] **Step 4: Configure WorkManager Hilt injection**
  In `ExpenseTrackerApplication.kt`, implement `Configuration.Provider` to supply `HiltWorkerFactory`.

- [ ] **Step 5: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.worker.SyncWorkerTest"`
  Expected: PASS

- [ ] **Step 6: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/worker/ app/src/main/java/com/example/expensetracker/ExpenseTrackerApplication.kt
  git commit -m "feat(sync): add hilt workmanager sync worker and scheduler" --no-gpg-sign
  ```

---

### Task 9: Presentation Layer — `SyncViewModel`

**Files:**
- Create: `app/src/main/java/com/example/expensetracker/presentation/sync/SyncUiState.kt`
- Create: `app/src/main/java/com/example/expensetracker/presentation/sync/SyncViewModel.kt`
- Create: `app/src/test/java/com/example/expensetracker/presentation/sync/SyncViewModelTest.kt`

**Interfaces:**
- Consumes: `AuthRepository`, `SyncManager`, `SyncScheduler`
- Produces: `StateFlow<SyncUiState>` and event handlers `signIn(email, password)`, `signUp(email, password)`, `signOut()`, `syncNow()`

- [ ] **Step 1: Write failing test in `SyncViewModelTest.kt`**
  Using Turbine, verify `SyncViewModel` emits `SyncUiState` changes when user signs in, triggers `syncNow()`, and handles authentication errors.

- [ ] **Step 2: Define `SyncUiState`**
  ```kotlin
  data class SyncUiState(
      val isAuthenticated: Boolean = false,
      val userEmail: String? = null,
      val isSyncing: Boolean = false,
      val lastSyncedTimeMillis: Long? = null,
      val pendingUploadCount: Int = 0,
      val errorMessage: String? = null
  )
  ```

- [ ] **Step 3: Implement `SyncViewModel`**
  Combine `authRepository.currentUserFlow`, `syncManager.isSyncing`, and dirty records count into `uiState: StateFlow<SyncUiState>`.
  Implement `signIn`, `signUp`, `signOut`, and `syncNow`.

- [ ] **Step 4: Run tests to verify**
  Run: `./gradlew testDebugUnitTest --tests "com.example.expensetracker.presentation.sync.SyncViewModelTest"`
  Expected: PASS

- [ ] **Step 5: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/presentation/sync/ app/src/test/java/com/example/expensetracker/presentation/sync/
  git commit -m "feat(presentation): implement sync and auth viewmodel with reactive state" --no-gpg-sign
  ```

---

### Task 10: UI Implementation — Settings Hub, Ambient Pill & Auth Bottom Sheet

**Files:**
- Create: `app/src/main/java/com/example/expensetracker/presentation/sync/AuthBottomSheet.kt`
- Create: `app/src/main/java/com/example/expensetracker/presentation/sync/SyncAccountCard.kt`
- Create: `app/src/main/java/com/example/expensetracker/presentation/sync/SyncAmbientPill.kt`
- Modify: `app/src/main/java/com/example/expensetracker/presentation/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/example/expensetracker/presentation/dashboard/DashboardScreen.kt`
- Modify: `app/src/main/java/com/example/expensetracker/presentation/transaction/TransactionScreen.kt`

**Interfaces:**
- Consumes: `SyncUiState` from `SyncViewModel`
- Produces: Interactive Settings cards, modal bottom sheet, ambient header pill, and local save status badge

- [ ] **Step 1: Implement `AuthBottomSheet.kt`**
  Material 3 ModalBottomSheet with:
  - Segmented toggle: "Sign In" vs "Create Account".
  - Outlined text fields for Email and Password.
  - Clear error banner if authentication fails.
  - Submit button with loading state.
  - Subtle privacy guarantee note: *"Your data is encrypted and private. Offline access remains 100% free."*

- [ ] **Step 2: Implement `SyncAccountCard.kt` in Settings**
  - **Signed-Out State**: Elegant organic card with cloud backup icon, value proposition bullet points, and `[ Enable Cloud Backup ]` button that opens `AuthBottomSheet`.
  - **Signed-In State**: Avatar / email display, relative last sync time (e.g. "Synced just now" or "Synced 5m ago"), manual `[ Sync Now ]` button with rotating indicator, and `[ Sign Out ]` button.

- [ ] **Step 3: Implement `SyncAmbientPill.kt` in Dashboard Header**
  When `isSyncing == true`, display compact animated pill in Dashboard top bar:
  `🔄 Syncing...` with smooth infinite rotation. Automatically hides when sync completes.

- [ ] **Step 4: Add local save badge to `TransactionScreen.kt`**
  On transaction save confirmation, display unobtrusive `⚡ Saved to device` indicator confirming instant zero-latency local write.

- [ ] **Step 5: Verify Compose previews and compile**
  Run: `./gradlew compileDebugKotlin`
  Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**
  ```bash
  git add app/src/main/java/com/example/expensetracker/presentation/
  git commit -m "feat(ui): add settings account card, auth bottom sheet, and ambient sync pill" --no-gpg-sign
  ```

---

### Task 11: End-to-End Verification & Sanity Checks

**Files:**
- Test: Full unit test suite
- Verification: Physical Android device via ADB

- [ ] **Step 1: Run complete test suite**
  Run: `./gradlew testDebugUnitTest`
  Expected: ALL TESTS PASS

- [ ] **Step 2: Build debug APK and install to connected device**
  Run: `./gradlew installDebug`
  Expected: Installed successfully.

- [ ] **Step 3: Verify end-to-end user journey**
  1. Launch app without signing in: add an expense -> instant save verified.
  2. Open Settings -> tap `[ Enable Cloud Backup ]`.
  3. Sign up with email/password -> verify account is created in Supabase GoTrue.
  4. Tap `[ Sync Now ]` -> observe animated sync pill on Dashboard.
  5. Check Supabase table via `supabase_mcp` `execute_sql` -> verify ledger, categories, and transactions rows exist in Supabase!
  6. Sign out -> verify local data remains intact and app transitions to offline guest mode.

- [ ] **Step 4: Final commit and documentation update**
  Update `docs/TECHNICAL_DECISIONS.md` with Decision 88: Supabase Cloud Sync & WorkManager Architecture.
  ```bash
  git add docs/TECHNICAL_DECISIONS.md
  git commit -m "docs(decisions): document supabase cloud sync and workmanager architecture (d88)" --no-gpg-sign
  ```
