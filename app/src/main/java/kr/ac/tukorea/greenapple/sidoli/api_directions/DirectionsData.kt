package kr.ac.tukorea.greenapple.sidoli.api_directions

import com.google.gson.annotations.SerializedName

data class DirectionsData(
    @SerializedName("routes")
    val routes:ArrayList<routesData>,
    @SerializedName("status")
    val status:String
)

data class routesData(
    @SerializedName("legs")
    val legs:ArrayList<legsData>,
)

data class legsData(
    @SerializedName("distance")
    val distance:ArrayList<distanceData>,
    @SerializedName("duration")
    val duration:ArrayList<durationData>,
    @SerializedName("steps")
    val steps:ArrayList<stepData>
)

data class distanceData(
    val text:String,
    val value:String
)

data class durationData(
    val text:String,
    val value:String
)

data class stepData(
    @SerializedName("distance")
    val distance:ArrayList<distanceData>,
    @SerializedName("duration")
    val duration:ArrayList<durationData>,
    @SerializedName("end_location")
    val end_location:ArrayList<locationData>,
    @SerializedName("start_location")
    val start_location:ArrayList<locationData>,
    @SerializedName("steps")
    val steps:ArrayList<stepData>?
)

data class locationData(
    val lat:String,
    val lng:String,
)

