package com.kaixuan.weightloss.api

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
}

class ApiRepository {
    private val api: ApiService get() = ApiClient.getService()

    // 认证
    suspend fun register(username: String, password: String, confirmPassword: String): ApiResult<AuthData> {
        return try {
            val body = api.register(RegisterRequest(username, password, confirmPassword))
            if (body.code == 0 && body.data != null) {
                ApiClient.saveAuthData(body.data)
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "注册失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun login(username: String, password: String): ApiResult<AuthData> {
        return try {
            val body = api.login(LoginRequest(username, password))
            if (body.code == 0 && body.data != null) {
                ApiClient.saveAuthData(body.data)
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "登录失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun logout(): ApiResult<Unit> {
        return try {
            api.logout()
            ApiClient.clearAuthData()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiClient.clearAuthData()
            ApiResult.Success(Unit)
        }
    }

    suspend fun refreshToken(): ApiResult<RefreshTokenData> {
        return try {
            val body = api.refreshToken()
            if (body.code == 0 && body.data != null) {
                ApiClient.updateToken(body.data.token)
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "刷新Token失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit> {
        return try {
            val body = api.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            if (body.code == 0) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "修改密码失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    // 用户设置
    suspend fun getSettings(): ApiResult<UserSettingsData> {
        return try {
            val body = api.getSettings()
            if (body.code == 0 && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "获取设置失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun updateSettings(
        height: Float? = null,
        targetWeight: Float? = null,
        weightUnit: String? = null,
        reminderEnabled: Boolean? = null,
        reminderTime: String? = null
    ): ApiResult<Unit> {
        return try {
            val body = api.updateSettings(
                UpdateSettingsRequest(height, targetWeight, weightUnit, reminderEnabled, reminderTime)
            )
            if (body.code == 0) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "更新设置失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    // 体重记录
    suspend fun addWeightRecord(date: String, weight: Float): ApiResult<WeightRecordData> {
        return try {
            val body = api.addWeightRecord(AddWeightRequest(date, weight))
            if (body.code == 0 && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "添加记录失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun getWeightRecords(days: Int? = null, startDate: String? = null, endDate: String? = null): ApiResult<WeightRecordsResponse> {
        return try {
            val body = api.getWeightRecords(startDate, endDate, days)
            if (body.code == 0 && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "获取记录失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun getWeightRecord(date: String): ApiResult<WeightRecordData> {
        return try {
            val body = api.getWeightRecord(date)
            if (body.code == 0 && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "获取记录失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun deleteWeightRecord(id: String): ApiResult<Unit> {
        return try {
            val body = api.deleteWeightRecord(id)
            if (body.code == 0) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(body.code, body.message.ifEmpty { "删除记录失败" })
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }
}
