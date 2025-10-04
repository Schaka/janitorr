# Guía de Inicio Rápido Multi-Tenancy

Esta guía te ayudará a habilitar y probar rápidamente las características de multi-tenancy en Janitorr.

## Requisitos Previos

- Janitorr instalado y funcionando
- Acceso para modificar `application.yml`
- curl o herramienta similar para pruebas de API

## Paso 1: Habilitar Multi-Tenancy

Edita tu `application.yml` y agrega:

```yaml
multitenancy:
  enabled: true
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "MiContraseñaSegura123!"  # ¡CAMBIA ESTO!
```

## Paso 2: Reiniciar Janitorr

```bash
docker-compose restart janitorr
# o
docker restart janitorr
```

Verifica los logs para el mensaje de creación del usuario admin:

```bash
docker logs janitorr | grep "DEFAULT ADMIN"
```

Deberías ver:
```
================================================================================
DEFAULT ADMIN USER CREATED
Email: admin@janitorr.local
Password: MiContraseñaSegura123!
PLEASE CHANGE THIS PASSWORD IMMEDIATELY!
================================================================================
```

## Paso 3: Verificar que Multi-Tenancy está Habilitado

Prueba los endpoints de usuarios:

```bash
curl http://localhost:8978/api/users
```

Deberías ver un array JSON con un usuario (el admin por defecto).

## Paso 4: Crear tu Primer Usuario

```bash
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@ejemplo.com",
    "password": "ContraseñaSegura456",
    "role": "POWER_USER"
  }'
```

Respuesta:
```json
{
  "id": "uuid-generado",
  "email": "juan@ejemplo.com",
  "role": "POWER_USER",
  "tenantId": null,
  "enabled": true,
  "createdAt": "2025-01-15T10:30:00",
  "lastLogin": null
}
```

## Paso 5: Crear un Tenant (Opcional)

Para soporte multi-organización:

```bash
curl -X POST http://localhost:8978/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mi Familia",
    "domain": "familia.ejemplo.com"
  }'
```

## Paso 6: Agregar Usuario al Tenant

```bash
# Obtén el ID del tenant de la respuesta anterior, luego:
curl -X POST http://localhost:8978/api/tenants/{tenantId}/users \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{userId}",
    "role": "POWER_USER"
  }'
```

## Operaciones Comunes

### Listar Todos los Usuarios

```bash
curl http://localhost:8978/api/users
```

### Obtener Perfil de Usuario

```bash
curl http://localhost:8978/api/users/{userId}/profile
```

### Actualizar Perfil de Usuario

```bash
curl -X PUT http://localhost:8978/api/users/{userId}/profile \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{userId}",
    "displayName": "Juan Pérez",
    "preferences": {
      "theme": "dark",
      "language": "es",
      "timezone": "America/Mexico_City"
    }
  }'
```

### Cambiar Contraseña de Usuario

```bash
curl -X POST http://localhost:8978/api/users/{userId}/password \
  -H "Content-Type: application/json" \
  -d '{
    "newPassword": "NuevaContraseñaSegura789"
  }'
```

### Actualizar Rol de Usuario

```bash
curl -X PATCH http://localhost:8978/api/users/{userId}/role \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN"
  }'
```

### Deshabilitar Usuario

```bash
curl -X PATCH http://localhost:8978/api/users/{userId}/enabled \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": false
  }'
```

### Eliminar Usuario

```bash
curl -X DELETE http://localhost:8978/api/users/{userId}
```

## Ejemplo de Configuración Familiar

Aquí hay un ejemplo completo para configurar una familia:

```bash
# 1. Crear tenant
TENANT_RESPONSE=$(curl -s -X POST http://localhost:8978/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"name": "Familia García"}')

TENANT_ID=$(echo $TENANT_RESPONSE | jq -r '.id')

# 2. Crear miembros de la familia
# Padre 1 (Admin)
USER1=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "padre1@garcia.familia",
    "password": "ContraseñaSegura1",
    "role": "ADMIN"
  }')
USER1_ID=$(echo $USER1 | jq -r '.id')

# Madre (Power User)
USER2=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "madre@garcia.familia",
    "password": "ContraseñaSegura2",
    "role": "POWER_USER"
  }')
USER2_ID=$(echo $USER2 | jq -r '.id')

# Adolescente (Standard User)
USER3=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "adolescente@garcia.familia",
    "password": "ContraseñaSegura3",
    "role": "STANDARD_USER"
  }')
USER3_ID=$(echo $USER3 | jq -r '.id')

# Niño (Read Only)
USER4=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "niño@garcia.familia",
    "password": "ContraseñaSegura4",
    "role": "READ_ONLY"
  }')
USER4_ID=$(echo $USER4 | jq -r '.id')

# 3. Agregar todos al tenant
for USER_ID in $USER1_ID $USER2_ID $USER3_ID $USER4_ID; do
  curl -s -X POST http://localhost:8978/api/tenants/$TENANT_ID/users \
    -H "Content-Type: application/json" \
    -d "{\"userId\": \"$USER_ID\", \"role\": \"STANDARD_USER\"}"
done

echo "¡Configuración familiar completa!"
echo "ID del Tenant: $TENANT_ID"
echo "Creados 4 usuarios con diferentes roles"
```

## Recordatorios de Seguridad

⚠️ **NOTAS IMPORTANTES DE SEGURIDAD:**

1. **Sin Autenticación**: Los endpoints de la API no están seguros. Cualquiera con acceso de red puede gestionar usuarios.
   
2. **Usa Proxy Inverso**: Agrega autenticación vía Nginx, Traefik, o Caddy:
   ```nginx
   location /api/users {
       auth_basic "Restringido";
       auth_basic_user_file /etc/nginx/.htpasswd;
       proxy_pass http://janitorr:8978;
   }
   ```

3. **Aislamiento de Red**: Restringe el acceso vía redes Docker o reglas de firewall.

4. **Cambia la Contraseña por Defecto**: Cambia inmediatamente la contraseña del admin por defecto.

## Solución de Problemas

### Los endpoints devuelven 404

Multi-tenancy no está habilitado. Verifica `application.yml`:
```yaml
multitenancy:
  enabled: true
```

### Admin por defecto no creado

Revisa los logs en busca de errores. Verifica la configuración:
```yaml
multitenancy:
  default-admin:
    create-on-startup: true
```

### No se puede conectar a la API

Asegúrate de que Janitorr está ejecutándose y el puerto es accesible:
```bash
docker ps | grep janitorr
curl http://localhost:8978/api/management/status
```

## Próximos Pasos

- Lee la [Guía Completa de Multi-Tenancy](Guia-Multi-Tenancy.md)
- Configura autenticación (proxy inverso o Spring Security)
- Configura perfiles y preferencias de usuario
- Integra con tus flujos de trabajo existentes de Janitorr

## Recursos

- [Documentación en Español](Guia-Multi-Tenancy.md)
- [English Documentation](../en/Multi-Tenancy-Guide.md)
- [GitHub Issues](https://github.com/carcheky/janitorr/issues)
