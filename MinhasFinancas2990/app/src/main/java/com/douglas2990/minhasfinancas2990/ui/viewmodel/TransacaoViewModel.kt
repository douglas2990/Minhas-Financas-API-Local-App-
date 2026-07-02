package com.douglas2990.minhasfinancas2990.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import com.douglas2990.minhasfinancas2990.domain.usecase.AdicionarTransacaoUseCase
import com.douglas2990.minhasfinancas2990.domain.usecase.ObterTransacoesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TransacaoViewModel @Inject constructor(
    private val obterTransacoesUseCase: ObterTransacoesUseCase,
    private val adicionarTransacaoUseCase: AdicionarTransacaoUseCase,
    private val repository: TransacaoRepository
) : ViewModel() {

    // Flag para garantir que a sincronização inicial ocorra apenas uma vez
    private var jaSincronizou = false

    // Mês/Ano selecionados para o filtro (padrão: mês/ano atual)
    private val calendarAtual = Calendar.getInstance()
    private val _mesSelecionado = MutableStateFlow(calendarAtual.get(Calendar.MONTH) + 1)
    val mesSelecionado: StateFlow<Int> = _mesSelecionado

    private val _anoSelecionado = MutableStateFlow(calendarAtual.get(Calendar.YEAR))
    val anoSelecionado: StateFlow<Int> = _anoSelecionado

    init {
        sincronizarDados()
    }

    private fun sincronizarDados() {
        if (!jaSincronizou) {
            viewModelScope.launch {
                try {
                    repository.sincronizar()
                    jaSincronizou = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun onMesAnoChange(mes: Int, ano: Int) {
        _mesSelecionado.value = mes
        _anoSelecionado.value = ano
    }

    // Combina mes/ano selecionados e troca a query do Room sempre que mudam
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transacoesState: StateFlow<List<Transacao>> = kotlinx.coroutines.flow.combine(
        _mesSelecionado, _anoSelecionado
    ) { mes, ano -> mes to ano }
        .flatMapLatest { (mes, ano) -> obterTransacoesUseCase(mes, ano) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun salvarTransacao(transacao: Transacao) {
        viewModelScope.launch {
            try {
                adicionarTransacaoUseCase(transacao)
                // A sincronização será disparada automaticamente pelo Repository.salvarTransacao()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}