package com.chase.battleship.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ship {
	private final ShipType type;
	private final List<Coordinate> coordinates = new ArrayList<>();
	private int remainingHealth;
	private boolean shielded;

	public Ship(ShipType type) {
		this.type = type;
		this.remainingHealth = type.getLength();
		this.shielded = false;
	}

	public ShipType getType() {
		return type;
	}

	public void addCoordinate(Coordinate c) {
		coordinates.add(c);
	}

	public List<Coordinate> getCoordinates() {
		return Collections.unmodifiableList(coordinates);
	}

	public boolean occupies(Coordinate c) {
		return coordinates.contains(c);
	}

	public boolean isSunk() {
		return remainingHealth <= 0;
	}

	public void applyShield() {
		this.shielded = true;
	}

	public boolean hasShield() {
		return shielded;
	}

	public void registerHit() {
		if(shielded) {
			// shield absorbs hit
			shielded = false;
		} else {
			remainingHealth--;
		}
	}
}