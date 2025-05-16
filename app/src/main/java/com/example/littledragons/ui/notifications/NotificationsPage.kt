package com.example.littledragons.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.littledragons.R
import com.example.littledragons.model.toLocalDateTime
import com.example.littledragons.model.types.Notification
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.activityViewModel
import com.example.littledragons.ui.components.BackButton
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.notifications.components.AddNotificationDialog
import com.example.littledragons.ui.notifications.components.NotificationCard
import com.example.littledragons.ui.notifications.model.AddNotificationState
import com.example.littledragons.ui.notifications.model.AddNotificationViewModel
import com.example.littledragons.ui.notifications.model.IAddNotificationViewModel
import com.example.littledragons.ui.notifications.model.NotificationsState
import com.example.littledragons.ui.notifications.model.model.DeleteNotificationResult
import com.example.littledragons.ui.notifications.model.model.INotificationsViewModel
import com.example.littledragons.ui.notifications.model.model.NotificationsViewModel
import com.example.littledragons.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime

@Composable
fun NotificationsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel = activityViewModel(),
    viewModel: NotificationsViewModel = hiltViewModel(),
    addViewModel: AddNotificationViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val appState by appViewModel.state.collectAsStateWithLifecycle()

    Page(
        snackbarHostState = snackbarHostState,
        onBack = navController::navigateUp,
        appState = appState,
        viewModel = viewModel,
        addViewModel = addViewModel,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: INotificationsViewModel,
    addViewModel: IAddNotificationViewModel,
    appState: AppState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var showAddDialog by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val addState by addViewModel.state.collectAsStateWithLifecycle()
    val refresh by addViewModel.refresh.collectAsStateWithLifecycle(false)
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.load()
    }

    LaunchedEffect(refresh) {
        if (refresh) {
            viewModel.load(force = true)
        }
    }

    LaunchedEffect(addState) {
        when (addState) {
            is AddNotificationState.InProgress -> showAddDialog = false
            is AddNotificationState.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_add_notification))

            else -> {}
        }
    }

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is DeleteNotificationResult.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_delete_notification))

            else -> {}
        }
    }

    CustomScaffold(
        topBar = {
            CustomTopAppBar(
                title = { Text(stringResource(R.string.notifications)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                end = 12.dp,
                start = 12.dp
            )
        ) {
            when (val s = appState) {
                is AppState.Loaded if s.user.role == UserRole.Teacher ->
                    AddButton(
                        state = addState,
                        onClick = { showAddDialog = true }
                    )

                else -> {}
            }

            when (val s = state) {
                NotificationsState.Initial, NotificationsState.Loading -> Loading()

                is NotificationsState.Failed -> Error(onRefresh = viewModel::load)

                is NotificationsState.Loaded if s.notifications.isEmpty() -> NoNotifications()

                is NotificationsState.Loaded ->
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.notifications.size) { index ->
                            val notification = s.notifications[index]
                            NotificationCard(
                                title = notification.title ?: stringResource(R.string.no_name),
                                timestamp = notification.timestamp?.toLocalDateTime(TimeZone.currentSystemDefault())
                                    ?: LocalDate.fromEpochDays(0)
                                        .atTime(LocalTime.fromMillisecondOfDay(0)),
                                role = when (val s = appState) {
                                    is AppState.Loaded -> s.user.role
                                    else -> null
                                },
                                onDelete = {
                                    viewModel.delete(notification)
                                },
                            )
                        }
                    }
            }
        }

        if (showAddDialog) {
            AddNotificationDialog(
                viewModel = addViewModel,
                onDismissRequest = { showAddDialog = false },
            )
        }
    }
}

@Composable
private fun AddButton(
    state: AddNotificationState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        when (state) {
            AddNotificationState.InProgress -> CircularProgressIndicator(
                modifier = Modifier.padding(
                    8.dp
                )
            )

            else -> Text(stringResource(R.string.add))
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
            label = { Text(stringResource(R.string.failed_to_fetch_notifications)) },
            onRefresh = onRefresh
        )
    }
}

@Composable
fun NoNotifications(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.no_notifications),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun NotificationsPagePreview() {
    AppTheme {
        Page(
            onBack = {},
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Teacher), child = null),
            viewModel = object : INotificationsViewModel {
                override val state: StateFlow<NotificationsState> = MutableStateFlow(
                    NotificationsState.Loaded(
                        notifications = listOf(
                            Notification(
                                title = "Уведомление",
                                timestamp = Timestamp.now()
                            )
                        )
                    )
                )
                override val deleteResult: StateFlow<DeleteNotificationResult> = MutableStateFlow(
                    DeleteNotificationResult.Initial
                )

                override fun load(force: Boolean) {
                }

                override fun delete(notification: Notification) {
                }
            },
            addViewModel = object : IAddNotificationViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val refresh: SharedFlow<Boolean> = MutableStateFlow(false)
                override val state: StateFlow<AddNotificationState> =
                    MutableStateFlow(AddNotificationState.Initial)

                override fun updateName(value: String) {}

                override fun submit(timestamp: Timestamp) {}

                override fun clear() {}

            },
        )
    }
}

@Preview
@Composable
private fun NotificationsPageParentPreview() {
    AppTheme {
        Page(
            onBack = {},
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Parent), child = null),
            viewModel = object : INotificationsViewModel {
                override val state: StateFlow<NotificationsState> = MutableStateFlow(
                    NotificationsState.Loaded(
                        notifications = listOf(
                            Notification(
                                title = "Уведомление",
                                timestamp = Timestamp.now()
                            )
                        )
                    )
                )
                override val deleteResult: StateFlow<DeleteNotificationResult> = MutableStateFlow(
                    DeleteNotificationResult.Initial
                )

                override fun load(force: Boolean) {
                }

                override fun delete(notification: Notification) {
                }
            },
            addViewModel = object : IAddNotificationViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val refresh: SharedFlow<Boolean> = MutableStateFlow(false)
                override val state: StateFlow<AddNotificationState> =
                    MutableStateFlow(AddNotificationState.Initial)

                override fun updateName(value: String) {}

                override fun submit(timestamp: Timestamp) {}

                override fun clear() {}

            },
        )
    }
}

@Preview
@Composable
private fun NotificationsPageNoNotificationsPreview() {
    AppTheme {
        Page(
            onBack = {},
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Teacher), child = null),
            viewModel = object : INotificationsViewModel {
                override val state: StateFlow<NotificationsState> = MutableStateFlow(
                    NotificationsState.Loaded(
                        notifications = listOf()
                    )
                )
                override val deleteResult: StateFlow<DeleteNotificationResult> = MutableStateFlow(
                    DeleteNotificationResult.Initial
                )

                override fun load(force: Boolean) {
                }

                override fun delete(notification: Notification) {
                }
            },
            addViewModel = object : IAddNotificationViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val refresh: SharedFlow<Boolean> = MutableStateFlow(false)
                override val state: StateFlow<AddNotificationState> =
                    MutableStateFlow(AddNotificationState.Initial)

                override fun updateName(value: String) {}

                override fun submit(timestamp: Timestamp) {}

                override fun clear() {}

            },
        )
    }
}