package com.saasstarterkit.user.repository;

import com.saasstarterkit.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    List<UserRole> findByUserId(Long userId);
    
    Optional<UserRole> findByUserIdAndRoleName(Long userId, String roleName);
    
    void deleteByUserId(Long userId);
    
    List<UserRole> findByRoleName(String roleName);
}
