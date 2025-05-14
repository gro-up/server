package com.hamster.gro_up.entity;

import com.hamster.gro_up.exception.ForbiddenException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Retrospect extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memo;

    @JoinColumn(name = "schedule_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Schedule schedule;

    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Retrospect(Long id, String memo, Schedule schedule, User user) {
        this.id = id;
        this.memo = memo;
        this.schedule = schedule;
        this.user = user;
    }

    public void update(String memo) {
        this.memo = memo;
    }

    public void validateOwner(Long userId) {
        if(!this.user.getId().equals(userId)) {
            throw new ForbiddenException();
        }
    }
}
