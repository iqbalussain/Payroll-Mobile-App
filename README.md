# Site Payroll Manager — Android (Kotlin + Jetpack Compose)

A complete native Android payroll management system engineered with modern **Kotlin**, **Jetpack Compose**, **Material Design 3**, and **Room Database** local offline-first persistence.

Originally rewritten from the web payroll system to native Android while faithfully preserving all mathematical formulas, advance carry-forward balances, cost center allocations, and multi-tab operational workflows.

---

## Key Features

1. **Worker Directory (Employees)**
   - Master fields: Name, Trade (CARPENTER, STEEL FIXER, HELPER, MASON, ELEC, PLUB, FORMAN), ID/Badge Number, Hourly Rate, Status (Active, Holiday, Cancelled).
   - Real-time search by name, trade, and ID.
   - Filter chips by Trade and Employment Status.
   - Employee summary metrics (Active Staff, Total Staff).
   - Add, Edit, and View employee profile dialogs.

2. **Monthly Payroll Operations**
   - Month selection across multi-year cycles.
   - Site/Project naming and default Batch Foreman assignment.
   - Prevention of duplicate payroll: Workers already added to a batch in a given month are protected from duplicate payouts.
   - Hourly wage entry cards: Hours worked, rate, food deduction, previous advance recovered, new advance added, other deductions.
   - Automated calculations: Gross Pay = `Hours × Rate`, Net Payable = `max(0, Gross - Food - PrevAdv - Other)`, Remaining Balance = `Net - Paid`.
   - Line-by-line foreman override capability.
   - Quick "Add All Active" button to auto-populate eligible workers.
   - Batch Totals summary card with realtime updates.

3. **Advance Management (Loans & Disbursements)**
   - Record cash or bank advance transactions: Worker, Date, Amount, Reason (Personal, Medical, Family support, Travel/Ticket, Emergency, Other), Payment Method (Cash, Bank), Notes.
   - Automated carry-forward calculation: Unrecovered advances from earlier months are accurately calculated and carried forward without duplication or loss.
   - Advance tracking KPI strip: Total Issued, Total Recovered via Payroll, and Net Outstanding Due.
   - Filter by worker and quick toggle for only workers with outstanding balances.

4. **Worker Lifetime History**
   - Comprehensive lifetime statement per worker.
   - Aggregated lifetime totals: Total Hours, Total Gross, Total Net, Paid Amount, Balance Due, and Outstanding Advance Balance.
   - Month-by-month history breakdown.
   - Advance disbursement records.
   - Export and share employee statement via Android System Share sheet.

5. **Salary Pay Slips**
   - Filter slips by month and worker search.
   - Multi-selection for bulk export and summary generation.
   - Interactive Pay Slip card dialog featuring itemized Earnings, Deductions, Net Payable, Paid amount, and Carried Forward Advance details.
   - Android System Share integration to send/export slips via WhatsApp, Email, or Print.

6. **Project Cost Allocation Centers**
   - Automatically flattens payroll records across sites, foremen, and months.
   - Project cost KPIs: Total Hours, Basic Wages, Total Net Cost, Allocated (Paid), and Remaining.
   - Visual progress bars for cost allocation percentage.
   - Export Cost Allocation summary report.

7. **Role-Based Access (Admin / HR)**
   - Interactive role switcher pill in the top app bar.
   - `ADMIN` mode: Full administrative rights including deleting employees, deleting payroll batches, and removing advance transactions.
   - `HR` mode: Standard operational access (create, edit, view, calculate, export) with deletion controls protected.

---

## Technical Architecture

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Local Persistence:** Room Database 2.6.1 (SQLite) with KSP
- **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel) + Repository Pattern
- **Reactive Streams:** Kotlin Coroutines & `Flow` / `StateFlow`
- **Design System:** Custom theme in `ui/theme/` (Charcoal, Deep Navy `#0F172A`, Teal `#0D9488`, Emerald `#059669`)

### Directory Structure

```
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/payrollsheet/
│       │   ├── PayrollApp.kt
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── local/          # Room Database, Entities & DAOs
│       │   │   ├── model/          # Kotlin Data Models & Calculation Formulas
│       │   │   └── repository/     # Data abstraction layer
│       │   └── ui/
│       │       ├── components/     # Dialogs (Employee, Advance, Salary Slip)
│       │       ├── screens/        # Compose screens (Home, Employees, Payroll, etc.)
│       │       ├── theme/          # Color, Type, Theme
│       │       └── viewmodel/      # PayrollViewModel & State
│       └── res/
│           ├── drawable/           # Vector icons & drawables
│           ├── mipmap-anydpi-v26/  # Adaptive launcher icons
│           └── values/             # strings.xml, colors.xml, themes.xml
├── gradle/
│   └── libs.versions.toml          # Gradle Version Catalog
├── build.gradle.kts
├── settings.gradle.kts
└── metadata.json                   # AI Studio platform sync metadata
```

---

## Building and Exporting

- **In AI Studio Build**: Export the project as a ZIP archive or push to GitHub using the Settings menu.
- **In Android Studio**: Open the root folder in Android Studio (Ladybug or newer), sync Gradle dependencies, and run on any Android device or emulator running Android 8.0 (API 26) or higher.
