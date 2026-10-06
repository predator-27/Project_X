package com.projectx.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.AppScaffold
import com.projectx.app.components.StatusPill
import com.projectx.app.components.StatusTone
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.model.HolidayEvent
import com.projectx.app.model.HolidayKind
import com.projectx.app.theme.CampusTokens
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Monthly calendar with holiday/event dots, today highlight, tap-a-day event list,
 * and an "Upcoming" strip below. Pure state — no Firestore read.
 */
@Composable
fun HolidayCalendarScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
) {
    val c = CampusTokens.colors
    val events: List<HolidayEvent> = remember { DemoCampusData.demoHolidays }
    val eventsByDate = remember(events) {
        events.groupBy { LocalDate.parse(it.date) }
    }

    var displayMonth by remember { mutableStateOf(YearMonth.of(2026, 10)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    AppScaffold(
        title = "Holidays & Calendar",
        onMenuClick = onMenuClick,
        modifier = modifier,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                MonthHeader(
                    month = displayMonth,
                    onPrev = { displayMonth = displayMonth.minusMonths(1) },
                    onNext = { displayMonth = displayMonth.plusMonths(1) },
                )
            }
            item {
                CalendarGrid(
                    month = displayMonth,
                    today = LocalDate.of(2026, 10, 6),
                    selected = selectedDate,
                    eventsByDate = eventsByDate,
                    onPick = { selectedDate = it },
                )
            }
            selectedDate?.let { date ->
                val dayEvents = eventsByDate[date].orEmpty()
                item {
                    DaySheet(date, dayEvents)
                }
            }
            item {
                Text(
                    text = "Upcoming",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.heading,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(events.sortedBy { it.date }.take(6), key = { it.id }) { e ->
                UpcomingRow(e)
            }
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month", tint = c.heading)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = c.heading,
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month", tint = c.heading)
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate?,
    eventsByDate: Map<LocalDate, List<HolidayEvent>>,
    onPick: (LocalDate) -> Unit,
) {
    val c = CampusTokens.colors
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val firstOfMonth = month.atDay(1)
    val startOffset = (firstOfMonth.dayOfWeek.value - 1) // Monday=0
    val daysInMonth = month.lengthOfMonth()
    val cells = (0 until 42).map { idx ->
        val dayOfMonth = idx - startOffset + 1
        if (dayOfMonth in 1..daysInMonth) month.atDay(dayOfMonth) else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.surfaceBorder, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekDays.forEach {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.mutedText,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        for (row in 0 until 6) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0 until 7) {
                    val day = cells[row * 7 + col]
                    DayCell(
                        day = day,
                        isToday = day == today,
                        isSelected = day == selected,
                        events = day?.let { eventsByDate[it] }.orEmpty(),
                        onClick = { day?.let(onPick) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<HolidayEvent>,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val c = CampusTokens.colors
    val bg = when {
        isSelected -> c.primary.copy(alpha = 0.20f)
        isToday -> c.primary.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val border = if (isToday) 1.5.dp else 0.dp
    val borderColor = if (isToday) c.primary else Color.Transparent
    Column(
        modifier = modifier
            .padding(2.dp)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg, RoundedCornerShape(10.dp))
            .border(border, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = day != null, onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = day?.dayOfMonth?.toString() ?: "",
            fontSize = 13.sp,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (day == null) Color.Transparent else c.bodyText,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            events.take(3).forEach { e ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colorFor(e.kind)),
                )
            }
        }
    }
}

@Composable
private fun DaySheet(date: LocalDate, events: List<HolidayEvent>) {
    val c = CampusTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.surfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = c.heading,
        )
        if (events.isEmpty()) {
            Text("No events scheduled.", fontSize = 13.sp, color = c.mutedText)
        } else events.forEach { e -> EventRow(e) }
    }
}

@Composable
private fun UpcomingRow(e: HolidayEvent) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.surfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(colorFor(e.kind)))
        Column(Modifier.weight(1f)) {
            Text(e.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.heading)
            Text(LocalDate.parse(e.date).format(DateTimeFormatter.ofPattern("d MMM yyyy")), fontSize = 12.sp, color = c.mutedText)
        }
        StatusPill(text = e.kind.name, tone = toneFor(e.kind))
    }
}

@Composable
private fun EventRow(e: HolidayEvent) {
    val c = CampusTokens.colors
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(colorFor(e.kind)).padding(top = 4.dp))
        Column {
            Text(e.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.heading)
            if (e.description.isNotBlank()) {
                Text(e.description, fontSize = 12.sp, color = c.bodyText)
            }
        }
    }
}

@Composable
private fun colorFor(kind: HolidayKind): Color {
    val c = CampusTokens.colors
    return when (kind) {
        HolidayKind.HOLIDAY  -> c.dangerRed
        HolidayKind.EXAM     -> c.warningAmber
        HolidayKind.EVENT    -> c.primary
        HolidayKind.DEADLINE -> c.infoBlue
    }
}

private fun toneFor(kind: HolidayKind): StatusTone = when (kind) {
    HolidayKind.HOLIDAY  -> StatusTone.DANGER
    HolidayKind.EXAM     -> StatusTone.WARNING
    HolidayKind.EVENT    -> StatusTone.NEUTRAL
    HolidayKind.DEADLINE -> StatusTone.NEUTRAL
}
