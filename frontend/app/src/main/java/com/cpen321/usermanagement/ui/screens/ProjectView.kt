package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitBetween: List<String>,
    val date: String,
    val amountPerPerson: Double
)

@Composable
fun ProjectView(
    navigationStateManager: NavigationStateManager,
    modifier: Modifier = Modifier
) {
    ProjectContent(
        navigationStateManager = navigationStateManager,
        modifier = modifier
    )
}

@Composable
private fun ProjectContent(
    navigationStateManager: NavigationStateManager,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProjectTopBar(
                onBackClick = { navigationStateManager.navigateBack() },
                onProfileClick = { navigationStateManager.navigateToProfile() }
            )
        }
    ) { paddingValues ->
        ProjectBody(paddingValues = paddingValues)
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
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    var progressExpanded by remember { mutableStateOf(false) }
    var selectedProgress by remember { mutableStateOf("In Progress") }
    var selectedTab by remember { mutableStateOf("Task") }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var taskProgress by remember { mutableStateOf("In Progress") }
    var deadline by remember { mutableStateOf("") }
    var taskProgressExpanded by remember { mutableStateOf(false) }
    
    // Project Settings state
    var projectName by remember { mutableStateOf("") }
    var selectedUserToRemove by remember { mutableStateOf("") }
    var removeUserExpanded by remember { mutableStateOf(false) }

    // Expense state
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var showCreateExpenseDialog by remember { mutableStateOf(false) }
    var expenseDescription by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expensePaidBy by remember { mutableStateOf("") }
    var paidByExpanded by remember { mutableStateOf(false) }
    var selectedUsersForSplit by remember { mutableStateOf(setOf<String>()) }
    val availableUsers = listOf("Justin", "Alice", "Bob", "Charlie")

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
                    Text(
                        text = "Resource",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
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
                        // Expense Table Header
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
                                text = "Description",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .padding(horizontal = spacing.small),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Amount",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = spacing.small),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Paid By",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = spacing.small),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Split Between",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .padding(horizontal = spacing.small),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Per Person",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = spacing.small),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Expense Table Rows
                        expenses.forEach { expense ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.small, vertical = spacing.small)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(spacing.medium),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = expense.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .padding(horizontal = spacing.small),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$${String.format("%.2f", expense.amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = spacing.small),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = expense.paidBy,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = spacing.small),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = expense.splitBetween.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .padding(horizontal = spacing.small),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$${String.format("%.2f", expense.amountPerPerson)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = spacing.small),
                                    textAlign = TextAlign.Center
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
                        OutlinedTextField(
                            value = taskName,
                            onValueChange = { taskName = it },
                            label = { Text("Task Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = assignee,
                            onValueChange = { assignee = it },
                            label = { Text("Assignee") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Progress Dropdown
                        Box {
                            TextButton(
                                onClick = { taskProgressExpanded = !taskProgressExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = taskProgress,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                            DropdownMenu(
                                expanded = taskProgressExpanded,
                                onDismissRequest = { taskProgressExpanded = false }
                            ) {
                                listOf("In Progress", "Done", "Backlog", "Blocked").forEach { option ->
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
                        
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Deadline") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            Log.d("ProjectView", "Task created: $taskName, $assignee, $taskProgress, $deadline")
                            Toast.makeText(context, "Task created: $taskName", Toast.LENGTH_SHORT).show()
                            showCreateTaskDialog = false
                            taskName = ""
                            assignee = ""
                            taskProgress = "In Progress"
                            deadline = ""
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
                            value = expenseDescription,
                            onValueChange = { expenseDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Amount ($)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Paid By Dropdown
                        Text(
                            text = "Paid By:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Box {
                            TextButton(
                                onClick = { paidByExpanded = !paidByExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = expensePaidBy.ifEmpty { "Select who paid" },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                            DropdownMenu(
                                expanded = paidByExpanded,
                                onDismissRequest = { paidByExpanded = false }
                            ) {
                                availableUsers.forEach { user ->
                                    DropdownMenuItem(
                                        text = { Text(user) },
                                        onClick = {
                                            expensePaidBy = user
                                            paidByExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Split Between Multi-select
                        Text(
                            text = "Split between:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Column {
                            availableUsers.forEach { user ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = selectedUsersForSplit.contains(user),
                                        onCheckedChange = { isChecked ->
                                            selectedUsersForSplit = if (isChecked) {
                                                selectedUsersForSplit + user
                                            } else {
                                                selectedUsersForSplit - user
                                            }
                                        }
                                    )
                                    Text(
                                        text = user,
                                        modifier = Modifier.padding(start = spacing.small)
                                    )
                                }
                            }
                        }
                        
                        if (selectedUsersForSplit.isNotEmpty()) {
                            Text(
                                text = "Selected: ${selectedUsersForSplit.joinToString(", ")}",
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
                            if (expenseDescription.isNotBlank() && 
                                amount != null && amount > 0 && 
                                expensePaidBy.isNotBlank() && 
                                selectedUsersForSplit.isNotEmpty()) {
                                
                                val amountPerPerson = amount / selectedUsersForSplit.size
                                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                                val currentDate = dateFormat.format(Date())
                                
                                val newExpense = Expense(
                                    id = System.currentTimeMillis().toString(),
                                    description = expenseDescription,
                                    amount = amount,
                                    paidBy = expensePaidBy,
                                    splitBetween = selectedUsersForSplit.toList(),
                                    date = currentDate,
                                    amountPerPerson = amountPerPerson
                                )
                                
                                expenses = expenses + newExpense
                                
                                Log.d("ProjectView", "Expense created: $newExpense")
                                Toast.makeText(context, "Expense added: $expenseDescription", Toast.LENGTH_SHORT).show()
                                
                                // Reset form
                                showCreateExpenseDialog = false
                                expenseDescription = ""
                                expenseAmount = ""
                                expensePaidBy = ""
                                selectedUsersForSplit = setOf()
                            } else {
                                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
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
                            expenseDescription = ""
                            expenseAmount = ""
                            expensePaidBy = ""
                            selectedUsersForSplit = setOf()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
