package com.trading.userservice.dto;

import java.math.BigDecimal;

public class AuthenticationResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal walletBalance;

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
