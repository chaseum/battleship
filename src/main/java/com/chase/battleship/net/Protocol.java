package com.chase.battleship.net;

import com.chase.battleship.core.*;

public final class Protocol {
	private Protocol() {}

	public static TurnAction parseClientCommand(String line) {
		String[] parts = line.trim().split("\\s+");
		if(parts.length == 0) {
			throw new IllegalArgumentException("Empty command");
		}
		switch(parts[0]) {
			case "F" -> {
				int r = Integer.parseInt(parts[1]);
				int c = Integer.parseInt(parts[2]);
				return new FireAction(new Coordinate(r,c));
			}
			case "A" -> {
				AbilityType type = AbilityType.valueOf(parts[1]);
				if(type == AbilityType.SHIELD) {
					int r = Integer.parseInt(parts[2]);
					int c = Integer.parseInt(parts[3]);
					return new UseAbilityAction(type, new AbilityTarget(new Coordinate(r,c), 0));
				} else if(type == AbilityType.MULTISHOT) {
					int n = Integer.parseInt(parts[2]);
					return new UseAbilityAction(type, new AbilityTarget(null, n));
				} else if(type == AbilityType.SONAR) {
					int r = Integer.parseInt(parts[2]);
					int c = Integer.parseInt(parts[3]);
					return new UseAbilityAction(type, new AbilityTarget(new Coordinate(r,c),0));
				} else {
					return new UseAbilityAction(type, new AbilityTarget(null, 0));
				}
			}
			default -> throw new IllegalArgumentException("Unknown command: "+ parts[0]);
		}
	}
}