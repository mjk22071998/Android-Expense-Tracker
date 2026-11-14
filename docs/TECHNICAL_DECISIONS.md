# Technical Decisions

What was decided in this project, why, what was rejected, and what each choice costs.
Originally compiled 2026-11-07 (commit `ad47c7d`); fully updated and expanded to commit `708c94a` (v0.3) covering all architectural patterns, the Organic Forest UI redesign, native PDF export engine, multi-tier stress hardening, standalone offline lockdown, and forward visual analytics architecture.

**How to read this.** Each decision has a short rationale following the format: Title, **Why**, **Rejected**, and **Cost / Trade-off**. Where an earlier decision or claim was later found to be wrong or required refinement, it is recorded in Section 17 rather than silently rewritten. Section 18 lists closed gaps and remaining open decisions. Statements marked "verified" were executed or verified against source code.

---

## 1. Platform and build

### D1. Native Android, Kotlin, Jetpack Compose, Material 3, single module
**Why:** Native Android was the requirement. Compose is the current UI toolkit and removes the XML, adapter and ViewHolder layer. One `app` module keeps the build simple.  
**Rejected:** XML views (legacy). Multi-module (premature for one developer; the packages already follow layer boundaries, so it can be split later).  
**Cost:** Compose and its libraries move quickly. Several decisions below exist because an API was deprecated or behaved unexpectedly.

### D2. Version catalog and KSP
All versions live in `gradle/libs.versions.toml`. Room and Hilt run through KSP, not kapt.  
**Why:** One source of truth; KSP is the supported path on current Kotlin.  
**Cost:** A TOML syntax error fails the whole build with an unhelpful message (a missing closing quote did exactly this). Compose artifacts declared without a version depend on the BOM, and every configuration needs its own `platform(bom)` line; `androidTestImplementation` does not inherit the one under `implementation`.

### D3. minSdk 24, target and compile SDK 37
**Why:** Keeps Android 7+ and older budget devices supported while targeting the current API.

### D4. Hilt, with `jakarta.inject` annotations
**Why:** The ecosystem is moving from `javax.inject` to `jakarta.inject`; Dagger supports it from 2.55 and the project is on 2.60.1. Checked against the release notes, not assumed. `@param:ApplicationContext` is used because the Kotlin compiler warns that the default annotation target is changing.  
**Cost:** Older tutorials use `javax`. Both compile.

### D5. One Activity, Navigation Compose, sealed `Screen` routes
Argument keys live in `Constants`. The edit screen gets its transaction id through `SavedStateHandle`, so the composable does not pass it into the ViewModel by hand.  
**Why:** The Compose-standard model. Intents remain only for crossing app boundaries.  
**Cost:** Routes are strings, so a typo is a runtime error, not a compile error.

### D6. Constants in one file, nested by concern
`Constants.NavArgs`, `TransactionType`, `ThemeMode`, `Preferences`, `Search`, `Database`.  
**Why:** Requested so a key changes in one place. Nesting keeps related values together.

---

## 2. Architecture

### D7. Clean Architecture with MVVM, as packages inside one module
`data/local`, `data/repository`, `domain/model`, `presentation/*`, `di`.  
**Why:** The chain UI -> ViewModel -> Repository -> Room lets the data source change (for example to add cloud sync) without touching UI code, and makes each layer testable.  
**Rejected:** A use-case layer (indirection with no payoff at this size).  
**Cost:** More files than a small app strictly needs. Accepted knowingly.

### D8. Repository = interface + implementation, bound with `@Binds`
**Why:** (1) ViewModels are unit-tested against hand-written Fakes; (2) a different implementation can replace the local one without changing ViewModels or UI.  
**Cost:** Two files per repository. Every signature change must be mirrored in the implementation and in the test Fake. This broke the build several times.

### D9. One immutable `UiState` data class per screen, exposed as `StateFlow`
The UI renders state and forwards events. Purely visual state (menu open, dialog shown) stays in `remember` inside the composable.  
**Why:** A screen holds several independent pieces (list, totals, filters, dialogs). `copy()` updates one without a state machine.  
**Rejected:** Sealed Loading/Success/Error per screen (fits parallel data badly); LiveData.

### D10. Flow and suspend rules
DAOs and repositories expose cold `Flow` for observed data and `suspend` for one-shot work. A function returning `Flow` must not be `suspend`; a one-shot DAO call must be.  
**Why:** Room dispatches `Flow` itself. A plain non-suspend DAO call can run on the main thread and throws at runtime (this was a real bug, see section 17).

---

## 3. Data layer

### D11. Room is the single source of truth; writes go to Room first
**Why:** The app must be fully usable offline, and any future sync can be a background mirror that never blocks the UI.

### D12. Primary keys: UUID for user data, fixed strings for seeded data
User transactions, ledgers and custom categories use `UUID.randomUUID()`. Seeded categories and the default ledger use fixed ids such as `cat_food` and `ledger_default`.  
**Why:** UUIDs cannot collide across devices. Fixed ids make seeding idempotent with `OnConflictStrategy.IGNORE`.  
**Rejected:** Auto-increment (two offline devices would both create id 5).

### D13. Timestamps as epoch milliseconds (`Long`)
**Why:** Range queries are plain `BETWEEN`.  
**Cost:** Every date picker and boundary needs explicit time-zone handling (section 5).

### D14. Amounts stored as `Double` (implicit decision, never debated)
Recorded for honesty. Floating point can drift in the last decimal places. Storing integer minor units (paisa, cents) is the rigorous alternative. See section 18.

### D15. Soft delete
Transactions are flagged `isDeleted`, never removed; every read filters `isDeleted = 0`.  
**Why:** A deletion has to be representable as data to be synchronised later, and it makes undo possible.  
**Cost:** One extra condition on every query.

### D16. Foreign key actions
Ledger -> transactions: `CASCADE`. Category -> transactions: `SET_NULL`, with a nullable `categoryId`; the list query uses `LEFT JOIN` plus `COALESCE` to show "Uncategorized".  
**How it was reached:** `RESTRICT` was proposed first, then changed to `CASCADE` by mistake, then reasoned back. `SET_DEFAULT` was rejected because it needs a guaranteed existing default row.  
**Why `SET_NULL`:** Deleting a custom category never destroys or blocks the user's transactions.

### D17. Running balance on the ledger
`balance`, `totalIncome` and `totalExpenses` are updated on every write instead of being recomputed by scanning transactions.  
**Why:** O(1) balance reads, requested as the scalable approach.  
**Cost:** The totals must never drift from the rows (D19 exists to guarantee that).

