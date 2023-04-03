package com.ib.service;

import com.ib.model.users.Authority;
import com.ib.repository.users.IAuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityService {
    @Autowired
    private IAuthorityRepository authorityRepository;

    public Authority findById(Integer id) {
        Authority auth = this.authorityRepository.findById(id).orElse(null);
        return auth;
    }

    public Authority findByName(String name) {
        Authority auth = this.authorityRepository.findByName(name);
        return auth;
    }
}
