package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.ExpenseDTO

interface ExpenseRepository {
    suspend fun createExpense(
        projectId: String,
        description: String,
        amount: Double,
        paidBy: String,
        splitBetween: List<String>
    ): Result<ExpenseDTO>
    
    suspend fun getProjectExpenses(projectId: String): Result<List<ExpenseDTO>>
    
    suspend fun deleteExpense(expenseId: String): Result<Unit>
}
