package com.chase.battleship.core;

public class AbilityStatus {
	private int charges;
	private int cooldownRemaining;

	public AbilityStatus(int initialCharges) {
		this.charges = initialCharges;
		this.cooldownRemaining = 0;
		assert charges >= 0 : "AbilityStatus: initial charges negative";
        assert cooldownRemaining >= 0 : "AbilityStatus: initial cooldown negative";
	}

	public boolean isAvailable() {
		return charges > 0 && cooldownRemaining == 0;
	}

	public void consume(AbilityRule rule) {
		if (!isAvailable()) {
            throw new IllegalStateException("Ability not available");
        }
        charges--;
        assert charges >= 0 : "AbilityStatus: charges went negative";
        cooldownRemaining = rule.getCooldownTurns();
        assert cooldownRemaining >= 0 : "AbilityStatus: cooldown negative after consume";
	}

	public void tickCooldown() {
		if(cooldownRemaining > 0) {
			cooldownRemaining--;
		}
		assert cooldownRemaining >= 0 : "AbilityStatus: cooldown went negative";
	}

	public int getCharges() {
		assert charges >= 0 : "AbilityStatus: charges negative at read";
		return charges; 
	}
	public int getCooldownRemaining() { assert cooldownRemaining >= 0 : "AbilityStatus: cooldown negative at read"; return cooldownRemaining; }
}