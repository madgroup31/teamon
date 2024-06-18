package com.teamon.app.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.viewmodels.UserViewModel

@Composable
fun ProgressSliders(user: UserViewModel? = null) {

    val projects by when(user) {
        null -> {
            projectsViewModel.getUserProjects(profileViewModel.userId).collectAsState(initial = emptyMap())
        }

        else -> {
            projectsViewModel.getUserProjects(user.userId).collectAsState(initial = emptyMap())
        }
    }
                if(projects.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Progress Sliders",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        projects.values.forEach {
                            val completedTasks by projectsViewModel.getProjectCompletedTasks(it.projectId)
                                .collectAsState(initial = emptyMap())
                            val tasks by projectsViewModel.getProjectTasks(it.projectId)
                                .collectAsState(initial = emptyMap())

                            ProgressSlider(
                                (completedTasks.size.toFloat() / tasks.size.toFloat()).takeIf { tasks.size.toFloat() != 0f } ?: 0f,
                                it.projectName,
                                it.projectImage.toImage(),
                                it.projectColor
                            )
                        }
                    }
                }
}

@Composable
fun ProgressSlider(
    progress: Float = 0f,
    projectTitle: String,
    projectImage: ImageVector,
    projectColor: ProjectColors
) {

    var showProgress by remember { mutableStateOf(false) }
    Theme(color = projectColor, applyToStatusBar = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            FilledTonalIconButton(onClick = { showProgress = !showProgress }) {
                if (!showProgress)
                    Icon(
                        projectImage,
                        contentDescription = "Project Image",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape),
                        tint = MaterialTheme.colorScheme.primary
                    )
                else
                    Text(
                        text = (progress * 100).toInt().toString() + "%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = projectTitle,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

}