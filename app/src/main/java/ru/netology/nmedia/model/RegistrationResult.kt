package ru.netology.nmedia.model

data class RegistrationResult (
    val success: RegisteredInUserView? = null,
    val error: Int? = null
        )