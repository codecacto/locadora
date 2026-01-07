package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.ClienteRepositoryImpl
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepositoryImpl
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepositoryImpl
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.koin.dsl.module

val dataModule = module {
    // Firebase Firestore
    single { Firebase.firestore }

    // Repositories
    single<ClienteRepository> { ClienteRepositoryImpl(get()) }
    single<EquipamentoRepository> { EquipamentoRepositoryImpl(get()) }
    single<LocacaoRepository> { LocacaoRepositoryImpl(get()) }
}
