// Global State
let stompClient = null;
let isStreamPaused = false;
let autoScroll = true;
let totalLogsCount = 0;
let totalErrorsCount = 0;
let liveLogBuffer = [];
let currentPage = 0;
let currentTotalPages = 1;

// Chart Instances
let chartCpuInstance = null;
let chartMemoryInstance = null;
let chartThreadsInstance = null;

// Telemetry History
const cpuHistory = [];
const memHistory = [];
const timeLabels = [];

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initWebSocket();
    initCharts();
    loadDashboardData();
    setInterval(fetchTelemetry, 3000);
});

// Tab Navigation
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const tabName = item.getAttribute('data-tab');
            switchTab(tabName);
        });
    });

    document.getElementById('simToggleHeader').addEventListener('change', (e) => {
        toggleSimulator(e.target.checked);
    });

    document.getElementById('simToggleMain').addEventListener('change', (e) => {
        toggleSimulator(e.target.checked);
        document.getElementById('simToggleHeader').checked = e.target.checked;
    });

    document.getElementById('btnPauseStream').addEventListener('click', () => {
        isStreamPaused = !isStreamPaused;
        const btnText = document.getElementById('pauseBtnText');
        const icon = document.getElementById('pauseIcon');
        if (isStreamPaused) {
            btnText.textContent = "Resume Stream";
            icon.className = "fa-solid fa-play";
        } else {
            btnText.textContent = "Pause Live Stream";
            icon.className = "fa-solid fa-pause";
        }
    });

    document.getElementById('chkAutoScroll').addEventListener('change', (e) => {
        autoScroll = e.target.checked;
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));

    const btn = document.querySelector(`.nav-item[data-tab="${tabName}"]`);
    if (btn) btn.classList.add('active');

    const tab = document.getElementById(`tab-${tabName}`);
    if (tab) tab.classList.add('active');

    // Page title updates
    const titleMap = {
        'overview': ['System Overview', 'Real-time telemetry, live microservice logs & JVM metrics'],
        'logs': ['Centralized Log Explorer', 'Full-text log search, structured filters, and trace stack analysis'],
        'metrics': ['System & JVM Health Telemetry', 'Live CPU load, JVM Heap usage, thread states & node monitoring'],
        'alerts': ['Alert Rules & Incident Management', 'Automated threshold monitoring and incident response'],
        'simulator': ['Traffic Simulator Studio', 'Inject synthetic faults, error spikes & microservice load']
    };

    if (titleMap[tabName]) {
        document.getElementById('pageTitle').textContent = titleMap[tabName][0];
        document.getElementById('pageSubtitle').textContent = titleMap[tabName][1];
    }

    if (tabName === 'logs') performLogSearch();
    if (tabName === 'metrics') loadServices();
    if (tabName === 'alerts') loadAlertsAndIncidents();
}

// WebSocket Setup
function initWebSocket() {
    const socket = new SockJS('/ws-monitoring');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Quiet STOMP debug logs

    stompClient.connect({}, () => {
        document.getElementById('statusDot').className = 'status-indicator connected';
        document.getElementById('statusText').textContent = 'Live Connected';

        // Subscribe to live log stream
        stompClient.subscribe('/topic/logs', (message) => {
            const log = JSON.parse(message.body);
            handleIncomingLiveLog(log);
        });

        // Subscribe to health alerts
        stompClient.subscribe('/topic/alerts', (message) => {
            const incident = JSON.parse(message.body);
            handleIncomingAlert(incident);
        });

        // Subscribe to service health updates
        stompClient.subscribe('/topic/health', () => {
            loadServices();
        });

    }, (error) => {
        document.getElementById('statusDot').className = 'status-indicator';
        document.getElementById('statusText').textContent = 'Disconnected (Retrying)';
        setTimeout(initWebSocket, 4000);
    });
}

