package kr.ac.tukorea.greenapple.sidoli.api_directions

import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsAPI {
    @GET("json")

    fun getDirection(
        @Query("origin") origin:String,
        @Query("destination") destination:String,
        @Query("mode") type:String="transit",
        @Query("departure_time") time:String="now",
        @Query("key") api_key:String = "AIzaSyC0sSlFxHsynAEiv8c--OZq8tyLu6PMLLo"    // direction api_key
    ):retrofit2.Call<DirectionsData>
}