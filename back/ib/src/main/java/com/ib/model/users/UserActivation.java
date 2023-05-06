package com.ib.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class UserActivation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private EndUser user;

    @Column(name = "token", nullable = false)
    private Integer token;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    public UserActivation(EndUser user, LocalDateTime creationDate, LocalDateTime expiryDate, Integer token) {
        this.user = user;
        this.creationDate = creationDate;
        this.expiryDate = expiryDate;
        this.token = token;
    }
}