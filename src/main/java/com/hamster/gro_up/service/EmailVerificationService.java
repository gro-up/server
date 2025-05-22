package com.hamster.gro_up.service;

import com.hamster.gro_up.exception.auth.InvalidEmailVerificationTokenException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.from}")
    private String fromAddress;

    private static final String VERIFICATION_PREFIX = "email_verification:";
    private static final String VERIFIED_SUFFIX = ":verified";
    private static final long VERIFICATION_CODE_TTL_MINUTES = 10L;

    public void sendVerificationCode(String email) {
        // 4자리 숫자 코드 생성
        String code = String.format("%04d", new Random().nextInt(9999));

        // Redis 에 인증번호 저장 (key: email_verification:{email}, value: code)
        String key = VERIFICATION_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, VERIFICATION_CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + code);
        mailSender.send(message);
    }

    public void verifyCode(String email, String code) {
        String key = VERIFICATION_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null || !storedCode.equals(code)) {
            throw new InvalidEmailVerificationTokenException();
        }

        redisTemplate.opsForValue().set(key + VERIFIED_SUFFIX, "true", VERIFICATION_CODE_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.delete(key);
    }

    // 인증 완료 여부 확인
    public boolean isEmailVerified(String email) {
        String verifiedKey = VERIFICATION_PREFIX + email + VERIFIED_SUFFIX;
        String verified = redisTemplate.opsForValue().get(verifiedKey);
        return "true".equalsIgnoreCase(verified);
    }
}
