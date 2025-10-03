// API endpoints
const CONFIG_API = '/api/management/config';

// State
let currentConfig = null;
let hasUnsavedChanges = false;

// DOM Elements - will be populated after DOM loads
const elements = {};

// Utility Functions
function showMessage(message, type = 'info') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    const container = document.getElementById('messageContainer');
    container.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        setTimeout(() => messageDiv.remove(), 300);
    }, 5000);
}

function setButtonLoading(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        button.classList.add('loading');
        button.dataset.originalText = button.textContent;
    } else {
        button.disabled = false;
        button.classList.remove('loading');
        if (button.dataset.originalText) {
            button.textContent = button.dataset.originalText;
        }
    }
}

function markUnsavedChanges() {
    hasUnsavedChanges = true;
    document.getElementById('saveConfigBtn').style.backgroundColor = '#ff9800';
}

// Tab Management
function initTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.dataset.tab;
            
            // Update active tab button
            tabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            // Update active tab content
            tabContents.forEach(content => {
                if (content.id === `${targetTab}-tab`) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });
        });
    });
}

// Load Configuration
async function loadConfiguration() {
    try {
        const response = await fetch(CONFIG_API);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        currentConfig = await response.json();
        populateForm(currentConfig);
        showMessage('Configuration loaded successfully', 'success');
    } catch (error) {
        console.error('Error loading configuration:', error);
        showMessage('Failed to load configuration: ' + error.message, 'error');
    }
}

// Populate Form
function populateForm(config) {
    // Services
    populateServiceConfig('sonarr', config.clients.sonarr);
    populateServiceConfig('radarr', config.clients.radarr);
    populateServiceConfig('jellyfin', config.clients.jellyfin);
    populateServiceConfig('emby', config.clients.emby);
    populateServiceConfig('jellyseerr', config.clients.jellyseerr);
    populateServiceConfig('jellystat', config.clients.jellystat);
    populateServiceConfig('streamystats', config.clients.streamystats);
    populateServiceConfig('bazarr', config.clients.bazarr);
    
    // Cleanup
    document.getElementById('media-deletion-enabled').checked = config.application.mediaDeletion.enabled;
    populateExpirationRules('movie', config.application.mediaDeletion.movieExpiration);
    populateExpirationRules('season', config.application.mediaDeletion.seasonExpiration);
    
    document.getElementById('tag-deletion-enabled').checked = config.application.tagBasedDeletion.enabled;
    document.getElementById('tag-deletion-minFreeDisk').value = config.application.tagBasedDeletion.minimumFreeDiskPercent;
    populateTagSchedules(config.application.tagBasedDeletion.schedules);
    
    document.getElementById('episode-deletion-enabled').checked = config.application.episodeDeletion.enabled;
    document.getElementById('episode-deletion-tag').value = config.application.episodeDeletion.tag;
    document.getElementById('episode-deletion-maxEpisodes').value = config.application.episodeDeletion.maxEpisodes;
    document.getElementById('episode-deletion-maxAge').value = parseDurationToDays(config.application.episodeDeletion.maxAge);
    
    // File System
    document.getElementById('filesystem-access').checked = config.fileSystem.access;
    document.getElementById('filesystem-validateSeeding').checked = config.fileSystem.validateSeeding;
    document.getElementById('filesystem-fromScratch').checked = config.fileSystem.fromScratch;
    document.getElementById('filesystem-leavingSoonDir').value = config.fileSystem.leavingSoonDir;
    document.getElementById('filesystem-mediaServerLeavingSoonDir').value = config.fileSystem.mediaServerLeavingSoonDir || '';
    document.getElementById('filesystem-freeSpaceCheckDir').value = config.fileSystem.freeSpaceCheckDir;
    
    // General
    document.getElementById('app-dryRun').checked = config.application.dryRun;
    document.getElementById('app-runOnce').checked = config.application.runOnce;
    document.getElementById('app-wholeTvShow').checked = config.application.wholeTvShow;
    document.getElementById('app-wholeShowSeedingCheck').checked = config.application.wholeShowSeedingCheck;
    document.getElementById('app-leavingSoon').value = parseDurationToDays(config.application.leavingSoon);
    populateExclusionTags(config.application.exclusionTags);
    
    document.getElementById('management-ui-enabled').checked = config.management.ui.enabled;
    
    hasUnsavedChanges = false;
    document.getElementById('saveConfigBtn').style.backgroundColor = '';
}

