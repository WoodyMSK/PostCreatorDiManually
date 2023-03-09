package ru.netology.nmedia.data

import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.RegisteredInUser

class RegistrationRepository(
    private val auth: AppAuth,
    private val dataSource: RegistrationDataSource,
    private val service: ApiService,
) {

    //Выключил
//    @Inject
//    lateinit var auth: AppAuth
//
//    @Inject
//    lateinit var service: ApiService
    // in-memory cache of the loggedInUser object
    var user: RegisteredInUser? = null
        private set

//    val isLoggedIn: Boolean
//        get() = user != null

    init {
        user = null
    }

    suspend fun registration(firstname: String, username: String, password: String): AuthState {
        val response = service.registerUser(username, password, firstname)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val body = response.body() ?: throw ApiError(response.code(), response.message())
        auth.setAuth(body.id, body.token!!)

        return body
    }





}