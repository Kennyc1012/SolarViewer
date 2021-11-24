package com.kennyc.solarviewer.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kennyc.solarviewer.R

//region StatCard
@Composable
fun StatCard(
    title: String,
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
        Column() {
            StatTitle(title, icon)
        }

    }
}

@Composable
fun StatTitle(title: String, @DrawableRes icon: Int) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Blue)
            .padding(8.dp)
    ) {
        val (titleRef, iconRef) = createRefs()

        Text(text = title,
            modifier = Modifier.constrainAs(titleRef) {
                linkTo(parent.start, iconRef.end, bias = 0f)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
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
    StatCard("Solar", R.drawable.ic_wb_sunny_24, Color.Red)
}
//endregion