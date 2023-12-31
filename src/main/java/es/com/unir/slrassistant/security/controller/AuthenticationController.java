package es.com.unir.slrassistant.security.controller;

import es.com.unir.slrassistant.security.model.request.LoginRequest;
import es.com.unir.slrassistant.security.model.request.ResetPasswordRequest;
import es.com.unir.slrassistant.security.model.response.AuthenticationResponse;
import es.com.unir.slrassistant.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest loginRequest
    ){
        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(authenticationService.forgotPassword(payload.get("email").toString()));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<Boolean> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        return ResponseEntity.ok(authenticationService.resetPassword(resetPasswordRequest));
    }

}
