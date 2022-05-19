package kr.ac.tukorea.greenapple.sidoli

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import retrofit2.Callback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kr.ac.tukorea.greenapple.sidoli.api_lamp.LampData
import kr.ac.tukorea.greenapple.sidoli.api_lamp.LampRetrofitClient
import kr.ac.tukorea.greenapple.sidoli.api_police.PoliceData
import kr.ac.tukorea.greenapple.sidoli.api_police.PoliceRetrofitClient
import kr.ac.tukorea.greenapple.sidoli.databinding.ActivityMapsBinding
import retrofit2.Call
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LampRetrofitClient.lampAPI.getLamp(page_no = 1, number_of_rows = 10)
            .enqueue(object : Callback<LampData>{
                override fun onResponse(call: Call<LampData>, response: Response<LampData>) {
                    if (response.isSuccessful){
                        for(data in response.body()!!.response.body.Items){
                            Log.d("response", "onResponse Success")
                        }
                    } else{
                        Log.d("response", "onResponse Failure")
                    }
                }

                override fun onFailure(call: Call<LampData>, t: Throwable) {
                    Log.d("response", "onFailure error ${t}")
                }
            })


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}