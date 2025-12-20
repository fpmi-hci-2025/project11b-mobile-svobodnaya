package com.example.huiban.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.huiban.data.model.ProjectResponse
import com.example.huiban.ui.theme.Orange500
import com.example.huiban.ui.theme.Accent
import com.example.huiban.ui.theme.Orange100
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class ProjectSortType(val displayName: String) {
    NAME_ASC("По имени (А-Я)"),
    NAME_DESC("По имени (Я-А)"),
    DATE_CREATED_ASC("По дате создания (старые)"),
    DATE_CREATED_DESC("По дате создания (новые)"),
    DATE_UPDATED_ASC("По дате обновления (старые)"),
    DATE_UPDATED_DESC("По дате обновления (новые)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projects: List<ProjectResponse>,
    currentUserId: Int,
    isLoading: Boolean,
    onProjectClick: (Int) -> Unit,
    onCreateProject: (String, String?) -> Unit,
    onLogout: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDescription by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(ProjectSortType.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Sort projects based on selected sort type
    val sortedProjects = remember(projects, sortType) {
        when (sortType) {
            ProjectSortType.NAME_ASC -> projects.sortedBy { it.name.lowercase() }
            ProjectSortType.NAME_DESC -> projects.sortedByDescending { it.name.lowercase() }
            ProjectSortType.DATE_CREATED_ASC -> projects.sortedBy { parseDateTime(it.createdAt) }
            ProjectSortType.DATE_CREATED_DESC -> projects.sortedByDescending { parseDateTime(it.createdAt) }
            ProjectSortType.DATE_UPDATED_ASC -> projects.sortedBy { parseDateTime(it.updatedAt) }
            ProjectSortType.DATE_UPDATED_DESC -> projects.sortedByDescending { parseDateTime(it.updatedAt) }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Мои проекты",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Сортировка")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            Text(
                                text = "Сортировка",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider()
                            ProjectSortType.values().forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text(sort.displayName) },
                                    onClick = {
                                        sortType = sort
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortType == sort) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Orange500
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Orange500,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать проект")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Orange500
                )
            } else if (projects.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет проектов",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Создайте свой первый проект",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                    ) {
                        Text("Создать проект")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedProjects) { project ->
                        ProjectCard(
                            project = project,
                            isOwner = project.ownerId == currentUserId,
                            onClick = { onProjectClick(project.id) }
                        )
                    }
                }
            }
        }
    }
    
    // Create Project Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newProjectName = ""
                newProjectDescription = ""
            },
            title = { Text("Новый проект", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newProjectDescription,
                        onValueChange = { newProjectDescription = it },
                        label = { Text("Описание (опционально)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateProject(
                            newProjectName,
                            newProjectDescription.ifBlank { null }
                        )
                        showCreateDialog = false
                        newProjectName = ""
                        newProjectDescription = ""
                    },
                    enabled = newProjectName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newProjectName = ""
                    newProjectDescription = ""
                }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

private fun parseDateTime(dateTimeString: String): LocalDateTime {
    return try {
        // Try ISO format first (e.g., "2024-01-15T10:30:00")
        LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        try {
            // Try ISO offset format (e.g., "2024-01-15T10:30:00+03:00")
            val offsetDateTime = java.time.OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            offsetDateTime.toLocalDateTime()
        } catch (e2: Exception) {
            try {
                // Try ISO instant format (e.g., "2024-01-15T10:30:00Z")
                val instant = java.time.Instant.parse(dateTimeString)
                LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            } catch (e3: Exception) {
                try {
                    // Try format with space replaced by T
                    LocalDateTime.parse(dateTimeString.replace(" ", "T"))
                } catch (e4: Exception) {
                    // If all parsing fails, return minimum date for sorting
                    LocalDateTime.MIN
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: ProjectResponse,
    isOwner: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Project icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Orange100,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = Orange500,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                if (isOwner) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Orange100
                    ) {
                        Text(
                            text = "Владелец",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Orange500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = project.description ?: "Нет описания",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Orange500, Accent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.owner.username.first().uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = project.owner.username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

