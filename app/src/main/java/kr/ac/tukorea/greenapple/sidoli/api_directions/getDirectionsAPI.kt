package kr.ac.tukorea.greenapple.sidoli.api_directions

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class getDirectionsAPI {
    lateinit var distance:String
    lateinit var duration:String

    fun getDirectionsData(start_location:String, end_location:String){
        DirectionsRetrofitClient.DirectionsAPI.getDirection(origin = start_location, destination = end_location)
            .enqueue(object: Callback<DirectionsData>{
                override fun onResponse(
                    call: Call<DirectionsData>,
                    response: Response<DirectionsData>,
                ) {
                    if (response.isSuccessful && response.body()!!.status == "OK"){
                        val datas = response.body()!!.routes[0].legs[0]
                        distance = datas.distance[0].text
                        duration = datas.duration[0].text
                    }
                }

                override fun onFailure(call: Call<DirectionsData>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })

    }
}