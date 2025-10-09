package com.cpen321.usermanagement.data.repository

import android.util.Log
import com.cpen321.usermanagement.data.remote.api.ProjectInterface
import com.cpen321.usermanagement.data.remote.dto.CreateProjectRequest
import com.cpen321.usermanagement.data.remote.dto.Project
import com.cpen321.usermanagement.data.remote.dto.UpdateProjectRequest
import com.cpen321.usermanagement.utils.JsonUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectInterface: ProjectInterface
) : ProjectRepository {

    companion object {
        private const val TAG = "ProjectRepository"
    }

    override suspend fun createProject(name: String, description: String?): Result<Project> {
        val createProjectReq = CreateProjectRequest(name, description)
        return try {
            val response = projectInterface.createProject(createProjectReq)
            if (response.isSuccessful && response.body()?.data != null) {
                val project = response.body()!!.data!!
                Log.d(TAG, "Project created successfully: ${project.id}")
                Result.success(project)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to create project."
                )
                Log.e(TAG, "Project creation failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during project creation", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during project creation", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during project creation", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during project creation: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserProjects(): Result<List<Project>> {
        return try {
            val response = projectInterface.getUserProjects()
            if (response.isSuccessful && response.body()?.data != null) {
                val projects = response.body()!!.data!!
                Log.d(TAG, "Retrieved ${projects.size} projects")
                Result.success(projects)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to retrieve projects."
                )
                Log.e(TAG, "Failed to retrieve projects: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during project retrieval", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during project retrieval", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during project retrieval", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during project retrieval: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectById(projectId: String): Result<Project> {
        return try {
            val response = projectInterface.getProjectById(projectId)
            if (response.isSuccessful && response.body()?.data != null) {
                val project = response.body()!!.data!!
                Log.d(TAG, "Retrieved project: ${project.id}")
                Result.success(project)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to retrieve project."
                )
                Log.e(TAG, "Failed to retrieve project: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during project retrieval", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during project retrieval", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during project retrieval", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during project retrieval: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProject(projectId: String, name: String?, description: String?): Result<Project> {
        val updateProjectReq = UpdateProjectRequest(name, description)
        return try {
            val response = projectInterface.updateProject(projectId, updateProjectReq)
            if (response.isSuccessful && response.body()?.data != null) {
                val project = response.body()!!.data!!
                Log.d(TAG, "Project updated successfully: ${project.id}")
                Result.success(project)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to update project."
                )
                Log.e(TAG, "Project update failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during project update", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during project update", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during project update", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during project update: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            val response = projectInterface.deleteProject(projectId)
            if (response.isSuccessful) {
                Log.d(TAG, "Project deleted successfully: $projectId")
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to delete project."
                )
                Log.e(TAG, "Project deletion failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during project deletion", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during project deletion", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during project deletion", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during project deletion: ${e.code()}", e)
            Result.failure(e)
        }
    }
}
