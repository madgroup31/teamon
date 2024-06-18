package com.teamon.app.account

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Performance
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.User
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt


@Composable
fun Histogram(data: Performance = Performance(), selectedMonthIndex: Int) {

    val maxValue = data.list.maxOf { it }.toFloat()
    val minValue = data.list.minOf { it }.toFloat()

    val mesi: Array<String> = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

    val stepSize = if(maxValue != 0f) (200.dp / maxValue).value else (200.dp).value
    val maxStepSize = maxValue

    Row{
        val stepSizes = (0..6).map { (maxStepSize * it/6).roundToInt() }.toSet().reversed()
        Column(modifier = Modifier.height(230.dp).align(Alignment.Bottom), verticalArrangement = if(stepSizes.size > 1) Arrangement.SpaceBetween else Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
             stepSizes.forEach {
                Text(text = it.toString(), fontSize = 10.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.width(1.dp))
        Column {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .height(250.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                val monthColor = MaterialTheme.colorScheme.onTertiaryContainer
                val selectedMonthColor = MaterialTheme.colorScheme.onPrimary
                val lineColor = MaterialTheme.colorScheme.surfaceContainerHighest
                val selectedBarColor = MaterialTheme.colorScheme.primary
                val unselectedBarColor = MaterialTheme.colorScheme.secondaryContainer
                val density = LocalDensity.current
                Canvas(
                    modifier = Modifier.fillMaxSize()
                )
                {

                    val gridSize = 50
                    val numRows = size.height.toInt() / gridSize

                    for (row in 0 until numRows) {
                        val startY = (row * gridSize).toFloat()

                        drawLine(
                            start = Offset(0f, startY),
                            end = Offset(size.width, startY), // Usa startY invece di endY
                            color = lineColor
                        )
                    }


                    data.list.forEachIndexed { index, value ->
                        val x = size.width / data.list.size * (index + 0.5f)
                        val y = size.height - (value * stepSize.dp.toPx() + 18.dp.toPx())

                        val barColor =
                            if (index == selectedMonthIndex) selectedBarColor else unselectedBarColor

                        drawRoundRect(
                            cornerRadius = CornerRadius(5.dp.toPx()),
                            color = barColor,
                            topLeft = Offset(x - (size.width/data.list.size - 15f)/2, y),
                            size = Size(size.width/data.list.size - 15f, size.height - y)
                        )

                        // Draw text on top of selected bar
                        if (index == selectedMonthIndex) {
                            val text = value.toString()
                            val textPaint = Paint().asFrameworkPaint().apply {
                                color = selectedBarColor.toArgb()
                                textSize = with(density) { 14.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawText(
                                    text,
                                    x,
                                    y - 10.dp.toPx(),
                                    textPaint
                                )
                            }

                        }
                        val textColor = if(index == selectedMonthIndex) selectedMonthColor else monthColor

                        val textPaint = Paint().asFrameworkPaint().apply {
                            color = textColor.toArgb()
                            textSize = with(density) { 11.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = index == selectedMonthIndex
                            isAntiAlias = true
                        }
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                mesi[index],
                                x,
                                245.dp.toPx(),
                                textPaint
                            )
                        }

                    }

                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}




@Composable
fun Diagram(mappa: Map<Int, Performance> = emptyMap(), title: String) {


    val listYears= mappa.keys.toIntArray()

    val listMonthName: Array<String> = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentDate = LocalDate.now()

    var selectedYear by remember { mutableIntStateOf( currentDate.year) }
    var selectedMonth by remember { mutableStateOf(listMonthName[currentDate.monthValue-1]) }
    var selectedMonthIndex by remember { mutableIntStateOf(currentDate.monthValue-1) }

    var performanceToShow by remember { mutableStateOf(mappa[selectedYear]) }
    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }

    Column {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(10.dp))


        Row(verticalAlignment = Alignment.CenterVertically) {

            //Dropdown menu year
            Button(onClick = { expandedYear = true }, modifier = Modifier.weight(1f)) {
                Text("$selectedYear")
                if (expandedYear)
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Expand dropdown menu")
                else
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Dismiss dropdown menu"
                    )

            }

            DropdownMenu(expanded = expandedYear,
                onDismissRequest = { expandedYear = false }) {
                listYears.forEachIndexed { _, year ->
                    DropdownMenuItem(
                        text = { Text("$year", color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = {
                            selectedYear = year
                            expandedYear = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            //Dropdown menu year
            Button(onClick = { expandedMonth = true }, modifier = Modifier.weight(1f)) {

                Text(text = if (selectedMonthIndex == -1) "month" else selectedMonth)

                if (expandedMonth)
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Expand dropdown menu")
                else
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Dismiss dropdown menu"
                    )

            }

            DropdownMenu(expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false }) {
                listMonthName.forEachIndexed { index, monthName ->
                    DropdownMenuItem(
                        text = { Text(monthName, color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = {
                            selectedMonth = monthName
                            selectedMonthIndex = index
                            expandedMonth = false
                        }
                    )
                }
            }

        }

        //update the performance to show
        performanceToShow = mappa[selectedYear]
        Spacer(modifier = Modifier.height(10.dp))
        Histogram(performanceToShow?:Performance(year = selectedYear), selectedMonthIndex)
    }

}

@Composable
fun AccountPerformance(feedbacks: List<Feedback>, tasks: List<Task>)
{

   val completedTasks = tasks
       .filter { it.status == TaskStatus.Completed }
       .groupBy { it.endDate.toInstant().atZone(ZoneId.systemDefault()).year }
       .mapValues { (year, yearTasks) ->
           val monthlyCounts = (0..11).map { 0 }.toIntArray()
           yearTasks.forEach { task ->
               val monthIndex = task.endDate.toInstant().atZone(ZoneId.systemDefault()).monthValue-1
               monthlyCounts[monthIndex]++ // Increment count for the corresponding month
           }
           Performance(year = year, list = monthlyCounts) // Create a Performance object for the year
       }

    val receivedFeedbacks = feedbacks
        .groupBy { it.timestamp.toInstant().atZone(ZoneId.systemDefault()).year }
        .mapValues { (year, yearFeedbacks) ->
            val monthlyCounts = (0..11).map { 0 }.toIntArray()
            yearFeedbacks.forEach { feedback ->
                val monthIndex = feedback.timestamp.toInstant().atZone(ZoneId.systemDefault()).monthValue-1
                monthlyCounts[monthIndex]++ // Increment count for the corresponding month
            }
            Performance(year = year, list = monthlyCounts) // Create a Performance object for the year

        }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {

        // COMPLETED TASK
        Diagram(mappa = completedTasks, title = "Completed Tasks")
        Spacer(modifier = Modifier.height(20.dp))
        // RECEIVED FEEDBACK
        Diagram(mappa = receivedFeedbacks, title = "Received Feedbacks")

    }

}