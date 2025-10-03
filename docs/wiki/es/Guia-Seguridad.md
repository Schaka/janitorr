# Guía de Seguridad

## Resumen

Janitorr proporciona autenticación HTTP Basic integrada para proteger los endpoints de la API del acceso no autorizado. Esta guía explica cómo habilitar y configurar la autenticación para tu instancia de Janitorr.

## ⚠️ Advertencia de Seguridad

Por defecto, todos los endpoints de la API son **públicamente accesibles sin autenticación**. Esto crea un riesgo de seguridad en entornos de producción. **DEBES** asegurar tu instancia de Janitorr usando uno de los métodos a continuación.

## Opciones de Seguridad

Tienes tres opciones para asegurar tu instancia de Janitorr:

### 1. Autenticación HTTP Basic Integrada (Recomendado)

Habilita la autenticación integrada de Janitorr configurando `application.yml`:

```yaml
security:
  enabled: true
  username: tu-usuario
  password: tu-contraseña-segura
```

**Ventajas:**
- Fácil de configurar
- No requiere infraestructura adicional
- Las contraseñas se cifran de forma segura usando BCrypt
- Funciona con todos los clientes HTTP y navegadores

**Desventajas:**
- Autenticación de un solo usuario
- La autenticación básica sobre HTTP no es segura (¡usa HTTPS!)
- Sin características avanzadas como 2FA u OAuth

### 2. Autenticación con Proxy Inverso

Usa un proxy inverso (Nginx, Traefik, Caddy) con autenticación:

**Ejemplo con Nginx:**
```nginx
location / {
    auth_basic "Janitorr";
    auth_basic_user_file /etc/nginx/.htpasswd;
    proxy_pass http://janitorr:8978;
}
```

**Ejemplo con Traefik:**
```yaml
http:
  middlewares:
    janitorr-auth:
      basicAuth:
        users:
          - "user:$apr1$..."
  routers:
    janitorr:
      middlewares:
        - janitorr-auth
```

**Ventajas:**
- Opciones de autenticación más flexibles
- Puede integrarse con sistemas de autenticación existentes
- Mejor para múltiples servicios
- Puede forzar HTTPS

**Desventajas:**
- Requiere infraestructura adicional
- Configuración más compleja

### 3. Restricciones a Nivel de Red

Usa reglas de firewall o aislamiento de red Docker para restringir el acceso:

**Ejemplo con Docker Compose:**
```yaml
services:
  janitorr:
    networks:
      - internal
    # Sin mapeo de puertos al host - solo accesible dentro de la red Docker

networks:
  internal:
    internal: true
```

**Ventajas:**
- Simple y efectivo
- Sin sobrecarga de autenticación
- Funciona para implementaciones privadas/internas

**Desventajas:**
- Solo funciona para entornos aislados
- No es adecuado para acceso remoto
- Sin control de acceso a nivel de usuario

## Habilitar la Autenticación Integrada

### Paso 1: Configurar Credenciales

Edita tu `application.yml`:

```yaml
security:
  enabled: true
  username: admin           # ¡Cámbialo!
  password: contraseña-super-secreta  # ¡Cámbialo!
```

**IMPORTANTE:** 
- NO uses las credenciales predeterminadas (`admin`/`admin`) en producción
- Usa una contraseña fuerte y única
- Janitorr registrará una advertencia si se detectan credenciales predeterminadas

### Paso 2: Reiniciar Janitorr

Reinicia el contenedor o servicio de Janitorr:

```bash
docker compose restart janitorr
```

### Paso 3: Verificar la Autenticación

Intenta acceder a la API sin credenciales:

```bash
curl http://localhost:8978/api/management/status
# Debería devolver 401 Unauthorized
```

Intenta con credenciales:

```bash
curl -u admin:contraseña-super-secreta http://localhost:8978/api/management/status
# Debería devolver información de estado
```

## Usar Endpoints Autenticados

### Navegador Web

Al acceder a la interfaz de gestión (`http://localhost:8978/`), tu navegador solicitará el nombre de usuario y la contraseña.

### curl

Usa la bandera `-u`:

```bash
curl -u usuario:contraseña http://localhost:8978/api/management/status
```

O usa el encabezado `Authorization`:

```bash
curl -H "Authorization: Basic $(echo -n 'usuario:contraseña' | base64)" \
  http://localhost:8978/api/management/status
```

### Python

```python
import requests
from requests.auth import HTTPBasicAuth

response = requests.get(
    'http://localhost:8978/api/management/status',
    auth=HTTPBasicAuth('usuario', 'contraseña')
)
```

### JavaScript/Node.js

```javascript
const axios = require('axios');

axios.get('http://localhost:8978/api/management/status', {
    auth: {
        username: 'usuario',
        password: 'contraseña'
    }
})
.then(response => console.log(response.data));
```

## Endpoints Públicos

Los siguientes endpoints permanecen públicamente accesibles incluso cuando la autenticación está habilitada:

- `/health` - Endpoint de verificación de salud
- `/actuator/health` - Endpoint de salud de Spring Boot
- `/actuator/info` - Información de la aplicación
- `/` - Archivos estáticos de la interfaz de gestión (HTML, CSS, JS)
- `/index.html` - Página principal de la interfaz

## Variables de Entorno de Docker

Puedes sobrescribir la configuración de seguridad usando variables de entorno:

```yaml
services:
  janitorr:
    environment:
      - SECURITY_ENABLED=true
      - SECURITY_USERNAME=miusuario
      - SECURITY_PASSWORD=micontraseña
```

## Mejores Prácticas de Seguridad

### 1. Usa HTTPS

La autenticación HTTP Basic transmite credenciales en formato base64, que es fácilmente reversible y no proporciona protección de seguridad. Siempre usa HTTPS en producción:

