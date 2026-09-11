package ru.netology.workingwithmultimedia.util

import kotlinx.coroutines.flow.Flow
import java.io.Closeable

/**
 * Информация о проигрываемом треке
 */
data class Position(
    val url: String,
    val positionMillis: Long
)

/**
 * Абстракция, чтобы не работать с конкретным плеером внутри ViewModel
 *
 * Это даёт
 * 1. Возможность написать Unit тесты на ViewModel
 * 2. Лёгкую замену разных плееров без необходимости менять ViewModel
 * 3. Соблюдение принципа единой ответственности
 */
interface MediaService : Closeable {
    val currentPosition: Flow<Position> // Подписка на проигрываемую композицию
    fun play(url: String, positionMillis: Long) // начать проигрывание с отступом в positionMillis
    fun stop() // Остановить проигрывание
    fun setOnCompleteListener(action: () -> Unit) // Слушатель конца трека
}
