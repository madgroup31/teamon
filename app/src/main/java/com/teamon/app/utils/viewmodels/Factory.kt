package com.teamon.app.utils.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.teamon.app.TeamOn


@Suppress("UNCHECKED_CAST")
class Factory(context: Context, val taskId: String? = null, val projectId: String? = null, val teamId: String? = null, val userId: String? = null) :


    ViewModelProvider.Factory {

    private val model =
        (context as? TeamOn)?.model ?: throw IllegalArgumentException("Bad application context!")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            CommentsViewModel::class.java -> {
                CommentsViewModel(model) as T
            }
            HistoryViewModel::class.java -> {
                HistoryViewModel(model) as T
            }
            AttachmentsViewModel::class.java -> {
                AttachmentsViewModel(model) as T
            }
            NewAttachmentViewModel::class.java -> {
                if(taskId != null) {
                    NewAttachmentViewModel(model, taskId) as T
                }
                else
                    throw IllegalArgumentException("Bad taskId")
            }

            TaskViewModel::class.java -> {
                if (taskId != null)
                    TaskViewModel(model = model, taskId = taskId) as T
                else
                    throw IllegalArgumentException("Bad taskId")
            }

            ProfileViewModel::class.java -> {
                    ProfileViewModel(model = model) as T
            }

            UserViewModel::class.java -> {
                if(userId != null)
                    UserViewModel(model = model, userId = userId) as T
                else
                    throw IllegalArgumentException("Bad userId")
            }

            NewAccountViewModel::class.java -> {
                NewAccountViewModel(model = model) as T
            }

            NewTaskViewModel::class.java -> {
                if(projectId != null && userId != null)
                    NewTaskViewModel(model = model, projectId = projectId, userId = userId) as T
                else
                    throw IllegalArgumentException("Bad projectId or userId")
            }

            ProjectViewModel::class.java -> {
                if (projectId != null)
                    ProjectViewModel(model = model, projectId = projectId) as T
                else {
                    throw IllegalArgumentException("Bad projectId")
                }
            }

            NewProjectViewModel::class.java -> {
                NewProjectViewModel() as T
            }

            ProjectsViewModel::class.java -> {
                ProjectsViewModel(model) as T
            }

            UsersViewModel::class.java -> {
                UsersViewModel(model) as T
            }
            FeedbacksViewModel::class.java -> {
                FeedbacksViewModel(model) as T
            }

            TasksViewModel::class.java ->{
                TasksViewModel(model) as T
            }

            TeamsViewModel::class.java -> {
                TeamsViewModel(model) as T
            }

            TeamViewModel::class.java -> {
                if (teamId != null)
                    TeamViewModel(model = model, teamId = teamId) as T
                else {
                    throw IllegalArgumentException("Bad teamId")
                }
            }

            NewTeamViewModel::class.java -> {
                if (userId != null)
                    NewTeamViewModel(model = model, userId = userId) as T

                else
                    throw IllegalArgumentException("Bad userID")
            }

            ChatViewModel::class.java -> {
                if (userId != null && teamId != null)
                    ChatViewModel(model = model, userId = userId, teamId = teamId) as T
                else {
                    throw IllegalArgumentException("Bad userId or teamId")
                }
            }

            ChatsViewModel::class.java -> {
                ChatsViewModel(model) as T
            }

            else -> {
                throw IllegalArgumentException("Bad ViewModel class")
            }
        }
    }
}