- Usa un proxy inverso con certificados SSL/TLS
- Obtén certificados gratuitos de [Let's Encrypt](https://letsencrypt.org/)
- Considera usar Cloudflare para terminación SSL

### 2. Usa Contraseñas Fuertes

- Mínimo 12 caracteres
- Mezcla de mayúsculas, minúsculas, números y símbolos
- No reutilices contraseñas de otros servicios
- Usa un administrador de contraseñas

### 3. Cambia las Credenciales Predeterminadas

Nunca uses las credenciales predeterminadas `admin`/`admin` en producción. Janitorr registrará advertencias si se detectan valores predeterminados.

### 4. Limita la Exposición de Red

- No expongas Janitorr directamente a Internet
- Usa VPN o túneles SSH para acceso remoto
- Usa reglas de firewall para restringir el acceso

### 5. Actualizaciones Regulares

Mantén Janitorr actualizado para recibir parches de seguridad:

```bash
docker compose pull janitorr
docker compose up -d janitorr
```

### 6. Monitorea los Registros de Acceso

Revisa los registros en busca de intentos de acceso no autorizados:

```bash
docker compose logs janitorr | grep -i "401\|unauthorized"
```

## Solución de Problemas

### Errores "401 Unauthorized"

**Problema:** Obteniendo errores 401 incluso con credenciales correctas

**Soluciones:**
1. Verifica que `security.enabled: true` esté configurado en `application.yml`
2. Verifica que las credenciales sean correctas (sensible a mayúsculas)
3. Asegúrate de que no haya espacios extra en la configuración
4. Verifica que las variables de entorno de Docker no sobrescriban la configuración
5. Reinicia Janitorr después de cambios en la configuración

### Bucle de Inicio de Sesión en la Interfaz de Gestión

**Problema:** El navegador sigue solicitando credenciales

**Soluciones:**
1. Limpia la caché y cookies del navegador
2. Verifica la consola del navegador en busca de errores
3. Verifica que los recursos estáticos se sirvan sin autenticación
4. Prueba un navegador diferente

### Fallos en las Verificaciones de Salud

**Problema:** Las verificaciones de salud de Docker fallan después de habilitar la seguridad

**Soluciones:**
Los endpoints de verificación de salud (`/health`, `/actuator/health`) deben permanecer públicos. Si están siendo bloqueados:

1. Verifica que SecurityConfig.kt permita estos endpoints
2. Actualiza la verificación de salud de Docker si usas un endpoint personalizado
3. Verifica que `@ConditionalOnProperty` esté funcionando correctamente

### Configuración No Aplicada

**Problema:** Los cambios en `application.yml` no tienen efecto

**Soluciones:**
1. Reinicia el contenedor/servicio de Janitorr
2. Verifica la sintaxis YAML (¡la indentación importa!)
3. Verifica que el archivo esté montado correctamente en Docker
4. Busca variables de entorno que sobrescriban la configuración
5. Busca errores de Spring Boot en los registros

## Integración con Multi-Tenancy

Si estás usando la función de multi-tenancy de Janitorr, la autenticación integrada proporciona seguridad básica para los endpoints de gestión de usuarios. Sin embargo, para uso en producción, deberías:

1. Habilitar la autenticación integrada
2. Implementar control de acceso basado en roles adicional (característica futura)
3. Usar un proxy inverso para autenticación avanzada

Ver la [Guía de Multi-Tenancy](Guia-Multi-Tenancy.md) para más información.

## Migración desde Configuración No Asegurada

Si estás agregando autenticación a una instancia existente de Janitorr:

1. **Respalda tu configuración:**
   ```bash
   cp application.yml application.yml.backup
   ```

2. **Agrega configuración de seguridad:**
   ```yaml
   security:
     enabled: true
     username: tu-usuario
     password: tu-contraseña
   ```

3. **Actualiza scripts y automatizaciones:**
   - Agrega autenticación a comandos curl
   - Actualiza scripts de monitoreo
   - Configura clientes API con credenciales

4. **Prueba antes de producción:**
   - Prueba con `security.enabled: false` primero
   - Verifica que toda la funcionalidad funcione
   - Habilita la seguridad y prueba nuevamente

5. **Actualiza la documentación:**
   - Documenta las credenciales de forma segura
   - Actualiza manuales y procedimientos
   - Capacita a los usuarios en el proceso de inicio de sesión

## Mejoras Futuras

Las siguientes características de seguridad están planificadas para futuras versiones:

- **Autenticación con Token JWT** - Para integraciones de API
- **OAuth 2.0** - Integración con Google, GitHub, Discord
- **Autenticación de Dos Factores (2FA)** - Seguridad mejorada
- **Control de Acceso Basado en Roles** - Permisos granulares
- **Claves API** - Para automatización e integraciones
- **Limitación de Tasa** - Prevenir ataques de fuerza bruta
- **Registro de Auditoría** - Rastrear eventos de seguridad
- **Gestión de Sesiones** - Controlar sesiones activas

## Soporte

Si encuentras problemas de seguridad o tienes preguntas:

- Revisa las [Preguntas Frecuentes](Preguntas-Frecuentes.md)
- Revisa la [Guía de Solución de Problemas](Solucion-Problemas.md)
- Abre una [Discusión en GitHub](https://github.com/carcheky/janitorr/discussions)
- Reporta vulnerabilidades de seguridad de forma privada (ver SECURITY.md)

## Documentación Relacionada

- [Guía de Configuración](Guia-Configuracion.md)
- [Configuración de Docker Compose](Configuracion-Docker-Compose.md)
- [Guía de Multi-Tenancy](Guia-Multi-Tenancy.md)
- [Solución de Problemas](Solucion-Problemas.md)
