package com.pulse.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_nodes")
public class ServiceNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String healthUrl;

    @Column(nullable = false)
    private String status; // UP, DOWN, DEGRADED, UNKNOWN

    private LocalDateTime lastChecked;
    private Long responseTimeMs;
    private String environment;
    private String version;

    public ServiceNode() {}

    public ServiceNode(String serviceName, String healthUrl, String status, String environment, String version) {
        this.serviceName = serviceName;
        this.healthUrl = healthUrl;
        this.status = status;
        this.environment = environment;
        this.version = version;
        this.lastChecked = LocalDateTime.now();
        this.responseTimeMs = 0L;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getHealthUrl() { return healthUrl; }
    public void setHealthUrl(String healthUrl) { this.healthUrl = healthUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
