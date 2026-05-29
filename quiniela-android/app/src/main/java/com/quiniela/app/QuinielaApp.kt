package com.quiniela.app

import android.app.Application
import com.quiniela.app.api.TokenManager

class QuinielaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
