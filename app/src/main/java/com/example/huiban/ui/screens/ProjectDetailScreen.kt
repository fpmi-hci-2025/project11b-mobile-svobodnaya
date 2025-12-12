package com.example.huiban.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.huiban.data.model.*
import com.example.huiban.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: ProjectDetailResponse?,
    tasks: List<TaskResponse>,
    currentUserId: Int,
    isLoading: Boolean,
    onBack: () -> Unit,
    onCreateTask: (String, String?, String, String, Int?) -> Unit,
    onUpdateTask: (Int, String, String?, String, String, Int?) -> Unit,
    onDeleteTask: (Int) -> Unit,
    searchUsers: List<UserBriefResponse>,
    onSearchUsers: (String) -> Unit,
    onAddMember: (Int) -> Unit,
    onRemoveMember: (Int) -> Unit
) {
    var viewMode by remember { mutableStateOf("board") }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskResponse?>(null) }
    var memberSearchQuery by remember { mutableStateOf("") }
    
    val isOwner = project?.ownerId == currentUserId
    
    // Task form state
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskStatus by remember { mutableStateOf("todo") }
    var taskComplexity by remember { mutableStateOf("medium") }
    var taskAssigneeId by remember { mutableStateOf<Int?>(null) }
    
    val statusOrder = listOf("todo", "in_progress", "review", "done")
    val statusLabels = mapOf(
        "todo" to "К выполнению",
        "in_progress" to "В работе",
        "review" to "На проверке",
        "done" to "Готово"
    )
    val complexityLabels = mapOf(
        "low" to "Низкая",
        "medium" to "Средняя",
        "high" to "Высокая",
        "critical" to "Критическая"
    )
    
    fun openCreateTask() {
        editingTask = null
        taskTitle = ""
        taskDescription = ""
        taskStatus = "todo"
        taskComplexity = "medium"
        taskAssigneeId = null
        showTaskDialog = true
    }
    
    fun openEditTask(task: TaskResponse) {
        editingTask = task
        taskTitle = task.title
        taskDescription = task.description ?: ""
        taskStatus = task.status
        taskComplexity = task.complexity
        taskAssigneeId = task.assigneeId
        showTaskDialog = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = project?.name ?: "Загрузка...",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (isOwner && project != null) {
                        IconButton(onClick = { showMembersDialog = true }) {
                            Icon(Icons.Default.Group, contentDescription = "Участники")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (project != null) {
                FloatingActionButton(
                    onClick = { openCreateTask() },
                    containerColor = Orange500,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать задачу")
                }
            }
        }
    ) { padding ->
        if (isLoading || project == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Orange500)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // View mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewMode == "list",
                        onClick = { viewMode = "list" },
                        label = { Text("Список") },
                        leadingIcon = {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange100,
                            selectedLabelColor = Orange500,
                            selectedLeadingIconColor = Orange500
                        )
                    )
                    FilterChip(
                        selected = viewMode == "board",
                        onClick = { viewMode = "board" },
                        label = { Text("Доска") },
                        leadingIcon = {
                            Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange100,
                            selectedLabelColor = Orange500,
                            selectedLeadingIconColor = Orange500
                        )
                    )
                }
                
                if (tasks.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет задач",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Создайте первую задачу",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (viewMode == "list") {
                    // List view
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            TaskListItem(
                                task = task,
                                onClick = { openEditTask(task) },
                                statusLabels = statusLabels,
                                complexityLabels = complexityLabels
                            )
                        }
                    }
                } else {
                    // Board view
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(statusOrder) { status ->
                            BoardColumn(
                                status = status,
                                statusLabel = statusLabels[status] ?: status,
                                tasks = tasks.filter { it.status == status },
                                onTaskClick = { openEditTask(it) },
                                complexityLabels = complexityLabels
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Task Dialog
    if (showTaskDialog && project != null) {
        AlertDialog(
            onDismissRequest = { showTaskDialog = false },
            title = {
                Text(
                    text = if (editingTask != null) "Редактировать задачу" else "Новая задача",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                    
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                    
                    // Status dropdown
                    var statusExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = statusLabels[taskStatus] ?: taskStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Статус") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Orange500,
                                focusedLabelColor = Orange500
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statusOrder.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(statusLabels[status] ?: status) },
                                    onClick = {
                                        taskStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Complexity dropdown
                    var complexityExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = complexityExpanded,
                        onExpandedChange = { complexityExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = complexityLabels[taskComplexity] ?: taskComplexity,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Сложность") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = complexityExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Orange500,
                                focusedLabelColor = Orange500
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = complexityExpanded,
                            onDismissRequest = { complexityExpanded = false }
                        ) {
                            complexityLabels.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        taskComplexity = key
                                        complexityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Assignee dropdown
                    var assigneeExpanded by remember { mutableStateOf(false) }
                    val assigneeOptions = buildList {
                        add(null to "Не назначен")
                        add(project.owner.id to "${project.owner.username} (владелец)")
                        project.members.forEach { member ->
                            add(member.user.id to member.user.username)
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = assigneeExpanded,
                        onExpandedChange = { assigneeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = assigneeOptions.find { it.first == taskAssigneeId }?.second ?: "Не назначен",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ответственный") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Orange500,
                                focusedLabelColor = Orange500
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = assigneeExpanded,
                            onDismissRequest = { assigneeExpanded = false }
                        ) {
                            assigneeOptions.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        taskAssigneeId = id
                                        assigneeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingTask != null) {
                            onUpdateTask(
                                editingTask!!.id,
                                taskTitle,
                                taskDescription.ifBlank { null },
                                taskStatus,
                                taskComplexity,
                                taskAssigneeId
                            )
                        } else {
                            onCreateTask(
                                taskTitle,
                                taskDescription.ifBlank { null },
                                taskStatus,
                                taskComplexity,
                                taskAssigneeId
                            )
                        }
                        showTaskDialog = false
                    },
                    enabled = taskTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                ) {
                    Text(if (editingTask != null) "Сохранить" else "Создать")
                }
            },
            dismissButton = {
                Row {
                    if (editingTask != null) {
                        TextButton(
                            onClick = {
                                onDeleteTask(editingTask!!.id)
                                showTaskDialog = false
                            }
                        ) {
                            Text("Удалить", color = Error)
                        }
                    }
                    TextButton(onClick = { showTaskDialog = false }) {
                        Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
    
    // Members Dialog
    if (showMembersDialog && project != null) {
        AlertDialog(
            onDismissRequest = { 
                showMembersDialog = false
                memberSearchQuery = ""
            },
            title = {
                Text("Участники проекта", fontWeight = FontWeight.SemiBold)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search field
                    OutlinedTextField(
                        value = memberSearchQuery,
                        onValueChange = { 
                            memberSearchQuery = it
                            if (it.length >= 2) {
                                onSearchUsers(it)
                            }
                        },
                        label = { Text("Добавить участника") },
                        placeholder = { Text("Введите имя (мин. 2 символа)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                    
                    // Search results
                    if (memberSearchQuery.length >= 2 && searchUsers.isNotEmpty()) {
                        val existingIds = listOf(project.ownerId) + project.members.map { it.user.id }
                        val filteredUsers = searchUsers.filter { it.id !in existingIds }
                        
                        if (filteredUsers.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column {
                                    filteredUsers.forEach { user ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onAddMember(user.id)
                                                    memberSearchQuery = ""
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            brush = Brush.linearGradient(
                                                                colors = listOf(Orange500, Accent)
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = user.username.first().uppercase(),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(user.username)
                                            }
                                            Text(
                                                "+ Добавить",
                                                color = Orange500,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Текущие участники",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Owner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
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
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(project.owner.username)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Orange100
                            ) {
                                Text(
                                    "Владелец",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Orange500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    // Members
                    project.members.forEach { member ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Orange500, Accent)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.user.username.first().uppercase(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(member.user.username)
                                }
                                IconButton(
                                    onClick = { onRemoveMember(member.user.id) }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showMembersDialog = false 
                        memberSearchQuery = ""
                    }
                ) {
                    Text("Закрыть", color = Orange500)
                }
            }
        )
    }
}

@Composable
private fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()

@Composable
fun TaskListItem(
    task: TaskResponse,
    onClick: () -> Unit,
    statusLabels: Map<String, String>,
    complexityLabels: Map<String, String>
) {
    val statusColor = when (task.status) {
        "todo" -> StatusTodo
        "in_progress" -> StatusProgress
        "review" -> StatusReview
        "done" -> StatusDone
        else -> StatusTodo
    }
    
    val complexityColor = when (task.complexity) {
        "low" -> ComplexityLow
        "medium" -> ComplexityMedium
        "high" -> ComplexityHigh
        "critical" -> ComplexityCritical
        else -> ComplexityMedium
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (!task.description.isNullOrBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = complexityColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = complexityLabels[task.complexity] ?: task.complexity,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = complexityColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (task.assignee != null) {
                Spacer(modifier = Modifier.width(8.dp))
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
                        text = task.assignee.username.first().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BoardColumn(
    status: String,
    statusLabel: String,
    tasks: List<TaskResponse>,
    onTaskClick: (TaskResponse) -> Unit,
    complexityLabels: Map<String, String>
) {
    val statusColor = when (status) {
        "todo" -> StatusTodo
        "in_progress" -> StatusProgress
        "review" -> StatusReview
        "done" -> StatusDone
        else -> StatusTodo
    }
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = tasks.size.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    BoardTaskCard(
                        task = task,
                        onClick = { onTaskClick(task) },
                        complexityLabels = complexityLabels
                    )
                }
            }
        }
    }
}

@Composable
fun BoardTaskCard(
    task: TaskResponse,
    onClick: () -> Unit,
    complexityLabels: Map<String, String>
) {
    val complexityColor = when (task.complexity) {
        "low" -> ComplexityLow
        "medium" -> ComplexityMedium
        "high" -> ComplexityHigh
        "critical" -> ComplexityCritical
        else -> ComplexityMedium
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = complexityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = complexityLabels[task.complexity] ?: task.complexity,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = complexityColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (task.assignee != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Orange500, Accent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = task.assignee.username.first().uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

