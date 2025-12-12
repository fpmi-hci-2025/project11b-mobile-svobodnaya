package com.example.huiban.data.model

import com.google.gson.annotations.SerializedName

// Auth
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    @SerializedName("created_at") val createdAt: String
)

data class UserBriefResponse(
    val id: Int,
    val username: String
)

// Projects
data class ProjectRequest(
    val name: String,
    val description: String?
)

data class ProjectResponse(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("owner_id") val ownerId: Int,
    val owner: UserBriefResponse,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class ProjectDetailResponse(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("owner_id") val ownerId: Int,
    val owner: UserBriefResponse,
    val members: List<MemberResponse>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class MemberResponse(
    val id: Int,
    val user: UserBriefResponse,
    @SerializedName("joined_at") val joinedAt: String
)

data class AddMemberRequest(
    @SerializedName("user_id") val userId: Int
)

// Tasks
enum class TaskStatus {
    @SerializedName("todo") TODO,
    @SerializedName("in_progress") IN_PROGRESS,
    @SerializedName("review") REVIEW,
    @SerializedName("done") DONE
}

enum class TaskComplexity {
    @SerializedName("low") LOW,
    @SerializedName("medium") MEDIUM,
    @SerializedName("high") HIGH,
    @SerializedName("critical") CRITICAL
}

data class TaskRequest(
    val title: String,
    val description: String?,
    val status: String = "todo",
    val complexity: String = "medium",
    @SerializedName("assignee_id") val assigneeId: Int?
)

data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String,
    val complexity: String,
    @SerializedName("project_id") val projectId: Int,
    @SerializedName("creator_id") val creatorId: Int,
    val creator: UserBriefResponse,
    @SerializedName("assignee_id") val assigneeId: Int?,
    val assignee: UserBriefResponse?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

