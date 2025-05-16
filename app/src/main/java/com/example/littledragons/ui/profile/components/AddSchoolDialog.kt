package com.example.littledragons.ui.profile.components

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
import com.example.littledragons.ui.profile.model.AddSchoolClassState
import com.example.littledragons.ui.profile.model.IAddSchoolClassViewModel
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchoolDialog(
    viewModel: IAddSchoolClassViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
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
                    label = { Text(stringResource(R.string.school_class_name)) },
                    supportingText = {
                        if (viewModel.nameIsNotValid) {
                            Text(stringResource(R.string.invalid_school_class_name))
                        }
                    },
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = viewModel::submit) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AddSchoolDialogPreview() {
    AppTheme {
        AddSchoolDialog(
            object : IAddSchoolClassViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val state: StateFlow<AddSchoolClassState> =
                    MutableStateFlow(AddSchoolClassState.Initial)

                override fun updateName(value: String) {
                }

                override fun submit() {
                }
            },
            onDismissRequest = {}
        )
    }
}