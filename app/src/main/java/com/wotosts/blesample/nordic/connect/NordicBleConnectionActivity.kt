package com.wotosts.blesample.nordic.connect

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.ScrollView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.wotosts.blesample.BaseActivity
import com.wotosts.blesample.R
import com.wotosts.blesample.rx.connect.ServiceItemClickListener
import com.wotosts.blesample.databinding.ActivityNordicBleConnectionBinding
import com.wotosts.blesample.model.ServiceItem
import com.wotosts.blesample.util.Utils.Companion.KEY_DEVICE
import java.util.*

class NordicBleConnectionActivity : BaseActivity() {

    lateinit var binding: ActivityNordicBleConnectionBinding
    val viewModel: NordicBleConnectionViewModel by viewModels()

    lateinit var serviceAdapter: DiscoveredServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nordic_ble_connection)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.connect(intent.getParcelableExtra(KEY_DEVICE) as BluetoothDevice)

        serviceAdapter =
            DiscoveredServiceAdapter(object :
                ServiceItemClickListener {
                override fun onItemClicked(item: ServiceItem) {
                    // do nothing
                }

                override fun onConnectClicked(checked: Boolean, uuid: UUID) {
                    // do nothing
                }

                override fun onReadBtnClicked(uuid: UUID) {
                    viewModel.readData(uuid)
                }

                override fun onWriteBtnClicked(uuid: UUID, msg: String?) {
                    viewModel.writeData(uuid, msg)
                }

                override fun onNotiBtnClicked(isChecked: Boolean, uuid: UUID) {
                    viewModel.toggleNotification(isChecked, uuid)
                }
            })

        binding.rvService.adapter = serviceAdapter

        viewModel.foundService.observe(
            this,
            Observer { list -> serviceAdapter.updateServices(list) })
        viewModel.log.observe(this, Observer {
            binding.scroll.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        })
    }
}
