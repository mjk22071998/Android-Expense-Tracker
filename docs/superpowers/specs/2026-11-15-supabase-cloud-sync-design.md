# Design Document: Supabase Cloud Sync & Authentication

**Date:** 2026-11-15  
**Status:** Approved  
**Milestone:** v0.4 (Cloud Sync & Optional Authentication)  
**Authors:** DeepMind Antigravity & User  

---

## 1. Intent and Context

The Expense Tracker application is currently a high-performance, strictly offline-first Android app built with Jetpack Compose, Room, Hilt, Clean Architecture, and Joda-Money. All financial records and categories reside on the device's local SQLite database.

The intent of this project is to introduce **seamless, optional cloud synchronization and backup powered by Supabase**. Users can optionally sign in to back up their financial data and sync across multiple devices. Crucially, login remains completely optional: for guests and offline users, the app continues to function with 100% feature completeness, zero login prompts, and zero network requirement.

---

## 2. Goals, Non-Goals, and Anti-Goals

### Goals
- **100% Offline-First Invariant**: The local Room database is always the primary immediate source of truth. Adding, editing, and deleting transactions always execute locally in `< 10ms` without blocking on network responses.
- **Optional Supabase Authentication**: Email/Password and Google OAuth sign-in via Supabase GoTrue.
- **Background Synchronization Engine**: Robust, battery-efficient synchronization managed by AndroidX `WorkManager` with a `NetworkType.CONNECTED` constraint.
- **Deterministic Concurrency & Snapshot Isolation**: Adding or editing transactions while a background sync pass is actively running must never corrupt data, overwrite newer edits, or block the UI.
- **Zero-Data-Loss First-Time Merge**: When a user who has existing local offline records signs in for the first time, their offline data is automatically associated with their new cloud account and uploaded without wiping or duplicating anything.
- **Unobtrusive, Organic Luxury UI**:
  - **Settings Tab**: The primary account hub. A quiet, elegant card when signed out; a live profile with manual `[ Sync Now ]` and `[ Sign Out ]` when signed in.
  - **Dashboard Tab**: An ambient, compact animated sync pill in the header (`🔄 Syncing...`) during active network passes.
  - **Transaction Form**: Immediate local save confirmation badge (`⚡ Saved to device`).
  - **Auth Modal**: A frictionless bottom sheet with transparent trust assurances.

### Non-Goals
- Mandatory authentication or gating core features (charts, PDF export, budgeting) behind an account.
- Real-time peer-to-peer WebSocket synchronization (unnecessary battery drain for personal single-user ledgers).
- Cloud-only storage: data is never stored exclusively in the cloud; local Room cache is always fully hydrated.

### Anti-Goals
- Blocking the UI thread or showing full-screen modal loading spinners during network requests.
- Dropping or reverting local user edits if a sync cycle fails or drops connection.
- Overwriting newer local edits with older cloud data (strict timestamp-based last-write-wins with server authority).

---

## 3. Constraints

- **Platform**: Android API 24 (Android 7.0 Nougat) to API 37 (Android 15+).
- **Architecture**: Clean Architecture (Data $\rightarrow$ Domain $\rightarrow$ Presentation) with reactive Kotlin `StateFlow` and unidirectional data flow.
- **Security**: Supabase Row-Level Security (RLS) policies enforcing `auth.uid() = user_id` across all tables. Secure hardware-backed token storage via AndroidX `EncryptedSharedPreferences`.
- **Dependencies**: Official Supabase Kotlin Multiplatform SDK (`io.github.jan-tennert.supabase:postgrest-kt`, `gotrue-kt`), Ktor Android client engine, and AndroidX `WorkManager`.

---

## 4. Approaches Considered

### Approach 1: Official Supabase Kotlin SDK + AndroidX WorkManager *(Selected)*
- **Architecture**: Uses official `supabase-kt` modules (`gotrue` + `postgrest`) with AndroidX `WorkManager`. Local Room entities track sync state (`SYNCED`, `PENDING_UPLOAD`, `PENDING_DELETE`).
- **Trade-offs**: Provides official GoTrue token management, auto-refresh, typed PostgREST queries, and OS-level battery optimization. Adds ~1.2 MB to APK size.
- **Decision**: **Selected**. Matches industry standards, eliminates hand-rolled token security hazards, and leverages native Android lifecycle management.

