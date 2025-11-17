package com.chase.battleship.cli;

import com.chase.battleship.core.*;
import com.chase.battleship.ai.DecisionTreeAI;
import com.chase.battleship.ai.PlayerAgent;

public class SmokeTestMain {
	public static void main(String[] args) {
    GameConfig cfg = GameConfig.neoRetroDefault();
    int games = 100;
    int hostWins = 0;
    int clientWins = 0;
    int draws = 0; // cap hits

    for (int i = 0; i < games; i++) {
        System.out.println("Starting smoke test…");
        GameResult result = runOneGame(cfg);
        switch (result) {
            case HOST_WIN -> hostWins++;
            case CLIENT_WIN -> clientWins++;
            case DRAW_CAP -> draws++;
        }
    }

    System.out.printf(
        "Ran %d games. Host wins: %d, Client wins: %d, Cap-draws: %d%n",
        games, hostWins, clientWins, draws
    );
}

enum GameResult { HOST_WIN, CLIENT_WIN, DRAW_CAP }

private static GameResult runOneGame(GameConfig config) {
    Board b1 = new Board(config.getRows(), config.getCols());
    Board b2 = new Board(config.getRows(), config.getCols());
    BoardUtils.randomFleetPlacement(b1);
    BoardUtils.randomFleetPlacement(b2);

    PlayerState ps1 = new PlayerState("P1", b1, config);
    PlayerState ps2 = new PlayerState("P2", b2, config);
    GameState state = new GameState(config, ps1, ps2);
    GameEngine engine = new GameEngine(state, new DefaultAbilityExecutor());

    PlayerAgent a1 = new DecisionTreeAI();
    PlayerAgent a2 = new DecisionTreeAI();

    int cap = 500;
    while (!state.isGameOver() && cap-- > 0) {
        PlayerState current = state.getCurrentPlayer();
        PlayerAgent agent = (current == ps1) ? a1 : a2;
        TurnAction action = agent.chooseAction(state, current == ps1);
        engine.processTurn(action);
    }

    if (cap <= 0 && !state.isGameOver()) {
        System.out.println("!! TURN CAP HIT: likely infinite loop dummy");
        return GameResult.DRAW_CAP;
    }

    return state.getCurrentPlayer().getName().equals("P1")
            ? GameResult.HOST_WIN
            : GameResult.CLIENT_WIN;
	}
}