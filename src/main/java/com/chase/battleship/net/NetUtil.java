package com.chase.battleship.net;

import java.util.Random;

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
}