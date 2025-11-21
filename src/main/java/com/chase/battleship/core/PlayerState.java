package com.chase.battleship.core;

public class PlayerState {
	private final String name;
	private final Board ownBoard;
	private final Board trackingBoard; // what player knows about opp
	private final PlayerAbilities abilities;
	private int empLockTurnsRemaining; // if > 0, abilities disabled

	public PlayerState(String name, Board ownBoard, GameConfig config) {
		this.name = name;
		this.ownBoard = ownBoard;
		this.trackingBoard = new Board(ownBoard.getRows(), ownBoard.getCols());
		this.abilities = config.getGameMode().isNeoRetro()
			? new PlayerAbilities(config.getAbilityRules())
			: null;
		this.empLockTurnsRemaining = 0;
	}

	public String getName() { return name; }
	public Board getOwnBoard() { return ownBoard; }
	public Board getTrackingBoard() { return trackingBoard; }
	public PlayerAbilities getAbilities() { return abilities; }

	public boolean abilitiesLocked() {
		return empLockTurnsRemaining > 0;
	}

	public void applyEmpLock(int turns) {
		this.empLockTurnsRemaining = Math.max(this.empLockTurnsRemaining, turns);
	}

	public void tickTurn() {
		if(empLockTurnsRemaining > 0) {
			empLockTurnsRemaining--;
		}
		if(abilities != null) {
			abilities.tickAll();
		}
	}
}
