package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun getNewPosts()
//    suspend fun maxId(): Int

    suspend fun saveWork(post: Post, upload: MediaUpload?): Long
    suspend fun removeFromPostWorkDao(id: Long)
    suspend fun processWork(id: Long)
}

