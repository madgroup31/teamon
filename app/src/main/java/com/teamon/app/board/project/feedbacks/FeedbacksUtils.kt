package com.teamon.app.board.project.feedbacks

sealed class FeedbackType(val id: String) {
    class PersonalFeedback(id: String) : FeedbackType(id)
    class TeamFeedback(id: String) : FeedbackType(id)
    class ProjectFeedback(id: String) : FeedbackType(id)

}