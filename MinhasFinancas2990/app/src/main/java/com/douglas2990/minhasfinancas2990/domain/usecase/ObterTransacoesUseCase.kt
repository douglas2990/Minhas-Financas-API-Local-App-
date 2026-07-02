package com.douglas2990.minhasfinancas2990.domain.usecase

import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import javax.inject.Inject

class ObterTransacoesUseCase @Inject constructor(
    private val repository: TransacaoRepository
) {
    operator fun invoke() = repository.getTransacoes()

    operator fun invoke(mes: Int, ano: Int) = repository.getTransacoesPorPeriodo(mes, ano)
}