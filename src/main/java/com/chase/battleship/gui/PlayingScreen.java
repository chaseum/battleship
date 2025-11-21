package com.chase.battleship.gui;

import com.chase.battleship.core.*;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class PlayingScreen extends BaseScreen {

    private static final int CELL_SIZE = 28;
    private static final double BASE_DELAY_MS = 650;
    private static Duration TURN_DELAY = Duration.millis(BASE_DELAY_MS);

    private final BorderPane root;
    private final GridPane myGrid;
    private final GridPane enemyGrid;
    private final Label turnLabel;
    private final Label actionLabel;
    private final VBox bottomBox;
    private final Label messageLabel;
    private final HBox hotbar;
    private final VBox fleetPanel;
    private final Label fleetTitle;
    private final Pane settingsOverlay;
    private final Pane handoffOverlay;
    private final Label handoffLabel;
    private final Slider volumeSlider;
    private final Slider speedSlider;
    private final java.util.List<Coordinate> sonarHighlights = new java.util.ArrayList<>();

    private Button sonarBtn;
    private Button multiBtn;
    private Button empBtn;

    private GuiGameSession session;
    private boolean isProcessing = false;
    private boolean waitingForHandOff = false;
    private PlayerState lastTurnOwner = null;
    private boolean isLocalTwoP = false;

    public PlayingScreen(ScreenManager manager) {
        super(manager);

        root = new BorderPane();
        root.setStyle("-fx-background-color: #001b29;");
        root.setPadding(new Insets(20));
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                toggleSettingsOverlay();
            }
        });

        turnLabel = new Label("Your turn");
        turnLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 20px;");

        HBox topBar = new HBox(10, turnLabel);
        topBar.setAlignment(Pos.TOP_LEFT);
        root.setTop(topBar);

        myGrid = createBoardGrid(false);
        enemyGrid = createBoardGrid(true);

        Label myLabel = new Label("Your Sea");
        myLabel.setStyle("-fx-text-fill: #f0f0f0;");

        Label enemyLabel = new Label("Enemy Sea");
        enemyLabel.setStyle("-fx-text-fill: #f0f0f0;");

        VBox myBox = new VBox(8, myLabel, wrapWithLabels(myGrid));
        myBox.setAlignment(Pos.CENTER);

        VBox enemyBox = new VBox(8, enemyLabel, wrapWithLabels(enemyGrid));
        enemyBox.setAlignment(Pos.CENTER);

        fleetTitle = new Label("Your Fleet");
        fleetTitle.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 16px;");
        fleetPanel = new VBox(10);
        fleetPanel.setAlignment(Pos.CENTER_LEFT);
        fleetPanel.setMinWidth(160);
        fleetPanel.getChildren().add(fleetTitle);

        HBox boardsBox = new HBox(80, myBox, enemyBox);
        boardsBox.setAlignment(Pos.CENTER);

        HBox mainRow = new HBox(30, fleetPanel, boardsBox);
        mainRow.setAlignment(Pos.CENTER);

        messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 14px;");

        actionLabel = new Label("");
        actionLabel.setStyle("-fx-text-fill: #f0f0f0;");

        hotbar = new HBox(10);
        hotbar.setAlignment(Pos.CENTER);

        bottomBox = new VBox(10, hotbar);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        VBox centerStack = new VBox(10, messageLabel, mainRow);
        centerStack.setAlignment(Pos.CENTER);

        // settings overlay
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setShowTickMarks(false);
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.setMajorTickUnit(0.1);
        volumeSlider.setBlockIncrement(0.1);
        volumeSlider.getStyleClass().add("minecraft-slider");

        speedSlider = new Slider(0.5, 2.0, 1.0);
        speedSlider.setShowTickMarks(false);
        speedSlider.setShowTickLabels(false);
        speedSlider.setSnapToTicks(true);
        speedSlider.setMajorTickUnit(0.1);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.getStyleClass().add("minecraft-slider");
        speedSlider.valueProperty().addListener((obs, ov, nv) -> TURN_DELAY = Duration.millis(BASE_DELAY_MS / nv.doubleValue()));

        Button closeSettings = new Button("Close");
        closeSettings.setOnAction(e -> toggleSettingsOverlay());
        Button backFromSettings = new Button("Back to Title");
        backFromSettings.setOnAction(e -> {
            toggleSettingsOverlay();
            if (manager.getCurrentSession() != null) {
                manager.getCurrentSession().close();
            }
            manager.clearCurrentSession();
            manager.show(ScreenId.TITLE);
        });

        VBox settingsBox = new VBox(10,
                new Label("Settings"),
                new Label("Volume"), volumeSlider,
                new Label("Game Speed"), speedSlider,
                backFromSettings,
                closeSettings);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(20));
        settingsBox.setMaxWidth(280);
        settingsBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: #f0f0f0; -fx-border-color: #00ffcc; -fx-border-width: 1;");

        StackPane overlayPane = new StackPane(settingsBox);
        overlayPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);
        StackPane.setAlignment(settingsBox, Pos.CENTER);
        settingsOverlay = overlayPane;

        // handoff overlay for local 2P
        handoffLabel = new Label("");
        Button handoffBtn = new Button("Ready");
        VBox handoffBox = new VBox(10, handoffLabel, handoffBtn);
        handoffBox.setAlignment(Pos.CENTER);
        handoffBox.setPadding(new Insets(20));
        handoffBox.setMaxWidth(300);
        handoffBox.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-text-fill: #f0f0f0; -fx-border-color: #00ffcc; -fx-border-width: 1;");
        StackPane handoffPane = new StackPane(handoffBox);
        handoffPane.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        handoffPane.setVisible(false);
        handoffPane.setMouseTransparent(true);
        StackPane.setAlignment(handoffBox, Pos.CENTER);
        handoffOverlay = handoffPane;
        handoffBtn.setOnAction(e -> hideHandOffOverlay());

        StackPane mainLayer = new StackPane(centerStack, settingsOverlay, handoffOverlay);
        root.setCenter(mainLayer);
        root.setBottom(bottomBox);
    }

    @Override
    public void onShow() {
        sonarHighlights.clear();
        session = manager.getCurrentSession();
        if (session == null) {
            turnLabel.setText("No active game");
            messageLabel.setText("");
            return;
        }
        actionLabel.setTextFill(Color.web("#f0f0f0"));
        isLocalTwoP = session.getConfig().getGameMode().isLocalTwoPlayer();
        lastTurnOwner = session.getState().getCurrentPlayer();
        waitingForHandOff = false;

        setupAbilityButtons();
        hideHandOffOverlay();
        syncFromState();
        triggerRemoteIfNeeded();
        root.requestFocus();
    }

    private GridPane createBoardGrid(boolean enemy) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.DARKCYAN);
                cell.setStroke(Color.web("#002b36"));
                grid.add(cell, c, r);

                if (enemy) {
                    final int fr = r;
                    final int fc = c;
                    cell.setOnMouseClicked(e -> handleEnemyClick(fr, fc));
                }
            }
        }
        return grid;
    }

    private void handleEnemyClick(int row, int col) {
        if (session == null || isProcessing) return;
        if (waitingForHandOff) return;
        if (enemyGrid.isDisable()) {
            actionLabel.setTextFill(Color.ORANGERED);
            actionLabel.setText("Not your turn");
            messageLabel.setText("Not your turn");
            return;
        }
        if (!session.isCurrentPlayerHuman()) {
            actionLabel.setTextFill(Color.ORANGERED);
            actionLabel.setText("Not your turn");
            messageLabel.setText("Not your turn");
            return;
        }
        if (session.getState().isGameOver()) return;

        clearSonarHighlights();
        GameState gs = session.getState();
        PlayerState me = gs.getCurrentPlayer();
        Board tracking = me.getTrackingBoard();

        CellState current = tracking.getCellState(new Coordinate(row, col));
        if (current == CellState.HIT || current == CellState.MISS) {
            // already fired here
            return;
        }

        TurnAction action = new FireAction(new Coordinate(row, col));
        performHumanTurn(action);
    }

    private void performHumanTurn(TurnAction action) {
        clearSonarHighlights();
        isProcessing = true;
        TurnResult res;
        try {
            res = session.processHumanAction(action);
        } catch (Exception ex) {
            handleDisconnect(ex.getMessage());
            isProcessing = false;
            return;
        }
        if (res != null) {
            actionLabel.setTextFill(Color.web("#f0f0f0"));
            actionLabel.setText("You: " + res.message());
            messageLabel.setText(simplifyMessage(res.message()));
        }
        maybeHighlightSonar(action, res == null ? null : res.message());
        syncFromState();

        if (session.getState().isGameOver()) {
            handleGameOver();
            isProcessing = false;
            return;
        }

        PauseTransition pause = new PauseTransition(TURN_DELAY);
        pause.setOnFinished(e -> {
            isProcessing = false;
            syncFromState();
            if (session.getState().isGameOver()) {
                handleGameOver();
            }
            triggerRemoteIfNeeded();
        });
        pause.play();
    }

    private void triggerRemoteIfNeeded() {
        if (session == null || session.isCurrentPlayerHuman() || session.getState().isGameOver()) {
            isProcessing = false;
            return;
        }
        if (isProcessing) return;
        isProcessing = true;
        PauseTransition pause = new PauseTransition(TURN_DELAY);
        pause.setOnFinished(e -> {
            TurnResult autoRes;
            try {
                autoRes = session.maybeLetAiAct();
            } catch (Exception ex) {
                handleDisconnect(ex.getMessage());
                isProcessing = false;
                return;
            }
            if (autoRes != null) {
                actionLabel.setTextFill(Color.web("#f0f0f0"));
                actionLabel.setText(autoRes.message());
                messageLabel.setText(simplifyMessage(autoRes.message()));
            }
            isProcessing = false;
            syncFromState();
            if (session.getState().isGameOver()) {
                handleGameOver();
            }
            triggerRemoteIfNeeded();
        });
        pause.play();
    }

    private void syncFromState() {
        if (session == null) return;

        GameState gs = session.getState();
        PlayerState current = gs.getCurrentPlayer();

        boolean turnChanged = isLocalTwoP && current != lastTurnOwner && !gs.isGameOver();
        if (turnChanged && !waitingForHandOff) {
            showHandOffOverlay(current);
        }

        PlayerState me;
        PlayerState enemy;
        if (isLocalTwoP) {
            if (waitingForHandOff && lastTurnOwner != null) {
                me = lastTurnOwner; // keep previous player's perspective until ready
            } else {
                me = current;
            }
            enemy = (me == current) ? gs.getOtherPlayer() : current;
        } else {
            me = session.getLocalPlayer();
            enemy = session.getRemotePlayer();
        }

        Board myBoard = me.getOwnBoard();
        Board myTracking = me.getTrackingBoard();

        redrawOwnBoard(myBoard);
        redrawEnemyBoard(myTracking);
        refreshFleetPanel(me);

        boolean currentHuman = session.isCurrentPlayerHuman();
        if (current == me && currentHuman && !waitingForHandOff) {
            turnLabel.setText("Your turn");
        } else if (!currentHuman) {
            turnLabel.setText("Opponent's turn");
        } else {
            // local 2-player, but still helpful:
            turnLabel.setText(current.getName() + "'s turn");
        }
        enemyGrid.setDisable(!currentHuman || session.getState().isGameOver() || isProcessing || waitingForHandOff);

        refreshAbilityButtons();
    }

    private void redrawOwnBoard(Board board) {
        for (Node node : myGrid.getChildren()) {
            if (!(node instanceof Rectangle rect)) continue;
            Integer cIdx = GridPane.getColumnIndex(node);
            Integer rIdx = GridPane.getRowIndex(node);
            int col = cIdx == null ? 0 : cIdx;
            int row = rIdx == null ? 0 : rIdx;

            CellState cs = board.getCellState(new Coordinate(row, col));
            rect.setFill(colorForOwnCell(cs));
        }
    }

    private void redrawEnemyBoard(Board tracking) {
        for (Node node : enemyGrid.getChildren()) {
            if (!(node instanceof Rectangle rect)) continue;
            Integer cIdx = GridPane.getColumnIndex(node);
            Integer rIdx = GridPane.getRowIndex(node);
            int col = cIdx == null ? 0 : cIdx;
            int row = rIdx == null ? 0 : rIdx;

            Coordinate coord = new Coordinate(row, col);
            if (sonarHighlights.contains(coord)) {
                rect.setFill(Color.GOLD);
                continue;
            }
            CellState cs = tracking.getCellState(coord);
            rect.setFill(colorForEnemyCell(cs));
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

    private Color colorForEnemyCell(CellState cs) {
        return switch (cs) {
            case HIT -> Color.CRIMSON;
            case MISS -> Color.DARKSLATEGRAY;
            default -> Color.DARKCYAN;
        };
    }

    private void setupAbilityButtons() {
        bottomBox.getChildren().clear();

        if (!session.getConfig().getGameMode().isNeoRetro()) {
            bottomBox.getChildren().addAll(hotbar);
            return;
        }

        sonarBtn = new Button("Sonar");
        multiBtn = new Button("Multishot");
        empBtn = new Button("EMP");

        sonarBtn.setOnAction(e -> useSonar());
        multiBtn.setOnAction(e -> useMultishot());
        empBtn.setOnAction(e -> useEmp());

        HBox abilityRow = new HBox(10, sonarBtn, multiBtn, empBtn);
        abilityRow.setAlignment(Pos.CENTER);

        hotbar.getChildren().clear();
        hotbar.getChildren().add(abilityRow);
        bottomBox.getChildren().add(hotbar);
        refreshAbilityButtons();
    }

    private void refreshAbilityButtons() {
        if (session == null) return;
        if (!session.getConfig().getGameMode().isNeoRetro()) return;
        if (sonarBtn == null || multiBtn == null || empBtn == null) return;

        PlayerState me = isLocalTwoP ? session.getState().getCurrentPlayer() : session.getLocalPlayer();
        if (me.getAbilities() == null) {
            sonarBtn.setDisable(true);
            multiBtn.setDisable(true);
            empBtn.setDisable(true);
            return;
        }

        PlayerAbilities abilities = me.getAbilities();
        boolean locked = me.abilitiesLocked();
        boolean myTurn = session.isCurrentPlayerHuman() && !session.getState().isGameOver() && !isProcessing && !waitingForHandOff;

        AbilityStatus sonar = abilities.getStatus(AbilityType.SONAR);
        AbilityStatus multi = abilities.getStatus(AbilityType.MULTISHOT);
        AbilityStatus emp = abilities.getStatus(AbilityType.EMP);

        sonarBtn.setDisable(!myTurn || locked || sonar == null || !sonar.isAvailable());
        multiBtn.setDisable(!myTurn || locked || multi == null || !multi.isAvailable());
        empBtn.setDisable(!myTurn || locked || emp == null || !emp.isAvailable());

        sonarBtn.setOpacity(sonarBtn.isDisabled() ? 0.5 : 1.0);
        multiBtn.setOpacity(multiBtn.isDisabled() ? 0.5 : 1.0);
        empBtn.setOpacity(empBtn.isDisabled() ? 0.5 : 1.0);
    }

    private void useSonar() {
        if (session == null || isProcessing) return;
        if (!session.isCurrentPlayerHuman()) return;
        PlayerState me = isLocalTwoP ? session.getState().getCurrentPlayer() : session.getLocalPlayer();
        if (me.abilitiesLocked()) {
            actionLabel.setText("Abilities disabled by EMP!");
            return;
        }

        // Simple sonar: center on the middle of the board for now
        Coordinate center = new Coordinate(5, 5);
        TurnAction action = new UseAbilityAction(
                AbilityType.SONAR,
                new AbilityTarget(center, 0)
        );
        performHumanTurn(action);
    }

    private void useMultishot() {
        if (session == null || isProcessing) return;
        if (!session.isCurrentPlayerHuman()) return;
        PlayerState me = isLocalTwoP ? session.getState().getCurrentPlayer() : session.getLocalPlayer();
        if (me.abilitiesLocked()) {
            actionLabel.setText("Abilities disabled by EMP!");
            return;
        }

        // Fire 3 random shots (same as AI semantics)
        TurnAction action = new UseAbilityAction(
                AbilityType.MULTISHOT,
                new AbilityTarget(null, 3)
        );
        performHumanTurn(action);
    }

    private void useEmp() {
        if (session == null || isProcessing) return;
        if (!session.isCurrentPlayerHuman()) return;
        PlayerState me = isLocalTwoP ? session.getState().getCurrentPlayer() : session.getLocalPlayer();
        if (me.abilitiesLocked()) {
            actionLabel.setText("Abilities disabled by EMP!");
            return;
        }

        TurnAction action = new UseAbilityAction(
                AbilityType.EMP,
                new AbilityTarget(null, 0)
        );
        performHumanTurn(action);
    }

    private void handleGameOver() {
        Boolean localWon = session.isLocalWinner();
        if (Boolean.TRUE.equals(localWon)) {
            manager.show(ScreenId.WIN);
        } else if (Boolean.FALSE.equals(localWon)) {
            manager.show(ScreenId.LOSE);
        } else {
            // Fallback if winner not set; assume current player is winner.
            GameState gs = session.getState();
            PlayerState winner = gs.getWinner() != null ? gs.getWinner() : gs.getCurrentPlayer();
            manager.show(winner == session.getLocalPlayer() ? ScreenId.WIN : ScreenId.LOSE);
        }
    }

    private void handleDisconnect(String message) {
        actionLabel.setText("Disconnected: " + message);
        manager.show(ScreenId.DISCONNECTED);
    }

    private String simplifyMessage(String message) {
        if (message == null) return "";
        String upper = message.toUpperCase();
        if (upper.contains("SUNK")) return "SUNK";
        if (upper.contains("HIT") && upper.contains("SHIELD")) return "HIT (Shielded)";
        if (upper.contains("HIT")) return "HIT";
        if (upper.contains("MISS")) return "MISS";
        if (upper.contains("SONAR")) return "SONAR USED";
        if (upper.contains("EMP")) return "EMP USED";
        if (upper.contains("MULTISHOT")) return "MULTISHOT";
        return message;
    }

    private void toggleSettingsOverlay() {
        boolean nowVisible = !settingsOverlay.isVisible();
        settingsOverlay.setVisible(nowVisible);
        settingsOverlay.setMouseTransparent(!nowVisible);
    }

    @Override
    public Region getRoot() {
        return root;
    }

    private void maybeHighlightSonar(TurnAction action, String message) {
        if (!(action instanceof UseAbilityAction abilityAction)) return;
        if (abilityAction.abilityType() != AbilityType.SONAR) return;
        AbilityTarget target = abilityAction.target();
        if (target == null || target.coordinate() == null) return;
        boolean hitsFound = message != null && message.contains("detected");
        if (hitsFound) {
            setSonarHighlights(target.coordinate());
        } else {
            clearSonarHighlights();
        }
    }

    private void setSonarHighlights(Coordinate center) {
        sonarHighlights.clear();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int rr = center.row() + dr;
                int cc = center.col() + dc;
                sonarHighlights.add(new Coordinate(rr, cc));
            }
        }
        syncFromState();
    }

    private void clearSonarHighlights() {
        if (sonarHighlights.isEmpty()) return;
        sonarHighlights.clear();
        syncFromState();
    }

    private void refreshFleetPanel(PlayerState me) {
        fleetPanel.getChildren().clear();
        fleetPanel.getChildren().add(fleetTitle);
        if (me == null) return;
        Board own = me.getOwnBoard();
        for (ShipType type : ShipType.values()) {
            Ship ship = own.getShips().stream()
                    .filter(s -> s.getType() == type)
                    .findFirst()
                    .orElse(null);
            boolean sunk = ship == null || ship.isSunk();
            javafx.scene.shape.Rectangle bar = new javafx.scene.shape.Rectangle(CELL_SIZE * type.getLength() * 0.7, CELL_SIZE * 0.5);
            bar.setArcWidth(6);
            bar.setArcHeight(6);
            bar.setFill(sunk ? Color.DARKSLATEGRAY : Color.LIGHTGRAY);
            Label label = new Label(type.name());
            label.setTextFill(Color.web("#f0f0f0"));
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8, bar, label);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setOpacity(sunk ? 0.4 : 1.0);
            fleetPanel.getChildren().add(row);
        }
    }


    private void showHandOffOverlay(PlayerState current) {
        waitingForHandOff = true;
        enemyGrid.setDisable(true);
        myGrid.setDisable(true);
        hotbar.setDisable(true);
        handoffLabel.setText(current.getName() + "'s turn. Please pass the device.");
        handoffOverlay.setVisible(true);
        handoffOverlay.setMouseTransparent(false);
    }

    private void hideHandOffOverlay() {
        boolean wasWaiting = waitingForHandOff;
        waitingForHandOff = false;
        handoffOverlay.setVisible(false);
        handoffOverlay.setMouseTransparent(true);
        enemyGrid.setDisable(false);
        myGrid.setDisable(false);
        hotbar.setDisable(false);
        if (wasWaiting) {
            lastTurnOwner = session != null ? session.getState().getCurrentPlayer() : null;
            refreshAbilityButtons();
            syncFromState();
        } else {
            refreshAbilityButtons();
        }
    }

    private Rectangle findCell(GridPane grid, int row, int col) {
        for (Node node : grid.getChildren()) {
            if (!(node instanceof Rectangle rect)) continue;
            Integer cIdx = GridPane.getColumnIndex(node);
            Integer rIdx = GridPane.getRowIndex(node);
            int c = cIdx == null ? 0 : cIdx;
            int r = rIdx == null ? 0 : rIdx;
            if (r == row && c == col) {
                return rect;
            }
        }
        return null;
    }

    private GridPane wrapWithLabels(GridPane grid) {
        GridPane outer = new GridPane();
        outer.setHgap(2);
        outer.setVgap(2);

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

        for (int c = 0; c < 10; c++) {
            Label lbl = new Label(String.valueOf((char) ('A' + c)));
            lbl.setTextFill(Color.WHITE);
            outer.add(lbl, c + 1, 0);
        }
        for (int r = 0; r < 10; r++) {
            Label lbl = new Label(String.valueOf(r + 1));
            lbl.setTextFill(Color.WHITE);
            outer.add(lbl, 0, r + 1);
        }

        outer.add(grid, 1, 1, 10, 10);
        return outer;
    }
}
