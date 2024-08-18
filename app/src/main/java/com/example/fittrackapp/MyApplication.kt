package com.example.fittrackapp // Asegúrate de que este sea el paquete correcto

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase
        FirebaseApp.initializeApp(this)
    }
}
