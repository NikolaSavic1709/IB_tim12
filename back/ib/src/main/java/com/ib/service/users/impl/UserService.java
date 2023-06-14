package com.ib.service.users.impl;

import com.ib.DTO.UserUpdateDTO;
import com.ib.exception.*;
import com.ib.model.users.User;
import com.ib.repository.users.IUserRepository;
import com.ib.service.users.interfaces.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService, UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IUserRepository userRepository;


    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return user;
    }

    @Override
    public User get(Integer id) {
        return userRepository.findById(id).orElseGet(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with email '%s'.", email));
        } else {
            return user;
        }
    }

    @Override
    public User update(Integer id, UserUpdateDTO userUpdateDTO) throws InvalidUserException {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) throw new InvalidUserException("Invalid user");
        user.setName(userUpdateDTO.getName());
        user.setSurname(userUpdateDTO.getSurname());
        user.setTelephoneNumber(userUpdateDTO.getTelephoneNumber());
        return userRepository.save(user);
    }
}
