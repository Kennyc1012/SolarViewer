package com.kennyc.solarviewer.daily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kennyc.solarviewer.BindingFragment
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.SystemsViewModel
import com.kennyc.solarviewer.data.model.SolarGraphData
import com.kennyc.solarviewer.databinding.FragmentDailyBinding
import com.kennyc.solarviewer.di.components.FragmentComponent
import com.kennyc.solarviewer.utils.asKilowattString
import com.kennyc.view.MultiStateView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.absoluteValue


@ExperimentalCoroutinesApi
class DailyFragment : BindingFragment<FragmentDailyBinding>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @ColorInt
    @JvmField
    @Inject
    @Named("produced-color")
    var producedColor: Int = 0

    @ColorInt
    @JvmField
    @Inject
    @Named("consumed-color")
    var consumedColor: Int = 0

    private val viewModel by viewModels<DailyViewModel> { factory }

    private val systemsViewModel by activityViewModels<SystemsViewModel> { factory }

    private val formatter = SimpleDateFormat("h:mma", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDailyBinding {
        return FragmentDailyBinding.inflate(inflater, container, false)
    }

    override fun inject(component: FragmentComponent) = component.inject(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dailyChart.apply {
            val white = resources.getColor(R.color.white_80, requireContext().theme)
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

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    binding.dailyStats.text = null
                }

                override fun onValueSelected(e: Entry, h: Highlight) {
                    val entries = binding.dailyChart.data.dataSets[0]
                    val stats = entries.getEntryForIndex(h.x.toInt())

                    val produced = stats.positiveSum
                    val consumed = stats.negativeSum
                    val date = stats.data as Date

                    binding.dailyStats.text = when {
                        consumed > 0 && produced > 0 -> {
                            getString(
                                R.string.daily_stat_header_both,
                                produced.toInt(),
                                consumed.toInt(),
                                formatter.format(date)
                            )
                        }

                        consumed > 0 -> {
                            getString(
                                R.string.daily_stat_header_consumed,
                                consumed.toInt(),
                                formatter.format(date)
                            )
                        }

                        else -> null
                    }
                }
            })
        }

        viewModel.solarData.observe(viewLifecycleOwner, Observer { onDailyDataUpdated(it) })
        viewModel.dateError.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), R.string.date_error, Toast.LENGTH_SHORT).show()
            binding.dailyMsv.viewState = MultiStateView.ViewState.CONTENT
        })
        viewModel.rateLimitError.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), R.string.rate_limit_error, Toast.LENGTH_SHORT).show()
            binding.dailyMsv.viewState = MultiStateView.ViewState.CONTENT
        })

        systemsViewModel.selectedSystem.observe(viewLifecycleOwner, Observer {
            binding.dailyMsv.viewState = MultiStateView.ViewState.LOADING
            viewModel.setSelectedSystem(it)
        })

        systemsViewModel.date.observe(viewLifecycleOwner, Observer {
            binding.dailyMsv.viewState = MultiStateView.ViewState.LOADING
            viewModel.setSelectedDate(it)
        })
    }

    private fun onDailyDataUpdated(solarData: List<SolarGraphData>) {
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

        val dataSet = BarDataSet(entries, null).apply {
            val consumedLabel = getString(
                R.string.daily_consumed,
                solarData.sumBy { it.consumed.toInt().absoluteValue }.asKilowattString()
            )

            val producedLabel = getString(
                R.string.daily_produced,
                solarData.sumBy { it.produced.toInt() }.asKilowattString()
            )

            stackLabels = arrayOf(producedLabel, consumedLabel)
            colors = barColors
            setDrawValues(false)
        }

        binding.dailyChart.run {
            data = BarData(dataSet)
            xAxis.valueFormatter = BarChartXAxisFormatter(time)
            invalidate()
        }

        binding.dailyMsv.viewState = MultiStateView.ViewState.CONTENT
    }
}