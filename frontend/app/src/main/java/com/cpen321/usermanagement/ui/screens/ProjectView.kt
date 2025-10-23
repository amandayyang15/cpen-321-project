package com.cpen321.usermanagement.ui.screens
import retrofit2.http.GET
import Icon
import com.cpen321.usermanagement.data.remote.dto.Task
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.Expense
import com.cpen321.usermanagement.data.remote.dto.Project
import com.cpen321.usermanagement.data.remote.dto.ProjectMember
import com.cpen321.usermanagement.data.remote.dto.Resource
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.ProjectViewModel
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

import com.cpen321.usermanagement.ui.theme.LocalSpacing

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectView(
    navigationStateManager: NavigationStateManager,
    projectViewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    ProjectContent(
        navigationStateManager = navigationStateManager,
        projectViewModel = projectViewModel,
        modifier = modifier
    )
}

@Composable
private fun ProjectContent(
    navigationStateManager: NavigationStateManager,
    projectViewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by projectViewModel.uiState.collectAsState()

    // Auto-select first project if none is selected
    LaunchedEffect(uiState.projects, uiState.selectedProject) {
        if (uiState.projects.isNotEmpty() && uiState.selectedProject == null) {
            Log.d("ProjectView", "Auto-selecting first project: ${uiState.projects.first().name}")
            projectViewModel.selectProject(uiState.projects.first())
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ProjectTopBar(
                onBackClick = { navigationStateManager.navigateBack() },
                onProfileClick = { navigationStateManager.navigateToProfile() }
            )
        }
    ) { paddingValues ->
        ProjectBody(
            paddingValues = paddingValues,
            projectViewModel = projectViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectTopBar(
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            AppTitle()
        },
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        actions = {
            ProfileActionButton(onClick = onProfileClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AppTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun ProfileActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ProfileIcon()
    }
}

@Composable
private fun ProfileIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
    )
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        BackIcon()
    }
}

