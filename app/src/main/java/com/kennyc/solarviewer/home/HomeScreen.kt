package com.kennyc.solarviewer.home

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.ui.*


//region StatCard
@Composable
fun StatCard(
    title: String,
    energy: String,
    footer: String,
    @DrawableRes icon: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    /*Card(
        shape = RoundedCornerShape(10.dp),
        backgroundColor = color,
        modifier = modifier.fillMaxHeight()
    ) {*/
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color)
    )
    {
        StatTitle(title, icon)
        Text(
            text = energy,
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

    //}
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

@Preview
@Composable
fun StatGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f)) {
            StatCard(
                title = "Solar",
                energy = "44.64kWh",
                footer = "Produce",
                icon = R.drawable.ic_wb_sunny_24,
                color = Production,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 4.dp, end = 4.dp)
            )
            StatCard(
                title = "Excess Energy",
                energy = "23.66kWh",
                footer = "Exported",
                icon = R.drawable.ic_export_power_24,
                color = Blue_800,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, bottom = 4.dp, end = 8.dp)
            )
        }

        Row(modifier = Modifier.weight(1f)) {
            StatCard(
                title = "Energy Usage",
                energy = "12.53kWh",
                footer = "Imported",
                icon = R.drawable.ic_flash_on_24,
                color = Consumption,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 4.dp, end = 4.dp)
            )
            StatCard(
                title = "Net Energy",
                energy = "11.14kWh",
                footer = "Produced",
                icon = R.drawable.ic_arrow_top_right_24,
                color = GRAY_800,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, bottom = 4.dp, end = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewStatCard() {
    StatCard(
        "Solar", "10kWh", "Produced", R.drawable.ic_wb_sunny_24, Production,
    )
}
//endregion

//region Donut
@Composable
fun EnergyPiChart(
    @FloatRange(from = 0.0, to = 1.0) solarEnergyPercentage: Float,
    consumedEnergy: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(16.dp)) {
        Donut(solarEnergyPercentage)
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
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        val stroke = Stroke(50f)
        val sweep = 360f * solarEnergyPercentage
        drawArc(Consumption,0f,360f,false, style = stroke)
        drawArc(Production, 270f,sweep,false, style = stroke)
    }
}

@Preview
@Composable
fun PreviewDonut() {
    EnergyPiChart(.75f, "12.76 kWh")
}
//endregion

@Preview
@Composable
fun PreviewHomeScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EnergyPiChart(
            solarEnergyPercentage = .75f, "33.5Wh",
            modifier = Modifier.weight(1f)
        )
        StatGrid(modifier = Modifier.weight(1f))
    }
}