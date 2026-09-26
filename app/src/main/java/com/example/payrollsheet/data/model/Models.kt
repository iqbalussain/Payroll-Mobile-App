package com.example.payrollsheet.data.model

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

data class Employee(
    val id: Long = 0,
    val name: String,
    val trade: String = "HELPER",
    val idNumber: String = "",
    val hourlyRate: Double = 0.0,
    val status: String = "Active" // "Active", "Holiday", "Cancelled"
)

data class PayrollLine(
    val id: String = "",
    val batchId: String = "",
    val employeeId: Long = 0,
    val month: String = "",
    val foreman: String = "",
    val hours: Double = 0.0,
    val rate: Double = 0.0,
    val foodDeduction: Double = 0.0,
    val prevAdvance: Double = 0.0,
    val newAdvance: Double = 0.0,
    val otherDeduction: Double = 0.0,
    val netSalary: Double = 0.0,
    val paid: Double = 0.0
)

data class PayrollBatch(
    val id: String = "",
    val month: String,
    val site: String = "",
    val foreman: String = "",
    val lines: List<PayrollLine> = emptyList()
)

data class AdvanceTx(
    val id: String = "",
    val employeeId: Long,
    val date: String,
    val amount: Double,
    val reason: String = "Personal",
    val paymentMethod: String = "Cash",
    val notes: String = ""
)

data class HistoryRow(
    val line: PayrollLine,
    val month: String,
    val site: String,
    val batchForeman: String,
    val batchId: String
)

data class AllocationRow(
    val key: String,
    val employeeIdNumber: String,
    val employeeName: String,
    val trade: String,
    val site: String,
    val foreman: String,
    val month: String,
    val hours: Double,
    val basic: Double,
    val foodDeduction: Double,
    val outstanding: Double,
    val total: Double,
    val allocated: Double,
    val allocationPct: Double,
    val remaining: Double
)

data class BatchTotals(
    val hours: Double = 0.0,
    val gross: Double = 0.0,
    val food: Double = 0.0,
    val prevAdvance: Double = 0.0,
    val newAdvance: Double = 0.0,
    val otherDeduction: Double = 0.0,
    val net: Double = 0.0,
    val paid: Double = 0.0,
    val balance: Double = 0.0
)

object PayrollCalculations {
    val TRADES = listOf(
        "CARPENTER",
        "STEEL FIXER",
        "HELPER",
        "MASON",
        "ELEC",
        "PLUB",
        "FORMAN"
    )

    val STATUSES = listOf("Active", "Holiday", "Cancelled")

    val MONTHS = listOf(
        "2026-07", "2026-08", "2026-09", "2026-10", "2026-11", "2026-12",
        "2027-01", "2027-02", "2027-03", "2027-04", "2027-05", "2027-06"
    )

    val PAYMENT_METHODS = listOf("Cash", "Bank")

    val ADVANCE_REASONS = listOf(
        "Personal",
        "Medical",
        "Family support",
        "Travel / ticket",
        "Emergency",
        "Other"
    )

    private val decimalFormat = DecimalFormat("#,##0.00")

    fun fmt(value: Double): String {
        return decimalFormat.format(value)
    }

    fun monthLabel(m: String): String {
        if (m.isBlank()) return ""
        return try {
            val parts = m.split("-")
            if (parts.size >= 2) {
                val cal = java.util.Calendar.getInstance()
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
                val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                sdf.format(cal.time)
            } else m
        } catch (_: Exception) {
            m
        }
    }

    fun lineGross(hours: Double, rate: Double): Double = hours * rate

    fun computeNet(
        gross: Double,
        foodDeduction: Double,
        prevAdvance: Double,
        otherDeduction: Double
    ): Double {
        return max(0.0, gross - foodDeduction - prevAdvance - otherDeduction)
    }

    fun lineBalance(netSalary: Double, paid: Double): Double = netSalary - paid

    fun txMonth(date: String): String {
        return if (date.length >= 7) date.substring(0, 7) else ""
    }

    fun advancesIssued(
        employeeId: Long,
        advances: List<AdvanceTx>,
        beforeMonth: String? = null
    ): Double {
        return advances
            .filter { it.employeeId == employeeId }
            .filter { beforeMonth == null || txMonth(it.date) < beforeMonth }
            .sumOf { it.amount }
    }

    fun advancesRecovered(
        employeeId: Long,
        batches: List<PayrollBatch>,
        beforeMonth: String? = null
    ): Double {
        var total = 0.0
        batches
            .filter { beforeMonth == null || it.month < beforeMonth }
            .forEach { b ->
                b.lines
                    .filter { it.employeeId == employeeId }
                    .forEach { l ->
                        total += l.prevAdvance
                    }
            }
        return total
    }

    fun payrollAdvancesIssued(
        employeeId: Long,
        batches: List<PayrollBatch>,
        beforeMonth: String? = null
    ): Double {
        var total = 0.0
        batches
            .filter { beforeMonth == null || it.month < beforeMonth }
            .forEach { b ->
                b.lines
                    .filter { it.employeeId == employeeId }
                    .forEach { l ->
                        total += l.newAdvance
                    }
            }
        return total
    }

