package com.unir.slrassistant.security.service;

import com.unir.slrassistant.security.data.UserRepository;
import com.unir.slrassistant.security.model.pojo.Role;
import com.unir.slrassistant.security.model.response.AuthenticationResponse;
import com.unir.slrassistant.security.model.pojo.User;
import com.unir.slrassistant.security.model.request.ChangeUserPasswordRequest;
import com.unir.slrassistant.security.model.request.RegisterRequest;
import com.unir.slrassistant.security.util.EmailUtil;
import com.unir.slrassistant.security.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Value("${slr-assistant.app.frontend.url}")
    private String frontEndUrl;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public List<User> getUsers() {
        return repository.findAll();
    }

    public List<User> findByName(String name) {
        return repository.findByName(name);
    }

    public List<User> findByEmail(String email) {
        return repository.findByEmailContaining(email);
    }

    public User getUser(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public User createUser(RegisterRequest registerRequest) {

        String password = passwordUtil.generateRandomPassword();

        // send email with password
        try {
            emailUtil.sendUserCredentials(registerRequest.getEmail(), password, frontEndUrl);
        } catch (Exception e) {
            throw new IllegalStateException("Error sending email");
        }

        return repository.save(User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(password))
                .createdDate(new Date())
                .updatedDate(new Date())
                .status(true)
                .build());
    }

    public AuthenticationResponse updateUser(Integer id, User user, Principal connectedUser) {

        var userConnected = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        boolean flag = false;
        User userToUpdate = repository.findById(id).orElse(null);

        assert userToUpdate != null;
        if(userConnected.getEmail().equals(userToUpdate.getEmail())) {
            flag = true;
        }

        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
        userToUpdate.setUpdatedDate(new Date());
        repository.save(userToUpdate);

        if(flag) {
            var jwtToken = jwtService.generateToken(userToUpdate);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .id(userToUpdate.getId())
                    .build();
        }
        else {
            return null;
        }
    }

    public User setStatus(Integer id, Boolean status) {
        User userToUpdate = repository.findById(id).orElse(null);
        if (userToUpdate != null) {
            userToUpdate.setStatus(status);
            userToUpdate.setUpdatedDate(new Date());
            return repository.save(userToUpdate);
        } else {
            return null;
        }
    }

    public User changePassword(Integer id, Map<String, String> payload) {

        if(!payload.get("password").equals(payload.get("passwordConfirm"))) {
            throw new IllegalStateException("Password are not the same");
        }

        User userToUpdate = repository.findById(id).orElse(null);
        if (userToUpdate != null) {
            userToUpdate.setPassword(passwordEncoder.encode(payload.get("password")));
            userToUpdate.setUpdatedDate(new Date());
            return repository.save(userToUpdate);
        } else {
            throw new IllegalStateException("User not found");
        }
    }

    public User userChangePassword(ChangeUserPasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("La contraseña actual es incorrecta");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);

        return user;
    }

    public List<Role> getRoles() {
        return List.of(Role.values());
    }
}
