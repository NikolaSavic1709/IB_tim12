package com.ib.DTO;

import com.ib.model.users.EndUser;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {

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

    public RegistrationDTO(EndUser endUser) {
        this.id = endUser.getId();
        this.name = endUser.getName();
        this.surname = endUser.getSurname();
        this.telephoneNumber = endUser.getTelephoneNumber();
        this.email = endUser.getEmail();
    }
}
