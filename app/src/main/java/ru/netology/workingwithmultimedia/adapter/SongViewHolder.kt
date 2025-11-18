package ru.netology.workingwithmultimedia.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.netology.workingwithmultimedia.databinding.VisionSongBinding
import ru.netology.workingwithmultimedia.dto.Song

class SongViewHolder(
    private val binding: VisionSongBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(song: Song) {
        with(binding) {
            titleText.text = song.title
            timeSong.text = song.time.toString()
            play.isChecked = song.play
            like.isChecked = song.liked
            share.isChecked = song.share

            play.setOnClickListener {
                menu.visibility = View.VISIBLE
                onInteractionListener.onPlay(song)
            }

            menu.setOnClickListener {
                groupMenu.visibility = View.VISIBLE
            }

            like.setOnClickListener {
                onInteractionListener.onLike(song)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(song)
            }
        }
    }
}