package com.chase.battleship.core;

public class GameState {
	private final GameConfig config;
	private final PlayerState player1;
	private final PlayerState player2;
	private boolean player1Turn;
	private boolean gameOver;

	public GameState(GameConfig config, PlayerState p1, PlayerState p2) {
		this.config = config;
		this.player1 = p1;
		this.player2 = p2;
		this.player1Turn = true;
		this.gameOver = false;
	}

	public GameConfig getConfig() {
		return config;
	}

	public PlayerState getCurrentPlayer() {
		return player1Turn ? player1 : player2;
	}

	public PlayerState getOtherPlayer() {
		return player1Turn ? player2 : player1;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void endGame() {
		this.gameOver = true;
	}

	public void nextTurn() {
		player1Turn = !player1Turn;
		getCurrentPlayer().tickTurn();
	}
}