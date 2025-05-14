DROP TABLE IF EXISTS retrospect;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS company;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS email_verification_token;

CREATE TABLE user (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      email VARCHAR(255),
                      password VARCHAR(255),
                      role ENUM ('ROLE_ADMIN', 'ROLE_USER'),
                      created_at DATETIME(6),
                      modified_at DATETIME(6),
                      PRIMARY KEY (id),
                      UNIQUE (email)
);

CREATE TABLE company (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         company_name VARCHAR(255),
                         location VARCHAR(255),
                         position VARCHAR(255),
                         url VARCHAR(255),
                         created_at DATETIME(6),
                         modified_at DATETIME(6),
                         PRIMARY KEY (id)
);

CREATE TABLE schedule (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          company_id BIGINT NOT NULL,
                          position VARCHAR(255),
                          memo VARCHAR(255),
                          due_date DATETIME(6),
                          step ENUM ('ASSIGNMENT_TEST','CODING_TEST','DOCUMENT','FAIL','FINAL_PASS','FIRST_INTERVIEW','SECOND_INTERVIEW','THIRD_INTERVIEW'),
                          created_at DATETIME(6),
                          modified_at DATETIME(6),
                          PRIMARY KEY (id)
);

CREATE TABLE retrospect (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            schedule_id BIGINT NOT NULL,
                            memo VARCHAR(255),
                            created_at DATETIME(6),
                            modified_at DATETIME(6),
                            PRIMARY KEY (id),
                            UNIQUE (schedule_id)
);

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
