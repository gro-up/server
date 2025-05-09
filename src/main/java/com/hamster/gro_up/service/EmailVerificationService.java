package com.hamster.gro_up.service;

import com.hamster.gro_up.entity.EmailVerificationToken;
import com.hamster.gro_up.exception.auth.InvalidEmailVerificationTokenException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.repository.EmailVerificationTokenRepository;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void sendVerificationCode(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("이미 가입된 이메일입니다.");
        }

        // 6자리 숫자 코드 생성
        String code = String.format("%06d", new Random().nextInt(999999));

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .token(code)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        tokenRepository.save(token);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + code);
        mailSender.send(message);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        Optional<EmailVerificationToken> optionalToken = tokenRepository.findByEmailAndTokenAndUsedFalse(email, code);

        if (optionalToken.isPresent()) {
            EmailVerificationToken token = optionalToken.get();
            if (token.getExpiryDate().isAfter(LocalDateTime.now())) {
                token.setUsed(true);
                return;
            }
        }

        throw new InvalidEmailVerificationTokenException();
    }

    // 인증 완료 여부 확인
    public boolean isEmailVerified(String email) {
        // 인증 성공한 토큰이 있는지 확인
        return tokenRepository.existsByEmailAndUsedTrue(email);
    }
}
