package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.data.LoginRepository
import ru.netology.nmedia.data.RegistrationRepository
import ru.netology.nmedia.repository.PostRepository

@ExperimentalCoroutinesApi
class ViewModelFactory(
    private val repository: PostRepository,
    private val loginRepository: LoginRepository,
    private val registrationRepository: RegistrationRepository,
    private val workManager: WorkManager,
    private val auth: AppAuth,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PostViewModel(repository, workManager, auth) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(auth) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LoginViewModel(loginRepository) as T
            }
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                RegistrationViewModel(registrationRepository) as T
            }
            else -> error("Unknown view model class ${modelClass.name}")
        }
}
