package com.example.myapplication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface CatsPhotoApi {
    @GET("v1/images/search?limit=10")
    suspend fun getCatPhotos(): List<CatsPhotoModel>

    @GET("cat/gif?json=true")
    suspend fun getCatGifs(): CatsGifModel
}


@Serializable
data class CatsPhotoModel(
    @SerialName("url") val url: String = ""
)

@Serializable
data class CatsGifModel(
    @SerialName("_id") val id: String = ""
)


