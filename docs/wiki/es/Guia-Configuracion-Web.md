# Guía de Configuración Web

## Descripción General

Janitorr ahora proporciona una interfaz de configuración web completa que le permite administrar todas las configuraciones a través de su navegador sin editar archivos YAML manualmente.

## Acceso a la Interfaz de Configuración

1. Navegue a la interfaz de administración de Janitorr en `http://<su-servidor>:8978/`
2. Haga clic en el botón **⚙️ Configuration**
3. Será llevado a la interfaz de configuración con múltiples pestañas

## Secciones de la Interfaz

### 🔌 Pestaña Servicios

Configure todas las conexiones de servicios externos:

#### Sonarr (Series de TV)
- **URL**: La URL base de su instancia de Sonarr (ej: `http://sonarr:8989`)
- **Clave API**: Su clave API de Sonarr (ubicada en Configuración → General → Seguridad)
- **Eliminar Series Vacías**: Eliminar entradas de series de Sonarr cuando se eliminan todas las temporadas
- **Exclusiones de Importación**: Agregar medios eliminados a la lista de exclusión de importación de Sonarr
- **Determinar Edad Por**: Elija cómo calcular la edad de los medios (Más Reciente, Más Antiguo, o Auto desde el perfil)
- **🔍 Probar Conexión**: Verificar conexión con Sonarr

#### Radarr (Películas)
- **URL**: La URL base de su instancia de Radarr (ej: `http://radarr:7878`)
- **Clave API**: Su clave API de Radarr
- **Solo Eliminar Archivos**: ⚠️ NO RECOMENDADO - Elimina archivos pero mantiene entradas de Radarr
- **Exclusiones de Importación**: Agregar medios eliminados a la lista de exclusión de importación de Radarr
- **Determinar Edad Por**: Elija cómo calcular la edad de los medios
- **🔍 Probar Conexión**: Verificar conexión con Radarr

#### Servidor de Medios Jellyfin/Emby
⚠️ Solo un servidor de medios puede estar habilitado a la vez

- **URL**: URL del servidor de medios (ej: `http://jellyfin:8096`)
- **Clave API**: Clave API del servidor de medios
- **Usuario/Contraseña**: Credenciales para operaciones de eliminación
- **Permitir Eliminación**: Permitir que Janitorr elimine medios del servidor
- **Tipo de Próximos a Salir**: Elija qué tipos de medios incluir (Películas y TV, Solo Películas, Solo TV, Ninguno)
- **Nombres de Colecciones**: Personalizar nombres de colecciones "Próximos a Salir"
- **🔍 Probar Conexión**: Verificar conexión con el servidor de medios

#### Jellyseerr/Overseerr
- **URL**: URL del sistema de gestión de solicitudes (ej: `http://jellyseerr:5055`)
- **Clave API**: Clave API de Jellyseerr/Overseerr
- **Coincidir Servidor**: Habilitar para múltiples instancias de Sonarr/Radarr
- **🔍 Probar Conexión**: Verificar conexión

#### Jellystat/Streamystats
⚠️ Solo un servicio de estadísticas puede estar habilitado a la vez

- **URL**: URL del servicio de estadísticas (ej: `http://jellystat:3000`)
- **Clave API**: Clave API del servicio de estadísticas
- **Serie Completa**: Considerar serie completa vista si cualquier episodio fue visto
- **🔍 Probar Conexión**: Verificar conexión

#### Bazarr (Opcional)
- **URL**: URL de Bazarr para gestión de subtítulos
- **Clave API**: Clave API de Bazarr
- **🔍 Probar Conexión**: Verificar conexión

**Consejo**: Use el botón **🔍 Probar Todas las Conexiones** en la parte superior para probar todos los servicios habilitados a la vez.

---

### 🧹 Pestaña Limpieza

Configure las reglas de limpieza automática:

#### Eliminación de Medios
Eliminar automáticamente películas y series de TV según el espacio en disco y la edad.

- **Habilitar Eliminación de Medios**: Activar limpieza automática
- **Reglas de Expiración de Películas**: Definir mapeos % de espacio en disco → días
  - Ejemplo: `10% → 30 días` significa que las películas de más de 30 días se eliminan cuando el disco tiene 10% o menos libre
  - Porcentajes más altos = limpieza más agresiva
- **Reglas de Expiración de Temporadas**: Igual que películas pero para temporadas de series de TV
- **+ Agregar Regla**: Crear reglas de expiración adicionales

#### Eliminación Basada en Etiquetas
Eliminar medios según etiquetas asignadas en Sonarr/Radarr.

