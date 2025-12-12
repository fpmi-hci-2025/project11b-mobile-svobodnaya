package com.example.huiban.data.repository

import com.example.huiban.data.api.RetrofitClient
import com.example.huiban.data.model.*

class TaskRepository {
    private val api = RetrofitClient.apiService
    
    suspend fun getTasks(projectId: Int): Result<List<TaskResponse>> {
        return try {
            val response = api.getTasks(projectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load tasks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createTask(
        projectId: Int,
        title: String,
        description: String?,
        status: String,
        complexity: String,
        assigneeId: Int?
    ): Result<TaskResponse> {
        return try {
            val request = TaskRequest(title, description, status, complexity, assigneeId)
            val response = api.createTask(projectId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create task"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTask(
        projectId: Int,
        taskId: Int,
        title: String,
        description: String?,
        status: String,
        complexity: String,
        assigneeId: Int?
    ): Result<TaskResponse> {
        return try {
            val request = TaskRequest(title, description, status, complexity, assigneeId)
            val response = api.updateTask(projectId, taskId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update task"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTask(projectId: Int, taskId: Int): Result<Unit> {
        return try {
            val response = api.deleteTask(projectId, taskId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete task"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

