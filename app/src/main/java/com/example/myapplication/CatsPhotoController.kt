package com.example.myapplication

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException

class CatsPhotoController(){

    private val api = Retrofit.Builder()
        .baseUrl("https://api.thecatapi.com/")
        .addConverterFactory(
            Json { ignoreUnknownKeys = true }
                .asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()
                )
        )
        .build()
        .create(CatsPhotoApi::class.java)

    suspend fun getPhoto(): Result<List<CatsPhotoModel>> {
        return try {
            val response = api.getCatPhotos()
            Result.success(response)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}