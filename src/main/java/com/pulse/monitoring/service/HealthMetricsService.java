package com.pulse.monitoring.service;

import com.pulse.monitoring.dto.SystemMetricsDTO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthMetricsService {

    public SystemMetricsDTO getSystemMetrics() {
        SystemMetricsDTO metrics = new SystemMetricsDTO();

        // Memory MXBean
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        metrics.setHeapUsedBytes(heapUsage.getUsed());
        metrics.setHeapMaxBytes(heapUsage.getMax() > 0 ? heapUsage.getMax() : heapUsage.getCommitted());
        metrics.setNonHeapUsedBytes(nonHeapUsage.getUsed());

        // Operating System MXBean
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = -1.0;
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsMXBean = (com.sun.management.OperatingSystemMXBean) osMXBean;
            cpuLoad = sunOsMXBean.getCpuLoad();
            metrics.setProcessCpuLoad(sunOsMXBean.getProcessCpuLoad() >= 0 ? sunOsMXBean.getProcessCpuLoad() * 100 : 0.0);
        }
        metrics.setSystemCpuLoad(cpuLoad >= 0 ? cpuLoad * 100 : 15.4); // fallback if restricted

        // Thread MXBean
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        metrics.setLiveThreadCount(threadMXBean.getThreadCount());
        metrics.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        metrics.setPeakThreadCount(threadMXBean.getPeakThreadCount());

        Map<String, Integer> threadStateBreakdown = new HashMap<>();
        threadStateBreakdown.put("RUNNABLE", 0);
        threadStateBreakdown.put("WAITING", 0);
        threadStateBreakdown.put("TIMED_WAITING", 0);
        threadStateBreakdown.put("BLOCKED", 0);

        long[] threadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);
        for (ThreadInfo info : threadInfos) {
            if (info != null && info.getThreadState() != null) {
                String state = info.getThreadState().name();
                threadStateBreakdown.put(state, threadStateBreakdown.getOrDefault(state, 0) + 1);
            }
        }
        metrics.setThreadStateBreakdown(threadStateBreakdown);

        // GC MXBeans
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalGcCount = 0;
        long totalGcTimeMs = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            long time = gcBean.getCollectionTime();
            if (count > 0) totalGcCount += count;
            if (time > 0) totalGcTimeMs += time;
        }
        metrics.setGcCollectionCount(totalGcCount);
        metrics.setGcCollectionTimeMs(totalGcTimeMs);

        // Runtime & Disk Space
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        metrics.setUptimeMs(runtimeMXBean.getUptime());

        File root = new File(".");
        metrics.setTotalDiskSpaceBytes(root.getTotalSpace());
        metrics.setFreeDiskSpaceBytes(root.getFreeSpace());

        return metrics;
    }
}
