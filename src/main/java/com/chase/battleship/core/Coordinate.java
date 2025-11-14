package com.chase.battleship.core;

public record Coordinate(int row, int col) {
	public Coordinate {
		if(row < 0 || col < 0) {
			throw new IllegalArgumentException("Negative indices not allowed");
		}
	}
}