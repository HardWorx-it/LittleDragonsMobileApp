package com.example.littledragons.ui.grades

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.littledragons.model.types.Grade
import com.example.littledragons.model.types.GradeId
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubject
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
import com.example.littledragons.ui.components.CustomExposedDropdownMenu
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.grades.components.AddGradeDialog
import com.example.littledragons.ui.grades.components.GradesTable
import com.example.littledragons.ui.grades.model.AddGradeState
import com.example.littledragons.ui.grades.model.AddGradeViewModel
import com.example.littledragons.ui.grades.model.DeleteGradeResult
import com.example.littledragons.ui.grades.model.GradeItem
import com.example.littledragons.ui.grades.model.GradesState
import com.example.littledragons.ui.grades.model.GradesViewModel
import com.example.littledragons.ui.grades.model.IAddGradeViewModel
import com.example.littledragons.ui.grades.model.IGradesViewModel
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.students.model.IStudentsViewModel
import com.example.littledragons.ui.students.model.StudentsState
import com.example.littledragons.ui.students.model.StudentsViewModel
import com.example.littledragons.ui.subjects.model.ISchoolSubjectsViewModel
import com.example.littledragons.ui.subjects.model.SchoolSubjectsState
import com.example.littledragons.ui.subjects.model.SchoolSubjectsViewModel
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

