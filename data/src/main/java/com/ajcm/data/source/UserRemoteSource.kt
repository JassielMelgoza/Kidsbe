package com.ajcm.data.source

import com.ajcm.data.api.FirebaseApi
import com.ajcm.data.mappers.toUser
import com.ajcm.domain.User
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class UserRemoteSource(private val api: FirebaseApi): FireBaseDataSource<User?> {

    override suspend fun searchIn(document: String, byReference: String): User? = suspendCancellableCoroutine { continuation ->
        api.db
            .collection(api.url)
            .whereEqualTo(document, byReference)
            .limit(1)
            .get()
            .addOnSuccessListener {
                val user = it.documents.firstOrNull()
                if (user ==null) {
                    continuation.resume(null)
                } else {
                    continuation.resume(user.toUser())
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }
    }

    override suspend fun save(value: User?): String = suspendCancellableCoroutine { continuation ->
        if (value == null) continuation.resume("")
        api.db
            .collection(api.url)
            .add(mapOf(
                "userName" to value!!.userName,
                "userAvatar" to value.userAvatar.name,
                "lastVideoWatched" to value.lastVideoWatched,
                "appEffect" to value.appEffect,
                "soundEffect" to value.soundEffect,
                "allowMobileData" to value.allowMobileData
            ))
            .addOnCompleteListener {
                continuation.resume(it.result?.id ?: "")
            }
    }

    override suspend fun update(value: User?): Boolean = suspendCancellableCoroutine { continuation ->
        if (value == null) continuation.resume(false)
        api.db
            .collection(api.url)
            .document(value!!.userId)
            .update(mapOf(
                "userName" to value.userName,
                "userAvatar" to value.userAvatar.name,
                "lastVideoWatched" to value.lastVideoWatched,
                "appEffect" to value.appEffect,
                "soundEffect" to value.soundEffect,
                "allowMobileData" to value.allowMobileData
            ))
            .addOnCompleteListener {
                continuation.resume(it.isSuccessful && it.exception == null)
            }
    }

    override suspend fun getList(): List<User?> {
        return emptyList()
    }
}