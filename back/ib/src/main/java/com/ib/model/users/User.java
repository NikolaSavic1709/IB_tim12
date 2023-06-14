package com.ib.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static jakarta.persistence.InheritanceType.JOINED;
import static jakarta.persistence.InheritanceType.TABLE_PER_CLASS;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Inheritance(strategy=JOINED)
@Table(name = "members",schema = "public")
@Entity
public class User implements UserDetails {

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

    @Column(name="is_enabled")
    private boolean isEnabled;

    @Column(name = "mfa_token")
    private Integer MFAToken;

    @Column(name = "mfa_expiry_date")
    private LocalDateTime MFATokenExpiryDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authority authority = this.getAuthority();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authority.getName()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    public User(Integer id, String email, String name, String surname, String telephoneNumber, String password, Authority authority, boolean isEnabled) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.telephoneNumber = telephoneNumber;
        this.password = password;
        this.authority = authority;
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", telephoneNumber='" + telephoneNumber + '\'' +
                ", authority=" + authority +
                ", isEnabled=" + isEnabled +
                ", MFATokenExpiryDate=" + MFATokenExpiryDate +
                '}';
    }
}
