package com.saasstarterkit.notification.repository;

import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserNotificationPreferencesRepository extends JpaRepository<UserNotificationPreferences, UUID> {
    
    Optional<UserNotificationPreferences> findByUserId(String userId);
    
    Optional<UserNotificationPreferences> findByUserIdAndTenantId(String userId, String tenantId);
    
    List<UserNotificationPreferences> findByTenantId(String tenantId);
    
    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.userId = :userId AND unp.enabled = true")
    List<UserNotificationPreferences> findEnabledPreferencesByUserId(@Param("userId") String userId);
    
    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.tenantId = :tenantId AND unp.enabled = true")
    List<UserNotificationPreferences> findEnabledPreferencesByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.userId = :userId AND unp.notificationType = :notificationType")
    Optional<UserNotificationPreferences> findByUserIdAndNotificationType(@Param("userId") String userId, @Param("notificationType") String notificationType);
    
    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.tenantId = :tenantId AND unp.notificationType = :notificationType")
    List<UserNotificationPreferences> findByTenantIdAndNotificationType(@Param("tenantId") String tenantId, @Param("notificationType") String notificationType);
    
    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.userId = :userId AND unp.channel = :channel")
    List<UserNotificationPreferences> findByUserIdAndChannel(@Param("userId") String userId, @Param("channel") String channel);
    
    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.tenantId = :tenantId AND unp.channel = :channel")
    List<UserNotificationPreferences> findByTenantIdAndChannel(@Param("tenantId") String tenantId, @Param("channel") String channel);
    
    boolean existsByUserId(String userId);
    
    boolean existsByUserIdAndTenantId(String userId, String tenantId);
    
    long countByTenantId(String tenantId);
    
    long countByUserId(String userId);
}
