package com.ib.repository.users;

import com.ib.model.users.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEndUserRepository extends JpaRepository<EndUser,Integer> {
}