function handleIncomingLiveLog(log) {
    totalLogsCount++;
    if (log.logLevel === 'ERROR' || log.logLevel === 'FATAL') totalErrorsCount++;

    updateKPIs();

    if (isStreamPaused) return;

    const feed = document.getElementById('liveLogFeed');
    if (feed.querySelector('.empty-feed-placeholder')) {
        feed.innerHTML = '';
    }

    const line = document.createElement('div');
    line.className = 'log-line';
    line.onclick = () => openLogModal(log);

    const timeStr = log.timestamp ? log.timestamp.substring(11, 19) : new Date().toLocaleTimeString();

    line.innerHTML = `
        <span class="log-time">${timeStr}</span>
        <span class="log-service">${escapeHtml(log.serviceName)}</span>
        <span class="log-level-badge level-${log.logLevel}">${log.logLevel}</span>
        <span class="log-msg">${escapeHtml(log.message)}</span>
        <span class="log-trace">${log.traceId ? log.traceId : ''}</span>
    `;

    feed.prepend(line);
    liveLogBuffer.push(log);

    // Limit live DOM buffer size for performance
    if (feed.children.length > 80) {
        feed.removeChild(feed.lastChild);
    }

    document.getElementById('liveLogCountBadge').textContent = `${totalLogsCount} total`;

    if (autoScroll) {
        feed.scrollTop = 0;
    }
}

function handleIncomingAlert(incident) {
    loadAlertsAndIncidents();
}

// Initial Data Load
function loadDashboardData() {
    fetch('/api/v1/logs/stats')
        .then(r => r.json())
        .then(data => {
            totalLogsCount = data.totalLogs || 0;
            totalErrorsCount = data.totalErrors || 0;
            updateKPIs();
        }).catch(() => {});

    loadServices();
    loadAlertsAndIncidents();
}

function updateKPIs() {
    document.getElementById('kpiTotalLogs').textContent = totalLogsCount;
    document.getElementById('kpiTotalErrors').textContent = totalErrorsCount;
    
    const rate = totalLogsCount > 0 ? ((totalErrorsCount / totalLogsCount) * 100).toFixed(1) : 0;
    document.getElementById('kpiErrorRate').textContent = `${rate}% error rate`;
}

// Telemetry & Metrics Polling
function fetchTelemetry() {
    fetch('/api/v1/health/metrics')
        .then(r => r.json())
        .then(data => {
            const cpu = data.systemCpuLoad ? data.systemCpuLoad.toFixed(1) : 0;
            const heapMb = (data.heapUsedBytes / (1024 * 1024)).toFixed(0);
            const heapMaxMb = (data.heapMaxBytes / (1024 * 1024)).toFixed(0);

            document.getElementById('kpiCpuMem').textContent = `${cpu}% | ${heapMb} MB`;
            document.getElementById('kpiGcCount').textContent = `GC Collections: ${data.gcCollectionCount || 0}`;

            document.getElementById('valUptime').textContent = formatDuration(data.uptimeMs);
            document.getElementById('valGcStats').textContent = `${data.gcCollectionCount || 0} cycles (${data.gcCollectionTimeMs || 0} ms)`;
            document.getElementById('valThreadsCount').textContent = `${data.liveThreadCount} (${data.peakThreadCount} peak)`;

            const freeGb = (data.freeDiskSpaceBytes / (1024*1024*1024)).toFixed(1);
            const totalGb = (data.totalDiskSpaceBytes / (1024*1024*1024)).toFixed(1);
            const diskPct = totalGb > 0 ? (((totalGb - freeGb)/totalGb)*100).toFixed(0) : 50;
            document.getElementById('valDiskSpace').textContent = `${freeGb} GB free of ${totalGb} GB`;
            document.getElementById('diskProgressBar').style.width = `${diskPct}%`;

            updateCharts(cpu, heapMb, data.threadStateBreakdown);
        }).catch(() => {});
}

