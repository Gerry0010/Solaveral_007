package com.solareval.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.solareval.app.data.SessionStore
import com.solareval.app.ui.Navegacion
import com.solareval.app.ui.tema.SolarevalTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolarevalTema {
                Navegacion(SessionStore(applicationContext))
            }
        }
    }
}