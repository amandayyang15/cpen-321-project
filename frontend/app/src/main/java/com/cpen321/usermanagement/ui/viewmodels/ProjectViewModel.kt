package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Project
import com.cpen321.usermanagement.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,

    // Data states
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,

    // Message states
    val message: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
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
