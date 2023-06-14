package com.ib.service.users.interfaces;

import com.ib.DTO.UserUpdateDTO;
import com.ib.exception.InvalidUserException;
import com.ib.model.users.User;

public interface IUserService {
    User findByEmail(String email);

    User get(Integer id);

    User update(Integer id, UserUpdateDTO userUpdateDTO) throws InvalidUserException;
}
