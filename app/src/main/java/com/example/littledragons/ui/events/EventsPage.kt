package com.example.littledragons.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.littledragons.model.getFirstAndLastDayOfMonth
import com.example.littledragons.model.toLocalDateTime
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Event
import com.example.littledragons.model.types.EventId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.activityViewModel
import com.example.littledragons.ui.components.BackButton
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.events.components.AddEventDialog
import com.example.littledragons.ui.events.components.EventCalendar
import com.example.littledragons.ui.events.components.EventCard
import com.example.littledragons.ui.events.model.AddEventState
import com.example.littledragons.ui.events.model.AddEventViewModel
import com.example.littledragons.ui.events.model.DeleteEventResult
import com.example.littledragons.ui.events.model.EventsState
import com.example.littledragons.ui.events.model.EventsViewModel
import com.example.littledragons.ui.events.model.IAddEventViewModel
import com.example.littledragons.ui.events.model.IEventsViewModel
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun EventsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel = activityViewModel(),
    viewModel: EventsViewModel = hiltViewModel(),
    addViewModel: AddEventViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val appState by appViewModel.state.collectAsStateWithLifecycle()

    Page(
        onBack = navController::navigateUp,
        snackbarHostState = snackbarHostState,
        appState = appState,
        addViewModel = addViewModel,
        viewModel = viewModel,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    appState: AppState,
    viewModel: IEventsViewModel,
    addViewModel: IAddEventViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var showAddDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val addState by addViewModel.state.collectAsStateWithLifecycle()
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()
    val refresh by addViewModel.refresh.collectAsStateWithLifecycle(false)

    LaunchedEffect(true) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val (first, last) = getFirstAndLastDayOfMonth(
            year = now.year,
            month = now.month
        )
        viewModel.setDateRange(first.toTimestamp() to last.toTimestamp())
        viewModel.load()
    }

    LaunchedEffect(refresh) {
        if (refresh) {
            viewModel.load(force = true)
        }
    }

    LaunchedEffect(addState) {
        when (addState) {
            is AddEventState.InProgress -> showAddDialog = false
            is AddEventState.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_add_event))

            else -> {}
        }
    }

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is DeleteEventResult.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_delete_event))

            else -> {}
        }
    }

    CustomScaffold(
        topBar = {
            CustomTopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                end = 12.dp,
                start = 12.dp
            )
        ) {
            when (val s = appState) {
                is AppState.Loaded if s.user.role == UserRole.Teacher ->
                    item {
                        AddEventButton(
                            state = addState,
                            onClick = { showAddDialog = true }
                        )
                    }

                else -> {}
            }

            item {
                EventCalendar(
                    events = when (val s = state) {
                        is EventsState.Loaded -> s.events
                        else -> listOf()
                    },
                    onMonthChanged = {
                        val (first, last) = getFirstAndLastDayOfMonth(
                            year = it.yearMonth.year,
                            month = it.yearMonth.month
                        )
                        viewModel.setDateRange(first.toTimestamp() to last.toTimestamp())
                        viewModel.load(force = true)
                    }
                )
            }

            item {
                Text(
                    stringResource(R.string.events),
                    modifier = Modifier.padding(12.dp)
                )
            }

            when (val s = state) {
                EventsState.Initial, EventsState.Loading -> item {
                    Loading()
                }

                is EventsState.Failed -> item {
                    Error(onRefresh = viewModel::load)
                }

                is EventsState.Loaded if s.events.isEmpty() -> item {
                    NoEvents()
                }

                is EventsState.Loaded -> items(s.events.size) { index ->
                    val event = s.events[index]
                    EventCard(
                        title = event.title ?: stringResource(R.string.no_name),
                        date = event.date?.toLocalDateTime()?.date ?: LocalDate.fromEpochDays(0),
                        role = when (val s = appState) {
                            is AppState.Loaded -> s.user.role
                            else -> null
                        },
                        onDelete = {
                            viewModel.delete(event)
                        },
                        onEdit = {
                            addViewModel.setExistsId(event.id)
                            event.title?.let { addViewModel.updateName(it) }
                            event.date?.let { addViewModel.updateDate(it.toLocalDateTime().date) }
                            showAddDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            viewModel = addViewModel,
            onDismissRequest = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddEventButton(
    state: AddEventState,
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
            AddEventState.InProgress -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            else -> Text(stringResource(R.string.add_event))
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
            label = { Text(stringResource(R.string.failed_to_fetch_events)) },
            onRefresh = onRefresh
        )
    }
}

@Composable
fun NoEvents(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.no_events),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun EventsPagePreview() {
    AppTheme {
        Page(
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Teacher), child = null),
            viewModel = object : IEventsViewModel {
                override val state: StateFlow<EventsState> =
                    MutableStateFlow(
                        EventsState.Loaded(
                            listOf(
                                Event(
                                    id = "id",
                                    title = "Название",
                                    date = Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .toTimestamp(),
                                )
                            )
                        )
                    )
                override val deleteResult: StateFlow<DeleteEventResult> =
                    MutableStateFlow(DeleteEventResult.Initial)

                override fun setDateRange(range: Pair<Timestamp, Timestamp>) {

                }

                override fun load(force: Boolean) {}
                override fun delete(event: Event) {
                }
            },
            addViewModel = object : IAddEventViewModel {
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
            onBack = {}
        )
    }
}

@Preview
@Composable
private fun EventsPageParentPreview() {
    AppTheme {
        Page(
            appState = AppState.Loaded(
                user = UserAccount(role = UserRole.Parent),
                child = Student(firstName = "Ученик", lastName = "Ученик", classId = "5А")
            ),
            viewModel = object : IEventsViewModel {
                override val deleteResult: StateFlow<DeleteEventResult> =
                    MutableStateFlow(DeleteEventResult.Initial)

                override fun setDateRange(range: Pair<Timestamp, Timestamp>) {

                }

                override val state: StateFlow<EventsState> =
                    MutableStateFlow(
                        EventsState.Loaded(
                            listOf(
                                Event(
                                    id = "id",
                                    title = "Название",
                                    date = Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .toTimestamp(),
                                )
                            )
                        )
                    )

                override fun load(force: Boolean) {}
                override fun delete(event: Event) {
                }
            },
            addViewModel = object : IAddEventViewModel {
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
            onBack = {}
        )
    }
}

@Preview
@Composable
private fun EventsPageNoEventsPreview() {
    AppTheme {
        Page(
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Teacher), child = null),
            viewModel = object : IEventsViewModel {
                override val state: StateFlow<EventsState> =
                    MutableStateFlow(
                        EventsState.Loaded(
                            listOf()
                        )
                    )
                override val deleteResult: StateFlow<DeleteEventResult> =
                    MutableStateFlow(DeleteEventResult.Initial)

                override fun setDateRange(range: Pair<Timestamp, Timestamp>) {
                }

                override fun load(force: Boolean) {}
                override fun delete(event: Event) {
                }
            },
            addViewModel = object : IAddEventViewModel {
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
            onBack = {}
        )
    }
}