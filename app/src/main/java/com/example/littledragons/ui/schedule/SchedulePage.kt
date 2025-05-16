package com.example.littledragons.ui.schedule

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
import androidx.compose.runtime.derivedStateOf
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
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Schedule
import com.example.littledragons.model.types.ScheduleId
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.activityViewModel
import com.example.littledragons.ui.classes.components.SelectSchoolClassDialog
import com.example.littledragons.ui.classes.model.ISchoolClassesViewModel
import com.example.littledragons.ui.classes.model.SchoolClassesState
import com.example.littledragons.ui.classes.model.SchoolClassesViewModel
import com.example.littledragons.ui.components.BackButton
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.schedule.components.AddScheduleDialog
import com.example.littledragons.ui.schedule.components.ScheduleCard
import com.example.littledragons.ui.schedule.components.ScheduleHeader
import com.example.littledragons.ui.schedule.model.AddScheduleState
import com.example.littledragons.ui.schedule.model.AddScheduleViewModel
import com.example.littledragons.ui.schedule.model.DeleteScheduleResult
import com.example.littledragons.ui.schedule.model.IAddScheduleViewModel
import com.example.littledragons.ui.schedule.model.ISchedulesViewModel
import com.example.littledragons.ui.schedule.model.SchedulesState
import com.example.littledragons.ui.schedule.model.SchedulesViewModel
import com.example.littledragons.ui.subjects.model.ISchoolSubjectsViewModel
import com.example.littledragons.ui.subjects.model.SchoolSubjectsState
import com.example.littledragons.ui.subjects.model.SchoolSubjectsViewModel
import com.example.littledragons.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun SchedulePage(
    modifier: Modifier = Modifier,
    viewModel: SchedulesViewModel = hiltViewModel(),
    appViewModel: AppViewModel = activityViewModel(),
    classesViewModel: SchoolClassesViewModel = hiltViewModel(),
    subjectsViewModel: SchoolSubjectsViewModel = hiltViewModel(),
    addViewModel: AddScheduleViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val appState by appViewModel.state.collectAsStateWithLifecycle()

    Page(
        snackbarHostState = snackbarHostState,
        onBack = navController::navigateUp,
        appState = appState,
        addViewModel = addViewModel,
        modifier = modifier,
        viewModel = viewModel,
        classesViewModel = classesViewModel,
        subjectsViewModel = subjectsViewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    modifier: Modifier = Modifier,
    viewModel: ISchedulesViewModel,
    classesViewModel: ISchoolClassesViewModel,
    subjectsViewModel: ISchoolSubjectsViewModel,
    appState: AppState,
    addViewModel: IAddScheduleViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val state by viewModel.state.collectAsStateWithLifecycle()
    val classesState by classesViewModel.state.collectAsStateWithLifecycle()
    val subjectsState by subjectsViewModel.state.collectAsStateWithLifecycle()
    val addState by addViewModel.state.collectAsStateWithLifecycle()
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()
    val refresh by addViewModel.refresh.collectAsStateWithLifecycle(false)

    var currentDay by remember {
        mutableStateOf(
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        )
    }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showSelectSchoolClassDialog by remember { mutableStateOf(false) }

    val title by remember {
        derivedStateOf {
            when (val u = state.currentUser) {
                is SchedulesState.UserRole.Parent, null -> context.getString(R.string.schedule)
                is SchedulesState.UserRole.Teacher -> u.schoolClassId
            }
        }
    }

    LaunchedEffect(appState) {
        when (val s = appState) {
            is AppState.Loaded if s.user.role == UserRole.Parent -> {
                viewModel.load(
                    user = SchedulesState.UserRole.Parent(s.child),
                    timeRange = dateToRange(currentDay)
                )
            }

            else -> {}
        }
    }

    fun onRefresh() {
        when (val s = state) {
            is SchedulesState.Failed.Error -> viewModel.load(
                user = s.currentUser,
                timeRange = dateToRange(currentDay),
            )

            is SchedulesState.Loaded -> viewModel.load(
                user = s.currentUser,
                timeRange = dateToRange(currentDay),
            )

            else -> {}
        }
    }

    LaunchedEffect(refresh) {
        if (refresh) {
            onRefresh()
        }
    }

    LaunchedEffect(addState) {
        when (addState) {
            is AddScheduleState.InProgress -> showAddScheduleDialog = false
            is AddScheduleState.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_add_schedule))

            else -> {}
        }
    }

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is DeleteScheduleResult.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_delete_schedule))

            else -> {}
        }
    }

    CustomScaffold(
        topBar = {
            CustomTopAppBar(
                title = { Text(title, maxLines = 1) },
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
                top = contentPadding.calculateTopPadding(),
                end = 12.dp,
                start = 12.dp
            )
        ) {
            when (val s = appState) {
                is AppState.Loaded if s.user.role == UserRole.Teacher -> {
                    item {
                        when (state) {
                            !is SchedulesState.Initial ->
                                AddSchedule(
                                    state = addState,
                                    onClick = { showAddScheduleDialog = true }
                                )

                            else -> {}
                        }
                    }
                    item {
                        SelectSchoolClass(onClick = { showSelectSchoolClassDialog = true })
                    }
                }

                else -> {}
            }

            item {
                ScheduleHeader(
                    currentDay = currentDay,
                    onChanged = {
                        currentDay = it
                        onRefresh()
                    }
                )
            }

            when (val s = state) {
                SchedulesState.Initial -> item {
                    SelectClassMessage()
                }

                is SchedulesState.Loading -> item {
                    Loading()
                }

                is SchedulesState.Failed.Error -> item {
                    Error(onRefresh = { onRefresh() })
                }

                SchedulesState.Failed.ChildNotFound -> item {
                    NoChild()
                }

                is SchedulesState.Loaded if s.schedules.isEmpty() -> item {
                    NoSchedules()
                }

                is SchedulesState.Loaded -> items(s.schedules.size) { index ->
                    val schedule = s.schedules[index]
                    val defaultTime = LocalDate.fromEpochDays(0).atTime(
                        LocalTime.fromMillisecondOfDay(0)
                    )
                    val startDate = schedule.startTime?.toLocalDateTime() ?: defaultTime
                    val endDate = schedule.endTime?.toLocalDateTime() ?: defaultTime

                    ScheduleCard(
                        schoolSubject = schedule.subjectId ?: stringResource(R.string.no_name),
                        dateRange = startDate to endDate,
                        role = when (val s = appState) {
                            is AppState.Loaded -> s.user.role
                            else -> null
                        },
                        onDelete = {
                            viewModel.delete(schedule)
                        },
                        onEdit = {
                            addViewModel.setExistsId(schedule.id)
                            schedule.subjectId?.let { addViewModel.updateSubjectId(it) }
                            schedule.classId?.let { addViewModel.setSchoolClassId(it) }
                            schedule.startTime?.let { addViewModel.updateStartTime(it.toLocalDateTime()) }
                            schedule.endTime?.let { addViewModel.updateEndTime(it.toLocalDateTime()) }
                            showAddScheduleDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showAddScheduleDialog) {
        state.currentUser?.let {
            val schoolClassId = when (val u = it) {
                is SchedulesState.UserRole.Parent -> u.child?.classId
                is SchedulesState.UserRole.Teacher -> u.schoolClassId
            }
            if (schoolClassId != null) {
                AddScheduleDialog(
                    schoolClassId = schoolClassId,
                    viewModel = addViewModel,
                    subjectsState = subjectsState,
                    onDismissRequest = { showAddScheduleDialog = false }
                )
            }
        }
    }

    if (showSelectSchoolClassDialog) {
        SelectSchoolClassDialog(
            state = classesState,
            onSelect = {
                showSelectSchoolClassDialog = false
                onSelect(
                    appState = appState,
                    viewModel = viewModel,
                    schoolClassId = it,
                    currentDay = currentDay
                )
            },
            onDismissRequest = { showSelectSchoolClassDialog = false }
        )
    }
}

private fun onSelect(
    appState: AppState,
    viewModel: ISchedulesViewModel,
    schoolClassId: SchoolClassId,
    currentDay: LocalDate
) {
    when (val a = appState) {
        is AppState.Loaded -> viewModel.load(
            user = when (a.user.role!!) {
                UserRole.Parent -> SchedulesState.UserRole.Parent(child = a.child)
                UserRole.Teacher -> SchedulesState.UserRole.Teacher(
                    teacherId = a.user.uid!!,
                    schoolClassId = schoolClassId,
                )
            },
            timeRange = dateToRange(currentDay),
        )

        else -> {}
    }
}

private fun dateToRange(date: LocalDate): Pair<Timestamp, Timestamp> =
    date.toTimestamp() to date.plus(1, DateTimeUnit.DAY).toTimestamp()

@Composable
private fun AddSchedule(
    state: AddScheduleState,
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
            AddScheduleState.InProgress -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            else -> Text(stringResource(R.string.add_schedule))
        }
    }
}

@Composable
private fun SelectSchoolClass(
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
        Text(stringResource(R.string.select_school_class))
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
            label = { Text(stringResource(R.string.fetch_schedule_failed)) },
            onRefresh = onRefresh
        )
    }
}

