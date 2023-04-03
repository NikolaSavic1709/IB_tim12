package com.ib.repository.users;

import com.ib.model.users.UserActivation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IUserActivationRepository extends JpaRepository<UserActivation, Integer> {
}
