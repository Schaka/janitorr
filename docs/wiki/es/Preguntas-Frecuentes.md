# Preguntas Frecuentes

Preguntas comunes sobre Janitorr y sus respuestas.

## Preguntas Generales

### ¿Qué es Janitorr?

Janitorr es una herramienta automatizada de gestión de bibliotecas multimedia que funciona con Jellyfin/Emby y el conjunto *arr (Sonarr, Radarr). Te ayuda a limpiar automáticamente medios antiguos y no vistos para liberar espacio en disco.

### ¿Es seguro usar Janitorr?

Sí, cuando está configurado correctamente. Recomendamos encarecidamente:
- Comenzar con `dry-run: true` habilitado
- Revisar los logs antes de habilitar eliminaciones
- Habilitar la Papelera de Reciclaje en tus aplicaciones *arr
- Probar con un subconjunto pequeño de medios primero

### ¿Qué servidores multimedia soporta Janitorr?

Janitorr soporta:
- Jellyfin (completamente probado y mantenido)
- Emby (implementado y probado, pero depende de informes de errores de la comunidad)

**Nota:** Solo se puede habilitar un servidor multimedia (Jellyfin O Emby) a la vez.

### ¿Necesito Jellyfin/Emby para usar Janitorr?

No, pero es muy recomendable. Sin un servidor multimedia configurado:
- Janitorr aún puede eliminar archivos a través de las aplicaciones *arr
- Sin embargo, puedes terminar con carpetas de metadatos huérfanas
- La función de colección "Próximamente a eliminar" no funcionará

## Preguntas de Configuración

### ¿Dónde obtengo el archivo de configuración?

Descarga la plantilla del repositorio:
```bash
wget -O /appdata/janitorr/config/application.yml \
  https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
```

Luego edítalo con tu configuración específica (claves API, URLs, etc.).

### ¿Cuáles son los requisitos mínimos?

- **Docker:** Versión 20.10 o superior
- **Memoria:** Mínimo 200MB, recomendado 256MB (512MB+ para bibliotecas grandes)
- **Espacio en Disco:** Mínimo (unos pocos MB para la aplicación)
- **Servicios Requeridos:**
  - Al menos una aplicación *arr (Sonarr o Radarr)
  - Recomendado: Jellyfin o Emby

### ¿Por qué no arranca mi contenedor?

Razones comunes:
1. **Falta application.yml** - Este archivo es obligatorio
2. **Permisos incorrectos** - El usuario (1000:1000) debe poder leer el archivo de configuración
3. **Conflicto de puerto** - Otro servicio está usando el puerto 8978
4. **YAML inválido** - Verifica tu application.yml por errores de sintaxis

Revisa los logs: `docker logs janitorr`

### ¿Cómo reviso los logs?

**Via Docker:**
```bash
docker logs janitorr
```

**Via archivos de log** (si el registro en archivo está habilitado en application.yml):
```bash
tail -f /appdata/janitorr/logs/janitorr.log
```

## Preguntas de Configuración

### ¿Qué es el modo dry-run?

El modo dry-run (`dry-run: true`) hace que Janitorr simule eliminaciones sin eliminar nada. Hará:
- Registrar lo que eliminaría
- Crear colecciones "Próximamente a eliminar" (estas siempre se crean)
- NO eliminar ningún archivo o metadato

Es perfecto para probar y entender lo que hará Janitorr.

### ¿Cómo evito que medios específicos sean eliminados?

Usa etiquetas en Sonarr/Radarr:
1. En Sonarr/Radarr, crea una etiqueta llamada `janitorr_keep` (o personalízala en tu configuración)
2. Aplica esta etiqueta a cualquier película/serie que quieras mantener permanentemente
3. Janitorr omitirá cualquier medio con esta etiqueta

### ¿Cómo determina Janitorr qué eliminar?

Janitorr usa múltiples factores:
1. **Edad:** Basado en cuándo fue descargado (del historial *arr)
2. **Historial de visualización:** Si Jellystat/Streamystats está configurado, la fecha de visualización más reciente reemplaza la fecha de descarga
3. **Espacio en disco:** Si está configurado, Janitorr solo elimina cuando el uso del disco excede un umbral
4. **Etiquetas:** Los medios con la etiqueta de exclusión configurada nunca se eliminan

### ¿Cuáles son las diferentes programaciones de limpieza?

Janitorr tiene tres programaciones de limpieza:

1. **Limpieza de Medios** - Elimina películas y series basándose en edad y espacio en disco
2. **Limpieza Basada en Etiquetas** - Elimina medios basándose en etiquetas configuradas y tiempos de expiración
3. **Limpieza de Episodios** - Elimina episodios individuales basándose en edad o cantidad máxima (para series semanales)