// Chart.js Graphs
function initCharts() {
    const ctxCpu = document.getElementById('chartCpu').getContext('2d');
    chartCpuInstance = new Chart(ctxCpu, {
        type: 'line',
        data: {
            labels: timeLabels,
            datasets: [{
                label: 'CPU Load (%)',
                data: cpuHistory,
                borderColor: '#38bdf8',
                backgroundColor: 'rgba(56, 189, 248, 0.1)',
                tension: 0.3,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { min: 0, max: 100, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { display: false } }
            },
            plugins: { legend: { display: false } }
        }
    });

    const ctxMem = document.getElementById('chartMemory').getContext('2d');
    chartMemoryInstance = new Chart(ctxMem, {
        type: 'line',
        data: {
            labels: timeLabels,
            datasets: [{
                label: 'Heap Used (MB)',
                data: memHistory,
                borderColor: '#a855f7',
                backgroundColor: 'rgba(168, 85, 247, 0.1)',
                tension: 0.3,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { display: false } }
            },
            plugins: { legend: { display: false } }
        }
    });

    const ctxThreads = document.getElementById('chartThreads').getContext('2d');
    chartThreadsInstance = new Chart(ctxThreads, {
        type: 'doughnut',
        data: {
            labels: ['RUNNABLE', 'WAITING', 'TIMED_WAITING', 'BLOCKED'],
            datasets: [{
                data: [10, 5, 3, 0],
                backgroundColor: ['#10b981', '#38bdf8', '#f59e0b', '#ef4444']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'right', labels: { color: '#94a3b8' } } }
        }
    });
}

function updateCharts(cpu, heapMb, threadStates) {
    const nowStr = new Date().toLocaleTimeString();
    timeLabels.push(nowStr);
    cpuHistory.push(cpu);
    memHistory.push(heapMb);

    if (timeLabels.length > 15) {
        timeLabels.shift();
        cpuHistory.shift();
        memHistory.shift();
    }

    chartCpuInstance.update();
    chartMemoryInstance.update();

    if (threadStates) {
        chartThreadsInstance.data.datasets[0].data = [
            threadStates.RUNNABLE || 0,
            threadStates.WAITING || 0,
            threadStates.TIMED_WAITING || 0,
            threadStates.BLOCKED || 0
        ];
        chartThreadsInstance.update();
    }
}

// Service Health Nodes
function loadServices() {
    fetch('/api/v1/health/services')
        .then(r => r.json())
        .then(nodes => {
            const grid = document.getElementById('overviewServiceGrid');
            const table = document.getElementById('registeredNodesTableBody');
            
            grid.innerHTML = '';
            table.innerHTML = '';

            let upCount = 0;

            nodes.forEach(node => {
                if (node.status === 'UP') upCount++;

                const statusClass = node.status === 'UP' ? 'badge-success' : 'badge-danger';
                
                // Card for overview tab
                const card = document.createElement('div');
                card.className = 'service-card';
                card.innerHTML = `
                    <div class="service-card-header">
                        <span class="service-title">${escapeHtml(node.serviceName)}</span>
                        <span class="status-badge ${statusClass}">${node.status}</span>
                    </div>
                    <small class="text-muted">${node.healthUrl}</small>
                    <div style="display:flex; justify-content:space-between; margin-top:0.3rem; font-size:0.75rem;">
                        <span>Latency: <strong>${node.responseTimeMs || 0} ms</strong></span>
                        <span>Env: ${node.environment || 'PROD'}</span>
                    </div>
                `;
                grid.appendChild(card);

                // Table row for metrics tab
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${escapeHtml(node.serviceName)}</strong></td>
                    <td><span class="badge badge-info">${node.environment}</span></td>
                    <td class="text-muted">${escapeHtml(node.healthUrl)}</td>
                    <td><span class="status-badge ${statusClass}">${node.status}</span></td>
                    <td>${node.responseTimeMs || 0} ms</td>
                    <td>${node.lastChecked ? node.lastChecked.substring(11, 19) : '--'}</td>
                    <td>
                        <button class="btn btn-xs btn-outline" onclick="pingServiceNode(${node.id})"><i class="fa-solid fa-rotate"></i> Ping</button>
                        <button class="btn btn-xs btn-danger" onclick="deleteServiceNode(${node.id})"><i class="fa-solid fa-trash"></i></button>
                    </td>
                `;
                table.appendChild(tr);
            });

            document.getElementById('kpiServicesHealth').textContent = `${upCount} / ${nodes.length} UP`;
        }).catch(() => {});
}

function pingServiceNode(id) {
    fetch(`/api/v1/health/services/${id}/ping`, { method: 'POST' })
        .then(() => loadServices());
}

function deleteServiceNode(id) {
    fetch(`/api/v1/health/services/${id}`, { method: 'DELETE' })
        .then(() => loadServices());
}

// Log Search and Filtering
function performLogSearch(page = 0) {
    currentPage = page;

    const service = document.getElementById('filterService').value;
    const level = document.getElementById('filterLevel').value;
    const traceId = document.getElementById('filterTraceId').value;
    const keyword = document.getElementById('filterKeyword').value;

    let url = `/api/v1/logs/search?page=${page}&size=20`;
    if (service !== 'ALL') url += `&serviceName=${encodeURIComponent(service)}`;
    if (level !== 'ALL') url += `&logLevel=${encodeURIComponent(level)}`;
    if (traceId) url += `&traceId=${encodeURIComponent(traceId)}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;

    fetch(url)
        .then(r => r.json())
        .then(data => {
            const tbody = document.getElementById('logsTableBody');
            tbody.innerHTML = '';

            if (data.content.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No matching logs found.</td></tr>`;
                return;
            }

            currentTotalPages = data.totalPages;

            data.content.forEach(log => {
                const tr = document.createElement('tr');
                const timeStr = log.timestamp ? log.timestamp.replace('T', ' ').substring(0, 19) : '--';
                
                tr.innerHTML = `
                    <td class="text-muted">${timeStr}</td>
                    <td><span class="log-service">${escapeHtml(log.serviceName)}</span></td>
                    <td><span class="log-level-badge level-${log.logLevel}">${log.logLevel}</span></td>
                    <td><code>${log.traceId || '--'}</code></td>
                    <td class="log-msg" title="${escapeHtml(log.message)}">${escapeHtml(log.message)}</td>
                    <td>
                        <button class="btn btn-xs btn-outline" onclick='openLogModal(${JSON.stringify(log).replace(/'/g, "&apos;")})'>
                            <i class="fa-solid fa-eye"></i> Inspect
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });

            document.getElementById('paginationInfo').textContent = `Page ${data.number + 1} of ${data.totalPages} (${data.totalElements} total entries)`;
            document.getElementById('btnPrevPage').disabled = data.first;
            document.getElementById('btnNextPage').disabled = data.last;
        });
}

function changePage(delta) {
    const target = currentPage + delta;
    if (target >= 0 && target < currentTotalPages) {
        performLogSearch(target);
    }
}

// Log Modal Popup
function openLogModal(log) {
    document.getElementById('modalTimestamp').textContent = log.timestamp ? log.timestamp.replace('T', ' ') : '--';
    document.getElementById('modalService').textContent = log.serviceName;
    document.getElementById('modalLevel').textContent = log.logLevel;
    document.getElementById('modalTraceId').textContent = log.traceId || '--';
    document.getElementById('modalHost').textContent = log.hostIp || '--';
    document.getElementById('modalEnv').textContent = log.environment || 'PRODUCTION';
    document.getElementById('modalMessage').textContent = log.message;

    const stackContainer = document.getElementById('modalStackContainer');
    if (log.exceptionStackTrace) {
        document.getElementById('modalStackTrace').textContent = log.exceptionStackTrace;
        stackContainer.style.display = 'block';
    } else {
        stackContainer.style.display = 'none';
    }

    document.getElementById('logModal').classList.add('active');
}

function closeLogModal() {
    document.getElementById('logModal').classList.remove('active');
}

// Register Node Modal
function openRegisterNodeModal() {
    document.getElementById('registerServiceModal').classList.add('active');
}
function closeRegisterNodeModal() {
    document.getElementById('registerServiceModal').classList.remove('active');
}
function submitRegisterNode() {
    const serviceName = document.getElementById('nodeNameInput').value;
    const healthUrl = document.getElementById('nodeUrlInput').value;
    const environment = document.getElementById('nodeEnvInput').value;

    if (!serviceName || !healthUrl) {
        alert('Please provide Service Name and Health URL');
        return;
    }

    fetch('/api/v1/health/services', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ serviceName, healthUrl, environment })
    }).then(() => {
        closeRegisterNodeModal();
        loadServices();
    });
}

