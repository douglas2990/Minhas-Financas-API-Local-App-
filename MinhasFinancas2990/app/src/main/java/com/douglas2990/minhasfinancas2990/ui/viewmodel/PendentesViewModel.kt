package com.douglas2990.minhasfinancas2990.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendentesViewModel @Inject constructor(
    private val repository: TransacaoRepository
) : ViewModel() {

    // Lista reativa: só os itens com sincronizado = false
    val pendentes: StateFlow<List<Transacao>> = repository.getTransacoes()
        .map { lista -> lista.filter { !it.sincronizado } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem

    fun limparMensagem() { _mensagem.value = null }

    /** Envia UM item para a API e remove do Room após sucesso */
    fun enviarUm(transacao: Transacao) {
        viewModelScope.launch {
            _enviando.value = true
            try {
                repository.sincronizarUm(transacao)
                _mensagem.value = "\"${transacao.titulo}\" enviado com sucesso!"
            } catch (e: Exception) {
                _mensagem.value = "Erro ao enviar: ${e.message}"
            } finally {
                _enviando.value = false
            }
        }
    }

    /** Envia TODOS os pendentes para a API */
    fun enviarTodos() {
        viewModelScope.launch {
            _enviando.value = true
            try {
                repository.sincronizar()
                _mensagem.value = "Todos os lançamentos enviados!"
            } catch (e: Exception) {
                _mensagem.value = "Erro ao enviar: ${e.message}"
            } finally {
                _enviando.value = false
            }
        }
    }

    /** Remove UM item do Room sem enviar para a API */
    fun deletarUm(transacao: Transacao) {
        viewModelScope.launch {
            try {
                repository.excluirLocal(transacao)
                _mensagem.value = "\"${transacao.titulo}\" removido."
            } catch (e: Exception) {
                _mensagem.value = "Erro ao deletar: ${e.message}"
            }
        }
    }

    /** Remove TODOS os pendentes do Room sem enviar para a API */
    fun deletarTodos() {
        viewModelScope.launch {
            try {
                val lista = pendentes.value
                lista.forEach { repository.excluirLocal(it) }
                _mensagem.value = "${lista.size} lançamentos removidos."
            } catch (e: Exception) {
                _mensagem.value = "Erro ao deletar: ${e.message}"
            }
        }
    }
}
