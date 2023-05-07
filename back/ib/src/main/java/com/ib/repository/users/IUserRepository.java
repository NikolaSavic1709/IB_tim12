package com.ib.repository.users;

import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Integer id);

    User findByTelephoneNumber(String telephoneNumber);

    @Query("SELECT e.isEnabled FROM User e where e.email = ?1")
    Boolean findEnabledByEmail(String email);

    @Query("SELECT e.isEnabled FROM User e where e.telephoneNumber = ?1")
    Boolean findEnabledByPhoneNumber(String telephoneNumber);
}
