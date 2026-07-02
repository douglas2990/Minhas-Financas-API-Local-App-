package com.douglas2990.minhasfinancas2990.domain.usecase


import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import javax.inject.Inject

class AdicionarTransacaoUseCase @Inject constructor(
    private val repository: TransacaoRepository
) {
    suspend operator fun invoke(transacao: Transacao) {
        // Aqui você pode adicionar validações de negócio, por exemplo:
        if (transacao.valor <= 0) {
            throw IllegalArgumentException("O valor deve ser maior que zero")
        }
        repository.salvarTransacao(transacao)
    }
}