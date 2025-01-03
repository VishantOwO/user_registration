// VehicleServiceImpl.java in service package
package com.parking.smartparking.service.impl;

import com.parking.smartparking.dto.VehicleDTO;
import com.parking.smartparking.entity.Vehicle;
import com.parking.smartparking.entity.User;
import com.parking.smartparking.repository.VehicleRepository;
import com.parking.smartparking.repository.UserRepository;
import com.parking.smartparking.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    public Vehicle getLatestVehicle() {
        try {
            return vehicleRepository.findTopByOrderByRegisteredAtDesc();
        } catch (Exception e) {
            return null; // Return null if no vehicle exists
        }
    }

    @Override
    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    @Override
    public Vehicle getVehicleByIdAndUserId(Long vehicleId, Long userId) {
        return vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found or unauthorized"));
    }

    @Override
    public Vehicle addVehicle(Long userId, VehicleDTO vehicleDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vehicle vehicle = new Vehicle();
        vehicle.setUser(user);
        updateVehicleFromDTO(vehicle, vehicleDTO);

        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Long vehicleId, Long userId, VehicleDTO vehicleDTO) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found or unauthorized"));

        updateVehicleFromDTO(vehicle, vehicleDTO);
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long vehicleId, Long userId) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found or unauthorized"));
        vehicleRepository.delete(vehicle);
    }

    // Helper method to update Vehicle from DTO
    private void updateVehicleFromDTO(Vehicle vehicle, VehicleDTO vehicleDTO) {
        vehicle.setVehicleNumber(vehicleDTO.getVehicleNumber());
        vehicle.setVehicleType(vehicleDTO.getVehicleType());
        vehicle.setManufacturer(vehicleDTO.getManufacturer());
        vehicle.setModel(vehicleDTO.getModel());
        vehicle.setColor(vehicleDTO.getColor());
    }

}