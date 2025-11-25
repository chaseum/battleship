package com.chase.battleship.core;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class DefaultAbilityExecutor implements AbilityExecutor {
	private final Random random = new Random();

	@Override
	public AbilityResult execute(
		AbilityType type,
		GameState gameState,
		PlayerState user,
		PlayerState opponent,
		AbilityTarget target
	) {
		return switch(type) {
			case EMP -> doEmp(user, opponent);
			case MULTISHOT -> doMultishot(gameState, user, opponent, target);
			case SHIELD -> doShield(user, target);
			case SONAR -> doSonar(user, opponent, target);
		};
	}

	private AbilityResult doEmp(PlayerState user, PlayerState opponent) {
		opponent.applyEmpLock(2);
		return new AbilityResult("EMP deployed! "+ opponent.getName() + "'s abilties disabled for 2 turns");
	}

	private AbilityResult doMultishot(GameState gameState,
                                  PlayerState user,
                                  PlayerState opponent,
                                  AbilityTarget target) {

    if (target.manualTargets() != null && !target.manualTargets().isEmpty()) {
        return doMultishotManual(user, opponent, target.manualTargets());
    }
    return autoMultishotAI(user, opponent);
}

	private AbilityResult doMultishotManual(PlayerState user,
											PlayerState opponent,
											List<Coordinate> targets) {
		Board tracking   = user.getTrackingBoard();
		Board enemyBoard = opponent.getOwnBoard();

		int hits = 0;
		int sunk = 0;
		int shots = targets.size();

		for (Coordinate c : targets) {
			if (!enemyBoard.inBounds(c)) continue;

			ShotOutcome outcome = enemyBoard.fireAt(c);

			if (outcome == ShotOutcome.MISS ||
				outcome == ShotOutcome.HIT ||
				outcome == ShotOutcome.SUNK ||
				outcome == ShotOutcome.SHIELDED_HIT) {

				CellState mark = (outcome == ShotOutcome.MISS)
						? CellState.MISS : CellState.HIT;
				if (tracking.inBounds(c)) {
					tracking.markSeen(c, mark);
				}
			}

			if (outcome == ShotOutcome.HIT || outcome == ShotOutcome.SHIELDED_HIT) {
				hits++;
			} else if (outcome == ShotOutcome.SUNK) {
				hits++;
				sunk++;
			}
		}

		String desc = "Multishot (manual): fired " + shots + ", hits=" + hits;
		if (sunk > 0) desc += ", ships sunk=" + sunk;
		return new AbilityResult(desc);
	}

	private AbilityResult autoMultishotAI(PlayerState user,
										PlayerState opponent) {
		Board tracking   = user.getTrackingBoard();
		Board enemyBoard = opponent.getOwnBoard();

		int rows = tracking.getRows();
		int cols = tracking.getCols();

		List<Coordinate> cluster = new ArrayList<>();
		boolean[][] seen = new boolean[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				Coordinate hit = new Coordinate(r, c);
				if (tracking.getCellState(hit) == CellState.HIT) {
					int[][] dirs = {
							{-1, 0}, {1, 0},
							{0, -1}, {0, 1}
					};
					for (int[] d : dirs) {
						int nr = r + d[0];
						int nc = c + d[1];
						if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
						if (seen[nr][nc]) continue;
						Coordinate nb = new Coordinate(nr, nc);
						if (tracking.getCellState(nb) == CellState.EMPTY) {
							seen[nr][nc] = true;
							cluster.add(nb);
						}
					}
				}
			}
		}

		List<Coordinate> empties = new ArrayList<>();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				Coordinate coord = new Coordinate(r, c);
				if (tracking.getCellState(coord) == CellState.EMPTY) {
					empties.add(coord);
				}
			}
		}

		List<Coordinate> candidates = new ArrayList<>();
		if (!cluster.isEmpty()) candidates.addAll(cluster);
		candidates.addAll(empties);

		if (candidates.isEmpty()) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					candidates.add(new Coordinate(r, c));
				}
			}
		}

		Collections.shuffle(candidates);

		int shots = Math.min(3, candidates.size());
		int hits = 0;
		int sunk = 0;

		for (int i = 0; i < shots; i++) {
			Coordinate c = candidates.get(i);
			ShotOutcome outcome = enemyBoard.fireAt(c);

			if (outcome == ShotOutcome.MISS ||
				outcome == ShotOutcome.HIT ||
				outcome == ShotOutcome.SUNK ||
				outcome == ShotOutcome.SHIELDED_HIT) {

				CellState mark = (outcome == ShotOutcome.MISS)
						? CellState.MISS : CellState.HIT;
				if (tracking.inBounds(c)) {
					tracking.markSeen(c, mark);
				}
			}

			if (outcome == ShotOutcome.HIT || outcome == ShotOutcome.SHIELDED_HIT) hits++;
			else if (outcome == ShotOutcome.SUNK) { hits++; sunk++; }
		}

		String desc = "Multishot fired " + shots + " shots, hits=" + hits;
		if (sunk > 0) desc += ", ships sunk=" + sunk;
		return new AbilityResult(desc);
	}




	private Coordinate randomCoordinate(Board b) {
		int r = random.nextInt(b.getRows());
		int c = random.nextInt(b.getCols());
		return new Coordinate(r,c);
	}

	private AbilityResult doShield(PlayerState user, AbilityTarget target) {
		Ship ship = user.getOwnBoard().findShipAt(target.coordinate()).orElse(null);
		if(ship == null) {
			return new AbilityResult("No ship at target coordinate, shield wasted.");
		}
		ship.applyShield();
		return new AbilityResult("Shield applied to " +ship.getType() + " at "+ target.coordinate().row() + "," + target.coordinate().col());
	}

	private AbilityResult doSonar(PlayerState user, PlayerState opponent, AbilityTarget target) {
		Coordinate center = target.coordinate();
		if(center == null) {
			return new AbilityResult("Sonar failed: no center coordinate specified.");
		}

        Board enemyBoard = opponent.getOwnBoard();
        int radius = 1; // 3x3
        List<Coordinate> hits = new ArrayList<>();

		for(int dr = -radius; dr <= radius; dr++) {
			for(int dc = -radius; dc <= radius; dc++) {
				int nr = center.row() + dr;
				int nc = center.col() + dc;

				if(!enemyBoard.inBounds(nr, nc)) {
					continue;
				}

				Coordinate c = new Coordinate(nr,nc);

                if(enemyBoard.findShipAt(c).isPresent()) {
                    hits.add(c);
                }
            }
        }
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(
			"Sonar scan at (%d,%d) detected %d ship segment(s) in the 3x3 area: ",
			center.row(), center.col(), hits.size()
		));
		for(int i = 0; i < hits.size(); i++) {
			Coordinate c = hits.get(i);
			sb.append("(").append(c.row()).append(",").append(c.col()).append(")");
			if(i < hits.size() - 1) sb.append(", ");
		}
		sb.append(".");
		if (hits.isEmpty()) {
			sb = new StringBuilder(String.format(
					"Sonar scan at (%d,%d) found no ship segments in the 3x3 area.",
					center.row(), center.col()
			));
		}

		return new AbilityResult(sb.toString());
	}
}
