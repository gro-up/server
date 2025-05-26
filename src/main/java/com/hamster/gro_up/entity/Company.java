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
public class Company extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String position;

    private String url;

    private String address;

    private String addressDetail;

    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Company(Long id, String companyName, String position, String url, String address, String addressDetail, User user) {
        this.id = id;
        this.companyName = companyName;
        this.position = position;
        this.url = url;
        this.address = address;
        this.addressDetail = addressDetail;
        this.user = user;
    }

    public void update(String companyName, String position, String address, String addressDetail, String url) {
        this.companyName = companyName;
        this.position = position;
        this.address = address;
        this.addressDetail = addressDetail;
        this.url = url;
    }

    public void validateOwner(Long userId) {
        if(!this.user.getId().equals(userId)) {
            throw new ForbiddenException();
        }
    }
}