### D18. Indices
Single-column indices on `ledgerId`, `categoryId`, `type`, `date`, `isDeleted`, plus a composite index on `(ledgerId, type, date, isDeleted)` (verified in `Transaction.kt`). Note: the original README described the order as `(ledgerId, type, isDeleted, date)`; this documentation error was resolved in commit `9a47b76`.

### D19. Multi-table writes are atomic
Insert, edit and soft-delete each run inside one Room `@Transaction` that updates the transaction row, the ledger totals and the monthly snapshot together. Edits reverse the old transaction's effect and then apply the new one.  
**Why:** A half-applied change would leave the balance wrong. The first version updated only the new values on edit and let the balance drift (a real bug).  
**Cost:** The DAO takes other DAOs as parameters.

### D20. Monthly snapshots are maintained but not displayed on the main dashboard
`monthly_snapshots` (opening and closing balance per ledger and month) is updated on every write, but the primary Dashboard does not show it.  
**Why kept:** The data is cheap to maintain and reserved for monthly financial reporting and visual analytics. It was removed from the primary balance card because it cost space and made the card wrap on small screens.  
**Hardened:** Missing monthly snapshots are now created dynamically with UUID keys when backdated entries arrive (see Section 17).

### D21. Seeding
`CategorySeeder` and `LedgerSeeder` insert defaults on launch with fixed ids and `IGNORE`. The intended owner is `AppStartupViewModel`, which gates the UI behind seeding. The default ledger was originally missed, which caused a foreign key crash on the first save.

### D22. Database versioning
`exportSchema = true`, explicit migrations (`MIGRATION_1_2` added the snapshot table), no `fallbackToDestructiveMigration` (verified absent).  
**Why:** User data must survive upgrades.

### D23. One parameterised query implements every list filter
`getTransactions` takes a date range, search text, an optional type and a set of category ids. Each optional filter is written so "empty" means "do not filter":  
`:searchQuery = ''`, `:typeFilter IS NULL`, and `:categoryFilterEmpty = 1 OR categoryId IN (...)`.  
**Why:** All filtering stays in SQL as originally specified, and there is no combinatorial set of DAO functions. `IN (...)` cannot be skipped inline, so the repository passes a separate boolean computed from the same set.  
**Trade-offs:** Search is a substring match on the text form of the amount (searching `50` also matches `150.0` and `50.25`). Leading-wildcard `LIKE` cannot use an index, which is fine at this scale.

### D24. Preferences in DataStore, not SharedPreferences
Currency code and theme mode.  
**Why:** Coroutine-safe and the current platform recommendation. Theme is a three-state string (`SYSTEM`, `LIGHT`, `DARK`), because a boolean cannot express "follow the system", and it is applied live without an app restart.

---

## 4. Error handling

### D25. `AppResult<T>` and `UiError`
Repository write functions return `AppResult` (Success or Error) through `safeCall`. `UiError` is a sealed class (Database, NotFound, Validation, Network, Unknown) where every case carries a user-readable message.  
**Why:** Failures become data the ViewModel must handle, and raw exception text such as SQL fragments never reaches the user.  
**Detail that matters:** `safeCall` rethrows `CancellationException` before its generic catch. Swallowing it would break coroutine cancellation everywhere.  
**Trade-off:** Exceptions are classified by class name so `domain/model` stays free of Android imports. That is fragile to renames; the foreign-key case is covered by an automated test.

### D26. `Flow` reads are not wrapped; ViewModels use `.catch`
A stream emits many times, so wrapping each emission would force every collector to unwrap a result forever. `.catch { }` on the collecting side handles the rare stream failure.

### D27. `BaseViewModel.launchSafely` as the backstop
It attaches a `CoroutineExceptionHandler`, so an unexpected exception becomes an error state instead of crashing the process. It returns the launched `Job` so callers can cancel a previous collection (needed by D37).

### D28. Errors surface as a Snackbar, then clear
Each screen runs `LaunchedEffect(uiState.error)`, shows the message and calls `clearError()` so the same error can show again later. Error display and navigation stay in separate effects so a suspending snackbar cannot delay navigation.  
**Related fix:** `isSaved` is set only on `AppResult.Success`. Before, the screen navigated back even when the save had failed.

---

## 5. Dates, time and currency

### D29. Pickers speak UTC; storage speaks local time
Material 3 `DatePicker` and `DateRangePicker` represent a day as UTC midnight of that day, whatever the device zone. `DateUtils` converts in both directions. Ranges are built from local day boundaries, with the end at 23:59:59.999.  
**Why:** Without this the custom range dropped the last day (UTC midnight is 05:00 in Pakistan, so most of that day fell after the boundary), and the single-date picker could be off by one day. Both were reproduced and fixed.

### D30. Period ranges are whole periods, computed without `set(DAY_OF_WEEK)`
`Today`, `thisWeekRange` (Monday 00:00:00.000 to Sunday 23:59:59.999), `thisMonthRange` and `thisYearRange` live in `DateUtils`. The week start is found by counting back from today.  
**Why:** `Calendar.set(DAY_OF_WEEK, MONDAY)` jumps forward on a Sunday in Sunday-first locales (verified by running it), and an unzeroed millisecond field excluded transactions stored at exact midnight. Whole periods also remove a dependency on when the query last ran.  
**Consequence:** Future-dated entries appear in the period they belong to, matching how the balance already counts them.

### D31. Currency detection from the SIM country
`TelephonyManager.simCountryIso` first, then device locale, then `PKR`. The symbol comes from `Currency.getSymbol(locale)` using the SIM-derived locale.  
**Why:** `Locale.getDefault()` follows the display language, so an English-language device in Pakistan returned a pound or dollar and showed `PKR` as a code instead of a symbol.  
**Rejected:** Third-party currency libraries (new and unproven, or old and Java-only); IP lookup (needs internet, which the app must not require).

### D32. Searchable currency picker over the platform list
`Currency.getAvailableCurrencies()` filtered by code and name; zero extra dependencies.

### D33. Locale reads inside composables use `LocalLocale`
Lint rule `NonObservableLocale` flags `Locale.getDefault()` in composables because it does not recompose on a locale change. Fixed in `DatePickerField`. Non-composable helpers still use `Locale.getDefault()`.

---

## 6. Dashboard behaviour

