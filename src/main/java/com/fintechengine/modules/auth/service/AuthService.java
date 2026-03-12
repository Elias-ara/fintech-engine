package com.fintechengine.modules.auth.service;

import com.fintechengine.modules.auth.dto.AuthResponse;
import com.fintechengine.modules.auth.dto.LoginRequest;
import com.fintechengine.modules.auth.dto.RegisterRequest;
import com.fintechengine.modules.user.entity.User;
import com.fintechengine.modules.user.repository.UserRepository;
import com.fintechengine.security.JwtService;
import com.fintechengine.shared.exception.BusinessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByDocument(request.document())) {
            throw new BusinessException("Document already registered");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered");
        }

        User user = new User(
                request.name(),
                request.document(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return new AuthResponse(jwtService.generateToken(request.email()));
    }
}
