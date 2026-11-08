# Graph Report - ExpenseTracker  (2026-11-08)

## Corpus Check
- 95 files · ~24,974 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 59 file(s) not represented in the graph (top: .xml 49, (none) 3, .properties 3)
- WARNING: 59 of 174 files seen (34%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 910 nodes · 2473 edges · 49 communities (23 shown, 26 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 61 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `78cb5eb1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TransactionFormScreen.kt
- LedgerDao
- DashboardViewModelTest.kt
- MainActivity.kt
- DateUtils
- Category
- Expense Tracker
- AppStartupViewModel.kt
- AppResult
- DashboardViewModel
- AmountInput.kt
- with
- safeCall
- Transaction
- UiError
- CategoryDaoTest.kt
- AddEditTransactionViewModel
- TransactionDaoTest
- LedgerDaoTest.kt
- Constants
- steps
- UserPreferences
- TransactionRepositoryImpl
- TransactionRepositoryTest.kt
- PreferencesModule.kt
- RepositoryModule.kt
- CurrencyHelper
- CurrencyModel
- TransactionWithCategory
- TransactionRepository
- DateRangeFilter
- TransactionDetailDialog
- BaseViewModel
- ExampleInstrumentedTest.kt
- CategoryRepository
- DashboardScreen
- BalanceCard
- DashboardTopBar
- FilterChipRow
- SummaryCard
- TypeFilterRow
- ExampleUnitTest.kt
- PULL_REQUEST_TEMPLATE.md

## God Nodes (most connected - your core abstractions)
1. `AppResult` - 53 edges
2. `Category` - 51 edges
3. `ExpenseTrackerTheme()` - 48 edges
4. `Ledger` - 40 edges
5. `Transaction` - 40 edges
6. `DashboardViewModel` - 32 edges
7. `Constants` - 28 edges
8. `LedgerDao` - 28 edges
9. `AddEditTransactionViewModel` - 28 edges
10. `AppDatabase` - 25 edges

## Surprising Connections (you probably didn't know these)
- `Key Design Decisions` --references--> `Ledger`  [INFERRED]
  README.md → app/src/main/java/com/example/expensetracker/data/local/entity/Ledger.kt
- `Current (v0.1) — Complete` --references--> `UiError`  [INFERRED]
  README.md → app/src/main/java/com/example/expensetracker/domain/model/UiError.kt
- `Testing Strategy` --references--> `LedgerDaoTest`  [INFERRED]
  README.md → app/src/androidTest/java/com/example/expensetracker/data/local/dao/LedgerDaoTest.kt
- `Testing Strategy` --references--> `TransactionDaoTest`  [INFERRED]
  README.md → app/src/androidTest/java/com/example/expensetracker/data/local/dao/TransactionDaoTest.kt
- `Testing Strategy` --references--> `CategoryDao`  [INFERRED]
  README.md → app/src/main/java/com/example/expensetracker/data/local/dao/CategoryDao.kt

## Import Cycles
- None detected.

## Communities (49 total, 26 thin omitted)

### Community 0 - "TransactionFormScreen.kt"
Cohesion: 0.08
Nodes (3): getCategoryIcon(), ExtendedColors, ExpenseTrackerTheme()

### Community 1 - "LedgerDao"
Cohesion: 0.05
Nodes (6): LedgerDao, MonthlySnapshotDao, TransactionDao, AppDatabase, MonthlySnapshot, DatabaseModule

### Community 2 - "DashboardViewModelTest.kt"
Cohesion: 0.07
Nodes (4): FakeCategoryRepository, DashboardViewModelTest, SettingsViewModelTest, MainDispatcherRule

### Community 3 - "MainActivity.kt"
Cohesion: 0.06
Nodes (11): MainActivity, NavGraph(), SettingsScreen(), SettingsScreenPreview(), SettingsSectionCard(), ThemeModeSelector(), ThemeModeSelectorPreview(), SplashOverlay() (+3 more)

### Community 4 - "DateUtils"
Cohesion: 0.09
Nodes (6): DateUtils, CustomDateRangePickerDialog(), SelectableDates, DatePickerField(), DatePickerFieldPreview(), DateUtilsTest

### Community 5 - "Category"
Cohesion: 0.08
Nodes (12): CategoryDao, Category, CategoryCheckboxRow(), CategoryFilterBottomSheet(), CategoryFilterBottomSheetPreview(), AddEditTransactionUiState, CategoryChip(), CategoryChipPreview() (+4 more)

### Community 6 - "Expense Tracker"
Cohesion: 0.07
Nodes (28): 1. Find or Pick an Issue, 2. Fork the Repository, 3. Clone Your Fork, 4. Checkout to `hacktoberfest`, 5. Do Your Changes, 6. Make Sure It Builds Successfully, 7. Push Your Changes, 8. Make a Pull Request (PR) (+20 more)

### Community 7 - "AppStartupViewModel.kt"
Cohesion: 0.11
Nodes (4): CategorySeeder, LedgerSeeder, ExpenseTrackerApp, AppStartupViewModel

### Community 8 - "AppResult"
Cohesion: 0.16
Nodes (4): Ledger, LedgerRepository, AppResult, FakeLedgerRepository

### Community 10 - "AmountInput.kt"
Cohesion: 0.11
Nodes (9): AmountInput(), AmountInputPreview(), CategoryDialog(), CategoryDialogPreview(), IconPickerGrid(), TransactionFormScreen(), TransactionFormScreenPreview(), TypeToggle() (+1 more)

### Community 11 - "with"
Cohesion: 0.08
Nodes (24): name, runs-on, steps, concurrency, cancel-in-progress, group, jobs, build-and-lint (+16 more)

### Community 12 - "safeCall"
Cohesion: 0.13
Nodes (7): CategoryRepositoryImpl, LedgerRepositoryImpl, safeCall(), Success, toUiError(), Database, Testing Strategy

### Community 14 - "UiError"
Cohesion: 0.14
Nodes (8): Error, Network, NotFound, UiError, Unknown, Validation, SettingsUiState, SettingsViewModel

### Community 19 - "Constants"
Cohesion: 0.13
Nodes (12): Constants, Database, NavArgs, Preferences, Search, ThemeMode, TransactionType, AddTransaction (+4 more)

### Community 20 - "steps"
Cohesion: 0.13
Nodes (14): jobs, validate-branch, name, on, pull_request, types, name, run (+6 more)

### Community 27 - "CurrencyModel"
Cohesion: 0.29
Nodes (4): CurrencyModel, CurrencyPickerDialog(), CurrencyPickerDialogPreview(), CurrencyRow()

### Community 28 - "TransactionWithCategory"
Cohesion: 0.31
Nodes (4): TransactionWithCategory, DeleteConfirmationDialog(), TransactionItem(), TransactionItemPreview()

### Community 30 - "DateRangeFilter"
Cohesion: 0.25
Nodes (7): Custom, DateRangeFilter, ThisMonth, ThisWeek, ThisYear, Today, DashboardUiState

### Community 31 - "TransactionDetailDialog"
Cohesion: 0.31
Nodes (6): DetailRow(), TransactionDetailDialog(), TransactionDetailDialogPreview(), formatTransactionDate(), isSameDay(), isYesterday()

### Community 32 - "BaseViewModel"
Cohesion: 0.29
Nodes (3): BaseViewModel, Architecture, Architecture Principles

### Community 36 - "DashboardScreen"
Cohesion: 0.33
Nodes (3): DashboardScreen(), EmptyTransactionState(), EmptyTransactionStatePreview()

### Community 39 - "FilterChipRow"
Cohesion: 0.53
Nodes (3): FilterChipRow(), FilterChipRowPreview(), StyledFilterChip()

### Community 44 - "PULL_REQUEST_TEMPLATE.md"
Cohesion: 0.50
Nodes (3): Changes Made, Checklist, Description

## Knowledge Gaps
- **7 isolated node(s):** `Today`, `ThisWeek`, `ThisMonth`, `ThisYear`, `Validation` (+2 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 168 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **26 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Constants` connect `Constants` to `TransactionFormScreen.kt`, `LedgerDao`, `AddEditTransactionViewModel.kt`, `MainActivity.kt`, `DashboardViewModelTest.kt`, `DashboardViewModel`, `UiError`, `CategoryDaoTest.kt`, `TransactionDaoTest`, `UserPreferences`, `TransactionRepositoryTest.kt`?**
  _High betweenness centrality (0.150) - this node is a cross-community bridge._
- **What connects `Today`, `ThisWeek`, `ThisMonth` to the rest of the system?**
  _7 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TransactionFormScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.07877903714510157 - nodes in this community are weakly interconnected._
- **Why does `Category` connect `Category` to `TransactionFormScreen.kt`, `LedgerDao`, `AddEditTransactionViewModel.kt`, `CategoryRepository`, `DashboardViewModelTest.kt`, `AppStartupViewModel.kt`, `AmountInput.kt`, `safeCall`, `Transaction`, `CategoryDaoTest.kt`, `AddEditTransactionViewModel`, `DateRangeFilter`?**
  _High betweenness centrality (0.149) - this node is a cross-community bridge._
- **Should `LedgerDao` be split into smaller, more focused modules?**
  _Cohesion score 0.054527750730282376 - nodes in this community are weakly interconnected._
- **Why does `Transaction` connect `Transaction` to `LedgerDao`, `AddEditTransactionViewModel.kt`, `Category`, `AppResult`, `DashboardViewModel`, `AddEditTransactionViewModel`, `TransactionDaoTest`, `TransactionRepositoryImpl`, `TransactionRepositoryTest.kt`, `TransactionRepository`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Should `DashboardViewModelTest.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06862745098039216 - nodes in this community are weakly interconnected._