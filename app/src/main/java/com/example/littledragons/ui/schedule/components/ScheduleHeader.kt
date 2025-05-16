package com.example.littledragons.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.littledragons.R
import com.example.littledragons.ui.components.NoRippleInteractionSource
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

private val shape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleHeader(
    currentDay: LocalDate,
    onChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 24.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { onChanged(currentDay.minus(1, DateTimeUnit.WEEK)) }
            ) {
                Text(stringResource(R.string.prev_week))
            }
            OutlinedButton(
                onClick = { onChanged(currentDay.plus(1, DateTimeUnit.WEEK)) }
            ) {
                Text(stringResource(R.string.next_week))
            }
        }
        Spacer(modifier = Modifier.heightIn(12.dp))

        Text(currentDay.format(LocalDate.Formats.ISO))

        Spacer(modifier = Modifier.heightIn(12.dp))

        WeeksList(
            selectedDayOfWeek = currentDay.dayOfWeek,
            onChanged = { dayOfWeek ->
                // Вычисляём количество дней до следующего дня недели
                val todayDayOfWeek = currentDay.dayOfWeek
                val daysUntilTarget = if (todayDayOfWeek < dayOfWeek) {
                    (dayOfWeek.ordinal - todayDayOfWeek.ordinal + 7) % 7
                } else {
                    (todayDayOfWeek.ordinal - dayOfWeek.ordinal + 7) % 7
                }
                // Если дни недели совпадают, игнорируем
                if (daysUntilTarget == 0) {
                    return@WeeksList
                } else if (todayDayOfWeek < dayOfWeek) {
                    onChanged(currentDay.plus(daysUntilTarget, DateTimeUnit.DAY))
                } else {
                    onChanged(currentDay.minus(daysUntilTarget, DateTimeUnit.DAY))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeksList(
    selectedDayOfWeek: DayOfWeek,
    onChanged: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {

    val names: Map<DayOfWeek, Pair<Int, String>> by remember {
        derivedStateOf {
            DayOfWeek.entries.withIndex().associate {
                val (index, dayOfWeek) = it
                dayOfWeek to (index to dayOfWeek.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                ))
            }
        }
    }

    OutlinedCard(shape = shape) {
        PrimaryScrollableTabRow(
            selectedTabIndex = names[selectedDayOfWeek]?.first ?: 0,
            edgePadding = (-10).dp,
            indicator = {
                CustomIndicator(
                    Modifier.tabIndicatorOffset(
                        names[selectedDayOfWeek]?.first ?: 0,
                        matchContentSize = true
                    ),
                )
            },
            divider = {},
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(shape = shape)
        ) {
            for ((dayOfWeek, namePair) in names.entries) {
                val (_, name) = namePair
                Tab(
                    text = {
                        Text(
                            name,
                            color = if (selectedDayOfWeek == dayOfWeek) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    },
                    selected = selectedDayOfWeek == dayOfWeek,
                    onClick = { onChanged(dayOfWeek) },
                    interactionSource = NoRippleInteractionSource(), //Отключить ripple эффект
                    modifier = Modifier
                        .requiredHeight(33.dp)
                        .zIndex(2.0f)
                )
            }
        }
    }
}

@Composable
fun CustomIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier
            .requiredHeight(33.dp)
            .requiredWidth(70.dp)
            .background(
                color = AppPalette.GreenVariant1,
                shape = shape,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
    }
}


@Preview
@Composable
private fun ScheduleHeaderPreview() {
    AppTheme {
        ScheduleHeader(
            currentDay = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
            onChanged = {},
        )
    }
}