package com.kaixuan.weightloss.api

import retrofit2.http.*

interface ApiService {

    // 认证接口
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(): RefreshTokenResponse

    @POST("auth/logout")
    suspend fun logout(): SimpleResponse

    @PUT("auth/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): SimpleResponse

    // 用户设置接口
    @GET("user/settings")
    suspend fun getSettings(): UserSettingsResponse

    @PUT("user/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): SimpleResponse

    // 体重记录接口
    @POST("weight/record")
    suspend fun addWeightRecord(@Body request: AddWeightRequest): WeightRecordResponse

    @GET("weight/records")
    suspend fun getWeightRecords(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("days") days: Int? = null
    ): WeightRecordsListResponse

    @GET("weight/record/{date}")
    suspend fun getWeightRecord(@Path("date") date: String): WeightRecordResponse

    @DELETE("weight/record/{id}")
    suspend fun deleteWeightRecord(@Path("id") id: String): SimpleResponse
}
