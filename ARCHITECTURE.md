# Payment Authorization App — Documentación Técnica

## 1. Arquitectura

El proyecto sigue **Clean Architecture** con 3 capas separadas por paquetes, usando **MVVM** en la capa de presentación.

```
com.example.example/
├── data/            ← Capa de datos (API, BD, repositorio)
│   ├── local/       ← Room (Entity, DAO, Database, Enum)
│   ├── remote/      ← Retrofit (DTOs, Service, Constants)
│   └── repository/  ← Implementación del repositorio + Mappers
├── di/              ← Módulos de inyección Hilt
├── domain/          ← Capa de dominio (modelos, interfaces, use cases)
│   ├── model/       ← Modelos de dominio
│   ├── repository/  ← Interfaz del repositorio
│   └── usecase/     ← Casos de uso
├── presentation/    ← Capa de presentación (Compose + ViewModel)
├── ExampleApp.kt    ← Application con @HiltAndroidApp
└── MainActivity.kt  ← Activity con @AndroidEntryPoint
```

### Flujo de datos

```
UI (Compose) → ViewModel → UseCase → Repository → API / Room
                                          ↓
                                      Mappers (DTO ↔ Domain ↔ Entity)
```

---

## 2. Capa de Datos (`data/`)

### 2.1 Local — Room

| Archivo | Descripción                                                                     |
|---|---------------------------------------------------------------------------------|
| `AppDatabase.kt` | Base de datos Room v4, entity: `TransactionEntity`                              |
| `TransactionEntity.kt` | Tabla `transactions` con 11 campos (incluye campos de anulación nullable)       |
| `TransactionDao.kt` | DAO con operaciones: `getAll()`, `insert()`, `updateAnnulment()`, `deleteAll()` |
| `TransactionType.kt` | Enum con `VENTA("Venta aprobada")` y `ANULACION("Anulación aprobada")`          |

Campos de `TransactionEntity`:
- `id` (PK autoGenerate), `receiptId`, `statusCode`, `statusDescription`, `hexData`, `amount`, `timestamp`
- Anulación (nullable): `annulmentTimestamp`, `annulmentStatusCode`, `annulmentStatusDescription`
- `transactionStatus` (default: `"Venta aprobada"`)

### 2.2 Remote — Retrofit

| Archivo | Descripción |
|---|---|
| `PaymentApiService.kt` | Interface Retrofit con 2 endpoints POST |
| `ApiConstants.kt` | Base URL y header de autorización |

Endpoints:
- `POST api/payments/authorization` → Envía `AuthorizationRequestDto(amount)`, retorna `AuthorizationResponseDto`
- `POST /api/payments/annulment` → Envía `VoidRequestDto(receiptId)`, retorna `AuthorizationResponseDto`

DTOs:
- `AuthorizationRequestDto` — `{ "amount": "999929" }`
- `AuthorizationResponseDto` — `{ "receiptId", "statusCode", "statusDescription", "hexData" }` (todos nullable)
- `VoidRequestDto` — `{ "receiptId": "..." }`

### 2.3 Repository

| Archivo | Descripción |
|---|---|
| `PaymentRepositoryImpl.kt` | Implementación que coordina API + DAO + masking |
| `Mappers.kt` | Funciones de extensión: `toDomain()`, `toEntity()` |

Lógica clave en `authorize()`:
1. Llama al API
2. Decodifica hexData y lo enmascara
3. Solo inserta en BD si `statusCode == "00"`
4. Retorna respuesta con hexData enmascarado

Clasificación de errores en `mapException()`:
- `JsonSyntaxException` / `MalformedJsonException` → `"Respuesta sin datos"`
- `IOException` → `"Error de red"`
- Otros → mensaje original

---

## 3. Capa de Dominio (`domain/`)

### 3.1 Modelos

