package com.douglas2990.minhasfinancas2990.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.douglas2990.minhasfinancas2990.domain.model.TipoTransacao
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.ui.viewmodel.TransacaoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarTransacaoScreen(
    viewModel: TransacaoViewModel,
    onNavigateBack: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var metodo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoTransacao.RECEITA) }
    var dataSelecionada by remember { mutableStateOf(Date()) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoria") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = metodo,
            onValueChange = { metodo = it },
            label = { Text("Método (ex: Cartão)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
        OutlinedTextField(
            value = dateFormat.format(dataSelecionada),
            onValueChange = {},
            readOnly = true,
            label = { Text("Data") },
            trailingIcon = {
                IconButton(onClick = { mostrarDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tipo de Transação:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = tipo == TipoTransacao.RECEITA, onClick = { tipo = TipoTransacao.RECEITA })
            Text("Receita")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = tipo == TipoTransacao.DESPESA, onClick = { tipo = TipoTransacao.DESPESA })
            Text("Despesa")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = titulo.isNotBlank() && valor.isNotBlank(), // Desativa o botão se campos vazios
            onClick = {
                val valorDouble = valor.toDoubleOrNull()

                if (valorDouble != null) {
                    val novaTransacao = Transacao(
                        id = "0", // Usando String "0" para indicar novo
                        titulo = titulo,
                        valor = valorDouble,
                        data = dataSelecionada,
                        tipo = tipo,
                        categoria = categoria.ifBlank { "Sem Categoria" },
                        metodo = metodo.ifBlank { "Dinheiro" }
                    )
                    viewModel.salvarTransacao(novaTransacao)
                    onNavigateBack()
                } else {
                    // Aqui você poderia mostrar um Toast ou Snackbar de erro: "Valor inválido"
                }
            }
        ) {
            Text("Salvar")
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dataSelecionada.time
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataSelecionada = Date(millis)
                    }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}