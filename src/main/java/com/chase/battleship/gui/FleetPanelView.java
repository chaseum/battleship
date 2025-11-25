package com.chase.battleship.gui;

import com.chase.battleship.core.Board;
import com.chase.battleship.core.CellState;
import com.chase.battleship.core.Coordinate;
import com.chase.battleship.core.Ship;
import com.chase.battleship.core.ShipType;
import com.chase.battleship.core.PlayerState;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Small helper to render the player's fleet status.
 */
public class FleetPanelView {
    private final VBox root;

    public FleetPanelView() {
        Label title = new Label("Your Fleet");
        title.getStyleClass().add("menu-item");
        title.setStyle("-fx-font-size: 14px;");
        root = new VBox(10, title);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setMinWidth(150);
        root.getStyleClass().add("ship-panel");
    }

    public VBox getRoot() {
        return root;
    }

    public void update(PlayerState me) {
        if (root.getChildren().size() > 1) {
            root.getChildren().remove(1, root.getChildren().size());
        }
        if (me == null) return;
        Board own = me.getOwnBoard();
        for (ShipType type : ShipType.values()) {
            Ship ship = own.getShips().stream()
                    .filter(s -> s.getType() == type)
                    .findFirst()
                    .orElse(null);
            int hits = 0;
            if (ship != null) {
                for (Coordinate coord : ship.getCoordinates()) {
                    if (own.getCellState(coord) == CellState.HIT) {
                        hits++;
                    }
                }
            }
            boolean sunk = ship == null || ship.isSunk();
            double damageRatio = ship == null ? 1.0 : hits / (double) type.getLength();

            Label name = new Label(type.name());
            name.getStyleClass().add("ship-name");

            Label status = new Label(sunk ? "SUNK" : (damageRatio > 0 ? "HIT" : "READY"));
            status.getStyleClass().add("ship-status");
            status.setWrapText(true);
            status.setMaxWidth(120);

            VBox textCol = new VBox(2, name, status);
            textCol.setAlignment(Pos.CENTER_LEFT);

            HBox row = new HBox(8, textCol);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("ship-card");
            if (sunk) {
                row.getStyleClass().add("sunk");
            } else if (damageRatio > 0) {
                row.getStyleClass().add("damaged");
            }

            root.getChildren().add(row);
        }
    }
}
