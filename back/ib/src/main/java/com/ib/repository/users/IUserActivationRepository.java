package com.ib.repository.users;

import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.model.users.UserActivation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Transactional
public interface IUserActivationRepository extends JpaRepository<UserActivation, Integer> {

    public Optional<UserActivation> findByToken(Integer token);

    public Optional<UserActivation> findByUser(EndUser user);
    public void deleteAllByUser(EndUser user);
}
