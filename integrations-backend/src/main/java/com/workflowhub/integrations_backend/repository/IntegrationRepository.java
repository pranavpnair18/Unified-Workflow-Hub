package com.workflowhub.integrations_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workflowhub.integrations_backend.entity.Integration;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, Long> {
    List<Integration> findByUserId(Long userId);
    Optional<Integration> findByIdAndUserId(Long id, Long userId);
    List<Integration> findAllByUserIdAndProvider(Long userId, String provider);
    List<Integration> findByProviderAndStatus(String provider, String status);
    
    

}