@Composable
private fun BackIcon() {
    Icon(
        name = R.drawable.ic_arrow_back,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectBody(
    paddingValues: PaddingValues,
    projectViewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val uiState by projectViewModel.uiState.collectAsState()
    val currentProject = uiState.selectedProject

    var progressExpanded by remember { mutableStateOf(false) }
    var selectedProgress by remember { mutableStateOf("In Progress") }
    var selectedTab by remember { mutableStateOf("Task") }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var taskProgress by remember { mutableStateOf("In Progress") }
    var deadline by remember { mutableStateOf("") }
    var taskProgressExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    // Resource state
    var showAddResourceDialog by remember { mutableStateOf(false) }
    var resourceName by remember { mutableStateOf("") }
    var resourceLink by remember { mutableStateOf("") }

    // Project Settings state
    var projectName by remember { mutableStateOf("") }
    var selectedUserToRemove by remember { mutableStateOf("") }
    var removeUserExpanded by remember { mutableStateOf(false) }

    // Expense state
    var showCreateExpenseDialog by remember { mutableStateOf(false) }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var paidByExpanded by remember { mutableStateOf(false) }
    var selectedUsersForSplit by remember { mutableStateOf(setOf<String>()) }

    // Get actual project members
    val projectMembers = currentProject?.members ?: emptyList()
    val ownerMember = currentProject?.let {
        ProjectMember(userId = it.ownerId, role = "owner", joinedAt = it.createdAt)
    }
    // Only add owner if they're not already in projectMembers (avoid duplicates)
    val allMembers = if (ownerMember != null && !projectMembers.any { it.userId == ownerMember.userId }) {
        listOf(ownerMember) + projectMembers
    } else {
        projectMembers
    }

    // Get expenses from ViewModel
    val expenses = uiState.expenses

    // Load expenses when switching to Expense tab or when project changes
    LaunchedEffect(selectedTab, currentProject?.id) {
        if (selectedTab == "Expense" && currentProject != null) {
            Log.d("ProjectView", "Loading expenses for project: ${currentProject.id}")
            projectViewModel.loadExpenses(currentProject.id)
        }
    }

    // Note: Tasks are now project-specific, so no need to clear when switching projects

    // Load tasks when switching to Task Board tab or when project changes
    LaunchedEffect(selectedTab, currentProject?.id) {
        if (selectedTab == "Task" && currentProject != null) {
            Log.d("ProjectView", "Loading tasks for project: ${currentProject.id}")
            projectViewModel.loadProjectTasks(currentProject.id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = spacing.small, vertical = spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Button(
                onClick = {
                    Log.d("ProjectView", "Clicked Project Settings")
                    selectedTab = "Project Settings"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Project Settings")
            }

            Button(
                onClick = {
                    Log.d("ProjectView", "Clicked Create Task")
                    showCreateTaskDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Task")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Button(
                onClick = {
                    Log.d("ProjectView", "Clicked Resource")
                    selectedTab = "Resource"
                    // Refresh project data to get latest resources
                    projectViewModel.refreshSelectedProject()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Resource")
            }

            Button(
                onClick = {
                    Log.d("ProjectView", "Clicked Chat")
                    selectedTab = "Chat"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Chat")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Button(
                onClick = {
                    Log.d("ProjectView", "Clicked Expense")
                    selectedTab = "Expense"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Expense")
            }

            Button(
                onClick = {
                    Log.d("ProjectView", "Clicked Task Board")
                    selectedTab = "Task"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Task Board")
            }
        }

        // Add Expense Button (only show when on Expense tab)
        if (selectedTab == "Expense") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        Log.d("ProjectView", "Clicked Add Expense")
                        showCreateExpenseDialog = true
                    }
                ) {
                    Text("Add Expense")
                }
            }
        }

        // Add Resource Button (only show when on Resource tab)
        if (selectedTab == "Resource") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        Log.d("ProjectView", "Clicked Add Resource")
                        showAddResourceDialog = true
                    }
                ) {
                    Text("Add Resource")
                }
            }
        }

        // Get tasks for current project and filter to ensure only current project tasks are shown
        val allTasks = currentProject?.let { projectViewModel.getTasksForProject(it.id) } ?: emptyList()
        val tasks = allTasks.filter { task ->
            task.projectId == currentProject?.id
        }

        if (selectedTab == "Task") {
            Column {
                Text("Task Board", style = MaterialTheme.typography.titleLarge)

                // Show error message if present
                if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Show success message if present
                if (uiState.message != null && uiState.message!!.contains("Task")) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = uiState.message!!,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (tasks.isEmpty()) {
                    Text("No tasks yet.")
                } else {
                    tasks.forEach { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Name: ${task.title}", fontWeight = FontWeight.Bold)
                                Text("Status: ${task.status}")
                                Text("Assignees: ${task.assignees.joinToString()}")
                                Text("Deadline: ${task.deadline ?: "None"}")
                            }
                        }
                    }
                }
            }
        }

        // Content area below the buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.large)
        ) {
            when (selectedTab) {
                "Task" -> {
                    Text(
                        text = "Tasks:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = spacing.medium)
                    )

                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.small, vertical = spacing.medium)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(spacing.medium),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Task Name",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Assignee",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Deadline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Table row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.small, vertical = spacing.medium)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(spacing.medium),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Task1",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Justin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small)
                        ) {
                    val backgroundColor = when (selectedProgress) {
                        "In Progress" -> Color(0xFFFFEB3B) // Yellow
                        "Done" -> Color(0xFF4CAF50) // Green
                        "Backlog" -> Color(0xFFFF9800) // Orange
                        "Blocked" -> Color(0xFFF44336) // Red
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val textColor = when (selectedProgress) {
                        "In Progress" -> Color.Black
                        "Done" -> Color.White
                        "Backlog" -> Color.White
                        "Blocked" -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                            TextButton(
                                onClick = { progressExpanded = !progressExpanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = backgroundColor,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                            Text(
                                text = selectedProgress,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                ),
                                textAlign = TextAlign.Center,
                                color = textColor
                            )
                            }
                            DropdownMenu(
                                expanded = progressExpanded,
                                onDismissRequest = { progressExpanded = false }
                            ) {
                                listOf("In Progress", "Done", "Backlog", "Blocked").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedProgress = option
                                            progressExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Oct 22nd",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.small),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                "Project Settings" -> {
                    Text(
                        text = "Project Settings:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = spacing.medium)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.small),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        // Add Users card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(spacing.medium)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Add Users",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "(CODE)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Delete Project card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(spacing.medium)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delete Project",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = { Log.d("ProjectView", "Delete Project clicked") },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }

                        // Rename Project card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(spacing.medium)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                Text(
                                    text = "Rename Project",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                                ) {
                                    OutlinedTextField(
                                        value = projectName,
                                        onValueChange = { projectName = it },
                                        placeholder = { Text("New name") },
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            Log.d("ProjectView", "Rename project to: $projectName")
                                            Toast.makeText(context, "Project renamed to: $projectName", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                        }

                        // Remove Users card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(spacing.medium)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                Text(
                                    text = "Remove Users",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        TextButton(
                                            onClick = { removeUserExpanded = !removeUserExpanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = selectedUserToRemove.ifEmpty { "Select user" },
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = removeUserExpanded,
                                            onDismissRequest = { removeUserExpanded = false }
                                        ) {
                                            listOf("Justin", "Alice", "Bob", "Charlie").forEach { user ->
                                                DropdownMenuItem(
                                                    text = { Text(user) },
                                                    onClick = {
                                                        selectedUserToRemove = user
                                                        removeUserExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            Log.d("ProjectView", "Remove user: $selectedUserToRemove")
                                            Toast.makeText(context, "Removed user: $selectedUserToRemove", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
                "Resource" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resources:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = spacing.medium)
                        )

                        // Show refresh button
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    Log.d("ProjectView", "Manual refresh clicked")
                                    projectViewModel.refreshSelectedProject()
                                }
                            ) {
                                Text("Refresh")
                            }
                        }
                    }

                    // Show success message if available
                    if (uiState.message?.contains("Resource added") == true) {
                        Text(
                            text = uiState.message!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = spacing.small)
                        )
                    }

                    if (currentProject?.resources?.isNotEmpty() == true) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            currentProject.resources.forEach { resource ->
                                ResourceItem(
                                    resource = resource,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No resources yet. Add your first resource!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                "Chat" -> {
                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                "Expense" -> {
                    Text(
                        text = "Expenses:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = spacing.medium)
                    )

                    if (expenses.isEmpty()) {
                        Text(
                            text = "No expenses added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(spacing.large)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(spacing.medium)
                        ) {
                            expenses.forEach { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    currentProject = currentProject,
                                    projectViewModel = projectViewModel,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Create Task Dialog
        if (showCreateTaskDialog) {
            AlertDialog(
                onDismissRequest = { showCreateTaskDialog = false },
                title = {
                    Text("Create New Task")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        // Task Name Field
                        OutlinedTextField(
                            value = taskName,
                            onValueChange = { taskName = it },
                            label = { Text("Task Name") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter task name") }
                        )

                        // Assignee Dropdown - Project Members
                        Text(
                            text = "Assignee",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box {
                            OutlinedTextField(
                                value = assignee,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Select Assignee") },
                                placeholder = { Text("Choose a project member") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { assigneeExpanded = !assigneeExpanded }
                                    ) {
                                        Icon(
                                            imageVector = if (assigneeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { assigneeExpanded = !assigneeExpanded }
                            )
                            DropdownMenu(
                                expanded = assigneeExpanded,
                                onDismissRequest = { assigneeExpanded = false }
                            ) {
                                allMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = member.userId,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = member.role.replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            assignee = member.userId
                                            assigneeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Dropdown - Clear UI
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box {
                            OutlinedTextField(
                                value = taskProgress,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Select Status") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { taskProgressExpanded = !taskProgressExpanded }
                                    ) {
                                        Icon(
                                            imageVector = if (taskProgressExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { taskProgressExpanded = !taskProgressExpanded }
                            )
                            DropdownMenu(
                                expanded = taskProgressExpanded,
                                onDismissRequest = { taskProgressExpanded = false }
                            ) {
                                listOf("In Progress", "Done", "Backlog", "Blocked", "Not Started").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            taskProgress = option
                                            taskProgressExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Date Picker for Deadline
                        Text(
                            text = "Deadline",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = if (selectedDate != null) {
                                val date = java.util.Date(selectedDate!!)
                                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                formatter.format(date)
                            } else "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Select Date") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // Show date picker
                                        val datePickerDialog = android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val calendar = java.util.Calendar.getInstance()
                                                calendar.set(year, month, dayOfMonth)
                                                selectedDate = calendar.timeInMillis
                                                deadline = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                                            },
                                            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                                            java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                                            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
                                        )
                                        datePickerDialog.show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = "Date Picker"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Show date picker
                                    val datePickerDialog = android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val calendar = java.util.Calendar.getInstance()
                                            calendar.set(year, month, dayOfMonth)
                                            selectedDate = calendar.timeInMillis
                                            deadline = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                                        },
                                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                                        java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                                        java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
                                    )
                                    datePickerDialog.show()
                                }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            Log.d("ProjectView", "Creating task: $taskName, $assignee, $taskProgress, $deadline")
                            currentProject?.let { project ->
                                projectViewModel.createTask(
                                    projectId = project.id,
                                    name = taskName,
                                    assignee = assignee,
                                    status = taskProgress,
                                    deadline = deadline.takeIf { it.isNotBlank() }
                                )
                            }
                            showCreateTaskDialog = false
                            taskName = ""
                            assignee = ""
                            taskProgress = "In Progress"
                            deadline = ""
                            selectedDate = null
                            assigneeExpanded = false
                            taskProgressExpanded = false
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCreateTaskDialog = false
                            taskName = ""
                            assignee = ""
                            taskProgress = "In Progress"
                            deadline = ""
                            selectedDate = null
                            assigneeExpanded = false
                            taskProgressExpanded = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Create Expense Dialog
        if (showCreateExpenseDialog) {
            AlertDialog(
                onDismissRequest = { showCreateExpenseDialog = false },
                title = {
                    Text("Add New Expense")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        OutlinedTextField(
                            value = expenseTitle,
                            onValueChange = { expenseTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Team Lunch") }
                        )

                        OutlinedTextField(
                            value = expenseDescription,
                            onValueChange = { expenseDescription = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Lunch at restaurant") }
                        )

                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Amount ($)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0.00") }
                        )

                        // Split Between Multi-select
                        Text(
                            text = "Split between (select users):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (allMembers.isEmpty()) {
                            Text(
                                text = "No members in this project",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Column {
                                allMembers.forEach { member ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = selectedUsersForSplit.contains(member.userId),
                                            onCheckedChange = { isChecked ->
                                                selectedUsersForSplit = if (isChecked) {
                                                    selectedUsersForSplit + member.userId
                                                } else {
                                                    selectedUsersForSplit - member.userId
                                                }
                                            }
                                        )
                                        Text(
                                            text = "${if (member.role == "owner") "👑 " else ""}User ${member.userId.take(8)}",
                                            modifier = Modifier.padding(start = spacing.small)
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedUsersForSplit.isNotEmpty()) {
                            val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                            val amountPerPerson = if (selectedUsersForSplit.isNotEmpty())
                                amount / selectedUsersForSplit.size else 0.0
                            Text(
                                text = "${selectedUsersForSplit.size} users selected • $${String.format("%.2f", amountPerPerson)} each",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = expenseAmount.toDoubleOrNull()
                            Log.d("ProjectView", "=== CREATE EXPENSE BUTTON CLICKED ===")
                            Log.d("ProjectView", "Title: '$expenseTitle'")
                            Log.d("ProjectView", "Description: '$expenseDescription'")
                            Log.d("ProjectView", "Amount string: '$expenseAmount'")
                            Log.d("ProjectView", "Amount parsed: $amount")
                            Log.d("ProjectView", "Selected users: $selectedUsersForSplit")
                            Log.d("ProjectView", "Current project: ${currentProject?.id}")
                            Log.d("ProjectView", "All members: ${allMembers.map { it.userId }}")

                            if (expenseTitle.isNotBlank() &&
                                amount != null && amount > 0 &&
                                selectedUsersForSplit.isNotEmpty() &&
                                currentProject != null) {

                                Log.d("ProjectView", "Validation passed - calling createExpense")
                                projectViewModel.createExpense(
                                    projectId = currentProject.id,
                                    title = expenseTitle,
                                    description = expenseDescription.ifBlank { null },
                                    amount = amount,
                                    splitUserIds = selectedUsersForSplit.toList()
                                )

                                Log.d("ProjectView", "Expense creation call completed")
                                Toast.makeText(context, "Expense added: $expenseTitle", Toast.LENGTH_SHORT).show()

                                // Reset form
                                showCreateExpenseDialog = false
                                expenseTitle = ""
                                expenseDescription = ""
                                expenseAmount = ""
                                selectedUsersForSplit = setOf()
                            } else {
                                Log.e("ProjectView", "Validation failed!")
                                Log.e("ProjectView", "  Title blank? ${expenseTitle.isBlank()}")
                                Log.e("ProjectView", "  Amount null or <= 0? ${amount == null || amount <= 0}")
                                Log.e("ProjectView", "  No users selected? ${selectedUsersForSplit.isEmpty()}")
                                Log.e("ProjectView", "  No project? ${currentProject == null}")
                                Toast.makeText(context, "Please fill all required fields correctly", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Add Expense")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCreateExpenseDialog = false
                            expenseTitle = ""
                            expenseDescription = ""
                            expenseAmount = ""
                            selectedUsersForSplit = setOf()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Resource Dialog
        if (showAddResourceDialog) {
            AddResourceDialog(
                onDismiss = {
                    showAddResourceDialog = false
                    resourceName = ""
                    resourceLink = ""
                },
                onAddResource = { name, link ->
                    Log.d("ProjectView", "onAddResource callback called with: '$name', '$link'")
                    Log.d("ProjectView", "currentProject: $currentProject")
                    Log.d("ProjectView", "uiState.selectedProject: ${uiState.selectedProject}")
                    currentProject?.let { project ->
                        Log.d("ProjectView", "Calling projectViewModel.addResource with projectId: ${project.id}")
                        projectViewModel.addResource(project.id, name, link)
                        Toast.makeText(context, "Resource added successfully!", Toast.LENGTH_SHORT).show()
                        showAddResourceDialog = false
                        resourceName = ""
                        resourceLink = ""
                    } ?: run {
                        Log.e("ProjectView", "currentProject is null! Cannot add resource.")
                        Toast.makeText(context, "Error: No project selected", Toast.LENGTH_SHORT).show()
                    }
                },
                resourceName = resourceName,
                onResourceNameChange = { resourceName = it },
                resourceLink = resourceLink,
                onResourceLinkChange = { resourceLink = it },
                isAdding = uiState.isCreating,
                errorMessage = uiState.errorMessage
            )
        }
    }
}

@Composable
private fun ResourceItem(
    resource: Resource,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(spacing.medium)
    ) {
        Column {
            Text(
                text = resource.resourceName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = resource.link,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(resource.link))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun AddResourceDialog(
    onDismiss: () -> Unit,
    onAddResource: (String, String) -> Unit,
    resourceName: String,
    onResourceNameChange: (String) -> Unit,
    resourceLink: String,
    onResourceLinkChange: (String) -> Unit,
    isAdding: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { if (!isAdding) onDismiss() },
        title = {
            Text("Add Resource")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = resourceName,
                    onValueChange = onResourceNameChange,
                    label = { Text("Resource Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAdding,
                    isError = resourceName.isBlank() && resourceName.isNotEmpty(),
                    supportingText = if (resourceName.isBlank() && resourceName.isNotEmpty()) {
                        { Text("Resource name is required") }
                    } else null
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = resourceLink,
                    onValueChange = onResourceLinkChange,
                    label = { Text("Resource Link") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAdding,
                    placeholder = { Text("https://example.com") },
                    isError = resourceLink.isBlank() && resourceLink.isNotEmpty(),
                    supportingText = if (resourceLink.isBlank() && resourceLink.isNotEmpty()) {
                        { Text("Resource link is required") }
                    } else null
                )

                // Display error message if present
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("ProjectView", "Add button clicked!")
                    if (resourceName.isNotBlank() && resourceLink.isNotBlank()) {
                        onAddResource(resourceName, resourceLink)
                    }
                },
                enabled = resourceName.isNotBlank() && resourceLink.isNotBlank() && !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isAdding
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    currentProject: Project?,
    projectViewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (expense.description != null) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", expense.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = expense.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(expense.status) {
                            "fully_paid" -> MaterialTheme.colorScheme.tertiary
                            "pending" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            // Created by info
            Text(
                text = "Created by ${expense.createdBy.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            // Splits
            Text(
                text = "Split Details:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(spacing.small))

            expense.splits.forEach { split ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.extraSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = split.isPaid,
                            onCheckedChange = { isPaid ->
                                currentProject?.let { project ->
                                    projectViewModel.markSplitPaid(
                                        projectId = project.id,
                                        expenseId = expense.id,
                                        userId = split.userId.id,
                                        isPaid = isPaid
                                    )
                                }
                            }
                        )
                        Text(
                            text = split.userId.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (split.isPaid) TextDecoration.LineThrough else null
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", split.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (split.isPaid)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
