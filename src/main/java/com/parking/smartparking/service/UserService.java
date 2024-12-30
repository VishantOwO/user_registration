package com.parking.smartparking.service;

import com.parking.smartparking.dto.UserProfileDTO;
import com.parking.smartparking.dto.UserRegistrationDTO;
import com.parking.smartparking.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface UserService {
    User registerUser(UserRegistrationDTO registrationDTO);
    boolean isEmailTaken(String email);
    boolean isPhoneNumberTaken(String phoneNumber);
    User authenticateUser(String email, String password);  // Changed return type to User

    User getUserById(Long id);
    void updateProfile(Long userId, UserProfileDTO profileDTO);

    void deactivateAccount(Long userId);
    void deleteAccount(Long userId);


}