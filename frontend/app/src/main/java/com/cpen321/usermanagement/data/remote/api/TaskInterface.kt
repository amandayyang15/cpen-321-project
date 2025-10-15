package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Task
import com.cpen321.usermanagement.data.remote.dto.CreateTaskRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskInterface {
    @POST("projects/{projectId}/tasks")
    suspend fun createTask(
        @Path("projectId") projectId: String,
        @Body request: CreateTaskRequest
    ): Response<ApiResponse<Task>>
}