### D34. Balance is all-time; Income and Expense cards follow the date range only
**Why:** Requested: "balance will be overall, but expenses and income will be of current month". The cards use the period queries (`getPeriodIncome`, `getPeriodExpense`) combined into one flow. They deliberately ignore search, type and category, so they stay a stable summary of the period while the list narrows.

### D35. Date chips: Today, This Week, This Month (default), This Year, Custom
"Last 30 Days" was removed because it overlapped "This Month". The Custom chip opens a range picker that defaults to today, blocks future dates and applies on confirmation. Selected chips show a check icon and unselected chips show none, applied uniformly.

### D36. Search lives in the top bar
Tapping the search icon turns the bar into a focused search field. Closing it clears the query. The back button closes search first (`BackHandler`, enabled only while search is active, otherwise back would never leave the screen). Search combines with the active date filter and never replaces it.  
**Debounce design:** Two copies of the query exist on purpose. One updates on every keystroke so typing never lags; the other is debounced 300 ms (`Constants.Search`) and is the only one that triggers a database query.

### D37. Only one listener per query is ever active
`loadTransactions` and `loadPeriodTotals` keep their `Job` and cancel it before starting a new one. Before this, each filter change left the old collector running, and whichever finished last could put a stale list on screen.

### D38. Type filter: two toggle chips, nullable state
"Income Only" and "Expense Only"; neither selected means both. Tapping the selected chip clears it.  
**Why not three options:** "All" is just the absence of a filter.

### D39. Category filter: bottom sheet with checkboxes and a badge
Categories are grouped Expense and Income. The top-bar filter icon shows the number of active categories. Checkbox taps change local state only; **Apply** runs the query, while **Clear** runs it immediately.  
**Why a sheet, not a chip row:** Categories are many, can grow (custom categories), and need multi-select; another permanent row would crowd an already full header. The badge doubles as the active-filter indicator.

---

## 7. UI and design

### D40. Brand identity over default Material 3
Static color schemes (no dynamic colour), gradient hero cards with decorative circles, larger bold numerals, rounded surfaces. The default look was judged too plain; Material 3 structure, spacing and components are kept so it still feels native. Dynamic colour was rejected so the app looks the same on every device.

### D41. Colour carries one meaning everywhere
Red and green appear only as small accents (list borders, amounts, icon tints). The Add/Edit screen is not themed red or green by transaction type.  
**Why:** Red reads as danger and green as success; applying them to a routine action makes the colour contradict the user's intent.

### D42. Icons are individual Material Symbols vector drawables
`material-icons-extended` was dropped: it is deprecated upstream and adds a large library for a handful of icons. Icons are tinted from the theme in code, so the colour inside the XML is irrelevant, and APK size dropped significantly.

### D43. Two-layer startup
The native AndroidX SplashScreen covers the gap before Compose exists. A Compose overlay then covers database seeding, gated on `isReady` with a 600 ms minimum so it never flashes. Without the gate the Dashboard briefly rendered with no data.

### D44. Dashboard layout
Balance card, summary cards and filter rows are a fixed header; only the transaction list scrolls. Tapping a transaction opens a detail dialog. Each item has an overflow menu (Edit, Delete) with a delete confirmation.

### D45. Add/Edit transaction
Segmented Expense/Income toggle, amount as the hero element, category chips filtered by type, a note, and a date. Save stays disabled until the amount is positive and a category is chosen.

### D46. Custom categories are created inline
The category row ends with an "Add" chip that opens a dialog with a name and an icon from a curated grid. Icons are stored as string keys mapped in `getCategoryIcon`.  
**Why not long-press:** Nothing on screen hints at it. Instead, only custom categories show an overflow icon (Edit, Delete); seeded ones show none, which also tells the user which are locked. Deleting a category that is currently selected clears the selection.  
**Why a curated grid:** Consistent style, and emoji were ruled out as a preference.

### D47. Currency and theme live in Settings
Searchable currency dialog; System, Light or Dark segmented control. Changes apply immediately.

---

## 8. Testing

### D48. Three layers, each where it runs
- **ViewModel unit tests** (`src/test`, plain JVM): fast, no emulator, use Fakes.
- **DAO and repository integration tests** (`src/androidTest`): real Room and real SQLite on an in-memory database; they need an actual Android runtime.
- **Pure logic tests** (`DateUtilsTest`): added for date-boundary guarantees.  
**Why:** A Fake can prove ViewModel logic but cannot prove that SQL does what we claim. Tests were physically verified on a real device (OPPO A12).

### D49. Fakes by default, MockK for classes without an interface
Repositories are interfaces, so they get small hand-written Fakes with `TODO()` for methods a test does not use (a loud failure if a path is reached unexpectedly) plus helpers to push values into their flows. `UserPreferences` and `Context` are concrete, so they use MockK.

### D50. `MainDispatcherRule` with an unconfined dispatcher
Replaces `Dispatchers.Main` so coroutines run eagerly and tests read `uiState.value` directly.  
**Trade-off:** Unconfined execution can hide ordering bugs a standard dispatcher would expose.

### D51. Base coverage
Settings and Dashboard ViewModels; Ledger, Transaction and Category DAOs (including the `isDefault` delete guard, idempotent insert, and balance reversal on edit and delete); a repository test where a real foreign-key violation must come back as `UiError.Database`; date range boundaries including Sunday and leap days.

---

## 9. Process, CI and repository

### D52. Lint before every push
Run `./gradlew lint` locally. It caught `NonObservableLocale` before it reached the repo.

### D53. CI execution scope
Originally configured to run `lint` and `assembleDebug`. Unit tests were initially deferred from CI due to build time concerns (see Section 15 for the modern streamlined CI architecture).

### D54. Hacktoberfest scoping
The public README listed only current work; the long-term roadmap was moved to a git-ignored `ROADMAP.md`. Pull requests were initially required to target the `hacktoberfest` branch, enforced by `validate-branch.yml` (superseded in Section 15).

### D55. Generated documents avoid emoji
A standing preference, because they read as machine-written.

### D56. Verify, then assert
Library facts are checked against official docs or release notes before being relied on. This prevented mistakes and exposed others (section 17).

---

## 10. Architecture patterns and structural modernization (Post-ad47c7d)

