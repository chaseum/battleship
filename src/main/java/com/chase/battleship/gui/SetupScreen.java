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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SetupScreen extends BaseScreen {

    private static final int CELL_SIZE = 36;
    private static final Color WATER_COLOR = Color.web("#6f8f89"); // softened teal base
    private static final Color SHIP_TINT = Color.web("#8faea5");  // lighter teal for ships
    private static final Color MISS_COLOR = Color.web("#555555"); // medium grey
    private static final Color HIT_COLOR = Color.web("#9c5a3c");  // rust

    private final BorderPane root;
    private final GridPane boardGrid;
    private final Pane shipOverlay;
    private final StackPane boardStack;
    private final GridPane gridWithLabels;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final Button autoBtn;
    private final Button readyBtn;
    private final VBox shipPalette;
    private final VBox paletteWrapper;
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
        root.setStyle("-fx-background-color: linear-gradient(#0d3a55, #0a2f45);");
        root.setPadding(new Insets(20));

        titleLabel = new Label("Place Your Ships");
        titleLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 28px;");

        subtitleLabel = new Label("");
        subtitleLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
        subtitleLabel.setMinWidth(960);
        subtitleLabel.setPrefWidth(1040);
        subtitleLabel.setMaxWidth(1200);
        subtitleLabel.setWrapText(false);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        VBox titleBox = new VBox(6, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        root.setTop(titleBox);

        boardGrid = createEmptyGrid(10, 10);
        shipOverlay = new Pane();
        shipOverlay.setMouseTransparent(true);
        shipOverlay.setPickOnBounds(false);
        double boardPixels = CELL_SIZE * 10 + boardGrid.getHgap() * 9;
        shipOverlay.setPrefSize(boardPixels, boardPixels);
        boardStack = new StackPane(boardGrid, shipOverlay);
        boardStack.setPickOnBounds(false);
        gridWithLabels = wrapWithLabels(boardStack);

        shipPalette = new VBox(16);
        shipPalette.setAlignment(Pos.CENTER);
        shipPalette.setFillWidth(false);
        shipPalette.setPadding(new Insets(0, 10, 0, 10));
        rebuildShipPalette();

        paletteWrapper = new VBox(shipPalette);
        paletteWrapper.setAlignment(Pos.CENTER);
        paletteWrapper.setMinWidth(CELL_SIZE * 4);
        paletteWrapper.setMinHeight(CELL_SIZE * 12);
        paletteWrapper.setPadding(new Insets(0, 10, 0, 0));
        paletteWrapper.minHeightProperty().bind(gridWithLabels.heightProperty());
        paletteWrapper.prefHeightProperty().bind(gridWithLabels.heightProperty());

        HBox centerBox = new HBox(24, paletteWrapper, gridWithLabels);
        centerBox.setAlignment(Pos.CENTER); // center the grid and palette together
        HBox.setHgrow(gridWithLabels, Priority.NEVER);
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
            updateSubtitle("Player 1: place your ships.");
        } else if (session.isOnline() && session.isHost()) {
            updateSubtitle("Lobby code: " + session.getLobbyCode());
        } else {
            updateSubtitle("");
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
            updateSubtitle("Place all ships before readying up.");
            return;
        }

        boolean localTwoP = isLocalTwoPlayer();
        if (localTwoP && !placingSecondPlayer) {
            // Move to player 2 placement
            placingSecondPlayer = true;
            selectedShip = null;
            placeHorizontal = true;
            updateSubtitle("Player 2: place your ships.");
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

        updateSubtitle("Sending layout... waiting for opponent...");
        readyBtn.setDisable(true);
        autoBtn.setDisable(true);
        manager.showLoading("Syncing fleets...");

        Thread t = new Thread(() -> {
            try {
                session.syncPlacementsOnReady();
                Platform.runLater(() -> {
                    manager.hideLoading();
                    manager.show(ScreenId.PLAYING);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    manager.hideLoading();
                    updateSubtitle("Sync failed: " + ex.getMessage());
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
        grid.setHgap(3);
        grid.setVgap(3);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(colorForOwnCell(CellState.EMPTY));
                cell.setStroke(Color.web("#123547"));
                cell.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
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
        renderShipOverlay(board);
    }

    private void handleCellClick(int row, int col) {
        if (session == null || selectedShip == null) return;
        Board board = getCurrentPlacementBoard();
        Coordinate start = new Coordinate(row, col);
        if (!board.canPlaceShip(selectedShip, start, placeHorizontal)) {
            updateSubtitle("Cannot place " + selectedShip + " here.");
            return;
        }
        Ship ship = new Ship(selectedShip);
        board.placeShip(ship, start, placeHorizontal);
        selectedShip = null;
        updateSubtitle("Placed ship. Remaining ships shown on left.");
        rebuildShipPalette();
        refreshBoardView();
        clearPreview();
    }

    private Color colorForOwnCell(CellState cs) {
        return switch (cs) {
            case HIT -> HIT_COLOR;
            case MISS -> MISS_COLOR;
            case SHIP -> SHIP_TINT;
            default -> WATER_COLOR;
        };
    }

    private void renderShipOverlay(Board board) {
        shipOverlay.getChildren().clear();
        if (board == null) return;
        double stepX = CELL_SIZE + boardGrid.getHgap();
        double stepY = CELL_SIZE + boardGrid.getVgap();
        double insetX = boardGrid.getHgap() / 2.0;
        double insetY = boardGrid.getVgap() / 2.0;

        for (Ship ship : board.getShips()) {
            java.util.List<Coordinate> coords = ship.getCoordinates();
            if (coords.isEmpty()) continue;

            boolean horizontal = coords.size() < 2 || coords.get(0).row() == coords.get(1).row();
            int minRow = coords.stream().mapToInt(Coordinate::row).min().orElse(0);
            int minCol = coords.stream().mapToInt(Coordinate::col).min().orElse(0);
            double x = minCol * stepX;
            double y = minRow * stepY;

            Image art = horizontal
                    ? AssetLibrary.shipTopDown(ship.getType())
                    : AssetLibrary.shipTopDownVertical(ship.getType());
            ImageView view = new ImageView(art);
            view.setSmooth(false);
            view.setPreserveRatio(true);

            int len = ship.getType().getLength();
            if (horizontal) {
                view.setFitHeight(CELL_SIZE);
                view.setFitWidth(len * CELL_SIZE + boardGrid.getHgap() * (len - 1));
            } else {
                view.setFitWidth(CELL_SIZE);
                view.setFitHeight(len * CELL_SIZE + boardGrid.getVgap() * (len - 1));
            }

            long hits = coords.stream()
                    .filter(c -> board.getCellState(c) == CellState.HIT)
                    .count();
            double opacity = ship.isSunk() ? 0.35 : (hits > 0 ? 0.78 : 0.92);
            view.setOpacity(opacity);
            view.setMouseTransparent(true);

            DropShadow shadow = new DropShadow(10, Color.web("#061826"));
            if (ship.getType() == ShipType.SUBMARINE) {
                ColorAdjust hue = new ColorAdjust();
                hue.setHue(0.12);
                shadow.setInput(hue);
            }
            view.setEffect(shadow);

            view.setLayoutX(x + insetX);
            view.setLayoutY(y + insetY);
            shipOverlay.getChildren().add(view);
        }
    }

    @Override
    public Region getRoot() {
        return root;
    }

    private void toggleOrientation() {
        placeHorizontal = !placeHorizontal;
        updateSubtitle("Orientation: " + (placeHorizontal ? "Horizontal" : "Vertical"));
    }

    private void resetManual() {
        if (session == null) return;
        getCurrentPlacementBoard().reset();
        selectedShip = null;
        placeHorizontal = true;
        readyBtn.setDisable(false);
        autoBtn.setDisable(false);
        updateSubtitle("Ships reset. Choose a ship to place.");
        rebuildShipPalette();
        refreshBoardView();
        clearPreview();
    }

    private void rebuildShipPalette() {
        shipPalette.getChildren().clear();
        Label header = new Label("Ships");
        header.setTextFill(Color.WHITE);
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

            ImageView icon = new ImageView(AssetLibrary.shipIcon(type));
            icon.setPreserveRatio(true);
            icon.setSmooth(false);
            icon.setFitHeight(CELL_SIZE * 0.9);

            StackPane artCard = new StackPane(icon);
            artCard.setPadding(new Insets(8));
            artCard.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(53, 240, 255, 0.6); -fx-border-radius: 6; -fx-background-radius: 6;");

            Label l = new Label(type.name());
            l.setTextFill(Color.web("#e6f4ff"));
            VBox box = new VBox(6, artCard, l);
            box.setAlignment(Pos.CENTER);
            box.setOnMouseClicked(e -> {
                selectedShip = type;
                updateSubtitle("Selected " + type + " (" + (placeHorizontal ? "Horizontal" : "Vertical") + ")");
                highlightSelection(box);
            });
            shipPalette.getChildren().add(box);
        }

        if (shipPalette.getChildren().size() == 1) {
            Label done = new Label("All ships placed");
            done.setTextFill(Color.WHITE);
            shipPalette.getChildren().add(done);
        }
        highlightSelection(null);

        boolean allPlaced = placed.size() == ShipType.values().length;
        paletteWrapper.setManaged(!allPlaced);
        paletteWrapper.setVisible(!allPlaced);
    }

    private GridPane wrapWithLabels(Node boardNode) {
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

        outer.add(boardNode, 1, 1, 10, 10);
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

    private void highlightSelection(VBox selectedBox) {
        for (Node n : shipPalette.getChildren()) {
            if (n instanceof VBox box) {
                if (selectedBox == null) {
                    box.setOpacity(1.0);
                } else {
                    box.setOpacity(box == selectedBox ? 1.0 : 0.6);
                }
            }
        }
    }

    private void updateSubtitle(String text) {
        FxAnimations.typewriter(subtitleLabel, text, Duration.millis(1200));
    }
}
