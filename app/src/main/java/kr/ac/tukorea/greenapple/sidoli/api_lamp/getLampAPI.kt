package kr.ac.tukorea.greenapple.sidoli.api_lamp

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class getLampAPI(val sql:SQLiteDatabase) {
    fun getLampData(){
        LampRetrofitClient.lampAPI.getLamp(page_no = 1, number_of_rows = 100)
            .enqueue(object :Callback<LampData>{
                override fun onResponse(call: Call<LampData>, response: Response<LampData>) {
                    if (response.isSuccessful){
                        for (data in response.body()!!.response.body.Items){
                            sql.execSQL("INSERT INTO LampData VALUES ('"+data.lmpLcNm+"','"+data.installationCo+"','"+data.latitude+"',"+data.longitude+");")
                        }
                    }else{
                        Log.d("response", "onResponse Failure")
                    }
                }

                override fun onFailure(call: Call<LampData>, t: Throwable) {
                    Log.d("response", "onFailure error $t")
                }
            })
    }
}