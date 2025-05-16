package com.example.littledragons.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.littledragons.R
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.activityViewModel
import com.example.littledragons.ui.components.CustomLargeTopAppBar
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.home.components.MenuList
import com.example.littledragons.ui.home.components.ProfileHeader
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.model.IAppViewModel
import com.example.littledragons.ui.model.ResendEmailVerifyResult
import com.example.littledragons.ui.model.VerifyEmailState
import com.example.littledragons.ui.navigateToRedirectAuth
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: AppViewModel = activityViewModel(),
) {
    Page(
        viewModel = viewModel,
        onNavigate = {
            when (it) {
                AppDestination.Initial -> navController.navigateToRedirectAuth()
                else -> navController.navigate(it)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    viewModel: IAppViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isEmailVerified by viewModel.isEmailVerified.collectAsStateWithLifecycle()
    val resendEmailVerifyResult by viewModel.resendEmailVerifyResult.collectAsStateWithLifecycle()


    LaunchedEffect(
        state,
        onNavigate
    ) {
        when (state) {
            AppState.Failed.NotAuthorized, AppState.Failed.NotRegistered ->
                onNavigate(AppDestination.Initial)

            else -> {}
        }
    }

    LaunchedEffect(resendEmailVerifyResult) {
        when (resendEmailVerifyResult) {
            ResendEmailVerifyResult.Failed -> scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.send_email_verification_failed))
            }

            else -> {}
        }
    }

    CustomScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CustomLargeTopAppBar(
                title = {
                    when (val s = state) {
                        is AppState.Loaded -> s.user.apply {
                            ProfileHeader(
                                firstName = firstName ?: "",
                                lastName = lastName ?: "",
                                avatar = avatar,
                            )
                        }

                        else ->
                            Text(stringResource(R.string.home_page))
                    }
                },
                scrollBehavior = scrollBehavior,
                expandedHeight = 154.dp,
                collapsedHeight = 0.dp
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        when (val s = state) {
            is AppState.Failed.Error ->
                Error(
                    onRefresh = viewModel::load,
                    modifier = modifier
                )

            is AppState.Loaded ->
                MenuList(
                    contentPadding = contentPadding,
                    onClick = onNavigate,
                    userRole = s.user.role,
                    isEmailVerified = isEmailVerified,
                    onSendEmail = viewModel::resendEmailVerify
                )

            else -> Loading()
        }
    }
}


@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        FetchError(
            label = { Text(stringResource(R.string.failed_to_fetch_profile)) },
            onRefresh = onRefresh
        )
    }
}

@Preview
@Composable
private fun HomePagePreview() {
    AppTheme {
        Page(
            viewModel = object : IAppViewModel {
                override val state: StateFlow<AppState> = MutableStateFlow(
                    AppState.Loaded(
                        user = UserAccount(
                            uid = "",
                            role = UserRole.Parent,
                            firstName = "Имя",
                            lastName = "Фамилия",
                            email = "",
                        ),
                        child = Student(firstName = "Ученик", lastName = "Ученик", classId = "5А")
                    )
                )
                override val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult> =
                    MutableStateFlow(
                        ResendEmailVerifyResult.Initial
                    )
                override val isEmailVerified: StateFlow<VerifyEmailState> = MutableStateFlow(
                    VerifyEmailState.Verified
                )

                override fun load(force: Boolean) {}
                override fun resendEmailVerify() {

                }
            },
            onNavigate = {})
    }
}

@Preview
@Composable
private fun HomePagePreviewNotVerified() {
    AppTheme {
        Page(
            viewModel = object : IAppViewModel {
                override val state: StateFlow<AppState> = MutableStateFlow(
                    AppState.Loaded(
                        user = UserAccount(
                            uid = "",
                            role = UserRole.Parent,
                            firstName = "Имя",
                            lastName = "Фамилия",
                            email = "",
                        ),
                        child = Student(firstName = "Ученик", lastName = "Ученик", classId = "5А")
                    )
                )
                override val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult> =
                    MutableStateFlow(
                        ResendEmailVerifyResult.Initial
                    )
                override val isEmailVerified: StateFlow<VerifyEmailState> = MutableStateFlow(
                    VerifyEmailState.NotVerified
                )

                override fun load(force: Boolean) {}
                override fun resendEmailVerify() {

                }
            },
            onNavigate = {})
    }
}

@Preview
@Composable
private fun HomePagePreviewLoading() {
    AppTheme {
        Page(
            viewModel = object : IAppViewModel {
                override val state: StateFlow<AppState> = MutableStateFlow(
                    AppState.Loading
                )
                override val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult> =
                    MutableStateFlow(
                        ResendEmailVerifyResult.Initial
                    )
                override val isEmailVerified: StateFlow<VerifyEmailState> = MutableStateFlow(
                    VerifyEmailState.Verified
                )

                override fun load(force: Boolean) {}
                override fun resendEmailVerify() {

                }
            },
            onNavigate = {})
    }
}

@Preview
@Composable
private fun HomePagePreviewError() {
    AppTheme {
        Page(
            viewModel = object : IAppViewModel {
                override val state: StateFlow<AppState> = MutableStateFlow(
                    AppState.Failed.Error(Exception())
                )
                override val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult> =
                    MutableStateFlow(
                        ResendEmailVerifyResult.Initial
                    )
                override val isEmailVerified: StateFlow<VerifyEmailState> = MutableStateFlow(
                    VerifyEmailState.Verified
                )

                override fun load(force: Boolean) {}
                override fun resendEmailVerify() {

                }
            },
            onNavigate = {})
    }
}