### Approach 2: Hand-rolled REST Client (Direct PostgREST HTTP)
- **Architecture**: Calling Supabase REST endpoints directly via Retrofit or raw Ktor without the Supabase SDK.
- **Trade-offs**: Saves ~1 MB of binary size, but requires manually implementing token refresh interceptors, OAuth PKCE exchange, and error parsers. High maintenance overhead and risk of auth bugs.
- **Decision**: Rejected. Unnecessary maintenance risk when the official SDK is actively maintained.

### Approach 3: Realtime WebSockets (`realtime-kt`)
- **Architecture**: Maintaining persistent WebSocket connections to Supabase Realtime CDC channels while the app is in the foreground.
- **Trade-offs**: Sub-second synchronization between devices, but high radio/battery drain and fragility during cellular handoffs.
- **Decision**: Rejected. A personal expense tracker does not require sub-second live streaming; batch sync on app launch and after edits is superior for battery and offline reliability.

---

## 5. Architectural Specification

### 5.1 Supabase Backend Schema & Security
All tables enforce Row-Level Security (RLS) so users can only ever access their own data.
Primary keys use `text` (defaulting to `gen_random_uuid()::text`) to map 1:1 with Room's existing `String` IDs (such as `"cat_food"`, `"ledger_default"`, and UUIDs) without requiring an error-prone translation table.

```sql
-- 1. Ledgers table
create table public.ledgers (
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
create policy "Users manage own ledgers" on public.ledgers
    for all using (auth.uid() = user_id);

-- 2. Categories table
create table public.categories (
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
create policy "Users manage own categories" on public.categories
    for all using (auth.uid() = user_id);

-- 3. Transactions table
create table public.transactions (
    id text primary key default gen_random_uuid()::text,
    user_id uuid references auth.users(id) on delete cascade not null,
    ledger_id text references public.ledgers(id) on delete cascade not null,
    category_id text references public.categories(id) on delete set null,
    amount numeric(15, 2) not null,
    type text not null check (type in ('EXPENSE', 'INCOME')),
    date bigint not null, -- epoch millis
    note text not null default '',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false
);

alter table public.transactions enable row level security;
create policy "Users manage own transactions" on public.transactions
    for all using (auth.uid() = user_id);
```

### 5.2 Local Room Database Schema Migration (Migration 2 $\rightarrow$ 3)
Add sync tracking columns to local Room entities:
- `sync_status`: `Int` (`0 = SYNCED`, `1 = PENDING_UPLOAD`, `2 = PENDING_DELETE`).
- `updated_at`: `Long` (epoch milliseconds of local modification).
- `server_id`: `String?` (mapped to cloud UUID).

When the user creates/updates a transaction locally, Room immediately writes:
- `sync_status = PENDING_UPLOAD`
- `updated_at = System.currentTimeMillis()`

When deleted locally:
- `is_deleted = true`
- `sync_status = PENDING_DELETE`
- `updated_at = System.currentTimeMillis()`

### 5.3 Concurrency & Snapshot Isolation Algorithm
To guarantee that user additions/edits during sync never break:
1. **Triggering**: WorkManager `SyncWorker` is enqueued upon app foregrounding, after local edits (debounced by 3 seconds), or upon manual `[ Sync Now ]` press.
2. **Snapshot Claim**: At the start of `doWork()`, the worker queries dirty records:
   ```kotlin
   val dirtyLedgers = ledgerDao.getPendingSyncLedgers()
   val dirtyCategories = categoryDao.getPendingSyncCategories()
   val dirtyTransactions = transactionDao.getPendingSyncTransactions()
   ```
3. **Sequential Dependency Upload**:
   - Upload dirty `Ledgers` $\rightarrow$ mark as `SYNCED`.
   - Upload dirty `Categories` $\rightarrow$ mark as `SYNCED`.
   - Upload dirty `Transactions` $\rightarrow$ mark as `SYNCED`.
