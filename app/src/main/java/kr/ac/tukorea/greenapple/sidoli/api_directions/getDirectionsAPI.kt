package kr.ac.tukorea.greenapple.sidoli.api_directions

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class getDirectionsAPI {
    lateinit var distance:String
    lateinit var duration:String
    fun getDirectionsData(start_location:String, end_location:String):ArrayList<pathData>{
        val temp = ArrayList<pathData>(0)
        DirectionsRetrofitClient.DirectionsAPI.getDirection(origin = start_location, destination = end_location)
            .enqueue(object: Callback<DirectionsData>{
                override fun onResponse(
                    call: Call<DirectionsData>,
                    response: Response<DirectionsData>,
                ) {
                    if (response.isSuccessful && response.body()!!.status == "OK"){
                        val datas = response.body()!!.routes[0].legs[0]
                        distance = datas.distance.text
                        duration = datas.duration.text
                        for (data in datas.steps){
                            temp.add(pathData(start_lat = data.start_location.lat, start_lng = data.start_location.lng, end_lat = data.end_location.lat, end_lng = data.end_location.lng))
                            if (data.steps != null){
                                for(m_data in data.steps){
                                    temp.add(pathData(start_lat = m_data.start_location.lat, start_lng = m_data.start_location.lng, end_lat = m_data.end_location.lat, end_lng = m_data.end_location.lng))
                                }
                            }
                        }
                    } else{
                        Log.d("response", "onResponse Failure")
                    }
                }

                override fun onFailure(call: Call<DirectionsData>, t: Throwable) {
                    Log.d("response", "onFailure error $t")
                }
            })
        return temp
    }
}