function populateServiceConfig(serviceName, config) {
    const enabled = document.getElementById(`${serviceName}-enabled`);
    const url = document.getElementById(`${serviceName}-url`);
    const apikey = document.getElementById(`${serviceName}-apikey`);
    
    if (enabled) enabled.checked = config.enabled;
    if (url) url.value = config.url;
    if (apikey) apikey.value = config.apiKey;
    
    // Service-specific fields
    if (serviceName === 'sonarr') {
        document.getElementById('sonarr-deleteEmptyShows').checked = config.deleteEmptyShows;
        document.getElementById('sonarr-importExclusions').checked = config.importExclusions;
        document.getElementById('sonarr-determineAgeBy').value = config.determineAgeBy || '';
    } else if (serviceName === 'radarr') {
        document.getElementById('radarr-onlyDeleteFiles').checked = config.onlyDeleteFiles;
        document.getElementById('radarr-importExclusions').checked = config.importExclusions;
        document.getElementById('radarr-determineAgeBy').value = config.determineAgeBy || '';
    } else if (serviceName === 'jellyfin' || serviceName === 'emby') {
        document.getElementById(`${serviceName}-username`).value = config.username;
        document.getElementById(`${serviceName}-password`).value = config.password;
        document.getElementById(`${serviceName}-delete`).checked = config.delete;
        document.getElementById(`${serviceName}-leavingSoonType`).value = config.leavingSoonType;
        document.getElementById(`${serviceName}-leavingSoonTv`).value = config.leavingSoonTv;
        document.getElementById(`${serviceName}-leavingSoonMovies`).value = config.leavingSoonMovies;
    } else if (serviceName === 'jellyseerr') {
        document.getElementById('jellyseerr-matchServer').checked = config.matchServer;
    } else if (serviceName === 'jellystat' || serviceName === 'streamystats') {
        document.getElementById(`${serviceName}-wholeTvShow`).checked = config.wholeTvShow;
    }
}

function populateExpirationRules(type, rules) {
    const container = document.getElementById(`${type}-expiration-container`);
    container.innerHTML = '';
    
    Object.entries(rules).forEach(([percent, duration]) => {
        const days = parseDurationToDays(duration);
        addExpirationRule(type, percent, days);
    });
}

function populateTagSchedules(schedules) {
    const container = document.getElementById('tag-schedules-container');
    container.innerHTML = '';
    
    schedules.forEach(schedule => {
        const days = parseDurationToDays(schedule.expiration);
        addTagSchedule(schedule.tag, days);
    });
}

function populateExclusionTags(tags) {
    const container = document.getElementById('exclusion-tags-container');
    container.innerHTML = '';
    
    tags.forEach(tag => addExclusionTag(tag));
}

function parseDurationToDays(durationStr) {
    const match = durationStr.match(/(\d+)d/);
    return match ? parseInt(match[1]) : 0;
}

// Dynamic List Management
function addExpirationRule(type, percent = '', days = '') {
    const container = document.getElementById(`${type}-expiration-container`);
    const item = document.createElement('div');
    item.className = 'expiration-item';
    item.innerHTML = `
        <input type="number" placeholder="Free Disk %" min="0" max="100" value="${percent}" class="${type}-percent">
        <span>→</span>
        <input type="number" placeholder="Days" min="1" value="${days}" class="${type}-days">
        <button type="button" class="remove-btn">×</button>
    `;
    
    item.querySelector('.remove-btn').addEventListener('click', () => {
        item.remove();
        markUnsavedChanges();
    });
    
    item.querySelectorAll('input').forEach(input => {
        input.addEventListener('change', markUnsavedChanges);
    });
    
    container.appendChild(item);
}

function addTagSchedule(tag = '', days = '') {
    const container = document.getElementById('tag-schedules-container');
    const item = document.createElement('div');
    item.className = 'schedule-item';
    item.innerHTML = `
        <input type="text" placeholder="Tag name" value="${tag}" class="schedule-tag">
        <span>→</span>
        <input type="number" placeholder="Days" min="1" value="${days}" class="schedule-days">
        <button type="button" class="remove-btn">×</button>
    `;
    
    item.querySelector('.remove-btn').addEventListener('click', () => {
        item.remove();
        markUnsavedChanges();
    });
    
    item.querySelectorAll('input').forEach(input => {
        input.addEventListener('change', markUnsavedChanges);
    });
    
    container.appendChild(item);
}

