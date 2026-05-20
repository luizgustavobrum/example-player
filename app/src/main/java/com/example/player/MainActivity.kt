package com.example.player

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.ui.PlayerView
import com.lgsb.player.Player

class MainActivity : AppCompatActivity() {

    private var player: Player? = null

    private val playerView: PlayerView by lazy { findViewById(R.id.player_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        player = Player(
            this,
            playerView,
            Player.Video.SSAI.url,
            Player.Video.SSAI.adTag,
            Player.Video.SSAI.adMode,
            listener = object : Player.Listener {
                override fun onEvent(event: String) {
                    Log.d("PlayerEvent", event)
                }
            })

        player?.load()
        player?.let { p -> playerView.player = p.getPlayer() }
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}

