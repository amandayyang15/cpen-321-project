package com.cpen321.usermanagement.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

data class CalendarStatusResponse(
    val connected: Boolean,
    val enabled: Boolean
)

data class AuthUrlResponse(
    val authUrl: String
)

interface CalendarInterface {
    @GET("calendar/status")
    suspend fun getCalendarStatus(): Response<CalendarStatusResponse>

    @GET("calendar/oauth/authorize")
    suspend fun getAuthorizationUrl(): Response<AuthUrlResponse>

    @POST("calendar/enable")
    suspend fun enableCalendar(): Response<Map<String, Any>>

    @POST("calendar/disable")
    suspend fun disableCalendar(): Response<Map<String, Any>>
}
