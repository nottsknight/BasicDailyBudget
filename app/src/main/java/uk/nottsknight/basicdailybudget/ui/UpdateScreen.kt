package uk.nottsknight.basicdailybudget.ui

import android.app.Application
import android.icu.text.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import uk.nottsknight.basicdailybudget.R
import uk.nottsknight.basicdailybudget.model.AccountRepository
import uk.nottsknight.basicdailybudget.model.BdbDatabase
import uk.nottsknight.basicdailybudget.model.PreferencesRepository
import uk.nottsknight.basicdailybudget.preferences
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.properties.Delegates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    snackHost: SnackbarHostState,
    viewModel: UpdateScreenViewModel = viewModel(factory = UpdateScreenViewModel.Factory(snackHost))
) {

    val chosenDate = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    var dateChanged by remember { mutableStateOf(false) }
    var newBalance by remember { mutableStateOf("0.00") }
    var balanceChanged by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {

        Text(stringResource(R.string.currentBalance))

        Spacer(modifier = Modifier.height(16.dp))

        CurrencyTextField(value = newBalance) {
            newBalance = it
            balanceChanged = true
        }

        Spacer(modifier = Modifier.size(32.dp))

        Text(stringResource(R.string.whenNextPayday))

        Spacer(modifier = Modifier.height(16.dp))

        DateSelector(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state = chosenDate,
            onConfirm = { dateChanged = true }
        )

        Spacer(modifier = Modifier.size(32.dp))

        Button(
            onClick = {
                if (dateChanged && chosenDate.selectedDateMillis != null) {
                    viewModel.updatePayday(chosenDate.selectedDateMillis!!)
                }
                dateChanged = false

                if (balanceChanged) {
                    viewModel.updateBalance(newBalance)
                }
                balanceChanged = false
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = dateChanged || balanceChanged
        ) {
            Text(stringResource(R.string.updateBtn))
        }
    }
}

class UpdateScreenViewModel(
    private val accountRepo: AccountRepository,
    private val prefsRepo: PreferencesRepository,
    private val snackHost: SnackbarHostState
) : ViewModel() {

    init {
        viewModelScope.launch {
            prefsRepo.currentAccountId.collect {
                currentAccountId = it
            }
        }
    }

    private var currentAccountId by Delegates.notNull<Int>()

    fun updatePayday(date: Long) = viewModelScope.launch {
        val account = accountRepo.select(currentAccountId)
            ?: snackHost.showSnackbar("Failed to get active account").let { return@launch }
        val account1 = account.copy(nextPayday = Instant.ofEpochMilli(date))
        accountRepo.update(account1)
    }

    fun updateBalance(balance: String) = viewModelScope.launch {
        val balanceValue = balance.toDouble() * 100
        val account = accountRepo.select(currentAccountId)
            ?: snackHost.showSnackbar("Failed to get active account").let { return@launch }

        val daysToPayday = ChronoUnit.DAYS.between(Instant.now(), account.nextPayday)
        val account1 = account.copy(dailyAllowance = (balanceValue / daysToPayday).toInt())
        accountRepo.update(account1)
    }

    companion object {
        fun Factory(snackHost: SnackbarHostState) = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val db = Room.databaseBuilder(context, BdbDatabase::class.java, "bdb").build()
                val accountRepo = AccountRepository(db.accounts())
                val prefsRepo = PreferencesRepository(context.preferences)
                UpdateScreenViewModel(accountRepo, prefsRepo, snackHost)
            }
        }
    }
}