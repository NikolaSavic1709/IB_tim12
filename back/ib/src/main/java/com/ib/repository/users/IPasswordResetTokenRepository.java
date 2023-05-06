package com.ib.repository.users;

import com.ib.model.users.EndUser;
import com.ib.model.users.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Integer> {
    public Optional<PasswordResetToken> findByToken(Integer token);

    public void deleteAllByUser(EndUser user);
}
