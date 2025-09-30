package com.studyhive.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationConfirmRequest {

    private Double currentLatitude;
    private Double currentLongitude;
    private Double fixedLatitude;
    private Double fixedLongitude;
    private Double accuracy;
    private Instant timeStampLoggedIn;
    private Instant timeStampLocationCheck;
}
