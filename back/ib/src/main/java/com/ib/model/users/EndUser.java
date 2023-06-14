package com.ib.model.users;

import com.ib.model.dto.request.RegistrationRequest;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;


@NoArgsConstructor
@Getter
@Setter
@Entity
public class EndUser extends User{

    public EndUser(RegistrationRequest registrationRequest) {
        this.setName(registrationRequest.getName());
        this.setSurname(registrationRequest.getSurname());
        this.setEmail(registrationRequest.getEmail());
        this.setTelephoneNumber(registrationRequest.getTelephoneNumber());
        this.setPassword(registrationRequest.getPassword());
        this.setEnabled(false);
        this.setPasswordHistory(new ArrayList<>());
    }

    public EndUser(Integer id, String email, String name, String surname, String telephoneNumber, String password, Authority authority, boolean isEnabled) {
        super(id, email, name, surname, telephoneNumber, password, authority, isEnabled);
    }
}
