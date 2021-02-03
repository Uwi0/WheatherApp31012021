package com.kakapo.weatherapps.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.kakapo.weatherapps.R
import com.kakapo.weatherapps.model.WeatherResponse
import com.kakapo.weatherapps.network.WeatherService
import com.kakapo.weatherapps.utils.Constants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var ivMain: ImageView
    private lateinit var tvMain: TextView
    private lateinit var tvMainDescription: TextView
    private lateinit var ivHumidity: ImageView
    private lateinit var tvTemp: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var ivMinMax: ImageView
    private lateinit var tvMin: TextView
    private lateinit var tvMax: TextView
    private lateinit var ivWind: ImageView
    private lateinit var tvSpeed: TextView
    private lateinit var tvSpeedUnit: TextView
    private lateinit var ivLocation: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvCountry: TextView
    private lateinit var ivSunRise: ImageView
    private lateinit var tvSunRiseTime: TextView
    private lateinit var ivSunset: ImageView
    private lateinit var tvSunsetTime: TextView


    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    private val mLocationCallback = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current longitude", "$longitude")
            getLocationWeatherDetail(latitude, longitude)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivMain = findViewById(R.id.iv_main)
        tvMain = findViewById(R.id.tv_main)
        tvMainDescription = findViewById(R.id.tv_main_description)
        ivHumidity = findViewById(R.id.iv_humidity)
        tvTemp = findViewById(R.id.tv_temp)
        tvHumidity = findViewById(R.id.tv_humidity)
        ivMinMax = findViewById(R.id.iv_min_max)
        tvMin = findViewById(R.id.tv_min)
        tvMax = findViewById(R.id.tv_max)
        ivWind = findViewById(R.id.iv_wind)
        tvSpeed = findViewById(R.id.tv_speed)
        tvSpeedUnit = findViewById(R.id.tv_speed_unit)
        ivLocation = findViewById(R.id.iv_location)
        tvName = findViewById(R.id.tv_name)
        tvCountry = findViewById(R.id.tv_country)
        ivSunRise = findViewById(R.id.iv_sunrise)
        tvSunRiseTime = findViewById(R.id.tv_sunrise_time)
        ivSunset = findViewById(R.id.iv_sunset)
        tvSunsetTime = findViewById(R.id.tv_sunset_time)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(!isLocationEnabled()){
            Toast.makeText(
                    this,
                    "Your Location provider is turned off. Please turn it on",
                    Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

        }else{
            Dexter.withActivity(this)
                    .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    .withListener(object: MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                            if(report!!.areAllPermissionsGranted()){
                                requestLocationData()
                            }

                            if(report.isAnyPermissionPermanentlyDenied){
                                Toast.makeText(
                                        this@MainActivity,
                                        "You have denied location permission. Please enable them " +
                                                "as it is mandatory for the App to work",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }

                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }

                    })
                    .onSameThread()
                    .check()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh -> {
                requestLocationData()
                true
            }
            else ->  return super.onOptionsItemSelected(item)
        }

    }

    private fun isLocationEnabled(): Boolean{

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
                .setMessage("It looks like you have turned off permissions required " +
                        "for this feature. it an be enabled under settings")
                .setPositiveButton("GO TO SETTINGS"){ _, _ ->
                    try{

                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }catch(e: Exception){
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("Cancel"){dialog, _->
                    dialog.dismiss()
                }.show()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
        )
    }

    private fun getLocationWeatherDetail(latitude: Double, longitude: Double){
        if(Constants.isNetWorkAvailable(this)){
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val service: WeatherService = retrofit.create(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                    latitude,
                    longitude,
                    Constants.METRIC_UNIT,
                    Constants.APP_ID
            )

            showCustomDialogProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if(response.isSuccessful){

                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()!!
                        setupUi(weatherList)
                        Log.i("Response Result", "$weatherList")
                    }else{
                        when(response.code()){
                            400 -> Log.e("Error 400", "Bad Request")
                            404 -> Log.e("Error 404", "Not Found")
                            else -> Log.e("Error", "Generic Error")
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Errorrrrr", t.message.toString())
                    hideProgressDialog()
                }

            })

        }else{
            Toast.makeText(
                this@MainActivity,
                "No Internet connection available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showCustomDialogProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUi(weatherList: WeatherResponse){

       for (i in weatherList.weather!!.indices){
           Log.i("Weather Name", weatherList.weather.toString())
           tvMain.text = weatherList.weather[i].main
           tvMainDescription.text = weatherList.weather[i].description
           tvTemp.text = "${weatherList.main!!.temp}" +
                   getUnit(application.resources.configuration.toString())

           tvHumidity.text = weatherList.main.humidity.toString() + "per cent"
           tvMin.text = weatherList.main.temp_min.toString() + "min"
           tvMax.text = weatherList.main.temp_max.toString() + "max"
           tvSpeed.text = weatherList.wind!!.speed.toString()
           tvName.text = weatherList.name
           tvCountry.text = weatherList.sys!!.country


           tvSunRiseTime.text = unixTime(weatherList.sys.sunnrise)
           tvSunsetTime.text = unixTime(weatherList.sys.sunset)

           when(weatherList.weather[i].icon){
               "01d" -> ivMain.setImageResource(R.drawable.sunny)
               "02d" -> ivMain.setImageResource(R.drawable.cloud)
               "03d" -> ivMain.setImageResource(R.drawable.cloud)
               "04d" -> ivMain.setImageResource(R.drawable.cloud)
               "04n" -> ivMain.setImageResource(R.drawable.cloud)
               "10d" -> ivMain.setImageResource(R.drawable.rain)
               "11d" -> ivMain.setImageResource(R.drawable.storm)
               "13d" -> ivMain.setImageResource(R.drawable.snowflake)
               "01n" -> ivMain.setImageResource(R.drawable.cloud)
               "02n" -> ivMain.setImageResource(R.drawable.cloud)
               "03n" -> ivMain.setImageResource(R.drawable.cloud)
               "10n" -> ivMain.setImageResource(R.drawable.cloud)
               "11n" -> ivMain.setImageResource(R.drawable.rain)
               "13n" -> ivMain.setImageResource(R.drawable.snowflake)
           }
       }
    }

    private fun getUnit(values: String) : String{
        var value = "°C"
        if("US" == value || "LR" == values || "MM" == values){
            value = "°F"
        }

        return value
    }

    private fun unixTime(timex: Long): String{
        val date = Date(timex * 1000L)
        val format = SimpleDateFormat("HH:mm", Locale.UK)
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }
}