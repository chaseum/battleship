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
		if(gameState.getConfig().getGameMode() == GameMode.NEO_RETRO) {
			TurnAction abilityAction = maybeUseAbility(gameState, me, enemy);
			if(abilityAction != null) {
				return abilityAction;
			}
		}

		Coordinate target = chooseFireTarget(me, enemy);
		return new FireAction(target);
	}

	private TurnAction maybeUseAbility(GameState gameState, PlayerState me, PlayerState enemy) {
		if(me.abilitiesLocked() || me.getAbilities() == null) {
			return null;
		}

		PlayerAbilities abilities = me.getAbilities();
		Board tracking = me.getTrackingBoard();

		//sonar early game info gather
		AbilityStatus sonar = abilities.getStatus(AbilityType.SONAR);
		if(sonar != null && sonar.isAvailable()) {
			int unknown = countUnknownCells(tracking);
			int total = tracking.getRows() * tracking.getCols();
			if(unknown > total * 0.7) { // only when there's low awareness 
				Coordinate center = randomUnknownCoordinate(tracking);
				return new UseAbilityAction(
					AbilityType.SONAR,
					new AbilityTarget(center, 0)
				);
			}
		}
		// to explain, an example of a heuristic:
		// use shield if there is a large ship unshielded and enemy has many hits on it
		AbilityStatus shield = abilities.getStatus(AbilityType.SHIELD);
		if(shield != null && shield.isAvailable()) {
			Coordinate critical = findCriticalShipCoordinate(me);
			if(critical != null) {
				return new UseAbilityAction(AbilityType.SHIELD, new AbilityTarget(critical, 0));
			}
		}

		// use multishot early when many cells are unknown
		AbilityStatus multiStatus = me.getAbilities().getStatus(AbilityType.MULTISHOT);
		if(multiStatus != null && multiStatus.isAvailable()) {
			int unknown = countUnknownCells(me.getTrackingBoard());
			int total = me.getTrackingBoard().getRows() * me.getTrackingBoard().getCols();
			if(unknown > total * 0.5) {
				return new UseAbilityAction(AbilityType.MULTISHOT, new AbilityTarget(null, 3));
			}
		}

		// use emp if enemy has lots of ships alive and we think they're using abilities (placeholder)
		AbilityStatus empStatus = me.getAbilities().getStatus(AbilityType.EMP);
		if(empStatus != null && empStatus.isAvailable()) {
			int aliveShips = (int) enemy.getOwnBoard().getShips().stream().filter(s -> !s.isSunk()).count();
			if(aliveShips >= 3) {
				return new UseAbilityAction(AbilityType.EMP, new AbilityTarget(null, 0));
			}
		}
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
		// looking for adj to hits
		List<Coordinate> candidates = new ArrayList<>();
		Board tracking = me.getTrackingBoard();

		for(int r = 0; r < tracking.getRows(); r++){
			for(int c = 0; c < tracking.getCols(); c++) {
				Coordinate coord = new Coordinate(r,c);
				if(tracking.getCellState(coord) == CellState.HIT) {
					addNeighbors(tracking, coord, candidates);
				}
			}
		}
		if(!candidates.isEmpty()) {
			return candidates.get(random.nextInt(candidates.size()));
		}

		// hunt mode: fire random cell that is still empty
		List<Coordinate> huntCells = new ArrayList<>();
		for(int r = 0; r < tracking.getRows(); r++) {
			for(int c = 0; c < tracking.getCols(); c++) {
				Coordinate coord = new Coordinate(r,c);
				if((r+c)%2 == 0 && tracking.getCellState(coord) == CellState.EMPTY) {
					huntCells.add(coord);
				}
			}
		}
		if(huntCells.isEmpty()) {
			// any empty cell
			for(int r = 0; r < tracking.getRows(); r++) {
				for(int c = 0; c < tracking.getCols(); c++) {
					Coordinate coord = new Coordinate(r,c);
					if(tracking.getCellState(coord) == CellState.EMPTY) {
						huntCells.add(coord);
					}
				}
			}
		}
		return huntCells.get(random.nextInt(huntCells.size()));
	}

	private void addNeighbors(Board b, Coordinate hit, List<Coordinate> out) {
		int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };
		for(int[] d : dirs) {
			Coordinate c = new Coordinate(hit.row() + d[0], hit.col() + d[1]);
			if(b.inBounds(c) && b.getCellState(c) == CellState.EMPTY) {
				out.add(c);
			}
		}
	}
}