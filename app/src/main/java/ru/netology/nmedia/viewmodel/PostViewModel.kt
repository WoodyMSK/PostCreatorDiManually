package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.work.RemovePostWorker
import ru.netology.nmedia.work.SavePostWorker
import java.io.IOException

private val empty = Post(
    id = 0,
    content = "",
    authorId = 0,
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0,
)

private val noPhoto = PhotoModel()

@ExperimentalCoroutinesApi
class PostViewModel(
    private val repository: PostRepository,
    private val workManager: WorkManager,
    private val auth: AppAuth
) : ViewModel() {

    val data: LiveData<FeedModel> = auth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            repository.getNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getNewPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    // Версия без воркера

//    fun save() {
//        edited.value?.let {
//            _postCreated.value = Unit
//            viewModelScope.launch {
//                try {
//                    when(_photo.value) {
//                        noPhoto -> repository.save(it)
//                        else -> _photo.value?.file?.let { file ->
//                            repository.saveWithAttachment(it, MediaUpload(file))
//                        }
//                    }
//                    _dataState.value = FeedModelState()
//                } catch (e: Exception) {
//                    _dataState.value = FeedModelState(error = true)
//                }
//            }
//        }
//        edited.value = empty
//        _photo.value = noPhoto
//    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(it, _photo.value?.uri?.let { MediaUpload(it.toFile()) } )

                    val data = workDataOf(SavePostWorker.postKey to id)

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = OneTimeWorkRequestBuilder<SavePostWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueue(request)

                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun removeById(id: Long) = viewModelScope.launch {
        val old = data.value?.posts.orEmpty()
        val posts = data.value?.posts.orEmpty().filter { it.id != id }
        data.value?.copy(posts = posts, empty = posts.isEmpty())

        try {
            val data = workDataOf(RemovePostWorker.removePostKey to id)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<RemovePostWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)

            _dataState.value = FeedModelState()
        } catch (e: IOException) {
            data.value?.copy(posts = old)
            _dataState.value = FeedModelState(error = true)
        }
    }

//     Версия без воркера

//    fun removeById(id: Long) = viewModelScope.launch {
//        val old = data.value?.posts.orEmpty()
//        val posts = data.value?.posts.orEmpty().filter { it.id != id }
//        data.value?.copy(posts = posts, empty = posts.isEmpty())
//
//        try {
//            _dataState.value = FeedModelState(loading = true)
//            repository.removeById(id)
//            _dataState.value = FeedModelState()
//        } catch (e: IOException) {
//            data.value?.copy(posts = old)
//            _dataState.value = FeedModelState(error = true)
//        }
//    }

    // Версия без воркера

//    fun removeById(id: Long) = viewModelScope.launch {
//        val old = data.value?.posts.orEmpty()
//        val posts = data.value?.posts.orEmpty().filter { it.id != id }
//        data.value?.copy(posts = posts, empty = posts.isEmpty())
//
//        try {
//            _dataState.value = FeedModelState(loading = true)
//            repository.removeById(id)
//            _dataState.value = FeedModelState()
//        } catch (e: IOException) {
//            data.value?.copy(posts = old)
//            _dataState.value = FeedModelState(error = true)
//        }
//    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun likeById(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.likeById(post)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }





}
