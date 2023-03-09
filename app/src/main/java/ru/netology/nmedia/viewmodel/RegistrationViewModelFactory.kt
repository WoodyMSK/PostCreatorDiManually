package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.data.LoginDataSource
import ru.netology.nmedia.data.LoginRepository
import ru.netology.nmedia.data.RegistrationDataSource
import ru.netology.nmedia.data.RegistrationRepository

//class RegistrationViewModelFactory: ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
//            return RegistrationViewModel(
//                registrationRepository = RegistrationRepository(
//                    dataSource = RegistrationDataSource()
//                )
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}