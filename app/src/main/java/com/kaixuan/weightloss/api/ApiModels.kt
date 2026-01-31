package com.kaixuan.weightloss.api

import com.google.gson.annotations.SerializedName

// 认证响应
data class AuthResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: AuthData? = null
)

data class RefreshTokenResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: RefreshTokenData? = null
)

data class SimpleResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = ""
)

// 用户设置响应
data class UserSettingsResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: UserSettingsData? = null
)

// 体重记录响应
data class WeightRecordResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: WeightRecordData? = null
)

data class WeightRecordsListResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: WeightRecordsResponse? = null
)

// 认证相关
data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class AuthData(
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("token") val token: String,
    @SerializedName("expiresIn") val expiresIn: Long
)

data class RefreshTokenData(
    @SerializedName("token") val token: String,
    @SerializedName("expiresIn") val expiresIn: Long
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

// 用户设置相关
data class UserSettingsData(
    @SerializedName("height") val height: Float?,
    @SerializedName("targetWeight") val targetWeight: Float?,
    @SerializedName("weightUnit") val weightUnit: String?,
    @SerializedName("reminderEnabled") val reminderEnabled: Boolean?,
    @SerializedName("reminderTime") val reminderTime: String?
)

data class UpdateSettingsRequest(
    @SerializedName("height") val height: Float? = null,
    @SerializedName("targetWeight") val targetWeight: Float? = null,
    @SerializedName("weightUnit") val weightUnit: String? = null,
    @SerializedName("reminderEnabled") val reminderEnabled: Boolean? = null,
    @SerializedName("reminderTime") val reminderTime: String? = null
)

// 体重记录相关
data class AddWeightRequest(
    @SerializedName("date") val date: String,
    @SerializedName("weight") val weight: Float
)

data class WeightRecordData(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("weight") val weight: Float,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class WeightRecordsResponse(
    @SerializedName("records") val records: List<WeightRecordData>,
    @SerializedName("statistics") val statistics: WeightStatistics?
)

data class WeightStatistics(
    @SerializedName("maxWeight") val maxWeight: Float,
    @SerializedName("minWeight") val minWeight: Float,
    @SerializedName("avgWeight") val avgWeight: Float,
    @SerializedName("latestWeight") val latestWeight: Float,
    @SerializedName("change") val change: Float
)
