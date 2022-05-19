package kr.ac.tukorea.greenapple.sidoli.api_police

import retrofit2.http.GET
import retrofit2.http.Query

interface PoliceAPI {
    @GET("Ptrldvsnsubpolcstus")

    fun getPolice(
        @Query("KEY") service_key:String = "9a7045bafef9438ca7926128568b25ce",
        @Query("Type") data_type:String = "json",
        @Query("pIndex")page_no:Int,
        @Query("pSize")number_of_rows:Int,
        @Query("SIGUN_NM")sigun_name:String="시흥시",
    ):retrofit2.Call<PoliceData>
}