package com.douglas2990.minhasfinancas2990.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.douglas2990.minhasfinancas2990.domain.model.CategoriaDto
import com.douglas2990.minhasfinancas2990.domain.model.MetodoPagamentoDto
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.model.TipoTransacao
import java.util.Date
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

    // Listas para os dropdowns da tela de Adicionar
    private val _categorias = MutableStateFlow<List<CategoriaDto>>(emptyList())
    val categorias: StateFlow<List<CategoriaDto>> = _categorias

    private val _metodos = MutableStateFlow<List<MetodoPagamentoDto>>(emptyList())
    val metodos: StateFlow<List<MetodoPagamentoDto>> = _metodos

    private val _erroCarregamento = MutableStateFlow<String?>(null)
    val erroCarregamento: StateFlow<String?> = _erroCarregamento

    // Contagem reativa de itens aguardando sync — alimenta o banner na TransacaoScreen
    val pendentesCount: StateFlow<Int> = repository.getPendentesCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        sincronizarDados()
        carregarCadastros()
    }

    fun recarregarCadastros() {
        carregarCadastros()
    }

    private fun carregarCadastros() {
        viewModelScope.launch {
            try {
                android.util.Log.d("VIEWMODEL", "Chamando buscarCategorias()...")
                val cats = repository.buscarCategorias()
                android.util.Log.d("VIEWMODEL", "Categorias recebidas no VM: ${cats.size}")
                _categorias.value = cats

                android.util.Log.d("VIEWMODEL", "Chamando buscarMetodos()...")
                val mets = repository.buscarMetodos()
                android.util.Log.d("VIEWMODEL", "Métodos recebidos no VM: ${mets.size}")
                _metodos.value = mets

                _erroCarregamento.value = null
            } catch (e: Exception) {
                android.util.Log.e("VIEWMODEL", "ERRO em carregarCadastros: ${e.message}", e)
                _erroCarregamento.value = "Erro: ${e.message}"
            }
        }
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

    fun tentarSincronizar() {
        viewModelScope.launch {
            try {
                repository.sincronizar()
            } catch (e: Exception) {
                _erroCarregamento.value = "Falha na sincronização: ${e.message}"
            }
        }
    }

    fun salvarTransacao(transacao: Transacao) {
        viewModelScope.launch {
            try {
                adicionarTransacaoUseCase(transacao)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Replica EXATAMENTE a lógica do MainControllerAPI.aoSalvar() do JavaFX:
     *
     * isCartao = cartaoDeCredito || parcelado || nomeMetodo contém "cartão"
     * avancoInicial = (isCartao && !faturaNaoVirou) ? +1 mês : 0
     *
     * Para cada parcela i de 1..qtdParcelas:
     *   - descrição: "Base i/total" se total > 1, senão só a base
     *   - data: dataBase + avancoInicial + (i-1) meses
     *   - salva um POST separado por parcela
     */
    fun salvarComParcelamento(
        titulo: String,
        valor: Double,
        data: Date,
        categoria: String,
        metodo: String,
        tipo: TipoTransacao,
        cartaoDeCredito: Boolean,
        faturaNaoVirou: Boolean,
        parcelado: Boolean,
        qtdParcelas: Int
    ) {
        viewModelScope.launch {
            try {
                val totalParcelas = if (parcelado) qtdParcelas.coerceAtLeast(1) else 1

                // Lógica inteligente do cartão (igual ao Java)
                val isCartao = cartaoDeCredito || parcelado ||
                        metodo.lowercase().contains("cartão")
                val avancoInicial = if (isCartao && !faturaNaoVirou) 1 else 0

                android.util.Log.d("VM_PARCELAS",
                    "isCartao=$isCartao | avancoInicial=$avancoInicial | parcelas=$totalParcelas")

                for (i in 1..totalParcelas) {
                    val descFinal = if (totalParcelas > 1) "$titulo $i/$totalParcelas" else titulo

                    // Calcula a data da parcela: avanço de fatura + offset de cada mês
                    val cal = Calendar.getInstance()
                    cal.time = data
                    cal.add(Calendar.MONTH, avancoInicial + (i - 1))
                    val dataFinal = cal.time

                    android.util.Log.d("VM_PARCELAS", "Salvando: $descFinal | data: $dataFinal")

                    val transacao = Transacao(
                        id = "0",
                        titulo = descFinal,
                        valor = valor,
                        data = dataFinal,
                        tipo = tipo,
                        categoria = categoria,
                        metodo = metodo
                    )
                    adicionarTransacaoUseCase(transacao)
                }
            } catch (e: Exception) {
                android.util.Log.e("VM_PARCELAS", "Erro ao salvar parcelas: ${e.message}", e)
                _erroCarregamento.value = "Erro ao salvar: ${e.message}"
            }
        }
    }
}