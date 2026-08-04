package com.pulse.monitoring.service;

import com.pulse.monitoring.dto.LogIngestionRequest;
import com.pulse.monitoring.dto.LogSearchCriteria;
import com.pulse.monitoring.model.LogEntry;
import com.pulse.monitoring.repository.LogEntryRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LogIngestionService {

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AlertingEngineService alertingEngineService;

    public LogEntry ingestLog(LogIngestionRequest req) {
        LogEntry entry = new LogEntry(
            req.getTimestamp() != null ? req.getTimestamp() : LocalDateTime.now(),
            req.getServiceName() != null ? req.getServiceName() : "UNKNOWN_SERVICE",
            req.getEnvironment() != null ? req.getEnvironment() : "PRODUCTION",
            req.getLogLevel() != null ? req.getLogLevel().toUpperCase() : "INFO",
            req.getMessage() != null ? req.getMessage() : "",
            req.getTraceId() != null ? req.getTraceId() : UUID.randomUUID().toString().substring(0, 8),
            req.getSpanId(),
            req.getLoggerName(),
            req.getThreadName(),
            req.getHostIp() != null ? req.getHostIp() : "127.0.0.1",
            req.getExceptionStackTrace(),
            req.getTags()
        );

        LogEntry saved = logEntryRepository.save(entry);

        // Broadcast to WebSockets live feed
        try {
            messagingTemplate.convertAndSend("/topic/logs", saved);
        } catch (Exception e) {
            // ignore WS broadcast failures if no clients
        }

        // Evaluate Alert Rules
        alertingEngineService.evaluateLogAlerts(saved);

        return saved;
    }

    public List<LogEntry> ingestBatch(List<LogIngestionRequest> requests) {
        List<LogEntry> result = new ArrayList<>();
        for (LogIngestionRequest req : requests) {
            result.add(ingestLog(req));
        }
        return result;
    }

    public Page<LogEntry> searchLogs(LogSearchCriteria criteria, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        Specification<LogEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getServiceName() != null && !criteria.getServiceName().trim().isEmpty() && !"ALL".equalsIgnoreCase(criteria.getServiceName())) {
                predicates.add(cb.equal(root.get("serviceName"), criteria.getServiceName().trim()));
            }

            if (criteria.getLogLevel() != null && !criteria.getLogLevel().trim().isEmpty() && !"ALL".equalsIgnoreCase(criteria.getLogLevel())) {
                predicates.add(cb.equal(root.get("logLevel"), criteria.getLogLevel().trim().toUpperCase()));
            }

            if (criteria.getTraceId() != null && !criteria.getTraceId().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("traceId"), criteria.getTraceId().trim()));
            }

            if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String kw = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                Predicate msgLike = cb.like(cb.lower(root.get("message")), kw);
                Predicate stackLike = cb.like(cb.lower(root.get("exceptionStackTrace")), kw);
                predicates.add(cb.or(msgLike, stackLike));
            }

            if (criteria.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), criteria.getStartTime()));
            }

            if (criteria.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), criteria.getEndTime()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return logEntryRepository.findAll(spec, pageable);
    }

    public List<LogEntry> getRecentLogs() {
        return logEntryRepository.findTop50ByOrderByTimestampDesc();
    }

    public Map<String, Object> getLogStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", logEntryRepository.count());
        stats.put("totalErrors", logEntryRepository.countByLogLevel("ERROR") + logEntryRepository.countByLogLevel("FATAL"));
        stats.put("totalWarnings", logEntryRepository.countByLogLevel("WARN"));
        stats.put("totalInfo", logEntryRepository.countByLogLevel("INFO"));
        stats.put("byService", logEntryRepository.countLogsByService());
        stats.put("byLevel", logEntryRepository.countLogsByLevel());
        stats.put("serviceList", logEntryRepository.findDistinctServiceNames());
        return stats;
    }
}
