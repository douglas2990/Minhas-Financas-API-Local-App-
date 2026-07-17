package com.douglas2990.minhasfinancas2990.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.ui.viewmodel.PendentesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendentesScreen(
    viewModel: PendentesViewModel,
    onNavigateBack: () -> Unit
) {
    val pendentes by viewModel.pendentes.collectAsState()
    val enviando by viewModel.enviando.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lançamentos Pendentes")
                        if (pendentes.isNotEmpty()) {
                            Text(
                                "${pendentes.size} aguardando sincronização",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFA000)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006437),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Botões de ação em massa
            if (pendentes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.enviarTodos() },
                        enabled = !enviando,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006437)),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (enviando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (enviando) "Enviando..." else "✓ Enviar Todos")
                    }

                    OutlinedButton(
                        onClick = { viewModel.deletarTodos() },
                        enabled = !enviando,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("✕ Deletar Todos")
                    }
                }

                HorizontalDivider()
            }

            if (pendentes.isEmpty()) {
                // Estado vazio
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tudo sincronizado!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Nenhum lançamento aguardando envio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pendentes, key = { it.id }) { transacao ->
                        PendenteItem(
                            transacao = transacao,
                            dateFormat = dateFormat,
                            enviando = enviando,
                            onEnviar = { viewModel.enviarUm(transacao) },
                            onDeletar = { viewModel.deletarUm(transacao) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendenteItem(
    transacao: Transacao,
    dateFormat: SimpleDateFormat,
    enviando: Boolean,
    onEnviar: () -> Unit,
    onDeletar: () -> Unit
) {
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1) // fundo amarelado indicando pendente
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳ ", style = MaterialTheme.typography.bodySmall)
                        Text(
                            transacao.titulo,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${transacao.categoria} • ${transacao.metodo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        dateFormat.format(transacao.data),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    "R$ ${"%.2f".format(transacao.valor)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botão Enviar para API
                FilledTonalButton(
                    onClick = onEnviar,
                    enabled = !enviando,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("↑ Enviar para API", style = MaterialTheme.typography.labelMedium)
                }

                // Botão Deletar
                OutlinedButton(
                    onClick = { mostrarConfirmacao = true },
                    enabled = !enviando,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Deletar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    // Diálogo de confirmação de exclusão
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacao = false },
            title = { Text("Deletar lançamento?") },
            text = { Text("\"${transacao.titulo}\" será removido apenas do dispositivo. Não foi enviado para a API.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletar()
                        mostrarConfirmacao = false
                    }
                ) {
                    Text("Deletar", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacao = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
