package com.example.littledragons.ui.auth

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.littledragons.R
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.auth.model.IRegisterViewModel
import com.example.littledragons.ui.auth.model.RegisterState
import com.example.littledragons.ui.auth.model.RegisterViewModel
import com.example.littledragons.ui.components.BackButton
import com.example.littledragons.ui.components.CustomButton
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTextField
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.customPasswordVisualTransformation
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.routes.AuthDestination
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun RegisterPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    Page(
        onBack = navController::navigateUp,
        viewModel = viewModel,
        onNavigate = {
            if (it == AppDestination.Home) {
                navController.navigate(it) {
                    popUpTo(AuthDestination.Login) {
                        inclusive = true
                    }
                }
            } else {
                navController.navigate(it)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Page(
    modifier: Modifier = Modifier,
    viewModel: IRegisterViewModel,
    onBack: () -> Unit,
    onNavigate: (AppDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val userOptions = mapOf(
        UserRole.Parent to stringResource(R.string.i_am_parent),
        UserRole.Teacher to stringResource(R.string.i_am_teacher)
    )

    CustomScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CustomTopAppBar(
                title = { Text(stringResource(R.string.registration)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Form(
            contentPadding,
            viewModel,
            userOptions,
            onNavigate = onNavigate,
            onShowSnackbar = {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                }
            })
    }
}

@Composable
private fun Form(
    contentPadding: PaddingValues,
    viewModel: IRegisterViewModel,
    userOptions: Map<UserRole, String>,
    onShowSnackbar: (String) -> Unit,
    onNavigate: (AppDestination) -> Unit,
) {
    val context = LocalContext.current
    val registerState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Failed.Error -> onShowSnackbar(context.getString(R.string.register_failed))
            is RegisterState.Failed.EmailVerification -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.send_email_verification_failed),
                    Toast.LENGTH_LONG
                ).show()
                onNavigate(AppDestination.Home)
            }

            is RegisterState.Success -> onNavigate(AppDestination.Home)
            else -> {}
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 30.dp, vertical = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CustomTextField(
            value = if (LocalInspectionMode.current)
                "Иван Иванов"
            else
                viewModel.name,
            onValueChange = viewModel::updateName,
            label = {
                Text(stringResource(R.string.first_and_last_name))
            },
            isError = viewModel.nameIsNotValid,
            supportingText = {
                if (viewModel.nameIsNotValid) {
                    Text(stringResource(R.string.name_field_error))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        CustomTextField(
            value = if (LocalInspectionMode.current)
                "name123@gmail.com"
            else
                viewModel.email,
            onValueChange = viewModel::updateEmail,
            label = {
                Text(stringResource(R.string.email_field_label))
            },
            supportingText = {
                if (viewModel.emailIsNotValid) {
                    Text(stringResource(R.string.invalid_email))
                }
            },
            isError = viewModel.emailIsNotValid,
            modifier = Modifier.fillMaxWidth(),
        )
        CustomTextField(
            value = if (LocalInspectionMode.current)
                "12345678"
            else
                viewModel.password,
            onValueChange = viewModel::updatePassword,
            label = {
                Text(stringResource(R.string.password))
            },
            supportingText = {
                if (viewModel.passwordIsNotValid) {
                    Text(stringResource(R.string.invalid_password))
                }
            },
            isError = viewModel.passwordIsNotValid,
            visualTransformation = customPasswordVisualTransformation,
            modifier = Modifier.fillMaxWidth(),
        )
        CustomTextField(
            value = if (LocalInspectionMode.current)
                "12345678"
            else
                viewModel.passwordRepeat,
            onValueChange = viewModel::updatePasswordRepeat,
            label = {
                Text(context.getString(R.string.repeat_password))
            },
            supportingText = {
                if (viewModel.passwordRepeatIsNotValid) {
                    Text(context.getString(R.string.password_mismatch))
                }
            },
            isError = viewModel.passwordRepeatIsNotValid,
            visualTransformation = customPasswordVisualTransformation,
            modifier = Modifier.fillMaxWidth(),
        )
        UserTypeOptions(
            options = userOptions.values.toList(),
            selectedOption = userOptions[viewModel.userRole]!!,
            onOptionSelected = {
                when (it) {
                    userOptions[UserRole.Parent] ->
                        viewModel.updateUserRole(UserRole.Parent)

                    userOptions[UserRole.Teacher] ->
                        viewModel.updateUserRole(UserRole.Teacher)
                }
            },
        )
        Spacer(modifier = Modifier.weight(1f))
        SubmitButton(
            onSubmit = viewModel::submit,
            registerState = registerState
        )
    }
}

@Composable
private fun SubmitButton(
    onSubmit: () -> Unit,
    registerState: RegisterState
) {
    CustomButton(
        onClick = onSubmit,
        enabled = when (registerState) {
            RegisterState.InProgress, is RegisterState.Success, is RegisterState.Failed.EmailVerification -> false
            RegisterState.Initial, is RegisterState.Failed.Error -> true
        },
        contentPadding = when (registerState) {
            RegisterState.InProgress -> PaddingValues(8.dp)
            else -> PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        },
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        when (registerState) {
            RegisterState.InProgress -> CircularProgressIndicator()
            else -> Text(stringResource(R.string.register))
        }
    }
}

@Composable
fun UserTypeOptions(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.selectableGroup()) {
        options.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null рекомендуется для обеспечения доступности с помощью программ чтения с экрана
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun RegisterPagePreview() {
    AppTheme {
        Page(
            viewModel = object : IRegisterViewModel {
                override val name: String = ""
                override val email: String = ""
                override val password: String = ""
                override val passwordRepeat: String = ""
                override val userRole: UserRole = UserRole.Parent
                override val nameIsNotValid: Boolean = false
                override val emailIsNotValid: Boolean = false
                override val passwordIsNotValid: Boolean = false
                override val passwordRepeatIsNotValid: Boolean = false
                override val state: StateFlow<RegisterState> =
                    MutableStateFlow(RegisterState.Initial)

                override fun updateName(value: String) {
                }

                override fun updateEmail(value: String) {
                }

                override fun updatePassword(value: String) {
                }

                override fun updatePasswordRepeat(value: String) {

                }

                override fun updateUserRole(value: UserRole) {
                }

                override fun submit() {
                }

            },
            onBack = {},
            onNavigate = {}
        )
    }
}