package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.CreateExpenseRequest
import com.cpen321.usermanagement.data.remote.dto.Expense
import com.cpen321.usermanagement.data.remote.dto.MarkSplitPaidRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ExpenseInterface {
    @POST("projects/{projectId}/expenses")
    suspend fun createExpense(
        @Path("projectId") projectId: String,
        @Body request: CreateExpenseRequest
    ): Response<ApiResponse<Expense>>

    @GET("projects/{projectId}/expenses")
    suspend fun getProjectExpenses(
        @Path("projectId") projectId: String
    ): Response<ApiResponse<List<Expense>>>

    @PATCH("projects/{projectId}/expenses/{expenseId}/splits")
    suspend fun markSplitPaid(
        @Path("projectId") projectId: String,
        @Path("expenseId") expenseId: String,
        @Body request: MarkSplitPaidRequest
    ): Response<ApiResponse<Expense>>

    @DELETE("projects/{projectId}/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("projectId") projectId: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Unit>>
}
