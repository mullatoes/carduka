package com.carduka.service;

import com.carduka.dto.CarRequest;
import com.carduka.dto.CarResponse;

import java.util.List;

public interface CarService {
    CarResponse createCar(CarRequest request);

    List<CarResponse> getAllCars();

    CarResponse getCarById(Long id);

    CarResponse updateCar(Long id, CarRequest request);

    void deleteCar(Long id);
}
