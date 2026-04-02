package com.hag.dashboard.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserController(UserRepository repo, PasswordEncoder encoder) {
        this.repo    = repo;
        this.encoder = encoder;
    }

    // ── Change own password ─────────────────────────────────

    @PostMapping("/settings/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication auth,
            RedirectAttributes redirect
    ) {
        UserEntity user = repo.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            redirect.addFlashAttribute("error", "User not found.");
            return "redirect:/settings";
        }

        if (!encoder.matches(currentPassword, user.getPassword())) {
            redirect.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/settings";
        }

        user.setPassword(encoder.encode(newPassword));
        repo.save(user);
        redirect.addFlashAttribute("success", "Password updated successfully.");
        return "redirect:/settings";
    }

    // ── Admin user management ───────────────────────────────

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUsers(Model model) {
        model.addAttribute("users", repo.findAll());
        return "settings";
    }

    @PostMapping("/admin/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "ROLE_USER") String role,
            RedirectAttributes redirect
    ) {
        if (repo.existsByUsername(username)) {
            redirect.addFlashAttribute("error", "User '" + username + "' already exists.");
            return "redirect:/settings";
        }

        repo.save(new UserEntity(username, encoder.encode(password), role));
        redirect.addFlashAttribute("success", "User '" + username + "' created.");
        return "redirect:/settings";
    }
}
