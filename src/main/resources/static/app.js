// API endpoints
const API_BASE = '/api/management';

// State
let statusData = null;
let darkMode = localStorage.getItem('darkMode') === 'true';

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
    darkModeToggle: document.getElementById('darkModeToggle'),
    
    // Message container
    messageContainer: document.getElementById('messageContainer'),
    
    // New elements
    dryRunBanner: document.getElementById('dryRunBanner')
};

// Utility functions
function showMessage(message, type = 'info') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.innerHTML = `
        <span class="message-text">${message}</span>
        <button class="message-close" onclick="this.parentElement.remove()">×</button>
    `;
    
    elements.messageContainer.appendChild(messageDiv);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (messageDiv.parentElement) {
            messageDiv.style.opacity = '0';
            setTimeout(() => messageDiv.remove(), 300);
        }
    }, 5000);
}

function formatTimestamp(timestamp) {
    if (!timestamp) return 'Never';
    
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    let relativeTime;
    if (diffMins < 1) {
        relativeTime = 'Just now';
    } else if (diffMins < 60) {
        relativeTime = `${diffMins} minute${diffMins !== 1 ? 's' : ''} ago`;
    } else if (diffHours < 24) {
        relativeTime = `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`;
    } else if (diffDays < 7) {
        relativeTime = `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`;
    } else {
        relativeTime = date.toLocaleDateString();
    }
    
    const timeStr = date.toLocaleString();
    return `<span title="${timeStr}">${relativeTime}</span>`;
}

function confirmAction(message) {
    return new Promise((resolve) => {
        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.innerHTML = `
            <div class="modal-content">
                <h3>⚠️ Confirm Action</h3>
                <p>${message}</p>
                ${statusData?.dryRun ? '<p class="warning-text"><strong>Note:</strong> Dry-run mode is enabled. No files will be deleted.</p>' : '<p class="warning-text"><strong>Warning:</strong> This action cannot be undone.</p>'}
                <div class="modal-buttons">
                    <button class="btn btn-secondary" id="cancelBtn">Cancel</button>
                    <button class="btn btn-primary" id="confirmBtn">Confirm</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        
        const confirmBtn = modal.querySelector('#confirmBtn');
        const cancelBtn = modal.querySelector('#cancelBtn');
        
        confirmBtn.addEventListener('click', () => {
            modal.remove();
            resolve(true);
        });
        
        cancelBtn.addEventListener('click', () => {
            modal.remove();
            resolve(false);
        });
        
        // Close on escape key
        const escHandler = (e) => {
            if (e.key === 'Escape') {
                modal.remove();
                document.removeEventListener('keydown', escHandler);
                resolve(false);
            }
        };
        document.addEventListener('keydown', escHandler);
        
        // Focus confirm button
        setTimeout(() => confirmBtn.focus(), 100);
    });
}

function toggleDarkMode() {
    darkMode = !darkMode;
    localStorage.setItem('darkMode', darkMode);
    applyDarkMode();
}

function applyDarkMode() {
    if (darkMode) {
        document.body.classList.add('dark-mode');
        if (elements.darkModeToggle) {
            elements.darkModeToggle.textContent = '☀️';
            elements.darkModeToggle.title = 'Switch to Light Mode';
        }
    } else {
        document.body.classList.remove('dark-mode');
        if (elements.darkModeToggle) {
            elements.darkModeToggle.textContent = '🌙';
            elements.darkModeToggle.title = 'Switch to Dark Mode';
        }
    }
}

function updateStatusDisplay(data) {
    statusData = data;
    
    // Update dry-run banner visibility
    if (elements.dryRunBanner) {
        if (data.dryRun) {
            elements.dryRunBanner.style.display = 'flex';
        } else {
            elements.dryRunBanner.style.display = 'none';
        }
    }
    
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
    
    // Update last run status with timestamps
    elements.mediaLastRun.innerHTML = data.hasMediaCleanupRun ? formatTimestamp(data.timestamp) : 'Not yet';
    elements.mediaLastRun.className = data.hasMediaCleanupRun ? 'status-value enabled' : 'status-value';
    
    elements.tagLastRun.innerHTML = data.hasTagBasedCleanupRun ? formatTimestamp(data.timestamp) : 'Not yet';
    elements.tagLastRun.className = data.hasTagBasedCleanupRun ? 'status-value enabled' : 'status-value';
    
    elements.episodeLastRun.innerHTML = data.hasWeeklyEpisodeCleanupRun ? formatTimestamp(data.timestamp) : 'Not yet';
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
    // Show confirmation dialog
    const cleanupType = endpoint.split('/').pop().replace('-', ' ');
    const confirmed = await confirmAction(
        `Are you sure you want to trigger ${cleanupType} cleanup?`
    );
    
    if (!confirmed) {
        showMessage('Cleanup cancelled', 'info');
        return;
    }
    
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

if (elements.darkModeToggle) {
    elements.darkModeToggle.addEventListener('click', toggleDarkMode);
}

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + R: Refresh status
    if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
        e.preventDefault();
        fetchStatus();
        showMessage('Status refreshed (Ctrl+R)', 'info');
    }
    
    // Ctrl/Cmd + D: Toggle dark mode
    if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
        e.preventDefault();
        toggleDarkMode();
    }
});

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    applyDarkMode();
    fetchStatus();
    
    // Auto-refresh status every 30 seconds
    setInterval(fetchStatus, 30000);
});
