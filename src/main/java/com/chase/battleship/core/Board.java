package com.chase.battleship.core;

import java.util.*;

public class Board {
	private final int rows;
	private final int cols;
	private final CellState[][] grid;
	private final List<Ship> ships = new ArrayList<>();

	public Board(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.grid = new CellState[rows][cols];
		for(int r = 0; r < rows; r++) {
			Arrays.fill(grid[r], CellState.EMPTY);
		}
	}

	public int getRows() { return rows; }
	public int getCols() { return cols; }

	public CellState getCellState(Coordinate c) {
		return grid[c.row()][c.col()];
	}

	public boolean inBounds(Coordinate c) {
		return c.row() >= 0 && c.row() < rows && c.col() >= 0 && c.col() < cols;
	}

	public boolean inBounds(int r, int c) {
		return r >= 0 && r < rows && c >= 0 && c < cols;
	}

	public boolean canPlaceShip(ShipType type, Coordinate start, boolean horizontal) {
		int length = type.getLength();
		for(int i = 0; i < length; i++) {
			int r = start.row() + (horizontal ? 0 : i);
			int c = start.col() + (horizontal ? i : 0);
			Coordinate coord = new Coordinate(r,c);
			if(!inBounds(coord) || grid[r][c] != CellState.EMPTY) {
				return false;
			}
			if (isAdjacentOccupied(r, c)) {
				return false;
			}
		}
		return true;
	}

	public void placeShip(Ship ship, Coordinate start, boolean horizontal) {
		int length = ship.getType().getLength();
		if(!canPlaceShip(ship.getType(), start, horizontal)) {
			throw new IllegalArgumentException("Invalid ship placement");
		}
		for(int i = 0; i < length; i++) {
			int r = start.row() + (horizontal ? 0 : i);
			int c = start.col() + (horizontal ? i : 0);
			Coordinate coord = new Coordinate(r,c);
			ship.addCoordinate(coord);
			grid[r][c] = CellState.SHIP;
		}
		ships.add(ship);
	}

	/**
	 * Returns true if any of the 8 neighboring cells (including diagonals)
	 * already contain a ship segment. Used to enforce no-touch placement.
	 */
	private boolean isAdjacentOccupied(int r, int c) {
		for (int dr = -1; dr <= 1; dr++) {
			for (int dc = -1; dc <= 1; dc++) {
				if (dr == 0 && dc == 0) continue;
				int nr = r + dr;
				int nc = c + dc;
				if (!inBounds(nr, nc)) continue;
				if (grid[nr][nc] == CellState.SHIP) {
					return true;
				}
			}
		}
		return false;
	}

	public ShotOutcome fireAt(Coordinate target) {
        assert inBounds(target) : "fireAt: coord out of bounds: " + target;

        if (!inBounds(target)) {
            return ShotOutcome.OUT_OF_BOUNDS;
        }

        CellState state = grid[target.row()][target.col()];

        if (state == CellState.MISS || state == CellState.HIT) {
            return ShotOutcome.ALREADY_TARGETED;
        }

        if (state == CellState.SHIP) {
            Ship hitShip = findShipAt(target)
                    .orElseThrow(() -> new IllegalArgumentException("No ship at cell but state is SHIP"));
            boolean wasShielded = hitShip.hasShield();
            hitShip.registerHit();
            grid[target.row()][target.col()] = CellState.HIT;

            if (hitShip.isSunk()) {
                return ShotOutcome.SUNK;
            }
            return wasShielded ? ShotOutcome.SHIELDED_HIT : ShotOutcome.HIT;
        }

        grid[target.row()][target.col()] = CellState.MISS;
        return ShotOutcome.MISS;
    }

	public boolean allShipsSunk() {
		return ships.stream().allMatch(Ship::isSunk);
	}

	public Optional<Ship> findShipAt(Coordinate c) {
		return ships.stream().filter(s -> s.occupies(c)).findFirst();
	}
	
	public List<Ship> getShips() {
		return Collections.unmodifiableList(ships);
	}

	public void markSeen(Coordinate c, CellState state) {
    	if (!inBounds(c)) throw new IllegalArgumentException("Out of bounds");
    	grid[c.row()][c.col()] = state;
	}

	// resets board
	public void reset() {
		for (int r = 0; r < rows; r++) {
			Arrays.fill(grid[r], CellState.EMPTY);
		}
		ships.clear();
	}

	public boolean allShipsSunkByGrid() {
            for (CellState[] grid1 : grid) {
                for (CellState grid11 : grid1) {
                    if (grid11 == CellState.SHIP) {
                        return false;
                    }
                }
            }
		return true;
	}

	public int countShipCells() {
		int count = 0;
		for (CellState[] grid1 : grid) {
                for (CellState grid11 : grid1) {
                    if (grid11 == CellState.SHIP) {
                        count++;
                    }
                }
            }
		return count;
	}
}
