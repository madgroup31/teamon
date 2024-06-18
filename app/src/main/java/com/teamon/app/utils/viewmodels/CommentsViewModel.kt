package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import com.teamon.app.Model
import com.teamon.app.utils.classes.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class CommentsViewModel(val model: Model): ViewModel() {

    fun getTaskComments(taskId: String): Flow<Map<String, Comment>> = channelFlow {
        val comments = mutableMapOf<String, Comment>()

        model.getTask(taskId).collect { task ->
            comments.clear()
            task.comments.forEach {
                launch {
                    model.getComment(it).collect { comment ->
                        comments[comment.commentId] = comment
                        send(comments)
                    }
                }
            }
        }
    }
}