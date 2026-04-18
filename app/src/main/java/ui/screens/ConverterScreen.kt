package com.example.converterapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.converterapp.domain.model.Currency
import com.example.converterapp.viewmodel.MainViewModel

@Composable
fun ConverterScreen(viewModel: MainViewModel) {
    val amount = viewModel.amount
    val result by viewModel.result.collectAsState()
    val currencies by viewModel.currencies.collectAsState()
    val selectedFrom by viewModel.selectedFromCurrency.collectAsState()
    val selectedTo by viewModel.selectedToCurrency.collectAsState()
    val note = viewModel.note
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadCurrencies() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Конвертер валют", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("Сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                SimpleCurrencyDropdown(
                    label = "Из",
                    currencies = currencies,
                    selected = selectedFrom,
                    onSelect = { viewModel.selectFromCurrency(it) }
                )

                SimpleCurrencyDropdown(
                    label = "В",
                    currencies = currencies,
                    selected = selectedTo,
                    onSelect = { viewModel.selectToCurrency(it) }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { viewModel.updateNote(it) },
                    label = { Text("Заметка (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.convert(
                            onSuccess = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("КОНВЕРТИРОВАТЬ") }

                Button(
                    onClick = {
                        viewModel.quickConvert(
                            onSuccess = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Быстрый пересчёт (последний курс)") }
            }
        }

        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "Результат: $result", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SimpleCurrencyDropdown(
    label: String,
    currencies: List<Currency>,
    selected: Currency?,
    onSelect: (Currency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label:", modifier = Modifier.width(40.dp))
        Box(modifier = Modifier.weight(1f)) {
            Button(onClick = { expanded = true }) {
                Text(selected?.code ?: "Выберите валюту")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency.code) },
                        onClick = {
                            onSelect(currency)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}