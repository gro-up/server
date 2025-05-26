-- schedule 테이블: company_location -> address 로 컬럼명 변경
ALTER TABLE schedule
    CHANGE company_location address VARCHAR(255);

-- schedule 테이블: address_detail 컬럼 추가
ALTER TABLE schedule
    ADD COLUMN address_detail VARCHAR(255) AFTER address;

-- company 테이블: location -> address 로 컬럼명 변경
ALTER TABLE company
    CHANGE location address VARCHAR(255);

-- company 테이블: address_detail 컬럼 추가
ALTER TABLE company
    ADD COLUMN address_detail VARCHAR(255) AFTER address;
