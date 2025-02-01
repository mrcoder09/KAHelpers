package dev.funkymuse.setofusefulkotlinextensions

import android.app.Application
import dev.funkymuse.common.ifNotNull
import dev.funkymuse.common.ifNull
import dev.funkymuse.common.isNotNull


class AppLevel : Application() {
    override fun onCreate() {
        super.onCreate()

        val string = "saf"
        string.ifNotNull {

            return
        }.ifNull {

        }
    }
}