@Composable
fun GradesPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: GradesViewModel = hiltViewModel(),
    addViewModel: AddGradeViewModel = hiltViewModel(),
    classesViewModel: SchoolClassesViewModel = hiltViewModel(),
    subjectsViewModel: SchoolSubjectsViewModel = hiltViewModel(),
    studentsViewModel: StudentsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = activityViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val appState by appViewModel.state.collectAsStateWithLifecycle()

    Page(
        snackbarHostState = snackbarHostState,
        onBack = navController::navigateUp,
        viewModel = viewModel,
        addViewModel = addViewModel,
        classesViewModel = classesViewModel,
        subjectsViewModel = subjectsViewModel,
        studentsViewModel = studentsViewModel,
        appState = appState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    modifier: Modifier = Modifier,
    viewModel: IGradesViewModel,
    addViewModel: IAddGradeViewModel,
    classesViewModel: ISchoolClassesViewModel,
    subjectsViewModel: ISchoolSubjectsViewModel,
    studentsViewModel: IStudentsViewModel,
    appState: AppState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val state by viewModel.state.collectAsStateWithLifecycle()
    val classesState by classesViewModel.state.collectAsStateWithLifecycle()
    val subjectsState by subjectsViewModel.state.collectAsStateWithLifecycle()
    val studentsState by studentsViewModel.state.collectAsStateWithLifecycle()
    val addState by addViewModel.state.collectAsStateWithLifecycle()
    val refresh by addViewModel.refresh.collectAsStateWithLifecycle(false)

    var showSelectSchoolClassDialog by remember { mutableStateOf(false) }
    var showAddGradeDialog by remember { mutableStateOf(false) }

    val title by remember {
        derivedStateOf {
            when (val u = state.currentUser) {
                is GradesState.UserRole.Parent, null -> context.getString(R.string.grades)
                is GradesState.UserRole.Teacher -> u.schoolClassId
            }
        }
    }

    LaunchedEffect(appState, subjectsState) {
        when (val s1 = appState) {
            is AppState.Loaded if s1.user.role == UserRole.Parent ->
                when (val s2 = subjectsState) {
                    is SchoolSubjectsState.Loaded -> {
                        viewModel.load(
                            user = GradesState.UserRole.Parent(s1.child?.id),
                            subjectId = s2.subjects.first().name!!,
                        )
                    }

                    else -> {}
                }

            else -> {}
        }
    }


    fun onRefresh() {
        when (val s = state) {
            is GradesState.Failed.Error -> viewModel.load(
                user = s.currentUser,
                subjectId = s.currentSubject
            )

            is GradesState.Loaded -> viewModel.load(
                user = s.currentUser,
                subjectId = s.currentSubject
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
            is AddGradeState.InProgress -> showAddGradeDialog = false
            is AddGradeState.Failed ->
                snackbarHostState.showSnackbar(context.getString(R.string.unable_to_add_grade))

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
        Column(
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding(),
                end = 12.dp,
                start = 12.dp
            )
        ) {
            when (val s = appState) {
                is AppState.Loaded if s.user.role == UserRole.Teacher -> {
                    when (state) {
                        !is GradesState.Initial ->
                            AddGrade(
                                state = addState,
                                onClick = { showAddGradeDialog = true }
                            )

                        else -> {}
                    }
                    SelectSchoolClass(onClick = { showSelectSchoolClassDialog = true })
                }

                else -> {}
            }

            when (val s1 = subjectsState) {
                SchoolSubjectsState.Initial, SchoolSubjectsState.Loading -> Loading()
                is SchoolSubjectsState.Failed -> Error(onRefresh = { onRefresh() })
                is SchoolSubjectsState.Loaded -> when (val s2 = state) {
                    is GradesState.Initial -> SelectClassMessage()

                    is GradesState.Loading -> Loading()

                    is GradesState.Failed.Error -> Error(onRefresh = { onRefresh() })

                    is GradesState.Failed.ChildNotFound -> NoChild()

                    is GradesState.Loaded if s2.grades.isEmpty() -> {
                        Spacer(modifier = Modifier.height(8.dp))

                        SelectSubject(
                            state = state,
                            viewModel = viewModel,
                            subjects = s1.subjects,
                        )

                        NoSchedules()
                    }

                    is GradesState.Loaded -> {
                        Spacer(modifier = Modifier.height(8.dp))

                        SelectSubject(
                            state = state,
                            viewModel = viewModel,
                            subjects = s1.subjects,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GradesTable(
                            role = when (s2.currentUser) {
                                is GradesState.UserRole.Parent -> UserRole.Parent
                                is GradesState.UserRole.Teacher -> UserRole.Teacher
                            },
                            grades = s2.grades,
                            onEdit = {
                                val (student, grade) = it

                                addViewModel.setExistsId(grade.id)
                                addViewModel.updateStudent(student)
                                grade.subjectId?.let { addViewModel.updateSubjectId(it) }
                                grade.gradeValue?.let { addViewModel.updateGrade(it) }
                                grade.date?.let { addViewModel.updateDate(it.toLocalDateTime().date) }

                                showAddGradeDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSelectSchoolClassDialog) {
        when (val s = subjectsState) {
            is SchoolSubjectsState.Loaded -> {
                SelectSchoolClassDialog(
                    state = classesState,
                    onSelect = {
                        showSelectSchoolClassDialog = false
                        onSelectClass(
                            appState = appState,
                            subjectId = state.currentSubject ?: s.subjects.first().name!!,
                            viewModel = viewModel,
                            schoolClassId = it,
                        )
                    },
                    onDismissRequest = { showSelectSchoolClassDialog = false }
                )
            }

            else -> {}
        }

    }

    if (showAddGradeDialog) {
        state.currentUser?.let {
            val schoolClassId = when (val u = it) {
                is GradesState.UserRole.Parent -> u.childId
                is GradesState.UserRole.Teacher -> u.schoolClassId
            }
            if (schoolClassId != null) {
                AddGradeDialog(
                    schoolClassId = schoolClassId,
                    viewModel = addViewModel,
                    subjectsState = subjectsState,
                    studentsState = studentsState,
                    onDismissRequest = { showAddGradeDialog = false }
                )
            }
        }
    }
}

private fun onSelectClass(
    appState: AppState,
    subjectId: SchoolSubjectId,
    viewModel: IGradesViewModel,
    schoolClassId: SchoolClassId,
) {
    when (val a = appState) {
        is AppState.Loaded -> viewModel.load(
            user = when (a.user.role!!) {
                UserRole.Parent -> GradesState.UserRole.Parent(childId = a.user.childId)
                UserRole.Teacher -> GradesState.UserRole.Teacher(
                    teacherId = a.user.uid!!,
                    schoolClassId = schoolClassId,
                )
            },
            subjectId = subjectId,
        )

        else -> {}
    }
}

private fun onSelectSubject(
    state: GradesState,
    viewModel: IGradesViewModel,
    subjectId: SchoolSubjectId,
) {
    state.currentUser?.let {
        viewModel.load(
            user = it,
            subjectId = subjectId,
        )
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
            label = { Text(stringResource(R.string.fetch_grades_failed)) },
            onRefresh = onRefresh
        )
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
private fun AddGrade(
    state: AddGradeState,
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
            AddGradeState.InProgress -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            else -> Text(stringResource(R.string.add_grade))
        }
    }
}

@Composable
fun NoSchedules(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            stringResource(R.string.no_grades),
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
            stringResource(R.string.grades_no_child),
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
            stringResource(R.string.select_class_to_show_grades),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun SelectSubject(
    state: GradesState,
    viewModel: IGradesViewModel,
    subjects: List<SchoolSubject>,
    modifier: Modifier = Modifier,
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    CustomExposedDropdownMenu(
        options = subjects.map { it.name ?: stringResource(R.string.no_name) }.toList(),
        value = state.currentSubject ?: subjects.first().name ?: stringResource(R.string.no_name),
        onValueChange = {
            onSelectSubject(
                state = state,
                viewModel = viewModel,
                subjectId = it,
            )
        },
        readOnly = true,
        label = { Text(stringResource(R.string.school_subject)) },
        expanded = subjectMenuExpanded,
        onExpandedChange = { subjectMenuExpanded = it },
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
private fun GradesPagePreview() {
    AppTheme {
        Page(
            onBack = {},
            classesViewModel = object : ISchoolClassesViewModel {
                override val state: StateFlow<SchoolClassesState> =
                    MutableStateFlow(SchoolClassesState.Initial)

            },
            appState = AppState.Loaded(user = UserAccount(role = UserRole.Teacher), child = null),
            viewModel = object : IGradesViewModel {
                override val state: StateFlow<GradesState> = MutableStateFlow(
                    GradesState.Loaded(
                        currentSubject = "Математика",
                        currentUser = GradesState.UserRole.Teacher(
                            teacherId = "id",
                            schoolClassId = "5А"
                        ),
                        grades = listOf(
                            GradeItem(
                                grade = Grade(
                                    id = "1",
                                    teacherId = "id",
                                    classId = "5А",
                                    studentId = "1",
                                    subjectId = "Математика",
                                    gradeValue = 5,
                                    date = LocalDate(2025, 1, 1).toTimestamp()
                                ),
                                student = Student(
                                    id = "1",
                                    firstName = "Иван",
                                    lastName = "Иванов",
                                    classId = "5А"
                                )
                            ),
                            GradeItem(
                                grade = Grade(
                                    id = "2",
                                    teacherId = "id",
                                    classId = "5А",
                                    studentId = "1",
                                    subjectId = "Русский язык",
                                    gradeValue = 5,
                                    date = LocalDate(2025, 1, 1).toTimestamp()
                                ),
                                student = Student(
                                    id = "1",
                                    firstName = "Иван",
                                    lastName = "Иванов",
                                    classId = "5А"
                                )
                            ),
                            GradeItem(
                                grade = Grade(
                                    id = "3",
                                    teacherId = "id",
                                    classId = "5А",
                                    studentId = "1",
                                    subjectId = "Литература",
                                    gradeValue = 5,
                                    date = LocalDate(2025, 1, 2).toTimestamp()
                                ),
                                student = Student(
                                    id = "1",
                                    firstName = "Иван",
                                    lastName = "Иванов",
                                    classId = "5А"
                                )
                            ),
                            GradeItem(
                                grade = Grade(
                                    id = "4",
                                    teacherId = "id",
                                    classId = "5А",
                                    studentId = "2",
                                    subjectId = "Литература",
                                    gradeValue = 5,
                                    date = LocalDate(2025, 1, 3).toTimestamp()
                                ),
                                student = Student(
                                    id = "2",
                                    firstName = "Петр",
                                    lastName = "Петров",
                                    classId = "5А"
                                )
                            )
                        )
                    )
                )
                override val deleteResult: StateFlow<DeleteGradeResult> =
                    MutableStateFlow(DeleteGradeResult.Initial)

                override fun load(user: GradesState.UserRole, subjectId: SchoolSubjectId) {
                }

                override fun delete(item: GradeItem) {
                }
            },
            subjectsViewModel = object : ISchoolSubjectsViewModel {
                override val state: StateFlow<SchoolSubjectsState> =
                    MutableStateFlow(
                        SchoolSubjectsState.Loaded(
                            subjects = listOf(SchoolSubject(name = "5А"))
                        )
                    )
            },
            addViewModel = object : IAddGradeViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val student: Student? = null
                override val grade: Int? = null
                override val gradeIsNotValid: Boolean = false
                override val studentIsNotValid: Boolean = false
                override val subjectIsNotValid: Boolean = false
                override val date: LocalDate = LocalDate.fromEpochDays(0)
                override val refresh: SharedFlow<Boolean> = MutableSharedFlow()
                override val state: StateFlow<AddGradeState> =
                    MutableStateFlow(AddGradeState.Initial)

                override fun setExistsId(value: GradeId?) {}

                override fun setSchoolClassId(value: SchoolClassId) {}

                override fun updateSubjectId(value: String) {}

                override fun updateStudent(value: Student?) {}

                override fun updateDate(value: LocalDate) {}

                override fun updateGrade(value: Int) {}

                override fun submit() {}

                override fun clear() {}
            },
            studentsViewModel = object : IStudentsViewModel {
                override val state: StateFlow<StudentsState> = MutableStateFlow(
                    StudentsState.Loaded(
                        students = listOf(
                            Student(
                                firstName = "Ученик",
                                lastName = "Ученик",
                                classId = "5А"
                            )
                        )
                    )
                )
            },
        )
    }
}