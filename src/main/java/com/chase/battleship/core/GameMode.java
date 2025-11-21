package com.chase.battleship.core;

public enum GameMode {
	CLASSIC,
	NEO_RETRO,
	CLASSIC_LOCAL_2P,
	NEORETRO_LOCAL_2P;

	public boolean isNeoRetro() {
		return this == NEO_RETRO || this == NEORETRO_LOCAL_2P;
	}

	public boolean isLocalTwoPlayer() {
		return this == CLASSIC_LOCAL_2P || this == NEORETRO_LOCAL_2P;
	}
}
