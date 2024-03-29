package com.kennyc.solarviewer.daily

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
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
import com.kennyc.solarviewer.data.model.SolarGraphData
import com.kennyc.solarviewer.ui.Error
import com.kennyc.solarviewer.ui.Loading
import com.kennyc.solarviewer.ui.RefreshLifecycle
import com.kennyc.solarviewer.ui.timeFormatter
import com.kennyc.solarviewer.utils.ContentState
import com.kennyc.solarviewer.utils.ErrorState
import com.kennyc.solarviewer.utils.LoadingState
import com.kennyc.solarviewer.utils.UiState
import com.kennyc.solarviewer.utils.asKilowattString
import java.util.Date
import kotlin.math.absoluteValue

//region DailyTitle
@Composable
fun DailyTitle(barPoint: BarPoint, modifier: Modifier = Modifier) {
    val text = if (barPoint == BarPoint.EMPTY_POINT) {
        ""
    } else {
        val produced = barPoint.produced
        val consumed = barPoint.consumed

        when {
            consumed > 0 && produced > 0 -> {
                stringResource(
                        R.string.daily_stat_header_both,
                        produced.toInt(),
                        consumed.toInt(),
                        timeFormatter.format(barPoint.date)
                )
            }

            consumed > 0 -> {
                stringResource(
                        R.string.daily_stat_header_consumed,
                        consumed.toInt(),
                        timeFormatter.format(barPoint.date)
                )
            }

            else -> ""
        }
    }

    Text(text = text, modifier = modifier, style = MaterialTheme.typography.labelSmall)
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
                solarData.sumOf { it.consumed.toInt().absoluteValue }.asKilowattString()
        )

        val producedLabel = context.getString(
                R.string.daily_produced,
                solarData.sumOf { it.produced.toInt() }.asKilowattString()
        )

        stackLabels = arrayOf(producedLabel, consumedLabel)
        colors = barColors
        setDrawValues(false)
    }

    return Pair(barData, time)
}
//endregion

@Composable
fun DailyScreen(viewModel: DailyViewModel) {
    val state by viewModel.state.subscribeAsState(LoadingState)
    val selectedBarPoint by viewModel.selectedBarPoint.subscribeAsState(BarPoint.EMPTY_POINT)
    val refresh = { viewModel.refresh() }
    RefreshLifecycle(onRefresh = refresh)

    DailyScreenUi(
            state = state,
            selectedBarPoint = selectedBarPoint,
            refresh = refresh) {
        viewModel.setSelectedBarPoint(it)
    }
}

@Composable
private fun DailyScreenUi(
        state: UiState,
        selectedBarPoint: BarPoint,
        refresh: () -> Unit = {},
        barPointSelected: (BarPoint) -> Unit = {}
) {
    when (state) {
        is ContentState<*> -> {
            require(state.item is List<*>)
            val list = state.item as List<SolarGraphData>
            val barData = generateBarData(LocalContext.current, list)
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, top = 16.dp)
            ) {
                DailyTitle(selectedBarPoint)
                BarGraph(
                        barData, Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(), createBarListener(barData) { point ->
                    barPointSelected.invoke(point)
                }
                )
            }
        }

        is ErrorState -> {
            Error(state.error) {
                refresh.invoke()
            }
        }

        else -> Loading()
    }
}

private fun createBarListener(
        barPair: Pair<BarDataSet, List<Date>>,
        pointSelected: (BarPoint) -> Unit = {}
): OnChartValueSelectedListener {
    return object : OnChartValueSelectedListener {
        override fun onNothingSelected() {
            pointSelected.invoke(BarPoint.EMPTY_POINT)
        }

        override fun onValueSelected(e: Entry, h: Highlight) {
            val barData = BarData(barPair.first)
            val entries = barData.dataSets[0]
            val stats = entries.getEntryForIndex(h.x.toInt())

            val produced = stats.positiveSum
            val consumed = stats.negativeSum
            val date = stats.data as Date
            pointSelected.invoke(BarPoint(produced, consumed, date))
        }
    }
}