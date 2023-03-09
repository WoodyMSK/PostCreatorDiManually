package ru.netology.nmedia.data

import ru.netology.nmedia.model.LoggedInUser
import ru.netology.nmedia.model.RegisteredInUser
import java.io.IOException
import java.util.*
import java.util.UUID.randomUUID

class RegistrationDataSource {

    fun registration(firstname: String, username: String, password: String, confirmPassword: String): Result<RegisteredInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = RegisteredInUser(java.util.UUID.randomUUID().toString(), "Bob Jones")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error registering in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }

}