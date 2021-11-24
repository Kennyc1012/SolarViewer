package com.kennyc.solarviewer.home

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kennyc.solarviewer.R

//region StatCard
@Composable
fun StatCard(
    title: String,
    energy: String,
    footer: String,
    @DrawableRes icon: Int,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        backgroundColor = color,
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
    ) {
        Box() {
            StatTitle(title, icon)
            Text(
                text = energy,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, end = 8.dp),
                fontSize = dimensionResource(id = R.dimen.stat_card_energy).value.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.white_80)
            )

            Text(
                text = footer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                fontSize = dimensionResource(id = R.dimen.stat_card_footer).value.sp,
                fontFamily = FontFamily.SansSerif,
                color = colorResource(id = R.color.white_80)
            )
        }

    }
}

@Composable
fun StatTitle(title: String, @DrawableRes icon: Int) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
    ) {
        val (titleRef, iconRef) = createRefs()

        Text(
            text = title,
            modifier = Modifier.constrainAs(titleRef) {
                linkTo(parent.start, iconRef.end, bias = 0f)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            fontSize = dimensionResource(id = R.dimen.stat_card_title).value.sp,
            fontFamily = FontFamily.SansSerif,
            color = colorResource(id = R.color.white_80)
        )

        Image(painter = painterResource(id = icon), null,
            modifier = Modifier.constrainAs(iconRef) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            })
    }
}

@Preview
@Composable
fun PreviewStatCard() {
    StatCard("Solar", "10kWh", "Produced", R.drawable.ic_wb_sunny_24, Color.Red)
}
//endregion

//region Donut
@Composable
fun EnergyPiChart(
    @FloatRange(from = 0.0, to = 1.0) solarEnergyPercentage: Float,
    consumedEnergy: String
) {
    Box {
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
    CircularProgressIndicator(
        progress = 1f,
        color = colorResource(id = R.color.color_consumption),
        modifier = Modifier.height(250.dp).width(250.dp)
    )

    CircularProgressIndicator(
        progress = solarEnergyPercentage,
        color = colorResource(id = R.color.color_production),
        modifier = Modifier.height(250.dp).width(250.dp)
    )
}

@Preview
@Composable
fun PreviewDonut() {
    EnergyPiChart(.75f, "12.76 kWh")
}
//endregion