package com.example.payrollsheet.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollLine

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val trade: String = "HELPER",
    val idNumber: String = "",
    val hourlyRate: Double = 0.0,
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Employee(
        id = id,
        name = name,
        trade = trade,
        idNumber = idNumber,
        hourlyRate = hourlyRate,
        status = status
    )

    companion object {
        fun fromDomain(e: Employee) = EmployeeEntity(
            id = e.id,
            name = e.name,
            trade = e.trade,
            idNumber = e.idNumber,
            hourlyRate = e.hourlyRate,
            status = e.status
        )
    }
}

@Entity(tableName = "payroll_batches")
data class PayrollBatchEntity(
    @PrimaryKey
    val id: String,
    val month: String,
    val site: String = "",
    val foreman: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payroll_lines",
    foreignKeys = [
        ForeignKey(
            entity = PayrollBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("batchId"),
        Index("employeeId")
    ]
)
data class PayrollLineEntity(
    @PrimaryKey
    val id: String,
    val batchId: String,
    val employeeId: Long,
    val month: String,
    val foreman: String = "",
    val hours: Double = 0.0,
    val rate: Double = 0.0,
    val foodDeduction: Double = 0.0,
    val prevAdvance: Double = 0.0,
    val newAdvance: Double = 0.0,
    val otherDeduction: Double = 0.0,
    val netSalary: Double = 0.0,
    val paid: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = PayrollLine(
        id = id,
        batchId = batchId,
        employeeId = employeeId,
        month = month,
        foreman = foreman,
        hours = hours,
        rate = rate,
        foodDeduction = foodDeduction,
        prevAdvance = prevAdvance,
        newAdvance = newAdvance,
        otherDeduction = otherDeduction,
        netSalary = netSalary,
        paid = paid
    )

    companion object {
        fun fromDomain(l: PayrollLine, batchMonth: String) = PayrollLineEntity(
            id = l.id.ifBlank { java.util.UUID.randomUUID().toString() },
            batchId = l.batchId,
            employeeId = l.employeeId,
            month = if (l.month.isNotBlank()) l.month else batchMonth,
            foreman = l.foreman,
            hours = l.hours,
            rate = l.rate,
            foodDeduction = l.foodDeduction,
            prevAdvance = l.prevAdvance,
            newAdvance = l.newAdvance,
            otherDeduction = l.otherDeduction,
            netSalary = l.netSalary,
            paid = l.paid
        )
    }
}

@Entity(
    tableName = "advance_transactions",
    indices = [Index("employeeId")]
)
data class AdvanceTxEntity(
    @PrimaryKey
    val id: String,
    val employeeId: Long,
    val date: String,
    val amount: Double,
    val reason: String = "Personal",
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = AdvanceTx(
        id = id,
        employeeId = employeeId,
        date = date,
        amount = amount,
        reason = reason,
        paymentMethod = paymentMethod,
        notes = notes
    )

    companion object {
        fun fromDomain(tx: AdvanceTx) = AdvanceTxEntity(
            id = tx.id.ifBlank { java.util.UUID.randomUUID().toString() },
            employeeId = tx.employeeId,
            date = tx.date,
            amount = tx.amount,
            reason = tx.reason,
            paymentMethod = tx.paymentMethod,
            notes = tx.notes
        )
    }
}
