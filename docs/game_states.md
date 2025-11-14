# Game States / Phases

## High-Level Phases

- `MENU` – Main menu in `BattleshipCli`:
  - Select Classic vs AI, Neo-Retro vs AI, Local 2P, Online Host/Join.
- `CONNECTING` – Online modes:
  - Host: run `OnlineHost`, register lobby code via `RendezvousClient.registerCode`.
  - Client: resolve lobby code via `RendezvousClient.resolveCode`.
- `SETUP` – Ship placement:
  - Either `BoardUtils.randomFleetPlacement` or interactive placement.
- `PLAYING` – Core gameplay:
  - `GameEngine.processTurn(TurnAction)` drives turns.
  - `DecisionTreeAI` or `TerminalHumanAgent` selects actions.
- `END` – Game over:
  - Winner announced, stats printed.

## Relationship to Engine

- `GameState` in `com.chase.battleship.core` tracks:
  - `PlayerState` for each player.
  - Current turn, game over flag.
- UI layers (CLI, networking, GUI later) only:
  - Collect actions from players/clients.
  - Call `GameEngine.processTurn`.
  - Render `Board` and abilities status.
