package com.chase.battleship.cli;

import com.chase.battleship.core.*;
import com.chase.battleship.ai.PlayerAgent;

import java.util.Scanner;

public class TerminalHumanAgent implements PlayerAgent {
	private final Scanner scanner;

	public TerminalHumanAgent(Scanner scanner) {
		this.scanner = scanner;
	}

	@Override
	public TurnAction chooseAction(GameState gameState, boolean ignored) {
		PlayerState me = gameState.getCurrentPlayer();
		GameConfig cfg = gameState.getConfig();

		if(cfg.getGameMode() == GameMode.NEO_RETRO && me.getAbilities() != null && !me.abilitiesLocked()) {
			System.out.print("Choose action: (F)ire or (A)bility? ");
			String line = scanner.nextLine().trim().toUpperCase();
			if(line.startsWith("A")) {
				return chooseAbilityAction(gameState);
			}
		}
		return chooseFire();
	}

	private TurnAction chooseFire() {
		System.out.print("Enter target (row col): ");
		String[] parts = scanner.nextLine().trim().split("\\s+");
		int r = Integer.parseInt(parts[0]);
		int c = Integer.parseInt(parts[1]);
		return new FireAction(new Coordinate(r,c));
	}

	private TurnAction chooseAbilityAction(GameState gameState) {
		PlayerState me = gameState.getCurrentPlayer();

		System.out.print("Which ability (EMP, Multishot, Shield, Sonar): ");
		String abilStr = scanner.nextLine().trim().toUpperCase();
		AbilityType type = AbilityType.valueOf(abilStr);

		AbilityTarget target = null;
		if(type == AbilityType.SHIELD) {
			System.out.print("Shield coordinate (row col) on your board: ");
			String[] parts = scanner.nextLine().trim().split("\\s+");
			target = new AbilityTarget(
				new Coordinate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
				0
			);	
		} else if (type == AbilityType.MULTISHOT) {
			System.out.print("How many exra shots (e.g., 3): ");
			int extra = Integer.parseInt(scanner.nextLine().trim());
			target = new AbilityTarget(null, extra);
		} else if (type == AbilityType.SONAR) {
			System.out.print("Sonar center coordinate on enemy board (row col): ");
			String[] parts = scanner.nextLine().trim().split("\\s+");
			target = new AbilityTarget(
				new Coordinate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])), 
				0
			);
		} else {
			target = new AbilityTarget(null, 0);
		}

		return new UseAbilityAction(type, target);
	}
}