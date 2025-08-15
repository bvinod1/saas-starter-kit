package com.saasstarterkit.notification.repository;

import com.saasstarterkit.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    
    Optional<NotificationTemplate> findByTemplateKey(String templateKey);
    
    List<NotificationTemplate> findByTemplateType(String templateType);
    
    List<NotificationTemplate> findByLanguage(String language);
    
    List<NotificationTemplate> findByActive(boolean active);
    
    Optional<NotificationTemplate> findByTemplateKeyAndLanguage(String templateKey, String language);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.templateKey = :templateKey AND nt.active = true")
    List<NotificationTemplate> findActiveTemplatesByKey(@Param("templateKey") String templateKey);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.templateType = :templateType AND nt.active = true")
    List<NotificationTemplate> findActiveTemplatesByType(@Param("templateType") String templateType);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.language = :language AND nt.active = true")
    List<NotificationTemplate> findActiveTemplatesByLanguage(@Param("language") String language);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.templateKey = :templateKey AND nt.language = :language AND nt.active = true")
    Optional<NotificationTemplate> findActiveTemplateByKeyAndLanguage(@Param("templateKey") String templateKey, @Param("language") String language);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.templateType = :templateType AND nt.language = :language AND nt.active = true")
    List<NotificationTemplate> findActiveTemplatesByTypeAndLanguage(@Param("templateType") String templateType, @Param("language") String language);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.templateKey LIKE %:searchTerm% OR nt.name LIKE %:searchTerm%")
    List<NotificationTemplate> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.templateType = :templateType AND nt.language = :language")
    List<NotificationTemplate> findByTypeAndLanguage(@Param("templateType") String templateType, @Param("language") String language);
    
    boolean existsByTemplateKey(String templateKey);
    
    boolean existsByTemplateKeyAndLanguage(String templateKey, String language);
    
    long countByTemplateType(String templateType);
    
    long countByLanguage(String language);
    
    long countByActive(boolean active);
}
