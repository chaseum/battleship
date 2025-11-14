Neo-Retro Battleship

A modular, extensible, multiplayer Battleship engine with Classic and Neo-Retro modes, full CLI gameplay, online matchmaking via a rendezvous server, and an ability-based combat system.

Overview

Neo-Retro Battleship is a full rebuild and modernization of the classic Battleship game, designed with:

Clean architecture (core engine → CLI/UI → AI → networking)

Terminal-first gameplay for fast testing and debugging

AI opponents using decision-tree heuristics

Online multiplayer with a minimalist text protocol

Neo-Retro abilities (EMP, Shield, Multishot, Sonar, etc.)

Future GUI compatibility for an Atari-style visual interface

This project highlights software engineering discipline:

Modular domain modeling

Reliable game-state transitions

Networked turn-based synchronization

Expandable mechanics and plug-in ability system

Clear separation of concerns across packages

Features
Classic Mode

Standard Battleship rules

Hidden enemy board

Turn-based firing

Win by sinking all ships

Neo-Retro Mode

Adds tactical abilities with charge/cooldown mechanics:

EMP – Temporarily disables opponent abilities

Shield – Protects a tile from being hit

Multishot – Fire multiple shots in one turn

Sonar – Scan an area for nearby ships

These abilities are implemented via AbilityRule, AbilityStatus, and DefaultAbilityExecutor.

src/main/java/com/chase/battleship/
│
├── core/          # Game engine, rules, board model, ability system
├── cli/           # Terminal UI + local players
├── ai/            # DecisionTreeAI, agents, heuristics
└── net/           # Online host/join, rendezvous client/server, protocol

Core Module
Handles:

Board representation

Ship placement

Turn actions

Ability rules + cooldowns

Game phase transitions

Turn resolution

Win/loss detection

CLI Module

Human terminal input

Board rendering

Game loop control

Menus and mode selection

AI Module

Pure state-driven decision agent

Heuristic targeting

Adaptive hunt-mode & target-mode logic

Networking Module

Rendezvous server (matchmaking)

Online host/client sockets

Simple text protocol for:

Commands

Board updates

Move prompts

Game-over signaling

Built to be GUI-agnostic—everything that touches the terminal is isolated to the CLI layer.

Requirements

Java 17+

Recommended: Maven or VS Code + Java extensions

For online play:

Java can run the rendezvous server locally, or

Host it remotely (no credentials required)

Running the Game
1. Compile

If using Maven:

mvn -q -DskipTests compile


If using plain Java:

javac -d bin $(find src/main/java -name "*.java")

Game Modes
2. Run the CLI
java -cp target/classes com.chase.battleship.cli.BattleshipCli


You’ll see:

=== Neo-Retro Battleship ===
1) Classic vs AI
2) Neo-Retro vs AI
3) Classic 2P Local
4) Neo-Retro 2P Local
5) Classic Online Host
6) Classic Online Join
7) Quit

Online Multiplayer
1. Start the Rendezvous Server (matchmaking)
java -cp target/classes com.chase.battleship.net.RendezvousServer

2. Host a Game
java -cp target/classes com.chase.battleship.cli.BattleshipCli


Choose “Classic Online Host”.
You’ll get a lobby code like:

Hosting game. Share this code: VSKmiK

3. Join a Game

Open another terminal:

java -cp target/classes com.chase.battleship.cli.BattleshipCli


Choose “Classic Online Join” and enter the code.

The system automatically:

Resolves the code via rendezvous

Connects client → host

Synchronizes turns

Sends board updates

Exchanges actions via text commands

Example Move Commands (Client & Host)
F r c                 → Fire at (r,c)
A EMP                 → Use EMP
A SHIELD r c          → Shield (r,c)
A MULTISHOT n         → Fire n extra shots
A SONAR r c           → Scan around (r,c)


These commands correspond directly to TurnAction types.

Screenshots (ASCII)

Board Example:

   0 1 2 3 4 5 6 7 8 9
 0  . . . . . . . . . .
 1  . S . . . . . . . .
 2  . S . . . . . . . .
 3  . S . . . . . . . S
 4  . S . . . S . . . S
 5  . S . S S S . . . S
 6  . . . . S S . . . S
 7  . . . . S . . . . .
 8  . . . . S . . . . .
 9  . . . . . . . . . .

Development Goals & Future Work

Add full GUI using:

LWJGL

JavaFX

LibGDX

Replace ASCII boards with pixel-art grid

Implement online matchmaking lobby

Expand AI with probability heatmaps

Replay viewer (record + playback)

Full sound + visual effects in Neo-Retro mode

This codebase is designed to make all of these additions straightforward.

Why This Project Matters

This project demonstrates:

Networking fundamentals

Turn-based engine design

Clean modular Java architecture

Terminal UI separation

AI behavior modeling

Serializable board/turn abstractions

A scalable ability system

It is engineered to be readable, extensible, and production-ready.

Perfect for showcasing:

Software engineering fundamentals

Multi-module Java projects

Game logic & networking

Architecture and maintainability

System design thinking

License

MIT License