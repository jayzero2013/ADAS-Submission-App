package com.jehubasa.adassubmissionlog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import com.jehubasa.adassubmissionlog.databinding.RecyclerOldDataBinding

class RecyclerAdapterQrScan(val data: List<SubmissionDataClass>) :
    RecyclerView.Adapter<RecyclerAdapterQrScan.VH>() {

    private lateinit var binding: RecyclerOldDataBinding
    private var listener: OnItemDialogClickLister? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = RecyclerOldDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        binding.recyclerSchName.text = data[position].sch
        binding.recyclerLrType.text = data[position].typ
        binding.recyclerDivStatus.text = data[position].sd.let {
            if(it == "true"){
                "Yes"
            } else {"Not Yet"}
        }
        binding.recyclerTimesSubmit.text = data[position].tos.toString()
        when (data[position].tos) {
            1 -> {
                binding.recyclerTimesSubmit.text = ("${data[position].tos}st submit")
            }
            2 -> {
                binding.recyclerTimesSubmit.text = ("${data[position].tos}nd submit")
            }
            3 -> {
                binding.recyclerTimesSubmit.text = ("${data[position].tos}rd submit")
            }
            4 -> {
                binding.recyclerTimesSubmit.text = ("${data[position].tos}th submit")
            }
            else -> {
                binding.recyclerTimesSubmit.text = ("${data[position].tos}th submit")
            }
        }

        holder.bind(data[position], listener!!)
    }

    fun setOnItemClickListener(listener: OnItemDialogClickLister) {
        this.listener = listener
    }

    class VH(private val v: RecyclerOldDataBinding) : RecyclerView.ViewHolder(v.root) {

        fun bind(data: SubmissionDataClass, listener: OnItemDialogClickLister) {
            v.root.setOnClickListener { listener.onItemClick(data) }
        }
    }

    interface OnItemDialogClickLister {
        fun onItemClick(data: SubmissionDataClass)
    }
}