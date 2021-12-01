package com.kennyc.solarviewer.daily

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.SystemsViewModel
import com.kennyc.solarviewer.data.model.SolarGraphData
import com.kennyc.solarviewer.utils.asKilowattString
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

//region DailyTitle
@Composable
fun DailyTitle(barPoint: BarPoint, modifier: Modifier = Modifier) {
    val text = if (barPoint == BarPoint.EMPTY_POINT) {
        ""
    } else {
        val produced = barPoint.produced
        val consumed = barPoint.consumed
        val formatter = SimpleDateFormat("h:mma", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

        when {
            consumed > 0 && produced > 0 -> {
                stringResource(
                    R.string.daily_stat_header_both,
                    produced.toInt(),
                    consumed.toInt(),
                    formatter.format(barPoint.date)
                )
            }

            consumed > 0 -> {
                stringResource(
                    R.string.daily_stat_header_consumed,
                    consumed.toInt(),
                    formatter.format(barPoint.date)
                )
            }

            else -> ""
        }
    }

    Text(text = text, modifier = modifier, style = MaterialTheme.typography.titleSmall)
}
//endregion

//region BarChart
@Composable
fun BarGraph(
    barPair: Pair<BarDataSet, List<Date>>,
    modifier: Modifier = Modifier,
    listener: OnChartValueSelectedListener? = null
) {
    AndroidView(modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                val white = resources.getColor(R.color.white_80, context.theme)
                xAxis.setDrawGridLines(false)
                xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(false)
                axisLeft.textColor = white
                axisLeft.valueFormatter = BarChartYAxisFormatter()
                xAxis.textColor = white
                legend.textColor = white
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                setPinchZoom(false)
                setScaleEnabled(false)
                setTouchEnabled(true)
                setOnChartValueSelectedListener(listener)
                data = BarData(barPair.first)
                xAxis.valueFormatter = BarChartXAxisFormatter(barPair.second)
                invalidate()
            }
        })
}

private fun generateBarData(
    context: Context,
    solarData: List<SolarGraphData>
): Pair<BarDataSet, List<Date>> {
    val producedColor =
        ResourcesCompat.getColor(context.resources, R.color.color_production, context.theme)
    val consumedColor =
        ResourcesCompat.getColor(context.resources, R.color.color_consumption, context.theme)

    val barColors = Array(solarData.size) {
        when (it % 2 == 0) {
            true -> producedColor
            else -> consumedColor
        }
    }.toList()

    val time = mutableListOf<Date>()
    val entries = solarData.map {
        time.add(it.time)
        BarEntry(it.x, floatArrayOf(it.produced, it.consumed), it.time)
    }

    val barData = BarDataSet(entries, null).apply {
        val consumedLabel = context.getString(
            R.string.daily_consumed,
            solarData.sumBy { it.consumed.toInt().absoluteValue }.asKilowattString()
        )

        val producedLabel = context.getString(
            R.string.daily_produced,
            solarData.sumBy { it.produced.toInt() }.asKilowattString()
        )

        stackLabels = arrayOf(producedLabel, consumedLabel)
        colors = barColors
        setDrawValues(false)
    }

    return Pair(barData, time)
}
//endregion

@Composable
fun DailyScreen(viewModel: DailyViewModel, systemsViewModel: SystemsViewModel) {
    val solarData by viewModel.solarData.observeAsState()
    val selectedBarPoint by viewModel.selectedBarPoint.observeAsState()

    solarData?.let {
        if (it.isSuccess) {
            val barData = generateBarData(LocalContext.current, it.getOrThrow())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp)
            ) {
                DailyTitle(selectedBarPoint ?: BarPoint.EMPTY_POINT)
                BarGraph(
                    barData, Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(), createBarListener(barData, viewModel)
                )
            }
        }
    }

    val system by systemsViewModel.selectedSystem.observeAsState()
    system?.let { viewModel.setSelectedSystem(it) }
    val date by systemsViewModel.date.observeAsState()
    date?.let { viewModel.setSelectedDate(it) }
}

private fun createBarListener(
    barPair: Pair<BarDataSet, List<Date>>,
    viewModel: DailyViewModel
): OnChartValueSelectedListener {
    return object : OnChartValueSelectedListener {
        override fun onNothingSelected() {
            viewModel.setSelectedBarPoint(BarPoint.EMPTY_POINT)
        }

        override fun onValueSelected(e: Entry, h: Highlight) {
            val barData = BarData(barPair.first)
            val entries = barData.dataSets[0]
            val stats = entries.getEntryForIndex(h.x.toInt())

            val produced = stats.positiveSum
            val consumed = stats.negativeSum
            val date = stats.data as Date
            viewModel.setSelectedBarPoint(BarPoint(produced, consumed, date))
        }
    }
}