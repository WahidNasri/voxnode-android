package org.voxnode.voxnode.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api3.voxnode.com/" // Replace with your actual API base URL
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiInterface by lazy {
        retrofit.create(ApiInterface::class.java)
    }

    /**
     * Update the base URL if needed
     */
    fun createApiService(baseUrl: String): ApiInterface {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}
