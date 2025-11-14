package com.chase.battleship.core;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

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

	private AbilityResult doMultishot(GameState gameState, PlayerState user, PlayerState opponent, AbilityTarget target) {
		int shots = Math.max(1, target.extraShots());
		int hits = 0;
		for(int i = 0; i < shots; i++) {
			// for now pick random new targets, let CLI fit coords
			Coordinate c = randomCoordinate(opponent.getOwnBoard());
			ShotOutcome outcome = opponent.getOwnBoard().fireAt(c);
			if(outcome == ShotOutcome.HIT || outcome == ShotOutcome.SUNK || outcome == ShotOutcome.SHIELDED_HIT) {
				hits++;
			}
		}
		return new AbilityResult("Multishot fired "+ shots + " shots, hits = " + hits);
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
				Coordinate c = new Coordinate(center.row() + dr, center.col() + dc);
				if(!enemyBoard.inBounds(c)) continue;
				if(enemyBoard.findShipAt(c).isPresent()) {
					hits.add(c);
				}
			}
		}
		if(hits.isEmpty()) {
			return new AbilityResult(String.format(
				"Sonar scan at (%d,$d) found no ship segments in the 3x3 area.",
				center.row(), center.col()
			));
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

		return new AbilityResult(sb.toString());
	}
}