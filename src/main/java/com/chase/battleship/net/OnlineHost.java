package com.chase.battleship.net;

import com.chase.battleship.ai.PlayerAgent;
import com.chase.battleship.cli.TerminalHumanAgent;
import com.chase.battleship.core.*;
import com.chase.battleship.cli.BoardPrinter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class OnlineHost {

    public void hostGame(GameConfig config, Scanner scanner) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) { // 0 = free port
            int port = serverSocket.getLocalPort();
            String code = NetUtil.makeCode(6);
            RendezvousClient.registerCode(code, port);
            System.out.println("Hosting game. Share this code with your friend: " + code);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected from " + clientSocket.getInetAddress());

            try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                out.println("MSG Connected to host. Game starting soon...");

                // host setup
                Board b1 = new Board(config.getRows(), config.getCols());
                Board b2 = new Board(config.getRows(), config.getCols());
                BoardUtils.randomFleetPlacement(b1);
                BoardUtils.randomFleetPlacement(b2);

                PlayerState ps1 = new PlayerState("Host", b1, config);
                PlayerState ps2 = new PlayerState("Client", b2, config);
                GameState state = new GameState(config, ps1, ps2);
                GameEngine engine = new GameEngine(state, new DefaultAbilityExecutor());

                PlayerAgent hostAgent = new TerminalHumanAgent(scanner);

                // turn loop
                while (!state.isGameOver()) {
                    PlayerState current = state.getCurrentPlayer();
                    PlayerState other   = state.getOtherPlayer();

                    if (current == ps1) {
                        // host using local cli
                        System.out.println("=== Your turn (Host) ===");
                        printBoardsForHost(state);

						sendBoardsToClientFor(state, ps2, out); 

                        TurnAction action = hostAgent.chooseAction(state, true);
                        TurnResult result = engine.processTurn(action);

                        // pass to client
                        out.println("MSG " + result.message());

                        if (state.isGameOver()) {
                            out.println("OVER " + current.getName());
                            System.out.println("Game over. Winner: " + current.getName());
                            break;
                        }
                    } else {
                        // client turn
						sendBoardsToClientFor(state, ps2, out);

                        out.println("PROMPT_MOVE");
                        String cmd = in.readLine();
                        if (cmd == null) {
                            System.out.println("Client disconnected.");
                            break;
                        }

                        try {
                            TurnAction action = Protocol.parseClientCommand(cmd);
                            TurnResult result = engine.processTurn(action);

                            // pass to client
                            out.println("MSG " + result.message());

                            if (state.isGameOver()) {
                                out.println("OVER " + current.getName());
                                System.out.println("Game over. Winner: " + current.getName());
                                break;
                            }
                        } catch (Exception ex) {
                            out.println("MSG Invalid command: " + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void printBoardsForHost(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        System.out.println("Your board:");
        BoardPrinter.printBoard(current.getOwnBoard(), true);
        System.out.println("Enemy board (what you know):");
        BoardPrinter.printBoard(current.getTrackingBoard(), false);
    }

	private void sendBoardsToClientFor(GameState state, PlayerState clientViewPlayer, PrintWriter out) {
		Board own = clientViewPlayer.getOwnBoard();
		Board tracking = clientViewPlayer.getTrackingBoard();

		out.println("BOWN_BEGIN");
		BoardPrinter.renderBoard(own, true).forEach(line -> out.println("BOWN " + line));
		out.println("BOWN_END");

		out.println("BTRK_BEGIN");
		BoardPrinter.renderBoard(tracking, false).forEach(line -> out.println("BTRK " + line));
		out.println("BTRK_END");
	}
}
