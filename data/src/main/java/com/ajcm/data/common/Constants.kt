package com.ajcm.data.common

import com.google.android.gms.common.Scopes
import com.google.api.services.youtube.YouTubeScopes

object Constants {

    val SCOPES = arrayListOf(Scopes.PROFILE, YouTubeScopes.YOUTUBE)
    const val APP_NAME = "KidsTube"

    const val DEFAULT_VIDEO_COUNT: Long = 50
    const val DEFAULT_LAST_VIDEO_ID = "n0X3KG9D0rM"

}