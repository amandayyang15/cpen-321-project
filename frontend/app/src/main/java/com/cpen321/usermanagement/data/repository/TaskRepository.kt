package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.api.TaskInterface
import com.cpen321.usermanagement.data.remote.dto.Task
import com.cpen321.usermanagement.data.remote.dto.CreateTaskRequest
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    suspend fun createTask(
        projectId: String,
        name: String,
        assignee: String,
        status: String,
        deadline: String?
    ): Task

    suspend fun getProjectTasks(projectId: String): List<Task>
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskInterface: TaskInterface
) : TaskRepository {
    override suspend fun createTask(
        projectId: String,
        name: String,
        assignee: String,
        status: String,
        deadline: String?
    ): Task {
        val request = CreateTaskRequest(name, assignee, status, deadline)
        val response = taskInterface.createTask(projectId, request)
        if (response.isSuccessful && response.body()?.data != null) {
            return response.body()!!.data!!
        } else {
            throw Exception(response.body()?.message ?: "Failed to create task")
        }
    }

    override suspend fun getProjectTasks(projectId: String): List<Task> {
        val response = taskInterface.getProjectTasks(projectId)
        if (response.isSuccessful && response.body()?.data != null) {
            return response.body()!!.data!!
        } else {
            throw Exception(response.body()?.message ?: "Failed to fetch tasks")
        }
    }
}
