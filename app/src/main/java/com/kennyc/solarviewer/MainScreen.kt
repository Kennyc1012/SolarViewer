package com.kennyc.solarviewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kennyc.solarviewer.ui.NavTab

//region MainScreen
@ExperimentalMaterial3Api
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
@ExperimentalMaterial3Api
@Composable
fun PreviewMainScreen() {
    Scaffold(topBar = { PreviewTopBar() },
        bottomBar = { PreviewBottomBar() }) {
    }
}
//endregion

//region TopBar
@ExperimentalComposeUiApi
@Composable
fun TopBar(systemNames: List<String>, selectedIndex: Int = 0, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier.padding(8.dp),
        ) {
            Text(text = systemNames[selectedIndex])
            Icon(imageVector = Icons.Filled.ArrowDropDown, null)
        }


        TextButton(
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
    NavigationBar {
        tabs.forEach {
            NavigationBarItem(
                icon = {
                    Icon(painter = painterResource(id = it.icon), null)
                },
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