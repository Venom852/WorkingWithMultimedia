package ru.netology.workingwithmultimedia.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.netology.workingwithmultimedia.dto.Song

class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
}