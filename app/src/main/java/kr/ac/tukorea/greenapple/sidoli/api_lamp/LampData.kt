package kr.ac.tukorea.greenapple.sidoli.api_lamp

import com.google.gson.annotations.SerializedName

data class LampData(
    @SerializedName(value = "response")
    val response: LampResponseData,
)
data class LampResponseData(
    @SerializedName("header")
    val header:LampHeadData,
    @SerializedName("body")
    val body: LampBodyData,
)
data class LampHeadData(
    @SerializedName("resultCode")
    val resultCode:String
)
data class LampBodyData(
    @SerializedName(value = "items")
    val Items: ArrayList<LampItemData>,
)

data class LampItemData(
    val lmpLcNm:String,
    val installationCo:Int,
    val latitude: String,
    val longitude: String,
)
