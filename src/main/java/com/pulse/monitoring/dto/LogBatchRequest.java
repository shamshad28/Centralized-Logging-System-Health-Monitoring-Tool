package com.pulse.monitoring.dto;

import java.util.List;

public class LogBatchRequest {
    private List<LogIngestionRequest> logs;

    public LogBatchRequest() {}

    public List<LogIngestionRequest> getLogs() { return logs; }
    public void setLogs(List<LogIngestionRequest> logs) { this.logs = logs; }
}
