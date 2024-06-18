package com.example.UberBookingService.services;

import com.example.UberBookingService.apis.LocationServiceApi;
import com.example.UberBookingService.apis.UberSocketApi;
import com.example.UberBookingService.dto.*;
import com.example.UberBookingService.repository.BookingRepository;
import com.example.UberBookingService.repository.DriverRepository;
import com.example.UberBookingService.repository.PassengerRepository;
import com.example.UberProjectEntityService.models.Booking;
import com.example.UberProjectEntityService.models.BookingStatus;
import com.example.UberProjectEntityService.models.Driver;
import com.example.UberProjectEntityService.models.Passenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Service
public class BookingService implements IBookingService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    //private static final String LOCATION_SERVICE = "http://localhost:7778";
    private final DriverRepository driverRepository;
    private final LocationServiceApi locationServiceApi;
    private final UberSocketApi uberSocketApi;
    public BookingService(PassengerRepository passengerRepository,
                          BookingRepository bookingRepository,
                          DriverRepository driverRepository,
                          LocationServiceApi locationServiceApi,
                          UberSocketApi uberSocketApi) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
        this.driverRepository = driverRepository;
        this.locationServiceApi = locationServiceApi;
        this.uberSocketApi = uberSocketApi;
    }


    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails){
        Optional<Passenger> passenger = passengerRepository.findById(bookingDetails.getPassengerId());
        Booking booking = Booking.builder()

                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                //.endLocation(bookingDetails.getEndLocation())
                .passenger(passenger.get())
                .build();

        Booking newBooking = bookingRepository.save(booking);

        //Make an api call to location service to fetch the nearBy drivers
        NearbyDriversRequestDto request = NearbyDriversRequestDto.builder().latitude(bookingDetails.getStartLocation().getLatitude()).longitude(bookingDetails.getEndLocation().getLongitude()).build();

        //This is async request
        processNearByDriverAsync(request,passenger.get().getId(), newBooking.getId());

        //Below is synchromous way
        /*ResponseEntity<DriverLocationDto[]> result = restTemplate.postForEntity(LOCATION_SERVICE+"/api/location/nearby/drivers", request, DriverLocationDto[].class);

        if(result.getStatusCode().is2xxSuccessful() && result.getBody()!=null){
            System.out.println("Hello***************************************"+ result.getBody());
            List<DriverLocationDto> driverLocationDtos = Arrays.asList(result.getBody());
            System.out.println(driverLocationDtos.size());
            driverLocationDtos.forEach(driverLocationDto -> {
                System.out.println(driverLocationDto.getDriverId()+"---"+driverLocationDto.getLatitude()+"---"+driverLocationDto.getLongitude());
            });
        }*/



        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                //.driver(Optional.of(newBooking.getDriver()))
                .build();
    }

    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto updateBookingRequestDto,Long bookingId){
            Optional<Driver> driver = driverRepository.findById(updateBookingRequestDto.getDriverId().get());
            //TODO if(driver.isPresent() && driver.isAvailabel)
            bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.valueOf(updateBookingRequestDto.getStatus()),driver.get());
            Optional<Booking> booking = bookingRepository.findById(bookingId);
            //TODO if driverRepository.update(); & set driver availiablity to false;
            return  UpdateBookingResponseDto.builder()
                    .bookingId(bookingId)
                    .status(booking.get().getBookingStatus())
                    .driver(Optional.ofNullable(booking.get().getDriver()))
                    .build();
    }

    private void processNearByDriverAsync(NearbyDriversRequestDto nearbyDriversRequestDto,Long passengerId,Long bookingId){
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(nearbyDriversRequestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if(response.isSuccessful() && response.body()!=null){
                    System.out.println("Hello***************************************"+ response.body());
                    List<DriverLocationDto> driverLocationDtos = Arrays.asList(response.body());
                    System.out.println(driverLocationDtos.size());
                    driverLocationDtos.forEach(driverLocationDto -> {

                        System.out.println(driverLocationDto.getDriverId()+"---"+driverLocationDto.getLatitude()+"---"+driverLocationDto.getLongitude());
                    });

                    //Raise the ride request
                    System.out.println("****************Raising the ride request *************************");
                    raiseRideRequestAsync(RideRequestDto.builder().passengerId(passengerId).bookingId(bookingId).build());
                }
                else{
                    System.out.println("Request failed "+response.message());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void raiseRideRequestAsync(RideRequestDto rideRequestDto){
        Call<Boolean> call = uberSocketApi.raiseRideRequest(rideRequestDto);
        call.enqueue(new Callback<Boolean>(){
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful() && response.body()!=null){
                    Boolean result = response.body().booleanValue();
                    System.out.println("Driver response is "+result);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                System.out.println("Response Failed");
            }
        });
    }
}
