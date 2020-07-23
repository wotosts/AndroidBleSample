package com.wotosts.blesample

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.wotosts.blesample.nordic.scan.NordicBleScanActivity
import com.wotosts.blesample.rx.scan.RxBleScanActivity

class MainActivity : AppCompatActivity() {
    val REQUEST_BT = 2000
    val REQUEST_LOCATION = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_BT)
        }

        findViewById<Button>(R.id.btnNordic).setOnClickListener { v -> startNordicBle() }
        findViewById<Button>(R.id.btnRx).setOnClickListener { v -> startRxBle() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_BT ->
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (!isLocationEnabled(this)) {
                        Toast.makeText(this, "위치 정보 사용을 켜주세요.", Toast.LENGTH_SHORT).show()
                        val intent =
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }

                } else {
                    Toast.makeText(this, "위치 권한을 허용해 주세요.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
    }

    fun startNordicBle() {
        val intent = Intent(this, NordicBleScanActivity::class.java)
        startActivity(intent)
    }

    fun startRxBle() {
        val intent = Intent(this, RxBleScanActivity::class.java)
        startActivity(intent)
    }

    fun isLocationEnabled(context: Context): Boolean {
        var locationMode = Settings.Secure.LOCATION_MODE_OFF
        try {
            locationMode = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            )
        } catch (e: SettingNotFoundException) {
            // do nothing
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }
}