### D57. Strategy Pattern for period date filtering (`DateRangeFilter`)
Replaced rigid `when/if` branching with a polymorphic `DateRangeFilter` sealed class hierarchy (`AllTime`, `Today`, `ThisWeek`, `ThisMonth`, `ThisYear`, `Custom`). Each strategy encapsulates its own time boundary calculation via `calculateRange(now: Long)`.  
**Why:** Satisfies the Open/Closed Principle. Adding future periods (such as fiscal quarters or rolling 90 days) requires zero modifications to ViewModels, DAOs, or UI layouts.  
**Rejected:** Hardcoded `when` expressions scattered across repository and presentation layers.  
**Cost / Trade-off:** More class declarations than a raw enum with static utility functions.

### D58. Unbounded epoch boundary for `AllTime` strategy (`0L..Long.MAX_VALUE`)
Instead of creating a dedicated `getAllTransactions()` query in DAOs and repositories, the `AllTime` strategy defines its range as `0L..Long.MAX_VALUE`.  
**Why:** Allows a single parameterized SQL query (`getTransactions`) to handle all time filters uniformly without duplicate DAO query methods or conditional SQL generation.  
**Rejected:** Nullable date bounds in SQL (which complicate indices and Room query parsing).  
**Cost / Trade-off:** Assumes transaction timestamps will never be negative or exceed `Long.MAX_VALUE`.

### D59. Kotlin `data object` for zero-allocation stateless types
Modernized singleton instances (`Screen.Dashboard`, `Screen.Settings`, `DateRangeFilter.Today`, `DateRangeFilter.ThisWeek`, etc.) to Kotlin 1.9+ `data object`.  
**Why:** Eliminates repetitive boilerplate, guarantees proper `toString()` formatting for logging, and prevents redundant memory allocations compared to parameterless data classes.  
**Rejected:** Plain `object` (lacks clean `toString()` output for debugging) and empty `data class`.

### D60. Native edge-to-edge display architecture
Enforced `enableEdgeToEdge()` in `MainActivity` with systematic `WindowInsets` and `systemBarsPadding()` handling across all composables.  
**Why:** Conforms to modern Android UX standards by drawing content seamlessly behind transparent system status and navigation bars.  
**Rejected:** Legacy window flags (`FLAG_LAYOUT_NO_LIMITS`) and hardcoded status bar offsets, which produce visual glitches across different Android versions and device notches.  
**Cost / Trade-off:** Every full-bleed screen must explicitly manage padding to avoid clipping against display cutouts and navigation gestures.

---

## 11. Luxury theming, visual language and aesthetics ("Organic Forest")

### D61. Organic Forest luxury palette over default Material 3
Replaced the generic purple/blue theme (D40) with a bespoke "Organic Forest" fintech design system:
- **Dark mode:** Radiant vertical canopy gradient (`#2E633B` to `#122E1B` to `#06110A`), emerald green inflow (`#10B981`), crimson rose outflow (`#F43F5E`), neon electric lime accents (`#CCFF00`), and dark forest card surfaces (`#183B22`).
- **Light mode:** Crisp sage/mint background surfaces (`#F4FBF4`, `#E8F5E9`), deep forest typography (`#0A2612`), and high-contrast dark card containers.  
**Why:** Personal finance applications require visual warmth, high contrast, and clear differentiation from generic boilerplate apps.  
**Rejected:** Default Material 3 styling (felt sterile) and dynamic theming via Material You (creates inconsistent brand identity across different OEM skins).  
**Cost / Trade-off:** Requires maintaining bespoke color tokens across both light and dark themes.

### D62. Calming semantic palette for transaction logging (Sky Blue and Electric Lime over Red)
On the Add/Edit transaction form, replaced aggressive alert-red tones with calm Sky Blue (`#38BDF8`) and energetic Electric Lime (`#A3E635`) for active selection states.  
**Why:** Budgeting and expense logging should feel encouraging and routine, not punitive or anxiety-inducing. Alarm-red is strictly reserved for outflow currency amounts and destructive actions (such as delete confirmations).  
**Rejected:** Painting expense entry fields red.  
**Cost / Trade-off:** Requires consistent design discipline to prevent red from creeping back into input controls.

### D63. Bespoke handcrafted vector iconography and native splash alignment
Designed a custom vector launcher icon (`ic_launcher.xml`, `ic_launcher_round.xml`) depicting a leather-stitched organic forest wallet with neon lime accents. Aligned AndroidX `SplashScreen` (`Theme.SplashScreen`) to use the exact dark forest background (`#0D1F12`) and balanced viewport margins.  
**Why:** Eliminates the harsh white flash and icon jump during cold app launch, establishing visual continuity from OS home screen to Compose UI.  
**Rejected:** Standard template icons and mismatched splash drawables.  
**Cost / Trade-off:** Vector XML assets must be tuned and tested across display densities.

### D64. Dedicated Financial Overview screen with filter inheritance
Created a dedicated Financial Overview sheet/screen accessible directly from the hero balance card. It displays aggregate metrics (net cash flow, average daily spend, month-over-month trend) that dynamically inherit the active date filter.  
**Why:** Provides users with executive situational awareness without cluttering the primary dashboard list view.  
**Rejected:** Crowding the primary dashboard screen with too many summary widgets.  
**Cost / Trade-off:** An additional screen and navigation route to maintain.

---

## 12. Currency, formatting and accessibility

### D65. Colloquial regional currency resolution (`CurrencyHelper`)
Standard Java `Currency.getSymbol(locale)` often returns ISO codes instead of familiar symbols depending on the phone's language (for example, displaying `PKR 2,500` instead of `Rs 2,500` on an English-US locale phone in Pakistan). Created `CurrencyHelper` which prioritizes recognized colloquial symbols (`Rs` for PKR, `$` for USD, `₹` for INR, `€` for EUR, `£` for GBP) before falling back to `Currency.getInstance(code).symbol` and finally the raw ISO code.  
**Why:** Financial amounts must feel native and intuitive to local users regardless of their phone's operating system language.  
**Rejected:** A massive hardcoded table for all 150+ currencies, or heavy third-party ICU dependencies.  
**Cost / Trade-off:** Currencies outside the curated colloquial map fall back to standard Java symbols, which may still display codes on certain OEM locales.

### D66. Centered contextual tooltips for interactive UI elements
Integrated accessible Material 3 tooltips (`AppTooltip`) on all dashboard, form, and navigation icon buttons, configured with centered horizontal alignment and subtle display delays.  
**Why:** Textless icon buttons fail WCAG accessibility and usability standards without visual text tooltips on hover or long-press.  
**Rejected:** Unannotated icon buttons or relying solely on screen-reader `contentDescription` (which aids blind users but leaves sighted users guessing).  
**Cost / Trade-off:** More verbose composable markup.

