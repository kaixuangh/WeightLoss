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
            val response = api.register(RegisterRequest(username, password, confirmPassword))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiClient.saveAuthData(body.data)
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "注册失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun login(username: String, password: String): ApiResult<AuthData> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiClient.saveAuthData(body.data)
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "登录失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun logout(): ApiResult<Unit> {
        return try {
            val response = api.logout()
            ApiClient.clearAuthData()
            if (response.isSuccessful && response.body()?.code == 0) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Success(Unit) // 即使服务端失败也清除本地token
            }
        } catch (e: Exception) {
            ApiClient.clearAuthData()
            ApiResult.Success(Unit)
        }
    }

    suspend fun refreshToken(): ApiResult<RefreshTokenData> {
        return try {
            val response = api.refreshToken()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiClient.updateToken(body.data.token)
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "刷新Token失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit> {
        return try {
            val response = api.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "修改密码失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    // 用户设置
    suspend fun getSettings(): ApiResult<UserSettingsData> {
        return try {
            val response = api.getSettings()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "获取设置失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
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
            val response = api.updateSettings(
                UpdateSettingsRequest(height, targetWeight, weightUnit, reminderEnabled, reminderTime)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "更新设置失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    // 体重记录
    suspend fun addWeightRecord(date: String, weight: Float): ApiResult<WeightRecordData> {
        return try {
            val response = api.addWeightRecord(AddWeightRequest(date, weight))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "添加记录失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun getWeightRecords(days: Int? = null, startDate: String? = null, endDate: String? = null): ApiResult<WeightRecordsResponse> {
        return try {
            val response = api.getWeightRecords(startDate, endDate, days)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "获取记录失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun getWeightRecord(date: String): ApiResult<WeightRecordData> {
        return try {
            val response = api.getWeightRecord(date)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0 && body.data != null) {
                    ApiResult.Success(body.data)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "获取记录失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }

    suspend fun deleteWeightRecord(id: String): ApiResult<Unit> {
        return try {
            val response = api.deleteWeightRecord(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(body?.code ?: -1, body?.message ?: "删除记录失败")
                }
            } else {
                ApiResult.Error(-1, "网络请求失败")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "网络错误")
        }
    }
}
