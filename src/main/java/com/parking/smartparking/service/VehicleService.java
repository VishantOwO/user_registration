package com.parking.smartparking.service;

import com.parking.smartparking.dto.VehicleDTO;
import com.parking.smartparking.entity.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VehicleService {
    List<Vehicle> getVehiclesByUserId(Long userId);
    Vehicle addVehicle(Long userId, VehicleDTO vehicleDTO);
    void deleteVehicle(Long vehicleId, Long userId);
    Vehicle getVehicleByIdAndUserId(Long vehicleId, Long userId);
    Vehicle updateVehicle(Long vehicleId, Long userId, VehicleDTO vehicleDTO);

    Vehicle getLatestVehicle();
}