---

## 13. Data export and storage architecture

### D67. Native hardware-accelerated ISO A4 PDF generation (`PdfDocument`)
Implemented zero-dependency, on-device statement generation in `PdfExportHelper` using Android's native `android.graphics.pdf.PdfDocument`. Generates standard ISO A4 pages (595 x 842 pt) containing an executive financial summary (inflow, outflow, net flow), category distribution, and an itemized ledger table with automatic multi-page pagination.  
**Why:** PDF export was the most requested reporting feature. Native `PdfDocument` generates clean, hardware-accelerated documents with zero external libraries.  
**Rejected:** Third-party PDF libraries (iText, PDFBox, Apache FOP, MuPDF). These libraries add 5 to 15 MB of APK bloat, introduce native NDK dependencies, or carry restrictive commercial/AGPL licenses.  
**Cost / Trade-off:** Text measurement, line wrapping, tabular pagination, and canvas rendering had to be calculated manually in Kotlin.

### D68. Dual-path export architecture (Direct storage save and system share)
Implemented two distinct user export actions:
1. **Save to Storage:** Writes directly to the public `Documents/ExpenseTracker/` folder (via `MediaStore` on API 29+ or direct file path on legacy Android), supporting both internal storage and removable SD cards.
2. **Share PDF:** Saves the file to secure app cache and launches the Android system share sheet via `FileProvider`.  
**Why:** Users have two distinct intents: saving statements locally for permanent personal records, or sending them immediately to an accountant or family member via WhatsApp/email.  
**Rejected:** Forcing users to share via an external app just to save a file, or conversely forcing them to locate saved files in a file manager just to share.  
**Cost / Trade-off:** Two distinct export flows and URI handling mechanisms to maintain.

### D69. Proactive dual-tier storage permission architecture
Android storage permissions are fragmented. Android 9 (API 28) and lower require `WRITE_EXTERNAL_STORAGE` to write public files; Android 10+ (API 29+) uses Scoped Storage / MediaStore and rejects legacy permission requests. Implemented proactive permission branching: requests permission only on API <= 28 with an explanatory user dialog, and bypasses permission prompts entirely on API 29+.  
**Why:** Prevents security exceptions on legacy devices while avoiding unnecessary and rejected permission prompts on modern Android releases.  
**Rejected:** Monolithic permission requests across all Android versions.  
**Cost / Trade-off:** Runtime branching based on `Build.VERSION.SDK_INT`.

### D70. Secure file sharing via `FileProvider` with explicit `ClipData` grants
Configured AndroidX `FileProvider` with `content://` URIs and explicitly attached URIs to `Intent.setClipData()` alongside `FLAG_GRANT_READ_URI_PERMISSION`.  
**Why:** Passing raw `file://` URIs crashes on Android 7+ via `FileUriExposedException`. Furthermore, certain OEM messaging and email apps (Gmail, WhatsApp, Samsung Email) ignore intent-level URI flags when accessing attachments, causing `SecurityException` or empty attachments unless `ClipData` is populated.  
**Cost / Trade-off:** Requires maintaining `res/xml/file_paths.xml` provider metadata.

---

## 14. Quality, stress testing and defensive hardening

### D71. Mathematical input hardening (`isFinite()`, negative guard, double validation)
Guarded all numeric parsing in `AddEditTransactionViewModel` with `isFinite()` checks, negative sign stripping, and strict `> 0.0` validation before enabling the Save action.  
**Why:** Floating-point edge cases (`Double.NaN`, `Double.POSITIVE_INFINITY`, `Double.NEGATIVE_INFINITY`) or accidental negative signs entered by users would corrupt database balances and cause unrecoverable math errors in running balance calculations.  
**Rejected:** Unchecked `toDoubleOrNull()` without bounds validation.  
**Cost / Trade-off:** Stricter input parsing logic in ViewModels.

### D72. Comprehensive multi-tier stress test suite (59 tests, 100% passing)
Implemented 5 specialized stress test suites:
1. `StressConcurrencyTest`: Concurrent multi-threaded read/write bursts against Room SQLite.
2. `ExtremeDateBoundaryStressTest`: Epoch 0, Year 2038/3000 boundaries, leap days, and millisecond edge cases.
3. `DataPaginationStressTest`: High-volume transaction sets (10,000+ records) and memory stability.
4. `TransactionRepositoryStressTest`: Rapid coroutine cancellation and atomic rollback integrity.
5. `DashboardViewModelStressTest`: Rapid concurrent filter switching and race condition elimination.  
**Result:** 59 out of 59 tests pass consistently in ~6 seconds.  
**Rejected:** Relying solely on manual QA or simple happy-path unit tests.  
**Cost / Trade-off:** Local test suite execution time increased from ~1s to ~6s.

### D73. Systematic resolution of audit findings (Commit `7531e53`)
Addressed 6 critical edge-case vulnerabilities identified during an architectural audit:
1. **System Theme Mode:** DataStore theme preference was not observing live system dark/light configuration changes; resolved using `isSystemInDarkTheme()`.
2. **Snapshot Date Updates:** Editing a transaction date across months failed to update snapshots when the target month row did not exist; fixed by generating missing snapshot rows with UUID primary keys.
3. **Ledger Validation:** Added safeguards ensuring the default ledger cannot be deleted or corrupted.
4. **DataStore Threading:** DataStore preference reads and writes were explicitly moved to `Dispatchers.IO`.
5. **Date Picker Contrast:** Date picker header and action text colors lacked contrast in dark theme; styled with high-contrast text tokens.
6. **Redundant Query Restarts:** Rapid filter chip tapping created query churn; debounced and deduped.

---

## 15. CI/CD, process and repository cleansing

### D74. CI/CD pipeline streamlining to build and unit tests
Streamlined `.github/workflows/android-ci.yml` to focus strictly on building and verifying code: executes `./gradlew testDebugUnitTest` followed by `./gradlew assembleDebug`. Removed redundant Gradle lint tasks, external CodeQL scans, and branch validation checks that caused flaky CI failures and 15+ minute build times.  
**Why:** CI must be fast, reliable, and provide immediate feedback on whether code compiles and passes tests. Lint remains a local pre-commit responsibility (D52).  
**Rejected:** Running full lint and static security scans on every minor CI push.  
**Cost / Trade-off:** Lint issues must be caught locally prior to push.

