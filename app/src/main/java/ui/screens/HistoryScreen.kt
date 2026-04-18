package com.example.converterapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.converterapp.domain.model.ConversionHistory
import com.example.converterapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val history by viewModel.history.collectAsState()
    val searchQuery = viewModel.searchQuery
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "История операций", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Поиск по дате/валюте/заметке") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            items(history) { item ->
                HistoryCard(item = item)
            }
        }

        Button(
            onClick = { scope.launch { viewModel.exportHistory(context) } },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Экспорт в CSV") }
        Button(
            onClick = {
                scope.launch { viewModel.clearAllHistory(context) }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Очистить историю")
        }
    }
}

@Composable
fun HistoryCard(item: ConversionHistory) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(item.date),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${"%.2f".format(item.amount)} ${item.fromCurrency} → ${item.toCurrency} = ${"%.2f".format(item.result)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(text = "Курс: ${"%.4f".format(item.rate)}", fontSize = 12.sp)
            if (!item.note.isNullOrEmpty()) {
                Text(text = "Заметка: ${item.note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}