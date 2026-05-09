package com.carduka.service;

import com.carduka.dto.CarRequest;
import com.carduka.dto.CarResponse;
import com.carduka.entity.Car;
import com.carduka.exception.CarNotFoundException;
import com.carduka.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    public CarResponse createCar(CarRequest request) {
        Car car = Car.builder()
                .make(request.make())
                .model(request.model())
                .manufactureYear(request.manufactureYear())
                .color(request.color())
                .price(request.price())
                .build();

        Car savedCar = carRepository.save(car);

        return mapToResponse(savedCar);
    }

    @Override
    public List<CarResponse> getAllCars() {
        return carRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        return mapToResponse(car);
    }

    @Override
    public CarResponse updateCar(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        car.setMake(request.make());
        car.setModel(request.model());
        car.setManufactureYear(request.manufactureYear());
        car.setColor(request.color());
        car.setPrice(request.price());

        Car updatedCar = carRepository.save(car);

        return mapToResponse(updatedCar);
    }

    @Override
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        carRepository.delete(car);
    }

    private CarResponse mapToResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getMake(),
                car.getModel(),
                car.getManufactureYear(),
                car.getColor(),
                car.getPrice()
        );
    }
}
