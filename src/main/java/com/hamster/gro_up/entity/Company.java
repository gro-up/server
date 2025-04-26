package com.hamster.gro_up.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Company extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    private String companyName;

    private String position;

    private String url;

    private String location;

    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Company(Long id, String companyName, String position, String url, String location, User user) {
        this.id = id;
        this.companyName = companyName;
        this.position = position;
        this.url = url;
        this.location = location;
        this.user = user;
    }

    public void update(String companyName, String position, String location, String url) {
        this.companyName = companyName;
        this.position = position;
        this.location = location;
        this.url = url;
    }
}
