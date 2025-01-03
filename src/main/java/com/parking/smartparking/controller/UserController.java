package com.parking.smartparking.controller;

import com.parking.smartparking.dto.PasswordChangeDTO;
import com.parking.smartparking.dto.UserProfileDTO;
import com.parking.smartparking.dto.UserRegistrationDTO;
import com.parking.smartparking.entity.User;
import com.parking.smartparking.service.UserService;
import com.parking.smartparking.service.VehicleService;
import com.parking.smartparking.service.impl.VehicleServiceImpl;
import com.parking.smartparking.util.PasswordMatchValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    // Add this line for logger
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PasswordMatchValidator passwordMatchValidator;

    @GetMapping("/")
    public String home() {
        return "home";
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDTO());
        }
        return "registration";
    }


    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        try {
            log.info("Received registration request for email: {}", userDTO.getEmail());

            // Custom password match validation
            passwordMatchValidator.validate(userDTO, result);

            // Check for existing email and phone
            if (userService.isEmailTaken(userDTO.getEmail())) {
                result.rejectValue("email", "email.exists", "Email already registered");
            }
            if (userService.isPhoneNumberTaken(userDTO.getPhoneNumber())) {
                result.rejectValue("phoneNumber", "phone.exists", "Phone number already registered");
            }

            if (result.hasErrors()) {
                log.warn("Validation errors occurred: {}", result.getAllErrors());
                return "registration";
            }

            User savedUser = userService.registerUser(userDTO);
            log.info("Successfully registered user with email: {}", savedUser.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Error in registration: ", e);
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "registration";
        }
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        log.info("Showing login page");
        // Add any necessary attributes to the model
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            log.info("Login attempt for email: {}", email);

            User user = userService.authenticateUser(email, password);

            if (user != null) {
                // Store user information in session
                session.setAttribute("userId", user.getId());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userFullName", user.getFullName());

                log.info("User successfully logged in: {}", email);
                return "redirect:/profile";  // Redirect to dashboard after successful login
            } else {
                log.warn("Failed login attempt for email: {}", email);
                redirectAttributes.addFlashAttribute("error", "Invalid email or password");
                return "redirect:/login?error";
            }
        } catch (Exception e) {
            log.error("Error during login: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred during login");
            return "redirect:/login?error";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been successfully logged out");
        return "redirect:/login?logout";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        // Add user information to model
        model.addAttribute("userFullName", session.getAttribute("userFullName"));
        model.addAttribute("latestVehicle", vehicleService.getLatestVehicle());
        return "dashboard";  // You'll need to create this view
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/login";
        }

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhoneNumber(user.getPhoneNumber());
        profileDTO.setAddress(user.getAddress());

        model.addAttribute("userProfile", profileDTO);
        return "edit-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("userProfile") UserProfileDTO profileDTO,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "edit-profile";
        }

        try {
            userService.updateProfile(userId, profileDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    @PostMapping("/profile/deactivate")
    public String deactivateAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            userService.deactivateAccount(userId);
            session.invalidate();
            redirectAttributes.addFlashAttribute("successMessage", "Account deactivated successfully");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating account: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            userService.deleteAccount(userId);
            session.invalidate();
            redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    // Add these methods to your existing UserController class

    @GetMapping("/change-password")
    public String showChangePasswordForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("passwordChangeRequest")) {
            model.addAttribute("passwordChangeRequest", new PasswordChangeDTO());
        }

        log.info("Showing change password form for user ID: {}", userId);
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeRequest") PasswordChangeDTO passwordChangeDTO,
                                 BindingResult result,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            log.info("Processing password change request for user ID: {}", userId);

            // Custom password match validation similar to registration
            if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmNewPassword())) {
                result.rejectValue("confirmNewPassword", "password.mismatch", "New passwords do not match");
            }

            // Verify current password
            if (!userService.verifyCurrentPassword(userId, passwordChangeDTO.getCurrentPassword())) {
                result.rejectValue("currentPassword", "password.incorrect", "Current password is incorrect");
            }

            if (result.hasErrors()) {
                log.warn("Validation errors in password change: {}", result.getAllErrors());
                return "change-password";
            }

            // Process password change
            userService.changePassword(userId, passwordChangeDTO.getNewPassword());

            log.info("Password successfully changed for user ID: {}", userId);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            log.error("Error changing password: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing password: " + e.getMessage());
            return "redirect:/change-password";
        }
    }

}