package com.hamster.gro_up.repository;

import com.hamster.gro_up.entity.Retrospect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RetrospectRepository extends JpaRepository<Retrospect, Long> {

    @Query("select r from Retrospect r " +
           "join fetch r.schedule s " +
           "where r.id = :id")
    Optional<Retrospect> findByIdWithSchedule(@Param("id") Long id);


    @Query("select r from Retrospect r " +
           "join fetch r.schedule s " +
           "where r.user.id = :userId")
    List<Retrospect> findAllByUserIdWithSchedule(@Param("userId") Long userId);
}
