package com.cpen321.usermanagement.data.repository

import android.util.Log
import com.cpen321.usermanagement.data.remote.api.ExpenseInterface
import com.cpen321.usermanagement.data.remote.dto.CreateExpenseRequest
import com.cpen321.usermanagement.data.remote.dto.ExpenseDTO
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseInterface: ExpenseInterface
) : ExpenseRepository {

    companion object {
        private const val TAG = "ExpenseRepositoryImpl"
    }

    override suspend fun createExpense(
        projectId: String,
        description: String,
        amount: Double,
        paidBy: String,
        splitBetween: List<String>
    ): Result<ExpenseDTO> {
        return try {
            Log.d(TAG, "Creating expense: projectId=$projectId, description=$description, amount=$amount, paidBy=$paidBy, splitBetween=$splitBetween")
            
            val request = CreateExpenseRequest(
                projectId = projectId,
                description = description,
                amount = amount,
                paidBy = paidBy,
                splitBetween = splitBetween
            )
            
            val response = expenseInterface.createExpense("", request)
            
            Log.d(TAG, "Create expense response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body()?.data != null) {
                val expenseData = response.body()!!.data!!
                val expense = ExpenseDTO(
                    id = expenseData.id,
                    description = expenseData.description,
                    amount = expenseData.amount,
                    paidBy = expenseData.paidBy,
                    splitBetween = expenseData.splitBetween,
                    amountPerPerson = expenseData.amountPerPerson,
                    date = expenseData.createdAt
                )
                Log.d(TAG, "Successfully created expense: ${expense.id}")
                Result.success(expense)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to create expense")
                Log.e(TAG, "Failed to create expense: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating expense", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectExpenses(projectId: String): Result<List<ExpenseDTO>> {
        return try {
            Log.d(TAG, "Fetching expenses for project: $projectId")
            val response = expenseInterface.getProjectExpenses("", projectId)
            
            Log.d(TAG, "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body()?.data != null) {
                val expenses = response.body()!!.data!!.data
                Log.d(TAG, "Successfully fetched ${expenses.size} expenses")
                Result.success(expenses)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch expenses")
                Log.e(TAG, "Failed to fetch expenses: $errorMessage, Response body: ${response.body()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching expenses", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            val response = expenseInterface.deleteExpense("", expenseId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to delete expense")
                Log.e(TAG, "Failed to delete expense: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting expense", e)
            Result.failure(e)
        }
    }
}
