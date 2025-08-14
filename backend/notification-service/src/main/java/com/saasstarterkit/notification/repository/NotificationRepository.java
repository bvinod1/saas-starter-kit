package com.saasstarterkit.notification.repository;

import com.saasstarterkit.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification entities
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by user ID
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find notifications by user ID with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find notifications by tenant ID
     */
    List<Notification> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    
    /**
     * Find notifications by organization ID
     */
    List<Notification> findByOrganizationIdOrderByCreatedAtDesc(String organizationId);
    
    /**
     * Find notifications by status
     */
    List<Notification> findByStatusOrderByCreatedAtDesc(Notification.NotificationStatus status);
    
    /**
     * Find notifications by user ID and status
     */
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, Notification.NotificationStatus status);
    
    /**
     * Find notifications by user ID and channel type
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, Notification.NotificationChannel type);
    
    /**
     * Find notifications by tenant ID and status
     */
    List<Notification> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, Notification.NotificationStatus status);
    
    /**
     * Find notifications by correlation ID
     */
    List<Notification> findByCorrelationId(String correlationId);
    
    /**
     * Find notifications by source event
     */
    List<Notification> findBySourceEventOrderByCreatedAtDesc(String sourceEvent);
    
    /**
     * Find pending notifications that are ready to be sent
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND " +
           "(n.scheduledAt IS NULL OR n.scheduledAt <= :now) AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > :now)")
    List<Notification> findPendingNotificationsReadyToSend(@Param("now") LocalDateTime now);
    
    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND " +
           "n.attemptCount < n.maxAttempts AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > :now)")
    List<Notification> findFailedNotificationsForRetry(@Param("now") LocalDateTime now);
    
    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Count notifications by user ID and status
     */
    long countByUserIdAndStatus(String userId, Notification.NotificationStatus status);
    
    /**
     * Count unread notifications by user ID
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.readAt IS NULL")
    long countUnreadNotifications(@Param("userId") String userId);
    
    /**
     * Find notifications created within a time range
     */
    List<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find notifications by user ID within a time range
     */
    List<Notification> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(String userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Find notifications by priority
     */
    List<Notification> findByPriorityOrderByCreatedAtDesc(Notification.NotificationPriority priority);
    
    /**
     * Find notifications by user ID and priority
     */
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(String userId, Notification.NotificationPriority priority);
    
    /**
     * Find notifications by category (using JSONB query)
     */
    @Query(value = "SELECT * FROM shared.notifications WHERE data->>'category' = :category", nativeQuery = true)
    List<Notification> findByCategory(@Param("category") String category);
    
    /**
     * Find notifications by user ID and category
     */
    @Query(value = "SELECT * FROM shared.notifications WHERE user_id = :userId AND data->>'category' = :category", nativeQuery = true)
    List<Notification> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);
    
    /**
     * Delete old notifications (cleanup)
     */
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Mark notifications as read by user ID
     */
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.userId = :userId AND n.readAt IS NULL")
    void markNotificationsAsRead(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Find notifications that need to be sent (for scheduled notifications)
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'SCHEDULED' AND n.scheduledAt <= :now")
    List<Notification> findScheduledNotificationsReadyToSend(@Param("now") LocalDateTime now);
}
