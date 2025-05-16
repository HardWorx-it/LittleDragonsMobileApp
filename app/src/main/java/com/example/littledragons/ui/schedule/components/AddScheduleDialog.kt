package com.example.littledragons.ui.schedule.components

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
import com.example.littledragons.model.types.ScheduleId
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubject
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.ui.components.CustomExposedDropdownMenu
import com.example.littledragons.ui.components.DatePickerModal
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.components.TimePickerModal
import com.example.littledragons.ui.schedule.model.AddScheduleState
import com.example.littledragons.ui.schedule.model.IAddScheduleViewModel
import com.example.littledragons.ui.subjects.model.SchoolSubjectsState
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

private enum class TimePickerMode {
    StartTime,
    EndTime,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    schoolClassId: SchoolClassId,
    viewModel: IAddScheduleViewModel,
    subjectsState: SchoolSubjectsState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker: TimePickerMode? by remember { mutableStateOf(null) }


    LaunchedEffect(true) {
        viewModel.setSchoolClassId(schoolClassId)

        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val truncatedSeconds = now.date.atTime(now.hour, now.minute)

        if (viewModel.startTime.date.toEpochDays() == 0) {
            viewModel.updateStartTime(truncatedSeconds)
        }
        if (viewModel.endTime.date.toEpochDays() == 0) {
            viewModel.updateEndTime(truncatedSeconds)
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
            when (val s = subjectsState) {
                is SchoolSubjectsState.Failed -> Error()
                SchoolSubjectsState.Initial, SchoolSubjectsState.Loading -> Loading()
                is SchoolSubjectsState.Loaded ->
                    Body(
                        subjects = s.subjects,
                        viewModel = viewModel,
                        onShowDatePicker = { showDatePicker = it },
                        onShowTimePicker = { showTimePicker = it },
                        onDismissRequest = onDismissRequest,
                    )
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
                    viewModel.updateStartTime(date.atTime(viewModel.startTime.time))
                    viewModel.updateEndTime(date.atTime(viewModel.endTime.time))
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker != null) {
        TimePickerModal(
            onTimeSelected = { hour, minute ->
                when (showTimePicker) {
                    TimePickerMode.StartTime ->
                        viewModel.updateStartTime(viewModel.startTime.date.atTime(hour, minute))

                    TimePickerMode.EndTime ->
                        viewModel.updateEndTime(viewModel.endTime.date.atTime(hour, minute))

                    null -> {}
                }
            },
            onDismiss = { showTimePicker = null }
        )
    }
}

@Composable
private fun Body(
    subjects: List<SchoolSubject>,
    viewModel: IAddScheduleViewModel,
    onShowDatePicker: (Boolean) -> Unit,
    onShowTimePicker: (TimePickerMode) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        CustomExposedDropdownMenu(
            options = subjects.map { it.name ?: stringResource(R.string.no_name) }.toList(),
            value = viewModel.subjectId,
            onValueChange = viewModel::updateSubjectId,
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    R.string.date_template,
                    viewModel.startTime.date.format(LocalDate.Formats.ISO),
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    R.string.start_time_template,
                    viewModel.startTime.time.format(LocalTime.Formats.ISO),
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onShowTimePicker(TimePickerMode.StartTime) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.pick_time)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    R.string.end_time_template,
                    viewModel.endTime.time.format(LocalTime.Formats.ISO),
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onShowTimePicker(TimePickerMode.EndTime) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.pick_time)
                )
            }
        }

        if (viewModel.timeRangeIsNotValid) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(R.string.invalid_time_range),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
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
            TextButton(onClick = {
                viewModel.submit(
                    createSubject = subjects.find { it.name == viewModel.subjectId } == null
                )
            }) {
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
        AddScheduleDialog(
            viewModel = object : IAddScheduleViewModel {
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

                override fun updateStartTime(value: LocalDateTime) {

                }

                override fun updateEndTime(value: LocalDateTime) {
                }


                override fun submit(createSubject: Boolean) {}
                override fun clear() {

                }

            },
            onDismissRequest = {},
            schoolClassId = "5А",
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
        AddScheduleDialog(
            viewModel = object : IAddScheduleViewModel {
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

                override fun updateStartTime(value: LocalDateTime) {

                }

                override fun updateEndTime(value: LocalDateTime) {
                }


                override fun submit(createSubject: Boolean) {}
                override fun clear() {

                }

            },
            onDismissRequest = {},
            schoolClassId = "5А",
            subjectsState = SchoolSubjectsState.Initial,
        )
    }
}

@Preview
@Composable
private fun AddStudentDialogErrorPreview() {
    AppTheme {
        AddScheduleDialog(
            viewModel = object : IAddScheduleViewModel {
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

                override fun updateStartTime(value: LocalDateTime) {

                }

                override fun updateEndTime(value: LocalDateTime) {
                }


                override fun submit(createSubject: Boolean) {}
                override fun clear() {

                }

            },
            onDismissRequest = {},
            schoolClassId = "5А",
            subjectsState = SchoolSubjectsState.Failed(Exception()),
        )
    }
}