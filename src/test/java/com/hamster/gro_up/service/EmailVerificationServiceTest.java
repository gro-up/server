package com.hamster.gro_up.service;

import com.hamster.gro_up.entity.EmailVerificationToken;
import com.hamster.gro_up.exception.auth.InvalidEmailVerificationTokenException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.repository.EmailVerificationTokenRepository;
import com.hamster.gro_up.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    @Test
    @DisplayName("이메일 인증 코드 발송에 성공한다")
    void sendVerificationCode_success() {
        // given
        String email = "test@example.com";
        given(userRepository.existsByEmail(email)).willReturn(false);

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        verify(tokenRepository).save(any(EmailVerificationToken.class));
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
        verify(tokenRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("인증 코드 검증에 성공하면 토큰이 사용 처리된다")
    void verifyCode_success() {
        // given
        String email = "test@example.com";
        String code = "123456";
        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .token(code)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        given(tokenRepository.findByEmailAndTokenAndUsedFalse(email, code)).willReturn(Optional.of(token));

        // when
        emailVerificationService.verifyCode(email, code);

        // then
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    @DisplayName("만료된 인증 코드는 예외가 발생한다")
    void verifyCode_fail_expired() {
        // given
        String email = "test@example.com";
        String code = "123456";
        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .token(code)
                .expiryDate(LocalDateTime.now().minusMinutes(1)) // 만료
                .used(false)
                .build();

        given(tokenRepository.findByEmailAndTokenAndUsedFalse(email, code)).willReturn(Optional.of(token));

        // when & then
        assertThrows(InvalidEmailVerificationTokenException.class, () -> emailVerificationService.verifyCode(email, code));
    }

    @Test
    @DisplayName("잘못된 인증 코드는 예외가 발생한다")
    void verifyCode_fail_invalid() {
        // given
        String email = "test@example.com";
        String code = "wrongcode";
        given(tokenRepository.findByEmailAndTokenAndUsedFalse(email, code)).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidEmailVerificationTokenException.class, () -> emailVerificationService.verifyCode(email, code));
    }

    @Test
    @DisplayName("이메일 인증이 완료된 경우 true 를 반환한다")
    void isEmailVerified_true() {
        // given
        String email = "test@example.com";
        given(tokenRepository.existsByEmailAndUsedTrue(email)).willReturn(Boolean.TRUE);

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
        given(tokenRepository.existsByEmailAndUsedTrue(email)).willReturn(Boolean.FALSE);

        // when
        boolean result = emailVerificationService.isEmailVerified(email);

        // then
        assertThat(result).isFalse();
    }
}