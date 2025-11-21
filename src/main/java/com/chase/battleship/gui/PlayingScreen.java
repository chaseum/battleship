package com.chase.battleship.gui;

import com.chase.battleship.core.*;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

public class PlayingScreen extends BaseScreen {

    private static final int CELL_SIZE = 28;
    private static final Duration TURN_DELAY = Duration.millis(1200);

    private final BorderPane root;
    private final GridPane myGrid;
    private final GridPane enemyGrid;
    private final Label turnLabel;
    private final Label actionLabel;
    private final VBox bottomBox;
    private final java.util.List<Coordinate> sonarHighlights = new java.util.ArrayList<>();

    private Button sonarBtn;
    private Button multiBtn;
    private Button empBtn;

    private GuiGameSession session;
    private boolean isProcessing = false;

    public PlayingScreen(ScreenManager manager) {
        super(manager);

        root = new BorderPane();
        root.setStyle("-fx-background-color: #001b29;");
        root.setPadding(new Insets(20));

        turnLabel = new Label("Your turn");
        turnLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 20px;");

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            if (manager.getCurrentSession() != null) {
                manager.getCurrentSession().close();
            }
            manager.clearCurrentSession();
            manager.show(ScreenId.TITLE);
        });

        HBox topBar = new HBox(10, turnLabel, backBtn);
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

        HBox boardsBox = new HBox(80, myBox, enemyBox);
        boardsBox.setAlignment(Pos.CENTER);
        root.setCenter(boardsBox);

        actionLabel = new Label("");
        actionLabel.setStyle("-fx-text-fill: #f0f0f0;");

        bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        root.setBottom(bottomBox);
    }

    @Override
    public void onShow() {
        sonarHighlights.clear();
        session = manager.getCurrentSession();
        if (session == null) {
            turnLabel.setText("No active game");
            actionLabel.setText("");
            bottomBox.getChildren().setAll(actionLabel);
            return;
        }
        actionLabel.setTextFill(Color.web("#f0f0f0"));

        setupAbilityButtons();
        syncFromState();
        triggerRemoteIfNeeded();
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
        if (enemyGrid.isDisable()) {
            actionLabel.setTextFill(Color.ORANGERED);
            actionLabel.setText("Not your turn");
            return;
        }
        if (!session.isCurrentPlayerHuman()) {
            actionLabel.setTextFill(Color.ORANGERED);
            actionLabel.setText("Not your turn");
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
        }
        maybeHighlightSonar(action);
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

        PlayerState me = session.getLocalPlayer();
        PlayerState enemy = session.getRemotePlayer();

        Board myBoard = me.getOwnBoard();
        Board myTracking = me.getTrackingBoard();

        redrawOwnBoard(myBoard);
        redrawEnemyBoard(myTracking);

        boolean currentHuman = session.isCurrentPlayerHuman();
        if (current == me && currentHuman) {
            turnLabel.setText("Your turn");
        } else if (!currentHuman) {
            turnLabel.setText("Opponent's turn");
        } else {
            // local 2-player, but still helpful:
            turnLabel.setText(current.getName() + "'s turn");
        }
        enemyGrid.setDisable(!currentHuman || session.getState().isGameOver() || isProcessing);

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

        if (session.getConfig().getGameMode() != GameMode.NEO_RETRO) {
            bottomBox.getChildren().addAll(actionLabel);
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

        bottomBox.getChildren().addAll(abilityRow, actionLabel);
        refreshAbilityButtons();
    }

    private void refreshAbilityButtons() {
        if (session == null) return;
        if (session.getConfig().getGameMode() != GameMode.NEO_RETRO) return;

        PlayerState me = session.getLocalPlayer();
        if (me.getAbilities() == null) {
            sonarBtn.setDisable(true);
            multiBtn.setDisable(true);
            empBtn.setDisable(true);
            return;
        }

        PlayerAbilities abilities = me.getAbilities();
        boolean locked = me.abilitiesLocked();
        boolean myTurn = session.isCurrentPlayerHuman() && !session.getState().isGameOver() && !isProcessing;

        AbilityStatus sonar = abilities.getStatus(AbilityType.SONAR);
        AbilityStatus multi = abilities.getStatus(AbilityType.MULTISHOT);
        AbilityStatus emp = abilities.getStatus(AbilityType.EMP);

        sonarBtn.setDisable(!myTurn || locked || sonar == null || !sonar.isAvailable());
        multiBtn.setDisable(!myTurn || locked || multi == null || !multi.isAvailable());
        empBtn.setDisable(!myTurn || locked || emp == null || !emp.isAvailable());
    }

    private void useSonar() {
        if (session == null || isProcessing) return;
        if (!session.isCurrentPlayerHuman()) return;
        if (session.getLocalPlayer().abilitiesLocked()) {
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
        if (session.getLocalPlayer().abilitiesLocked()) {
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
        if (session.getLocalPlayer().abilitiesLocked()) {
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

    @Override
    public Region getRoot() {
        return root;
    }

    private void maybeHighlightSonar(TurnAction action) {
        if (!(action instanceof UseAbilityAction abilityAction)) return;
        if (abilityAction.abilityType() != AbilityType.SONAR) return;
        AbilityTarget target = abilityAction.target();
        if (target == null || target.coordinate() == null) return;
        setSonarHighlights(target.coordinate());
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
