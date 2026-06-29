package com.example.moviehubpro.data

import android.content.Context
import androidx.room.Room
import com.example.moviehubpro.data.api.ApiService
import com.example.moviehubpro.data.db.AppDatabase
import com.example.moviehubpro.data.repository.MovieRepo
import com.example.moviehubpro.data.datastore.AppDataStore
import com.example.moviehubpro.util.Constants
import com.example.moviehubpro.util.NetworkMonitor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME
    )
    .fallbackToDestructiveMigration() // 极其重要：防止表结构变化导致的崩溃
    .build()

    val movieRepo = MovieRepo(apiService, db.movieDao())
    val appDataStore = AppDataStore(context)
    val networkMonitor = NetworkMonitor(context)
}
