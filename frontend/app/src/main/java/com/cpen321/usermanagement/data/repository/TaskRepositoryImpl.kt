package com.cpen321.usermanagement.data.repository

import javax.inject.Inject
import com.cpen321.usermanagement.data.remote.api.TaskInterface
import com.cpen321.usermanagement.data.remote.dto.Task
import com.cpen321.usermanagement.data.remote.dto.CreateTaskRequest

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
        val requestId = System.currentTimeMillis()
        println("🚀 TaskRepositoryImpl.createTask() called - Request ID: $requestId")
        println("📝 Task data: projectId=$projectId, name=$name, assignee=$assignee, status=$status, deadline=$deadline")
        println("🔍 Thread: ${Thread.currentThread().name}")
        println("🔍 Timestamp: ${System.currentTimeMillis()}")
        
        val request = CreateTaskRequest(name, assignee, status, deadline)
        println("📤 Sending CreateTaskRequest: $request")
        
        try {
            println("🌐 About to call taskInterface.createTask()")
            println("🌐 Calling taskInterface.createTask() with projectId=$projectId")
            val response = taskInterface.createTask(projectId, request)
            println("🌐 taskInterface.createTask() completed")
            println("📡 HTTP Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            println("📄 Response body: ${response.body()}")
            println("❌ Response error body: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful && response.body()?.data != null) {
                val task = response.body()!!.data!!
                println("✅ Task created successfully: ${task.id}")
                return task
            } else {
                val errorMessage = response.body()?.message ?: "Failed to create task"
                println("❌ Task creation failed: $errorMessage")
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Exception in createTask: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    override suspend fun getProjectTasks(projectId: String): List<Task> {
        println("🔍 TaskRepositoryImpl.getProjectTasks() called for project: $projectId")
        println("🔍 Project ID type: ${projectId::class.java.simpleName}")
        println("🔍 Project ID length: ${projectId.length}")
        println("🔍 Project ID characters: ${projectId.toCharArray().joinToString(", ")}")
        
        try {
            println("🌐 About to call taskInterface.getProjectTasks() with projectId=$projectId")
            val response = taskInterface.getProjectTasks(projectId)
            println("🌐 taskInterface.getProjectTasks() completed")
            println("📡 HTTP Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            println("📄 Response body: ${response.body()}")
            println("❌ Response error body: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful && response.body()?.data != null) {
                val tasks = response.body()!!.data!!
                println("✅ Retrieved ${tasks.size} tasks for project $projectId")
                tasks.forEach { task ->
                    println("📋 Task: ${task.id} - ${task.title} (${task.status}) - ProjectID: ${task.projectId}")
                }
                return tasks
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch tasks"
                println("❌ Failed to fetch tasks: $errorMessage")
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Exception in getProjectTasks: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
