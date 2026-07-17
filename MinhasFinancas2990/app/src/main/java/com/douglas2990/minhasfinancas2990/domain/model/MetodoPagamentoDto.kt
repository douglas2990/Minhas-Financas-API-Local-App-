package com.douglas2990.minhasfinancas2990.domain.model

data class MetodoPagamentoDto(
    val id: Int = 0,
    val nome: String,
    val diaVencimento: Int = 0,
    val cor: String = "#FFFFFF" // hex, ex: "#FF5733"
)