4. **Concurrent Edit Safety**: If the user edits Transaction #42 *while* the upload HTTP call is in flight, the local Room update immediately bumps Transaction #42's `updated_at` and resets `sync_status = PENDING_UPLOAD`. When the active worker finishes, it only marks records as `SYNCED` if their `updated_at` has not changed since the snapshot was taken. Any concurrent edit remains `PENDING_UPLOAD` and is automatically picked up by the next sync cycle.
5. **Download & Delta Merging**:
   - Query Supabase for rows with `updated_at > last_successful_sync_timestamp`.
   - If a cloud row has an `updated_at` greater than the local row, upsert into Room. If local row has `PENDING_UPLOAD`, local changes take precedence (Last-Write-Wins with conflict avoidance).
6. **Derived State Recalculation Pass**:
   - Supabase stores raw source-of-truth records (`transactions`, `categories`, `ledgers`).
   - After applying downloaded transaction deltas to Room, `SyncManager` invokes a local Room aggregation pass to recompute `Ledger.balance`, `totalIncome`, `totalExpenses`, and regenerates `MonthlySnapshot` entries for affected calendar months. This guarantees 100% mathematical precision and prevents balance drift.

### 5.4 First-Time Login Merge, Sign-Out & Multi-Device Scenarios
- **Scenario A: Fresh Device / Reinstall (Cloud Account Already Has Data)**:
  - If the user signs in on a device with 0 user-created transactions (only the initial empty default ledger), Room simply purges the local default seed and hydrates completely from the cloud snapshot.
- **Scenario B: First Sign-In with Pre-Existing Offline Transactions**:
  - Fetch the user's `user_id` from GoTrue.
  - If the cloud already has a default ledger, local pre-login transactions are re-parented to the cloud default ledger. Categories are matched by `(name, type)` to prevent duplicates.
  - Room transaction marks all local records with `user_id` and `sync_status = PENDING_UPLOAD`.
  - Enqueue immediate initial sync to upload offline history to Supabase without data loss.
- **Scenario C: Sign-Out Data Retention**:
  - When the user signs out, local Room data is **retained** so the user can continue using the app as an offline guest without losing access to their records.
  - Session tokens are cleared from secure storage.
  - If a *different* Supabase account signs in on this device in the future, the app detects account mismatch (`last_authenticated_user_id != new_user_id`) and displays a confirmation dialog asking the user whether to merge local records into the new account or clear local records before loading the new account's data.

### 5.5 Authentication Scope (Phase 1 vs Future)
- **Phase 1 (This Milestone)**:
  - Email & Password authentication using Supabase GoTrue (`gotrue-kt`).
  - Seamless Sign In, Sign Up, and Password Reset.
  - Instant out-of-the-box reliability without third-party console dependencies.
- **Phase 2 (Fast-Follow)**:
  - Google and Facebook OAuth integration once core sync functionality is verified in production.

### 5.5 UI Components & State Representation
- **`SyncUiState`**:
  ```kotlin
  data class SyncUiState(
      val isAuthenticated: Boolean = false,
      val userEmail: String? = null,
      val isSyncing: Boolean = false,
      val lastSyncedTimeMillis: Long? = null,
      val pendingUploadCount: Int = 0
  )
  ```
- **Settings Screen Hub**:
  - `SignedOutCard`: Explains benefits, button opens `AuthBottomSheet`.
  - `SignedInCard`: Displays user identity, relative last sync time (e.g. "Synced 2m ago"), `[ Sync Now ]` with rotating progress indicator, and `[ Sign Out ]`.
- **Dashboard Header**:
  - Animated `🔄 Syncing...` pill appears only when `isSyncing == true`.
- **Add/Edit Transaction**:
  - Clean `⚡ Saved to device` status confirms instant local persistence.

---

## 6. What's Left to the Builder

- Exact animation curve and duration for the sync rotation indicator (standard `infiniteRepeatable` with `tween(1200, easing = LinearEasing)`).
- Specific exponential backoff retry intervals for `WorkManager` (initial 15s backoff, max 3 retries).
- Supabase project URL and anon public key configuration via Gradle `BuildConfig` / `local.properties`.

---
