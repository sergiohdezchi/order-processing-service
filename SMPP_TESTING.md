# Testing SMPP Integration

## Paso 8 - Integración de Cloudhopper SMPP Completado

### 📋 Resumen

La librería **Cloudhopper SMPP** ha sido integrada para el envío de SMS cuando un pedido termina de procesarse.

### 🔧 Componentes Implementados

#### 1. **SmppProperties** (`config/SmppProperties.java`)
Configuración SMPP desde `application.yml`:
- Host y puerto del servidor SMPP
- Credenciales (systemId, password)
- Direcciones de origen y destino (TON/NPI)
- Timeouts y configuración de ventana
- Flag `enabled` para activar/desactivar

#### 2. **SmppClientService** (`service/SmppClientService.java`)
Cliente SMPP con las siguientes características:
- ✅ Conexión automática al servidor SMPP al iniciar
- ✅ Reconexión automática en caso de fallo
- ✅ Método `sendSms(destinationNumber, message)` genérico
- ✅ Método `sendOrderProcessedNotification(orderId, phoneNumber)` específico
- ✅ Manejo de errores robusto
- ✅ Logging detallado de eventos
- ✅ Cierre limpio de sesión al apagar la aplicación
- ✅ Modo degradado: Si SMPP no está disponible, la app sigue funcionando

#### 3. **OrderProcessingActor Actualizado**
El actor ahora:
- ✅ Recibe `SmppClientService` en el constructor
- ✅ Envía SMS después de procesar el pedido exitosamente
- ✅ Mensaje del SMS: `"Your order {orderId} has been processed"`
- ✅ SMS se envía ANTES de responder al gRPC (según el flujo)

#### 4. **OrderMessages Actualizado**
- ✅ `OrderSaved` ahora incluye `customerPhone`
- ✅ Permite al actor enviar SMS al número correcto

### 📝 Configuración en `application.yml`

```yaml
# SMPP configuration
smpp:
  host: localhost                    # Host del servidor SMPP
  port: 2775                        # Puerto estándar SMPP
  systemId: testuser                # Usuario SMPP
  password: testpass                # Contraseña SMPP
  systemType: ""                    # Tipo de sistema (opcional)
  sourceAddress: "1234"             # Número origen
  sourceAddressTon: 1               # Type of Number (Internacional)
  sourceAddressNpi: 1               # Numbering Plan Indicator
  destAddressTon: 1                 # TON destino
  destAddressNpi: 1                 # NPI destino
  requestExpiryTimeout: 30000       # 30 segundos
  windowMonitorInterval: 15000      # 15 segundos
  windowSize: 1                     # Ventana de mensajes
  connectTimeout: 10000             # 10 segundos
  bindTimeout: 5000                 # 5 segundos
  enabled: true                     # Activar/desactivar SMPP
```

### 🔄 Flujo Completo con SMS

```
1. Cliente gRPC envía CreateOrderRequest
         ↓
2. OrderGrpcService recibe y convierte items
         ↓
3. ActorService envía mensaje ProcessOrder al Actor
         ↓
4. OrderProcessingActor procesa el pedido
         ↓
5. OrderService guarda en MongoDB (estado: PENDING)
         ↓
6. Actor recibe OrderSaved con customerPhone
         ↓
7. Actor actualiza estado a PROCESSING en MongoDB
         ↓
8. 🆕 Actor llama SmppClientService.sendOrderProcessedNotification()
         ↓
9. SmppClientService envía SMS vía SMPP
         ↓
10. Actor envía respuesta gRPC al cliente
         ↓
11. Cliente recibe respuesta
```

### 🧪 Pruebas

#### Opción 1: Con Servidor SMPP Real

Si tienes un servidor SMPP corriendo (ej: SMSC Simulator):

```bash
# Actualizar application.yml con credenciales reales
smpp:
  host: your-smpp-server.com
  port: 2775
  systemId: your-username
  password: your-password
  sourceAddress: "1234567890"
  enabled: true

# Iniciar aplicación
./gradlew bootRun

# Enviar pedido con cliente gRPC
# Verificar que el SMS se envíe al customerPhone
```

#### Opción 2: Sin Servidor SMPP (Modo Simulado)

La aplicación funciona sin servidor SMPP disponible:

