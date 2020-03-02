package com.example.locationcatproject.LocationFragment

import androidx.lifecycle.MutableLiveData
import com.example.locationcatproject.NetworkLayer.Webservice
import com.example.myapplication.Models.SubmitModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationRepository {
    fun submitLocation(
        accessToken: String,
        lng: String,
        lat: String,
        userId: Int
    ): MutableLiveData<ResponseBody> {
        val data = MutableLiveData<ResponseBody>()
        val city = "cairo"
        val adress = "cairo"
        val body = mapOf(
            "latitude" to lat,
            "longitude" to lng,
            "city" to city,
            "address" to adress
        )
        Webservice.getInstance().api.submitlocation(userId, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        data.value = response.body()
                    } else {
                        data.value = response.body()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    data.value = null
                }
            })
        return data


    }

}