### D75. Gradle wrapper network hardening in CI
Configured `networkTimeout=120000`, `retries=5`, and `retryBackOffMs=2000` in `gradle/wrapper/gradle-wrapper.properties`.  
**Why:** GitHub Actions runners frequently suffered from `SocketTimeoutException: Read timed out` during the initial Gradle 9.8.0 wrapper download on default 10-second socket timeouts.  
**Rejected:** Committing the entire Gradle binary distribution into git.  
**Cost / Trade-off:** Builds may take up to 2 minutes to fail during genuine upstream CDN outages.

### D76. Post-Hacktoberfest cleansing and single mainstream branch
Deleted the obsolete `hacktoberfest` git branch and `validate-branch.yml` workflow. Cleansed `CONTRIBUTING.md`, `README.md`, and issue/PR templates of event-specific tags, restoring a standard open-source contribution workflow targeting `master`.  
**Why:** Keeps git history, repository configuration, and contributor onboarding clean and focused on permanent product goals.  
**Rejected:** Maintaining historical event branches indefinitely.

### D77. 100% standalone offline project lockdown
Scrubbed speculative cloud synchronization contracts (`SyncableEntity`, `SyncContracts`, etc.) to keep the codebase clean, unburdened by half-baked abstractions, and strictly offline. Confirmed zero network permissions in `AndroidManifest.xml`.  
**Why:** Clean Architecture often suffers from speculative scaffolding that remains untested and adds cognitive debt. The core app is strictly private, local-first, and offline.  
**Rejected:** Maintaining unused sync abstractions for hypothetical future features.

### D78. Explicit versioning and living roadmap (`ROADMAP.md`)
Bumped versioning in `app/build.gradle.kts` and `SettingsScreen.kt` to `versionCode = 3`, `versionName = "0.3"`. Converted `ROADMAP.md` into a living progress report tracking completed features, test coverage, and milestone roadmaps (v0.3 active, v1.0 offline production target, v1.1 companion services).  
**Cost / Trade-off:** Version bumps must be mirrored in both Gradle build configuration and UI settings.

### D79. Minimal zero-bloat offline source backup (`git archive`)
Standardized offline project archiving using `git archive` to generate `ExpenseTracker-source.zip` (1.52 MB). Includes all version-controlled source files, Gradle wrapper, and metadata needed to import directly into Android Studio and build immediately, while excluding build caches (`build/`, `.gradle/`), IDE caches (`.idea/`), and machine-specific configuration (`local.properties`). Added `*.zip` to `.gitignore`.  
**Rejected:** Zipping the workspace directory directly (which produces 200 to 500 MB archives containing machine-specific caches and binaries).  
**Cost / Trade-off:** Android Studio must perform a clean Gradle sync and download dependencies on first import.

---

## 16. Visual analytics and forward architecture (Planned / In-progress)

### D80. Native Jetpack Compose Canvas for visual analytics (Zero third-party chart dependencies)
Decided to build interactive spending charts (Weekly, Monthly, Yearly) directly using Jetpack Compose `Canvas` (`drawPath`, `drawCircle`, `drawText`, `PathEffect`).  
**Why:**
- **Zero bloat:** Eliminates 3 to 5 MB of external library bloat.
- **Theme continuity:** Enables pixel-perfect rendering of Organic Forest vertical canopy gradients and neon accent glow.
- **Hardware performance:** Ensures guaranteed 60fps rendering and instant touch response on budget devices (such as Android 9 / 3GB RAM devices).  
**Rejected:** MPAndroidChart (legacy View-based library, poor Compose interop, 3.5 MB footprint) and Vico / Compose-Charts (external dependencies with frequent breaking API changes).  
**Cost / Trade-off:** Coordinate scaling, Bézier curve smoothing, grid lines, and touch hitboxes must be calculated manually in Kotlin.

### D81. Visual continuity and flat balance carryforward on zero-activity days
In Weekly and Monthly charts showing daily data points:
- **Expenses & Income charts:** Days with no activity render `0.0`.
- **Balance chart:** Days with no activity carry forward the previous day's closing balance (flat step), reflecting actual accumulated wealth rather than artificially dropping to zero or skipping days.
- **Yearly chart:** Aggregates totals by calendar month (12 data points).  
**Why:** Skipping days produces distorted non-linear time axes; dropping balance to zero on inactive days misrepresents personal finances.  
**Cost / Trade-off:** Time-series pre-processing must fill missing date buckets before plotting.

### D82. Touch-scrubbing interactive crosshair with haptic feedback
Designed an interactive touch scrubber on the Compose Canvas: dragging across the graph renders a vertical indicator guideline, highlights the active data point, opens a floating badge with date and amount, and triggers a subtle haptic tick (`HapticFeedbackType.TextHandleMove`) on data point transitions. When the drag finishes (`onDragEnd` / `onDragCancel`), the scrubber immediately resets to `null`, returning the canvas to its clean baseline resting state. Discrete tap gestures retain inspection selection until tapped again.  
**Cost / Trade-off:** Requires pointer input handling via `detectHorizontalDragGestures` and `detectTapGestures` to prevent intercepting vertical scroll gestures of parent containers.

### D83. Decoupled server-side WhatsApp companion bot (Zero in-app chat bloat)
User feedback favored natural language logging ("Bought milk for Rs 250", "Spent Rs 2,000 on groceries") and conversational queries ("How much did I spend on travelling last month?", "What is my current balance?") via WhatsApp Business.  
**Decided:**
1. The core Android app remains 100% standalone, offline, private, and unbloated.
2. No heavy in-app chat interface or on-device LLM (which would consume 2 to 4 GB of storage/RAM and overwhelm budget devices).
3. The WhatsApp feature is architected as an optional external server-side companion bot (v1.1) that binds the user's phone number, processes natural language via server-side LLM with slot-filling clarification for missing parameters, and syncs via secure API.  
**Rejected:** Embedding chat UI or LLM runtimes directly into the Android application.  
**Cost / Trade-off:** Requires a lightweight server backend and phone-binding authentication for users who opt into WhatsApp companion services in v1.1.

