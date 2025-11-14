package com.chase.battleship.core;

public record TurnResult(boolean success, String message, GameState newState) {}