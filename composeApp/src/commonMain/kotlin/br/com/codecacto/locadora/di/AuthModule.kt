package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepositoryImpl
import br.com.codecacto.locadora.features.auth.presentation.login.LoginViewModel
import br.com.codecacto.locadora.features.auth.presentation.register.RegisterViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    // Firebase Auth
    single { Firebase.auth }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
}
