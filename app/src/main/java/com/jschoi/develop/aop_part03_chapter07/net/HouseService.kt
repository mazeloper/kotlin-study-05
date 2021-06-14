package com.jschoi.develop.aop_part03_chapter07.net

import com.jschoi.develop.aop_part03_chapter07.dto.HouseDto
import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/787091fb-55ac-41d0-8b69-0d93ddd60424")
    fun getHouseList(): Call<HouseDto>
}