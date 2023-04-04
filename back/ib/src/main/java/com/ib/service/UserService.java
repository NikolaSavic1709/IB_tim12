package com.ib.service;

import com.ib.model.users.Authority;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.model.users.UserActivation;
import com.ib.repository.users.IUserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Random;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    PasswordEncoder passwordEncoder;

    public Collection<User> findAll() { return userRepository.findAll(); } ;

    public User findOne(Integer id) { return (User) userRepository.findById(id).orElse(null);};
    public User save(User user) { return (User) userRepository.save(user);};

    public void delete(Integer id) { userRepository.deleteById(id);};
    public User findByEmail(String email) { return (User) userRepository.findByEmail(email);}


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with email '%s'.", email));
        } else {
            return user;
        }
    }

}

