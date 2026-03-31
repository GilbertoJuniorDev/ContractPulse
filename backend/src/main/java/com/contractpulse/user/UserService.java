package com.contractpulse.user;

import com.contractpulse.user.dto.SyncUserRequest;
import com.contractpulse.user.dto.UpdateProfileRequest;
import com.contractpulse.user.dto.UserResponse;
import com.contractpulse.user.exception.UserNotFoundException;
import com.contractpulse.user.model.User;
import com.contractpulse.user.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço com regras de negócio de usuário.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Busca um usuário pelo ID (UUID do Supabase Auth).
     *
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    @Transactional(readOnly = true)
    public UserResponse findById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponse.fromEntity(user);
    }

    /**
     * Sincroniza (cria ou atualiza) o usuário no banco local após login via Supabase.
     * O ID vem do JWT (sub claim), os dados vêm do request body.
     */
    @Transactional
    public UserResponse syncUser(UUID userId, SyncUserRequest request) {
        User user = userRepository.findById(userId)
                .map(existing -> {
                    // Só atualiza o nome — avatar customizado pelo usuário
                    // não deve ser sobrescrito pelo avatar do OAuth provider
                    existing.updateFullName(request.fullName());
                    log.info("Sync do usuário (nome atualizado): {}", userId);
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("Criando novo usuário local: {} ({})", request.fullName(), userId);
                    return User.builder()
                            .id(userId)
                            .fullName(request.fullName())
                            .email(request.email())
                            .avatarUrl(request.avatarUrl())
                            .build();
                });

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Atualiza o perfil do usuário autenticado (nome e avatar).
     * E-mail não é editável — é gerenciado pelo Supabase Auth.
     *
     * @param userId  ID do usuário extraído do JWT
     * @param request dados de atualização do perfil
     * @return resposta com dados atualizados do usuário
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateProfile(request.fullName(), request.avatarUrl());

        User saved = userRepository.save(user);
        log.info("Perfil atualizado pelo próprio usuário: {}", userId);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Lista todos os usuários com role CLIENT.
     * Usado no wizard de criação de contrato para selecionar o cliente.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findClientUsers() {
        return userRepository.findByRole(UserRole.CLIENT).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
