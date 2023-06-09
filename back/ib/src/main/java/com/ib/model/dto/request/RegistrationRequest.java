package com.ib.model.dto.request;

import com.ib.model.users.EndUser;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    private Integer id;
    @NotNull
    @NotEmpty
    @NotBlank
    private String name;
    @NotNull
    @NotBlank
    @NotEmpty
    private String surname;
    @NotEmpty
    @NotNull
    @NotBlank
    @Pattern(regexp = "^$|^\\+381[0-9]{5,9}")
    private String telephoneNumber;
    @NotNull @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotEmpty
    @NotBlank
    private String email;
    @NotNull
    @NotEmpty
    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=_!]).{8,}$")
    private String password;
    @NotNull
    @NotEmpty
    @NotBlank
    private String userActivationType;

    public RegistrationRequest(EndUser endUser) {
        this.id = endUser.getId();
        this.name = endUser.getName();
        this.surname = endUser.getSurname();
        this.telephoneNumber = endUser.getTelephoneNumber();
        this.email = endUser.getEmail();
        this.password = endUser.getPassword();
    }


    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", telephoneNumber='" + telephoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", password='" + new BCryptPasswordEncoder().encode(password) + '\'' +
                ", userActivationType='" + userActivationType + '\'' +
                '}';
    }
}
