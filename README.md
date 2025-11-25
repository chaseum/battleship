<p align="center">
  <img src="https://img.shields.io/badge/Neo--Retro%20Battleship-v1.0.0-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Build-Maven-0d96f6?style=for-the-badge" />
  <img src="https://img.shields.io/badge/GUI-JavaFX-1abc9c?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-First%20Release-success?style=for-the-badge" />
</p>

# Neo-Retro Battleship
A modern Battleship experience featuring a custom Java engine, AI opponents, online multiplayer, a retro-styled JavaFX GUI, and early-stage animations.

> **Note:** This release includes **fully working gameplay and GUI**, but **visual assets are not final** and **no music or SFX** are included yet.

---

# Overview

Neo-Retro Battleship is a from-scratch reimagining of the classic Battleship experience, built on a fully modular Java engine.

The project supports:
- Classic and Neo-Retro modes
- Complete GUI (JavaFX) with pixel-art style visuals
- Early animation system (semi-finished)
- Local multiplayer
- Online multiplayer via a custom rendezvous server
- CLI mode for full terminal play

---

# Key Features

### Classic Mode
- Standard rules
- Turn-based firing
- Hidden enemy board
- Win when all ships are sunk

### Neo-Retro Mode
Abilities with cooldowns:
- EMP
- Shield
- Multishot
- Sonar

---

# Project Structure

src/main/java/com/chase/battleship/
├── core/
├── ai/
├── net/
├── cli/
└── gui/

---

# Running the Game

GUI:
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp target/classes com.chase.battleship.gui.GameApp

Rendezvous Server:
java -cp target/classes com.chase.battleship.net.RendezvousServer

---

# Roadmap

Completed:
- Engine
- AI
- Abilities
- GUI
- Networking

Planned:
- Final assets
- Audio
- Replay viewer
- Ranked matchmaking

---

# License
MIT License
