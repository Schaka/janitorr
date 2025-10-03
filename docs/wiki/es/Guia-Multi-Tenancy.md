# Sistema de Multi-Tenancy y Perfiles de Usuario

## Resumen

Janitorr ahora soporta multi-tenancy y perfiles de usuario, permitiendo múltiples usuarios con diferentes roles y configuraciones aisladas. Esta característica transforma Janitorr de una aplicación de un solo usuario a un sistema multi-usuario adecuado para familias, hosting compartido, o despliegues empresariales.

## ⚠️ Notas Importantes

- **Esta característica está DESHABILITADA por defecto** - Debes habilitarla explícitamente en tu configuración
- **Almacenamiento en memoria** - La implementación actual usa almacenamiento en memoria (los datos se pierden al reiniciar)
- **Sin autenticación por defecto** - Los endpoints de la API no están seguros a menos que agregues autenticación
- **Etapa de desarrollo** - Esta es una implementación fundamental que puede ser extendida

## Características

### Gestión de Usuarios
- Crear, leer, actualizar y eliminar usuarios
- Control de acceso basado en roles (ADMIN, POWER_USER, STANDARD_USER, READ_ONLY)
- Perfiles de usuario con configuraciones personalizadas
- Gestión de contraseñas

### Multi-Tenancy
- Crear y gestionar tenants (organizaciones/familias)
- Asociar usuarios con tenants
- Aislamiento y configuración a nivel de tenant
- Soporte para servicios compartidos con filtros

### Roles de Usuario

| Rol | Permisos |
|------|------------|
| **ADMIN** | Acceso completo al sistema, gestión de usuarios, configuración global, todas las operaciones de limpieza |
| **POWER_USER** | Gestión de perfil propio, creación de reglas avanzadas, ejecución manual de limpieza, ver todas las estadísticas |
| **STANDARD_USER** | Acceso básico al perfil, ver estadísticas propias, solicitar operaciones de limpieza, configuración limitada |
| **READ_ONLY** | Ver dashboards, verificar estado del sistema, sin permisos de limpieza |

## Configuración

### Habilitar Multi-Tenancy

Agrega a tu `application.yml`:

```yaml
multitenancy:
  enabled: true
  
  # Opcional: Crear usuario admin por defecto al iniciar
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "change-me-please"  # ¡CAMBIA ESTO!
  
  # Opcional: Autenticación JWT (aún no implementado)
  auth:
    jwt-enabled: false
    jwt-secret: "tu-clave-secreta-aqui"
    jwt-expiration-seconds: 86400
```

### Consideraciones de Seguridad

**CRÍTICO**: La implementación actual NO incluye autenticación. Los endpoints de la API están expuestos sin protección. **DEBES** agregar una de las siguientes opciones:

1. **Autenticación con Proxy Inverso**: Usa Nginx, Traefik, o Caddy con autenticación
2. **Spring Security**: Agregar configuración de Spring Security (mejora futura)
3. **Aislamiento de Red**: Restringir acceso mediante reglas de firewall

## Endpoints de la API

### Gestión de Usuarios

#### Crear Usuario
```bash
POST /api/users
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "password": "contraseña-segura",
  "role": "STANDARD_USER",
  "tenantId": "id-tenant-opcional"
}
```

#### Obtener Todos los Usuarios
```bash
GET /api/users
```

#### Obtener Usuario por ID
```bash
GET /api/users/{userId}
```

#### Obtener Perfil de Usuario
```bash
GET /api/users/{userId}/profile
```

#### Actualizar Perfil de Usuario
```bash
PUT /api/users/{userId}/profile
Content-Type: application/json

{
  "userId": "id-usuario",
  "displayName": "Juan Pérez",
  "preferences": {
    "theme": "dark",
    "language": "es",
    "timezone": "UTC"
  }
}
```

#### Actualizar Rol de Usuario
```bash
PATCH /api/users/{userId}/role
Content-Type: application/json

{
  "role": "POWER_USER"
}
```

#### Habilitar/Deshabilitar Usuario
```bash
PATCH /api/users/{userId}/enabled
Content-Type: application/json

{
  "enabled": false
}
```

