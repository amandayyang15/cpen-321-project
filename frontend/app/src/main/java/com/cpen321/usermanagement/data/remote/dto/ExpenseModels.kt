package com.cpen321.usermanagement.data.remote.dto

data class ExpenseDTO(
    val id: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitBetween: List<String>,
    val amountPerPerson: Double,
    val date: String
)

data class CreateExpenseRequest(
    val projectId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitBetween: List<String>
)

data class ExpenseData(
    val id: String,
    val projectId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitBetween: List<String>,
    val amountPerPerson: Double,
    val createdAt: String,
    val status: String
)

data class ExpenseListData(
    val data: List<ExpenseDTO>
)
