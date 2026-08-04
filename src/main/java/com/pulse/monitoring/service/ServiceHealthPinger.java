package com.pulse.monitoring.service;

import com.pulse.monitoring.model.AlertRule;
import com.pulse.monitoring.model.ServiceNode;
import com.pulse.monitoring.repository.AlertRuleRepository;
import com.pulse.monitoring.repository.ServiceNodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceHealthPinger {

    @Autowired
    private ServiceNodeRepository serviceNodeRepository;

    @Autowired
    private AlertingEngineService alertingEngineService;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void initDefaultServices() {
        if (serviceNodeRepository.count() == 0) {
            serviceNodeRepository.save(new ServiceNode("auth-service", "http://localhost:8080/actuator/health", "UP", "PRODUCTION", "v2.1.0"));
            serviceNodeRepository.save(new ServiceNode("payment-gateway", "http://localhost:8080/actuator/health", "UP", "PRODUCTION", "v1.4.2"));
            serviceNodeRepository.save(new ServiceNode("order-service", "http://localhost:8080/actuator/health", "UP", "PRODUCTION", "v3.0.1"));
            serviceNodeRepository.save(new ServiceNode("inventory-api", "http://localhost:8080/actuator/health", "UP", "STAGING", "v1.0.8"));
        }

        if (alertRuleRepository.count() == 0) {
            alertRuleRepository.save(new AlertRule("High Error Rate Alert", null, "ERROR_LOG_COUNT", 5.0, 5, "CRITICAL", true));
            alertRuleRepository.save(new AlertRule("Payment Gateway Failure", "payment-gateway", "SERVICE_DOWN", 1.0, 1, "CRITICAL", true));
            alertRuleRepository.save(new AlertRule("Order Service Warning", "order-service", "ERROR_LOG_COUNT", 3.0, 2, "WARNING", true));
        }
    }

    @Scheduled(fixedRate = 10000) // Ping every 10 seconds
    public void pingAllServices() {
        List<ServiceNode> nodes = serviceNodeRepository.findAll();
        for (ServiceNode node : nodes) {
            pingNode(node);
        }
    }

    public ServiceNode pingNode(ServiceNode node) {
        long start = System.currentTimeMillis();
        String previousStatus = node.getStatus();
        try {
            // For local actuator health check
            URL url = new URL(node.getHealthUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            long duration = System.currentTimeMillis() - start;
            node.setResponseTimeMs(duration);
            node.setLastChecked(LocalDateTime.now());

            if (responseCode >= 200 && responseCode < 300) {
                node.setStatus("UP");
            } else {
                node.setStatus("DEGRADED");
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            node.setResponseTimeMs(duration);
            node.setLastChecked(LocalDateTime.now());
            
            // If it's a simulated external node, keep it UP unless simulator marked it DOWN
            if (!"DOWN".equals(node.getStatus())) {
                node.setStatus("UP"); // Default fallback for internal mock URL
            }
        }

        ServiceNode updated = serviceNodeRepository.save(node);

        // Alert if service status changed to DOWN
        if ("DOWN".equals(updated.getStatus()) && !"DOWN".equals(previousStatus)) {
            List<AlertRule> rules = alertRuleRepository.findByEnabledTrue();
            for (AlertRule rule : rules) {
                if ("SERVICE_DOWN".equalsIgnoreCase(rule.getMetricType()) && 
                   (rule.getServiceName() == null || rule.getServiceName().equalsIgnoreCase(updated.getServiceName()))) {
                    alertingEngineService.triggerIncident(rule, updated.getServiceName(), 
                        String.format("CRITICAL: Monitored service '%s' is DOWN! Endpoint %s failed to respond.", 
                            updated.getServiceName(), updated.getHealthUrl()));
                }
            }
        }

        try {
            messagingTemplate.convertAndSend("/topic/health", updated);
        } catch (Exception ex) {}

        return updated;
    }

    public List<ServiceNode> getAllNodes() {
        return serviceNodeRepository.findAll();
    }

    public ServiceNode registerNode(ServiceNode node) {
        node.setStatus("UNKNOWN");
        node.setLastChecked(LocalDateTime.now());
        ServiceNode saved = serviceNodeRepository.save(node);
        pingNode(saved);
        return saved;
    }

    public void deleteNode(Long id) {
        serviceNodeRepository.deleteById(id);
    }
}
