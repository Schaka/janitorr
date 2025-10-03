// Rule Builder JavaScript

let currentRule = {
    id: generateId(),
    name: '',
    enabled: true,
    conditions: [],
    actions: [],
    logicOperator: 'AND'
};

let savedRules = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    initializeDragAndDrop();
    initializeEventListeners();
    loadSavedRules();
});

function generateId() {
    return 'rule_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

function initializeDragAndDrop() {
    // Make condition and action blocks draggable
    const blocks = document.querySelectorAll('.block-item');
    blocks.forEach(block => {
        block.addEventListener('dragstart', handleDragStart);
        block.addEventListener('dragend', handleDragEnd);
    });

    // Make drop areas accept drops
    const dropAreas = document.querySelectorAll('.drop-area');
    dropAreas.forEach(area => {
        area.addEventListener('dragover', handleDragOver);
        area.addEventListener('drop', handleDrop);
        area.addEventListener('dragleave', handleDragLeave);
    });
}

let draggedItem = null;

function handleDragStart(e) {
    draggedItem = this;
    e.dataTransfer.effectAllowed = 'copy';
    e.dataTransfer.setData('text/html', this.innerHTML);
    e.dataTransfer.setData('block-type', this.getAttribute('data-type'));
    this.style.opacity = '0.5';
}

function handleDragEnd(e) {
    this.style.opacity = '1';
}

function handleDragOver(e) {
    if (e.preventDefault) {
        e.preventDefault();
    }
    e.dataTransfer.dropEffect = 'copy';
    this.classList.add('drag-over');
    return false;
}

function handleDragLeave(e) {
    this.classList.remove('drag-over');
}

function handleDrop(e) {
    if (e.stopPropagation) {
        e.stopPropagation();
    }
    e.preventDefault();
    
    this.classList.remove('drag-over');
    
    const blockType = e.dataTransfer.getData('block-type');
    const isCondition = this.id === 'conditionsArea';
    
    if (blockType) {
        addDroppedBlock(this, blockType, isCondition);
    }
    
    return false;
}

function addDroppedBlock(container, blockType, isCondition) {
    // Remove placeholder if exists
    const placeholder = container.querySelector('.placeholder');
    if (placeholder) {
        placeholder.remove();
    }

    const itemDiv = document.createElement('div');
    itemDiv.className = 'dropped-item';
    itemDiv.setAttribute('data-block-type', blockType);
    
    if (isCondition) {
        itemDiv.innerHTML = createConditionHtml(blockType);
    } else {
        itemDiv.innerHTML = createActionHtml(blockType);
    }
    
    container.appendChild(itemDiv);
    
    // Add event listener to remove button
    const removeBtn = itemDiv.querySelector('.remove-btn');
    removeBtn.addEventListener('click', function() {
        itemDiv.remove();
        if (container.children.length === 0) {
            container.innerHTML = '<p class="placeholder">Drag ' + (isCondition ? 'conditions' : 'actions') + ' here</p>';
        }
    });
}

function createConditionHtml(type) {
    const configs = {
        'age': {
            name: '📅 Media Age',
            fields: `
                <select class="condition-operator">
                    <option value="GREATER_THAN">older than</option>
                    <option value="LESS_THAN">newer than</option>
                    <option value="EQUALS">exactly</option>
                </select>
                <input type="number" class="condition-value" placeholder="Days" value="30" min="0" />
                <span>days</span>
            `
        },
        'size': {
            name: '💾 File Size',
            fields: `
                <select class="condition-operator">
                    <option value="GREATER_THAN">larger than</option>
                    <option value="LESS_THAN">smaller than</option>
                    <option value="EQUALS">exactly</option>
                </select>
                <input type="number" class="condition-value" placeholder="Size" value="5" min="0" step="0.1" />
                <span>GB</span>
            `
        },
        'rating': {
            name: '⭐ Rating',
            fields: `
                <select class="condition-operator">
                    <option value="LESS_THAN">below</option>
                    <option value="GREATER_THAN">above</option>
                    <option value="EQUALS">equals</option>
                </select>
                <input type="number" class="condition-value" placeholder="Rating" value="6.0" min="0" max="10" step="0.1" />
            `
        },
        'disk-usage': {
            name: '📊 Disk Usage',
            fields: `
                <select class="condition-operator">
                    <option value="GREATER_THAN">above</option>
                    <option value="LESS_THAN">below</option>
                </select>
                <input type="number" class="condition-value" placeholder="Percentage" value="80" min="0" max="100" />
                <span>%</span>
            `
        },
        'plays': {
            name: '▶️ Play Count',
            fields: `
                <select class="condition-operator">
                    <option value="EQUALS">equals</option>
                    <option value="LESS_THAN">less than</option>
                    <option value="GREATER_THAN">more than</option>
                </select>
                <input type="number" class="condition-value" placeholder="Plays" value="0" min="0" />
                <span>plays</span>
            `
        },
        'tag': {
            name: '🏷️ Tag',
            fields: `
                <select class="condition-operator">
                    <option value="CONTAINS">has tag</option>
                    <option value="NOT_CONTAINS">does not have tag</option>
                </select>
                <input type="text" class="condition-value" placeholder="Tag name" value="janitorr_keep" />
            `
        }
    };
    
    const config = configs[type];
    return `
        <div class="dropped-item-config">
            <div class="dropped-item-header">${config.name}</div>
            <div class="config-row">
                ${config.fields}
            </div>
        </div>
        <button class="remove-btn">Remove</button>
    `;
}

function createActionHtml(type) {
    const configs = {
        'delete': {
            name: '🗑️ Delete File',
            fields: `
                <label>
                    <input type="checkbox" class="action-option" checked />
                    Remove from media server
                </label>
            `
        },
        'log': {
            name: '📝 Log Action',
            fields: `
                <select class="action-level">
                    <option value="INFO">INFO</option>
                    <option value="WARN">WARN</option>
                    <option value="DEBUG">DEBUG</option>
                    <option value="ERROR">ERROR</option>
                </select>
                <input type="text" class="action-message" placeholder="Log message" value="Rule executed" />
            `
        },
        'tag-add': {
            name: '➕ Add Tag',
            fields: `
                <input type="text" class="action-tag" placeholder="Tag name" value="marked_for_review" />
            `
        },
        'notify': {
            name: '🔔 Notify',
            fields: `
                <input type="text" class="action-message" placeholder="Notification message" value="Media processed" />
            `
        }
    };
    
    const config = configs[type];
    return `
        <div class="dropped-item-config">
            <div class="dropped-item-header">${config.name}</div>
            <div class="config-row">
                ${config.fields}
            </div>
        </div>
        <button class="remove-btn">Remove</button>
    `;
}

function initializeEventListeners() {
    document.getElementById('ruleName').addEventListener('input', function(e) {
        currentRule.name = e.target.value;
    });

    document.getElementById('logicOperator').addEventListener('change', function(e) {
        currentRule.logicOperator = e.target.value;
    });

    document.getElementById('ruleEnabled').addEventListener('change', function(e) {
        currentRule.enabled = e.target.checked;
    });

    document.getElementById('newRuleBtn').addEventListener('click', newRule);
    document.getElementById('validateBtn').addEventListener('click', validateRule);
    document.getElementById('previewBtn').addEventListener('click', previewRule);
    document.getElementById('saveBtn').addEventListener('click', saveRule);
    document.getElementById('testBtn').addEventListener('click', testRule);

    // Modal close
    document.querySelector('.modal-close').addEventListener('click', function() {
        document.getElementById('previewModal').classList.remove('active');
    });
}

function newRule() {
    if (confirm('Create a new rule? Unsaved changes will be lost.')) {
        currentRule = {
            id: generateId(),
            name: '',
            enabled: true,
            conditions: [],
            actions: [],
            logicOperator: 'AND'
        };
        
        document.getElementById('ruleName').value = '';
        document.getElementById('logicOperator').value = 'AND';
        document.getElementById('ruleEnabled').checked = true;
        document.getElementById('conditionsArea').innerHTML = '<p class="placeholder">Drag conditions here</p>';
        document.getElementById('actionsArea').innerHTML = '<p class="placeholder">Drag actions here</p>';
        
        showMessage('New rule created', 'success');
    }
}

function buildRuleFromUI() {
    const rule = {
        ...currentRule,
        name: document.getElementById('ruleName').value,
        enabled: document.getElementById('ruleEnabled').checked,
        logicOperator: document.getElementById('logicOperator').value,
        conditions: [],
        actions: []
    };

    // Build conditions
    const conditionItems = document.getElementById('conditionsArea').querySelectorAll('.dropped-item');
    conditionItems.forEach(item => {
        const blockType = item.getAttribute('data-block-type');
        const operator = item.querySelector('.condition-operator')?.value;
        const value = item.querySelector('.condition-value')?.value;

        if (blockType && operator && value) {
            const condition = createConditionObject(blockType, operator, value);
            if (condition) {
                rule.conditions.push(condition);
            }
        }
    });

    // Build actions
    const actionItems = document.getElementById('actionsArea').querySelectorAll('.dropped-item');
    actionItems.forEach(item => {
        const blockType = item.getAttribute('data-block-type');
        const action = createActionObject(blockType, item);
        if (action) {
            rule.actions.push(action);
        }
    });

    return rule;
}

function createConditionObject(type, operator, value) {
    const baseCondition = { operator };
    
    switch (type) {
        case 'age':
            return { ...baseCondition, type: 'AGE', days: parseInt(value) };
        case 'size':
            return { ...baseCondition, type: 'SIZE', sizeInGB: parseFloat(value) };
        case 'rating':
            return { ...baseCondition, type: 'RATING', rating: parseFloat(value) };
        case 'disk-usage':
            return { ...baseCondition, type: 'DISK_USAGE', percentage: parseFloat(value) };
        case 'plays':
            return { ...baseCondition, type: 'PLAYS', plays: parseInt(value) };
        case 'tag':
            return { ...baseCondition, type: 'TAG', tag: value };
        default:
            return null;
    }
}

function createActionObject(type, item) {
    switch (type) {
        case 'delete':
            const removeFromServer = item.querySelector('.action-option')?.checked ?? true;
            return { type: 'DELETE_FILE', removeFromMediaServer: removeFromServer };
        case 'log':
            const level = item.querySelector('.action-level')?.value || 'INFO';
            const logMessage = item.querySelector('.action-message')?.value || 'Rule executed';
            return { type: 'LOG_ACTION', level, message: logMessage };
        case 'tag-add':
            const tag = item.querySelector('.action-tag')?.value;
            return tag ? { type: 'ADD_TAG', tag } : null;
        case 'notify':
            const message = item.querySelector('.action-message')?.value || 'Notification';
            return { type: 'NOTIFY_DISCORD', message };
        default:
            return null;
    }
}

async function validateRule() {
    const rule = buildRuleFromUI();
    
    if (!rule.name) {
        showMessage('Please enter a rule name', 'error');
        return;
    }

    if (rule.conditions.length === 0) {
        showMessage('Please add at least one condition', 'error');
        return;
    }

    if (rule.actions.length === 0) {
        showMessage('Please add at least one action', 'error');
        return;
    }

    showMessage('Rule is valid!', 'success');
}

async function previewRule() {
    const rule = buildRuleFromUI();
    
    try {
        showMessage('Loading preview...', 'info');
        
        // Simulate API call
        // In real implementation, this would call /api/rules/{id}/preview
        const previewResults = document.getElementById('previewResults');
        previewResults.innerHTML = `
            <div style="text-align: center; padding: 2rem;">
                <h3>Preview Results</h3>
                <p>Rule: <strong>${rule.name || 'Unnamed Rule'}</strong></p>
                <p>Conditions: ${rule.conditions.length}</p>
                <p>Actions: ${rule.actions.length}</p>
                <p style="color: #6b7280; margin-top: 1rem;">
                    Preview functionality requires the rule engine to be enabled and media to be loaded.
                </p>
            </div>
        `;
        
        document.getElementById('previewModal').classList.add('active');
    } catch (error) {
        showMessage('Error loading preview: ' + error.message, 'error');
    }
}

async function saveRule() {
    const rule = buildRuleFromUI();
    
    if (!rule.name) {
        showMessage('Please enter a rule name', 'error');
        return;
    }

    if (rule.conditions.length === 0) {
        showMessage('Please add at least one condition', 'error');
        return;
    }

    if (rule.actions.length === 0) {
        showMessage('Please add at least one action', 'error');
        return;
    }

    try {
        showMessage('Saving rule...', 'info');
        
        // Simulate saving - in real implementation, call /api/rules
        savedRules.push(rule);
        localStorage.setItem('janitorr_rules', JSON.stringify(savedRules));
        
        renderRulesList();
        showMessage('Rule saved successfully!', 'success');
    } catch (error) {
        showMessage('Error saving rule: ' + error.message, 'error');
    }
}

async function testRule() {
    const rule = buildRuleFromUI();
    
    if (!rule.name) {
        showMessage('Please enter a rule name', 'error');
        return;
    }

    try {
        showMessage('Testing rule in dry-run mode...', 'info');
        
        // Simulate test - in real implementation, call /api/rules/{id}/execute?dryRun=true
        setTimeout(() => {
            showMessage('Rule test completed (dry-run mode). Check logs for details.', 'success');
        }, 1000);
    } catch (error) {
        showMessage('Error testing rule: ' + error.message, 'error');
    }
}

function loadSavedRules() {
    try {
        const stored = localStorage.getItem('janitorr_rules');
        if (stored) {
            savedRules = JSON.parse(stored);
            renderRulesList();
        }
    } catch (error) {
        console.error('Error loading saved rules:', error);
    }
}

function renderRulesList() {
    const rulesList = document.getElementById('rulesList');
    
    if (savedRules.length === 0) {
        rulesList.innerHTML = '<p class="placeholder">No rules created yet</p>';
        return;
    }

    rulesList.innerHTML = savedRules.map(rule => `
        <div class="rule-item" data-rule-id="${rule.id}">
            <div class="rule-item-name">${rule.name}</div>
            <div class="rule-item-meta">
                <span>${rule.conditions.length} conditions, ${rule.actions.length} actions</span>
                <span class="rule-item-status ${rule.enabled ? '' : 'disabled'}"></span>
            </div>
        </div>
    `).join('');

    // Add click handlers
    rulesList.querySelectorAll('.rule-item').forEach(item => {
        item.addEventListener('click', function() {
            const ruleId = this.getAttribute('data-rule-id');
            loadRule(ruleId);
        });
    });
}

function loadRule(ruleId) {
    const rule = savedRules.find(r => r.id === ruleId);
    if (!rule) return;

    // Populate UI with rule data
    currentRule = { ...rule };
    document.getElementById('ruleName').value = rule.name;
    document.getElementById('logicOperator').value = rule.logicOperator;
    document.getElementById('ruleEnabled').checked = rule.enabled;

    showMessage(`Loaded rule: ${rule.name}`, 'info');
}

function showMessage(message, type = 'info') {
    const messageContainer = document.getElementById('messageContainer');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${type}`;
    messageDiv.textContent = message;
    
    messageContainer.innerHTML = '';
    messageContainer.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}
