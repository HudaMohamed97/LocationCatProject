package com.example.myapplication.Models

import com.google.gson.annotations.SerializedName

data class RegisterRequestModel(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val photo: String
)