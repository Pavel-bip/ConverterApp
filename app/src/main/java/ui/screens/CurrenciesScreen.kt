package com.example.converterapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.converterapp.domain.model.Currency
import com.example.converterapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CurrenciesScreen(viewModel: MainViewModel) {
    val currencies by viewModel.currencies.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadCurrencies() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Управление валютами", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { scope.launch { viewModel.showAddCurrencyDialog(context) } },
                modifier = Modifier.weight(1f)
            ) { Text("Добавить") }
            Button(
                onClick = { scope.launch { viewModel.updateCurrenciesFromApi(context) } },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) { Text("Обновить из API") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(currencies) { currency ->
                CurrencyCard(currency = currency, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CurrencyCard(currency: Currency, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "${currency.code} — ${"%.4f".format(currency.rateToRub)} ₽", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "Обновлено: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(currency.updatedDate)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { scope.launch { viewModel.showEditCurrencyDialog(context, currency) } }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
                if (currency.code != "RUB") {
                    IconButton(onClick = { scope.launch { viewModel.deleteCurrency(currency) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }
}