package com.unir.slrassistant.security.service;

import com.unir.slrassistant.security.data.PasswordResetTokenRepository;
import com.unir.slrassistant.security.data.UserRepository;
import com.unir.slrassistant.security.model.pojo.PasswordResetToken;
import com.unir.slrassistant.security.model.response.AuthenticationResponse;
import com.unir.slrassistant.security.model.pojo.User;
import com.unir.slrassistant.security.model.request.LoginRequest;
import com.unir.slrassistant.security.model.request.RegisterRequest;
import com.unir.slrassistant.security.model.request.ResetPasswordRequest;
import com.unir.slrassistant.security.util.EmailUtil;
import com.unir.slrassistant.security.util.TokenUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Value("${slr-assistant.app.frontend.url}")
    private String frontEndUrl;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        var user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(true)
                .createdDate(new Date())
                .updatedDate(new Date())
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));

        var user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public String forgotPassword(String email) {
        userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));

        passwordResetTokenRepository.findByEmail(email)
            .ifPresent(passwordResetTokenRepository::delete);

        String token = tokenUtil.generateToken();

        try {
            emailUtil.sendForgotPasswordEmail(email, token, frontEndUrl);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send email please try again");
        }

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setEmail(email);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(tokenUtil.getExpiryDate());
        passwordResetTokenRepository.save(passwordResetToken);

        return "Email sent... please reset password within 1 minute";
    }

    public Boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {

        if(!resetPasswordRequest.getPassword().equals(resetPasswordRequest.getPasswordConfirm())) {
            throw new RuntimeException("Password and confirm password does not match");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(resetPasswordRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (isBefore(passwordResetToken.getExpiryDate(), tokenUtil.getCurrentDate())) {
            throw new RuntimeException("Token expired");
        }

        User user = userRepository.findByEmail(passwordResetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);

        return true;
    }

    private boolean isBefore(long expiryDate, long currentDate) {
    	return expiryDate < currentDate;
    }
}
