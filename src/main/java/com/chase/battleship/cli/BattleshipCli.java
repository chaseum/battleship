package com.chase.battleship.cli;

import com.chase.battleship.core.*;
import com.chase.battleship.ai.DecisionTreeAI;
import com.chase.battleship.ai.PlayerAgent;
import com.chase.battleship.net.OnlineHost;
import com.chase.battleship.net.OnlineClient;

import java.util.Scanner;

public class BattleshipCli {
	public static void main(String[] args) {
		new BattleshipCli().run();
	}

	private final Scanner scanner = new Scanner(System.in);

	public void run() {
		System.out.println("=== Neo-Retro Battleship === ");
		while(true) {
			System.out.println("1) Classic vs AI");
			System.out.println("2) Neo-Retro vs AI");
			System.out.println("3) Classic 2P Local");
			System.out.println("4) Neo-Retro 2P Local");
			System.out.println("5) Classic Online Host");
			System.out.println("6) Classic Online Join");
			System.out.println("7) Quit");
			System.out.print("Choice: ");
			String line = scanner.nextLine();

			switch(line) {
				case "1" -> startVsAi(GameConfig.classic());
				case "2" -> startVsAi(GameConfig.neoRetroDefault());
				case "3" -> startLocal2P(GameConfig.classic());
				case "4" -> startLocal2P(GameConfig.neoRetroDefault());
				case "5" -> startOnlineHost(GameConfig.classic());
				case "6" -> startOnlineJoin(GameConfig.classic());
				case "7" -> {
					System.out.println("Goodbye!");
					return;
				}
				default -> System.out.println("Invalid option. \n");
			}
		}
	}
	private void startVsAi(GameConfig config) {
		PlayerAgent humanAgent = new TerminalHumanAgent(scanner);
		PlayerAgent aiAgent = new DecisionTreeAI();
		runGame(config, "You", "AI", humanAgent, aiAgent);
	}
	
	private void startLocal2P(GameConfig config) {
		PlayerAgent p1 = new TerminalHumanAgent(scanner);
		PlayerAgent p2 = new TerminalHumanAgent(scanner);
		runGame(config, "Player 1", "Player 2", p1, p2);
	}

	private void runGame(GameConfig config, String p1Name, String p2Name, PlayerAgent p1Agent, PlayerAgent p2Agent) {
		Board b1 = new Board(config.getRows(), config.getCols());
		Board b2 = new Board(config.getRows(), config.getCols());

		// for now, random placement, allow manual w cli later
		BoardUtils.setupBoardInteractive(b1);
		// setupBoardInteractive(b2);
		// BoardUtils.randomFleetPlacement(b1);
		BoardUtils.randomFleetPlacement(b2);

		PlayerState ps1 = new PlayerState(p1Name, b1, config);
		PlayerState ps2 = new PlayerState(p2Name, b2, config);
		GameState state = new GameState(config, ps1, ps2);
		GameEngine engine = new GameEngine(state, new DefaultAbilityExecutor());

		PlayerAgent[] agents = { p1Agent, p2Agent };
		int currentIdx = 0;

		while(!state.isGameOver()) {
			PlayerState current = state.getCurrentPlayer();
			PlayerAgent agent = (current == ps1) ? p1Agent : p2Agent;

			printBoards(state, current == ps1);

			TurnAction action = agent.chooseAction(state, current == ps1);
			TurnResult result = engine.processTurn(action);

			System.out.println(result.message());
			System.out.println();
		}
	}

	private void startOnlineHost(GameConfig config) {
		OnlineHost host = new OnlineHost();
		try {
			host.hostGame(config, scanner);
		} catch (Exception e) {
			System.out.println("Error hosting game: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void startOnlineJoin(GameConfig config) {
		OnlineClient client = new OnlineClient();
		try {
			client.joinGame(config, scanner);
		} catch (Exception e) {
			System.out.println("Error joining game: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void printBoards(GameState state, boolean currentIsPlayer1) {
    	PlayerState current = state.getCurrentPlayer();

		System.out.println("=== " + current.getName() + "'s turn ===");
		System.out.println("Your board:");
		BoardPrinter.printBoard(current.getOwnBoard(), true);

		System.out.println("Enemy board (what you know):");
		BoardPrinter.printBoard(current.getTrackingBoard(), false);

		if (state.getConfig().getGameMode() == GameMode.NEO_RETRO &&
			current.getAbilities() != null) {

			System.out.println("Abilities:");
			current.getAbilities().getStatusMap().forEach((type, status) ->
					System.out.printf(" - %s: charges=%d, cooldown=%d%n",
							type, status.getCharges(), status.getCooldownRemaining()));

			if (current.abilitiesLocked()) {
				System.out.println(" -> Your abilities are currently EMP-locked!");
			}
		}
	}
}