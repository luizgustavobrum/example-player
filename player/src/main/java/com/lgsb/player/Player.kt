package com.lgsb.player

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.ima.ImaServerSideAdInsertionMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView

class Player(
    context: Context,
    private val playerView: PlayerView,
    private val url: String,
    private val adTag: String? = null,
    private val adMode: AdMode = AdMode.NONE,
    private val listener: Listener? = null
) {

    private var player: ExoPlayer? = null

    /**
     * CSAI
     */
    private var clientSideAdsLoader: ImaAdsLoader? = null

    /**
     * SSAI
     */
    private var serverSideAdsLoader:
            ImaServerSideAdInsertionMediaSource.AdsLoader? = null

    private var mediaSourceFactory: MediaSource.Factory? = null

    init {
        clientSideAdsLoader =
            ImaAdsLoader.Builder(context)
                .build()

        serverSideAdsLoader =
            ImaServerSideAdInsertionMediaSource
                .AdsLoader
                .Builder(
                    context,
                    playerView
                )
                .build()

        val serverSideMediaSourceFactory =
            ImaServerSideAdInsertionMediaSource.Factory(
                serverSideAdsLoader!!,
                DefaultMediaSourceFactory(context)
            )

        mediaSourceFactory =
            DefaultMediaSourceFactory(context)
                .setLocalAdInsertionComponents(
                    { clientSideAdsLoader },
                    playerView
                )
                .setServerSideAdInsertionMediaSourceFactory(
                    serverSideMediaSourceFactory
                )

        player =
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    mediaSourceFactory!!
                )
                .build()

        playerView.player = player
        serverSideAdsLoader?.setPlayer(player!!)
    }

    fun load() {
        val currentPlayer =
            player ?: run {
                listener?.onEvent("Player is null")
                return
            }

        addListener(currentPlayer)

        val mediaItem =
            MediaItem.Builder()
                .setUri(url)
                .apply {
                    if (
                        adMode == AdMode.CSAI &&
                        !adTag.isNullOrEmpty()
                    ) {
                        setAdsConfiguration(
                            MediaItem.AdsConfiguration
                                .Builder(adTag.toUri())
                                .build()
                        )
                    }
                }
                .build()

        currentPlayer.setMediaItem(mediaItem)
        clientSideAdsLoader?.setPlayer(currentPlayer)
        currentPlayer.prepare()
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        player?.stop()
    }

    fun release() {
        clientSideAdsLoader?.setPlayer(null)
        clientSideAdsLoader?.release()
        clientSideAdsLoader = null

        serverSideAdsLoader?.release()
        serverSideAdsLoader = null

        playerView.player = null

        player?.release()
        player = null
    }

    fun getPlayer(): ExoPlayer {
        return requireNotNull(player)
    }

    private fun addListener(
        player: ExoPlayer
    ) {

        player.addListener(
            object : Player.Listener {

                override fun onEvents(
                    player: Player,
                    events: Player.Events
                ) {
                    super.onEvents(player, events)

                    listener?.onEvent(
                        "Player: $player - Events: $events"
                    )
                }

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {
                    super.onIsPlayingChanged(isPlaying)

                    listener?.onEvent(
                        if (isPlaying) {
                            "Playing"
                        } else {
                            "Paused"
                        }
                    )
                }

                override fun onPlayerError(
                    error: PlaybackException
                ) {
                    super.onPlayerError(error)

                    listener?.onEvent(
                        "Error: ${error.message}"
                    )
                }

                override fun onPlaybackStateChanged(
                    state: Int
                ) {
                    super.onPlaybackStateChanged(state)

                    when (state) {

                        Player.STATE_IDLE -> {
                            listener?.onEvent("Idle")
                        }

                        Player.STATE_BUFFERING -> {
                            listener?.onEvent("Buffering")
                        }

                        Player.STATE_READY -> {
                            listener?.onEvent("Ready")
                        }

                        Player.STATE_ENDED -> {
                            listener?.onEvent("Ended")
                        }
                    }
                }
            }
        )
    }

    interface Listener {
        fun onEvent(event: String)
    }

    enum class AdMode {
        NONE,
        CSAI,
        SSAI
    }

    enum class Video(
        val url: String,
        val adTag: String? = null,
        val adMode: AdMode = AdMode.NONE
    ) {

        HLS(
            url =
                "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"
        ),

        DASH(
            url =
                "https://storage.googleapis.com/exoplayer-test-media-1/60fps/bbb-clear-1080/manifest.mpd"
        ),

        CSAI(
            url =
                "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",

            adTag =
                "https://pubads.g.doubleclick.net/gampad/ads?" +
                        "sz=640x480&" +
                        "iu=/124319096/external/ad_rule_samples&" +
                        "ciu_szs=300x250&" +
                        "ad_rule=1&" +
                        "impl=s&" +
                        "gdfp_req=1&" +
                        "env=vp&" +
                        "output=vmap&" +
                        "unviewed_position_start=1&" +
                        "cust_params=deployment%3Ddevsite%26sample_ar%3Dpreonly&" +
                        "cmsid=496&" +
                        "vid=short_onecue&" +
                        "correlator=",

            adMode = AdMode.CSAI
        ),
        SSAI(
            url =
                "ssai://dai.google.com/" +
                        "?contentSourceId=2559737" +
                        "&videoId=tos-dash" +
                        "&format=0" +
                        "&adsId=1",

            adMode = AdMode.SSAI
        )
    }
}