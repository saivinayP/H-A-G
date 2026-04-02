package com.hag.dashboard.auth;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HagUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public HagUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(), true, true, true,
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
