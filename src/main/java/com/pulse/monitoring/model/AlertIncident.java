package com.pulse.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_incidents")
public class AlertIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ruleId;
    private String ruleName;
    private String serviceName;
    private String severity;

    @Column(length = 2000, nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private String status; // OPEN, RESOLVED, ACKNOWLEDGED

    public AlertIncident() {}

    public AlertIncident(Long ruleId, String ruleName, String serviceName, String severity, String message) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.serviceName = serviceName;
        this.severity = severity;
        this.message = message;
        this.triggeredAt = LocalDateTime.now();
        this.status = "OPEN";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
