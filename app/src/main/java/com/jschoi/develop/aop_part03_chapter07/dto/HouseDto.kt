package com.jschoi.develop.aop_part03_chapter07.dto

import com.google.gson.annotations.SerializedName
import com.jschoi.develop.aop_part03_chapter07.model.HouseModel

data class HouseDto(
    @SerializedName("items") val items: List<HouseModel>
)