| Modelo | Campos |
|---|---|
| `AuthorizationResponse` | `receiptId`, `statusCode`, `statusDescription`, `hexData` |
| `Transaction` | 11 campos (incluye `annulmentTimestamp`, `annulmentStatusCode`, `annulmentStatusDescription`, `transactionStatus`) |
| `AuthorizationRequest` | `amount` |

### 3.2 Interfaces

| Interface | Descripción |
|---|---|
| `PaymentRepository` | Contrato: `authorize()`, `voidTransaction()`, `getTransactions()`, `deleteAll()` |
| `HexDecoder` | `fun interface` — `decode(hex: String): String` |
| `StringMasker` | `fun interface` — `mask(value: String): String` |

### 3.3 Use Cases

| Use Case | Función |
|---|---|
| `AuthorizePaymentUseCase` | Delega `repository.authorize(amount)` |
| `VoidTransactionUseCase` | Delega `repository.voidTransaction(id, receiptId)` |
| `GetTransactionsUseCase` | Retorna `Flow<List<Transaction>>` del repositorio |
| `DecodeHexUseCase` | Implementa `HexDecoder` — convierte hex a ASCII |
| `MaskFrameUseCase` | Implementa `StringMasker` — enmascara caracteres centrales con `*` |

---

## 4. Inyección de Dependencias (`di/`)

| Módulo | Scope | Provee |
|---|---|---|
| `DatabaseModule` | `SingletonComponent` | `AppDatabase`, `TransactionDao` |
| `NetworkModule` | `SingletonComponent` | `OkHttpClient`, `Retrofit`, `PaymentApiService` |
| `RepositoryModule` | `SingletonComponent` | Binds: `PaymentRepository`, `StringMasker`, `HexDecoder` |

### Interceptors

| Interceptor | Orden | Función |
|---|---|---|
| `JsonFormattingInterceptor` | 1° | Reemplaza `":"` por `": "` en el body JSON |
| `HttpLoggingInterceptor` | 2° | Log del body HTTP (nivel BODY) |

---

## 5. Capa de Presentación (`presentation/`)

### 5.1 Estado

`AuthorizationStatus` — Sealed interface con 5 estados:

| Estado | Datos | Uso |
|---|---|---|
| `Idle` | — | Estado inicial, no muestra card |
| `Loading` | — | Spinner + "Procesando autorización..." |
| `Success` | `AuthorizationResponse` | Card verde "Aprobada" (statusCode == "00") |
| `Rejected` | `AuthorizationResponse` | Card roja "Rechazada" (statusCode != "00") |
| `NetworkError` | `message: String` | Card naranja "Error de red" |

`PaymentUiState`:

| Campo | Tipo | Default |
|---|---|---|
| `amount` | `String` | `""` |
| `authStatus` | `AuthorizationStatus` | `Idle` |
| `transactions` | `List<Transaction>` | `emptyList()` |
| `showHistory` | `Boolean` | `false` |
| `selectedTransaction` | `Transaction?` | `null` |
| `isVoiding` | `Boolean` | `false` |

### 5.2 ViewModel

`PaymentViewModel` — Acciones:

| Función | Descripción |
|---|---|
| `onAmountChanged(amount)` | Actualiza monto en el state |
| `authorize()` | Llama al use case, clasifica respuesta por statusCode |
| `toggleHistory()` | Alterna visibilidad del histórico |
| `selectTransaction(tx)` | Selecciona transacción para diálogo de anulación |
| `dismissDialog()` | Cierra diálogo |
| `voidTransaction()` | Ejecuta anulación de la transacción seleccionada |

### 5.3 Componentes UI (Compose)

