package com.chase.battleship.net;

import com.chase.battleship.core.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class OnlinePeerConnection {

    private final GameConfig config;
    private final PlayerState hostPlayer;
    private final PlayerState clientPlayer;
    private final boolean host;
    private final ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String lobbyCode;
    private final CountDownLatch readyLatch = new CountDownLatch(1);
    private IOException connectionFailure;

    public OnlinePeerConnection(GameConfig config, PlayerState hostPlayer, PlayerState clientPlayer, boolean host, String joinCode) {
        this.config = config;
        this.hostPlayer = hostPlayer;
        this.clientPlayer = clientPlayer;
        this.host = host;

        try {
            if (host) {
                serverSocket = new ServerSocket(0);
                lobbyCode = NetUtil.makeCode(6);
                RendezvousClient.registerCode(lobbyCode, serverSocket.getLocalPort());
                startAcceptThread();
            } else {
                serverSocket = null;
                lobbyCode = null;
                if (joinCode == null || joinCode.isBlank()) {
                    throw new IllegalStateException("No join code provided");
                }
                connectToHost(joinCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to establish online connection", e);
        }
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    /**
     * Blocks until the remote peer has connected and the initial setup exchange is complete.
     * For hosts this waits for the client to join; for clients it returns immediately because
     * the constructor already performed the handshake.
     */
    public void waitForPeerReady() {
        awaitReady();
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {}
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void startAcceptThread() throws IOException {
        Thread t = new Thread(() -> {
            try {
                socket = serverSocket.accept();
                initIo();
                sendInitialSetup();
            } catch (IOException e) {
                connectionFailure = e;
            } finally {
                readyLatch.countDown();
            }
        }, "online-host-accept");
        t.setDaemon(true);
        t.start();
    }

    private void connectToHost(String joinCode) throws IOException {
        RendezvousClient.HostEndpoint ep = RendezvousClient.resolveCode(joinCode);
        if (ep == null) {
            throw new IllegalStateException("Join code not found on rendezvous server");
        }
        socket = new Socket(ep.host(), ep.port());
        initIo();
        receiveInitialSetup();
        readyLatch.countDown();
    }

    private void initIo() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    private void awaitReady() {
        try {
            readyLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for peer connection", e);
        }
        if (connectionFailure != null) {
            throw new IllegalStateException("Failed to establish online connection", connectionFailure);
        }
    }

    private void sendInitialSetup() {
        out.println("HELLO HOST");
        out.println("CONFIG " + config.getRows() + " " + config.getCols());
        out.println("MODE " + config.getGameMode());
    }

    private void receiveInitialSetup() throws IOException {
        String line = in.readLine();
        if (line == null || !line.startsWith("HELLO")) {
            throw new IOException("Invalid hello from host");
        }
        while ((line = in.readLine()) != null) {
            if (line.startsWith("CONFIG")) {
                // rows/cols already match via config
                continue;
            }
            if (line.startsWith("MODE")) {
                String mode = line.substring(5).trim();
                if (!mode.equals(config.getGameMode().name())) {
                    throw new IOException("Mode mismatch: host is " + mode + ", client is " + config.getGameMode());
                }
                break;
            }
        }
    }

    private void sendPlacements(String prefix, Board board) {
        for (Ship ship : board.getShips()) {
            ShipPlacement placement = ShipPlacement.fromShip(ship);
            out.println("PLACE " + prefix + " " + placement.type() + " "
                    + placement.start().row() + " " + placement.start().col() + " "
                    + (placement.horizontal() ? "H" : "V"));
        }
    }

    /**
     * Called after both players click Ready. Sends this player's placements and waits for the peer's
     * placements so both sides share identical board state.
     */
    public void exchangePlacementsOnReady(Board localBoard, boolean senderIsHost) {
        awaitReady();
        sendReady(localBoard, senderIsHost);
        waitForReadyPayload();
    }

    private void sendReady(Board localBoard, boolean senderIsHost) {
        String prefix = senderIsHost ? "P1" : "P2";
        out.println("READY " + prefix);
        sendPlacements(prefix, localBoard);
        out.println("READY_END");
    }

    private void waitForReadyPayload() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.startsWith("READY")) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    throw new IllegalStateException("READY missing target player");
                }
                String target = parts[1];
                Board targetBoard = target.equals("P1") ? hostPlayer.getOwnBoard() : clientPlayer.getOwnBoard();
                targetBoard.reset();
                while ((line = in.readLine()) != null) {
                    if ("READY_END".equals(line)) {
                        return;
                    }
                    if (line.startsWith("PLACE")) {
                        String[] p = line.split("\\s+");
                        ShipType type = ShipType.valueOf(p[2]);
                        int r = Integer.parseInt(p[3]);
                        int c = Integer.parseInt(p[4]);
                        boolean horizontal = p[5].equals("H");
                        Ship ship = new Ship(type);
                        targetBoard.placeShip(ship, new Coordinate(r, c), horizontal);
                    }
                }
            }
            throw new IllegalStateException("Peer disconnected before sending READY");
        } catch (IOException e) {
            throw new IllegalStateException("Lost connection to peer", e);
        }
    }

    public TurnAction waitForRemoteMove() {
        awaitReady();
        try {
            String line = in.readLine();
            if (line == null || !line.startsWith("MOVE ")) {
                throw new IllegalStateException("Unexpected remote message: " + line);
            }
            return Protocol.parseClientCommand(line.substring(5));
        } catch (IOException e) {
            throw new IllegalStateException("Lost connection to peer", e);
        }
    }

    public void sendLocalMove(TurnAction action) {
        awaitReady();
        out.println("MOVE " + Protocol.formatAction(action));
    }

    public RemoteUpdate waitForUpdate(GameEngine engine) {
        awaitReady();
        try {
            TurnAction action = null;
            String message = "";
            boolean gameOver = false;
            String winnerTag = null;
            while (true) {
                String line = in.readLine();
                if (line == null) throw new IOException("Disconnected");
                if (line.startsWith("APPLY ")) {
                    action = Protocol.parseClientCommand(line.substring(6));
                } else if (line.startsWith("MSG ")) {
                    message = line.substring(4);
                } else if (line.startsWith("OVER ")) {
                    gameOver = !line.endsWith("NONE");
                    winnerTag = line.substring(5).trim();
                } else if (line.equals("STATE_END")) {
                    break;
                }
            }
            if (action == null) {
                throw new IllegalStateException("Host did not send action update");
            }
            TurnResult res = engine.processTurn(action);
            if (gameOver) {
                engine.getGameState().endGame();
                if (winnerTag != null && !"NONE".equals(winnerTag)) {
                    PlayerState winner = winnerTag.equals("P1") ? hostPlayer : clientPlayer;
                    engine.getGameState().setWinner(winner);
                }
            }
            PlayerState winner = gameOver && engine.getGameState().getWinner() != null
                    ? engine.getGameState().getWinner()
                    : null;
            return new RemoteUpdate(res, message, winner, action);
        } catch (IOException e) {
            throw new IllegalStateException("Lost connection to peer", e);
        }
    }

    public void broadcastResult(TurnAction action, TurnResult result, GameState state) {
        awaitReady();
        out.println("APPLY " + Protocol.formatAction(action));
        out.println("MSG " + result.message());
        if (state.isGameOver()) {
            PlayerState w = state.getWinner();
            String winnerTag = w == null ? "NONE" : (w == hostPlayer ? "P1" : "P2");
            out.println("OVER " + winnerTag);
        } else {
            out.println("OVER NONE");
        }
        out.println("STATE_END");
    }

    public record RemoteUpdate(TurnResult localResult, String remoteMessage, PlayerState winner, TurnAction action) {}
}

