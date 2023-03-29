package com.jehubasa.adassubmissionlog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.jehubasa.adassubmissionlog.QrGenDataBaseHelper
import com.jehubasa.adassubmissionlog.R
import com.jehubasa.adassubmissionlog.StatusRecyclerAdapter
import com.jehubasa.adassubmissionlog.databinding.FragmentCheckStatusBinding
import java.text.SimpleDateFormat
import java.util.*

class CheckStatusFragment : Fragment() {

    private lateinit var binding: FragmentCheckStatusBinding
    private lateinit var dataBaseHelper: QrGenDataBaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dataBaseHelper = QrGenDataBaseHelper(requireContext())

        binding = FragmentCheckStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statusDateFilterButton.setOnClickListener {
            openCalendar()
        }
    }

    private fun openCalendar() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date range")
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            .build()
        dateRangePicker.show(parentFragmentManager, "date_range")
        dateRangePicker.addOnPositiveButtonClickListener {

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date1 = dateFormat.format(Date(it.first))
            val date2 = dateFormat.format(Date(it.second))
            retrieveData(date1, date2)
        }

    }

    private fun retrieveData(date1: String, date2: String) {
        val data = dataBaseHelper.queryDateRange(
            dataBaseHelper.readableDatabase,
            arrayOf(getString(R.string.submDate), date1, date2)
        )
        binding.statusNoItems.text = data.size.toString()
        with(binding.statusRecycleView) {
            adapter = StatusRecyclerAdapter(data)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}