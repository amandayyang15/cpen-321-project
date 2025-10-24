package com.cpen321.usermanagement.data.remote.dto

data class Task(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val status: String,
    val assignees: List<String>,
    val createdBy: String,
    val deadline: String?,
    val createdAt: String,
    val updatedAt: String
)
