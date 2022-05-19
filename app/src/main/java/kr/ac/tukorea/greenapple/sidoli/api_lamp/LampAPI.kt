package kr.ac.tukorea.greenapple.sidoli.api_lamp

import retrofit2.http.GET
import retrofit2.http.Query

interface LampAPI {
    @GET("tn_pubr_public_scrty_lmp_api")

    fun getLamp(
        @Query("serviceKey") service_key: String = "0IfyY7c/qyoIT0yjBurlux1vP7uzsfSVrb1dVg22jmwNPh+DYJuyIluDgii4ewEB7+kzX/kKgqhdDx9VQVf8Yg==",
        @Query("pageNo") page_no: Int,
        @Query("numOfRows") number_of_rows: Int,
        @Query("type") data_type:String = "json",
        @Query("institutionNm") institution_Name:String = "경기도 시흥시청",
    ):retrofit2.Call<LampData>
}