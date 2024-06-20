package com.teamon.app

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.messaging
import com.teamon.app.utils.classes.FirestoreMessageListener
import com.teamon.app.utils.viewmodels.AttachmentsViewModel
import com.teamon.app.utils.viewmodels.ProfileViewModel
import com.teamon.app.utils.viewmodels.UsersViewModel
import com.teamon.app.utils.viewmodels.ProjectsViewModel
import com.teamon.app.utils.viewmodels.ChatsViewModel
import com.teamon.app.utils.viewmodels.CommentsViewModel
import com.teamon.app.utils.viewmodels.TeamsViewModel
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.utils.viewmodels.FeedbacksViewModel
import com.teamon.app.utils.viewmodels.HistoryViewModel
import com.teamon.app.utils.viewmodels.TasksViewModel


lateinit var teamsViewModel: TeamsViewModel
lateinit var profileViewModel: ProfileViewModel
lateinit var teamOnViewModel: ProjectsViewModel
lateinit var chatsViewModel: ChatsViewModel
lateinit var usersViewModel: UsersViewModel
lateinit var feedbacksViewModel: FeedbacksViewModel
lateinit var projectsViewModel: ProjectsViewModel
lateinit var tasksViewModel: TasksViewModel
lateinit var attachmentsViewModel: AttachmentsViewModel
lateinit var historyViewModel: HistoryViewModel
lateinit var commentsViewModel: CommentsViewModel
lateinit var prefs: SharedPreferences
lateinit var firestoreMessageListener: FirestoreMessageListener


class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            prefs = getSharedPreferences("animate", MODE_PRIVATE)
            teamsViewModel =
                viewModel<TeamsViewModel>(factory = Factory(LocalContext.current.applicationContext))
            projectsViewModel =
                viewModel<ProjectsViewModel>(factory = Factory(LocalContext.current.applicationContext))
            teamOnViewModel =
                viewModel<ProjectsViewModel>(factory = Factory(LocalContext.current.applicationContext))
            attachmentsViewModel =
                viewModel<AttachmentsViewModel>(factory = Factory(LocalContext.current.applicationContext))
            teamsViewModel =
                viewModel<TeamsViewModel>(factory = Factory(LocalContext.current.applicationContext))
            chatsViewModel =
                viewModel<ChatsViewModel>(factory = Factory(LocalContext.current.applicationContext))
            usersViewModel =
                viewModel<UsersViewModel>(factory = Factory(LocalContext.current.applicationContext))
            feedbacksViewModel =
                viewModel<FeedbacksViewModel>(factory = Factory(LocalContext.current.applicationContext))
            tasksViewModel =
                viewModel<TasksViewModel>(factory = Factory(LocalContext.current.applicationContext))
            //TO SWITCH BETWEEN LOGGED IN USERS, CHANGE THE USER ID BELOW
            profileViewModel =
                viewModel<ProfileViewModel>(factory = Factory(LocalContext.current.applicationContext))
            historyViewModel =
                viewModel<HistoryViewModel>(factory = Factory(LocalContext.current.applicationContext))
            commentsViewModel =
                viewModel<CommentsViewModel>(factory = Factory(LocalContext.current.applicationContext))



            MessagingService.initialize(this, this)
            Navigator()
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        firestoreMessageListener.stopListeningForMessages()
    }


}