package com.kennyc.solarviewer.daily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.kennyc.solarviewer.BindingFragment
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.SystemsViewModel
import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.databinding.FragmentDailyBinding
import com.kennyc.solarviewer.di.components.FragmentComponent
import com.kennyc.view.MultiStateView
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class DailyFragment : BindingFragment<FragmentDailyBinding>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

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

        viewModel.lineData.observe(viewLifecycleOwner, Observer { onDailyDataUpdated(it) })
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

    private fun onDailyDataUpdated(barData: BarData) {
        val time = mutableListOf<Date>()

        barData.dataSets.first().run {
            for (x in 0 until entryCount) {
                val item = getEntryForIndex(x).data

                if (item is ProductionStats) {
                    time.add(Date(TimeUnit.SECONDS.toMillis(item.endingAtTS)))
                } else if (item is ConsumptionStats) {
                    time.add(Date(TimeUnit.SECONDS.toMillis(item.endingAtTS)))
                }
            }
        }

        binding.dailyChart.run {
            data = barData
            xAxis.valueFormatter = BarChartXAxisFormatter(time)
            invalidate()
        }

        binding.dailyMsv.viewState = MultiStateView.ViewState.CONTENT
    }
}