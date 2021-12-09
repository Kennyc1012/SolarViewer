package com.kennyc.solarviewer.home

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.ui.*
import com.kennyc.solarviewer.utils.*
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
    Card(
        shape = RoundedCornerShape(10.dp),
        backgroundColor = color,
        modifier = modifier
            .fillMaxHeight()
    )
    {
        Box {
            StatTitle(title, icon)
            Text(
                text = energy.asKilowattString() + "kWh",
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .align(Alignment.CenterStart),
                fontSize = dimensionResource(id = R.dimen.stat_card_energy).value.sp,
                fontWeight = FontWeight.Medium,
                color = White_80
            )

            Text(
                text = footer,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .align(Alignment.BottomStart),
                fontSize = dimensionResource(id = R.dimen.stat_card_footer).value.sp,
                fontWeight = FontWeight.Normal,
                color = White_80
            )
        }
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
            fontWeight = FontWeight.Normal,
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
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
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
                    .padding(start = 4.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
            )
        }
    }
}
//endregion

//region EnergyPiChart
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

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Donut(percentage)
        Text(
            text = consumedEnergy,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
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

@Preview
@Composable
private fun PreviewEnergyPiChart() {
    EnergyPiChart(
        SolarSystemReport(
            10000,
            20000,
            1000,
            2000, Date()
        )
    )
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

//region HomeScreen
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.subscribeAsState(LoadingState)

    HomeUi(state) {
        viewModel.refresh()
    }
}

@Composable
fun HomeUi(state: UiState, refresh: () -> Unit = {}) {
    when (state) {
        is ContentState<*> -> {
            require(state.item is SolarSystemReport) { "Required SolarSystemReport but got $state" }
            Content(report = state.item)
        }

        is ErrorState -> {
            Error(state.error) {
                refresh.invoke()
            }
        }

        else -> Loading()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewHomeScreenContent() {
    AppTheme {
        HomeUi(
            ContentState(
                SolarSystemReport(
                    10000,
                    20000,
                    1000,
                    2000, Date()
                )
            )
        )
    }
}


@Preview
@Composable
private fun PreviewHomeError() {
    HomeUi(ErrorState(RuntimeException()))
}


@Preview
@Composable
private fun PreviewHomeScreenLoading() {
    HomeUi(LoadingState)
}
//endregion