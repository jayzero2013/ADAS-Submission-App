package com.jehubasa.adassubmissionlog.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jehubasa.adassubmissionlog.R
import com.jehubasa.adassubmissionlog.RecyclerAdapterQrScan
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import com.jehubasa.adassubmissionlog.databinding.FragmentDialogScanQrBinding

class QrScanDialogFragment : DialogFragment(), RecyclerAdapterQrScan.OnItemDialogClickLister {

    private lateinit var binding: FragmentDialogScanQrBinding
    private var listener: OnDialogExitListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = FragmentDialogScanQrBinding.inflate(requireActivity().layoutInflater)

        val labelData = runRecycler()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Found old records of ${labelData[0]}")
            .setMessage("Number of data: ${labelData[1]}")
            .create()
    }

    private fun runRecycler(): List<String?> {
        val data =
            arguments?.getParcelableArrayList<SubmissionDataClass>(getString(R.string.old_data))

        data.let {
            RecyclerAdapterQrScan(data as List<SubmissionDataClass>).also {
                binding.qrScanOldDataList.adapter = it
            }.setOnItemClickListener(this)

            binding.qrScanOldDataList.layoutManager = LinearLayoutManager(requireContext())
            return listOf(data[0].Sch, data.size.toString())
        }

    }

    override fun onItemClick(data: SubmissionDataClass) {
        Log.d("ASP", data.toString())

        listener?.exitListener(data)

        dismiss()
    }

    fun setOnDialogExitListener(l: OnDialogExitListener) {
        listener = l
    }

    interface OnDialogExitListener {
        fun exitListener(data: SubmissionDataClass)
    }
}