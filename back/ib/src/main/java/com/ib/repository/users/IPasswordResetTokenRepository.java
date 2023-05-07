package com.ib.repository.users;

import com.ib.model.users.EndUser;
import com.ib.model.users.PasswordResetToken;
import com.ib.model.users.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Transactional
public interface IPasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Integer> {
    public Optional<PasswordResetToken> findByToken(Integer token);

    public void deleteAllByUser(User user);
}
