package ru.netology.nmedia.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.data.RegistrationRepository
import ru.netology.nmedia.model.LoginFormState
import ru.netology.nmedia.model.RegisteredInUserView
import ru.netology.nmedia.model.RegistrationFormState
import ru.netology.nmedia.model.RegistrationResult

class RegistrationViewModel(private val registrationRepository: RegistrationRepository): ViewModel() {

    private val _registrationForm = MutableLiveData<RegistrationFormState>()
    val RegistrationFormState: LiveData<RegistrationFormState> = _registrationForm

    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> = _registrationResult

    fun registration(
        firstname: String,
        username: String,
        password: String
    ) = viewModelScope.launch {
        try {
            val result = registrationRepository.registration(firstname, username, password)
            _registrationResult.value =
                RegistrationResult(success = RegisteredInUserView(displayName = "{$result.id}"))
        } catch (e: Exception) {
            _registrationResult.value =
                RegistrationResult(error = R.string.registration_failed)
        }
    }

    fun registrationDataChecked(username: String, password: String, confirmPassword: String) {
        if (!isLoginValid(username)) {
            _registrationForm.value = RegistrationFormState(loginError = R.string.invalid_username)
        } else if (!isPasswordValid(password, confirmPassword)) {
            _registrationForm.value = RegistrationFormState(confirmPasswordError = R.string.confirm_password_error)
        } else {
            _registrationForm.value = RegistrationFormState(isRegistrationDataValid = true)
        }
    }

    private fun isLoginValid(username: String): Boolean {
        return username.length > 3
    }

    private fun isPasswordValid(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}


