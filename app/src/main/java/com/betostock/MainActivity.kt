package com.betostock

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.betostock.ui.navigation.BetoApp
import com.betostock.ui.theme.BetoStockTheme
import com.betostock.ui.viewmodel.betoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tema claro fijo: íconos oscuros en las barras del sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val repository = (application as BetoStockApp).repository
        setContent {
            BetoStockTheme {
                BetoApp(factory = betoViewModelFactory(repository))
            }
        }
    }
}