### D84. Five-Tab Non-Slidable Navigation Architecture over Modal Bottom Sheets
User testing revealed that using modal bottom sheets (`TotalCardsBottomSheet`, `CategoryFilterBottomSheet`) for primary navigation buttons violated user expectations of a bottom navigation bar. Furthermore, rendering the interactive financial chart card on the home dashboard overcrowded the vertical viewport on compact devices (e.g., 720x1520 budget phones), restricting visible transaction history.  
**Decided:**
1. Converted primary navigation into 5 first-class top-level full-screen tabs: `HOME`, `ANALYTICS`, `LEDGER`, `CATEGORIES`, `SETTINGS`.
2. Tab switching is **strictly tap-based and non-slidable** (no `HorizontalPager` or horizontal swipe gestures). This eliminates gesture conflicts with the Canvas touch scrubber and vertical list scrolling.
3. Created a dedicated **Analytics Tab** (`AnalyticsTabContent.kt`) hosting the hardware-accelerated Compose Canvas chart, 3-way toggle (`[ Expenses | Income | Balance ]`), summary metric strip, and category spending distribution progress bars.
4. Restored the **Home Tab** to a spacious, focused dashboard (Hero Total Balance, Quick Action pills, Search, and uncrowded full Transaction History).
5. Converted Financial Overview and Category management into full-screen tabs (`LedgerTabContent.kt`, `CategoriesTabContent.kt`).
6. Embedded Settings directly into the 5th tab (`SettingsTabContent.kt`), ensuring the persistent floating bar remains accessible for instant 1-tap switching across destinations.
7. System back button behavior: pressing Back while on any secondary tab returns cleanly to the `HOME` tab.  
**Rejected:** Horizontal swipeable tab pager (conflicted with chart dragging and list scrolling); modal bottom sheets for primary app navigation.  
**Cost / Trade-off:** Tab state is retained via `rememberSaveable`; each destination requires full-screen layout and content padding for the floating bar.

### D85. No-Future-Dates Chart Bins, All-Time Relative Category Recency, and Color System Discipline
Physical testing on hardware surfaced three visual/UX inconsistencies:
1. **No future dates in time-series charts:** In open-ended periods (`ThisMonth`, `ThisWeek`, `ThisYear`, or custom ranges spanning into today), the chart generator previously populated placeholder bins through the end of the calendar interval (e.g., showing days 15 through 30 in mid-November). This wasted horizontal pixel density and misled users into expecting future predictions. Bins are now strictly bounded to elapsed time: `1..currentDay` for `ThisMonth`, Monday through today for `ThisWeek`, January through current month for `ThisYear`, and `minOf(endDate, now)` for custom ranges. Completed historical periods (e.g. `LastMonth`) still show their full historical span.
2. **Relative recency for category management:** The Categories tab is a standalone management screen without a period filter. Displaying "No transactions in this period" was ambiguous and unhelpful. Replaced with all-time relative recency metadata: `"Never used"` when transaction count is 0, or `"$count transactions • Last $relativeTime • $currencySymbol $totalAmount"` (e.g., `"4 transactions • Last 2 days ago • Rs 1,215.00"`).
3. **Total elimination of blue/cyan palette leaks:** The design contract is strictly **Organic Forest & Electric Lime** with semantic **Crimson** for deductions/expenses and **Emerald** for income. Legacy prototype blue/cyan (`#0099FF`, `#00A3FF`, `#0284C7`) had persisted in calendar icon badges, period filter pills, M3 theme `secondary` tokens, and the transaction form hero card. All blue instances were audited and replaced with Forest Green, Electric Lime, or Crimson Expense tokens.  
**Why:** Unifies visual brand identity, eliminates date ambiguity, and maximizes data point legibility on mobile screens.  
**Cost / Trade-off:** Requires `BuildChartDataUseCase` to take a reference timestamp `now` and `DashboardViewModel` to aggregate all-time category usage.

### D86. CI Release Gating Strictly to Master and Conventional Git Workflow Alignment
**Context:**
The CI pipeline previously built release APKs on every branch push, consuming runner minutes on unfinished branches and generating untracked release artifacts. Furthermore, development branches did not align with the standard conventions documented in `CONTRIBUTING.md`.  
**Decided:**
1. Branch naming and conventional commit standards from `CONTRIBUTING.md` (`feat/**`, `fix/**`, `chore/**`, `refactor/**`, `test/**`, `docs/**`) are formally integrated into CI triggers (`.github/workflows/android-ci.yml`).
2. Release APK build (`./gradlew assembleRelease`) and release APK artifact publishing (`actions/upload-artifact@v4`) are strictly gated to direct pushes or merged PRs on `master` (and `main`):
   `if: github.event_name == 'push' && (github.ref == 'refs/heads/master' || github.ref == 'refs/heads/main')`.
3. Feature branches and pull requests run unit tests (`./gradlew testDebugUnitTest`) and compile the debug APK (`./gradlew assembleDebug`), conserving runner minutes and preventing premature release artifact generation.
4. Repository documentation is reorganized: `README.md`, `CONTRIBUTING.md`, and `LICENSE` remain in the repository root as required by GitHub standards; all architecture, technical decision, roadmap, and screenshot documentation is consolidated under `docs/` (`docs/TECHNICAL_DECISIONS.md`, `docs/ROADMAP.md`, `docs/screenshots/`).  
**Why:** Guarantees release artifacts are only generated from production-ready code on `master`, aligns CI pipeline triggers with documented developer contribution workflows, and maintains a clean repository root structure.  
**Cost / Trade-off:** Release APK is not built for PR previews; developers must test release variants locally via `./gradlew assembleRelease` if verifying ProGuard/R8 optimizations prior to merge.

---

## 17. Decisions and claims that were corrected

