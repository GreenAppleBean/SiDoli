package kr.ac.tukorea.greenapple.sidoli

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_maps.*
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위칫값 사용
    private lateinit var locationCallback: LocationCallback // 위칫값 요청에 대한 갱신 정보를 받아옴

    lateinit var initial_marker : Marker
    lateinit var initial_location : Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 권한 array 저장
        val permission = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
        requirePermissions(permission, 999)

        // 공공데이터로부터 바로 읽어오기(경찰 데이터 부분)
        adb = AssetDatabaseOpenHelper(this) // assets에 있는 db파일 불러오기
        pp = adb.openDatabase() // database 열기

        // 쿼리문을 통해 데이터가 있는 지 검사하기
        insertPoliceData()
        //insertLampData()

        updateLocation()

        // 램프 켜기 버튼(클러스터가 확대 축소 시에 무조건 다시 뜨는 버그 있음)
        var flag1 = 0
        Floating1.setOnClickListener{
            if(flag1 == 0) {
                addClusterOnMap()
                flag1 = 1
            }
            else if(flag1 == 1){
                mMap.clear()
                flag1 = 0
            }
        }

        // 현제 위치 마커로 찍고 카메라 위치 옮기기
        var flag2 = 0
        Floating2.setOnClickListener{
            if(flag2 == 0) {
                updateLocation()
                flag2 = 1
            }
            else if(flag2 == 1){
                initial_marker.remove()
                //mMap.clear()
                flag2 = 0
            }
        }

        // 경찰서 전화걸기
        bellbtn.setOnClickListener {
            var intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:112")
            if(intent.resolveActivity(packageManager) != null){
                startActivity(intent)
            }
        }

        // 경찰서 위치 마커 표시
        var police_flag = 0
        nearbtn.setOnClickListener {
            if(police_flag == 0){
                addMarkerOnMap()
                police_flag = 1
            }
            else if(police_flag == 1){
                mMap.clear()
                police_flag = 0
            }
        }

    }

    fun startProcess(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun requirePermissions(permissions: Array<String>, requestCode: Int) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionGranted(requestCode)
        } else {
            val isAllPermissionsGranted = permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            if (isAllPermissionsGranted) {
                permissionGranted(requestCode)
            } else {
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            permissionGranted(requestCode)
        } else {
            permissionDenied(requestCode)
        }
    }

    // 권한이 있는 경우 실행
    fun permissionGranted(requestCode: Int) {
        startProcess() // 권한이 있는 경우 구글 지도를준비하는 코드 실행
    }

    // 권한이 없는 경우 실행
    fun permissionDenied(requestCode: Int) {
        Toast.makeText(this
            , "권한 승인이 필요합니다."
            , Toast.LENGTH_LONG)
            .show()
    }


    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        val policeArray = adb.PoliceDataExtract(pp)
        // 경찰 data 마커 표시하기 (파랑색)
        for (idx: Int in 0 until policeArray.size) {
            //val police = LatLng(policeArray[idx].latitude, policeArray[idx].longitude)
            //mMap.addMarker(MarkerOptions().position(police).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            Log.d("Police", "${policeArray[idx].name}, ${policeArray[idx].latitude}, ${policeArray[idx].longitude}")
        }

        mMap = googleMap
        pp = adb.openDatabase()

        clusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // 일단 한국공학대학교 위치를 기준으로 기본 카메라 줌 위치를 설정했습니다. 추후에 gps 위치 기반으로 줌 설정하는 것도 구현해봅시다!
        // 한국공학대학교(37.3401906, 126.7335293)
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.3401906,126.7335293), 14f))

        // 이부분이 길찾기 클릭 리스너에 들어가면 됩니다.
        // getcurrentDirection("37.3401906,126.7335293", "37.3302817253012,126.68978386536874")
        getcurrentDirection("${initial_location.latitude},${initial_location.longitude}", "${policeArray[0].latitude}, ${policeArray[0].longitude}")
    }

    // 위치 정보를 받아오는 역할
    @SuppressLint("MissingPermission") //requestLocationUpdates는 권한 처리가 필요한데 현재 코드에서는 확인 할 수 없음. 따라서 해당 코드를 체크하지 않아도 됨.
    fun updateLocation() {
        //var LATLNG = LatLng(0.0, 0.0)
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            //interval = 1000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.let {
                    for(location in it.locations) {
                        Log.d("Location", "${location.latitude} , ${location.longitude}")
                        setLastLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    fun setLastLocation(lastLocation: Location){
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        val markerOptions = MarkerOptions()
            .position(LATLNG)
            .title("Here!")

        val cameraPosition = CameraPosition.Builder()
            .target(LATLNG)
            .zoom(15.0f)
            .build()
        //mMap.clear()
        initial_marker = mMap.addMarker(markerOptions)
        initial_location = lastLocation//LatLng(lastLocation.latitude, lastLocation.longitude)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    //////////////////////////////////////////////////////////////////////////////
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

    // 공공데이터로부터 램프 데이터 읽어오기
    private fun insertLampData() {
        val lampExistTest = pp.rawQuery("SELECT * FROM LampData;", null)
        if (!lampExistTest.moveToNext()) {   // db 파일에 가로등 data가 없을 시
            getLampAPI(pp).getLampData()    // api에서 가로등 data 끌어와서 집어넣음
        }
        lampExistTest.close()   // 데이터베이스 닫기
    }

    // 공공데이터로부터 경찰서 위치 데이터 읽어오기
    private fun insertPoliceData() {
        val policeExistTest = pp.rawQuery("SELECT * FROM PoliceData;", null)
        if (!policeExistTest.moveToNext()) {   // db 파일에 경찰 data가 없을 시
            getPoliceAPI(pp).getPoliceData()    // api에서 경찰 data 끌어와서 집어넣음
        }
        policeExistTest.close()   // 데이터베이스 닫기
    }

    private fun addMarkerOnMap() {
        val policeArray = adb.PoliceDataExtract(pp)
        // 경찰 data 마커 표시하기 (파랑색)
        for (idx: Int in 0 until policeArray.size) {
            val police = LatLng(policeArray[idx].latitude, policeArray[idx].longitude)
            mMap.addMarker(MarkerOptions().position(police).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        }
    }

    private fun addClusterOnMap(){
        val lampArray = adb.LampDataExtract(pp)
        // for loop을 돌려서 지도에 클러스터링 마커를 찍어줌
        for (idx: Int in 0 until lampArray.size) {
            clusterManager.addItem(LampItemData(lampArray[idx].latitude.toString(),
                lampArray[idx].longitude.toInt(),
                lampArray[idx].latitude.toString(),
                lampArray[idx].longitude.toString()))
        }
    }
}