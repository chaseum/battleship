package com.chase.battleship.gui;

import com.chase.battleship.ai.DecisionTreeAI;
import com.chase.battleship.ai.PlayerAgent;
import com.chase.battleship.core.*;
import com.chase.battleship.net.OnlinePeerConnection;

public class GuiGameSession {

    public enum Mode {
        CLASSIC_VS_AI,
        NEORETRO_VS_AI,
        CLASSIC_LOCAL_2P,
        NEORETRO_LOCAL_2P,
        CLASSIC_ONLINE_HOST,
        NEORETRO_ONLINE_HOST,
        CLASSIC_ONLINE_CLIENT,
        NEORETRO_ONLINE_CLIENT
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

    private final PlayerState localPlayer;
    private final PlayerState remotePlayer;

    private final boolean online;
    private final boolean isHost;
    private final String lobbyCode;
    private final OnlinePeerConnection net;
    private final String joinCode;
    private Boolean localWon;
    private TurnAction lastRemoteAction;

    public GuiGameSession(Mode uiMode) {
        this(uiMode, null);
    }

    public GuiGameSession(Mode uiMode, String joinCode) {
         this.uiMode = uiMode;
        this.joinCode = joinCode;

                this.config = switch (uiMode) {
                        case CLASSIC_VS_AI, CLASSIC_LOCAL_2P, CLASSIC_ONLINE_HOST, CLASSIC_ONLINE_CLIENT -> GameConfig.classic();
                        case NEORETRO_VS_AI, NEORETRO_LOCAL_2P, NEORETRO_ONLINE_HOST, NEORETRO_ONLINE_CLIENT -> GameConfig.neoRetroDefault();
                };

                Board b1 = new Board(config.getRows(), config.getCols());
                Board b2 = new Board(config.getRows(), config.getCols());

                boolean randomize = switch (uiMode) {
                    case CLASSIC_ONLINE_HOST, NEORETRO_ONLINE_HOST,
                            CLASSIC_ONLINE_CLIENT, NEORETRO_ONLINE_CLIENT -> false;
                    default -> true;
                };
                if (randomize) {
                    BoardUtils.randomFleetPlacement(b1);
                    BoardUtils.randomFleetPlacement(b2);
                }

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
                localPlayer = p1;
                remotePlayer = p2;
                online = false;
                isHost = false;
                lobbyCode = null;
                net = null;
            }
            case CLASSIC_LOCAL_2P, NEORETRO_LOCAL_2P -> {
                tmpP1Human = true;
                tmpP2Human = true;
                tmpP1Agent = null;
                tmpP2Agent = null;
                localPlayer = p1;
                remotePlayer = p2;
                online = false;
                isHost = false;
                lobbyCode = null;
                net = null;
            }
            case CLASSIC_ONLINE_HOST, NEORETRO_ONLINE_HOST -> {
                tmpP1Human = true;
                tmpP2Human = false; // remote human
                tmpP1Agent = null;
                tmpP2Agent = null;
                localPlayer = p1;
                remotePlayer = p2;
                online = true;
                isHost = true;
                OnlinePeerConnection conn = new OnlinePeerConnection(config, p1, p2, true, null);
                net = conn;
                lobbyCode = conn.getLobbyCode();
            }
            case CLASSIC_ONLINE_CLIENT, NEORETRO_ONLINE_CLIENT -> {
                tmpP1Human = false; // host remote
                tmpP2Human = true;
                tmpP1Agent = null;
                tmpP2Agent = null;
                localPlayer = p2;
                remotePlayer = p1;
                online = true;
                isHost = false;
                OnlinePeerConnection conn = new OnlinePeerConnection(config, p1, p2, false, joinCode);
                net = conn;
                lobbyCode = null;
            }
            default -> throw new IllegalStateException("Unexpected value: " + uiMode);
        }

