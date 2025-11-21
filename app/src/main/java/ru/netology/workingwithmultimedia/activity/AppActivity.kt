package ru.netology.workingwithmultimedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.workingwithmultimedia.lifecycle.MediaLifecycleObserver
import ru.netology.workingwithmultimedia.R
import ru.netology.workingwithmultimedia.adapter.SongAdapter
import ru.netology.workingwithmultimedia.databinding.ActivityMainBinding
import ru.netology.workingwithmultimedia.adapter.OnInteractionListener
import ru.netology.workingwithmultimedia.dto.Song
import kotlin.getValue
import ru.netology.workingwithmultimedia.viewModel.SongViewModel
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.media.session.MediaController

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_main) {
    private val mediaObserver = MediaLifecycleObserver()
    private val gson = Gson()

    companion object {
        var isPaused = false
        var checked = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val viewModel: SongViewModel by viewModels()

        enableEdgeToEdge()
        setContentView(binding.root)
        applyInset(binding.root)

        val adapter = SongAdapter(object : OnInteractionListener {
            override fun onLike(song: Song) {
                viewModel.like(song.id)
            }

            override fun onShare(song: Song) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, gson.toJson(song))
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_song))
                startActivity(chooser)
                viewModel.share(song.id)
            }

            override fun onPlay(song: Song) {
//                mediaController = MediaController(this@AppActivity, mediaController.sessionToken)
                viewModel.play(song.id)
//                viewModel.playSong(song.id)
            }
        })

        binding.main.adapter = adapter
        lifecycle.addObserver(mediaObserver)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest {
                    adapter.submitList(it)
                }
            }
        }

        binding.play.setOnClickListener {
            lifecycleScope.launch {
                binding.play.addOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && !isPaused) {
                        viewModel.pauseSong()
                        isPaused = true
                    }
                    binding.play.isChecked = !isChecked
                    checked = !isChecked
                }

//                mediaController = MediaController(this@AppActivity, mediaController.sessionToken)

                if (viewModel.isEmpty()) {
                    viewModel.saveSongs()
                } else {
                    viewModel.playSong()
                }
            }
        }
    }

    private fun applyInset(main: View) {
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                if (isImeVisible) imeInsets.bottom else systemBars.bottom
            )
            insets
        }
    }
}