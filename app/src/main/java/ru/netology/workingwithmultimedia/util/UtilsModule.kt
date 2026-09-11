package ru.netology.workingwithmultimedia.util

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
interface UtilsModule {
    @Binds
    fun bindMediaPlayerMediaService(impl: MediaPlayerMediaService): MediaService
}
