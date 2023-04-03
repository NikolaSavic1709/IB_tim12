package com.ib.repository.users;

import com.ib.model.users.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Integer> {
}
