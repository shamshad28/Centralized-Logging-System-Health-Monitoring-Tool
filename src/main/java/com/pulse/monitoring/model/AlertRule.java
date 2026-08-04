package com.pulse.monitoring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ruleName;

    private String serviceName; // null or specific service name

    @Column(nullable = false)
    private String metricType; // ERROR_LOG_COUNT, SERVICE_DOWN, HIGH_CPU, MEMORY_USAGE

    @Column(nullable = false)
    private Double threshold; // e.g. 5 errors, or 85% CPU

    private Integer timeWindowMinutes; // e.g. last 5 minutes

    @Column(nullable = false)
    private String severity; // CRITICAL, WARNING, INFO

    private Boolean enabled = true;

    public AlertRule() {}

    public AlertRule(String ruleName, String serviceName, String metricType, Double threshold, Integer timeWindowMinutes, String severity, Boolean enabled) {
        this.ruleName = ruleName;
        this.serviceName = serviceName;
        this.metricType = metricType;
        this.threshold = threshold;
        this.timeWindowMinutes = timeWindowMinutes;
        this.severity = severity;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }

    public Integer getTimeWindowMinutes() { return timeWindowMinutes; }
    public void setTimeWindowMinutes(Integer timeWindowMinutes) { this.timeWindowMinutes = timeWindowMinutes; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
