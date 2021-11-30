package com.kennyc.solarviewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kennyc.solarviewer.home.HomeScreen
import com.kennyc.solarviewer.home.HomeViewModel
import com.kennyc.solarviewer.ui.NavTab

//region MainScreen
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun MainScreen(
    viewModelFactory: ViewModelProvider.Factory? = null,
    viewModel: SystemsViewModel
) {
    // TODO Values
    val navController = rememberNavController()
    viewModel(modelClass = HomeViewModel::class.java, factory = viewModelFactory)
    Scaffold(topBar = { TopBar(listOf("System Name"), 0, "11/23/2021") },
        bottomBar = { BottomBar(tabs = listOf(NavTab.Home, NavTab.Daily), navController) }) {

        NavHost(
            navController = navController,
            startDestination = NavTab.Home.route,
            modifier = Modifier.padding(it)
        ) {
            // TODO Correct screens
            composable(NavTab.Home.route) {
                HomeScreen(
                    viewModel = viewModel(
                        modelClass = HomeViewModel::class.java,
                        factory = viewModelFactory
                    ), viewModel
                )
            }

            composable(NavTab.Daily.route) {
                // PreviewHomeScreen()
            }
        }
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
fun BottomBar(tabs: List<NavTab>, navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = navBackStackEntry?.destination?.route

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(painter = painterResource(id = tab.icon), null)
                },
                label = { Text(stringResource(id = tab.title)) },
                alwaysShowLabel = true,
                selected = currentTab == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
//endregion