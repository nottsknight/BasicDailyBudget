package uk.nottsknight.basicdailybudget.ui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import kotlinx.coroutines.launch
import uk.nottsknight.basicdailybudget.model.Account
import uk.nottsknight.basicdailybudget.model.AccountRepository
import uk.nottsknight.basicdailybudget.model.BdbDatabase
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountScreen(
    viewModel: NewAccountViewModel = viewModel(factory = NewAccountViewModel.Factory()),
    onNavToMain: () -> Unit
) {
    var balance = remember { mutableStateOf("0.00") }
    var payday = rememberDatePickerState(initialSelectedDateMillis = Instant.now().toEpochMilli())

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "How much do you have available to spend this month? (Subtract bills, etc.)")
        CurrencyTextField(
            value = balance.value,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { balance.value = it }

        Text(text = "When is your next payday?")
        DateSelector(modifier = Modifier.align(Alignment.CenterHorizontally), state = payday)

        Button(onClick = {
            viewModel.createNewAccount(
                balance.value,
                Instant.ofEpochMilli(payday.selectedDateMillis!!)
            )
            onNavToMain()
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(text = "Create account")
        }
    }
}

class NewAccountViewModel(private val accountRepo: AccountRepository) : ViewModel() {
    fun createNewAccount(balance: String, payday: Instant) {
        val balanceInt = balance.toInt()
        val daysRemaining = ChronoUnit.DAYS.between(Instant.now(), payday)
        val dailySpend = balanceInt / daysRemaining
        val account = Account(0, dailySpend.toInt(), payday)

        viewModelScope.launch {
            accountRepo.insert(account)
        }
    }

    companion object {
        fun Factory() = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val db = Room.databaseBuilder(context, BdbDatabase::class.java, "bdb").build()
                val repo = AccountRepository(db.accounts())
                NewAccountViewModel(repo)
            }
        }
    }
}