- **Habilitar Eliminación Basada en Etiquetas**: Activar limpieza basada en etiquetas
- **% Mínimo de Disco Libre**: Solo eliminar cuando el espacio en disco esté por debajo de este umbral
- **Programaciones de Etiquetas**: Configurar mapeos etiqueta → expiración
  - Ejemplo: Etiqueta `demo` → `7 días` significa que los medios etiquetados con "demo" expiran después de 7 días
- **+ Agregar Programación**: Crear programaciones de etiquetas adicionales

#### Eliminación de Episodios
Gestionar episodios de programas específicos (típicamente para programas diarios).

- **Habilitar Eliminación de Episodios**: Activar limpieza de episodios
- **Etiqueta**: Los programas con esta etiqueta en Sonarr tendrán episodios gestionados
- **Máx. Episodios a Mantener**: Número de episodios más recientes a retener
- **Edad Máxima (días)**: Edad máxima para cualquier episodio, incluso si está bajo el conteo máximo

---

### 📁 Pestaña Sistema de Archivos

Configure el acceso al sistema de archivos y rutas:

- **Habilitar Acceso al Sistema de Archivos**: Permitir que Janitorr acceda al sistema de archivos
- **Validar Semilla**: Verificar si los archivos aún están sembrando antes de eliminar
- **Reconstruir desde Cero**: Limpiar y reconstruir directorio "Próximos a Salir" en cada ejecución
- **Directorio Próximos a Salir (Janitorr)**: Ruta vista por Janitorr
- **Directorio Próximos a Salir (Servidor de Medios)**: Ruta vista por Jellyfin/Emby (si es diferente)
- **Directorio de Verificación de Espacio Libre**: Directorio para verificar espacio libre en disco (predeterminado: `/`)

**Importante**: Las rutas deben ser consistentes entre Janitorr, servidores de medios y servicios *arr.

---

### ⚡ Pestaña General

Configure ajustes generales de la aplicación:

#### Comportamiento de la Aplicación
- **Modo de Prueba en Seco**: 🔴 RECOMENDADO - Previsualizar eliminaciones sin eliminar realmente
- **Ejecutar Una Vez y Salir**: Ejecutar limpieza una vez y luego detener la aplicación
- **Modo de Serie Completa**: Tratar serie completa como vista recientemente si cualquier episodio fue visto
- **Verificación de Semilla de Serie Completa**: Verificar si alguna temporada está sembrando antes de eliminar serie completa
- **Advertencia de Próximos a Salir (días)**: Días antes de la eliminación para agregar medios a la colección "Próximos a Salir"

#### Etiquetas de Exclusión
Etiquetas que protegen medios de ser eliminados. Los medios con estas etiquetas en Sonarr/Radarr nunca se limpiarán.

- Predeterminado: `janitorr_keep`
- **+ Agregar Etiqueta**: Crear etiquetas de exclusión adicionales

#### Interfaz de Gestión
- **Habilitar Interfaz de Gestión**: Activar/desactivar esta interfaz web

---

### 💾 Pestaña Respaldo

Gestionar respaldos de configuración:

#### Acciones
- **Crear Respaldo Ahora**: Crear manualmente un respaldo de la configuración actual
- **📥 Exportar Configuración**: Descargar configuración actual como archivo YAML
- **📤 Importar Configuración**: Cargar y aplicar un archivo YAML de configuración
- **🔄 Restablecer a Predeterminados**: Restaurar configuración a los valores predeterminados de la plantilla

#### Respaldos Disponibles
Lista todos los respaldos de configuración disponibles con marcas de tiempo. Cada respaldo puede ser restaurado con el botón **Restaurar**.

**Nota**: Los respaldos se crean automáticamente antes de cualquier cambio de configuración.

---

## Flujo de Trabajo

### Configuración Inicial

1. **Configurar Pestaña de Servicios**
   - Ingresar URLs y claves API para todos sus servicios
   - Usar botones **🔍 Probar Conexión** para verificar cada servicio
   - Hacer clic en **🔍 Probar Todas las Conexiones** para verificar todos a la vez

2. **Configurar Reglas de Limpieza**
   - Comenzar con reglas conservadoras (tiempos de expiración más largos)
   - Habilitar **Modo de Prueba en Seco** en la pestaña General
   - Probar reglas y ajustar según sea necesario

3. **Establecer Rutas del Sistema de Archivos**
   - Asegurar que las rutas coincidan con sus mapeos de volúmenes Docker
   - Verificar que las rutas sean accesibles por Janitorr

4. **Guardar Configuración**
   - Hacer clic en **💾 Guardar Configuración**
   - ⚠️ Reiniciar Janitorr para que los cambios surtan efecto

### Realizar Cambios

