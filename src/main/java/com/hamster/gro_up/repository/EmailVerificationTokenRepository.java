package com.hamster.gro_up.repository;

import com.hamster.gro_up.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByEmailAndTokenAndUsedFalse(String email, String token);

    boolean existsByEmailAndUsedTrue(String email);
}
