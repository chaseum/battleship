package com.chase.battleship.core;

import java.util.List;

public record AbilityTarget(
        Coordinate coordinate,
        int extraShots,
        List<Coordinate> manualTargets
) {
    public AbilityTarget(Coordinate coordinate, int extraShots) {
        this(coordinate, extraShots, null);
    }

    public AbilityTarget(List<Coordinate> manualTargets) {
        this(null, 0, manualTargets);
    }
}
