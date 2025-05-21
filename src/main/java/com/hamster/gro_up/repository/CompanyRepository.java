package com.hamster.gro_up.repository;

import com.hamster.gro_up.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByUserId(Long userId);

    @Query(value = "SELECT company_name FROM company WHERE user_id = :userId " +
                   "UNION " +
                   "SELECT company_name FROM schedule WHERE user_id = :userId", nativeQuery = true)
    List<String> findAllCompanyNamesByUserId(@Param("userId") Long userId);
}
