package com.douglas2990.minhasfinancas2990.domain.model

import com.google.gson.annotations.SerializedName

data class TransacaoDto(
    val id: Int = 0,
    @SerializedName("descricao")
    val descricao: String,
    val valor: Double,
    val data: String,
    val tipo: String? = null,
    // Novos campos necessários pelo seu Backend
    val categoria: String = "",
    val metodo: String = "",
    val totalParcelas: Int = 1,
    val parcelaAtual: Int = 1
)