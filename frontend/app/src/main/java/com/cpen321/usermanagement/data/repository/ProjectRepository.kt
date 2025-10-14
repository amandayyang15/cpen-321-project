package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.CreateProjectRequest
import com.cpen321.usermanagement.data.remote.dto.Project
import com.cpen321.usermanagement.data.remote.dto.UpdateProjectRequest

interface ProjectRepository {
    suspend fun createProject(name: String, description: String? = null): Result<Project>
    suspend fun joinProject(invitationCode: String): Result<Project>
    suspend fun getUserProjects(): Result<List<Project>>
    suspend fun getProjectById(projectId: String): Result<Project>
    suspend fun updateProject(projectId: String, name: String? = null, description: String? = null): Result<Project>
    suspend fun deleteProject(projectId: String): Result<Unit>
}