@Composable
fun NoSchedules(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.no_schedules),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun NoChild(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.schedules_no_child),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun SelectClassMessage(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.select_class_to_show_schedule),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview
@Composable
private fun SchedulePagePreview() {
    AppTheme {
        Page(
            addViewModel = object : IAddScheduleViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val subjectIsNotValid: Boolean = false
                override val timeRangeIsNotValid: Boolean = false
                override val startTime: LocalDateTime = LocalDateTime(2025, 5, 1, 9, 0, 0, 0)
                override val endTime: LocalDateTime = LocalDateTime(2025, 5, 1, 9, 45, 0, 0)
                override val refresh: SharedFlow<Boolean> = MutableSharedFlow()
                override val state: StateFlow<AddScheduleState> =
                    MutableStateFlow(AddScheduleState.Initial)

                override fun setExistsId(value: ScheduleId?) {}

                override fun setSchoolClassId(value: SchoolClassId) {}

                override fun updateSubjectId(value: String) {}

                override fun updateStartTime(value: LocalDateTime) {}

                override fun updateEndTime(value: LocalDateTime) {}

                override fun submit(createSubject: Boolean) {}
                override fun clear() {}

            },
            viewModel = object : ISchedulesViewModel {
                override val state: StateFlow<SchedulesState> =
                    MutableStateFlow(
                        SchedulesState.Loaded(
                            currentUser = SchedulesState.UserRole.Teacher(
                                teacherId = "id",
                                schoolClassId = "5А"
                            ),
                            timeRange = LocalDateTime(2025, 5, 1, 0, 0, 0, 0).toTimestamp()
                                    to LocalDateTime(2025, 5, 31, 0, 0, 0, 0).toTimestamp(),
                            schedules = listOf(
                                Schedule(
                                    subjectId = "Математика",
                                    classId = "5А",
                                    startTime = LocalDateTime(2025, 5, 1, 9, 0, 0, 0).toTimestamp(),
                                    endTime = LocalDateTime(2025, 5, 1, 9, 45, 0, 0).toTimestamp()
                                )
                            )
                        )
                    )
                override val deleteResult: StateFlow<DeleteScheduleResult> =
                    MutableStateFlow(DeleteScheduleResult.Initial)

                override fun load(
                    user: SchedulesState.UserRole,
                    timeRange: Pair<Timestamp, Timestamp>
                ) {
                }

                override fun delete(schedule: Schedule) {}

            },
            classesViewModel = object : ISchoolClassesViewModel {
                override val state: StateFlow<SchoolClassesState> =
                    MutableStateFlow(SchoolClassesState.Initial)

            },
            subjectsViewModel = object : ISchoolSubjectsViewModel {
                override val state: StateFlow<SchoolSubjectsState> =
                    MutableStateFlow(SchoolSubjectsState.Initial)
            },
            onBack = {},
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Teacher), child = null),
        )
    }
}

