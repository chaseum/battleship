package com.chase.battleship.core;

public record UseAbilityAction(AbilityType abilityType, AbilityTarget target) implements TurnAction { }