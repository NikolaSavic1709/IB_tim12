package com.ib.repository.users;

import com.ib.model.users.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuthorityRepository extends JpaRepository<Authority,Integer> {
    Authority findByName(String name);

}
