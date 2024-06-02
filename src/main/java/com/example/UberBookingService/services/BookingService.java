package com.example.UberBookingService.services;

import com.example.UberBookingService.dto.*;
import com.example.UberBookingService.repository.BookingRepository;
import com.example.UberBookingService.repository.DriverRepository;
import com.example.UberBookingService.repository.PassengerRepository;
import com.example.UberProjectEntityService.models.Booking;
import com.example.UberProjectEntityService.models.BookingStatus;
import com.example.UberProjectEntityService.models.Passenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService implements IBookingService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private static final String LOCATION_SERVICE = "http://localhost:7778";
    private final DriverRepository driverRepository;

    public BookingService(PassengerRepository passengerRepository,
                          BookingRepository bookingRepository,
                          DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
        this.driverRepository = driverRepository;
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
        ResponseEntity<DriverLocationDto[]> result = restTemplate.postForEntity(LOCATION_SERVICE+"/api/location/nearby/drivers", request, DriverLocationDto[].class);

        if(result.getStatusCode().is2xxSuccessful() && result.getBody()!=null){
            System.out.println("Hello***************************************"+ result.getBody());
            List<DriverLocationDto> driverLocationDtos = Arrays.asList(result.getBody());
            System.out.println(driverLocationDtos.size());
            driverLocationDtos.forEach(driverLocationDto -> {
                System.out.println(driverLocationDto.getDriverId()+"---"+driverLocationDto.getLatitude()+"---"+driverLocationDto.getLongitude());
            });
        }



        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                //.driver(Optional.of(newBooking.getDriver()))
                .build();
    }

    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto updateBookingRequestDto, Long bookingId){
        return null;
    }
}
