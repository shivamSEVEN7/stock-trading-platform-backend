package com.trading.userservice.dto;

import com.trading.userservice.entity.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal walletBalance;
    private UserStatus status;
    private LocalDateTime createdAt;
}
