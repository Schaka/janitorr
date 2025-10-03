# Guía de Notificaciones

Janitorr soporta notificaciones multi-canal para mantenerte informado sobre operaciones de limpieza, errores y estado del sistema.

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Canales Soportados](#canales-soportados)
- [Configuración](#configuración)
- [Probar Notificaciones](#probar-notificaciones)
- [Eventos de Notificación](#eventos-de-notificación)
- [Solución de Problemas](#solución-de-problemas)

## Descripción General

El sistema de notificaciones te permite recibir alertas sobre:
- Operaciones de limpieza completadas
- Archivos eliminados y espacio liberado
- Errores durante la limpieza
- Indicadores de modo dry-run
- Futuro: Reportes diarios/semanales, alertas de espacio en disco

**Todos los canales de notificación están deshabilitados por defecto** por seguridad. Debes habilitar y configurar explícitamente los canales que desees usar.

## Canales Soportados

### 1. Discord Webhook

Envía embeds enriquecidos a canales de Discord con eventos codificados por color y estadísticas de limpieza.

**Características:**
- Embeds codificados por color (verde para éxito, rojo para errores, naranja para advertencias)
- Estadísticas detalladas en campos del embed
- Nombre de usuario y avatar del bot personalizables
- Marca de tiempo en todas las notificaciones

**Configuración:**
1. Crea un webhook en tu servidor de Discord:
   - Configuración del Servidor → Integraciones → Webhooks → Nuevo Webhook
   - Copia la URL del webhook
2. Configura en `application.yml`:
   ```yaml
   notifications:
     enabled: true
     discord:
       enabled: true
       webhook-url: "https://discord.com/api/webhooks/TU_URL_WEBHOOK"
       username: "Janitorr"  # Opcional: Nombre personalizado del bot
       avatar-url: ""        # Opcional: URL de avatar personalizado
   ```

### 2. Telegram Bot

Envía mensajes formateados en HTML a chats de Telegram con indicadores de emoji.

**Características:**
- Mensajes formateados en HTML con texto en negrita/cursiva
- Indicadores emoji para diferentes tipos de eventos
- Notificaciones push en tiempo real
- Soporta chats privados y grupos

**Configuración:**
1. Crea un bot con [@BotFather](https://t.me/botfather):
   - Envía `/newbot` y sigue las instrucciones
   - Guarda el token del bot
2. Obtén tu chat ID:
   - Inicia un chat con tu bot
   - Envía un mensaje al bot
   - Visita: `https://api.telegram.org/bot<TU_TOKEN_BOT>/getUpdates`
   - Encuentra `"chat":{"id":TU_CHAT_ID}` en la respuesta
3. Configura en `application.yml`:
   ```yaml
   notifications:
     enabled: true
     telegram:
       enabled: true
       bot-token: "TU_TOKEN_BOT"
       chat-id: "TU_CHAT_ID"
   ```

### 3. Email (SMTP)

Envía reportes de email formateados en HTML profesional.

**Características:**
- Plantillas de email HTML profesionales
- Diseño responsivo
- Soporte para múltiples destinatarios
- Cifrado TLS/SSL
- Reportes detallados de limpieza

**Configuración:**
Configura tus ajustes SMTP en `application.yml`:

**Ejemplo Gmail:**
```yaml
notifications:
  enabled: true
  email:
    enabled: true
    host: "smtp.gmail.com"
    port: 587
    username: "tu-email@gmail.com"
    password: "tu-contraseña-app"  # Usa Contraseña de Aplicación, no tu contraseña normal
    from: "janitorr@tudominio.com"
    to:
      - "destinatario1@ejemplo.com"
      - "destinatario2@ejemplo.com"
    use-tls: true
```

**Otros Proveedores SMTP:**
- **Outlook/Office 365:** `smtp.office365.com:587`
- **Yahoo:** `smtp.mail.yahoo.com:587`
- **SMTP Personalizado:** Usa la configuración de tu proveedor

**Nota:** Para Gmail, necesitas crear una [Contraseña de Aplicación](https://support.google.com/accounts/answer/185833).

### 4. Webhook Genérico

Envía payloads JSON a cualquier endpoint HTTP con lógica de reintento.

**Características:**
- Formato de payload JSON flexible
- Headers HTTP personalizados (para autenticación)
- Método HTTP configurable (POST/PUT)
- Lógica de reintento con backoff exponencial (3 reintentos por defecto)
- Funciona con Slack, Microsoft Teams y endpoints personalizados

**Configuración:**
```yaml
notifications:
  enabled: true
  webhook:
    enabled: true
    url: "https://tu-endpoint-webhook.com/notify"
    method: "POST"  # o "PUT"
    headers:
      Authorization: "Bearer TU_TOKEN"
      X-Custom-Header: "valor"
    retry-count: 3
```

**Formato del Payload:**
```json
{
  "event_type": "CLEANUP_COMPLETED",
  "title": "Limpieza Completada: MEDIA",
  "message": "✅ Eliminados 5 archivo(s), liberados 10.50 GB",
  "details": {
    "Files Deleted": 5,
    "Space Freed (GB)": 10.5,
    "Dry Run": false,
    "Errors": 0
  },
  "timestamp": "2025-10-03T15:30:00"
}
```

**Ejemplo Slack:**
Para Slack, puede que prefieras usar el canal de Discord en su lugar, ya que proporciona mejor formato con la compatibilidad de webhook de Slack.

### 5. Web Push (Experimental)

Notificaciones push basadas en navegador (marcador de posición para implementación futura).

**Estado:** Aún no está completamente implementado. Requiere:
- Generación de par de claves VAPID
- Gestión de suscripciones del navegador
- Integración de service worker
- Implementación de API de notificaciones push

## Configuración

### Configuración Mínima

Habilita notificaciones y al menos un canal:

```yaml
notifications:
  enabled: true  # Interruptor maestro - debe ser true
  discord:
    enabled: true
    webhook-url: "TU_URL_WEBHOOK_DISCORD"
```

### Múltiples Canales

Puedes habilitar múltiples canales simultáneamente:

```yaml
notifications:
  enabled: true
  
  discord:
    enabled: true
    webhook-url: "TU_URL_WEBHOOK_DISCORD"
  
  telegram:
    enabled: true
    bot-token: "TU_TOKEN_BOT"
    chat-id: "TU_CHAT_ID"
  
  email:
    enabled: true
    host: "smtp.gmail.com"
    port: 587
    username: "tu-email@gmail.com"
    password: "tu-contraseña-app"
    from: "janitorr@tudominio.com"
    to:
      - "admin@ejemplo.com"
```

### Variables de Entorno

Puedes usar variables de entorno para valores sensibles:

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

Luego define en tu Docker Compose:
```yaml
environment:
  - DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...
  - TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
  - TELEGRAM_CHAT_ID=123456789
```

## Probar Notificaciones

### Vía Interfaz de Gestión

1. Abre la Interfaz de Gestión: `http://tu-servidor:8978/`
2. Desplázate a la sección "Notification Testing"
3. Haz clic en el botón de prueba para tu canal configurado
4. Verifica tu canal para la notificación de prueba

### Vía API

Prueba un canal específico:
```bash
curl -X POST http://localhost:8978/api/management/notifications/test/discord
```

Canales disponibles: `discord`, `telegram`, `email`, `webhook`

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Test notification sent successfully to discord",
  "timestamp": 1696348800000
}
```

## Eventos de Notificación

### Eventos Actuales

**CLEANUP_COMPLETED**
- Disparado: Después de que cada operación de limpieza se complete
- Contenido:
  - Tipo de limpieza (MEDIA, TAG, EPISODE)
  - Conteo de archivos eliminados
  - Espacio liberado (GB)
  - Indicador de modo dry-run
  - Conteo de errores

**CLEANUP_ERROR**
- Disparado: Cuando una operación de limpieza falla
- Contenido:
  - Mensaje de error
  - Contexto (qué limpieza falló)
  - Marca de tiempo

### Eventos Futuros

Estos tipos de eventos están definidos pero aún no implementados:
- `SYSTEM_STATUS_CHANGE` - Cambios de estado de conexión del servicio
- `DISK_SPACE_WARNING` - Alertas de espacio bajo en disco
- `DAILY_REPORT` - Resumen diario de limpieza
- `WEEKLY_REPORT` - Resumen semanal de limpieza

## Solución de Problemas

### No se Reciben Notificaciones

1. **Verifica el interruptor maestro:**
   ```yaml
   notifications:
     enabled: true  # Debe ser true
   ```

2. **Verifica que el canal esté habilitado:**
   ```yaml
   discord:
     enabled: true  # El canal debe estar habilitado
   ```

3. **Prueba el canal:**
   - Usa el botón de prueba de la Interfaz de Gestión
   - Revisa los logs de la aplicación en busca de errores

4. **Verifica la configuración:**
   - Discord: La URL del webhook es correcta y el canal existe
   - Telegram: El token del bot y chat ID son correctos
   - Email: Las credenciales SMTP son correctas
   - Webhook: El endpoint es accesible

### Las Notificaciones de Discord No Funcionan

- **Error 404:** La URL del webhook es inválida o fue eliminada
- **Error 401:** La URL del webhook es incorrecta
- **Error 429:** Límite de tasa alcanzado - demasiadas solicitudes
- **Verifica:** Asegúrate de que el webhook aún existe en Discord

### Las Notificaciones de Telegram No Funcionan

- **401 No Autorizado:** El token del bot es incorrecto
- **400 Solicitud Incorrecta:** El chat ID es incorrecto
- **403 Prohibido:** El bot fue bloqueado por el usuario o expulsado del grupo
- **Obtener Actualizaciones:** Visita `https://api.telegram.org/bot<TOKEN>/getUpdates` para verificar

### Las Notificaciones por Email No Funcionan

- **Autenticación Fallida:** Verifica usuario y contraseña
  - Gmail: Usa [Contraseña de Aplicación](https://support.google.com/accounts/answer/185833)
  - Outlook: Habilita SMTP en la configuración de la cuenta
- **Conexión Rechazada:** Verifica host y puerto
- **Errores SSL/TLS:** Verifica que `use-tls` coincida con los requisitos del servidor
- **Revisa Spam:** Las notificaciones pueden estar en la carpeta de spam

### Las Notificaciones por Webhook No Funcionan

- **Tiempo de Conexión Agotado:** El endpoint del webhook no es accesible
- **Error de Certificado SSL:** El endpoint tiene certificado SSL inválido
- **Errores 401/403:** Verifica el header de Authorization
- **Lógica de Reintento:** El webhook reintentará 3 veces con backoff exponencial
- **Revisa Logs:** Los logs de la aplicación muestran mensajes de error detallados

### Los Logs Muestran "Notifications disabled"

Esto significa que el servicio NoOp está activo. Verifica:
1. `notifications.enabled` es `true`
2. Al menos un canal está habilitado
3. La aplicación fue reiniciada después de cambios de configuración

### Múltiples Canales - Algunos Funcionan, Otros No

Cada canal es independiente:
- Prueba cada canal por separado
- Revisa los logs para errores específicos del canal
- Un canal roto no afectará a otros

## Consideraciones de Seguridad

### Datos Sensibles en Notificaciones

Las notificaciones incluyen:
- ✅ Estadísticas de limpieza (archivos eliminados, espacio liberado)
- ✅ Tipos de limpieza (media, tag, episode)
- ❌ Nombres de archivos o rutas (no incluidos)
- ❌ Información de usuario (no incluida)

### Protección de Credenciales

**Nunca confirmes credenciales en git:**
```yaml
# ❌ NO HACER
notifications:
  telegram:
    bot-token: "123456:ABC-DEF..."  # Expuesto en historial de git

# ✅ HACER
notifications:
  telegram:
    bot-token: "${TELEGRAM_BOT_TOKEN}"  # Desde variable de entorno
```

**Docker Secrets (recomendado):**
```yaml
services:
  janitorr:
    secrets:
      - telegram_bot_token
    environment:
      - TELEGRAM_BOT_TOKEN_FILE=/run/secrets/telegram_bot_token
```

### Seguridad de Red

- Usa webhooks HTTPS cuando sea posible
- Mantén seguros los tokens de bot y claves API
- Limita el contenido de notificaciones si los canales son públicos
- Considera usar canales privados de Discord o grupos de Telegram

## Ejemplos

### Ejemplo 1: Solo Discord

```yaml
notifications:
  enabled: true
  discord:
    enabled: true
    webhook-url: "https://discord.com/api/webhooks/123456/abcdef"
    username: "Janitorr Bot"
```

### Ejemplo 2: Múltiples Canales

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
    from: "janitorr@midominio.com"
    to:
      - "admin@midominio.com"
```

### Ejemplo 3: Slack vía Webhook

```yaml
notifications:
  enabled: true
  webhook:
    enabled: true
    url: "https://hooks.slack.com/services/TU/SLACK/WEBHOOK"
    method: "POST"
```

## Documentación Relacionada

- [Guía de Configuración](Guia-Configuracion.md)
- [Configuración con Docker Compose](Configuracion-Docker-Compose.md)
- [Interfaz de Gestión](../../MANAGEMENT_UI.md)
- [Solución de Problemas](Solucion-Problemas.md)
