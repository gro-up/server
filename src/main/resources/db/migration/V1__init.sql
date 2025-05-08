-- user 테이블
CREATE TABLE user (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      email VARCHAR(255),
                      name VARCHAR(255),
                      password VARCHAR(255),
                      role ENUM ('ROLE_ADMIN', 'ROLE_USER'),
                      created_at DATETIME(6),
                      modified_at DATETIME(6),
                      PRIMARY KEY (id),
                      UNIQUE (email)
);

-- company 테이블
CREATE TABLE company (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         company_name VARCHAR(255),
                         location VARCHAR(255),
                         position VARCHAR(255),
                         url VARCHAR(255),
                         created_at DATETIME(6),
                         modified_at DATETIME(6),
                         PRIMARY KEY (id),
                         FOREIGN KEY (user_id) REFERENCES user(id)
);

-- schedule 테이블
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
                          PRIMARY KEY (id),
                          FOREIGN KEY (user_id) REFERENCES user(id),
                          FOREIGN KEY (company_id) REFERENCES company(id)
);

-- favorite 테이블
CREATE TABLE favorite (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          company_id BIGINT NOT NULL,
                          created_at DATETIME(6),
                          modified_at DATETIME(6),
                          PRIMARY KEY (id),
                          FOREIGN KEY (user_id) REFERENCES user(id),
                          FOREIGN KEY (company_id) REFERENCES company(id)
);

-- retrospect 테이블
CREATE TABLE retrospect (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            schedule_id BIGINT NOT NULL,
                            memo VARCHAR(255),
                            created_at DATETIME(6),
                            modified_at DATETIME(6),
                            PRIMARY KEY (id),
                            FOREIGN KEY (user_id) REFERENCES user(id),
                            FOREIGN KEY (schedule_id) REFERENCES schedule(id),
                            UNIQUE (schedule_id)
);
