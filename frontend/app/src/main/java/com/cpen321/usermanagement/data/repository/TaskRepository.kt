package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Task

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
