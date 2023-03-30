package com.jehubasa.adassubmissionlog.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
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
import com.google.android.material.transition.MaterialFadeThrough
import com.jehubasa.adassubmissionlog.QrGenDataBaseHelper
import com.jehubasa.adassubmissionlog.R
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import com.jehubasa.adassubmissionlog.databinding.FragmentCheckStatusBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CheckStatusFragment : Fragment() {

    private lateinit var binding: FragmentCheckStatusBinding
    private lateinit var dataBaseHelper: QrGenDataBaseHelper
    private var data: List<SubmissionDataClass> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()

    }

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

        binding.statusDateSaveButton.setOnClickListener {
            if (data.isNotEmpty()) {
                saveData()
            } else {
                Toast.makeText(requireContext(), "Not Data", Toast.LENGTH_LONG).show()
            }
        }
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
        data = dataBaseHelper.queryDateRange(
            dataBaseHelper.readableDatabase,
            arrayOf(getString(R.string.submDate), date1, date2)
        )

        val tableLayout = binding.statusTableLayout

        for (d in data.indices) {
            val dataRow = TableRow(context)
            dataRow.addView(TextView(context).apply {
                text = data[d].Sch
                setTextColor(Color.BLACK)
                setPadding(10, 10, 10, 10)
            })
            dataRow.addView(TextView(context).apply {
                text = data[d].typ
                setPadding(10, 10, 10, 10)
            })

            dataRow.addView(TextView(context).apply {
                text = data[d].ds
                setPadding(10, 10, 10, 10)
            })

            dataRow.addView(TextView(context).apply {
                text = data[d].dr
                setPadding(10, 10, 10, 10)
            })

            dataRow.addView(TextView(context).apply {
                text = data[d].tos.toString()
                setPadding(10, 10, 10, 10)
            })

            dataRow.addView(TextView(context).apply {
                text = data[d].sb
                setPadding(10, 10, 10, 10)
            })

            dataRow.addView(TextView(context).apply {
                text = data[d].rt
                setPadding(10, 10, 10, 10)
            })

            dataRow.addView(TextView(context).apply {
                text = data[d].tsd
                setPadding(10, 10, 10, 10)
            })

            tableLayout.addView(dataRow)
        }
    }
}