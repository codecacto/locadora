package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.ui.util.TipoPessoa
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Testes unitários para todos os Settings Contracts.
 */
class SettingsContractsTest {

    // ==================== TESTES DE SettingsContract (Main) ====================

    @Test
    fun `Settings State inicial deve ter valores padrao`() {
        val state = SettingsContract.State()

        assertFalse(state.isLoading)
        assertFalse(state.isDeletingAllData)
        assertEquals("", state.currentEmail)
    }

    @Test
    fun `Settings State com email preenchido`() {
        val state = SettingsContract.State(
            currentEmail = "usuario@exemplo.com"
        )

        assertEquals("usuario@exemplo.com", state.currentEmail)
    }

    @Test
    fun `Settings State durante exclusao de dados`() {
        val state = SettingsContract.State(
            isDeletingAllData = true
        )

        assertTrue(state.isDeletingAllData)
    }

    @Test
    fun `Settings Action DeleteAllData deve existir`() {
        assertTrue(SettingsContract.Action.DeleteAllData is SettingsContract.Action)
    }

    @Test
    fun `Settings Effect ShowSuccess deve conter mensagem correta`() {
        val effect = SettingsContract.Effect.ShowSuccess("Todos os dados foram apagados com sucesso!")
        assertEquals("Todos os dados foram apagados com sucesso!", effect.message)
    }

    @Test
    fun `Settings Effect ShowError deve conter mensagem correta`() {
        val effect = SettingsContract.Effect.ShowError("Erro ao apagar dados")
        assertEquals("Erro ao apagar dados", effect.message)
    }

    // ==================== CENARIOS DE NEGOCIO - Apagar Todos os Dados ====================

    @Test
    fun `Cenario - fluxo de apagar todos os dados`() {
        var state = SettingsContract.State(
            currentEmail = "usuario@email.com"
        )

        // Estado inicial
        assertFalse(state.isDeletingAllData)
        assertEquals("usuario@email.com", state.currentEmail)

        // Inicia exclusão
        state = state.copy(isDeletingAllData = true)
        assertTrue(state.isDeletingAllData)

        // Finaliza exclusão com sucesso
        state = state.copy(isDeletingAllData = false)
        assertFalse(state.isDeletingAllData)
    }

    @Test
    fun `Cenario - erro durante exclusao de dados`() {
        var state = SettingsContract.State()

        // Inicia exclusão
        state = state.copy(isDeletingAllData = true)
        assertTrue(state.isDeletingAllData)

        // Simula erro - volta para estado não deletando
        state = state.copy(isDeletingAllData = false)
        assertFalse(state.isDeletingAllData)
    }

    @Test
    fun `State copy deve manter isDeletingAllData independente de outros campos`() {
        val initial = SettingsContract.State(
            isLoading = false,
            isDeletingAllData = true,
            currentEmail = "test@test.com"
        )

        val updated = initial.copy(currentEmail = "novo@email.com")

        assertTrue(updated.isDeletingAllData)
        assertEquals("novo@email.com", updated.currentEmail)
    }

    // ==================== TESTES DE ChangePasswordContract ====================

