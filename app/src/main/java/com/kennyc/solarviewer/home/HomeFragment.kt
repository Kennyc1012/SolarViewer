package com.kennyc.solarviewer.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kennyc.solarviewer.BindingFragment
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.SystemsViewModel
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.databinding.FragmentHomeBinding
import com.kennyc.solarviewer.di.components.FragmentComponent
import com.kennyc.solarviewer.utils.asKilowattString
import com.kennyc.view.MultiStateView
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

class HomeFragment : BindingFragment<FragmentHomeBinding>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by viewModels<HomeViewModel> { factory }

    private val systemsViewModel by activityViewModels<SystemsViewModel> { factory }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun inject(component: FragmentComponent) = component.inject(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.summary.observe(viewLifecycleOwner, { report ->
            if (report.isSuccess) {
                renderDonut(report.getOrThrow())
                renderStats(report.getOrThrow())
            } else {
                handleError(report.exceptionOrNull())
            }

            binding.homeMsv.viewState = MultiStateView.ViewState.CONTENT
            binding.homeRefresh.isRefreshing = false
        })

        systemsViewModel.selectedSystem.observe(viewLifecycleOwner, Observer {
            binding.homeMsv.viewState = MultiStateView.ViewState.LOADING
            viewModel.setSelectedSystem(it)
        })

        systemsViewModel.date.observe(viewLifecycleOwner, Observer {
            binding.homeMsv.viewState = MultiStateView.ViewState.LOADING
            viewModel.setSelectedDate(it)
        })

        binding.homeRefresh.setOnRefreshListener {
            systemsViewModel.date.value?.let { viewModel.setSelectedDate(it) }
        }
    }

    private fun renderDonut(report: SolarSystemReport) {
        val max = report.consumptionInWatts
        val slice = report.productionInWatts - report.exportedInWatts

        val time = SimpleDateFormat(
            "h:mma",
            Locale.getDefault()
        ).format(report.lastReported)

        val consumedString = getString(
            R.string.home_kwh_consumed,
            report.consumptionInWatts.asKilowattString(),
            time
        )

        binding.homeDonutChart.setDonutStats(max, slice, consumedString)
    }

    private fun renderStats(report: SolarSystemReport) {
        binding.homeProducedStats.setEnergyStat(report.productionInWatts.asKilowattString() + "kWh")
        binding.homeImportedStats.setEnergyStat(report.importedInWatts.asKilowattString() + "kWh")
        binding.homeExportedStats.setEnergyStat(report.exportedInWatts.asKilowattString() + "kWh")

        binding.homeNetStats.run {
            when (report.isNetPositive) {
                true -> {
                    setFooter(getString(R.string.home_stat_produced))
                    setIcon(R.drawable.ic_arrow_top_right_24)
                }
                else -> {
                    setFooter(getString(R.string.home_stat_imported))
                    setIcon(R.drawable.ic_arrow_bottom_left_24)
                }
            }

            setEnergyStat(abs(report.netEnergy).asKilowattString() + "kWh")
        }
    }

    private fun handleError(error: Throwable?) {
        when (error) {
            is RateLimitException -> {
                Toast.makeText(requireContext(), R.string.rate_limit_error, Toast.LENGTH_SHORT)
                    .show()
                binding.homeMsv.viewState = MultiStateView.ViewState.CONTENT
            }

            else -> {
                Toast.makeText(requireContext(), R.string.date_error, Toast.LENGTH_SHORT).show()
                binding.homeMsv.viewState = MultiStateView.ViewState.CONTENT
            }
        }
    }
}