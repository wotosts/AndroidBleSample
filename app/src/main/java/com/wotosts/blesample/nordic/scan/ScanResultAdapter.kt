package com.wotosts.blesample.nordic.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wotosts.blesample.databinding.ItemNordicScanBinding
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ScanResultAdapter(private var list: List<ScanResult>, private var listener: ItemClickListener?) :
    RecyclerView.Adapter<ScanResultAdapter.ScanResultViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScanResultViewHolder {
        val binding: ItemNordicScanBinding =
            ItemNordicScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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

    fun updateList(list: List<ScanResult>) {
        this.list = list

        notifyDataSetChanged()
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

    public interface ItemClickListener {
        fun onClicked(result: ScanResult)
    }
}