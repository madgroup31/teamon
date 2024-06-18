package com.teamon.app.board.project.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions
import com.teamon.app.account.Diagram
import com.teamon.app.projectsViewModel
import com.teamon.app.tasks.TaskCard
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.utils.viewmodels.ProjectViewModel
import com.teamon.app.utils.classes.Performance
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.Orientation
import java.time.ZoneId

@Composable
fun PerformanceActions() {

}

@Composable
fun PerformanceView(actions: Actions, projectVM: ProjectViewModel) {

    val tasks by projectsViewModel!!.getProjectTasks(projectVM.projectId).collectAsState(initial = emptyMap())
    val feedbacks by projectsViewModel!!.getProjectFeedbacks(projectVM.projectId).collectAsState(initial = emptyMap())

    val completedTasksList = tasks.values.filter { it.status == TaskStatus.Completed }

    val completedTasks = completedTasksList
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

    val receivedFeedbacks = feedbacks.values
        .groupBy { it.timestamp.toInstant().atZone(ZoneId.systemDefault()).year }
        .mapValues { (year, yearFeedbacks) ->
            val monthlyCounts = (0..11).map { 0 }.toIntArray()
            yearFeedbacks.forEach { feedback ->
                val monthIndex = feedback.timestamp.toInstant().atZone(ZoneId.systemDefault()).monthValue-1
                monthlyCounts[monthIndex]++ // Increment count for the corresponding month
            }
            Performance(year = year, list = monthlyCounts) // Create a Performance object for the year

        }


    //PERFORMANCE PART

    if (tasks.isEmpty())
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Available Performances yet.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
        }
    else
            AnimatedGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                columns = GridCells.Adaptive(minSize = 400.dp),
                items = listOf(completedTasks, receivedFeedbacks)
            ) { it, index ->
                when(index) {
                    0 -> {
                        Diagram(mappa = it as Map<Int, Performance>, "Completed Tasks")
                    }
                        1 -> {
                            Diagram(mappa = it as Map<Int, Performance>, "Received Feedbacks")
                        }
                    else -> {}
                }
            }



}