@Preview
@Composable
private fun SchedulePageParentPreview() {
    AppTheme {
        Page(
            addViewModel = object : IAddScheduleViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val subjectIsNotValid: Boolean = false
                override val timeRangeIsNotValid: Boolean = false
                override val startTime: LocalDateTime = LocalDateTime(2025, 5, 1, 9, 0, 0, 0)
                override val endTime: LocalDateTime = LocalDateTime(2025, 5, 1, 9, 45, 0, 0)
                override val refresh: SharedFlow<Boolean> = MutableSharedFlow()
                override val state: StateFlow<AddScheduleState> =
                    MutableStateFlow(AddScheduleState.Initial)

                override fun setExistsId(value: ScheduleId?) {}

                override fun setSchoolClassId(value: SchoolClassId) {}

                override fun updateSubjectId(value: String) {}

                override fun updateStartTime(value: LocalDateTime) {}

                override fun updateEndTime(value: LocalDateTime) {}

                override fun submit(createSubject: Boolean) {}
                override fun clear() {}

            },
            viewModel = object : ISchedulesViewModel {
                override val state: StateFlow<SchedulesState> =
                    MutableStateFlow(
                        SchedulesState.Loaded(
                            currentUser = SchedulesState.UserRole.Parent(
                                child = Student(
                                    firstName = "Ученик",
                                    lastName = "Ученик",
                                    classId = "5А"
                                )
                            ),
                            timeRange = LocalDateTime(2025, 5, 1, 0, 0, 0, 0).toTimestamp()
                                    to LocalDateTime(2025, 5, 31, 0, 0, 0, 0).toTimestamp(),
                            schedules = listOf(
                                Schedule(
                                    subjectId = "Математика",
                                    classId = "5А",
                                    startTime = LocalDateTime(2025, 5, 1, 9, 0, 0, 0).toTimestamp(),
                                    endTime = LocalDateTime(2025, 5, 1, 9, 45, 0, 0).toTimestamp()
                                )
                            )
                        )
                    )
                override val deleteResult: StateFlow<DeleteScheduleResult> =
                    MutableStateFlow(DeleteScheduleResult.Initial)

                override fun load(
                    user: SchedulesState.UserRole,
                    timeRange: Pair<Timestamp, Timestamp>
                ) {
                }


                override fun delete(schedule: Schedule) {}

            },
            classesViewModel = object : ISchoolClassesViewModel {
                override val state: StateFlow<SchoolClassesState> =
                    MutableStateFlow(SchoolClassesState.Initial)

            },
            subjectsViewModel = object : ISchoolSubjectsViewModel {
                override val state: StateFlow<SchoolSubjectsState> =
                    MutableStateFlow(SchoolSubjectsState.Initial)
            },
            onBack = {},
            appState = AppState.Loaded(
                user = UserAccount(role = UserRole.Parent),
                child = Student(firstName = "Ученик", lastName = "Ученик", classId = "5А")
            ),
        )
    }
}

