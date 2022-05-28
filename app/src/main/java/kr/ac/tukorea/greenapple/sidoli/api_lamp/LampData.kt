package kr.ac.tukorea.greenapple.sidoli.api_lamp

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem

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
) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(latitude.toDouble(), longitude.toDouble()) // 개별 마커가 표시될 좌표
    }
    override fun getTitle(): String? {
        return lmpLcNm // 마커를 클릭했을 때 나타나는 타이틀
    }
    override fun getSnippet(): String? {
        return lmpLcNm // 마커를 클릭했을 때 나타나는 서브타이틀
    }
}
