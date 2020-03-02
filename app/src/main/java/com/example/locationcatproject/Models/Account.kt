package com.example.myapplication.Models

import com.google.gson.annotations.SerializedName


data class Account(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("image") val image: String = ""
)