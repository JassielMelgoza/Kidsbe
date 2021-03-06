package com.ajcm.data.repository

import com.ajcm.data.models.Result
import com.ajcm.data.source.RemoteDataSource

class VideoRepository(
    val remoteDataSource: RemoteDataSource,
    private val apiKey: String
) {

    suspend fun search(byText: String): Result {
        return remoteDataSource.searchVideos(apiKey, byText)
    }

    suspend fun getList(relatedTo: String, attempts: Int): Result {
        return remoteDataSource.getPopularVideos(apiKey, relatedTo, attempts)
    }
}