package com.kaixuan.weightloss.api

import com.google.gson.annotations.SerializedName

// 通用响应基类
open class BaseResponse(
    val code: Int = 0,
    val message: String = ""
)

// 认证响应
data class AuthResponse(
    val code: Int = 0,
    val message: String = "",
    val data: AuthData? = null
)

data class RefreshTokenResponse(
    val code: Int = 0,
    val message: String = "",
    val data: RefreshTokenData? = null
)

data class SimpleResponse(
    val code: Int = 0,
    val message: String = ""
)

// 用户设置响应
data class UserSettingsResponse(
    val code: Int = 0,
    val message: String = "",
    val data: UserSettingsData? = null
)

// 体重记录响应
data class WeightRecordResponse(
    val code: Int = 0,
    val message: String = "",
    val data: WeightRecordData? = null
)

data class WeightRecordsListResponse(
    val code: Int = 0,
    val message: String = "",
    val data: WeightRecordsResponse? = null
)

// 认证相关
data class RegisterRequest(
    val username: String,
    val password: String,
    val confirmPassword: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthData(
    val userId: String,
    val username: String,
    val token: String,
    val expiresIn: Long
)

data class RefreshTokenData(
    val token: String,
    val expiresIn: Long
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

// 用户设置相关
data class UserSettingsData(
    val height: Float?,
    val targetWeight: Float?,
    val weightUnit: String?,
    val reminderEnabled: Boolean?,
    val reminderTime: String?
)

data class UpdateSettingsRequest(
    val height: Float? = null,
    val targetWeight: Float? = null,
    val weightUnit: String? = null,
    val reminderEnabled: Boolean? = null,
    val reminderTime: String? = null
)

// 体重记录相关
data class AddWeightRequest(
    val date: String,
    val weight: Float
)

data class WeightRecordData(
    val id: String,
    val date: String,
    val weight: Float,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class WeightRecordsResponse(
    val records: List<WeightRecordData>,
    val statistics: WeightStatistics?
)

data class WeightStatistics(
    val maxWeight: Float,
    val minWeight: Float,
    val avgWeight: Float,
    val latestWeight: Float,
    val change: Float
)
