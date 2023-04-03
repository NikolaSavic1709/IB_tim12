package com.ib.repository.users;

import com.ib.model.users.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAdminRepository extends JpaRepository<Administrator, Integer> {
}
