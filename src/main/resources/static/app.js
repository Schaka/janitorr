// API endpoints
const API_BASE = '/api/management';

// State
let statusData = null;
let cleanupHistoryChart = null;
let mediaTypeChart = null;

// DOM elements
const elements = {
    // Status elements
    dryRunStatus: document.getElementById('dryRunStatus'),
    runOnceStatus: document.getElementById('runOnceStatus'),
    mediaEnabledStatus: document.getElementById('mediaEnabledStatus'),
    tagEnabledStatus: document.getElementById('tagEnabledStatus'),
    episodeEnabledStatus: document.getElementById('episodeEnabledStatus'),
    
    // Last run elements
    mediaLastRun: document.getElementById('mediaLastRun'),
    tagLastRun: document.getElementById('tagLastRun'),
    episodeLastRun: document.getElementById('episodeLastRun'),
    
    // Buttons
    refreshStatusBtn: document.getElementById('refreshStatusBtn'),
    mediaCleanupBtn: document.getElementById('mediaCleanupBtn'),
    tagCleanupBtn: document.getElementById('tagCleanupBtn'),
    episodeCleanupBtn: document.getElementById('episodeCleanupBtn'),
    
    // Stats elements
    totalFilesDeleted: document.getElementById('totalFilesDeleted'),
    totalSpaceFreed: document.getElementById('totalSpaceFreed'),
    moviesDeleted: document.getElementById('moviesDeleted'),
    showsDeleted: document.getElementById('showsDeleted'),
    
    // Message container
    messageContainer: document.getElementById('messageContainer')
};

// Utility functions
function showMessage(message, type = 'info') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    elements.messageContainer.appendChild(messageDiv);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        setTimeout(() => messageDiv.remove(), 300);
    }, 5000);
}

function updateStatusDisplay(data) {
    statusData = data;
    
    // Update status values
    elements.dryRunStatus.textContent = data.dryRun ? 'Enabled' : 'Disabled';
    elements.dryRunStatus.className = data.dryRun ? 'status-value enabled' : 'status-value disabled';
    
    elements.runOnceStatus.textContent = data.runOnce ? 'Enabled' : 'Disabled';
    elements.runOnceStatus.className = data.runOnce ? 'status-value enabled' : 'status-value disabled';
    
    elements.mediaEnabledStatus.textContent = data.mediaDeletionEnabled ? 'Enabled' : 'Disabled';
    elements.mediaEnabledStatus.className = data.mediaDeletionEnabled ? 'status-value enabled' : 'status-value disabled';
    
    elements.tagEnabledStatus.textContent = data.tagBasedDeletionEnabled ? 'Enabled' : 'Disabled';
    elements.tagEnabledStatus.className = data.tagBasedDeletionEnabled ? 'status-value enabled' : 'status-value disabled';
    
    elements.episodeEnabledStatus.textContent = data.episodeDeletionEnabled ? 'Enabled' : 'Disabled';
    elements.episodeEnabledStatus.className = data.episodeDeletionEnabled ? 'status-value enabled' : 'status-value disabled';
    
    // Update last run status
    elements.mediaLastRun.textContent = data.hasMediaCleanupRun ? 'Completed' : 'Not yet';
    elements.mediaLastRun.className = data.hasMediaCleanupRun ? 'status-value enabled' : 'status-value';
    
    elements.tagLastRun.textContent = data.hasTagBasedCleanupRun ? 'Completed' : 'Not yet';
    elements.tagLastRun.className = data.hasTagBasedCleanupRun ? 'status-value enabled' : 'status-value';
    
    elements.episodeLastRun.textContent = data.hasWeeklyEpisodeCleanupRun ? 'Completed' : 'Not yet';
    elements.episodeLastRun.className = data.hasWeeklyEpisodeCleanupRun ? 'status-value enabled' : 'status-value';
}

function setButtonLoading(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        button.dataset.originalText = button.textContent;
        button.innerHTML = '<span class="loading"></span> Running...';
    } else {
        button.disabled = false;
        button.textContent = button.dataset.originalText || button.textContent;
    }
}

