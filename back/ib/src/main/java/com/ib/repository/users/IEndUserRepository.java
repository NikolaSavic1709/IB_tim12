package com.ib.repository.users;

import com.ib.model.users.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IEndUserRepository extends JpaRepository<EndUser,Integer> {

    EndUser findByEmail(String email);

    EndUser findByTelephoneNumber(String telephoneNumber);

    void deleteByEmail(String email);

    void deleteByTelephoneNumber(String telephoneNumber);

    @Query("SELECT e.isEnabled FROM EndUser e where e.email = ?1")
    Boolean findEnabledByEmail(String email);

    @Query("SELECT e.isEnabled FROM EndUser e where e.telephoneNumber = ?1")
    Boolean findEnabledByPhoneNumber(String telephoneNumber);
}
