package com.hamster.gro_up.service;

import com.hamster.gro_up.exception.auth.InvalidEmailVerificationTokenException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    private static final String VERIFICATION_PREFIX = "email_verification:";
    private static final String VERIFIED_SUFFIX = ":verified";
    private static final long VERIFICATION_CODE_TTL_MINUTES = 10L;

    @Test
    @DisplayName("이메일 인증 코드 발송에 성공한다")
    void sendVerificationCode_success() {
        // given
        String email = "test@example.com";
        given(userRepository.existsByEmail(email)).willReturn(false);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        verify(redisTemplate.opsForValue()).set(
                startsWith(VERIFICATION_PREFIX),
                anyString(),
                eq(VERIFICATION_CODE_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
        verify(mailSender).send(mailCaptor.capture());
        SimpleMailMessage message = mailCaptor.getValue();
        assertThat(message.getTo()).contains(email);
        assertThat(message.getSubject()).isEqualTo("이메일 인증 코드");
        assertThat(message.getText()).contains("인증 코드:");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 예외가 발생한다")
    void sendVerificationCode_fail_duplicate() {
        // given
        String email = "test@example.com";
        given(userRepository.existsByEmail(email)).willReturn(true);

        // when & then
        assertThrows(DuplicateUserException.class, () -> emailVerificationService.sendVerificationCode(email));
        verify(redisTemplate, never()).opsForValue();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("인증 코드 검증에 성공하면 인증 상태가 저장된다")
    void verifyCode_success() {
        // given
        String email = "test@example.com";
        String code = "123456";
        String key = VERIFICATION_PREFIX + email;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(code);

        // when
        emailVerificationService.verifyCode(email, code);

        // then
        verify(valueOperations).set(
                eq(key + VERIFIED_SUFFIX),
                eq("true"),
                eq(VERIFICATION_CODE_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("잘못된 인증 코드는 예외가 발생한다")
    void verifyCode_fail_invalid() {
        // given
        String email = "test@example.com";
        String code = "wrongcode";
        String key = VERIFICATION_PREFIX + email;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn("654321"); // 불일치

        // when & then
        assertThrows(InvalidEmailVerificationTokenException.class, () -> emailVerificationService.verifyCode(email, code));
    }

    @Test
    @DisplayName("이메일 인증이 완료된 경우 true 를 반환한다")
    void isEmailVerified_true() {
        // given
        String email = "test@example.com";
        String verifiedKey = VERIFICATION_PREFIX + email + VERIFIED_SUFFIX;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(verifiedKey)).willReturn("true");

        // when
        boolean result = emailVerificationService.isEmailVerified(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일 인증이 완료되지 않은 경우 false 를 반환한다")
    void isEmailVerified_false() {
        // given
        String email = "test@example.com";
        String verifiedKey = VERIFICATION_PREFIX + email + VERIFIED_SUFFIX;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(verifiedKey)).willReturn(null);

        // when
        boolean result = emailVerificationService.isEmailVerified(email);

        // then
        assertThat(result).isFalse();
    }
}