package com.chase.battleship.gui;

import com.chase.battleship.core.*;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class PlayingScreen extends BaseScreen {

    private static final int CELL_SIZE = 28;

    private final BorderPane root;
    private final GridPane myGrid;
    private final GridPane enemyGrid;
    private final Label turnLabel;
    private final Label actionLabel;
    private final VBox bottomBox;

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

        VBox myBox = new VBox(8, myLabel, myGrid);
        myBox.setAlignment(Pos.CENTER);

        VBox enemyBox = new VBox(8, enemyLabel, enemyGrid);
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
        session = manager.getCurrentSession();
        if (session == null) {
            turnLabel.setText("No active game");
            actionLabel.setText("");
            bottomBox.getChildren().setAll(actionLabel);
            return;
        }

        setupAbilityButtons();
        syncFromState();
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
        if (!session.isCurrentPlayerHuman()) return;
        if (session.getState().isGameOver()) return;

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
        isProcessing = true;
        TurnResult res = session.processHumanAction(action);
        if (res != null) {
            actionLabel.setText("You: " + res.message());
        }
        syncFromState();

        if (session.getState().isGameOver()) {
            handleGameOver();
            isProcessing = false;
            return;
        }

        if (!session.isCurrentPlayerHuman()) {
            // Let AI move after a short delay so the player can see the result.
            PauseTransition pause = new PauseTransition(Duration.millis(450));
            pause.setOnFinished(e -> {
                TurnResult aiRes = session.maybeLetAiAct();
                if (aiRes != null) {
                    actionLabel.setText("Enemy: " + aiRes.message());
                }
                syncFromState();
                if (session.getState().isGameOver()) {
                    handleGameOver();
                }
                isProcessing = false;
            });
            pause.play();
        } else {
            isProcessing = false;
        }
    }

    private void syncFromState() {
        if (session == null) return;

        GameState gs = session.getState();
        PlayerState current = gs.getCurrentPlayer();

        // For single-player we always show from Player 1's perspective.
        PlayerState me = session.getP1();
        PlayerState enemy = (me == session.getP1()) ? session.getP2() : session.getP1();

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

            CellState cs = tracking.getCellState(new Coordinate(row, col));
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

        PlayerState me = session.getP1(); // only human player in single-player
        if (me.getAbilities() == null) {
            sonarBtn.setDisable(true);
            multiBtn.setDisable(true);
            empBtn.setDisable(true);
            return;
        }

        PlayerAbilities abilities = me.getAbilities();

        AbilityStatus sonar = abilities.getStatus(AbilityType.SONAR);
        AbilityStatus multi = abilities.getStatus(AbilityType.MULTISHOT);
        AbilityStatus emp = abilities.getStatus(AbilityType.EMP);

        sonarBtn.setDisable(sonar == null || !sonar.isAvailable());
        multiBtn.setDisable(multi == null || !multi.isAvailable());
        empBtn.setDisable(emp == null || !emp.isAvailable());
    }

    private void useSonar() {
        if (session == null || isProcessing) return;
        if (!session.isCurrentPlayerHuman()) return;

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

        TurnAction action = new UseAbilityAction(
                AbilityType.EMP,
                new AbilityTarget(null, 0)
        );
        performHumanTurn(action);
    }

    private void handleGameOver() {
        GameState gs = session.getState();
        PlayerState winner = gs.getCurrentPlayer(); // GameEngine leaves winner as current

        if (winner == session.getP1()) {
            manager.show(ScreenId.WIN);
        } else {
            manager.show(ScreenId.LOSE);
        }
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
