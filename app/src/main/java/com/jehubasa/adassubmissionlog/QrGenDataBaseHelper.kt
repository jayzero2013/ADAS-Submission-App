package com.jehubasa.adassubmissionlog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jehubasa.adassubmissionlog.data.QrInfoDataClass

class QrGenDataBaseHelper(context: Context?) : SQLiteOpenHelper(
    context,
    "ASP_DATABASE", null, 1
) {

    private val MY_TABLE = "QRinfo"
    private val SCH_NAME = "sch_name"
    private val SCH_HEAD = "sch_head"
    private val SCH_ID = "sch_id"

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = "CREATE TABLE $MY_TABLE ($SCH_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$SCH_NAME TEXT, $SCH_HEAD TEXT)"
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $MY_TABLE")
        onCreate(db)
    }

    fun insertDate(db: SQLiteDatabase?, data: Array<QrInfoDataClass>): Long {
        for (d in data) {
            val values = ContentValues().apply {
                put(SCH_NAME, d.sch_name)
                put(SCH_HEAD, d.sch_head)
            }
            return db?.insert(MY_TABLE, null, values)!!
        }
        return -1
    }

    fun queryData(db: SQLiteDatabase?, data: Array<String>): QrInfoDataClass{
        val projection = arrayOf(SCH_ID, SCH_NAME, SCH_HEAD)

        val cursor = db?.query(
            MY_TABLE, projection, "$SCH_NAME = ?",
            data, null, null, null
        )

        with(cursor) {
            while (this!!.moveToNext()) {
                if (data[0] == getString(getColumnIndexOrThrow("sch_name"))) {
                    return QrInfoDataClass(
                        getInt(getColumnIndexOrThrow(SCH_ID)),
                        getString(getColumnIndexOrThrow(SCH_NAME)),
                        getString(getColumnIndexOrThrow(SCH_HEAD))
                    )
                    break
                }
            }
        }

        cursor?.close()
        return QrInfoDataClass(null, null, null)

    }

    fun updateData(db: SQLiteDatabase?, data: Array<QrInfoDataClass>): Int? {
        val values = ContentValues().apply {
            put(SCH_NAME, data[0].sch_name)
            put(SCH_HEAD, data[0].sch_head)
        }
        val selection = "$SCH_ID=?"
        return db?.update(MY_TABLE, values, selection, arrayOf(data[0].sch_id.toString()))
    }

}