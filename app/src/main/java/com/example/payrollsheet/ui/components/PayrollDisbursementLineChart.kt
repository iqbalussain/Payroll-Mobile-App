package com.example.payrollsheet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate400
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Slate900
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight
import java.util.Locale

/**
 * Data model for a single month's salary disbursement datapoint
 */
data class MonthlyDisbursementPoint(
    val monthCode: String,      // "2026-04"
    val monthLabel: String,     // "Apr '26"
    val fullMonthName: String,  // "April 2026"
    val netDisbursed: Double,   // Total Net Salary
    val grossSalary: Double,    // Total Gross Salary
    val deductions: Double,     // Total Deductions
    val employeeCount: Int      // Workers active in this month
)

enum class ChartMetricFilter {
    NET_SALARY,
    GROSS_SALARY,
    BOTH
}

/**
 * Recharts-inspired clean, interactive line chart for visualizing
 * monthly salary disbursements over the past six months.
 */
@Composable
fun PayrollDisbursementLineChart(
    dataPoints: List<MonthlyDisbursementPoint>,
    modifier: Modifier = Modifier,
    onMonthSelected: ((String) -> Unit)? = null
) {
    var selectedMetric by remember { mutableStateOf(ChartMetricFilter.BOTH) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(dataPoints.lastIndex.takeIf { it >= 0 }) }
    val textMeasurer = rememberTextMeasurer()

    val totalSixMonthNet = remember(dataPoints) { dataPoints.sumOf { it.netDisbursed } }
    val avgMonthlyNet = remember(dataPoints) {
        if (dataPoints.isNotEmpty()) totalSixMonthNet / dataPoints.size else 0.0
    }
    val peakMonthPoint = remember(dataPoints) { dataPoints.maxByOrNull { it.netDisbursed } }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payroll_line_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = Teal600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Disbursement Trend (Past 6 Months)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    Text(
                        text = "Monthly net vs gross salary payout comparison",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                // Interactive 6-Mo Total Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldLight,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Emerald600.copy(alpha = 0.3f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "6-MO TOTAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = PayrollCalculations.formatCurrency(totalSixMonthNet),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                    }
                }
            }

            // Quick Stats Row (Monthly Average + Peak Month)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monthly Average
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Teal600,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(text = "Monthly Avg", fontSize = 10.sp, color = Slate500)
                            Text(
                                text = PayrollCalculations.formatCurrency(avgMonthlyNet),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                    }
                }

                // Peak Month
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Navy800,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(text = "Peak Month", fontSize = 10.sp, color = Slate500)
                            Text(
                                text = "${peakMonthPoint?.monthLabel ?: "N/A"} (${PayrollCalculations.formatCurrency(peakMonthPoint?.netDisbursed ?: 0.0)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Legend & Metric Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Net Disbursed Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { selectedMetric = ChartMetricFilter.NET_SALARY }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Teal600)
                        )
                        Text(
                            text = "Net Payout",
                            fontSize = 11.sp,
                            fontWeight = if (selectedMetric == ChartMetricFilter.NET_SALARY || selectedMetric == ChartMetricFilter.BOTH) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMetric == ChartMetricFilter.NET_SALARY || selectedMetric == ChartMetricFilter.BOTH) Slate900 else Slate400
                        )
                    }

                    // Gross Earnings Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { selectedMetric = ChartMetricFilter.GROSS_SALARY }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Navy800)
                        )
                        Text(
                            text = "Gross Earnings",
                            fontSize = 11.sp,
                            fontWeight = if (selectedMetric == ChartMetricFilter.GROSS_SALARY || selectedMetric == ChartMetricFilter.BOTH) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMetric == ChartMetricFilter.GROSS_SALARY || selectedMetric == ChartMetricFilter.BOTH) Slate900 else Slate400
                        )
                    }
                }

                // Filter chip to toggle Both
                FilterChip(
                    selected = selectedMetric == ChartMetricFilter.BOTH,
                    onClick = {
                        selectedMetric = if (selectedMetric == ChartMetricFilter.BOTH) ChartMetricFilter.NET_SALARY else ChartMetricFilter.BOTH
                    },
                    label = { Text("Compare", fontSize = 10.sp) },
                    modifier = Modifier.height(26.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate200,
                        selectedLabelColor = Slate900
                    )
                )
            }

            // Interactive Tooltip Card (shows details of tapped month)
            val activePoint = selectedPointIndex?.let { dataPoints.getOrNull(it) }
            AnimatedVisibility(
                visible = activePoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activePoint != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMonthSelected?.invoke(activePoint.monthCode)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Teal600.copy(alpha = 0.4f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = activePoint.fullMonthName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TealLight)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "${activePoint.employeeCount} Staff",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Teal600
                                        )
                                    }
                                }
                                Text(
                                    text = "Gross: ${PayrollCalculations.formatCurrency(activePoint.grossSalary)} | Deds: ${PayrollCalculations.formatCurrency(activePoint.deductions)}",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Net Disbursed",
                                    fontSize = 9.sp,
                                    color = Slate500
                                )
                                Text(
                                    text = PayrollCalculations.formatCurrency(activePoint.netDisbursed),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Teal600
                                )
                            }
                        }
                    }
                }
            }

            // Native Recharts Canvas Line Chart
            RechartsStyleCanvas(
                dataPoints = dataPoints,
                selectedMetric = selectedMetric,
                selectedPointIndex = selectedPointIndex,
                textMeasurer = textMeasurer,
                onPointTapped = { index ->
                    selectedPointIndex = if (selectedPointIndex == index) null else index
                    if (index in dataPoints.indices) {
                        onMonthSelected?.invoke(dataPoints[index].monthCode)
                    }
                }
            )

            // Touch interaction hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap on any point to inspect disbursements and filter summary",
                    fontSize = 10.sp,
                    color = Slate400
                )
            }
        }
    }
}

