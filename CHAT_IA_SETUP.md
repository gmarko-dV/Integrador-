# Configuración del Chat con IA

Este documento explica cómo configurar y usar el chat con IA que recomienda vehículos basándose en las características que el usuario busca.

## Características

- Chat interactivo con IA que pregunta sobre las características deseadas en un vehículo
- Recomendaciones inteligentes de vehículos disponibles en la plataforma
- Interfaz moderna y fácil de usar
- Sistema de fallback que funciona sin API de OpenAI (recomendación básica)

## Configuración

### 1. Configurar API Key de OpenAI (Opcional pero Recomendado)

Para usar la funcionalidad completa de IA, necesitas configurar una API key de OpenAI:

1. Obtén una API key de OpenAI en: https://platform.openai.com/api-keys
2. Agrega la key en `spring-user/src/main/resources/application.properties`:

```properties
openai.api.key=tu-api-key-aqui
openai.api.url=https://api.openai.com/v1/chat/completions
```

**Nota:** Si no configuras la API key, el sistema usará un sistema de recomendación básico que filtra por tipo de vehículo.

### 2. Variables de Entorno (Alternativa)

También puedes configurar la API key como variable de entorno:

```bash
export OPENAI_API_KEY=tu-api-key-aqui
```

Y actualizar el código para leerla desde variables de entorno.

## Uso

### Acceso al Chat

1. Inicia sesión en la aplicación
2. Haz clic en "🤖 Chat IA" en el menú de navegación
3. O navega directamente a `/chat`

### Cómo Funciona

1. El usuario escribe mensajes describiendo las características que busca en un vehículo
2. La IA analiza el mensaje y hace preguntas de seguimiento si es necesario
3. La IA recomienda vehículos disponibles que coinciden con los criterios
4. El usuario puede hacer clic en "Ver Detalles" para ver más información sobre un vehículo recomendado

### Ejemplos de Mensajes

- "Busco un SUV del 2020 en adelante"
- "Quiero un auto económico, máximo 50,000 soles"
- "Necesito un sedán con poco kilometraje"
- "Busco un hatchback para ciudad"

## Arquitectura

### Backend (Spring Boot)

- **ChatController**: Maneja las peticiones HTTP del chat
- **ChatService**: Procesa los mensajes y se comunica con OpenAI
- **DTOs**: 
  - `ChatMessage`: Representa un mensaje en la conversación
  - `ChatRequest`: Request del frontend
  - `ChatResponse`: Response con la respuesta de la IA y recomendaciones

### Frontend (React)

- **ChatIA.js**: Componente principal del chat
- **ChatIA.css**: Estilos del componente
- **chatService.js**: Servicio para comunicarse con el backend

## Endpoints

### POST `/api/chat`

Envía un mensaje al chat de IA.

**Request:**
```json
{
  "message": "Busco un SUV del 2020",
  "conversationHistory": [
    {
      "role": "user",
      "content": "Hola"
    },
    {
      "role": "assistant",
      "content": "¡Hola! ¿Qué características buscas?"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "response": "Encontré varios SUVs del 2020...",
  "recommendedAnuncioIds": [1, 2, 3],
  "hasRecommendations": true
}
```

## Notas Importantes

1. **Costo de OpenAI**: El uso de la API de OpenAI tiene costos. Asegúrate de configurar límites de uso en tu cuenta de OpenAI.

2. **Sistema de Fallback**: Si la API de OpenAI no está disponible o no está configurada, el sistema usa un filtro básico por tipo de vehículo.

3. **Seguridad**: El endpoint de chat está configurado como público (sin autenticación requerida) para facilitar el uso. Si necesitas restringir el acceso, modifica `SecurityConfig.java`.

4. **Rendimiento**: Las llamadas a OpenAI pueden tardar varios segundos. El componente muestra un indicador de "escribiendo" mientras procesa.

## Troubleshooting

### El chat no responde
- Verifica que el backend Spring esté corriendo
- Revisa los logs del backend para ver errores
- Si usas OpenAI, verifica que la API key sea válida

### No se muestran recomendaciones
- Verifica que haya anuncios activos en la base de datos
- Revisa la consola del navegador para errores
- Asegúrate de que el formato de respuesta de la IA incluya los IDs en el formato `[RECOMMEND: id1, id2]`

### Errores de CORS
- Verifica que `SecurityConfig.java` tenga configurado CORS correctamente
- Asegúrate de que el frontend esté en el origen permitido

