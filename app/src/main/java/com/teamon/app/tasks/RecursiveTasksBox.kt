package com.teamon.app.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.teamon.app.Actions
import com.teamon.app.prefs
import com.teamon.app.utils.classes.Task

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RecursiveTasksBox(
    tasks: List<Task> = listOf(),
    actions: Actions,
    snackbarHostState: SnackbarHostState
) {
    val animate = prefs.getBoolean("animate", true)
    var expanded by remember { mutableStateOf(false) }
    val boxHeight by animateDpAsState(
        targetValue = if (expanded) (tasks.size * 155.dp) else (155.dp + (tasks.size.takeIf { it<3 }?:3) * 15.dp),
        animationSpec = if (animate) spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ) else snap(), label = ""
    )

    Card(
        modifier = Modifier
            .clip(
                RoundedCornerShape(20.dp)
            )
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    )
    {
        AnimatedVisibility(modifier = Modifier.fillMaxWidth(), visible = expanded) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = false }) {
                    Text(
                        modifier = Modifier,
                        text ="Show less",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null)
                }


            }
        }

        Box(
            modifier = Modifier
                .height(boxHeight)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))

        ) {
            tasks.take(if (!expanded) 3 else tasks.size).forEachIndexed { index, it ->
                val offsetY: Dp by animateDpAsState(
                    targetValue = if (expanded) (index * 155).dp else (index * 25).dp,
                    animationSpec = if (animate) spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ) else snap(), label = ""
                )
                val padding: Dp by animateDpAsState(
                    targetValue = if (!expanded) (index * 10).dp else 0.dp,
                    animationSpec = if (animate) spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ) else snap(), label = ""
                )
                Box(
                    modifier = Modifier
                        .zIndex((tasks.size - index).toFloat())
                        .height(155.dp)
                        .offset(y = offsetY)
                        .padding(horizontal = padding.takeIf { it > 0.dp } ?: 0.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    if (expanded)
                        TaskCard(
                            taskId = it.taskId,
                            actions = actions,
                            snackbarHostState = snackbarHostState
                        )
                    else
                        TaskCard(
                            taskId = it.taskId,
                            actions = actions,
                            setView = { expanded = it },
                            snackbarHostState = snackbarHostState
                        )
                }
            }
        }

    }
}