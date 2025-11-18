package ru.netology.workingwithmultimedia.adapter

import ru.netology.workingwithmultimedia.dto.Song

interface OnInteractionListener {
    fun onLike(song: Song)
    fun onShare(song: Song)
    fun onPlay(song: Song)
}