package com.meita.snapshelf

import android.app.Application
import com.meita.snapshelf.core.AppContainer

class SnapShelfApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