| Composable | Descripción | Parámetros clave |
|---|---|---|
| `PaymentScreen` | Entry point, conecta ViewModel con UI | `viewModel: PaymentViewModel` |
| `PaymentScreenContent` | Stateless, toda la pantalla | `state`, callbacks |
| `AuthorizationForm` | Campo de monto + botón autorizar | `amount`, `isLoading`, `onAmountChanged`, `onAuthorize` |
| `AuthorizationStatusCard` | Card según estado de autorización | `status: AuthorizationStatus` |
| `TransactionCard` | Card de transacción en el histórico | `transaction`, `maskedHexData`, `onClick` |
| `ResponseCard` | Card genérica de respuesta | `receiptId`, `status`, `maskedHexData` |
| `VoidConfirmationDialog` | Diálogo de confirmación de anulación | `transaction`, `isVoiding`, `onConfirm`, `onDismiss` |
| `InfoRow` | Fila label-valor | `label`, `value` |

Validaciones en UI:
- `AuthorizationForm`: regex `[^0-9]` filtra caracteres no numéricos del monto
- `VoidConfirmationDialog`: botón "Anular" deshabilitado si `statusCode != "00"` o ya anulada

### 5.4 Compose Previews

| Preview | Estado |
|---|---|
| `PreviewAuthorizationFormEmpty` | Campo vacío, botón deshabilitado |
| `PreviewAuthorizationFormWithAmount` | Con monto, botón habilitado |
| `PreviewAuthorizationFormLoading` | Loading, botón deshabilitado |
| `PreviewLoading` | Pantalla completa con spinner |
| `PreviewSuccess` | Card verde aprobada |
| `PreviewRejected` | Card roja rechazada |
| `PreviewNetworkError` | Card naranja error de red |
| `PreviewWithHistory` | Con histórico expandido |

---

## 6. Testing

### 6.1 Unit Tests (`src/test/`)

| Test | Clase bajo test | Casos |
|---|---|---|
| `AmountFilterTest` | Regex `[^0-9]` | 5 tests: filtrado de letras, símbolos, espacios, input limpio, vacío |
| `AuthorizePaymentUseCaseTest` | `AuthorizePaymentUseCase` | 2 tests: success y failure por red |
| `GetTransactionsUseCaseTest` | `GetTransactionsUseCase` | 2 tests: lista con datos y lista vacía |
| `MaskFrameUseCaseTest` | `MaskFrameUseCase` | 5 tests: enmascarado real, corto, exacto 8, 16 chars, vacío |

Dependencias de test:
- `JUnit 4`
- `Mockito` + `mockito-kotlin`
- `kotlinx-coroutines-test`
- `Turbine` (testing de Flows)

### 6.2 UI Tests — Instrumented (`src/androidTest/`)

| Test | Composable bajo test | Casos |
|---|---|---|
| `PaymentScreenContentTest` | `PaymentScreenContent` | 12 tests en 4 secciones |

Secciones:
- **AuthorizationForm** (4): campo con valor, botón deshabilitado vacío, botón habilitado con monto, botón loading
- **AuthorizationStatusCard** (4): loading, success, rejected, network error
- **History** (3): botón con conteo, expandido, sin transacciones
- **Idle** (1): título y formulario visibles

Configuración:
- `HiltTestRunner` como `testInstrumentationRunner`
- `@HiltAndroidTest` + `HiltAndroidRule` (order=0) + `createAndroidComposeRule<ComponentActivity>()` (order=1)
- `hiltRule.inject()` en `@Before`

Dependencias de test:
- `compose-ui-test-junit4`
- `compose-ui-test-manifest` (debugImplementation)
- `hilt-android-testing`
- `Espresso`

---

## 7. Dependencias principales

| Categoría | Librería |
|---|---|
| UI | Jetpack Compose (BOM), Material3 |
| Navegación | Compose Navigation |
| Lifecycle | ViewModel Compose, Runtime Compose |
| DI | Hilt + Hilt Navigation Compose |
| BD | Room (runtime, ktx, compiler via KSP) |
| Red | Retrofit + Gson Converter + OkHttp Logging |
| Async | Kotlin Coroutines (core + android) |
| Build | KSP (procesador de anotaciones) |

Configuración:
- `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`
- `jvmTarget = "11"`
- `fallbackToDestructiveMigration()` en Room
