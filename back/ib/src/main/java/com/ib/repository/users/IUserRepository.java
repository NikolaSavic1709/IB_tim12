package com.ib.repository.users;

import com.ib.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User,Integer> {

    User findByEmail(String email);
}
