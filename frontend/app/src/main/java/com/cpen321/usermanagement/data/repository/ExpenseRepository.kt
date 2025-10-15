package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Expense

interface ExpenseRepository {
    suspend fun createExpense(
        projectId: String,
        title: String,
        description: String?,
        amount: Double,
        splitUserIds: List<String>
    ): Result<Expense>
    
    suspend fun getProjectExpenses(projectId: String): Result<List<Expense>>
    
    suspend fun markSplitPaid(
        projectId: String,
        expenseId: String,
        userId: String,
        isPaid: Boolean
    ): Result<Expense>
    
    suspend fun deleteExpense(projectId: String, expenseId: String): Result<Unit>
}
