package kr.ac.tukorea.greenapple.sidoli.api_directions

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DirectionsRetrofitClient {
    private var BASE_URL = "https://maps.googleapis.com/maps/api/directions/"
    var directionsAPI:DirectionsAPI

    init {
        val gson = GsonBuilder().setLenient().create()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(200, TimeUnit.SECONDS)
            .readTimeout(200, TimeUnit.SECONDS)
            .writeTimeout(200, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        directionsAPI = retrofit.create(DirectionsAPI::class.java)
    }
}