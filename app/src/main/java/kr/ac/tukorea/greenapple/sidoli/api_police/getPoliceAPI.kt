package kr.ac.tukorea.greenapple.sidoli.api_police

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class getPoliceAPI(val sql: SQLiteDatabase) {
    fun getPoliceData(){
        PoliceRetrofitClient.policeAPI.getPolice(page_no = 1, number_of_rows = 10)
            .enqueue(object : Callback<PoliceData> {
                override fun onResponse(call: Call<PoliceData>, response: Response<PoliceData>) {
                    if (response.isSuccessful){
                        for(data in response.body()!!.Ptrldvsnsubpolcstus[1].row){
                            sql.execSQL("INSERT INTO PoliceData VALUES ('"+data.GOVOFC_NM+"','"+data.DIV_NM+"','"+data.REFINE_WGS84_LAT+"',"+data.REFINE_WGS84_LOGT+");")
                        }
                    } else{
                        Log.d("response", "onResponse Failure")
                    }
                }

                override fun onFailure(call: Call<PoliceData>, t: Throwable) {
                    Log.d("response", "onFailure error $t")
                }
            })
    }
}