@Preview
@Composable
private fun SchedulePageParentNoChildPreview() {
    AppTheme {
        Page(
            addViewModel = object : IAddScheduleViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val subjectIsNotValid: Boolean = false
                override val timeRangeIsNotValid: Boolean = false
                override val startTime: LocalDateTime = LocalDateTime(2025, 5, 1, 9, 0, 0, 0)
                override val endTime: LocalDateTime = LocalDateTime(2025, 5, 1, 9, 45, 0, 0)
                override val refresh: SharedFlow<Boolean> = MutableSharedFlow()
                override val state: StateFlow<AddScheduleState> =
                    MutableStateFlow(AddScheduleState.Initial)

                override fun setExistsId(value: ScheduleId?) {}

                override fun setSchoolClassId(value: SchoolClassId) {}

                override fun updateSubjectId(value: String) {}

                override fun updateStartTime(value: LocalDateTime) {}

                override fun updateEndTime(value: LocalDateTime) {}

                override fun submit(createSubject: Boolean) {}
                override fun clear() {}

            },
            viewModel = object : ISchedulesViewModel {
                override val state: StateFlow<SchedulesState> =
                    MutableStateFlow(SchedulesState.Failed.ChildNotFound)
                override val deleteResult: StateFlow<DeleteScheduleResult> =
                    MutableStateFlow(DeleteScheduleResult.Initial)

                override fun load(
                    user: SchedulesState.UserRole,
                    timeRange: Pair<Timestamp, Timestamp>
                ) {
                }

                override fun delete(schedule: Schedule) {}

            },
            classesViewModel = object : ISchoolClassesViewModel {
                override val state: StateFlow<SchoolClassesState> =
                    MutableStateFlow(SchoolClassesState.Initial)

            },
            subjectsViewModel = object : ISchoolSubjectsViewModel {
                override val state: StateFlow<SchoolSubjectsState> =
                    MutableStateFlow(SchoolSubjectsState.Initial)
            },
            onBack = {},
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Parent), child = null),
        )
    }
}