package com.example.littledragons.ui.events.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.getMonthNames
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(FormatStringsInDatetimeFormats::class, ExperimentalLayoutApi::class)
@Composable
fun EventCard(
    title: String,
    date: LocalDate,
    role: UserRole?,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .heightIn(min = 76.dp)
                    .weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge
                )

                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            date.format(
                                LocalDate.Format {
                                    dayOfMonth()
                                    char(' ')
                                    monthName(MonthNames(getMonthNames(Locale.getDefault().language)))
                                },
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            date.dayOfWeek.getDisplayName(
                                TextStyle.FULL_STANDALONE,
                                Locale.getDefault()
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (role) {
                UserRole.Parent, null -> {}
                UserRole.Teacher -> {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun EventCardPreview() {
    AppTheme {
        EventCard(
            title = "Название",
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            role = UserRole.Teacher,
            onDelete = {},
            onEdit = {}
        )
    }
}

@Preview
@Composable
private fun EventCardParentPreview() {
    AppTheme {
        EventCard(
            title = "Название",
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            role = UserRole.Parent,
            onDelete = {},
            onEdit = {}
        )
    }
}