package com.kennyc.solarviewer

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kennyc.solarviewer.daily.DailyScreen
import com.kennyc.solarviewer.daily.DailyViewModel
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.home.HomeScreen
import com.kennyc.solarviewer.home.HomeViewModel
import com.kennyc.solarviewer.ui.NavTab
import com.kennyc.solarviewer.ui.dateFormatter
import java.util.*

//region MainScreen
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun MainScreen(
        viewModelFactory: ViewModelProvider.Factory? = null,
        viewModel: SystemsViewModel
) {
    val navController = rememberNavController()
    val systems by viewModel.systems.subscribeAsState(emptyList())
    val selectedSystem by viewModel.selectedSystem.subscribeAsState(EMPTY_SYSTEM)
    val date by viewModel.selectedDate.subscribeAsState(viewModel.currentTime)
    val openDialog = remember { mutableStateOf(false) }
    createDatePickerClickAction(openDialog, viewModel)
    val dateClick = {
        openDialog.value = true
    }

    Scaffold(topBar = { TopBar(systems, selectedSystem, date, dateClick) },
            bottomBar = { BottomBar(tabs = listOf(NavTab.Home, NavTab.Daily), navController) }) {
        NavHost(
                navController = navController,
                startDestination = NavTab.Home.route,
                modifier = Modifier.padding(it)
        ) {
            composable(NavTab.Home.route) {
                HomeScreen(
                        viewModel = viewModel(
                                modelClass = HomeViewModel::class.java,
                                factory = viewModelFactory
                        )
                )
            }

            composable(NavTab.Daily.route) {
                DailyScreen(
                        viewModel = viewModel(
                                modelClass = DailyViewModel::class.java,
                                factory = viewModelFactory
                        )
                )
            }
        }
    }
}
//endregion

//region TopBar
@ExperimentalComposeUiApi
@Composable
fun TopBar(
        systems: List<SolarSystem> = emptyList(),
        selectedSystem: SolarSystem = EMPTY_SYSTEM,
        date: Date,
        dateButtonClick: () -> Unit = {}
) {
    if (systems.isNotEmpty() && selectedSystem != EMPTY_SYSTEM) {
        val selectedIndex = systems.indexOfFirst {
            it.id == selectedSystem.id
        }

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                    modifier = Modifier.padding(8.dp),
            ) {
                Text(text = systems[selectedIndex].name,
                        style = MaterialTheme.typography.titleLarge)
                Icon(imageVector = Icons.Filled.ArrowDropDown, null)
            }

            TextButton(
                    onClick = dateButtonClick
            ) {
                Text(text = dateFormatter.format(date))
            }
        }
    }
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

//region DatePicker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createDatePickerClickAction(openDialogState: MutableState<Boolean>, viewModel: SystemsViewModel) {
    val current = Calendar.getInstance().apply {
        time = viewModel.currentTime
    }

    if (openDialogState.value) {
        val datePickerState = rememberDatePickerState(initialDisplayedMonthMillis = current.timeInMillis)

        DatePickerDialog(
                onDismissRequest = {
                    openDialogState.value = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        openDialogState.value = false
                        datePickerState.selectedDateMillis?.let {
                            viewModel.setNewDate(Date(it))
                        }
                    }) {
                        Text(text = stringResource(id = R.string.button_ok))
                    }
                }) {
            DatePicker(state = datePickerState)
        }
    }
}
//endregion