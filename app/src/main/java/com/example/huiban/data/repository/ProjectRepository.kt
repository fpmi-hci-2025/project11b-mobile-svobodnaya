package com.example.huiban.data.repository

import com.example.huiban.data.api.RetrofitClient
import com.example.huiban.data.model.*

class ProjectRepository {
    private val api = RetrofitClient.apiService
    
    suspend fun getProjects(): Result<List<ProjectResponse>> {
        return try {
            val response = api.getProjects()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load projects"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProject(id: Int): Result<ProjectDetailResponse> {
        return try {
            val response = api.getProject(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load project"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createProject(name: String, description: String?): Result<ProjectDetailResponse> {
        return try {
            val request = ProjectRequest(name, description)
            val response = api.createProject(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create project"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProject(id: Int, name: String, description: String?): Result<ProjectDetailResponse> {
        return try {
            val request = ProjectRequest(name, description)
            val response = api.updateProject(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update project"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProject(id: Int): Result<Unit> {
        return try {
            val response = api.deleteProject(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete project"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addMember(projectId: Int, userId: Int): Result<MemberResponse> {
        return try {
            val request = AddMemberRequest(userId)
            val response = api.addMember(projectId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeMember(projectId: Int, userId: Int): Result<Unit> {
        return try {
            val response = api.removeMember(projectId, userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchUsers(query: String): Result<List<UserBriefResponse>> {
        return try {
            val response = api.searchUsers(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

