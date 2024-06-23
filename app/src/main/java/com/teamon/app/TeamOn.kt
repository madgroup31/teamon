package com.teamon.app

import android.app.Application
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize


@Suppress("DEPRECATION")
class TeamOn: Application() {
    lateinit var model: Model

    override fun onCreate() {
        super.onCreate()
        model = Model(this)

        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
    }
}