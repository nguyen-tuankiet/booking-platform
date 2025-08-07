package com.booking.auth.auth_service.repository;


import com.booking.auth.auth_service.entity.Role;
import com.booking.auth.auth_service.utils.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}