/**
 * Custom Canvas implementation replicating modern Recharts aesthetic:
 * Smooth Bezier curve, vertical gradient fill, dashed grid lines,
 * glowing data points, and interactive crosshair cursor.
 */
@Composable
private fun RechartsStyleCanvas(
    dataPoints: List<MonthlyDisbursementPoint>,
    selectedMetric: ChartMetricFilter,
    selectedPointIndex: Int?,
    textMeasurer: TextMeasurer,
    onPointTapped: (Int) -> Unit
) {
    val axisColor = Slate200
    val gridLineColor = Slate200.copy(alpha = 0.7f)
    val textStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Slate500
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    if (dataPoints.isEmpty()) return@detectTapGestures
                    val chartLeft = 70f
                    val chartRight = size.width - 24f
                    val stepX = (chartRight - chartLeft) / maxOf(1, dataPoints.size - 1)

                    // Find nearest point
                    var closestIndex = 0
                    var minDistance = Float.MAX_VALUE
                    for (i in dataPoints.indices) {
                        val pointX = chartLeft + i * stepX
                        val dist = kotlin.math.abs(offset.x - pointX)
                        if (dist < minDistance) {
                            minDistance = dist
                            closestIndex = i
                        }
                    }
                    if (minDistance < stepX * 0.75f) {
                        onPointTapped(closestIndex)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // Canvas Layout Paddings
        val paddingLeft = 70f
        val paddingRight = 24f
        val paddingTop = 20f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (dataPoints.isEmpty() || chartWidth <= 0 || chartHeight <= 0) {
            return@Canvas
        }

        // Calculate Max Y value for scaling
        val maxNet = dataPoints.maxOfOrNull { it.netDisbursed } ?: 10000.0
        val maxGross = dataPoints.maxOfOrNull { it.grossSalary } ?: 10000.0
        val rawMax = when (selectedMetric) {
            ChartMetricFilter.NET_SALARY -> maxNet
            ChartMetricFilter.GROSS_SALARY -> maxGross
            ChartMetricFilter.BOTH -> maxOf(maxNet, maxGross)
        }
        // Round up nicely for Y-axis headroom
        val maxY = if (rawMax <= 0.0) 10000.0 else (Math.ceil(rawMax / 5000.0) * 5000.0)

        // Draw 4 Horizontal Grid Lines & Y-Axis Labels
        val gridSteps = 4
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

        for (i in 0..gridSteps) {
            val yNorm = i.toFloat() / gridSteps.toFloat()
            val yPos = paddingTop + (1f - yNorm) * chartHeight
            val valueAtY = (yNorm * maxY).toLong()

            // Dashed Grid Line
            drawLine(
                color = gridLineColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(width - paddingRight, yPos),
                strokeWidth = 1f,
                pathEffect = dashedEffect
            )

            // Currency Tick Label
            val labelText = if (valueAtY >= 1000) "${valueAtY / 1000}k" else "$valueAtY"
            val textLayout = textMeasurer.measure(
                text = labelText,
                style = textStyle
            )
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                topLeft = Offset(paddingLeft - textLayout.size.width - 10f, yPos - textLayout.size.height / 2f),
                style = textStyle
            )
        }

        // Calculate Coordinates for Each Month
        val stepX = chartWidth / maxOf(1, dataPoints.size - 1)
        val netPoints = mutableListOf<Offset>()
        val grossPoints = mutableListOf<Offset>()

        dataPoints.forEachIndexed { index, point ->
            val x = paddingLeft + index * stepX

            val netNorm = (point.netDisbursed / maxY).toFloat().coerceIn(0f, 1f)
            val netY = paddingTop + (1f - netNorm) * chartHeight
            netPoints.add(Offset(x, netY))

            val grossNorm = (point.grossSalary / maxY).toFloat().coerceIn(0f, 1f)
            val grossY = paddingTop + (1f - grossNorm) * chartHeight
            grossPoints.add(Offset(x, grossY))

            // X-Axis Month Label
            val monthLayout = textMeasurer.measure(
                text = point.monthLabel,
                style = textStyle.copy(
                    fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedPointIndex == index) Teal600 else Slate500
                )
            )
            drawText(
                textMeasurer = textMeasurer,
                text = point.monthLabel,
                topLeft = Offset(x - monthLayout.size.width / 2f, height - paddingBottom + 10f),
                style = textStyle.copy(
                    fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedPointIndex == index) Teal600 else Slate500
                )
            )
        }

        // Draw Interactive Crosshair Cursor if a point is selected
        selectedPointIndex?.let { index ->
            if (index in dataPoints.indices) {
                val pointX = paddingLeft + index * stepX
                drawLine(
                    color = Teal600.copy(alpha = 0.5f),
                    start = Offset(pointX, paddingTop),
                    end = Offset(pointX, height - paddingBottom),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }
        }

        // 1. Draw Net Salary Gradient Area Fill (when NET or BOTH selected)
        if (selectedMetric == ChartMetricFilter.NET_SALARY || selectedMetric == ChartMetricFilter.BOTH) {
            val fillPath = Path()
            fillPath.moveTo(netPoints.first().x, height - paddingBottom)
            fillPath.lineTo(netPoints.first().x, netPoints.first().y)

            // Smooth cubic bezier curve for fill
            for (i in 0 until netPoints.size - 1) {
                val p0 = netPoints[i]
                val p1 = netPoints[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            fillPath.lineTo(netPoints.last().x, height - paddingBottom)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Teal600.copy(alpha = 0.30f),
                        Teal600.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )
        }

        // 2. Draw Gross Salary Smooth Curve Line (when GROSS or BOTH selected)
        if (selectedMetric == ChartMetricFilter.GROSS_SALARY || selectedMetric == ChartMetricFilter.BOTH) {
            val grossPath = Path()
            grossPath.moveTo(grossPoints.first().x, grossPoints.first().y)

            for (i in 0 until grossPoints.size - 1) {
                val p0 = grossPoints[i]
                val p1 = grossPoints[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                grossPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            drawPath(
                path = grossPath,
                color = Navy800.copy(alpha = 0.85f),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Gross Point Dots
            grossPoints.forEachIndexed { i, pt ->
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Navy800,
                    radius = 4.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 3. Draw Net Salary Smooth Curve Line (when NET or BOTH selected)
        if (selectedMetric == ChartMetricFilter.NET_SALARY || selectedMetric == ChartMetricFilter.BOTH) {
            val netPath = Path()
            netPath.moveTo(netPoints.first().x, netPoints.first().y)

            for (i in 0 until netPoints.size - 1) {
                val p0 = netPoints[i]
                val p1 = netPoints[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                netPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            drawPath(
                path = netPath,
                color = Teal600,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Net Point Dots & Glow on selected
            netPoints.forEachIndexed { i, pt ->
                val isSelected = selectedPointIndex == i

                if (isSelected) {
                    // Outer Glow Ring
                    drawCircle(
                        color = Teal600.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = pt
                    )
                }

                // Inner White Center
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                    center = pt
                )

                // Colored Border Ring
                drawCircle(
                    color = Teal600,
                    radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                    center = pt,
                    style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 2.dp.toPx())
                )
            }
        }
    }
}
