package com.pulse.monitoring.dto;

import java.util.Map;

public class SystemMetricsDTO {
    private double systemCpuLoad;
    private double processCpuLoad;
    private long heapUsedBytes;
    private long heapMaxBytes;
    private long nonHeapUsedBytes;
    private int liveThreadCount;
    private int daemonThreadCount;
    private int peakThreadCount;
    private Map<String, Integer> threadStateBreakdown;
    private long totalDiskSpaceBytes;
    private long freeDiskSpaceBytes;
    private long gcCollectionCount;
    private long gcCollectionTimeMs;
    private long uptimeMs;

    public SystemMetricsDTO() {}

    public double getSystemCpuLoad() { return systemCpuLoad; }
    public void setSystemCpuLoad(double systemCpuLoad) { this.systemCpuLoad = systemCpuLoad; }

    public double getProcessCpuLoad() { return processCpuLoad; }
    public void setProcessCpuLoad(double processCpuLoad) { this.processCpuLoad = processCpuLoad; }

    public long getHeapUsedBytes() { return heapUsedBytes; }
    public void setHeapUsedBytes(long heapUsedBytes) { this.heapUsedBytes = heapUsedBytes; }

    public long getHeapMaxBytes() { return heapMaxBytes; }
    public void setHeapMaxBytes(long heapMaxBytes) { this.heapMaxBytes = heapMaxBytes; }

    public long getNonHeapUsedBytes() { return nonHeapUsedBytes; }
    public void setNonHeapUsedBytes(long nonHeapUsedBytes) { this.nonHeapUsedBytes = nonHeapUsedBytes; }

    public int getLiveThreadCount() { return liveThreadCount; }
    public void setLiveThreadCount(int liveThreadCount) { this.liveThreadCount = liveThreadCount; }

    public int getDaemonThreadCount() { return daemonThreadCount; }
    public void setDaemonThreadCount(int daemonThreadCount) { this.daemonThreadCount = daemonThreadCount; }

    public int getPeakThreadCount() { return peakThreadCount; }
    public void setPeakThreadCount(int peakThreadCount) { this.peakThreadCount = peakThreadCount; }

    public Map<String, Integer> getThreadStateBreakdown() { return threadStateBreakdown; }
    public void setThreadStateBreakdown(Map<String, Integer> threadStateBreakdown) { this.threadStateBreakdown = threadStateBreakdown; }

    public long getTotalDiskSpaceBytes() { return totalDiskSpaceBytes; }
    public void setTotalDiskSpaceBytes(long totalDiskSpaceBytes) { this.totalDiskSpaceBytes = totalDiskSpaceBytes; }

    public long getFreeDiskSpaceBytes() { return freeDiskSpaceBytes; }
    public void setFreeDiskSpaceBytes(long freeDiskSpaceBytes) { this.freeDiskSpaceBytes = freeDiskSpaceBytes; }

    public long getGcCollectionCount() { return gcCollectionCount; }
    public void setGcCollectionCount(long gcCollectionCount) { this.gcCollectionCount = gcCollectionCount; }

    public long getGcCollectionTimeMs() { return gcCollectionTimeMs; }
    public void setGcCollectionTimeMs(long gcCollectionTimeMs) { this.gcCollectionTimeMs = gcCollectionTimeMs; }

    public long getUptimeMs() { return uptimeMs; }
    public void setUptimeMs(long uptimeMs) { this.uptimeMs = uptimeMs; }
}
