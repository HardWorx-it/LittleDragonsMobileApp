package com.example.littledragons.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.littledragons.R
import com.example.littledragons.ui.auth.model.ILoginViewModel
import com.example.littledragons.ui.auth.model.LoginState
import com.example.littledragons.ui.auth.model.LoginViewModel
import com.example.littledragons.ui.components.CustomButton
import com.example.littledragons.ui.components.CustomLargeTopAppBar
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTextField
import com.example.littledragons.ui.customPasswordVisualTransformation
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.routes.AuthDestination
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Page(
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Page(
    viewModel: ILoginViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    CustomScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CustomLargeTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.welcome_message),
                        modifier = Modifier.offset(y = (-80).dp)
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Form(
            contentPadding = contentPadding,
            viewModel = viewModel,
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
    viewModel: ILoginViewModel,
    onShowSnackbar: (String) -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    val context = LocalContext.current
    val loginState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Failed -> onShowSnackbar(context.getString(R.string.login_failed))
            is LoginState.Success -> onNavigate(AppDestination.Home)
            else -> {}
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(35.dp),
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 30.dp, vertical = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CustomTextField(
            value = if (LocalInspectionMode.current) "name123@gmail.com"
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
            value = if (LocalInspectionMode.current) "12345678"
            else
                viewModel.password,
            onValueChange = viewModel::updatePassword,
            label = {
                Text(stringResource(R.string.password))
            },
            supportingText = {
                if (viewModel.passwordIsNotValid) {
                    Text(stringResource(R.string.empty_password))
                }
            },
            isError = viewModel.passwordIsNotValid,
            visualTransformation = customPasswordVisualTransformation,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.weight(1f))
        SubmitButton(
            onSubmit = viewModel::submit,
            registerState = loginState,
        )
        TextButton(onClick = {
            onNavigate(AuthDestination.Register)
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.register))
        }
    }
}

@Composable
private fun SubmitButton(
    onSubmit: () -> Unit,
    registerState: LoginState
) {
    CustomButton(
        onClick = onSubmit,
        enabled = when (registerState) {
            LoginState.InProgress, is LoginState.Success -> false
            LoginState.Initial, is LoginState.Failed -> true
        },
        contentPadding = when (registerState) {
            LoginState.InProgress -> PaddingValues(8.dp)
            else -> PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        },
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        when (registerState) {
            LoginState.InProgress -> CircularProgressIndicator()
            else -> Text(stringResource(R.string.login))
        }
    }
}

@Preview
@Composable
private fun LoginPagePreview() {
    AppTheme {
        Page(
            viewModel = object : ILoginViewModel {
                override val email: String
                    get() = ""
                override val password: String
                    get() = ""
                override val emailIsNotValid: Boolean
                    get() = false
                override val passwordIsNotValid: Boolean
                    get() = false
                override val state: StateFlow<LoginState>
                    get() = MutableStateFlow(LoginState.Initial)

                override fun updateEmail(value: String) {
                }

                override fun updatePassword(value: String) {
                }

                override fun submit() {
                }
            },
            onNavigate = {},
        );
    }
}