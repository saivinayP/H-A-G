package com.hag.dashboard.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository repo, PasswordEncoder encoder) {
        this.repo    = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!repo.existsByUsername("HAGAdmin")) {
            repo.save(new UserEntity(
                    "HAGAdmin",
                    encoder.encode("HagDash1!"),
                    "ROLE_ADMIN"
            ));
            LOG.info("HAG Dashboard → Default admin 'HAGAdmin' created.");
        } else {
            LOG.info("HAG Dashboard → Admin 'HAGAdmin' already exists.");
        }
    }
}
