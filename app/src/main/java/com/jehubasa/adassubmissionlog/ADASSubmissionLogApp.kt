package com.jehubasa.adassubmissionlog

import android.app.Application

class ADASSubmissionLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}