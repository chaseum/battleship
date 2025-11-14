### SONAR

- **Type**: Informational ability (no damage).
- **Charges**: 3 (configurable via `GameConfig.neoRetroDefault()`).
- **Cooldown**: 2 turns.
- **Effect**:
  - Player selects a center coordinate on the enemy board.
  - The ability scans the 3×3 region around that coordinate.
  - The engine reports how many enemy ship segments are present and their coordinates.
  - No ships are damaged; the player must still fire to hit them.