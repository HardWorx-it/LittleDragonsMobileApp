package com.example.littledragons.ui.events.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Event
import com.example.littledragons.ui.theme.AppTheme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun EventCalendar(
    modifier: Modifier = Modifier,
    events: List<Event>,
    initialMonth: YearMonth = YearMonth.now(),
    onMonthChanged: (CalendarMonth) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val startMonth = remember { initialMonth.minusMonths(100) }
    val endMonth = remember { initialMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(state.firstVisibleMonth) {
        onMonthChanged(state.firstVisibleMonth)
    }

    val eventsMap by remember(events) {
        derivedStateOf {
            events.associateBy { it.date }
        }
    }

    HorizontalCalendar(
        state = state,
        monthHeader = {
            Header(
                onPrevMonth = {
                    scope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                    }
                },
                onNextMonth = {
                    scope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                    }
                },
                month = it,
            )
        },
        dayContent = {
            val timestamp = it.date.toKotlinLocalDate().toTimestamp()
            Day(
                day = it,
                event = eventsMap[timestamp]
            )
        },
        modifier = modifier,
    )
}

@Composable
fun ColumnScope.Header(
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    month: CalendarMonth,
) {
    val (yearMonth) = month
    val daysOfWeek = daysOfWeek()

    Month(
        onPrevMonth = onPrevMonth,
        yearMonth = yearMonth,
        onNextMonth = onNextMonth
    )

    Spacer(modifier = Modifier.height(16.dp))

    Weeks(daysOfWeek = daysOfWeek)

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun Weeks(
    daysOfWeek: List<DayOfWeek>,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        for (day in daysOfWeek) {
            Text(
                day.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Month(
    onPrevMonth: () -> Unit,
    yearMonth: YearMonth,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonsModifier = Modifier.size(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(
                painterResource(R.drawable.arrow_back_ios_24px),
                contentDescription = stringResource(R.string.prev_month),
                modifier = buttonsModifier
            )
        }

        Text(
            yearMonth.format(DateTimeFormatter.ofPattern("MMM y")),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                painterResource(R.drawable.arrow_back_ios_24px),
                contentDescription = stringResource(R.string.next_month),
                modifier = buttonsModifier.rotate(180.0f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Day(
    modifier: Modifier = Modifier,
    day: CalendarDay,
    event: Event?
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    val (date, position) = day

    val colorScheme = MaterialTheme.colorScheme

    val (color, contentColor) = if (event != null) {
        colorScheme.primary to colorScheme.onPrimary
    } else if (date.dayOfWeek.value == 7) {
        colorScheme.tertiary to colorScheme.onTertiary
    } else {
        colorScheme.surface to colorScheme.onSurface
    }

    val currentMonthTextStyle = MaterialTheme.typography.bodyMedium
    val textStyle = if (position == DayPosition.MonthDate) {
        currentMonthTextStyle
    } else {
        currentMonthTextStyle.copy(color = contentColor.copy(alpha = 0.5f))
    }.copy(
        fontWeight = if (event != null) {
            FontWeight.SemiBold
        } else {
            FontWeight.Normal
        }
    )

    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            if (event != null) {
                RichTooltip(
                    text = {
                        Text(
                            event.title ?: stringResource(R.string.no_name),
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    colors = TooltipDefaults.richTooltipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
        },
        state = tooltipState
    ) {
        Box(modifier = modifier.padding(9.dp)) {
            Surface(
                color = color,
                shape = CircleShape,
                onClick = {
                    if (event != null) {
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                },
                modifier = modifier.aspectRatio(1f)
            ) {
                Box {
                    Text(
                        date.dayOfMonth.toString(),
                        style = textStyle,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EventCalendarPreview() {
    AppTheme {
        EventCalendar(
            events = listOf(
                Event(
                    id = "id",
                    title = "Имя мероприятия",
                    date = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .toTimestamp()
                )
            ),
            onMonthChanged = {}
        )
    }
}