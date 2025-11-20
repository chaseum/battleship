package com.chase.battleship.net;

import com.chase.battleship.core.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class OnlinePeerConnection {

    private final GameConfig config;
    private final PlayerState hostPlayer;
    private final PlayerState clientPlayer;
    private final boolean host;
    private final ServerSocket serverSocket;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String lobbyCode;

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
                socket = serverSocket.accept();
            } else {
                serverSocket = null;
                lobbyCode = null;
                if (joinCode == null || joinCode.isBlank()) {
                    throw new IllegalStateException("No join code provided");
                }
                RendezvousClient.HostEndpoint ep = RendezvousClient.resolveCode(joinCode);
                if (ep == null) {
                    throw new IllegalStateException("Join code not found on rendezvous server");
                }
                socket = new Socket(ep.host(), ep.port());
            }

            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            if (host) {
                sendInitialSetup();
            } else {
                receiveInitialSetup();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to establish online connection", e);
        }
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void sendInitialSetup() {
        out.println("HELLO HOST");
        out.println("CONFIG " + config.getRows() + " " + config.getCols());
        sendPlacements("P1", hostPlayer.getOwnBoard());
        sendPlacements("P2", clientPlayer.getOwnBoard());
        out.println("READY");
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
            if (line.startsWith("PLACE")) {
                String[] parts = line.split("\\s+");
                boolean forP1 = parts[1].equals("P1");
                ShipType type = ShipType.valueOf(parts[2]);
                int r = Integer.parseInt(parts[3]);
                int c = Integer.parseInt(parts[4]);
                boolean horizontal = parts[5].equals("H");
                Board targetBoard = forP1 ? hostPlayer.getOwnBoard() : clientPlayer.getOwnBoard();
                Ship ship = new Ship(type);
                targetBoard.placeShip(ship, new Coordinate(r, c), horizontal);
            }
            if ("READY".equals(line)) {
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

    public TurnAction waitForRemoteMove() {
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
        out.println("MOVE " + Protocol.formatAction(action));
    }

    public RemoteUpdate waitForUpdate(GameEngine engine) {
        try {
            TurnAction action = null;
            String message = "";
            boolean gameOver = false;
            while (true) {
                String line = in.readLine();
                if (line == null) throw new IOException("Disconnected");
                if (line.startsWith("APPLY ")) {
                    action = Protocol.parseClientCommand(line.substring(6));
                } else if (line.startsWith("MSG ")) {
                    message = line.substring(4);
                } else if (line.startsWith("OVER ")) {
                    gameOver = !line.endsWith("NONE");
                } else if (line.equals("STATE_END")) {
                    break;
                }
            }
            if (action == null) {
                throw new IllegalStateException("Host did not send action update");
            }
            TurnResult res = engine.processTurn(action);
            if (gameOver) {
                engine.getState().endGame();
            }
            return new RemoteUpdate(res, message);
        } catch (IOException e) {
            throw new IllegalStateException("Lost connection to peer", e);
        }
    }

    public void broadcastResult(TurnAction action, TurnResult result, GameState state) {
        out.println("APPLY " + Protocol.formatAction(action));
        out.println("MSG " + result.message());
        if (state.isGameOver()) {
            out.println("OVER " + state.getCurrentPlayer().getName());
        } else {
            out.println("OVER NONE");
        }
        out.println("STATE_END");
    }

    public record RemoteUpdate(TurnResult localResult, String remoteMessage) {}
}

