package com.kaixuan.weightloss.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 认证接口
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("auth/refresh")
    suspend fun refreshToken(): Response<ApiResponse<RefreshTokenData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @PUT("auth/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    // 用户设置接口
    @GET("user/settings")
    suspend fun getSettings(): Response<ApiResponse<UserSettingsData>>

    @PUT("user/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<ApiResponse<Unit>>

    // 体重记录接口
    @POST("weight/record")
    suspend fun addWeightRecord(@Body request: AddWeightRequest): Response<ApiResponse<WeightRecordData>>

    @GET("weight/records")
    suspend fun getWeightRecords(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("days") days: Int? = null
    ): Response<ApiResponse<WeightRecordsResponse>>

    @GET("weight/record/{date}")
    suspend fun getWeightRecord(@Path("date") date: String): Response<ApiResponse<WeightRecordData>>

    @DELETE("weight/record/{id}")
    suspend fun deleteWeightRecord(@Path("id") id: String): Response<ApiResponse<Unit>>
}
