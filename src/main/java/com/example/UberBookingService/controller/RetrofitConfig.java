package com.example.UberBookingService.controller;

import com.example.UberBookingService.apis.LocationServiceApi;
import com.netflix.discovery.EurekaClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitConfig {

    @Autowired
    private EurekaClient eurekaClient;

    private String getServiceUrl(String serviceName){
        String ans =  eurekaClient.getNextServerFromEureka(serviceName,false).getHomePageUrl();
        System.out.println(ans+" ***************************************************");
        return ans;
    }

    @Bean
    public LocationServiceApi locationServiceApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("UBERLOCATIONSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(LocationServiceApi.class);
    }
}
