package com.example.littledragons.ui.grades.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.toLocalDateTime
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Grade
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.model.types.name
import com.example.littledragons.ui.grades.model.GradeItem
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.DataTable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format

@Composable
fun GradesTable(
    modifier: Modifier = Modifier,
    onEdit: (GradeItem) -> Unit,
    role: UserRole,
    grades: List<GradeItem>,
) {
    val headerStyle = MaterialTheme.typography.bodyLarge

    val groupedByStudent by remember {
        derivedStateOf {
            grades
                .groupBy { it.student.name }
                .mapValues { (_, grades) ->
                    grades.sortedByDescending { it.grade.date }
                }
        }
    }
    val dates by remember {
        derivedStateOf {
            grades
                .map { it.grade.date }
                .toSet()
                .sortedByDescending { it }
        }
    }

    val dateColumns by remember {
        derivedStateOf {
            dates.map {
                DataColumn {
                    Text(
                        it
                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                            ?.date
                            ?.format(LocalDate.Formats.ISO) ?: stringResource(R.string.no_date),
                        style = headerStyle
                    )
                }
            }
        }
    }

    val firstAndLastNameColumn = listOf(DataColumn {
        Text(
            stringResource(R.string.first_and_last_name_short),
            style = headerStyle,
        )
    })

    DataTable(
        columns = firstAndLastNameColumn + dateColumns,
        headerBackgroundColor = AppPalette.Gray1,
        modifier = modifier
            .fillMaxWidth()
            .background(color = AppPalette.Gray2),
    ) {
        groupedByStudent.forEach { (studentName, items) ->
            row {
                cell {
                    Text(studentName)
                }
                dates.forEach { date ->
                    cell {
                        val item = items.firstOrNull { it.grade.date == date }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (role) {
                                UserRole.Teacher if item != null -> {
                                    IconButton(
                                        onClick = { onEdit(item) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.edit),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                else -> {}
                            }
                            Text(
                                item?.grade?.gradeValue?.toString() ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun GradesTablePreview() {
    AppTheme {
        GradesTable(
            onEdit = {},
            role = UserRole.Teacher,
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
    }
}

@Preview
@Composable
private fun GradesTableParentPreview() {
    AppTheme {
        GradesTable(
            onEdit = {},
            role = UserRole.Parent,
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
    }
}