package com.example.littledragons.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.littledragons.R
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.components.CustomTextField
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.IAppViewModel
import com.example.littledragons.ui.profile.model.IProfileViewModel

@Composable
fun ProfileForm(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    buttons: @Composable ColumnScope.() -> Unit,
    viewModel: IProfileViewModel,
    appViewModel: IAppViewModel,
) {
    val state by appViewModel.state.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        header()

        CustomTextField(
            value = if (LocalInspectionMode.current) "example@example.org" else viewModel.email,
            onValueChange = viewModel::updateEmail,
            label = { Text(stringResource(R.string.email_field_label)) },
            isError = viewModel.emailIsNotValid,
            supportingText = {
                if (viewModel.emailIsNotValid) {
                    Text(stringResource(R.string.invalid_email))
                }
            }
        )

        when (val s = state) {
            is AppState.Loaded if s.user.role == UserRole.Parent -> {
                CustomTextField(
                    value = if (LocalInspectionMode.current) "Имя Фамилия" else viewModel.childName,
                    onValueChange = viewModel::updateChildName,
                    label = { Text(stringResource(R.string.child_first_last_name)) },
                    isError = viewModel.childNameIsNotValid,
                    supportingText = {
                        if (viewModel.childNameIsNotValid) {
                            Text(stringResource(R.string.name_field_error))
                        }
                    }
                )

                CustomTextField(
                    value = if (LocalInspectionMode.current) "5А" else viewModel.childSchoolClass,
                    onValueChange = viewModel::updateChildSchoolClass,
                    label = { Text(stringResource(R.string.child_school_class)) },
                )
            }

            else -> {}
        }

        buttons()
    }
}