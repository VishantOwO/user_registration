package com.parking.smartparking.service.impl;

import com.parking.smartparking.dto.UserProfileDTO;
import com.parking.smartparking.dto.UserRegistrationDTO;
import com.parking.smartparking.entity.User;
import com.parking.smartparking.repository.UserRepository;
import com.parking.smartparking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(UserRegistrationDTO dto) {
        User user = new User();
        // Set all fields explicitly
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPhoneNumber(dto.getPhoneNumber().trim());
        user.setUserType(dto.getUserType());
        user.setPassword(hashPassword(dto.getPassword()));
        user.setAddress(dto.getAddress().trim());
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        // Log the user object before saving (for debugging)
        System.out.println("Attempting to save user: " + user);

        return userRepository.save(user);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isPhoneNumberTaken(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public User authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String hashedInputPassword = hashPassword(password);

            if (hashedInputPassword.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void updateProfile(Long userId, UserProfileDTO profileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(profileDTO.getFullName().trim());
        user.setEmail(profileDTO.getEmail().trim().toLowerCase());
        user.setPhoneNumber(profileDTO.getPhoneNumber().trim());
        user.setAddress(profileDTO.getAddress().trim());

        userRepository.save(user);
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long userId) {
        userRepository.deleteById(userId);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}