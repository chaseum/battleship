# agents.md
# Game Agents Architecture Overview (comment-wrapped for safe copy/paste)

# -----------------------------------------------------------------------------
# Agents – Overview
# -----------------------------------------------------------------------------
# This document explains how "agents" (player controllers) work in Neo-Retro
# Battleship. An agent is anything that decides what a player does on their turn,
# whether that is a human in the terminal, a GUI user, or an AI.
#
# Agents DO NOT mutate game state directly. They only return a TurnAction.
# The GameEngine is the single source of truth for applying actions.

# -----------------------------------------------------------------------------
# PlayerAgent Interface
# -----------------------------------------------------------------------------
# All agents implement:
#
#   public interface PlayerAgent {
#       TurnAction chooseAction(GameState state, boolean isP1);
#   }
#
# Responsibilities:
# - Inspect current GameState (current player, boards, abilities, etc.).
# - Decide whether to fire or use an ability.
# - Return a TurnAction (FireAction, UseAbilityAction, etc.).
#
# Non-responsibilities:
# - Applying damage
# - Flipping turns
# - Declaring a winner
# - Mutating the board directly

# -----------------------------------------------------------------------------
# Built-in Agents
# -----------------------------------------------------------------------------

# 1) TerminalHumanAgent (CLI)
# ---------------------------
# Package: com.chase.battleship.cli
#
# - Used by the terminal version of the game.
# - Prompts the player for input via Scanner.
# - Supports both Classic and Neo-Retro actions:
#   - FireAction: "row col"
#   - UseAbilityAction: prompts for ability type and coordinates/extra shots.
#
# Good for:
# - Debugging the engine
# - Quick manual smoke tests
# - Non-GUI environments

# 2) GUI "Agent" (JavaFX Screens)
# -------------------------------
# There is no explicit GuiHumanAgent class. Instead, JavaFX screens act as
# the human agent by calling:
#
#   session.processHumanAction(new FireAction(...));
#
# or
#
#   session.processHumanAction(new UseAbilityAction(...));
#
# The GUI handles:
# - Mouse clicks
# - Keyboard navigation
# - Ability selection
# - Turn-confirmation
# and then relays a TurnAction into the GameEngine via GuiGameSession.

# 3) DecisionTreeAI
# -----------------
# Package: com.chase.battleship.ai.DecisionTreeAI
#
# Primary AI used in single-player modes.
#
# Behaviors:
# - Hunt/Target mode:
#   - Hunt: fire at parity cells (checkerboard pattern) until a hit is found.
#   - Target: when a hit is found, prioritize adjacent cells to finish ships.
# - Avoids firing at cells already seen as MISS/HIT.
# - Supports Neo-Retro abilities with heuristics:
#   - Sonar early game to discover ship clusters.
#   - Shield mid-game on critical ship segments.
#   - Multishot when many unknown tiles remain.
#   - EMP in mid/late game when opponent has multiple ships alive.
#
# Safety:
# - Turn caps in smoke tests detect infinite-loop behavior.
# - Ability usage is gated by unknown-tile ratio and ship counts.

# -----------------------------------------------------------------------------
# Integration with Game Flow
# -----------------------------------------------------------------------------
# The GuiGameSession orchestrates which agent controls which player.
#
# Example:
#   switch (mode) {
#       case CLASSIC_VS_AI, NEORETRO_VS_AI:
#           p1IsHuman = true;  p1Agent = null;
#           p2IsHuman = false; p2Agent = new DecisionTreeAI();
#           break;
#       case CLASSIC_LOCAL_2P, NEORETRO_LOCAL_2P:
#           p1IsHuman = true; p2IsHuman = true;
#           p1Agent = null;  p2Agent = null;
#           break;
#   }
#
# On a human turn (GUI or CLI):
# - Input -> TurnAction -> engine.processTurn(action).
#
# On an AI turn:
# - agent.chooseAction(state, isP1) -> TurnAction -> engine.processTurn(action).

# -----------------------------------------------------------------------------
# Adding a New Agent
# -----------------------------------------------------------------------------
# Template:
#
#   public class MyAgent implements PlayerAgent {
#       @Override
#       public TurnAction chooseAction(GameState state, boolean isP1) {
#           // Inspect state.getCurrentPlayer(), boards, abilities, etc.
#           Coordinate c = new Coordinate(3, 5);
#           return new FireAction(c);
#       }
#   }
#
# Useful data:
# - state.getCurrentPlayer(), state.getOtherPlayer()
# - current player's tracking board
# - own board for defensive decisions
# - ability availability and cooldown status
# - game mode (Classic vs Neo-Retro)
#
# Best practices:
# - Do not mutate GameState directly.
# - Avoid heavy computation each turn.
# - Respect board boundaries.
# - Avoid firing at already-known tiles when possible.

# -----------------------------------------------------------------------------
# Neo-Retro Ability Considerations for Agents
# -----------------------------------------------------------------------------
# Sonar:
# - Best used when many tiles are unknown.
# - Choose a center coordinate that is in bounds.
#
# Shield:
# - Consider ships that are not sunk and span multiple tiles.
# - Shield coordinates where hits are likely or already clustering.
#
# Multishot:
# - Choose multiple distinct coordinates.
# - Useful early/mid game for exploring unknown tiles.
#
# EMP:
# - More valuable when opponent still has several ships alive.
# - Temporarily disables opponent abilities, making aggressive play safer.

# -----------------------------------------------------------------------------
# Future Agent Extensions
# -----------------------------------------------------------------------------
# The PlayerAgent interface is intentionally minimal to allow:
# - Network-driven agents (remote players via sockets).
# - Replay agents (play back recorded games).
# - Machine-learning agents.
# - Tournament agents for AI-vs-AI competitions.
#
# None of these require changes to GameEngine or GameState.

# -----------------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------------
# Agents are the bridge between players (human or AI) and the core game engine.
# They isolate "decision-making" from "state mutation", keeping the system:
# - Testable
# - Extensible
# - Easy to reason about
