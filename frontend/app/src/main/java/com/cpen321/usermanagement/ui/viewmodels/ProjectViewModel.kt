package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Expense
import com.cpen321.usermanagement.data.remote.dto.Project
import com.cpen321.usermanagement.data.repository.ExpenseRepository
import com.cpen321.usermanagement.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cpen321.usermanagement.data.remote.dto.Task
import com.cpen321.usermanagement.data.repository.TaskRepository


data class ProjectUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,

    // Data states
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val expenses: List<Expense> = emptyList(),

    // Message states
    val message: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val expenseRepository: ExpenseRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ProjectViewModel"
    }

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    private var isCreatingProject = false
    private var isJoiningProject = false

    init {
        loadUserProjects()
    }

    // Store tasks per project ID
    private val _tasksByProject = MutableStateFlow<Map<String, List<Task>>>(emptyMap())
    val tasksByProject: StateFlow<Map<String, List<Task>>> = _tasksByProject
    
    // Get tasks for current project
    fun getTasksForProject(projectId: String): List<Task> {
        return _tasksByProject.value[projectId] ?: emptyList()
    }
    
    fun clearTasks() {
        Log.d(TAG, "Clearing all tasks")
        _tasksByProject.value = emptyMap()
    }
    
    fun clearTasksForProject(projectId: String) {
        Log.d(TAG, "Clearing tasks for project: $projectId")
        val currentTasks = _tasksByProject.value.toMutableMap()
        currentTasks.remove(projectId)
        _tasksByProject.value = currentTasks
    }

    fun loadProjectTasks(projectId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading tasks for project: $projectId")
                val fetchedTasks = taskRepository.getProjectTasks(projectId)
                
                // Validate that all tasks belong to the specified project
                val validTasks = fetchedTasks.filter { task ->
                    task.projectId == projectId
                }
                
                if (validTasks.size != fetchedTasks.size) {
                    Log.w(TAG, "Filtered out ${fetchedTasks.size - validTasks.size} tasks that don't belong to project: $projectId")
                }
                
                // Store tasks for this specific project
                val currentTasks = _tasksByProject.value.toMutableMap()
                currentTasks[projectId] = validTasks
                _tasksByProject.value = currentTasks
                
                Log.d(TAG, "Successfully loaded ${validTasks.size} tasks for project: $projectId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load tasks", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load tasks: ${e.message}"
                )
            }
        }
    }

    fun createTask(
        projectId: String,
        name: String,
        assignee: String,
        status: String,
        deadline: String?
    ) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Task name cannot be empty"
            )
            return
        }

        if (assignee.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Assignee cannot be empty"
            )
            return
        }

        Log.d(TAG, "Creating task: $name for project: $projectId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            try {
                val task = taskRepository.createTask(projectId, name.trim(), assignee.trim(), status, deadline)
                Log.d(TAG, "Task created successfully: ${task.id}")
                
                // Validate that the created task belongs to the correct project
                if (task.projectId != projectId) {
                    Log.e(TAG, "ERROR: Created task belongs to project ${task.projectId} but was created for project $projectId")
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = "Task creation failed: Project ID mismatch"
                    )
                    return@launch
                }
                
                // Add task to local state immediately for instant UI update
                val currentTasks = _tasksByProject.value.toMutableMap()
                val projectTasks = currentTasks[projectId] ?: emptyList()
                currentTasks[projectId] = listOf(task) + projectTasks
                _tasksByProject.value = currentTasks
                Log.d(TAG, "Task added to local state for project: $projectId")
                
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    message = "Task created successfully"
                )

                Log.d(TAG, "Task added to local state. Total tasks: ${_tasksByProject.value[projectId]?.size ?: 0}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create task", e)
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Failed to create task: ${e.message}"
                )
            }
        }
    }
    fun loadUserProjects() {
        Log.d(TAG, "Loading user projects...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            projectRepository.getUserProjects()
                .onSuccess { projects ->
                    Log.d(TAG, "Successfully loaded ${projects.size} projects: ${projects.map { it.name }}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        projects = projects,
                        message = "Projects loaded successfully"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load projects", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load projects"
                    )
                }
        }
    }

    fun createProject(name: String, description: String? = null) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Project name cannot be empty"
            )
            return
        }

        // Prevent duplicate calls with both state and flag
        if (_uiState.value.isCreating || isCreatingProject) {
            Log.w(TAG, "Project creation already in progress, ignoring duplicate call")
            return
        }

        Log.d(TAG, "Creating project: $name")
        isCreatingProject = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            projectRepository.createProject(name.trim(), description?.trim())
                .onSuccess { project ->
                    Log.d(TAG, "Project created successfully: ${project.id}")
                    // Add project to local state immediately for instant UI update
                    val currentProjects = _uiState.value.projects
                    val updatedProjects = listOf(project) + currentProjects
                    Log.d(TAG, "Updated projects list: ${updatedProjects.map { it.name }}")

                    // Force state update with explicit copy
                    val newState = _uiState.value.copy(
                        isCreating = false,
                        projects = updatedProjects,
                        message = "Project created successfully"
                    )
                    _uiState.value = newState
                    isCreatingProject = false

                    Log.d(TAG, "State updated - projects count: ${_uiState.value.projects.size}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to create project", error)
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "Failed to create project"
                    )
                    isCreatingProject = false
                }
        }
    }

    fun joinProject(invitationCode: String) {
        if (invitationCode.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Invitation code cannot be empty"
            )
            return
        }

        // Prevent duplicate calls
        if (_uiState.value.isCreating || isJoiningProject) {
            Log.w(TAG, "Project join already in progress, ignoring duplicate call")
            return
        }

        Log.d(TAG, "Joining project with code: $invitationCode")
        isJoiningProject = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            projectRepository.joinProject(invitationCode.trim())
                .onSuccess { project ->
                    Log.d(TAG, "Successfully joined project: ${project.id}")
                    // Add project to local state immediately for instant UI update
                    val currentProjects = _uiState.value.projects
                    val updatedProjects = listOf(project) + currentProjects
                    Log.d(TAG, "Updated projects list: ${updatedProjects.map { it.name }}")

                    // Force state update with explicit copy
                    val newState = _uiState.value.copy(
                        isCreating = false,
                        projects = updatedProjects,
                        message = "Successfully joined project"
                    )
                    _uiState.value = newState
                    isJoiningProject = false

                    Log.d(TAG, "State updated - projects count: ${_uiState.value.projects.size}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to join project", error)
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "Failed to join project"
                    )
                    isJoiningProject = false
                }
        }
    }

    fun updateProject(projectId: String, name: String? = null, description: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            projectRepository.updateProject(projectId, name, description)
                .onSuccess { updatedProject ->
                    Log.d(TAG, "Project updated: ${updatedProject.id}")
                    val updatedProjects = _uiState.value.projects.map { project ->
                        if (project.id == projectId) updatedProject else project
                    }
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        projects = updatedProjects,
                        selectedProject = if (_uiState.value.selectedProject?.id == projectId) updatedProject else _uiState.value.selectedProject,
                        message = "Project updated successfully"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to update project", error)
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message ?: "Failed to update project"
                    )
                }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)

            projectRepository.deleteProject(projectId)
                .onSuccess {
                    Log.d(TAG, "Project deleted: $projectId")
                    val updatedProjects = _uiState.value.projects.filter { it.id != projectId }
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        projects = updatedProjects,
                        selectedProject = if (_uiState.value.selectedProject?.id == projectId) null else _uiState.value.selectedProject,
                        message = "Project deleted successfully"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to delete project", error)
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: "Failed to delete project"
                    )
                }
        }
    }

    fun selectProject(project: Project) {
        Log.d(TAG, "selectProject called with project: ${project.name} (${project.id})")
        _uiState.value = _uiState.value.copy(selectedProject = project)
        Log.d(TAG, "selectedProject updated: ${_uiState.value.selectedProject?.name}")
        // Load expenses for the selected project
        loadExpenses(project.id)
    }

    // Expense-related methods
    fun loadExpenses(projectId: String) {
        Log.d(TAG, "Loading expenses for project: $projectId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            expenseRepository.getProjectExpenses(projectId)
                .onSuccess { expenses ->
                    Log.d(TAG, "Successfully loaded ${expenses.size} expenses")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        expenses = expenses
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load expenses", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load expenses"
                    )
                }
        }
    }

    fun createExpense(
        projectId: String,
        title: String,
        description: String?,
        amount: Double,
        splitUserIds: List<String>
    ) {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Expense title cannot be empty"
            )
            return
        }

        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Amount must be greater than 0"
            )
            return
        }

        if (splitUserIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "At least one user must be selected"
            )
            return
        }

        Log.d(TAG, "Creating expense: $title for project: $projectId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            expenseRepository.createExpense(projectId, title.trim(), description?.trim(), amount, splitUserIds)
                .onSuccess { expense ->
                    Log.d(TAG, "Expense created successfully: ${expense.id}")
                    // Add expense to local state immediately
                    val updatedExpenses = listOf(expense) + _uiState.value.expenses
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        expenses = updatedExpenses,
                        message = "Expense created successfully"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to create expense", error)
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "Failed to create expense"
                    )
                }
        }
    }

    fun markSplitPaid(projectId: String, expenseId: String, userId: String, isPaid: Boolean) {
        Log.d(TAG, "Marking split as ${if (isPaid) "paid" else "unpaid"} for expense: $expenseId, user: $userId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            expenseRepository.markSplitPaid(projectId, expenseId, userId, isPaid)
                .onSuccess { updatedExpense ->
                    Log.d(TAG, "Split status updated successfully")
                    // Update expense in local state
                    val updatedExpenses = _uiState.value.expenses.map { expense ->
                        if (expense.id == expenseId) updatedExpense else expense
                    }
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        expenses = updatedExpenses,
                        message = "Payment status updated"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to update split status", error)
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message ?: "Failed to update payment status"
                    )
                }
        }
    }

    fun deleteExpense(projectId: String, expenseId: String) {
        Log.d(TAG, "Deleting expense: $expenseId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)

            expenseRepository.deleteExpense(projectId, expenseId)
                .onSuccess {
                    Log.d(TAG, "Expense deleted successfully")
                    val updatedExpenses = _uiState.value.expenses.filter { it.id != expenseId }
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        expenses = updatedExpenses,
                        message = "Expense deleted successfully"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to delete expense", error)
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: "Failed to delete expense"
                    )
                }
        }
    }

    fun addResource(projectId: String, resourceName: String, link: String) {
        Log.d(TAG, "addResource called with projectId: $projectId, resourceName: '$resourceName', link: '$link'")

        if (resourceName.isBlank()) {
            Log.d(TAG, "Resource name is blank, showing error")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Resource name cannot be empty"
            )
            return
        }

        if (link.isBlank()) {
            Log.d(TAG, "Resource link is blank, showing error")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Resource link cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            projectRepository.addResource(projectId, resourceName.trim(), link.trim())
                .onSuccess { project ->
                    Log.d(TAG, "Resource added successfully to project: ${project.id}")
                    // Update the project in the local state
                    val updatedProjects = _uiState.value.projects.map { p ->
                        if (p.id == projectId) project else p
                    }
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        projects = updatedProjects,
                        selectedProject = if (_uiState.value.selectedProject?.id == projectId) project else _uiState.value.selectedProject,
                        message = "Resource added successfully"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to add resource", error)
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "Failed to add resource"
                    )
                }
        }
    }

    fun refreshSelectedProject() {
        val selectedProject = _uiState.value.selectedProject
        if (selectedProject != null) {
            Log.d(TAG, "Refreshing selected project: ${selectedProject.name}")
            viewModelScope.launch {
                projectRepository.getProjectById(selectedProject.id)
                    .onSuccess { updatedProject ->
                        Log.d(TAG, "Project refreshed successfully: ${updatedProject.name}")
                        // Update the project in the local state
                        val updatedProjects = _uiState.value.projects.map { p ->
                            if (p.id == updatedProject.id) updatedProject else p
                        }
                        _uiState.value = _uiState.value.copy(
                            projects = updatedProjects,
                            selectedProject = updatedProject
                        )
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to refresh project", error)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to refresh project data"
                        )
                    }
            }
        }
    }

    fun refreshAllProjects() {
        Log.d(TAG, "Refreshing all projects for user")
        loadUserProjects()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            message = null,
            errorMessage = null
        )
    }
}
