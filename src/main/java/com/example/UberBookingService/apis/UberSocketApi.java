package com.example.UberBookingService.apis;

import com.example.UberBookingService.dto.DriverLocationDto;
import com.example.UberBookingService.dto.NearbyDriversRequestDto;
import com.example.UberBookingService.dto.RideRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UberSocketApi {
    @POST("/api/socket/newRide")
    Call<Boolean> raiseRideRequest(@Body RideRequestDto rideRequestDto);
}
