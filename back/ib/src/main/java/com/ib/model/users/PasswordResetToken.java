package com.ib.model.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "token", nullable = false)
    @NotNull
    private Integer token;

    @Column(name = "token_ramain_attempts", nullable = false)
    @NotNull
    private Integer tokenRemainAttempts;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @NotNull
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(name = "expiry_date", nullable = false)
    @NotNull
    private LocalDateTime tokenExpiryDate;
}

