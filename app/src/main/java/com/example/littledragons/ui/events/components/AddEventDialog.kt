package com.example.littledragons.ui.events.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.types.EventId
import com.example.littledragons.ui.components.CustomTextField
import com.example.littledragons.ui.components.DatePickerModal
import com.example.littledragons.ui.events.model.AddEventState
import com.example.littledragons.ui.events.model.IAddEventViewModel
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    viewModel: IAddEventViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        if (viewModel.date.toEpochDays() == 0) {
            viewModel.updateDate(
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            )
        }
    }

    BasicAlertDialog(
        onDismissRequest = {
            viewModel.clear()
            onDismissRequest()
        }
    ) {
        Surface(
            modifier = modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CustomTextField(
                    value = viewModel.name,
                    onValueChange = viewModel::updateName,
                    isError = viewModel.nameIsNotValid,
                    label = { Text(stringResource(R.string.naming)) },
                    supportingText = {
                        if (viewModel.nameIsNotValid) {
                            Text(stringResource(R.string.naming_empty_error))
                        }
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            R.string.date_template,
                            viewModel.date.format(LocalDate.Formats.ISO)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.pick_date)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = {
                        viewModel.clear()
                        onDismissRequest()
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = viewModel::submit) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = {
                it.let {
                    val date = Instant.fromEpochMilliseconds(it!!)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    viewModel.updateDate(date)
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Preview
@Composable
private fun AddEventDialogPreview() {
    AppTheme {
        AddEventDialog(
            object : IAddEventViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val date: LocalDate = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                override val refresh: SharedFlow<Boolean> = MutableSharedFlow()
                override val state: StateFlow<AddEventState> =
                    MutableStateFlow(AddEventState.Initial)

                override fun setExistsId(value: EventId?) {

                }

                override fun updateName(value: String) {
                }

                override fun updateDate(value: LocalDate) {
                }

                override fun submit() {
                }

                override fun clear() {
                }
            },
            onDismissRequest = {},
        )
    }
}