#### Cambiar Contraseña
```bash
POST /api/users/{userId}/password
Content-Type: application/json

{
  "newPassword": "nueva-contraseña-segura"
}
```

#### Eliminar Usuario
```bash
DELETE /api/users/{userId}
```

### Gestión de Tenants

#### Crear Tenant
```bash
POST /api/tenants
Content-Type: application/json

{
  "name": "Familia García",
  "domain": "garcia.ejemplo.com"
}
```

#### Obtener Todos los Tenants
```bash
GET /api/tenants
```

#### Obtener Tenant por ID
```bash
GET /api/tenants/{tenantId}
```

#### Obtener Usuarios del Tenant
```bash
GET /api/tenants/{tenantId}/users
```

#### Agregar Usuario al Tenant
```bash
POST /api/tenants/{tenantId}/users
Content-Type: application/json

{
  "userId": "id-usuario",
  "role": "STANDARD_USER"
}
```

#### Remover Usuario del Tenant
```bash
DELETE /api/tenants/{tenantId}/users/{userId}
```

#### Eliminar Tenant
```bash
DELETE /api/tenants/{tenantId}
```

## Ejemplos de Uso

### Configuración Familiar

1. **Crear tenant para la familia:**
```bash
curl -X POST http://localhost:8978/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"name": "Familia García"}'
```

2. **Crear usuarios con diferentes roles:**
```bash
# Padre (Admin)
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "papa@garcia.familia",
    "password": "contraseña-segura-1",
    "role": "ADMIN"
  }'

# Madre (Power User)
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "mama@garcia.familia",
    "password": "contraseña-segura-2",
    "role": "POWER_USER"
  }'

# Adolescente (Standard User)
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "adolescente@garcia.familia",
    "password": "contraseña-segura-3",
    "role": "STANDARD_USER"
  }'

# Niño (Read-Only)
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "niño@garcia.familia",
    "password": "contraseña-segura-4",
    "role": "READ_ONLY"
  }'
```

3. **Agregar usuarios al tenant:**
```bash
curl -X POST http://localhost:8978/api/tenants/{tenantId}/users \
  -H "Content-Type: application/json" \
  -d '{"userId": "{userId}", "role": "ADMIN"}'
```

## Arquitectura

### Modelo de Datos

```
User
├── id: String
├── email: String
├── passwordHash: String
├── role: UserRole
├── tenantId: String?
├── enabled: Boolean
└── createdAt: LocalDateTime

UserProfile
├── userId: String
├── displayName: String
├── preferences: UserPreferences
├── cleanupSettings: UserCleanupSettings
├── notifications: NotificationSettings
├── mediaAccess: MediaAccessRules
└── quotas: ResourceQuotas

Tenant
├── id: String
├── name: String
├── domain: String?
├── settings: TenantSettings
└── enabled: Boolean
```

### Almacenamiento

**Actual**: En memoria (ConcurrentHashMap)
- Los datos se pierden al reiniciar la aplicación
- Adecuado para pruebas y desarrollo
- Rendimiento rápido

**Futuro**: Persistencia en base de datos
- Soporte para H2, PostgreSQL, MySQL
- Entidades JPA/Hibernate
- Scripts de migración
- Listo para producción

## Limitaciones

1. **Sin Persistencia**: Los datos se almacenan solo en memoria
2. **Sin Autenticación**: Los endpoints de la API no están seguros
3. **Sin Tokens JWT**: Autenticación basada en tokens no implementada
4. **Sin OAuth**: Autenticación de terceros no implementada
5. **Sin Gestión de Sesiones**: No hay seguimiento de sesiones activas
6. **Hash de Contraseña Básico**: Usa Base64 simple (debería usar BCrypt en producción)
7. **Sin Log de Auditoría**: Las acciones del usuario no se registran
8. **Sin Limitación de Tasa**: No hay limitación de tasa de API por usuario
9. **Sin Aplicación de Cuotas**: Las cuotas se rastrean pero no se aplican

## Mejoras Futuras

Las siguientes mejoras están bajo consideración y pueden implementarse en versiones futuras:

