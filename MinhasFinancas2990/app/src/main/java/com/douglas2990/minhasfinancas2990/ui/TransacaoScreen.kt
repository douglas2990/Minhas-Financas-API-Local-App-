package com.douglas2990.minhasfinancas2990.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.douglas2990.minhasfinancas2990.domain.model.TipoTransacao
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.ui.viewmodel.TransacaoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransacaoScreen(
    viewModel: TransacaoViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToPendentes: () -> Unit
) {
    val transacoes by viewModel.transacoesState.collectAsState()
    val mesSelecionado by viewModel.mesSelecionado.collectAsState()
    val anoSelecionado by viewModel.anoSelecionado.collectAsState()
    val pendentesCount by viewModel.pendentesCount.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var menuExpandido by remember { mutableStateOf(false) }

    // Cores personalizadas
    val corPrincipal = Color(0xFF006437)
    val corTextoVerde = Color(0xFF2E7D32)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Finanças 2990", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = corPrincipal),
                actions = {
                    // Badge no ícone quando há pendentes
                    BadgedBox(
                        badge = {
                            if (pendentesCount > 0) {
                                Badge { Text(pendentesCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⏳")
                                    Text("Verificar Pendentes")
                                    if (pendentesCount > 0) {
                                        Badge(containerColor = Color(0xFFFFA000)) {
                                            Text(pendentesCount.toString())
                                        }
                                    }
                                }
                            },
                            onClick = {
                                menuExpandido = false
                                onNavigateToPendentes()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = corPrincipal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FiltroMesAno(
                mes = mesSelecionado,
                ano = anoSelecionado,
                onMesAnoChange = viewModel::onMesAnoChange
            )

            TotalPeriodo(transacoes = transacoes)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transacoes) { transacao ->
                    TransacaoItem(
                        transacao = transacao,
                        dateFormat = dateFormat,
                        corReceita = corTextoVerde
                    )
                }
            }
        }
    }
}

private val NOMES_MESES = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)

@Composable
private fun TotalPeriodo(transacoes: List<Transacao>) {
    val totalReceitas = transacoes.filter { it.tipo == TipoTransacao.RECEITA }.sumOf { it.valor }
    val totalDespesas = transacoes.filter { it.tipo == TipoTransacao.DESPESA }.sumOf { it.valor }
    val saldo = totalReceitas - totalDespesas

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Receitas:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "+ R$ %.2f".format(totalReceitas),
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Despesas:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "- R$ %.2f".format(totalDespesas),
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Saldo do Período:", fontWeight = FontWeight.Bold)
                Text(
                    "R$ %.2f".format(saldo),
                    color = if (saldo >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroMesAno(
    mes: Int,
    ano: Int,
    onMesAnoChange: (Int, Int) -> Unit
) {
    var expandedMes by remember { mutableStateOf(false) }
    var anoTexto by remember(ano) { mutableStateOf(ano.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedMes,
            onExpandedChange = { expandedMes = !expandedMes },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = NOMES_MESES[mes - 1],
                onValueChange = {},
                readOnly = true,
                label = { Text("Mês") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMes) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedMes, onDismissRequest = { expandedMes = false }) {
                NOMES_MESES.forEachIndexed { index, nome ->
                    DropdownMenuItem(
                        text = { Text(nome) },
                        onClick = {
                            onMesAnoChange(index + 1, ano)
                            expandedMes = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = anoTexto,
            onValueChange = { novoTexto ->
                anoTexto = novoTexto
                novoTexto.toIntOrNull()?.let { novoAno ->
                    if (novoTexto.length == 4) onMesAnoChange(mes, novoAno)
                }
            },
            label = { Text("Ano") },
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun TransacaoItem(
    transacao: Transacao,
    dateFormat: SimpleDateFormat,
    corReceita: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transacao.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    // Indicador de status de sincronização
                    if (!transacao.sincronizado) {
                        Badge(containerColor = Color(0xFFFFA000)) {
                            Text("⏳", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Text(
                    text = "${transacao.categoria} • ${transacao.metodo}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = dateFormat.format(transacao.data),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = (if (transacao.tipo == TipoTransacao.DESPESA) "-" else "+") +
                        " R$ ${"%.2f".format(transacao.valor)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (transacao.tipo == TipoTransacao.DESPESA) Color(0xFFD32F2F) else corReceita,
                fontWeight = FontWeight.Bold
            )
        }
    }
}