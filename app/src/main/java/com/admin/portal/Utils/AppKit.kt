package com.admin.portal.Utils

import android.app.Application
import io.paperdb.Paper
class AppKit: Application() {
    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
    }
}