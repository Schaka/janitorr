# Rule Engine - Visual Rule Builder

The Rule Engine provides a powerful visual interface for creating custom cleanup rules without programming. Build complex cleanup logic using drag-and-drop blocks for conditions and actions.

## 📖 Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Using the Rule Builder](#using-the-rule-builder)
- [Condition Types](#condition-types)
- [Action Types](#action-types)
- [Rule Examples](#rule-examples)
- [API Reference](#api-reference)
- [Troubleshooting](#troubleshooting)

## Overview

The Rule Engine extends Janitorr's cleanup capabilities by allowing you to create custom rules with:

- **Visual Drag-and-Drop Interface** - No coding required
- **Flexible Conditions** - Media age, size, rating, disk usage, tags, and more
- **Powerful Actions** - Delete, tag, log, notify, and more
- **Logic Operators** - Combine conditions with AND/OR logic
- **Preview Mode** - Test rules before executing them
- **Dry-Run Support** - Simulate rule execution safely

## Quick Start

### 1. Enable the Rule Engine

Add to your `application.yml`:

```yaml
rule-engine:
  enabled: true
  rules-directory: "/config/rules"
```

### 2. Access the Rule Builder

Navigate to the Management UI and click **🧩 Rule Builder** or go directly to:

```
http://your-janitorr-host:8978/rule-builder.html
```

### 3. Create Your First Rule

1. **Name your rule** in the top input field
2. **Drag conditions** from the left toolbox to the IF area
3. **Configure each condition** with your desired values
4. **Drag actions** to the THEN area
5. **Click Save** to persist your rule
6. **Click Preview** to see which media would match
7. **Click Test** to run in dry-run mode

## Configuration

### Basic Configuration

```yaml
rule-engine:
  enabled: false # Set to true to enable the rule engine
  rules-directory: "/config/rules" # Where rules are stored
  max-rules-per-execution: 100 # Limit concurrent rule execution
  enable-scheduled-rules: false # Enable automatic rule execution
```

### Docker Environment Variables

```bash
RULE_ENGINE_ENABLED=true
RULE_ENGINE_RULES_DIRECTORY=/config/rules
```

### Volume Mounts

Ensure your rules directory is persisted:

```yaml
volumes:
  - ./config/rules:/config/rules
```

## Using the Rule Builder

### Interface Layout

The Rule Builder has three main areas:

1. **Toolbox (Left)** - Available conditions and actions
2. **Canvas (Center)** - Build your rule with IF/THEN blocks
3. **Rules List (Right)** - Saved rules library

### Building a Rule

#### Step 1: Name Your Rule

Give your rule a descriptive name that explains what it does.

```
Example: "Delete old low-rated movies when disk is full"
```

#### Step 2: Choose Logic Operator

- **AND** - All conditions must be true (default)
- **OR** - Any condition can be true

#### Step 3: Add Conditions

Drag condition blocks from the toolbox to the IF area:

- **📅 Media Age** - Filter by how old media is
- **💾 File Size** - Filter by file size in GB
- **⭐ Rating** - Filter by rating score
- **📊 Disk Usage** - Filter based on disk space
- **▶️ Play Count** - Filter by watch history
- **🏷️ Tag** - Filter by Sonarr/Radarr tags

#### Step 4: Configure Conditions

Click on each condition to set:
- **Operator** - greater than, less than, equals, etc.
- **Value** - The threshold for comparison

#### Step 5: Add Actions

Drag action blocks to the THEN area:

- **🗑️ Delete File** - Remove media
- **📝 Log Action** - Log information
- **➕ Add Tag** - Tag media in *arr services
- **🔔 Notify** - Send notifications

#### Step 6: Save and Test

- **Validate** - Check if rule is correctly configured
- **Preview** - See which media items match
- **Test** - Execute in dry-run mode
- **Save** - Persist the rule for later use

## Condition Types

### Media Age

Filter media based on time since download or last watch.

**Configuration:**
- Operator: `older than`, `newer than`, `exactly`
- Value: Number of days

**Example:** "older than 90 days"

### File Size

Filter media based on file size.

**Configuration:**
- Operator: `larger than`, `smaller than`, `exactly`
- Value: Size in GB

**Example:** "larger than 10 GB"

### Rating

Filter based on media rating (when available).

**Configuration:**
- Operator: `below`, `above`, `equals`
- Value: Rating from 0-10

**Example:** "below 6.0"

### Disk Usage

Filter based on current disk space usage.

**Configuration:**
- Operator: `above`, `below`
- Value: Percentage (0-100)

**Example:** "above 85%"

### Play Count

Filter based on how many times media has been watched.

**Configuration:**
- Operator: `equals`, `less than`, `more than`
- Value: Number of plays

**Example:** "equals 0 plays" (never watched)

### Tag

Filter based on tags in Sonarr/Radarr.

**Configuration:**
- Operator: `has tag`, `does not have tag`
- Value: Tag name

**Example:** "has tag 'janitorr_keep'"

## Action Types

### Delete File

Removes the media file and optionally from media server.

**Options:**
- Remove from media server: Yes/No

### Log Action

Logs information about the rule execution.

**Configuration:**
- Level: INFO, WARN, DEBUG, ERROR
- Message: Custom log message

### Add Tag

Adds a tag to the media in Sonarr/Radarr.

**Configuration:**
- Tag name: The tag to add

### Notify

Sends a notification (requires Discord webhook setup).

**Configuration:**
- Message: Notification text

## Rule Examples

### Example 1: Clean Up Old Unwatched Movies

**Goal:** Delete movies older than 90 days that have never been watched when disk is over 80% full.

**Conditions:**
- Media Age: older than 90 days
- Play Count: equals 0 plays
- Disk Usage: above 80%
- Logic: AND

**Actions:**
- Delete File
- Log Action: "Deleted unwatched movie"

### Example 2: Tag Low-Rated Content

**Goal:** Tag media with rating below 6.0 for review.

**Conditions:**
- Rating: below 6.0
- Logic: AND

**Actions:**
- Add Tag: "low_rating"
- Log Action: "Tagged low-rated media"

### Example 3: Archive Large 4K Files

**Goal:** Mark very large files for manual review.

**Conditions:**
- File Size: larger than 50 GB
- Logic: AND

**Actions:**
- Add Tag: "review_size"
- Notify: "Large file detected"

## API Reference

The Rule Engine provides REST API endpoints for programmatic access.

### Get All Rules

```http
GET /api/rules
```

**Response:**
```json
[
  {
    "id": "rule_123",
    "name": "Delete old movies",
    "enabled": true,
    "conditions": [...],
    "actions": [...],
    "logicOperator": "AND"
  }
]
```

### Create Rule

```http
POST /api/rules
Content-Type: application/json

{
  "id": "rule_123",
  "name": "My Custom Rule",
  "enabled": true,
  "conditions": [...],
  "actions": [...],
  "logicOperator": "AND"
}
```

### Preview Rule

```http
POST /api/rules/{id}/preview?type=movies
```

Returns list of media items that match the rule.

### Execute Rule

```http
POST /api/rules/{id}/execute?dryRun=true
```

Executes the rule. Set `dryRun=false` to perform actual deletions.

### Validate Rule

```http
POST /api/rules/{id}/validate
```

Checks if the rule configuration is valid.

## Troubleshooting

### Rule Builder Not Loading

**Issue:** 404 error when accessing rule builder

**Solutions:**
1. Check that Management UI is enabled
2. Verify rule engine is enabled in configuration
3. Ensure `leyden` profile is not active at runtime

### Rules Not Executing

**Issue:** Rules don't run or have no effect

**Solutions:**
1. Check rule is enabled (toggle in rule list)
2. Verify conditions are correctly configured
3. Test with Preview mode first
4. Check logs for error messages
5. Ensure dry-run mode is disabled for actual execution

### Rules Directory Not Found

**Issue:** Error saving rules

**Solutions:**
1. Verify rules directory exists: `/config/rules`
2. Check volume mount in docker-compose
3. Ensure directory has write permissions

### No Media Matching Rules

**Issue:** Preview shows 0 matches

**Solutions:**
1. Check condition values are realistic
2. Verify logic operator (AND vs OR)
3. Test with simpler conditions first
4. Check media exists in Sonarr/Radarr

## Best Practices

1. **Start with dry-run** - Always test rules in dry-run mode first
2. **Use Preview** - Check which media matches before executing
3. **Validate rules** - Use the validate button before saving
4. **Descriptive names** - Use clear names for easy identification
5. **Simple first** - Start with simple rules and add complexity gradually
6. **Backup rules** - Export rules directory periodically
7. **Tag instead of delete** - Consider tagging for manual review before deleting

## See Also

- [Configuration Guide](Configuration-Guide.md)
- [Troubleshooting](Troubleshooting.md)
- [FAQ](FAQ.md)
