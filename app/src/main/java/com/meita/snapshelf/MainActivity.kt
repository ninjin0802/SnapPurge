package com.meita.snapshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.meita.snapshelf.ui.SnapShelfApp
import com.meita.snapshelf.ui.theme.SnapShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SnapShelfApplication
        setContent {
            val themeMode by app.container.userPreferences.themeMode.collectAsState(initial = null)
            SnapShelfTheme(themeMode = themeMode) {
                SnapShelfApp(container = app.container)
            }
        }
    }
}

