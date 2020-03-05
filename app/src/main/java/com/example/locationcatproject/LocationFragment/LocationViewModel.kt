package com.example.myapplication.LoginFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catapplication.utilies.Validation
import com.example.locationcatproject.LocationFragment.LocationRepository
import com.example.myapplication.Models.ResponseModelData
import com.example.myapplication.Models.SubmitModel
import okhttp3.ResponseBody


class LocationViewModel : ViewModel() {
    private var repositoryHelper: LocationRepository =
        LocationRepository()
    private lateinit var mutableLiveData: MutableLiveData<ResponseBody>

    fun validateLoginInfo(
        emailEt: String,
        passwordEt: String
    ): Boolean {
        val isEmailValid = Validation.validateEmail(emailEt)
        val isPasswordValid = Validation.validate(passwordEt)
        return !(!isEmailValid || !isPasswordValid)
    }


    fun submitLocation(lat: String, lng: String, accessToken: String, userId: Int) {
        mutableLiveData = repositoryHelper.submitLocation(accessToken, lng, lat, userId)

    }

    fun getData(): MutableLiveData<ResponseBody> {
        return mutableLiveData
    }

}







