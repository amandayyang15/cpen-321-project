package com.cpen321.usermanagement.data.remote.dto

import java.util.Date

data class ProjectMember(
    val userId: String,
    val role: String, // "owner" or "user"
    val joinedAt: String
)

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val invitationCode: String,
    val ownerId: String,
    val members: List<ProjectMember>,
    val createdAt: String,
    val updatedAt: String,
    val isOwner: Boolean? = null
)

data class CreateProjectRequest(
    val name: String,
    val description: String? = null
)

data class UpdateProjectRequest(
    val name: String? = null,
    val description: String? = null
)
