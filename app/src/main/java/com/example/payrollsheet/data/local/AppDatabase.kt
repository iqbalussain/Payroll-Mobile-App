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
        AdvanceTxEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun payrollDao(): PayrollDao
    abstract fun employeeDao(): EmployeeDao

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
                        seedInitialData(database.payrollDao())
                    }
                }
            }
        }

        suspend fun seedInitialData(dao: PayrollDao) {
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
        }
    }
}
