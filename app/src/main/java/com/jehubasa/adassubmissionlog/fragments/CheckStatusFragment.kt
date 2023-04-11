package com.jehubasa.adassubmissionlog.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jehubasa.adassubmissionlog.QrGenDataBaseHelper
import com.jehubasa.adassubmissionlog.R
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import com.jehubasa.adassubmissionlog.databinding.FragmentCheckStatusBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class CheckStatusFragment : Fragment() {

    private lateinit var binding: FragmentCheckStatusBinding
    private lateinit var dataBaseHelper: QrGenDataBaseHelper
    private var data: List<SubmissionDataClass> = listOf()
    private var fabState = false
    private val dbRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_liquidationLog_ref))
    }
    private var filteredDates: MutableList<String> = mutableListOf()

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

        dbRef.keepSynced(true)
        Log.d("ASP", "Firebase UID: ${FirebaseAuth.getInstance().currentUser?.uid}")

        with(binding) {
            statusDateFilterButton.setOnClickListener {
                openCalendar()
            }

            statusPrintButton.setOnClickListener {
                if (data.isNotEmpty()) {
                    saveData()
                } else {
                    Toast.makeText(requireContext(), "No Data", Toast.LENGTH_LONG).show()
                }
            }

            statusFilterOption.setOnClickListener {
                if (!fabState) {
                    statusDateFilterButton.show()
                    statusPrintButton.show()
                    statusSchoolFilterButton.show()
                    statusDeleteButton.show()
                    fabState = true
                } else {
                    statusDateFilterButton.hide()
                    statusPrintButton.hide()
                    statusSchoolFilterButton.hide()
                    statusDeleteButton.hide()
                    fabState = false
                }
            }

            statusSchoolFilterButton.setOnClickListener {
                schoolFilter()
            }

            statusDeleteButton.setOnClickListener {
                if (data.isNotEmpty() && filteredDates.isNotEmpty()) {
                    deleteData()
                } else {
                    Toast.makeText(requireContext(), "No Data", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteData() {
        com.jehubasa.adassubmissionlog.FirebaseDatabase()
            .deleteDataSubmissionDateRange(dbRef, filteredDates[0], filteredDates[1]) {
                if (it) {
                    dbRef.keepSynced(true)
                    Toast.makeText(requireContext(), "Data Deleted", Toast.LENGTH_LONG).show()
                    binding.statusTableLayout.invalidate()
                    retrieveData(filteredDates[0], filteredDates[1])
                    filteredDates.clear()
                }
            }
    }

    private fun schoolFilter() {
        dbRef.keepSynced(true)
        val view = layoutInflater.inflate(R.layout.alert_dialog_school_filter, null)
        val editText = view.findViewById<MaterialAutoCompleteTextView>(R.id.filter_school)
        //fill the autocomplete of schools
        val schools: ArrayList<String?> = arrayListOf()
        com.jehubasa.adassubmissionlog.FirebaseDatabase().fetchDataQR(
            FirebaseDatabase.getInstance()
                .getReference(getString(R.string.firebase_qrdata_ref))
        ) {
            for (content in it) {
                if (!schools.contains(content.sch_name)) {
                    schools += content.sch_name
                }
            }
            schools.let { sch ->
                editText?.setSimpleItems(
                    sch.toTypedArray()
                )
            }
        }

        val alertDialog = MaterialAlertDialogBuilder(requireContext()).create().also {
            it.setTitle("School Name Filter")
            it.setIcon(R.drawable.institution)
            it.setMessage("Select the school you want to view the records.")
            it.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { _, _ ->
                Log.d("ASP", editText.text.toString())
                com.jehubasa.adassubmissionlog.FirebaseDatabase()
                    .fetchDataSubmissionSchool(dbRef, editText.text.toString()) { schoolFiltered ->
                        binding.statusTableLayout.removeViews(
                            1,
                            max(0, binding.statusTableLayout.childCount - 1)
                        )
                        loadTOTable(schoolFiltered)
                    }
            }
        }
        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun saveData() {
        // Measure the layout
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        binding.statusTableLayout.measure(widthMeasureSpec, heightMeasureSpec)
        val width = binding.statusTableLayout.measuredWidth
        val height = binding.statusTableLayout.measuredHeight

        //convert table to PDF
        val document = PdfDocument()
        val pageInfo =
            PdfDocument.PageInfo.Builder(width, height, 2)
                .create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        binding.statusTableLayout.draw(canvas)
        document.finishPage(page)

        val file = File(context?.cacheDir, "table.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.jehubasa.adassubmissionlog" + ".provider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context?.startActivity(Intent.createChooser(intent, "Send PDF"))
    }


    private fun openCalendar() {
        filteredDates.clear()
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
            binding.statusProgress.visibility = View.VISIBLE
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date1 = dateFormat.format(Date(it.first))
            val date2 = dateFormat.format(Date(it.second))
            binding.statusTableLayout.removeViews(
                1,
                max(0, binding.statusTableLayout.childCount - 1)
            )
            retrieveData(date1, date2)
            filteredDates.add(date1)
            filteredDates.add(date2)
        }

    }

    private fun retrieveData(date1: String, date2: String) {
        dbRef.keepSynced(true)
        com.jehubasa.adassubmissionlog.FirebaseDatabase()
            .fetchDataSubmissionDateRange(dbRef, date1, date2) {
                loadTOTable(it)
            }
    }

    private fun loadTOTable(it: ArrayList<SubmissionDataClass>) {
        if (it.isNotEmpty()) {
            binding.statusProgress.visibility = View.GONE
            binding.statusInstruction.visibility = View.GONE
            val tableLayout = binding.statusTableLayout

            for (d in it.indices) {
                val dataRow = TableRow(context)
                dataRow.addView(TextView(context).apply {
                    text = it[d].sch
                    setTextColor(Color.BLACK)
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })
                dataRow.addView(TextView(context).apply {
                    text = it[d].typ
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                dataRow.addView(TextView(context).apply {
                    text = it[d].ds
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                dataRow.addView(TextView(context).apply {
                    text = it[d].dr
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                dataRow.addView(TextView(context).apply {
                    text = it[d].tos.toString()
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                dataRow.addView(TextView(context).apply {
                    text = it[d].sb
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                dataRow.addView(TextView(context).apply {
                    text = it[d].rt
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                dataRow.addView(TextView(context).apply {
                    text = it[d].tsd
                    setPadding(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                })

                tableLayout.addView(dataRow)
                data = it
                binding.statusNoItems.text = it.size.toString()
            }
        } else {
            Toast.makeText(requireContext(), "No existing data", Toast.LENGTH_LONG).show()
            filteredDates.clear()
            binding.statusProgress.visibility = View.GONE
            binding.statusInstruction.visibility = View.VISIBLE
            binding.statusNoItems.text = ""
            for (i in 1 until binding.statusTableLayout.childCount) {
                val row = binding.statusTableLayout.getChildAt(i)
                binding.statusTableLayout.removeView(row)
            }
        }
    }
}