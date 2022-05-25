package kr.ac.tukorea.greenapple.sidoli

import android.database.sqlite.SQLiteDatabase
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
import kr.ac.tukorea.greenapple.sidoli.api_police.getPoliceAPI
import kr.ac.tukorea.greenapple.sidoli.databinding.ActivityMapsBinding
import kr.ac.tukorea.greenapple.sidoli.sql.AssetDatabaseOpenHelper
import retrofit2.Call
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var adb:AssetDatabaseOpenHelper
    lateinit var pp:SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adb = AssetDatabaseOpenHelper(this)
        pp = adb.openDatabase()
        getPoliceAPI(pp).getPoliceData()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        pp = adb.openDatabase()
        val lampArray = adb.DataExtract(pp)
        for(idx : Int in 0 until lampArray.size){
            val sample = LatLng(lampArray[idx].latitude, lampArray[idx].longitude)
            mMap.addMarker(MarkerOptions().position(sample))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lampArray[0].latitude, lampArray[0].longitude)))
    }
}