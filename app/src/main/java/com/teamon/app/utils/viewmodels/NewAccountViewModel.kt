package com.teamon.app.utils.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamon.app.Model
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.User
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.util.Calendar
import java.util.Locale
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
class NewAccountViewModel(val model: Model): ViewModel() {
    var userId by mutableStateOf("")
        private set

    var emailValue by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set

    var uploadStatus by mutableStateOf(Any())
        private set

    var nameValue by mutableStateOf("")
        private set
    var nameError by mutableStateOf("")
        private set

    fun setName(n: String) {
        nameValue = n
    }
    private fun checkName() {
        nameError = if (nameValue.isBlank()) {
            "Name cannot be blank"
        } else ""
    }

    var surnameValue by mutableStateOf("")
        private set
    var surnameError by mutableStateOf("")
        private set

    fun setSurname(s: String) {
        surnameValue = s
    }

    private fun checkSurname() {
        surnameError = if (surnameValue.isBlank()) {
            "Surname cannot be blank"
        } else ""
    }


    var nicknameValue by mutableStateOf("")
        private set
    var nicknameError by mutableStateOf("")
        private set

    fun setNickname(n: String) {
        nicknameValue = n
    }

    private fun checkNickname() {
        nicknameError = if (nicknameValue.isBlank()) {
            "Nickname cannot be blank"
        } else ""
    }

    init {
        val auth = Firebase.auth
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
            if(!auth.currentUser!!.isAnonymous) {
                nameValue = auth.currentUser?.displayName?.split(" ")?.get(0)?:""
                surnameValue = auth.currentUser?.displayName?.split(" ")?.get(1)?:""
                emailValue =  auth.currentUser?.email?:"Not Found"
                nicknameValue = auth.currentUser?.displayName?.lowercase(Locale.ROOT)?.replace(" ", "")?.trim() ?: ""
            }
        }
    }

    var error by mutableStateOf<String?>("")
    private set

    fun setErrorMessage(e: String?) {
        error = e
    }

    suspend fun validate(): Boolean {
        checkName()
        checkSurname()
        checkNickname()
        checkEmail()
        checkLocation()
        checkBirthdate()
        checkBio()
        if (nameError.isBlank() && surnameError.isBlank() && emailError.isBlank() && nicknameError.isBlank() && emailError.isBlank() && locationError.isBlank() && bioError.isBlank() && birthdateError.isBlank()) {

            return viewModelScope.async {
                if (usersViewModel.updateUser(
                        userId,
                        User(
                            name = nameValue,
                            surname = surnameValue,
                            nickname = nicknameValue,
                            email = emailValue,
                            location = locationValue,
                            birthdate = birthdateValue.toTimestamp(),
                            biography = bioValue,
                            profileImage = profileImageUri.toString(),
                            profileImageSource = profileImageSource,
                            lastUpdate = Timestamp.now(),
                            feedbacks = listOf()
                        )
                    )
                ) {
                    true
                } else {
                    error = "An error occurred. Please try again."
                    false
                }
            }.await()
        } else {
            error = "An error occurred. Please try again."
            return false
        }
    }

    fun setColor(c: String) {
        color = ProjectColors.valueOf(c)
    }

    var color by mutableStateOf(ProjectColors.PURPLE)
    private set

    var profileImageUri: Uri by mutableStateOf(Uri.EMPTY)
        private set

    var profileImageSource: ImageSource by mutableStateOf(ImageSource.MONOGRAM)
        private set

    fun saveBitmapAsJpeg(context: Context, bitmap: Bitmap, filename: String): File? {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun setProfileImage(source: ImageSource, uri: Uri = Uri.EMPTY, context: Context? = null) {
        profileImageSource = source
        when (source) {
            ImageSource.CAMERA, ImageSource.LIBRARY -> {
                viewModelScope.launch {
                    context?.let {
                        var bitmap: Bitmap? = null
                        val inputStream = it.contentResolver.openInputStream(uri)
                        bitmap = BitmapFactory.decodeStream(inputStream)

                        val file = saveBitmapAsJpeg(context, bitmap, "$userId.jpg")
                        file?.let { jpegFile ->

                            usersViewModel!!.uploadProfileImage(userId = userId, file = jpegFile).collect {
                                if(it is UploadStatus.Success) {
                                    profileImageUri = (it as UploadStatus.Success).downloadUrl.toUri()
                                    profileImageSource = ImageSource.REMOTE
                                }
                            }
                        }

                    }

                }
            }
            else -> {
                /*TODO: delete any old profile picture*/}
        }

    }



    private fun checkEmail() {
        emailError = if (emailValue.isBlank()) {
            "Email cannot be blank"
        } else if (!emailValue.contains('@')) {
            "Invalid email address"
        } else ""
    }

    var locationValue by mutableStateOf("")
        private set
    var locationError by mutableStateOf("")
        private set



    fun setLocation(l: String) {
        locationValue = l
    }


    private fun checkLocation() {
        locationError = if (locationValue.isBlank()) {
            "Location cannot be blank"
        } else ""
    }

    var datePickerDialog by mutableStateOf(false)
        private set

    fun setDatePickerDialog() {
        datePickerDialog = !datePickerDialog
    }

    var birthdateValue by mutableStateOf("")
        private set
    var birthdateError by mutableStateOf("")
        private set

    fun setBirthdate(b: String) {
        birthdateValue = b
    }

    private fun checkBirthdate() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (birthdateValue.isBlank())
                throw DateTimeException("Birthdate can not be blank")
            val dateOfBirth = dateFormat.parse(birthdateValue)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            if (!dateOfBirth!!.before(calendar.time))
                throw DateTimeException("User must be at least 18 years old")
            birthdateError = ""
        } catch (e: Exception) {
            when (e) {
                is DateTimeException -> {
                    birthdateError = e.message.toString()
                }

                else -> {
                    birthdateError = "Invalid birthdate date"
                }
            }

        }
    }

    var bioValue by mutableStateOf("")
        private set
    var bioError by mutableStateOf("")
        private set

    fun setBio(d: String) {
        bioValue = d
    }

    private fun checkBio() {
        bioError = if (bioValue.isBlank()) {
            "Biography cannot be blank"
        } else ""
    }


}

