package com.douglas2990.minhasfinancas2990.data.mapper

import com.douglas2990.minhasfinancas2990.data.local.entity.TransacaoEntity
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.model.TransacaoDto
import com.douglas2990.minhasfinancas2990.domain.model.TipoTransacao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun TransacaoDto.toDomain(): Transacao {
    val formatador = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val dataConvertida = try { formatador.parse(this.data) ?: Date() } catch (_: Exception) { Date() }

    return Transacao(
        id = this.id.toString(),
        titulo = this.descricao,
        valor = this.valor,
        data = dataConvertida,
        tipo = try { TipoTransacao.valueOf(this.tipo?.uppercase() ?: "RECEITA") } catch (_: Exception) { TipoTransacao.RECEITA },
        categoria = this.categoria,
        metodo = this.metodo
    )
}

fun TransacaoEntity.toDomain(): Transacao {
    return Transacao(
        id = this.localId.toString(),
        titulo = this.titulo ?: "Sem título",
        valor = this.valor,
        data = Date(this.data),
        tipo = try { TipoTransacao.valueOf(this.tipo) } catch (_: Exception) { TipoTransacao.DESPESA },
        categoria = this.categoria,
        metodo = this.metodo,
        sincronizado = this.sincronizado  // ← expõe o status de sync
    )
}

fun Transacao.toEntity(isSynced: Boolean = false): TransacaoEntity {
    return TransacaoEntity(
        titulo = this.titulo,
        valor = this.valor,
        data = this.data.time,
        tipo = this.tipo.name,
        categoria = this.categoria,
        metodo = this.metodo,
        sincronizado = isSynced
    )
}

fun TransacaoEntity.toDto(): TransacaoDto {
    val formatador = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val dataString = formatador.format(Date(this.data))

    return TransacaoDto(
        id = this.localId.toInt(),
        descricao = this.titulo,
        valor = this.valor,
        data = dataString,
        tipo = this.tipo,
        categoria = this.categoria,
        metodo = this.metodo,
        totalParcelas = 1,
        parcelaAtual = 1
    )
}