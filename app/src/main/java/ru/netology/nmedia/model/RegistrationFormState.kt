package ru.netology.nmedia.model

data class RegistrationFormState(
    val loginError: Int? = null,
    val confirmPasswordError: Int? = null,
    val isRegistrationDataValid: Boolean = false
)