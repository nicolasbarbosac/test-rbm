# 💳 Payment Authorization App

Aplicación Android para autorización y anulación de pagos con persistencia local y enmascaramiento de datos sensibles.

## Características

- Autorización de pagos vía API REST
- Anulación de transacciones aprobadas
- Histórico de transacciones persistido en Room
- Enmascaramiento automático de datos hexadecimales
- Validación de input (solo numéricos)
- Clasificación de respuestas: Aprobada, Rechazada, Error de red
- Transacciones rechazadas no se almacenan en BD
- Transacciones con `statusCode != "00"` no son anulables

## Stack Tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material3 |
| Arquitectura | Clean Architecture + MVVM |
| DI | Hilt |
| Base de datos | Room |
| Red | Retrofit + OkHttp + Gson |
| Async | Kotlin Coroutines + Flow |
| Testing | JUnit4, Mockito, Turbine, Compose UI Test |

## Requisitos

- Android Studio Ladybug o superior
- JDK 11
- Min SDK 24 / Target SDK 36

## Testing

### Ejecutar unit tests
```bash
./gradlew test
```

### Ejecutar UI tests (requiere dispositivo/emulador)
```bash
./gradlew connectedAndroidTest
```

### Cobertura de tests

**Unit Tests** (`src/test/`) — 14 casos:

| Test | Casos |
|---|---|
| `AmountFilterTest` | 5 — Filtrado regex de caracteres no numéricos |
| `AuthorizePaymentUseCaseTest` | 2 — Success y failure |
| `GetTransactionsUseCaseTest` | 2 — Con datos y vacío |
| `MaskFrameUseCaseTest` | 5 — Enmascarado en distintos tamaños |

**UI Tests** (`src/androidTest/`) — 12 casos:

| Test | Casos |
|---|---|
| `PaymentScreenContentTest` | 12 — Formulario, estados de autorización, histórico, estado idle |

## Documentación Adicional

Ver [ARCHITECTURE.md](ARCHITECTURE.md) para documentación detallada de arquitectura, componentes UI, dependencias y configuración de testing.

## Configuración del Build

```
compileSdk = 36
minSdk = 24
targetSdk = 36
jvmTarget = "11"
```
