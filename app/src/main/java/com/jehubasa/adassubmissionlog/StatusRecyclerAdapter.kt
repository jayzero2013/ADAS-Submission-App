package com.jehubasa.adassubmissionlog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import com.jehubasa.adassubmissionlog.databinding.RecyclerStatusBinding

class StatusRecyclerAdapter(val data: List<SubmissionDataClass>) :
    RecyclerView.Adapter<StatusRecyclerAdapter.VH>() {

    private lateinit var binding : RecyclerStatusBinding

    class VH(itemView: RecyclerStatusBinding) : RecyclerView.ViewHolder(itemView.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = RecyclerStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        with(binding){
            statusSchName.text = data[position].Sch
            statusTypeLr.text= data[position].typ
            statusDivStatus.text= data[position].tsd
            statusReleasedTo.text=data[position].rt
            statusReleasedDate.text=data[position].dr
            statusSubmissionDate.text=data[position].ds
            statusSubmitedBy.text=data[position].sb
            when(data[position].tos){
                1 ->{
                    statusTimeSubmitted.text="${data[position].tos}st submission"
                }
                2 ->{
                    statusTimeSubmitted.text="${data[position].tos}nd submission"
                }
                3 ->{
                    statusTimeSubmitted.text="${data[position].tos}rd submission"
                }
                4 ->{
                    statusTimeSubmitted.text="${data[position].tos}th submission"
                } else -> {statusTimeSubmitted.text="${data[position].tos}th submission"}
            }
        }
    }
}