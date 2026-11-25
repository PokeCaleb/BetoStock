package com.betostock

import android.app.Application
import com.betostock.data.AppDatabase
import com.betostock.data.StoreRepository
import com.betostock.data.seedDemoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BetoStockApp : Application() {
    lateinit var repository: StoreRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = StoreRepository(AppDatabase.create(this))

        // Datos de ejemplo solo en compilaciones debug (y solo si la base está vacía).
        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch { seedDemoData(repository) }
        }
    }
}
