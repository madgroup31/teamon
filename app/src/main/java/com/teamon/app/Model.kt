package com.teamon.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.TransactionOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.OnPausedListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.teamon.app.utils.classes.Attachment
import com.teamon.app.utils.classes.Chat
import com.teamon.app.utils.classes.Comment
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.History
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.Performance
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.HistoryIcons
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.asDate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File




class Model(val context: Context) {
    init {
        FirebaseApp.initializeApp(context)
    }

    val db = Firebase.firestore
    val storageRef = Firebase.storage.reference


    fun addFeedbackToUser(feedbackToAdd: Feedback, userId: String) {

        val feedbackMap = mapOf(
            "authorId" to feedbackToAdd.authorId,
            "description" to feedbackToAdd.description,
            "value" to feedbackToAdd.value,
            "anonymous" to feedbackToAdd.anonymous,
            "timestamp" to feedbackToAdd.timestamp
        )

        db.collection("feedbacks")
            .add(feedbackMap)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Feedback added with ID: ${documentReference.id}")
                val newFeedbackId = documentReference.id

                db.collection("users")
                    .document(userId)
                    .update("feedbacks", FieldValue.arrayUnion(newFeedbackId))
                    .addOnSuccessListener {
                        Log.d("Firestore", "Feedback ID added to user with ID: $userId")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating project with new feedback ID", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding feedback", e)
            }
    }


    fun addFeedbackByProjectId(feedbackToAdd: Feedback, projectId: String) {

        val feedbackMap = mapOf(
            "authorId" to feedbackToAdd.authorId,
            "description" to feedbackToAdd.description,
            "value" to feedbackToAdd.value,
            "anonymous" to feedbackToAdd.anonymous,
            "timestamp" to feedbackToAdd.timestamp
        )

        db.collection("feedbacks")
            .add(feedbackMap)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Feedback added with ID: ${documentReference.id}")
                val newFeedbackId = documentReference.id

                db.collection("projects")
                    .document(projectId)
                    .update("feedbacks", FieldValue.arrayUnion(newFeedbackId))
                    .addOnSuccessListener {
                        Log.d("Firestore", "Feedback ID added to project with ID: $projectId")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating project with new feedback ID", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding feedback", e)
            }
    }


    fun addFeedbackByTeamId(feedbackToAdd: Feedback, teamId: String) {


        val feedbackMap = mapOf(
            "authorId" to feedbackToAdd.authorId,
            "description" to feedbackToAdd.description,
            "value" to feedbackToAdd.value,
            "anonymous" to feedbackToAdd.anonymous,
            "timestamp" to feedbackToAdd.timestamp
        )

        db.collection("feedbacks")
            .add(feedbackMap)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Feedback added with ID: ${documentReference.id}")
                val newFeedbackId = documentReference.id

                db.collection("teams")
                    .document(teamId)
                    .update("feedback", FieldValue.arrayUnion(newFeedbackId))
                    .addOnSuccessListener {
                        Log.d("Firestore", "Feedback ID added to team with ID: $teamId")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating team with new feedback ID", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding feedback", e)
            }
    }

    fun addTeam(team: Team) {
        val teamMap = mapOf(

            "name" to team.name,
            "description" to team.description,
            "image" to team.image,
            "imageSource" to team.imageSource,
            "admin" to team.admin,
            "users" to team.users,
            "category" to team.category,
            "creationDate" to team.creationDate,
            "feedback" to team.feedback,
            "performance" to team.performance,
            "color" to team.color
        )

        db.collection("teams")
            .add(teamMap)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Team added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding team", e)
            }

    }

    suspend fun addTask(projectId: String, task: Task): Boolean {

        val taskMap = mapOf(
            "taskName" to task.taskName,
            "tag" to task.tag,
            "status" to task.status,
            "repeat" to task.repeat,
            "recurringType" to task.recurringType,
            "projectName" to task.projectName,
            "priority" to task.priority,
            "listUser" to task.listUser,
            "history" to task.history,
            "endRepeat" to task.endRepeat,
            "endDate" to task.endDate,
            "description" to task.description,
            "creationDate" to task.creationDate,
            "comments" to task.comments,
            "attachments" to task.attachments,
        )
        val user = profileViewModel.nameValue + " " + profileViewModel.surnameValue
        val historyMap = mapOf(
            "text" to "$user created the task \"${task.taskName}\"",
            "icon" to HistoryIcons.CREATION,
            "user" to profileViewModel.userId,
            "timestamp" to Timestamp.now()
        )
        val projectRef = db.collection("projects").document(projectId)
        return try {
            db.runTransaction() { transaction ->
                val snapShot = transaction.get(projectRef)
                val taskRef = db.collection("tasks").document()
                transaction.set(taskRef, taskMap)
                transaction.update(projectRef, "tasks", FieldValue.arrayUnion(taskRef.id))
                val historyRef = db.collection("history").document()
                transaction.set(historyRef, historyMap)
                transaction.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addRecursiveTasks(projectId: String, tasks: List<Task>): Boolean {
        val projectRef = db.collection("projects").document(projectId)
        return try {
            db.runTransaction { transaction ->
                val snapShot = transaction.get(projectRef)
                tasks.forEach { task ->
                    val taskMap = mapOf(
                        "taskName" to task.taskName,
                        "tag" to task.tag,
                        "status" to task.status,
                        "repeat" to task.repeat,
                        "recurringType" to task.recurringType,
                        "projectName" to task.projectName,
                        "priority" to task.priority,
                        "listUser" to task.listUser,
                        "history" to task.history,
                        "recurringSet" to task.recurringSet,
                        "endRepeat" to task.endRepeat,
                        "endDate" to task.endDate,
                        "description" to task.description,
                        "creationDate" to task.creationDate,
                        "comments" to task.comments,
                        "attachments" to task.attachments,
                    )
                    val user = profileViewModel.nameValue + " " + profileViewModel.surnameValue
                    val historyMap = mapOf(
                        "text" to "$user created the task \"${task.taskName}\"",
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val taskRef = db.collection("tasks").document()
                    transaction.set(taskRef, taskMap)
                    transaction.update(projectRef, "tasks", FieldValue.arrayUnion(taskRef.id))
                    val historyRef = db.collection("history").document()
                    transaction.set(historyRef, historyMap)
                    transaction.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addProject(project: Project) {
        val projectMap = mapOf(
            "projectName" to project.projectName,
            "projectColor" to project.projectColor,
            "projectImage" to project.projectImage,
            "description" to project.description,
            "endDate" to project.endDate,
            "teams" to project.teams,
            "progress" to project.progress,
            "tasks" to project.tasks
        )
        db.collection("projects")
            .add(projectMap)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Project added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding team", e)
            }

    }

    suspend fun deleteProject(projectId: String): Boolean {
        val projectRef = db.collection("projects").document(projectId)

        return try {
            db.runTransaction(
                TransactionOptions.Builder().setMaxAttempts(1).build()
            ) { transaction ->
                val snapShot = transaction.get(projectRef)
                val tasks = transaction.get(projectRef).get("tasks") as List<String>
                tasks.forEach { transaction.delete(db.collection("tasks").document(it)) }
                transaction.delete(projectRef)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getHistory(historyId: String): Flow<History> = callbackFlow {
        val listener = db
            .collection("history")
            .document(historyId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val history = result.toObject(History::class.java)
                    history?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching history")
                }


            }
        awaitClose { listener.remove() }
    }

    fun getComment(commentId: String): Flow<Comment> = callbackFlow {
        val listener = db
            .collection("comments")
            .document(commentId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val comment = result.toObject(Comment::class.java)
                    comment?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching comment")
                }

            }
        awaitClose { listener.remove() }
    }

    fun deleteTeam(teamId: String) {
        val teamRef = db.collection("teams").document(teamId)

        teamRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Fetch all projects that include this team
                db.collection("projects")
                    .whereArrayContains("teams", teamId)
                    .get()
                    .addOnSuccessListener { result ->
                        val projects = result.documents.mapNotNull { documentSnapshot ->
                            documentSnapshot.toObject(Project::class.java)
                        }

                        // Fetch all chats associated with the team
                        db.collection("chats")
                            .whereEqualTo("teamId", teamId)
                            .get()
                            .addOnSuccessListener { chatSnapshots ->
                                val chatIds = chatSnapshots.documents.map { it.id }

                                if(chatIds.isNotEmpty())
                                {
                                // Fetch all messages associated with the chats
                                db.collection("messages")
                                    .whereIn("chatId", chatIds)
                                    .get()
                                    .addOnSuccessListener { messageSnapshots ->
                                        // Create a batch to perform all deletions and updates
                                        val batch = db.batch()

                                        // Remove the team from the projects
                                        for (project in projects) {
                                            val newTeams = project.teams.filter { it != teamId }
                                            val projectRef = db.collection("projects").document(project.projectId)
                                            batch.update(projectRef, "teams", newTeams)
                                        }

                                        // Delete all messages associated with the chats
                                        for (message in messageSnapshots.documents) {
                                            batch.delete(message.reference)
                                        }

                                        // Delete all chats associated with the team
                                        for (chat in chatSnapshots.documents) {
                                            batch.delete(chat.reference)
                                        }

                                        // Delete the team itself
                                        batch.delete(teamRef)

                                        // Commit the batch
                                        batch.commit().addOnSuccessListener {
                                            Log.d("Firestore", "Successfully deleted team, chats, messages, and updated projects")
                                        }.addOnFailureListener { e ->
                                            Log.w("Firestore", "Error deleting team, chats, messages, or updating projects", e)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error fetching messages", e)
                                    }
                                    }
                                else {
                                    // Create a batch to perform all deletions and updates
                                    val batch = db.batch()

                                    // Remove the team from the projects
                                    for (project in projects) {
                                        val newTeams = project.teams.filter { it != teamId }
                                        val projectRef = db.collection("projects").document(project.projectId)
                                        batch.update(projectRef, "teams", newTeams)
                                    }

                                    // Delete all chats associated with the team
                                    for (chat in chatSnapshots.documents) {
                                        batch.delete(chat.reference)
                                    }

                                    // Delete the team itself
                                    batch.delete(teamRef)

                                    // Commit the batch
                                    batch.commit().addOnSuccessListener {
                                        Log.d("Firestore", "Successfully deleted team, chats, messages, and updated projects")
                                    }.addOnFailureListener { e ->
                                        Log.w("Firestore", "Error deleting team, chats, messages, or updating projects", e)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error fetching chats", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching projects", e)
                    }
            } else {
                Log.w("Firestore", "Team with ID: $teamId does not exist")
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching team", e)
        }
    }

    suspend fun removeMemberFromATeam(teamId: String, memberIdToRemove: String): Boolean {
        val usersList = db.collection("teams").document(teamId).get().await()
            .get("users") as MutableList<String>
        usersList.remove(memberIdToRemove)
        return try {
            db.collection("teams").document(teamId).update("users", usersList).await()
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Error removing user from team", e)
            false
        }
    }

    fun promoteMemberToAdminByTeamId(teamId: String, admin: List<String>) {

        val updateFields = mapOf(
            "admin" to admin
        )

        val docRef = db.collection("teams").document(teamId)
        docRef.update(updateFields)
            .addOnSuccessListener {
                println("Admin successfully updated")
            }
            .addOnFailureListener { e ->
                println("Error updating admin: $e")
            }

    }


    suspend fun updateTeam(teamId: String, team: Team): Boolean {
        val updateFields = mapOf(
            "name" to team.name,
            "description" to team.description,
            "category" to team.category,
            "image" to team.image,
            "imageSource" to team.imageSource,
            "creationDate" to team.creationDate,
            "color" to team.color,
            "admin" to team.admin,
            "feedback" to team.feedback,
            "users" to team.users,
        )

        val docRef = db.collection("teams").document(teamId)
        return try {
            docRef.update(updateFields).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getTeamProjects(teamId: String): Flow<Map<String, Project>> = callbackFlow {

    }

    fun getProjectsByTeamId(teamId: String): Flow<List<Project>> = callbackFlow {
        val listener = db
            .collection("projects")
            .whereArrayContains("teams", teamId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val projects = result.documents.mapNotNull { documentSnapshot ->
                        documentSnapshot.toObject(Project::class.java)
                    }
                    trySend(projects)
                } else {
                    Log.e("Firebase", "Error fetching projects")
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getMembersByTeamId(teamId: String): Flow<List<User>> = callbackFlow {
        val teamDocument = db.collection("teams").document(teamId)

        val teamListener = teamDocument.addSnapshotListener { teamSnapshot, teamError ->

            if (teamSnapshot != null) {
                val teamUsersId = teamSnapshot.get("users") as? List<String> ?: emptyList()

                val usersListener = db.collection("users")
                    .addSnapshotListener { usersSnapshot, usersError ->

                        if (usersSnapshot != null) {
                            val users = usersSnapshot.documents
                                .mapNotNull { it.toObject(User::class.java) }
                                .filter { user -> teamUsersId.contains(user.userId) }

                            trySend(users)

                        } else
                            if (usersError != null) {
                                Log.e("Firebase", "Error fetching users", usersError)
                                close(usersError)
                            }
                    }
            } else if (teamError != null) {
                Log.e("Firebase", "Error fetching team document", teamError)
                close(teamError)
            }
        }
        awaitClose { teamListener.remove() }
    }

    fun getFeedbacksByTeamId(teamId: String): Flow<List<Feedback>> = callbackFlow {
        val teamDocument = db.collection("teams").document(teamId)

        val teamListener = teamDocument.addSnapshotListener { teamSnapshot, teamError ->

            if (teamSnapshot != null) {
                val teamFeedbacksId = teamSnapshot.get("feedback") as? List<String> ?: emptyList()

                val feedbacksListener = db.collection("feedbacks")
                    .addSnapshotListener { feedbacksSnapshot, feedbacksError ->

                        if (feedbacksSnapshot != null) {
                            val feedbacks = feedbacksSnapshot.documents
                                .mapNotNull { it.toObject(Feedback::class.java) }
                                .filter { feedback -> teamFeedbacksId.contains(feedback.feedbackId) }

                            trySend(feedbacks)

                        } else
                            if (feedbacksError != null) {
                                Log.e("Firebase", "Error fetching feedbacks", feedbacksError)
                                close(feedbacksError)
                            }
                    }
            } else if (teamError != null) {
                Log.e("Firebase", "Error fetching team document", teamError)
                close(teamError)
            }
        }

        awaitClose { teamListener.remove() }
    }



    fun getUsers(): Flow<Map<String, User>> = callbackFlow {
        val myTeams = db
            .collection("teams")
            .whereArrayContains("users", profileViewModel.userId)
            .get()
            .await()
            .documents
            .map { it.id }

        val knownUsers: List<String>

        if (myTeams.isNotEmpty()) {

            val commonProjects = db
                .collection("projects")
                .whereArrayContainsAny("teams", myTeams)
                .get()
                .await()
                .toObjects(Project::class.java)
                .map { it.teams }

            knownUsers = commonProjects
                .flatten()
                .map { teamId ->
                    db
                        .collection("teams")
                        .document(teamId)
                        .get()
                        .await()
                        .get("users") as List<String>
                }
                .flatten()
                .distinct()
        } else {
            knownUsers = listOf(profileViewModel.userId)
        }

        val batchSize = 10
        val queryBatches = knownUsers.chunked(batchSize)

        val listeners = queryBatches.map { batch ->
            db
                .collection("users")
                .whereIn(FieldPath.documentId(), batch)
                .addSnapshotListener { result, error ->
                    if (result != null) {
                        val users = result.toObjects(User::class.java).associateBy { it.userId }
                        trySend(users)
                    } else {
                        Log.e("Firebase", "Error fetching users", error)
                        trySend(emptyMap())
                    }
                }
        }
        awaitClose { listeners.forEach { it.remove() } }
    }

    suspend fun exists(userId: String): Boolean {
        return try {
            db.collection("users")
                .document(userId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    fun getUser(userId: String): Flow<User> = callbackFlow {
        val listener = db
            .collection("users")
            .document(userId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val user = result.toObject(User::class.java)
                    user?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching user")
                }

            }
        awaitClose { listener.remove() }
    }

    suspend fun addUser(user: User): String? {
        val userRef = db.collection("users").document()

        return try {
            db.runTransaction(TransactionOptions.Builder().setMaxAttempts(1).build()) {
                val snapShot = it.get(userRef)

                val userMap = mapOf(
                    "name" to user.name,
                    "surname" to user.surname,
                    "nickname" to user.nickname,
                    "email" to user.email,
                    "location" to user.location,
                    "birthdate" to user.birthdate,
                    "biography" to user.biography,
                    "color" to user.color,
                    "lastUpdate" to user.lastUpdate,
                    "profileImageSource" to user.profileImageSource,
                    "profileImage" to user.profileImage,
                    "feedbacks" to user.feedbacks,
                    "favorites" to user.favorites
                )

                it.set(userRef, userMap)
            }.await()
            userRef.id
        } catch (e: Exception) {
            null
        }

    }

    suspend fun updateUser(userId: String, user: User): Boolean {
        val userRef = db.collection("users").document(userId)

        return try {
            db.runTransaction(TransactionOptions.Builder().setMaxAttempts(1).build()) {
                val snapShot = it.get(userRef)

                val userMap = mapOf(
                    "name" to user.name,
                    "surname" to user.surname,
                    "nickname" to user.nickname,
                    "email" to user.email,
                    "location" to user.location,
                    "birthdate" to user.birthdate,
                    "biography" to user.biography,
                    "color" to user.color,
                    "lastUpdate" to user.lastUpdate,
                    "profileImageSource" to user.profileImageSource,
                    "profileImage" to user.profileImage,
                    "feedbacks" to user.feedbacks,
                    "favorites" to user.favorites
                )

                it.set(userRef, userMap)

            }.await()
            true
        } catch (e: Exception) {
            false
        }

    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            db.collection("users").document(userId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setUserFavorites(userId: String, favorites: List<String>): Boolean {
        val userRef = db.collection("users").document(userId)

        return try {
            db.runTransaction(TransactionOptions.Builder().setMaxAttempts(1).build()) {
                val snapShot = it.get(userRef)

                it.update(userRef, "favorites", favorites)

            }.await()
            true
        } catch (e: Exception) {
            false
        }

    }

    fun getAttachment(attachmentId: String): Flow<Attachment> = callbackFlow {
        val listener = db
            .collection("attachments")
            .document(attachmentId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val attachment = result.toObject(Attachment::class.java)
                    attachment?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching attachment")
                }

            }
        awaitClose { listener.remove() }
    }

    suspend fun createAttachment(taskId: String, attachment: Attachment): String? {

        val attachmentMap = mapOf(
            "ownerId" to attachment.ownerId,
            "name" to attachment.name,
            "description" to attachment.description,
            "fileType" to attachment.fileType,
            "fileSize" to attachment.fileSize,
            "uploadedOn" to attachment.uploadedOn,
            "downloadUrl" to attachment.downloadUrl
        )

        val taskRef = db.collection("tasks").document(taskId)
        Log.d("attachment", "taskId: "+taskId)
        return try {
            db.runTransaction { transaction ->
                val snapShot = transaction.get(taskRef)
                val attachmentRef = db.collection("attachments").document()
                transaction.set(attachmentRef, attachmentMap)
                transaction.update(taskRef, "attachments", FieldValue.arrayUnion(attachmentRef.id))
                attachmentRef.id
            }.await()
        } catch (e: Exception) {
            Log.d("attachment", e.message.toString())
            null
        }
    }

    suspend fun updateAttachment(attachmentId: String, attachment: Attachment): Boolean {
        val attachmentRef = db.collection("attachments").document(attachmentId)

        return try {
            db.runTransaction(TransactionOptions.Builder().setMaxAttempts(1).build()) {
                val snapShot = it.get(attachmentRef)

                it.update(attachmentRef, "name", attachment.name)
                it.update(attachmentRef, "description", attachment.description)
                it.update(attachmentRef, "uploadedOn", attachment.uploadedOn)
                it.update(attachmentRef, "ownerId", attachment.ownerId)
                it.update(attachmentRef, "downloadUrl", attachment.downloadUrl)
                it.update(attachmentRef, "fileSize", attachment.fileSize)
                it.update(attachmentRef, "fileType", attachment.fileType)

            }.await()
            true
        } catch (e: Exception) {
            false
        }

    }

    suspend fun deleteAttachment(taskId: String, attachmentId: String): Boolean {
        return try {
            Log.d("attachment", taskId)
            Log.d("attachment", attachmentId)
            //storageRef.child("files/${attachmentId}").delete().await()
            val taskRef = db.collection("tasks").document(taskId)
            val attachmentRef = db.collection("attachments").document(attachmentId)

            db.runTransaction { transaction ->
                val snapShot = transaction.get(taskRef)
                transaction.update(
                    taskRef,
                    "attachments",
                    FieldValue.arrayRemove(attachmentRef.id)
                )
                transaction.delete(attachmentRef)
            }.await()
            true
        } catch (e: Exception) {
            Log.d("attachment", e.message.toString())
            false
        }
    }

    fun getFeedbacks(): Flow<Map<String, Feedback>> = callbackFlow {
        val listener = db
            .collection("feedbacks")
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val feedbacks =
                        result.toObjects(Feedback::class.java).associateBy { it.feedbackId }
                    trySend(feedbacks)
                } else {
                    Log.e("Firebase", "Error fetching feedbacks")
                    trySend(emptyMap())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getFeedback(feedbackId: String): Flow<Feedback> = callbackFlow {
        val listener = db
            .collection("feedbacks")
            .document(feedbackId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val feedback = result.toObject(Feedback::class.java)
                    feedback?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching feedback")
                }

            }
        awaitClose { listener.remove() }
    }

    fun uploadImage(id: String, file: File): Flow<UploadStatus> = callbackFlow {
        val imageRef = storageRef.child("images/$id")

        val progressListener = OnProgressListener<UploadTask.TaskSnapshot> {
            trySend(UploadStatus.Progress((it.bytesTransferred / it.totalByteCount).toFloat()))
        }
        val pausedListener = OnPausedListener<UploadTask.TaskSnapshot> {
            trySend(UploadStatus.Progress((it.bytesTransferred / it.totalByteCount).toFloat()))
        }
        val canceledListener = OnCanceledListener {
            trySend(UploadStatus.Error("Upload canceled. Please try again."))
        }
        val failureListener = OnFailureListener {
            trySend(UploadStatus.Error("Upload failed. Please try again."))
        }
        val successListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            launch {
                val downloadUri = imageRef.downloadUrl.await()
                trySend(UploadStatus.Success(downloadUri.toString()))
            }
        }

        val uploadTask = imageRef
            .putFile(file.toUri())
            .addOnProgressListener(progressListener)
            .addOnPausedListener(pausedListener)
            .addOnCanceledListener(canceledListener)
            .addOnFailureListener(failureListener)
            .addOnSuccessListener(successListener)

        awaitClose {
            uploadTask.removeOnProgressListener(progressListener)
            uploadTask.removeOnPausedListener(pausedListener)
            uploadTask.removeOnCanceledListener(canceledListener)
            uploadTask.removeOnFailureListener(failureListener)
            uploadTask.removeOnSuccessListener(successListener)
        }
    }

    fun uploadFile(id: String, file: File): Flow<UploadStatus> = callbackFlow {
        val fileRef = storageRef.child("files/$id")

        val progressListener = OnProgressListener<UploadTask.TaskSnapshot> {
            trySend(UploadStatus.Progress((it.bytesTransferred / it.totalByteCount).toFloat()))
        }
        val pausedListener = OnPausedListener<UploadTask.TaskSnapshot> {
            trySend(UploadStatus.Progress((it.bytesTransferred / it.totalByteCount).toFloat()))
        }
        val canceledListener = OnCanceledListener {
            trySend(UploadStatus.Error("Upload canceled. Please try again."))
        }
        val failureListener = OnFailureListener {
            trySend(UploadStatus.Error("Upload failed. Please try again."))
        }
        val successListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            launch {
                val downloadUri = fileRef.downloadUrl.await()
                trySend(UploadStatus.Success(downloadUri.toString()))
            }
        }

        val uploadTask = fileRef
            .putFile(file.toUri())
            .addOnProgressListener(progressListener)
            .addOnPausedListener(pausedListener)
            .addOnCanceledListener(canceledListener)
            .addOnFailureListener(failureListener)
            .addOnSuccessListener(successListener)

        awaitClose {
            uploadTask.removeOnProgressListener(progressListener)
            uploadTask.removeOnPausedListener(pausedListener)
            uploadTask.removeOnCanceledListener(canceledListener)
            uploadTask.removeOnFailureListener(failureListener)
            uploadTask.removeOnSuccessListener(successListener)
        }
    }

    fun getProjects(): Flow<Map<String, Project>> = callbackFlow {
        val myTeams = db
            .collection("teams")
            .whereArrayContains("users", profileViewModel.userId)
            .get()
            .await()
            .map { it.id }

        if (myTeams.isEmpty()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val listener = db
            .collection("projects")
            .whereArrayContainsAny("teams", myTeams)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val projects =
                        result.toObjects(Project::class.java).associateBy { it.projectId }
                    trySend(projects)
                } else {
                    Log.e("Firebase", "Error fetching projects")
                    trySend(emptyMap())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getProject(projectId: String): Flow<Project> = callbackFlow {
        val listener = db
            .collection("projects")
            .document(projectId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val project = result.toObject(Project::class.java)
                    project?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching project")
                }

            }
        awaitClose { listener.remove() }
    }

    fun getTask(taskId: String): Flow<Task> = callbackFlow {
        val listener = db
            .collection("tasks")
            .document(taskId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val task = result.toObject(Task::class.java)
                    task?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching task")
                }

            }
        awaitClose { listener.remove() }
    }

    fun getUserTasks(): Flow<Map<String, Task>> = callbackFlow {
        val auth = Firebase.auth
        val listener = db
            .collection("tasks")
            .whereArrayContains("listUser", profileViewModel.userId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val tasks =
                        result.toObjects(Task::class.java).associateBy { it.taskId }
                    trySend(tasks)
                } else {
                    Log.e("Firebase", "Error fetching tasks")
                    trySend(emptyMap())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getTeams(): Flow<Map<String, Team>> = callbackFlow {

        val listener = db
            .collection("teams")
            .whereArrayContains("users", profileViewModel.userId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val teams =
                        result.toObjects(Team::class.java).associateBy { it.teamId }
                    trySend(teams)
                } else {
                    Log.e("Firebase", "Error fetching teams")
                    trySend(emptyMap())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getTeam(teamId: String): Flow<Team> = callbackFlow {
        val listener = db
            .collection("teams")
            .document(teamId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val team = result.toObject(Team::class.java)
                    team?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching team")
                }

            }
        awaitClose { listener.remove() }
    }

    fun getUserTeams(userId: String): Flow<Map<String, Team>> = callbackFlow {
        val listener = db
            .collection("teams")
            .whereArrayContains("users", userId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val teams =
                        result.toObjects(Team::class.java).associateBy { it.teamId }
                    trySend(teams)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addTeamMember(userId: String, teamId: String): Boolean {
        val teamRef = db.collection("teams").document(teamId)

        return try {
            db.runTransaction(TransactionOptions.Builder().setMaxAttempts(1).build()) {
                val snapShot = it.get(teamRef).get("users") as MutableList<String>
                snapShot.add(userId)
                it.update(teamRef, "users", snapShot.toList())
            }.await()
            true
        } catch (e: Exception) {
            Log.d("exc", e.message?.toString() ?: "null")
            false
        }


    }

    fun getUserChats(teamId: String): Flow<Map<String, Chat>> = callbackFlow {
        val listener = db
            .collection("chats")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("personal", true)
            .whereArrayContains("userIds", profileViewModel!!.userId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val chats = result.toObjects(Chat::class.java).associateBy { it.chatId }
                    trySend(chats)
                } else {
                    Log.e("Firebase", "Error fetching chats")
                    trySend(emptyMap())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getChat(userId: String, teamId: String): Flow<Chat> = callbackFlow {
        val listener = db
            .collection("chats")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("personal", true)
            .whereArrayContains("userIds", profileViewModel!!.userId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val chats = result.toObjects(Chat::class.java)
                    val chat = chats.firstOrNull { it.userIds.contains(userId) }
                    if (chat != null)
                        trySend(chat)

                } else {
                    Log.e("Firebase", "Error fetching chat")
                    trySend(Chat())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getChatById(chatId: String): Flow<Chat> = callbackFlow {
        val listener = db
            .collection("chats")
            .document(chatId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val chat = result.toObject(Chat::class.java)
                    if (chat != null)
                        trySend(chat)

                } else {
                    Log.e("Firebase", "Error fetching chat")
                    trySend(Chat())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getTeamChat(teamId: String): Flow<Chat> = callbackFlow {
        val listener = db
            .collection("chats")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("personal", false)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val chat = result.toObjects(Chat::class.java).firstOrNull()
                    chat?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching chat")
                }

            }
        awaitClose { listener.remove() }
    }

    fun getChatMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = db
            .collection("messages")
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val messages = result.toObjects(Message::class.java)
                    trySend(messages)
                } else {
                    Log.e("Firebase", "Error fetching messages")
                    trySend(emptyList())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getLastChatMessage(chatId: String): Flow<Message> = callbackFlow {
        val listener = db
            .collection("messages")
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val messages = result.toObjects(Message::class.java)
                    val lastMessage = messages.maxByOrNull { it.timestamp }
                        if (lastMessage!=null)
                                trySend(lastMessage)
                } else {
                    Log.e("Firebase", "Error fetching messages")
                    trySend(Message())
                }

            }
        awaitClose { listener.remove() }
    }

    fun getUnreadMessages(chatId: String): Flow<Int> = callbackFlow {
        val listener = db
            .collection("messages")
            .whereEqualTo("chatId", chatId)
            .whereArrayContains("unread", profileViewModel!!.userId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val messages = result.toObjects(Message::class.java)
                    trySend(messages.size)
                } else {
                    Log.e("Firebase", "Error fetching messages")
                    trySend(0)
                }
            }
        awaitClose { listener.remove() }
    }

    fun setMessageRead(messageId: String) {
        db.collection("messages")
            .document(messageId)
            .update("unread", FieldValue.arrayRemove(profileViewModel.userId))
            .addOnSuccessListener {
                Log.d("Firestore", "Message successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating message", e)
            }
    }

    fun isMessageRead(messageId: String, userId: String): Flow<Boolean> = callbackFlow {
        val listener = db
            .collection("messages")
            .document(messageId)
            .addSnapshotListener { result, error ->
                if (result != null) {
                    val message = result.toObject(Message::class.java)
                    if (message != null) {
                        trySend(!message.unread.contains(userId))
                    } else {
                        trySend(false)
                    }
                } else {
                    Log.e("Firebase", "Error fetching message")
                    trySend(false)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getMessage(messageId: String): Flow<Message> = callbackFlow {
        val listener = db
            .collection("messages")
            .document(messageId)
            .addSnapshotListener { result, error ->

                if (result != null) {
                    val message = result.toObject(Message::class.java)
                    message?.let { trySend(it) }
                } else {
                    Log.e("Firebase", "Error fetching feedback")
                }

            }
        awaitClose { listener.remove() }
    }

    fun addMessage(addresseeId: String, authorId: String, teamId: String, text: String) {

        db.collection("chats")
            .whereEqualTo("teamId", teamId)
            .whereArrayContains("userIds", addresseeId)
            //.whereArrayContains("userIds", authorId)
            .get()
            .addOnSuccessListener { chats ->
                val chatId = chats.firstOrNull {
                    (it.get("userIds") as List<String>).contains(
                        profileViewModel!!.userId
                    )
                }?.id
                if (chatId == null) {
                    val newChat = mapOf(
                        "teamId" to teamId,
                        "userIds" to listOf(addresseeId, authorId),
                        "personal" to true
                    )
                    db.collection("chats")
                        .add(newChat)
                        .addOnSuccessListener {
                            val message = mapOf(
                                "chatId" to it.id,
                                "senderId" to profileViewModel!!.userId,
                                "content" to text,
                                "timestamp" to Timestamp(System.currentTimeMillis() / 1000, 0),
                                "unread" to listOf(addresseeId)
                            )
                            db.collection("messages")
                                .add(message)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Message successfully written!")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error writing message", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing chat", e)
                        }
                } else {
                    val message = mapOf(
                        "chatId" to chatId,
                        "senderId" to profileViewModel!!.userId,
                        "content" to text,
                        "timestamp" to Timestamp(System.currentTimeMillis() / 1000, 0),
                        "unread" to listOf(addresseeId)
                    )
                    db.collection("messages")
                        .add(message)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Message successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing message", e)
                        }
                }
            }
    }

    fun addTeamMessage(teamId: String, users: List<String>, text: String) {
        db.collection("chats")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("personal", false)
            .get()
            .addOnSuccessListener { it ->
                var chatId = it.documents.firstOrNull()?.id

                if (chatId == null) {
                    val newChat = mapOf(
                        "teamId" to teamId,
                        "userIds" to emptyList<String>(),
                        "personal" to false
                    )
                    db.collection("chats")
                        .add(newChat)
                        .addOnSuccessListener {
                            val message = mapOf(
                                "chatId" to it.id,
                                "senderId" to profileViewModel.userId,
                                "content" to text,
                                "timestamp" to Timestamp(System.currentTimeMillis() / 1000, 0),
                                "unread" to users.filter { it != profileViewModel.userId },
                            )
                            db.collection("messages")
                                .add(message)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Message successfully written!")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error writing message", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing chat", e)
                        }
                } else {
                    val message = mapOf(
                        "chatId" to chatId,
                        "senderId" to profileViewModel!!.userId,
                        "content" to text,
                        "timestamp" to Timestamp(System.currentTimeMillis() / 1000, 0),
                        "unread" to users.filter { it != profileViewModel.userId },
                    )
                    db.collection("messages")
                        .add(message)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Message successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing message", e)
                        }
                }
            }
    }

    fun deleteMessage(messageId: String) {
        db.collection("messages")
            .document(messageId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Message successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting message", e)
            }
    }

    fun deleteTeamChat(teamId: String) {

        // Step 1: Fetch all chats associated with the team

    }
    fun deleteChat(userId: String, teamId: String) {
        val auth = Firebase.auth

        // Step 1: Fetch the chat document
        db.collection("chats")
            .whereEqualTo("teamId", teamId)
            .whereArrayContains("userIds", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { chats ->
                val chatId = chats.documents
                    .mapNotNull { it.toObject(Chat::class.java) }
                    .firstOrNull { it.userIds.contains(userId) }?.chatId

                if (chatId != null) {
                    // Step 2: Fetch all messages associated with the chat
                    db.collection("messages")
                        .whereEqualTo("chatId", chatId)
                        .get()
                        .addOnSuccessListener { messages ->
                            // Step 3: Run the transaction
                            db.runTransaction { transaction ->
                                for (message in messages.documents) {
                                    transaction.delete(message.reference)
                                }
                                val chatDocumentRef = db.collection("chats").document(chatId)
                                transaction.delete(chatDocumentRef)
                            }.addOnSuccessListener {
                                // Success handling
                                Log.d("chat", "Chat and messages deleted successfully.")
                            }.addOnFailureListener { exception ->
                                // Failure handling
                                Log.d("chat", "Error deleting chat and messages: ${exception.message}")
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Failure handling for fetching messages
                            Log.d("chat", "Error fetching messages: ${exception.message}")
                        }
                } else {
                    Log.d("chat", "Error deleting chat and messages: Chat not found.")
                }
            }
            .addOnFailureListener { exception ->
                // Failure handling for fetching chats
                Log.d("chat", "Error fetching personal chats: ${exception.message}")
            }
    }

    fun editMessage(messageId: String, text: String) {
        db.collection("messages")
            .document(messageId)
            .update("content", text)
            .addOnSuccessListener {
                Log.d("Firestore", "Message successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating message", e)
            }
    }

    suspend fun deleteTask(projectId: String, taskId: String): Boolean {
        val projectRef = db.collection("projects").document(projectId)
        val taskRef = db.collection("tasks").document(taskId)
        return try {
            db.runTransaction(
                TransactionOptions.Builder().setMaxAttempts(1).build()
            ) { transaction ->
                val snapShot = transaction.get(projectRef)
                transaction.update(projectRef, "tasks", FieldValue.arrayRemove(taskRef.id))
                transaction.delete(taskRef)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTask(taskId: String, task: Task): Boolean {
        val taskRef = db.collection("tasks").document(taskId)

        return try {
            db.runTransaction(TransactionOptions.Builder().setMaxAttempts(1).build()) {
                val snapShot = it.get(taskRef)

                it.update(taskRef, "taskName", task.taskName)
                it.update(taskRef, "tag", task.tag)
                it.update(taskRef, "status", task.status)
                it.update(taskRef, "repeat", task.repeat)
                it.update(taskRef, "recurringType", task.recurringType)
                it.update(taskRef, "projectName", task.projectName)
                it.update(taskRef, "priority", task.priority)
                it.update(taskRef, "listUser", task.listUser)
                it.update(taskRef, "endRepeat", task.endRepeat)
                it.update(taskRef, "endDate", task.endDate)
                it.update(taskRef, "description", task.description)
                it.update(taskRef, "creationDate", task.creationDate)
                it.update(taskRef, "comments", task.comments)
                it.update(taskRef, "attachments", task.attachments)

                if ((snapShot.get("taskName") as String).compareTo(task.taskName) != 0) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} changed the title to \"${task.taskName}\".",
                        "icon" to HistoryIcons.TITLE,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
                if ((snapShot.get("description") as String).compareTo(task.description) != 0) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} updated the description to \"${task.description}\".",
                        "icon" to HistoryIcons.DESCRIPTION,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
                if ((snapShot.get("tag") as String).compareTo(task.tag) != 0) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} changed the tag to \"${task.tag}\".",
                        "icon" to HistoryIcons.TAG,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
                if ((snapShot.get("status") as String) != task.status.toString()) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} changed the status to \"${task.status}\".",
                        "icon" to HistoryIcons.STATUS,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
                if ((snapShot.get("endDate") as Timestamp).asDate() != task.endDate.asDate()) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} changed the task end date to \"${task.endDate.asDate()}\".",
                        "icon" to HistoryIcons.ENDDATE,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
                if ((snapShot.get("listUser") as List<String>) != task.listUser) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} updated the task's collaborators.",
                        "icon" to HistoryIcons.COLLABORATORS,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
                if ((snapShot.get("priority") as String) != task.priority.toString()) {
                    val historyMap = mapOf(
                        "text" to "${profileViewModel.nameValue} ${profileViewModel.surnameValue} changed the task priority to \"${task.priority}\".",
                        "icon" to HistoryIcons.PRIORITY,
                        "user" to profileViewModel.userId,
                        "timestamp" to Timestamp.now()
                    )
                    val historyRef = db.collection("history").document()
                    it.set(historyRef, historyMap)
                    it.update(taskRef, "history", FieldValue.arrayUnion(historyRef.id))
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }

    }

    suspend fun addComment(taskId: String, comment: Comment): Boolean {

        val commentMap = mapOf(
            "author" to comment.author,
            "text" to comment.text,
            "timestamp" to comment.timestamp
        )

        val taskRef = db.collection("tasks").document(taskId)
        return try {
            db.runTransaction() { transaction ->
                val snapShot = transaction.get(taskRef)
                val commentRef = db.collection("comments").document()
                transaction.set(commentRef, commentMap)
                transaction.update(taskRef, "comments", FieldValue.arrayUnion(commentRef.id))
            }.await()
            true
        } catch (e: Exception) {
            false
        }

    }

}