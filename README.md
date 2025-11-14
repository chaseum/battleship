# Neo-Retro Battleship  
*A modular, extensible Battleship engine with Classic and Neo-Retro modes, AI, networking, and a full CLI.*

---

## Overview

Neo-Retro Battleship is a rebuilt, modernized version of the Battleship game designed with:

- Clean, modular architecture  
- Terminal-first design for debugging  
- AI opponents (decision-tree based)  
- Online multiplayer via rendezvous server  
- Neo-Retro abilities (EMP, Shield, Multishot, Sonar, etc.)  
- Future GUI compatibility (Atari-style grid)

This project emphasizes software engineering best practices:

- Domain-driven design  
- Robust state transitions  
- Networked turn synchronization  
- Extensible mechanics (plug-in ability system)  
- Clear separation between core engine, UI, networking, and AI

---

## Features

### Classic Mode
- Standard Battleship rules  
- Turn-based firing  
- Hidden enemy board  
- Win by sinking all ships  

### Neo-Retro Mode
Adds tactical abilities with cooldowns and charges:

- **EMP** – disables enemy abilities  
- **Shield** – protects a tile  
- **Multishot** – fire multiple shots  
- **Sonar** – scan an area for ships  

---

## Project Structure

```text
src/main/java/com/chase/battleship/
│
├── core/    # Game engine, rules, board, abilities
├── cli/     # Terminal UI, local multiplayer
├── ai/      # DecisionTreeAI and player agents
└── net/     # Rendezvous server/client and online protocol

# Core Module
Handles:
- Board representation
- Ship placement
- Turn actions
- Ability rules and cooldowns
- Game phases
- Turn resolution
- Win/loss logic

# CLI Module
- Terminal menus
- Board printing
- Local vs AI or 2-player
- Mode selection

# AI Module
- Decision-tree heuristic AI
- Hunt/target logic
- State-driven move selection

# Networking Module
- Rendezvous server (matchmaking)
- Online host/client sockets
- Lightweight text protocol
- Built to be UI-agnostic

# Requirements
- Java 17+
- Recommended: Maven or VS Code Java extensions
- Multiplayer requires running the rendezvous server

# Running the Game

## 1. Compile

### Using Maven:
- mvn -q -DskipTests compile
- javac -d bin $(find src/main/java -name "*.java")
- java -cp target/classes com.chase.battleship.cli.BattleshipCli

You will see:
=== Neo-Retro Battleship ===
1. Classic vs AI
2. Neo-Retro vs AI
3. Classic 2P Local
4. Neo-Retro 2P Local
5. Classic Online Host
6. Classic Online Join
7. Quit

# Online Multiplayer

## 1. Run the Rendezvous Server
- java -cp target/classes com.chase.battleship.net.RendezvousServer
## 2. Host a Game
- java -cp target/classes com.chase.battleship.cli.BattleshipCli
- Choose **Classic Online Host**.  
It will display a join code, e.g.:
- Hosting game. Share this code: VSKmiK
## 3. Join a Game
Open another terminal and run:
- java -cp target/classes com.chase.battleship.cli.BattleshipCli

Select **Classic Online Join**, enter the code, and the game begins.

# Move Commands (Online)
- F r c → Fire at (row, col)
- A EMP → Use EMP
- A SHIELD r c → Shield tile
- A MULTISHOT n → Fire n extra shots
- A SONAR r c → Scan around (row, col)

# ASCII Board Example
0 1 2 3 4 5 6 7 8 9
0 . . . . . . . . . .
1 . S . . . . . . . .
2 . S . . . . . . . .
3 . S . . . . . . . S
4 . S . . . S . . . S
5 . S . S S S . . . S
6 . . . . S S . . . S
7 . . . . S . . . . .
8 . . . . S . . . . .
9 . . . . . . . . . .

# Roadmap
- Full GUI (JavaFX, LibGDX, LWJGL)
- Pixel-art grid rendering
- Online matchmaking lobby
- Probability-based AI
- Replay viewer with logs
- Visual and sound effects

# Why This Project Matters

Demonstrates proficiency in:
- Networking
- Turn-based engine design
- Clean Java architecture
- AI heuristics
- Modular extensibility
- Ability & rule systems
- CLI/UX separation

Ideal for showcasing:
- Software engineering fundamentals
- Multiplayer systems
- AI behavior programming
- Game engine design
- Maintainable code structure

# License
MIT License