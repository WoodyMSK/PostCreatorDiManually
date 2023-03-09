package ru.netology.nmedia.data

import android.util.Log
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.LoggedInUser


class LoginRepository(
    private val auth: AppAuth,
    private val dataSource: LoginDataSource,
    private val service: ApiService,
) {

    //Выключил
//    @Inject
//    lateinit var service: ApiService

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    suspend fun login(username: String, password: String): AuthState {
        // handle login
        val response = service.updateUser(username, password)
        Log.d("LOG", "сообщение отправлено")
        if (!response.isSuccessful) {
            Log.d("LOG", "Возникла ошибка")
            throw ApiError(response.code(), response.message())
        }

        Log.d("LOG", "сообщение принято")
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        Log.d("LOG", "сообщение распаковано")
        auth.setAuth(body.id, body.token!!)
        Log.d("LOG", "авторизация установлена")
//        auth.setAuth(body.id, body.token!!)
        return body
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}