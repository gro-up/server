package com.hamster.gro_up.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Schedule extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Step step;

    private LocalDateTime dueDate;

    private String position;

    private String memo;

    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Company company;

    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Schedule(Long id, Step step, String position, LocalDateTime dueDate, String memo, Company company, User user) {
        this.id = id;
        this.step = step;
        this.dueDate = dueDate;
        this.position = position;
        this.memo = memo;
        this.company = company;
        this.user = user;
    }
}
