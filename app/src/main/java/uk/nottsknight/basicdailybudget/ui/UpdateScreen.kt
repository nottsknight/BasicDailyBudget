package uk.nottsknight.basicdailybudget.ui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    viewModel: UpdateScreenViewModel = viewModel(factory = UpdateScreenViewModel.Factory),
    onNavToSummary: () -> Unit
) {

    val chosenDate =
        rememberDatePickerState(initialSelectedDateMillis = Instant.now().toEpochMilli())

    Column {
        Text(stringResource(R.string.whenNextPayday))
        DatePicker(state = chosenDate)

        Button(onClick = {
            viewModel.updatePayday(chosenDate.selectedDateMillis)
            onNavToSummary()
        }) {
            Text(stringResource(R.string.updateBtn))
        }
    }
}

class UpdateScreenViewModel(private val accountRepo: AccountRepository) : ViewModel() {
    fun updatePayday(date: Long?) = viewModelScope.launch {
        if (date == null) return@launch
        val account = accountRepo.select(0) ?: return@launch
        val account1 = account.copy(nextPayday = Instant.ofEpochMilli(date))
        accountRepo.update(account1)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val db = Room.databaseBuilder(context, BdbDatabase::class.java, "bdb").build()
                val accountRepo = AccountRepository(db.accounts())
                UpdateScreenViewModel(accountRepo)
            }
        }
    }
}