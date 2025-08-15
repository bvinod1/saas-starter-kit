package com.saasstarterkit.analytics.repository;

import com.saasstarterkit.analytics.entity.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {
    
    List<UserActivity> findByUserId(String userId);
    
    List<UserActivity> findByTenantId(String tenantId);
    
    List<UserActivity> findByEventType(String eventType);
    
    List<UserActivity> findByUserIdAndTenantId(String userId, String tenantId);
    
    @Query("SELECT ua FROM UserActivity ua WHERE ua.tenantId = :tenantId AND ua.timestamp >= :startDate")
    List<UserActivity> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ua FROM UserActivity ua WHERE ua.userId = :userId AND ua.timestamp >= :startDate")
    List<UserActivity> findByUserIdAndDateRange(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ua FROM UserActivity ua WHERE ua.tenantId = :tenantId AND ua.eventType = :eventType")
    List<UserActivity> findByTenantIdAndEventType(@Param("tenantId") String tenantId, @Param("eventType") String eventType);
    
    @Query("SELECT ua FROM UserActivity ua WHERE ua.tenantId = :tenantId ORDER BY ua.timestamp DESC")
    Page<UserActivity> findByTenantIdOrderedByDate(@Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT ua FROM UserActivity ua WHERE ua.userId = :userId ORDER BY ua.timestamp DESC")
    Page<UserActivity> findByUserIdOrderedByDate(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.tenantId = :tenantId AND ua.timestamp >= :startDate")
    long countByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.userId = :userId AND ua.timestamp >= :startDate")
    long countByUserIdAndDateRange(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ua.eventType, COUNT(ua) FROM UserActivity ua WHERE ua.tenantId = :tenantId GROUP BY ua.eventType")
    List<Object[]> countEventsByTypeForTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT ua.eventType, COUNT(ua) FROM UserActivity ua WHERE ua.userId = :userId GROUP BY ua.eventType")
    List<Object[]> countEventsByTypeForUser(@Param("userId") String userId);
    
    boolean existsByUserIdAndSessionId(String userId, String sessionId);
    
    long countByTenantId(String tenantId);
    
    long countByUserId(String userId);
}
