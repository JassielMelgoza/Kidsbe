package com.ajcm.kidstube.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import com.ajcm.data.source.LocalDataSource
import com.ajcm.domain.User
import com.ajcm.domain.Video
import com.ajcm.kidstube.R
import com.ajcm.kidstube.arch.ActionState
import com.ajcm.kidstube.arch.ScopedViewModel
import com.ajcm.kidstube.common.DashNav
import com.ajcm.kidstube.common.VideoAction
import com.ajcm.kidstube.extensions.delete
import com.ajcm.kidstube.extensions.getPositionOf
import com.ajcm.kidstube.ui.main.SongTrackListener
import com.ajcm.usecases.GetYoutubeVideos
import com.ajcm.usecases.VideoWatched
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getYoutubeVideos: GetYoutubeVideos,
    private val videoWatched: VideoWatched,
    private val localDB: LocalDataSource<User>,
    uiDispatcher: CoroutineDispatcher
) : ScopedViewModel<UiDashboard>(uiDispatcher) {

    override val model: LiveData<UiDashboard>
        get() {
            if (mModel.value == null) dispatch(ActionDashboard.Start)
            return mModel
        }

    var videos: List<Video> = emptyList()

    init {
        initScope()
    }

    override fun dispatch(actionState: ActionState) {
        println("DashboardViewModel.dispatch --> $actionState")
        when (actionState) {
            ActionDashboard.Start -> {
                launch {
                    consume(UiDashboard.UpdateUserInfo(localDB.getObject().userAvatar))
                }
            }
            ActionDashboard.StartYoutube -> {
                launch {
                    getYoutubeVideos.startYoutubeWith(localDB.getObject().userName)
                    consume(UiDashboard.YoutubeStarted)
                }
            }
            ActionDashboard.Refresh -> {
                launch {
                    if (videos.isEmpty()) {
                        refresh(localDB.getObject().lastVideoWatched)
                    } else {
                        consume(UiDashboard.Content(videos))
                    }
                }
            }
            is ActionDashboard.ParseException -> {
                val msg = parseError(actionState.exception)
                consume(UiDashboard.LoadingError(msg))
            }
            is ActionDashboard.LoadError -> {
                consume(UiDashboard.LoadingError(actionState.msg))
            }
            is ActionDashboard.VideoSelected -> {
                launch {
                    when (val act = actionState.videoAction) {
                        is VideoAction.Play -> {
                            consume(UiDashboard.NavigateTo(DashNav.VIDEO, act.video, localDB.getObject().userId))
                        }
                        is VideoAction.Block -> {
                            launch {
                                videoWatched.addToBlackList(act.video.title)
                            }
                            videos = videos.delete(act.video)
                            consume(UiDashboard.Content(videos))
                        }
                        is VideoAction.RelatedTo -> {
                            refresh(act.video.videoId)
                        }
                    }
                }
            }
            is ActionDashboard.ChangeRoot -> {
                consume(UiDashboard.NavigateTo(actionState.root, null, ""))
            }
        }
    }

    private fun parseError(exception: Exception?): String {
        Log.e("DashboardViewModel", exception?.toString() ?: "Unspecific error")
        return when(exception) {
            is GoogleJsonResponseException -> {
                exception.details.message
            }
            else -> "Ocurrió un error al procesar la solicitud. Intenta de nuevo!"
        }
    }

    private fun refresh(videoId: String) = launch {
        consume(UiDashboard.Loading)
        val result = getYoutubeVideos.invoke(videoId, attempts = 1)
        if (result.videos.isNotEmpty()) {
            val videoIndex = videos.getPositionOf(videoId)

            videos = if (videoIndex != -1 && (videoIndex - 1) < videos.size) {
                val tempVideos = videos.toMutableList()
                tempVideos.addAll(videoIndex, result.videos)
                tempVideos.toList()
            } else {
                (videos + result.videos)
            }

            consume(UiDashboard.Content(videos.shuffled()))
        } else {
            consume(UiDashboard.RequestPermissions(result.exception))
        }
    }

    fun onResume(songTrackListener: SongTrackListener?) = launch {
        songTrackListener?.enableSong(true)
        songTrackListener?.onResumeSong()
    }

    fun onPlaySong(songTrackListener: SongTrackListener?) = launch {
        songTrackListener?.onPlaySong(R.raw.dashboard_ben_smile)
    }

}