package com.ib.service;

import com.ib.exception.EmailAlreadyUsedException;
import com.ib.exception.MailSendingException;
import com.ib.exception.PhoneNumberAlreadyUsedException;
import com.ib.exception.SMSSendingException;
import com.ib.model.users.Authority;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.model.users.UserActivation;
import com.ib.repository.users.IEndUserRepository;
import com.ib.repository.users.IUserActivationRepository;
import com.ib.service.users.interfaces.IUserActivationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class EndUserService {
    @Autowired
    AuthorityService authorityService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    IEndUserRepository endUserRepository;
    @Autowired
    IUserActivationService userActivationService;

    public boolean checkUserEnabled(String email) {
        EndUser user = endUserRepository.findByEmail(email);
        Boolean enabled = endUserRepository.findEnabledByEmail(email);
        return user != null && enabled;
    }

    public EndUser save(EndUser endUser) { return endUserRepository.save(endUser);};


    public void register(EndUser newUser, String userActivationType) throws MailSendingException, SMSSendingException, PhoneNumberAlreadyUsedException, EmailAlreadyUsedException {
        Authority authority = authorityService.findByName("END_USER");
        newUser.setAuthority(authority);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        newUser.setEnabled(false);
        checkIFUserResourcesAreUsed(newUser);
        save(newUser);

        userActivationService.create(newUser, userActivationType);
    }

    public void checkIFUserResourcesAreUsed(EndUser newUser) throws EmailAlreadyUsedException, PhoneNumberAlreadyUsedException {
        EndUser userByEmail = endUserRepository.findByEmail(newUser.getEmail());
        Boolean enabled = endUserRepository.findEnabledByEmail(newUser.getEmail());
        if (userByEmail != null && enabled) {
            throw new EmailAlreadyUsedException("Email already used exception");
        }
        if (userByEmail != null) {
            endUserRepository.deleteByEmail(userByEmail.getEmail());
        }

        EndUser userByTelephoneNumber = endUserRepository.findByTelephoneNumber(newUser.getTelephoneNumber());
        Boolean enabledByNumber = endUserRepository.findEnabledByPhoneNumber(newUser.getTelephoneNumber());
        if (userByTelephoneNumber != null && enabledByNumber) {
            throw new PhoneNumberAlreadyUsedException("Phone number already used exception");
        }
        if (userByTelephoneNumber != null) {
            endUserRepository.deleteByTelephoneNumber(userByTelephoneNumber.getTelephoneNumber());
        }
    }

}
