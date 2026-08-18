# Rygg — Android app coding style

This is the **code** for the Rygg hiking/mountaineering Android app (the product concept
lives in `../../README.md`). This file describes the conventions already used across the
codebase. When adding or changing code, match these patterns rather than reaching for
framework defaults.

Stack: Kotlin, Jetpack Compose (Material 3), Hilt, Room, Coroutines/Flow, type-safe
Navigation Compose, Firebase (auth).

## Architecture & package layout

Root package `com.example.rygg`, split into two top-level areas:

- `core/` — cross-cutting: `common/`, `database/`, `di/`, `gpx/`, `navigation/`,
  `notification/`, `locale/`, `ui/{components,theme,utils}`.
- `feature/<name>/` — one folder per feature (`auth`, `library`, `map`, `details`,
  `record`, `profile`, `settings`), each split into:
  - `data/` — repositories, `data/local/` DAOs + Room entities
  - `domain/` — plain domain models and enums
  - `ui/` — split into `wrapper/`, `screen/`, `viewmodel/`, `components/`

Keep a feature's code inside its own folder; only promote something to `core/` when a
second feature needs it.

## The three-layer UI pattern (most important convention)

Every screen is built from three files. Do not collapse them.

1. **Wrapper** — `ui/wrapper/XWrapper.kt`
   - `@Composable`; parameters are navigation callbacks plus
     `viewModel: XViewModel = hiltViewModel()`.
   - The **only** UI layer that touches the ViewModel. Collects state with
     `collectAsStateWithLifecycle()`, constructs `XScreenParams`, and calls the screen.
   - Ref: `feature/library/ui/wrapper/LibraryWrapper.kt`.

2. **Screen** — `ui/screen/XScreen.kt`
   - Stateless. Takes a single `params: XScreenParams` — never a ViewModel.
   - Break UI into `private` sub-composables (e.g. `LoadedContent`).
   - Include a `@Preview` composable wrapped in `RyggTheme` with hand-built fake params.
   - Declare the `XScreenParams` data class (holds `uiState` + all `on*` lambdas) at the
     **bottom of the same file**.
   - Ref: `feature/library/ui/screen/LibraryScreen.kt`.

3. **ViewModel** — `ui/viewmodel/XViewModel.kt`
   - `@HiltViewModel` with `@Inject constructor`.
   - Expose exactly one `uiState: StateFlow<XUiState>`, built with
     `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
     Drive internal changes through a private `MutableStateFlow` (e.g. a `filter`) and
     `.update { it.copy(...) }`.
   - Name intent handlers `on*` (`onToggleSort`, `onDisciplineSelected`). Launch mutations
     in `viewModelScope.launch { ... }`.
   - Declare `XUiState` and any nested `sealed interface` loading state at the bottom of
     the file. Put pure list/filter helpers as `private fun List<T>.applyX()` extensions.
   - Ref: `feature/library/ui/viewmodel/LibraryViewModel.kt`.

## State & result modeling

- Use the custom `Outcome<T>` (`Loading` / `Success` / `Error`) from
  `core/common/Outcome.kt` — **not** Kotlin's `Result`.
  - Wrap a stream with `Flow<T>.asResult()` (emits `Loading` first, then `Success`/`Error`).
  - Wrap a suspend block with `outcomeCatching { }` (it rethrows `CancellationException`).
- Model screen-specific loading as a nested `sealed interface` (e.g. `GpxFilesLoadingState`
  → `Loading` / `GpxFilesLoaded` / `Error`) and render it with an exhaustive `when` in the
  screen. Use `data object` for stateless variants, `data class` for ones carrying data.

## Data layer

- Repositories are plain classes with `@Inject constructor` — **no interface** unless a
  real second implementation exists.
- `observe*()` returns `Flow<Domain>`; suspend mutations return `Outcome<T>` via
  `outcomeCatching`; wrap blocking I/O in `withContext(Dispatchers.IO)`.
- Map between Room entities and domain models with `toDomain()` / `toEntity()` extension
  functions. Keep Room entities in `data/local/`, domain models in `domain/`.
- Ref: `feature/library/data/GpxFileEntryRepository.kt`.

## Dependency injection (Hilt)

- Modules are `object`s annotated `@Module @InstallIn(SingletonComponent::class)` with
  `@Provides` functions (`@Singleton` for app-wide singletons).
- One module per concern: `DatabaseModule`, `NetworkModule`, `DataStoreModule`,
  `FirebaseModule`. Ref: `core/di/DatabaseModule.kt`.

## Theming — use `RyggTheme`, not Material directly

- For app colors call `RyggTheme.getColor(RyggColor.SurfaceDim)` — **do not** read
  `MaterialTheme.colorScheme` directly. Colors are `RyggColor` tokens that each carry a
  `lightColor` and `darkColor`; `getColor` resolves by `RyggTheme.isDarkMode`.
- For spacing/sizing use `RyggTheme.dimens.*` tokens — **no raw `.dp` literals** in UI.
  Tokens are semantic-with-scale (`commonContentPadding16`, `commonSpacing8`, `iconSize24`,
  `radius12`, `elevation2`); add a new token to `Dimensions` rather than inlining a value.
- Refs: `core/ui/theme/Theme.kt`, `core/ui/theme/Dimensions.kt`, `.../Color.kt`.

## Navigation

- Type-safe routes: `@Serializable data object`/`data class` in
  `core/navigation/NavRoutes.kt` (`Library`, `Map(entryId: Long? = null)`).
- Register each with `composable<Route> { XWrapper(...) }` in `AppNavigation.kt`; pass
  navigation as lambdas into wrappers. Reset the back stack with
  `popUpTo(navController.graph.id) { inclusive = true }`.

## Shared components & naming

- Reusable Compose components live in `core/ui/components/` and take the `Rygg` prefix
  (`RyggTopAppBar`, `RyggPrimaryButton`, `RyggTextField`, `RyggBottomAppBar`). Feature-only
  components stay in that feature's `ui/components/`.
- Naming: `on*` = user intent lambdas, `observe*` = repository Flows, `remember*` = Compose
  factory helpers (`rememberFilePicker`).
- All user-facing text via `stringResource(R.string.…)` — never hardcode UI strings.

## Comments & formatting

- Prefer a terse `//` one-liner above a declaration explaining **why** it exists, not what
  it does (see `asResult` / `outcomeCatching`). Otherwise let the code speak — no redundant
  comments.
- 4-space indent; trailing commas on multiline argument lists; use explicit named arguments
  on multi-argument Compose calls.
