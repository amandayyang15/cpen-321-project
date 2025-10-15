package com.cpen321.usermanagement.data.remote.dto

data class ExpenseSplit(
    val userId: ExpenseUser,  // Changed from String to ExpenseUser to match backend populated response
    val amount: Double,
    val isPaid: Boolean
)

data class ExpenseUser(
    val id: String,
    val name: String,
    val email: String,
    val profilePicture: String?
)

data class Expense(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val amount: Double,
    val createdBy: ExpenseUser,
    val splits: List<ExpenseSplit>,
    val status: String, // "pending", "fully_paid", "cancelled"
    val createdAt: String,
    val updatedAt: String
)

data class CreateExpenseRequest(
    val title: String,
    val description: String?,
    val amount: Double,
    val splitUserIds: List<String>
)

data class MarkSplitPaidRequest(
    val userId: String,
    val isPaid: Boolean
)