// Alerts & Incidents
function loadAlertsAndIncidents() {
    fetch('/api/v1/alerts/incidents')
        .then(r => r.json())
        .then(incidents => {
            const container = document.getElementById('incidentsContainer');
            container.innerHTML = '';

            let openCount = 0;

            if (incidents.length === 0) {
                container.innerHTML = `<div class="text-muted p-3">No incidents reported. All systems nominal.</div>`;
            }

            incidents.forEach(inc => {
                if (inc.status === 'OPEN') openCount++;

                const card = document.createElement('div');
                card.className = 'service-card mt-2';
                card.innerHTML = `
                    <div class="service-card-header">
                        <strong><i class="fa-solid fa-triangle-exclamation text-red"></i> ${escapeHtml(inc.ruleName)}</strong>
                        <span class="status-badge ${inc.status === 'OPEN' ? 'badge-danger' : 'badge-success'}">${inc.status}</span>
                    </div>
                    <p class="small mt-1">${escapeHtml(inc.message)}</p>
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.4rem; font-size:0.75rem;">
                        <span class="text-muted">Triggered: ${inc.triggeredAt ? inc.triggeredAt.replace('T', ' ').substring(0, 19) : ''}</span>
                        ${inc.status === 'OPEN' ? `<button class="btn btn-xs btn-primary" onclick="resolveIncident(${inc.id})">Resolve</button>` : ''}
                    </div>
                `;
                container.appendChild(card);
            });

            document.getElementById('navAlertBadge').textContent = openCount;
        });

    fetch('/api/v1/alerts/rules')
        .then(r => r.json())
        .then(rules => {
            const tbody = document.getElementById('rulesTableBody');
            tbody.innerHTML = '';
            rules.forEach(rule => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${escapeHtml(rule.ruleName)}</strong></td>
                    <td>${rule.serviceName ? escapeHtml(rule.serviceName) : 'ANY (Global)'}</td>
                    <td><span class="badge badge-info">${rule.metricType}</span></td>
                    <td>${rule.threshold} (${rule.timeWindowMinutes || 1} min)</td>
                    <td><span class="status-badge badge-warning">${rule.severity}</span></td>
                    <td><button class="btn btn-xs btn-danger" onclick="deleteRule(${rule.id})"><i class="fa-solid fa-trash"></i></button></td>
                `;
                tbody.appendChild(tr);
            });
        });
}

function resolveIncident(id) {
    fetch(`/api/v1/alerts/incidents/${id}/resolve`, { method: 'POST' })
        .then(() => loadAlertsAndIncidents());
}

function deleteRule(id) {
    fetch(`/api/v1/alerts/rules/${id}`, { method: 'DELETE' })
        .then(() => loadAlertsAndIncidents());
}

// Traffic Simulator Controls
function toggleSimulator(enabled) {
    fetch(`/api/v1/simulator/toggle?enabled=${enabled}`, { method: 'POST' })
        .then(r => r.json())
        .then(data => {
            const badge = document.getElementById('simStatusBadge');
            if (data.enabled) {
                badge.className = 'status-badge badge-success';
                badge.textContent = 'RUNNING (2.5s interval)';
            } else {
                badge.className = 'status-badge badge-warning';
                badge.textContent = 'PAUSED';
            }
        });
}

function injectFault(serviceName, faultType) {
    fetch(`/api/v1/simulator/fault?serviceName=${serviceName}&faultType=${faultType}`, { method: 'POST' })
        .then(() => {
            loadDashboardData();
            switchTab('overview');
        });
}

// Helpers
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function formatDuration(ms) {
    if (!ms) return '0m';
    const totalSec = Math.floor(ms / 1000);
    const hrs = Math.floor(totalSec / 3600);
    const mins = Math.floor((totalSec % 3600) / 60);
    return `${hrs}h ${mins}m`;
}
