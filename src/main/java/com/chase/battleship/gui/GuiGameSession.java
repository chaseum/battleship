package com.chase.battleship.gui;

import com.chase.battleship.ai.DecisionTreeAI;
import com.chase.battleship.ai.PlayerAgent;
import com.chase.battleship.core.*;

public class GuiGameSession {

    public enum Mode {
        CLASSIC_VS_AI,
        NEORETRO_VS_AI,
        CLASSIC_LOCAL_2P,
        NEORETRO_LOCAL_2P
    }

    private final Mode uiMode;
    private final GameConfig config;
    private final GameState state;
    private final GameEngine engine;

    private final PlayerState p1;
    private final PlayerState p2;

    private final boolean p1IsHuman;
    private final boolean p2IsHuman;
    private final PlayerAgent p1Agent;
    private final PlayerAgent p2Agent;

    public GuiGameSession(Mode uiMode) {
         this.uiMode = uiMode;

		this.config = switch (uiMode) {
			case CLASSIC_VS_AI, CLASSIC_LOCAL_2P -> GameConfig.classic();
			case NEORETRO_VS_AI, NEORETRO_LOCAL_2P -> GameConfig.neoRetroDefault();
		};

		Board b1 = new Board(config.getRows(), config.getCols());
		Board b2 = new Board(config.getRows(), config.getCols());

		// Use your existing helper – no fleet parameter
		BoardUtils.randomFleetPlacement(b1);
		BoardUtils.randomFleetPlacement(b2);

		PlayerState ps1 = new PlayerState("Player 1", b1, config);
		PlayerState ps2 = new PlayerState("Player 2", b2, config);
		this.p1 = ps1;
		this.p2 = ps2;

		this.state = new GameState(config, ps1, ps2);
		this.engine = new GameEngine(state, new DefaultAbilityExecutor());

        boolean tmpP1Human;
        boolean tmpP2Human;
        PlayerAgent tmpP1Agent;
        PlayerAgent tmpP2Agent;

        switch (uiMode) {
            case CLASSIC_VS_AI, NEORETRO_VS_AI -> {
                tmpP1Human = true;
                tmpP2Human = false;
                tmpP1Agent = null;
                tmpP2Agent = new DecisionTreeAI();
            }
            case CLASSIC_LOCAL_2P, NEORETRO_LOCAL_2P -> {
                tmpP1Human = true;
                tmpP2Human = true;
                tmpP1Agent = null;
                tmpP2Agent = null;
            }
            default -> throw new IllegalStateException("Unexpected value: " + uiMode);
        }

        this.p1IsHuman = tmpP1Human;
        this.p2IsHuman = tmpP2Human;
        this.p1Agent = tmpP1Agent;
        this.p2Agent = tmpP2Agent;
    }

    public Mode getUiMode() {
        return uiMode;
    }

    public GameConfig getConfig() {
        return config;
    }

    public GameState getState() {
        return state;
    }

    public GameEngine getEngine() {
        return engine;
    }

    public PlayerState getP1() {
        return p1;
    }

    public PlayerState getP2() {
        return p2;
    }

    public boolean isP1Human() {
        return p1IsHuman;
    }

    public boolean isP2Human() {
        return p2IsHuman;
    }

    public boolean isCurrentPlayerHuman() {
        PlayerState current = state.getCurrentPlayer();
        if (current == p1) {
            return p1IsHuman;
        } else if (current == p2) {
            return p2IsHuman;
        }
        return true;
    }

    public TurnResult processHumanAction(TurnAction action) {
        return engine.processTurn(action);
    }

    public TurnResult maybeLetAiAct() {
        if (state.isGameOver()) return null;

        PlayerState current = state.getCurrentPlayer();
        boolean isP1 = (current == p1);
        PlayerAgent agent = isP1 ? p1Agent : p2Agent;

        if (agent == null) {
            return null;
        }

        TurnAction aiAction = agent.chooseAction(state, isP1);
        return engine.processTurn(aiAction);
    }
}
