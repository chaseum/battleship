package com.chase.battleship.core;

public class AbilityStatus {
	private int charges;
	private int cooldownRemaining;

	public AbilityStatus(int initialCharges) {
		this.charges = initialCharges;
		this.cooldownRemaining = 0;
	}

	public boolean isAvailable() {
		return charges > 0 && cooldownRemaining == 0;
	}

	public void consume(AbilityRule rule) {
		if(!isAvailable()) {
			throw new IllegalStateException("Ability not available");
		}
		charges--;
		cooldownRemaining = rule.getCooldownTurns();
	}

	public void tickCooldown() {
		if(cooldownRemaining > 0) {
			cooldownRemaining--;
		}
	}

	public int getCharges() { return charges; }
	public int getCooldownRemaining() { return cooldownRemaining; }
}