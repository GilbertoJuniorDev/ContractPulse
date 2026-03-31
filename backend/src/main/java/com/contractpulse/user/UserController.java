package com.contractpulse.user;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.user.dto.SyncUserRequest;
import com.contractpulse.user.dto.UpdateProfileRequest;
import com.contractpulse.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

/**
 * Controller REST para operações de usuário.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /**
     * Retorna o perfil do usuário autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UUID userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(userService.findById(userId));
    }

    /**
     * Sincroniza o usuário após login via Supabase Auth.
     * Cria o registro local caso não exista, ou atualiza se já existir.
     */
    @PostMapping("/sync")
    public ResponseEntity<UserResponse> syncUser(@Valid @RequestBody SyncUserRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(userService.syncUser(userId, request));
    }

    /**
     * Atualiza o perfil do usuário autenticado (nome e avatar).
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    /**
     * Lista todos os usuários com role CLIENT.
     * Usado no wizard de criação de contrato para selecionar o cliente.
     */
    @GetMapping("/clients")
    public ResponseEntity<List<UserResponse>> listClients() {
        return ResponseEntity.ok(userService.findClientUsers());
    }
}
