package com.jehubasa.adassubmissionlog.data

import android.os.Parcel
import android.os.Parcelable


data class SubmissionDataClass(
    val Sch: String?,//school name
    val typ: String?,//type of LR
    val ds: String?,//submission date
    val dr: String?,//Released date
    val tos: Int?,//times of submission
    val sb: String?,//submitted by
    val rt: String?,//released to whom
    val sd: String?,//submitted to division
    val tsd: String?//time submitted to division
) : Parcelable{
    constructor(parcel: Parcel) : this(
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
        parcel.writeString(Sch)
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

}
