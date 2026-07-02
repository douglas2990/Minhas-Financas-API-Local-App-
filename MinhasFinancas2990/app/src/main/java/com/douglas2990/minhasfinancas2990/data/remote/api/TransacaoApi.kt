package com.douglas2990.minhasfinancas2990.data.remote.api

import com.douglas2990.minhasfinancas2990.domain.model.TransacaoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TransacaoApi {
    @GET("api/gastos") // A rota correta baseada no seu controller
    suspend fun buscarTransacoes(): List<TransacaoDto>

    @POST("api/gastos") // A rota correta para envio
    suspend fun enviarTransacao(@Body gasto: TransacaoDto): Response<Unit>
}