function addExclusionTag(tag = '') {
    const container = document.getElementById('exclusion-tags-container');
    const item = document.createElement('div');
    item.className = 'tag-item';
    item.innerHTML = `
        <input type="text" placeholder="Tag name" value="${tag}" class="exclusion-tag">
        <button type="button" class="remove-btn">×</button>
    `;
    
    item.querySelector('.remove-btn').addEventListener('click', () => {
        item.remove();
        markUnsavedChanges();
    });
    
    item.querySelector('input').addEventListener('change', markUnsavedChanges);
    
    container.appendChild(item);
}

// Collect Form Data
function collectFormData() {
    return {
        application: {
            dryRun: document.getElementById('app-dryRun').checked,
            runOnce: document.getElementById('app-runOnce').checked,
            wholeTvShow: document.getElementById('app-wholeTvShow').checked,
            wholeShowSeedingCheck: document.getElementById('app-wholeShowSeedingCheck').checked,
            leavingSoon: `${document.getElementById('app-leavingSoon').value}d`,
            exclusionTags: collectExclusionTags(),
            mediaDeletion: {
                enabled: document.getElementById('media-deletion-enabled').checked,
                movieExpiration: collectExpirationRules('movie'),
                seasonExpiration: collectExpirationRules('season')
            },
            tagBasedDeletion: {
                enabled: document.getElementById('tag-deletion-enabled').checked,
                minimumFreeDiskPercent: parseFloat(document.getElementById('tag-deletion-minFreeDisk').value),
                schedules: collectTagSchedules()
            },
            episodeDeletion: {
                enabled: document.getElementById('episode-deletion-enabled').checked,
                tag: document.getElementById('episode-deletion-tag').value,
                maxEpisodes: parseInt(document.getElementById('episode-deletion-maxEpisodes').value),
                maxAge: `${document.getElementById('episode-deletion-maxAge').value}d`
            }
        },
        fileSystem: {
            access: document.getElementById('filesystem-access').checked,
            validateSeeding: document.getElementById('filesystem-validateSeeding').checked,
            leavingSoonDir: document.getElementById('filesystem-leavingSoonDir').value,
            mediaServerLeavingSoonDir: document.getElementById('filesystem-mediaServerLeavingSoonDir').value || null,
            fromScratch: document.getElementById('filesystem-fromScratch').checked,
            freeSpaceCheckDir: document.getElementById('filesystem-freeSpaceCheckDir').value
        },
        clients: {
            sonarr: collectServiceConfig('sonarr', {
                deleteEmptyShows: 'sonarr-deleteEmptyShows',
                importExclusions: 'sonarr-importExclusions',
                determineAgeBy: 'sonarr-determineAgeBy'
            }),
            radarr: collectServiceConfig('radarr', {
                onlyDeleteFiles: 'radarr-onlyDeleteFiles',
                importExclusions: 'radarr-importExclusions',
                determineAgeBy: 'radarr-determineAgeBy'
            }),
            bazarr: collectServiceConfig('bazarr', {}),
            jellyfin: collectServiceConfig('jellyfin', {
                username: 'jellyfin-username',
                password: 'jellyfin-password',
                delete: 'jellyfin-delete',
                leavingSoonTv: 'jellyfin-leavingSoonTv',
                leavingSoonMovies: 'jellyfin-leavingSoonMovies',
                leavingSoonType: 'jellyfin-leavingSoonType'
            }),
            emby: collectServiceConfig('emby', {
                username: 'emby-username',
                password: 'emby-password',
                delete: 'emby-delete',
                leavingSoonTv: 'emby-leavingSoonTv',
                leavingSoonMovies: 'emby-leavingSoonMovies',
                leavingSoonType: 'emby-leavingSoonType'
            }),
            jellyseerr: collectServiceConfig('jellyseerr', {
                matchServer: 'jellyseerr-matchServer'
            }),
            jellystat: collectServiceConfig('jellystat', {
                wholeTvShow: 'jellystat-wholeTvShow'
            }),
            streamystats: collectServiceConfig('streamystats', {
                wholeTvShow: 'streamystats-wholeTvShow'
            })
        },
        management: {
            ui: {
                enabled: document.getElementById('management-ui-enabled').checked
            }
        }
    };
}

function collectServiceConfig(serviceName, extraFields) {
    const config = {
        enabled: document.getElementById(`${serviceName}-enabled`).checked,
        url: document.getElementById(`${serviceName}-url`).value,
        apiKey: document.getElementById(`${serviceName}-apikey`).value
    };
    
    Object.entries(extraFields).forEach(([key, fieldId]) => {
        const element = document.getElementById(fieldId);
        if (element) {
            if (element.type === 'checkbox') {
                config[key] = element.checked;
            } else {
                const value = element.value;
                config[key] = value === '' ? null : value;
            }
        }
    });
    
    return config;
}

