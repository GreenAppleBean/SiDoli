package kr.ac.tukorea.greenapple.sidoli.api_police

import com.google.gson.annotations.SerializedName


data class PoliceData(
    val Ptrldvsnsubpolcstus: ArrayList<PtrldvsnsubpolcstusData>,
)
data class PtrldvsnsubpolcstusData(
    @SerializedName("head")
    val head:ArrayList<headData>,
    @SerializedName("row")
    val row: ArrayList<rowData>,
)
data class headData(
    @SerializedName("list_total_count")
    val list_total_count:Int,
)
data class rowData(
    val GOVOFC_NM:String,
    val DIV_NM:String,
    val REFINE_WGS84_LOGT:String,
    val REFINE_WGS84_LAT:String,
)