    fun advanceCarryForward(
        employeeId: Long,
        month: String,
        batches: List<PayrollBatch>,
        advances: List<AdvanceTx> = emptyList()
    ): Double {
        val issued = advancesIssued(employeeId, advances, month) +
                payrollAdvancesIssued(employeeId, batches, month)
        val recovered = advancesRecovered(employeeId, batches, month)
        val bal = issued - recovered
        return max(0.0, Math.round(bal * 1000.0) / 1000.0)
    }

    fun advanceOutstanding(
        employeeId: Long,
        batches: List<PayrollBatch>,
        advances: List<AdvanceTx> = emptyList()
    ): Double {
        val issued = advancesIssued(employeeId, advances) +
                payrollAdvancesIssued(employeeId, batches)
        val recovered = advancesRecovered(employeeId, batches)
        val bal = issued - recovered
        return max(0.0, Math.round(bal * 1000.0) / 1000.0)
    }

    fun lockedEmployeeIds(
        batches: List<PayrollBatch>,
        month: String,
        currentBatchId: String? = null
    ): Set<Long> {
        val set = mutableSetOf<Long>()
        batches
            .filter { it.month == month && (currentBatchId == null || it.id != currentBatchId) }
            .forEach { b ->
                b.lines.forEach { l ->
                    if (l.employeeId > 0) set.add(l.employeeId)
                }
            }
        return set
    }

    fun employeeRows(
        batches: List<PayrollBatch>,
        employeeId: Long? = null
    ): List<HistoryRow> {
        val out = mutableListOf<HistoryRow>()
        batches.forEach { b ->
            b.lines.forEach { l ->
                if (l.employeeId <= 0) return@forEach
                if (employeeId != null && l.employeeId != employeeId) return@forEach
                out.add(
                    HistoryRow(
                        line = l,
                        month = b.month,
                        site = b.site,
                        batchForeman = b.foreman,
                        batchId = b.id
                    )
                )
            }
        }
        return out.sortedBy { it.month }
    }

    fun calculateBatchTotals(lines: List<PayrollLine>): BatchTotals {
        var h = 0.0
        var g = 0.0
        var f = 0.0
        var pa = 0.0
        var na = 0.0
        var od = 0.0
        var net = 0.0
        var paid = 0.0
        var bal = 0.0

        for (l in lines) {
            h += l.hours
            g += lineGross(l.hours, l.rate)
            f += l.foodDeduction
            pa += l.prevAdvance
            na += l.newAdvance
            od += l.otherDeduction
            net += l.netSalary
            paid += l.paid
            bal += lineBalance(l.netSalary, l.paid)
        }

        return BatchTotals(
            hours = h,
            gross = g,
            food = f,
            prevAdvance = pa,
            newAdvance = na,
            otherDeduction = od,
            net = net,
            paid = paid,
            balance = bal
        )
    }

    fun buildAllocationRows(
        batches: List<PayrollBatch>,
        employees: List<Employee>
    ): List<AllocationRow> {
        val empMap = employees.associateBy { it.id }
        val advanceMap = mutableMapOf<Long, Double>()
        val rows = mutableListOf<AllocationRow>()

        val sortedBatches = batches.sortedBy { it.month }
        for (b in sortedBatches) {
            b.lines.forEachIndexed { i, l ->
                if (l.employeeId <= 0) return@forEachIndexed
                val emp = empMap[l.employeeId]
                val gross = lineGross(l.hours, l.rate)
                val total = l.netSalary
                val allocated = l.paid
                val currentOutstanding = (advanceMap[l.employeeId] ?: 0.0) + l.newAdvance - l.prevAdvance
                advanceMap[l.employeeId] = max(0.0, currentOutstanding)

                val idTag = if (emp?.idNumber?.isNotBlank() == true) emp.idNumber else "Not Assigned"
                val name = emp?.name ?: "Employee #${l.employeeId}"
                val trade = emp?.trade ?: "—"
                val site = b.site.ifBlank { "(No Site)" }
                val foreman = l.foreman.ifBlank { b.foreman.ifBlank { "(No Foreman)" } }

                rows.add(
                    AllocationRow(
                        key = "${b.id}-${l.id.ifBlank { i.toString() }}",
                        employeeIdNumber = idTag,
                        employeeName = name,
                        trade = trade,
                        site = site,
                        foreman = foreman,
                        month = b.month,
                        hours = l.hours,
                        basic = gross,
                        foodDeduction = l.foodDeduction,
                        outstanding = max(0.0, currentOutstanding),
                        total = total,
                        allocated = allocated,
                        allocationPct = if (total > 0.0) (allocated / total) * 100.0 else 0.0,
                        remaining = total - allocated
                    )
                )
            }
        }
        return rows
    }
}
