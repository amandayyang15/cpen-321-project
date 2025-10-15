package com.cpen321.usermanagement.data.remote.dto

data class CreateTaskRequest(
    val name: String,
    val assignee: String,
    val status: String,
    val deadline: String?
)
