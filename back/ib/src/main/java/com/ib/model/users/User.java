package com.ib.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.InheritanceType.JOINED;
import static jakarta.persistence.InheritanceType.TABLE_PER_CLASS;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Inheritance(strategy=JOINED)
@Table(name = "members")
@Entity
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "telephone_number", nullable = false)
    private String telephoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authority_id")
    private Authority authority;

}
