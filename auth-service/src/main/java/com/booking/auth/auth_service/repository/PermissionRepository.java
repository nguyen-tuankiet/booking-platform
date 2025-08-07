package com.booking.auth.auth_service.repository;

import com.booking.auth.auth_service.entity.Permission;
import com.booking.auth.auth_service.utils.PermissionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByCategory(PermissionCategory category);

    boolean existsByName(String name);
}