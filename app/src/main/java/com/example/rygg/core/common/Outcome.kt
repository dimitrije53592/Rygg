package com.example.rygg.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface Outcome<out T> {
    data object Loading : Outcome<Nothing>

    data class Success<out T>(val data: T) : Outcome<T>

    data class Error(val cause: Throwable) : Outcome<Nothing>
}

// Wraps a data stream so collectors get Loading first, then Success or Error.
fun <T> Flow<T>.asResult(): Flow<Outcome<T>> =
    map<T, Outcome<T>> { Outcome.Success(it) }
        .onStart { emit(Outcome.Loading) }
        .catch { emit(Outcome.Error(it)) }

// Runs a suspend block and captures success/failure as an Outcome (rethrows cancellation).
suspend fun <T> outcomeCatching(block: suspend () -> T): Outcome<T> =
    try {
        Outcome.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Outcome.Error(e)
    }
