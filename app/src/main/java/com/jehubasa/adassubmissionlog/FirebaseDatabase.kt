package com.jehubasa.adassubmissionlog

import android.util.Log
import com.google.firebase.database.*
import com.jehubasa.adassubmissionlog.data.QrInfoDataClass
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass

class FirebaseDatabase() {

    fun initQrDatabase(db: DatabaseReference, data: QrInfoDataClass) {

        db.child(data.id!!).setValue(data).addOnCompleteListener {
            Log.d("ASP", "Data inserted to internet ( ${data.toString()} )")
        }.addOnFailureListener {
            Log.d("ASP", "Data insertion failed ( ${data.toString()} )")
        }
    }

    fun initLiquidationDatabase(
        db: DatabaseReference,
        data: SubmissionDataClass,
        callback: (Boolean) -> Unit
    ) {
        db.child(data.id!!).setValue(data).addOnCompleteListener {
            Log.d("ASP", "Data inserted to internet ( ${data.toString()} )")
            callback(true)
        }.addOnFailureListener {
            Log.d("ASP", "Data insertion failed ( ${data.toString()} )")
            callback(false)
        }
    }

    fun fetchDataQR(dbRef: DatabaseReference, callback: (ArrayList<QrInfoDataClass>) -> Unit) {
        val temp: ArrayList<QrInfoDataClass> = arrayListOf()
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val data = snap.getValue(QrInfoDataClass::class.java)
                        Log.d("ASP", "fetch data $data")
                        temp.add(data!!)
                    }
                    callback(temp)

                } else {
                    Log.d("ASP", "No Data to fetch in QR")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ASP", "data fetch error")
            }
        })
    }


    fun fetchDataSubmission(
        dbRef: DatabaseReference,
        filter: String,
        callback: (ArrayList<SubmissionDataClass>) -> Unit
    ) {
        val temp: ArrayList<SubmissionDataClass> = arrayListOf()

        val query = dbRef.orderByChild("sch").equalTo(filter)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val data = snap.getValue(SubmissionDataClass::class.java)
                        Log.d("ASP", "fetch data $data")
                        temp.add(data!!)
                    }
                    callback(temp)

                } else {
                    Log.d("ASP", "No Data to fetch in Submission")
                    callback(temp)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ASP", "data fetch error")
            }
        })
    }

    fun updateDataSubmission(
        dbRef: DatabaseReference,
        parent: String,
        id: String,
        data: SubmissionDataClass,
        callback: (Boolean) -> Unit
    ) {
        val updateMap: Map<String, Any> = data.toMap()
        dbRef.child(id).setValue(updateMap)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(
                    "ASP",
                    "Data updated successfully ${
                        snapshot.getValue(SubmissionDataClass::class.java).toString()
                    }"
                )
                callback(true)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ASP", "Data update failed: ${error.message}")
                callback(false)
            }
        })
    }

    fun fetchDataSubmissionDateRange(
        dbRef: DatabaseReference,
        startDate: String,
        endDate: String,
        callback: (ArrayList<SubmissionDataClass>) -> Unit
    ) {
        val temp: ArrayList<SubmissionDataClass> = arrayListOf()

        val query = dbRef.orderByChild("ds").startAt(startDate).endAt(endDate)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val data = snap.getValue(SubmissionDataClass::class.java)
                        Log.d("ASP", "fetch data $data")
                        temp.add(data!!)
                    }
                    callback(temp)

                } else {
                    Log.d("ASP", "No Data to fetch in Submission")
                    callback(temp)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ASP", "data fetch error")
            }
        })
    }

    fun deleteDataSubmissionDateRange(
        dbRef: DatabaseReference,
        startDate: String,
        endDate: String,
        callback: (Boolean) -> Unit
    ) {
        val query = dbRef.orderByChild("ds").startAt(startDate).endAt(endDate)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        snap.ref.removeValue()
                    }
                    callback(true)

                } else {
                    Log.d("ASP", "Error on deleting data.")
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ASP", "data fetch error")
            }
        })
    }

    fun fetchDataSubmissionSchool(
        dbRef: DatabaseReference,
        sch: String,
        callback: (ArrayList<SubmissionDataClass>) -> Unit
    ) {
        val temp: ArrayList<SubmissionDataClass> = arrayListOf()

        val query = dbRef.orderByChild("sch").equalTo(sch)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val data = snap.getValue(SubmissionDataClass::class.java)
                        Log.d("ASP", "fetch data $data")
                        temp.add(data!!)
                    }
                    callback(temp)

                } else {
                    Log.d("ASP", "No Data to fetch in Submission")
                    callback(temp)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ASP", "data fetch error")
            }
        })
    }
}