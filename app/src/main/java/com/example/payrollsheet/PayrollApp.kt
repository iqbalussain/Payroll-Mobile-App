package com.example.payrollsheet

import android.app.Application
import com.example.payrollsheet.data.local.AppDatabase
import com.example.payrollsheet.data.repository.EmployeeRepository
import com.example.payrollsheet.data.repository.EmployeeRepositoryImpl
import com.example.payrollsheet.data.repository.PayrollRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PayrollApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val employeeRepository: EmployeeRepository by lazy { EmployeeRepositoryImpl(database.employeeDao()) }
    val repository by lazy {
        PayrollRepository(
            dao = database.payrollDao(),
            employeeDao = database.employeeDao(),
            employeeRepository = employeeRepository,
            payrollEntryDao = database.payrollEntryDao()
        )
    }
}
