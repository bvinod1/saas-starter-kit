package com.saasstarterkit.organisation.repository;

import com.saasstarterkit.organisation.domain.OrganizationMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {
    
    List<OrganizationMember> findByOrganizationId(UUID organizationId);
    
    Page<OrganizationMember> findByOrganizationId(UUID organizationId, Pageable pageable);
    
    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    
    List<OrganizationMember> findByUserId(UUID userId);
    
    List<OrganizationMember> findByOrganizationIdAndStatus(UUID organizationId, com.saasstarterkit.organisation.domain.MemberStatus status);
    
    List<OrganizationMember> findByOrganizationIdAndRole(UUID organizationId, com.saasstarterkit.organisation.domain.MemberRole role);
    
    @Query("SELECT om FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.userId = :userId")
    Optional<OrganizationMember> findMemberInOrganization(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);
    
    @Query("SELECT COUNT(om) FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.status = 'ACTIVE'")
    long countActiveMembersByOrganizationId(@Param("organizationId") UUID organizationId);
    
    @Query("SELECT om FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.role = 'ADMIN'")
    List<OrganizationMember> findAdminsByOrganizationId(@Param("organizationId") UUID organizationId);
    
    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    
    void deleteByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