### Fase 1: Seguridad (Prioridad - Bajo Consideración)
- [ ] Integración con Spring Security (puede agregarse)
- [ ] Generación y validación de tokens JWT (bajo consideración)
- [ ] Hash de contraseñas BCrypt (puede implementarse)
- [ ] Gestión de sesiones (bajo consideración)
- [ ] Protección CSRF (puede agregarse)

### Fase 2: Persistencia (Bajo Consideración)
- [ ] Entidades JPA (puede implementarse)
- [ ] Base de datos H2 para desarrollo (bajo consideración)
- [ ] Soporte PostgreSQL para producción (puede agregarse)
- [ ] Scripts de migración de base de datos (bajo consideración)
- [ ] Backup y restauración (puede implementarse)

### Fase 3: Características Avanzadas (Bajo Consideración)
- [ ] Integración OAuth (Google, GitHub, Discord) (puede agregarse)
- [ ] Autenticación de dos factores (bajo consideración)
- [ ] Gestión de claves API (puede implementarse)
- [ ] Limitación de tasa (bajo consideración)
- [ ] Log de auditoría (puede agregarse)
- [ ] Notificaciones por email (bajo consideración)
- [ ] Recuperación de contraseña (puede implementarse)
- [ ] Sistema de invitación de usuarios (bajo consideración)

### Fase 4: Integración UI (Bajo Consideración)
- [ ] Páginas de login/logout (puede agregarse)
- [ ] Dashboard de gestión de usuarios (bajo consideración)
- [ ] Página de configuración de perfil (puede implementarse)
- [ ] Selector de tenant (bajo consideración)
- [ ] Componentes UI basados en roles

### Fase 5: Multi-Tenancy
- [ ] Configuraciones específicas por tenant
- [ ] Aislamiento de rutas (`/data/tenants/{tenantId}/`)
- [ ] Filtrado de servicios (instancias *arr por tenant)
- [ ] Aplicación de cuotas de recursos
- [ ] Integración de facturación

## Guía de Migración

### Despliegues Existentes

Si estás actualizando desde una instancia Janitorr de un solo usuario:

1. **Multi-tenancy está deshabilitado por defecto** - Tu configuración existente funcionará sin cambios
2. **Migración opcional** - Puedes elegir habilitar multi-tenancy más tarde
3. **Sin cambios disruptivos** - La configuración existente permanece válida

### Habilitar Multi-Tenancy

1. Agregar configuración a `application.yml`
2. Reiniciar Janitorr
3. Crear usuario admin vía API o habilitar admin por defecto
4. Crear usuarios adicionales según sea necesario

## Mejores Prácticas de Seguridad

1. **Cambiar contraseña de admin por defecto inmediatamente**
2. **Usar contraseñas fuertes y únicas** para todos los usuarios
3. **Habilitar HTTPS** vía proxy inverso
4. **Restringir acceso de red** a rangos IP confiables
5. **Implementar autenticación** antes del uso en producción
6. **Rotar contraseñas regularmente**
7. **Monitorear actividad de usuarios** vía logs
8. **Deshabilitar características no usadas**
9. **Mantener Janitorr actualizado**
10. **Hacer backup de configuración** regularmente

## Solución de Problemas

### Los endpoints de multi-tenancy devuelven 404

Verifica que multi-tenancy esté habilitado en tu configuración:
```yaml
multitenancy:
  enabled: true
```

### Admin por defecto no creado

Verifica la configuración:
```yaml
multitenancy:
  enabled: true
  default-admin:
    create-on-startup: true
```

Revisa los logs en busca de errores durante el inicio.

### No se puede eliminar usuario

Asegúrate de que el ID de usuario sea correcto y que el usuario exista. Los usuarios asociados con tenants deben ser removidos de los tenants primero (o serán removidos automáticamente al eliminar).

## Soporte

Para preguntas, problemas o solicitudes de características:
- GitHub Issues: https://github.com/carcheky/janitorr/issues
- GitHub Discussions: https://github.com/carcheky/janitorr/discussions

## Contribuir

¡Las contribuciones son bienvenidas! Áreas que necesitan ayuda:
- Integración con Spring Security
- Persistencia en base de datos (JPA)
- Integración OAuth
- Componentes UI para gestión de usuarios
- Pruebas y documentación

Ver [CONTRIBUTING.md](../../CONTRIBUTING.md) para lineamientos.
