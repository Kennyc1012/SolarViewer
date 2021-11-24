package com.kennyc.solarviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kennyc.solarviewer.ui.NavTab

//region MainScreen
@ExperimentalComposeUiApi
@Composable
fun MainScreen() {
    // TODO Values
    Scaffold(topBar = { TopBar(listOf("System Name"), 0, "11/23/2021") },
        bottomBar = { BottomBar(tabs = listOf(NavTab.Home, NavTab.Daily)) }) {

    }
}

@Preview(showBackground = true)
@ExperimentalComposeUiApi
@Composable
fun PreviewMainScreen() {
    // TODO Values
    Scaffold(topBar = { PreviewTopBar() },
        bottomBar = { PreviewBottomBar()}) {
    }
}
//endregion

//region TopBar
@ExperimentalComposeUiApi
@Composable
fun TopBar(systemNames: List<String>, selectedIndex: Int = 0, date: String) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()
        .background(MaterialTheme.colors.primary)) {
        val (button, row) = createRefs()

        Row(modifier = Modifier
            .padding(8.dp)
            .constrainAs(row) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                linkTo(parent.start, button.end, bias = 0f)
            },) {
            Text(text = systemNames[selectedIndex])
            Icon(imageVector = Icons.Filled.ArrowDropDown, null)
        }


        Button(modifier = Modifier.constrainAs(button) {
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        },
            onClick = { /*TODO*/ }) {
            Text(text = date)
        }
    }
}

@Preview
@Composable
@ExperimentalComposeUiApi
fun PreviewTopBar() {
    TopBar(listOf("System Name"), 0, "11/23/2021")
}


//endregion

//region BottomBar
@Composable
fun BottomBar(tabs: List<NavTab>) {
    BottomNavigation() {
        tabs.forEach {
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = it.icon), null) },
                label = { Text(stringResource(id = it.title)) },
                alwaysShowLabel = true,
                selected = false,
                onClick = { /*TODO*/ }
            )
        }
    }
}

@Preview
@Composable
fun PreviewBottomBar() {
    BottomBar(tabs = listOf(NavTab.Home, NavTab.Daily))
}
//endregion