package com.hamster.gro_up.entity;

import com.hamster.gro_up.exception.ForbiddenException;
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

    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    @ManyToOne(fetch = FetchType.LAZY)
    private Company company;

    private String companyName;

    private String companyLocation;

    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Schedule(Long id, Step step, String position, LocalDateTime dueDate, String memo, Company company, String companyName, String companyLocation, User user) {
        this.id = id;
        this.step = step;
        this.dueDate = dueDate;
        this.position = position;
        this.memo = memo;
        this.company = company;
        this.companyName = companyName;
        this.companyLocation = companyLocation;
        this.user = user;
    }

    public void update(LocalDateTime dueDate, String memo, String position, Step step) {
        this.dueDate = dueDate;
        this.memo = memo;
        this.position = position;
        this.step = step;
    }

    public void updateCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void validateOwner(Long userId) {
        if(!this.user.getId().equals(userId)) {
            throw new ForbiddenException();
        }
    }
}
