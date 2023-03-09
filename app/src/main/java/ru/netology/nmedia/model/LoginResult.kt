package ru.netology.nmedia.model

data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)