package com.ajcm.data.api

import android.app.Application
import com.ajcm.data.common.Constants
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube

class GoogleSession(application: Application, accountName: String) {

    private val transport = NetHttpTransport()
    private val jsonFactory: JsonFactory = GsonFactory()
    private var credential: GoogleAccountCredential? = null

    init {
        credential = GoogleAccountCredential
            .usingOAuth2(application.applicationContext, Constants.SCOPES)
            .setBackOff(ExponentialBackOff())

        credential?.selectedAccountName = accountName
    }

    fun getYoutubeSession(): YouTube {
        return YouTube.Builder(transport, jsonFactory, credential)
            .setApplicationName(Constants.APP_NAME)
            .build()
    }

}