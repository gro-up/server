package com.hamster.gro_up.repository;

import com.hamster.gro_up.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("select s from Schedule s join fetch s.company where s.id = :id")
    Optional<Schedule> findByIdWithCompany(@Param("id") Long id);

    @Query("select s from Schedule s join fetch s.company where s.user.id = :userId")
    List<Schedule> findByUserIdWithCompany(@Param("userId") Long userId);

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.user.id = :userId " +
           "AND s.dueDate BETWEEN :start AND :end " +
           "ORDER BY s.dueDate ASC")
    List<Schedule> findSchedulesInRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
