package com.example.locationcatproject.LocationFragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.myapplication.LoginFragment.LocationViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.location_fragment.*


class LocationFragment : Fragment() {
    private lateinit var root: View
    private lateinit var loginPreferences: SharedPreferences
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val LOCATION_PERMISSION = 1001
    private val REQUEST_CHECK_SETTINGS = 10001
    private var fromButton = false
    private var getLocation: Button? = null
    private var mLastLocation: Location? = null
    private var mResultReceiver: AddressResultReceiver? = null
    private var address: TextView? = null
    private lateinit var locationViewModel: LocationViewModel


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) {
                return
            }
            for (location in locationResult.locations) {
                // Update UI with location data
                mLastLocation = location
                val lng = location.longitude.toString()
                val lat = location.latitude.toString()
                if (fromButton) {
                    callSubmitLocation(lat, lng)
                }
                mFusedLocationClient!!.removeLocationUpdates(this)
                startIntentService()
            }
        }
    }


    private fun callSubmitLocation(lat: String, lng: String) {
        location_progressBar.visibility = View.VISIBLE
        val accessToken = loginPreferences.getString("accessToken", "")
        val userId = loginPreferences.getInt("userId", -1)
        if (accessToken != null && userId != -1) {
            locationViewModel.submitLocation(lat, lng, accessToken, userId)
        }
        locationViewModel.getData().observe(this, Observer {
            location_progressBar.visibility = View.GONE
            if (it != null) {
                openAlertDialog("Submitted Successfully ,Thanks ")

            } else {
                openAlertDialog("There is an Error Occurs Please Try Again.")
            }


        })
    }

    private fun openAlertDialog(massage: String) {
        val builder1 = AlertDialog.Builder(activity)
        builder1.setMessage(massage)
        builder1.setCancelable(true)
        builder1.setPositiveButton(
            "ok"
        ) { dialog, id -> dialog.cancel() }
        val alert = builder1.create()
        alert.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(
            com.example.locationcatproject.R.layout.location_fragment,
            container,
            false
        )
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginPreferences = activity!!.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        mResultReceiver = AddressResultReceiver(null)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        if (checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )
            return
        }
        createAndCheckLocationRequest()
        get_location.setOnClickListener {
            fromButton = true
            getLocation()
        }
    }


    override fun onResume() {
        super.onResume()
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS) {

        } else {
            getLocation?.isEnabled = false
        }
    }

    private fun getLocation() {
        mFusedLocationClient?.lastLocation?.addOnSuccessListener(
            activity!!
        ) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                mLastLocation = location
                val lng = location.longitude.toString()
                val lat = location.latitude.toString()
                callSubmitLocation(lat, lng)
                //startIntentService();
            } else {
                createAndCheckLocationRequest()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    //permission denied! we cant use location services.
                }
            }
        }
    }


    private fun requestLocationUpdate() {
        if (checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )
        }
        if (fromButton) {
            location_progressBar.visibility = View.VISIBLE
        } else {
            location_progressBar.visibility = View.GONE
        }
        mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                requestLocationUpdate()
            } else {
                Toast.makeText(activity, "Location request not satisfied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun startIntentService() {
        val intent = Intent(activity, FetchAddressIntentService::class.java)
        intent.putExtra(Constants.RECEIVER, mResultReceiver)
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation)
        activity?.startService(intent)
    }

    fun displayAddressOutput(addressText: String) {
        activity?.runOnUiThread(Runnable { address?.setText(addressText) })
    }

    class AddressResultReceiver(handler: Handler?) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (resultData == null) {
                return
            }

            // Display the address string
            // or an error message sent from the intent service.
            var mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY)
            if (mAddressOutput == null) {
                mAddressOutput = ""
            }
            mAddressOutput
        }
    }

    private fun createAndCheckLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000
        mLocationRequest!!.fastestInterval = 5000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest!!)
        val client = LocationServices.getSettingsClient(activity!!)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(activity!!) {
            requestLocationUpdate()
        }
        task.addOnFailureListener(activity!!) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }

            }
        }
    }


}