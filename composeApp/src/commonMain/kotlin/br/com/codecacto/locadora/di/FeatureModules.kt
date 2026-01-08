package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.features.locacoes.presentation.LocacoesViewModel
import br.com.codecacto.locadora.features.locacoes.presentation.NovaLocacaoViewModel
import br.com.codecacto.locadora.features.locacoes.presentation.DetalhesLocacaoViewModel
import br.com.codecacto.locadora.features.entregas.presentation.EntregasViewModel
import br.com.codecacto.locadora.features.recebimentos.presentation.RecebimentosViewModel
import br.com.codecacto.locadora.features.clientes.presentation.ClientesViewModel
import br.com.codecacto.locadora.features.equipamentos.presentation.EquipamentosViewModel
import br.com.codecacto.locadora.features.settings.presentation.SettingsViewModel
import br.com.codecacto.locadora.features.settings.presentation.ChangePasswordViewModel
import br.com.codecacto.locadora.features.settings.presentation.ChangeEmailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val locacoesModule = module {
    viewModel { LocacoesViewModel(get(), get(), get(), get()) }
    viewModel { NovaLocacaoViewModel(get(), get(), get(), get()) }
    viewModel { params -> DetalhesLocacaoViewModel(params.get(), get(), get(), get(), get()) }
}

val entregasModule = module {
    viewModel { EntregasViewModel(get(), get(), get(), get()) }
}

val recebimentosModule = module {
    viewModel { RecebimentosViewModel(get(), get(), get(), get()) }
}

val clientesModule = module {
    viewModel { ClientesViewModel(get(), get()) }
}

val equipamentosModule = module {
    viewModel { EquipamentosViewModel(get(), get(), get()) }
}

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { ChangePasswordViewModel(get(), get()) }
    viewModel { ChangeEmailViewModel(get(), get()) }
}
