package com.teamon.app.account

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.teamon.app.Location
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.convertMillisToDate
import com.teamon.app.utils.graphics.toInt
import com.teamon.app.utils.viewmodels.NewAccountViewModel
import com.teamon.app.utils.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@Composable

fun AccountPersonalInformation(orientation: Orientation, userVm: UserViewModel? = null) {
    when (userVm) {
        null -> {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    text = "Personal Information",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = !profileViewModel!!.isEditing,
                            enabled = true,
                            value = profileViewModel!!.nameValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Name") },
                            onValueChange = { name -> profileViewModel!!.setName(name) },
                            isError = profileViewModel!!.nameError.isNotBlank()
                        )
                        if (profileViewModel!!.nameError.isNotBlank()) {
                            Text(
                                profileViewModel!!.nameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = !profileViewModel!!.isEditing,
                            enabled = true,
                            value = profileViewModel!!.surnameValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Surname") },
                            onValueChange = { surname -> profileViewModel!!.setSurname(surname) },
                            isError = profileViewModel!!.surnameError.isNotBlank()
                        )
                        if (profileViewModel!!.surnameError.isNotBlank()) {
                            Text(
                                profileViewModel!!.surnameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = !profileViewModel!!.isEditing,
                            enabled = true,
                            value = profileViewModel!!.nicknameValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Nickname") },
                            onValueChange = { nickname -> profileViewModel!!.setNickname(nickname) },
                            isError = profileViewModel!!.nicknameError.isNotBlank()
                        )
                        if (profileViewModel!!.nicknameError.isNotBlank()) {
                            Text(
                                profileViewModel!!.nicknameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }


                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = !profileViewModel!!.isEditing,
                            enabled = true,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(enabled = profileViewModel!!.isEditing, onClick = {}) {
                                    Icon(Icons.Rounded.LocationOn, contentDescription = "Location")
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            value = profileViewModel!!.locationValue,
                            label = { Text("Location") },
                            onValueChange = { location -> profileViewModel!!.setLocation(location) },
                            isError = profileViewModel!!.locationError.isNotBlank()
                        )
                        if (profileViewModel!!.locationError.isNotBlank()) {
                            Text(
                                profileViewModel!!.locationError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = true,
                            enabled = true,
                            value = profileViewModel!!.birthdateValue,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(enabled = profileViewModel!!.isEditing,
                                    onClick = { profileViewModel!!.setDatePickerDialog() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.round_calendar_today_24),
                                        contentDescription = "Date picker"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Birthdate") },
                            onValueChange = { birthdate -> profileViewModel!!.setBirthdate(birthdate) },
                            isError = profileViewModel!!.birthdateError.isNotBlank()
                        )
                        if (profileViewModel!!.birthdateError.isNotBlank()) {
                            Text(
                                profileViewModel!!.birthdateError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    var colorExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = true,
                            enabled = true,
                            value = profileViewModel.color.toString().lowercase().capitalize(),
                            singleLine = true,
                            leadingIcon = { Surface(modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape), color = Color(profileViewModel.color.toInt())) {} },
                            trailingIcon = {
                                IconButton(enabled = profileViewModel.isEditing,
                                    onClick = { colorExpanded = !colorExpanded  }) {
                                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
                                }
                            },
                            label = { Text("Color") },
                            onValueChange = {  },
                        )
                        DropdownMenu(expanded = colorExpanded, onDismissRequest = { colorExpanded = false }) {
                            ProjectColors.entries.forEach {
                                DropdownMenuItem(
                                    leadingIcon = { Surface(modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape), color = Color(it.toInt())) {} },
                                    text = { Text(it.name.lowercase().capitalize()) },
                                    onClick = { profileViewModel.setColor(it.name) })
                            }
                        }
                    }

                }

                if (profileViewModel!!.datePickerDialog) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        MyDatePickerDialog(
                            onDateSelected = {
                                profileViewModel!!.setBirthdate(
                                    it.replace(
                                        "/",
                                        "-"
                                    )
                                )
                            },
                            onDismiss = { profileViewModel!!.setDatePickerDialog() },
                            actualBirthdate = profileViewModel!!.birthdateValue
                        )
                    }
                }


                if (orientation == Orientation.PORTRAIT) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        enabled = true,
                        readOnly = !profileViewModel!!.isEditing,
                        value = profileViewModel!!.bioValue,
                        label = { Text("Biography") },
                        onValueChange = { bio -> profileViewModel!!.setBio(bio) },
                        isError = profileViewModel!!.bioError.isNotBlank()
                    )
                    if (profileViewModel!!.bioError.isNotBlank()) {
                        Text(
                            profileViewModel!!.bioError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    text = "Personal Information",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            enabled = false,
                            value = userVm.nameValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Name") },
                            onValueChange = { },
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            enabled = false,
                            value = userVm.surnameValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Surname") },
                            onValueChange = { },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            enabled = false,
                            value = userVm.nicknameValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Nickname") },
                            onValueChange = { }
                        )
                    }


                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(enabled = false, onClick = {}) {
                                    Icon(Icons.Rounded.LocationOn, contentDescription = "Location")
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            value = userVm.locationValue,
                            label = { Text("Location") },
                            onValueChange = { },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = true,
                            enabled = false,
                            value = userVm.birthdateValue,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Birthdate") },
                            onValueChange = { }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxSize(),
                            readOnly = true,
                            enabled = false,
                            value = userVm.color.toString().lowercase().capitalize(),
                            singleLine = true,
                            leadingIcon = { Surface(modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape), color = Color(userVm.color.toInt())) {} },
                            label = { Text("Color") },
                            onValueChange = {  },
                        )
                    }

                }


                if (orientation == Orientation.PORTRAIT) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        enabled = false,
                        readOnly = true,
                        value = userVm.bioValue,
                        label = { Text("Biography") },
                        onValueChange = { },
                    )
                }

            }
        }
    }

}


