# Motor de Reglas - Constructor Visual de Reglas

El Motor de Reglas proporciona una interfaz visual poderosa para crear reglas de limpieza personalizadas sin programación. Construye lógica de limpieza compleja usando bloques arrastrables para condiciones y acciones.

## 📖 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Inicio Rápido](#inicio-rápido)
- [Configuración](#configuración)
- [Usando el Constructor de Reglas](#usando-el-constructor-de-reglas)
- [Tipos de Condiciones](#tipos-de-condiciones)
- [Tipos de Acciones](#tipos-de-acciones)
- [Ejemplos de Reglas](#ejemplos-de-reglas)
- [Referencia de API](#referencia-de-api)
- [Solución de Problemas](#solución-de-problemas)

## Descripción General

El Motor de Reglas extiende las capacidades de limpieza de Janitorr permitiéndote crear reglas personalizadas con:

- **Interfaz Visual Drag-and-Drop** - No requiere programación
- **Condiciones Flexibles** - Edad de medios, tamaño, rating, uso de disco, etiquetas y más
- **Acciones Poderosas** - Eliminar, etiquetar, registrar, notificar y más
- **Operadores Lógicos** - Combina condiciones con lógica AND/OR
- **Modo de Vista Previa** - Prueba reglas antes de ejecutarlas
- **Soporte Dry-Run** - Simula la ejecución de reglas de forma segura

## Inicio Rápido

### 1. Habilitar el Motor de Reglas

Agrega a tu `application.yml`:

```yaml
rule-engine:
  enabled: true
  rules-directory: "/config/rules"
```

### 2. Acceder al Constructor de Reglas

Navega a la Interfaz de Gestión y haz clic en **🧩 Rule Builder** o ve directamente a:

```
http://tu-host-janitorr:8978/rule-builder.html
```

### 3. Crear tu Primera Regla

1. **Nombra tu regla** en el campo superior
2. **Arrastra condiciones** desde la caja de herramientas izquierda al área IF
3. **Configura cada condición** con los valores deseados
4. **Arrastra acciones** al área THEN
5. **Haz clic en Guardar** para persistir tu regla
6. **Haz clic en Preview** para ver qué medios coincidirían
7. **Haz clic en Test** para ejecutar en modo dry-run

## Configuración

### Configuración Básica

```yaml
rule-engine:
  enabled: false # Configura en true para habilitar el motor de reglas
  rules-directory: "/config/rules" # Dónde se almacenan las reglas
  max-rules-per-execution: 100 # Limita ejecución concurrente de reglas
  enable-scheduled-rules: false # Habilita ejecución automática de reglas
```

### Variables de Entorno Docker

```bash
RULE_ENGINE_ENABLED=true
RULE_ENGINE_RULES_DIRECTORY=/config/rules
```

### Montajes de Volumen

Asegura que tu directorio de reglas sea persistente:

```yaml
volumes:
  - ./config/rules:/config/rules
```

## Usando el Constructor de Reglas

### Diseño de la Interfaz

El Constructor de Reglas tiene tres áreas principales:

1. **Caja de Herramientas (Izquierda)** - Condiciones y acciones disponibles
2. **Lienzo (Centro)** - Construye tu regla con bloques IF/THEN
3. **Lista de Reglas (Derecha)** - Biblioteca de reglas guardadas

### Construyendo una Regla

#### Paso 1: Nombra tu Regla

Dale a tu regla un nombre descriptivo que explique qué hace.

```
Ejemplo: "Eliminar películas antiguas con baja calificación cuando el disco está lleno"
```

#### Paso 2: Elegir Operador Lógico

- **AND** - Todas las condiciones deben ser verdaderas (predeterminado)
- **OR** - Cualquier condición puede ser verdadera

#### Paso 3: Agregar Condiciones

Arrastra bloques de condición desde la caja de herramientas al área IF:

- **📅 Edad de Medios** - Filtrar por antigüedad de los medios
- **💾 Tamaño de Archivo** - Filtrar por tamaño de archivo en GB
- **⭐ Rating** - Filtrar por puntuación
- **📊 Uso de Disco** - Filtrar basado en espacio en disco
- **▶️ Contador de Reproducciones** - Filtrar por historial de visualización
- **🏷️ Etiqueta** - Filtrar por etiquetas de Sonarr/Radarr

#### Paso 4: Configurar Condiciones

Haz clic en cada condición para establecer:
- **Operador** - mayor que, menor que, igual a, etc.
- **Valor** - El umbral para la comparación

#### Paso 5: Agregar Acciones

Arrastra bloques de acción al área THEN:

- **🗑️ Eliminar Archivo** - Remover medios
- **📝 Registrar Acción** - Registrar información
- **➕ Agregar Etiqueta** - Etiquetar medios en servicios *arr
- **🔔 Notificar** - Enviar notificaciones

#### Paso 6: Guardar y Probar

- **Validar** - Verificar si la regla está configurada correctamente
- **Vista Previa** - Ver qué elementos de medios coinciden
- **Probar** - Ejecutar en modo dry-run
- **Guardar** - Persistir la regla para uso posterior

## Tipos de Condiciones

### Edad de Medios

Filtra medios basado en tiempo desde descarga o última visualización.

**Configuración:**
- Operador: `más antiguo que`, `más reciente que`, `exactamente`
- Valor: Número de días

**Ejemplo:** "más antiguo que 90 días"

### Tamaño de Archivo

Filtra medios basado en tamaño de archivo.

**Configuración:**
- Operador: `más grande que`, `más pequeño que`, `exactamente`
- Valor: Tamaño en GB

**Ejemplo:** "más grande que 10 GB"

### Rating

Filtra basado en calificación de medios (cuando esté disponible).

**Configuración:**
- Operador: `por debajo`, `por encima`, `igual a`
- Valor: Calificación de 0-10

**Ejemplo:** "por debajo de 6.0"

### Uso de Disco

Filtra basado en uso actual de espacio en disco.

**Configuración:**
- Operador: `por encima`, `por debajo`
- Valor: Porcentaje (0-100)

**Ejemplo:** "por encima de 85%"

### Contador de Reproducciones

Filtra basado en cuántas veces se han visto los medios.

**Configuración:**
- Operador: `igual a`, `menos que`, `más que`
- Valor: Número de reproducciones

**Ejemplo:** "igual a 0 reproducciones" (nunca visto)

### Etiqueta

Filtra basado en etiquetas en Sonarr/Radarr.

**Configuración:**
- Operador: `tiene etiqueta`, `no tiene etiqueta`
- Valor: Nombre de etiqueta

**Ejemplo:** "tiene etiqueta 'janitorr_keep'"

## Tipos de Acciones

### Eliminar Archivo

Remueve el archivo de medios y opcionalmente del servidor de medios.

**Opciones:**
- Remover del servidor de medios: Sí/No

### Registrar Acción

Registra información sobre la ejecución de la regla.

**Configuración:**
- Nivel: INFO, WARN, DEBUG, ERROR
- Mensaje: Mensaje de registro personalizado

### Agregar Etiqueta

Agrega una etiqueta a los medios en Sonarr/Radarr.

**Configuración:**
- Nombre de etiqueta: La etiqueta a agregar

### Notificar

Envía una notificación (requiere configuración de webhook de Discord).

**Configuración:**
- Mensaje: Texto de notificación

## Ejemplos de Reglas

### Ejemplo 1: Limpiar Películas Antiguas No Vistas

**Objetivo:** Eliminar películas de más de 90 días que nunca han sido vistas cuando el disco está sobre el 80% lleno.

**Condiciones:**
- Edad de Medios: más antiguo que 90 días
- Contador de Reproducciones: igual a 0 reproducciones
- Uso de Disco: por encima de 80%
- Lógica: AND

**Acciones:**
- Eliminar Archivo
- Registrar Acción: "Película no vista eliminada"

### Ejemplo 2: Etiquetar Contenido con Baja Calificación

**Objetivo:** Etiquetar medios con calificación por debajo de 6.0 para revisión.

**Condiciones:**
- Rating: por debajo de 6.0
- Lógica: AND

**Acciones:**
- Agregar Etiqueta: "baja_calificacion"
- Registrar Acción: "Medios con baja calificación etiquetados"

### Ejemplo 3: Archivar Archivos Grandes 4K

**Objetivo:** Marcar archivos muy grandes para revisión manual.

**Condiciones:**
- Tamaño de Archivo: más grande que 50 GB
- Lógica: AND

**Acciones:**
- Agregar Etiqueta: "revisar_tamano"
- Notificar: "Archivo grande detectado"

## Referencia de API

El Motor de Reglas proporciona endpoints de API REST para acceso programático.

### Obtener Todas las Reglas

```http
GET /api/rules
```

**Respuesta:**
```json
[
  {
    "id": "rule_123",
    "name": "Eliminar películas antiguas",
    "enabled": true,
    "conditions": [...],
    "actions": [...],
    "logicOperator": "AND"
  }
]
```

### Crear Regla

```http
POST /api/rules
Content-Type: application/json

{
  "id": "rule_123",
  "name": "Mi Regla Personalizada",
  "enabled": true,
  "conditions": [...],
  "actions": [...],
  "logicOperator": "AND"
}
```

### Vista Previa de Regla

```http
POST /api/rules/{id}/preview?type=movies
```

Devuelve lista de elementos de medios que coinciden con la regla.

### Ejecutar Regla

```http
POST /api/rules/{id}/execute?dryRun=true
```

Ejecuta la regla. Establece `dryRun=false` para realizar eliminaciones reales.

### Validar Regla

```http
POST /api/rules/{id}/validate
```

Verifica si la configuración de la regla es válida.

## Solución de Problemas

### Constructor de Reglas No Carga

**Problema:** Error 404 al acceder al constructor de reglas

**Soluciones:**
1. Verifica que la Interfaz de Gestión esté habilitada
2. Verifica que el motor de reglas esté habilitado en la configuración
3. Asegura que el perfil `leyden` no esté activo en tiempo de ejecución

### Las Reglas No se Ejecutan

**Problema:** Las reglas no se ejecutan o no tienen efecto

**Soluciones:**
1. Verifica que la regla esté habilitada (interruptor en lista de reglas)
2. Verifica que las condiciones estén configuradas correctamente
3. Prueba primero con modo Vista Previa
4. Revisa los logs para mensajes de error
5. Asegura que el modo dry-run esté deshabilitado para ejecución real

### Directorio de Reglas No Encontrado

**Problema:** Error al guardar reglas

**Soluciones:**
1. Verifica que el directorio de reglas existe: `/config/rules`
2. Verifica el montaje de volumen en docker-compose
3. Asegura que el directorio tenga permisos de escritura

### Ningún Medio Coincide con las Reglas

**Problema:** Vista previa muestra 0 coincidencias

**Soluciones:**
1. Verifica que los valores de condición sean realistas
2. Verifica el operador lógico (AND vs OR)
3. Prueba primero con condiciones más simples
4. Verifica que existan medios en Sonarr/Radarr

## Mejores Prácticas

1. **Comienza con dry-run** - Siempre prueba reglas en modo dry-run primero
2. **Usa Vista Previa** - Verifica qué medios coinciden antes de ejecutar
3. **Valida reglas** - Usa el botón validar antes de guardar
4. **Nombres descriptivos** - Usa nombres claros para fácil identificación
5. **Primero lo simple** - Comienza con reglas simples y agrega complejidad gradualmente
6. **Respalda reglas** - Exporta el directorio de reglas periódicamente
7. **Etiqueta en lugar de eliminar** - Considera etiquetar para revisión manual antes de eliminar

## Ver También

- [Guía de Configuración](Guia-Configuracion.md)
- [Solución de Problemas](Solucion-Problemas.md)
- [Preguntas Frecuentes](Preguntas-Frecuentes.md)
