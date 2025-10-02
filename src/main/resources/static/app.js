// API endpoints
const API_BASE = '/api/management';

// State
let statusData = null;

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
        
        // Refresh status after cleanup
        setTimeout(() => fetchStatus(), 1000);
    } catch (error) {
        console.error('Error triggering cleanup:', error);
        showMessage('Failed to trigger cleanup: ' + error.message, 'error');
    } finally {
        setButtonLoading(buttonElement, false);
    }
}

// Event listeners
elements.refreshStatusBtn.addEventListener('click', () => {
    showMessage('Refreshing status...', 'info');
    fetchStatus();
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
    fetchStatus();
    
    // Auto-refresh status every 30 seconds
    setInterval(fetchStatus, 30000);
});
