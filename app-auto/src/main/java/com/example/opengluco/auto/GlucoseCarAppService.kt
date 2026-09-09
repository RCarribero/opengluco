package com.example.opengluco.auto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class GlucoseCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        return HostValidator.Builder(this)
            .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
            .build()
    }

    override fun onCreateSession(): Session {
        return GlucoseCarSession()
    }
}
