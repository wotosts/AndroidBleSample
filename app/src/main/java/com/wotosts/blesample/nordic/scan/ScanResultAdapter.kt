package com.wotosts.blesample.nordic.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.wotosts.blesample.databinding.ItemNordicScanBinding
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ScanResultAdapter(private var list: List<ScanResult>,
                        private var listener: ItemClickListener?) :
    RecyclerView.Adapter<ScanResultAdapter.ScanResultViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScanResultViewHolder {
        val binding: ItemNordicScanBinding =
            ItemNordicScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ScanResultViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: ScanResultViewHolder,
        position: Int
    ) {
        holder.onBind(list[position])
    }

    fun updateList(newList: List<ScanResult>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return list[oldItemPosition].device.address == newList[newItemPosition].device.address
            }

            override fun getOldListSize(): Int = list.size

            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldItemPosition, newItemPosition)
            }
        })

        diffResult.dispatchUpdatesTo(this)
        list = newList
    }

    class ScanResultViewHolder(
        var binding: ItemNordicScanBinding,
        var listener: ItemClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(result: ScanResult) {
            binding.scanResult = result
            binding.listener = listener
        }
    }

    interface ItemClickListener {
        fun onClicked(result: ScanResult)
    }
}