Cada una puede habilitarse/deshabilitarse y programarse independientemente.

## Preguntas de Operación

### ¿Qué es la colección "Próximamente a eliminar"?

La colección "Próximamente a eliminar" se muestra en Jellyfin/Emby antes de que los medios sean eliminados. Esto:
- Da a los usuarios una advertencia de que el contenido será eliminado pronto
- Se crea mediante enlaces simbólicos de archivos de tu biblioteca
- Aparece en la página de inicio de Jellyfin
- Puede configurarse para mostrarse durante X días antes de la eliminación

**Importante:** Esta colección SIEMPRE se crea, incluso en modo dry-run.

### ¿Cómo ejecuto manualmente una limpieza?

Usa la Interfaz de Gestión:
1. Navega a `http://<tu-servidor>:8978/`
2. Haz clic en el botón de la limpieza que quieres ejecutar
3. Monitorea el estado y los logs

### ¿Por qué la Interfaz de Gestión devuelve errores 404?

**Problema:** Al acceder a `/api/management/status` o la Interfaz de Gestión, obtienes errores 404.

**Causa Más Común:** El perfil `leyden` está activo en tiempo de ejecución.

**Solución:**
1. Verifica tu docker-compose.yml para `SPRING_PROFILES_ACTIVE`
2. Elimina `leyden` de las variables de entorno:
   ```yaml
   environment:
     # INCORRECTO - NO hagas esto:
     # - SPRING_PROFILES_ACTIVE=leyden
     
     # CORRECTO - Omítelo completamente, o configura perfiles personalizados:
     - SPRING_PROFILES_ACTIVE=prod,custom  # ¡Sin leyden!
   ```
3. Reinicia el contenedor
4. La Interfaz de Gestión ahora debería ser accesible

**¿Por qué?** El perfil `leyden` es solo para generación de caché AOT en tiempo de compilación. Cuando está activo en tiempo de ejecución, deshabilita el ManagementController para reducir el tiempo de inicio de la aplicación durante las compilaciones.

### ¿Puedo ejecutar Janitorr una vez y salir?

¡Sí! Configura `run-once: true` en tu application.yml. Janitorr:
1. Realizará todas las limpiezas habilitadas
2. Saldrá automáticamente
3. Esto es útil para trabajos cron o ejecuciones manuales

### ¿Elimina Janitorr después de ver?

**No.** Janitorr NO elimina medios basándose únicamente en el estado de visualización.

