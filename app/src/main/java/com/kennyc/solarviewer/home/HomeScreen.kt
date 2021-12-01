package com.kennyc.solarviewer.home

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.SystemsViewModel
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.ui.*
import com.kennyc.solarviewer.utils.ContentState
import com.kennyc.solarviewer.utils.ErrorState
import com.kennyc.solarviewer.utils.asKilowattString
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

//region StatCard
@Composable
fun StatCard(
    title: String,
    energy: Int,
    footer: String,
    @DrawableRes icon: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color)
    )
    {
        StatTitle(title, icon)
        Text(
            text = energy.asKilowattString() + "kWh",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp, end = 8.dp),
            fontSize = dimensionResource(id = R.dimen.stat_card_energy).value.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            color = White_80
        )

        Text(
            text = footer,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            fontSize = dimensionResource(id = R.dimen.stat_card_footer).value.sp,
            fontFamily = FontFamily.SansSerif,
            color = White_80
        )
    }
}

@Composable
fun StatTitle(title: String, @DrawableRes icon: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = dimensionResource(id = R.dimen.stat_card_title).value.sp,
            fontFamily = FontFamily.SansSerif,
            color = White_80
        )

        Image(painter = painterResource(id = icon), null)
    }
}

@Composable
private fun StatGrid(report: SolarSystemReport, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f)) {
            StatCard(
                title = stringResource(id = R.string.home_stat_title_solar),
                energy = report.productionInWatts,
                footer = stringResource(id = R.string.home_stat_produced),
                icon = R.drawable.ic_wb_sunny_24,
                color = Production,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 4.dp, end = 4.dp)
            )
            StatCard(
                title = stringResource(id = R.string.home_stat_title_exported),
                energy = report.exportedInWatts,
                footer = stringResource(id = R.string.home_stat_exported),
                icon = R.drawable.ic_export_power_24,
                color = Blue_800,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, bottom = 4.dp, end = 8.dp)
            )
        }

        Row(modifier = Modifier.weight(1f)) {
            StatCard(
                title = stringResource(id = R.string.home_stat_title_usage),
                energy = report.importedInWatts,
                footer = stringResource(id = R.string.home_stat_imported),
                icon = R.drawable.ic_flash_on_24,
                color = Consumption,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 4.dp, end = 4.dp)
            )

            val icon = when (report.isNetPositive) {
                true -> {
                    R.drawable.ic_arrow_top_right_24
                }
                else -> {
                    R.drawable.ic_arrow_bottom_left_24
                }
            }

            val footer = when (report.isNetPositive) {
                true -> {
                    R.string.home_stat_produced
                }
                else -> {
                    R.string.home_stat_imported
                }
            }

            StatCard(
                title = stringResource(id = R.string.home_stat_title_net),
                energy = abs(report.netEnergy),
                footer = stringResource(id = footer),
                icon = icon,
                color = GRAY_800,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, bottom = 4.dp, end = 8.dp)
            )
        }
    }
}
//endregion

//region Donut
@Composable
fun EnergyPiChart(
    report: SolarSystemReport,
    modifier: Modifier = Modifier
) {
    val max = report.consumptionInWatts
    val slice = report.productionInWatts - report.exportedInWatts
    val percentage = slice.toFloat() / max.toFloat()

    val time = SimpleDateFormat(
        "h:mma",
        Locale.getDefault()
    ).format(report.lastReported)

    val consumedEnergy = stringResource(
        R.string.home_kwh_consumed,
        report.consumptionInWatts.asKilowattString(),
        time
    )

    Box(modifier = modifier.padding(16.dp)) {
        Donut(percentage)
        Text(
            text = consumedEnergy,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
fun Donut(@FloatRange(from = 0.0, to = 1.0) solarEnergyPercentage: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Canvas(
            modifier = Modifier
                .height(225.dp)
                .width(225.dp)
                .align(Alignment.Center)
        ) {
            val stroke = Stroke(50f)
            val sweep = 360f * solarEnergyPercentage
            drawArc(Consumption, 0f, 360f, false, style = stroke)
            drawArc(Production, 270f, sweep, false, style = stroke)
        }
    }

}
//endregion

//region Loading
@Composable
fun Loading() {
    // TODO Better loading
    Text(text = "Loading")
}

//endregion

//region Content
@Composable
fun Content(report: SolarSystemReport) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EnergyPiChart(
            report,
            modifier = Modifier.weight(1f)
        )
        StatGrid(report = report, modifier = Modifier.weight(1f))
    }
}

//endregion

//region Error
@Preview(showSystemUi = true)
@Composable
fun Error(
    error: Throwable? = null,
    onClick: () -> Unit = {}
) {
    val errorText = when (error) {
        is RateLimitException -> stringResource(id = R.string.rate_limit_error)
        else -> stringResource(id = R.string.date_error)
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorText,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )
        Button(onClick = onClick) {
            Text(text = stringResource(id = R.string.error_retry))
        }
    }


}
//endregion

@Composable
@Suppress("UnnecessaryVariable")
fun HomeScreen(viewModel: HomeViewModel, systemsViewModel: SystemsViewModel) {
    val state by viewModel.state.observeAsState()
    when (val safeState = state) {
        is ContentState<*> -> {
            require(safeState.item is SolarSystemReport)
            Content(report = safeState.item)
        }

        is ErrorState -> {
            Error(safeState.error) {
                viewModel.refresh()
            }
        }

        else -> Loading()
    }

    val system by systemsViewModel.selectedSystem.observeAsState()
    system?.let { viewModel.setSelectedSystem(it) }
    val date by systemsViewModel.date.observeAsState()
    date?.let { viewModel.setSelectedDate(it) }
}


