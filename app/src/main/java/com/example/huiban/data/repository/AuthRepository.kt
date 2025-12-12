package com.example.huiban.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.huiban.data.api.RetrofitClient
import com.example.huiban.data.model.RegisterRequest
import com.example.huiban.data.model.UserResponse

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("taskflow_prefs", Context.MODE_PRIVATE)
    private val api = RetrofitClient.apiService
    
    init {
        // Restore token from prefs
        prefs.getString("token", null)?.let {
            RetrofitClient.setToken(it)
        }
    }
    
    suspend fun login(username: String, password: String): Result<UserResponse> {
        return try {
            val response = api.login(username, password)
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.accessToken
                saveToken(token)
                
                // Get user info
                val userResponse = api.getMe()
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    saveUser(userResponse.body()!!)
                    Result.success(userResponse.body()!!)
                } else {
                    Result.failure(Exception("Failed to get user info"))
                }
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, password: String): Result<UserResponse> {
        return try {
            val request = RegisterRequest(username, password)
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                // Auto login after registration
                login(username, password)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getString("token", null) != null
    }
    
    fun getCurrentUser(): UserResponse? {
        val id = prefs.getInt("user_id", -1)
        val username = prefs.getString("user_username", null)
        val createdAt = prefs.getString("user_created_at", null)
        
        return if (id != -1 && username != null && createdAt != null) {
            UserResponse(id, username, createdAt)
        } else null
    }
    
    fun logout() {
        prefs.edit().clear().apply()
        RetrofitClient.setToken(null)
    }
    
    private fun saveToken(token: String) {
        RetrofitClient.setToken(token)
        prefs.edit().putString("token", token).apply()
    }
    
    private fun saveUser(user: UserResponse) {
        prefs.edit().apply {
            putInt("user_id", user.id)
            putString("user_username", user.username)
            putString("user_created_at", user.createdAt)
            apply()
        }
    }
}

