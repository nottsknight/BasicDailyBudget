package uk.nottsknight.basicdailybudget.ui

import android.icu.text.DateFormat
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uk.nottsknight.basicdailybudget.R

@Composable
fun CurrencyTextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) = TextField(
    value = value,
    onValueChange = { newValue ->
        newValue.trimStart('0').trim { !it.isDigit() }.let { onValueChange(it) }
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    visualTransformation = CurrencyVisualTransformation(),
    modifier = modifier
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    modifier: Modifier,
    state: DatePickerState,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {

    var showDialog by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {

        Icon(imageVector = Icons.Default.Today, contentDescription = "Choose payday")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(state.selectedDateMillis)
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false; onDismiss() },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onConfirm() }) {
                    Text(stringResource(R.string.okBtn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; onDismiss() }) {
                    Text(stringResource(R.string.cancelBtn))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}