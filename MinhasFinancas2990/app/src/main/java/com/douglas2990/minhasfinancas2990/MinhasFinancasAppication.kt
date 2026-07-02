package com.douglas2990.minhasfinancas2990

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MinhasFinancasApplication : Application() {
    // Essa classe serve de ponto de partida para o Hilt gerar o código de injeção
}