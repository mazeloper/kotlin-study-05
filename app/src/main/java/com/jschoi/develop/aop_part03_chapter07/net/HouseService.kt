package com.jschoi.develop.aop_part03_chapter07.net

import com.jschoi.develop.aop_part03_chapter07.dto.HouseDto
import retrofit2.Call
import retrofit2.http.GET

interface HouseService {

    @GET("/v3/42434f93-f376-4644-9aad-272127f4fd42")
    fun getHouseList(): Call<HouseDto>
}