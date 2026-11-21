# Design Document: Multi-Ledger Management & Cloud Synchronization

**Date:** 2026-11-21  
**Status:** In Review  
**Milestone:** v0.5 (Multi-Ledger Management & Cloud Sync)  
**Authors:** DeepMind Antigravity & User  

---

## 1. Intent and Context

In milestones v0.1 through v0.4, the Expense Tracker was architected around a single active ledger (`ledger_default`, named "Personal" or "Personal Expenses"). While the foundational database schema and Supabase backend were built with relational foreign keys (`transactions.ledger_id -> ledgers.id`), the user interface and presentation layers have treated the app as a single-account experience.

In real-world personal finance, users—particularly authenticated cloud users—regularly juggle distinct financial spaces:
- **Personal / Day-to-Day**: Groceries, dining, entertainment, shopping.
- **Business / Freelance**: Invoicing, client expenses, equipment, taxes.
- **Family / Household**: Shared rent, utility bills, children's expenses.
- **Vacation / Project**: Discrete, time-bounded travel or renovation budgets.

The intent of **Milestone v0.5** is to enable users to create, switch, edit, and synchronize multiple isolated ledgers seamlessly, both locally offline and across cloud devices via Supabase.

---

## 2. Goals, Non-Goals, and Anti-Goals

### Goals
- **Isolated Financial Books**: Each ledger operates as a completely distinct book of accounts with its own:
  - Running balance, total income, and total expense.
  - Filtered transaction history.
  - Cashflow analytics and Compose spline charts.
  - PDF export statements.
  - Optional custom currency code (overriding the app default).
- **Luxury TopBar Switcher**: Replace static title ("Personal Expenses") with an interactive glass pill `[Book Icon] <Ledger Name> [Chevron Down]`. Tapping opens a bottom sheet allowing instantaneous one-tap ledger switching with smooth Compose transitions.
- **Ledger Management (CRUD)**:
  - Create new ledger with custom name, optional currency selection, and visual badge icon.
  - Edit ledger details (rename, change currency).
  - Delete non-default ledger with cascading safeguards (confirmation dialog warning if transactions exist, automatic switch to default ledger, soft deletion in Room & Supabase).
- **Default Ledger Protection**:
  - Exactly one ledger is marked `isDefault = true`.
  - The default primary ledger cannot be deleted.
- **Bidirectional Supabase Sync**:
  - `SyncManager` pushes newly created or modified ledgers and pulls cloud changes for `auth.uid() = user_id`.
  - Remote deletions and insertions propagate across all logged-in devices.
- **Offline-First Compatibility**:
  - Multi-ledger management works 100% offline.
  - Active ledger selection persists in Jetpack DataStore `UserPreferences`.
  - If signed out (Option A clean-slate), local ledgers reset to a fresh default ledger.

### Non-Goals
- Inter-ledger transfers in this phase (e.g. "Transfer Rs 5,000 from Business to Personal"). While valuable, this introduces complex double-entry multi-transaction mechanics reserved for a future update.
- Multi-user ledger sharing (inviting another Supabase user to collaborate on a shared ledger). Ledgers in v0.5 are private to the authenticated owner (`user_id = auth.uid()`).

### Anti-Goals
- Disrupting existing transactions: migration from v3 to v4 must preserve all existing transactions and link them safely to `ledger_default`.
- Showing full-screen loading spinners when switching ledgers: switching should be instantaneous (<16ms) by leveraging Room Flow streams.

---

## 3. Architecture & Data Model Changes

