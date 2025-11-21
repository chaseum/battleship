package com.chase.battleship.core;

import java.util.EnumMap;
import java.util.Map;

public class GameConfig {
	private final int rows;
	private final int cols;
	private final GameMode gameMode;
	private final Map<AbilityType, AbilityRule> abilityRules = new EnumMap<>(AbilityType.class);

	public GameConfig(int rows, int cols, GameMode gameMode) {
		this.rows = rows;
		this.cols = cols;
		this.gameMode = gameMode;
	}

	public static GameConfig classic() {
		return new GameConfig(10, 10, GameMode.CLASSIC);
	}

	public static GameConfig classicLocal2p() {
		return new GameConfig(10, 10, GameMode.CLASSIC_LOCAL_2P);
	}

	public static GameConfig neoRetroDefault() {
		GameConfig cfg = new GameConfig(10, 10, GameMode.NEO_RETRO) ;
		cfg.abilityRules.put(AbilityType.EMP, new AbilityRule(2,1)); // cd = 2, maxCharges = 1
		cfg.abilityRules.put(AbilityType.MULTISHOT, new AbilityRule(3,2)); // cd = 3, maxCharges = 2
		cfg.abilityRules.put(AbilityType.SHIELD, new AbilityRule(2,2));
		cfg.abilityRules.put(AbilityType.SONAR, new AbilityRule(2,3)); 
		return cfg;
	}

	public static GameConfig neoRetroLocal2p() {
		GameConfig cfg = new GameConfig(10, 10, GameMode.NEORETRO_LOCAL_2P);
		cfg.abilityRules.put(AbilityType.EMP, new AbilityRule(2,1));
		cfg.abilityRules.put(AbilityType.MULTISHOT, new AbilityRule(3,2));
		cfg.abilityRules.put(AbilityType.SHIELD, new AbilityRule(2,2));
		cfg.abilityRules.put(AbilityType.SONAR, new AbilityRule(2,3));
		return cfg;
	}

	public int getRows() { return rows; }
	public int getCols() { return cols; }
	public GameMode getGameMode() { return gameMode; }

	public Map<AbilityType, AbilityRule> getAbilityRules() {
		return abilityRules;
	}
}
