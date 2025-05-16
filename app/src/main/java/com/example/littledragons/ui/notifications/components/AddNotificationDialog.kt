package com.example.littledragons.ui.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.ui.components.CustomTextField
import com.example.littledragons.ui.notifications.model.AddNotificationState
import com.example.littledragons.ui.notifications.model.IAddNotificationViewModel
import com.example.littledragons.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNotificationDialog(
    viewModel: IAddNotificationViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    TextButton(onClick = {
                        viewModel.submit(timestamp = Timestamp.now())
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AddNotificationDialogPreview() {
    AppTheme {
        AddNotificationDialog(
            viewModel = object : IAddNotificationViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val refresh: SharedFlow<Boolean> = MutableStateFlow(false)
                override val state: StateFlow<AddNotificationState> =
                    MutableStateFlow(AddNotificationState.Initial)

                override fun updateName(value: String) {}

                override fun submit(timestamp: Timestamp) {}

                override fun clear() {}

            },
            onDismissRequest = {},
        )
    }
}