package com.bank.PAYMENT_SYSTEM.user.service;


import com.bank.PAYMENT_SYSTEM.exception.ResourceNotFoundException;
import com.bank.PAYMENT_SYSTEM.user.dto.UserResponse;
import com.bank.PAYMENT_SYSTEM.user.entity.User;
import com.bank.PAYMENT_SYSTEM.user.mapper.UserMapper;
import com.bank.PAYMENT_SYSTEM.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return UserMapper.toResponse(user);
    }
}