package com.wotosts.blesample.nordic.connect

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.wotosts.blesample.rx.connect.ServiceItemClickListener
import com.wotosts.blesample.databinding.ItemNordicDiscoverServiceBinding
import com.wotosts.blesample.model.ServiceItem
import java.util.*

class DiscoveredServiceAdapter(private val listener: ServiceItemClickListener) :
    RecyclerView.Adapter<DiscoveredServiceAdapter.DiscoveredServiceViewHolder>() {

    private val resultList: MutableList<ServiceItem> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiscoveredServiceViewHolder {
        val binding: ItemNordicDiscoverServiceBinding = ItemNordicDiscoverServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return DiscoveredServiceViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DiscoveredServiceViewHolder,
        position: Int
    ) {
        holder.onBind(resultList[position], listener)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = resultList.size

    override fun getItemViewType(position: Int): Int = resultList[position].type

    fun updateServices(serviceList: List<BluetoothGattService>) {
        resultList.clear()

        for (service in serviceList) {
            resultList.add(
                ServiceItem(
                    ServiceItem.SERVICE,
                    getServiceType(service),
                    service.uuid
                )
            )
            val characteristics =
                service.characteristics
            for (characteristic in characteristics) {
                resultList.add(
                    ServiceItem(
                        ServiceItem.CHARACTERISTIC,
                        describeProperties(characteristic),
                        characteristic.uuid
                    )
                )
            }
        }
        notifyDataSetChanged()
    }

    private fun describeProperties(characteristic: BluetoothGattCharacteristic): String {
        val properties: MutableList<String?> =
            ArrayList()
        if (isCharacteristicReadable(characteristic)) properties.add("Read")
        if (isCharacteristicWriteable(characteristic)) properties.add("Write")
        if (isCharacteristicNotifiable(characteristic)) properties.add("Notify")
        return TextUtils.join(" ", properties)
    }

    private fun getServiceType(service: BluetoothGattService): String {
        return if (service.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) "primary" else "secondary"
    }

    private fun isCharacteristicNotifiable(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    }

    private fun isCharacteristicReadable(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0
    }

    private fun isCharacteristicWriteable(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE
                or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
    }

    inner class DiscoveredServiceViewHolder(var binding: ItemNordicDiscoverServiceBinding) :
        ViewHolder(binding.root) {
        fun onBind(
            item: ServiceItem,
            listener: ServiceItemClickListener
        ) {
            binding.item = item
            binding.listener = object :
                ServiceItemClickListener {
                override fun onItemClicked(item: ServiceItem) {
                    listener.onItemClicked(item)

                    if (item.type == ServiceItem.SERVICE) return

                    if (item.description.contains("Write") && binding.layoutMsg.visibility == View.GONE)
                        binding.layoutMsg.visibility = View.VISIBLE
                    else if (item.description.contains("Write") && binding.layoutMsg.visibility == View.VISIBLE)
                        binding.layoutMsg.visibility = View.GONE
                }

                override fun onConnectClicked(
                    checked: Boolean,
                    uuid: UUID
                ) {
                    listener.onConnectClicked(checked, uuid)
                }

                override fun onReadBtnClicked(
                    uuid: UUID
                ) {
                    listener.onReadBtnClicked(uuid)
                    Log.d("Item", "Read Clicked")
                }

                override fun onWriteBtnClicked(
                    uuid: UUID,
                    msg: String?
                ) {
                    listener.onWriteBtnClicked(uuid, binding.etMsg.text.toString())
                    Log.d("Item", "Write Clicked")
                }

                override fun onNotiBtnClicked(
                    isChecked: Boolean,
                    uuid: UUID
                ) {
                    listener.onNotiBtnClicked(isChecked, uuid)
                    Log.d("Item", "Noti Clicked")
                }
            }

//            if(item.type == ServiceItem.CHARACTERISTIC)
//                binding.switchConnect.setVisibility(View.VISIBLE);
//            else
//                binding.switchConnect.setVisibility(View.GONE);

            binding.btnRead.visibility = if (item.description.contains("Read")) View.VISIBLE else View.GONE
            binding.btnNoti.visibility = if (item.description.contains("Notify")) View.VISIBLE else View.GONE
            binding.layoutMsg.visibility = if (item.description.contains("Write")) View.VISIBLE else View.GONE
        }
    }

    init {
        setHasStableIds(true)
    }
}