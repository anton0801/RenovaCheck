package com.apprenova.renovacheck.data.domain.data

import android.util.Log
import com.apprenova.renovacheck.MainApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RRPushTokenUC {

    suspend fun batchManagerGetToken(): String = suspendCoroutine { continuation ->
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (!it.isSuccessful) {
                    continuation.resume(it.result)
                    Log.d(MainApplication.MAIN_TAG, "Token error: ${it.exception}")
                } else {
                    continuation.resume(it.result)
                }
            }
        } catch (e: Exception) {
            Log.d(MainApplication.MAIN_TAG, "FirebaseMessagingPushToken = null")
            continuation.resume("")
        }
    }


}