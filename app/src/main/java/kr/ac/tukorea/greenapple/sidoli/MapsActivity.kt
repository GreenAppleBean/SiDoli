package kr.ac.tukorea.greenapple.sidoli

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.clustering.ClusterManager
import kr.ac.tukorea.greenapple.sidoli.api_directions.getDirectionsAPI
import kr.ac.tukorea.greenapple.sidoli.api_lamp.LampItemData
import kr.ac.tukorea.greenapple.sidoli.api_lamp.getLampAPI
import kr.ac.tukorea.greenapple.sidoli.api_police.getPoliceAPI
import kr.ac.tukorea.greenapple.sidoli.databinding.ActivityMapsBinding
import kr.ac.tukorea.greenapple.sidoli.sql.AssetDatabaseOpenHelper

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var adb:AssetDatabaseOpenHelper
    lateinit var pp:SQLiteDatabase
    private lateinit var clusterManager: ClusterManager<LampItemData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 공공데이터로부터 바로 읽어오기(경찰 데이터 부분)
        adb = AssetDatabaseOpenHelper(this) // assets에 있는 db파일 불러오기
        pp = adb.openDatabase() // database 열기

        // 쿼리문을 통해 데이터가 있는 지 검사하기
        insertPoliceData()
        //insertLampData()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun insertLampData() {
        val lampExistTest = pp.rawQuery("SELECT * FROM LampData;", null)
        if (!lampExistTest.moveToNext()) {   // db 파일에 가로등 data가 없을 시
            getLampAPI(pp).getLampData()    // api에서 가로등 data 끌어와서 집어넣음
        }
        lampExistTest.close()   // 데이터베이스 닫기
    }

    private fun insertPoliceData() {
        val policeExistTest = pp.rawQuery("SELECT * FROM PoliceData;", null)
        if (!policeExistTest.moveToNext()) {   // db 파일에 경찰 data가 없을 시
            getPoliceAPI(pp).getPoliceData()    // api에서 경찰 data 끌어와서 집어넣음
        }
        policeExistTest.close()   // 데이터베이스 닫기
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        pp = adb.openDatabase()

        clusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        // data를 담을 ArrayList 선언
        addMarkerOnMap()

        // 일단 한국공학대학교 위치를 기준으로 기본 카메라 줌 위치를 설정했습니다. 추후에 gps 위치 기반으로 줌 설정하는 것도 구현해봅시다!
        // 한국공학대학교(37.3401906, 126.7335293)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.3401906,126.7335293), 11f))

        // 이부분이 길찾기 클릭 리스너에 들어가면 됩니다.
        getcurrentDirection("37.3401906,126.7335293", "37.351857902626435,126.742838367119")
    }

    private fun getcurrentDirection(startLoc:String, endLoc:String) {
        getDirectionsAPI(pp).getDirectionsData(startLoc,endLoc)
        val cursor = pp.rawQuery("SELECT * From Directions;", null)
        while (cursor.moveToNext()) {
            val point1 = LatLng(cursor.getDouble(0), cursor.getDouble(1))
            val point2 = LatLng(cursor.getDouble(2), cursor.getDouble(3))
            mMap.addPolyline(PolylineOptions()
                .clickable(true)
                .add(point1, point2)
            )
        }
        cursor.close()
    }

    private fun addMarkerOnMap() {
        val lampArray = adb.LampDataExtract(pp)
        val policeArray = adb.PoliceDataExtract(pp)


        // for loop을 돌려서 지도에 클러스터링 마커를 찍어줌
        for (idx: Int in 0 until lampArray.size) {
            clusterManager.addItem(LampItemData(lampArray[idx].latitude.toString(),
                lampArray[idx].longitude.toInt(),
                lampArray[idx].latitude.toString(),
                lampArray[idx].longitude.toString()))

        }

        // 경찰 data 마커 표시하기 (파랑색)
        for (idx: Int in 0 until policeArray.size) {
            val police = LatLng(policeArray[idx].latitude, policeArray[idx].longitude)
            mMap.addMarker(MarkerOptions().position(police)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        }
    }


}