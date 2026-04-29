package com.dak.spravel.seeder;

import com.dak.spravel.model.User;
import com.dak.spravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default super admin user on startup.
 */
@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;

    public void run() {
        User su = new User();
        su.setUsername("su");
        su.setFullname("Super Admin");
        su.setPassword("su@123");
        process(su);
    }

    public void process(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) return;

        var userEntity = new User();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userEntity.setFullname(user.getFullname());
        userRepository.save(userEntity);
    }
}
