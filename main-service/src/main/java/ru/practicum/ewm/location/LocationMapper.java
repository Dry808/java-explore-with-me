package ru.practicum.ewm.location;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {
    public static Location toLocation(Location.LocationDto locationDto) {
        return Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }


    public  static Location.LocationDto toLocationDto(Location location) {
        return Location.LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}