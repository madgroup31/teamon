package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.projectsViewModel
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(val model: Model): ViewModel() {
    lateinit var projects: StateFlow<Map<String, Project>>

    init {
        viewModelScope.launch {
            projects = model.getProjects().stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyMap())
        }
    }


    fun addProject(project: Project) = model.addProject(project)

    suspend fun deleteProject(projectId: String) = model.deleteProject(projectId)

    fun getProjects() = model.getProjects()

    fun getProject(projectId: String) = model.getProject(projectId)

    fun getProjectTasks(projectId: String): Flow<Map<String, Task>> = channelFlow {
        val tasks = mutableMapOf<String, Task>()

        model.getProject(projectId).collect { project ->
            tasks.clear()
            project.tasks.forEach { taskId ->
                launch {
                    model.getTask(taskId).collect { task ->
                        tasks[task.taskId] = task
                        send(tasks)
                    }
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }

    fun getProjectTeams(projectId: String): Flow<Map<String, Team>> = channelFlow {
        val teams = mutableMapOf<String, Team>()

        model.getProject(projectId).collect { project ->
            teams.clear()
            project.teams.forEach { teamId ->
                launch {
                    model.getTeam(teamId).collect { team ->
                        teams[team.teamId] = team
                        send(teams)
                    }
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }

    fun getProjectAdmins(projectId: String): Flow<Map<String, User>> = channelFlow {
        val admins = mutableMapOf<String, User>()

        getProjectTeams(projectId).collect { teams ->
            admins.clear()
            teams.values.flatMap { it.admin }.forEach { admin ->
                launch {
                    model.getUser(admin).collect { user ->
                        admins[user.userId] = user
                        send(admins)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }

    fun getProjectMembers(projectId: String): Flow<Map<String, User>> = channelFlow {
        val members = mutableMapOf<String, User>()

        getProjectTeams(projectId).collect { teams ->
            members.clear()
            teams.values.flatMap { it.users }.forEach { member ->
                launch {
                    model.getUser(member).collect { user ->
                        members[user.userId] = user
                        send(members)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }

    fun getProjectFeedbacks(projectId: String): Flow<Map<String, Feedback>> = channelFlow {
        val feedbacks = mutableMapOf<String, Feedback>()

        getProject(projectId).collect { project ->
            feedbacks.clear()
            project.feedbacks.forEach { feedbackId ->
                launch {
                    model.getFeedback(feedbackId).collect { feedback ->
                        feedbacks[feedback.feedbackId] = feedback
                        send(feedbacks)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }



    fun getProjectOverdueTasks(projectId: String): Flow<Map<String, Task>> = channelFlow {
        val tasks = mutableMapOf<String, Task>()

        model.getProject(projectId).collect { project ->
            tasks.clear()
            project.tasks.forEach { taskId ->
                launch {
                    model.getTask(taskId).collect { task ->
                        if(task.endDate < Timestamp.now() && task.status != TaskStatus.Completed) {
                            tasks[task.taskId] = task
                            send(tasks)
                        }
                    }
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }

    fun getProjectCompletedTasks(projectId: String): Flow<Map<String, Task>> = channelFlow {
        val tasks = mutableMapOf<String, Task>()

        model.getProject(projectId).collect { project ->
            tasks.clear()
            project.tasks.forEach { taskId ->
                launch {
                    model.getTask(taskId).collect { task ->
                        if(task.status == TaskStatus.Completed) {
                            tasks[task.taskId] = task
                            send(tasks)
                        }
                    }
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }

    fun getUserProjects(userId: String): Flow<Map<String, Project>> = channelFlow {
        val projects = mutableMapOf<String, Project>()

        usersViewModel.getUserTeams(userId).collect {teams ->
            projects.clear()
            teams.values.forEach {team ->
                launch {
                    projectsViewModel.getProjects().collect { p ->
                        projects.putAll(p.values.filter { it.teams.contains(team.teamId) }.associateBy { it.projectId })
                        send(projects)
                    }
                }
            }
        }

        awaitClose { }
    }

    fun getTeamProjects(teamId: String): Flow<List<Project>> = model.getProjectsByTeamId(teamId)




}