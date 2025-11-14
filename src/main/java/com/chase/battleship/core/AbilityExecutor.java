package com.chase.battleship.core;

public interface AbilityExecutor {
	AbilityResult execute(
		AbilityType type,
		GameState gameState,
		PlayerState user,
		PlayerState opponent,
		AbilityTarget target
	);
}