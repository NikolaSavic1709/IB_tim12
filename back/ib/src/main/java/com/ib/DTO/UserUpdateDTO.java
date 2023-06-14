package com.ib.DTO;

import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    private Integer id;
    private String email;
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


    public UserUpdateDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.telephoneNumber = user.getTelephoneNumber();
        this.email = user.getEmail();
    }
}
