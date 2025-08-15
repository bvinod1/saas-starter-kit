package com.saasstarterkit.notification.repository;

import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.domain.enums.NotificationPriority;
import com.saasstarterkit.notification.domain.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserId(String userId);
    
    List<Notification> findByTenantId(String tenantId);
    
    List<Notification> findByUserIdAndTenantId(String userId, String tenantId);
    
    List<Notification> findByUserIdAndRead(String userId, boolean read);
    
    List<Notification> findByTenantIdAndRead(String tenantId, boolean read);
    
    List<Notification> findByUserIdAndPriority(String userId, NotificationPriority priority);
    
    List<Notification> findByTenantIdAndPriority(String tenantId, NotificationPriority priority);
    
    List<Notification> findByUserIdAndChannel(String userId, NotificationChannel channel);
    
    List<Notification> findByTenantIdAndChannel(String tenantId, NotificationChannel channel);
    
    List<Notification> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Notification> findByTenantIdAndCreatedAtBetween(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Notification> findByUserIdAndPriorityIn(String userId, List<NotificationPriority> priorities);
    
    List<Notification> findByTenantIdAndPriorityIn(String tenantId, List<NotificationPriority> priorities);
    
    List<Notification> findByUserIdAndChannelIn(String userId, List<NotificationChannel> channels);
    
    List<Notification> findByTenantIdAndChannelIn(String tenantId, List<NotificationChannel> channels);
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    Page<Notification> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    
    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(String userId, boolean read, Pageable pageable);
    
    Page<Notification> findByTenantIdAndReadOrderByCreatedAtDesc(String tenantId, boolean read, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.priority IN :priorities ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndPriorityInOrderByCreatedAtDesc(@Param("userId") String userId, 
                                                                   @Param("priorities") List<NotificationPriority> priorities, 
                                                                   Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.priority IN :priorities ORDER BY n.createdAt DESC")
    Page<Notification> findByTenantIdAndPriorityInOrderByCreatedAtDesc(@Param("tenantId") String tenantId, 
                                                                     @Param("priorities") List<NotificationPriority> priorities, 
                                                                     Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false")
    long countUnreadByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.tenantId = :tenantId AND n.read = false")
    long countUnreadByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.priority = :priority")
    long countByUserIdAndPriority(@Param("userId") String userId, @Param("priority") NotificationPriority priority);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.tenantId = :tenantId AND n.priority = :priority")
    long countByTenantIdAndPriority(@Param("tenantId") String tenantId, @Param("priority") NotificationPriority priority);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.priority IN ('URGENT', 'CRITICAL', 'FATAL') ORDER BY n.createdAt DESC")
    List<Notification> findUrgentByUserId(@Param("userId") String userId);
    
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.priority IN ('URGENT', 'CRITICAL', 'FATAL') ORDER BY n.createdAt DESC")
    List<Notification> findUrgentByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByTenantId(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);
    
    Optional<Notification> findByExternalId(String externalId);
    
    List<Notification> findByTemplateId(UUID templateId);
    
    List<Notification> findByStatus(String status);
    
    List<Notification> findByUserIdAndStatus(String userId, String status);
    
    List<Notification> findByTenantIdAndStatus(String tenantId, String status);
    
    @Query("SELECT DISTINCT n.userId FROM Notification n WHERE n.tenantId = :tenantId")
    List<String> findDistinctUserIdsByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.channel = :channel AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserIdAndChannel(@Param("userId") String userId, @Param("channel") NotificationChannel channel);
    
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.channel = :channel AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByTenantIdAndChannel(@Param("tenantId") String tenantId, @Param("channel") NotificationChannel channel);
    
    long countByUserId(String userId);
    
    long countByTenantId(String tenantId);
    
    long countByUserIdAndRead(String userId, boolean read);
    
    long countByTenantIdAndRead(String tenantId, boolean read);
}
