package com.chase.battleship.gui;

import com.chase.battleship.core.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayingScreen extends BaseScreen {

    private static final int CELL_SIZE = 36;
    private static final Color WATER_COLOR = Color.web("#6f8f89"); // softened teal base
    private static final Color SHIP_TINT = Color.web("#8faea5");  // lighter teal for ships
    private static final Color MISS_COLOR = Color.web("#555555"); // medium grey
    private static final Color HIT_COLOR = Color.web("#9c5a3c");  // rust
    private static final double BASE_DELAY_MS = 650;
    private static Duration TURN_DELAY = Duration.millis(BASE_DELAY_MS);

    private final BorderPane root;
    private final GridPane myGrid;
    private final GridPane enemyGrid;
    private final Pane myShipOverlay;
    private final Pane myEffectOverlay;
    private final Pane enemyEffectOverlay;
    private final javafx.scene.shape.Rectangle enemyHoverRect = new javafx.scene.shape.Rectangle();
    private final javafx.scene.shape.Rectangle enemySelectRect = new javafx.scene.shape.Rectangle();
    private final StackPane myBoardStack;
    private final Label turnLabel;
    private final ImageView turnAvatar;
    private final Label actionLabel;
    private final Label fireHint;
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
    private final java.util.Map<Coordinate, CellState> lastMyBoardStates = new java.util.HashMap<>();
    private final java.util.Map<Coordinate, CellState> lastEnemySeenStates = new java.util.HashMap<>();
    private final java.util.Set<String> sunkMyShipsShown = new java.util.HashSet<>();
    private PauseTransition actionClear;

    private Button sonarBtn;
    private Button multiBtn;
    private Button empBtn;

    private GuiGameSession session;
    private boolean isProcessing = false;
    private boolean waitingForHandOff = false;
    private PlayerState lastTurnOwner = null;
    private boolean isLocalTwoP = false;
    private TurnAction lastAnimatedAction = null;
    private Coordinate pendingFireTarget = null;

    public PlayingScreen(ScreenManager manager) {
        super(manager);

        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(#0d3a55, #0a2f45);");
        root.setPadding(new Insets(20));

        turnLabel = new Label("Your turn");
        turnLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 22px;");

        turnAvatar = new ImageView(AssetLibrary.playerOne());
        turnAvatar.setFitHeight(34);
        turnAvatar.setFitWidth(34);
        turnAvatar.setPreserveRatio(true);
        turnAvatar.setSmooth(false);
        turnAvatar.setEffect(new DropShadow(8, Color.web("#061826")));

        actionLabel = new Label("");
        actionLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 17px;");
        actionLabel.setWrapText(false);
        actionLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        actionLabel.setMinWidth(0);
        actionLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        actionLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(actionLabel, Priority.ALWAYS);

        HBox topBar = new HBox(16, turnAvatar, turnLabel, actionLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setFillHeight(true);
        topBar.setMaxWidth(Double.MAX_VALUE);
        root.setTop(topBar);

        myGrid = createBoardGrid(false);
        myShipOverlay = new Pane();
        myShipOverlay.setMouseTransparent(true);
        double boardPixels = CELL_SIZE * 10 + myGrid.getHgap() * 9;
        myShipOverlay.setPrefSize(boardPixels, boardPixels);
        myEffectOverlay = new Pane();
        myEffectOverlay.setMouseTransparent(true);
        myEffectOverlay.setPickOnBounds(false);
        myEffectOverlay.setPrefSize(boardPixels, boardPixels);
        enemyHoverRect.setVisible(false);
        enemyHoverRect.setMouseTransparent(true);
        enemyHoverRect.setArcWidth(2);
        enemyHoverRect.setArcHeight(2);
        enemySelectRect.setVisible(false);
        enemySelectRect.setMouseTransparent(true);
        enemySelectRect.setArcWidth(2);
        enemySelectRect.setArcHeight(2);
        enemySelectRect.setStroke(Color.web("#8ae8ff"));
        enemySelectRect.setFill(Color.color(0.7, 1.0, 0.7, 0.35));
        enemySelectRect.setStrokeWidth(1.6);
        myBoardStack = new StackPane(myGrid, myEffectOverlay, myShipOverlay);
        myBoardStack.setPickOnBounds(false);
        enemyGrid = createBoardGrid(true);
        enemyEffectOverlay = new Pane();
        enemyEffectOverlay.setMouseTransparent(true);
        enemyEffectOverlay.setPickOnBounds(false);
        enemyEffectOverlay.setPrefSize(boardPixels, boardPixels);
        StackPane enemyBoardStack = new StackPane(enemyGrid, enemyHoverRect, enemySelectRect, enemyEffectOverlay);
        enemyBoardStack.setPickOnBounds(false);

        Label myLabel = new Label("Your Sea");
        myLabel.setStyle("-fx-text-fill: #f0f0f0;");

        Label enemyLabel = new Label("Enemy Sea");
        enemyLabel.setStyle("-fx-text-fill: #f0f0f0;");

        VBox myBox = new VBox(8, myLabel, wrapWithLabels(myBoardStack));
        myBox.setAlignment(Pos.CENTER);

        VBox enemyBox = new VBox(8, enemyLabel, wrapWithLabels(enemyBoardStack));
        enemyBox.setAlignment(Pos.CENTER);

        fleetTitle = new Label("Your Fleet");
        fleetTitle.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 16px;");
        fleetPanel = new VBox(10);
        fleetPanel.setAlignment(Pos.CENTER_LEFT);
        fleetPanel.setMinWidth(150);
        fleetPanel.getChildren().add(fleetTitle);

        HBox boardsBox = new HBox(80, myBox, enemyBox);
        boardsBox.setAlignment(Pos.CENTER);

        HBox mainRow = new HBox(48, fleetPanel, boardsBox);
        mainRow.setAlignment(Pos.CENTER);

        messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 14px;");
        messageLabel.setMaxWidth(520);
        messageLabel.setMinHeight(22);
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);

        hotbar = new HBox(10);
        hotbar.setAlignment(Pos.CENTER);

        fireHint = new Label("Click again to fire");
        fireHint.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 12px;");
        fireHint.setVisible(false);
        FadeTransition hintPulse = new FadeTransition(Duration.millis(900), fireHint);
        hintPulse.setFromValue(1.0);
        hintPulse.setToValue(0.4);
        hintPulse.setCycleCount(Animation.INDEFINITE);
        hintPulse.setAutoReverse(true);
        hintPulse.play();

        bottomBox = new VBox(10, hotbar, fireHint);
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
        Button controlsBtn = new Button("Controls");
        controlsBtn.setOnAction(e -> {
            toggleSettingsOverlay();
            manager.show(ScreenId.CONTROLS);
        });
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
                controlsBtn,
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
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                toggleSettingsOverlay();
                return;
            }
            if (settingsOverlay.isVisible() || waitingForHandOff) {
                return;
            }
            switch (e.getCode()) {
                case DIGIT1, NUMPAD1 -> useSonar();
                case DIGIT2, NUMPAD2 -> useMultishot();
                case DIGIT3, NUMPAD3 -> useEmp();
                default -> { }
            }
        });

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
        lastMyBoardStates.clear();
        lastEnemySeenStates.clear();
        sunkMyShipsShown.clear();
        myEffectOverlay.getChildren().clear();
        enemyEffectOverlay.getChildren().clear();
        lastAnimatedAction = null;
        pendingFireTarget = null;
        enemySelectRect.setVisible(false);
        session = manager.getCurrentSession();
        if (session == null) {
            turnLabel.setText("No active game");
            turnAvatar.setImage(AssetLibrary.playerOne());
            actionLabel.setTextFill(Color.web("#f0f0f0"));
            updateMessages("", "");
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
        grid.setHgap(3);
        grid.setVgap(3);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(enemy ? colorForEnemyCell(CellState.EMPTY) : colorForOwnCell(CellState.EMPTY));
                cell.setStroke(Color.web("#123547"));
                cell.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
                grid.add(cell, c, r);

                if (enemy) {
                    final int fr = r;
                    final int fc = c;
                    cell.setOnMouseClicked(e -> handleEnemyClick(fr, fc));
                    cell.setOnMouseEntered(e -> showEnemyHover(fr, fc));
                }
            }
        }
        if (enemy) {
            grid.setOnMouseExited(e -> clearEnemyHover());
        }
        return grid;
    }

    private void handleEnemyClick(int row, int col) {
        if (session == null || isProcessing) return;
        if (waitingForHandOff) return;
        if (enemyGrid.isDisable()) {
            actionLabel.setTextFill(Color.ORANGERED);
            updateMessages("Not your turn", "Not your turn");
            return;
        }
        updateEnemyHoverState(row, col);
        if (!session.isCurrentPlayerHuman()) {
            actionLabel.setTextFill(Color.ORANGERED);
            updateMessages("Not your turn", "Not your turn");
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

        Coordinate target = new Coordinate(row, col);
        if (pendingFireTarget != null && pendingFireTarget.equals(target)) {
            pendingFireTarget = null;
            enemySelectRect.setVisible(false);
            fireHint.setVisible(false);
            TurnAction action = new FireAction(target);
            performHumanTurn(action);
        } else {
            pendingFireTarget = target;
            showEnemySelection(target);
            fireHint.setVisible(true);
            updateMessages("Target " + (row + 1) + "," + (char)('A' + col), null);
        }
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
            updateMessages(formatActionMessage(action, res, true), null);
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
                TurnAction autoAction = session.getLastAction();
                boolean byLocal = session.wasLastActionByLocal();
                updateMessages(formatActionMessage(autoAction, autoRes, byLocal), null);
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
            actionLabel.setText("");
            pendingFireTarget = null;
            enemySelectRect.setVisible(false);
            fireHint.setVisible(false);
        }
        lastTurnOwner = current;

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

        java.util.List<ShotMark> newEnemyMarks = collectNewMarks(myTracking, lastEnemySeenStates);
        java.util.List<ShotMark> newMyMarks = collectNewMarks(myBoard, lastMyBoardStates);

        redrawOwnBoard(myBoard);
        redrawEnemyBoard(myTracking);
        refreshFleetPanel(me);
        animateSunkShips(myBoard);

        boolean currentHuman = session.isCurrentPlayerHuman();
        updateTurnBanner(current, me, currentHuman);
        enemyGrid.setDisable(!currentHuman || session.getState().isGameOver() || isProcessing || waitingForHandOff);

        refreshAbilityButtons();
        animateShots(newEnemyMarks, enemyEffectOverlay, enemyGrid, true);
        animateShots(newMyMarks, myEffectOverlay, myGrid, false);

        TurnAction newestAction = session.getLastAction();
        if (newestAction != null && !newestAction.equals(lastAnimatedAction)) {
            if (newestAction instanceof UseAbilityAction abilityAction) {
                if (abilityAction.abilityType() == AbilityType.SONAR) {
                    animateSonar(abilityAction.target() != null ? abilityAction.target().coordinate() : null, session.wasLastActionByLocal());
                }
                if (abilityAction.abilityType() == AbilityType.EMP) {
                    animateEmp();
                }
            }
            lastAnimatedAction = newestAction;
        }

        snapshotBoard(myBoard, lastMyBoardStates);
        snapshotBoard(myTracking, lastEnemySeenStates);
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
        renderOwnShipOverlay(board);
    }

    private void animateSunkShips(Board board) {
        if (board == null) return;
        double stepX = CELL_SIZE + myGrid.getHgap();
        double stepY = CELL_SIZE + myGrid.getVgap();
        for (Ship ship : board.getShips()) {
            if (!ship.isSunk()) continue;
            String key = ship.getType().name() + ship.getCoordinates();
            if (sunkMyShipsShown.contains(key)) continue;
            sunkMyShipsShown.add(key);

            int minRow = ship.getCoordinates().stream().mapToInt(Coordinate::row).min().orElse(0);
            int maxRow = ship.getCoordinates().stream().mapToInt(Coordinate::row).max().orElse(0);
            int minCol = ship.getCoordinates().stream().mapToInt(Coordinate::col).min().orElse(0);
            int maxCol = ship.getCoordinates().stream().mapToInt(Coordinate::col).max().orElse(0);

            double x = minCol * stepX;
            double y = minRow * stepY;
            double w = (maxCol - minCol + 1) * stepX - myGrid.getHgap();
            double h = (maxRow - minRow + 1) * stepY - myGrid.getVgap();

            Rectangle flash = new Rectangle(w, h, Color.color(1, 0.6, 0.6, 0.45));
            flash.setLayoutX(x);
            flash.setLayoutY(y);
            flash.setMouseTransparent(true);
            myEffectOverlay.getChildren().add(flash);

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(flash.opacityProperty(), 0.0)),
                    new KeyFrame(Duration.millis(150), new KeyValue(flash.opacityProperty(), 0.9)),
                    new KeyFrame(Duration.millis(520), new KeyValue(flash.opacityProperty(), 0.0))
            );
            tl.setOnFinished(e -> myEffectOverlay.getChildren().remove(flash));
            tl.play();
        }
    }

    private void renderOwnShipOverlay(Board board) {
        myShipOverlay.getChildren().clear();
        if (board == null) return;
        double stepX = CELL_SIZE + myGrid.getHgap();
        double stepY = CELL_SIZE + myGrid.getVgap();
        double insetX = myGrid.getHgap() / 2.0;
        double insetY = myGrid.getVgap() / 2.0;

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
                view.setFitWidth(len * CELL_SIZE + myGrid.getHgap() * (len - 1));
            } else {
                view.setFitWidth(CELL_SIZE);
                view.setFitHeight(len * CELL_SIZE + myGrid.getVgap() * (len - 1));
            }

            long hits = coords.stream().filter(c -> board.getCellState(c) == CellState.HIT).count();
            double opacity = ship.isSunk() ? 0.35 : (hits > 0 ? 0.7 : 0.9);
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
            myShipOverlay.getChildren().add(view);
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
            case HIT -> HIT_COLOR;
            case MISS -> MISS_COLOR;
            case SHIP -> SHIP_TINT;
            default -> WATER_COLOR;
        };
    }

    private Color colorForEnemyCell(CellState cs) {
        return switch (cs) {
            case HIT -> HIT_COLOR;
            case MISS -> MISS_COLOR;
            default -> WATER_COLOR;
        };
    }

    private void updateTurnBanner(PlayerState current, PlayerState me, boolean currentHuman) {
        if (current == null) {
            turnLabel.setText("No active game");
            turnAvatar.setImage(AssetLibrary.playerOne());
            return;
        }
        if (current == me && currentHuman && !waitingForHandOff) {
            turnLabel.setText("Your turn");
        } else if (!currentHuman) {
            turnLabel.setText("Opponent's turn");
        } else {
            turnLabel.setText(current.getName() + "'s turn");
        }
        turnAvatar.setImage(avatarFor(current));
    }

    private Image avatarFor(PlayerState player) {
        if (session == null || player == null) {
            return AssetLibrary.playerOne();
        }
        return player == session.getLocalPlayer() ? AssetLibrary.playerOne() : AssetLibrary.playerTwo();
    }

    private void setupAbilityButtons() {
        bottomBox.getChildren().clear();
        hotbar.getChildren().clear();

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
        if (!session.getConfig().getGameMode().isNeoRetro()) return;
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
        if (!session.getConfig().getGameMode().isNeoRetro()) return;
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
        if (!session.getConfig().getGameMode().isNeoRetro()) return;
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

    private String formatActionMessage(TurnAction action, TurnResult res, boolean byLocalActor) {
        String resMsg = res != null ? res.message() : "";
        String actor = byLocalActor ? "You" : "Opponent";
        if (action instanceof FireAction fire) {
            String outcome = parseOutcome(resMsg);
            return actor + " fired at " + coordLabel(fire.target()) + (outcome != null ? ": " + outcome : "");
        }
        if (action instanceof UseAbilityAction use) {
            return switch (use.abilityType()) {
                case MULTISHOT -> formatMultishotMessage(resMsg, actor);
                case EMP -> actor + " used EMP: enemy abilities disabled for 2 turns.";
                case SONAR -> formatSonarMessage(resMsg, actor);
                case SHIELD -> formatShieldMessage(resMsg, actor);
            };
        }
        return (actor + ": " + resMsg).trim();
    }

    private String formatMultishotMessage(String resMsg, String actor) {
        Pattern p = Pattern.compile("fired\\s+(\\d+)\\D+hits=(\\d+)(?:,\\s*ships sunk=(\\d+))?", Pattern.CASE_INSENSITIVE);
        Matcher m = resMsg != null ? p.matcher(resMsg) : null;
        if (m != null && m.find()) {
            int shots = safeInt(m.group(1));
            int hits = safeInt(m.group(2));
            int sunk = m.group(3) != null ? safeInt(m.group(3)) : 0;
            int misses = shots >= 0 && hits >= 0 ? Math.max(0, shots - hits) : 0;
            StringBuilder sb = new StringBuilder(actor + " used Multishot: ");
            if (shots > 0) {
                sb.append(shots).append(" shots (").append(hits).append(" hit, ").append(misses).append(" miss");
                if (misses != 1) sb.append("es");
                sb.append(")");
            } else {
                sb.append(resMsg);
            }
            if (sunk > 0) {
                sb.append(", sank ").append(sunk).append(sunk == 1 ? " ship" : " ships");
            }
            return sb.toString();
        }
        return actor + " used Multishot" + (resMsg == null || resMsg.isBlank() ? "" : (": " + resMsg));
    }

    private String formatSonarMessage(String resMsg, String actor) {
        int row = -1, col = -1, hits = -1;
        Pattern centerPat = Pattern.compile("Sonar scan at \\((\\d+),(\\d+)\\)", Pattern.CASE_INSENSITIVE);
        Pattern hitsPat = Pattern.compile("detected\\s+(\\d+)\\s+ship", Pattern.CASE_INSENSITIVE);
        if (resMsg != null) {
            Matcher c = centerPat.matcher(resMsg);
            if (c.find()) {
                row = safeInt(c.group(1));
                col = safeInt(c.group(2));
            }
            Matcher h = hitsPat.matcher(resMsg);
            if (h.find()) {
                hits = safeInt(h.group(1));
            } else if (resMsg.toLowerCase().contains("no ship segments")) {
                hits = 0;
            }
        }
        StringBuilder sb = new StringBuilder(actor + " used Sonar");
        if (row >= 0 && col >= 0) {
            sb.append(" at ").append(coordLabel(new Coordinate(row, col)));
        }
        if (hits >= 0) {
            sb.append(": detected ").append(hits).append(hits == 1 ? " segment" : " segments");
        } else if (resMsg != null && !resMsg.isBlank()) {
            sb.append(": ").append(resMsg);
        }
        return sb.toString();
    }

    private String formatShieldMessage(String resMsg, String actor) {
        if (resMsg == null) return actor + " used Shield";
        Pattern p = Pattern.compile("Shield applied to\\s+(\\w+)\\s+at\\s+(\\d+),(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(resMsg);
        if (m.find()) {
            String ship = m.group(1);
            int r = safeInt(m.group(2));
            int c = safeInt(m.group(3));
            return actor + " used Shield on " + ship + " at " + coordLabel(new Coordinate(r, c));
        }
        return actor + " used Shield: " + resMsg;
    }

    private String parseOutcome(String resMsg) {
        if (resMsg == null) return null;
        Matcher m = Pattern.compile("=>\\s*([^\\n]+)").matcher(resMsg);
        return m.find() ? m.group(1).trim() : resMsg;
    }

    private String coordLabel(Coordinate coord) {
        char colLetter = (char) ('A' + coord.col());
        return colLetter + String.valueOf(coord.row() + 1);
    }

    private int safeInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return -1;
        }
    }

    private void updateMessages(String actionText, String simpleText) {
        if (actionText != null) {
            if (actionClear != null) {
                actionClear.stop();
            }
            FxAnimations.stopMarquee(actionLabel);
            FxAnimations.typewriter(actionLabel, actionText, Duration.millis(1400));
            actionClear = new PauseTransition(Duration.seconds(6));
            actionClear.setOnFinished(e -> actionLabel.setText(""));
            actionClear.playFromStart();
            Platform.runLater(() -> {
                double visibleWidth = actionLabel.getWidth() > 0 ? actionLabel.getWidth() : 900;
                FxAnimations.marqueeIfNeeded(actionLabel, visibleWidth);
            });
        }
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

    private void showEnemyHover(int row, int col) {
        updateEnemyHoverState(row, col);
        enemyHoverRect.setWidth(CELL_SIZE);
        enemyHoverRect.setHeight(CELL_SIZE);
    }

    private void clearEnemyHover() {
        enemyHoverRect.setVisible(false);
    }

    private void updateEnemyHoverState(int row, int col) {
        if (session == null || enemyGrid.isDisable()) {
            enemyHoverRect.setVisible(false);
            return;
        }
        PlayerState me = session.getState().getCurrentPlayer();
        Board tracking = me.getTrackingBoard();
        CellState state = tracking.getCellState(new Coordinate(row, col));
        boolean canFire = state == CellState.EMPTY && session.isCurrentPlayerHuman() && !waitingForHandOff;

        double stepX = CELL_SIZE + enemyGrid.getHgap();
        double stepY = CELL_SIZE + enemyGrid.getVgap();
        enemyHoverRect.setWidth(CELL_SIZE);
        enemyHoverRect.setHeight(CELL_SIZE);
        enemyHoverRect.setLayoutX(col * stepX);
        enemyHoverRect.setLayoutY(row * stepY);
        enemyHoverRect.setFill(canFire ? Color.color(0.7, 1.0, 0.7, 0.35) : Color.color(1.0, 0.3, 0.3, 0.28));
        enemyHoverRect.setVisible(true);
    }

    private void showEnemySelection(Coordinate coord) {
        double stepX = CELL_SIZE + enemyGrid.getHgap();
        double stepY = CELL_SIZE + enemyGrid.getVgap();
        enemySelectRect.setWidth(CELL_SIZE);
        enemySelectRect.setHeight(CELL_SIZE);
        enemySelectRect.setLayoutX(coord.col() * stepX);
        enemySelectRect.setLayoutY(coord.row() * stepY);
        enemySelectRect.setOpacity(0.85);
        enemySelectRect.setVisible(true);
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
            int hits = 0;
            if (ship != null) {
                for (Coordinate coord : ship.getCoordinates()) {
                    if (own.getCellState(coord) == CellState.HIT) {
                        hits++;
                    }
                }
            }
            boolean sunk = ship == null || ship.isSunk();
            double damageRatio = hits / (double) type.getLength();

            ImageView art = new ImageView(AssetLibrary.shipIcon(type));
            art.setPreserveRatio(true);
            art.setSmooth(false);
            art.setFitHeight(CELL_SIZE * 0.9);
            art.setFitWidth(CELL_SIZE * type.getLength());
            art.setOpacity(sunk ? 0.32 : (damageRatio > 0 ? 0.82 : 1.0));
            art.setEffect(new DropShadow(8, Color.web("#061826")));

            StackPane artCard = new StackPane(art);
            artCard.setPadding(new Insets(6));
            artCard.setStyle("-fx-background-color: rgba(12, 52, 72, 0.55); -fx-border-color: rgba(53, 240, 255, 0.55); -fx-border-radius: 6; -fx-background-radius: 6;");

            Label name = new Label(type.name());
            name.setTextFill(Color.web("#e6f4ff"));
            name.setStyle("-fx-font-size: 10px;");
            Label status = new Label(sunk ? "SUNK" : (damageRatio > 0 ? "HIT" : "READY"));
            status.setTextFill(sunk ? Color.web("#ff8fa0") : (damageRatio > 0 ? Color.web("#ffe599") : Color.web("#9ff7ff")));
            status.setStyle("-fx-font-size: 9px;");
            status.setWrapText(true);
            status.setMaxWidth(CELL_SIZE * 2.0);

            VBox textCol = new VBox(2, name, status);
            HBox row = new HBox(10, artCard, textCol);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setOpacity(sunk ? 0.45 : 1.0);
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

    private double[] cellOrigin(GridPane grid, Pane overlay, Coordinate coord) {
        for (Node node : grid.getChildren()) {
            if (!(node instanceof Rectangle)) continue;
            Integer cIdx = GridPane.getColumnIndex(node);
            Integer rIdx = GridPane.getRowIndex(node);
            int c = cIdx == null ? 0 : cIdx;
            int r = rIdx == null ? 0 : rIdx;
            if (r == coord.row() && c == coord.col()) {
                var sceneBounds = node.localToScene(node.getBoundsInLocal());
                var local = overlay.sceneToLocal(sceneBounds);
                return new double[]{local.getMinX(), local.getMinY()};
            }
        }
        double stepX = CELL_SIZE + grid.getHgap();
        double stepY = CELL_SIZE + grid.getVgap();
        return new double[]{coord.col() * stepX, coord.row() * stepY};
    }

    private GridPane wrapWithLabels(javafx.scene.Node grid) {
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

    private record ShotMark(Coordinate coord, boolean hit) {}

    private java.util.List<ShotMark> collectNewMarks(Board board, java.util.Map<Coordinate, CellState> previous) {
        java.util.List<ShotMark> events = new java.util.ArrayList<>();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Coordinate coord = new Coordinate(r, c);
                CellState current = board.getCellState(coord);
                CellState prev = previous.getOrDefault(coord, CellState.EMPTY);
                if (current != prev && (current == CellState.HIT || current == CellState.MISS)) {
                    events.add(new ShotMark(coord, current == CellState.HIT));
                }
            }
        }
        return events;
    }

    private void animateShots(java.util.List<ShotMark> marks, Pane overlay, GridPane grid, boolean enemyBoard) {
        if (marks.isEmpty()) return;
        TurnAction newestAction = session != null ? session.getLastAction() : null;
        boolean lastByLocal = session != null && session.wasLastActionByLocal();
        boolean isMulti = enemyBoard
                && newestAction instanceof UseAbilityAction use
                && use.abilityType() == AbilityType.MULTISHOT
                && lastByLocal;
        boolean stagger = isMulti;

        int idx = 0;
        for (ShotMark mark : marks) {
            int delay = stagger ? idx * 160 : 0;
            if (isMulti) {
                Color pulseColor = mark.hit() ? HIT_COLOR : Color.web("#6b968e");
                playSquarePop(mark.coord(), overlay, grid, delay, pulseColor);
            } else if (mark.hit()) {
                playHitEffect(mark.coord(), overlay, grid, delay);
            } else {
                playMissEffect(mark.coord(), overlay, grid, delay);
            }
            idx++;
        }
    }

    private void playHitEffect(Coordinate coord, Pane overlay, GridPane grid, int delayMs) {
        double[] pos = cellOrigin(grid, overlay, coord);
        double x = pos[0];
        double y = pos[1];

        double boardW = CELL_SIZE * 10 + grid.getHgap() * 9;
        double boardH = CELL_SIZE * 10 + grid.getVgap() * 9;

        Rectangle row = new Rectangle(boardW, CELL_SIZE);
        row.setFill(Color.color(0.9, 0.12, 0.12, 0.32));
        row.setMouseTransparent(true);
        row.setLayoutY(y);

        Rectangle col = new Rectangle(CELL_SIZE, boardH);
        col.setFill(Color.color(0.9, 0.12, 0.12, 0.32));
        col.setMouseTransparent(true);
        col.setLayoutX(x);

        overlay.getChildren().addAll(row, col);

        FadeTransition rowFade = new FadeTransition(Duration.millis(360), row);
        rowFade.setToValue(0);
        FadeTransition colFade = new FadeTransition(Duration.millis(360), col);
        colFade.setToValue(0);

        SequentialTransition seq = new SequentialTransition(
                new PauseTransition(Duration.millis(delayMs)),
                new ParallelTransition(rowFade, colFade)
        );
        seq.setOnFinished(e -> overlay.getChildren().removeAll(row, col));
        seq.play();
    }

    private void playMissEffect(Coordinate coord, Pane overlay, GridPane grid, int delayMs) {
        double[] pos = cellOrigin(grid, overlay, coord);
        double x = pos[0];
        double y = pos[1];

        Rectangle splash = new Rectangle(CELL_SIZE, CELL_SIZE, Color.rgb(230, 240, 255, 0.8));
        splash.setLayoutX(x);
        splash.setLayoutY(y);
        splash.setMouseTransparent(true);
        splash.setArcWidth(6);
        splash.setArcHeight(6);

        overlay.getChildren().add(splash);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(splash.opacityProperty(), 0.0),
                        new KeyValue(splash.scaleXProperty(), 0.65),
                        new KeyValue(splash.scaleYProperty(), 0.65)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(splash.opacityProperty(), 0.85),
                        new KeyValue(splash.scaleXProperty(), 1.05),
                        new KeyValue(splash.scaleYProperty(), 1.05)),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(splash.opacityProperty(), 0.0),
                        new KeyValue(splash.scaleXProperty(), 1.12),
                        new KeyValue(splash.scaleYProperty(), 1.12))
        );
        tl.setDelay(Duration.millis(delayMs));
        tl.setOnFinished(e -> overlay.getChildren().remove(splash));
        tl.play();
    }

    private void playSquarePop(Coordinate coord, Pane overlay, GridPane grid, int delayMs, Color fill) {
        double[] pos = cellOrigin(grid, overlay, coord);
        double x = pos[0];
        double y = pos[1];

        Rectangle block = new Rectangle(CELL_SIZE, CELL_SIZE, fill.deriveColor(0, 1, 1, 0.8));
        block.setLayoutX(x);
        block.setLayoutY(y);
        block.setMouseTransparent(true);
        block.setArcWidth(6);
        block.setArcHeight(6);
        overlay.getChildren().add(block);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(block.opacityProperty(), 0.0),
                        new KeyValue(block.scaleXProperty(), 0.65),
                        new KeyValue(block.scaleYProperty(), 0.65)),
                new KeyFrame(Duration.millis(160),
                        new KeyValue(block.opacityProperty(), 0.9),
                        new KeyValue(block.scaleXProperty(), 1.08),
                        new KeyValue(block.scaleYProperty(), 1.08)),
                new KeyFrame(Duration.millis(320),
                        new KeyValue(block.opacityProperty(), 0.6),
                        new KeyValue(block.scaleXProperty(), 0.96),
                        new KeyValue(block.scaleYProperty(), 0.96)),
                new KeyFrame(Duration.millis(520),
                        new KeyValue(block.opacityProperty(), 0.0),
                        new KeyValue(block.scaleXProperty(), 1.12),
                        new KeyValue(block.scaleYProperty(), 1.12))
        );
        tl.setDelay(Duration.millis(delayMs));
        tl.setOnFinished(e -> overlay.getChildren().remove(block));
        tl.play();
    }

    private void animateSonar(Coordinate center, boolean byLocalPlayer) {
        Coordinate c = center != null ? center : new Coordinate(4, 4);
        if (c.row() < 0 || c.col() < 0 || c.row() >= 10 || c.col() >= 10) {
            return;
        }
        java.util.List<Coordinate> centerOnly = java.util.List.of(c);
        java.util.List<Coordinate> ring = new java.util.ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                ring.add(new Coordinate(c.row() + dr, c.col() + dc));
            }
        }
        int cycleMs = 900;
        Pane overlay = byLocalPlayer ? enemyEffectOverlay : myEffectOverlay;
        GridPane grid = byLocalPlayer ? enemyGrid : myGrid;

        for (int i = 0; i < 3; i++) {
            int cycleDelay = i * cycleMs;
            flashCells(overlay, grid, centerOnly, Color.web("#ffe566"), cycleMs, cycleDelay);
            flashCells(overlay, grid, ring, Color.web("#f8d34a"), cycleMs, cycleDelay + 220);
        }
    }

    private void animateEmp() {
        int rows = session != null ? session.getConfig().getRows() : 10;
        int cols = session != null ? session.getConfig().getCols() : 10;
        int centerR = rows / 2;
        int centerC = cols / 2;
        int maxDist = centerR + centerC;
        java.util.Map<Integer, java.util.List<Coordinate>> rings = new java.util.HashMap<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int dist = Math.abs(r - centerR) + Math.abs(c - centerC);
                rings.computeIfAbsent(dist, k -> new java.util.ArrayList<>()).add(new Coordinate(r, c));
            }
        }
        for (int dist = 0; dist <= maxDist; dist++) {
            java.util.List<Coordinate> layer = rings.get(dist);
            if (layer == null) continue;
            int delay = dist * 80;
            flashCells(enemyEffectOverlay, enemyGrid, layer, Color.web("#c4c2c4"), 1050, delay);
            flashCells(enemyEffectOverlay, enemyGrid, layer, Color.web("#8faea5"), 980, delay + 160);
        }

        // pixel flicker near center for EMP static
        double centerX = enemyEffectOverlay.getPrefWidth() / 2.0;
        double centerY = enemyEffectOverlay.getPrefHeight() / 2.0;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 10; i++) {
            Rectangle spark = new Rectangle(4, 4, Color.color(0.8, 0.95, 1.0, 0.8));
            spark.setLayoutX(centerX + rng.nextDouble(-CELL_SIZE, CELL_SIZE));
            spark.setLayoutY(centerY + rng.nextDouble(-CELL_SIZE, CELL_SIZE));
            enemyEffectOverlay.getChildren().add(spark);

            FadeTransition sparkFade = new FadeTransition(Duration.millis(220 + rng.nextInt(120)), spark);
            sparkFade.setFromValue(0.9);
            sparkFade.setToValue(0);
            sparkFade.setDelay(Duration.millis(50 * i));
            sparkFade.setOnFinished(e -> enemyEffectOverlay.getChildren().remove(spark));
            sparkFade.play();
        }
    }

    private void flashCells(Pane overlay, GridPane grid, java.util.List<Coordinate> coords, Color highlight, int totalMs, int delayMs) {
        if (coords == null || coords.isEmpty()) return;
        int rows = session != null ? session.getConfig().getRows() : 10;
        int cols = session != null ? session.getConfig().getCols() : 10;

        for (Coordinate c : coords) {
            if (c.row() < 0 || c.col() < 0 || c.row() >= rows || c.col() >= cols) continue;
            double[] pos = cellOrigin(grid, overlay, c);
            Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE, highlight);
            rect.setOpacity(0.0);
            rect.setMouseTransparent(true);
            rect.setScaleX(0.92);
            rect.setScaleY(0.92);
            rect.setLayoutX(pos[0]);
            rect.setLayoutY(pos[1]);
            overlay.getChildren().add(rect);

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(rect.opacityProperty(), 0.0),
                            new KeyValue(rect.scaleXProperty(), 0.92),
                            new KeyValue(rect.scaleYProperty(), 0.92)),
                    new KeyFrame(Duration.millis(Math.max(80, totalMs * 0.24)),
                            new KeyValue(rect.opacityProperty(), 0.85),
                            new KeyValue(rect.scaleXProperty(), 1.08),
                            new KeyValue(rect.scaleYProperty(), 1.08)),
                    new KeyFrame(Duration.millis(Math.max(140, totalMs * 0.6)),
                            new KeyValue(rect.opacityProperty(), 0.35),
                            new KeyValue(rect.scaleXProperty(), 0.97),
                            new KeyValue(rect.scaleYProperty(), 0.97)),
                    new KeyFrame(Duration.millis(totalMs),
                            new KeyValue(rect.opacityProperty(), 0.0),
                            new KeyValue(rect.scaleXProperty(), 1.05),
                            new KeyValue(rect.scaleYProperty(), 1.05))
            );
            tl.setDelay(Duration.millis(delayMs));
            tl.setOnFinished(e -> overlay.getChildren().remove(rect));
            tl.play();
        }
    }

    private void snapshotBoard(Board board, java.util.Map<Coordinate, CellState> target) {
        target.clear();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Coordinate coord = new Coordinate(r, c);
                target.put(coord, board.getCellState(coord));
            }
        }
    }
}
