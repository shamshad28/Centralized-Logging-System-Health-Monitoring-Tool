package com.pulse.monitoring.controller;

import com.pulse.monitoring.model.AlertIncident;
import com.pulse.monitoring.model.AlertRule;
import com.pulse.monitoring.service.AlertingEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertingEngineService alertingEngineService;

    @GetMapping("/rules")
    public ResponseEntity<List<AlertRule>> getAllRules() {
        return ResponseEntity.ok(alertingEngineService.getAllRules());
    }

    @PostMapping("/rules")
    public ResponseEntity<AlertRule> createRule(@RequestBody AlertRule rule) {
        return ResponseEntity.ok(alertingEngineService.saveRule(rule));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alertingEngineService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<AlertIncident>> getIncidents() {
        return ResponseEntity.ok(alertingEngineService.getRecentIncidents());
    }

    @PostMapping("/incidents/{id}/resolve")
    public ResponseEntity<AlertIncident> resolveIncident(@PathVariable Long id) {
        AlertIncident resolved = alertingEngineService.resolveIncident(id);
        if (resolved != null) {
            return ResponseEntity.ok(resolved);
        }
        return ResponseEntity.notFound().build();
    }
}
