package ru.netology.nmedia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.netology.nmedia.repository.PostRepository

class RemovePostWorker(
    private val repository: PostRepository,
    applicationContext: Context,
    params: WorkerParameters
): CoroutineWorker(applicationContext, params) {
    companion object {
        const val removePostKey = "removePost"
}

    override suspend fun doWork(): Result {
        val id = inputData.getLong(removePostKey, 0L)
        if (id == 0L) {
            return Result.failure()
        }

        return try {
            repository.removeById(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}