```bash
# Configurar SMPP como enabled pero con servidor inexistente
smpp:
  host: localhost
  port: 2775
  enabled: true

# Iniciar aplicación
./gradlew bootRun

# Verás en los logs:
# WARN - Could not bind SMPP session: ... Will operate without SMS capability.
# WARN - Order notification SMS could not be sent for order ORD-xxx

# La aplicación sigue funcionando normalmente
# Los pedidos se procesan pero sin enviar SMS
```

#### Opción 3: SMPP Deshabilitado

```yaml
smpp:
  enabled: false
```

```bash
# Logs mostrarán:
# INFO - SMPP client is disabled
# INFO - SMPP is disabled. SMS not sent to +52-999-888-7777: Your order ORD-xxx has been processed
```

### 📱 Formato del SMS

```
Your order ORD-1729398765432 has been processed
```

- Longitud: ~45 caracteres
- Codificación: GSM 7-bit
- Sin caracteres especiales para máxima compatibilidad

### 🔍 Logs Esperados

**Cuando SMPP está habilitado y funcionando:**
```
INFO - SMPP client created successfully
INFO - SMPP session bound successfully to localhost:2775
INFO - Sending order notification SMS to +52-999-888-7777: Your order ORD-123 has been processed
INFO - SMS sent successfully to +52-999-888-7777. Message ID: msg-abc-123
INFO - Order notification SMS sent successfully for order ORD-123
```

**Cuando SMPP no está disponible:**
```
WARN - Could not bind SMPP session: Connection refused. Will operate without SMS capability.
INFO - Sending order notification SMS to +52-999-888-7777: Your order ORD-123 has been processed
WARN - SMPP session is not bound. Attempting to reconnect...
ERROR - Cannot send SMS, SMPP session is not available
WARN - Order notification SMS could not be sent for order ORD-123
```

### 🛠️ Testing con SMSC Simulator

Para probar localmente, puedes usar un simulador SMSC:

```bash
# Opción 1: Docker
docker run -d -p 2775:2775 --name smsc-simulator \
  sidhantpanda/smsc-simulator

# Opción 2: Descarga manual
# https://github.com/smn/txssmi
# O cualquier simulador SMPP compatible
```

Configurar:
```yaml
smpp:
  host: localhost
  port: 2775
  systemId: smppclient1
  password: password
  enabled: true
```

### ✅ Verificación

1. **Verificar cliente gRPC**:
```bash
# Ejecutar cliente
./gradlew run --args="com.hacom.telecom.order_processing_service.grpc.OrderGrpcClient"

# Observar logs del servidor para SMS
```

2. **Verificar en MongoDB**:
```bash
mongosh
use exampleDb
db.orders.find().pretty()

# Verificar que el pedido tenga estado "PROCESSING"
# Y que customerPhoneNumber esté presente
```

3. **Verificar logs del Actor**:
```
Processing order: ORD-xxx
Order saved successfully: ORD-xxx
Sending SMS notification for order: ORD-xxx
Sending order notification SMS to +52-999-888-7777: Your order ORD-xxx has been processed
Response sent successfully for order: ORD-xxx
```

### 🎯 Requisitos Cumplidos

✅ **Integrar librería Cloudhopper SMPP** - Versión 5.0.9
✅ **Crear cliente SMPP** - `SmppClientService.java`
✅ **Enviar SMS con texto específico** - `"Your order {orderId} has been processed"`
✅ **Enviar cuando el actor termina** - En método `handleOrderSaved()`

### 📦 Dependencias Agregadas

```gradle
implementation 'com.cloudhopper:ch-smpp:5.0.9'
implementation 'com.cloudhopper:ch-commons-charset:3.0.2'
```

### 🚀 Características Adicionales

- **Tolerancia a fallos**: Si SMPP no está disponible, la app sigue funcionando
- **Reconexión automática**: Intenta reconectar en cada envío
- **Configuración flexible**: Todo configurable desde `application.yml`
- **Logging completo**: Trazabilidad de cada SMS enviado
- **Producción ready**: Manejo de timeouts y errores de red

---

**¡Paso 8 completado exitosamente!** 📱 El sistema ahora envía SMS automáticamente cuando un pedido es procesado.
