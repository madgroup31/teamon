package com.teamon.app.myteams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.account.Diagram
import com.teamon.app.teams.feedbacks.NewTeamFeedbackDialog
import com.teamon.app.utils.classes.Performance
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.viewmodels.TeamViewModel
import java.time.ZoneId

@Composable
fun TeamAchievement(teamVM: TeamViewModel)
{

    val projects by teamVM.getProjectsTeam().collectAsState(initial = emptyList())
    val feedbacks by teamVM.getFeedbacksTeam().collectAsState(initial = emptyList())


    val completedProjectsMap by teamVM.getTeamCompletedProjects().collectAsState(initial = emptyMap())


    val completedProjects = completedProjectsMap.values
        .groupBy { it.endDate.toInstant().atZone(ZoneId.systemDefault()).year }
        .mapValues { (_, yearProjects) ->
            val monthlyCounts = (0..11).map { 0 }.toIntArray()
            yearProjects.forEach { project ->
                val monthIndex = project.endDate.toInstant().atZone(ZoneId.systemDefault()).monthValue-1
                monthlyCounts[monthIndex]++ // Increment count for the corresponding month
            }
            Performance(list = monthlyCounts) // Create a Performance object for the year
        }



    val receivedFeedbacks = feedbacks
        .groupBy { it.timestamp.toInstant().atZone(ZoneId.systemDefault()).year }
        .mapValues { (_, yearFeedbacks) ->
            val monthlyCounts = (0..11).map { 0 }.toIntArray()
            yearFeedbacks.forEach { feedback ->
                val monthIndex = feedback.timestamp.toInstant().atZone(ZoneId.systemDefault()).monthValue-1
                monthlyCounts[monthIndex]++ // Increment count for the corresponding month
            }
            Performance(list = monthlyCounts) // Create a Performance object for the year

        }



    if (teamVM.isWritingFeedback) {
        NewTeamFeedbackDialog(
            title = teamVM.teamName,
            onDismissRequest = { teamVM.toggleIsWritingFeedback() },
            teamVm = teamVM
        )
    }


    //EMPTY PROJECTS AND FEEDBACKS
    if(projects.isEmpty() && feedbacks.isEmpty() )
    {
        AnimatedItem(index = 1) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedItem(index = 1) {
                    Text(
                        text = "No Performances available yet.",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            //COMPLETED PROJECTS
            if(projects.isNotEmpty())
            {
                item {
                    AnimatedItem(index = 1) {
                        Diagram(mappa = completedProjects, "Completed Projects")
                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }

            item{
                Spacer(modifier= Modifier.height(32.dp))
            }

            //RECEIVED FEEDBACKS
            if(feedbacks.isNotEmpty())
            {
                item {
                    AnimatedItem(index = 1) {
                        Diagram(mappa = receivedFeedbacks, "Received Feedbacks")
                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )

                    }
                }
            }


        }
    }


}
