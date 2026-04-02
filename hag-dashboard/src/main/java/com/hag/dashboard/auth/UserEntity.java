package com.hag.dashboard.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;   // ROLE_ADMIN or ROLE_USER

    @Column(nullable = false)
    private boolean enabled = true;

    public UserEntity() {}

    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role     = role;
        this.enabled  = true;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getUsername()                  { return username; }
    public void   setUsername(String username)   { this.username = username; }

    public String getPassword()                  { return password; }
    public void   setPassword(String password)   { this.password = password; }

    public String getRole()              { return role; }
    public void   setRole(String role)   { this.role = role; }

    public boolean isEnabled()                   { return enabled; }
    public void    setEnabled(boolean enabled)   { this.enabled = enabled; }
}
