package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.ClienteRepositoryImpl
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepositoryImpl
import br.com.codecacto.locadora.data.repository.FeedbackRepository
import br.com.codecacto.locadora.data.repository.FeedbackRepositoryImpl
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepositoryImpl
import br.com.codecacto.locadora.data.repository.NotificacaoRepository
import br.com.codecacto.locadora.data.repository.NotificacaoRepositoryImpl
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.koin.dsl.module

val dataModule = module {
    // Firebase Firestore
    single { Firebase.firestore }

    // Repositories
    single<ClienteRepository> { ClienteRepositoryImpl(get(), get()) }
    single<EquipamentoRepository> { EquipamentoRepositoryImpl(get(), get()) }
    single<LocacaoRepository> { LocacaoRepositoryImpl(get(), get()) }
    single<FeedbackRepository> { FeedbackRepositoryImpl(get(), get()) }
    single<NotificacaoRepository> { NotificacaoRepositoryImpl(get(), get()) }
}
