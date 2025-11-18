package com.chase.battleship.net;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import com.chase.battleship.core.*;

public final class NetUtil {
	private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private NetUtil() {}

	public static String makeCode(int len) {
		StringBuilder sb = new StringBuilder(len);
		Random r = new Random();
		for (int i = 0; i < len; i++) {
			sb.append(CODE_CHARS.charAt(r.nextInt(CODE_CHARS.length())));
		}
		return sb.toString();
	}

	// TODO: implement ip <-> code?
	// public static String ipToCode(String ip) {}
	// public static String codeToIp(String code) {}

	public static int clamp(int min, int num, int max) {
		return Math.max(min, Math.min(num, max));
	}

	public static String encodeAction(TurnAction action) {
		if (action instanceof FireAction f) {
			Coordinate c = f.target();
			return "F " + c.row() + " " + c.col();
		}
		if (action instanceof UseAbilityAction a) {
			AbilityType type = a.abilityType();
			AbilityTarget tgt = a.target();

			StringBuilder sb = new StringBuilder();
			sb.append("A ").append(type.name());

			if (type == AbilityType.MULTISHOT) {
				List<Coordinate> manual = tgt.manualTargets();
				if (manual != null && !manual.isEmpty()) {
					for (Coordinate c : manual) {
						sb.append(' ').append(c.row()).append(' ').append(c.col());
					}
				} else if (tgt.extraShots() > 0) {
					sb.append(" AUTO ").append(tgt.extraShots());
				}
			} else {
				if (tgt.coordinate() != null) {
					sb.append(' ')
					.append(tgt.coordinate().row())
					.append(' ')
					.append(tgt.coordinate().col());
				}
			}
			return sb.toString();
		}
		throw new IllegalArgumentException("Unknown TurnAction: " + action);
	}

	public static TurnAction decodeAction(String line) {
		String[] parts = line.trim().split("\\s+");
		if (parts.length == 0) {
			throw new IllegalArgumentException("Empty action line");
		}

		if (parts[0].equals("F")) {
			int r = Integer.parseInt(parts[1]);
			int c = Integer.parseInt(parts[2]);
			return new FireAction(new Coordinate(r, c));
		}

		if (parts[0].equals("A")) {
			AbilityType type = AbilityType.valueOf(parts[1]);

			if (type == AbilityType.MULTISHOT) {
				if (parts.length >= 4 && parts[2].equalsIgnoreCase("AUTO")) {
					int shots = Integer.parseInt(parts[3]);
					return new UseAbilityAction(
							AbilityType.MULTISHOT,
							new AbilityTarget(null, shots)
					);
				} else {
					List<Coordinate> coords = new ArrayList<>();
					for (int i = 2; i + 1 < parts.length; i += 2) {
						int r = Integer.parseInt(parts[i]);
						int c = Integer.parseInt(parts[i + 1]);
						coords.add(new Coordinate(r, c));
					}
					return new UseAbilityAction(
							AbilityType.MULTISHOT,
							new AbilityTarget(coords)
					);
				}
			} else {
				Coordinate coord = null;
				if (parts.length >= 4) {
					int r = Integer.parseInt(parts[2]);
					int c = Integer.parseInt(parts[3]);
					coord = new Coordinate(r, c);
				}
				return new UseAbilityAction(
						type,
						new AbilityTarget(coord, 0)
				);
			}
		}

		throw new IllegalArgumentException("Unknown action line: " + line);
	}


}