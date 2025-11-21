package com.chase.battleship.ai;

import com.chase.battleship.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DecisionTreeAI implements PlayerAgent {
	private final Random random = new Random();

	@Override
	public TurnAction chooseAction(GameState gameState, boolean isSelfPlayer1) {
		PlayerState me = gameState.getCurrentPlayer();
		PlayerState enemy = gameState.getOtherPlayer();

		// decision tree root: are abilities available & beneficial?
		if(gameState.getConfig().getGameMode().isNeoRetro()) {
			TurnAction abilityAction = maybeUseAbility(gameState, me, enemy);
			if(abilityAction != null) {
				return abilityAction;
			}
		}

		Coordinate target = chooseFireTarget(me, enemy);
		return new FireAction(target);
	}

	private TurnAction maybeUseAbility(GameState gameState, PlayerState me, PlayerState enemy) {
		if (!gameState.getConfig().getGameMode().isNeoRetro()) {
			return null;
		}
		if (me.abilitiesLocked() || me.getAbilities() == null) {
			return null;
		}

		PlayerAbilities abilities = me.getAbilities();
		Board tracking = me.getTrackingBoard();
		int rows = tracking.getRows();
		int cols = tracking.getCols();
		int total = rows * cols;

		int unknown = countUnknownCells(tracking);
		double unknownRatio = (double) unknown / total;

		boolean earlyGame = unknownRatio > 0.70;
		boolean midGame   = unknownRatio > 0.40 && unknownRatio <= 0.70;
		boolean lateGame  = unknownRatio <= 0.40;

		// Global gate: most turns should just be shots.
		// Early: 40% chance to even consider abilities
		// Mid:   25%
		// Late:  10%
		double roll = random.nextDouble();
		double abilityChance = earlyGame ? 0.40 : (midGame ? 0.25 : 0.10);
		if (roll > abilityChance) {
			return null;
		}

		AbilityStatus sonar     = abilities.getStatus(AbilityType.SONAR);
		AbilityStatus shield    = abilities.getStatus(AbilityType.SHIELD);
		AbilityStatus multishot = abilities.getStatus(AbilityType.MULTISHOT);
		AbilityStatus emp       = abilities.getStatus(AbilityType.EMP);

		// 1) Early-game info: SONAR, but only then, and not all the time.
		if (earlyGame && sonar != null && sonar.isAvailable()) {
			if (random.nextDouble() < 0.50) {
				Coordinate center = randomUnknownCoordinate(tracking);
				return new UseAbilityAction(
						AbilityType.SONAR,
						new AbilityTarget(center, 0)
				);
			}
		}

		// 2) Offensive priority: MULTISHOT when board is still reasonably unknown.
		if ((earlyGame || midGame) && multishot != null && multishot.isAvailable()) {
			if (unknownRatio > 0.30 && random.nextDouble() < 0.70) {
				return new UseAbilityAction(
						AbilityType.MULTISHOT,
						new AbilityTarget(null, 3)
				);
			}
		}

		// 3) EMP: support offensive, mid/late game only, occasional.
		if (!earlyGame && emp != null && emp.isAvailable()) {
			int aliveShips = (int) enemy.getOwnBoard()
					.getShips()
					.stream()
					.filter(s -> !s.isSunk())
					.count();
			if (aliveShips >= 3 && random.nextDouble() < 0.30) {
				return new UseAbilityAction(
						AbilityType.EMP,
						new AbilityTarget(null, 0)
				);
			}
		}

		// 4) SHIELD: defensive, relatively rare overall.
		if (shield != null && shield.isAvailable()) {
			double pShield = earlyGame ? 0.25 : (midGame ? 0.15 : 0.05);
			if (random.nextDouble() < pShield) {
				Coordinate critical = findCriticalShipCoordinate(me);
				if (critical != null) {
					return new UseAbilityAction(
							AbilityType.SHIELD,
							new AbilityTarget(critical, 0)
					);
				}
			}
		}

		// Default: no ability this turn → fire
		return null;
	}


	private int countUnknownCells(Board tracking) {
		int unknown = 0;
		for(int r = 0; r < tracking.getRows(); r++){
			for(int c = 0; c < tracking.getCols(); c++) {
				CellState s = tracking.getCellState(new Coordinate(r,c));
				if(s == CellState.EMPTY) unknown++;
			}
		}
		return unknown;
	}

	private Coordinate randomUnknownCoordinate(Board tracking) {
    List<Coordinate> unknowns = new ArrayList<>();
    for (int r = 0; r < tracking.getRows(); r++) {
        for (int c = 0; c < tracking.getCols(); c++) {
            Coordinate coord = new Coordinate(r, c);
            if (tracking.getCellState(coord) == CellState.EMPTY) {
                unknowns.add(coord);
            }
        }
    }
    if (unknowns.isEmpty()) {
        // fallback: middle of board
        return new Coordinate(tracking.getRows() / 2, tracking.getCols() / 2);
    }
    return unknowns.get(random.nextInt(unknowns.size()));
}

	private Coordinate findCriticalShipCoordinate(PlayerState me) {
		// pick any ship that is not sunk ans has > 1 cell
		return me.getOwnBoard().getShips().stream()
			.filter(s -> !s.isSunk() && s.getType().getLength() >= 3)
			.findAny()
			.map(ship ->  ship.getCoordinates().get(0))
			.orElse(null);
	}

	private Coordinate chooseFireTarget(PlayerState me, PlayerState enemy) {
		Board tracking = me.getTrackingBoard();
		int rows = tracking.getRows();
		int cols = tracking.getCols();

		// target mode
		List<Coordinate> candidates = new ArrayList<>();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				Coordinate coord = new Coordinate(r, c);
				if (tracking.getCellState(coord) == CellState.HIT) {
					addNeighbors(tracking, coord, candidates);
				}
			}
		}
		if (!candidates.isEmpty()) {
			return candidates.get(random.nextInt(candidates.size()));
		}

		// hunt mode
		List<Coordinate> huntCells = new ArrayList<>();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if ((r + c) % 2 == 0) {
					Coordinate coord = new Coordinate(r, c);
					if (tracking.getCellState(coord) == CellState.EMPTY) {
						huntCells.add(coord);
					}
				}
			}
		}
		if (!huntCells.isEmpty()) {
			return huntCells.get(random.nextInt(huntCells.size()));
		}

		// fallback on empty cell
		List<Coordinate> empties = new ArrayList<>();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				Coordinate coord = new Coordinate(r, c);
				if (tracking.getCellState(coord) == CellState.EMPTY) {
					empties.add(coord);
				}
			}
		}
		if (!empties.isEmpty()) {
			return empties.get(random.nextInt(empties.size()));
		}

		// failsafe pick random
		int rr = random.nextInt(rows);
		int cc = random.nextInt(cols);
		return new Coordinate(rr, cc);
}


	private void addNeighbors(Board board, Coordinate hit, List<Coordinate> out) {
        int r = hit.row();
        int c = hit.col();
        int rows = board.getRows();
        int cols = board.getCols();

        int[][] dirs = {
                {-1, 0},
                { 1, 0},
                { 0,-1},
                { 0, 1}
        };

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];

            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                continue;
            }
            Coordinate cand = new Coordinate(nr, nc);
            CellState s = board.getCellState(cand);
            if (s == CellState.EMPTY && !out.contains(cand)) {
                out.add(cand);
            }
        }
    }
}
