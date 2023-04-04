package com.ib.service;

import com.ib.model.users.Authority;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.model.users.UserActivation;
import com.ib.repository.users.IEndUserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class EndUserService {
    @Autowired
    AuthorityService authorityService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    IEndUserRepository endUserRepository;

    public EndUser save(EndUser endUser) { return endUserRepository.save(endUser);};


    public void register(EndUser newUser) {
        Authority authority = authorityService.findByName("END_USER");
        newUser.setAuthority(authority);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        String randomCode = RandomStringUtils.randomAlphanumeric(64);
        new UserActivation( newUser, LocalDateTime.now() , LocalDateTime.now().plusDays(3), randomCode);
        newUser.setEnabled(false);
        save(newUser);
    }
}
