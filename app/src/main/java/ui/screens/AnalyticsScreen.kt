package com.example.converterapp.ui.screens

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.converterapp.domain.model.Currency
import com.example.converterapp.viewmodel.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.converterapp.ui.screens.SimpleCurrencyDropdown
@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val currencies by viewModel.currencies.collectAsState()
    val selectedAnalyticsCurrency by viewModel.selectedAnalyticsCurrency.collectAsState()
    val statsText by viewModel.statsText.collectAsState()
    val chartData by viewModel.chartData.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCurrencies() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Аналитика", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        SimpleCurrencyDropdown(
            label = "Валюта",
            currencies = currencies,
            selected = selectedAnalyticsCurrency,
            onSelect = { viewModel.selectAnalyticsCurrency(it) }
        )

        // График — теперь занимает всё доступное пространство
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),  // Занимает всё свободное место
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (chartData == null) {
                    Text(
                        text = "Недостаточно данных для графика.\nВыполните несколько конвертаций.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LineChartView(chartData = chartData!!)
                }
            }
        }

        // Статистика
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = statsText.ifEmpty { "Статистика появится после выбора валюты" })
            }
        }
    }
}

@Composable
fun LineChartView(chartData: ChartData) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),  // ← Важно! Занимает всё пространство родителя
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = IndexAxisValueFormatter(chartData.labels)
                xAxis.granularity = 1f
                xAxis.setAvoidFirstLastClipping(true)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = (chartData.minY - chartData.minY * 0.1f).coerceAtLeast(0f)
                setExtraOffsets(16f, 8f, 16f, 8f)
                invalidate()
            }
        },
        update = { chart ->
            val entries = chartData.entries.mapIndexed { index, value ->
                Entry(index.toFloat(), value.toFloat())
            }
            val dataSet = LineDataSet(entries, "Курс").apply {
                color = Color.BLUE
                setCircleColor(Color.BLUE)
                lineWidth = 2f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 10f
                setDrawValues(true)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

data class ChartData(
    val entries: List<Double>,
    val labels: List<String>,
    val minY: Float
)