package com.pulse.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries", indexes = {
    @Index(name = "idx_service_level", columnList = "serviceName, logLevel"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_trace_id", columnList = "traceId")
})
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String serviceName;

    private String environment;

    @Column(nullable = false)
    private String logLevel; // TRACE, DEBUG, INFO, WARN, ERROR, FATAL

    @Column(length = 4000, nullable = false)
    private String message;

    private String traceId;
    private String spanId;
    private String loggerName;
    private String threadName;
    private String hostIp;

    @Column(length = 8000)
    private String exceptionStackTrace;

    @Column(length = 2000)
    private String tags; // JSON string e.g. {"userId": "123", "httpMethod": "POST"}

    public LogEntry() {}

    public LogEntry(LocalDateTime timestamp, String serviceName, String environment, String logLevel, 
                    String message, String traceId, String spanId, String loggerName, 
                    String threadName, String hostIp, String exceptionStackTrace, String tags) {
        this.timestamp = timestamp;
        this.serviceName = serviceName;
        this.environment = environment;
        this.logLevel = logLevel;
        this.message = message;
        this.traceId = traceId;
        this.spanId = spanId;
        this.loggerName = loggerName;
        this.threadName = threadName;
        this.hostIp = hostIp;
        this.exceptionStackTrace = exceptionStackTrace;
        this.tags = tags;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getLoggerName() { return loggerName; }
    public void setLoggerName(String loggerName) { this.loggerName = loggerName; }

    public String getThreadName() { return threadName; }
    public void setThreadName(String threadName) { this.threadName = threadName; }

    public String getHostIp() { return hostIp; }
    public void setHostIp(String hostIp) { this.hostIp = hostIp; }

    public String getExceptionStackTrace() { return exceptionStackTrace; }
    public void setExceptionStackTrace(String exceptionStackTrace) { this.exceptionStackTrace = exceptionStackTrace; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
