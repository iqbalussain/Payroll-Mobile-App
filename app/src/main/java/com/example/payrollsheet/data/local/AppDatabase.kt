package com.example.payrollsheet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        EmployeeEntity::class,
        PayrollBatchEntity::class,
        PayrollLineEntity::class,
        AdvanceTxEntity::class,
        PayrollEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun payrollDao(): PayrollDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun payrollEntryDao(): PayrollEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "payroll_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(database.payrollDao(), database.payrollEntryDao())
                    }
                }
            }
        }

        suspend fun seedInitialData(dao: PayrollDao, entryDao: PayrollEntryDao? = null) {
            if (dao.getEmployeeCount() > 0) return

            val emp1 = dao.insertEmployee(
                EmployeeEntity(name = "Ahmed Al-Mansoor", trade = "FORMAN", idNumber = "EMP-1001", hourlyRate = 28.50, status = "Active")
            )
            val emp2 = dao.insertEmployee(
                EmployeeEntity(name = "Rajesh Kumar", trade = "CARPENTER", idNumber = "EMP-1002", hourlyRate = 18.00, status = "Active")
            )
            val emp3 = dao.insertEmployee(
                EmployeeEntity(name = "Bilal Tariq", trade = "STEEL FIXER", idNumber = "EMP-1003", hourlyRate = 19.50, status = "Active")
            )
            val emp4 = dao.insertEmployee(
                EmployeeEntity(name = "Suresh Patel", trade = "MASON", idNumber = "EMP-1004", hourlyRate = 17.50, status = "Active")
            )
            val emp5 = dao.insertEmployee(
                EmployeeEntity(name = "Mohammed Imran", trade = "ELEC", idNumber = "EMP-1005", hourlyRate = 22.00, status = "Active")
            )
            val emp6 = dao.insertEmployee(
                EmployeeEntity(name = "Kishore Babu", trade = "HELPER", idNumber = "EMP-1006", hourlyRate = 12.00, status = "Active")
            )
            val emp7 = dao.insertEmployee(
                EmployeeEntity(name = "David Ochieng", trade = "PLUB", idNumber = "EMP-1007", hourlyRate = 20.00, status = "Holiday")
            )

            val batchId = UUID.randomUUID().toString()
            dao.insertBatch(
                PayrollBatchEntity(
                    id = batchId,
                    month = "2026-09",
                    site = "Marina Tower Block B",
                    foreman = "Ahmed Al-Mansoor"
                )
            )

            // Seed lines
            val lines = listOf(
                PayrollLineEntity(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    employeeId = emp1,
                    month = "2026-09",
                    foreman = "Ahmed Al-Mansoor",
                    hours = 208.0,
                    rate = 28.50,
                    foodDeduction = 150.0,
                    prevAdvance = 0.0,
                    newAdvance = 0.0,
                    otherDeduction = 0.0,
                    netSalary = 5778.0,
                    paid = 5778.0
                ),
                PayrollLineEntity(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    employeeId = emp2,
                    month = "2026-09",
                    foreman = "Ahmed Al-Mansoor",
                    hours = 195.0,
                    rate = 18.00,
                    foodDeduction = 120.0,
                    prevAdvance = 100.0,
                    newAdvance = 0.0,
                    otherDeduction = 0.0,
                    netSalary = 3290.0,
                    paid = 3000.0
                ),
                PayrollLineEntity(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    employeeId = emp3,
                    month = "2026-09",
                    foreman = "Ahmed Al-Mansoor",
                    hours = 210.0,
                    rate = 19.50,
                    foodDeduction = 130.0,
                    prevAdvance = 0.0,
                    newAdvance = 200.0,
                    otherDeduction = 0.0,
                    netSalary = 3965.0,
                    paid = 3965.0
                ),
                PayrollLineEntity(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    employeeId = emp4,
                    month = "2026-09",
                    foreman = "Ahmed Al-Mansoor",
                    hours = 180.0,
                    rate = 17.50,
                    foodDeduction = 110.0,
                    prevAdvance = 50.0,
                    newAdvance = 0.0,
                    otherDeduction = 25.0,
                    netSalary = 2965.0,
                    paid = 2965.0
                ),
                PayrollLineEntity(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    employeeId = emp6,
                    month = "2026-09",
                    foreman = "Ahmed Al-Mansoor",
                    hours = 215.0,
                    rate = 12.00,
                    foodDeduction = 100.0,
                    prevAdvance = 0.0,
                    newAdvance = 50.0,
                    otherDeduction = 0.0,
                    netSalary = 2480.0,
                    paid = 2480.0
                )
            )
            dao.insertLines(lines)

            // Seed an advance transaction
            dao.insertAdvance(
                AdvanceTxEntity(
                    id = UUID.randomUUID().toString(),
                    employeeId = emp3,
                    date = "2026-08-15",
                    amount = 300.0,
                    reason = "Family support",
                    paymentMethod = "Cash",
                    notes = "Emergency remittance for family"
                )
            )

            // Seed PayrollEntry records linking to EmployeeEntity
            entryDao?.let { eDao ->
                val entries = listOf(
                    PayrollEntry(
                        employeeId = emp1,
                        month = "2026-09",
                        hoursWorked = 208.0,
                        hourlyRate = 28.50,
                        grossSalary = 5928.0,
                        deductions = 150.0,
                        netSalary = 5778.0,
                        paidAmount = 5778.0,
                        paymentDate = "2026-09-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "September payroll disbursed"
                    ),
                    PayrollEntry(
                        employeeId = emp2,
                        month = "2026-09",
                        hoursWorked = 195.0,
                        hourlyRate = 18.00,
                        grossSalary = 3510.0,
                        deductions = 220.0,
                        netSalary = 3290.0,
                        paidAmount = 3000.0,
                        paymentDate = "2026-09-30",
                        paymentStatus = "Partial",
                        paymentMethod = "Cash",
                        notes = "Balance 290 carried forward"
                    ),
                    PayrollEntry(
                        employeeId = emp3,
                        month = "2026-09",
                        hoursWorked = 210.0,
                        hourlyRate = 19.50,
                        grossSalary = 4095.0,
                        deductions = 130.0,
                        netSalary = 3965.0,
                        paidAmount = 3965.0,
                        paymentDate = "2026-09-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "Includes overtime incentive"
                    ),
                    PayrollEntry(
                        employeeId = emp4,
                        month = "2026-09",
                        hoursWorked = 180.0,
                        hourlyRate = 17.50,
                        grossSalary = 3150.0,
                        deductions = 185.0,
                        netSalary = 2965.0,
                        paidAmount = 2965.0,
                        paymentDate = "2026-09-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "Standard disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp6,
                        month = "2026-09",
                        hoursWorked = 215.0,
                        hourlyRate = 12.00,
                        grossSalary = 2580.0,
                        deductions = 100.0,
                        netSalary = 2480.0,
                        paidAmount = 2480.0,
                        paymentDate = "2026-09-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "Site helper on-time payout"
                    ),
                    // August entries
                    PayrollEntry(
                        employeeId = emp1,
                        month = "2026-08",
                        hoursWorked = 208.0,
                        hourlyRate = 28.50,
                        grossSalary = 5928.0,
                        deductions = 150.0,
                        netSalary = 5778.0,
                        paidAmount = 5778.0,
                        paymentDate = "2026-08-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "August disbursement completed"
                    ),
                    PayrollEntry(
                        employeeId = emp2,
                        month = "2026-08",
                        hoursWorked = 200.0,
                        hourlyRate = 18.00,
                        grossSalary = 3600.0,
                        deductions = 120.0,
                        netSalary = 3480.0,
                        paidAmount = 3480.0,
                        paymentDate = "2026-08-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "August disbursement completed"
                    ),
                    PayrollEntry(
                        employeeId = emp3,
                        month = "2026-08",
                        hoursWorked = 190.0,
                        hourlyRate = 19.50,
                        grossSalary = 3705.0,
                        deductions = 100.0,
                        netSalary = 3605.0,
                        paidAmount = 3605.0,
                        paymentDate = "2026-08-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "August disbursement completed"
                    ),
                    // July entries
                    PayrollEntry(
                        employeeId = emp1,
                        month = "2026-07",
                        hoursWorked = 208.0,
                        hourlyRate = 28.50,
                        grossSalary = 5928.0,
                        deductions = 150.0,
                        netSalary = 5778.0,
                        paidAmount = 5778.0,
                        paymentDate = "2026-07-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "July disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp2,
                        month = "2026-07",
                        hoursWorked = 195.0,
                        hourlyRate = 18.00,
                        grossSalary = 3510.0,
                        deductions = 160.0,
                        netSalary = 3350.0,
                        paidAmount = 3350.0,
                        paymentDate = "2026-07-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "July disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp3,
                        month = "2026-07",
                        hoursWorked = 200.0,
                        hourlyRate = 19.50,
                        grossSalary = 3900.0,
                        deductions = 100.0,
                        netSalary = 3800.0,
                        paidAmount = 3800.0,
                        paymentDate = "2026-07-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "July disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp4,
                        month = "2026-07",
                        hoursWorked = 180.0,
                        hourlyRate = 17.50,
                        grossSalary = 3150.0,
                        deductions = 150.0,
                        netSalary = 3000.0,
                        paidAmount = 3000.0,
                        paymentDate = "2026-07-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "July disbursement"
                    ),
                    // June entries
                    PayrollEntry(
                        employeeId = emp1,
                        month = "2026-06",
                        hoursWorked = 208.0,
                        hourlyRate = 28.50,
                        grossSalary = 5928.0,
                        deductions = 150.0,
                        netSalary = 5778.0,
                        paidAmount = 5778.0,
                        paymentDate = "2026-06-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "June disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp2,
                        month = "2026-06",
                        hoursWorked = 190.0,
                        hourlyRate = 18.00,
                        grossSalary = 3420.0,
                        deductions = 150.0,
                        netSalary = 3270.0,
                        paidAmount = 3270.0,
                        paymentDate = "2026-06-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "June disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp3,
                        month = "2026-06",
                        hoursWorked = 195.0,
                        hourlyRate = 19.50,
                        grossSalary = 3802.5,
                        deductions = 120.0,
                        netSalary = 3682.5,
                        paidAmount = 3682.5,
                        paymentDate = "2026-06-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "June disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp6,
                        month = "2026-06",
                        hoursWorked = 210.0,
                        hourlyRate = 12.00,
                        grossSalary = 2520.0,
                        deductions = 100.0,
                        netSalary = 2420.0,
                        paidAmount = 2420.0,
                        paymentDate = "2026-06-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "June disbursement"
                    ),
                    // May entries
                    PayrollEntry(
                        employeeId = emp1,
                        month = "2026-05",
                        hoursWorked = 208.0,
                        hourlyRate = 28.50,
                        grossSalary = 5928.0,
                        deductions = 150.0,
                        netSalary = 5778.0,
                        paidAmount = 5778.0,
                        paymentDate = "2026-05-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "May disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp2,
                        month = "2026-05",
                        hoursWorked = 185.0,
                        hourlyRate = 18.00,
                        grossSalary = 3330.0,
                        deductions = 130.0,
                        netSalary = 3200.0,
                        paidAmount = 3200.0,
                        paymentDate = "2026-05-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "May disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp3,
                        month = "2026-05",
                        hoursWorked = 190.0,
                        hourlyRate = 19.50,
                        grossSalary = 3705.0,
                        deductions = 150.0,
                        netSalary = 3555.0,
                        paidAmount = 3555.0,
                        paymentDate = "2026-05-31",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "May disbursement"
                    ),
                    // April entries
                    PayrollEntry(
                        employeeId = emp1,
                        month = "2026-04",
                        hoursWorked = 208.0,
                        hourlyRate = 28.50,
                        grossSalary = 5928.0,
                        deductions = 150.0,
                        netSalary = 5778.0,
                        paidAmount = 5778.0,
                        paymentDate = "2026-04-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "April disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp2,
                        month = "2026-04",
                        hoursWorked = 180.0,
                        hourlyRate = 18.00,
                        grossSalary = 3240.0,
                        deductions = 140.0,
                        netSalary = 3100.0,
                        paidAmount = 3100.0,
                        paymentDate = "2026-04-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Cash",
                        notes = "April disbursement"
                    ),
                    PayrollEntry(
                        employeeId = emp3,
                        month = "2026-04",
                        hoursWorked = 185.0,
                        hourlyRate = 19.50,
                        grossSalary = 3607.5,
                        deductions = 150.0,
                        netSalary = 3457.5,
                        paidAmount = 3457.5,
                        paymentDate = "2026-04-30",
                        paymentStatus = "Paid",
                        paymentMethod = "Bank",
                        notes = "April disbursement"
                    )
                )
                eDao.insertEntries(entries)
            }
        }
    }
}
