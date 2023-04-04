package com.ib.service.users.interfaces;

import com.ib.model.users.User;

public interface IUserService {
    User findByEmail(String email);

    User get(Integer id);
}
