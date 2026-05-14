package com.bank.PAYMENT_SYSTEM.user.mapper;


import com.bank.PAYMENT_SYSTEM.user.dto.UserResponse;
import com.bank.PAYMENT_SYSTEM.user.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setEnabled(user.getEnabled());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}