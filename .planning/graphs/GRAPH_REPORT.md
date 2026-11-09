# Graph Report - ExpenseTracker  (2026-11-08)

## Corpus Check
- 100 files · ~185,803 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 59 file(s) not represented in the graph (top: .xml 49, (none) 3, .properties 3)
- WARNING: 59 of 180 files seen (33%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 962 nodes · 2577 edges · 46 communities (23 shown, 23 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 115 edges (avg confidence: 0.91)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9a47b768`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TransactionFormScreen.kt
- LedgerDao
- DashboardViewModelTest.kt
- MainActivity.kt
- DateUtils
- Category
- 🛠️ Contribution Workflow
- CategorySeeder
- AppResult
- DashboardViewModel
- Communities (49 total, 26 thin omitted)
- with
- safeCall
- Transaction
- SettingsViewModel
- TransactionDaoTest
- AddEditTransactionViewModel
- LedgerDao
- UiError
- LedgerSeeder
- steps
- UserPreferences
- TransactionRepositoryImpl
- Expense Tracker
- PreferencesModule.kt
- RepositoryModule.kt
- CurrencyHelper
- CurrencyModel
- TransactionWithCategory
- Transaction
- DateRangeFilter
- DashboardScreen
- AppStartupViewModel.kt
- SyncStatus
- FilterChipRow
- ExampleUnitTest.kt
- PULL_REQUEST_TEMPLATE.md

## God Nodes (most connected - your core abstractions)
1. `Category` - 54 edges
2. `AppResult` - 53 edges
3. `ExpenseTrackerTheme()` - 48 edges
4. `Transaction` - 45 edges
5. `Ledger` - 43 edges
6. `DashboardViewModel` - 34 edges
7. `AddEditTransactionViewModel` - 30 edges
8. `LedgerDao` - 30 edges
9. `AppDatabase` - 27 edges
10. `UiError` - 26 edges

## Surprising Connections (you probably didn't know these)
- `Suggested Questions` --references--> `UserPreferences`  [INFERRED]
  .planning/graphs/GRAPH_REPORT.md → app/src/main/java/com/example/expensetracker/data/local/UserPreferences.kt
- `Applied Design Patterns` --references--> `UserPreferences`  [INFERRED]
  README.md → app/src/main/java/com/example/expensetracker/data/local/UserPreferences.kt
- `God Nodes (most connected - your core abstractions)` --references--> `Category`  [INFERRED]
  .planning/graphs/GRAPH_REPORT.md → app/src/main/java/com/example/expensetracker/data/local/entity/Category.kt
- `Suggested Questions` --references--> `Category`  [INFERRED]
  .planning/graphs/GRAPH_REPORT.md → app/src/main/java/com/example/expensetracker/data/local/entity/Category.kt
- `Applied Design Patterns` --references--> `Category`  [INFERRED]
  README.md → app/src/main/java/com/example/expensetracker/data/local/entity/Category.kt

## Import Cycles
- None detected.

## Communities (46 total, 23 thin omitted)

### Community 0 - "TransactionFormScreen.kt"
Cohesion: 0.06
Nodes (10): EmptyTransactionState(), EmptyTransactionStatePreview(), TypeFilterRow(), TypeFilterRowPreview(), AmountInput(), AmountInputPreview(), TypeToggle(), TypeTogglePreview() (+2 more)

### Community 1 - "LedgerDao"
Cohesion: 0.06
Nodes (6): CategoryDao, MonthlySnapshotDao, TransactionDao, AppDatabase, MonthlySnapshot, DatabaseModule

### Community 2 - "DashboardViewModelTest.kt"
Cohesion: 0.05
Nodes (15): Constants, Database, NavArgs, Preferences, Search, ThemeMode, TransactionType, AddTransaction (+7 more)

### Community 3 - "MainActivity.kt"
Cohesion: 0.05
Nodes (11): MainActivity, NavGraph(), SettingsScreen(), SettingsScreenPreview(), SettingsSectionCard(), ThemeModeSelector(), ThemeModeSelectorPreview(), SplashOverlay() (+3 more)

### Community 4 - "DateUtils"
Cohesion: 0.09
Nodes (6): DateUtils, CustomDateRangePickerDialog(), SelectableDates, DatePickerField(), DatePickerFieldPreview(), DateUtilsTest

### Community 5 - "Category"
Cohesion: 0.07
Nodes (18): Category, getCategoryIcon(), CategoryCheckboxRow(), CategoryFilterBottomSheet(), CategoryFilterBottomSheetPreview(), AddEditTransactionUiState, CategoryChip(), CategoryChipPreview() (+10 more)

### Community 6 - "🛠️ Contribution Workflow"
Cohesion: 0.18
Nodes (10): 1. Find or Pick an Issue, 2. Fork the Repository, 3. Clone Your Fork, 4. Checkout to `master`, 5. Do Your Changes, 6. Make Sure It Builds Successfully, 7. Push Your Changes, 8. Make a Pull Request (PR) (+2 more)

### Community 8 - "AppResult"
Cohesion: 0.15
Nodes (4): Ledger, LedgerRepository, SyncableEntity, FakeLedgerRepository

### Community 10 - "Communities (49 total, 26 thin omitted)"
Cohesion: 0.06
Nodes (31): Communities (49 total, 26 thin omitted), Community 0 - "TransactionFormScreen.kt", Community 10 - "AmountInput.kt", Community 11 - "with", Community 12 - "safeCall", Community 14 - "UiError", Community 19 - "Constants", Community 1 - "LedgerDao" (+23 more)

### Community 11 - "with"
Cohesion: 0.08
Nodes (24): name, runs-on, steps, concurrency, cancel-in-progress, group, jobs, build-and-lint (+16 more)

### Community 12 - "safeCall"
Cohesion: 0.14
Nodes (5): CategoryRepositoryImpl, LedgerRepositoryImpl, safeCall(), Success, toUiError()

### Community 13 - "Transaction"
Cohesion: 0.14
Nodes (4): CategoryRepository, AppResult, FakeTransactionRepository, Suggested Questions

### Community 15 - "TransactionDaoTest"
Cohesion: 0.05
Nodes (7): CategoryDaoTest, LedgerDaoTest, TransactionDaoTest, TransactionRepositoryTest, ExampleInstrumentedTest, DateRangeFilterTest, Testing & Verification

### Community 18 - "UiError"
Cohesion: 0.25
Nodes (6): Error, Database, Network, NotFound, UiError, Unknown

### Community 20 - "steps"
Cohesion: 0.13
Nodes (14): jobs, validate-branch, name, on, pull_request, types, name, run (+6 more)

### Community 23 - "Expense Tracker"
Cohesion: 0.29
Nodes (6): Architecture & Design Patterns, Database Schema, Design Language: Organic Forest Luxury, Entities, Expense Tracker, Tech Stack

### Community 27 - "CurrencyModel"
Cohesion: 0.24
Nodes (5): CurrencyModel, CurrencyPickerDialog(), CurrencyPickerDialogPreview(), CurrencyRow(), SettingsUiState

### Community 29 - "Transaction"
Cohesion: 0.15
Nodes (3): Transaction, TransactionRepository, God Nodes (most connected - your core abstractions)

### Community 30 - "DateRangeFilter"
Cohesion: 0.18
Nodes (11): Custom, DateRangeFilter, ThisMonth, ThisWeek, ThisYear, Today, Validation, Knowledge Gaps (+3 more)

### Community 31 - "DashboardScreen"
Cohesion: 0.08
Nodes (16): BalanceCard(), BalanceCardPreview(), DashboardScreen(), DashboardTopBar(), DashboardTopBarPreview(), SummaryCard(), SummaryCardsPreview(), DetailRow() (+8 more)

### Community 34 - "SyncStatus"
Cohesion: 0.40
Nodes (4): SyncStatus, PENDING_DELETE, PENDING_UPLOAD, SYNCED

### Community 39 - "FilterChipRow"
Cohesion: 0.53
Nodes (3): FilterChipRow(), FilterChipRowPreview(), StyledFilterChip()

### Community 44 - "PULL_REQUEST_TEMPLATE.md"
Cohesion: 0.50
Nodes (3): Changes Made, Checklist, Description

## Knowledge Gaps
- **5 isolated node(s):** `SYNCED`, `PENDING_UPLOAD`, `PENDING_DELETE`, `Settings`, `Unknown`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 198 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Category` connect `Category` to `TransactionFormScreen.kt`, `LedgerDao`, `AddEditTransactionViewModel.kt`, `CategorySeeder`, `safeCall`, `Transaction`, `TransactionDaoTest`, `AddEditTransactionViewModel`, `TransactionWithCategory`, `Transaction`, `DateRangeFilter`?**
  _High betweenness centrality (0.135) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `Category` (e.g. with `God Nodes (most connected - your core abstractions)` and `Suggested Questions`) actually correct?**
  _`Category` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `SYNCED`, `PENDING_UPLOAD`, `PENDING_DELETE` to the rest of the system?**
  _5 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TransactionFormScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06127549786342014 - nodes in this community are weakly interconnected._
- **Why does `Constants` connect `DashboardViewModelTest.kt` to `TransactionFormScreen.kt`, `LedgerDao`, `MainActivity.kt`, `Transaction`, `TransactionDaoTest`, `CurrencyModel`, `Transaction`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `AppResult` (e.g. with `God Nodes (most connected - your core abstractions)` and `Suggested Questions`) actually correct?**
  _`AppResult` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `LedgerDao` be split into smaller, more focused modules?**
  _Cohesion score 0.0578386605783866 - nodes in this community are weakly interconnected._