function collectExpirationRules(type) {
    const rules = {};
    document.querySelectorAll(`#${type}-expiration-container .expiration-item`).forEach(item => {
        const percent = item.querySelector(`.${type}-percent`).value;
        const days = item.querySelector(`.${type}-days`).value;
        if (percent && days) {
            rules[parseInt(percent)] = `${days}d`;
        }
    });
    return rules;
}

function collectTagSchedules() {
    const schedules = [];
    document.querySelectorAll('#tag-schedules-container .schedule-item').forEach(item => {
        const tag = item.querySelector('.schedule-tag').value;
        const days = item.querySelector('.schedule-days').value;
        if (tag && days) {
            schedules.push({ tag, expiration: `${days}d` });
        }
    });
    return schedules;
}

function collectExclusionTags() {
    const tags = [];
    document.querySelectorAll('#exclusion-tags-container .tag-item').forEach(item => {
        const tag = item.querySelector('.exclusion-tag').value;
        if (tag) {
            tags.push(tag);
        }
    });
    return tags;
}

// Save Configuration
async function saveConfiguration() {
    const saveBtn = document.getElementById('saveConfigBtn');
    setButtonLoading(saveBtn, true);
    
    try {
        const config = collectFormData();
        const response = await fetch(CONFIG_API, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message, 'success');
            hasUnsavedChanges = false;
            saveBtn.style.backgroundColor = '';
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        console.error('Error saving configuration:', error);
        showMessage('Failed to save configuration: ' + error.message, 'error');
    } finally {
        setButtonLoading(saveBtn, false);
    }
}

// Test Connections
async function testConnection(event, service) {
    const testBtn = event.target;
    const resultSpan = document.getElementById(`${service}-test-result`);
    
    setButtonLoading(testBtn, true);
    resultSpan.textContent = 'Testing...';
    resultSpan.className = 'test-result testing';
    
    try {
        const config = collectFormData();
        const response = await fetch(`${CONFIG_API}/test/${service}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        resultSpan.textContent = result.message;
        resultSpan.className = `test-result ${result.success ? 'success' : 'error'}`;
    } catch (error) {
        console.error(`Error testing ${service}:`, error);
        resultSpan.textContent = `❌ Error: ${error.message}`;
        resultSpan.className = 'test-result error';
    } finally {
        setButtonLoading(testBtn, false);
    }
}

async function testAllConnections() {
    const testBtn = document.getElementById('testAllBtn');
    setButtonLoading(testBtn, true);
    
    try {
        const config = collectFormData();
        const response = await fetch(`${CONFIG_API}/test`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const results = await response.json();
        
        // Update UI with results
        Object.entries(results).forEach(([service, result]) => {
            if (result) {
                const resultSpan = document.getElementById(`${service}-test-result`);
                if (resultSpan) {
                    resultSpan.textContent = result.message;
                    resultSpan.className = `test-result ${result.success ? 'success' : 'error'}`;
                }
            }
        });
        
        showMessage('Connection tests completed', 'info');
    } catch (error) {
        console.error('Error testing connections:', error);
        showMessage('Failed to test connections: ' + error.message, 'error');
    } finally {
        setButtonLoading(testBtn, false);
    }
}

// Backup Functions
async function loadBackups() {
    try {
        const response = await fetch(`${CONFIG_API}/backups`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        const container = document.getElementById('backups-list');
        
        if (!data.backups || data.backups.length === 0) {
            container.innerHTML = '<div class="empty-state">No backups available</div>';
            return;
        }
        
        container.innerHTML = data.backups.map(backup => `
            <div class="backup-item">
                <div class="backup-item-info">
                    <div class="backup-item-name">${backup}</div>
                    <div class="backup-item-date">${formatBackupDate(backup)}</div>
                </div>
                <div class="backup-item-actions">
                    <button class="btn btn-small btn-secondary" onclick="restoreBackup('${backup}')">Restore</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading backups:', error);
        document.getElementById('backups-list').innerHTML = '<div class="empty-state">Error loading backups</div>';
    }
}

function formatBackupDate(filename) {
    const match = filename.match(/application_(\d{8})_(\d{6})\.yml/);
    if (match) {
        const date = match[1];
        const time = match[2];
        return `${date.slice(0,4)}-${date.slice(4,6)}-${date.slice(6,8)} ${time.slice(0,2)}:${time.slice(2,4)}:${time.slice(4,6)}`;
    }
    return filename;
}

async function createBackup() {
    const btn = document.getElementById('createBackupBtn');
    setButtonLoading(btn, true);
    
    try {
        const response = await fetch(`${CONFIG_API}/backup`, { method: 'POST' });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message, 'success');
            await loadBackups();
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        console.error('Error creating backup:', error);
        showMessage('Failed to create backup: ' + error.message, 'error');
    } finally {
        setButtonLoading(btn, false);
    }
}

