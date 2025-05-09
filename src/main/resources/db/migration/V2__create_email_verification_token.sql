-- EmailVerificationToken 테이블
CREATE TABLE email_verification_token (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          token VARCHAR(255) NOT NULL,
                                          email VARCHAR(255) NOT NULL,
                                          expiry_date DATETIME(6) NOT NULL,
                                          used BOOLEAN NOT NULL DEFAULT FALSE,
                                          created_at DATETIME(6),
                                          modified_at DATETIME(6),
                                          PRIMARY KEY (id),
                                          INDEX idx_email (email)
);