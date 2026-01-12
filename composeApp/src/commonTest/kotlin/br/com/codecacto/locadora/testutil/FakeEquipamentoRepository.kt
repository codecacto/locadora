package br.com.codecacto.locadora.testutil

import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake EquipamentoRepository para testes.
 */
class FakeEquipamentoRepository : EquipamentoRepository {

    private val equipamentosFlow = MutableStateFlow<List<Equipamento>>(emptyList())
    private var nextId = 1

    var shouldThrowOnAdd = false
    var shouldThrowOnUpdate = false
    var shouldThrowOnDelete = false
    var exceptionToThrow: Exception = Exception("Erro simulado")

    override fun getEquipamentos(): Flow<List<Equipamento>> = equipamentosFlow

    override suspend fun getEquipamentoById(id: String): Equipamento? {
        return equipamentosFlow.value.find { it.id == id }
    }

    override suspend fun addEquipamento(equipamento: Equipamento): String {
        if (shouldThrowOnAdd) throw exceptionToThrow

        val id = "equip-${nextId++}"
        val newEquipamento = equipamento.copy(id = id)
        equipamentosFlow.value = equipamentosFlow.value + newEquipamento
        return id
    }

    override suspend fun updateEquipamento(equipamento: Equipamento) {
        if (shouldThrowOnUpdate) throw exceptionToThrow

        equipamentosFlow.value = equipamentosFlow.value.map {
            if (it.id == equipamento.id) equipamento else it
        }
    }

    override suspend fun deleteEquipamento(id: String) {
        if (shouldThrowOnDelete) throw exceptionToThrow

        equipamentosFlow.value = equipamentosFlow.value.filter { it.id != id }
    }

    override suspend fun searchEquipamentos(query: String): List<Equipamento> {
        val queryLower = query.lowercase()
        return equipamentosFlow.value.filter { equipamento ->
            equipamento.nome.lowercase().contains(queryLower) ||
            equipamento.categoria.lowercase().contains(queryLower) ||
            equipamento.identificacao?.lowercase()?.contains(queryLower) == true
        }
    }

    // Helpers para testes
    fun setEquipamentos(equipamentos: List<Equipamento>) {
        equipamentosFlow.value = equipamentos
    }

    fun addEquipamentoDirectly(equipamento: Equipamento) {
        equipamentosFlow.value = equipamentosFlow.value + equipamento
    }

    fun clear() {
        equipamentosFlow.value = emptyList()
        nextId = 1
    }
}
