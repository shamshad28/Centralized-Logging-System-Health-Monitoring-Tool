package com.pulse.monitoring.repository;

import com.pulse.monitoring.model.ServiceNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceNodeRepository extends JpaRepository<ServiceNode, Long> {
    Optional<ServiceNode> findByServiceName(String serviceName);
}
