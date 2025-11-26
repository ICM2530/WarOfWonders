# Combat System Implementation - War of Wonders

## Overview

Se ha implementado un sistema de combate completo con dos modos principales:
1. **Combate PvP (Jugador vs Jugador)**: Se inicia automáticamente cuando dos usuarios se encuentran.
2. **Combate Territorial (Jugador vs Clan)**: Se inicia cuando un jugador invade el territorio de un clan.

---

## Architecture

### Modelos de Datos

#### `Combat.kt` - Modelos principales

```kotlin
data class Combatant(
    val id: String,
    val name: String,
    val clan: String? = null,
    val level: Int = 1,
    val attack: Int = 10,
    val maxHealth: Int = 100,
    val resources: Int = 0
)

data class CombatResult(
    val winnerId: String,
    val loserId: String,
    val winnerClan: String?,
    val loserClan: String?,
    val resourcesTransferred: Int,
    val rounds: Int
)
```

### Servicios

#### `CombatService.kt`

Implementa la simulación de combate con lógica determinista:

- **Mecánica**: Daño basado en `attack` + aleatorio(0 a level+3) cada ronda
- **Victoria**: El primero que reduce HP del oponente a ≤ 0 gana
- **Transferencia de recursos**: 10% de los recursos del perdedor pasa al ganador

```kotlin
suspend fun fight(attacker: Combatant, defender: Combatant): CombatResult
```

#### `MapViewModel.kt` - Integración

Se añadieron métodos para:

1. **Publicar ubicación**: Cada vez que se actualiza la ubicación del usuario, se publica en `Realtime DB -> user_locations/uid`
2. **Detectar encuentros**: Se consultan ubicaciones de otros jugadores y puntos de interés
3. **Iniciar combates**:
   - `initiatePvPCombat()` - Entre dos jugadores
   - `initiateTerritoryIncursion()` - Jugador vs Clan
4. **Ajustar territorios**: `adjustClanTerritory()` expande/contrae territorio según victoria/derrota

### ViewModels

#### `CombatViewModel.kt`

Gestiona el estado de la UI durante los combates:

```kotlin
data class CombatUIState(
    val attacker: Combatant? = null,
    val defender: Combatant? = null,
    val result: CombatResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

Métodos públicos:
- `startCombat(attacker, defender)` - Inicia el combate
- `resetCombat()` - Limpia el estado
- `getWinner()` / `getLoser()` - Helpers para la UI

---

## Flujo de Combate

### PvP (Jugador vs Jugador)

1. **Detección**: Cuando dos usuarios están a ≤ 20 metros de distancia
2. **Inicio automático**:
   - Se obtienen stats (nivel, coins, clan) de ambos jugadores desde Realtime DB
   - Se crea `Combatant` para cada uno
   - Se ejecuta `combatService.fight()`
3. **Resolución**:
   - Recursos se transfieren del perdedor al ganador (10% de coins)
   - Se actualiza `users/uid/coins` para ambos

### Territorial (Jugador vs Clan)

1. **Detección**: Cuando el jugador está a ≤ 50 metros de un punto de interés (con clanes)
2. **Inicio automático**:
   - Se obtiene info del jugador atacante
   - Se crea un `Combatant` "defensor" del clan basado en su poder
   - Se ejecuta `combatService.fight()`
3. **Resolución**:
   - **Si gana el invasor**:
     - Territorio del clan invasor se expande (+50 metros)
     - Territorio del clan defensor se reduce (-50 metros)
   - **Si gana el defensor**: No hay cambio territorial

---

## Base de Datos (Realtime)

### Estructuras creadas/usadas

```
user_locations/
  {uid}/
    lat: double
    lng: double
    ts: timestamp
    uid: string

users/
  {uid}/
    name: string
    nivel: int
   coins: int
    clan: string (opcional)
    ... otros campos

clans_territory/
  {clanName}/
    centerLat: double
    centerLng: double
    radiusMeters: long
```

---

## UI Integration

### CombatScreen.kt

Se actualiza para mostrar dinámicamente:
- Nombres y niveles de combatientes
- Resultado de la batalla (ganador, recursos transferidos, rondas)
- Botón para volver al mapa

```kotlin
@Composable
fun CombatScreen(combatViewModel: CombatViewModel = viewModel())
```

---

## Pruebas

Se incluye `CombatServiceTest.kt` con casos:

1. **`testCombat_HighLevelAlwaysWins`**: Confirma que stats más altos siempre vencen
2. **`testCombat_ResourceTransfer`**: Verifica que se transfiera el 10% de recursos
3. **`testCombat_BothCanWin`**: Confirma que combates entre iguales tienen resultados variables

```bash
./gradlew test
```

---

## Configuración de Distancias

Edita en `MapViewModel.kt`:

```kotlin
// Distancia PvP
if (dist <= 20.0) { initiatePvPCombat(...) }

// Distancia Territorial
if (distToPoint <= 50.0) { initiateTerritoryIncursion(...) }
```

---

## Consideraciones Futuras

1. **Animaciones**: Animar los ataques/daño en `CombatScreen`
2. **Cooldown**: Implementar espera entre combates para el mismo par de usuarios
3. **Equipamiento**: Integrar armaduras/criaturas como multiplicadores de daño
4. **Persistencia**: Guardar historial de batallas en Firestore
5. **Notificaciones**: Push notifications cuando se inicia un combate
6. **Balanceo**: Ajustar fórmulas de daño y transferencia según gameplay

---

## Cambios Realizados

### Archivos Nuevos
- `app/src/main/java/com/example/warofwonders/data/model/Combat.kt`
- `app/src/main/java/com/example/warofwonders/data/service/CombatService.kt`
- `app/src/main/java/com/example/warofwonders/ui/model/CombatViewModel.kt`
- `app/src/test/java/com/example/warofwonders/CombatServiceTest.kt`

### Archivos Modificados
- `app/src/main/java/com/example/warofwonders/ui/screens/map/MapViewModel.kt` (Integración de combate + detección de encuentros)
- `app/src/main/java/com/example/warofwonders/ui/screens/combat/CombatScreen.kt` (UI dinámica con resultados)

---

## Comandos Rápidos

```bash
# Compilar
./gradlew assembleDebug

# Pruebas
./gradlew test

# Lint
./gradlew lint

# Build completo
./gradlew build
```

---

**Desarrollado para War of Wonders - Noviembre 2025**
