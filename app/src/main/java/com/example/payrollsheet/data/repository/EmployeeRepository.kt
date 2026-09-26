package com.example.payrollsheet.data.repository

import com.example.payrollsheet.data.local.EmployeeDao
import com.example.payrollsheet.data.local.EmployeeEntity
import com.example.payrollsheet.data.model.Employee
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Clean Architecture repository interface for employee data operations.
 * Abstracts local persistence details (Room / DAO / Entities) and exposes pure domain models.
 */
interface EmployeeRepository {
    val allEmployees: Flow<List<Employee>>

    fun getEmployeesByStatus(status: String): Flow<List<Employee>>

    suspend fun getEmployeeById(id: Long): Employee?

    suspend fun getEmployeeCount(): Int

    suspend fun insertEmployee(employee: Employee): Long

    suspend fun insertEmployees(employees: List<Employee>): List<Long>

    suspend fun updateEmployee(employee: Employee)

    suspend fun deleteEmployee(employee: Employee)

    suspend fun deleteEmployeeById(id: Long)
}

/**
 * Production implementation of [EmployeeRepository] wrapping [EmployeeDao].
 */
class EmployeeRepositoryImpl(
    private val employeeDao: EmployeeDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EmployeeRepository {

    override val allEmployees: Flow<List<Employee>> = employeeDao
        .getAllEmployees()
        .map { entities -> entities.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override fun getEmployeesByStatus(status: String): Flow<List<Employee>> = employeeDao
        .getEmployeesByStatus(status)
        .map { entities -> entities.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override suspend fun getEmployeeById(id: Long): Employee? = withContext(ioDispatcher) {
        employeeDao.getEmployeeById(id)?.toDomain()
    }

    override suspend fun getEmployeeCount(): Int = withContext(ioDispatcher) {
        employeeDao.getEmployeeCount()
    }

    override suspend fun insertEmployee(employee: Employee): Long = withContext(ioDispatcher) {
        employeeDao.insertEmployee(EmployeeEntity.fromDomain(employee))
    }

    override suspend fun insertEmployees(employees: List<Employee>): List<Long> = withContext(ioDispatcher) {
        employeeDao.insertEmployees(employees.map { EmployeeEntity.fromDomain(it) })
    }

    override suspend fun updateEmployee(employee: Employee) = withContext(ioDispatcher) {
        employeeDao.updateEmployee(EmployeeEntity.fromDomain(employee))
    }

    override suspend fun deleteEmployee(employee: Employee) = withContext(ioDispatcher) {
        employeeDao.deleteEmployee(EmployeeEntity.fromDomain(employee))
    }

    override suspend fun deleteEmployeeById(id: Long) = withContext(ioDispatcher) {
        employeeDao.deleteEmployeeById(id)
    }
}