// API calls
async function fetchStatus() {
    try {
        const response = await fetch(`${API_BASE}/status`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        updateStatusDisplay(data);
    } catch (error) {
        console.error('Error fetching status:', error);
        showMessage('Failed to fetch status: ' + error.message, 'error');
    }
}

async function fetchMetrics() {
    try {
        const response = await fetch(`${API_BASE}/metrics/summary`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        updateMetricsDisplay(data);
    } catch (error) {
        console.error('Error fetching metrics:', error);
    }
}

async function fetchCleanupHistory() {
    try {
        const response = await fetch(`${API_BASE}/metrics/cleanup-history`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        updateCleanupHistoryChart(data.events);
    } catch (error) {
        console.error('Error fetching cleanup history:', error);
    }
}

async function fetchMediaTypeDistribution() {
    try {
        const response = await fetch(`${API_BASE}/metrics/media-types`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        updateMediaTypeChart(data.distribution);
    } catch (error) {
        console.error('Error fetching media type distribution:', error);
    }
}

function updateMetricsDisplay(data) {
    if (elements.totalFilesDeleted) {
        elements.totalFilesDeleted.textContent = data.totalFilesDeleted.toLocaleString();
    }
    if (elements.totalSpaceFreed) {
        elements.totalSpaceFreed.textContent = `${data.totalSpaceFreedGB} GB`;
    }
    if (elements.moviesDeleted && data.mediaTypeCounts) {
        elements.moviesDeleted.textContent = (data.mediaTypeCounts.movies || 0).toLocaleString();
    }
    if (elements.showsDeleted && data.mediaTypeCounts) {
        elements.showsDeleted.textContent = (data.mediaTypeCounts.shows || 0).toLocaleString();
    }
}

function updateCleanupHistoryChart(events) {
    const ctx = document.getElementById('cleanupHistoryChart');
    if (!ctx) return;
    
    // Take last 30 events
    const recentEvents = events.slice(-30);
    
    // Aggregate by date
    const dateMap = new Map();
    recentEvents.forEach(event => {
        const date = event.timestamp.split('T')[0];
        if (!dateMap.has(date)) {
            dateMap.set(date, { files: 0, space: 0 });
        }
        const entry = dateMap.get(date);
        entry.files += event.filesDeleted;
        entry.space += event.spaceFreed;
    });
    
    const labels = Array.from(dateMap.keys());
    const filesData = Array.from(dateMap.values()).map(v => v.files);
    const spaceData = Array.from(dateMap.values()).map(v => (v.space / 1024 / 1024 / 1024).toFixed(2));
    
    if (cleanupHistoryChart) {
        cleanupHistoryChart.destroy();
    }
    
    cleanupHistoryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Files Deleted',
                data: filesData,
                backgroundColor: 'rgba(59, 130, 246, 0.5)',
                borderColor: 'rgba(59, 130, 246, 1)',
                borderWidth: 1,
                yAxisID: 'y'
            }, {
                label: 'Space Freed (GB)',
                data: spaceData,
                backgroundColor: 'rgba(16, 185, 129, 0.5)',
                borderColor: 'rgba(16, 185, 129, 1)',
                borderWidth: 1,
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Files'
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'GB'
                    },
                    grid: {
                        drawOnChartArea: false
                    }
                }
            }
        }
    });
}

function updateMediaTypeChart(distribution) {
    const ctx = document.getElementById('mediaTypeChart');
    if (!ctx) return;
    
    const labels = Object.keys(distribution);
    const data = Object.values(distribution);
    
    if (mediaTypeChart) {
        mediaTypeChart.destroy();
    }
    
    mediaTypeChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels.map(l => l.charAt(0).toUpperCase() + l.slice(1)),
            datasets: [{
                data: data,
                backgroundColor: [
                    'rgba(59, 130, 246, 0.8)',
                    'rgba(16, 185, 129, 0.8)',
                    'rgba(245, 158, 11, 0.8)',
                    'rgba(139, 92, 246, 0.8)',
                    'rgba(239, 68, 68, 0.8)'
                ],
                borderColor: [
                    'rgba(59, 130, 246, 1)',
                    'rgba(16, 185, 129, 1)',
                    'rgba(245, 158, 11, 1)',
                    'rgba(139, 92, 246, 1)',
                    'rgba(239, 68, 68, 1)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

async function refreshDashboard() {
    await Promise.all([
        fetchStatus(),
        fetchMetrics(),
        fetchCleanupHistory(),
        fetchMediaTypeDistribution()
    ]);
}

async function triggerCleanup(endpoint, buttonElement) {
    setButtonLoading(buttonElement, true);
    
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            showMessage(data.message, 'success');
        } else {
            showMessage(data.message, 'error');
        }
        
        // Refresh dashboard after cleanup
        setTimeout(() => refreshDashboard(), 1000);
    } catch (error) {
        console.error('Error triggering cleanup:', error);
        showMessage('Failed to trigger cleanup: ' + error.message, 'error');
    } finally {
        setButtonLoading(buttonElement, false);
    }
}

// Event listeners
elements.refreshStatusBtn.addEventListener('click', () => {
    showMessage('Refreshing dashboard...', 'info');
    refreshDashboard();
});

elements.mediaCleanupBtn.addEventListener('click', () => {
    triggerCleanup('/cleanup/media', elements.mediaCleanupBtn);
});

elements.tagCleanupBtn.addEventListener('click', () => {
    triggerCleanup('/cleanup/tag-based', elements.tagCleanupBtn);
});

elements.episodeCleanupBtn.addEventListener('click', () => {
    triggerCleanup('/cleanup/episodes', elements.episodeCleanupBtn);
});

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    refreshDashboard();
    
    // Auto-refresh dashboard every 30 seconds
    setInterval(refreshDashboard, 30000);
});
