package uk.nottsknight.basicdailybudget.ui

import android.app.Application
import android.icu.text.DateFormat
import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import java.time.Instant
import java.util.Date

@Composable
fun SummaryScreen(viewModel: SummaryScreenViewModel = viewModel(factory = SummaryScreenViewModel.Factory)) {
    val dailySpend = viewModel.dailySpend.collectAsState()
    val spendFormatter = DecimalFormat.getCurrencyInstance()

    val nextPayday = viewModel.nextPayday.collectAsState()
    val paydayFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {

        Text(stringResource(R.string.dailySpendLabel))
        Text(spendFormatter.format(dailySpend.value / 100f))

        Spacer(modifier = Modifier.size(32.dp))

        Text(stringResource(R.string.nextPaydayLabel))
        Text(paydayFormatter.format(Date.from(nextPayday.value)))
    }
}

class SummaryScreenViewModel(private val accountRepo: AccountRepository) : ViewModel() {
    private val dailySpendState = MutableStateFlow(0)
    private val nextPaydayState = MutableStateFlow(Instant.now())

    val dailySpend: StateFlow<Int> get() = dailySpendState
    val nextPayday: StateFlow<Instant> get() = nextPaydayState

    init {
        getModelValues()
    }

    private fun getModelValues() = viewModelScope.launch {
        val account = accountRepo.select(0) ?: return@launch
        dailySpendState.value = account.dailyAllowance
        nextPaydayState.value = account.nextPayday
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val db = Room.databaseBuilder(context, BdbDatabase::class.java, "bdb").build()
                val accountRepo = AccountRepository(db.accounts())
                SummaryScreenViewModel(accountRepo)
            }
        }
    }
}