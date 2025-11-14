package com.chase.battleship.core;

public class AbilityRule {
	private final int cooldownTurns;
	private final int maxCharges;

	public AbilityRule(int cooldownTurns, int maxCharges) {
		this.cooldownTurns = cooldownTurns;
		this.maxCharges = maxCharges;
	}

	public int getCooldownTurns() {
		return cooldownTurns;
	}

	public int getMaxCharges() {
		return maxCharges;
	}
}