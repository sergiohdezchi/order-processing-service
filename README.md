# Order Processing Service

Sistema de procesamiento de órdenes con arquitectura reactiva, mensajería asíncrona y notificaciones SMS.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![MongoDB](https://img.shields.io/badge/MongoDB-Reactive-green)
![gRPC](https://img.shields.io/badge/gRPC-1.60.0-blue)
![Akka](https://img.shields.io/badge/Akka-2.6.20-red)

## 📋 Descripción

Servicio de procesamiento de órdenes construido con Spring Boot que integra múltiples tecnologías para proporcionar un sistema robusto, escalable y observable:

- **Spring WebFlux**: API REST reactiva en puerto 9898
- **gRPC Server**: Servicio de alta performance en puerto 9090
- **MongoDB Reactive**: Persistencia reactiva de órdenes
- **Akka Actors**: Procesamiento asíncrono con modelo de actores
- **SMPP Client**: Notificaciones SMS usando protocolo SMPP
- **Spring Actuator**: Métricas en formato Prometheus
- **Log4j2**: Logging estructurado con configuración YAML

## 🚀 Tecnologías

### Core
- **Java 17**: Lenguaje de programación
- **Spring Boot 3.5.6**: Framework principal
- **Gradle**: Sistema de construcción

### Reactive Stack
- **Spring WebFlux**: API REST reactiva
- **MongoDB Reactive**: Driver reactivo para MongoDB
- **Project Reactor**: Programación reactiva (Mono/Flux)

### Comunicación
- **gRPC**: RPC de alta performance
- **Protocol Buffers**: Serialización eficiente
- **Cloudhopper SMPP**: Cliente SMPP para SMS

### Procesamiento Asíncrono
- **Akka Actors 2.6.20**: Modelo de actores para concurrencia

### Observabilidad
- **Spring Actuator**: Endpoints de monitoreo
- **Micrometer Prometheus**: Métricas en formato Prometheus
- **Log4j2**: Logging con configuración YAML

## 📦 Requisitos Previos

- **Java 17** o superior
- **MongoDB** 4.x o superior
- **Gradle** 7.x o superior (incluido con wrapper)
- **Python 3** (para simulador SMPP)

## 🔧 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/sergiohdezchi/order-processing-service.git
cd order-processing-service
```

### 2. Instalar MongoDB

#### Ubuntu/Debian
```bash
sudo apt-get install mongodb
sudo systemctl start mongodb
```

#### macOS
```bash
brew install mongodb-community
brew services start mongodb-community
```

#### Docker
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 3. Compilar el proyecto

```bash
./gradlew clean build
```

## ▶️ Ejecución

### Opción 1: Usando Gradle

```bash
./gradlew bootRun
```

### Opción 2: Usando JAR

```bash
./gradlew bootJar
java -jar build/libs/order-processing-service-0.0.1-SNAPSHOT.jar
```

La aplicación iniciará en:
- **REST API**: http://localhost:9898
- **gRPC Server**: localhost:9090
- **Actuator**: http://localhost:9898/actuator

## 📡 Simulador SMPP

Para probar las notificaciones SMS, ejecuta el simulador SMPP incluido:

### Iniciar el simulador

```bash
python3 smpp-simulator.py
```

Salida esperada:
```
============================================================
🚀 SMPP Simulator Iniciado
============================================================
📡 Escuchando en 0.0.0.0:2775
🔑 Acepta cualquier system_id/password
📱 Simula envío de SMS exitoso
============================================================

Esperando conexiones...
```

### Verificar mensajes SMS durante la creación de órdenes

El simulador mostrará los mensajes SMS cuando se procesen órdenes:

```
🔌 Nueva conexión desde ('127.0.0.1', 54321)
📡 BIND Request recibido (command_id: 0x9)
   System ID: testuser
✅ BIND Response enviado
💓 ENQUIRE_LINK recibido
✅ ENQUIRE_LINK_RESP enviado

📨 SUBMIT_SM recibido!
   Tamaño del body: 87 bytes
   📱 Mensaje SMS: 'Your order ORD-001 has been processed'
✅ SUBMIT_SM_RESP enviado (Message ID: MSG00000001)
```

### Puerto personalizado

```bash
python3 smpp-simulator.py 2776
```

## 🧪 Pruebas

### 1. Health Check

```bash
curl http://localhost:9898/actuator/health
```

### 2. Crear orden vía gRPC

#### Opción A: Usando grpcurl (Recomendado)

**Instalar grpcurl:**

```bash
# Ubuntu/Debian
sudo apt-get install grpcurl

# macOS
brew install grpcurl

# O descargar desde GitHub
# https://github.com/fullstorydev/grpcurl/releases
```

**Listar servicios disponibles:**

```bash
grpcurl -plaintext localhost:9090 list
```

Salida esperada:
```
grpc.health.v1.Health
grpc.reflection.v1alpha.ServerReflection
orderservice.OrderService
```

**Describir el servicio OrderService:**

```bash
grpcurl -plaintext localhost:9090 describe orderservice.OrderService
```

**Crear una orden usando grpcurl:**

```bash
grpcurl -plaintext -d '{
  "order_id": "ORD-GRPC-001",
  "customer_id": "CUST-456",
  "customer_phone": "+52-999-888-7777",
  "items": [
    {
      "item_id": "ITEM-1",
      "product_name": "Product A",
      "quantity": 2,
      "price": 99.99
    },
    {
      "item_id": "ITEM-2",
      "product_name": "Product B",
      "quantity": 1,
      "price": 49.99
    }
  ]
}' localhost:9090 orderservice.OrderService/CreateOrder
```

**Respuesta esperada:**

```json
{
  "order_id": "ORD-GRPC-001",
  "status": "PENDING",
  "message": "Order received and will be processed"
}
```

#### Opción B: Usando Python con grpcio

**Instalar dependencias:**
```bash
pip install grpcio grpcio-tools
```

**Crear script `test_grpc.py`:**
```python
import grpc
import sys
sys.path.append('build/generated/source/proto/main/python')

import order_service_pb2
import order_service_pb2_grpc

channel = grpc.insecure_channel('localhost:9090')
stub = order_service_pb2_grpc.OrderServiceStub(channel)

request = order_service_pb2.CreateOrderRequest(
    order_id='ORD-GRPC-002',
    customer_id='CUST-789',
    customer_phone='+52-999-888-7777',
    items=[
        order_service_pb2.OrderItem(
            item_id='ITEM-1',
            product_name='Product A',
            quantity=2,
            price=99.99
        )
    ]
)

response = stub.CreateOrder(request)
print(f"Order ID: {response.order_id}")
print(f"Status: {response.status}")
print(f"Message: {response.message}")
```

**Ejecutar:**
```bash
python3 test_grpc.py
```

#### Opción C: Usando grpc_cli (Herramienta oficial de gRPC)

```bash
# Listar servicios
grpc_cli ls localhost:9090

# Llamar al método
grpc_cli call localhost:9090 orderservice.OrderService.CreateOrder \
  "order_id: 'ORD-GRPC-003' customer_id: 'CUST-999' customer_phone: '+52-999-888-7777' items: { item_id: 'ITEM-1' product_name: 'Product A' quantity: 2 price: 99.99 }"
```

### 3. Consultar estado de orden

```bash
curl http://localhost:9898/api/v1/orders/ORD-001/status
```

### 4. Contar órdenes por rango de fechas

```bash
curl "http://localhost:9898/api/v1/orders/count?startDate=2025-10-01T00:00:00Z&endDate=2025-10-31T23:59:59Z"
```

### 5. Ver métricas de Prometheus

```bash
# Todas las métricas
curl http://localhost:9898/actuator/prometheus

# Filtrar métricas de órdenes
curl http://localhost:9898/actuator/prometheus | grep "orders_"

# Filtrar métricas de SMS
curl http://localhost:9898/actuator/prometheus | grep "sms_"
```

### 6. Ver métrica individual

```bash
curl http://localhost:9898/actuator/metrics/orders.created.total | jq
```

## 📊 Métricas Personalizadas

El servicio expone las siguientes métricas en formato Prometheus:

### Métricas de Órdenes
| Métrica | Tipo | Descripción |
|---------|------|-------------|
| `orders.created.total` | Counter | Total de órdenes creadas exitosamente |
| `orders.duplicate.total` | Counter | Total de órdenes duplicadas detectadas |


### Métricas de SMS
| Métrica | Tipo | Descripción |
|---------|------|-------------|
| `sms.sent.total` | Counter | Total de SMS enviados exitosamente |
| `sms.failed.total` | Counter | Total de SMS que fallaron al enviar |

### Consultas PromQL Útiles

```promql
# Rate de órdenes creadas por segundo (últimos 5 minutos)
rate(orders_total{service="order-processing"}[5m])

# Total de órdenes duplicadas
orders_duplicate_total{service="order-processing"}

# Tasa de éxito de SMS
sms_sent_total / (sms_sent_total + sms_failed_total)

# Latencia promedio de gRPC CreateOrder
rate(grpc_server_processing_duration_seconds_sum{method="CreateOrder"}[5m]) / 
rate(grpc_server_processing_duration_seconds_count{method="CreateOrder"}[5m])
```

## 📝 Logs

Los logs se almacenan en:
- **Consola**: Salida estándar con colores
- **Archivo**: `logs/order-processing-service.log`

### Rotación de logs
- Tamaño máximo: 10 MB por archivo
- Archivos de respaldo: 10 máximo
- Compresión: gzip automática

### Niveles de log configurados
- `com.hacom.telecom`: INFO
- `akka`: INFO
- `org.springframework`: INFO
- `org.mongodb.driver`: INFO

### Ver logs en tiempo real

```bash
tail -f logs/order-processing-service.log
```

## 🔐 Configuración

Todas las configuraciones se encuentran en `src/main/resources/application.yml`:

### MongoDB
```yaml
mongodbDatabase: exampleDb
mongodbUri: "mongodb://127.0.0.1:27017"
```

### Puertos
```yaml
apiPort: 9898  # REST API
grpc:
  server:
    port: 9090  # gRPC Server
```

### SMPP
```yaml
smpp:
  host: localhost
  port: 2775
  systemId: testuser
  password: testpass
  sourceAddress: "1234"
  enabled: true
```

### Actuator
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,loggers,prometheus
```

## 🐛 Troubleshooting

### MongoDB no se conecta

```bash
# Verificar que MongoDB esté corriendo
sudo systemctl status mongodb

# Ver logs de MongoDB
sudo journalctl -u mongodb -f
```

### Puerto 9090 o 9898 ya en uso

```bash
# Encontrar proceso usando el puerto
lsof -i :9090
lsof -i :9898

# Matar el proceso
kill -9 <PID>
```

### SMPP no envía mensajes

1. Asegúrate de que el simulador SMPP esté corriendo
2. Verifica la configuración en `application.yml`:
   - `smpp.host: localhost`
   - `smpp.port: 2775`
   - `smpp.enabled: true`
3. Revisa los logs: `tail -f logs/order-processing-service.log | grep SMPP`

### Métricas no aparecen en Prometheus endpoint

```bash
# Verificar que el endpoint esté habilitado
curl http://localhost:9898/actuator | jq '._links | keys'

# Debe incluir "prometheus" en la lista
```

## 📦 Construcción para Producción

### Generar JAR ejecutable

```bash
./gradlew bootJar
```

El JAR se generará en: `build/libs/order-processing-service-0.0.1-SNAPSHOT.jar`

### Ejecutar en producción

```bash
java -jar build/libs/order-processing-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --mongodbUri=mongodb://prod-server:27017 \
  --smpp.host=prod-smpp-server
```

### Dashboard Grafana

Importar dashboard con métricas clave:
- Panel 1: Orders Created Rate
- Panel 2: SMS Success Rate
- Panel 3: gRPC Latency
- Panel 4: JVM Memory Usage

## 👥 Autores

- **Sergio Hernández** - *Desarrollo Inicial* - [sergiohdezchi](https://github.com/sergiohdezchi)

## 🙏 Agradecimientos

- Hacom Telecom por el proyecto
- Comunidad Spring Boot
- Comunidad Akka
- Cloudhopper SMPP Library

---

**Nota**: Este es un proyecto de demostración que integra múltiples tecnologías para propósitos educativos y de prueba de concepto.
