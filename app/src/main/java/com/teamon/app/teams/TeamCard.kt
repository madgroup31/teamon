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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme

@Composable
fun TeamCard(
    team: Team,
    actions: Actions,
) {
    val selectedNavItem =
        actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first().toString()


    Theme(
        applyToStatusBar = false,
        color = team.color
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(20.dp),
                onClick = { actions.openTeam(team.teamId) }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(15.dp)
                            .weight(1.5f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1.5f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    team.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    team.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        )
                        {
                            Text(
                                team.description,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    actions.openTeamMembers(
                                        selectedNavItem,
                                        team.teamId
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            team.users.sortedByDescending { team.admin.contains(it) }.take(3)
                                .forEach {
                                    val user by usersViewModel.getUser(it)
                                        .collectAsState(initial = User())
                                    TeamOnImage(
                                        modifier = if (team.admin.contains(user.userId)) Modifier
                                            .size(
                                                30.dp
                                            )
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                        else Modifier
                                            .size(24.dp)
                                            .clip(CircleShape),
                                        source = user.profileImageSource,
                                        uri = user.profileImage?.toUri(),
                                        name = user.name,
                                        surname = user.surname,
                                        description = user.name + " " + user.surname + " profile image"
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            if (team.users.size > 3)
                                Text(
                                    " +${team.users.size - 3}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp
                                )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                modifier = Modifier,
                                onClick = { actions.openTeamChat(team.teamId) },
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.outline_comment_24),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                                )
                                Badge(modifier = Modifier.offset(x = 0.dp, y = (-10).dp))
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                TeamOnImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(5.dp)
                        .alpha(0.2f),
                    source = team.imageSource,
                    name = team.name,
                    surname = "",
                    uri = team.image.toUri(),
                    contentScale = ContentScale.FillHeight,
                    description = "Team image"
                )
            }
        }
    }
}
