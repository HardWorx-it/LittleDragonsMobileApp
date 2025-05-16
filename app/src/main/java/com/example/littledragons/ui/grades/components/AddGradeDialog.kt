package com.example.littledragons.ui.grades.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.types.GradeId
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubject
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.name
import com.example.littledragons.ui.components.CustomExposedDropdownMenu
import com.example.littledragons.ui.components.DatePickerModal
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.grades.model.AddGradeState
import com.example.littledragons.ui.grades.model.IAddGradeViewModel
import com.example.littledragons.ui.students.model.StudentsState
import com.example.littledragons.ui.subjects.model.SchoolSubjectsState
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGradeDialog(
    schoolClassId: SchoolClassId,
    viewModel: IAddGradeViewModel,
    subjectsState: SchoolSubjectsState,
    studentsState: StudentsState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        viewModel.setSchoolClassId(schoolClassId)

        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
        if (viewModel.date.toEpochDays() == 0) {
            viewModel.updateDate(now.date)
        }
    }

    BasicAlertDialog(
        onDismissRequest = {
            viewModel.clear()
            onDismissRequest()
        },
    ) {
        Surface(
            modifier = modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            when (val s1 = subjectsState) {
                is SchoolSubjectsState.Failed -> Error()
                SchoolSubjectsState.Initial, SchoolSubjectsState.Loading -> Loading()
                is SchoolSubjectsState.Loaded ->
                    when (val s2 = studentsState) {
                        is StudentsState.Failed -> Error()
                        StudentsState.Initial, StudentsState.Loading -> Loading()
                        is StudentsState.Loaded -> Body(
                            schoolClassId = schoolClassId,
                            subjects = s1.subjects,
                            students = s2.students,
                            viewModel = viewModel,
                            onShowDatePicker = { showDatePicker = it },
                            onDismissRequest = onDismissRequest,
                        )
                    }
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = {
                it.let {
                    val date = Instant.fromEpochMilliseconds(it!!)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    viewModel.updateDate(date)
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun Body(
    schoolClassId: SchoolClassId,
    subjects: List<SchoolSubject>,
    students: List<Student>,
    viewModel: IAddGradeViewModel,
    onShowDatePicker: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var studentMenuExpanded by remember { mutableStateOf(false) }
    var gradesMenuExpanded by remember { mutableStateOf(false) }

    val studentsMap: Map<String, Student> by remember {
        derivedStateOf {
            students.filter { it.classId == schoolClassId }
                .associateBy { it.name }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        CustomExposedDropdownMenu(
            options = studentsMap.keys.toList(),
            value = viewModel.student?.name ?: "",
            onValueChange = { fullName ->
                val student = studentsMap[fullName]
                viewModel.updateStudent(student)
            },
            readOnly = true,
            label = { Text(stringResource(R.string.student)) },
            isError = viewModel.studentIsNotValid,
            supportingText = {
                if (viewModel.studentIsNotValid) {
                    Text(stringResource(R.string.student_empty_error))
                }
            },
            expanded = studentMenuExpanded,
            onExpandedChange = { studentMenuExpanded = it },
        )

        CustomExposedDropdownMenu(
            options = subjects.map { it.name ?: stringResource(R.string.no_name) }.toList(),
            value = viewModel.subjectId,
            onValueChange = viewModel::updateSubjectId,
            readOnly = true,
            label = { Text(stringResource(R.string.school_subject)) },
            isError = viewModel.subjectIsNotValid,
            supportingText = {
                if (viewModel.subjectIsNotValid) {
                    Text(stringResource(R.string.naming_empty_error))
                }
            },
            expanded = subjectMenuExpanded,
            onExpandedChange = { subjectMenuExpanded = it },
        )

        CustomExposedDropdownMenu(
            options = (2..5).map { it.toString() }.toList(),
            value = viewModel.grade?.toString() ?: "",
            onValueChange = {
                viewModel.updateGrade(it.toInt())
            },
            readOnly = true,
            label = { Text(stringResource(R.string.grade)) },
            isError = viewModel.gradeIsNotValid,
            supportingText = {
                if (viewModel.gradeIsNotValid) {
                    Text(stringResource(R.string.grade_empty_error))
                }
            },
            expanded = gradesMenuExpanded,
            onExpandedChange = { gradesMenuExpanded = it },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    R.string.date_template,
                    viewModel.date.format(LocalDate.Formats.ISO),
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onShowDatePicker(true) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.pick_date)
                )
            }
        }

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
            TextButton(onClick = viewModel::submit) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        FetchError(
            label = { Text(stringResource(R.string.fetch_school_subjects_error)) },
        )
    }
}

@Preview
@Composable
private fun AddStudentDialogPreview() {
    AppTheme {
        AddGradeDialog(
            viewModel = object : IAddGradeViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val student: Student? = null
                override val grade: Int? = null
                override val gradeIsNotValid: Boolean = false
                override val studentIsNotValid: Boolean = false
                override val subjectIsNotValid: Boolean = false
                override val date: LocalDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
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
            onDismissRequest = {},
            schoolClassId = "5А",
            studentsState = StudentsState.Loaded(
                students = listOf(
                    Student(
                        firstName = "Ученик",
                        lastName = "Ученик",
                        classId = "5А"
                    )
                )
            ),
            subjectsState = SchoolSubjectsState.Loaded(
                subjects = listOf(
                    SchoolSubject(name = "Математика"),
                    SchoolSubject(name = "Русский язык")
                )
            ),
        )
    }
}

@Preview
@Composable
private fun AddStudentDialogLoadingPreview() {
    AppTheme {
        AddGradeDialog(
            viewModel = object : IAddGradeViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val student: Student? = null
                override val grade: Int? = null
                override val gradeIsNotValid: Boolean = false
                override val studentIsNotValid: Boolean = false
                override val subjectIsNotValid: Boolean = false
                override val date: LocalDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
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
            schoolClassId = "5А",
            studentsState = StudentsState.Loaded(
                students = listOf(
                    Student(
                        firstName = "Ученик",
                        lastName = "Ученик",
                        classId = "5А"
                    )
                )
            ),
            subjectsState = SchoolSubjectsState.Initial,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun AddStudentDialogErrorPreview() {
    AppTheme {
        AddGradeDialog(
            viewModel = object : IAddGradeViewModel {
                override val subjectId: SchoolSubjectId = ""
                override val student: Student? = null
                override val grade: Int? = null
                override val gradeIsNotValid: Boolean = false
                override val studentIsNotValid: Boolean = false
                override val subjectIsNotValid: Boolean = false
                override val date: LocalDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
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
            onDismissRequest = {},
            schoolClassId = "5А",
            studentsState = StudentsState.Loaded(
                students = listOf(
                    Student(
                        firstName = "Ученик",
                        lastName = "Ученик",
                        classId = "5А"
                    )
                )
            ),
            subjectsState = SchoolSubjectsState.Failed(Exception()),
        )
    }
}