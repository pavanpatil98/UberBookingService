package com.example.UberBookingService.apis;

import com.example.UberBookingService.dto.DriverLocationDto;
import com.example.UberBookingService.dto.NearbyDriversRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LocationServiceApi {

    @POST("/api/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDrivers(@Body NearbyDriversRequestDto requestDto);


}
