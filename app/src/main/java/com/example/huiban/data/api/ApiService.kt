package com.example.huiban.data.api

import com.example.huiban.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>
    
    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>
    
    // Projects
    @GET("api/projects/")
    suspend fun getProjects(): Response<List<ProjectResponse>>
    
    @GET("api/projects/{id}")
    suspend fun getProject(@Path("id") id: Int): Response<ProjectDetailResponse>
    
    @POST("api/projects/")
    suspend fun createProject(@Body request: ProjectRequest): Response<ProjectDetailResponse>
    
    @PUT("api/projects/{id}")
    suspend fun updateProject(@Path("id") id: Int, @Body request: ProjectRequest): Response<ProjectDetailResponse>
    
    @DELETE("api/projects/{id}")
    suspend fun deleteProject(@Path("id") id: Int): Response<Unit>
    
    @POST("api/projects/{id}/members")
    suspend fun addMember(@Path("id") projectId: Int, @Body request: AddMemberRequest): Response<MemberResponse>
    
    @DELETE("api/projects/{projectId}/members/{userId}")
    suspend fun removeMember(@Path("projectId") projectId: Int, @Path("userId") userId: Int): Response<Unit>
    
    // Tasks
    @GET("api/projects/{projectId}/tasks/")
    suspend fun getTasks(@Path("projectId") projectId: Int): Response<List<TaskResponse>>
    
    @POST("api/projects/{projectId}/tasks/")
    suspend fun createTask(@Path("projectId") projectId: Int, @Body request: TaskRequest): Response<TaskResponse>
    
    @PUT("api/projects/{projectId}/tasks/{taskId}")
    suspend fun updateTask(
        @Path("projectId") projectId: Int,
        @Path("taskId") taskId: Int,
        @Body request: TaskRequest
    ): Response<TaskResponse>
    
    @DELETE("api/projects/{projectId}/tasks/{taskId}")
    suspend fun deleteTask(@Path("projectId") projectId: Int, @Path("taskId") taskId: Int): Response<Unit>
    
    // Users
    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<UserBriefResponse>>
}

