package com.pulse.monitoring.repository;

import com.pulse.monitoring.model.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {

    List<LogEntry> findTop50ByOrderByTimestampDesc();

    long countByLogLevel(String logLevel);

    long countByTimestampAfter(LocalDateTime since);

    long countByLogLevelAndTimestampAfter(String logLevel, LocalDateTime since);

    long countByServiceNameAndLogLevelAndTimestampAfter(String serviceName, String logLevel, LocalDateTime since);

    @Query("SELECT l.serviceName, COUNT(l) FROM LogEntry l GROUP BY l.serviceName")
    List<Object[]> countLogsByService();

    @Query("SELECT l.logLevel, COUNT(l) FROM LogEntry l GROUP BY l.logLevel")
    List<Object[]> countLogsByLevel();

    @Query("SELECT DISTINCT l.serviceName FROM LogEntry l")
    List<String> findDistinctServiceNames();
}
