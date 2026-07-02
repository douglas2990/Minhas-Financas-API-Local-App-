package com.douglas2990.minhasfinancas2990

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.douglas2990.minhasfinancas2990.ui.AdicionarTransacaoScreen
import com.douglas2990.minhasfinancas2990.ui.TransacaoScreen
import com.douglas2990.minhasfinancas2990.ui.theme.MinhasFinancas2990Theme
import com.douglas2990.minhasfinancas2990.ui.viewmodel.TransacaoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TransacaoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinhasFinancas2990Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "lista") {
                    composable("lista") {
                        TransacaoScreen(
                            viewModel = viewModel,
                            onNavigateToAdd = { navController.navigate("adicionar") }
                        )
                    }
                    composable("adicionar") {
                        AdicionarTransacaoScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}