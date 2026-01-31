package com.kaixuan.weightloss.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaixuan.weightloss.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://okeng.top:8080/api/"

    private var apiService: ApiService? = null
    private var context: Context? = null

    object TokenKeys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
    }

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getService(): ApiService {
        if (apiService == null) {
            val ctx = context ?: throw IllegalStateException("ApiClient not initialized. Call init() first.")

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val token = runBlocking {
                    ctx.dataStore.data.map { it[TokenKeys.TOKEN] }.first()
                }

                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()
                } else {
                    chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .build()
                }
                chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }

    suspend fun saveAuthData(authData: AuthData) {
        val ctx = context ?: return
        ctx.dataStore.edit { prefs ->
            prefs[TokenKeys.TOKEN] = authData.token
            prefs[TokenKeys.USER_ID] = authData.userId
            prefs[TokenKeys.USERNAME] = authData.username
        }
    }

    suspend fun updateToken(token: String) {
        val ctx = context ?: return
        ctx.dataStore.edit { prefs ->
            prefs[TokenKeys.TOKEN] = token
        }
    }

    suspend fun clearAuthData() {
        val ctx = context ?: return
        ctx.dataStore.edit { prefs ->
            prefs.remove(TokenKeys.TOKEN)
            prefs.remove(TokenKeys.USER_ID)
            prefs.remove(TokenKeys.USERNAME)
        }
    }

    suspend fun getToken(): String? {
        val ctx = context ?: return null
        return ctx.dataStore.data.map { it[TokenKeys.TOKEN] }.first()
    }

    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    suspend fun getUsername(): String? {
        val ctx = context ?: return null
        return ctx.dataStore.data.map { it[TokenKeys.USERNAME] }.first()
    }
}
