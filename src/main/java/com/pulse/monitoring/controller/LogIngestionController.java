package com.pulse.monitoring.controller;

import com.pulse.monitoring.dto.LogBatchRequest;
import com.pulse.monitoring.dto.LogIngestionRequest;
import com.pulse.monitoring.dto.LogSearchCriteria;
import com.pulse.monitoring.model.LogEntry;
import com.pulse.monitoring.service.LogIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/logs")
@CrossOrigin(origins = "*")
public class LogIngestionController {

    @Autowired
    private LogIngestionService logIngestionService;

    @PostMapping
    public ResponseEntity<LogEntry> ingestLog(@RequestBody LogIngestionRequest request) {
        LogEntry saved = logIngestionService.ingestLog(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<LogEntry>> ingestBatch(@RequestBody LogBatchRequest batchRequest) {
        List<LogEntry> savedLogs = logIngestionService.ingestBatch(batchRequest.getLogs());
        return new ResponseEntity<>(savedLogs, HttpStatus.CREATED);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<LogEntry>> getRecentLogs() {
        return ResponseEntity.ok(logIngestionService.getRecentLogs());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<LogEntry>> searchLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        LogSearchCriteria criteria = new LogSearchCriteria();
        criteria.setServiceName(serviceName);
        criteria.setLogLevel(logLevel);
        criteria.setTraceId(traceId);
        criteria.setKeyword(keyword);

        if (startTime != null && !startTime.isEmpty()) {
            try { criteria.setStartTime(LocalDateTime.parse(startTime)); } catch (Exception e) {}
        }
        if (endTime != null && !endTime.isEmpty()) {
            try { criteria.setEndTime(LocalDateTime.parse(endTime)); } catch (Exception e) {}
        }

        Page<LogEntry> result = logIngestionService.searchLogs(criteria, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(logIngestionService.getLogStats());
    }
}
