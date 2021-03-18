package com.wotosts.blesample.rx.scan

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.scan.ScanResult
import com.wotosts.blesample.databinding.ItemRxbleScanBinding

class ScanResultAdapter(private var list: List<ScanResult>,
                        private var listener: ItemClickListener?) :
    RecyclerView.Adapter<ScanResultAdapter.ScanResultViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScanResultViewHolder {
        val binding: ItemRxbleScanBinding =
            ItemRxbleScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ScanResultViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: ScanResultViewHolder,
        position: Int
    ) {
        holder.onBind(list[position])
    }

    fun updateList(newList: List<ScanResult>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return list[oldItemPosition].bleDevice.macAddress == newList[newItemPosition].bleDevice.macAddress
            }

            override fun getOldListSize(): Int = list.size

            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldItemPosition, newItemPosition)
            }
        })

        diffResult.dispatchUpdatesTo(this)
        list = newList

        Log.d("test", "${list.size} update!!")
    }

    class ScanResultViewHolder(
        var binding: ItemRxbleScanBinding,
        var listener: ItemClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(result: ScanResult) {
            binding.scanResult = result
            binding.listener = listener
        }
    }

    public interface ItemClickListener {
        fun onClicked(result: ScanResult)
    }
}