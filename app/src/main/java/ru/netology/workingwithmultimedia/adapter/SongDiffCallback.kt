package ru.netology.workingwithmultimedia.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.netology.workingwithmultimedia.dto.Song

class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id

    // Здесь сравниваем все видимые части
    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
        oldItem.currentPositionFormatted == newItem.currentPositionFormatted &&
                oldItem.title == newItem.title &&
                oldItem.play == newItem.play &&
                oldItem.liked == newItem.liked &&
                oldItem.share == newItem.share


    // Смотрим, что именно изменилось
    override fun getChangePayload(
        oldItem: Song,
        newItem: Song
    ): Any = Payload(
        currentPositionFormatted = newItem.currentPositionFormatted.takeIf {
            it != oldItem.currentPositionFormatted
        },
        liked = newItem.liked.takeIf { it != oldItem.liked },
        share = newItem.share.takeIf { it != oldItem.share },
        play = newItem.play.takeIf { it != oldItem.play },
    )

    // Частичное обновление. Если обновлений не произошло, то свойства будут null
    data class Payload(
        val currentPositionFormatted: String?,
        val liked: Boolean?,
        val share: Boolean?,
        val play: Boolean?,
    )
}