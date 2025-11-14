package com.chase.battleship.ai;

import com.chase.battleship.core.GameState;
import com.chase.battleship.core.TurnAction;

public interface PlayerAgent {
	TurnAction chooseAction(GameState gameState, boolean isSelfPlayer1);
}