package com.chase.battleship.net;

import com.chase.battleship.core.Coordinate;
import com.chase.battleship.core.Ship;
import com.chase.battleship.core.ShipType;

/**
 * Simple DTO describing a ship's placement on a board.
 */
public record ShipPlacement(ShipType type, Coordinate start, boolean horizontal) {

    public static ShipPlacement fromShip(Ship ship) {
        Coordinate first = ship.getCoordinates().get(0);
        Coordinate second = ship.getCoordinates().get(1);
        boolean horiz = first.row() == second.row();
        return new ShipPlacement(ship.getType(), first, horiz);
    }
}

