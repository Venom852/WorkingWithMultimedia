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
            timeSong.text = song.currentPositionFormatted
            play.isChecked = song.play
            like.isChecked = song.liked
            share.isChecked = song.share

            play.setOnClickListener {
                play.isChecked = !play.isChecked // Управляем состоянием исходя из данных в Song, а не по клику сразу
                menu.visibility = View.VISIBLE
                onInteractionListener.onPlay(song)
            }

            menu.setOnClickListener {
                groupMenu.visibility = View.VISIBLE
            }

            like.setOnClickListener {
                like.isChecked = !like.isChecked
                onInteractionListener.onLike(song)
            }

            share.setOnClickListener {
                share.isChecked = !share.isChecked
                onInteractionListener.onShare(song)
            }
        }
    }

    // Частичное обновление
    fun bind(payload: SongDiffCallback.Payload) {
        with(binding) {
            payload.currentPositionFormatted?.let { timeSong.text = it }
            payload.share?.let { share.isChecked = it }
            payload.play?.let { play.isChecked = it }
            payload.liked?.let { like.isChecked = it }
        }
    }
}