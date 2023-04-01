package com.jehubasa.adassubmissionlog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jehubasa.adassubmissionlog.data.QrInfoDataClass
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import kotlin.collections.MutableList

class QrGenDataBaseHelper(context: Context?) : SQLiteOpenHelper(
    context,
    "ASP_DATABASE", null, 1
) {
    private val table1 = context?.getString(R.string.qrInfoTable)
    private val schName = context?.getString(R.string.sch_name)
    private val schHead = context?.getString(R.string.sch_head)
    private val schId = context?.getString(R.string.sch_id)

    private val table2 = context?.getString(R.string.submissionTable)
    private val schName2 = context?.getString(R.string.sch_name)
    private val lrType = context?.getString(R.string.lrType)
    private val submDate = context?.getString(R.string.submDate)
    private val releDate = context?.getString(R.string.releDate)
    private val timesSubm = context?.getString(R.string.timesSubm)
    private val submBy = context?.getString(R.string.submBy)
    private val releWhom = context?.getString(R.string.releWhom)
    private val submDiv = context?.getString(R.string.submDiv)
    private val whenSubmDiv = context?.getString(R.string.whenSubmDiv)

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $table1 ($schId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$schName TEXT, $schHead TEXT)"

        val createTable2 = "CREATE TABLE $table2 (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$schName2 TEXT, $lrType TEXT, $submDate DATE, $releDate DATE, $timesSubm INT, " +
                "$submBy TEXT, $releWhom TEXT, $submDiv TEXT, $whenSubmDiv DATE)"
        db?.execSQL(createTable)
        db?.execSQL(createTable2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $table1")
        db?.execSQL("DROP TABLE IF EXISTS $table2")
        onCreate(db)
    }

    fun insertDateAtTable1(db: SQLiteDatabase?, data: Array<QrInfoDataClass>): Long {
        for (d in data) {
            val values = ContentValues().apply {
                put(schName, d.sch_name)
                put(schHead, d.sch_head)
            }
            return db?.insert(table1, null, values)!!
        }
        return -1
    }

    fun queryDataAtTable1(db: SQLiteDatabase?, column: Array<String>): List<String?> {
        val data: MutableList<String?> = mutableListOf()
        val cursor = db?.query(
            table1, column, null,
            null, null, null, null
        )

        with(cursor) {
            if (column.size == 1) {
                while (this!!.moveToNext()) {
                    val s = getString(getColumnIndexOrThrow(column[0]))
                    if (!data.contains(s)) {
                        data += s
                    }
                }
            } else if (column.size == 2) {
                while (this!!.moveToNext()) {
                    val s = getString(getColumnIndexOrThrow(column[0]))
                    if (!data.contains(s)) {
                        data += s
                        data += getString(getColumnIndexOrThrow(column[1]))
                    }
                }
            }
        }

        cursor?.close()
        return data

    }


    fun insertDateAtTable2(db: SQLiteDatabase?, data: Array<SubmissionDataClass>): Long {
        for (d in data) {
            val values = ContentValues().apply {
                put(schName2, d.sch)
                put(lrType, d.typ)
                put(submDate, d.ds)
                put(releDate, d.dr)
                put(timesSubm, d.tos)
                put(submBy, d.sb)
                put(releWhom, d.rt)
                put(submDiv, d.sd)
                put(whenSubmDiv, d.tsd)
            }
            return db?.insert(table2, null, values)!!
        }
        return -1
    }

    fun queryDataAtTable2(db: SQLiteDatabase?, column: Array<String>): List<SubmissionDataClass> {
        val cursor = db?.query(
            table2, null, "$schName = ?",
            column, null, null, null
        )
        val data: MutableList<SubmissionDataClass> = mutableListOf()

        with(cursor) {
            while (this!!.moveToNext()) {
                data.add(
                    SubmissionDataClass(
                        getString(getColumnIndexOrThrow("id")),
                        getString(getColumnIndexOrThrow(schName2)),
                        getString(getColumnIndexOrThrow(lrType)),
                        getString(getColumnIndexOrThrow(submDate)),
                        getString(getColumnIndexOrThrow(releDate)),
                        getInt(getColumnIndexOrThrow(timesSubm)),
                        getString(getColumnIndexOrThrow(submBy)),
                        getString(getColumnIndexOrThrow(releWhom)),
                        getString(getColumnIndexOrThrow(submDiv)),
                        getString(getColumnIndexOrThrow(whenSubmDiv))
                    )
                )
            }
        }

        cursor?.close()
        return data
    }

    fun queryDateRange(db: SQLiteDatabase?, column: Array<String>): List<SubmissionDataClass> {

        val query =
            "SELECT * FROM $table2 WHERE ${column[0]} BETWEEN '${column[1]}' AND '${column[2]}'"

        val cursor = db?.rawQuery(query, null)
        val data: MutableList<SubmissionDataClass> = mutableListOf()

        with(cursor) {
            while (this!!.moveToNext()) {
                data.add(
                    SubmissionDataClass(
                        getString(getColumnIndexOrThrow("id")),
                        getString(getColumnIndexOrThrow(schName2)),
                        getString(getColumnIndexOrThrow(lrType)),
                        getString(getColumnIndexOrThrow(submDate)),
                        getString(getColumnIndexOrThrow(releDate)),
                        getInt(getColumnIndexOrThrow(timesSubm)),
                        getString(getColumnIndexOrThrow(submBy)),
                        getString(getColumnIndexOrThrow(releWhom)),
                        getString(getColumnIndexOrThrow(submDiv)),
                        getString(getColumnIndexOrThrow(whenSubmDiv))
                    )
                )
            }
        }

        cursor?.close()
        return data
    }


    fun updateDataAtTable2(db: SQLiteDatabase?, id: Int?, d: SubmissionDataClass?): Int? {
        val values = ContentValues().apply {
            put(schName2, d?.sch)
            put(lrType, d?.typ)
            put(submDate, d?.ds)
            put(releDate, d?.dr)
            put(timesSubm, d?.tos)
            put(submBy, d?.sb)
            put(releWhom, d?.rt)
            put(submDiv, d?.sd)
            put(whenSubmDiv, d?.tsd)
        }
        val selection = "id=?"
        return db?.update(table2, values, selection, arrayOf(id.toString()))
    }

}