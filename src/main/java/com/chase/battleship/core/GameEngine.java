package com.chase.battleship.core;

public class GameEngine {
	private final GameState gameState;
	private final AbilityExecutor abilityExecutor;
	public GameEngine(GameState state, AbilityExecutor abilityExecutor) {
		this.gameState = state;
		this.abilityExecutor = abilityExecutor;
	}
	
	public GameState getGameState() {
		return gameState;
	}

	public TurnResult processTurn(TurnAction action) {
		if(gameState.isGameOver()) {
			return new TurnResult(false, "Game already over", null);
		}
		
		PlayerState current = gameState.getCurrentPlayer();
		PlayerState other = gameState.getOtherPlayer();

		String msg;
		if(action instanceof FireAction fire) {
			msg = handleFire(current, other, fire.target());
		} else if(action instanceof UseAbilityAction use) {
			msg = handleAbility(current, other, use);
		} else {
			msg = "Unknown action";
		}

		if(other.getOwnBoard().allShipsSunk()) {
			gameState.endGame();
			msg += "\n" + current.getName() + " wins!";
		} else {
			gameState.nextTurn();
		}
		return new TurnResult(true, msg, gameState);
	}

	private String handleFire(PlayerState current, PlayerState other, Coordinate target) {
		ShotOutcome outcome = other.getOwnBoard().fireAt(target);

		// update tracking board only if we fire inside bounds & not duplicate
		if(outcome == ShotOutcome.MISS || outcome == ShotOutcome.HIT || outcome == ShotOutcome.SUNK || outcome == ShotOutcome.SHIELDED_HIT) {
			
			CellState mark = (outcome == ShotOutcome.MISS) ? CellState.MISS : CellState.HIT;
			Board tracking = current.getTrackingBoard();
			if(tracking.inBounds(target)) {
				tracking.markSeen(target,mark);
			}
		}

		return "Fired at " + target.row() + "," +target.col() + " => " + outcome;
	}

	private String handleAbility(PlayerState current, PlayerState other, UseAbilityAction use) {
		if(gameState.getConfig().getGameMode() != GameMode.NEO_RETRO) {
			return "Abilities not available in Classic mode";
		}

		if(current.abilitiesLocked()) {
			return "Your abilities are disabled by EMP!";
		}

		AbilityRule rule = gameState.getConfig().getAbilityRules().get(use.abilityType());
		AbilityStatus status = current.getAbilities().getStatus(use.abilityType());

		if(status == null) {
			return "Ability not configured";
		}
		if(!status.isAvailable()) {
			return "Ability on cooldown or out of charges";
		}

		status.consume(rule);
		AbilityResult res = abilityExecutor.execute(
			use.abilityType(),
			gameState,
			current,
			other,
			use.target()
		);

		return "Used " + use.abilityType() + ": " +res.description();
	}
}