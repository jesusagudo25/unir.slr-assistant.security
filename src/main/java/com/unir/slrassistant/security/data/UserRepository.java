package com.unir.slrassistant.security.data;

import com.unir.slrassistant.security.model.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer>{

    Optional<User> findByEmail(String email);

    List<User> findByEmailContaining(String email);

    List<User> findByName(String name);

}
