package com.jehubasa.adassubmissionlog.data


data class SubmissionDataClass(
    val Sch: String?,//school name
    val typ: String?,//type of LR
    val ds: String?,//submission date
    val dr: String?,//Released date
    val tos: Int?,//times of submission
    val sb: String?,//submitted by
    val rt: String?,//released to whom
    val sd: Boolean?,//submitted to division
    val tsd: String?//time submitted to division
)
