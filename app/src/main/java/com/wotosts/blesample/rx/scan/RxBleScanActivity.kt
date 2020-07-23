package com.wotosts.blesample.rx.scan

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.polidea.rxandroidble2.scan.ScanResult
import com.wotosts.blesample.BaseActivity
import com.wotosts.blesample.R
import com.wotosts.blesample.databinding.ActivityRxBleScanBinding
import com.wotosts.blesample.rx.connect.RxBleConnectionActivity
import com.wotosts.blesample.util.Utils.Companion.KEY_MAC

class RxBleScanActivity : BaseActivity() {

    lateinit var viewModel: RxBleScanViewModel
    lateinit var binding: ActivityRxBleScanBinding

    lateinit var scanAdapter: ScanResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rx_ble_scan)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(RxBleScanViewModel::class.java)
        binding.viewModel = viewModel

        scanAdapter =
            ScanResultAdapter(mutableListOf(), object : ScanResultAdapter.ItemClickListener {
                override fun onClicked(result: ScanResult) {
                    connect(result)
                }
            })
        binding.rvResult.adapter = scanAdapter

        viewModel.resultUpdateEvent.observe(this, Observer { event -> scanAdapter.updateList(viewModel.scanResults)})
    }

    override fun onStart() {
        super.onStart()
        if (BluetoothAdapter.getDefaultAdapter().isEnabled)
            viewModel.startScan()
        else {
            Toast.makeText(this, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onStop() {
        viewModel.stopScan()
        super.onStop()
    }

    fun connect(result: ScanResult) {
        val connectIntent = Intent(this, RxBleConnectionActivity::class.java)
        connectIntent.putExtra(KEY_MAC, result.bleDevice.macAddress)
        startActivity(connectIntent)
    }
}
