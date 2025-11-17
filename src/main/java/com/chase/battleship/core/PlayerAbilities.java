package com.chase.battleship.core;

import java.util.EnumMap;
import java.util.Map;

public class PlayerAbilities {
	private final Map<AbilityType, AbilityStatus> statusMap = new EnumMap<>(AbilityType.class);

	public PlayerAbilities(Map<AbilityType, AbilityRule> rules) {
		rules.forEach((type, rule) -> statusMap.put(type, new AbilityStatus(rule.getMaxCharges())));
	}

	public AbilityStatus getStatus(AbilityType type) {
		return statusMap.get(type);
	}

	public Map<AbilityType, AbilityStatus> getStatusMap() {
		return statusMap;
	}

	public void tickAll() {
		statusMap.values().forEach(AbilityStatus::tickCooldown);
        statusMap.forEach((type, status) -> {
            assert status.getCharges() >= 0 : "PlayerAbilities: negative charges for " + type;
            assert status.getCooldownRemaining() >= 0 : "PlayerAbilities: negative cooldown for " + type;
        });
    }
}