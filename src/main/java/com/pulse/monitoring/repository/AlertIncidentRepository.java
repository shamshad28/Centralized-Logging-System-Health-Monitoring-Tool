package com.pulse.monitoring.repository;

import com.pulse.monitoring.model.AlertIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertIncidentRepository extends JpaRepository<AlertIncident, Long> {
    List<AlertIncident> findTop50ByOrderByTriggeredAtDesc();
    List<AlertIncident> findByStatus(String status);
}