        this.p1IsHuman = tmpP1Human;
        this.p2IsHuman = tmpP2Human;
        this.p1Agent = tmpP1Agent;
        this.p2Agent = tmpP2Agent;
    }

    public PlayerState getLocalPlayer() {
        return localPlayer;
    }

    public PlayerState getRemotePlayer() {
        return remotePlayer;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isHost() {
        return isHost;
    }

    public String getLobbyCode() {
        return lobbyCode;
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

    /**
     * Waits for the remote player to join when hosting an online game.
     */
    public void waitForPeerReady() {
        if (online && net != null) {
            net.waitForPeerReady();
        }
    }

    /**
     * Randomly places the local fleet (used by GUI auto-setup).
     */
    public void randomizeLocalFleet() {
        BoardUtils.randomFleetPlacement(localPlayer.getOwnBoard());
    }

    /**
     * Sends the local placements to the peer and blocks until the peer also sends theirs.
     */
    public void syncPlacementsOnReady() {
        if (!online || net == null) return;
        if (localPlayer.getOwnBoard().getShips().isEmpty()) {
            BoardUtils.randomFleetPlacement(localPlayer.getOwnBoard());
        }
        net.exchangePlacementsOnReady(
                localPlayer.getOwnBoard(),
                isHost
        );
    }

    private void updateLocalWinFlagFromState() {
        if (state.getWinner() == null) {
            localWon = null;
        } else {
            localWon = (state.getWinner() == localPlayer);
        }
    }

    /**
     * Releases any network resources for the current session.
     */
    public void close() {
        if (net != null) {
            net.close();
        }
    }

    public Boolean isLocalWinner() {
        return localWon;
    }

    public TurnAction getLastRemoteAction() {
        return lastRemoteAction;
    }

    public boolean isCurrentPlayerHuman() {
        PlayerState current = state.getCurrentPlayer();
        if (online) {
            return current == localPlayer;
        }
        if (current == p1) {
            return p1IsHuman;
        } else if (current == p2) {
            return p2IsHuman;
        }
        return true;
    }

    public TurnResult processHumanAction(TurnAction action) {
        if (online) {
            if (isHost) {
                TurnResult res = engine.processTurn(action);
                net.broadcastResult(action, res, state);
                if (state.isGameOver()) {
                    updateLocalWinFlagFromState();
                }
                return res;
            } else {
                net.sendLocalMove(action);
                OnlinePeerConnection.RemoteUpdate update = net.waitForUpdate(engine);
                if (update.winner() != null) {
                    state.setWinner(update.winner());
                    updateLocalWinFlagFromState();
                }
                return new TurnResult(update.localResult().success(), update.remoteMessage(), state);
            }
        }
        TurnResult res = engine.processTurn(action);
        if (state.isGameOver()) {
            updateLocalWinFlagFromState();
        }
        return res;
    }

    public TurnResult maybeLetAiAct() {
        if (state.isGameOver()) return null;

        PlayerState current = state.getCurrentPlayer();
        boolean isP1 = (current == p1);
        PlayerAgent agent = isP1 ? p1Agent : p2Agent;

        if (online) {
            if (isHost) {
                if (current == remotePlayer) {
                    TurnAction action = net.waitForRemoteMove();
                    lastRemoteAction = action;
                    TurnResult res = engine.processTurn(action);
                    net.broadcastResult(action, res, state);
                    if (state.isGameOver()) {
                        updateLocalWinFlagFromState();
                    }
                    return new TurnResult(res.success(), "Enemy: " + res.message(), state);
                }
                return null;
            } else {
                OnlinePeerConnection.RemoteUpdate update = net.waitForUpdate(engine);
                lastRemoteAction = update.action();
                if (update.winner() != null) {
                    state.setWinner(update.winner());
                    updateLocalWinFlagFromState();
                }
                return new TurnResult(update.localResult().success(), update.remoteMessage(), state);
            }
        }

        if (agent == null) {
            return null;
        }

        TurnAction aiAction = agent.chooseAction(state, isP1);
        return engine.processTurn(aiAction);
    }
}