@Composable

fun NewAccountPersonalInformation(orientation: Orientation, userVm: NewAccountViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
     LaunchedEffect(Unit) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    fusedLocationClient
                        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnCompleteListener { location ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val l = location.await()

                                    val cityName = Geocoder(context, Locale.getDefault()).getFromLocation(
                                        l.latitude,
                                        l.longitude,
                                        1
                                    )
                                    val value = cityName?.first()?.locality
                                    if(value != null)
                                        userVm.setLocation(value)
                            }
                        }
                } catch (e: Exception) {
                    userVm.setLocation("")
                }

            } else {
                userVm.setLocation("")
            }

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {

        Text(
            text = "Personal Information",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    value = userVm.nameValue,
                    singleLine = true,
                    isError = userVm.nameError.isNotBlank(),
                    supportingText = {
                        if(userVm.nameError.isNotBlank())
                            Text(text = userVm.nameError)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Name") },
                    onValueChange = { name -> userVm.setName(name) },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    value = userVm.surnameValue,
                    singleLine = true,
                    isError = userVm.surnameError.isNotBlank(),
                    supportingText = {
                        if(userVm.surnameError.isNotBlank())
                            Text(text = userVm.surnameError)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Surname") },
                    onValueChange = { surname -> userVm.setSurname(surname) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    value = userVm.nicknameValue,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = userVm.nicknameError.isNotBlank(),
                    supportingText = {
                        if(userVm.nicknameError.isNotBlank())
                            Text(text = userVm.nicknameError)
                    },
                    label = { Text("Nickname") },
                    onValueChange = { nickname -> userVm.setNickname(nickname) }
                )
            }


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(enabled = true, onClick = {}) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = "Location")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    value = userVm.locationValue,
                    isError = userVm.locationError.isNotBlank(),
                    supportingText = {
                        if(userVm.locationError.isNotBlank())
                            Text(text = userVm.locationError)
                    },
                    label = { Text("Location") },
                    onValueChange = { location -> userVm.setLocation(location) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    readOnly = true,
                    enabled = true,
                    value = userVm.birthdateValue,
                    singleLine = true,
                    isError = userVm.birthdateError.isNotBlank(),
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { userVm.setDatePickerDialog() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_calendar_today_24),
                                contentDescription = "Date picker"
                            )
                        }
                    },
                    supportingText = {
                        if(userVm.birthdateError.isNotBlank())
                            Text(text = userVm.birthdateError)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Birthdate") },
                    onValueChange = { birthdate -> userVm.setBirthdate(birthdate) },
                )
            }
            var colorExpanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    readOnly = true,
                    enabled = true,
                    value = userVm.color.toString().lowercase().capitalize(),
                    singleLine = true,
                    isError = false,
                    supportingText = {Text("")},
                    leadingIcon = { Surface(modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape), color = Color(userVm.color.toInt())) {} },
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { colorExpanded = !colorExpanded  }) {
                            Icon(if(!colorExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp, contentDescription = null)
                        }
                    },
                    label = { Text("Color") },
                    onValueChange = {  },
                )
                DropdownMenu(expanded = colorExpanded, onDismissRequest = { colorExpanded = false }) {
                    ProjectColors.entries.forEach {
                        DropdownMenuItem(
                            leadingIcon = { Surface(modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape), color = Color(it.toInt())) {} },
                            text = { Text(it.name.lowercase().capitalize()) },
                            onClick = { userVm.setColor(it.name) })
                    }
                }
            }


            if (userVm.datePickerDialog) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    MyDatePickerDialog(
                        onDateSelected = {
                            userVm.setBirthdate(
                                it.replace(
                                    "/",
                                    "-"
                                )
                            )
                        },
                        onDismiss = { userVm.setDatePickerDialog() },
                        actualBirthdate = userVm.birthdateValue
                    )
                }
            }

        }


        if (orientation == Orientation.PORTRAIT) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                enabled = true,
                readOnly = false,
                value = userVm.bioValue,
                isError = userVm.bioError.isNotBlank(),
                supportingText = {
                    if(userVm.bioError.isNotBlank())
                        Text(text = userVm.bioError)
                },
                label = { Text("Biography") },
                onValueChange = { bio -> userVm.setBio(bio) },
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    actualBirthdate: String
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {

        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)

            val date = Date(utcTimeMillis)
            return date.before(calendar.time)
        }

    })

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: actualBirthdate

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