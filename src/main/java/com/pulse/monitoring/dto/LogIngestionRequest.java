package com.pulse.monitoring.dto;

import java.time.LocalDateTime;

public class LogIngestionRequest {
    private String serviceName;
    private String environment;
    private String logLevel;
    private String message;
    private String traceId;
    private String spanId;
    private String loggerName;
    private String threadName;
    private String hostIp;
    private String exceptionStackTrace;
    private String tags;
    private LocalDateTime timestamp;

    public LogIngestionRequest() {}

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

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
