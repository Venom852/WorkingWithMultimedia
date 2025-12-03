package ru.netology.workingwithmultimedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netology.workingwithmultimedia.databinding.VisionSongBinding
import ru.netology.workingwithmultimedia.dto.Song

class SongAdapter (
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Song, SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = VisionSongBinding.inflate(layoutInflater, parent, false)
        return SongViewHolder(binding, onInteractionListener)
    }

    // Либо полное, либо частичное обновление
    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        payloads: List<Any?>,
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach {
                if (it is SongDiffCallback.Payload) {
                    holder.bind(it)
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}