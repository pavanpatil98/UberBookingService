package com.example.UberBookingService.dto;


import com.example.UberProjectEntityService.models.BookingStatus;
import com.example.UberProjectEntityService.models.Driver;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {

    private Long bookingId;
    private BookingStatus status;
    private Optional<Driver> driver;

}
