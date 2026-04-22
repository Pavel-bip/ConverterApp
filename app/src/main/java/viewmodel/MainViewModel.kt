package com.example.converterapp.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.converterapp.data.repository.CurrencyRepository
import com.example.converterapp.data.repository.HistoryRepository
import com.example.converterapp.domain.model.ConversionHistory
import com.example.converterapp.domain.model.Currency
import com.example.converterapp.ui.screens.ChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.Date
import kotlin.text.Charsets

class MainViewModel(
    private val currencyRepository: CurrencyRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _amount = mutableStateOf("")
    val amount: String get() = _amount.value

    private val _result = MutableStateFlow("0.00")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _note = mutableStateOf("")
    val note: String get() = _note.value

    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())
    val currencies: StateFlow<List<Currency>> = _currencies.asStateFlow()

    private val _selectedFromCurrency = MutableStateFlow<Currency?>(null)
    val selectedFromCurrency: StateFlow<Currency?> = _selectedFromCurrency.asStateFlow()

    private val _selectedToCurrency = MutableStateFlow<Currency?>(null)
    val selectedToCurrency: StateFlow<Currency?> = _selectedToCurrency.asStateFlow()

    private val _selectedAnalyticsCurrency = MutableStateFlow<Currency?>(null)
    val selectedAnalyticsCurrency: StateFlow<Currency?> = _selectedAnalyticsCurrency.asStateFlow()

    private val _history = MutableStateFlow<List<ConversionHistory>>(emptyList())
    val history: StateFlow<List<ConversionHistory>> = _history.asStateFlow()

    private val _searchQuery = mutableStateOf("")
    val searchQuery: String get() = _searchQuery.value

    private val _statsText = MutableStateFlow("")
    val statsText: StateFlow<String> = _statsText.asStateFlow()

    private val _chartData = MutableStateFlow<ChartData?>(null)
    val chartData: StateFlow<ChartData?> = _chartData.asStateFlow()

    private var lastUsedRate = 0.0
    private var lastUsedFromCurrency = "USD"
    private var lastUsedToCurrency = "RUB"
    init {
        viewModelScope.launch {
            val currenciesList = currencyRepository.getAllCurrencies().first()
            if (currenciesList.none { it.code == "RUB" }) {
                currencyRepository.updateCurrency(Currency(code = "RUB", rateToRub = 1.0))
            }
        }
    }

    fun updateAmount(value: String) {
        _amount.value = value
    }

    fun updateNote(value: String) {
        _note.value = value
    }

    fun updateSearchQuery(value: String) {
        _searchQuery.value = value
        viewModelScope.launch {
            _history.value = if (value.isNotEmpty()) {
                historyRepository.searchHistory(value).first()
            } else {
                historyRepository.getAllHistory().first()
            }
        }
    }

    fun selectFromCurrency(currency: Currency) {
        val updated = _currencies.value.find { it.code == currency.code } ?: currency
        _selectedFromCurrency.value = updated
    }

    fun selectToCurrency(currency: Currency) {
        val updated = _currencies.value.find { it.code == currency.code } ?: currency
        _selectedToCurrency.value = updated
    }

    fun selectAnalyticsCurrency(currency: Currency) {
        val updated = _currencies.value.find { it.code == currency.code } ?: currency
        _selectedAnalyticsCurrency.value = updated
        viewModelScope.launch { loadAnalytics() }
    }

    fun loadCurrencies() {
        viewModelScope.launch {
            currencyRepository.getAllCurrencies().collect { list ->
                val allowedCodes = setOf("RUB", "USD", "EUR", "CNY")
                val filteredList = list.filter { it.code in allowedCodes }
                list.filter { it.code !in allowedCodes }.forEach { currency ->
                    currencyRepository.deleteCurrency(currency.code)
                }

                _currencies.value = filteredList
                _selectedFromCurrency.value = filteredList.find { it.code == _selectedFromCurrency.value?.code }
                    ?: filteredList.find { it.code == "USD" } ?: filteredList.firstOrNull()
                _selectedToCurrency.value = filteredList.find { it.code == _selectedToCurrency.value?.code }
                    ?: filteredList.find { it.code == "RUB" } ?: filteredList.firstOrNull()
                _selectedAnalyticsCurrency.value = filteredList.find { it.code == _selectedAnalyticsCurrency.value?.code }
                    ?: filteredList.find { it.code == "USD" } ?: filteredList.firstOrNull()
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getAllHistory().collect { list -> _history.value = list }
        }
    }

    fun convert(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val amountValue = _amount.value.toDoubleOrNull() ?: 0.0
        if (amountValue <= 0) {
            onError("Дима, сумма не может быть отрицательной или равной нулю!")
            return
        }
        val from = _selectedFromCurrency.value
        val to = _selectedToCurrency.value

        if (from == null || to == null) {
            onError("Дима, одной из валют нет в базе!")
            return
        }
        if (from.rateToRub == 0.0) {
            onError("Дима, курс исходной валюты не может быть равен 0!")
            return
        }
        val fromRate = from.rateToRub
        val toRate = to.rateToRub
        val resultValue = amountValue * fromRate / toRate
        _result.value = "%.2f".format(resultValue)

        lastUsedRate = if (to.code == "RUB") fromRate else fromRate / toRate
        lastUsedFromCurrency = from.code
        lastUsedToCurrency = to.code
        viewModelScope.launch {
            historyRepository.saveConversion(
                ConversionHistory(
                    date = Date(),
                    fromCurrency = from.code,
                    toCurrency = to.code,
                    amount = amountValue,
                    result = resultValue,
                    rate = lastUsedRate,
                    note = _note.value.ifEmpty { null }
                )
            )
            loadHistory()
        }
        onSuccess("Конвертация сохранена в историю!")
        _note.value = ""
    }
    fun quickConvert(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (lastUsedRate == 0.0) {
            onError("Сначала выполните хотя бы одну конвертацию!")
            return
        }
        val amountValue = _amount.value.toDoubleOrNull() ?: 0.0
        if (amountValue <= 0) {
            onError("Дима, сумма не может быть отрицательной или равной нулю!")
            return
        }

        val resultValue = amountValue * lastUsedRate
        _result.value = "%.2f".format(resultValue)

        viewModelScope.launch {
            historyRepository.saveConversion(
                ConversionHistory(
                    date = Date(),
                    fromCurrency = lastUsedFromCurrency,
                    toCurrency = lastUsedToCurrency,
                    amount = amountValue,
                    result = resultValue,
                    rate = lastUsedRate,
                    note = _note.value.ifEmpty { null }
                )
            )
            loadHistory()
        }
        onSuccess("Быстрый пересчёт выполнен!")
    }

    suspend fun showAddCurrencyDialog(context: Context) {
        val editText = android.widget.EditText(context)
        editText.hint = "Код валюты (USD, EUR, CNY)"
        android.app.AlertDialog.Builder(context)
            .setTitle("Добавить валюту")
            .setView(editText)
            .setPositiveButton("Далее") { _, _ ->
                val code = editText.text.toString().uppercase().trim()
                if (code.isEmpty()) {
                    Toast.makeText(context, "Код валюты не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (code !in setOf("USD", "EUR", "CNY")) {
                    Toast.makeText(context, "Можно добавить только USD, EUR или CNY", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val rateEdit = android.widget.EditText(context)
                rateEdit.hint = "Курс к рублю (90.5)"
                rateEdit.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                android.app.AlertDialog.Builder(context)
                    .setTitle("Курс для $code")
                    .setView(rateEdit)
                    .setPositiveButton("Сохранить") { _, _ ->
                        val rate = rateEdit.text.toString().toDoubleOrNull()
                        if (rate == null || rate <= 0) {
                            Toast.makeText(context, "Неверный курс", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        viewModelScope.launch {
                            currencyRepository.updateCurrency(Currency(code, rate))
                            loadCurrencies()
                            Toast.makeText(context, "Валюта $code добавлена", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    suspend fun showEditCurrencyDialog(context: Context, currency: Currency) {
        val rateEdit = android.widget.EditText(context)
        rateEdit.hint = "Новый курс (текущий: ${currency.rateToRub})"
        rateEdit.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        android.app.AlertDialog.Builder(context)
            .setTitle("Редактировать ${currency.code}")
            .setView(rateEdit)
            .setPositiveButton("Сохранить") { _, _ ->
                val rate = rateEdit.text.toString().toDoubleOrNull()
                if (rate == null || rate <= 0) {
                    Toast.makeText(context, "Неверный курс", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModelScope.launch {
                    val updatedCurrency =
                        currency.copy(rateToRub = rate, updatedDate = System.currentTimeMillis())
                    currencyRepository.updateCurrency(updatedCurrency)
                    loadCurrencies()
                    if (_selectedAnalyticsCurrency.value?.code == currency.code) {
                        loadAnalytics()
                    }

                    Toast.makeText(context, "Курс ${currency.code} обновлён", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    suspend fun deleteCurrency(currency: Currency) {
        if (currency.code == "RUB") return
        currencyRepository.deleteCurrency(currency.code)
        loadCurrencies()
    }

    suspend fun updateCurrenciesFromApi(context: Context) {
        try {
            currencyRepository.fetchAndSaveCurrenciesFromApi()
            loadCurrencies()

            if (_selectedAnalyticsCurrency.value != null) {
                loadAnalytics()
            }

            Toast.makeText(context, "Курсы успешно обновлены из API ЦБ РФ!", Toast.LENGTH_SHORT)
                .show()
        } catch (e: UnknownHostException) {
            Toast.makeText(
                context,
                "Нет подключения к интернету. Проверьте сеть.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка загрузки курсов. Попробуйте позже.", Toast.LENGTH_LONG)
                .show()
        }
    }

    suspend fun loadAnalytics() {
        val currency = _selectedAnalyticsCurrency.value ?: return
        val historyList = historyRepository.getHistoryForCurrency(currency.code).first()

        if (historyList.size < 2) {
            _statsText.value = "Недостаточно данных для графика.\nВыполните несколько конвертаций."
            _chartData.value = null
            return
        }

        val entries = mutableListOf<Double>()
        val labels = mutableListOf<String>()
        val dateFormat = java.text.SimpleDateFormat("dd.MM", java.util.Locale.getDefault())

        historyList.forEach { item ->
            if (item.fromCurrency == currency.code) {
                entries.add(item.rate)
            } else if (item.toCurrency == currency.code) {
                entries.add(1.0 / item.rate)
            }
            labels.add(dateFormat.format(item.date))
        }

        val minY = entries.minOrNull()?.toFloat() ?: 0f
        _chartData.value = ChartData(entries = entries, labels = labels, minY = minY)

        val stats = mutableMapOf<String, Int>()
        historyList.forEach { item ->
            stats[item.fromCurrency] = stats.getOrDefault(item.fromCurrency, 0) + 1
            stats[item.toCurrency] = stats.getOrDefault(item.toCurrency, 0) + 1
        }
        val total = stats.values.sum()
        val statsList = stats.entries
            .sortedByDescending { it.value }
            .take(5)
            .map {
                "${it.key}: ${it.value} раз (${it.value * 100.0 / total}%.1f)".replace(
                    ",",
                    "."
                )
            }
        _statsText.value = "Частота использования валют:\n${statsList.joinToString("\n")}"
    }

    suspend fun clearAllHistory(context: Context) {
        historyRepository.clearAllHistory()
        loadHistory()
        _chartData.value = null
        _statsText.value = ""
        Toast.makeText(context, "История очищена", Toast.LENGTH_SHORT).show()
    }

    suspend fun exportHistory(context: Context) {
        val historyList = historyRepository.getAllHistory().first()
        if (historyList.isEmpty()) {
            Toast.makeText(context, "История пуста", Toast.LENGTH_SHORT).show()
            return
        }

        val csv = StringBuilder()
        csv.append("\uFEFF")
        csv.append("Дата;Из;В;Сумма;Результат;Курс;Заметка\n")

        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale("ru"))

        historyList.forEach { item ->
            val date = dateFormat.format(item.date)
            val from = item.fromCurrency
            val to = item.toCurrency
            val amount = "%.2f".format(item.amount).replace('.', ',')
            val result = "%.2f".format(item.result).replace('.', ',')
            val rate = "%.4f".format(item.rate).replace('.', ',')
            val note = item.note?.replace(";", ",") ?: ""

            csv.append("$date;$from;$to;$amount;$result;$rate;$note\n")
        }

        val fileName = "История_конвертаций_${System.currentTimeMillis()}.csv"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Для Android 10+ используем MediaStore
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS)
            }
            val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csv.toString().toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Экспортировано в папку Документы", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(context, "Ошибка экспорта", Toast.LENGTH_SHORT).show()
        } else {
            // Для старых версий Android
            val documentsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
            if (!documentsDir.exists()) documentsDir.mkdirs()
            val file = java.io.File(documentsDir, fileName)
            file.writeText(csv.toString(), Charsets.UTF_8)
            Toast.makeText(context, "Экспортировано в ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}
