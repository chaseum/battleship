# Neo-Retro Battleship
# A fully modern Battleship experience featuring a custom Java engine, AI opponents,
# online play, a retro-styled GUI, animations, and an original soundtrack.

# ------------------------------------------------------------------------------

# Overview
#
# Neo-Retro Battleship is a from-scratch reimagining of the classic Battleship game,
# built around a modular, extensible Java engine. The project supports Classic and
# Neo-Retro modes, offers a polished JavaFX GUI with pixel-art visuals, includes a
# retro soundtrack, and provides online multiplayer via a custom rendezvous server.
#
# The system is designed to be maintainable, testable, and easily expandable—with
# a clean separation between the engine, AI, networking layer, and UI.
#
# This game demonstrates full-stack game engineering in Java—from deterministic turn
# resolution to real-time UI/UX, network synchronization, AI agents, and visual/audio design.

# ------------------------------------------------------------------------------

# Key Features

# Classic Mode
# - Standard Battleship rules
# - Turn-based firing
# - Hidden enemy board
# - Victory when all enemy ships are sunk

# Neo-Retro Mode
# Abilities with cooldowns, charges, animations, and SFX:
# - EMP — disables opponent abilities
# - Shield — protects a tile for multiple turns
# - Multishot — fires multiple coordinated shots
# - Sonar — scans a 3×3 region for ships

# ------------------------------------------------------------------------------

# Project Structure

# src/main/java/com/chase/battleship/
# ├── core/        # Engine, rules, abilities, turn system
# ├── ai/          # DecisionTreeAI, heuristics
# ├── net/         # Rendezvous server/client protocol
# ├── cli/         # Terminal mode
# └── gui/         # JavaFX UI, screens, animations, sound

# Core Module
# - Turn system & phases
# - Ship placement & board model
# - Hit/miss/shield/sunk logic
# - Ability rule resolution
# - Deterministic AI- and network-safe state transitions

# AI Module
# - Hunt/target logic
# - Probability-based move selection
# - Ability-use heuristics

# Networking Module
# - Join-code matchmaking
# - Host/Join workflow
# - Turn synchronization
# - Disconnection handling

# GUI Module (JavaFX)
# - Retro pixel-art grid
# - Responsive layout (fullscreen & windowed)
# - Animations: hits, misses, sonar pulse, shield glow, EMP flash
# - Retro synth soundtrack + SFX
# - Crosshair cursor
# - Keyboard + mouse controls

# ------------------------------------------------------------------------------

# Screens & User Flow
#
# - Title Screen
# - Single / Multiplayer Selection
# - Mode Selection
# - Host Lobby / Join Code Screen
# - Setup Screen (Auto / Manual)
# - Playing Screen (turns, abilities, animations)
# - Win / Lose Screen

# All screens:
# - Support Back navigation
# - Show in-game labels instead of OS pop-ups
# - Provide animated transitions
# - Use a retro UI theme

# ------------------------------------------------------------------------------

# Installation & Requirements
#
# - Java 17+
# - Maven (recommended)
# - JavaFX installed locally
# - Rendezvous server required for online multiplayer
#
# Example Maven:
#   mvn clean package

# ------------------------------------------------------------------------------

# Running the Game

# GUI:
# java --module-path /path/to/javafx-sdk/lib \
#      --add-modules javafx.controls,javafx.fxml \
#      -cp target/classes com.chase.battleship.gui.GameApp

# Rendezvous Server:
# java -cp target/classes com.chase.battleship.net.RendezvousServer

# ------------------------------------------------------------------------------

# Online Multiplayer Instructions

# Hosting:
# - Select "Host Game"
# - Receive a lobby code
# - Share the code

# Joining:
# - Select "Join Game"
# - Enter the lobby code
# - Automatically transition into the game

# During play:
# - Turns alternate with built-in delays
# - Disconnections display a UI overlay and return the player to the Title Screen

# ------------------------------------------------------------------------------

# GUI Visuals & Effects

# Hit / Miss:
# - Pixel splash animations
# - Floating text labels
# - Tile overlays

# Ability SFX & VFX:
# - EMP: bright neon pulse with screen flash
# - Sonar: radial scanning ring effect
# - Shield: shimmering hex tile
# - Multishot: sequential rapid impacts

# Audio:
# - Synthwave background music
# - SFX for moves, abilities, transitions, and wins

# ------------------------------------------------------------------------------

# Roadmap & Completed Work

# Completed:
# - Java engine (core)
# - AI opponent
# - Abilities system
# - Local multiplayer
# - Online matchmaking
# - JavaFX GUI
# - Animations + sound design
# - Full retro styling
# - Input system (keyboard + mouse)
# - Turn sequencing with delays

# Planned:
# - Replay viewer
# - Ranked matchmaking
# - Additional skins & themes
# - Colorblind mode
# - Expanded ability suite

# ------------------------------------------------------------------------------

# Why This Project Matters

# The project demonstrates:
# - Modular software architecture
# - Deterministic gameplay engine
# - AI decision-making
# - JavaFX UI engineering
# - Networked multiplayer systems
# - Visual/audio asset integration
# - Clean separation of concerns

# Ideal for:
# - Game dev portfolios
# - Software engineering resumes
# - Demonstrating Java proficiency
# - Showcasing full-stack game architecture

# ------------------------------------------------------------------------------

# License
# MIT License
