package com.chase.battleship.gui;

import com.chase.battleship.core.Board;
import com.chase.battleship.core.CellState;
import com.chase.battleship.core.Coordinate;
import com.chase.battleship.core.Ship;
import com.chase.battleship.core.ShipType;
import com.chase.battleship.core.GameMode;
import com.chase.battleship.core.BoardUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SetupScreen extends BaseScreen {

    private static final int CELL_SIZE = 28;

    private final BorderPane root;
    private final GridPane boardGrid;
    private final GridPane gridWithLabels;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final Button autoBtn;
    private final Button readyBtn;
    private final VBox shipPalette;
    private final java.util.Set<Coordinate> previewCells = new java.util.HashSet<>();
    private boolean previewValid = false;
    private int previewAnchorRow = -1;
    private int previewAnchorCol = -1;

    private GuiGameSession session;
    private ShipType selectedShip = null;
    private boolean placeHorizontal = true;
    private boolean placingSecondPlayer = false;

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
        gridWithLabels = wrapWithLabels(boardGrid);

        shipPalette = new VBox(16);
        shipPalette.setAlignment(Pos.CENTER);
        shipPalette.setFillWidth(false);
        shipPalette.setPadding(new Insets(0, 10, 0, 10));
        rebuildShipPalette();

        VBox paletteWrapper = new VBox(shipPalette);
        paletteWrapper.setAlignment(Pos.CENTER);
        paletteWrapper.setMinWidth(CELL_SIZE * 4);
        paletteWrapper.setMinHeight(CELL_SIZE * 12);
        paletteWrapper.setPadding(new Insets(0, 10, 0, 0));

        HBox centerBox = new HBox(24, paletteWrapper, gridWithLabels);
        centerBox.setAlignment(Pos.CENTER); // center the grid and palette together
        root.setCenter(centerBox);

        // bottom controls
        autoBtn = new Button("Auto Setup");
        Button rotateBtn = new Button("Rotate (R)");
        Button resetBtn = new Button("Reset");
        readyBtn = new Button("Ready");
        Button backBtn = new Button("Back");

        autoBtn.setOnAction(e -> autoSetup());
        rotateBtn.setOnAction(e -> toggleOrientation());
        resetBtn.setOnAction(e -> resetManual());
        readyBtn.setOnAction(e -> handleReady());
        backBtn.setOnAction(e -> {
            if (manager.getCurrentSession() != null) {
                manager.getCurrentSession().close();
            }
            manager.clearCurrentSession();
            manager.goBack();
        });

        HBox bottomBar = new HBox(15, autoBtn, rotateBtn, resetBtn, readyBtn, backBtn);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBar);

        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.R) {
                toggleOrientation();
            }
        });
    }

    @Override
    public void onShow() {
        GuiGameSession existing = manager.getCurrentSession();
        boolean canReuse = existing != null && existing.isOnline() && !existing.getState().isGameOver();
        if (!canReuse) {
            GuiGameSession.Mode mode = manager.getPlannedMode();
            session = new GuiGameSession(mode, manager.getPendingJoinCode());
            manager.setCurrentSession(session);
        } else {
            session = existing;
        }
        placingSecondPlayer = false;
        selectedShip = null;
        placeHorizontal = true;

        boolean localTwoP = isLocalTwoPlayer();
        if (localTwoP) {
            session.getLocalPlayer().getOwnBoard().reset();
            session.getRemotePlayer().getOwnBoard().reset();
            subtitleLabel.setText("Player 1: place your ships.");
        } else if (session.isOnline() && session.isHost()) {
            subtitleLabel.setText("Lobby code: " + session.getLobbyCode());
        } else {
            subtitleLabel.setText("");
        }
        readyBtn.setDisable(false);
        autoBtn.setDisable(false);
        clearPreview();
        refreshBoardView();
        rebuildShipPalette();
        root.requestFocus();
    }

    private void autoSetup() {
        if (session == null) return;
        Board target = getCurrentPlacementBoard();
        target.reset();
        BoardUtils.randomFleetPlacement(target);
        selectedShip = null;
        placeHorizontal = true;
        readyBtn.setDisable(false);
        autoBtn.setDisable(false);
        refreshBoardView();
        rebuildShipPalette();
    }

    private void handleReady() {
        if (session == null) return;
        Board board = getCurrentPlacementBoard();
        if (board == null) return;
        if (board.getShips().size() < ShipType.values().length) {
            subtitleLabel.setText("Place all ships before readying up.");
            return;
        }

        boolean localTwoP = isLocalTwoPlayer();
        if (localTwoP && !placingSecondPlayer) {
            // Move to player 2 placement
            placingSecondPlayer = true;
            selectedShip = null;
            placeHorizontal = true;
            subtitleLabel.setText("Player 2: place your ships.");
            session.getRemotePlayer().getOwnBoard().reset();
            refreshBoardView();
            rebuildShipPalette();
            clearPreview();
            return;
        }

        if (localTwoP) {
            placingSecondPlayer = false;
        }

        if (!session.isOnline()) {
            manager.show(ScreenId.PLAYING);
            return;
        }

        subtitleLabel.setText("Sending layout... waiting for opponent");
        readyBtn.setDisable(true);
        autoBtn.setDisable(true);

        Thread t = new Thread(() -> {
            try {
                session.syncPlacementsOnReady();
                Platform.runLater(() -> manager.show(ScreenId.PLAYING));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    subtitleLabel.setText("Sync failed: " + ex.getMessage());
                    readyBtn.setDisable(false);
                    autoBtn.setDisable(false);
                    manager.show(ScreenId.DISCONNECTED);
                });
            }
        }, "setup-ready-sync");
        t.setDaemon(true);
        t.start();
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
                int fr = r;
                int fc = c;
                cell.setOnMouseClicked(e -> handleCellClick(fr, fc));
                cell.setOnMouseEntered(e -> showPreview(fr, fc));
                grid.add(cell, c, r);
            }
        }
        grid.setOnMouseExited(e -> clearPreview());
        grid.setOnMouseReleased(e -> attemptPlaceFromPreview());
        return grid;
    }

    private void refreshBoardView() {
        if (session == null) return;

        Board board = getCurrentPlacementBoard();
        if (board == null) return;
        for (Node node : boardGrid.getChildren()) {
            if (!(node instanceof Rectangle rect)) continue;
            Integer cIdx = GridPane.getColumnIndex(node);
            Integer rIdx = GridPane.getRowIndex(node);
            int col = (cIdx == null ? 0 : cIdx);
            int row = (rIdx == null ? 0 : rIdx);

            Coordinate coord = new Coordinate(row, col);
            CellState cs = board.getCellState(coord);
            if (selectedShip != null && previewCells.contains(coord)) {
                rect.setFill(previewValid ? Color.LIGHTGREEN : Color.ORANGERED);
            } else {
                rect.setFill(colorForOwnCell(cs));
            }
        }
    }

    private void handleCellClick(int row, int col) {
        if (session == null || selectedShip == null) return;
        Board board = getCurrentPlacementBoard();
        Coordinate start = new Coordinate(row, col);
        if (!board.canPlaceShip(selectedShip, start, placeHorizontal)) {
            subtitleLabel.setText("Cannot place " + selectedShip + " here.");
            return;
        }
        Ship ship = new Ship(selectedShip);
        board.placeShip(ship, start, placeHorizontal);
        selectedShip = null;
        subtitleLabel.setText("Placed ship. Remaining ships shown on left.");
        rebuildShipPalette();
        refreshBoardView();
        clearPreview();
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

    private void toggleOrientation() {
        placeHorizontal = !placeHorizontal;
        subtitleLabel.setText("Orientation: " + (placeHorizontal ? "Horizontal" : "Vertical"));
    }

    private void resetManual() {
        if (session == null) return;
        getCurrentPlacementBoard().reset();
        selectedShip = null;
        placeHorizontal = true;
        readyBtn.setDisable(false);
        autoBtn.setDisable(false);
        subtitleLabel.setText("Ships reset. Choose a ship to place.");
        rebuildShipPalette();
        refreshBoardView();
        clearPreview();
    }

    private void rebuildShipPalette() {
        shipPalette.getChildren().clear();
        Label header = new Label("Ships");
        shipPalette.getChildren().add(header);
        Board board = getCurrentPlacementBoard();
        if (board == null) return;

        java.util.EnumSet<ShipType> placed = java.util.EnumSet.noneOf(ShipType.class);
        for (Ship ship : board.getShips()) {
            placed.add(ship.getType());
        }
        if (placed.isEmpty()) {
            selectedShip = null;
        }
        for (ShipType type : ShipType.values()) {
            if (placed.contains(type)) continue;

            Rectangle r = new Rectangle(CELL_SIZE * type.getLength(), CELL_SIZE * 0.7, Color.LIGHTGRAY);
            r.setArcWidth(6);
            r.setArcHeight(6);
            Label l = new Label(type.name());
            VBox box = new VBox(4, r, l);
            box.setAlignment(Pos.CENTER);
            box.setOnMouseClicked(e -> {
                selectedShip = type;
                subtitleLabel.setText("Selected " + type + " (" + (placeHorizontal ? "Horizontal" : "Vertical") + ")");
            });
            shipPalette.getChildren().add(box);
        }

        if (shipPalette.getChildren().size() == 1) {
            shipPalette.getChildren().add(new Label("All ships placed"));
        }
    }

    private GridPane wrapWithLabels(GridPane grid) {
        GridPane outer = new GridPane();
        outer.setHgap(2);
        outer.setVgap(2);

        // Column constraints: first for labels, then 10 for cells
        outer.getColumnConstraints().clear();
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPrefWidth(CELL_SIZE * 0.8);
        labelCol.setHalignment(javafx.geometry.HPos.CENTER);
        outer.getColumnConstraints().add(labelCol);
        for (int i = 0; i < 10; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(CELL_SIZE);
            cc.setHalignment(javafx.geometry.HPos.CENTER);
            outer.getColumnConstraints().add(cc);
        }

        outer.getRowConstraints().clear();
        RowConstraints labelRow = new RowConstraints();
        labelRow.setPrefHeight(CELL_SIZE * 0.8);
        labelRow.setValignment(javafx.geometry.VPos.CENTER);
        outer.getRowConstraints().add(labelRow);
        for (int i = 0; i < 10; i++) {
            RowConstraints rr = new RowConstraints();
            rr.setPrefHeight(CELL_SIZE);
            rr.setValignment(javafx.geometry.VPos.CENTER);
            outer.getRowConstraints().add(rr);
        }

        // top headers A-J
        for (int c = 0; c < 10; c++) {
            Label lbl = new Label(String.valueOf((char) ('A' + c)));
            lbl.setTextFill(Color.WHITE);
            outer.add(lbl, c + 1, 0);
        }
        // left headers 1-10
        for (int r = 0; r < 10; r++) {
            Label lbl = new Label(String.valueOf(r + 1));
            lbl.setTextFill(Color.WHITE);
            outer.add(lbl, 0, r + 1);
        }

        outer.add(grid, 1, 1, 10, 10);
        return outer;
    }

    private void showPreview(int anchorRow, int anchorCol) {
        if (session == null) return;
        if (selectedShip == null) {
            clearPreview();
            return;
        }
        previewCells.clear();
        previewAnchorRow = anchorRow;
        previewAnchorCol = anchorCol;
        Board board = getCurrentPlacementBoard();
        if (board == null) return;
        Coordinate start = new Coordinate(anchorRow, anchorCol);
        previewValid = board.canPlaceShip(selectedShip, start, placeHorizontal);

        int len = selectedShip.getLength();
        for (int i = 0; i < len; i++) {
            int r = anchorRow + (placeHorizontal ? 0 : i);
            int c = anchorCol + (placeHorizontal ? i : 0);
            previewCells.add(new Coordinate(r, c));
        }
        refreshBoardView();
    }

    private void clearPreview() {
        previewCells.clear();
        previewValid = false;
        previewAnchorRow = -1;
        previewAnchorCol = -1;
        refreshBoardView();
    }

    private void attemptPlaceFromPreview() {
        if (selectedShip == null || !previewValid || previewAnchorRow < 0) return;
        handleCellClick(previewAnchorRow, previewAnchorCol);
    }

    private Board getCurrentPlacementBoard() {
        if (session == null) return null;
        if (placingSecondPlayer) {
            return session.getRemotePlayer().getOwnBoard();
        }
        return session.getLocalPlayer().getOwnBoard();
    }

    private boolean isLocalTwoPlayer() {
        GameMode mode = session != null ? session.getConfig().getGameMode() : null;
        return mode != null && mode.isLocalTwoPlayer();
    }
}
