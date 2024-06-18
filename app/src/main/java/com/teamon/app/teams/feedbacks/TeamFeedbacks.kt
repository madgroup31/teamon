package com.teamon.app.myteams

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.account.FeedbackCard
import com.teamon.app.tasks.comments.CommentCard
import com.teamon.app.teams.feedbacks.NewTeamFeedbackDialog
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.graphics.convertMillisToDate
import com.teamon.app.utils.viewmodels.TeamViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogFeedback(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    actualDate: String,
    firstSelectable: String? = null
) {
    val datePickerState = rememberDatePickerState()

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: actualDate

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                onDateSelected(selectedDate)
                onDismiss()
            }

            ) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text(text = "Cancel")
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(enabled = true, state = rememberScrollState())
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}

@Composable
fun DayHeader(day: String)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = day, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.size(10.dp))
        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

    }
}



@Composable
fun RatingRow(rating: Int,
              onRatingChange: (Int) -> Unit,
              start: Int,
              end: Int,
              label: String)
{
    var showMenu by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally)
    {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable { showMenu = true }
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rating.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                (start..end).forEach { value ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            onRatingChange(value)
                            showMenu = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FeedbackActions(anonymousFilter: Boolean,
                    onAnonymousFilterChange: (Boolean) -> Unit,
                    startRating: Int,
                    onStartRateChange: (Int) -> Unit,
                    fromRating: Int,
                    onFromRateChange: (Int) -> Unit,
                    filterDate: String,
                    onFilterDateChange: (String)->Unit
)
{
    var showMenu by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

    Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "MoreVert",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(26.dp).clickable { showMenu= true }
    )


    Box {

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu= false }
        ) {
            // ANONYMOUS SWITCH
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier= Modifier.width(10.dp))
                    Switch(
                        checked = anonymousFilter,
                        onCheckedChange = { onAnonymousFilterChange(it) }
                    )
                }
            }

            // RATE SELECTION
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Rate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier= Modifier.width(10.dp))

                    RatingRow(
                        rating = startRating,
                        onRatingChange = onStartRateChange,
                        start = 1,
                        end = fromRating - 1,
                        label = "From"
                    )
                    Spacer(modifier= Modifier.width(10.dp))

                    RatingRow(
                        rating = fromRating,
                        onRatingChange = onFromRateChange,
                        start = startRating + 1,
                        end = 10,
                        label = "To"
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            //DATE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = filterDate,
                        onValueChange = {onFilterDateChange(it)},
                        modifier = Modifier.weight(1f),
                        label = { Text("Date") },
                        trailingIcon = {
                                IconButton(onClick = { showDate= true}) {
                                    Image(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = R.drawable.round_calendar_today_24),
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                        contentDescription = "Date picker"
                                    )
                                }
                        }
                    )
                    if(showDate)
                    {
                        DatePickerDialogFeedback(
                            onDateSelected = {onFilterDateChange(it.replace("/","-"))},
                            onDismiss = { showDate= false },
                            actualDate = filterDate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(modifier= Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            //RESET
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable {
                        onAnonymousFilterChange(false)
                        onStartRateChange(0)
                        onFromRateChange(10)
                        onFilterDateChange("")
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}


@Composable
fun TeamFeedbacks(actions: Actions, teamVM: TeamViewModel)
{

    val feedbacks by teamVM.getFeedbacksTeam().collectAsState(initial = emptyList())


    var anonymousFilter by remember { mutableStateOf(false) }
    val onAnonymousFilterChange: (Boolean) -> Unit= { anonymousFilter = it }

    var startRating by remember { mutableStateOf(0) }
    val onStartRateChange: (Int) -> Unit= { startRating = it }

    var fromRating by remember { mutableStateOf(10) }
    val onFromRateChange: (Int) -> Unit= { fromRating = it }

    //DATE
    var dateFilter by remember { mutableStateOf<String>("") }
    val onDateFilterChange: (String) -> Unit= {
        dateFilter= it
    }


    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val onSearchActiveChange: (Boolean) -> Unit = { searchActive = it }
    val onQueryChange: (String) -> Unit = { query = it }


    if (teamVM.isWritingFeedback)
    {

        NewTeamFeedbackDialog(
            title = teamVM.teamName,
            onDismissRequest = { teamVM.toggleIsWritingFeedback() },
            teamVm = teamVM
        )
    }

    if(feedbacks.isEmpty())
    {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            AnimatedItem(index = 1) {
                Text(
                    text = "No Feedbacks available yet",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
    else
    {
        if(!searchActive)
        {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val filteredFeedbacks= feedbacks
                    .filter { it.value in startRating..fromRating }
                    .filter { if(!anonymousFilter) true else it.anonymous }
                    .filter{if (dateFilter.isEmpty()) {
                                 true
                          } else {
                            val localDateTime = LocalDateTime.ofInstant(it.timestamp.toDate().toInstant(), ZoneId.systemDefault())
                            val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                            val formattedDate = localDateTime.format(outputFormatter)

                            dateFilter == formattedDate
                        }
                    }

                //HEADER
                item{
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        //NUMBER FEEDBACKS
                        Text(
                            "${filteredFeedbacks.size} feedbacks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Start
                        )
                        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            //SEARCH ICON
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onSearchActiveChange(true) }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            FeedbackActions(
                                anonymousFilter = anonymousFilter,
                                onAnonymousFilterChange = onAnonymousFilterChange,
                                startRating = startRating,
                                onStartRateChange = onStartRateChange,
                                fromRating = fromRating,
                                onFromRateChange = onFromRateChange,
                                filterDate = dateFilter,
                                onFilterDateChange = onDateFilterChange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                //FILTERED FEEDBACKS EMPTY
                if(filteredFeedbacks.isEmpty())
                {
                    item {
                        Column(modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            AnimatedItem(index = 1) {
                                Text(
                                    text = "No Feedback Found",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
                else
                {
                    //FEEDBACKS
                    val groupedFeedbacks = filteredFeedbacks.sortedByDescending { it.timestamp }
                        .groupBy { it.timestamp.asPastRelativeDate() }


                    groupedFeedbacks.forEach { (date, feedbacks) ->
                        item {
                            DayHeader(date)
                        }
                        items(feedbacks) { feedback ->
                            FeedbackCard(actions = actions, feedback = feedback)
                        }
                    }

                }
            }
        }
        else
        {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                val filteredFeedbacks= feedbacks
                    .filter { it.value in startRating..fromRating }
                    .filter { if(!anonymousFilter) true else it.anonymous }
                    .filter { feedback-> feedback.description.contains(query, ignoreCase = true) }
                    .filter{if (dateFilter.isEmpty()) {
                                    true
                                } else {
                                    val localDateTime = LocalDateTime.ofInstant(it.timestamp.toDate().toInstant(), ZoneId.systemDefault())
                                    val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                    val formattedDate = localDateTime.format(outputFormatter)

                                    dateFilter == formattedDate
                                }
                    }


                //HEADER
                item{
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        //NUMBER FEEDBACKS
                        Text(
                            "${filteredFeedbacks.size} feedbacks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Start
                        )
                        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            FeedbackActions(
                                anonymousFilter = anonymousFilter,
                                onAnonymousFilterChange = onAnonymousFilterChange,
                                startRating = startRating,
                                onStartRateChange = onStartRateChange,
                                fromRating = fromRating,
                                onFromRateChange = onFromRateChange,
                                filterDate = dateFilter,
                                onFilterDateChange = onDateFilterChange
                            )
                        }
                    }
                    Spacer(modifier= Modifier.height(10.dp))
                }

                //SEARCH BAR
                item{
                    SearchBar(
                        query = query,
                        onQueryChange = onQueryChange,
                        searchActive = searchActive,
                        onSearchActiveChange = onSearchActiveChange,
                        placeholder = "Search for description"
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                            //FILTERED FEEDBACKS EMPTY
                            if(filteredFeedbacks.isEmpty())
                            {
                                item {
                                    Column(modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ){
                                        AnimatedItem(index = 1) {
                                            Text(
                                                text = "No Feedback Found",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                            else
                            {
                                //FEEDBACKS
                                val groupedFeedbacks = filteredFeedbacks.sortedByDescending { it.timestamp }
                                    .groupBy { it.timestamp.asPastRelativeDate() }

                                groupedFeedbacks.forEach { (date, feedbacks) ->
                                    item {
                                        DayHeader(date)
                                    }
                                    items(feedbacks) { feedback ->
                                        FeedbackCard(actions = actions, feedback = feedback)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

}