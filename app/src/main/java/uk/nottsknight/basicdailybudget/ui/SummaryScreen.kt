package uk.nottsknight.basicdailybudget.ui

import android.app.Application
import android.icu.text.DateFormat
import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.nottsknight.basicdailybudget.R
import uk.nottsknight.basicdailybudget.model.AccountRepository
import uk.nottsknight.basicdailybudget.model.BdbDatabase
import uk.nottsknight.basicdailybudget.model.PreferencesRepository
import uk.nottsknight.basicdailybudget.model.Spend
import uk.nottsknight.basicdailybudget.model.SpendRepository
import uk.nottsknight.basicdailybudget.preferences
import java.time.Instant
import java.util.Date
import kotlin.properties.Delegates

@Composable
fun SummaryScreen(viewModel: SummaryScreenViewModel = viewModel(factory = SummaryScreenViewModel.Factory)) {
    val dailySpend = viewModel.dailySpend.collectAsState()
    val spendFormatter = DecimalFormat.getCurrencyInstance()

    val nextPayday = viewModel.nextPayday.collectAsState()
    val paydayFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)

    val transactions = viewModel.transactions.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(stringResource(R.string.dailySpendLabel))
        Text(spendFormatter.format(dailySpend.value / 100f))

        Spacer(modifier = Modifier.size(16.dp))

        Text(stringResource(R.string.nextPaydayLabel))
        Text(paydayFormatter.format(Date.from(nextPayday.value)))

        Spacer(modifier = Modifier.size(16.dp))

        Button(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, "Add transaction")
            Text(stringResource(R.string.addTransBtn))
        }

        Spacer(modifier = Modifier.size(16.dp))
        HorizontalDivider()

        if (transactions.value.isEmpty()) {
            Text(
                stringResource(R.string.noTransactions),
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxHeight(), contentPadding = PaddingValues(8.dp)) {
                itemsIndexed(transactions.value, key = { _, spend -> spend.id }) { i, spend ->
                    SpendListItem(spend)
                    if (i < transactions.value.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showDialog) {
            SpendDialog(onDismiss = { showDialog = false }) { spend ->
                showDialog = false

                val df = DecimalFormat.getInstance()
                val spendAmt = df.parse(spend).toDouble() * 100
                viewModel.addTransaction(spendAmt.toInt())
            }
        }
    }
}

@Composable
private fun SpendListItem(spend: Spend) {
    val df = DateFormat.getDateInstance(DateFormat.SHORT)
    val cf = DecimalFormat.getCurrencyInstance().apply { maximumFractionDigits = 2 }
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(df.format(spend.date))
        Spacer(modifier = Modifier.width(10.dp))
        Text(spend.label)
        Text(cf.format(spend.amount / 100f))
    }
}

@Composable
private fun SpendDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var spendAmount by remember { mutableStateOf("0.00") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Column {
                Text(stringResource(R.string.howMuchSpend))
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = spendAmount,
                    onValueChange = { value ->
                        spendAmount = value.trimStart('0').trim { !it.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancelBtn))
                    }

                    Button(onClick = { onConfirm(spendAmount) }) {
                        Text(stringResource(R.string.okBtn))
                    }
                }
            }
        }
    }
}

class SummaryScreenViewModel(
    private val accountRepo: AccountRepository,
    private val spendRepo: SpendRepository,
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    private val dailySpendState = MutableStateFlow(0)
    private val nextPaydayState = MutableStateFlow(Instant.now())
    private val transactionList = MutableStateFlow(listOf<Spend>())
    private var currentAccountId by Delegates.notNull<Int>()

    val dailySpend: StateFlow<Int> get() = dailySpendState
    val nextPayday: StateFlow<Instant> get() = nextPaydayState
    val transactions: StateFlow<List<Spend>> get() = transactionList

    init {
        viewModelScope.launch {
            prefsRepo.currentAccountId.collect { id ->
                currentAccountId = id
                getModelValues()
            }
        }
    }

    private fun getModelValues() = viewModelScope.launch {
        val account = accountRepo.select(currentAccountId) ?: return@launch
        dailySpendState.value = account.dailyAllowance
        nextPaydayState.value = account.nextPayday
        transactionList.value = spendRepo.getAllByAccount(currentAccountId)
    }

    fun addTransaction(amount: Int) = viewModelScope.launch {
        val spend = Spend(0, currentAccountId, Instant.now(), amount, "")
        spendRepo.insert(spend)
        transactionList.value = spendRepo.getAllByAccount(currentAccountId)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val db = Room.databaseBuilder(context, BdbDatabase::class.java, "bdb").build()
                val accountRepo = AccountRepository(db.accounts())
                val spendRepo = SpendRepository(db.spends())
                val prefsRepo = PreferencesRepository(context.preferences)
                SummaryScreenViewModel(accountRepo, spendRepo, prefsRepo)
            }
        }
    }
}