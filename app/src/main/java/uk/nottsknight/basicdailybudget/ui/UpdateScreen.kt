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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    viewModel: UpdateScreenViewModel = viewModel(factory = UpdateScreenViewModel.Factory),
    onNavToSummary: () -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }
    val chosenDate = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli(),
        initialDisplayMode = DisplayMode.Input
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {

        Text(stringResource(R.string.whenNextPayday))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {

            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Choose payday")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(chosenDate.selectedDateMillis)
            )
        }

        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.okBtn))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancelBtn))
                    }
                }
            ) {
                DatePicker(state = chosenDate)
            }
        }

        Spacer(modifier = Modifier.size(32.dp))

        Button(
            onClick = {
                if (chosenDate.selectedDateMillis != null) {
                    viewModel.updatePayday(chosenDate.selectedDateMillis!!)
                }
                onNavToSummary()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.updateBtn))
        }
    }
}

class UpdateScreenViewModel(private val accountRepo: AccountRepository) : ViewModel() {
    fun updatePayday(date: Long) = viewModelScope.launch {
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