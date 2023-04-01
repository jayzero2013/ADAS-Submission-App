package com.jehubasa.adassubmissionlog.data

import android.os.Parcel
import android.os.Parcelable


data class SubmissionDataClass(
    val id: String? = null,
    val sch: String?= null,//school name
    val typ: String?= null,//type of LR
    val ds: String?= null,//submission date
    val dr: String?= null,//Released date
    val tos: Int?= null,//times of submission
    val sb: String?= null,//submitted by
    val rt: String?= null,//released to whom
    val sd: String?= null,//submitted to division
    val tsd: String?= null//time submitted to division
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(sch)
        parcel.writeString(typ)
        parcel.writeString(ds)
        parcel.writeString(dr)
        parcel.writeValue(tos)
        parcel.writeString(sb)
        parcel.writeString(rt)
        parcel.writeString(sd)
        parcel.writeString(tsd)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SubmissionDataClass> {
        override fun createFromParcel(parcel: Parcel): SubmissionDataClass {
            return SubmissionDataClass(parcel)
        }

        override fun newArray(size: Int): Array<SubmissionDataClass?> {
            return arrayOfNulls(size)
        }
    }
        fun toMap(): Map<String, Any> {
            val result = HashMap<String, Any>()
            result["id"] = id!!
            result["sch"] = sch!!
            result["typ"] = typ!!
            result["ds"] = ds!!
            result["dr"] = dr!!
            result["tos"] = tos!!
            result["sb"] = sb!!
            result["rt"] = rt!!
            result["sd"] = sd!!
            result["tsd"] = tsd!!
            return result

    }
}
