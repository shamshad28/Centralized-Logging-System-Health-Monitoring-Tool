package com.pulse.monitoring.controller;

import com.pulse.monitoring.dto.SystemMetricsDTO;
import com.pulse.monitoring.model.ServiceNode;
import com.pulse.monitoring.service.HealthMetricsService;
import com.pulse.monitoring.service.ServiceHealthPinger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health")
@CrossOrigin(origins = "*")
public class HealthMetricsController {

    @Autowired
    private HealthMetricsService healthMetricsService;

    @Autowired
    private ServiceHealthPinger serviceHealthPinger;

    @GetMapping("/metrics")
    public ResponseEntity<SystemMetricsDTO> getSystemMetrics() {
        return ResponseEntity.ok(healthMetricsService.getSystemMetrics());
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceNode>> getAllServices() {
        return ResponseEntity.ok(serviceHealthPinger.getAllNodes());
    }

    @PostMapping("/services")
    public ResponseEntity<ServiceNode> registerService(@RequestBody ServiceNode node) {
        return ResponseEntity.ok(serviceHealthPinger.registerNode(node));
    }

    @PostMapping("/services/{id}/ping")
    public ResponseEntity<ServiceNode> pingService(@PathVariable Long id) {
        ServiceNode node = serviceHealthPinger.getAllNodes().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (node != null) {
            return ResponseEntity.ok(serviceHealthPinger.pingNode(node));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceHealthPinger.deleteNode(id);
        return ResponseEntity.noContent().build();
    }
}
