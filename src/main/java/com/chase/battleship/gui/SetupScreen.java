package com.chase.battleship.gui;

import com.chase.battleship.core.Board;
import com.chase.battleship.core.CellState;
import com.chase.battleship.core.Coordinate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SetupScreen extends BaseScreen {

    private static final int CELL_SIZE = 28;

    private final BorderPane root;
    private final GridPane boardGrid;
    private final Label titleLabel;
    private final Label subtitleLabel;

    private GuiGameSession session;

    public SetupScreen(ScreenManager manager) {
        super(manager);

        root = new BorderPane();
        root.setStyle("-fx-background-color: #001b29;");
        root.setPadding(new Insets(20));

        titleLabel = new Label("Place Your Ships");
        titleLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 28px;");

        subtitleLabel = new Label("");
        subtitleLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");

        VBox titleBox = new VBox(6, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        root.setTop(titleBox);

        boardGrid = createEmptyGrid(10, 10);

        HBox centerBox = new HBox(boardGrid);
        centerBox.setAlignment(Pos.CENTER); // center the grid
        root.setCenter(centerBox);

        // bottom controls
        Button autoBtn = new Button("Auto Setup");
        Button manualBtn = new Button("Manual Setup (WIP)");
        manualBtn.setDisable(true);
        Button readyBtn = new Button("Ready");
        Button backBtn = new Button("Back");

        autoBtn.setOnAction(e -> autoSetup());
        readyBtn.setOnAction(e -> manager.show(ScreenId.PLAYING));
        backBtn.setOnAction(e -> {
            manager.clearCurrentSession();
            manager.goBack();
        });

        HBox bottomBar = new HBox(15, autoBtn, manualBtn, readyBtn, backBtn);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBar);
    }

    @Override
    public void onShow() {
        if (manager.getCurrentSession() == null) {
            GuiGameSession.Mode mode = manager.getPlannedMode();
            session = new GuiGameSession(mode, manager.getPendingJoinCode());
            manager.setCurrentSession(session);
        } else {
            session = manager.getCurrentSession();
        }
        if (session.isOnline() && session.isHost()) {
            subtitleLabel.setText("Lobby code: " + session.getLobbyCode());
        } else {
            subtitleLabel.setText("");
        }
        refreshBoardView();
    }

    private void autoSetup() {
        GuiGameSession.Mode mode = manager.getPlannedMode();
        session = new GuiGameSession(mode, manager.getPendingJoinCode());
        manager.setCurrentSession(session);
        refreshBoardView();
    }

    private GridPane createEmptyGrid(int rows, int cols) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.DARKCYAN);
                cell.setStroke(Color.web("#002b36"));
                grid.add(cell, c, r);
            }
        }
        return grid;
    }

    private void refreshBoardView() {
        if (session == null) return;

        Board board = session.getLocalPlayer().getOwnBoard();
        for (Node node : boardGrid.getChildren()) {
            if (!(node instanceof Rectangle rect)) continue;
            Integer cIdx = GridPane.getColumnIndex(node);
            Integer rIdx = GridPane.getRowIndex(node);
            int col = (cIdx == null ? 0 : cIdx);
            int row = (rIdx == null ? 0 : rIdx);

            CellState cs = board.getCellState(new Coordinate(row, col));
            rect.setFill(colorForOwnCell(cs));
        }
    }

    private Color colorForOwnCell(CellState cs) {
        return switch (cs) {
            case HIT -> Color.CRIMSON;
            case MISS -> Color.DARKSLATEGRAY;
            case SHIP -> Color.LIGHTGRAY;
            default -> Color.DARKCYAN;
        };
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
