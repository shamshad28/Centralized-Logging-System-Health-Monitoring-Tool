package com.pulse.monitoring.service;

import com.pulse.monitoring.model.AlertIncident;
import com.pulse.monitoring.model.AlertRule;
import com.pulse.monitoring.model.LogEntry;
import com.pulse.monitoring.repository.AlertIncidentRepository;
import com.pulse.monitoring.repository.AlertRuleRepository;
import com.pulse.monitoring.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertingEngineService {

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertIncidentRepository alertIncidentRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void evaluateLogAlerts(LogEntry entry) {
        if ("ERROR".equalsIgnoreCase(entry.getLogLevel()) || "FATAL".equalsIgnoreCase(entry.getLogLevel())) {
            List<AlertRule> rules = alertRuleRepository.findByEnabledTrue();

            for (AlertRule rule : rules) {
                if ("ERROR_LOG_COUNT".equalsIgnoreCase(rule.getMetricType())) {
                    if (rule.getServiceName() == null || rule.getServiceName().equalsIgnoreCase(entry.getServiceName())) {
                        int windowMinutes = rule.getTimeWindowMinutes() != null ? rule.getTimeWindowMinutes() : 5;
                        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);
                        
                        long count;
                        if (rule.getServiceName() != null) {
                            count = logEntryRepository.countByServiceNameAndLogLevelAndTimestampAfter(entry.getServiceName(), "ERROR", since);
                        } else {
                            count = logEntryRepository.countByLogLevelAndTimestampAfter("ERROR", since);
                        }

                        if (count >= rule.getThreshold()) {
                            triggerIncident(rule, entry.getServiceName(), 
                                String.format("Error threshold reached! %d ERROR logs detected in the last %d minute(s) (Threshold: %.0f). Last error: %s", 
                                    count, windowMinutes, rule.getThreshold(), entry.getMessage()));
                        }
                    }
                }
            }
        }
    }

    public void triggerIncident(AlertRule rule, String serviceName, String message) {
        // Check if an OPEN incident for this rule and service already exists to prevent duplicate noise
        List<AlertIncident> openIncidents = alertIncidentRepository.findByStatus("OPEN");
        boolean exists = openIncidents.stream().anyMatch(i -> i.getRuleId() != null && i.getRuleId().equals(rule.getId()) 
                                                               && i.getServiceName().equalsIgnoreCase(serviceName));

        if (!exists) {
            AlertIncident incident = new AlertIncident(rule.getId(), rule.getRuleName(), serviceName, rule.getSeverity(), message);
            AlertIncident saved = alertIncidentRepository.save(incident);
            
            // Broadcast WS alert event
            try {
                messagingTemplate.convertAndSend("/topic/alerts", saved);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public List<AlertRule> getAllRules() {
        return alertRuleRepository.findAll();
    }

    public AlertRule saveRule(AlertRule rule) {
        return alertRuleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        alertRuleRepository.deleteById(id);
    }

    public List<AlertIncident> getRecentIncidents() {
        return alertIncidentRepository.findTop50ByOrderByTriggeredAtDesc();
    }

    public AlertIncident resolveIncident(Long id) {
        AlertIncident incident = alertIncidentRepository.findById(id).orElse(null);
        if (incident != null) {
            incident.setStatus("RESOLVED");
            incident.setResolvedAt(LocalDateTime.now());
            AlertIncident updated = alertIncidentRepository.save(incident);
            try {
                messagingTemplate.convertAndSend("/topic/alerts", updated);
            } catch (Exception e) {}
            return updated;
        }
        return null;
    }
}
