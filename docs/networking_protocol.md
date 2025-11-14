# Networking Protocol

## Rendezvous Server (Lobby)

Port: `9000`

### Commands

- `REG <code> <port>`
  - Registers a host under a lobby code.
  - Server stores `(code -> hostIp, port)`.
  - Response: `OK` or `ERR ...`.

- `GET <code>`
  - Looks up host by code.
  - Response:
    - `<ip> <port>` if found.
    - `NF` if not found.
    - `ERR ...` on bad usage.

## Game Host / Client Protocol

All messages are plain UTF-8 text lines.

### Client → Host

- `F r c`
  - Fire at row `r`, column `c`.

- `A EMP`
  - Use EMP ability.

- `A SHIELD r c`
  - Shield own tile at `(r,c)`.

- `A MULTISHOT n`
  - Use MULTISHOT with `n` extra shots.

### Host → Client

- `MSG <text>`
  - Human-readable description of the result (same as `TurnResult.message()`).

- `OVER <winnerName>`
  - Indicates the game is finished and provides the winner's name.

### Mapping to Engine

- `Protocol.parseClientCommand(...)` converts client commands into:
  - `FireAction`, `UseAbilityAction`.
- Host converts `TurnAction` into `TurnResult` by calling:
  - `GameEngine.processTurn(action)`.
- Later extensions may include:
  - Serialized `GameView` objects representing the remote board state.