    @Test
    fun `ChangePassword State inicial deve ter valores padrao`() {
        val state = ChangePasswordContract.State()

        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.showCurrentPassword)
        assertFalse(state.showNewPassword)
        assertFalse(state.showConfirmPassword)
        assertNull(state.currentPasswordError)
        assertNull(state.newPasswordError)
        assertNull(state.confirmPasswordError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `ChangePassword State com senhas preenchidas`() {
        val state = ChangePasswordContract.State(
            currentPassword = "senhaAtual",
            newPassword = "novaSenha123",
            confirmPassword = "novaSenha123"
        )

        assertEquals("senhaAtual", state.currentPassword)
        assertEquals("novaSenha123", state.newPassword)
        assertEquals("novaSenha123", state.confirmPassword)
    }

    @Test
    fun `ChangePassword State com erros de validacao`() {
        val state = ChangePasswordContract.State(
            currentPasswordError = "Senha atual incorreta",
            newPasswordError = "Senha muito curta",
            confirmPasswordError = "Senhas não conferem"
        )

        assertEquals("Senha atual incorreta", state.currentPasswordError)
        assertEquals("Senha muito curta", state.newPasswordError)
        assertEquals("Senhas não conferem", state.confirmPasswordError)
    }

    @Test
    fun `ChangePassword Actions devem conter valores corretos`() {
        val setCurrentPassword = ChangePasswordContract.Action.SetCurrentPassword("atual")
        val setNewPassword = ChangePasswordContract.Action.SetNewPassword("nova")
        val setConfirmPassword = ChangePasswordContract.Action.SetConfirmPassword("confirma")

        assertEquals("atual", setCurrentPassword.password)
        assertEquals("nova", setNewPassword.password)
        assertEquals("confirma", setConfirmPassword.password)
    }

    @Test
    fun `ChangePassword Toggle actions devem existir`() {
        assertTrue(ChangePasswordContract.Action.ToggleCurrentPasswordVisibility is ChangePasswordContract.Action)
        assertTrue(ChangePasswordContract.Action.ToggleNewPasswordVisibility is ChangePasswordContract.Action)
        assertTrue(ChangePasswordContract.Action.ToggleConfirmPasswordVisibility is ChangePasswordContract.Action)
        assertTrue(ChangePasswordContract.Action.Submit is ChangePasswordContract.Action)
    }

    @Test
    fun `ChangePassword Effects devem conter valores corretos`() {
        assertTrue(ChangePasswordContract.Effect.NavigateBack is ChangePasswordContract.Effect)

        val success = ChangePasswordContract.Effect.ShowSuccess("Senha alterada!")
        assertEquals("Senha alterada!", success.message)

        val error = ChangePasswordContract.Effect.ShowError("Erro ao alterar")
        assertEquals("Erro ao alterar", error.message)
    }

    // ==================== TESTES DE DadosEmpresaContract ====================

    @Test
    fun `DadosEmpresa State inicial deve ter valores padrao`() {
        val state = DadosEmpresaContract.State()

        assertEquals("", state.nomeEmpresa)
        assertEquals("", state.telefone)
        assertEquals("", state.email)
        assertEquals("", state.endereco)
        assertEquals("", state.documento)
        assertEquals(TipoPessoa.JURIDICA, state.tipoPessoa)
        assertNull(state.documentoError)
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
    }

    @Test
    fun `DadosEmpresa State com dados preenchidos`() {
        val state = DadosEmpresaContract.State(
            nomeEmpresa = "Minha Empresa LTDA",
            telefone = "(11) 99999-8888",
            email = "contato@empresa.com",
            endereco = "Rua Principal, 123",
            documento = "11222333000181",
            tipoPessoa = TipoPessoa.JURIDICA
        )

        assertEquals("Minha Empresa LTDA", state.nomeEmpresa)
        assertEquals("(11) 99999-8888", state.telefone)
        assertEquals("contato@empresa.com", state.email)
        assertEquals("Rua Principal, 123", state.endereco)
        assertEquals("11222333000181", state.documento)
        assertEquals(TipoPessoa.JURIDICA, state.tipoPessoa)
    }

    @Test
    fun `DadosEmpresa State pessoa fisica`() {
        val state = DadosEmpresaContract.State(
            nomeEmpresa = "João da Silva",
            documento = "12345678909",
            tipoPessoa = TipoPessoa.FISICA
        )

        assertEquals(TipoPessoa.FISICA, state.tipoPessoa)
        assertEquals("12345678909", state.documento)
    }

    @Test
    fun `DadosEmpresa Actions devem conter valores corretos`() {
        assertEquals("Empresa", (DadosEmpresaContract.Action.SetNomeEmpresa("Empresa") as DadosEmpresaContract.Action.SetNomeEmpresa).value)
        assertEquals("11999998888", (DadosEmpresaContract.Action.SetTelefone("11999998888") as DadosEmpresaContract.Action.SetTelefone).value)
        assertEquals("email@test.com", (DadosEmpresaContract.Action.SetEmail("email@test.com") as DadosEmpresaContract.Action.SetEmail).value)
        assertEquals("Rua A", (DadosEmpresaContract.Action.SetEndereco("Rua A") as DadosEmpresaContract.Action.SetEndereco).value)
        assertEquals("12345678909", (DadosEmpresaContract.Action.SetDocumento("12345678909") as DadosEmpresaContract.Action.SetDocumento).value)
        assertEquals(TipoPessoa.FISICA, (DadosEmpresaContract.Action.SetTipoPessoa(TipoPessoa.FISICA) as DadosEmpresaContract.Action.SetTipoPessoa).value)
    }

    @Test
    fun `DadosEmpresa Effects devem conter valores corretos`() {
        assertTrue(DadosEmpresaContract.Effect.NavigateBack is DadosEmpresaContract.Effect)

        val success = DadosEmpresaContract.Effect.ShowSuccess("Dados salvos!")
        assertEquals("Dados salvos!", success.message)

        val error = DadosEmpresaContract.Effect.ShowError("Erro ao salvar")
        assertEquals("Erro ao salvar", error.message)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - alterar senha passo a passo`() {
        var state = ChangePasswordContract.State()

        // Digita senha atual
        state = state.copy(currentPassword = "senhaAtual123")
        assertEquals("senhaAtual123", state.currentPassword)

        // Digita nova senha
        state = state.copy(newPassword = "novaSenha456")
        assertEquals("novaSenha456", state.newPassword)

        // Confirma nova senha
        state = state.copy(confirmPassword = "novaSenha456")
        assertEquals("novaSenha456", state.confirmPassword)

        // Inicia loading
        state = state.copy(isLoading = true)
        assertTrue(state.isLoading)
    }

    @Test
    fun `Cenario - toggle visibilidade de senhas`() {
        var state = ChangePasswordContract.State()

        // Toggle senha atual
        state = state.copy(showCurrentPassword = true)
        assertTrue(state.showCurrentPassword)
        assertFalse(state.showNewPassword)
        assertFalse(state.showConfirmPassword)

        // Toggle nova senha
        state = state.copy(showNewPassword = true)
        assertTrue(state.showNewPassword)

        // Toggle confirmar senha
        state = state.copy(showConfirmPassword = true)
        assertTrue(state.showConfirmPassword)
    }

    @Test
    fun `Cenario - preencher dados empresa`() {
        var state = DadosEmpresaContract.State()

        // Seleciona tipo pessoa
        state = state.copy(tipoPessoa = TipoPessoa.JURIDICA)
        assertEquals(TipoPessoa.JURIDICA, state.tipoPessoa)

        // Preenche dados
        state = state.copy(
            nomeEmpresa = "Locadora XYZ LTDA",
            documento = "11222333000181",
            telefone = "(11) 3333-4444",
            email = "contato@locadoraxyz.com.br",
            endereco = "Av. Industrial, 1000"
        )

        assertEquals("Locadora XYZ LTDA", state.nomeEmpresa)
        assertEquals("11222333000181", state.documento)

        // Inicia salvamento
        state = state.copy(isSaving = true)
        assertTrue(state.isSaving)
    }

    @Test
    fun `Cenario - erro de validacao de documento`() {
        var state = DadosEmpresaContract.State(
            tipoPessoa = TipoPessoa.JURIDICA,
            documento = "11111111111111" // CNPJ inválido
        )

        // Simula erro de validação
        state = state.copy(documentoError = "CNPJ inválido")
        assertEquals("CNPJ inválido", state.documentoError)

        // Limpa erro ao corrigir
        state = state.copy(
            documento = "11222333000181",
            documentoError = null
        )
        assertNull(state.documentoError)
    }

    // ==================== TESTES DE ChangeProfileContract ====================

    @Test
    fun `ChangeProfile State inicial deve ter valores padrao`() {
        val state = ChangeProfileContract.State()

        assertEquals("", state.currentName)
        assertEquals("", state.newName)
        assertNull(state.nameError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `ChangeProfile State com dados preenchidos`() {
        val state = ChangeProfileContract.State(
            currentName = "Nome Atual",
            newName = "Novo Nome"
        )

        assertEquals("Nome Atual", state.currentName)
        assertEquals("Novo Nome", state.newName)
    }

    @Test
    fun `ChangeProfile State com erro de validacao`() {
        val state = ChangeProfileContract.State(
            nameError = "Nome deve ter pelo menos 2 caracteres"
        )

        assertEquals("Nome deve ter pelo menos 2 caracteres", state.nameError)
    }

    @Test
    fun `ChangeProfile Actions devem conter valores corretos`() {
        val setName = ChangeProfileContract.Action.SetName("Novo Nome")
        assertEquals("Novo Nome", setName.name)

        assertTrue(ChangeProfileContract.Action.Submit is ChangeProfileContract.Action)
    }

    @Test
    fun `ChangeProfile Effects devem conter valores corretos`() {
        assertTrue(ChangeProfileContract.Effect.NavigateBack is ChangeProfileContract.Effect)

        val success = ChangeProfileContract.Effect.ShowSuccess("Perfil atualizado!")
        assertEquals("Perfil atualizado!", success.message)

        val error = ChangeProfileContract.Effect.ShowError("Erro ao atualizar")
        assertEquals("Erro ao atualizar", error.message)
    }

    // ==================== TESTES DE DataPrivacyContract ====================

    @Test
    fun `DataPrivacy State inicial deve ter valores padrao`() {
        val state = DataPrivacyContract.State()

        assertFalse(state.showDeleteDialog)
        assertEquals("", state.password)
        assertFalse(state.isDeleting)
        assertNull(state.errorMessage)
    }

    @Test
    fun `DataPrivacy State com dialog aberto`() {
        val state = DataPrivacyContract.State(
            showDeleteDialog = true,
            password = "minhasenha"
        )

        assertTrue(state.showDeleteDialog)
        assertEquals("minhasenha", state.password)
    }

    @Test
    fun `DataPrivacy State durante exclusao`() {
        val state = DataPrivacyContract.State(
            showDeleteDialog = true,
            isDeleting = true
        )

        assertTrue(state.isDeleting)
    }

    @Test
    fun `DataPrivacy State com erro`() {
        val state = DataPrivacyContract.State(
            showDeleteDialog = true,
            errorMessage = "Senha incorreta"
        )

        assertEquals("Senha incorreta", state.errorMessage)
    }

    @Test
    fun `DataPrivacy Actions devem existir`() {
        assertTrue(DataPrivacyContract.Action.ShowDeleteDialog is DataPrivacyContract.Action)
        assertTrue(DataPrivacyContract.Action.HideDeleteDialog is DataPrivacyContract.Action)
        assertTrue(DataPrivacyContract.Action.ConfirmDeleteAccount is DataPrivacyContract.Action)
        assertTrue(DataPrivacyContract.Action.OpenTermsOfUse is DataPrivacyContract.Action)
        assertTrue(DataPrivacyContract.Action.OpenPrivacyPolicy is DataPrivacyContract.Action)

        val setPassword = DataPrivacyContract.Action.SetPassword("senha123")
        assertEquals("senha123", setPassword.password)
    }

    @Test
    fun `DataPrivacy Effects devem conter valores corretos`() {
        assertTrue(DataPrivacyContract.Effect.NavigateBack is DataPrivacyContract.Effect)
        assertTrue(DataPrivacyContract.Effect.AccountDeleted is DataPrivacyContract.Effect)

        val openUrl = DataPrivacyContract.Effect.OpenUrl("https://example.com/terms")
        assertEquals("https://example.com/terms", openUrl.url)

        val error = DataPrivacyContract.Effect.ShowError("Erro ao excluir")
        assertEquals("Erro ao excluir", error.message)
    }

    // ==================== CENARIOS DE NEGOCIO ADICIONAIS ====================

    @Test
    fun `Cenario - fluxo de exclusao de conta`() {
        var state = DataPrivacyContract.State()

        // Abre dialog de confirmação
        state = state.copy(showDeleteDialog = true)
        assertTrue(state.showDeleteDialog)

        // Digita senha
        state = state.copy(password = "minhasenha123")
        assertEquals("minhasenha123", state.password)

        // Inicia exclusão
        state = state.copy(isDeleting = true)
        assertTrue(state.isDeleting)

        // Erro de senha
        state = state.copy(
            isDeleting = false,
            errorMessage = "Senha incorreta"
        )
        assertFalse(state.isDeleting)
        assertEquals("Senha incorreta", state.errorMessage)
    }

    @Test
    fun `Cenario - alterar nome do perfil`() {
        var state = ChangeProfileContract.State(
            currentName = "Nome Antigo"
        )

        // Digita novo nome
        state = state.copy(newName = "N")
        assertEquals("N", state.newName)

        // Erro - nome muito curto
        state = state.copy(nameError = "Nome deve ter pelo menos 2 caracteres")
        assertEquals("Nome deve ter pelo menos 2 caracteres", state.nameError)

        // Corrige nome
        state = state.copy(newName = "Novo Nome Completo", nameError = null)
        assertEquals("Novo Nome Completo", state.newName)
        assertNull(state.nameError)

        // Inicia salvamento
        state = state.copy(isLoading = true)
        assertTrue(state.isLoading)
    }
}
