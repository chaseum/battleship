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
		assert invariantCurrentPlayer() : "GameState: invariant failed in constructor";
	}

	private boolean invariantCurrentPlayer() {
        PlayerState current = getCurrentPlayer();
        PlayerState other = getOtherPlayer();
        return current != null && other != null && current != other;
    }

	public GameConfig getConfig() {
		return config;
	}

	public PlayerState getCurrentPlayer() {
		PlayerState current = player1Turn ? player1 : player2;
        assert current != null : "GameState: current player is null";
        return current;
	}

	public PlayerState getOtherPlayer() {
		PlayerState other = player1Turn ? player2 : player1;
        assert other != null : "GameState: other player is null";
        return other;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void endGame() {
		this.gameOver = true;
	}

	public void nextTurn() {
		assert !isGameOver() : "GameState: nextTurn called after game over";
        player1Turn = !player1Turn;
        getCurrentPlayer().tickTurn();
        assert invariantCurrentPlayer() : "GameState: invariant failed after nextTurn";
	}
}