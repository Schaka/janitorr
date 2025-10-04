# Notifications Guide

Janitorr supports multi-channel notifications to keep you informed about cleanup operations, errors, and system status.

## Table of Contents

- [Overview](#overview)
- [Supported Channels](#supported-channels)
- [Configuration](#configuration)
- [Testing Notifications](#testing-notifications)
- [Notification Events](#notification-events)
- [Troubleshooting](#troubleshooting)

## Overview

The notification system allows you to receive alerts about:
- Cleanup operations completed
- Files deleted and space freed
- Errors during cleanup
- Dry-run mode indicators
- Future: Daily/weekly reports, disk space warnings

**All notification channels are disabled by default** for safety. You must explicitly enable and configure the channels you want to use.

## Supported Channels

### 1. Discord Webhook

Sends rich embeds to Discord channels with color-coded events and cleanup statistics.

**Features:**
- Color-coded embeds (green for success, red for errors, orange for warnings)
- Detailed statistics in embed fields
- Customizable bot username and avatar
- Timestamp on all notifications

**Setup:**
1. Create a webhook in your Discord server:
   - Server Settings → Integrations → Webhooks → New Webhook
   - Copy the webhook URL
2. Configure in `application.yml`:
   ```yaml
   notifications:
     enabled: true
     discord:
       enabled: true
       webhook-url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL"
       username: "Janitorr"  # Optional: Custom bot name
       avatar-url: ""        # Optional: Custom bot avatar URL
   ```

### 2. Telegram Bot

Sends HTML-formatted messages to Telegram chats with emoji indicators.

**Features:**
- HTML formatted messages with bold/italic text
- Emoji indicators for different event types
- Real-time push notifications
- Supports both private chats and groups

**Setup:**
1. Create a bot with [@BotFather](https://t.me/botfather):
   - Send `/newbot` and follow the prompts
   - Save the bot token
2. Get your chat ID:
   - Start a chat with your bot
   - Send a message to the bot
   - Visit: `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates`
   - Find `"chat":{"id":YOUR_CHAT_ID}` in the response
3. Configure in `application.yml`:
   ```yaml
   notifications:
     enabled: true
     telegram:
       enabled: true
       bot-token: "YOUR_BOT_TOKEN"
       chat-id: "YOUR_CHAT_ID"
   ```

### 3. Email (SMTP)

Sends professional HTML-formatted email reports.

**Features:**
- Professional HTML email templates
- Responsive design
- Support for multiple recipients
- TLS/SSL encryption
- Detailed cleanup reports

**Setup:**
Configure your SMTP settings in `application.yml`:

**Gmail Example:**
```yaml
notifications:
  enabled: true
  email:
    enabled: true
    host: "smtp.gmail.com"
    port: 587
    username: "your-email@gmail.com"
    password: "your-app-password"  # Use App Password, not your regular password
    from: "janitorr@yourdomain.com"
    to:
      - "recipient1@example.com"
      - "recipient2@example.com"
    use-tls: true
```

**Other SMTP Providers:**
- **Outlook/Office 365:** `smtp.office365.com:587`
- **Yahoo:** `smtp.mail.yahoo.com:587`
- **Custom SMTP:** Use your provider's settings

**Note:** For Gmail, you need to create an [App Password](https://support.google.com/accounts/answer/185833).

### 4. Generic Webhook

Sends JSON payloads to any HTTP endpoint with retry logic.

**Features:**
- Flexible JSON payload format
- Custom HTTP headers (for authentication)
- Configurable HTTP method (POST/PUT)
- Retry logic with exponential backoff (3 retries by default)
- Works with Slack, Microsoft Teams, and custom endpoints

**Setup:**
```yaml
notifications:
  enabled: true
  webhook:
    enabled: true
    url: "https://your-webhook-endpoint.com/notify"
    method: "POST"  # or "PUT"
    headers:
      Authorization: "Bearer YOUR_TOKEN"
      X-Custom-Header: "value"
    retry-count: 3
```

**Payload Format:**
```json
{
  "event_type": "CLEANUP_COMPLETED",
  "title": "Cleanup Completed: MEDIA",
  "message": "✅ Deleted 5 file(s), freed 10.50 GB",
  "details": {
    "Files Deleted": 5,
    "Space Freed (GB)": 10.5,
    "Dry Run": false,
    "Errors": 0
  },
  "timestamp": "2025-10-03T15:30:00"
}
```

**Slack Example:**
For Slack, you may want to use the Discord channel instead, as it provides better formatting with Slack's webhook compatibility.

### 5. Web Push (Experimental)

Browser-based push notifications (placeholder for future implementation).

**Status:** Not yet fully implemented. Requires:
- VAPID key pair generation
- Browser subscription management
- Service worker integration
- Push notification API implementation

## Configuration

### Minimal Configuration

Enable notifications and at least one channel:

```yaml
notifications:
  enabled: true  # Master switch - must be true
  discord:
    enabled: true
    webhook-url: "YOUR_DISCORD_WEBHOOK_URL"
```

### Multiple Channels

You can enable multiple channels simultaneously:

```yaml
notifications:
  enabled: true
  
  discord:
    enabled: true
    webhook-url: "YOUR_DISCORD_WEBHOOK_URL"
  
  telegram:
    enabled: true
    bot-token: "YOUR_BOT_TOKEN"
    chat-id: "YOUR_CHAT_ID"
  
  email:
    enabled: true
    host: "smtp.gmail.com"
    port: 587
    username: "your-email@gmail.com"
    password: "your-app-password"
    from: "janitorr@yourdomain.com"
    to:
      - "admin@example.com"
```

### Environment Variables

You can use environment variables for sensitive values:

```yaml
notifications:
  enabled: true
  discord:
    enabled: true
    webhook-url: "${DISCORD_WEBHOOK_URL}"
  telegram:
    enabled: true
    bot-token: "${TELEGRAM_BOT_TOKEN}"
    chat-id: "${TELEGRAM_CHAT_ID}"
```

Then set in your Docker Compose:
```yaml
environment:
  - DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...
  - TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
  - TELEGRAM_CHAT_ID=123456789
```

## Testing Notifications

### Via Management UI

1. Open the Management UI: `http://your-server:8978/`
2. Scroll to the "Notification Testing" section
3. Click the test button for your configured channel
4. Check your channel for the test notification

### Via API

Test a specific channel:
```bash
curl -X POST http://localhost:8978/api/management/notifications/test/discord
```

Available channels: `discord`, `telegram`, `email`, `webhook`

**Expected Response:**
```json
{
  "success": true,
  "message": "Test notification sent successfully to discord",
  "timestamp": 1696348800000
}
```

## Notification Events

### Current Events

**CLEANUP_COMPLETED**
- Triggered: After each cleanup operation completes
- Content:
  - Cleanup type (MEDIA, TAG, EPISODE)
  - Files deleted count
  - Space freed (GB)
  - Dry-run mode indicator
  - Error count

**CLEANUP_ERROR**
- Triggered: When a cleanup operation fails
- Content:
  - Error message
  - Context (which cleanup failed)
  - Timestamp

### Future Events

These event types are defined but not yet implemented:
- `SYSTEM_STATUS_CHANGE` - Service connection status changes
- `DISK_SPACE_WARNING` - Low disk space alerts
- `DAILY_REPORT` - Daily cleanup summary
- `WEEKLY_REPORT` - Weekly cleanup summary

## Troubleshooting

### No Notifications Received

1. **Check master switch:**
   ```yaml
   notifications:
     enabled: true  # Must be true
   ```

2. **Check channel is enabled:**
   ```yaml
   discord:
     enabled: true  # Channel must be enabled
   ```

3. **Test the channel:**
   - Use the Management UI test button
   - Check application logs for errors

4. **Verify configuration:**
   - Discord: Webhook URL is correct and channel exists
   - Telegram: Bot token and chat ID are correct
   - Email: SMTP credentials are correct
   - Webhook: Endpoint is accessible

### Discord Notifications Not Working

- **404 Error:** Webhook URL is invalid or was deleted
- **401 Error:** Webhook URL is incorrect
- **429 Error:** Rate limited - too many requests
- **Check:** Ensure the webhook still exists in Discord

### Telegram Notifications Not Working

- **401 Unauthorized:** Bot token is incorrect
- **400 Bad Request:** Chat ID is incorrect
- **403 Forbidden:** Bot was blocked by user or kicked from group
- **Get Updates:** Visit `https://api.telegram.org/bot<TOKEN>/getUpdates` to verify

### Email Notifications Not Working

- **Authentication Failed:** Check username and password
  - Gmail: Use [App Password](https://support.google.com/accounts/answer/185833)
  - Outlook: Enable SMTP in account settings
- **Connection Refused:** Check host and port
- **SSL/TLS Errors:** Verify `use-tls` setting matches server requirements
- **Check Spam:** Notifications may be in spam folder

### Webhook Notifications Not Working

- **Connection Timeout:** Webhook endpoint is not accessible
- **SSL Certificate Error:** Endpoint has invalid SSL certificate
- **401/403 Errors:** Check Authorization header
- **Retry Logic:** Webhook will retry 3 times with exponential backoff
- **Check Logs:** Application logs show detailed error messages

### Logs Show "Notifications disabled"

This means the NoOp service is active. Check:
1. `notifications.enabled` is `true`
2. At least one channel is enabled
3. Application was restarted after config changes

### Multiple Channels - Some Work, Some Don't

Each channel is independent:
- Test each channel separately
- Check logs for channel-specific errors
- One broken channel won't affect others

## Security Considerations

### Sensitive Data in Notifications

Notifications include:
- ✅ Cleanup statistics (files deleted, space freed)
- ✅ Cleanup types (media, tag, episode)
- ❌ File names or paths (not included)
- ❌ User information (not included)

### Protecting Credentials

**Never commit credentials to git:**
```yaml
# ❌ DON'T
notifications:
  telegram:
    bot-token: "123456:ABC-DEF..."  # Exposed in git history

# ✅ DO
notifications:
  telegram:
    bot-token: "${TELEGRAM_BOT_TOKEN}"  # From environment variable
```

**Docker Secrets (recommended):**
```yaml
services:
  janitorr:
    secrets:
      - telegram_bot_token
    environment:
      - TELEGRAM_BOT_TOKEN_FILE=/run/secrets/telegram_bot_token
```

### Network Security

- Use HTTPS webhooks when possible
- Keep bot tokens and API keys secure
- Limit notification content if channels are public
- Consider using private Discord channels or Telegram groups

## Examples

### Example 1: Discord Only

```yaml
notifications:
  enabled: true
  discord:
    enabled: true
    webhook-url: "https://discord.com/api/webhooks/123456/abcdef"
    username: "Janitorr Bot"
```

### Example 2: Multiple Channels

```yaml
notifications:
  enabled: true
  discord:
    enabled: true
    webhook-url: "${DISCORD_WEBHOOK_URL}"
  telegram:
    enabled: true
    bot-token: "${TELEGRAM_BOT_TOKEN}"
    chat-id: "${TELEGRAM_CHAT_ID}"
  email:
    enabled: true
    host: "smtp.gmail.com"
    port: 587
    username: "${SMTP_USERNAME}"
    password: "${SMTP_PASSWORD}"
    from: "janitorr@mydomain.com"
    to:
      - "admin@mydomain.com"
```

### Example 3: Slack via Webhook

```yaml
notifications:
  enabled: true
  webhook:
    enabled: true
    url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
    method: "POST"
```

## Related Documentation

- [Configuration Guide](Configuration-Guide.md)
- [Docker Compose Setup](Docker-Compose-Setup.md)
- [Management UI](../../MANAGEMENT_UI.md)
- [Troubleshooting](Troubleshooting.md)
