package com.example.huiban

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.huiban.data.model.*
import com.example.huiban.data.repository.AuthRepository
import com.example.huiban.data.repository.ProjectRepository
import com.example.huiban.data.repository.TaskRepository
import com.example.huiban.ui.screens.*
import com.example.huiban.ui.theme.HuibanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository
    private val projectRepository = ProjectRepository()
    private val taskRepository = TaskRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authRepository = AuthRepository(this)
        enableEdgeToEdge()
        
        setContent {
            HuibanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskFlowApp(
                        authRepository = authRepository,
                        projectRepository = projectRepository,
                        taskRepository = taskRepository,
                        lifecycleScope = lifecycleScope
                    )
                }
            }
        }
    }
}

@Composable
fun TaskFlowApp(
    authRepository: AuthRepository,
    projectRepository: ProjectRepository,
    taskRepository: TaskRepository,
    lifecycleScope: kotlinx.coroutines.CoroutineScope
) {
    var currentScreen by remember { mutableStateOf(if (authRepository.isLoggedIn()) "projects" else "login") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Projects state
    var projects by remember { mutableStateOf<List<ProjectResponse>>(emptyList()) }
    var currentProject by remember { mutableStateOf<ProjectDetailResponse?>(null) }
    var tasks by remember { mutableStateOf<List<TaskResponse>>(emptyList()) }
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }
    var searchUsers by remember { mutableStateOf<List<UserBriefResponse>>(emptyList()) }
    
    val currentUser = authRepository.getCurrentUser()
    
    // Load projects when on projects screen
    LaunchedEffect(currentScreen) {
        if (currentScreen == "projects") {
            isLoading = true
            projectRepository.getProjects().fold(
                onSuccess = { projects = it },
                onFailure = { error = it.message }
            )
            isLoading = false
        }
    }
    
    // Load project details when selected
    LaunchedEffect(selectedProjectId) {
        selectedProjectId?.let { id ->
            isLoading = true
            projectRepository.getProject(id).fold(
                onSuccess = { currentProject = it },
                onFailure = { error = it.message }
            )
            taskRepository.getTasks(id).fold(
                onSuccess = { tasks = it },
                onFailure = { error = it.message }
            )
            isLoading = false
        }
    }
    
    when (currentScreen) {
        "login" -> LoginScreen(
            onLogin = { username, password ->
                lifecycleScope.launch {
                    isLoading = true
                    error = null
                    authRepository.login(username, password).fold(
                        onSuccess = {
                            currentScreen = "projects"
                        },
                        onFailure = {
                            error = "Неверные имя пользователя или пароль"
                        }
                    )
                    isLoading = false
                }
            },
            onNavigateToRegister = {
                error = null
                currentScreen = "register"
            },
            isLoading = isLoading,
            error = error
        )
        
        "register" -> RegisterScreen(
            onRegister = { username, password ->
                lifecycleScope.launch {
                    isLoading = true
                    error = null
                    authRepository.register(username, password).fold(
                        onSuccess = {
                            currentScreen = "projects"
                        },
                        onFailure = {
                            error = "Ошибка регистрации. Возможно, пользователь уже существует."
                        }
                    )
                    isLoading = false
                }
            },
            onNavigateToLogin = {
                error = null
                currentScreen = "login"
            },
            isLoading = isLoading,
            error = error
        )
        
        "projects" -> ProjectsScreen(
            projects = projects,
            currentUserId = currentUser?.id ?: 0,
            isLoading = isLoading,
            onProjectClick = { id ->
                selectedProjectId = id
                currentScreen = "project_detail"
            },
            onCreateProject = { name, description ->
                lifecycleScope.launch {
                    projectRepository.createProject(name, description).fold(
                        onSuccess = { newProject ->
                            selectedProjectId = newProject.id
                            currentScreen = "project_detail"
                        },
                        onFailure = { error = it.message }
                    )
                }
            },
            onLogout = {
                authRepository.logout()
                projects = emptyList()
                currentProject = null
                tasks = emptyList()
                selectedProjectId = null
                currentScreen = "login"
            }
        )
        
        "project_detail" -> ProjectDetailScreen(
            project = currentProject,
            tasks = tasks,
            currentUserId = currentUser?.id ?: 0,
            isLoading = isLoading,
            onBack = {
                currentProject = null
                tasks = emptyList()
                selectedProjectId = null
                searchUsers = emptyList()
                currentScreen = "projects"
                // Refresh projects
                lifecycleScope.launch {
                    projectRepository.getProjects().fold(
                        onSuccess = { projects = it },
                        onFailure = { }
                    )
                }
            },
            onCreateTask = { title, description, status, complexity, assigneeId ->
                lifecycleScope.launch {
                    selectedProjectId?.let { projectId ->
                        taskRepository.createTask(projectId, title, description, status, complexity, assigneeId).fold(
                            onSuccess = { newTask ->
                                tasks = listOf(newTask) + tasks
                            },
                            onFailure = { error = it.message }
                        )
                    }
                }
            },
            onUpdateTask = { taskId, title, description, status, complexity, assigneeId ->
                lifecycleScope.launch {
                    selectedProjectId?.let { projectId ->
                        taskRepository.updateTask(projectId, taskId, title, description, status, complexity, assigneeId).fold(
                            onSuccess = {
                                projectRepository.getProject(projectId).fold(
                                    onSuccess = { currentProject = it },
                                    onFailure = { }
                                )
                                taskRepository.getTasks(projectId).fold(
                                    onSuccess = { tasks = it },
                                    onFailure = { }
                                )
                            },
                            onFailure = { error = it.message }
                        )
                    }
                }
            },
            onDeleteTask = { taskId ->
                lifecycleScope.launch {
                    selectedProjectId?.let { projectId ->
                        taskRepository.deleteTask(projectId, taskId).fold(
                            onSuccess = {
                                tasks = tasks.filter { it.id != taskId }
                            },
                            onFailure = { error = it.message }
                        )
                    }
                }
            },
            searchUsers = searchUsers,
            onSearchUsers = { query ->
                lifecycleScope.launch {
                    projectRepository.searchUsers(query).fold(
                        onSuccess = { searchUsers = it },
                        onFailure = { searchUsers = emptyList() }
                    )
                }
            },
            onAddMember = { userId ->
                lifecycleScope.launch {
                    selectedProjectId?.let { projectId ->
                        projectRepository.addMember(projectId, userId).fold(
                            onSuccess = {
                                // Refresh project to get updated members
                                projectRepository.getProject(projectId).fold(
                                    onSuccess = { currentProject = it },
                                    onFailure = { }
                                )
                                searchUsers = emptyList()
                            },
                            onFailure = { error = it.message }
                        )
                    }
                }
            },
            onRemoveMember = { userId ->
                lifecycleScope.launch {
                    selectedProjectId?.let { projectId ->
                        projectRepository.removeMember(projectId, userId).fold(
                            onSuccess = {
                                // Refresh project and tasks
                                projectRepository.getProject(projectId).fold(
                                    onSuccess = { currentProject = it },
                                    onFailure = { }
                                )
                                taskRepository.getTasks(projectId).fold(
                                    onSuccess = { tasks = it },
                                    onFailure = { }
                                )
                            },
                            onFailure = { error = it.message }
                        )
                    }
                }
            }
        )
    }
}
