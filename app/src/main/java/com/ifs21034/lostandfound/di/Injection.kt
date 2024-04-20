package com.ifs21034.lostandfound.di

import android.content.Context
import com.ifs21034.lostandfound.data.pref.UserPreference
import com.ifs21034.lostandfound.data.pref.dataStore
import com.ifs21034.lostandfound.data.remote.retrofit.ApiConfig
import com.ifs21034.lostandfound.data.remote.retrofit.IApiService
import com.ifs21034.lostandfound.data.repository.AuthRepository
import com.ifs21034.lostandfound.data.repository.LostFoundRepository
import com.ifs21034.lostandfound.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {

    fun provideAuthRepository(context: Context): AuthRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService: IApiService = ApiConfig.getApiService(user.token)
        return AuthRepository.getInstance(pref, apiService)
    }

    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService: IApiService = ApiConfig.getApiService(user.token)
        return UserRepository.getInstance(apiService)
    }

    fun provideTodoRepository(context: Context): LostFoundRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService: IApiService = ApiConfig.getApiService(user.token)
        return LostFoundRepository.getInstance(apiService)
    }

}