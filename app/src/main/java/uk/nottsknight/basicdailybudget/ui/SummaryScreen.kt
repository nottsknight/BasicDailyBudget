package uk.nottsknight.basicdailybudget.ui

import android.app.Application
import android.icu.text.DateFormat
import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import uk.nottsknight.basicdailybudget.model.Spend
import uk.nottsknight.basicdailybudget.model.SpendRepository
import java.time.Instant
import java.util.Date

@Composable
fun SummaryScreen(viewModel: SummaryScreenViewModel = viewModel(factory = SummaryScreenViewModel.Factory)) {
    val dailySpend = viewModel.dailySpend.collectAsState()
    val spendFormatter = DecimalFormat.getCurrencyInstance()

    val nextPayday = viewModel.nextPayday.collectAsState()
    val paydayFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)

    val transactions = viewModel.transactions.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {

        Text(stringResource(R.string.dailySpendLabel))
        Text(spendFormatter.format(dailySpend.value / 100f))

        Spacer(modifier = Modifier.size(16.dp))

        Text(stringResource(R.string.nextPaydayLabel))
        Text(paydayFormatter.format(Date.from(nextPayday.value)))

        Spacer(modifier = Modifier.size(16.dp))
        HorizontalDivider()

        if (transactions.value.isEmpty()) {
            Text("No transactions yet", modifier = Modifier.align(Alignment.Start))
        } else {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(transactions.value, key = { it.id }) {
                    SpendListItem(it)
                    HorizontalDivider()
                }
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

class SummaryScreenViewModel(
    private val accountRepo: AccountRepository,
    private val spendRepo: SpendRepository
) : ViewModel() {

    private val dailySpendState = MutableStateFlow(0)
    private val nextPaydayState = MutableStateFlow(Instant.now())
    private val transactionList = MutableStateFlow(listOf<Spend>())

    val dailySpend: StateFlow<Int> get() = dailySpendState
    val nextPayday: StateFlow<Instant> get() = nextPaydayState
    val transactions: StateFlow<List<Spend>> get() = transactionList

    init {
        getModelValues()
    }

    private fun getModelValues() = viewModelScope.launch {
        val account = accountRepo.select(0) ?: return@launch
        dailySpendState.value = account.dailyAllowance
        nextPaydayState.value = account.nextPayday
        transactionList.value = spendRepo.getAllByAccount(0)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val db = Room.databaseBuilder(context, BdbDatabase::class.java, "bdb").build()
                val accountRepo = AccountRepository(db.accounts())
                val spendRepo = SpendRepository(db.spends())
                SummaryScreenViewModel(accountRepo, spendRepo)
            }
        }
    }
}