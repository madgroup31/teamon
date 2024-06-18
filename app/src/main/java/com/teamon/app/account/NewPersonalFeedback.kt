package com.teamon.app.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.teamon.app.R
import com.teamon.app.utils.viewmodels.UserViewModel

@Composable
fun NewPersonalFeedbackDialog(
    title: String,
    onDismissRequest: () -> Unit,
    userVm: UserViewModel,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        OutlinedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
            //border = BorderStroke(1.dp, Color.DarkGray),
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column {
                    Text(
                        text = "Write a new personal feedback",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    )
                }
                Column {
                    Text(
                        text = "Anonymous",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    )
                    Switch(
                        checked = userVm.isFeedbackAnonymous,
                        onCheckedChange = { userVm.toggleIsFeedbackAnonymous() }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    readOnly = false,
                    enabled = true,
                    value = userVm.newFeedback,
                    trailingIcon = {
                        Image(
                            painter = painterResource(R.drawable.round_text_fields_24),
                            contentDescription = "New Feedback"
                        )
                    },
                    singleLine = false,
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Feedback") },
                    onValueChange = { userVm.updateNewFeedback(it) },
                    isError = userVm.newFeedbackError.isNotBlank()
                )
            }
            if (userVm.newFeedbackError.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                ) {
                    Text(
                        userVm.newFeedbackError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically

            ) {
                val ratingColor =
                    when (userVm.newFeedbackRating) {
                        in 0..4 -> Color.Red
                        in 8..10 -> Color.Green
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                Text(
                    text = "Rating: ",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = userVm.newFeedbackRating.toString(),
                    color = ratingColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Slider(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 0.dp),
                    value = userVm.newFeedbackRating.toFloat() / 10,
                    onValueChange = { userVm.updateNewFeedbackRating(it) },
                    valueRange = 0f..1f,
                    steps = 10
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        userVm.resetFeedback()
                        userVm.toggleIsWritingFeedback()
                    },
                ) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = { userVm.addFeedback() },
                ) {
                    Text(text = "Send")
                }
            }
        }
    }
}