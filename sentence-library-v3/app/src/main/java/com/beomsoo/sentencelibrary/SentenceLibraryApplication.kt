package com.beomsoo.sentencelibrary

import android.app.Application
import com.beomsoo.sentencelibrary.data.AppDatabase
import com.beomsoo.sentencelibrary.data.ReadingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SentenceLibraryApplication : Application() {
    lateinit var repository: ReadingRepository
        private set
    override fun onCreate() {
        super.onCreate()
        repository=ReadingRepository(AppDatabase.get(this))
        CoroutineScope(SupervisorJob()+Dispatchers.IO).launch { repository.bootstrapDefaults() }
    }
}
