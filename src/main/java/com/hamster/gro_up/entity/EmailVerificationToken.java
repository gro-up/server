package com.hamster.gro_up.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class EmailVerificationToken extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String email;

    private LocalDateTime expiryDate;

    @Setter
    private boolean used = false;

    @Builder
    public EmailVerificationToken(String email, LocalDateTime expiryDate, Long id, String token, boolean used) {
        this.email = email;
        this.expiryDate = expiryDate;
        this.id = id;
        this.token = token;
        this.used = used;
    }
}
