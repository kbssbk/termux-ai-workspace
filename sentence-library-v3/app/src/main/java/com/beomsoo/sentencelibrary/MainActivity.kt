package com.beomsoo.sentencelibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.beomsoo.sentencelibrary.ui.SentenceLibraryApp
import com.beomsoo.sentencelibrary.ui.theme.SentenceLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo=(application as SentenceLibraryApplication).repository
        setContent { SentenceLibraryTheme { SentenceLibraryApp(repo) } }
    }
}
