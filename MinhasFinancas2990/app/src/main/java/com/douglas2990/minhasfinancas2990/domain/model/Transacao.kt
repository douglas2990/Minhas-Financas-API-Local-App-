package com.douglas2990.minhasfinancas2990.domain.model

import java.util.Date

data class Transacao(
    val id: String = "",
    val titulo: String,
    val valor: Double,
    val data: Date,
    val tipo: TipoTransacao,
    val categoria: String, // Adicione aqui
    val metodo: String     // Adicione aqui
)

enum class TipoTransacao {
    RECEITA, DESPESA
}