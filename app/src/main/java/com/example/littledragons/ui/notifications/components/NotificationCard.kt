package com.example.littledragons.ui.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun NotificationCard(
    title: String,
    timestamp: LocalDateTime,
    role: UserRole?,
    onDelete: () -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .heightIn(min = 76.dp)
                    .weight(1f)
            ) {
                Text(title)

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        timestamp.date.format(
                            LocalDate.Format {
                                byUnicodePattern("dd.MM.yyyy")
                            }
                        ),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        timestamp.time.format(
                            LocalTime.Format {
                                byUnicodePattern("H:mm")
                            }
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (role) {
                UserRole.Parent, null -> {}
                UserRole.Teacher -> {
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
private fun NotificationCardPreview() {
    AppTheme {
        NotificationCard(
            title = "Уведомление",
            timestamp = LocalDateTime(2025, 5, 1, 9, 45, 0, 0),
            role = UserRole.Teacher,
            onDelete = {},
        )
    }
}

@Preview
@Composable
private fun NotificationCardParentPreview() {
    AppTheme {
        NotificationCard(
            title = "Уведомление",
            timestamp = LocalDateTime(2025, 5, 1, 9, 45, 0, 0),
            role = UserRole.Parent,
            onDelete = {},
        )
    }
}