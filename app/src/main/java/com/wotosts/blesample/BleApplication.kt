package com.wotosts.blesample

import android.app.Application
import android.content.Context
import android.util.Log
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.exceptions.BleException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins

class BleApplication: Application() {
    lateinit var rxBleClient: RxBleClient

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        RxBleClient.updateLogOptions(
            LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        )
        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            if (throwable is UndeliverableException && throwable.cause is BleException) {
                Log.v(
                    "BleTest",
                    "Suppressed UndeliverableException: $throwable"
                )
                return@setErrorHandler   // ignore BleExceptions as they were surely delivered at least once
            }
        }
    }

    companion object {
        fun getBleClient(context: Context): RxBleClient {
            val application: BleApplication =
                context.applicationContext as BleApplication
            return application.rxBleClient
        }
    }
}