async function restoreBackup(filename) {
    if (!confirm(`Are you sure you want to restore configuration from backup "${filename}"? This will replace your current configuration.`)) {
        return;
    }
    
    try {
        const response = await fetch(`${CONFIG_API}/restore?backupFile=${encodeURIComponent(filename)}`, {
            method: 'POST'
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message, 'success');
            await loadConfiguration();
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        console.error('Error restoring backup:', error);
        showMessage('Failed to restore backup: ' + error.message, 'error');
    }
}

async function exportConfiguration() {
    try {
        const response = await fetch(`${CONFIG_API}/export`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const yamlContent = await response.text();
        
        // Create download
        const blob = new Blob([yamlContent], { type: 'application/x-yaml' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'application.yml';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        showMessage('Configuration exported successfully', 'success');
    } catch (error) {
        console.error('Error exporting configuration:', error);
        showMessage('Failed to export configuration: ' + error.message, 'error');
    }
}

async function importConfiguration(file) {
    try {
        const yamlContent = await file.text();
        
        const response = await fetch(`${CONFIG_API}/import`, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: yamlContent
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message, 'success');
            await loadConfiguration();
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        console.error('Error importing configuration:', error);
        showMessage('Failed to import configuration: ' + error.message, 'error');
    }
}

async function resetConfiguration() {
    if (!confirm('Are you sure you want to reset configuration to defaults? This will replace your current configuration with the template.')) {
        return;
    }
    
    const btn = document.getElementById('resetConfigBtn');
    setButtonLoading(btn, true);
    
    try {
        const response = await fetch(`${CONFIG_API}/reset`, { method: 'POST' });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message, 'success');
            await loadConfiguration();
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        console.error('Error resetting configuration:', error);
        showMessage('Failed to reset configuration: ' + error.message, 'error');
    } finally {
        setButtonLoading(btn, false);
    }
}

// Event Listeners
function initEventListeners() {
    // Save button
    document.getElementById('saveConfigBtn').addEventListener('click', saveConfiguration);
    
    // Test all connections
    document.getElementById('testAllBtn').addEventListener('click', testAllConnections);
    
    // Individual test buttons
    document.querySelectorAll('.btn-test').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const service = e.target.dataset.service;
            testConnection(service);
        });
    });
    
    // Dynamic list buttons
    document.getElementById('add-movie-expiration').addEventListener('click', () => {
        addExpirationRule('movie');
        markUnsavedChanges();
    });
    
    document.getElementById('add-season-expiration').addEventListener('click', () => {
        addExpirationRule('season');
        markUnsavedChanges();
    });
    
    document.getElementById('add-tag-schedule').addEventListener('click', () => {
        addTagSchedule();
        markUnsavedChanges();
    });
    
    document.getElementById('add-exclusion-tag').addEventListener('click', () => {
        addExclusionTag();
        markUnsavedChanges();
    });
    
    // Backup buttons
    document.getElementById('createBackupBtn').addEventListener('click', createBackup);
    document.getElementById('exportConfigBtn').addEventListener('click', exportConfiguration);
    document.getElementById('resetConfigBtn').addEventListener('click', resetConfiguration);
    
    document.getElementById('importConfigBtn').addEventListener('click', () => {
        document.getElementById('importFileInput').click();
    });
    
    document.getElementById('importFileInput').addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            importConfiguration(file);
        }
    });
    
    // Track changes on all inputs
    document.querySelectorAll('input, select, textarea').forEach(element => {
        element.addEventListener('change', markUnsavedChanges);
    });
    
    // Warn on page leave with unsaved changes
    window.addEventListener('beforeunload', (e) => {
        if (hasUnsavedChanges) {
            e.preventDefault();
            e.returnValue = '';
        }
    });
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initEventListeners();
    loadConfiguration();
    loadBackups();
});