```
┌─────────────────────────────────────────────────────────────┐
│                    DashboardTopBar                          │
│     [Book Icon] "Business Account" [Chevron Down]           │
└───────────────────────────┬─────────────────────────────────┘
                            │ (tap)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              LedgerSwitcherBottomSheet                      │
│  - Personal (Default)       Rs 15,200.00                    │
│  - Business (Active ✓)      Rs 84,500.00                    │
│  - Family Vacation          Rs 32,000.00                    │
│  ─────────────────────────────────────────────────────────  │
│  [+ Create New Ledger]                                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
              ┌───────────────────────────┐
              │      UserPreferences      │
              │  active_ledger_id: "..."  │
              └─────────────┬─────────────┘
                            │
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌───────────────────────┐       ┌───────────────────────┐
│  DashboardViewModel   │       │ AddEditTransactionVM  │
│  - Scopes balance     │       │ - Auto-assigns tx to  │
│  - Scopes tx list     │       │   active ledger       │
│  - Scopes charts      │       └───────────────────────┘
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│      SyncManager      │ ◄───► Supabase postgrest["ledgers"]
│  - Pushes all ledgers │
│  - Pulls all ledgers  │
└───────────────────────┘
```

### 3.1 Room Schema Migration (v3 $\to$ v4)
`Ledger` entity currently lacks `isDeleted`. We add `isDeleted: Boolean = false` to enable soft-delete replication:

```kotlin
@Entity(tableName = "ledgers")
data class Ledger(
    @PrimaryKey val id: String,
    val name: String,
    val currencyCode: String,
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val createdAt: Long,
    val updatedAt: Long,
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false, // Added in v4
    val syncStatus: Int = SyncStatus.SYNCED,
    val serverId: String? = null
)
```

`MIGRATION_3_4`:
```sql
ALTER TABLE ledgers ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0;
```

### 3.2 User Preferences
```kotlin
private val ACTIVE_LEDGER_ID = stringPreferencesKey("active_ledger_id")

val activeLedgerId: Flow<String?> = ...
suspend fun setActiveLedgerId(id: String)
```

---

## 4. UI & UX Specifications (Organic Forest Luxury)

1. **DashboardTopBar**:
   - Replaces the plain text title with a clickable pill:
     - Background: `extendedColors.cardElevated` with `1.dp` border `extendedColors.cardBorder`.
     - Icon: `R.drawable.ic_wallet` or `R.drawable.ic_book` (ElectricLime in dark mode).
     - Text: Active ledger name (truncated if > 18 characters) + `ic_keyboard_arrow_down`.
   - Tooltip: "Switch active ledger".

2. **LedgerSwitcherBottomSheet**:
   - Clean drag handle with title "Accounts & Ledgers".
   - Cards for each non-deleted ledger:
     - Name in `FontWeight.SemiBold`.
     - Balance formatted with the ledger's currency symbol.
     - Radio selection indicator / checkmark for the active ledger.
     - Secondary chip: "Default" for the primary ledger.
     - Three-dot overflow menu for custom ledgers: "Edit", "Delete".
   - Bottom CTA: "+ New Ledger" button styled in `ElectricLime`.

3. **CreateEditLedgerDialog**:
   - Input: Ledger Name (e.g., "Freelance Consulting").
   - Currency Picker / Toggle: Inherits app default currency with option to change.
   - Validation: Name cannot be blank, must not conflict with existing active ledger name.
   - Save button with immediate local write + background sync queue.

4. **Delete Ledger Safeguard Dialog**:
   - If ledger has transactions: warns "This ledger contains X transactions totaling [Balance]. Deleting this ledger will remove all its transactions. This action cannot be undone."
   - Confirming switches active ledger to default, marks ledger `isDeleted = true`, soft-deletes associated transactions, and sets `syncStatus = PENDING_UPLOAD`.

---

## 5. Verification Plan

1. **Unit Tests**:
   - `LedgerDaoTest`: migration 3->4, insert, soft-delete, get all active non-deleted ledgers.
   - `DashboardViewModelTest`: active ledger switching updates totals, transactions flow, and chart data.
   - `AddEditTransactionViewModelTest`: new transaction inherits active ledger ID.
   - `SyncManagerTest`: multiple ledgers pushed and pulled to/from Supabase mock.
2. **Device Manual Verification**:
   - Create 2 new ledgers: "Business" and "Vacation".
   - Switch between them via TopBar pill.
   - Add transactions in "Business" -> verify "Personal" balance remains unchanged.
   - Trigger manual sync -> verify both ledgers appear in Supabase PostgreSQL table.
   - Delete "Vacation" -> verify confirmation dialog, smooth fallback to "Personal", and soft-delete sync.
