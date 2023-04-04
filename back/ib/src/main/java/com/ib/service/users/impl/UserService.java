package com.ib.service.users.impl;

import com.ib.model.users.User;
import com.ib.repository.users.IUserRepository;
import com.ib.service.users.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseGet(null);
    }

    @Override
    public User get(Integer id) {
        return userRepository.findById(id).orElseGet(null);
    }
}