1. Modificar cualquier configuración en la interfaz web
2. Probar conexiones si se cambian configuraciones de servicios
3. Hacer clic en **💾 Guardar Configuración**
4. Reiniciar contenedor Janitorr para que los cambios surtan efecto

### Pruebas Antes de Producción

1. Habilitar **Modo de Prueba en Seco** en la pestaña General
2. Guardar configuración y reiniciar
3. Revisar registros para ver qué se eliminaría
4. Ajustar reglas según sea necesario
5. Cuando esté satisfecho, deshabilitar Modo de Prueba en Seco
6. Guardar y reiniciar

---

## Notas Importantes

### Modo de Prueba en Seco
**¡Siempre pruebe primero con Prueba en Seco habilitada!** Esto le permite ver qué eliminaría Janitorr sin eliminar nada realmente.

### Reinicio Requerido
Los cambios de configuración requieren un reinicio de Janitorr para tomar efecto. Esto es porque Spring Boot carga la configuración al inicio.

```bash
docker restart janitorr
```

### Respaldos
- Los respaldos se crean automáticamente antes de cualquier cambio de configuración
- Los respaldos se almacenan en `/config/backups/` dentro del contenedor
- Puede restaurar cualquier respaldo anterior desde la pestaña Respaldo

### Consistencia de Rutas
Asegurar que las rutas sean consistentes entre:
- Contenedor Janitorr
- Contenedor Jellyfin/Emby
- Contenedores Sonarr/Radarr

Si Janitorr ve `/data/media/movies` y Jellyfin ve `/media/movies`, no coincidirán correctamente.

### Prueba de Conexiones
Siempre pruebe las conexiones después de ingresar nuevas credenciales de servicio. Esto ayuda a identificar:
- URLs inválidas
- Claves API incorrectas
- Problemas de conectividad de red
- Problemas de autenticación

### Seguridad
La Interfaz de Gestión no tiene autenticación incorporada. Si se expone a Internet, use un proxy inverso con autenticación (ej: Nginx, Traefik con Auth Básica).

---

## Solución de Problemas

### La Configuración No Se Guarda
- Verificar que Janitorr tenga permisos de escritura en `/config/application.yml`
- Verificar registros del contenedor para errores
- Verificar que el mapeo de volumen sea correcto

### Las Pruebas de Conexión Fallan
- Verificar que las URLs sean accesibles desde el contenedor Janitorr
- Verificar que las claves API sean correctas
- Asegurar que los servicios estén ejecutándose
- Verificar configuración de red (redes Docker, reglas de firewall)

### Los Cambios No Surten Efecto
- ¿Reinició Janitorr después de guardar?
- Verificar registros del contenedor para errores de configuración
- Verificar que los cambios se guardaron en `/config/application.yml`

### La Interfaz No Carga
- Verificar que la Interfaz de Gestión esté habilitada: `management.ui.enabled=true`
- Verificar que no esté usando el perfil `leyden` en tiempo de ejecución
- Verificar la consola del navegador para errores de JavaScript

---

## Uso Avanzado

### Importar/Exportar Configuraciones

**Exportar:**
1. Ir a la pestaña Respaldo
2. Hacer clic en **📥 Exportar Configuración**
3. Guardar el archivo `application.yml` descargado

**Importar:**
1. Tener un archivo `application.yml` válido listo
2. Ir a la pestaña Respaldo
3. Hacer clic en **📤 Importar Configuración**
4. Seleccionar su archivo YAML
5. Reiniciar Janitorr

Casos de uso:
- Compartir configuraciones entre instancias
- Control de versiones de configuraciones
- Recuperación rápida ante desastres

### Múltiples Instancias
Si ejecuta múltiples instancias de Janitorr:
1. Exportar configuración de la instancia primaria
2. Importar a instancias secundarias
3. Ajustar configuraciones específicas de la instancia (puertos, rutas)
4. Guardar y reiniciar

---

## Próximos Pasos

Después de configurar Janitorr:
1. ✅ Probar con Prueba en Seco habilitada
2. ✅ Revisar registros para verificar comportamiento
3. ✅ Ajustar reglas según sus necesidades
4. ✅ Deshabilitar Prueba en Seco cuando esté confiado
5. ✅ Configurar limpiezas programadas (programaciones cron en YAML)

Para más información, consulte:
- [Guía de Configuración](Guia-Configuracion.md) - Referencia detallada de configuración YAML
- [Configuración Docker Compose](Configuracion-Docker-Compose.md) - Guía de despliegue de contenedores
- [Preguntas Frecuentes](Preguntas-Frecuentes.md) - Preguntas y respuestas comunes
- [Solución de Problemas](Solucion-Problemas.md) - Resolver problemas comunes
