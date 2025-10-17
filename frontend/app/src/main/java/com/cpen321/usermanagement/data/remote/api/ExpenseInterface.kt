package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.CreateExpenseRequest
import com.cpen321.usermanagement.data.remote.dto.ExpenseData
import com.cpen321.usermanagement.data.remote.dto.ExpenseListData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ExpenseInterface {
    @POST("expenses")
    suspend fun createExpense(
        @Header("Authorization") authHeader: String,
        @Body request: CreateExpenseRequest
    ): Response<ApiResponse<ExpenseData>>

    @GET("expenses/project/{projectId}")
    suspend fun getProjectExpenses(
        @Header("Authorization") authHeader: String,
        @Path("projectId") projectId: String
    ): Response<ApiResponse<ExpenseListData>>

    @DELETE("expenses/{expenseId}")
    suspend fun deleteExpense(
        @Header("Authorization") authHeader: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Unit>>
}