| Item | What was wrong | Resolution |
|---|---|---|
| Inject annotations | `javax.inject` was recommended; the direction is `jakarta.inject` | Switched after checking release notes |
| Compose BOM | A newer BOM was called non-existent; it exists. Real cause of test failure was missing `platform(bom)` under `androidTestImplementation` | BOM declared in that configuration |
| `getLedgerById` | Described as compile error; it was a runtime main-thread hazard | Made `suspend` |
| Save navigation | `isSaved` set even when the save failed | Only set on `AppResult.Success` |
| Balance on edit/delete | Not reversed | Atomic reverse-then-apply |
| Default ledger | Never seeded, causing foreign key crash on first save | Added `LedgerSeeder` |
| Currency symbol | Locale-only detection | SIM-based detection with `CurrencyHelper` fallback |
| Custom range | Last day excluded; comparison mixed UTC and local time | `DateUtils` bridging |
| Single-date picker | UTC/local mismatch, risk of off-by-one | Converted both ways |
| Week boundary | Wrong on Sundays; milliseconds not zeroed | Rebuilt in `DateUtils`, tested |
| Dashboard listeners | Old collectors never cancelled | Jobs kept and cancelled |
| Summary cards | Showed all-time totals, ignored the chips | Period queries |
| Cross-DAO unguarded | Claimed they bypass error handling; repository wraps transactional call | Real unguarded paths were seeders and DataStore |
| Splash screen | Blamed on OEM before checking manifest | Manifest theme, icon source, and viewport padding |
| README index order | Stated `(ledgerId, type, isDeleted, date)` order that differed from code | Fixed in README (commit `9a47b76`) |
| DataStore theme mode | `SYSTEM` mode did not react dynamically to system dark/light configuration changes | Fixed using `isSystemInDarkTheme()` in `7531e53` |
| Monthly snapshot dates | Editing transactions across months failed to update snapshots when target month row did not exist | Dynamically create missing snapshots with UUIDs in `7531e53` |
| Ledger deletion guard | Default ledger lacked validation against accidental deletion | Added strict invariant validation |
| DataStore threading | Preference reads/writes ran on default dispatcher | Dispatched explicitly to `Dispatchers.IO` |
| Date picker contrast | Dark mode M3 date picker header was illegible on certain OLED displays | Custom high-contrast styling applied |
| CI Gradle download timeout | 10s default socket timeout failed CI runs | Set `networkTimeout=120000`, 5 retries in `851f737` |
| Branch validation in CI | `validate-branch.yml` rejected all PRs targeting `master` after Hacktoberfest | Removed obsolete workflow in `ffcc793` |
| CI test execution | CI previously ran only `lint` and `assembleDebug` without running unit tests | Added `./gradlew testDebugUnitTest` in `60fdb67` |
| Speculative sync code | `SyncableEntity` and `SyncContracts` added dead scaffolding to offline app | Removed in commit `29ff3e3` |
| Chart drag gestures | `detectDragGestures` intercepted all pointer motion including vertical drags, locking parent vertical scrolling | Switched to `detectHorizontalDragGestures` and `detectTapGestures` so vertical scrolls pass through cleanly |
| Chart future dates | `BuildChartDataUseCase` generated bins through the entire calendar month/year even when mid-period | Bounded current period bins strictly to elapsed time (`1..currentDay`, `0..daysSinceMonday`, `0..currentMonth`) |
| Categories tab empty text | "No transactions in this period" was displayed on a screen with no period filter | Replaced with all-time relative recency ("Never used" or "X transactions • Last X ago • Rs X") |
| Theme color leaks | M3 secondary color and calendar icon badges used electric blue/cyan (`#0099FF`, `#00A3FF`, `#0284C7`) | Replaced with Organic Forest & Electric Lime brand tokens |
| Chart drag dismissal | Inspection tooltip remained stuck on screen after drag finished | Added immediate reset on `onDragEnd` and `onDragCancel` to return chart to resting baseline |
| CI Release Gating | Release APK was built on every branch push, consuming runner minutes | Gated `assembleRelease` strictly to `master`/`main` pushes; feature branches run unit tests & debug assemble |
| Documentation Reorganization | Technical docs and roadmaps were scattered in the repository root | Consolidated all documentation into `docs/` keeping only README, CONTRIBUTING, and LICENSE in root |
| settings.gradle.kts @Incubating inspection | IDE flagged Gradle's `dependencyResolutionManagement` as unstable `@Incubating` API | Added `@file:Suppress("UnstableApiUsage")` at top of `settings.gradle.kts` |
| Hilt @Module unused inspection | Android Studio flagged `RepositoryModule` as "never used" because Dagger discovers modules via KSP rather than direct Kotlin calls | Added `@Suppress("unused")` to all DI modules (`RepositoryModule`, `DatabaseModule`, `PreferencesModule`) |

---

## 18. Known gaps and open decisions

| Area | Status | Detail / Direction |
|---|---|---|
| Seeding redundancy | Open | Runs from both `ExpenseTrackerApp.onCreate` and `AppStartupViewModel`. Safe and idempotent, but redundant. Direction: remove Application block. |
| Startup failure | Open | `AppStartupViewModel` uses bare `viewModelScope.launch`; a seeding failure would crash the app. Direction: wrap in `launchSafely`. |
| Money type | Open | `Double` amounts. Floating point can drift over millions of transactions. Direction: evaluate integer minor units (paisa, cents) before multi-year data accumulates. |
| Currency source | Open | `ledgers.currencyCode` is written by seeder but never read; UI reads DataStore. Direction: unify single source of truth when multi-ledger is introduced. |
| Future dates in form | Open | The add form allows future dates. Direction: decide whether to allow freely or show a warning. |
| Paging | Open | `androidx.room:room-paging` is declared in Gradle dependencies but unused. Direction: implement paging when list exceeds 1,000 items, or remove dependency. |
| Non-English locale digits | Open | Amount formatting uses `Locale.getDefault()`; not verified with non-Latin numeral scripts (e.g., Arabic-Indic digits). |
| Custom chip label polish | Open | Custom filter chip currently displays "Custom"; could show the active picked date range. |
| WhatsApp Companion | Planned (v1.1) | External server-side companion bot with user phone binding and natural language slot-filling. |
| Visual Analytics (Charts) | Resolved | Native hardware-accelerated Compose Canvas charts implemented with zero third-party dependencies, daily/monthly granularity, 3-way toggle (Expenses, Income, Balance), zero-activity flat carryforward, no future dates in open periods, and interactive touch scrubbing with haptic feedback. |
| Navigation Architecture | Resolved | Converted floating navigation bar from modal bottom sheet triggers into 5 first-class non-slidable tabs (Home, Analytics, Ledger, Categories, Settings) with dedicated Graph tab. |
| CI test execution | Resolved | Unit test step (`testDebugUnitTest`) added to GitHub Actions workflow. |
| CI Release Gating | Resolved | Release APK build strictly gated to pushes on master/main; branch triggers aligned with CONTRIBUTING.md conventions. |
| Hacktoberfest cleanup | Resolved | Branch removed, documentation cleansed, PR validation retargeted to `master`. |
| PDF Export | Resolved | Native ISO A4 generator with dual storage save and system share paths implemented. |
| Test coverage gaps | Resolved | Added 59-test stress suite covering concurrency, pagination, boundaries, and repositories. |
| Unverified device behavior | Resolved | Period totals and category sheets physically verified on Android 9 (OPPO A12) hardware. |

---

## Keeping this current

When a decision changes, update its entry and add a row to section 17 stating what was wrong and why. When an open item in section 18 is resolved, move it from Open to Resolved or update its status.
