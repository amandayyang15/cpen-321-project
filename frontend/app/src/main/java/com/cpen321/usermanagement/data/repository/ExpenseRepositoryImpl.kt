package com.cpen321.usermanagement.data.repository

import android.util.Log
import com.cpen321.usermanagement.data.remote.api.ExpenseInterface
import com.cpen321.usermanagement.data.remote.dto.CreateExpenseRequest
import com.cpen321.usermanagement.data.remote.dto.Expense
import com.cpen321.usermanagement.data.remote.dto.MarkSplitPaidRequest
import com.cpen321.usermanagement.utils.JsonUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseInterface: ExpenseInterface
) : ExpenseRepository {

    companion object {
        private const val TAG = "ExpenseRepository"
    }

    override suspend fun createExpense(
        projectId: String,
        title: String,
        description: String?,
        amount: Double,
        splitUserIds: List<String>
    ): Result<Expense> {
        val createExpenseReq = CreateExpenseRequest(title, description, amount, splitUserIds)
        return try {
            val response = expenseInterface.createExpense(projectId, createExpenseReq)
            if (response.isSuccessful && response.body()?.data != null) {
                val expense = response.body()!!.data!!
                Log.d(TAG, "Expense created successfully: ${expense.id}")
                Result.success(expense)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to create expense."
                )
                Log.e(TAG, "Expense creation failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during expense creation", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during expense creation", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during expense creation", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during expense creation: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectExpenses(projectId: String): Result<List<Expense>> {
        return try {
            val response = expenseInterface.getProjectExpenses(projectId)
            if (response.isSuccessful && response.body()?.data != null) {
                val expenses = response.body()!!.data!!
                Log.d(TAG, "Retrieved ${expenses.size} expenses for project $projectId")
                Result.success(expenses)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to retrieve expenses."
                )
                Log.e(TAG, "Failed to retrieve expenses: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during expense retrieval", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during expense retrieval", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during expense retrieval", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during expense retrieval: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun markSplitPaid(
        projectId: String,
        expenseId: String,
        userId: String,
        isPaid: Boolean
    ): Result<Expense> {
        val markPaidReq = MarkSplitPaidRequest(userId, isPaid)
        return try {
            Log.d(TAG, "Calling API to mark split paid: projectId=$projectId, expenseId=$expenseId, userId=$userId, isPaid=$isPaid")
            val response = expenseInterface.markSplitPaid(projectId, expenseId, markPaidReq)
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful && response.body()?.data != null) {
                val expense = response.body()!!.data!!
                Log.d(TAG, "Split marked as ${if (isPaid) "paid" else "unpaid"} for expense ${expense.id}")
                Log.d(TAG, "Expense createdBy: ${expense.createdBy}")
                Log.d(TAG, "Expense splits count: ${expense.splits.size}")
                expense.splits.forEachIndexed { index, split ->
                    Log.d(TAG, "Split[$index]: userId=${split.userId}, amount=${split.amount}, isPaid=${split.isPaid}")
                }
                Result.success(expense)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to update split status."
                )
                Log.e(TAG, "Failed to update split: $errorMessage")
                Log.e(TAG, "Error body: $errorBodyString")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "JSON parsing error during split update", e)
            Log.e(TAG, "JSON parsing error message: ${e.message}")
            Log.e(TAG, "JSON parsing error cause: ${e.cause}")
            Result.failure(Exception("Failed to parse response: ${e.message}"))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during split update", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during split update", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during split update", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during split update: ${e.code()}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during split update", e)
            Log.e(TAG, "Error type: ${e.javaClass.name}")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Error stacktrace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(projectId: String, expenseId: String): Result<Unit> {
        return try {
            val response = expenseInterface.deleteExpense(projectId, expenseId)
            if (response.isSuccessful) {
                Log.d(TAG, "Expense deleted successfully: $expenseId")
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = JsonUtils.parseErrorMessage(
                    errorBodyString,
                    response.body()?.message ?: "Failed to delete expense."
                )
                Log.e(TAG, "Expense deletion failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout during expense deletion", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed during expense deletion", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during expense deletion", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error during expense deletion: ${e.code()}", e)
            Result.failure(e)
        }
    }
}
