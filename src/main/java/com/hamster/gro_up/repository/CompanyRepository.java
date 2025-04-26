package com.hamster.gro_up.repository;

import com.hamster.gro_up.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
