package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Integer> {
    Optional<SysUser> findByUsername(String username);
    List<SysUser> findByRole(String role);
    List<SysUser> findByStatus(Integer status);
    boolean existsByUsername(String username);
}