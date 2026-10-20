package com.trading.userservice.service;

import com.trading.userservice.dto.AuthenticationResponse;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String USER_REGISTERED_TOPIC = "user.registered";

    public AuthenticationResponse register(RegisterRequest request){

        log.info("registering user: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException(
                    "Email already registered "+request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setWalletBalance(
                request.getInitialDeposit() != null
                        ? request.getInitialDeposit()
                        : BigDecimal.valueOf(10000));

        User savedUser = userRepository.save(user);

        log.info("User registered: {}", savedUser.getId());

        // Publish user.registered event
        Map<String, Object> userRegisteredEvent = new HashMap<>();
        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance", savedUser.getWalletBalance());

        kafkaTemplate.send(USER_REGISTERED_TOPIC, savedUser.getId(), userRegisteredEvent);

        String token = generateAccessToken(savedUser.getId(), savedUser.getEmail());

        String refreshToken = generateRefreshToken(savedUser.getId());

        return buildAuthResponse(savedUser, token, refreshToken);
    }

}