Para esa funcionalidad, consulta [Jellyfin Media Cleaner](https://github.com/shemanaev/jellyfin-plugin-media-cleaner).

Janitorr elimina basándose en:
- Edad (días desde la descarga o última visualización)
- Umbrales de espacio en disco
- Programaciones basadas en etiquetas

## Preguntas de Solución de Problemas

### ¿Por qué no se eliminan los archivos?

Razones comunes:
1. **Modo dry-run habilitado** - Configura `dry-run: false`
2. **Los medios no cumplen requisitos de edad** - Verifica tus días mínimos configurados
3. **Umbral de espacio en disco no alcanzado** - Si tienes eliminación consciente del espacio en disco habilitada
4. **Los medios tienen etiqueta de exclusión** - Verifica si los medios tienen tu etiqueta de conservación configurada
5. **Los medios no fueron descargados por *arr** - Janitorr solo gestiona medios descargados a través de Sonarr/Radarr

### ¿Por qué Janitorr no puede crear enlaces simbólicos?

Razones:
1. **Desajuste en mapeo de volúmenes** - Asegúrate de que todos los contenedores vean las mismas rutas
2. **Problemas de permisos** - El usuario necesita acceso de escritura al directorio leaving-soon
3. **Limitaciones del sistema de archivos** - Algunos sistemas de archivos no soportan enlaces simbólicos (raro con Linux)

Solución: Revisa los logs y verifica que los mapeos de volúmenes sean consistentes.

### ¿Por qué veo errores de "Path not found"?

Esto generalmente significa:
- La aplicación *arr reporta una ruta que Janitorr no puede acceder
- Los mapeos de volúmenes son diferentes entre contenedores

Ejemplo del problema:
- Radarr ve películas en `/movies`
- Janitorr las ve en `/data/movies`

Solución: Usa el mismo mapeo de volumen para todos los contenedores.

### ¿Cómo actualizo Janitorr?

```bash
cd /ruta/a/docker-compose.yml
docker-compose pull
docker-compose up -d
```

Esto:
1. Descargará la última imagen
2. Recreará el contenedor con la nueva imagen
3. Preservará tu configuración y logs

## Preguntas Avanzadas

### ¿Puedo usar Janitorr con Plex?

No, Janitorr está diseñado solo para Jellyfin y Emby. Para Plex, considera usar [Maintainerr](https://github.com/jorenn92/Maintainerr), que inspiró a Janitorr.

### ¿Puedo usar múltiples instancias de Sonarr o Radarr?

Sí, configura múltiples instancias *arr en tu application.yml. Janitorr gestionará medios de todas las instancias configuradas.

### ¿Cuál es la diferencia entre imágenes JVM y nativas?

- **Imagen JVM** (`jvm-stable`):
  - Recomendada
  - Mejor soportada
  - Uso de memoria ligeramente superior (~256MB)
  - Mejor rendimiento a largo plazo debido a optimización JIT

- **Imagen Nativa** (`native-stable`):
  - Obsoleta desde v1.9.0
  - Menor huella inicial de memoria
  - Inicio más rápido
  - Soporte limitado en el futuro

### ¿Puedo personalizar las programaciones de limpieza?

Sí, en application.yml puedes configurar:
- Expresiones cron para cada programación
- Habilitar/deshabilitar cada tipo de limpieza
- Establecer diferentes umbrales de edad
- Configurar umbrales de espacio en disco

### ¿Cómo integro con Jellystat o Streamystats?

Configura los detalles de conexión en application.yml. Cuando esté conectado, Janitorr:
- Usará el historial de visualización para determinar la edad de los medios
- Preferirá la fecha de visualización más reciente sobre la fecha de descarga
- Esto evita la eliminación de medios vistos recientemente

**Nota:** Solo se puede habilitar un servicio de estadísticas (Jellystat O Streamystats) a la vez.

## Características de IA/ML

### ¿Qué es el Motor de Inteligencia IA/ML?

El Motor de Inteligencia IA/ML es una **característica futura** que utilizará aprendizaje automático para tomar decisiones de limpieza más inteligentes basadas en patrones de visualización y comportamiento del usuario. Actualmente está en fase de planificación y arquitectura.

### ¿La IA está habilitada por defecto?

No. Todas las características de IA están **deshabilitadas por defecto** y no están implementadas aún. La configuración existe en el código base como placeholder para desarrollo futuro.

### ¿Cuándo estarán disponibles las características de IA?

Esta es una característica compleja a largo plazo con un cronograma estimado de 12-16 meses para implementación completa:
- **Fase 1:** Recopilación de datos (2-3 meses)
- **Fase 2:** Modelos ML centrales (3-4 meses)
- **Fase 3:** Características de inteligencia (2-3 meses)
- **Fase 4:** Integración de UI (2 meses)
- **Fase 5:** Características avanzadas (3-4 meses)

Sigue el progreso en [GitHub Issues](https://github.com/carcheky/janitorr/issues).

### ¿Qué harán las características de IA?

Cuando se implementen, el motor de IA:
- Analizará el historial de visualización para predecir qué medios conservar
- Aprenderá de tus decisiones de limpieza y preferencias
- Proporcionará recomendaciones inteligentes con explicaciones
- Optimizará el momento de limpieza basándose en patrones de uso
- Detectará maratones y patrones de visualización estacionales
- Pronosticará necesidades de almacenamiento

### ¿Las características de IA compartirán mis datos externamente?

No. El motor de IA está diseñado con la privacidad en mente:
- **Todo el procesamiento es local** - no se envían datos a servicios externos
- **Los IDs de usuario se anonimizan** antes del procesamiento ML
- **La retención de datos es limitada** - datos de entrenamiento purgados después de 90 días
- **Fácil opt-out** - la IA puede deshabilitarse completamente
- **Decisiones transparentes** - todas las recomendaciones incluyen explicaciones

### ¿Cómo puedo contribuir al desarrollo de IA?

Puedes contribuir:
- Revisando la documentación de arquitectura: [Arquitectura del Motor IA/ML](../../ARQUITECTURA_MOTOR_IA_ML.md)
- Proporcionando retroalimentación sobre requisitos de características
- Sugiriendo algoritmos ML y enfoques
- Probando cuando estén disponibles versiones alpha/beta

### ¿Dónde puedo aprender más sobre la arquitectura de IA?

Consulta la documentación detallada de arquitectura:
- **Inglés:** [AI/ML Engine Architecture](../../AI_ML_ENGINE_ARCHITECTURE.md)
- **Español:** [Arquitectura del Motor IA/ML](../../ARQUITECTURA_MOTOR_IA_ML.md)

## ¿Aún tienes preguntas?

- Revisa la guía de [Solución de Problemas](Solucion-Problemas.md)
- Consulta la [Guía de Configuración](Guia-Configuracion.md)
- Busca en [problemas existentes](https://github.com/carcheky/janitorr/issues)
- Inicia una [discusión](https://github.com/carcheky/janitorr/discussions)

---

[← Volver al Inicio](Home.md)
