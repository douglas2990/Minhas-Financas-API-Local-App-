package com.douglas2990.minhasfinancas2990.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.douglas2990.minhasfinancas2990.domain.model.CategoriaDto
import com.douglas2990.minhasfinancas2990.domain.model.MetodoPagamentoDto
import com.douglas2990.minhasfinancas2990.domain.model.TipoTransacao
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
    var categoriaSelecionada by remember { mutableStateOf<CategoriaDto?>(null) }
    var metodoSelecionado by remember { mutableStateOf<MetodoPagamentoDto?>(null) }
    var dataSelecionada by remember { mutableStateOf(Date()) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    // Checkboxes de cartão/parcelamento (igual ao JavaFX)
    var cartaoDeCredito by remember { mutableStateOf(false) }
    var faturaNaoVirou by remember { mutableStateOf(false) }
    var parcelado by remember { mutableStateOf(false) }
    var qtdParcelas by remember { mutableStateOf("1") }

    val categorias by viewModel.categorias.collectAsState()
    val metodos by viewModel.metodos.collectAsState()
    val erroCarregamento by viewModel.erroCarregamento.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Novo Lançamento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Descrição (ex: Compra Mercado)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it },
                label = { Text("Valor") },
                modifier = Modifier.weight(1f)
            )
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
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))

        DropdownCategoria(categorias = categorias, selecionada = categoriaSelecionada, onSelecionar = { categoriaSelecionada = it })
        Spacer(Modifier.height(8.dp))

        DropdownMetodo(
            metodos = metodos,
            selecionado = metodoSelecionado,
            onSelecionar = {
                metodoSelecionado = it
                // Se nome contém "cartão", marca automaticamente (igual ao Java)
                if (it.nome.lowercase().contains("cartão")) cartaoDeCredito = true
            }
        )

        if (erroCarregamento != null) {
            Spacer(Modifier.height(4.dp))
            Text("⚠️ $erroCarregamento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { viewModel.recarregarCadastros() }) { Text("Tentar novamente") }
        }

        Spacer(Modifier.height(8.dp))

        // Card de opções de cartão/parcelamento
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                CheckboxRow("Cartão de Crédito", cartaoDeCredito) {
                    cartaoDeCredito = it
                    if (!it) faturaNaoVirou = false
                }
                AnimatedVisibility(visible = cartaoDeCredito) {
                    CheckboxRow("Fatura NÃO Virou (lançar no mês atual)", faturaNaoVirou) {
                        faturaNaoVirou = it
                    }
                }
                CheckboxRow("Parcelado?", parcelado) {
                    parcelado = it
                    if (it) cartaoDeCredito = true // parcela implica cartão
                }
                AnimatedVisibility(visible = parcelado) {
                    OutlinedTextField(
                        value = qtdParcelas,
                        onValueChange = { if (it.all { c -> c.isDigit() }) qtdParcelas = it },
                        label = { Text("Quantidade de parcelas") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = titulo.isNotBlank() && valor.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006437)),
            onClick = {
                val valorDouble = valor.replace(",", ".").toDoubleOrNull()
                if (valorDouble != null) {
                    viewModel.salvarComParcelamento(
                        titulo = titulo.trim(),
                        valor = valorDouble,
                        data = dataSelecionada,
                        categoria = categoriaSelecionada?.nome ?: "Sem Categoria",
                        metodo = metodoSelecionado?.nome ?: "Dinheiro",
                        tipo = TipoTransacao.DESPESA, // fixo até implementar receita
                        cartaoDeCredito = cartaoDeCredito,
                        faturaNaoVirou = faturaNaoVirou,
                        parcelado = parcelado,
                        qtdParcelas = qtdParcelas.toIntOrNull() ?: 1
                    )
                    onNavigateBack()
                }
            }
        ) {
            Text("Salvar Lançamento", fontWeight = FontWeight.Bold)
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dataSelecionada.time)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dataSelecionada = Date(it) }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownCategoria(categorias: List<CategoriaDto>, selecionada: CategoriaDto?, onSelecionar: (CategoriaDto) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selecionada?.nome ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (categorias.isEmpty()) DropdownMenuItem(text = { Text("Nenhuma categoria cadastrada") }, onClick = {}, enabled = false)
            categorias.forEach { cat ->
                DropdownMenuItem(text = { Text(cat.nome) }, onClick = { onSelecionar(cat); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMetodo(metodos: List<MetodoPagamentoDto>, selecionado: MetodoPagamentoDto?, onSelecionar: (MetodoPagamentoDto) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selecionado?.nome ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Método de Pagamento") },
            leadingIcon = { if (selecionado != null) ColorDot(selecionado.cor) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (metodos.isEmpty()) DropdownMenuItem(text = { Text("Nenhum método cadastrado") }, onClick = {}, enabled = false)
            metodos.forEach { met ->
                DropdownMenuItem(
                    text = { Text(met.nome) },
                    leadingIcon = { ColorDot(met.cor) },
                    onClick = { onSelecionar(met); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ColorDot(corHex: String) {
    val cor = remember(corHex) {
        try { Color(android.graphics.Color.parseColor(corHex)) } catch (_: Exception) { Color.Gray }
    }
    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(cor))
}