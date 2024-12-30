package com.parking.smartparking.controller;

import com.parking.smartparking.dto.VehicleDTO;
import com.parking.smartparking.entity.Vehicle;
import com.parking.smartparking.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public String listVehicles(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        List<Vehicle> vehicles = vehicleService.getVehiclesByUserId(userId);
        model.addAttribute("vehicles", vehicles);
        return "vehicles/list";
    }

    @GetMapping("/add")
    public String showAddVehicleForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("vehicle", new VehicleDTO());
        return "vehicles/add";
    }

    @PostMapping("/add")
    public String addVehicle(@Valid @ModelAttribute("vehicle") VehicleDTO vehicleDTO,
                             BindingResult result,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vehicles/add";
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            vehicleService.addVehicle(userId, vehicleDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle added successfully!");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding vehicle: " + e.getMessage());
            return "vehicles/add";
        }
    }

    @GetMapping("/{id}")
    public String viewVehicle(@PathVariable("id") Long vehicleId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Vehicle vehicle = vehicleService.getVehicleByIdAndUserId(vehicleId, userId);
            model.addAttribute("vehicle", vehicle);
            return "vehicles/view";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long vehicleId,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Vehicle vehicle = vehicleService.getVehicleByIdAndUserId(vehicleId, userId);
            VehicleDTO vehicleDTO = new VehicleDTO();
            vehicleDTO.setVehicleNumber(vehicle.getVehicleNumber());
            vehicleDTO.setVehicleType(vehicle.getVehicleType());
            vehicleDTO.setManufacturer(vehicle.getManufacturer());
            vehicleDTO.setModel(vehicle.getModel());
            vehicleDTO.setColor(vehicle.getColor());

            model.addAttribute("vehicleId", vehicleId);
            model.addAttribute("vehicle", vehicleDTO);
            return "vehicles/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateVehicle(@PathVariable("id") Long vehicleId,
                                @Valid @ModelAttribute("vehicle") VehicleDTO vehicleDTO,
                                BindingResult result,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("vehicleId", vehicleId);
            return "vehicles/edit";
        }

        try {
            vehicleService.updateVehicle(vehicleId, userId, vehicleDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle updated successfully!");
            return "redirect:/vehicles";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating vehicle: " + e.getMessage());
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable("id") Long vehicleId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            vehicleService.deleteVehicle(vehicleId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting vehicle: " + e.getMessage());
        }

        return "redirect:/vehicles";
    }
}