package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import com.teamon.app.Model
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.classes.Comment
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class TasksViewModel(val model: Model): ViewModel() {


    fun getTask(taskId: String) = model.getTask(taskId)

    fun getTaskProject(taskId: String): Flow<Project> = callbackFlow {
        model.getProjects().collect { projects ->
            val project = projects.values.firstOrNull { it.tasks.contains(taskId) }
            if(project!=null){
                trySend(project)
            }
        }
        awaitClose {  }
    }
    fun getUserTasks() = model.getUserTasks()

    fun getTaskProjectAdmins(taskId: String) : Flow<Map<String,User>> = callbackFlow {
        val admins = mutableMapOf<String,User>()
        getTaskProject(taskId).collect{ project ->
            projectsViewModel.getProjectAdmins(project.projectId).collect{
                admins.clear()
                admins.putAll(it)
                trySend(admins)
            }
        }
        awaitClose {  }
    }

    suspend fun deleteTask(projectId: String, taskId: String) {
        model.deleteTask(projectId, taskId)
    }

    fun uploadFile(attachmentId: String, file: File) = model.uploadFile(attachmentId, file)

    suspend fun addComment(taskId: String, comment: Comment) = model.addComment(taskId, comment)

}