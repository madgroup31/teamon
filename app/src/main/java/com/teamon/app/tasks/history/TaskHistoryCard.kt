package com.teamon.app.tasks.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.History
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.asTime
import com.teamon.app.utils.graphics.getIcon

@Composable
fun TaskHistoryCard(entry: History){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ){
        Column(
            modifier = Modifier.weight(1.5f).padding(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Icon(modifier = Modifier, painter = painterResource(id = entry.icon.getIcon()), contentDescription = "updateType")
            Text(entry.timestamp.asTime(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(
            modifier = Modifier.weight(6f),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedCard(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 0.dp
                ),
                shape = RoundedCornerShape(20.dp),
                //colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(text = entry.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Column(
            modifier = Modifier.weight(1.5f).padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val user by usersViewModel.getUser(entry.user).collectAsState(initial = User())

            TeamOnImage(
                modifier = Modifier.size(32.dp).clip(CircleShape),
                source = user.profileImageSource,
                uri = user.profileImage?.toUri(),
                name = user.name,
                surname = user.surname,
                color = user.color,
                description = user.name + " " + user.surname + " profile image")
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
}
