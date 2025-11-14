package com.chase.battleship.core;

public sealed interface TurnAction permits FireAction, UseAbilityAction { }

