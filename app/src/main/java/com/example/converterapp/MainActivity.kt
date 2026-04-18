package com.example.converterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.converterapp.data.local.AppDatabase
import com.example.converterapp.data.remote.CurrencyApiService
import com.example.converterapp.data.repository.CurrencyRepository
import com.example.converterapp.data.repository.HistoryRepository
import com.example.converterapp.ui.screens.*
import com.example.converterapp.ui.theme.ConverterAppTheme
import com.example.converterapp.viewmodel.MainViewModel
import com.example.converterapp.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val currencyDao = database.currencyDao()
        val historyDao = database.historyDao()
        val apiService = CurrencyApiService.create()
        val currencyRepository = CurrencyRepository(currencyDao, apiService)
        val historyRepository = HistoryRepository(historyDao)
        val viewModelFactory = MainViewModelFactory(currencyRepository, historyRepository)

        setContent {
            ConverterAppTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            val items = listOf(
                                NavItem("Конвертер", "converter", Icons.Default.SwapVert),
                                NavItem("Валюты", "currencies", Icons.Default.List),
                                NavItem("История", "history", Icons.Default.History),
                                NavItem("Аналитика", "analytics", Icons.Default.BarChart)
                            )
                            items.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(navController, startDestination = "converter", Modifier.padding(paddingValues)) {
                        composable("converter") { ConverterScreen(viewModel) }
                        composable("currencies") { CurrenciesScreen(viewModel) }
                        composable("history") { HistoryScreen(viewModel) }
                        composable("analytics") { AnalyticsScreen(viewModel) }
                    }
                }
            }
        }
    }
}

data class NavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)