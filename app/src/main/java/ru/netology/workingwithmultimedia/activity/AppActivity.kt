package ru.netology.workingwithmultimedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.workingwithmultimedia.R
import ru.netology.workingwithmultimedia.adapter.OnInteractionListener
import ru.netology.workingwithmultimedia.adapter.SongAdapter
import ru.netology.workingwithmultimedia.databinding.ActivityMainBinding
import ru.netology.workingwithmultimedia.dto.Song
import ru.netology.workingwithmultimedia.viewModel.SongViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
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
                    putExtra(Intent.EXTRA_TEXT, song.url) // Делиться объектом в формате json наверное не очень интересно. Лучше ссылкой
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_song))
                startActivity(chooser)
                viewModel.share(song.id)
            }

            override fun onPlay(song: Song) {
                viewModel.play(song.id)
            }
        })

        binding.main.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest { songs ->
                    adapter.submitList(songs)
                    binding.play.isChecked = songs.any { it.play }
                }
            }
        }

        binding.play.setOnClickListener {
            viewModel.playBig()
            binding.play.isChecked = !binding.play.isChecked
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