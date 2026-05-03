package com.kennyc.solarviewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.util.Date

//region MainScreen
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun MainScreen(
    viewModel: SystemsViewModel
) {
    val navController = rememberNavController()
    val systems by viewModel.systems.collectAsStateWithLifecycle(emptyList())
    val selectedSystem by viewModel.selectedSystem.collectAsStateWithLifecycle()
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    var openDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(systems, selectedSystem, date, { openDialog = true }) },
        bottomBar = { BottomBar(tabs = listOf(NavTab.Home, NavTab.Daily), navController) }) {
        NavHost(
            navController = navController,
            startDestination = NavTab.Home.route,
            modifier = Modifier.padding(it)
        ) {
            composable(NavTab.Home.route) {
                HomeScreen()
            }

            composable(NavTab.Daily.route) {
                DailyScreen()
            }
        }
    }

    if (openDialog) {
        DatePicker(
            currentTime = viewModel.currentTime.time,
            onDismiss = { openDialog = false },
            onConfirm = { viewModel.setNewDate(Date(it)) }
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.padding(8.dp),
            ) {
                Text(
                    text = systems[selectedIndex].name,
                    style = MaterialTheme.typography.titleLarge
                )
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
fun DatePicker(
    currentTime: Long,
    onConfirm: (Long) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentTime)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDismiss()
                    onConfirm(it)
                }
            }) {
                Text(text = stringResource(id = R.string.button_ok))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
//endregion