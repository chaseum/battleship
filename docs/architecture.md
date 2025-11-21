# architecture.md
# High-Level Architecture of Neo-Retro Battleship (comment-wrapped)

# -----------------------------------------------------------------------------
# 1. Architectural Goals
# -----------------------------------------------------------------------------
# - Strong separation of concerns:
#   - Core engine (rules, state transitions)
#   - UI (CLI and GUI)
#   - AI agents
#   - Networking
# - Deterministic game logic:
#   - Game outcomes depend only on inputs (turns & RNG seed).
# - Testable and maintainable codebase:
#   - Smoke tests for AI vs AI.
#   - Clear models for board, ships, and abilities.
# - Extensibility:
#   - New abilities.
#   - New AIs.
#   - Alternative UIs (e.g., different GUI frameworks).

# -----------------------------------------------------------------------------
# 2. Package Layout
# -----------------------------------------------------------------------------
# src/main/java/com/chase/battleship/
#
# - core/
#   - Board, Ship, Coordinate, CellState
#   - GameConfig, GameState, GameEngine
#   - AbilityType, AbilityRule, AbilityStatus, PlayerAbilities
#   - PlayerState, TurnAction hierarchy, TurnResult
#
# - ai/
#   - PlayerAgent
#   - DecisionTreeAI
#
# - net/
#   - RendezvousServer
#   - RendezvousClient (or equivalent)
#   - Network protocols for host/join + turn exchange
#
# - cli/
#   - BattleshipCli
#   - TerminalHumanAgent
#   - SmokeTestMain (AI vs AI tests)
#
# - gui/
#   - GameApp (JavaFX Application)
#   - ScreenManager, ScreenId
#   - BaseScreen and concrete screens:
#     - TitleScreen
#     - SinglePlayerScreen
#     - MultiPlayerScreen
#     - HostLobbyScreen / JoinCodeScreen
#     - SetupScreen
#     - PlayingScreen
#     - WinScreen / LoseScreen
#   - GuiGameSession
#   - Board rendering components
#   - Animations and audio hooks

# -----------------------------------------------------------------------------
# 3. Core Engine
# -----------------------------------------------------------------------------
# The "core" package is completely UI-agnostic.
#
# Responsibilities:
# - Represent the board (grid of CellState).
# - Track ship placement and hits.
# - Enforce rules for firing (bounds, duplicate shots).
# - Apply ability effects via an AbilityExecutor.
# - Maintain GameState: whose turn, game over flag, etc.
#
# GameEngine:
# - processTurn(TurnAction action) -> TurnResult
# - Internal flow:
#   1) Validate that game is not already over.
#   2) Identify current player and opponent.
#   3) Dispatch:
#      - FireAction -> handleFire(...)
#      - UseAbilityAction -> handleAbility(...)
#   4) Check if all opponent ships are sunk, set game over if so.
#   5) Otherwise, advance to the next turn and tick ability cooldowns.

# -----------------------------------------------------------------------------
# 4. Abilities System
# -----------------------------------------------------------------------------
# - GameConfig defines AbilityRule for each AbilityType:
#   - max charges
#   - cooldown turns
#   - any extra metadata
#
# - PlayerAbilities holds AbilityStatus for each configured ability:
#   - charges remaining
#   - cooldownRemaining
#
# - AbilityExecutor (e.g., DefaultAbilityExecutor) encapsulates the
#   implementation of:
#   - EMP
#   - Shield
#   - Multishot
#   - Sonar
#
# This design keeps core turn handling generic while allowing ability logic
# to be updated or extended without changing GameEngine itself.

# -----------------------------------------------------------------------------
# 5. AI Layer
# -----------------------------------------------------------------------------
# The AI layer consumes only the core model types:
# - GameState
# - PlayerState
# - Board
# - Ability-related classes
#
# DecisionTreeAI:
# - Implements PlayerAgent.
# - Uses purely read-only access to GameState.
# - Calculates which TurnAction to perform based on:
#   - tracking board
#   - known hits/misses
#   - unknown ratio
#   - alive ships
# - Keeps the engine free of AI logic.

# -----------------------------------------------------------------------------
# 6. Networking Layer
# -----------------------------------------------------------------------------
# The net package manages:
# - RendezvousServer:
#   - Simple TCP server for mapping lobby codes to host endpoints.
# - Client-side components:
#   - Register host with code.
#   - Resolve code to host IP:port.
#   - Negotiate a connection and send/receive text-based commands.
#
# Protocol responsibilities:
# - Top-level:
#   - HOST registers "code -> IP:PORT".
#   - JOIN queries by code and gets host endpoint.
# - Game-level:
#   - Client sends serialized TurnAction commands.
#   - Host runs GameEngine and broadcasts results / updated views.

# -----------------------------------------------------------------------------
# 7. UI Layers
# -----------------------------------------------------------------------------

# CLI:
# - Thin wrapper around GameEngine.
# - Renders ASCII boards.
# - Accepts simple text commands.
# - Great for debugging and development.

# GUI (JavaFX):
# - Uses ScreenManager to manage scenes and navigation stack.
# - Each screen encapsulates its own layout and event handlers.
# - Interacts with GuiGameSession to:
#   - create/hold GameState
#   - send actions to the engine
#   - respond to TurnResults for visual updates.
#
# Key design points:
# - GUI does not directly mutate GameState.
# - All notifications (e.g., errors, disconnects) are rendered as in-game labels,
#   overlays, or dialogs drawn in the scene (no OS-native pop-ups).

# -----------------------------------------------------------------------------
# 8. Testing & Smoke Tests
# -----------------------------------------------------------------------------
# SmokeTestMain:
# - Runs AI vs AI for many games.
# - Uses a turn cap to detect infinite loops.
# - Validates:
#   - no exceptions
#   - games conclude with all ships sunk on one side
#   - abilities do not break state invariants.
#
# Invariants:
# - Coordinates are never negative or out of bounds.
# - Cooldown and charges never go below zero.
# - Game does not advance once gameOver is true.

# -----------------------------------------------------------------------------
# 9. Extensibility
# -----------------------------------------------------------------------------
# The architecture supports:
# - Additional AbilityTypes without modifying screen logic, as long as they are
#   surfaced via GameConfig and AbilityExecutor.
# - New AIs as more PlayerAgent implementations.
# - Alternate UIs that integrate with GameEngine and GameState in the same way.
# - UI reskins via CSS and asset swaps.
#
# The core does not depend on JavaFX or other UI frameworks, ensuring that the
# engine can be reused with different frontends.

# -----------------------------------------------------------------------------
# 10. Summary
# -----------------------------------------------------------------------------
# Neo-Retro Battleship separates:
# - What the game IS (core and engine)
# - How it is PLAYED (CLI/GUI/net)
# - Who is controlling it (agents)
#
# This structure makes the project highly maintainable and ideal as a
# portfolio-ready demonstration of system design and game engineering in Java.
