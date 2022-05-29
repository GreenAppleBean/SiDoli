package kr.ac.tukorea.greenapple.sidoli.api_directions

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class getDirectionsAPI(val sql: SQLiteDatabase) {
    fun getDirectionsData(start_location:String, end_location:String){
        DirectionsRetrofitClient.directionsAPI.getDirection(origin = start_location, destination = end_location)
            .enqueue(object : Callback<DirectionsData> {
                override fun onResponse(
                    call: Call<DirectionsData>,
                    response: Response<DirectionsData>,
                ) {
                    if (response.isSuccessful && response.body()!!.status == "OK") {
                        sql.execSQL("DROP TABLE IF EXISTS Directions;")
                        sql.execSQL("create table Directions(start_lat NUMBER, start_lng NUMBER, end_lat NUMBER, end_lng NUMBER);")
                        val datas = response.body()!!.routes[0].legs[0]
                        for (data in datas.steps) {
                            sql.execSQL("INSERT INTO Directions VALUES ('"+data.start_location.lat+"','"+data.start_location.lng+"','"+data.end_location.lat+"',"+data.end_location.lng+");")
                            if (data.steps != null) {
                                for (m_data in data.steps) {
                                    sql.execSQL("INSERT INTO Directions VALUES ('"+m_data.start_location.lat+"','"+m_data.start_location.lng+"','"+m_data.end_location.lat+"',"+m_data.end_location.lng+");")
                                }
                            }
                        }
                    } else {
                        Log.d("response", "onResponse Failure")
                    }
                }

                override fun onFailure(call: Call<DirectionsData>, t: Throwable) {
                    Log.d("response", "onFailure error $t")
                }

            })

    }
}